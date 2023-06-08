package com.alibaba.nacos.frame;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.handler.codec.http.HttpMethod;
import com.alibaba.nacos.util.ParamsUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.alibaba.nacos.util.HttpRequestUtils.request;

public class ResourceInit {
    private static final Logger log = LoggerFactory.getLogger(ResourceInit.class);
    /**
     * Read the configuration file to use
     */
    public static SystemConfigFileHelper configFileHelper = new SystemConfigFileHelper();
    /**
     * Access point Information
     */
    private static Properties properties = null;
    protected static String serverList = null;
    protected static String allIp = null;
    protected static List<String> serverIpList = new ArrayList<>();
    protected static String mode = null;
    protected static String namespace = null;
    protected static String namespace1 = null;
    protected static String namespace2 = null;
    protected static Boolean aclEnable = null;
    protected static Boolean tlsEnable = null;
    protected static String accessKey = null;
    protected static String secretKey = null;
    protected static String nacosClientVersion = null;
    protected static String nacosServerVersion = null;

    static {
        initResource();
        log.info("Init process success");
    }

    private static void initResource() {
        try {
            String env = System.getProperty("env", "daily");
            String region = System.getProperty("region", "daily");
            InputStream inputStream = new FileInputStream(String.format("src/test/resources/env/%s/%s.conf", env, region));
            log.info("INIT - use config env:{}, config:{}", env, region);
            properties = configFileHelper.loadConfig(inputStream);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            log.info("INIT - execute error,exit the process");
            System.exit(0);
        }

        initConnectionInfo();
        initAcl();
        getNacosClientVersion();
        getNacosServerVersion();
    }

    private static void initConnectionInfo() {
        // pod_name:pod_ip,pod_name1:pod_ip1
        allIp = System.getenv("ALL_IP");
        mode = System.getenv("mode") == null ?
            System.getProperty("mode", properties.getProperty("mode", "serverAddr")):
            System.getenv("mode");
        if (allIp != null) {
            String[] allPodInfos = allIp.split(",");
            for (String podInfo : allPodInfos) {
                serverIpList.add(podInfo.substring(podInfo.indexOf(":") + 1));
            }
            if (serverIpList.isEmpty()) {
                log.warn("INIT- Get serverList from external is empty");
                serverList = System.getProperty("serverList", properties.getProperty("serverList"));
            } else {
                String tempServerList = "";
                if ("serverAddr".equals(mode)) {
                    for (String server : serverIpList) {
                        if (!server.contains(":8848")) {
                            tempServerList += server + ":8848" + ",";
                        } else {
                            tempServerList += server + ",";
                        }
                    }
                } else {
                    for (String server : serverIpList) {
                        if (server.contains(":8848")) {
                            tempServerList += server.split(":")[0] + ",";
                        } else {
                            tempServerList += server + ",";
                        }
                    }
                }
                serverList = tempServerList.endsWith(",") ? tempServerList.substring(0,
                    tempServerList.length()-1) : tempServerList;
            }
        } else {
            log.info("INIT- Get ALL_IP is null, use local info");
            serverList = System.getProperty("serverList", properties.getProperty("serverList"));
        }
        if (serverList == null) {
            log.error("INIT- Get serverList is null");
            System.exit(-1);
        }
        namespace = System.getenv("namespace") == null ?
            System.getProperty("namespace", properties.getProperty("namespace", "")) :
            System.getenv("namespace");
        namespace1 = System.getenv("namespace1") == null ?
            System.getProperty("namespace1", properties.getProperty("namespace1", "test1")) :
            System.getenv("namespace1");
        namespace2 = System.getenv("namespace2") == null ?
            System.getProperty("namespace2", properties.getProperty("namespace2", "test2")) :
            System.getenv("namespace2");
        //make namespace/namespace1/namespace2 be existed
        String[] servers = serverList.split(",");
        String server0 = servers[0].endsWith(":8848") ? servers[0] : servers[0]+":8848";
        makeNsExist(server0);
        log.info("INIT- serverList:{}, mode:{}, namespace:{}", serverList, mode, namespace);
    }

    private static void initConnectionInfo_old() {
        serverList = System.getenv("serverList") == null ? properties.getProperty("serverList") : System.getenv("serverList");
        mode = System.getenv("mode") == null ? properties.getProperty("mode") : System.getenv("mode");
        namespace = System.getenv("namespace") == null ? properties.getProperty("namespace") : System.getenv("namespace");
        namespace1 = System.getenv("namespace1") == null ? properties.getProperty("namespace1") : System.getenv("namespace1");
        namespace2 = System.getenv("namespace2") == null ? properties.getProperty("namespace2") : System.getenv("namespace2");

        // use mode join right serverList
        if (StringUtils.isNotBlank(serverList)) {
            String[] servers = serverList.split(",");
            //make namespace/namespace1/namespace2 be existed
            String server0 = servers[0].endsWith(":8848") ? servers[0] : servers[0]+":8848";
            makeNsExist(server0);
            String tempServerList = "";
            if ("serverAddr".equals(mode)) {
                for (String server : servers) {
                    if (!server.contains(":8848")) {
                        tempServerList += server + ":8848" + ",";
                    } else {
                        tempServerList += server + ",";
                    }
                }
            } else {
                for (String server : servers) {
                    if (serverList.contains(":8848")) {
                        tempServerList += server.split(":")[0] + ",";
                    } else {
                        tempServerList += server + ",";
                    }
                }
            }
            serverList = tempServerList.endsWith(",") ? tempServerList.substring(0,
                tempServerList.length()-1) : tempServerList;
        } else {
            log.info("INIT- Get serverList is null");
        }
        log.info("INIT- serverList:{}, mode:{}, namespace:{}", serverList, mode, namespace);
    }

    private static void initAcl() {
        aclEnable = Boolean.parseBoolean(System.getenv("aclEnable") == null ?
            System.getProperty("aclEnable", properties.getProperty("aclEnable", "false")) :
            System.getenv("aclEnable"));
        tlsEnable = Boolean.parseBoolean(System.getenv("tlsEnable") == null ?
            System.getProperty("tlsEnable", properties.getProperty("tlsEnable", "false")) :
            System.getenv("tlsEnable"));
        if (aclEnable) {
            log.info("INIT - acl is enabled");
            accessKey = System.getenv("ACCESS_KEY") == null ?
                System.getProperty("ACCESS_KEY", properties.getProperty("ACCESS_KEY")):
                System.getenv("ACCESS_KEY");
            secretKey = System.getenv("SECRET_KEY") == null ?
                System.getProperty("SECRET_KEY", properties.getProperty("SECRET_KEY")) :
                System.getenv("SECRET_KEY");
        } else {
            log.info("INIT - acl is disabled");
        }
    }

    private static void getNacosClientVersion() {
        String key = "nacos.client.version";
        nacosClientVersion = System.getenv(key);
        if (StringUtils.isBlank(nacosClientVersion)) {
            nacosClientVersion = getPomProperties(key, "pom.xml");
        }
        if (StringUtils.isBlank(nacosClientVersion)) {
            nacosClientVersion = getPomProperties(key, "java/nacos-2X/pom.xml");
        }
        log.info("nacosClientVersion is " + nacosClientVersion);
    }

    private static void getNacosServerVersion() {
        try {
            String[] servers = serverList.split(",");
            if (servers.length > 0) {
                String url = String.format("http://%s", servers[0]);
                HttpRestResult getResult = getOperatorServers(url);
                log.info("getResult = "+ getResult);
                if (getResult.getCode() == 200) {
                    JSONObject json = JSON.parseObject(getResult.getData().toString());
                    nacosServerVersion = json.getString("version");
                    //JSONArray array = json.getJSONArray("servers");
                    //if (array.size() > 0 && array.getJSONObject(0).containsKey("extendInfo")) {
                    //    nacosServerVersion =
                    //        array.getJSONObject(0).getJSONObject("extendInfo").getString("version");
                    //}
                } else {
                    log.info("getNacosServerVersion return " + JSON.toJSONString(getResult));
                }
            } else {
                log.info("INIT- Get server0 is null");
            }
        } catch (Exception e) {
            log.error("getNacosServerVersion Exception", e);
        }
        log.info("nacosServerVersion is " + nacosServerVersion);
    }

    public static class SystemConfigFileHelper {
        private String file;

        public SystemConfigFileHelper() {
        }

        public Properties loadConfig() throws Exception {
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            Properties properties = new Properties();
            properties.load(in);
            in.close();
            return properties;
        }

        public Properties loadConfig(InputStream inputStream) throws Exception {
            InputStream in = new BufferedInputStream(inputStream);
            Properties properties = new Properties();
            properties.load(in);
            in.close();
            return properties;
        }

        public void update(Properties properties) throws Exception {
            log.error("[SystemConfigFileHelper] update no thing.");
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getFile() {
            return file;
        }
    }

    public static boolean telnet(String hostname, int port, int timeout) {
        Socket socket = new Socket();
        boolean isConnected = false;
        try {
            socket.connect(new InetSocketAddress(hostname, port), timeout);
            isConnected = socket.isConnected();
        } catch (IOException e) {
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
        return isConnected;
    }

    private static void makeNsExist(String server0) {
        try {
            Boolean haveNs = false;
            Boolean haveNs1 = false;
            Boolean haveNs2 = false;
            String url = String.format("http://%s", server0);
            HttpRestResult listResult = listNamespaceV1(url);
            log.info("listResult :" + listResult);
            if (listResult.getCode() == 200 && listResult.getData() != null) {
                JSONObject resultJson = JSON.parseObject(listResult.getData().toString());
                JSONArray listArray = resultJson.getJSONArray("data");
                for (int i = 0; i < listArray.size(); i++) {
                    JSONObject json = listArray.getJSONObject(i);
                    if (namespace.equals(json.getString("namespace"))){
                        haveNs = true;
                    }
                    if (namespace1.equals(json.getString("namespace"))){
                        haveNs1 = true;
                    }
                    if (namespace2.equals(json.getString("namespace"))){
                        haveNs2 = true;
                    }
                }
            }

            if (!haveNs) {
                HttpRestResult makeResult = makeNamespaceV1(url, namespace, namespace);
                log.info("ns=" + namespace + ",makeResult=" + makeResult);
            }
            if (!haveNs1) {
                HttpRestResult makeResult = makeNamespaceV1(url, namespace1, namespace1);
                log.info("ns=" + namespace1 + ",makeResult=" + makeResult);
            }
            if (!haveNs2) {
                HttpRestResult makeResult = makeNamespaceV1(url, namespace2, namespace2);
                log.info("ns=" + namespace2 + ",makeResult=" + makeResult);
            }

        } catch (Exception e) {
            log.error("makeNsExist Exception", e);
        }
    }

    // /nacos/v1/ns/operator/servers  will be removed
    public static HttpRestResult getOperatorServers(String url) throws Exception{
        HttpRestResult httpResult =
            request(url + "/nacos/v1/console/server/state",
                Collections.<String>emptyList(),
                ParamsUtils.newParams()
                    .done(), StringUtils.EMPTY, "UTF-8", String.valueOf(HttpMethod.GET));
        return  httpResult;
    }

    public static HttpRestResult listNamespaceV1(String url) throws Exception{
        HttpRestResult httpResult =
            request(url + "/nacos/v1/console/namespaces",
                Collections.<String>emptyList(),
                ParamsUtils.newParams()
                    .done(), StringUtils.EMPTY, "UTF-8", String.valueOf(HttpMethod.GET));
        return  httpResult;
    }

    public static HttpRestResult makeNamespaceV1(String url, String namespaceId, String namespaceName) throws Exception{
        HttpRestResult httpResult =
            request(url + "/nacos/v1/console/namespaces",
                Collections.<String>emptyList(),
                ParamsUtils.newParams()
                    .appendParam("customNamespaceId", namespaceId)
                    .appendParam("namespaceName", namespaceName)
                    .done(), StringUtils.EMPTY, "UTF-8", String.valueOf(HttpMethod.POST));
        return  httpResult;
    }

    //{"timestamp":"2023-05-06T14:00:01.857+0800","status":404,"error":"Not Found","message":"No message available","path":"/nacos/v2/console/namespace/list"}
    public static HttpRestResult listNamespaceV2(String url) throws Exception{
        HttpRestResult httpResult =
            request(url + "/nacos/v2/console/namespace/list",
                Collections.<String>emptyList(),
                ParamsUtils.newParams()
                    .done(), StringUtils.EMPTY, "UTF-8", String.valueOf(HttpMethod.GET));
        return  httpResult;
    }

    public static HttpRestResult makeNamespaceV2(String url, String namespaceId, String namespaceName) throws Exception{
        HttpRestResult httpResult =
            request(url + "/nacos/v2/console/namespace",
                Collections.<String>emptyList(),
                ParamsUtils.newParams()
                    .appendParam("namespaceId", namespaceId)
                    .appendParam("namespaceName", namespaceName)
                    .done(), StringUtils.EMPTY, "UTF-8", String.valueOf(HttpMethod.POST));
        return  httpResult;
    }

    private static String getPomProperties(String key, String filePath){
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            Document doc = dbf.newDocumentBuilder().parse(new File(filePath));
            Element root = doc.getDocumentElement();
            NodeList properties = root.getElementsByTagName("properties");
            Element prop = (Element) properties.item(0);
            NodeList nodes = prop.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                if (nodes.item(i) instanceof Element) {
                    Element el = (Element) nodes.item(i);
                    String tagName = el.getTagName();
                    String textContent = el.getTextContent();
                    if (key.equals(tagName)) {
                        return textContent;
                    }
                }
            }
        } catch (Exception e) {
            log.error("getPomProperties Exception", e);
        }
        return null;
    }
}

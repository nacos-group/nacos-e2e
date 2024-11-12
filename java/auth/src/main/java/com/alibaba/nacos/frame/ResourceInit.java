package com.alibaba.nacos.frame;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.util.OkHttpUtils;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
    protected static String regionId = null;
    protected static String instanceId = null;
    protected static String namespace = null;
    protected static String namespace1 = null;
    protected static String namespace2 = null;
    protected static Boolean aclEnable = null;
    protected static Boolean tlsEnable = null;
    protected static String username = null;
    protected static String password = null;
    protected static String accessToken = null;
    protected static String nacosClientVersion = null;
    protected static String nacosServerVersion = null;

    static {
        try {
            initResource();
        } catch (Exception e) {
            log.error("initResource exception", e);
        }
        log.info("Init process success");
    }

    private static void initResource() throws Exception {
        try {
            String env = System.getProperty("env", "daily");
            String region = System.getProperty("region", "daily");
            //InputStream inputStream = new FileInputStream(String.format("src/test/resources/env/%s/%s.conf", env, region));
            InputStream inputStream = ResourceInit.class.getResourceAsStream(String.format("/env/%s/%s.conf", env, region));
            log.info("INIT - use config env:{}, config:{}", env, region);
            properties = configFileHelper.loadConfig(inputStream);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            log.info("INIT - execute error,exit the process");
            System.exit(0);
        }

        initConnectionInfo();
        initAcl();
        makeNsExist();
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
                if (podInfo.startsWith("nacos-")) {
                    serverIpList.add(podInfo.substring(podInfo.indexOf(":") + 1));
                }
            }
            if (serverIpList.isEmpty()) {
                log.warn("INIT- Get serverList from external is empty");
                serverList = System.getenv("serverList") == null ?
                        System.getProperty("serverList", properties.getProperty("serverList")) :
                        System.getenv("serverList");
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
            serverList = System.getenv("serverList") == null ?
                    System.getProperty("serverList", properties.getProperty("serverList")) :
                    System.getenv("serverList");
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
        log.info("INIT- serverList:{}, mode:{}, regionId:{}, instanceId:{}, namespace:{}, namespace1:{}, "
                + "namespace2:{} ", serverList, mode, regionId, instanceId, namespace, namespace1, namespace2);
    }

    private static void initAcl() throws Exception {
        aclEnable = Boolean.parseBoolean(System.getenv("aclEnable") == null ?
            System.getProperty("aclEnable", properties.getProperty("aclEnable", "true")) :
            System.getenv("aclEnable"));
        tlsEnable = Boolean.parseBoolean(System.getenv("tlsEnable") == null ?
            System.getProperty("tlsEnable", properties.getProperty("tlsEnable", "false")) :
            System.getenv("tlsEnable"));
        if (aclEnable) {
            log.info("INIT - acl is enabled");
            username = System.getenv("username") == null ?
                System.getProperty("username", properties.getProperty("username")):
                System.getenv("username");
            password = System.getenv("password") == null ?
                System.getProperty("password", properties.getProperty("password")) :
                System.getenv("password");
            String[] servers = serverList.split(",");
            String url = String.format("http://%s", servers[0]);
            // make admin user when not exist
            makeNacosV1AuthUserAdmin(url, password);
            accessToken = doNacosV1AuthLogin(url, username, password);

            log.info("INIT- username:{}, password:{} ", username, password);
        } else {
            log.info("INIT - acl is disabled");
        }
    }

    private static void getNacosClientVersion() {
        String key = "nacos.client.version";
        nacosClientVersion = System.getenv(key) == null ? System.getProperty(key) : System.getenv(key);
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
                String getResult = getOperatorServers(url);
                if (StringUtils.isNotBlank(getResult)) {
                    log.info("getResult = "+ JSON.toJSONString(getResult));
                    JSONObject json = JSON.parseObject(getResult);
                    nacosServerVersion = json.getString("version");
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

    private static void makeNsExist() {
        //make namespace/namespace1/namespace2 be existed
        String[] servers = serverList.split(",");
        String server0 = servers[0].endsWith(":8848") ? servers[0] : servers[0]+":8848";
        try {
            Boolean haveNs = false;
            Boolean haveNs1 = false;
            Boolean haveNs2 = false;
            String url = String.format("http://%s", server0);
            String listResult = listNamespaceV1(url);
            log.info("listResult :" + listResult);
            if (StringUtils.isNotBlank(listResult)) {
                JSONObject resultJson = JSON.parseObject(listResult);
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
                String makeResult = makeNamespaceV1(url, namespace, namespace);
                log.info("ns=" + namespace + ",makeResult=" + makeResult);
            }
            if (!haveNs1) {
                String makeResult = makeNamespaceV1(url, namespace1, namespace1);
                log.info("ns=" + namespace1 + ",makeResult=" + makeResult);
            }
            if (!haveNs2) {
                String makeResult = makeNamespaceV1(url, namespace2, namespace2);
                log.info("ns=" + namespace2 + ",makeResult=" + makeResult);
            }

        } catch (Exception e) {
            log.error("makeNsExist Exception", e);
        }
    }

    public static Boolean makeNacosV1AuthUserAdmin(String url, String password) throws Exception{
        url = url + "/nacos/v1/auth/users/admin";

        Map<String, String> params = new HashMap<>();
        params.put("password", password);
        String result = OkHttpUtils.post(url, params);
        log.info("makeNacosV1AuthUserAdmin result:{}", result);
        if (StringUtils.isNotBlank(result) && result.contains("nacos") ) {
            return true;
        }
        return false;
    }

    public static String doNacosV1AuthLogin(String url, String username, String password) throws Exception{
        url = url + "/nacos/v1/auth/login";

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        String result = OkHttpUtils.post(url, params);
        if (StringUtils.isNotBlank(result)) {
            if (result.contains("token")) {
                log.info("doNacosV1AuthLogin success, result:{}", result);
                JSONObject jsonObject = JSONObject.parseObject(result);
                accessToken = jsonObject.getString("accessToken");
                return accessToken;
            } else {
                log.info("doNacosV1AuthLogin error, result:{}", result);
            }
        }
        return null;
    }

    // /nacos/v1/ns/operator/servers  will be removed
    public static String getOperatorServers(String url) throws Exception{
        url = url + "/nacos/v1/console/server/state";
        Map<String, String> params = new HashMap<>();
        if (aclEnable) {
            params.put("accessToken", accessToken);
        }
        String result = OkHttpUtils.get(url, params);
        return  result;
    }

    public static String listNamespaceV1(String url) throws Exception{
        url = url + "/nacos/v1/console/namespaces";
        Map<String, String> params = new HashMap<>();
        if (aclEnable) {
            params.put("accessToken", accessToken);
        }
        String result = OkHttpUtils.get(url, params);
        return  result;
    }

    public static String makeNamespaceV1(String url, String namespaceId, String namespaceName) throws Exception{
        url = url + "/nacos/v1/console/namespaces";
        Map<String, String> params = new HashMap<>();
        if (aclEnable) {
            params.put("accessToken", accessToken);
        }
        params.put("customNamespaceId", namespaceId);
        params.put("namespaceName", namespaceName);

        String result = OkHttpUtils.post(url, params);

        return  result;
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

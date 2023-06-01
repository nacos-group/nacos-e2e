package com.alibaba.nacos.naming;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.net.HttpClient;
import com.alibaba.nacos.common.utils.HttpMethod;
import com.alibaba.nacos.frame.BaseOperate;
import com.alibaba.nacos.util.ParamsUtils;
import com.alibaba.nacos.util.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.client.naming.net.HttpClient.request;

public class NamingBase extends BaseOperate {
    private static final Logger log = LoggerFactory.getLogger(NamingBase.class);
    public static final long TIME_OUT = 5;
    public static final int TEST_PORT = 8848;
    public static final int TEST_PORT_8080 = 8080;
    public static final String TEST_PORT2_4_DOM_1 = "8888";
    public static final String TEST_PORT3_4_DOM_1 = "80";
    public static final String TEST_GROUP = "group";
    public static final String TEST_GROUP_1 = "group1";
    public static final String TEST_GROUP_2 = "group2";
    public static final String TEST_IP_4_DOM_1 = "127.0.0.1";
    public static final String TEST_IP_4_DOM_2 = "127.0.0.2";
    public static final String TEST_SERVER_STATUS = "UP";
    public static final String TEST_NEW_CLUSTER_4_DOM_1 = "TEST1";
    public static String SPECIAL_CHARACTERS = "！#￥%……6*（）测试u《》~(){}【】”‘：；。，.,/?";
    public static final String APP_FIELD_KEY = "preserved.vs.app.name";

    public static NamingService naming;
    public static URL base;

    static {
        try {
            naming = NacosFactory.createNamingService(properties);
            String[] serverArr = serverList.split(",");
            String server0 = serverArr[0].endsWith(":8848") ? serverArr[0] : serverArr[0]+":8848";
            String url = String.format("http://%s", server0);
            base = new URL(url);
        } catch (Exception e) {
            log.error("NamingBase init NamingService Exception", e);
        }
    }

    public static void verifyInstanceList(List<Instance> instances, int size, String serviceName) throws Exception {
        int i = 0;
        while (i < 30) {
            instances = naming.getAllInstances(serviceName);
            if (instances.size() == size) {
                break;
            } else {
                TimeUnit.SECONDS.sleep(3);
                i++;
            }
        }
    }

    public static boolean verifyInstanceList(List<Instance> instanceList1, List<Instance> instanceList2) {
        Map<String, Instance> instanceMap = new HashMap<String, Instance>();
        for (Instance instance : instanceList1) {
            instanceMap.put(instance.getIp(), instance);
        }

        Map<String, Instance> instanceGetMap = new HashMap<String, Instance>();
        for (Instance instance : instanceList2) {
            instanceGetMap.put(instance.getIp(), instance);
        }

        for (String ip : instanceMap.keySet()) {
            if (!instanceGetMap.containsKey(ip)) {
                return false;
            }
            if (!verifyInstance(instanceMap.get(ip), instanceGetMap.get(ip))) {
                return false;
            }
        }
        return true;
    }

    public static void verifyInstanceList(List<Instance> expectedInstances, String serviceName) throws Exception {
        int i = 0;
        while (i < 20) {
            List<Instance> actualInstances = naming.getAllInstances(serviceName);

            if (expectedInstances.size() == actualInstances.size()) {
                verifyInstanceList(expectedInstances,actualInstances);
                break;
            } else {
                TimeUnit.SECONDS.sleep(3);
                i++;
            }
            if (i == 20) {
                Assertions.assertFalse(true, "verifyInstanceList error: time out");
            }
        }
    }

    public static boolean containsMetadata(Instance instance, String key) {
        return instance.getMetadata() != null && !instance.getMetadata().isEmpty() ? instance.getMetadata().containsKey(key) : false;
    }

    public static boolean verifyInstance(Instance i1, Instance i2) {

        if (!i1.getIp().equals(i2.getIp()) || i1.getPort() != i2.getPort() ||
            i1.getWeight() != i2.getWeight() || i1.isHealthy() != i2.isHealthy()) {
            return false;
        }

        // Adapt for edas after 2.1.0.2 version, metaData will add app key return.
        if (Boolean.parseBoolean(System.getenv("naming.check.metadata.app.field") == null?
            "false" : System.getenv("naming.check.metadata.app.field"))) {
            if (!containsMetadata(i1, APP_FIELD_KEY) && !containsMetadata(i2, APP_FIELD_KEY)) {
                return false;
            }
        }
        // check metadata after remove APP_FIELD_KEY
        i1.getMetadata().remove(APP_FIELD_KEY);
        i2.getMetadata().remove(APP_FIELD_KEY);
        return i1.getMetadata().equals(i2.getMetadata());

        //Service service1 = i1.getService();
        //Service service2 = i2.getService();
        //
        //if (!service1.getApp().equals(service2.getApp()) || !service1.getGroup().equals(service2.getGroup()) ||
        //    !service1.getMetadata().equals(service2.getMetadata()) || !service1.getName().equals(service2.getName()) ||
        //    service1.getProtectThreshold() != service2.getProtectThreshold() ||
        //    service1.isEnableClientBeat() != service2.isEnableClientBeat() ||
        //    service1.isEnableHealthCheck() != service2.isEnableHealthCheck()) {
        //    return false;
        //}

        //Cluster cluster1 = i1.getCluster();
        //Cluster cluster2 = i2.getCluster();
        //
        //if (!cluster1.getName().equals(cluster2.getName()) ||
        //    cluster1.getDefaultCheckPort() != cluster2.getDefaultCheckPort() ||
        //    cluster1.getDefaultPort() != cluster2.getDefaultPort() ||
        //    !cluster1.getServiceName().equals(cluster2.getServiceName()) ||
        //    !cluster1.getMetadata().equals(cluster2.getMetadata())||
        //    cluster1.isUseIPPort4Check() != cluster2.isUseIPPort4Check()) {
        //    return false;
        //}
        //
        //HealthChecker healthChecker1 = cluster1.getHealthChecker();
        //HealthChecker healthChecker2 = cluster2.getHealthChecker();
        //
        //if (healthChecker1.getClass().getName() != healthChecker2.getClass().getName()) {
        //    return false;
        //}
        //
        //if (healthChecker1 instanceof HealthChecker.Http) {
        //    HealthChecker.Http h1 = (HealthChecker.Http) healthChecker1;
        //    HealthChecker.Http h2 = (HealthChecker.Http) healthChecker2;
        //
        //    if (h1.getExpectedResponseCode() != h2.getExpectedResponseCode() ||
        //        !h1.getHeaders().equals(h2.getHeaders()) ||
        //        !h1.getPath().equals(h2.getPath()) ||
        //        !h1.getCustomHeaders().equals(h2.getCustomHeaders())) {
        //        return false;
        //    }
        //}
    }

    public static void verifyInstance(Instance instance, int size, String serviceName) throws Exception {

        int i = 0;
        while (i < 20) {
            List<Instance> actualInstance = naming.getAllInstances(serviceName);
            if (actualInstance.size() == size) {
                // Adapt for edas, get instances after 2.1.0.2 version, metaData will add app key return.
                Instance actual = actualInstance.get(0);
                if (Boolean.parseBoolean(System.getenv("naming.check.metadata.app.field")==null?
                    "false":System.getenv("naming.check.metadata.app.field"))) {
                    if (!instance.getMetadata().containsKey(APP_FIELD_KEY) && !actual.getMetadata().containsKey(APP_FIELD_KEY)) {
                        Assertions.fail(String.format("%s not contain in metadata", APP_FIELD_KEY));
                    }
                }
                actual.getMetadata().remove(APP_FIELD_KEY);
                Assertions.assertEquals(instance, actual, "received data instance error: expected instance:{" + instance + "},but actual {"
                    + actual + "}");
                break;
            } else {
                TimeUnit.SECONDS.sleep(3);
                i++;
            }
            if (i == 20) {
                Assertions.assertFalse(true, "serviceName="+serviceName +", verifyInstance error: "
                    + "time out, expectSize="+size + ", actualSize="+actualInstance.size() +
                    ", expectInstance="+JSON.toJSONString(instance) +
                    ", actualInstance="+JSON.toJSONString(actualInstance));
            }
        }
    }

    public static void verifyInstanceListForNaming(NamingService naming, int size,
        String serviceName) throws Exception {
        int i = 0;
        while (i < 20) {
            List<Instance> instances = naming.getAllInstances(serviceName);
            if (instances.size() == size) {
                break;
            } else {
                TimeUnit.SECONDS.sleep(3);
                i++;
            }
            if (i == 10) {
                Assertions.assertFalse(true, "time out");
            }
        }
    }

    public static void verifyInstanceListForNaming(NamingService naming, List<Instance> expectedInstances, String serviceName) throws Exception {
        int i = 0;
        while (i < 10) {
            List<Instance> actualInstances = naming.getAllInstances(serviceName);
            if (actualInstances.size() == expectedInstances.size()) {
                verifyInstanceList(expectedInstances,actualInstances);
                break;
            } else {
                TimeUnit.SECONDS.sleep(3);
                i++;
            }
            if (i == 10) {
                Assertions.assertFalse(true, "time out");
            }
        }
    }

    public static String randomDomainName() {
        StringBuilder sb = new StringBuilder();
        sb.append("nacos.");
        for (int i = 0; i < 2; i++) {
            sb.append(RandomUtils.getStringWithCharacter(5));
            sb.append(".");
        }
        int i = RandomUtils.getIntegerBetween(0, 2);
        if (i == 0) {
            sb.append("com");
        } else {
            sb.append("net");
        }
        return sb.toString();
    }

    public static Instance getInstance(String serviceName) {
        Instance instance = new Instance();
        instance.setIp("127.0.0.1");
        instance.setPort(TEST_PORT);
        instance.setHealthy(true);
        instance.setWeight(2.0);
        Map<String, String> instanceMeta = new HashMap<String, String>();
        instanceMeta.put("site", "et2");
        instance.setMetadata(instanceMeta);

        instance.setServiceName(serviceName);
        instance.setClusterName("c1");

        return instance;
    }

    public static Instance getInstance(String serviceName,String ip) throws Exception {
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(8848);
        instance.setClusterName("DEFAULT");
        instance.setServiceName("DEFAULT_GROUP@@" + serviceName);
        instance.setInstanceId(ip + "#8848#DEFAULT#DEFAULT_GROUP@@" + serviceName);
        return instance;
    }

    public static List<Instance> getInstancesList(String serviceName) throws Exception {
        List<Instance> instances = new ArrayList<Instance>();
        instances.add(getInstance(serviceName,TEST_IP_4_DOM_1));
        instances.add(getInstance(serviceName,TEST_IP_4_DOM_2));
        return instances;
    }

    public static List<Instance> getInstancesClustersList(String serviceName) throws Exception {
        List<Instance> instances = new ArrayList<Instance>();
        instances.add(getInstanceAndCluster(serviceName,TEST_IP_4_DOM_1,"c1"));
        instances.add(getInstanceAndCluster(serviceName,TEST_IP_4_DOM_2,"c2"));
        return instances;
    }

    public static List<Instance> getInstancesClustersNaming1(String serviceName) throws Exception {
        List<Instance> instances = new ArrayList<Instance>();
        instances.add(getInstanceAndCluster(serviceName, "11.11.11.11", "c1"));
        instances.add(getInstanceAndCluster(serviceName, "22.22.22.22", "c2"));
        return instances;
    }

    public static List<Instance> getInstancesClustersNaming2(String serviceName) throws Exception {
        List<Instance> instances = new ArrayList<Instance>();
        instances.add(getInstanceAndCluster(serviceName, "22.22.22.22", "c2"));
        return instances;
    }

    public static Instance getInstanceAndCluster(String serviceName,String ip,String clusterName) throws Exception {
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(8848);
        instance.setServiceName("DEFAULT_GROUP@@" + serviceName);
        instance.setClusterName(clusterName);
        instance.setInstanceId(ip + "#8848#"+clusterName + "#DEFAULT_GROUP@@" + serviceName);
        return instance;
    }

    public static Instance getExpectedInstance(String ip, int port, String cluster, double weight,
        boolean healthy) {
        Instance result = new Instance();
        result.setIp(ip);
        result.setPort(port);
        result.setWeight(weight);
        result.setClusterName(cluster);
        result.setHealthy(healthy);
        return result;
    }

    public HttpClient.HttpResult deleteService(String serviceName, String namespace) throws Exception {
        return deleteService(serviceName, namespace, Constants.DEFAULT_GROUP);
    }
    public HttpClient.HttpResult deleteService(String serviceName, String namespace, String groupName) throws Exception {
        //删除service
        HttpClient.HttpResult httpResult =
            request(base.toString() + "/nacos/v1/ns/service",
                Collections.<String>emptyList(),
                ParamsUtils.newParams()
                    .appendParam("serviceName", serviceName)
                    .appendParam("namespaceId", namespace)
                    .appendParam("groupName", groupName)
                    .done(), StringUtils.EMPTY, "UTF-8", String.valueOf(HttpMethod.DELETE));
        return  httpResult;
    }

    public void namingServiceCreate(String serviceName, String namespace) throws Exception {
        namingServiceCreate(serviceName, namespace, Constants.DEFAULT_GROUP);
    }

    public void namingServiceCreate(String serviceName, String namespace,
        String groupName) throws Exception {
        List<String> listParams = Arrays.asList("serviceName", serviceName, "protectThreshold", "0.3", "namespaceId", namespace, "groupName",
            groupName);
        log.info("namingServiceCreate params=" + JSON.toJSONString(listParams));
        HttpClient.HttpResult httpResult = request(base.toString() + "/nacos/v1/ns/service",
            Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("protectThreshold", "0.3")
                .appendParam("namespaceId", namespace)
                .appendParam("groupName", groupName)
                .done(), StringUtils.EMPTY, "UTF-8", HttpMethod.POST);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.code);
    }

    public void instanceRegister(String serviceName, String namespace, String ip, String port) throws Exception {
        instanceRegister(serviceName, namespace, Constants.DEFAULT_GROUP, ip, port, Constants.DEFAULT_CLUSTER_NAME);
    }

    public void instanceRegister(String serviceName, String namespace, String groupName,
        String ip, String port) throws Exception {
        instanceRegister(serviceName, namespace, groupName, ip, port, Constants.DEFAULT_CLUSTER_NAME);
    }

    public void instanceRegister(String serviceName, String namespace, String groupName,
        String ip, String port, String clusterName) throws Exception {
        HttpClient.HttpResult httpResult = request(base.toString() + "/nacos/v1/ns/instance", Collections.<String>emptyList(),
            ParamsUtils.newParams().appendParam("serviceName", serviceName).appendParam("ip", ip)
                .appendParam("port", port)
                .appendParam("namespaceId", namespace)
                .appendParam("groupName", groupName)
                .appendParam("clusterName", clusterName)
                .appendParam("ephemeral", "false")
                .done(), StringUtils.EMPTY, "UTF-8", HttpMethod.POST);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.code);
    }

    public void instanceDeregister(String serviceName, String namespace, String ip,
        String port) throws Exception {
        instanceDeregister(serviceName, namespace, Constants.DEFAULT_GROUP, ip, port);
    }

    public void instanceDeregister(String serviceName, String namespace, String groupName,
        String ip, String port) throws Exception {
        List<String> listParams = Arrays.asList("serviceName", serviceName, "ip", ip, "port", port, "namespaceId", namespace, "groupName",
                groupName);
        log.info("deregister params=" + JSON.toJSONString(listParams));
        HttpClient.HttpResult httpResult = request(base.toString() + "/nacos/v1/ns/instance", Collections.<String>emptyList(),
            ParamsUtils.newParams().appendParam("serviceName", serviceName)
                .appendParam("namespaceId", namespace)
                .appendParam("ip", ip)
                .appendParam("port", port)
                .appendParam("groupName", groupName)
                .appendParam("ephemeral", "false")
                .done(), StringUtils.EMPTY, "UTF-8", HttpMethod.DELETE);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.code);
    }
}

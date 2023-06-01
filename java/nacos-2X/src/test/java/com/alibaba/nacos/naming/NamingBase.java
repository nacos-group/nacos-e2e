package com.alibaba.nacos.naming;

import com.alibaba.nacos.api.NacosFactory;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.frame.BaseOperate;
import com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.handler.codec.http.HttpMethod;
import com.alibaba.nacos.util.ParamsUtils;
import com.alibaba.nacos.util.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.util.HttpRequestUtils.request;

public class NamingBase extends BaseOperate {
    private static final Logger log = LoggerFactory.getLogger(NamingBase.class);
    public static final long TIME_OUT = 5;
    public static final int TEST_PORT = 8848;
    public static final int TEST_PORT_8080 = 8080;
    public static final String TEST_GROUP = "group";
    public static final String TEST_GROUP_1 = "group1";
    public static final String TEST_GROUP_2 = "group2";
    public static final String TEST_IP_4_DOM_1 = "127.0.0.1";
    public static final String TEST_IP_4_DOM_2 = "127.0.0.2";
    public static final String TEST_SERVER_STATUS = "UP";
    public static final String TEST_NEW_CLUSTER_4_DOM_1 = "TEST1";
    public static String SPECIAL_CHARACTERS = "！#￥%……6*（）测试u《》~(){}【】”‘：；。，.,/?";

    public static NamingService naming;
    public static URL base;

    @BeforeAll
    public static void setUpAll() throws Exception {
        naming = NacosFactory.createNamingService(properties);
        String[] serverArr = serverList.split(",");
        String url = String.format("http://%s", serverArr[0]);
        base = new URL(url);
    }

    @AfterAll
    public static void tearDownAll() throws Exception {

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

    public static boolean verifyInstance(Instance i1, Instance i2) {

        if (!i1.getIp().equals(i2.getIp()) || i1.getPort() != i2.getPort() ||
            i1.getWeight() != i2.getWeight() || i1.isHealthy() != i2.isHealthy() ||
            !i1.getMetadata().equals(i2.getMetadata())) {
            return false;
        }

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
        return true;
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



    public HttpRestResult createService(String namespace, String serviceName, String metadata) throws Exception{
        //创建service
        HttpRestResult httpResult = request(this.base.toString() + "/nacos/v2/ns/service/" + serviceName ,
            Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("namespaceId", namespace)
                .appendParam("metadata", metadata)
                .appendParam("groupName", Constants.DEFAULT_GROUP)
                .done(), StringUtils.EMPTY, "UTF-8",
            String.valueOf(HttpMethod.POST));
        return  httpResult;
    }

    public HttpRestResult modifyService(String namespace, String serviceName, String metadata) throws Exception{
        //修改service
        HttpRestResult httpResult = request(this.base.toString() + "/nacos/v2/ns/service/" + serviceName,
            Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("metadata", metadata)
                .appendParam("namespaceId", namespace)
                .appendParam("groupName", Constants.DEFAULT_GROUP)
                .done(), StringUtils.EMPTY, "UTF-8", String.valueOf(HttpMethod.PUT));
        return  httpResult;
    }

    public HttpRestResult listService(String namespace) throws Exception {
        //查询service
        HttpRestResult httpResult = request(this.base.toString() + "/nacos/v2/ns/service/list",
            Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("namespaceId", namespace)
                .appendParam("pageNo", "1")
                .appendParam("pageSize", "100000")
                .appendParam("selector", "{\"type\":\"none\",\"contextType\":\"NONE\"}")
                .done(), StringUtils.EMPTY, "UTF-8", String.valueOf(HttpMethod.GET));
        return  httpResult;
    }

    public HttpRestResult getService(String namespace, String serviceName, String clusterDetail) throws Exception {
        //查询service
        Map<String, String> params = new HashMap<String, String>();
        if (StringUtils.isBlank(clusterDetail)) {
            HttpRestResult httpResult = request(this.base.toString() + "/nacos/v2/ns/service/" + serviceName,
                Collections.<String>emptyList(),
                ParamsUtils.newParams()
                    .appendParam("namespaceId", namespace)
                    .appendParam("serviceName", serviceName)
                    .done(), StringUtils.EMPTY, "UTF-8", String.valueOf(HttpMethod.GET));
            return  httpResult;
        } else {
            HttpRestResult httpResult = request(this.base.toString() + "/nacos/v2/ns/service/" + serviceName,
                Collections.<String>emptyList(),
                ParamsUtils.newParams()
                    .appendParam("namespaceId", namespace)
                    .appendParam("serviceName", serviceName)
                    .appendParam("clusterDetail", clusterDetail)
                    .done(), StringUtils.EMPTY, "UTF-8", String.valueOf(HttpMethod.GET));
            return  httpResult;
        }
    }

    public HttpRestResult deleteService(String namespace, String serviceName) throws Exception {
        //删除service
        HttpRestResult httpResult =
            request(this.base.toString() + "/nacos/v2/ns/service/" + serviceName,
                Collections.<String>emptyList(),
                ParamsUtils.newParams()
                    .appendParam("serviceName", serviceName)
                    .appendParam("namespaceId", namespace)
                    .appendParam("groupName", Constants.DEFAULT_GROUP)
                    .done(), StringUtils.EMPTY, "UTF-8", String.valueOf(HttpMethod.DELETE));
        return  httpResult;
    }

    public HttpRestResult updateCluster(String namespace, String serviceName,
        String checkPort,
        String useInstancePort4Check, String healthChecker) throws Exception{
        //更新健康检查
        HttpRestResult httpResult =
            request(this.base.toString() + "/nacos/v2/ns/cluster",
                Collections.<String>emptyList(),
                ParamsUtils.newParams()
                    .appendParam("namespaceId", namespace)
                    .appendParam("serviceName", serviceName)
                    .appendParam("checkPort", checkPort)
                    .appendParam("useInstancePort4Check", useInstancePort4Check)
                    .appendParam("healthChecker", healthChecker)
                    .appendParam("groupName", Constants.DEFAULT_GROUP)
                    .appendParam("clusterName", Constants.DEFAULT_CLUSTER_NAME)
                    .done(), StringUtils.EMPTY, "UTF-8", String.valueOf(HttpMethod.PUT));
        return  httpResult;
    }

    public HttpRestResult getInstanceList(String namespace, String serviceName) throws Exception{
        //更新健康检查
        HttpRestResult httpResult =
            request(this.base.toString() + "/nacos/v2/ns/instance/list",
                Collections.<String>emptyList(),
                ParamsUtils.newParams()
                    .appendParam("namespaceId", namespace)
                    .appendParam("serviceName", serviceName)
                    .appendParam("groupName", Constants.DEFAULT_GROUP)
                    .appendParam("clusterName", Constants.DEFAULT_CLUSTER_NAME)
                    .done(), StringUtils.EMPTY, "UTF-8", String.valueOf(HttpMethod.GET));
        return  httpResult;
    }

    public HttpRestResult getInstanceDetail(String namespace, String serviceName,
        String ip, String port) throws Exception{
        //更新健康检查
        HttpRestResult httpResult =
            request(this.base.toString() + "/nacos/v2/ns/instance\n" + "\n",
                Collections.<String>emptyList(),
                ParamsUtils.newParams()
                    .appendParam("namespaceId", namespace)
                    .appendParam("serviceName", serviceName)
                    .appendParam("ip", ip)
                    .appendParam("port", port)
                    .appendParam("groupName", Constants.DEFAULT_GROUP)
                    .appendParam("clusterName", Constants.DEFAULT_CLUSTER_NAME)
                    .done(), StringUtils.EMPTY, "UTF-8", String.valueOf(HttpMethod.GET));
        return  httpResult;
    }
}

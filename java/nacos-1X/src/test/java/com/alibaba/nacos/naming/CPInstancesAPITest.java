package com.alibaba.nacos.naming;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.utils.HttpMethod;
import com.alibaba.nacos.enums.TESTSET;
import com.alibaba.nacos.util.ParamsUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.util.HttpRequestUtils.request;

@Tag(TESTSET.NAMING)
public class CPInstancesAPITest extends NamingBase {
    private static final Logger log = LoggerFactory.getLogger(CPInstancesAPITest.class);
    private String serviceName;
    private Instance noPersistInstance;

    private static NamingService naming1;
    private static NamingService naming2;

    @BeforeAll
    public static void setUpAll() throws Exception {
        naming1 = NacosFactory.createNamingService(properties1);
        naming2 = NacosFactory.createNamingService(properties2);
    }

    @BeforeEach
    public void setUp() throws Exception{
        serviceName = randomDomainName();

    }

    @AfterEach
    public void cleanup() throws Exception {
        if (noPersistInstance != null) {
            try {
                naming1.deregisterInstance(serviceName, TEST_GROUP_1, noPersistInstance);
                TimeUnit.SECONDS.sleep(12);
                deleteService(serviceName, namespace1, TEST_GROUP_1);
            }catch (Exception e){

            }
        }

    }

    @Test
    @DisplayName("Register instance set ephemeral is true.")
    public void testRegisterInstance_ephemeral_true() throws Exception {

        Instance instance = new Instance();
        instance.setEphemeral(true);
        instance.setClusterName("c1");
        instance.setIp("11.11.11.11");
        instance.setPort(80);
        naming1.registerInstance(serviceName, TEST_GROUP_1, instance);
        TimeUnit.SECONDS.sleep(5);

        List<Instance> instances = naming1.getAllInstances(serviceName, TEST_GROUP_1);
        Assertions.assertEquals(1, instances.size());
        Assertions.assertEquals(true, instances.get(0).isEphemeral());
    }
    
    @Test
    @DisplayName("Register instance set ephemeral is false before deregisterInstance.")
    public void testRegisterInstance_ephemeral_false() throws Exception {

        noPersistInstance = new Instance();
        noPersistInstance.setEphemeral(false);
        noPersistInstance.setClusterName("c1");
        noPersistInstance.setIp("11.11.11.11");
        noPersistInstance.setPort(80);
        naming1.registerInstance(serviceName, TEST_GROUP_1, noPersistInstance);
        TimeUnit.SECONDS.sleep(5);

        List<Instance> instances = naming1.getAllInstances(serviceName, TEST_GROUP_1);
        Assertions.assertEquals(1, instances.size());
        Assertions.assertEquals(false, instances.get(0).isEphemeral());

        deleteService(serviceName, namespace1, TEST_GROUP_1);
    }
    
    @Test
    @DisplayName("Register instance set ephemeral is false after deregisterInstance.")
    public void testRegisterInstance_ephemeral_false_deregisterInstance() throws Exception {
        namingServiceCreate(serviceName, namespace1, TEST_GROUP_1);

        noPersistInstance = new Instance();
        noPersistInstance.setEphemeral(false);  
        noPersistInstance.setClusterName("c1");
        noPersistInstance.setIp("11.11.11.11");
        noPersistInstance.setPort(80);
        naming1.registerInstance(serviceName, TEST_GROUP_1, noPersistInstance);
        TimeUnit.SECONDS.sleep(5);
        naming1.deregisterInstance(serviceName, TEST_GROUP_1, noPersistInstance);
        TimeUnit.SECONDS.sleep(12);
        deleteService(serviceName, namespace1, TEST_GROUP_1);
        noPersistInstance = null;

    }
    
    @Test
    @DisplayName("Register and delete service has no instance in default group.")
    public void testCreateService_noInstance() throws Exception {
        namingServiceCreate(serviceName, namespace1);
        TimeUnit.SECONDS.sleep(3L);
        deleteService(serviceName, namespace1);
    }
    
    @Test
    @DisplayName("Register instance set ephemeral false in service, if instance exist can't be deleted.")
    public void testDeleteService_hasInstance() throws Exception {
        namingServiceCreate(serviceName, namespace1);
        log.info(serviceName);
        HttpRestResult httpResult = request(this.base.toString() + "/nacos/v1/ns/instance", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("ip", "11.11.11.11")
                .appendParam("port", "80")
                .appendParam("namespaceId", namespace1)
                .appendParam("ephemeral", "false")
                .done(), StringUtils.EMPTY, "UTF-8", HttpMethod.POST);

        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        TimeUnit.SECONDS.sleep(5L);

        //the service have instances can't be deleted
        HttpRestResult deleteResult = deleteService(serviceName, namespace1);
        Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, deleteResult.getCode());

        //the service deregister the instances can be deleted
        noPersistInstance = new Instance();
        noPersistInstance.setEphemeral(false);
        noPersistInstance.setClusterName("DEFAULT_GROUP");
        noPersistInstance.setIp("11.11.11.11");
        noPersistInstance.setPort(80);
        naming1.deregisterInstance(serviceName,noPersistInstance);
        TimeUnit.SECONDS.sleep(12);
        deleteResult = deleteService(serviceName, namespace1,"DEFAULT_GROUP");
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, deleteResult.getCode());

        noPersistInstance=null;
    }
    
    @Test
    @DisplayName("Register instance set ephemeral false in service, and change the instance, the " 
        + "get the instance detail.")
    public void testGetService() throws Exception {
        log.info(serviceName);
        namingServiceCreate(serviceName, namespace1);
        TimeUnit.SECONDS.sleep(3);
        HttpRestResult httpResult = request(this.base.toString() + "/nacos/v1/ns/service", Collections.<String>emptyList(),
                ParamsUtils.newParams()
                        .appendParam("serviceName", serviceName)
                        .appendParam("protectThreshold", "0.5")
                        .appendParam("namespaceId", namespace1)
                        .done(), StringUtils.EMPTY, "UTF-8",
                HttpMethod.PUT);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        TimeUnit.SECONDS.sleep(5);
        httpResult = request(this.base.toString() + "/nacos/v1/ns/service", Collections.<String>emptyList(),
                ParamsUtils.newParams()
                        .appendParam("serviceName", serviceName)
                        .appendParam("namespaceId", namespace1)
                        .done(), StringUtils.EMPTY, "UTF-8", HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        JSONObject json = JSON.parseObject(httpResult.getData().toString());
        Assertions.assertEquals(serviceName, json.getString("name"));
        TimeUnit.SECONDS.sleep(3);
        Assertions.assertEquals("0.5", json.getString("protectThreshold"));

        deleteService(serviceName, namespace1);
    }

    @Test
    @DisplayName("Get services of server after create a service.")
    public void testGetServicesOfServer() throws Exception {
        ListView<String> listView = naming2.getServicesOfServer(1, 1000);

        log.info("serviceName=" + serviceName + ",before create=" + JSON.toJSONString(listView));
        namingServiceCreate(serviceName, namespace2);
        TimeUnit.SECONDS.sleep(10L);
        ListView<String> listView1 = naming2.getServicesOfServer(1, 1000);
        log.info("serviceName=" + serviceName + ",after create=" + JSON.toJSONString(listView1));
        //if run in parallel will not get expect count
        //Assertions.assertEquals(listView.getCount() + 1, listView1.getCount());
        Assertions.assertTrue(listView1.getData().contains(serviceName));

        deleteService(serviceName, namespace2);
        TimeUnit.SECONDS.sleep(5L);
        ListView<String> listView2 = naming2.getServicesOfServer(1, 1000);
        log.info("serviceName=" + serviceName + ",after delete=" + JSON.toJSONString(listView2));
        //Assertions.assertEquals(listView.getCount(), listView2.getCount());
        Assertions.assertFalse(listView2.getData().contains(serviceName));
    }

    @Test
    @DisplayName("List the service after create a service.")
    public void testListService() throws Exception {
        ListView<String> listView = naming.getServicesOfServer(1, 200);
        namingServiceCreate(serviceName, namespace);
        log.info(serviceName);
        TimeUnit.SECONDS.sleep(10);

        HttpRestResult httpResult = request(this.base.toString() + "/nacos/v1/ns/service/list", Collections.<String>emptyList(),
                ParamsUtils.newParams()
                        .appendParam("serviceName", serviceName)
                        .appendParam("pageNo", "1")
                        .appendParam("pageSize", "1000")
                        .done(), StringUtils.EMPTY, "UTF-8", HttpMethod.GET);
        TimeUnit.SECONDS.sleep(5L);

        log.info(httpResult.toString());
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());

        JSONObject json = JSON.parseObject(httpResult.getData().toString());
        //if run in parallel will not get expect count
        int count = json.getIntValue("count");
        //Assertions.assertEquals(listView.getCount() + 1, count);
        JSONArray serviceNames = json.getJSONArray("doms");
        Assertions.assertTrue(serviceNames.contains(serviceName));

        deleteService(serviceName, namespace);
    }

    @Test
    @DisplayName("Register and deregister instance set ephemeral is false, use http to get")
    public void testRegisterAndDeregisterInstance_HTTP() throws Exception {
        namingServiceCreate(serviceName, namespace);

        instanceRegister(serviceName, namespace, "33.33.33.33", TEST_PORT2_4_DOM_1);
        TimeUnit.SECONDS.sleep(5L);

        HttpRestResult httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
                ParamsUtils.newParams()
                        .appendParam("serviceName", serviceName)
                        .appendParam("namespaceId", namespace)
                        .done(), StringUtils.EMPTY, "UTF-8",
                HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        JSONObject json = JSON.parseObject(httpResult.getData().toString());
        log.info("before deregister:"+json.toJSONString());
        Assertions.assertEquals(1, json.getJSONArray("hosts").size());

        instanceDeregister(serviceName, namespace, "33.33.33.33",
                TEST_PORT2_4_DOM_1);
        TimeUnit.SECONDS.sleep(3L);
        httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("namespaceId", namespace)
                .done(), StringUtils.EMPTY, "UTF-8",
            HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        json = JSON.parseObject(httpResult.getData().toString());
        log.info("after deregister:"+json.toJSONString());
        Assertions.assertEquals(0, json.getJSONArray("hosts").size());
        deleteService(serviceName, namespace);
    }

    @Test
    @DisplayName("Register and deregister instance set ephemeral is false, use client to get")
    public void testRegisterAndDeregisterInstance_client() throws Exception {
        namingServiceCreate(serviceName, namespace);

        instanceRegister(serviceName, namespace, "33.33.33.33", TEST_PORT2_4_DOM_1);
        TimeUnit.SECONDS.sleep(3L);
        List<Instance> instances = naming.getAllInstances(serviceName);
        log.info("before deregister:"+JSON.toJSONString(instances));
        Assertions.assertEquals(1, instances.size());
        Assertions.assertEquals("33.33.33.33", instances.get(0).getIp());

        instanceDeregister(serviceName, namespace, "33.33.33.33", TEST_PORT2_4_DOM_1);

        // Trigger push empty protection for Service need sleep long time
        TimeUnit.SECONDS.sleep(140L);

        List<Instance> instances1 = naming.getAllInstances(serviceName);
        log.info("after deregister:"+JSON.toJSONString(instances1));
        Assertions.assertEquals(0, instances1.size());

        deleteService(serviceName, namespace);

    }

    @Test
    @DisplayName("Create service and register instance in diff group, get all instances and "
        + "deregister instance.")
    public void testRegisterInstance_delete() throws Exception {
        namingServiceCreate(serviceName, namespace);
        namingServiceCreate(serviceName, namespace, TEST_GROUP_1);

        instanceRegister(serviceName, namespace, "33.33.33.33",
                TEST_PORT2_4_DOM_1);
        instanceRegister(serviceName, namespace, TEST_GROUP_1, "22.22.22.22",
                TEST_PORT2_4_DOM_1);
        TimeUnit.SECONDS.sleep(3L);

        HttpRestResult httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
                ParamsUtils.newParams()
                        .appendParam("serviceName", serviceName)
                        .appendParam("namespaceId", namespace)
                        .done(), StringUtils.EMPTY, "UTF-8",
                HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        JSONObject json = JSON.parseObject(httpResult.getData().toString());
        Assertions.assertEquals(1, json.getJSONArray("hosts").size());

        TimeUnit.SECONDS.sleep(3L);
        instanceDeregister(serviceName, namespace, "33.33.33.33",
                TEST_PORT2_4_DOM_1);
        instanceDeregister(serviceName, namespace, TEST_GROUP_1, "22.22.22.22",
                TEST_PORT2_4_DOM_1);
        TimeUnit.SECONDS.sleep(3L);
        deleteService(serviceName, namespace);
        deleteService(serviceName, namespace, TEST_GROUP_1);
    }
}

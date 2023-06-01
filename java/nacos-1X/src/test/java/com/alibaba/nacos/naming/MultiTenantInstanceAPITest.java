package com.alibaba.nacos.naming;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.utils.HttpMethod;
import com.alibaba.nacos.enums.TESTSET;
import com.alibaba.nacos.util.ParamsUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.alibaba.nacos.util.HttpRequestUtils.request;

@Tag(TESTSET.NAMING)
public class MultiTenantInstanceAPITest extends NamingBase{
    private static final Logger log = LoggerFactory.getLogger(MultiTenantInstanceAPITest.class);
    private String serviceName;

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

    @Test
    @DisplayName("Register instance in diff namespace and default group, list instance from diff namespace.")
    public void testMultipleTenant_listInstance() throws Exception {

        naming1.registerInstance(serviceName, "11.11.11.11", 80);
        naming2.registerInstance(serviceName, "22.22.22.22", 80);
        naming.registerInstance(serviceName, "33.33.33.33", 8888);
        naming.registerInstance(serviceName, "44.44.44.44", 8888);
        TimeUnit.SECONDS.sleep(5L);

        HttpRestResult httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
                ParamsUtils.newParams()
                        .appendParam("serviceName", serviceName)
                        .appendParam("namespaceId", namespace1)
                        .done(), StringUtils.EMPTY, "UTF-8", HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());

        JSONObject json = JSON.parseObject((String) httpResult.getData());
        Assertions.assertEquals("11.11.11.11", json.getJSONArray("hosts").getJSONObject(0).getString("ip"));

        httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
                ParamsUtils.newParams()
                        .appendParam("serviceName", serviceName)
                        .done(), StringUtils.EMPTY, "UTF-8",
                HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        json = JSON.parseObject((String) httpResult.getData());
        Assertions.assertEquals(2, json.getJSONArray("hosts").size());
    }

    @Test
    @DisplayName("Register instance in diff namespace and diff group, list instance from diff namespace.")
    public void testMultipleTenant_group_listInstance() throws Exception {
        
        naming1.registerInstance(serviceName, TEST_GROUP_1,"11.11.11.11", 80);

        naming1.registerInstance(serviceName,"22.22.22.22", 80);

        naming.registerInstance(serviceName, TEST_GROUP_1,"33.33.33.33", 8888);
        naming.registerInstance(serviceName, TEST_GROUP_2,"44.44.44.44", 8888);

        TimeUnit.SECONDS.sleep(5L);

        HttpRestResult httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("namespaceId", namespace1)
                .appendParam("groupName", TEST_GROUP_1)
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        JSONObject json = JSON.parseObject((String) httpResult.getData());
        Assertions.assertEquals("11.11.11.11", json.getJSONArray("hosts").getJSONObject(0).getString("ip"));

        httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("groupName", TEST_GROUP_1)
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        json = JSON.parseObject((String) httpResult.getData());
        Assertions.assertEquals(1, json.getJSONArray("hosts").size());
        Assertions.assertEquals("33.33.33.33",json.getJSONArray("hosts").getJSONObject(0).getString("ip"));
    }

    @Test
    @DisplayName("Register instance in diff namespace and default group, get instance from diff namespace.")
    public void testMultipleTenant_getInstance() throws Exception {
        naming1.registerInstance(serviceName, "11.11.11.11", 80);

        naming2.registerInstance(serviceName, "22.22.22.22", 80);

        naming.registerInstance(serviceName, "33.33.33.33", 8888);
        naming.registerInstance(serviceName, "44.44.44.44", 8888);

        TimeUnit.SECONDS.sleep(5L);

        HttpRestResult httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("ip", "33.33.33.33")
                .appendParam("port", TEST_PORT2_4_DOM_1)
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        JSONObject json = JSON.parseObject((String) httpResult.getData());
        log.info(json.toJSONString());

        Assertions.assertEquals(2, json.getJSONArray("hosts").size());
    }

    @Test
    @DisplayName("Register instance in diff namespace and diff cluster, list instance from diff namespace.")
    public void testMultipleTenant_cluster_listInstance() throws Exception {
        naming1.registerInstance(serviceName, "11.11.11.11", 80);
        naming2.registerInstance(serviceName, "22.22.22.22", 80);
        naming.registerInstance(serviceName, "33.33.33.33", 8888, "c1");
        naming.registerInstance(serviceName, "44.44.44.44", 8888, "c2");
        TimeUnit.SECONDS.sleep(5L);

        List<String> listParams = Arrays.asList("serviceName", serviceName, "namespaceId", namespace2, "groupName", TEST_GROUP_1, "ip", "33.33.33.33", "port", "8888");
        HttpRestResult httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("namespaceId", namespace2)
                .appendParam("groupName", TEST_GROUP_1)
                .appendParam("ip", "33.33.33.33")
                .appendParam("port", TEST_PORT2_4_DOM_1)
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        JSONObject json = JSON.parseObject((String) httpResult.getData());
        Assertions.assertEquals(0, json.getJSONArray("hosts").size());

        listParams = Arrays.asList("serviceName", serviceName, "clusters", "c2", "healthyOnly", "true");
        httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("clusters", "c2")
                .appendParam("healthyOnly", "true")
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        json = JSON.parseObject((String) httpResult.getData());
        Assertions.assertEquals(1, json.getJSONArray("hosts").size());
        Assertions.assertEquals("44.44.44.44", json.getJSONArray("hosts").getJSONObject(0).getString("ip"));
    }


    @Test
    @DisplayName("Register instance in diff namespace and default group, delete instance and list instance.")
    public void testMultipleTenant_deleteInstance() throws Exception {
        naming1.registerInstance(serviceName, "11.11.11.11", 80);

        naming2.registerInstance(serviceName, "22.22.22.22", 80);

        naming.registerInstance(serviceName, "33.33.33.33", 8888);
        naming.registerInstance(serviceName, "44.44.44.44", 8888);

        TimeUnit.SECONDS.sleep(3L);
        //Before delete instance use API, need to remove beat
        NacosNamingService namingServiceImpl = (NacosNamingService) naming2;
        namingServiceImpl.getBeatReactor().
            removeBeatInfo(Constants.DEFAULT_GROUP + Constants.SERVICE_INFO_SPLITER + serviceName, "33.33.33.33", 8888);

        TimeUnit.SECONDS.sleep(3L);
        HttpRestResult httpResult = request(this.base.toString() + "/nacos/v1/ns/instance", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("namespaceId", namespace1)
                .appendParam("ip", "33.33.33.33")
                .appendParam("port", TEST_PORT2_4_DOM_1)
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.DELETE);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());

        TimeUnit.SECONDS.sleep(8L);
        httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName) //获取naming中的实例
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        JSONObject json = JSON.parseObject((String) httpResult.getData());
        Assertions.assertEquals(2, json.getJSONArray("hosts").size());
    }

    @Test
    @DisplayName("Register instance in diff namespace and diff group, delete instance and list instance.")
    public void testMultipleTenant_group_deleteInstance() throws Exception {
        
        naming1.registerInstance(serviceName, TEST_GROUP_1, "11.11.11.11", 80);
        naming2.registerInstance(serviceName, TEST_GROUP_2, "22.22.22.22", 80);
        TimeUnit.SECONDS.sleep(5L);

        NacosNamingService namingServiceImpl = (NacosNamingService) naming2;
        namingServiceImpl.getBeatReactor().
            removeBeatInfo(TEST_GROUP_2 + Constants.SERVICE_INFO_SPLITER + serviceName, "22.22.22.22", 80);

        TimeUnit.SECONDS.sleep(3L);
        HttpRestResult httpResult = request(this.base.toString() + "/nacos/v1/ns/instance", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("namespaceId", namespace2)
                .appendParam("groupName", TEST_GROUP_2)
                .appendParam("ip", "22.22.22.22")
                .appendParam("port", TEST_PORT3_4_DOM_1)
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.DELETE);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());

        TimeUnit.SECONDS.sleep(8L);
        httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName) //获取naming中的实例
                .appendParam("namespaceId", namespace2)
                .appendParam("groupName", TEST_GROUP_2)
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        JSONObject json = JSON.parseObject((String) httpResult.getData());
        log.info("json = " + json);
        Assertions.assertEquals(0, json.getJSONArray("hosts").size());
    }

    @Test
    @DisplayName("Register instance in diff namespace and default group, update instance and list instance.")
    public void testMultipleTenant_updateInstance_notExsitInstance() throws Exception {
        naming1.registerInstance(serviceName, "11.11.11.11", 80);

        naming2.registerInstance(serviceName, "22.22.22.22", 80);

        naming.registerInstance(serviceName, "33.33.33.33", 8888);
        naming.registerInstance(serviceName, "44.44.44.44", 8888);

        TimeUnit.SECONDS.sleep(5L);

        // add ns1 instance
        HttpRestResult  httpResult = request(this.base.toString() + "/nacos/v1/ns/instance", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("ip", "33.33.33.33")
                .appendParam("port", "8888")
                .appendParam("namespaceId", namespace1) //新增
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.POST);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());

        TimeUnit.SECONDS.sleep(5L);
        // list ns1 instance
        httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName) //获取naming中的实例
                .appendParam("namespaceId", namespace1)
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        JSONObject json = JSON.parseObject((String) httpResult.getData());
        Assertions.assertEquals(2, json.getJSONArray("hosts").size());

        // list ns2 instance
        httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("namespaceId", namespace2)
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        json = JSON.parseObject((String) httpResult.getData());
        Assertions.assertEquals(1, json.getJSONArray("hosts").size());
    }

    @Test
    @DisplayName("Register instance in diff namespace and default group, update instance's group "
        + "and list instance.")
    public void testMultipleTenant_group_updateInstance_notExsitInstance_1() throws Exception {
        naming1.registerInstance(serviceName, "11.11.11.11", 80);
        naming2.registerInstance(serviceName, "22.22.22.22", 80);
        TimeUnit.SECONDS.sleep(5L);

        HttpRestResult  httpResult = request(this.base.toString() + "/nacos/v1/ns/instance", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("ip", "33.33.33.33")
                .appendParam("port", "8888")
                .appendParam("namespaceId", namespace1)
                .appendParam("groupName", TEST_GROUP_1)
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.POST);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());

        TimeUnit.SECONDS.sleep(5L);
        httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("namespaceId", namespace1)
                .appendParam("groupName", TEST_GROUP_1)
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        JSONObject json = JSON.parseObject((String) httpResult.getData());
        Assertions.assertEquals("33.33.33.33", json.getJSONArray("hosts").getJSONObject(0).getString("ip"));
    }

    @Test
    @DisplayName("Register instance in ns2, update instance in ns1 and list instance in ns1 or ns2.")
    public void testMultipleTenant_updateInstance() throws Exception {
        
        naming2.registerInstance(serviceName, "22.22.22.22", 80);

        HttpRestResult  httpResult = request(this.base.toString() + "/nacos/v1/ns/instance", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("ip", "11.11.11.11")
                .appendParam("port", "80")
                .appendParam("namespaceId", namespace1)
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.POST);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());

        TimeUnit.SECONDS.sleep(5L);
        httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("namespaceId", namespace1)
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        Assertions.assertEquals(1, JSON.parseObject((String) httpResult.getData()).getJSONArray("hosts").size());

        httpResult = request(this.base.toString() + "/nacos/v1/ns/instance/list", Collections.<String>emptyList(),
            ParamsUtils.newParams()
                .appendParam("serviceName", serviceName)
                .appendParam("namespaceId", namespace2)
                .done(), StringUtils.EMPTY,
            "UTF-8",
            HttpMethod.GET);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, httpResult.getCode());
        Assertions.assertEquals(1, JSON.parseObject((String) httpResult.getData()).getJSONArray("hosts").size());
    }
}

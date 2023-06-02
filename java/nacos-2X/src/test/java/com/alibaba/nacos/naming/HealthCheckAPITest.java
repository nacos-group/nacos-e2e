package com.alibaba.nacos.naming;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.enums.TESTSET;
import com.alibaba.nacos.util.ConvertUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Tag(TESTSET.NAMING)
public class HealthCheckAPITest extends NamingBase {
    private static final Logger log = LoggerFactory.getLogger(HealthCheckAPITest.class);
    private List<String> cleanServiceNames = new ArrayList();
    private String serviceName;

    @BeforeEach
    public void setUp() throws Exception{
        serviceName = randomDomainName();
    }

    @AfterEach
    public void tearDown() throws Exception {
        List<String> remove = new ArrayList();
        for (String serviceName : cleanServiceNames) {
            HttpRestResult deleteResult = deleteService(serviceName, namespace);
            log.info("deleteResult " + serviceName + ":" + deleteResult.getCode());
            if (deleteResult.getCode() == 200) {
                remove.add(serviceName);
            }
        }
        log.info("deleteService list:" + ConvertUtils.listToString(remove));
        for (String serviceName : remove) {
            cleanServiceNames.remove(serviceName);
        }
    }

    @Test
    @DisplayName("Create service set enable_auto_clean is true, expect null service will be "
        + "clean after sleep 120s.")
    public void testEnableAutoClean_true() throws Exception {
        // 1. create service where metadata have enable_auto_clean=false
        HttpRestResult createResult = createService(namespace, serviceName, "{\"appName\":\"test"
            + "-app\",\"enable_auto_clean\":\"true\"}");
        log.info("serviceName:" + serviceName + ", createResult:" + JSON.toJSONString(createResult));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, createResult.getCode());
        cleanServiceNames.add(serviceName);

        // 2. wait for 120s, check null service will be clean
        log.info("sleep 120s");
        TimeUnit.SECONDS.sleep(TIME_OUT*24);
        HttpRestResult listResult = listService(namespace);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, listResult.getCode());
        log.info("serviceName:" + serviceName + ", listResult:" + JSON.toJSONString(listResult));
        Assertions.assertFalse(listResult.getData().toString().contains(serviceName));
    }

    @Test
    @DisplayName("Create service set enable_auto_clean is false, expect null service will not be "
        + "clean after sleep 120s, then change service set enable_auto_clean is true, expect null "
        + "service will be clean after sleep 120s.")
    public void testEnableAutoClean_false() throws Exception {
        // 1. create service where metadata have enable_auto_clean=false
        HttpRestResult createResult = createService(namespace, serviceName, "{\"appName\":\"test"
            + "-app\",\"enable_auto_clean\":\"false\"}");
        log.info("serviceName:" + serviceName + ", createResult:" + JSON.toJSONString(createResult));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, createResult.getCode());
        cleanServiceNames.add(serviceName);

        // 2. wait for 120s, check null service will not be clean
        log.info("sleep 120s");
        TimeUnit.SECONDS.sleep(TIME_OUT*24);
        HttpRestResult listResult = listService(namespace);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, listResult.getCode());
        log.info("serviceName:" + serviceName + ", listResult:" + JSON.toJSONString(listResult));
        Assertions.assertTrue(listResult.getData().toString().contains(serviceName));

        //3. change service set metadata enable_auto_clean=true
        HttpRestResult modifyResult = modifyService(namespace, serviceName, "{\"appName\":\"test"
            + "-app\",\"enable_auto_clean\":\"true\"}");
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, modifyResult.getCode());
        log.info("serviceName:" + serviceName + ", modifyResult:" + JSON.toJSONString(modifyResult));

        // 4. wait for 120s, check null service will be clean
        log.info("sleep 120s");
        TimeUnit.SECONDS.sleep(TIME_OUT*24);
        listResult = listService(namespace);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, listResult.getCode());
        log.info("serviceName:" + serviceName + ", listResult:" + JSON.toJSONString(listResult));
        Assertions.assertFalse(listResult.getData().toString().contains(serviceName));
    }

    @Test
    @DisplayName("Create service not set enable_auto_clean key, expect null service will be "
        + "clean after sleep 120s.")
    public void testEnableAutoClean_null() throws Exception {
        // 1. create service where metadata not have enable_auto_clean key
        HttpRestResult createResult = createService(namespace, serviceName, "");
        log.info("serviceName:" + serviceName + ", createResult:" + JSON.toJSONString(createResult));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, createResult.getCode());
        cleanServiceNames.add(serviceName);

        // 2. wait for 120s, check null service will be clean
        log.info("sleep 120s");
        TimeUnit.SECONDS.sleep(TIME_OUT*24);
        HttpRestResult listResult = listService(namespace);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, listResult.getCode());
        log.info("serviceName:" + serviceName + ", listResult:" + JSON.toJSONString(listResult));
        Assertions.assertFalse(listResult.getData().toString().contains(serviceName));
    }

    @Test
    @DisplayName("Update cluster health check type to be http, and get service detail or not")
    public void testHealthChecker_HTTP() throws Exception {
        // 1. create service where metadata have enable_auto_clean=false
        HttpRestResult createResult = createService(namespace, serviceName, "{\"appName\":\"test"
            + "-app\",\"enable_auto_clean\":\"false\"}");
        log.info("serviceName:" + serviceName + ", createResult:" + JSON.toJSONString(createResult));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, createResult.getCode());
        cleanServiceNames.add(serviceName);
        TimeUnit.SECONDS.sleep(3);

        // 2. update cluster health check type to be http
        String healthChecker = "{\"path\":\"/health\",\"headers\":\"\",\"timeoutMs\":3001,"
            + "\"expectedResponseCode\":200,\"internalMs\":4002,\"unhealthyCheckThreshold\":4,"
            + "\"healthyCheckThreshold\":4,\"type\":\"HTTP\"}";
        HttpRestResult updateResult = updateCluster(namespace, serviceName, "8080", "true", healthChecker);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, updateResult.getCode());
        log.info("serviceName:" + serviceName + ", updateResult:" + JSON.toJSONString(updateResult));
        TimeUnit.SECONDS.sleep(TIME_OUT*2);

        //3. get service detail is true
        HttpRestResult getResult = getService(namespace, serviceName, "true");
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, getResult.getCode());
        log.info("serviceName:" + serviceName + ", getResult:" + JSON.toJSONString(getResult));
        JSONObject json = JSON.parseObject(getResult.getData().toString());
        Assertions.assertNotEquals(JSON.parseObject("{}"), json.getJSONObject("data").getJSONObject(
            "clusterMap"));
        JSONObject jsonObject =
            json.getJSONObject("data").getJSONObject("clusterMap").getJSONObject("DEFAULT").getJSONObject("healthChecker");
        JSONObject jsonObject1 = JSON.parseObject(healthChecker);
        boolean isEqual = jsonObject1.equals(jsonObject);
        Assertions.assertTrue(isEqual);

        //4. get service detail is false
        getResult = getService(namespace, serviceName, "false");
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, getResult.getCode());
        log.info("serviceName:" + serviceName + ", getResult:" + JSON.toJSONString(getResult));
        json = JSON.parseObject(getResult.getData().toString());
        Assertions.assertEquals(JSON.parseObject("{}"), json.getJSONObject("data").getJSONObject("clusterMap"));

        //5. get service detail is null
        getResult = getService(namespace, serviceName, "");
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, getResult.getCode());
        log.info("getResult:" + JSON.toJSONString(getResult));
        json = JSON.parseObject(getResult.getData().toString());
        Assertions.assertEquals(JSON.parseObject("{}"), json.getJSONObject("data").getJSONObject("clusterMap"));
    }

    @Test
    @DisplayName("Update cluster health check type to be tcp, and get service detail or not")
    public void testHealthChecker_TCP() throws Exception {
        // 1. create service where metadata have enable_auto_clean=false
        HttpRestResult createResult = createService(namespace, serviceName, "{\"appName\":\"test"
            + "-app\",\"enable_auto_clean\":\"false\"}");
        log.info("serviceName:" + serviceName + ", createResult:" + JSON.toJSONString(createResult));
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, createResult.getCode());
        cleanServiceNames.add(serviceName);
        TimeUnit.SECONDS.sleep(3);

        // 2. update cluster health check type to be tcp
        String healthChecker = "{\"type\":\"TCP\",\"timeoutMs\":5001,\"internalMs\":4005,"
            + "\"healthyCheckThreshold\":10,\"unhealthyCheckThreshold\":10}";
        HttpRestResult updateResult = updateCluster(namespace, serviceName, "8080", "true", healthChecker);
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, updateResult.getCode());
        log.info("updateResult:" + JSON.toJSONString(updateResult));
        TimeUnit.SECONDS.sleep(TIME_OUT*2);

        //3. get service detail is true
        HttpRestResult getResult = getService(namespace, serviceName, "true");
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, getResult.getCode());
        log.info("getResult:" + JSON.toJSONString(getResult));
        JSONObject json = JSON.parseObject(getResult.getData().toString());
        Assertions.assertNotEquals(JSON.parseObject("{}"), json.getJSONObject("data").getJSONObject(
            "clusterMap"));
        JSONObject jsonObject =
            json.getJSONObject("data").getJSONObject("clusterMap").getJSONObject("DEFAULT").getJSONObject("healthChecker");
        JSONObject jsonObject1 = JSON.parseObject(healthChecker);
        boolean isEqual = jsonObject1.equals(jsonObject);
        Assertions.assertTrue(isEqual);

        //4. get service detail is false
        getResult = getService(namespace, serviceName, "false");
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, getResult.getCode());
        log.info("getResult:" + JSON.toJSONString(getResult));
        json = JSON.parseObject(getResult.getData().toString());
        Assertions.assertEquals(JSON.parseObject("{}"), json.getJSONObject("data").getJSONObject("clusterMap"));

        //5. get service detail is null
        getResult = getService(namespace, serviceName, "");
        Assertions.assertEquals(HttpURLConnection.HTTP_OK, getResult.getCode());
        log.info("getResult:" + JSON.toJSONString(getResult));
        json = JSON.parseObject(getResult.getData().toString());
        Assertions.assertEquals(JSON.parseObject("{}"), json.getJSONObject("data").getJSONObject("clusterMap"));
    }
}

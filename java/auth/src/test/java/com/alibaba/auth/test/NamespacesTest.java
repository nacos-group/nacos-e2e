package com.alibaba.auth.test;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.enums.TESTSET;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Tag(TESTSET.OPEN_SOURCE)
public class NamespacesTest extends OpenAPIBase{
    private static final Logger log = LoggerFactory.getLogger(NamespacesTest.class);

    private static String url = "";

    private Map<String, String> cleanNameSpaceMap = new HashMap<>();

    static boolean isOpenSourceAuthRun(){
        Boolean openSourceAuth = Boolean.parseBoolean(System.getProperty("openSourceAuth", "true"));
        if (openSourceAuth && StringUtils.isNotBlank(accessToken)) {
            return true;
        }
        return false;
    }

    @BeforeAll
    public static void beforeAll() throws Exception {
        String[] servers = serverList.split(",");
        String server0 = servers[0].endsWith(":8848") ? servers[0] : servers[0]+":8848";
        url = String.format("http://%s", server0);
    }

    @AfterEach
    public void tearDown() throws Exception {
        Iterator<Map.Entry<String, String>> namespaces = cleanNameSpaceMap.entrySet().iterator();
        while (namespaces.hasNext()) {
            Map.Entry<String, String> entry = namespaces.next();
            Boolean removeResult = removeNamespaces(url, accessToken, entry.getKey());
            if (removeResult) {
                namespaces.remove();
            }
        }
    }

    @Test
    @DisplayName("case1: test add modify search and remove namespaces")
    @EnabledIf("isOpenSourceAuthRun")
    public void testNamespaces() throws Exception {
        String allListResult = getNamespaces(url, accessToken, null);
        Assertions.assertNotNull(allListResult, "Expect get  all namespace success");
        JSONObject allListJson = JSONObject.parseObject(allListResult);
        Assertions.assertTrue(allListJson.getInteger("code") == 200, "Expect get all namespace code 200");
        Assertions.assertTrue(allListJson.getJSONArray("data").size() > 0, "Expect get all namespace data size > 0");

        String namespace = "test-namespace-" + System.currentTimeMillis();
        Boolean addResult = addNamespaces(url, accessToken, namespace, namespace, "auto test");
        Assertions.assertTrue(addResult, "Expect add namespace success");
        Boolean modifyResult = modifyNamespaces(url, accessToken, namespace, namespace + "-1", "auto test 11");
        Assertions.assertTrue(modifyResult, "Expect modify namespace success");
        String listResult = getNamespaces(url, accessToken, namespace);
        Assertions.assertNotNull(listResult, "Expect get namespace success");
        JSONObject listJson = JSONObject.parseObject(listResult);
        Assertions.assertEquals(namespace + "-1", listJson.getString("namespaceShowName"), "Expect get namespaceShowName equals");
        Assertions.assertEquals("auto test 11", listJson.getString("namespaceDesc"), "Expect get namespaceDesc equals");
        Boolean removeResult = removeNamespaces(url, accessToken, namespace);
        Assertions.assertTrue(removeResult, "Expect remove namespace success");
    }
}

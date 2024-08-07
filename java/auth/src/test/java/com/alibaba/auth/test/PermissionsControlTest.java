package com.alibaba.auth.test;

import com.alibaba.nacos.enums.TESTSET;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

@Tag(TESTSET.OPEN_SOURCE)
public class PermissionsControlTest extends OpenAPIBase{
    private static final Logger log = LoggerFactory.getLogger(PermissionsControlTest.class);

    private static String url = "";

    private Map<String ,String> cleanUserMap = new HashMap<>();
    private Map<String ,String> cleanRoleMap = new HashMap<>();

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
        Iterator<Entry<String, String>> users = cleanUserMap.entrySet().iterator();
        while (users.hasNext()) {
            Map.Entry<String, String> entry = users.next();
            Boolean removeResult = removeNacosV1AuthUsers(url, accessToken, entry.getKey());
            if (removeResult) {
                users.remove();
            }
        }
        Iterator<Entry<String, String>> roles = cleanRoleMap.entrySet().iterator();
        while (roles.hasNext()) {
            Map.Entry<String, String> entry = roles.next();
            Boolean removeResult = removeNacosV1AuthRoles(url, accessToken, entry.getKey(),
                entry.getValue());
            if (removeResult) {
                roles.remove();
            }
        }
    }

    @Test
    @DisplayName("case1: test add modify search and remove user")
    @EnabledIf("isOpenSourceAuthRun")
    public void testUserManagement() throws Exception {
        String username = "testUser-" + System.currentTimeMillis();
        String password = "nacos1234test";
        Boolean addResult = addNacosV1AuthUsers(url, accessToken, username, password);
        Assertions.assertTrue(addResult, "expected create user success");
        cleanUserMap.put(username, password);

        Boolean searchResult = searchNacosV1AuthUsers(url, accessToken, username, "1", "10", "accurate");
        Assertions.assertTrue(searchResult, "expected accurate search user success");

        Boolean modifyResult = modifyNacosV1AuthUsers(url, accessToken, username, "nacostest1234");
        Assertions.assertTrue(modifyResult, "expected modify psw success");

        Boolean removeResult = removeNacosV1AuthUsers(url, accessToken, username);
        Assertions.assertTrue(removeResult, "expected remove user success");
        cleanUserMap.remove(username);
    }

    @Test
    @DisplayName("case2: test add search and remove role")
    @EnabledIf("isOpenSourceAuthRun")
    public void testRolesManagement() throws Exception {
        String username = "testUser-" + System.currentTimeMillis();
        String password = "nacos1234test";
        String role = "testRole-" + System.currentTimeMillis();

        Boolean addUserResult = addNacosV1AuthUsers(url, accessToken, username, password);
        Assertions.assertTrue(addUserResult, "expected create user success");
        cleanUserMap.put(username, password);

        Boolean addRoleResult = addNacosV1AuthRoles(url, accessToken, username, role);
        Assertions.assertTrue(addRoleResult, "expected create role success");

        Boolean searchResult = searchNacosV1AuthRoles(url, accessToken, username, role,"1", "10",
            "accurate");
        Assertions.assertTrue(searchResult, "expected accurate search role success");

        Boolean removeResult = removeNacosV1AuthRoles(url, accessToken, username, role);
        Assertions.assertTrue(removeResult, "expected remove role success");
    }

    @Test
    @DisplayName("case3: test add search and remove permissions")
    @EnabledIf("isOpenSourceAuthRun")
    public void testPermissionsManagement() throws Exception {
        String username = "testUser-" + System.currentTimeMillis();
        String password = "nacos1234test";
        String role = "testRole-" + System.currentTimeMillis();

        Boolean addUserResult = addNacosV1AuthUsers(url, accessToken, username, password);
        Assertions.assertTrue(addUserResult, "expected create user success");
        cleanUserMap.put(username, password);

        Boolean addRoleResult = addNacosV1AuthRoles(url, accessToken, username, role);
        Assertions.assertTrue(addRoleResult, "expected create role success");
        cleanRoleMap.put(username, role);

        String resource = "*:*";    // bb:*:*
        String action = "r";  // r、w、rw
        Boolean addPermissionsResult = addNacosV1AuthPermissions(url, accessToken, role, resource, action);
        Assertions.assertTrue(addPermissionsResult, "expected create permissions success");

        Boolean searchResult = searchNacosV1AuthPermissions(url, accessToken, role,"1", "10",
            "accurate");
        Assertions.assertTrue(searchResult, "expected accurate search permissions success");

        Boolean removeResult = removeNacosV1AuthPermissions(url, accessToken, role, resource, action);
        Assertions.assertTrue(removeResult, "expected remove permissions success");
    }


}

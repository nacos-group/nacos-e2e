package com.alibaba.auth.test;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.frame.BaseOperate;
import com.alibaba.nacos.util.OkHttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class OpenAPIBase extends BaseOperate {
    private static final Logger log = LoggerFactory.getLogger(OpenAPIBase.class);

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
                String accessToken = jsonObject.getString("accessToken");
                return accessToken;
            } else {
                log.info("doNacosV1AuthLogin error, result:{}", result);
            }
        }
        return null;
    }

    public static Boolean addNacosV1AuthUsers(String url, String accessToken, String username,
        String password) throws Exception{
        url = url + "/nacos/v1/auth/users?accessToken=" + accessToken;

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        String result = OkHttpUtils.post(url, params);
        if (StringUtils.isNotBlank(result) && result.contains("ok")) {
            return true;
        }
        return false;
    }

    public static Boolean removeNacosV1AuthUsers(String url, String accessToken, String username) throws Exception{
        url = url + "/nacos/v1/auth/users";

        Map<String, String> params = new HashMap<>();
        params.put("accessToken", accessToken);
        params.put("username", username);
        String result = OkHttpUtils.delete(url, params);
        if (StringUtils.isNotBlank(result) && result.contains("ok")) {
            return true;
        }
        return false;
    }

    public static Boolean modifyNacosV1AuthUsers(String url, String accessToken, String username, String newPassword) throws Exception{
        url = url + "/nacos/v1/auth/users?accessToken=" + accessToken;

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("newPassword", newPassword);
        String result = OkHttpUtils.put(url, params);
        if (StringUtils.isNotBlank(result) && result.contains("ok")) {
            return true;
        }
        return false;
    }

    public static Boolean searchNacosV1AuthUsers(String url, String accessToken, String username,
        String pageNo, String pageSize, String search) throws Exception{
        url = url + "/nacos/v1/auth/users";

        Map<String, String> params = new HashMap<>();
        params.put("accessToken", accessToken);
        params.put("username", username);
        params.put("pageNo", pageNo);
        params.put("pageSize", pageSize);
        params.put("search", search);
        String result = OkHttpUtils.get(url, params);
        if (StringUtils.isNotBlank(result) && result.contains(username)) {
            return true;
        }
        return false;
    }

    public static Boolean addNacosV1AuthRoles(String url, String accessToken, String username,
        String role) throws Exception{
        url = url + "/nacos/v1/auth/roles?accessToken=" + accessToken;

        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("role", role);
        String result = OkHttpUtils.post(url, params);
        if (StringUtils.isNotBlank(result) && result.contains("ok")) {
            return true;
        }
        return false;
    }

    public static Boolean searchNacosV1AuthRoles(String url, String accessToken, String username,
        String role, String pageNo, String pageSize, String search) throws Exception{
        url = url + "/nacos/v1/auth/roles";

        Map<String, String> params = new HashMap<>();
        params.put("accessToken", accessToken);
        params.put("role", role);
        params.put("username", username);
        params.put("pageNo", pageNo);
        params.put("pageSize", pageSize);
        params.put("search", search);
        String result = OkHttpUtils.get(url, params);
        if (StringUtils.isNotBlank(result) && result.contains(username) && result.contains(role)) {
            return true;
        }
        return false;
    }

    public static Boolean removeNacosV1AuthRoles(String url, String accessToken, String username,
        String role) throws Exception{
        url = url + "/nacos/v1/auth/roles";

        Map<String, String> params = new HashMap<>();
        params.put("accessToken", accessToken);
        params.put("username", username);
        params.put("role", role);
        String result = OkHttpUtils.delete(url, params);
        if (StringUtils.isNotBlank(result) && result.contains("ok")) {
            return true;
        }
        return false;
    }

    public static Boolean addNacosV1AuthPermissions(String url, String accessToken, String role,
        String resource, String action) throws Exception{
        url = url + "/nacos/v1/auth/permissions?accessToken=" + accessToken;

        Map<String, String> params = new HashMap<>();
        params.put("role", role);
        params.put("resource", resource);
        params.put("action", action);
        String result = OkHttpUtils.post(url, params);
        if (StringUtils.isNotBlank(result) && result.contains("ok")) {
            return true;
        }
        return false;
    }

    public static Boolean searchNacosV1AuthPermissions(String url, String accessToken,
        String role, String pageNo, String pageSize, String search) throws Exception{
        url = url + "/nacos/v1/auth/permissions";

        Map<String, String> params = new HashMap<>();
        params.put("accessToken", accessToken);
        params.put("role", role);
        params.put("pageNo", pageNo);
        params.put("pageSize", pageSize);
        params.put("search", search);
        String result = OkHttpUtils.get(url, params);
        if (StringUtils.isNotBlank(result) && result.contains(role)) {
            return true;
        }
        return false;
    }

    public static Boolean removeNacosV1AuthPermissions(String url, String accessToken, String role,
        String resource, String action) throws Exception{
        url = url + "/nacos/v1/auth/permissions";

        Map<String, String> params = new HashMap<>();
        params.put("accessToken", accessToken);
        params.put("role", role);
        params.put("resource", resource);
        params.put("action", action);
        String result = OkHttpUtils.delete(url, params);
        if (StringUtils.isNotBlank(result) && result.contains("ok")) {
            return true;
        }
        return false;
    }
}

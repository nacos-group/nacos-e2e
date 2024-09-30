package com.alibaba.nacos.util;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OkHttpUtils {
    private static OkHttpClient okHttpClient= new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build();


    public static Response getRequest(String url) throws IOException {
        Map<String, String> headers = new HashMap<>();
        return getRequest(url, headers);
    }

    public static Response getRequest(String url, Map<String, String> headers) throws IOException {
        Request.Builder requestBuilder = new Request.Builder();

        for (String key : headers.keySet()) {
            requestBuilder.addHeader(key, headers.get(key));
        }

        Request request = requestBuilder
            .url(url)
            .build();

        log.info("okhttp3 get request url: " + request.url());
        Response response = okHttpClient.newCall(request).execute();
        MediaType mediaType = response.body().contentType();
        String content = JSONObject.toJSONString(response);

        return response.newBuilder()
            .body(ResponseBody.create(content, mediaType))
            .build();
    }

    public static String get(String url, Map<String, String> queries) throws IOException {
        Map<String, String> headers = new HashMap<>();
        return get(url, queries, headers);
    }

    public static String get(String url, Map<String, String> queries, Map<String, String> headers) throws IOException {
        String responseBody = "";
        Request.Builder requestBuilder = new Request.Builder();
        for (String key : headers.keySet()) {
            requestBuilder.addHeader(key, headers.get(key));
        }
        StringBuffer sb = new StringBuffer(url);
        if (queries != null && !queries.keySet().isEmpty()) {
            boolean firstFlag = true;
            Iterator iterator = queries.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry<String, String>) iterator.next();
                if (firstFlag) {
                    sb.append("?" + entry.getKey() + "=" + entry.getValue());
                    firstFlag = false;
                } else {
                    sb.append("&" + entry.getKey() + "=" + entry.getValue());
                }
            }
        }
        Request request = requestBuilder
            .url(sb.toString())
            .build();
        log.info("okhttp3 get request url: " + request.url());
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                log.error("okhttp3 get response is not successful, response code: " + response.code());
            }
        } catch (Exception e) {
            log.error("okhttp3 get with Exception: " + ExceptionUtils.getStackTrace(e));
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return responseBody;
    }

    public static String post(String url, Map<String, String> params) {
        Map<String, String> headers = new HashMap<>();
        return post(url, params, headers);
    }

    public static String post(String url, Map<String, String> params, Map<String, String> headers) {
        String responseBody = "";

        FormBody.Builder formBuilder = new FormBody.Builder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody formBody = formBuilder.build();

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url)
            .post(formBody);

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();
        log.info("okhttp3 post request url: " + request.url());

        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                responseBody = response.body().string();
                return responseBody;
            } else {
                log.error("okhttp3 put response is not successful, response code: " + response.code());
            }
        } catch (Exception e) {
            log.error("okhttp3 post exception: ", e);
        }

        return responseBody;
    }

    public static String postJsonParams(String url, String jsonParams) {
        String responseBody = "";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams);
        Request request = new Request.Builder()
            .url(url)
            .post(requestBody)
            .build();
        log.info("okhttp3 post request url: " + request.url());
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            int status = response.code();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (Exception e) {
            log.error("okhttp3 postJsonParams with Exception: " + ExceptionUtils.getStackTrace(e));
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return responseBody;
    }

    public static String postXmlParams(String url, String xml) {
        String responseBody = "";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/xml; charset=utf-8"), xml);
        Request request = new Request.Builder()
            .url(url)
            .post(requestBody)
            .build();
        log.info("okhttp3 post request url: " + request.url());
        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            int status = response.code();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (Exception e) {
            log.error("okhttp3 postXmlParams with Exception: " + ExceptionUtils.getStackTrace(e));
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return responseBody;
    }


    public static String put(String url, Map<String, String> params) {
        Map<String, String> headers = new HashMap<>();
        return put(url, params, headers);
    }

    public static String put(String url, Map<String, String> params, Map<String, String> headers) {
        String responseBody = "";

        FormBody.Builder formBuilder = new FormBody.Builder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        RequestBody formBody = formBuilder.build();

        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url)
            .put(formBody);

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }

        Request request = requestBuilder.build();
        log.info("okhttp3 put request url: " + request.url());

        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                responseBody = response.body().string();
                return responseBody;
            } else {
                log.error("okhttp3 put response is not successful, response code: " + response.code());
            }
        } catch (Exception e) {
            log.error("okhttp3 put exception: ", e);
        }

        return responseBody;
    }

    public static String delete(String url, Map<String, String> queries) throws IOException {
        Map<String, String> headers = new HashMap<>();
        return delete(url, queries, headers);
    }

    public static String delete(String url, Map<String, String> queries, Map<String, String> headers) throws IOException {
        String responseBody = "";
        Request.Builder requestBuilder = new Request.Builder();
        for (String key : headers.keySet()) {
            requestBuilder.addHeader(key, headers.get(key));
        }
        StringBuffer sb = new StringBuffer(url);
        if (queries != null && !queries.keySet().isEmpty()) {
            boolean firstFlag = true;
            Iterator iterator = queries.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry<String, String>) iterator.next();
                if (firstFlag) {
                    sb.append("?" + entry.getKey() + "=" + entry.getValue());
                    firstFlag = false;
                } else {
                    sb.append("&" + entry.getKey() + "=" + entry.getValue());
                }
            }
        }
        Request request = requestBuilder
            .url(sb.toString())
            .delete()
            .build();
        log.info("okhttp3 delete request url: " + request.url());

        Response response = null;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                log.error("okhttp3 delete response is not successful, response code: " + response.code());
            }
        } catch (Exception e) {
            log.error("okhttp3 delete with Exception: " + ExceptionUtils.getStackTrace(e));
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return responseBody;
    }
}

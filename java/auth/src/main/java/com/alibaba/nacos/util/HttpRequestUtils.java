package com.alibaba.nacos.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.client.naming.remote.http.NamingHttpClientManager;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.shaded.io.grpc.netty.shaded.io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class HttpRequestUtils {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestUtils.class);

    public static HttpRestResult request(String url, List<String> header, Map<String, String> params, String body,
        String encode, String method) throws Exception {
        log.info("request - url:{}, header:{}, params:{}, body:{}, encode:{}, method:{}",
            url, ConvertUtils.listToString(header), ConvertUtils.mapToString(params), body, encode, method);
        HttpRestResult httpResult = new HttpRestResult();
        NacosRestTemplate restTemplate = NamingHttpClientManager.getInstance().getNacosRestTemplate();
        if (method.equalsIgnoreCase(String.valueOf(HttpMethod.GET))) {
            httpResult = restTemplate.get(url, Header.newInstance().addAll(header),
                Query.newInstance().initParams(params),
                String.class);
        } else if (method.equalsIgnoreCase(String.valueOf(HttpMethod.PUT))) {
            httpResult = restTemplate.put(url, Header.newInstance().addAll(header), Query.newInstance().initParams(params), body,
                    String.class);
        } else if (method.equalsIgnoreCase(String.valueOf(HttpMethod.POST))) {
            httpResult = restTemplate.post(url, Header.newInstance().addAll(header), Query.newInstance().initParams(params), body,
                    String.class);
        } else if (method.equalsIgnoreCase(String.valueOf(HttpMethod.DELETE))) {
            httpResult = restTemplate.delete(url, Header.newInstance().addAll(header), Query.newInstance().initParams(params),
                String.class);
        }
        log.info("response - " + JSON.toJSONString(httpResult));
        return httpResult;
    }
}

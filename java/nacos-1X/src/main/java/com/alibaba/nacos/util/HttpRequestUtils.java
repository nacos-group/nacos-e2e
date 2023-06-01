package com.alibaba.nacos.util;

import com.alibaba.nacos.client.naming.net.NamingHttpClientManager;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.common.utils.HttpMethod;
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
        NacosRestTemplate restTemplate = NamingHttpClientManager.getInstance().getNacosRestTemplate();
        if (method.equalsIgnoreCase(String.valueOf(HttpMethod.GET))) {
            return restTemplate.get(url, Header.newInstance().addAll(header), Query.newInstance().initParams(params),
                String.class);
        } else if (method.equalsIgnoreCase(String.valueOf(HttpMethod.PUT))) {
            return restTemplate
                .put(url, Header.newInstance().addAll(header), Query.newInstance().initParams(params), body,
                    String.class);
        } else if (method.equalsIgnoreCase(String.valueOf(HttpMethod.POST))) {
            return restTemplate
                .post(url, Header.newInstance().addAll(header), Query.newInstance().initParams(params), body,
                    String.class);
        } else if (method.equalsIgnoreCase(String.valueOf(HttpMethod.DELETE))) {
            return restTemplate.delete(url, Header.newInstance().addAll(header), Query.newInstance().initParams(params),
                String.class);
        }
        return null;
    }
}

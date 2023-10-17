package com.alibaba.nacos.frame;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NacosClientVersionChecker {
    private static final Logger log = LoggerFactory.getLogger(NacosClientVersionChecker.class);
    private static final String REPO_BASE_URL = "https://repo.maven.apache.org/maven2";

    public static String getLatestVersion(String startVersion, String endVersion, String type) {
        String latestVersion = startVersion;
        try {
            if (StringUtils.isNotBlank(endVersion) && "close".equals(type)) {
                latestVersion = endVersion;
            }
            if ("open".endsWith(type)) {
                String url = String.format("%s/%s/maven-metadata.xml", REPO_BASE_URL, "com/alibaba/nacos/nacos-client");
                HttpClient httpClient = HttpClientBuilder.create().build();
                HttpGet httpGet = new HttpGet(url);
                String response = EntityUtils.toString(httpClient.execute(httpGet).getEntity());
                log.info("getLatestVersion response is " + response);
                JSONObject json = XML.toJSONObject(response);
                latestVersion = json.getJSONObject("metadata").getJSONObject("versioning").getString("latest");
                //JSONArray versionArray = json.getJSONObject("metadata").getJSONObject("versioning").getJSONObject("versions").getJSONArray("version");
            }
        } catch (Exception e) {
            log.error("getLatestVersion Exception", e);
        }

        return latestVersion;
    }

    public static void main(String[] args) {
        System.out.println(getLatestVersion("2.0.0", "2.2.2", "close"));    //2.2.2
        System.out.println(getLatestVersion("2.2.1", "2.2.2", "open"));     //2.2.4
        System.out.println(getLatestVersion("2.2.1", "", "open"));          //2.2.4
    }
}

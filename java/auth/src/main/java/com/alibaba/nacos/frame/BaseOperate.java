package com.alibaba.nacos.frame;

import com.alibaba.nacos.api.PropertyKeyConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class BaseOperate extends ResourceInit{
    private static final Logger log = LoggerFactory.getLogger(BaseOperate.class);
    public static Properties properties;
    public static Properties properties1;
    public static Properties properties2;
    public static Properties properties_errorAkSk;
    public static Properties properties_emptyAkSk;
    public static Properties properties_rightAkWrongSk;

    static {
        properties = new Properties();
        properties1 = new Properties();
        properties2 = new Properties();
        properties_errorAkSk = new Properties();
        properties_emptyAkSk = new Properties();
        properties_rightAkWrongSk = new Properties();
        if (mode.equals("endpoint")) {
            properties.put(PropertyKeyConst.ENDPOINT, serverList);
            properties1.put(PropertyKeyConst.ENDPOINT, serverList);
            properties2.put(PropertyKeyConst.ENDPOINT, serverList);
            properties_errorAkSk.put(PropertyKeyConst.ENDPOINT, serverList);
            properties_emptyAkSk.put(PropertyKeyConst.ENDPOINT, serverList);
            properties_rightAkWrongSk.put(PropertyKeyConst.ENDPOINT, serverList);
        } else {
            properties.put(PropertyKeyConst.SERVER_ADDR, serverList);
            properties1.put(PropertyKeyConst.SERVER_ADDR, serverList);
            properties2.put(PropertyKeyConst.SERVER_ADDR, serverList);
            properties_errorAkSk.put(PropertyKeyConst.SERVER_ADDR, serverList);
            properties_emptyAkSk.put(PropertyKeyConst.SERVER_ADDR, serverList);
            properties_rightAkWrongSk.put(PropertyKeyConst.SERVER_ADDR, serverList);
        }
        properties.put(PropertyKeyConst.NAMESPACE, namespace);
        properties1.put(PropertyKeyConst.NAMESPACE, namespace1);
        properties2.put(PropertyKeyConst.NAMESPACE, namespace2);
        properties_errorAkSk.put(PropertyKeyConst.NAMESPACE, namespace);
        properties_emptyAkSk.put(PropertyKeyConst.NAMESPACE, namespace);
        properties_rightAkWrongSk.put(PropertyKeyConst.NAMESPACE, namespace);

        if (aclEnable) {
            log.info("acl enable");
            properties.put(PropertyKeyConst.USERNAME, username);
            properties.put(PropertyKeyConst.PASSWORD, password);

            properties1.put(PropertyKeyConst.USERNAME, username);
            properties1.put(PropertyKeyConst.PASSWORD, password);

            properties2.put(PropertyKeyConst.USERNAME, username);
            properties2.put(PropertyKeyConst.PASSWORD, password);

            properties_errorAkSk.put(PropertyKeyConst.USERNAME, "123456");
            properties_errorAkSk.put(PropertyKeyConst.PASSWORD, "123456");

            properties_rightAkWrongSk.put(PropertyKeyConst.USERNAME, username);
            properties_rightAkWrongSk.put(PropertyKeyConst.PASSWORD, "abcdefghijklmn");
        }
        if (tlsEnable) {
            System.setProperty("nacos.remote.client.rpc.tls.enable", "true");
            System.setProperty("nacos.remote.client.rpc.tls.trustAll", "true");

            // if pure nacos client, don't set this
            // not pure nacos client & jdk version under 1.8.0_u252 , set OPENSSL
            //System.setProperty("nacos.remote.client.rpc.tls.provider", "OPENSSL");
            // not pure nacos client & jdk version up 1.8.0_u252, set JDK
            System.setProperty("nacos.remote.client.rpc.tls.provider", "JDK");
        }
    }

}

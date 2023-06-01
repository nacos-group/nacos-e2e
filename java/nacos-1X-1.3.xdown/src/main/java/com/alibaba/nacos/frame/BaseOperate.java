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

    static {
        properties = new Properties();
        properties1 = new Properties();
        properties2 = new Properties();
        if (mode.equals("endpoint")) {
            properties.put(PropertyKeyConst.ENDPOINT, serverList);
            properties1.put(PropertyKeyConst.ENDPOINT, serverList);
            properties2.put(PropertyKeyConst.ENDPOINT, serverList);
        } else {
            properties.put(PropertyKeyConst.SERVER_ADDR, serverList);
            properties1.put(PropertyKeyConst.SERVER_ADDR, serverList);
            properties2.put(PropertyKeyConst.SERVER_ADDR, serverList);
        }
        properties.put(PropertyKeyConst.NAMESPACE, namespace);
        properties1.put(PropertyKeyConst.NAMESPACE, namespace1);
        properties2.put(PropertyKeyConst.NAMESPACE, namespace2);

        if (aclEnable) {
            log.info("acl enable");
            properties.put(PropertyKeyConst.ACCESS_KEY, accessKey);
            properties.put(PropertyKeyConst.SECRET_KEY, secretKey);

            properties1.put(PropertyKeyConst.ACCESS_KEY, accessKey);
            properties1.put(PropertyKeyConst.SECRET_KEY, secretKey);

            properties2.put(PropertyKeyConst.ACCESS_KEY, accessKey);
            properties2.put(PropertyKeyConst.SECRET_KEY, secretKey);
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

package com.alibaba.nacos.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.enums.TESTSET;
import com.alibaba.nacos.util.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

@Tag(TESTSET.DIRECTADDRESS)
public class ConfigDirectAddrTest extends ConfigBase {
    private static final Logger log = LoggerFactory.getLogger(ConfigDirectAddrTest.class);
    private String dataId;
    private String group;
    private String content;
    
    static Boolean useEndpoint = Boolean.parseBoolean(System.getenv("nacos.use.endpoint.parsing.rule") == null ?
        System.getProperty("nacos.use.endpoint.parsing.rule","false") :
        System.getenv("nacos.use.endpoint.parsing.rule"));
    static String  endpointUrl = System.getenv("ALIBABA_ALIWARE_ENDPOINT_URL") == null ?
        System.getProperty("ALIBABA_ALIWARE_ENDPOINT_URL") : System.getenv("ALIBABA_ALIWARE_ENDPOINT_URL");
    static String directAddr = System.getenv("directAddr") == null ?
        System.getProperty("directAddr") : System.getenv("directAddr");
    private static ConfigService config;
    private static ConfigService configDirect;
    private static ConfigService configEndpoint;

    private boolean isRun() {
        log.info("endpointUrl is " + endpointUrl + ", directAddr is " + directAddr);
        if (StringUtils.isNotBlank(endpointUrl) && StringUtils.isNotBlank(directAddr)) {
            directAddr = directAddr.contains(":8848") ? directAddr : directAddr + ":8848";
            endpointUrl = endpointUrl.contains(":8848") ? endpointUrl : endpointUrl + ":8848";
            return true;
        } else {
            return false;
        }
    }
    
    @BeforeEach
    public void setUp() throws Exception{
        dataId = "config.test." + RandomUtils.getStringWithCharacter(10);
        group = DEFAULT_GROUP;
        content = RandomUtils.getStringWithCharacter(20);

        if (configDirect == null) {
            log.info("init configDirect " + directAddr);
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, directAddr);
            //if not set false，will be change the value by nacos.use.endpoint.parsing.rule
            properties.put(PropertyKeyConst.IS_USE_ENDPOINT_PARSING_RULE, "false");
            if (aclEnable) {
                properties.put(PropertyKeyConst.ACCESS_KEY, accessKey);
                properties.put(PropertyKeyConst.SECRET_KEY, secretKey);
            }
            properties.put(PropertyKeyConst.NAMESPACE, namespace);
            configDirect = NacosFactory.createConfigService(properties);
        }
        if (configEndpoint == null) {
            log.info("init configEndpoint " + endpointUrl);
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, endpointUrl);
            properties.put(PropertyKeyConst.IS_USE_ENDPOINT_PARSING_RULE, "false");
            if (aclEnable) {
                properties.put(PropertyKeyConst.ACCESS_KEY, accessKey);
                properties.put(PropertyKeyConst.SECRET_KEY, secretKey);
            }
            properties.put(PropertyKeyConst.NAMESPACE, namespace);
            configEndpoint = NacosFactory.createConfigService(properties);
        }
        if (config == null) {
            // directAddr publish a config
            log.info("init pub " + directAddr);
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, directAddr);
            if (aclEnable) {
                properties.put(PropertyKeyConst.ACCESS_KEY, accessKey);
                properties.put(PropertyKeyConst.SECRET_KEY, secretKey);
            }
            properties.put(PropertyKeyConst.NAMESPACE, namespace);
            config = NacosFactory.createConfigService(properties);
        }
        boolean result = config.publishConfig(dataId, group, content);
        log.info(
            "test dataId=" + dataId + ", group=" + group + ",content=" + content + ", result=" + result);
        Thread.sleep(TIME_OUT);
    }
    

    @Test
    @EnabledIf("isRun")
    @DisplayName("After directAddr publish a config, and get config when use endpoint is true or false.")
    public void getConfigTest() throws Exception {

        if (!useEndpoint) {
            //when nacos.use.endpoint.parsing.rule=false and client version is 2.2.0 or 2.2.1 will be get config null
            if (StringUtils.isNotBlank(nacosClientVersion)) {
                nacosClientVersion = nacosClientVersion.replaceAll("\\.", "").split("-")[0];
                if (Integer.parseInt(nacosClientVersion) == 220 || Integer.parseInt(
                    nacosClientVersion) == 221) {
                    String value = config.getConfig(dataId, group, 3000);
                    Assertions.assertEquals(content, value);
                    log.info("config getConfig value=" + value);

                    String valueExist = configDirect.getConfig(dataId, group, 3000);
                    String valueEmpty = configEndpoint.getConfig(dataId, group, 3000);
                    log.info("configDirect getConfig value=" + valueExist + ", configEndpoint getConfig value=" + valueEmpty);
                    Assertions.assertEquals(null, valueExist);
                    Assertions.assertEquals(content, valueEmpty);

                }
            } else {
                String value = config.getConfig(dataId, group, 3000);
                Assertions.assertEquals(content, value);
                log.info("config getConfig value=" + value);

                String valueExist = configDirect.getConfig(dataId, group, 3000);
                String valueEmpty = configEndpoint.getConfig(dataId, group, 3000);
                log.info("configDirect getConfig value=" + valueExist + ", configEndpoint getConfig value=" + valueEmpty);
                Assertions.assertEquals(content, valueExist);
                Assertions.assertEquals(null, valueEmpty);
            }

        } else {
            //when nacos.use.endpoint.parsing.rule=true, it will be success
            String value = config.getConfig(dataId, group, 3000);
            Assertions.assertEquals(content, value);
            log.info("config getConfig value=" + value);

            String valueEmpty = configDirect.getConfig(dataId, group, 3000);
            String valueExist = configEndpoint.getConfig(dataId, group, 3000);
            log.info("configDirect getConfig value=" + valueEmpty + ", configEndpoint getConfig value=" + valueExist);
            Assertions.assertEquals(null, valueEmpty);
            Assertions.assertEquals(content, valueExist);
        }
    }
}

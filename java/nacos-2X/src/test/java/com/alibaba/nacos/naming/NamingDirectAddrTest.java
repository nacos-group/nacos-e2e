package com.alibaba.nacos.naming;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.enums.TESTSET;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Tag(TESTSET.DIRECTADDRESS)
public class NamingDirectAddrTest extends NamingBase{
    private static final Logger log = LoggerFactory.getLogger(NamingDirectAddrTest.class);

    static Boolean useEndpoint = Boolean.parseBoolean(System.getenv("nacos.use.endpoint.parsing"
        + ".rule") == null ? "false" : System.getenv("nacos.use.endpoint.parsing.rule"));
    static String  endpointUrl = System.getenv("ALIBABA_ALIWARE_ENDPOINT_URL");
    static String directAddr = System.getenv("directAddr");
    private String serviceName;
    private static NamingService naming;
    private static NamingService namingDirect;
    private static NamingService namingEndpoint;

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
    public void setUp() throws Exception {
        serviceName = randomDomainName();
        if (namingDirect == null) {
            log.info("init namingDirect " + directAddr);
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, directAddr);
            properties.put(PropertyKeyConst.IS_USE_ENDPOINT_PARSING_RULE, "false");
            if (aclEnable) {
                properties.put(PropertyKeyConst.ACCESS_KEY, accessKey);
                properties.put(PropertyKeyConst.SECRET_KEY, secretKey);
            }
            properties.put(PropertyKeyConst.NAMESPACE, namespace);
            namingDirect = NacosFactory.createNamingService(properties);
        }
        if (namingEndpoint == null) {
            log.info("init namingEndpoint " + endpointUrl);
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, endpointUrl);
            properties.put(PropertyKeyConst.IS_USE_ENDPOINT_PARSING_RULE, "false");
            if (aclEnable) {
                properties.put(PropertyKeyConst.ACCESS_KEY, accessKey);
                properties.put(PropertyKeyConst.SECRET_KEY, secretKey);
            }
            properties.put(PropertyKeyConst.NAMESPACE, namespace);
            namingEndpoint = NacosFactory.createNamingService(properties);
        }
        if (naming == null) {
            log.info("init pub " + directAddr);
            Properties properties = new Properties();
            properties.put(PropertyKeyConst.SERVER_ADDR, directAddr);
            if (aclEnable) {
                properties.put(PropertyKeyConst.ACCESS_KEY, accessKey);
                properties.put(PropertyKeyConst.SECRET_KEY, secretKey);
            }
            properties.put(PropertyKeyConst.NAMESPACE, namespace);
            naming = NacosFactory.createNamingService(properties);
        }
        naming.registerInstance(serviceName, NamingBase.TEST_IP_4_DOM_1, NamingBase.TEST_PORT);
        log.info("test serviceName=" + serviceName + ", ip=" + NamingBase.TEST_IP_4_DOM_1 + ", port=" + NamingBase.TEST_PORT);
        TimeUnit.SECONDS.sleep(TIME_OUT);
    }

    @AfterEach
    public void tearDown() throws Exception {
        naming.deregisterInstance(serviceName, NamingBase.TEST_IP_4_DOM_1, NamingBase.TEST_PORT);
    }

    @Test
    @EnabledIf("isRun")
    @DisplayName("After directAddr register a instance, and get instance when use endpoint is "
        + "true or false.")
    public void getAllInstancesTest() throws Exception{
        if (!useEndpoint) {
            //when nacos.use.endpoint.parsing.rule=false and client version is 2.2.0 or 2.2.1 will be get instance null
            if (StringUtils.isNotBlank(nacosClientVersion)) {
                nacosClientVersion = nacosClientVersion.replaceAll("\\.", "").split("-")[0];
                if (Integer.parseInt(nacosClientVersion) == 220 || Integer.parseInt(nacosClientVersion) == 221) {
                    List<Instance> instances = naming.getAllInstances(serviceName);
                    log.info("naming getAllInstances value=" + instances);
                    Assertions.assertEquals(instances.size(), 1);

                    List<Instance> instancesExist = namingDirect.getAllInstances(serviceName);
                    List<Instance> instancesEmpty = namingEndpoint.getAllInstances(serviceName);
                    log.info("namingDirect getAllInstances value=" + instancesExist + ", namingEndpoint getAllInstances value=" + instancesEmpty);
                    Assertions.assertEquals(0, instancesExist.size());
                    Assertions.assertEquals(1, instancesEmpty.size());
                }
            } else {    // it fixed in client 2.2.2
                List<Instance> instances = naming.getAllInstances(serviceName);
                log.info("naming getAllInstances value=" + instances);
                Assertions.assertEquals(1, instances.size());

                List<Instance> instancesExist = namingDirect.getAllInstances(serviceName);
                List<Instance> instancesEmpty = namingEndpoint.getAllInstances(serviceName);
                log.info("namingDirect getAllInstances value=" + instancesExist + ", namingEndpoint getAllInstances value=" + instancesEmpty);

                Assertions.assertEquals(instancesExist.size(), 1);
                Assertions.assertEquals("DEFAULT_GROUP@@" + serviceName, instancesExist.get(0).getServiceName());
                Assertions.assertEquals(NamingBase.TEST_IP_4_DOM_1, instancesExist.get(0).getIp());
                Assertions.assertEquals(NamingBase.TEST_PORT, instancesExist.get(0).getPort());

                Assertions.assertEquals(0, instancesEmpty.size());
            }

        } else {
            //when nacos.use.endpoint.parsing.rule=true, it will be success
            List<Instance> instances = naming.getAllInstances(serviceName);
            log.info("naming getAllInstances value=" + instances);
            Assertions.assertEquals(1, instances.size());

            List<Instance> instancesExist = namingEndpoint.getAllInstances(serviceName);
            List<Instance> instancesEmpty = namingDirect.getAllInstances(serviceName);
            log.info("namingDirect getAllInstances value=" + instancesEmpty + ", namingEndpoint getAllInstances value=" + instancesExist);

            Assertions.assertEquals(1, instancesExist.size());
            Assertions.assertEquals("DEFAULT_GROUP@@"+serviceName, instancesExist.get(0).getServiceName());
            Assertions.assertEquals(NamingBase.TEST_IP_4_DOM_1, instancesExist.get(0).getIp());
            Assertions.assertEquals(NamingBase.TEST_PORT, instancesExist.get(0).getPort());

            Assertions.assertEquals(0, instancesEmpty.size());
        }

    }
}

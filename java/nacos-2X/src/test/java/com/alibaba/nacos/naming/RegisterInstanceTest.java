package com.alibaba.nacos.naming;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.enums.TESTSET;
import com.alibaba.nacos.util.ConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Tag(TESTSET.NAMING)
public class RegisterInstanceTest extends NamingBase{
    private static final Logger log = LoggerFactory.getLogger(RegisterInstanceTest.class);
    private List<String> cleanServiceNames = new ArrayList();
    private String serviceName;

    @BeforeEach
    public void setUp(TestInfo testInfo) throws Exception{
        serviceName = randomDomainName();
        log.info("Running test=" + testInfo.getTestMethod() + ", serviceName=" + serviceName);
    }

    @AfterEach
    public void tearDown() throws Exception {
        List<String> remove = new ArrayList();
        for (String serviceName : cleanServiceNames) {
            HttpRestResult deleteResult = deleteService(serviceName, namespace);
            log.info("deleteResult " + serviceName + ":" + deleteResult.getCode());
            if (deleteResult.getCode() == 200) {
                remove.add(serviceName);
            }
        }
        log.info("deleteService list:" + ConvertUtils.listToString(remove));
        for (String serviceName : remove) {
            cleanServiceNames.remove(serviceName);
        }
    }

    @Test
    @DisplayName("RegisterInstance set metadata，expect getAllInstances contain register one.")
    public void testRegDomWithInstance_basic() throws Exception {
        Instance instance = new Instance();
        instance.setIp("127.0.0.1");
        instance.setPort(TEST_PORT);
        instance.setHealthy(true);
        instance.setWeight(2.0);
        Map<String, String> instanceMeta = new HashMap<>();
        instanceMeta.put("tlstest", "manlingtest");
        instance.setMetadata(instanceMeta);

        instance.setServiceName(serviceName);
        instance.setClusterName("c1");

        naming.registerInstance(serviceName, instance);

        TimeUnit.SECONDS.sleep(10);

        List<Instance> instances = naming.getAllInstances(serviceName);

        Assertions.assertEquals(instances.size(), 1);

        Assertions.assertTrue(NamingBase.verifyInstance(instance, instances.get(0)));

    }

    @Test
    @DisplayName("Register one default instance and check")
    public void testRegDom() throws Exception{
        naming.registerInstance(serviceName, NamingBase.TEST_IP_4_DOM_1, NamingBase.TEST_PORT);

        TimeUnit.SECONDS.sleep(10);

        List<Instance> instances = naming.getAllInstances(serviceName);
        log.info("instances=" + JSON.toJSONString(instances));

        Assertions.assertEquals(instances.size(), 1);
        Assertions.assertEquals("DEFAULT_GROUP@@"+serviceName,instances.get(0).getServiceName());
        Assertions.assertEquals(instances.get(0).getIp(), NamingBase.TEST_IP_4_DOM_1);
        Assertions.assertEquals(instances.get(0).getPort(), NamingBase.TEST_PORT);
        Assertions.assertEquals( "DEFAULT", instances.get(0).getClusterName());

    }

    @Test
    @DisplayName("Register instance use appointed cluster and check")
    public void testRegDomCluster() throws Exception{
        naming.registerInstance(serviceName, NamingBase.TEST_IP_4_DOM_1, NamingBase.TEST_PORT, NamingBase.TEST_NEW_CLUSTER_4_DOM_1);
        TimeUnit.SECONDS.sleep(10);

        List<Instance> instances = naming.getAllInstances(serviceName);

        Assertions.assertEquals(instances.size(), 1);
        //Assertions.assertTrue(instances.get(0).getServiceName().contains(serviceName));
        Assertions.assertEquals(instances.get(0).getIp(), NamingBase.TEST_IP_4_DOM_1);
        Assertions.assertEquals(instances.get(0).getPort(), NamingBase.TEST_PORT);
        Assertions.assertEquals(NamingBase.TEST_NEW_CLUSTER_4_DOM_1,instances.get(0).getClusterName());

        List<Instance> instances2 = naming.getAllInstances(serviceName, Arrays.asList(NamingBase.TEST_NEW_CLUSTER_4_DOM_1));

        Assertions.assertEquals(instances2.size(), 1);
        Assertions.assertTrue(instances2.get(0).getServiceName().contains(serviceName));
        Assertions.assertEquals(instances2.get(0).getIp(), NamingBase.TEST_IP_4_DOM_1);
        Assertions.assertEquals(instances2.get(0).getPort(), NamingBase.TEST_PORT);
        Assertions.assertEquals(instances2.get(0).getClusterName(),NamingBase.TEST_NEW_CLUSTER_4_DOM_1);
    }

    @Test
    @DisplayName("Register one  user-defined instance and check.")
    public void testRegDomWithInstance() throws Exception {
        Instance i1 = NamingBase.getInstance(serviceName);
        naming.registerInstance(serviceName, i1);

        TimeUnit.SECONDS.sleep(10);

        List<Instance> instances = naming.getAllInstances(serviceName);

        Assertions.assertEquals(instances.size(), 1);

        Assertions.assertTrue(NamingBase.verifyInstance(i1, instances.get(0)));

    }

    @Test
    @DisplayName("Register instance with special words metadata.")
    public void testRegServiceWithSpecialWordsMetadata() throws NacosException,
        InterruptedException {
        Instance instance = NamingBase.getInstance(serviceName);
        instance.getMetadata().put("spWords", SPECIAL_CHARACTERS);
        naming.registerInstance(serviceName, instance);
        TimeUnit.SECONDS.sleep(5);

        List<Instance> instances = naming.getAllInstances(serviceName,false);

        Assertions.assertEquals(1, instances.size());
        //log.info(instances.get(0).getMetadata().size());
        Assertions.assertEquals(2, instances.get(0).getMetadata().size());
        Assertions.assertEquals(SPECIAL_CHARACTERS, instances.get(0).getMetadata().get("spWords"));

    }

    @Test
    @DisplayName("Register instance with special words cluster Name.")
    public void testRegServiceWithSpecialWordsClusterName() throws Exception{
        Instance instance = NamingBase.getInstance(serviceName);
        instance.setClusterName(SPECIAL_CHARACTERS);
        Assertions.assertThrows(NacosException.class, () -> {
            naming.registerInstance(serviceName, instance);
        });

        String clusterName = "0-9a-zA-Z-";
        instance.setClusterName(clusterName);
        naming.registerInstance(serviceName, instance);
        TimeUnit.SECONDS.sleep(5);

        List<Instance> instances = naming.getAllInstances(serviceName,false);

        Assertions.assertEquals(1, instances.size());
        Assertions.assertEquals(clusterName, instances.get(0).getClusterName());
    }

    @Test
    @DisplayName("Register instance with underline cluster Name.")
    public void testRegServiceWithUnderlineClusterName() throws Exception{
        Instance instance = NamingBase.getInstance(serviceName);

        // server 1.x and up 2.1.1 will not accept clusterName with underline
        // client up 2.1.1 will not accept clusterName with underline
        if (StringUtils.isNotBlank(nacosServerVersion) && StringUtils.isNotBlank(nacosClientVersion) ) {
            nacosClientVersion = nacosClientVersion.replaceAll("\\.", "").split("-")[0];
            nacosServerVersion = nacosServerVersion.replaceAll("\\.", "");
            String clusterName = "checkClusterName_underline";
            instance.setClusterName(clusterName);
            if (Integer.parseInt(nacosClientVersion) < 211) {
                if (Integer.parseInt(nacosServerVersion) < 211 && !nacosServerVersion.startsWith("1")) {

                    naming.registerInstance(serviceName, instance);
                    TimeUnit.SECONDS.sleep(5);

                    List<Instance> instances = naming.getAllInstances(serviceName,false);

                    Assertions.assertEquals(1, instances.size());
                    Assertions.assertEquals(clusterName, instances.get(0).getClusterName());
                } else {
                    Throwable exception = Assertions.assertThrows(NacosException.class, () -> {
                        naming.registerInstance(serviceName, instance);
                    });
                    log.info("checkClusterName_underline " + exception.getMessage());
                }
            } else {
                Throwable exception = Assertions.assertThrows(NacosException.class, () -> {
                    naming.registerInstance(serviceName, instance);
                });
                log.info("checkClusterName_underline " + exception.getMessage());
            }
        }
    }
}

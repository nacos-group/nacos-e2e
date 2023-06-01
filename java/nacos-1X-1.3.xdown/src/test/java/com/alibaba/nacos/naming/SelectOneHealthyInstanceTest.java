package com.alibaba.nacos.naming;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.net.HttpClient;
import com.alibaba.nacos.util.ConvertUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SelectOneHealthyInstanceTest extends NamingBase{
    private static final Logger log = LoggerFactory.getLogger(SelectOneHealthyInstanceTest.class);
    private List<String> cleanServiceNames = new ArrayList();
    private String serviceName;
    private static NamingService naming1;

    @BeforeAll
    public static void setUpAll() throws Exception {
        naming1 = NacosFactory.createNamingService(properties1);
    }

    @BeforeEach
    public void setUp(TestInfo testInfo) throws Exception{
        serviceName = randomDomainName();
        log.info("Running test=" + testInfo.getTestMethod() + ", serviceName=" + serviceName);
    }

    @AfterEach
    public void tearDown() throws Exception {
        List<String> remove = new ArrayList();
        for (String serviceName : cleanServiceNames) {
            HttpClient.HttpResult deleteResult = deleteService(serviceName, namespace);
            log.info("deleteResult " + serviceName + ":" + deleteResult.code);
            if (deleteResult.code == 200) {
                remove.add(serviceName);
            }
        }
        log.info("deleteService list:" + ConvertUtils.listToString(remove));
        for (String serviceName : remove) {
            cleanServiceNames.remove(serviceName);
        }
    }
    
    @Test
    @DisplayName("Select one healthy instances.")
    public void testSelectOneHealthyInstances() throws Exception {
        naming.registerInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT);
        naming.registerInstance(serviceName, "127.0.0.1", 60000);

        TimeUnit.SECONDS.sleep(10);
        //随机获取一个健康的Instance
        Instance instance = naming.selectOneHealthyInstance(serviceName);

        List<Instance> instancesGet = naming.getAllInstances(serviceName);

        for (Instance instance1 : instancesGet) {
            if (instance1.getIp().equals(instance.getIp())&&
                instance1.getPort() == instance.getPort()) {
                Assertions.assertTrue(instance.isHealthy());
                Assertions.assertTrue(NamingBase.verifyInstance(instance1, instance));
                return;
            }
        }
        Assertions.assertTrue(false);
    }
    
    @Test
    @DisplayName("Select one healthy instances in appointed cluster.")
    public void testSelectOneHealthyInstancesCluster() throws Exception {
        naming.registerInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT, "c1");
        naming.registerInstance(serviceName, "127.0.0.1", 60000, "c1");
        naming.registerInstance(serviceName, "1.1.1.1", NamingBase.TEST_PORT, "c1");
        naming.registerInstance(serviceName, "127.0.0.1", 60001, "c1");
        naming1.registerInstance(serviceName, "127.0.0.1", 60002, "c2");
        TimeUnit.SECONDS.sleep(10);
        Instance instance = naming.selectOneHealthyInstance(serviceName, Arrays.asList("c1"));

        Assertions.assertTrue(instance.getIp() != "1.1.1.1");
        Assertions.assertTrue(instance.getPort() != 60002);

        List<Instance> instancesGet = naming.getAllInstances(serviceName);

        for (Instance instance1 : instancesGet) {
            if (instance1.getIp().equals(instance.getIp())&&
                instance1.getPort() == instance.getPort()) {
                Assertions.assertTrue(instance.isHealthy());
                Assertions.assertTrue(NamingBase.verifyInstance(instance1, instance));
                log.info("instance="+ JSON.toJSONString(instance));
                return;
            }
        }

        Assertions.assertTrue(false);
    }
    
    @Test
    @DisplayName("Select one healthy instances in multi cluster.")
    public void testSelectOneHealthyInstancesClusters() throws Exception {
        naming.registerInstance(serviceName, "1.1.1.1", NamingBase.TEST_PORT, "c1");
        naming.registerInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT, "c1");
        naming.registerInstance(serviceName, "127.0.0.1", 60000, "c1");
        naming.registerInstance(serviceName, "127.0.0.1", 60001, "c2");

        TimeUnit.SECONDS.sleep(10);
        Instance instance = naming.selectOneHealthyInstance(serviceName, Arrays.asList("c2","c1"));
        Assertions.assertTrue(instance.getIp() != "1.1.1.1");

        List<Instance> instancesGet = naming.getAllInstances(serviceName);

        for (Instance instance1 : instancesGet) {
            if (instance1.getIp().equals(instance.getIp()) &&
                instance1.getPort() == instance.getPort()) {
                Assertions.assertTrue(instance.isHealthy());
                Assertions.assertTrue(NamingBase.verifyInstance(instance1, instance));
                return;
            }
        }

        Assertions.assertTrue(false);
    }
    
}

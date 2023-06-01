package com.alibaba.nacos.naming;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.enums.TESTSET;
import com.alibaba.nacos.util.ConvertUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Tag(TESTSET.NAMING)
public class SelectInstancesTest extends NamingBase{
    private static final Logger log = LoggerFactory.getLogger(SelectInstancesTest.class);
    private List<String> cleanServiceNames = new ArrayList();
    private String serviceName;
    private volatile List<Instance> instances;
    private static NamingService namingNewConn;

    @BeforeAll
    public static void setUpAll() throws Exception {
        NamingBase.setUpAll();
        namingNewConn = NacosFactory.createNamingService(properties);
    }

    @AfterAll
    public static void tearDownAll() throws Exception {
        NamingBase.tearDownAll();
    }

    @BeforeEach
    public void setUp(TestInfo testInfo) throws Exception{
        instances = Collections.emptyList();
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
    @DisplayName("Get all healthy instances.")
    public void testSelectHealthyInstances() throws Exception {
        naming.registerInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT);
        namingNewConn.registerInstance(serviceName, "1.1.1.1", 9090);

        TimeUnit.SECONDS.sleep(10);

        List<Instance> instances = naming.selectInstances(serviceName, true);

        Assertions.assertEquals(2, instances.size());

        Instance instanceNotH = null;
        List<Instance> instancesGet = naming.getAllInstances(serviceName);
        for (Instance instance : instancesGet) {
            if (!instance.isHealthy()) {
                instanceNotH = instance;
            }
        }
        instancesGet.remove(instanceNotH);

        Assertions.assertTrue(NamingBase.verifyInstanceList(instances, instancesGet));
    }

    @Test
    @DisplayName("Get all unhealthy instances in default cluster.")
    public void testSelectUnhealthyInstances() throws Exception {
        naming.registerInstance(serviceName, "1.1.1.1", NamingBase.TEST_PORT);
        namingNewConn.registerInstance(serviceName, "1.1.1.2", NamingBase.TEST_PORT);

        TimeUnit.SECONDS.sleep(8);

        List<Instance> instances = naming.selectInstances(serviceName, false);

        TimeUnit.SECONDS.sleep(2);
        Assertions.assertEquals(0, instances.size());

        List<Instance> instancesGet = naming.getAllInstances(serviceName);

        Assertions.assertTrue(NamingBase.verifyInstanceList(instances, instancesGet));
    }

    @Test
    @DisplayName("Get all healthy instance from appointed cluster(single or multi).")
    public void testSelectHealthyInstancesClusters() throws Exception {
        naming.registerInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT, "c1");
        namingNewConn.registerInstance(serviceName, "127.0.0.2", 9090, "c2");

        TimeUnit.SECONDS.sleep(10);
        List<Instance> instances = naming
            .selectInstances(serviceName, Arrays.asList("c1", "c2"), true);
        Assertions.assertEquals(2, instances.size());

        List<Instance> instancesGet = naming.getAllInstances(serviceName);
        Assertions.assertTrue(NamingBase.verifyInstanceList(instances, instancesGet));
    }

    @Test
    @DisplayName("Get all unhealthy instance from appointed cluster(single or multi).")
    public void testSelectUnhealthyInstancesClusters() throws Exception {
        naming.registerInstance(serviceName, "1.1.1.1", NamingBase.TEST_PORT, "c1");
        naming.registerInstance(serviceName, "1.1.1.2", NamingBase.TEST_PORT, "c2");

        TimeUnit.SECONDS.sleep(8);
        List<Instance> instances = naming
            .selectInstances(serviceName, Arrays.asList("c1", "c2"), false);
        Assertions.assertEquals(instances.size(), 0);

        List<Instance> instancesGet = naming.getAllInstances(serviceName);

        Assertions.assertTrue(NamingBase.verifyInstanceList(instances, instancesGet));
    }

    @Test
    @DisplayName("Get instance which weight is not zero.")
    public void testSelectAllWeightedInstances() throws Exception {
        naming.registerInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT);
        namingNewConn.registerInstance(serviceName, "1.1.1.1", 9090);

        TimeUnit.SECONDS.sleep(10);

        List<Instance> instances = naming.getAllInstances(serviceName);

        Assertions.assertEquals(2, instances.size());

        instances = naming.selectInstances(serviceName, true);

        Assertions.assertEquals(2, instances.size());
        //log.info("2:"+instances.size());
        instances.get(0).setWeight(0);

        instances = naming.selectInstances(serviceName, true);

        Assertions.assertEquals(1, instances.size());

        Instance instanceNotH = null;
        List<Instance> instancesGet = naming.getAllInstances(serviceName);

        for (Instance instance : instancesGet) {
            if (!instance.isHealthy() || !instance.isEnabled() || instance.getWeight() <= 0) {

                instanceNotH = instance;
            }
        }

        instancesGet.remove(instanceNotH);

        Assertions.assertTrue(NamingBase.verifyInstanceList(instances, instancesGet));
    }

    @Test
    @DisplayName("Get instance which weight is not zero from appointed cluster(single or multi).")
    public void testSelectAllWeightedInstancesClusters() throws Exception {
        naming.registerInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT, "c1");
        namingNewConn.registerInstance(serviceName, "1.1.1.1", 9090, "c2");

        TimeUnit.SECONDS.sleep(10);

        List<Instance> instances = naming.getAllInstances(serviceName);

        Assertions.assertEquals(2, instances.size());

        instances = naming.selectInstances(serviceName, Arrays.asList("c1", "c2"), true);

        Assertions.assertEquals(2, instances.size());

        instances.get(0).setWeight(0);

        instances = naming.selectInstances(serviceName, Arrays.asList("c1", "c2"), true);

        Assertions.assertEquals(1, instances.size());

        Instance instanceNotH = null;
        List<Instance> instancesGet = naming.getAllInstances(serviceName);

        for (Instance instance : instancesGet) {
            if (!instance.isHealthy() || !instance.isEnabled() || instance.getWeight() <= 0) {

                instanceNotH = instance;
            }
        }

        instancesGet.remove(instanceNotH);

        Assertions.assertTrue(NamingBase.verifyInstanceList(instances, instancesGet));
    }

    @Test
    @DisplayName("Get instance which enable is true.")
    public void testSelectAllEnabledInstances() throws Exception {
        naming.registerInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT);
        namingNewConn.registerInstance(serviceName, "1.1.1.1", 9090);

        TimeUnit.SECONDS.sleep(10);

        List<Instance> instances = naming.getAllInstances(serviceName);

        Assertions.assertEquals(2, instances.size());

        instances = naming.selectInstances(serviceName, true);

        Assertions.assertEquals(2, instances.size());
        instances.get(0).setEnabled(false);
        instances = naming.selectInstances(serviceName, true);

        Assertions.assertEquals(1, instances.size());

        Instance instanceNotH = null;
        List<Instance> instancesGet = naming.getAllInstances(serviceName);

        for (Instance instance : instancesGet) {
            if (!instance.isHealthy() || !instance.isEnabled() || instance.getWeight() <= 0) {

                instanceNotH = instance;
            }
        }

        instancesGet.remove(instanceNotH);

        Assertions.assertTrue(NamingBase.verifyInstanceList(instances, instancesGet));
    }

    @Test
    @DisplayName("Get instance which enable is true from appointed cluster(single or multi).")
    public void testSelectAllEnabledInstancesClusters() throws Exception {
        naming.registerInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT, "c1");
        namingNewConn.registerInstance(serviceName, "1.1.1.1", 9090, "c2");

        TimeUnit.SECONDS.sleep(10);

        List<Instance> instances = naming.getAllInstances(serviceName);

        Assertions.assertEquals(2, instances.size());

        instances = naming.selectInstances(serviceName, Arrays.asList("c1", "c2"), true);

        Assertions.assertEquals(2, instances.size());

        instances.get(0).setEnabled(false);
        instances = naming.selectInstances(serviceName, Arrays.asList("c1", "c2"), true);

        Assertions.assertEquals(1, instances.size());

        Instance instanceNotH = null;
        List<Instance> instancesGet = naming.getAllInstances(serviceName);

        for (Instance instance : instancesGet) {
            if (!instance.isHealthy() || !instance.isEnabled() || instance.getWeight() <= 0) {

                instanceNotH = instance;
            }
        }

        instancesGet.remove(instanceNotH);

        Assertions.assertTrue(NamingBase.verifyInstanceList(instances, instancesGet));
    }
}

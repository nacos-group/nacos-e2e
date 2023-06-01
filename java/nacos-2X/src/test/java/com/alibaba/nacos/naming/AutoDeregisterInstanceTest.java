package com.alibaba.nacos.naming;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.enums.TESTSET;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Tag(TESTSET.NAMING)
public class AutoDeregisterInstanceTest extends NamingBase{
    private static final Logger log = LoggerFactory.getLogger(AutoDeregisterInstanceTest.class);
    private String serviceName;

    public static NamingService namingNewConn;
    private static NamingService naming1;
    private static NamingService naming1NewConn;

    @BeforeAll
    public static void setUpAll() throws Exception {
        NamingBase.setUpAll();
        namingNewConn = NacosFactory.createNamingService(properties);
        naming1 = NacosFactory.createNamingService(properties1);
        naming1NewConn = NacosFactory.createNamingService(properties1);
    }

    @BeforeEach
    public void setUp() throws Exception{
        serviceName = randomDomainName();

    }

    @Test
    @DisplayName("Register two instances in two diff cluster and shutDown namingNewConn")
    public void testAutoDregDomClusters() throws Exception {

        naming.registerInstance(serviceName, "127.0.0.1", TEST_PORT, "c1");
        namingNewConn.registerInstance(serviceName, "127.0.0.2", TEST_PORT, "c2");

        TimeUnit.SECONDS.sleep(TIME_OUT);

        List<Instance> instances = namingNewConn.getAllInstances(serviceName);
        Assertions.assertEquals(2, instances.size());
        namingNewConn.shutDown();
        TimeUnit.SECONDS.sleep(TIME_OUT);

        verifyInstanceList(instances, 1, serviceName);

        TimeUnit.SECONDS.sleep(TIME_OUT);
        instances = naming.getAllInstances(serviceName);
        Assertions.assertEquals(1, instances.size());

        instances = naming.getAllInstances(serviceName, Arrays.asList("c1"));

        Assertions.assertEquals(1, instances.size());

        instances = naming.getAllInstances(serviceName, Arrays.asList("c2"));

        Assertions.assertEquals(0, instances.size());

    }

    @Test
    @Disabled("shutDown is invalid!!")
    @DisplayName("Register two instances in default cluster and shutDown namingNewConn")
    public void testAutoDregDom() throws Exception {
        naming1NewConn.registerInstance(serviceName, "127.0.0.1", TEST_PORT);
        naming1NewConn.registerInstance(serviceName, "127.0.0.2", TEST_PORT);
        TimeUnit.SECONDS.sleep(5);
        List<Instance> instances = naming1.getAllInstances(serviceName);

        Assertions.assertEquals(1, instances.size());
        naming1NewConn.shutDown();

        verifyInstanceList(instances, 1, serviceName);
        TimeUnit.SECONDS.sleep(TIME_OUT*6);

        instances = naming1.getAllInstances(serviceName);
        Assertions.assertEquals(0, instances.size());
    }
}

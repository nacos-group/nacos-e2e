package com.alibaba.nacos.naming;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.enums.TESTSET;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

// this test class should be serial execute, if parallel execute will make data conflict
@Execution(ExecutionMode.SAME_THREAD)
@Tag(TESTSET.NAMING)
public class DeregisterInstanceTest extends NamingBase{
    private static final Logger log = LoggerFactory.getLogger(DeregisterInstanceTest.class);
    private String serviceName;

    public static NamingService namingNewConn;
    public static NamingService naming1;
    public static NamingService naming1NewConn;

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
    @DisplayName("Register two instances in default cluster and deregister one ip")
    public void testDregDom() throws Exception {
        naming.registerInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT);
        namingNewConn.registerInstance(serviceName, "127.0.0.2", NamingBase.TEST_PORT);

        TimeUnit.SECONDS.sleep(TIME_OUT);

        List<Instance> instances = naming.getAllInstances(serviceName);
        Assertions.assertEquals(instances.size(), 2);
        TimeUnit.SECONDS.sleep(TIME_OUT);

        naming.deregisterInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT);
        TimeUnit.SECONDS.sleep(12);

        instances = naming.getAllInstances(serviceName);

        Assertions.assertEquals(1, instances.size());
        Assertions.assertEquals(instances.get(0).getIp(), "127.0.0.2");
    }

    @Test
    @DisplayName("Register two instances in two diff cluster and deregister one ip")
    public void testDeregisterDomCluster() throws Exception {
        naming1.registerInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT, "c1");
        naming1NewConn.registerInstance(serviceName, "127.0.0.2", NamingBase.TEST_PORT, "c2");
        TimeUnit.SECONDS.sleep(TIME_OUT);

        List<Instance> instances = naming1.getAllInstances(serviceName);

        Assertions.assertEquals(2, instances.size());

        naming1.deregisterInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT, "c1");
        TimeUnit.SECONDS.sleep(12);

        instances = naming1.getAllInstances(serviceName);

        Assertions.assertEquals(1, instances.size());

        instances = naming1.getAllInstances(serviceName, Arrays.asList("c2"));
        Assertions.assertEquals(1, instances.size());

        List<Instance> instances1 = naming1.getAllInstances(serviceName, Arrays.asList("c1"));
        Assertions.assertEquals(0, instances1.size());
    }

}

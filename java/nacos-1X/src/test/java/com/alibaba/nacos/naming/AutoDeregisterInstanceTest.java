package com.alibaba.nacos.naming;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.alibaba.nacos.client.naming.beat.BeatInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AutoDeregisterInstanceTest extends NamingBase{
    private static final Logger log = LoggerFactory.getLogger(AutoDeregisterInstanceTest.class);
    private String serviceName;

    public static NamingService namingNewConn;
    private static NamingService naming1;
    private static NamingService naming1NewConn;

    @BeforeAll
    public static void setUpAll() throws Exception {
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
    @DisplayName("Register two instances in default cluster and removeBeatInfo for service.")
    public void testAutoDregDom() throws Exception {
        naming.registerInstance(serviceName, "127.0.0.1", TEST_PORT);
        naming.registerInstance(serviceName, "127.0.0.2", TEST_PORT);
        TimeUnit.SECONDS.sleep(5);
        List<Instance> instances = naming.getAllInstances(serviceName);

        Assertions.assertEquals(2, instances.size());
        NacosNamingService namingServiceImpl = (NacosNamingService) naming;
        namingServiceImpl.getBeatReactor().
            removeBeatInfo(Constants.DEFAULT_GROUP + Constants.SERVICE_INFO_SPLITER + serviceName, "127.0.0.1", TEST_PORT);
        verifyInstance(getInstance(serviceName,TEST_IP_4_DOM_2), 1, serviceName);
        // Wait sync complete
        instances = naming.getAllInstances(serviceName);
        Assertions.assertEquals(1, instances.size());
    }

    @Test
    @DisplayName("Register two instances in default cluster, removeBeatInfo for service will "
        + "deregister instance, add BeatInfo for service will auto register instance.")
    public void testAutoRegDom() throws Exception {
        naming.registerInstance(serviceName, "127.0.0.1", TEST_PORT);
        naming.registerInstance(serviceName, "127.0.0.2", TEST_PORT);
        TimeUnit.SECONDS.sleep(5);
        List<Instance> instances = naming.getAllInstances(serviceName);

        Assertions.assertEquals(instances.size(), 2);
        NacosNamingService namingServiceImpl = (NacosNamingService) naming;
        namingServiceImpl.getBeatReactor().
            removeBeatInfo(Constants.DEFAULT_GROUP + Constants.SERVICE_INFO_SPLITER + serviceName, "127.0.0.1", TEST_PORT);
        verifyInstance(getInstance(serviceName,TEST_IP_4_DOM_2), 1, serviceName);

        instances = naming.getAllInstances(serviceName);
        naming.registerInstance(serviceName, "127.0.0.1", TEST_PORT);
        Assertions.assertEquals(instances.size(), 1);

        BeatInfo beatInfo = new BeatInfo();
        beatInfo.setServiceName(Constants.DEFAULT_GROUP + Constants.SERVICE_INFO_SPLITER + serviceName);
        beatInfo.setIp("127.0.0.1");
        beatInfo.setPort(TEST_PORT);
        beatInfo.setWeight(1);
        //beatInfo.setMetadata();
        namingServiceImpl.getBeatReactor().
            addBeatInfo(Constants.DEFAULT_GROUP + Constants.SERVICE_INFO_SPLITER + serviceName, beatInfo);

        verifyInstanceList(getInstancesList(serviceName), serviceName);

        //Wait sync complete
        instances = naming.getAllInstances(serviceName);

        Assertions.assertEquals(instances.size(), 2);
    }

    @Test
    @DisplayName("Register two instances in diff cluster, removeBeatInfo for service will deregister instance.")
    public void testAutoRegDomClusters() throws Exception {
        naming.registerInstance(serviceName, "127.0.0.1", TEST_PORT, "c1");
        naming.registerInstance(serviceName, "127.0.0.2", TEST_PORT, "c2");
        TimeUnit.SECONDS.sleep(5);

        List<Instance> instances = naming.getAllInstances(serviceName);
        Assertions.assertEquals(2, instances.size());

        NacosNamingService namingServiceImpl = (NacosNamingService) naming;
        namingServiceImpl.getBeatReactor().
            removeBeatInfo(Constants.DEFAULT_GROUP + Constants.SERVICE_INFO_SPLITER + serviceName, "127.0.0.1", TEST_PORT);

        verifyInstance(getInstanceAndCluster(serviceName,TEST_IP_4_DOM_2,"c2"), 1, serviceName);

        instances = naming.getAllInstances(serviceName);

        Assertions.assertEquals(1, instances.size());
        naming.registerInstance(serviceName, "127.0.0.1", TEST_PORT,"c1");
        BeatInfo beatInfo = new BeatInfo();
        beatInfo.setServiceName(Constants.DEFAULT_GROUP + Constants.SERVICE_INFO_SPLITER + serviceName);
        beatInfo.setIp("127.0.0.1");
        beatInfo.setPort(TEST_PORT);
        beatInfo.setCluster("c1");
        beatInfo.setWeight(1);
        beatInfo.setMetadata(null);
        namingServiceImpl.getBeatReactor().
            addBeatInfo(Constants.DEFAULT_GROUP + Constants.SERVICE_INFO_SPLITER + serviceName, beatInfo);
        //      Wait sync complete

        verifyInstanceList(getInstancesClustersList(serviceName), serviceName);
        instances = naming.getAllInstances(serviceName);
        log.info(JSON.toJSONString(instances));
        log.info(JSON.toJSONString(getInstancesClustersList(serviceName)));

        Assertions.assertEquals(instances.size(), 2);

        instances = naming.getAllInstances(serviceName, Arrays.asList("c2"));
        Assertions.assertEquals(1, instances.size());

        instances = naming.getAllInstances(serviceName, Arrays.asList("c1"));
        Assertions.assertEquals(1, instances.size());
    }
}

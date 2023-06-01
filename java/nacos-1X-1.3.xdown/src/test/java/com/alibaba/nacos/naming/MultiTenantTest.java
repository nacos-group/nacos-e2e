package com.alibaba.nacos.naming;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.client.naming.net.HttpClient;
import com.alibaba.nacos.util.ConvertUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

// this test class should be serial execute, if parallel execute will make data conflict
@Execution(ExecutionMode.SAME_THREAD)
public class MultiTenantTest extends NamingBase{
    private static final Logger log = LoggerFactory.getLogger(MultiTenantTest.class);
    private List<String> cleanServiceNames = new ArrayList();
    private String serviceName;
    private volatile List<Instance> instances;
    private static NamingService namingNewConn;
    private static NamingService naming1;
    private static NamingService naming1NewConn;
    private static NamingService naming2;

    @BeforeAll
    public static void setUpAll() throws Exception {
        namingNewConn = NacosFactory.createNamingService(properties);
        naming1 = NacosFactory.createNamingService(properties1);
        naming1NewConn = NacosFactory.createNamingService(properties1);
        naming2 = NacosFactory.createNamingService(properties2);
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
    @DisplayName("Multiple tenant register different instance in default group.")
    public void testMultipleTenant_registerInstance() throws Exception {
        naming1.registerInstance(serviceName, "11.11.11.11", 80);


        naming2.registerInstance(serviceName, "22.22.22.22", 80);

        naming.registerInstance(serviceName, "33.33.33.33", 8888);
        naming.registerInstance(serviceName, "44.44.44.44", 8888);

        TimeUnit.SECONDS.sleep(5L);

        List<Instance> instances = naming1.getAllInstances(serviceName);
        Assertions.assertEquals(1, instances.size());
        Assertions.assertEquals("11.11.11.11", instances.get(0).getIp());
        Assertions.assertEquals(80, instances.get(0).getPort());

        instances = naming2.getAllInstances(serviceName);
        Assertions.assertEquals(1, instances.size());
        Assertions.assertEquals("22.22.22.22", instances.get(0).getIp());
        Assertions.assertEquals(80, instances.get(0).getPort());

        instances = naming.getAllInstances(serviceName);
        Assertions.assertEquals(2, instances.size());
    }

    @Test
    @DisplayName("Multiple tenant register different instance in different group.")
    public void testMultipleTenant_multiGroup_registerInstance() throws Exception {
        naming1.registerInstance(serviceName, TEST_GROUP_1, "11.11.11.11", 80);

        naming2.registerInstance(serviceName, TEST_GROUP_2, "22.22.22.22", 80);

        naming.registerInstance(serviceName, "33.33.33.33", 8888);
        naming.registerInstance(serviceName, "44.44.44.44", 8888);

        TimeUnit.SECONDS.sleep(5L);

        List<Instance> instances = naming1.getAllInstances(serviceName);
        Assertions.assertEquals(0, instances.size());

        instances = naming2.getAllInstances(serviceName, TEST_GROUP_2);
        Assertions.assertEquals(1, instances.size());
        Assertions.assertEquals("22.22.22.22", instances.get(0).getIp());
        Assertions.assertEquals(80, instances.get(0).getPort());

        instances = naming.getAllInstances(serviceName);
        Assertions.assertEquals(2, instances.size());

        naming1.deregisterInstance(serviceName, TEST_GROUP_1, "11.11.11.11", 80);
        naming1.deregisterInstance(serviceName, TEST_GROUP_2, "22.22.22.22", 80);
        TimeUnit.SECONDS.sleep(5);

        instances = naming1.getAllInstances(serviceName, TEST_GROUP_1);
        Assertions.assertEquals(0, instances.size());
    }
    
    @Test
    @DisplayName("Multiple tenant register same instance in default group.")
    public void testMultipleTenant_equalIP() throws Exception {
        naming1.registerInstance(serviceName, "11.11.11.11", 80);

        naming2.registerInstance(serviceName, "11.11.11.11", 80);

        naming.registerInstance(serviceName, "11.11.11.11", 80);
        naming.registerInstance(serviceName, "22.22.22.22", 80);

        TimeUnit.SECONDS.sleep(5L);

        List<Instance> instances = naming1.getAllInstances(serviceName);
        log.info(JSON.toJSONString(instances));
        Assertions.assertEquals(1, instances.size());
        Assertions.assertEquals("11.11.11.11", instances.get(0).getIp());
        Assertions.assertEquals(80, instances.get(0).getPort());

        instances = naming2.getAllInstances(serviceName);

        Assertions.assertEquals(1, instances.size());
        Assertions.assertEquals("11.11.11.11", instances.get(0).getIp());
        Assertions.assertEquals(80, instances.get(0).getPort());

        instances = naming.getAllInstances(serviceName);
        Assertions.assertEquals(2, instances.size());
    }
    
    @Test
    @DisplayName("Multiple tenant register same instance in different group.")
    public void testMultipleTenant_group_equalIP() throws Exception {
        naming1.registerInstance(serviceName, TEST_GROUP_1, "11.11.11.11", 80);
        naming2.registerInstance(serviceName, TEST_GROUP_2, "11.11.11.11", 80);
        naming.registerInstance(serviceName, Constants.DEFAULT_GROUP, "11.11.11.11", 80);

        TimeUnit.SECONDS.sleep(5L);

        List<Instance> instances = naming1.getAllInstances(serviceName);
        Assertions.assertEquals(0, instances.size());

        instances = naming2.getAllInstances(serviceName, TEST_GROUP_2);
        Assertions.assertEquals(1, instances.size());
        Assertions.assertEquals("11.11.11.11", instances.get(0).getIp());
        Assertions.assertEquals(80, instances.get(0).getPort());

        instances = naming.getAllInstances(serviceName);
        Assertions.assertEquals(1, instances.size());
    }

    @Test
    @DisplayName("Multiple tenant register same instance in different group, and get all "
        + "instances from other group.")
    public void testMultipleTenant_group_getInstances() throws Exception {
        naming1.registerInstance(serviceName, TEST_GROUP_1, "11.11.11.11", 80);
        naming1.registerInstance(serviceName, TEST_GROUP_2, "11.11.11.11", 80);
        naming.registerInstance(serviceName, Constants.DEFAULT_GROUP, "11.11.11.11", 80);

        TimeUnit.SECONDS.sleep(5L);
        List<Instance> instances = naming1.getAllInstances(serviceName, TEST_GROUP);

        Assertions.assertEquals(0, instances.size());

        instances = naming.getAllInstances(serviceName);
        Assertions.assertEquals(1, instances.size());
        naming1.deregisterInstance(serviceName, TEST_GROUP_1, "11.11.11.11", 80);
        naming1.deregisterInstance(serviceName, TEST_GROUP_2, "11.11.11.11", 80);
    }
    
    @Test
    @DisplayName("Multiple tenant register same instance in default group, select correct " 
        + "instance.")
    public void testMultipleTenant_selectInstances() throws Exception {
        naming1.registerInstance(serviceName, TEST_IP_4_DOM_1, TEST_PORT);

        naming2.registerInstance(serviceName, "22.22.22.22", 80);

        naming.registerInstance(serviceName, TEST_IP_4_DOM_1, TEST_PORT);
        namingNewConn.registerInstance(serviceName, "44.44.44.44", 8888);

        TimeUnit.SECONDS.sleep(5L);

        List<Instance> instances = naming1.selectInstances(serviceName, true);

        Assertions.assertEquals(1, instances.size());
        Assertions.assertEquals(TEST_IP_4_DOM_1, instances.get(0).getIp());
        Assertions.assertEquals(TEST_PORT, instances.get(0).getPort());

        instances = naming2.selectInstances(serviceName, false);
        Assertions.assertEquals(0, instances.size());

        instances = naming.selectInstances(serviceName, true);
        Assertions.assertEquals(2, instances.size());
    }
    
    @Test
    @DisplayName("Multiple tenant register same service with default group instance, check getServicesOfServer")
    public void testMultipleTenant_getServicesOfServer() throws Exception {
        naming1.registerInstance(serviceName, "11.11.11.11", TEST_PORT, "c1");
        TimeUnit.SECONDS.sleep(5L);
        ListView<String> listView = naming1.getServicesOfServer(1, 200);
        ListView<String> listView1 = naming2.getServicesOfServer(1, 200);
        Assertions.assertTrue(listView.getData().contains(serviceName));
        Assertions.assertFalse(listView1.getData().contains(serviceName));

        naming2.registerInstance(serviceName, "33.33.33.33", TEST_PORT, "c1");
        TimeUnit.SECONDS.sleep(5L);
        listView = naming1.getServicesOfServer(1, 200);
        listView1 = naming2.getServicesOfServer(1, 200);

        Assertions.assertTrue(listView.getData().contains(serviceName));
        Assertions.assertTrue(listView1.getData().contains(serviceName));
        Assertions.assertEquals("11.11.11.11", naming1.getAllInstances(serviceName).get(0).getIp());
        // count is not stable, because the execute order or service auto clean will cause the two counts will be diff
        //Assertions.assertEquals(listView.getCount(), listView1.getCount());
    }
    
    @Test
    @DisplayName("Multiple tenant register same service with diff group instance, check " 
        + "getServicesOfServer")
    public void testMultipleTenant_group_getServicesOfServer() throws Exception {
        naming1.registerInstance(serviceName, TEST_GROUP_1, "11.11.11.11", TEST_PORT, "c1");
        naming1NewConn.registerInstance(serviceName, TEST_GROUP_2, "22.22.22.22", TEST_PORT, "c1");

        TimeUnit.SECONDS.sleep(5L);

        ListView<String> listView = naming1.getServicesOfServer(1, 200, TEST_GROUP_1);
        //listView = naming1.getServicesOfServer(1, listView.getCount()+1, TEST_GROUP_1);
        ListView<String> listView1 = naming1.getServicesOfServer(1, 200, TEST_GROUP_2);
        //listView1 = naming1.getServicesOfServer(1, listView1.getCount()+1, TEST_GROUP_2);

        //log.info(listView1.getCount());
        Assertions.assertTrue(listView.getData().contains(serviceName));
        Assertions.assertTrue(listView1.getData().contains(serviceName));
        naming2.registerInstance(serviceName, "33.33.33.33", TEST_PORT, "c1");
        TimeUnit.SECONDS.sleep(5L);
        ListView<String> listView2 = naming1.getServicesOfServer(1, 200, Constants.DEFAULT_GROUP);
        //listView2 = naming1.getServicesOfServer(1, listView2.getCount(), Constants.DEFAULT_GROUP);
        //log.info(listView2.getCount());

        Assertions.assertFalse(listView2.getData().contains(serviceName));
        // count is not stable, because the execute order or service auto clean will cause the two counts will be diff
        // Assertions.assertEquals(listView.getCount(), listView1.getCount());
    }

    @Test
    @DisplayName("Multiple tenant subscribe same service in default group.")
    public void testMultipleTenant_subscribe() throws Exception {
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(Event event) {
                instances = ((NamingEvent) event).getInstances();
            }
        };

        try {
            naming1.subscribe(serviceName, listener);
            naming1.registerInstance(serviceName, "11.11.11.11", TEST_PORT, "c1");
            naming2.registerInstance(serviceName, "33.33.33.33", TEST_PORT, "c1");

            while (instances.size() == 0) {
                TimeUnit.SECONDS.sleep(1L);
                log.info("instances=" + JSON.toJSONString(instances));
            }
            Assertions.assertEquals(1, instances.size());

            TimeUnit.SECONDS.sleep(2L);
            Assertions.assertTrue(verifyInstanceList(instances, naming1.getAllInstances(serviceName)));
        } finally {
            naming1.unsubscribe(serviceName, listener);
        }
    }

    @Test
    @DisplayName("Multiple tenant subscribe same service in diff group.")
    public void testMultipleTenant_group_subscribe() throws Exception {
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(Event event) {
                instances = ((NamingEvent) event).getInstances();
            }
        };

        try {
            naming1.subscribe(serviceName, TEST_GROUP_1, listener);
            naming1.registerInstance(serviceName, "11.11.11.11", TEST_PORT, "c1");
            naming1.registerInstance(serviceName, TEST_GROUP_1, "33.33.33.33", TEST_PORT, "c1");

            while (instances.size() == 0) {
                TimeUnit.SECONDS.sleep(1L);
            }
            TimeUnit.SECONDS.sleep(2L);
            Assertions.assertEquals(1, instances.size());

            TimeUnit.SECONDS.sleep(2L);
            Assertions.assertTrue(verifyInstanceList(instances, naming1.getAllInstances(serviceName, TEST_GROUP_1)));

            naming1.deregisterInstance(serviceName, "11.11.11.11", TEST_PORT, "c1");
            naming1.deregisterInstance(serviceName, TEST_GROUP_1, "33.33.33.33", TEST_PORT, "c1");
        } finally {
            naming1.unsubscribe(serviceName, TEST_GROUP_1, listener);
        }
    }

    @Test
    @DisplayName("Multiple tenant unsubscribe same service in default group.")
    public void testMultipleTenant_unSubscribe() throws Exception {
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(Event event) {
                log.info(((NamingEvent) event).getServiceName());
                instances = ((NamingEvent) event).getInstances();
            }
        };

        try {
            naming1.subscribe(serviceName, listener);
            naming1.registerInstance(serviceName, "11.11.11.11", TEST_PORT, "c1");
            naming2.registerInstance(serviceName, "33.33.33.33", TEST_PORT, "c1");

            while (instances.size() == 0) {
                TimeUnit.SECONDS.sleep(1L);
            }
            Assertions.assertEquals(serviceName, naming1.getSubscribeServices().get(0).getName());
            Assertions.assertEquals(0, naming2.getSubscribeServices().size());

            naming1.unsubscribe(serviceName, listener);

            TimeUnit.SECONDS.sleep(5L);
            Assertions.assertEquals(0, naming1.getSubscribeServices().size());
            Assertions.assertEquals(0, naming2.getSubscribeServices().size());
        } finally {
            naming1.unsubscribe(serviceName, listener);
        }
    }

    @Test
    @DisplayName("Multiple tenant subscribe same service in diff group.")
    public void testMultipleTenant_group_nosubscribe_unSubscribe() throws Exception {
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(Event event) {
                log.info(((NamingEvent) event).getServiceName());
                instances = ((NamingEvent) event).getInstances();
            }
        };

        try {
            naming1.subscribe(serviceName, TEST_GROUP_1, listener);
            naming1.registerInstance(serviceName, "11.11.11.11", TEST_PORT, "c1");
            naming1.registerInstance(serviceName, TEST_GROUP_2, "33.33.33.33", TEST_PORT, "c1");

            TimeUnit.SECONDS.sleep(3L);
            // Get all subscribed services of current client.
            Assertions.assertEquals(serviceName, naming1.getSubscribeServices().get(0).getName());
            Assertions.assertEquals(0, naming2.getSubscribeServices().size());

            naming1.unsubscribe(serviceName, listener);    //unsubscribe，no group
            TimeUnit.SECONDS.sleep(3L);
            Assertions.assertEquals(1, naming1.getSubscribeServices().size());

            naming1.unsubscribe(serviceName, TEST_GROUP_1, listener);   //unsubscribe，have group
            TimeUnit.SECONDS.sleep(3L);
            Assertions.assertEquals(0, naming1.getSubscribeServices().size());

            Assertions.assertEquals(0, naming2.getSubscribeServices().size());
        } finally {
            naming1.unsubscribe(serviceName, listener);
            naming1.unsubscribe(serviceName, TEST_GROUP_1, listener);
        }
    }

    @Test
    @DisplayName("Multiple tenant subscribe and unsubscribe same service in diff group.")
    public void testMultipleTenant_group_unSubscribe() throws Exception {
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(Event event) {
                log.info(((NamingEvent) event).getServiceName());
                instances = ((NamingEvent) event).getInstances();
            }
        };

        try {
            naming1.subscribe(serviceName, Constants.DEFAULT_GROUP, listener);
            naming1.subscribe(serviceName, TEST_GROUP_2, listener);
            naming1.subscribe(serviceName, TEST_GROUP_1, listener);

            naming1.registerInstance(serviceName, "11.11.11.11", TEST_PORT, "c1");
            naming1.registerInstance(serviceName, TEST_GROUP_2, "33.33.33.33", TEST_PORT, "c1");

            while (instances.size() == 0) {
                TimeUnit.SECONDS.sleep(1L);
            }
            TimeUnit.SECONDS.sleep(2L);
            Assertions.assertEquals(serviceName, naming1.getSubscribeServices().get(0).getName());
            Assertions.assertEquals(3, naming1.getSubscribeServices().size());

            naming1.unsubscribe(serviceName, listener);
            naming1.unsubscribe(serviceName, TEST_GROUP_2, listener);
            TimeUnit.SECONDS.sleep(3L);
            Assertions.assertEquals(1, naming1.getSubscribeServices().size());
            Assertions.assertEquals(TEST_GROUP_1, naming1.getSubscribeServices().get(0).getGroupName());

            naming1.unsubscribe(serviceName, TEST_GROUP_1, listener);
        } finally {
            naming1.unsubscribe(serviceName, Constants.DEFAULT_GROUP, listener);
            naming1.unsubscribe(serviceName, TEST_GROUP_2, listener);
            naming1.unsubscribe(serviceName, TEST_GROUP_1, listener);
        }
    }

    @Test
    @DisplayName("Multiple tenant getServerStatus")
    public void testMultipleTenant_serverStatus() throws Exception {
        Assertions.assertEquals(TEST_SERVER_STATUS, naming1.getServerStatus());
        Assertions.assertEquals(TEST_SERVER_STATUS, naming2.getServerStatus());
    }

    @Test
    @DisplayName("Multiple tenant deregisterInstance in default group.")
    public void testMultipleTenant_deregisterInstance() throws Exception {
        naming1.registerInstance(serviceName, "11.11.11.11", TEST_PORT, "c1");
        naming1.registerInstance(serviceName, "22.22.22.22", TEST_PORT, "c1");
        naming2.registerInstance(serviceName, "22.22.22.22", TEST_PORT, "c1");
        TimeUnit.SECONDS.sleep(5L);

        verifyInstanceListForNaming(naming2, getInstancesClustersNaming2(serviceName), serviceName);

        Assertions.assertEquals(2, naming1.getAllInstances(serviceName, false).size());

        naming1.deregisterInstance(serviceName, "22.22.22.22", TEST_PORT, "c1");
        TimeUnit.SECONDS.sleep(10);

        Assertions.assertEquals(1, naming1.getAllInstances(serviceName, false).size());
        Assertions.assertEquals(1, naming2.getAllInstances(serviceName, false).size());
    }

    @Test
    @DisplayName("Multiple tenant deregisterInstance in diff group.")
    public void testMultipleTenant_group_deregisterInstance() throws Exception {
        naming1.registerInstance(serviceName, "11.11.11.11", TEST_PORT, "c1");
        naming1NewConn.registerInstance(serviceName, "22.22.22.22", TEST_PORT, "c2");

        List<Instance> instances = naming1.getAllInstances(serviceName);
        verifyInstanceListForNaming(naming1, 2, serviceName);

        Assertions.assertEquals(2, naming1.getAllInstances(serviceName).size());

        naming1.deregisterInstance(serviceName, TEST_GROUP_2, "22.22.22.22", TEST_PORT, "c1");
        TimeUnit.SECONDS.sleep(12);

        Assertions.assertEquals(2, naming1.getAllInstances(serviceName).size());
    }

    @Test
    @DisplayName("Multiple tenant deregisterInstance in diff group.")
    public void testMultipleTenant_group_cluster_deregisterInstance() throws Exception {
        naming1.deregisterInstance(serviceName, "11.11.11.11", TEST_PORT, "c1");
        naming1.deregisterInstance(serviceName, "22.22.22.22", TEST_PORT, "c2");

        naming1.registerInstance(serviceName, "11.11.11.11", TEST_PORT, "c1");
        naming1.registerInstance(serviceName, "22.22.22.22", TEST_PORT, "c2");
        verifyInstanceListForNaming(naming1, getInstancesClustersNaming1(serviceName), serviceName);

        naming1.deregisterInstance(serviceName, "22.22.22.22", TEST_PORT);
        TimeUnit.SECONDS.sleep(60);

        instances = naming1.getAllInstances(serviceName);
        Assertions.assertEquals(1, instances.size());

        naming1.deregisterInstance(serviceName, "11.11.11.11", TEST_PORT, "c1");
        naming1.deregisterInstance(serviceName, "22.22.22.22", TEST_PORT, "c2");
    }

    @Test
    @DisplayName("Multiple tenant selectOneHealthyInstance in diff cluster and default group.")
    public void testMultipleTenant_selectOneHealthyInstance() throws Exception {
        naming1.registerInstance(serviceName, "11.11.11.11", TEST_PORT, "c1");
        naming1NewConn.registerInstance(serviceName, "22.22.22.22", TEST_PORT, "c2");
        naming2.registerInstance(serviceName, "22.22.22.22", TEST_PORT, "c3");

        TimeUnit.SECONDS.sleep(3);

        Instance instance = naming1.selectOneHealthyInstance(serviceName, Arrays.asList("c1"));
        verifyInstanceListForNaming(naming1, 2, serviceName);
        Assertions.assertEquals(2, naming1.getAllInstances(serviceName).size());

        Assertions.assertEquals("11.11.11.11", instance.getIp());
        naming1.deregisterInstance(serviceName, "11.11.11.11", TEST_PORT, "c1");
        TimeUnit.SECONDS.sleep(10);
        instance = naming1.selectOneHealthyInstance(serviceName);
        Assertions.assertEquals("22.22.22.22", instance.getIp());
    }

    @Test
    @DisplayName("Multiple tenant selectOneHealthyInstance in diff cluster and diff group.")
    public void testMultipleTenant_group_selectOneHealthyInstance() throws Exception {
        naming1.registerInstance(serviceName, TEST_GROUP, "11.11.11.11", TEST_PORT, "c1");
        naming1.registerInstance(serviceName, TEST_GROUP_1, "22.22.22.22", TEST_PORT, "c2");
        naming1.registerInstance(serviceName, TEST_GROUP_2, "33.33.33.33", TEST_PORT, "c3");

        TimeUnit.SECONDS.sleep(3);

        verifyInstanceListForNaming(naming1, 0, serviceName);

        Assertions.assertEquals(0, naming1.getAllInstances(serviceName).size());   //defalut group

        Instance instance = naming1.selectOneHealthyInstance(serviceName, TEST_GROUP, Arrays.asList("c1"));
        Assertions.assertEquals("11.11.11.11", instance.getIp());

        instance = naming1.selectOneHealthyInstance(serviceName, TEST_GROUP_1);
        Assertions.assertEquals("22.22.22.22", instance.getIp());

        naming1.deregisterInstance(serviceName, TEST_GROUP, "11.11.11.11", TEST_PORT, "c1");
        naming1.deregisterInstance(serviceName, TEST_GROUP_1, "22.22.22.22", TEST_PORT, "c2");
        naming1.deregisterInstance(serviceName, TEST_GROUP_2, "33.33.33.33", TEST_PORT, "c3");
    }

    @Test
    @DisplayName("Multiple tenant selectOneHealthyInstance in no group.")
    public void testMultipleTenant_noGroup_selectOneHealthyInstance() throws Exception {
        naming1.registerInstance(serviceName, TEST_GROUP, "11.11.11.11", TEST_PORT, "c1");
        naming1.registerInstance(serviceName, TEST_GROUP_1, "22.22.22.22", TEST_PORT, "c2");
        TimeUnit.SECONDS.sleep(10);
        List<Instance> instances = naming1.getAllInstances(serviceName, TEST_GROUP);

        Assertions.assertEquals(1, instances.size());

        verifyInstanceListForNaming(naming1, 0, serviceName);
        Throwable exception = Assertions.assertThrows(IllegalStateException.class, () -> {
            Instance instance = naming1.selectOneHealthyInstance(serviceName, Arrays.asList("c1"));
            Assertions.assertEquals(null, instance);
        });
        Assertions.assertTrue(exception.getMessage().contains("no host to srv for serviceInfo"));

        naming1.deregisterInstance(serviceName, TEST_GROUP, "11.11.11.11", TEST_PORT, "c1");
        naming1.deregisterInstance(serviceName, TEST_GROUP_1, "22.22.22.22", TEST_PORT, "c2");
    }
}

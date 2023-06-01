package com.alibaba.nacos.naming;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.net.HttpClient;
import com.alibaba.nacos.util.ConvertUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SubscribeClusterTest extends NamingBase{
    private static final Logger log = LoggerFactory.getLogger(SubscribeClusterTest.class);
    private List<String> cleanServiceNames = new ArrayList();
    private String serviceName;
    private volatile List<Instance> instances;

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
    @DisplayName("Subscribe service and register instance.")
    @Timeout(value = 50000, unit = TimeUnit.MILLISECONDS)
    public void testSubscribeAdd() throws Exception {

        naming.subscribe(serviceName, Arrays.asList("c1"), new EventListener() {
            @Override
            public void onEvent(Event event) {
                log.info(((NamingEvent) event).getServiceName());
                log.info(JSON.toJSONString(((NamingEvent) event).getInstances()));
                instances = ((NamingEvent) event).getInstances();
            }
        });

        naming.registerInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT, "c1");


        int i = 0;
        while (instances.isEmpty()) {
            Thread.sleep(1000L);
            log.info("wait to subscribe instance...");
            if (i++ > 10) {
                return;
            }
        }

        Instance expected = getExpectedInstance("127.0.0.1", NamingBase.TEST_PORT, "c1", 1.0, true);

        Assertions.assertTrue(
            NamingBase.verifyInstanceList(instances, Collections.singletonList(expected)));
    }

    @Test
    @DisplayName("Subscribe service and deregister instance.")
    @Timeout(value = 50000, unit = TimeUnit.MILLISECONDS)
    public void testSubscribeDelete() throws Exception {

        naming.registerInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT, "c1");
        naming.registerInstance(serviceName, "127.0.0.2", NamingBase.TEST_PORT, "c1");
        TimeUnit.SECONDS.sleep(5);
        naming.subscribe(serviceName, Arrays.asList("c1"), new EventListener() {
            @Override
            public void onEvent(Event event) {
                log.info("subscribe event");
                log.info(((NamingEvent) event).getServiceName());
                log.info(JSON.toJSONString(((NamingEvent) event).getInstances().size()));
                instances = ((NamingEvent) event).getInstances();
            }
        });
        naming.deregisterInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT, "c1");
        TimeUnit.SECONDS.sleep(5);
        int i = 0;
        while (instances.size() != 1) {
            Thread.sleep(1000L);
            System.out.println("wait to subscribe instance...");
            if (i++ > 10) {
                Assertions.assertFalse(true, "wait 10s timeout");
            }
        }

        Instance expected = getExpectedInstance("127.0.0.2", NamingBase.TEST_PORT, "c1", 1.0, true);

        Assertions.assertTrue(
            NamingBase.verifyInstanceList(instances, Collections.singletonList(expected)));
    }

    @Test
    @DisplayName("Subscribe service and change instance wight.")
    @Timeout(value = 50000, unit = TimeUnit.MILLISECONDS)
    public void testSubscribeChangeWeight() throws Exception {

        Instance instance = NamingBase.getInstance(serviceName);
        naming.registerInstance(serviceName, instance);

        TimeUnit.SECONDS.sleep(10);
        double updateWeight = 66.0;
        naming.subscribe(serviceName, Arrays.asList("c1"), new EventListener() {
            int index = 0;
            @Override
            public void onEvent(Event event) {
                log.info(((NamingEvent) event).getServiceName());
                List<Instance> tempInstances = ((NamingEvent) event).getInstances();
                log.info("tempInstances=" + JSON.toJSONString(tempInstances));
                if (index == 0) {
                    index++;
                    // update weight will be subscribed
                    if (tempInstances.size() >0 && updateWeight == tempInstances.get(0).getWeight()) {
                        instances = ((NamingEvent) event).getInstances();
                    }
                    return;
                }
                instances = ((NamingEvent) event).getInstances();
            }
        });

        instance.setWeight(updateWeight);
        naming.registerInstance(serviceName, instance);

        int i = 0;
        while (instances.isEmpty()) {
            Thread.sleep(1000L);
            log.info("wait to subscribe instance...");
            if (i++ > 10) {
                Assertions.assertFalse(true, "time out");
            }
        }
        log.info("instance="+JSON.toJSONString(instance));
        List<Instance> allInstances = naming.getAllInstances(serviceName);
        log.info("allInstances="+JSON.toJSONString(allInstances));
        Assertions.assertTrue(
            NamingBase.verifyInstanceList(instances, naming.getAllInstances(serviceName)));
    }

    @Test
    @DisplayName("Subscribe service and registerInstance unhealthy instance.")
    @Timeout(value = 50000, unit = TimeUnit.MILLISECONDS)
    public void testSubscribeUnhealthy() throws Exception {

        naming.subscribe(serviceName, Arrays.asList("c1"), new EventListener() {
            @Override
            public void onEvent(Event event) {
                log.info(((NamingEvent) event).getServiceName());
                log.info(JSON.toJSONString(((NamingEvent) event).getInstances()));
                instances = ((NamingEvent) event).getInstances();
            }
        });

        naming.registerInstance(serviceName, "1.1.1.1", NamingBase.TEST_PORT, "c1");

        int i = 0;
        while (instances.isEmpty()) {
            Thread.sleep(1000L);
            log.info("wait to subscribe instance...");
            if (i++ > 10) {
                return;
            }
        }
        Instance expected = getExpectedInstance("1.1.1.1", NamingBase.TEST_PORT, "c1", 1.0, true);
        Assertions.assertTrue(
            NamingBase.verifyInstanceList(instances, Collections.singletonList(expected)));
    }

    @Test
    @DisplayName("Subscribe service and registerInstance in other cluster.")
    public void testSubscribeOtherCluster() throws Exception {

        naming.subscribe(serviceName, Arrays.asList("c2"), new EventListener() {
            int index = 0;

            @Override
            public void onEvent(Event event) {
                if (index == 0) {
                    index++;
                    return;
                }
                log.info(((NamingEvent) event).getServiceName());
                log.info(JSON.toJSONString(((NamingEvent) event).getInstances()));
                instances = ((NamingEvent) event).getInstances();
            }
        });

        naming.registerInstance(serviceName, "1.1.1.1", NamingBase.TEST_PORT, "c1");

        int i = 0;
        while (instances.isEmpty()) {
            Thread.sleep(1000L);
            if (i++ > 20) {
                return;
            }
        }

        Assertions.assertTrue(false);
    }

}

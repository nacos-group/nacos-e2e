package com.alibaba.nacos.naming;

import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UnsubscribeTest extends NamingBase{
    private static final Logger log = LoggerFactory.getLogger(UnsubscribeTest.class);
    private String serviceName;
    private volatile List<Instance> instances;

    @BeforeEach
    public void setUp(TestInfo testInfo) throws Exception{
        instances = Collections.emptyList();
        serviceName = randomDomainName();
        log.info("Running test=" + testInfo.getTestMethod() + ", serviceName=" + serviceName);
    }

    @Test
    @DisplayName("Register instance After unsubscribe will not get event.")
    @Timeout(value = 50000, unit = TimeUnit.MILLISECONDS)
    public void unsubscribe() throws Exception {
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(Event event) {
                System.out.println(((NamingEvent) event).getServiceName());
                System.out.println(((NamingEvent) event).getInstances());
                instances = ((NamingEvent) event).getInstances();
            }
        };

        naming.subscribe(serviceName, listener);

        naming.registerInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT, "c1");

        int i = 0;
        while (instances.isEmpty()) {
            Thread.sleep(1000L);
            System.out.println("wait to subscribe instance...");
            if (i++ > 10) {
                i = 0;
                return;
            }
        }

        Assertions.assertTrue(
            NamingBase.verifyInstanceList(instances, naming.getAllInstances(serviceName)));

        naming.unsubscribe(serviceName, listener);

        instances = Collections.emptyList();
        naming.registerInstance(serviceName, "127.0.0.2", NamingBase.TEST_PORT, "c1");

        while (instances.isEmpty()) {
            Thread.sleep(1000L);
            if (i++ > 10) {
                return;
            }
        }

        Assertions.assertTrue(false);
    }

    @Test
    @DisplayName("Register instance in appointed cluster After unsubscribe will not get event.")
    @Timeout(value = 50000, unit = TimeUnit.MILLISECONDS)
    public void unsubscribeCluster() throws Exception {
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(Event event) {
                System.out.println(((NamingEvent) event).getServiceName());
                System.out.println(((NamingEvent) event).getInstances());
                instances = ((NamingEvent) event).getInstances();
            }
        };

        naming.subscribe(serviceName, Arrays.asList("c1"), listener);

        naming.registerInstance(serviceName, "127.0.0.1", NamingBase.TEST_PORT, "c1");


        int i = 0;
        while (instances.isEmpty()) {
            Thread.sleep(1000L);
            System.out.println("wait to subscribe instance...");
            if (i++ > 10) {
                i = 0;
                return;
            }
        }
        //sleep time in distro sync default is 2s, the longest sleep time in reconcile is 5s
        //when subscribe node and register node is the same node, the push takes effect may be in
        // the sync time, will make case unstable
        Thread.sleep(2000L);

        Assertions.assertTrue(
            NamingBase.verifyInstanceList(instances, naming.getAllInstances(serviceName)));

        naming.unsubscribe(serviceName, Arrays.asList("c1"), listener);

        instances = Collections.emptyList();
        naming.registerInstance(serviceName, "127.0.0.2", NamingBase.TEST_PORT, "c1");

        while (instances.isEmpty()) {
            Thread.sleep(1000L);
            if (i++ > 10) {
                return;
            }
        }
        Assertions.assertTrue(false);
    }
    
}

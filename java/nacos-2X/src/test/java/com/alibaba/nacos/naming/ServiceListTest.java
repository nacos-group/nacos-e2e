package com.alibaba.nacos.naming;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.enums.TESTSET;
import com.alibaba.nacos.util.ConvertUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

// this test class should be serial execute, if parallel execute will make data conflict
@Execution(ExecutionMode.SAME_THREAD)
@Tag(TESTSET.NAMING)
public class ServiceListTest extends NamingBase{
    private static final Logger log = LoggerFactory.getLogger(ServiceListTest.class);
    private List<String> cleanServiceNames = new ArrayList();
    private String serviceName;
    private volatile List<Instance> instances;
    public static NamingService namingNewConn;

    @BeforeAll
    public static void setUpAll() throws Exception {
        NamingBase.setUpAll();
        namingNewConn = NacosFactory.createNamingService(properties);
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
    @DisplayName("Get services of server.")
    public void testServiceList() throws NacosException {
        ListView<String>  listView = naming.getServicesOfServer(1, 200);
        log.info("listView=" + JSON.toJSONString(listView));
    }

    @Test
    @DisplayName("Get all current subscribe services.")
    public void testGetSubscribeServices() throws NacosException, InterruptedException {

        ListView<String> listView = naming.getServicesOfServer(1, 200);
        if (listView != null && listView.getCount() > 0) {
            naming.getAllInstances(listView.getData().get(0));
        }
        EventListener listener = new EventListener() {
            @Override
            public void onEvent(Event event) {
                log.info(((NamingEvent)event).getServiceName());
                log.info(JSON.toJSONString(((NamingEvent)event).getInstances()));
                instances = ((NamingEvent)event).getInstances();
            }
        };

        List<ServiceInfo> serviceInfoList = naming.getSubscribeServices();
        int count = serviceInfoList.size();


        String serviceName = randomDomainName();
        naming.registerInstance(serviceName, "127.0.0.1", TEST_PORT, "c1");
        naming.registerInstance(serviceName, "127.0.0.2", TEST_PORT, "c2");
        naming.subscribe(serviceName, listener);

        Assertions.assertTrue(verifyInstanceList(instances, naming.getAllInstances(serviceName)));
        serviceInfoList = naming.getSubscribeServices();
        TimeUnit.SECONDS.sleep(10);

        Assertions.assertEquals(count+1, serviceInfoList.size());
    }

    @Test
    @DisplayName("Get all current subscribe services and deregister instance.")
    public void testGetSubscribeServices_deregisterInstance() throws NacosException, InterruptedException {

        EventListener listener = new EventListener() {
            @Override
            public void onEvent(Event event) {
                log.info(((NamingEvent)event).getServiceName());
                log.info(JSON.toJSONString(((NamingEvent)event).getInstances()));
                instances = ((NamingEvent)event).getInstances();
            }
        };

        List<ServiceInfo> serviceInfoList = namingNewConn.getSubscribeServices();
        int count = serviceInfoList.size();

        String serviceName = randomDomainName();
        namingNewConn.registerInstance(serviceName, "127.0.0.1", TEST_PORT, "c1");
        namingNewConn.registerInstance(serviceName, "127.0.0.2", TEST_PORT, "c2");
        namingNewConn.subscribe(serviceName, listener);

        TimeUnit.SECONDS.sleep(10);
        Assertions.assertTrue(verifyInstanceList(instances, naming.getAllInstances(serviceName)));
        serviceInfoList = namingNewConn.getSubscribeServices();


        namingNewConn.deregisterInstance(serviceName, "127.0.0.3", TEST_PORT, "c1");
        namingNewConn.deregisterInstance(serviceName, "127.0.0.4", TEST_PORT, "c2");
        TimeUnit.SECONDS.sleep(10);

        Assertions.assertEquals(count+1, serviceInfoList.size());
    }
}

package com.alibaba.nacos.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.enums.TESTSET;
import com.alibaba.nacos.util.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Tag(TESTSET.CONFIG)
public class ConfigSyncTest extends ConfigBase {
    private static final Logger log = LoggerFactory.getLogger(ConfigSyncTest.class);
    private String dataId;
    private String group;
    private String content;
    private Map<String ,String> cleanConfigMap = new HashMap<>();

    public static ConfigService configNewConn1;
    public static ConfigService configNewConn2;

    @BeforeAll
    public static void setUpAll() throws Exception {
        ConfigBase.setUpAll();
        configNewConn1 = NacosFactory.createConfigService(properties);
        configNewConn2 = NacosFactory.createConfigService(properties);
    }
    
    @BeforeEach
    public void setUp() throws Exception{
        dataId = "config.test." + RandomUtils.getStringWithCharacter(10);
        group = DEFAULT_GROUP;
        content = "test" + SPECIAL_CHARACTERS;
    }

    @AfterEach
    public void tearDown() throws Exception {
        Iterator<Entry<String, String>> iterator = cleanConfigMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            Boolean result = config.removeConfig(entry.getKey(), entry.getValue());
            if (result) {
                iterator.remove();
            }
        }
    }

    @Test
    @DisplayName("Publish config and get config use other configService")
    public void testClusterSync_writeAndRead() throws Exception {
        //config write
        boolean result = config.publishConfig(dataId, group, content);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);

        //configNewConn1 read
        String value = configNewConn1.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(content, value);

        //configNewConn2 read
        value = configNewConn2.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(content, value);

        result = configNewConn1.removeConfig(dataId, group);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);

        value = configNewConn1.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(null, value);

        value = configNewConn2.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(null, value);

        value = config.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(null, value);
    }

    @Test
    @DisplayName("Publish config and remove config use other configService")
    public void testClusterSync_writeAndRemove() throws Exception {
        boolean result = config.publishConfig(dataId, group, content);
        Thread.sleep(TIME_OUT);
        Assertions.assertTrue(result);

        String value = configNewConn1.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(content, value);

        //configNewConn1 remove
        result = configNewConn1.removeConfig(dataId, group);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);

        //configNewConn1 read
        value = configNewConn1.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(null, value);
    }

    @Test
    @DisplayName("Config publish and configNewConn1 remove, then configNewConn1 get null ")
    public void testClusterSync_writeAndRemove2() throws Exception {
        boolean result = config.publishConfig(dataId, group, content);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);

        String value = config.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(content, value);

        //configNewConn1 remove
        result = configNewConn1.removeConfig(dataId, group);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);

        //config get
        value = config.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(null, value);
    }

    @Test
    @Timeout(value = 15 * TIME_OUT, unit = TimeUnit.MILLISECONDS)
    @DisplayName("Publish config and addListener use other configService")
    public void testClusterSync_writeAndListener() throws Exception {
        final AtomicInteger count = new AtomicInteger(0);
        Listener ml = newListener(count);
       
        //config addListener
        config.addListener(dataId, group, ml);
        Thread.sleep(TIME_OUT);

        //configNewConn1 publish
        boolean result = configNewConn1.publishConfig(dataId, group, content);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);

        while (count.get() == 0) {
            config.addListener(dataId, group, ml);
            Thread.sleep(TIME_OUT);
            log.info("wait " + TIME_OUT +"ms..." + "count:" + count.get());
        }

        Assertions.assertEquals(1, count.get());

        config.removeListener(dataId, group, ml);
        Thread.sleep(TIME_OUT);
        result = configNewConn1.removeConfig(dataId, group);
        Assertions.assertTrue(result);
    }

    @Test
    @Timeout(value = 8 * TIME_OUT, unit = TimeUnit.MILLISECONDS)
    @DisplayName("Config addListener and configNewConn1 removeListener")
    public void testClusterSync_addListenerAndRemoveListener() throws Exception {
        boolean result = config.publishConfig(dataId, group, content);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);
        final AtomicInteger count = new AtomicInteger(0);
        Listener ml = newListener(count);
        
        //config addListener
        config.addListener(dataId, group, ml);
        Thread.sleep(TIME_OUT);
        while (count.get() == 0) {
            config.addListener(dataId, group, ml);
            Thread.sleep(TIME_OUT);
            log.info("wait " + TIME_OUT +"ms..." + "count:" + count.get());
        }
        Assertions.assertNotSame(0, count.get());

        //configNewConn1 removeListener
        configNewConn1.removeListener(dataId, group, ml);
        Thread.sleep(TIME_OUT);

        result = config.removeConfig(dataId, group);
        Thread.sleep(TIME_OUT);
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("Config publish and configNewConn1 addListener, then configNewConn2 removeListener")
    public void testClusterSync_addListenerAndRemoveListener2() throws Exception {
        boolean result = config.publishConfig(dataId, group, content);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);

        final AtomicInteger count = new AtomicInteger(0);
        Listener ml = newListener(count);
        configNewConn1.addListener(dataId, group, ml);
        Thread.sleep(TIME_OUT);

        configNewConn2.removeListener(dataId, group, ml);
        Thread.sleep(TIME_OUT);

        result = config.removeConfig(dataId, group);
        Thread.sleep(TIME_OUT);
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("Config publish one content and configNewConn1 publish other content.")
    public void testClusterSync_writeAndChange() throws Exception {
        boolean result = config.publishConfig(dataId, group, content);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);

        content = "test-sync-2";
        result = configNewConn1.publishConfig(dataId, group, content);
        Thread.sleep(TIME_OUT);
        Assertions.assertTrue(result);

        String value = config.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(content, value);

        result = config.removeConfig(dataId, group);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);

        value = config.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(null, value);
    }

    @Test
    @DisplayName("Config publish content and configNewConn1 publish content1, then configNewConn2 publish content2")
    public void testClusterSync_writeAndChange2() throws Exception {
        boolean result = config.publishConfig(dataId, group, content);
        Assertions.assertTrue(result);

        content = "test-sync-2";
        result = configNewConn1.publishConfig(dataId, group, content);
        Assertions.assertTrue(result);

        content = "test-sync-3";
        result = configNewConn2.publishConfig(dataId, group, content);
        Assertions.assertTrue(result);

        Thread.sleep(TIME_OUT);
        String value = config.getConfig(dataId, group, TIME_OUT);
        String value1 = configNewConn1.getConfig(dataId, group, TIME_OUT);
        String value2 = configNewConn2.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(value, value1);
        Assertions.assertEquals(value1, value2);

        result = config.removeConfig(dataId, group);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);

        value = config.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(null, value);
    }

    @Test
    @DisplayName("Three configServer removeConfig")
    public void testClusterSync_writeAndChange3() throws Exception {
        boolean result = config.publishConfig(dataId, group, content);
        Assertions.assertTrue(result);

        content = "test-sync-2";
        result = configNewConn1.publishConfig(dataId, group, content);
        Assertions.assertTrue(result);

        content = "test-sync-3" + SPECIAL_CHARACTERS;
        result = configNewConn2.publishConfig(dataId, group, content);
        Assertions.assertTrue(result);

        Thread.sleep(TIME_OUT);
        String value = config.getConfig(dataId, group, TIME_OUT);
        String value1 = configNewConn1.getConfig(dataId, group, TIME_OUT);
        String value2 = configNewConn2.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(value, value1);
        Assertions.assertEquals(value1, value2);

        result = config.removeConfig(dataId, group);
        Assertions.assertTrue(result);
        result = configNewConn1.removeConfig(dataId, group);
        Assertions.assertTrue(result);
        result = configNewConn2.removeConfig(dataId, group);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);

        value = config.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(null, value);
    }

}

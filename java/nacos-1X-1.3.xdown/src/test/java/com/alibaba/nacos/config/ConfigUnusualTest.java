package com.alibaba.nacos.config;

import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.enums.TESTSET;
import com.alibaba.nacos.util.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

@Tag(TESTSET.CONFIG)
public class ConfigUnusualTest extends ConfigBase {
    private static final Logger log = LoggerFactory.getLogger(ConfigUnusualTest.class);
    private String dataId;
    private String group;
    private String content;
    private Map<String ,String> cleanConfigMap = new HashMap<>();

    @BeforeEach
    public void setUp() throws Exception{
        dataId = "config.test." + RandomUtils.getStringWithCharacter(10);
        group = DEFAULT_GROUP;
    }

    @AfterEach
    public void tearDown() throws Exception {
        Iterator<Entry<String, String>> iterator = cleanConfigMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            Boolean result = config.removeConfig(entry.getKey(), entry.getValue());
            if (result) {
                iterator.remove();
            }
        }
    }

    @Test
    @DisplayName("Get config unExist, expect get null character.")
    public void testGetConfig_configUnExist() throws Exception {
        String content = config.getConfig(dataId, group, TIME_OUT);
        Assertions.assertNull(content, "getConfig value is not expect one");
    }

    @Test
    @DisplayName("Publish config when dataId is null, expect throw exception.")
    public void testPublishConfig_dataIdIsNull() throws Exception {
        try {
            boolean result = config.publishConfig(null, group, content);
            Assertions.assertTrue(result, "publishConfig check fail");
        } catch (Exception e) {
            Assertions.assertTrue(true);
            if (e.getMessage() != null && e instanceof NacosException) {
                // if is NacosException，check it
                Assertions.assertEquals(-400, ((NacosException)e).getErrCode());
                Assertions.assertEquals("dataId invalid", ((NacosException)e).getErrMsg());
            } else {
                // else print it
                log.info("publishConfig null dataId got exception ", e);
            }
            return;
        }
        Assertions.assertTrue(false);
    }

    @Test
    @DisplayName("Get config when dataId is null, expect throw exception.")
    public void testGetConfig_dataIdIsNull() throws Exception {
        try {
            String content = config.getConfig(null, group, TIME_OUT);
            Assertions.assertEquals(null, content, "getConfig check fail");
        } catch (Exception e) {
            Assertions.assertTrue(true);
            if (e.getMessage() != null && e instanceof NacosException) {
                // if is NacosException，check it
                Assertions.assertEquals(-400, ((NacosException)e).getErrCode());
                Assertions.assertEquals("dataId invalid", ((NacosException)e).getErrMsg());
            } else {
                // else print it
                log.info("getConfig null dataId got exception ", e);
            }
            return;
        }
        Assertions.assertTrue(false);
    }

    @Test
    @DisplayName("Publish and get config when group is bull, expect null group don't affect used.")
    public void testPublishAndGetConfig_groupIsNull() throws Exception {
        content = RandomUtils.getStringWithCharacter(20);
        boolean result = config.publishConfig(dataId, null, content);
        Thread.sleep(TIME_OUT);
        Assertions.assertTrue(result);

        String value = config.getConfig(dataId, null, TIME_OUT);
        Assertions.assertEquals(content, value);

        result = config.removeConfig(dataId, null);
        Thread.sleep(TIME_OUT);
        Assertions.assertTrue(result);

        value = config.getConfig(dataId, null, TIME_OUT);
        Assertions.assertEquals(null, value);
    }

    @Test
    @DisplayName("Publish empty content and sleep 3000ms to get config, "
        + "expect get correct config content.")
    public void testPublishAndGetConfig_emptyCharacterContent() throws Exception{
        content = "";
        try {
            boolean result = config.publishConfig(dataId, group, content);
            log.info("publishConfig dataId:{}, group:{}, result:{}", dataId, group, result);
            Assertions.assertTrue(result, "publishConfig check fail");
            cleanConfigMap.put(dataId, group);
            Thread.sleep(TIME_OUT);

            String value = config.getConfig(dataId, group, TIME_OUT);
            log.info("getConfig dataId:{}, group:{}, value:{}", dataId, group, value);
            Assertions.assertEquals(content, value, "getConfig value is not expect one");

        } catch (Exception e) {
            Assertions.assertTrue(true);
            if (e.getMessage() != null && e instanceof NacosException) {
                // if is NacosException，check it
                Assertions.assertEquals(-400, ((NacosException)e).getErrCode());
                Assertions.assertEquals("content invalid", ((NacosException)e).getErrMsg());
            } else {
                // else print it
                log.info("publishConfig empty content got exception ", e);
            }
            return;
        }
    }

    @Test
    @DisplayName("Get config when config is unExist, expect can publish this config and get "
        + "expect config after publish.")
    public void testGetConfig_configIsUnExist() throws Exception {
        content = RandomUtils.getStringWithCharacter(20);
        String value = config.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(null, value);

        boolean result = config.publishConfig(dataId, group, content);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);

        result = config.removeConfig(dataId, group);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);

        value = config.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(null, value);
    }

    @Test
    @DisplayName("Publish special character and sleep 3000ms to get config, "
        + "expect get correct config content.")
    public void testPublishAndGetConfig_specialCharacter() throws Exception{
        content = SPECIAL_CHARACTERS;
        boolean result = config.publishConfig(dataId, group, content);
        log.info("publishConfig dataId:{}, group:{}, result:{}", dataId, group, result);
        Assertions.assertTrue(result, "publishConfig check fail");
        cleanConfigMap.put(dataId, group);
        Thread.sleep(TIME_OUT);

        String value = config.getConfig(dataId, group, TIME_OUT);
        log.info("getConfig dataId:{}, group:{}, value:{}", dataId, group, value);
        Assertions.assertEquals(content, value, "getConfig value is not expect one");
    }

    @Test
    @DisplayName("Publish special character and sleep 3000ms to get config, expect get correct config content.")
    public void testPublishAndGetConfig_specialCharacter1() throws Exception {
        content = SPECIAL_CHARACTERS1;
        boolean result = config.publishConfig(dataId, group, content);
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
    @DisplayName("Publish 100KB character and sleep 3000ms to get config, "
        + "expect get correct config content.")
    public void testPublishAndGetConfig_bigSizeCharacter() throws Exception{
        content = RandomUtils.getStringWithCharacter(100 * 1024);
        boolean result = config.publishConfig(dataId, group, content);
        log.info("publishConfig dataId:{}, group:{}, result:{}", dataId, group, result);
        Assertions.assertTrue(result, "publishConfig check fail");
        cleanConfigMap.put(dataId, group);
        Thread.sleep(TIME_OUT);

        String value = config.getConfig(dataId, group, TIME_OUT);
        log.info("getConfig dataId:{}, group:{}, value:{}", dataId, group, value);
        Assertions.assertEquals(content, value, "getConfig value is not expect one");
    }

    @Test
    @DisplayName("Get and remove unExist config, expect get null and remove return true")
    public void testGetAndRemoveUnExistConfig() throws Exception {
        String value = config.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(null, value);
        boolean result = config.removeConfig(dataId, group);
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("Remove config when dataId is null, expect catch exception.")
    public void testRemoveConfig_dataIdIsNull() throws Exception {
        try {
            boolean result = config.removeConfig(null, group);
            Assertions.assertTrue(result);
        } catch (Exception e) {
            Assertions.assertTrue(true);
            if (e.getMessage() != null && e instanceof NacosException) {
                // if is NacosException，check it
                Assertions.assertEquals(-400, ((NacosException)e).getErrCode());
                Assertions.assertEquals("dataId invalid", ((NacosException)e).getErrMsg());
            } else {
                // else print it
                log.info("getConfig null dataId got exception ", e);
            }
            return;
        }
        Assertions.assertTrue(false);
    }

    @Test
    @DisplayName("Remove config when group is null, expect get null and remove return true")
    public void testRemoveConfig_groupIsNull() throws Exception {
        String value = config.getConfig(dataId, null, TIME_OUT);
        Assertions.assertEquals(null, value);
        boolean result = config.removeConfig(dataId, null);
        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("Add null listener，expect catch exception.")
    public void testAddListener_listenerIsNull() {
        try {
            config.addListener(dataId, group, null);
            Assertions.assertFalse(true);
        } catch (Exception e) {
            Assertions.assertFalse(false);
        }
    }

    @Test
    @DisplayName("Add listener unExist config，expect listener nothing")
    public void testAddListener_unExistConfig() throws Exception {
        final AtomicInteger count = new AtomicInteger(0);
        //make sure this config
        config.removeConfig(dataId, group);
        Thread.sleep(TIME_OUT);
        String value = config.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(null, value);

        Listener listener = newListener(count);
        config.addListener(dataId, group, listener);

        int i = 0;
        while (count.get() == 0) {
            Thread.sleep(TIME_OUT);
            log.info("wait " + TIME_OUT +"ms..." + "count:" + count.get());
            i++;
            if (i < 3 && count.get() == 0) {
                break;
            }
        }
        Assertions.assertEquals(0, count.get());
        config.removeListener(dataId, group, listener);
    }

    @Test
    @DisplayName("Remove listener dataId is null, expect catch expection")
    public void testRemoveListener_dataIdIsNull() {
        try {
            config.removeListener(null, group, newListener());
        } catch (Exception e) {
            Assertions.assertTrue(true);
        }
    }

    @Test
    @DisplayName("Remove listener when listener is null, expect not affect to use")
    public void testRemoveListener_listenerIsNull() {
        config.removeListener(dataId, group, (Listener) null);
        Assertions.assertTrue(true);
    }
}

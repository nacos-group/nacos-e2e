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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Tag(TESTSET.CONFIG)
public class ConfigNormalTest extends ConfigBase {
    private static final Logger log = LoggerFactory.getLogger(ConfigNormalTest.class);
    private String dataId;
    private String group;
    private String content;
    private Map<String ,String> cleanConfigMap = new HashMap<>();

    public static ConfigService configNewConn1;

    @BeforeAll
    public static void setUpAll() throws Exception {
        configNewConn1 = NacosFactory.createConfigService(properties);
    }

    @BeforeEach
    public void setUp() throws Exception{
        dataId = "config.test." + RandomUtils.getStringWithCharacter(10);
        group = DEFAULT_GROUP;
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
    @DisplayName("Publish normal character and sleep 3000ms to get config, "
        + "expect get correct config content.")
    public void testPublishAndGetConfig_normalCharacter() throws Exception{
        content = RandomUtils.getStringWithCharacter(20);
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
    @DisplayName("Publish and  get config frequently, expect get correct config content.")
    public void testPublishAndGetConfig_frequently() throws Exception{
        content = RandomUtils.getStringWithCharacter(20);
        int i = 0;
        while (i < MORE_NUM) {
            content = content + i;
            boolean result = config.publishConfig(dataId, group, content);
            log.info("publishConfig dataId:{}, group:{}, result:{}", dataId, group, result);
            Assertions.assertTrue(result, "publishConfig check fail");

            Thread.sleep(TIME_OUT/3);

            String value = config.getConfig(dataId, group, TIME_OUT);
            log.info("getConfig dataId:{}, group:{}, value:{}", dataId, group, value);
            Assertions.assertEquals(content, value, "getConfig value is not expect one");
            i++;
        }
        cleanConfigMap.put(dataId, group);
    }

    @Test
    @DisplayName("Publish and countDownLatch 30S to one listener, expect listener correct content.")
    public void testPublishAndAddOneListener() throws Exception{
        final CountDownLatch latch = new CountDownLatch(1);
        content = RandomUtils.getStringWithCharacter(20);
        boolean result = config.publishConfig(dataId, group, content);
        log.info("publishConfig dataId:{}, group:{}, result:{}", dataId, group, result);
        Assertions.assertTrue(result, "publishConfig check fail");
        cleanConfigMap.put(dataId, group);
        config.addListener(dataId, group, getListener(dataId, group, content,
            System.currentTimeMillis(), latch));

        latch.await(30, TimeUnit.SECONDS);
        Assertions.assertTrue(latch.getCount() == 0, "listen config is not expect one in timeout");
    }

    @Test
    @DisplayName("Publish and countDownLatch 30S to more listener, expect listener correct content.")
    public void testPublishAndAddMoreListener() throws Exception{
        final CountDownLatch latch = new CountDownLatch(MORE_NUM);
        content = RandomUtils.getStringWithCharacter(20);

         /*
            Because client have local cache, if local cache have content the same as server ,
            will be not trigger listener. So before the publishConfig, should addListener first.
         */
        for (int i = 0; i < MORE_NUM; i++) {
            config.addListener(dataId, group, getListener(dataId, group, content,
                System.currentTimeMillis(), latch));
        }

        boolean result = config.publishConfig(dataId, group, content);
        log.info("publishConfig dataId:{}, group:{}, result:{}", dataId, group, result);
        Assertions.assertTrue(result, "publishConfig check fail");
        cleanConfigMap.put(dataId, group);
        latch.await(30, TimeUnit.SECONDS);
        Assertions.assertTrue(latch.getCount() == 0, "listen config is not expect one in timeout");
    }

    @Test
    @DisplayName("Publish different dataId and countDownLatch 30S to different listener, expect "
        + "listener correct content.")
    public void testPublishAndAddMoreListener_InDifferentDataId() throws Exception{
        final CountDownLatch latch = new CountDownLatch(MORE_NUM);
        content = RandomUtils.getStringWithCharacter(20);

        for (int i = 0; i < MORE_NUM; i++) {
            boolean result = config.publishConfig(dataId+i, group+i, content);
            log.info("publishConfig dataId:{}, group:{}, result:{}", dataId+i, group+i, result);
            Assertions.assertTrue(result, "publishConfig check fail");
            cleanConfigMap.put(dataId+i, group+i);
        }

        for (int i = 0; i < MORE_NUM; i++) {
            config.addListener(dataId+i, group+i, getListener(dataId+i, group+i, content,
                System.currentTimeMillis(), latch));
        }

        latch.await(60, TimeUnit.SECONDS);
        Assertions.assertTrue(latch.getCount() == 0, "listen config is not expect one in timeout");
    }

    @Test
    @DisplayName("Publish different dataId and countDownLatch 30S to one listener, expect "
        + "listener correct content.")
    public void testPublishAndAddOneListener_InDifferentDataId() throws Exception{
        final CountDownLatch latch = new CountDownLatch(MORE_NUM);
        content = RandomUtils.getStringWithCharacter(20);

        Listener listener = getListener(content, latch);

        for (int i = 0; i < MORE_NUM; i++) {
            boolean result = config.publishConfig(dataId+i, group+i, content);
            log.info("publishConfig dataId:{}, group:{}, result:{}", dataId+i, group+i, result);
            Assertions.assertTrue(result, "publishConfig check fail");
            cleanConfigMap.put(dataId+i, group+i);
        }

        for (int i = 0; i < MORE_NUM; i++) {
            config.addListener(dataId+i, group+i, listener);
        }

        latch.await(30, TimeUnit.SECONDS);
        Assertions.assertTrue(latch.getCount() == 0, "listen config is not expect one in timeout");
    }

    @Test
    @DisplayName("Publish and remove config, expect get null after remove config.")
    public void testRemoveConfig() throws Exception{
        content = RandomUtils.getStringWithCharacter(20);
        boolean result = config.publishConfig(dataId, group, content);
        log.info("publishConfig dataId:{}, group:{}, result:{}", dataId, group, result);
        Assertions.assertTrue(result, "publishConfig check fail");
        Thread.sleep(TIME_OUT);
        String value = config.getConfig(dataId, group, TIME_OUT);
        log.info("getConfig dataId:{}, group:{}, value:{}", dataId, group, value);
        Assertions.assertEquals(content, value, "getConfig value is not expect one");

        result = config.removeConfig(dataId, group);
        Assertions.assertTrue(result, "removeConfig check fail");
        Thread.sleep(TIME_OUT);
        value = config.getConfig(dataId, group, TIME_OUT);
        Assertions.assertEquals(null, value, "getConfig not null after remove");
    }

    @Test
    @DisplayName("Publish and change config，expect get correct config")
    public void testPublishAndChangeConfig() throws Exception {
        content = RandomUtils.getStringWithCharacter(20);
        boolean result = config.publishConfig(dataId, group, content);
        log.info("publishConfig dataId:{}, group:{}, result:{}", dataId, group, result);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);
        String value = config.getConfig(dataId, group, TIME_OUT);
        log.info("getConfig dataId:{}, group:{}, value:{}", dataId, group, value);
        Assertions.assertEquals(content, value, "getConfig value is not expect one");

        content += "_" + RandomUtils.getStringWithCharacter(10);
        result = config.publishConfig(dataId, group, content);
        log.info("publishConfig dataId:{}, group:{}, result:{}", dataId, group, result);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);
        value = config.getConfig(dataId, group, TIME_OUT);
        log.info("getConfig dataId:{}, group:{}, value:{}", dataId, group, value);
        Assertions.assertEquals(content, value, "getConfig value is not expect one");

        result = config.removeConfig(dataId, group);
        Assertions.assertTrue(result, "removeConfig check fail");
        Thread.sleep(TIME_OUT);
        value = config.getConfig(dataId, group, TIME_OUT);
        log.info("getConfig dataId:{}, group:{}, value:{}", dataId, group, value);
        Assertions.assertEquals(null, value, "getConfig value is not expect one");
    }

    @Test
    @DisplayName("Publish and add listener, expect get null after remove config.")
    public void testRemoveListener() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        content = RandomUtils.getStringWithCharacter(20);
        boolean result = config.publishConfig(dataId, group, content);
        log.info("publishConfig dataId:{}, group:{}, result:{}", dataId, group, result);
        Assertions.assertTrue(result, "publishConfig check fail");
        cleanConfigMap.put(dataId, group);
        Thread.sleep(TIME_OUT);

        Listener listener = getListener(dataId, group, content, System.currentTimeMillis(), latch);
        config.addListener(dataId, group, listener);
        latch.await(30, TimeUnit.SECONDS);
        Assertions.assertEquals(0, latch.getCount(), "listen config is not expect one in timeout");
        config.removeListener(dataId, group, listener);
        Assertions.assertNotSame(0, latch.getCount());
    }

    @Test
    @DisplayName("Publish encryption config and get decryption config use KMS.")
    public void testEncryptionAndDecryption() throws Exception {
        //KMS Encryption
        dataId = "cipher-" + RandomUtils.getStringWithCharacter(10);
        content = "！#￥%……u0026*（）测试u003cu003e《》~(){}【】”‘：；。，.,/?";
        boolean result = config.publishConfig(dataId, group, content);
        log.info("publishConfig dataId:{}, group:{}, result:{}", dataId, group, result);
        Assertions.assertTrue(result, "publish KMS encryption config check fail");
        cleanConfigMap.put(dataId, group);
        Thread.sleep(TIME_OUT);

        String value = config.getConfig(dataId, group, TIME_OUT);
        log.info("getConfig dataId:{}, group:{}, value:{}", dataId, group, value);
        Assertions.assertEquals(content, value, "getConfig value is not expect one");

        //KMS AES-128 Encryption
        dataId = "cipher-kms-aes-128-" + RandomUtils.getStringWithCharacter(10);
        content = "@128:"+"！#￥%……6*（）测试u《》~(){}【】”‘：；。，.,/?";
        result = config.publishConfig(dataId, group, content);
        log.info("publishConfig dataId:{}, group:{}, result:{}", dataId, group, result);
        Assertions.assertTrue(result, "publish KMS AES-128 encryption config check fail");
        cleanConfigMap.put(dataId, group);
        Thread.sleep(TIME_OUT);

        value = config.getConfig(dataId, group, TIME_OUT);
        log.info("getConfig dataId:{}, group:{}, value:{}", dataId, group, value);
        Assertions.assertEquals(content, value, "getConfig value is not expect one");

        //KMS AES-256 encryption
        dataId = "cipher-kms-aes-256-" + RandomUtils.getStringWithCharacter(10);
        content = "@256:"+"！#￥%……6*（）测试u《》~(){}【】”‘：；。，.,/?";
        result = config.publishConfig(dataId, group, content);
        log.info("publishConfig dataId:{}, group:{}, result:{}", dataId, group, result);
        Assertions.assertTrue(result, "publish KMS AES-256 encryption config check fail");
        cleanConfigMap.put(dataId, group);
        Thread.sleep(TIME_OUT);

        value = config.getConfig(dataId, group, TIME_OUT);
        log.info("getConfig dataId:{}, group:{}, value:{}", dataId, group, value);
        Assertions.assertEquals(content, value, "getConfig value is not expect one");
    }

    @Test
    @Timeout(value = 10 * TIME_OUT, unit = TimeUnit.MILLISECONDS)
    @DisplayName("Add listener After publish config，expect listener expect config")
    public void testAddListenerAfterPublishConfig() throws Exception {
        final AtomicInteger count = new AtomicInteger(0);
        content = "test-abc" + SPECIAL_CHARACTERS;
        boolean result = config.publishConfig(dataId, group, content);
        log.info("publishConfig dataId:{}, group:{}, result:{}", dataId, group, result);
        Assertions.assertTrue(result);
        Listener listener = newListener(count);
        config.addListener(dataId, group, listener);
        Thread.sleep(TIME_OUT);

        while (count.get() == 0) {
            //if wait count is 0 too long，can new addListener.
            config.addListener(dataId, group, newListener(count));
            Thread.sleep(TIME_OUT);
            log.info("wait " + TIME_OUT +"ms..." + "count:" + count.get());
        }
        Assertions.assertTrue(count.get() >= 1);

        //get config after listen
        String value = config.getConfig(dataId, group, TIME_OUT);
        log.info("getConfig dataId:{}, group:{}, value:{}", dataId, group, value);
        Assertions.assertEquals(content, value);

        config.removeListener(dataId, group, listener);
        result = config.removeConfig(dataId, group);
        Assertions.assertTrue(result);
    }

    @Test
    @Timeout(value = 10 * TIME_OUT, unit = TimeUnit.MILLISECONDS)
    @DisplayName("Add two listener After publish config，expect not affect listen after remove "
        + " one listener.")
    public void testAddTwoListener_removeLastListener() throws Exception {
        content = "test-abc-two" + SPECIAL_CHARACTERS;
        final AtomicInteger count = new AtomicInteger(0);
        boolean result = configNewConn1.publishConfig(dataId, group, content);
        Assertions.assertTrue(result);
        Thread.sleep(TIME_OUT);

        Listener ml = newListener(count);
        Listener ml1 = newListener(count);
        configNewConn1.addListener(dataId, group, ml);
        configNewConn1.addListener(dataId, group, ml1);
        Thread.sleep(TIME_OUT);

        while (count.get() == 0) {
            Thread.sleep(TIME_OUT);
            log.info("wait " + TIME_OUT +"ms..." + "count:" + count.get());
        }

        configNewConn1.removeListener(dataId, group, ml);
        Thread.sleep(TIME_OUT);
        Assertions.assertNotSame(0, count.get());

        configNewConn1.removeListener(dataId, group, ml1);
        result = configNewConn1.removeConfig(dataId, group);
        Assertions.assertTrue(result);
    }
}

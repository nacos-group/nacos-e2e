package com.alibaba.nacos.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.frame.BaseOperate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class ConfigBase extends BaseOperate {
    private static final Logger log = LoggerFactory.getLogger(ConfigBase.class);
    public static final long TIME_OUT = 3000;
    public static int MORE_NUM = 100;
    public static String DEFAULT_GROUP = "DEFAULT_GROUP";
    public static String SPECIAL_CHARACTERS = "！#￥%……6*（）测试u《》~(){}【】”‘：；。，.,/?";
    public static String SPECIAL_CHARACTERS1 = "测试abc!@~~*())(>?L!@@DSKED K한국어Tiếng Kinh/España";

    public static ConfigService config;

    @BeforeAll
    public static void setUpAll() throws Exception {
        config = NacosFactory.createConfigService(properties);
    }

    @AfterAll
    public static void tearDownAll() throws Exception {

    }

    public static Listener getListener(final String content, final CountDownLatch latch) {
        Listener listener = new Listener() {
            @Override
            public Executor getExecutor() {

                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                int count = (int)latch.getCount();
                log.info("receiveConfig configInfo:{}, count:{}", configInfo, count);
                if (count > 0) {
                    Assertions.assertEquals(content, configInfo, "receive config is not expect one");
                    latch.countDown();
                }
            }
        };
        return listener;
    }

    public static Listener getListener(final String dataId, final String group, final String content,
        final Long startTime, final CountDownLatch latch) {
        Listener listener = new Listener() {
            @Override
            public Executor getExecutor() {

                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                Long costTime = System.currentTimeMillis() - startTime;
                int count = (int)latch.getCount();
                log.info("receiveConfig dataId:{}, group:{}, costTime:{}MS, count:{}", dataId, group, costTime, count);
                if (count > 0) {
                    Assertions.assertEquals(content, configInfo, "receive config is not expect one");
                    latch.countDown();
                }
            }
        };
        return listener;
    }

    public  static Listener newListener(final AtomicInteger count1){
        final AtomicInteger count = count1;

        Listener listener = new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                count.incrementAndGet();
                log.info("receiveConfig configInfo:{}, count:{}", configInfo, count.get());
            }
        };
        return listener;
    }

    public  static Listener newListener( ){

        Listener listener = new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                log.info("receiveConfig configInfo:{}", configInfo);
                Assertions.assertTrue(false);
            }
        };
        return listener;
    }
}

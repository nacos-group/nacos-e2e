# -*- coding: utf-8 -*-
import os
import unittest
import random
import time
from datetime import date
import logging.handlers
import nacos_wrapper

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
LOG_FILE = BASE_DIR+'/nacos.log'
handler = logging.handlers.RotatingFileHandler(LOG_FILE, maxBytes=1024 * 1024, backupCount=5)
fmt = '%(asctime)s - %(filename)s:%(lineno)s - %(name)s - %(message)s'
formatter = logging.Formatter(fmt)
handler.setFormatter(formatter)
logger = logging.getLogger('nacos')
logger.addHandler(handler)
logger.setLevel(logging.DEBUG)

# logger = logging.getLogger('nacos')
# logger.setLevel(logging.DEBUG)
#
# console_handler = logging.StreamHandler()
# file_handler = logging.FileHandler("./log.txt",mode="a",encoding="utf-8")
# console_handler.setLevel(logging.DEBUG)
#
# console_fmt = '%(asctime)s - %(filename)s:%(lineno)s - %(name)s - %(message)s'
# file_fmt = '%(asctime)s - %(filename)s:%(lineno)s - %(name)s - %(message)s'
#
# console_formatter = logging.Formatter(fmt=console_fmt)
# file_formatter = logging.Formatter(fmt=file_fmt)
#
# console_handler.setFormatter(fmt=console_formatter)
# console_handler.setFormatter(fmt=file_formatter)
# logger.addHandler(console_handler)
# logger.addHandler(console_handler)

class MyTestCase(unittest.TestCase):


    # publish one config with different character, wait for 5 second to listen
    def test_publish_watcher(self):
        logger.info("case ====================test_publish_watcher==================== start")
        randomvalue = "test_" + str(random.randint(1, 1000)) + "_" + str(date.today())
        data_id = "nacos.python.test." + randomvalue
        group = "DEFAULT_GROUP"
        content = u"112233测试！@#￥$%……^&*()——+$^_=-~.|、|【】[]{}:'’‘“”《》.<>/?,「」"
#         content = "112233" + randomvalue
        logger.info("data_id->" + data_id + ",group->" + group + ",content->" + content)
        logger.debug("test log print")
        nacos_wrapper.addConfigWatcher(data_id, group)
        publish_config_re= nacos_wrapper.publishConfig(data_id, group, content)
        time.sleep(10)
        content_re= nacos_wrapper.content
        logger.info(content_re)
        remove_config_re= nacos_wrapper.removeConfig(data_id, group)
        time.sleep(5)
        get_config_re= nacos_wrapper.getConfig(data_id, group)
        self.assertEqual(content_re, content)
        self.assertEqual(publish_config_re, True)
        self.assertEqual(remove_config_re, True)
        self.assertEqual(get_config_re, None)
        logger.info("case ====================test_publish_watcher==================== finish")

    # publish one config with different character, wait for 5 second to get config.
    def test_publish_get(self):
        logger.info("case ====================test_publish_get==================== start")
        randomvalue = "test_" + str(random.randint(1, 1000)) + "_" + str(date.today())
        data_id = "nacos.python.test." + randomvalue
        group = "DEFAULT_GROUP"
        content = u"112233测试！@#￥$%……^&*()——+$^_=-~.|、|【】[]{}:'’‘“”《》.<>/?,「」"
#         content = "112233" + randomvalue
        logger.info("data_id->" + data_id + ",group->" + group + ",content->" + content)

        publish_config_re= nacos_wrapper.publishConfig(data_id, group, content)
        time.sleep(5)
        get_config_re= nacos_wrapper.getConfig(data_id, group)
        time.sleep(5)
        remove_config_re= nacos_wrapper.removeConfig(data_id, group)
        time.sleep(5)
        get_config_re2= nacos_wrapper.getConfig(data_id, group)
        self.assertEqual(publish_config_re, True)
        self.assertEqual(get_config_re, content)
        self.assertEqual(remove_config_re, True)
        self.assertEqual(get_config_re2, None)
        logger.info("case ====================test_publish_get==================== finish")

if __name__ == '__main__':
    unittest.main()

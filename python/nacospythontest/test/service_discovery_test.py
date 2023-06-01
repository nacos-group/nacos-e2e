# -*- coding: utf-8 -*-
import os
import unittest
import socket
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

class MyTestCase(unittest.TestCase):

    # register one instance and wait for 10 second to get all instance
    def test_register_instance(self):
        logger.info("case ====================test_register_instance==================== start")
        randomvalue = "test_" + str(random.randint(1, 1000)) + "_" + str(date.today())
        service_name = "nacos.python.test."+randomvalue
        hostname = socket.gethostname()
        ip=socket.gethostbyname(hostname)
        port=3001
        logger.info("serviceName->"+service_name+"ip->"+ip+"port->"+str(port))
        add_re = nacos_wrapper.addNamingInstance(service_name, ip, port)
        self.assertEqual(add_re, True)
        time.sleep(10)

        instances_re = nacos_wrapper.listNamingInstance(service_name)
        self.assertIsNotNone(instances_re)
        for key in range(len(instances_re['hosts'])):
            self.assertEqual(str(instances_re['hosts'][key]['ip']), ip, "ip check not equal")
            self.assertEqual(instances_re['hosts'][key]['port'], port, "port check not equal")
            self.assertIn(service_name, instances_re['hosts'][key]['serviceName'], "serviceName check not contain")

        remove_re=nacos_wrapper.removeNamingInstance(service_name, ip, port)
        self.assertEqual(remove_re, True)
        logger.info("case ====================test_register_instance==================== finish")

    # register one service and subscribe the service, after register instance wait for 10 second to listen the instance
    def test_listener_instance(self):
        logger.info("case ====================test_listener_instance==================== start")
        randomvalue = "test_" + str(random.randint(1, 1000)) + "_" + str(date.today())
        service_name = "nacos.python..test."+randomvalue
        hostname = socket.gethostname()
        ip=socket.gethostbyname(hostname)
        port=3001
        logger.info("serviceName->"+service_name+"ip->"+ip+"port->"+str(port))
        nacos_wrapper.subscribeServiceListener(service_name)
        add_re = nacos_wrapper.addNamingInstance(service_name, ip, port)
        time.sleep(10)
        logger.info("subscribe finished")
        time.sleep(10)
        logger.info("nacos_wrapper.serviceListenValue:" + str(nacos_wrapper.serviceListenValue))
        serviceValue = nacos_wrapper.serviceListenValue
        remove_re = nacos_wrapper.removeNamingInstance(service_name, ip, port)
        nacos_wrapper.unSubscribeServiceListener(service_name)
        logger.info(service_name+"has unsubscribed")
        time.sleep(5)
        nacos_wrapper.stopSubscribe()
        logger.info("subscribe has stopped")
        self.assertEqual(add_re, True)
        self.assertEqual(remove_re, True)
        self.assertEqual(serviceValue['ip'], ip, "ip check not equal")
        self.assertEqual(serviceValue['port'], port, "port check not equal")
        self.assertIn(service_name, serviceValue['serviceName'], "serviceName check not contain")
        logger.info("case ====================test_listener_instance==================== finish")

if __name__ == '__main__':
    unittest.main()

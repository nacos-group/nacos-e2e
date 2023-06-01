# -*- coding: utf-8 -*-
import logging
import os
import sys
import traceback

import nacos
from nacos.listener import SubscribeListener

from handle_config import do_config

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
LOG_FILE = BASE_DIR+'/nacos.log'
handler = logging.handlers.RotatingFileHandler(LOG_FILE, maxBytes=1024 * 1024, backupCount=5)
fmt = '%(asctime)s - %(filename)s:%(lineno)s - %(name)s - %(message)s'
formatter = logging.Formatter(fmt)
handler.setFormatter(formatter)
logger = logging.getLogger('nacos')
logger.addHandler(handler)
logger.setLevel(logging.DEBUG)

#get nacos servcer from env
env_dist = os.environ
SERVER_ADDRESSES = env_dist.get("serverList")
NAMESPACE = None
client = nacos.NacosClient(SERVER_ADDRESSES, namespace=NAMESPACE, username=None, password=None)
ip_list = []

row_content =""
content=""
namespace=""
group=""
data_id=""
serviceListenValue=""

def configCallback(args):
    print("watch Config:"+str(args))
    global content,row_content,namespace,group,data_id
    content=args["content"]
    row_content = args["raw_content"]
    namespace = args["namespace"]
    group = args["group"]
    data_id = args["data_id"]

def truncate_pangu_log():
    all_files = os.listdir('/dev/shm')
    pangu_logs = list(filter(lambda x: x.startswith('pangu_client_metric') and not x.endswith('lock'), all_files))
    for pangu_log in pangu_logs:
        pangu_log_path = os.path.join('/dev/shm', pangu_log)
        with open(pangu_log_path, 'w') as pangu_log_file:
            print("truncate file: " + pangu_log_path)
            pangu_log_file.truncate()

def addConfigWatcher(data_id, group):
    logger.info("addConfigWatcher success data_id:" + data_id + " group:" + group)
    try:
        truncate_pangu_log()
        client.add_config_watcher(data_id=data_id, group=group, cb=configCallback)
    except Exception as e:
        info = traceback.format_exc()
        print('addConfigWatcher get Exception',e)
        print('addConfigWatcher get Exception info',info)


def publishConfig(data_id,group,content):
    re=client.publish_config(data_id,group,content)
    if bool(re):
        logger.info("publishConfig success data_id:"+data_id+" group:"+group+" content:"+content)
    else:
        logger.info("publishConfig fail data_id:" + data_id + " group:" + group + " content:" + content)
    return re

def getConfig(data_id, group):
    re=client.get_config(data_id, group)
    logger.info("get_config success data_id:" + data_id + " group:" + group)
    return re

def removeConfig(data_id, group):
    re=client.remove_config(data_id, group)
    if bool(re):
        logger.info("remove_config success data_id:" + data_id + " group:" + group)
    else:
        logger.info("remove_config fail data_id:" + data_id + " group:" + group)
    return re

def addNamingInstance(service_name, ip, port):
    re=client.add_naming_instance(service_name, ip, port)
    if bool(re):
        logger.info("Nacos register success serviceName->" + service_name + "ip->" + ip + "port->" + str(port))
    else:
        logger.info("Nacos register fail serviceName->" + service_name + "ip->" + ip + "port->" + str(port))
    return re


def removeNamingInstance(service_name, ip, port):
    re=client.remove_naming_instance(service_name, ip, port)
    if bool(re):
        logger.info("Nacos deregister success serviceName->" + service_name + "ip->" + ip + "port->" + str(port))
    else:
        logger.info("Nacos deregister fail serviceName->" + service_name + "ip->" + ip + "port->" + str(port))
    return re

def listNamingInstance(service_name):
    re=client.list_naming_instance(service_name)
    if bool(re):
        logger.info("list Nacos Instance success:" + str(re))
    else:
        logger.info("list Nacos Instance fail:" + str(re))
    return re


def serviceListener(event, instance):
    logger.info("listening nacos service ==> " + str(event) + str(instance.instance))
    global serviceListenValue
    serviceListenValue = instance.instance
    logger.info("listened nacos service ==> " + str(serviceListenValue))
    pass

def subscribeServiceListener(service_name):
    fn1 = SubscribeListener(fn=serviceListener, listener_name="serviceListener")
    client.subscribe(fn1, 2, service_name, )
    logger.info("subscribeServiceListener success service_name:" + service_name)

def unSubscribeServiceListener(service_name):
    client.unsubscribe(service_name=service_name, listener_name="serviceListener")
    logger.info("unSubscribeServiceListener success service_name:" + service_name)

def stopSubscribe():
    client.stop_subscribe()
    logger.info("stop subscribe success")

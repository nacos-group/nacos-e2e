# -*- coding: utf-8 -*-
import os
from configparser import ConfigParser


class HandleConfig:
    def __init__(self, filename):
        self.filename = filename
        self.config = ConfigParser()
        self.config.read(self.filename, encoding="utf-8")

    def get_value(self, section, option):
        return self.config.get(section, option)  # str

    def get_int(self, section, option):  # int
        return self.config.getint(section, option)

    def get_float(self, section, option):  # float
        return self.get_float(section, option)

    def get_boolean(self, section, option):
        return self.get_boolean(section, option)

    def get_eval_data(self, section, option):
        return eval(self.config.get(section, option))

    @staticmethod
    def write_config(datas, filename):
        if isinstance(datas, dict):
            for value in datas.values():
                if not isinstance(value, dict):
                    return "data is not illegal"
            config = ConfigParser()
            for key in datas:
                config[key] = datas[key]
            with open(filename, 'w') as file:
                config.write(file)



BASE_DIR = os.path.dirname(os.path.abspath(__file__))
do_config = HandleConfig(os.path.join(BASE_DIR ,'nacos.conf'))
# do_config = HandleConfig("../conf/nacos.conf")
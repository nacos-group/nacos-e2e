# -*- coding: utf-8 -*-
import unittest
import os

def get_allcase():
    # path = os.getcwd()
    path = "test"
    discover = unittest.defaultTestLoader.discover(path, pattern="*_test.py")
    suite = unittest.TestSuite()
    suite.addTest(discover)
    return suite

if __name__ == '__main__':
    runner = unittest.TextTestRunner(verbosity=2)
    runner.run(get_allcase())


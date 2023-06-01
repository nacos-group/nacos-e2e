# -*- coding: utf-8 -*-
import json

def get_json_str(json_value):
    return json.dumps(json_value)

def parse_json_str(json_str, default_data=None):
    try:
        return json.loads(json_str)
    except:
        return default_data
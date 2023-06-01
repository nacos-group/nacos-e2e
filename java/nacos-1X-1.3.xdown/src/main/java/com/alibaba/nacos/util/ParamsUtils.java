package com.alibaba.nacos.util;

import java.util.HashMap;
import java.util.Map;

public class ParamsUtils {

    private Map<String, String> paramMap;

    public static ParamsUtils newParams() {
        ParamsUtils paramsUtils = new ParamsUtils();
        paramsUtils.paramMap = new HashMap<String, String>();
        return paramsUtils;
    }

    public ParamsUtils appendParam(String name, String value) {
        this.paramMap.put(name, value);
        return this;
    }

    public Map<String, String> done() {
        return paramMap;
    }
}
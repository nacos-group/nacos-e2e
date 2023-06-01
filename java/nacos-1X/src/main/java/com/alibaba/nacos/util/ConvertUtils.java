package com.alibaba.nacos.util;

import java.util.List;
import java.util.Map;

public class ConvertUtils {
    public static String listToString(List<String> header) {
        StringBuilder builder = new StringBuilder();
        for (String str : header) {
            builder.append(str).append(", ");
        }
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 2); // 移除最后一个逗号和空格
            String myString = builder.toString();
            return myString;
        } else {
            return "";
        }
    }

    public static String mapToString(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.append(entry.getKey()).append(":").append(entry.getValue()).append(", ");
        }
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 2); // 移除最后一个逗号和空格
            String myString = builder.toString();
            return myString;
        } else {
            return "";
        }
    }
}

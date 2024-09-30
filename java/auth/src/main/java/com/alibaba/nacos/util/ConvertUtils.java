package com.alibaba.nacos.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConvertUtils {
    public static String listToString(List<String> header) {
        StringBuilder builder = new StringBuilder();
        for (String str : header) {
            builder.append(str).append(", ");
        }
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 2); // remove last comma and space
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
            builder.setLength(builder.length() - 2); // remove last
            String myString = builder.toString();
            return myString;
        } else {
            return "";
        }
    }

    public static List<String> mapToList(Map<String, String> params) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            list.add(entry.getKey());
            list.add(entry.getValue());
        }
        return list;
    }
}

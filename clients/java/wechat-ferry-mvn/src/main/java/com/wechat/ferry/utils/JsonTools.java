package com.wechat.ferry.utils;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class JsonTools {
    private static final Gson gson = new Gson();


    // 字符串转json
    public static <T> T fromJson(String jsonStr, Class<T> tClass) {
        return gson.fromJson(jsonStr, tClass);
    }

    public static <T> T fromJson(String jsonStr, Type mapType) {
        return gson.fromJson(jsonStr, mapType);
    }

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }
}

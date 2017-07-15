package com.irwin.utils;


import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * created by Devil 2015/09/02
 * gson 工具
 */
public class GsonUtil {
    private static final Gson GSON = new Gson();

    private GsonUtil() {
            /* cannot be instantiated */
    }


    public static Gson getGson() {
        return GSON;
    }

    public static String toJson(Object source) {
        return GSON.toJson(source);
    }


    public static String toJson(Object source, Type type) {
        return GSON.toJson(source, type);
    }

    public static <T> T fromJson(String json, Class<T> cls) {
        return GSON.fromJson(json, cls);
    }

    public static <T> T fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }

    /**
     * Create fields and create a new instance of <code>type</code>.
     *
     * @param source Source object.
     * @param type   Class type.
     * @param <T>
     * @return
     */
    public static <T> T copyCreate(Object source, Class<T> type) {
        String srcStr = GSON.toJson(source);
        return GSON.fromJson(srcStr, type);
    }


}

package com.okhttp3utils.demo;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GsonUtil {

    // 3.将Json字符串转换成JsonObject对象
    public static JsonObject strToJsonObject(String jsonstr) {
        JsonObject jsonObject = new JsonParser().parse(jsonstr).getAsJsonObject();
        return jsonObject;
    }

}

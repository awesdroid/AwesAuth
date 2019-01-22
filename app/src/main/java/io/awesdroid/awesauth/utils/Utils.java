package io.awesdroid.awesauth.utils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.os.Looper;

import org.json.JSONObject;

/**
 * @auther Awesdroid
 */
public class Utils {

    public static boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }

    public static String prettyJson(String jsonStr) {
        JsonElement jsonElement = new JsonParser().parse(jsonStr);
        return new GsonBuilder().setPrettyPrinting().create().toJson(jsonElement);
    }

    public static String prettyJson(JSONObject json) {
        JsonObject jso = new JsonParser().parse(json.toString()).getAsJsonObject();
        return new GsonBuilder().setPrettyPrinting().create().toJson(jso);
    }
}

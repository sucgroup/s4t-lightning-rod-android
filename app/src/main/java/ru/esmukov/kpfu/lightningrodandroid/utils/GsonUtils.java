package ru.esmukov.kpfu.lightningrodandroid.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kostya on 07/04/2017.
 */

public class GsonUtils {

    public static Gson createGson() {
        Gson gson = new GsonBuilder().create();
        return gson;
    }

    public static JsonObject emptyJsonObject() {
        return createGson().toJsonTree(new HashMap<String, Object>()).getAsJsonObject();
    }

    /**
     * JavaScript Object.assign alike
     * @param destinationObject
     * @param objs
     */
    public static void extendJsonObject(JsonObject destinationObject, JsonObject... objs) {
        // http://stackoverflow.com/a/34092374
        for (JsonObject obj : objs) {
            extendJsonObject(destinationObject, obj);
        }
    }

    private static void extendJsonObject(JsonObject leftObj, JsonObject rightObj) {
        for (Map.Entry<String, JsonElement> rightEntry : rightObj.entrySet()) {
            String rightKey = rightEntry.getKey();
            JsonElement rightVal = rightEntry.getValue();
            if (leftObj.has(rightKey)) {  // conflict
                JsonElement leftVal = leftObj.get(rightKey);
                if (leftVal.isJsonObject() && rightVal.isJsonObject()) {
                    // recursive merging
                    extendJsonObject(leftVal.getAsJsonObject(), rightVal.getAsJsonObject());
                } else {
                    // i.e. list, primitive -- keep the one from rightObj
                    leftObj.add(rightKey, deepCopy(rightVal));
                }
            } else {  // no conflict, add to the object
                leftObj.add(rightKey, deepCopy(rightVal));
            }
        }
    }

    private static JsonElement deepCopy(JsonElement element) {
        // https://github.com/google/gson/issues/760
        // https://github.com/google/gson/issues/301

        if (element.isJsonNull() || element.isJsonPrimitive())
            // these are immutable
            return element;

        if (element.isJsonObject()) {
            JsonObject result = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                result.add(entry.getKey(), deepCopy(entry.getValue()));
            }
            return result;
        }

        if (element.isJsonArray()) {
            JsonArray result = new JsonArray();
            for (JsonElement el : element.getAsJsonArray()) {
                result.add(deepCopy(el));
            }
            return result;
        }

        throw new AssertionError("Unreachable");
    }


}

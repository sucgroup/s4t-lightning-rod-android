package ru.esmukov.kpfu.lightningrodandroid.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import ru.esmukov.kpfu.lightningrodandroid.utils.GsonUtils;

/**
 * Created by kostya on 07/04/2017.
 */

public class SettingsStorage {
    public static final String KEY_DELIMITER_SPLIT_REGEX = "[.]";

    private SharedPreferences mSharedPreferences;
    private JsonObject mDefaultSettingsJson;

    SettingsStorage(Context context, JsonObject defaultSettings) {
        mSharedPreferences = new SharedPreferencesFactory(context)
                .getLrSettingsSharedPreferences();

        mDefaultSettingsJson = defaultSettings;
    }

    public JsonObject toJsonObject() {
        Gson gson = GsonUtils.createGson();


        JsonObject targetElement = GsonUtils.emptyJsonObject();
        JsonObject defaultSettings = mDefaultSettingsJson;
        JsonObject userSettings = sharedPreferencesToJsonObject(mSharedPreferences);

        GsonUtils.extendJsonObject(targetElement,
                defaultSettings,
                userSettings);

        return targetElement;
    }

    @NotNull
    public String getString(String key) {
        String ret = mSharedPreferences.getString(key, null);

        if (ret == null) {
            ret = getStringByKeyFromJson(key, mDefaultSettingsJson.getAsJsonObject());
        }
        return ret;
    }

    public void setString(String key, @NotNull String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public double getDouble(String key) {
        return Double.valueOf(getString(key));
    }

    public void setDouble(String key, double value) {
        setString(key, Double.toString(value));
    }

    private static String getStringByKeyFromJson(String key, JsonObject jsonObject) {
        String[] keys = key.split(KEY_DELIMITER_SPLIT_REGEX, 2);

        if (keys.length == 1 || "".equals(keys[1])) {
            JsonElement element = jsonObject.get(keys[0]);
            if (element.isJsonNull())
                return ""; // todo is it OK to cast null to "" ???
            if (!element.isJsonPrimitive())
                throw new IllegalArgumentException("Leaf value is not a json primitive");
            return element.getAsString();
        }

        return getStringByKeyFromJson(keys[1], jsonObject.getAsJsonObject(keys[0]));
    }

    private static JsonObject sharedPreferencesToJsonObject(SharedPreferences sharedPreferences) {
        JsonObject res = GsonUtils.emptyJsonObject();

        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            putStringByKeyToJson(entry.getKey(), (String) entry.getValue(), res);
        }

        return res;
    }

    private static void putStringByKeyToJson(String key, String value, JsonObject jsonObject) {

        String[] keys = key.split(KEY_DELIMITER_SPLIT_REGEX, 2);

        if (keys.length == 1 || "".equals(keys[1])) {
            jsonObject.addProperty(keys[0], value);
            return;
        }

        JsonElement element = jsonObject.get(keys[0]);

        if (element == null) {
            jsonObject.add(keys[0], GsonUtils.emptyJsonObject());
            element = jsonObject.get(keys[0]);
        }

        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Unable to descend into not an object element");
        }

        putStringByKeyToJson(keys[1], value, element.getAsJsonObject());
    }
}

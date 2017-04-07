package ru.esmukov.kpfu.lightningrodandroid.settings;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ru.esmukov.kpfu.lightningrodandroid.NodeAssetsManager;
import ru.esmukov.kpfu.lightningrodandroid.utils.GsonUtils;

/**
 * Created by kostya on 07/04/2017.
 */

public class SettingsJsonManager {

    public static JsonObject loadDefaultSettings(NodeAssetsManager nodeAssetsManager) {
        try (Reader reader = new FileReader(getDefaultSettingsPath(nodeAssetsManager))) {
            return parseSettingsJson(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveSettings(NodeAssetsManager nodeAssetsManager,
                                    JsonObject overrideSettings) throws IOException {
        String path = getSettingsPath(nodeAssetsManager);
        Gson gson = GsonUtils.createGson();

        // settings.json doesn't have a strict schema, so simply writing the overrideSettings
        // object leads to loosing previously saved data which is not a part of
        // the overrideSettings schema.
        // Workaround this by reading json as a tree of JsonElements and merging overrideSettings
        // into that tree.

        JsonElement rawJson;
        try (Reader reader = new FileReader(path)) {
            rawJson = gson.fromJson(reader, JsonElement.class);
        }

        GsonUtils.extendJsonObject(
                rawJson.getAsJsonObject(),
                overrideSettings);

        try (Writer writer = new FileWriter(path)) {
            gson.toJson(rawJson, writer);
        }
    }

    private static String getSettingsPath(NodeAssetsManager nodeAssetsManager) {
        return nodeAssetsManager.getJsPath() + "/settings.json";
    }

    private static String getDefaultSettingsPath(NodeAssetsManager nodeAssetsManager) {
        return nodeAssetsManager.getJsPath() + "/settings.android.default.json";
    }

    private static JsonElement parseSettingsJson(Reader jsonReader) {
        return GsonUtils.createGson().fromJson(jsonReader, JsonElement.class);
    }
}

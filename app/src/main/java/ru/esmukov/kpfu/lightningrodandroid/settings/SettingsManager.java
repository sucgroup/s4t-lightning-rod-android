package ru.esmukov.kpfu.lightningrodandroid.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import ru.esmukov.kpfu.lightningrodandroid.NodeAssetsManager;
import ru.esmukov.kpfu.lightningrodandroid.settings.model.Settings;

/**
 * Created by kostya on 23/03/2017.
 */

public class SettingsManager {

    private Settings mSettings;
    private NodeAssetsManager mNodeAssetsManager;

    private SettingsManager(Settings settings, NodeAssetsManager nodeAssetsManager) {
        this.mSettings = settings;
        this.mNodeAssetsManager = nodeAssetsManager;
    }

    public static SettingsManager load(NodeAssetsManager nodeAssetsManager) {
        try (Reader reader = new FileReader(getSettingsPath(nodeAssetsManager))) {
            return new SettingsManager(parseSettingsJson(reader), nodeAssetsManager);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Settings parseSettingsJson(Reader jsonReader) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(jsonReader, Settings.class);
    }

    private static String getSettingsPath(NodeAssetsManager nodeAssetsManager) {
        return nodeAssetsManager.getJsPath() + "/settings.json";
    }

    public Settings getSettings() {
        return mSettings;
    }

    public void save() throws IOException {
        String path = getSettingsPath(mNodeAssetsManager);
        Gson gson = new GsonBuilder().create();

        // settings.json doesn't have a strict schema, so simply writing the mSettings object
        // leads to loosing previously saved data which is not a part of the Settings.class schema.
        // Workaround this by reading json as a tree of JsonElements and merging mSettings
        // into that tree.

        JsonElement rawJson;
        try (Reader reader = new FileReader(path)) {
             rawJson = gson.fromJson(reader, JsonElement.class);
        }

        extendJsonObject(
                rawJson.getAsJsonObject(),
                gson.toJsonTree(mSettings, Settings.class).getAsJsonObject());

        try (Writer writer = new FileWriter(path)) {
            gson.toJson(rawJson, writer);
        }
    }

    private static void extendJsonObject(JsonObject destinationObject, JsonObject... objs) {
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
                    leftObj.add(rightKey, rightVal);
                }
            } else {  // no conflict, add to the object
                leftObj.add(rightKey, rightVal);
            }
        }
    }
}

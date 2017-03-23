package ru.esmukov.kpfu.lightningrodandroid.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

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
        try {
            return new SettingsManager(parseSettingsJson(
                    new FileReader(getSettingsPath(nodeAssetsManager))), nodeAssetsManager);
        } catch (FileNotFoundException e) {
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
        Gson gson = new GsonBuilder().create();
        gson.toJson(mSettings, new FileWriter(getSettingsPath(mNodeAssetsManager)));
    }
}

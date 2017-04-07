package ru.esmukov.kpfu.lightningrodandroid.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by kostya on 07/04/2017.
 */

public class SharedPreferencesFactory {
    public static final String LR_SETTINGS_PREFS_NAME = "SettingsJsonPrefs";

    private Context mContext;

    SharedPreferencesFactory(Context context) {
        mContext = context;
    }

    public SharedPreferences getLrSettingsSharedPreferences() {
        return mContext.getSharedPreferences(LR_SETTINGS_PREFS_NAME, 0);
    }
}

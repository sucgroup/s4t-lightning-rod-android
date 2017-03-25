package ru.esmukov.kpfu.lightningrodandroid;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.esmukov.kpfu.lightningrodandroid.settings.SettingsManager;
import ru.esmukov.kpfu.lightningrodandroid.settings.model.board.Board;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    private static final String TAG = "SettingsActivity";

    private static void updatePreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list.
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);

        } else {
            // For all other preferences, set the summary to the value's
            // simple string representation.
            preference.setSummary(stringValue);
        }
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || LightningRodPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            // bindPreferenceSummaryToValue(findPreference("example_text"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().onBackPressed();
                // startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class LightningRodPreferenceFragment extends PreferenceFragment {
        private NodeAssetsManager mNodeAssetsManager;
        private SettingsManager mSettingsManager;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_lightning_rod);
            setHasOptionsMenu(true);

            mNodeAssetsManager = new NodeAssetsManager(getActivity());
            mSettingsManager = SettingsManager.load(mNodeAssetsManager);

            initPreferences();
        }

        private void initPreferences() {
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            initEditPreference(
                    "wamp_url",
                    mSettingsManager.getSettings().getConfig().getWamp().getUrl(),
                    new Setter<String>() {
                        @Override
                        public void set(String value) {
                            mSettingsManager.getSettings().getConfig().getWamp().setUrl(value);
                        }
                    });

            initEditPreference(
                    "wamp_port",
                    mSettingsManager.getSettings().getConfig().getWamp().getPort(),
                    new Setter<String>() {
                        @Override
                        public void set(String value) {
                            mSettingsManager.getSettings().getConfig().getWamp().setPort(value);
                        }
                    });
            initEditPreference(
                    "wamp_realm",
                    mSettingsManager.getSettings().getConfig().getWamp().getRealm(),
                    new Setter<String>() {
                        @Override
                        public void set(String value) {
                            mSettingsManager.getSettings().getConfig().getWamp().setRealm(value);
                        }
                    });

            initEditPreference(
                    "reverse_url",
                    mSettingsManager.getSettings().getConfig().getReverse().getServer().getUrl(),
                    new Setter<String>() {
                        @Override
                        public void set(String value) {
                            mSettingsManager.getSettings().getConfig().getReverse().getServer().setUrl(value);
                        }
                    });
            initEditPreference(
                    "reverse_port",
                    mSettingsManager.getSettings().getConfig().getReverse().getServer().getPort(),
                    new Setter<String>() {
                        @Override
                        public void set(String value) {
                            mSettingsManager.getSettings().getConfig().getReverse().getServer().setPort(value);
                        }
                    });

            initEditPreference(
                    "board_code",
                    mSettingsManager.getSettings().getConfig().getBoard().getCode(),
                    new Setter<String>() {
                        @Override
                        public void set(String value) {
                            mSettingsManager.getSettings().getConfig().getBoard().setCode(value);
                        }
                    });
            initListPreference(
                    "board_status",
                    mSettingsManager.getSettings().getConfig().getBoard().getStatus(),
                    new Setter<Board.Status>() {
                        @Override
                        public void set(Board.Status value) {
                            mSettingsManager.getSettings().getConfig().getBoard().setStatus(value);
                        }
                    });

            initEditPreference(
                    "position_altitude",
                    Double.toString(mSettingsManager.getSettings().getConfig().getBoard().getPosition().getAltitude()),
                    new Setter<String>() {
                        @Override
                        public void set(String value) {
                            mSettingsManager.getSettings().getConfig().getBoard().getPosition().setAltitude(Double.valueOf(value));
                        }
                    });
            initEditPreference(
                    "position_longitude",
                    Double.toString(mSettingsManager.getSettings().getConfig().getBoard().getPosition().getLongitude()),
                    new Setter<String>() {
                        @Override
                        public void set(String value) {
                            mSettingsManager.getSettings().getConfig().getBoard().getPosition().setLongitude(Double.valueOf(value));
                        }
                    });
            initEditPreference(
                    "position_latitude",
                    Double.toString(mSettingsManager.getSettings().getConfig().getBoard().getPosition().getLatitude()),
                    new Setter<String>() {
                        @Override
                        public void set(String value) {
                            mSettingsManager.getSettings().getConfig().getBoard().getPosition().setLatitude(Double.valueOf(value));
                        }
                    });
        }

        private boolean saveSettings() {
            try {
                mSettingsManager.save();
                return true;
            }
            catch (IOException e) {
                Log.e(TAG, "Unable to save settings.json", e);
                Toast.makeText(getActivity(), "Unable to save settings.json", Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        }

        private void initEditPreference(String id, String value, final Setter<String> setter) {
            EditTextPreference editTextPreference = (EditTextPreference) findPreference(id);
            editTextPreference.setText(value);

            // render summary immediately
            updatePreferenceSummary(editTextPreference,
                    PreferenceManager
                        .getDefaultSharedPreferences(editTextPreference.getContext())
                        .getString(editTextPreference.getKey(), ""));

            editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    setter.set((String) newValue);

                    boolean res = saveSettings();
                    if (res) {
                        updatePreferenceSummary(preference, newValue);
                    }
                    return res;
                }
            });
        }

        //private void initListPreference(String id, Class<? extends Enum> enumClass) {
        private <E extends Enum> void initListPreference(String id, final E value, final Setter<E> setter) {
            ListPreference listPreference = (ListPreference) findPreference(id);

            List<String> names = new ArrayList<>();
            List<String> values = new ArrayList<>();
            for (Enum enumValue : value.getClass().getEnumConstants()) {
                names.add(StringUtils.capfirst(enumValue.name()));
                values.add(enumValue.name().toLowerCase());
            }

            // http://stackoverflow.com/a/15264299
            listPreference.setEntries(names.toArray(new String[0]));
            listPreference.setEntryValues(values.toArray(new String[0]));

            listPreference.setValue(value.name().toLowerCase());

            // render summary immediately
            updatePreferenceSummary(listPreference,
                    PreferenceManager
                            .getDefaultSharedPreferences(listPreference.getContext())
                            .getString(listPreference.getKey(), ""));

            listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    setter.set((E) findEnumByLowercaseStringValue((String) newValue, value.getClass()));

                    boolean res = saveSettings();
                    if (res) {
                        updatePreferenceSummary(preference, newValue);
                    }
                    return res;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().onBackPressed();
                // startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private static <E extends Enum> E findEnumByLowercaseStringValue(String value, Class<E> enumClass) {
            for (E enumValue : enumClass.getEnumConstants()) {
                if (enumValue.name().toLowerCase().equals(value))
                    return enumValue;
            }
            throw new IllegalArgumentException();
        }

        private interface Setter<T> {
            void set(T value);
        }
    }
}

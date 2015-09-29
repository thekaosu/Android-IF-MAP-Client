package de.simu.decoit.android.decomap.activities.setupview;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import de.simu.decoit.android.decomap.activities.R;
import de.simu.decoit.android.decomap.preferences.PreferencesValues;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * Fragment for setting Preferences
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class SetupFragment extends PreferenceFragment {

    private String selectedMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Toolbox.logTxt(this.getClass().getName(), "SetupFragment.OnCreate(...) called");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        initializeFunctionality();

        handleSettingsLock();

    }

    @Override
    public void onResume() {
        Toolbox.logTxt(this.getClass().getName(), "SetupActivity.OnResume(...) called");
        super.onResume();
        handleSettingsLock();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //removing the padding
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if (v != null) {
            ListView lv = (ListView) v.findViewById(android.R.id.list);
            lv.setPadding(0, 0, 0, 0);
        }
        return v;
    }

    private void handleSettingsLock() {
        ListPreference runningModeSettings = (ListPreference) findPreference("monitoringModeSettings");
        PreferenceCategory connectionSettings = (PreferenceCategory) findPreference("connectionSettings");
        PreferenceCategory applicationSettings = (PreferenceCategory) findPreference("applicationSettings");
        PreferenceScreen locationTrackingSettings = (PreferenceScreen) findPreference("locationSettings");


        // lock/unlock user and server settings
        if (PreferencesValues.sLockPreferences) {
            connectionSettings.setEnabled(false);
            applicationSettings.setEnabled(false);
            runningModeSettings.setEnabled(false);
        } else {
            connectionSettings.setEnabled(true);
            applicationSettings.setEnabled(true);
            runningModeSettings.setEnabled(true);
        }

        // lock/unlock connection settings
        if (PreferencesValues.sLockConnectionPreferences) {
            connectionSettings.setEnabled(false);
        } else {
            connectionSettings.setEnabled(true);
        }

        // lock/unlock location tracking system
        if (locationTrackingSettings != null) {
            if (PreferencesValues.sLockLocationTrackingOptions) {
                locationTrackingSettings.setEnabled(false);
            } else {
                locationTrackingSettings.setEnabled(true);
            }
        }
    }

    private void initializeFunctionality(){
        final ListPreference mode = (ListPreference) findPreference("monitoringModeSettings");
        selectedMode = mode.getValue();
        mode.setSummary(selectedMode);

        mode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Toolbox.logTxt(this.getClass().getName(), "monitoringModeSettings.OnPreferenceChangeListener(...) called");
                if (selectedMode != null && !selectedMode.equals(newValue)) {
                    selectedMode= (String) newValue;
                    mode.setSummary(selectedMode);
                    PreferenceScreen screen = SetupFragment.this.getPreferenceScreen();
                    screen.removeAll();
                    screen.addPreference(mode);
                    if (newValue.equals("IF-MAP")) {
                        addPreferencesFromResource(R.xml.ifmap_preferences);
                    } else if (newValue.equals("iMonitor")) {
                        addPreferencesFromResource(R.xml.imonitor_preferences);
                    }
                }
                return true;
            }
        });

        if (mode.getValue().equals("IF-MAP")) {
            addPreferencesFromResource(R.xml.ifmap_preferences);
        } else if (mode.getValue().equals("iMonitor")) {
            addPreferencesFromResource(R.xml.imonitor_preferences);
        }
    }
}

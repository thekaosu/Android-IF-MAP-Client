package de.simu.decoit.android.decomap.activities.setupview.fragments;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.ToggleButton;

import de.simu.decoit.android.decomap.activities.setupview.SwitchPreferenceHandler;

/**
 * Abstract Fragment with backbutton and preference inside of the header
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public abstract class AbstractSwitchFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    protected int fragmentID;
    protected long keyID;
    private SwitchPreferenceHandler switchPreferenceHandler;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(this);

        addPreferencesFromResource(fragmentID);
        ActionBar actionbar = (getActivity()).getActionBar();
        ToggleButton actionBarSwitch = new ToggleButton(getActivity());

        if (actionbar != null) {
            actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
            actionbar.setCustomView(actionBarSwitch, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL
                    | Gravity.RIGHT));

            actionbar.setDisplayHomeAsUpEnabled(true);

        }

        switchPreferenceHandler = new SwitchPreferenceHandler(getActivity(), actionBarSwitch, keyID+"");
        updateSettings();
    }

    public void onResume() {
        super.onResume();
        switchPreferenceHandler.resume();
        updateSettings();
    }

    public void onPause() {
        super.onPause();
        switchPreferenceHandler.pause();
    }

    protected void updateSettings() {
        boolean available = switchPreferenceHandler.isSwitchOn();

        int count = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < count; ++i) {
            Preference pref = getPreferenceScreen().getPreference(i);
            pref.setEnabled(available);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(key)) {
            updateSettings();
        }
    }
}

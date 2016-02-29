package de.simu.decoit.android.decomap.activities.setupview.fragments;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.view.Gravity;
import android.widget.Switch;

import de.simu.decoit.android.decomap.activities.setupview.SwitchPreferenceHandler;

/**
 * Abstract Fragment with backbutton and preference inside of the header
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public abstract class AbstractSwitchFragment extends AbstractPreferenceFragment {

    protected long keyID;

    private SwitchPreferenceHandler switchPreferenceHandler;

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ActionBar actionbar = (getActivity()).getActionBar();
        Switch actionBarSwitch = new Switch(getActivity());

        if (actionbar != null) {
            actionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
            actionbar.setCustomView(actionBarSwitch, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER_VERTICAL
                    | Gravity.END));

            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        switchPreferenceHandler = new SwitchPreferenceHandler(getActivity(), actionBarSwitch, keyID + "");
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

    private void updateSettings() {
        boolean available = switchPreferenceHandler.isSwitchOn();

        int count = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < count; ++i) {
            Preference pref = getPreferenceScreen().getPreference(i);
            pref.setEnabled(available);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        super.onSharedPreferenceChanged(sharedPreferences, key);
        updateSettings();
    }


}

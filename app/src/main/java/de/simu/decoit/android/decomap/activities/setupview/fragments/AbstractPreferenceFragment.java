package de.simu.decoit.android.decomap.activities.setupview.fragments;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.InputType;

import java.util.ArrayList;

import de.simu.decoit.android.decomap.activities.R;
import de.simu.decoit.android.decomap.activities.setupview.fragments.preferences.PreferenceFileChooserDialogPreference;

/**
 * Abstract Fragment with backbutton and summary changer
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public abstract class AbstractPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    int fragmentID;
    final ArrayList<String> dynamicSummaryKeys = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (fragmentID == 0) {
            throw new IllegalArgumentException(getString(R.string.illegalFragmentID));
        }
        addPreferencesFromResource(fragmentID);

        ActionBar actionbar = (getActivity()).getActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        updateAllSummarys();
    }

    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
    }

    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (dynamicSummaryKeys.contains(key)) {
            updateSummary(findPreference(key));
        }
    }

    void updateAllSummarys() {
        for (String key : dynamicSummaryKeys) {
            updateSummary(findPreference(key));
        }
    }

    private void updateSummary(Preference p) {
        if (p instanceof ListPreference) {
            ListPreference listPref = (ListPreference) p;
            p.setSummary(listPref.getEntry());
        } else if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            if (editTextPref.getText() == null || editTextPref.getText().trim().length() == 0) {
                p.setSummary(getString(R.string.preferences_values_novalue));
            } else if (editTextPref.getEditText().getInputType() == (InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                StringBuilder stars = new StringBuilder();
                for (int i = 0; i < editTextPref.getText().length(); i++) {
                    stars.append("*");
                }
                p.setSummary(stars.toString());
            } else {
                p.setSummary(editTextPref.getText());
            }
        } else if (p instanceof PreferenceFileChooserDialogPreference) {
            PreferenceFileChooserDialogPreference fileChooser = (PreferenceFileChooserDialogPreference) p;
            fileChooser.setSummary(fileChooser.getPath());
        }
    }

}

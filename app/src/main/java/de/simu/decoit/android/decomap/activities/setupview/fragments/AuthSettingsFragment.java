/*
 * AuthSettingsFragment..java          0.3 2015-03-08
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 3.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.simu.decoit.android.decomap.activities.setupview.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.widget.BaseAdapter;

import de.simu.decoit.android.decomap.activities.R;

/**
 * Fragment for basic auth setting Preferences
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.3
 */
public class AuthSettingsFragment extends AbstractPreferenceFragment {

    private ListPreference authType;

    public AuthSettingsFragment() {
        fragmentID = R.xml.preferences_auth_fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        dynamicSummaryKeys.add(getActivity().getResources().getString(R.string.preferences_keys_authentication_type));
        dynamicSummaryKeys.add(getActivity().getResources().getString(R.string.preferences_keys_keystore_path));
        dynamicSummaryKeys.add(getActivity().getResources().getString(R.string.preferences_keys_keystore_password));
        dynamicSummaryKeys.add(getActivity().getResources().getString(R.string.preferences_keys_password));
        dynamicSummaryKeys.add(getActivity().getResources().getString(R.string.preferences_keys_username));

        super.onCreate(savedInstanceState);

        updateSettings();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (authType.getKey().equals(key)) {
            updateSettings();
        } else {
            super.onSharedPreferenceChanged(sharedPreferences, key);
        }
    }

    private void updateSettings() {
        getPreferenceScreen().removeAll();
        addPreferencesFromResource(fragmentID);

        authType = (ListPreference) findPreference(getActivity().getResources().getString(R.string.preferences_keys_authentication_type));
        if (authType.getValue() == null) {
            authType.setValueIndex(0);
        }

        if (authType.getValue().equals(getResources().getStringArray(R.array.preferences_auth_types)[0])) {
            addPreferencesFromResource(R.xml.preferences_basicauth_fragment);
        } else if (authType.getValue().equals(getResources().getStringArray(R.array.preferences_auth_types)[1])) {
            addPreferencesFromResource(R.xml.preferences_keystoreauth_fragment);
        }

        ((BaseAdapter) getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
        updateAllSummarys();

    }
}

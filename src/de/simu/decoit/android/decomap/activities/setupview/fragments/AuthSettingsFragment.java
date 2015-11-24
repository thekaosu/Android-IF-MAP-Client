/*
 * BasicAuthSettingsFragment.java        0.2 2015-03-08
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
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
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;

import de.simu.decoit.android.decomap.activities.R;
import de.simu.decoit.android.decomap.activities.setupview.FileChooserPreferenceDialog;

/**
 * Fragment for basic auth setting Preferences
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class AuthSettingsFragment extends AbstractPreferenceFragment {

    public AuthSettingsFragment() {
        fragmentID = R.xml.preferences_basicauth_fragment;
        dynamicSummaryKeys.add("passwordPreference");
        dynamicSummaryKeys.add("usernamePreference");


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Preference auth = findPreference("authentication");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        auth.setSummary(prefs.getString("keystorePath", Environment.getExternalStorageDirectory() + "/ifmap-client/keystore/"));
        auth.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new FileChooserPreferenceDialog(getActivity(), "", "keystorePath").show();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                preference.setSummary(prefs.getString("keystorePath", Environment.getExternalStorageDirectory() + "/ifmap-client/keystore/"));
                return false;
            }
        });
    }
}

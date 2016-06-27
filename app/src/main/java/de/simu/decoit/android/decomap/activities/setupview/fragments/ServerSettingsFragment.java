/*
 * EsukomMetadataSettingsFragment..java          0.3 2015-03-08
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
import android.preference.PreferenceManager;

import de.simu.decoit.android.decomap.activities.R;
import de.simu.decoit.android.decomap.activities.setupview.SetupAdapter;

/**
 * Fragment for server setting Preferences
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.3
 */
public class ServerSettingsFragment extends AbstractPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        String mode = prefs.getString(SetupAdapter.MONITORINGMODE_VIEW_ID + "", getActivity().getResources().getTextArray(R.array.preferences_value_serverForm)[1].toString());
        if (mode.equals(getActivity().getResources().getTextArray(R.array.preferences_value_serverForm)[1].toString())) {
            fragmentID = R.xml.preferences_server_fragment_ifmap;
            dynamicSummaryKeys.add(getActivity().getResources().getString(R.string.preferences_keys_ifmap_mapserver_ip));
            dynamicSummaryKeys.add(getActivity().getResources().getString(R.string.preferences_keys_ifmap_mapserver_port));
        } else if (mode.equals(getActivity().getResources().getTextArray(R.array.preferences_value_serverForm)[0].toString())) {
            fragmentID = R.xml.preferences_server_fragment_imonitor;
            dynamicSummaryKeys.add(getActivity().getResources().getString(R.string.preferences_keys_imonitor_server_ip));
            dynamicSummaryKeys.add(getActivity().getResources().getString(R.string.preferences_keys_imonitor_server_port));
        }
        super.onCreate(savedInstanceState);
    }
}
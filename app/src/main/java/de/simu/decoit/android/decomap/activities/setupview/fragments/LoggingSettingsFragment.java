/*
 * LoggingSettingsFragment.java        0.2 2015-03-08
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

import android.os.Bundle;

import de.simu.decoit.android.decomap.activities.R;

/**
 * Fragment logging Preferences
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class LoggingSettingsFragment extends AbstractPreferenceFragment {

    public LoggingSettingsFragment() {
        fragmentID = R.xml.preferences_logging_fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        dynamicSummaryKeys.add(getActivity().getResources().getString(R.string.preferences_keys_logpath));
        super.onCreate(savedInstanceState);
    }
}

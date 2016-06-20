/*
 * PreferenceValidator.java       0.2 2015-03-08
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
package de.simu.decoit.android.decomap.preferences;

import android.content.res.Resources;
import android.text.TextUtils;
import android.widget.TextView;

import java.util.regex.Matcher;

import de.simu.decoit.android.decomap.activities.R;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * Validate current preference configuration
 *
 * @author Leonid Schwenke, DECOIT GmbH
 */
public class PreferenceValidator {

    /**
     * check if the preference values are valid
     *
     * @param mPreferences Preferences to get values from
     * @param res android resources
     * @param mStatusMessageField TextView field to append errors onto
     * @return are the preferences incorrect
     */
    public static boolean incorrectPreferencesConfiguration(PreferencesValues mPreferences, Resources res, TextView mStatusMessageField) {

        if (mPreferences.getMonitoringPreference().equalsIgnoreCase(res.getTextArray(R.array.preferences_value_serverForm)[0].toString())) {
            // validate password (defaults always to "icinga")
            if (mPreferences.getNscaPassPreference() == null) {
                mStatusMessageField.append("\n"
                        + res.getString(
                        R.string.main_status_message_errorprefix)
                        + res.getString(R.string.main_status_message_nsca_password_null));
                return true;
            }

            // validate ip-setting from preferences
            if (mPreferences.getIMonitorServerIpPreference() == null
                    || mPreferences.getIMonitorServerIpPreference().length() == 0) {
                mStatusMessageField.append("\n"
                        + res.getString(
                        R.string.main_status_message_errorprefix)
                        + res.getString(R.string.main_status_message_imonitor_ip_null));
                return true;
            } else {
                Matcher ipMatcher = Toolbox.getIpPattern().matcher(
                        mPreferences.getIMonitorServerIpPreference());
                if (!ipMatcher.find()) {
                    mStatusMessageField.append("\n"
                            + res.getString(
                            R.string.main_status_message_errorprefix)
                            + res.getString(R.string.main_status_message_imonitor_ip_not_valid));
                    return true;
                }
            }
            // validate portnumber
            if (mPreferences.getIMonitorServerPortPreference() == null ||
                    mPreferences.getIMonitorServerPortPreference().length() == 0) {
                mStatusMessageField.append("\n"
                        + res.getString(
                        R.string.main_status_message_errorprefix)
                        + res.getString(R.string.main_status_message_imonitor_port_null));
                return true;
            } else {
                if (!TextUtils.isDigitsOnly(mPreferences.getIMonitorServerPortPreference())) {
                    mStatusMessageField.append("\n"
                            + res.getString(
                            R.string.main_status_message_errorprefix)
                            + res.getString(R.string.main_status_message_imonitor_port_not_number));
                    return true;
                }
            }
        } else if (mPreferences.getMonitoringPreference().equalsIgnoreCase(res.getTextArray(R.array.preferences_value_serverForm)[1].toString())) {

            if (mPreferences.isUseBasicAuth()) {
                // validate username
                if (mPreferences.getUsernamePreference() == null
                        || mPreferences.getUsernamePreference().length() == 0) {
                    mStatusMessageField.append("\n"
                            + res.getString(
                            R.string.main_status_message_errorprefix)
                            + res.getString(R.string.main_status_message_basic_auth_username_null));
                    return true;
                }
                // validate password
                if (mPreferences.getPasswordPreference() == null) {
                    mStatusMessageField.append("\n"
                            + res.getString(
                            R.string.main_status_message_errorprefix)
                            + res.getString(R.string.main_status_message_basic_auth_password_null));
                    return true;
                }
            }

            // validate ip-setting from preferences
            if (mPreferences.getIFMAPServerIpPreference() == null
                    || mPreferences.getIFMAPServerIpPreference().length() == 0) {
                mStatusMessageField.append("\n"
                        + res.getString(
                        R.string.main_status_message_errorprefix)
                        + res.getString(R.string.main_status_message_ifmap_ip_null));
                return true;
            } else {
                Matcher ipMatcher = Toolbox.getIpPattern().matcher(
                        mPreferences.getIFMAPServerIpPreference());
                if (!ipMatcher.find()) {
                    mStatusMessageField.append("\n"
                            + res.getString(
                            R.string.main_status_message_errorprefix)
                            + res.getString(R.string.main_status_message_ifmap_ip_not_valid));
                    return true;
                }
            }
            // validate portnumber
            if (mPreferences.getIFMAPServerPortPreference() == null ||
                    mPreferences.getIFMAPServerPortPreference().length() == 0) {
                mStatusMessageField.append("\n"
                        + res.getString(
                        R.string.main_status_message_errorprefix)
                        + res.getString(R.string.main_status_message_ifmap_port_null));
                return true;
            } else {
                if (!TextUtils.isDigitsOnly(mPreferences.getIFMAPServerPortPreference())) {
                    mStatusMessageField.append("\n"
                            + res.getString(
                            R.string.main_status_message_errorprefix)
                            + res.getString(R.string.main_status_message_ifmap_port_not_a_numer));
                    return true;
                }
            }
        } else {
            mStatusMessageField.append("\n"
                    + res.getString(
                    R.string.main_status_message_errorprefix)
                    + res.getString(R.string.main_status_message_monitoring_unknwon));
            return true;
        }

        return false;
    }
}

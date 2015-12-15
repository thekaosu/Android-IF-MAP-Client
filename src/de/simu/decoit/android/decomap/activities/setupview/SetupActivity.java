/* 
 * SetupActivity.java        0.2 2015-03-08
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

package de.simu.decoit.android.decomap.activities.setupview;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

import de.simu.decoit.android.decomap.activities.R;
import de.simu.decoit.android.decomap.preferences.PreferencesValues;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * Activity for setting Preferences
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @version 0.2
 */
public class SetupActivity extends PreferenceActivity {

    private List<Header> headers;
    private String setupMode;

    private List<Header> mainHeader = new ArrayList<Header>();
    private List<Header> iMonitorHeaderList = new ArrayList<Header>();
    private List<Header> ifMapHeaderList = new ArrayList<Header>();

    private PreferencesValues mPreferences = PreferencesValues.getInstance();

    // -------------------------------------------------------------------------
    // ACTIVITY LIFECYCLE HANDLING
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbox.logTxt(this.getClass().getName(), "SetupActivity.OnCreate(...) called");
        super.onCreate(savedInstanceState);
//        getFragmentManager().beginTransaction().replace(android.R.id.content, new SetupFragment()).commit();
//        addPreferencesFromResource(R.xml.preferences);
        getListView().setPadding(0, 0, 0, 0);
    }

    @Override
    protected void onResume() {
        Toolbox.logTxt(this.getClass().getName(), "SetupActivity.OnResume(...) called");
        super.onResume();

        if (getListAdapter() instanceof SetupAdapter) {
            ((SetupAdapter) getListAdapter()).resume();
        }
    }

    public void onBuildHeaders(List<Header> target) {

        loadHeadersFromResource(R.xml.preferences_header, mainHeader);

        loadHeadersFromResource(R.xml.preferences_header_imonitor, iMonitorHeaderList);
        loadHeadersFromResource(R.xml.preferences_header_ifmap, ifMapHeaderList);

        target.addAll(mainHeader);

        headers = target;
    }

    public void refreshHeaders() {
        String mode = PreferenceManager.getDefaultSharedPreferences(this).getString(SetupAdapter.MONITORINGMODE_VIEW_ID + "", "IF-MAP");
        if (mode != null && !mode.equals(setupMode)) {
            setupMode = mode;
            headers.clear();
            headers.addAll(mainHeader);
            if (mode.equals("IF-MAP")) {
                headers.addAll(ifMapHeaderList);
            } else if (mode.equals("iMonitor")) {
                headers.addAll(iMonitorHeaderList);
            }
            super.setListAdapter(new SetupAdapter(this, headers));
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getListAdapter() instanceof SetupAdapter) {
            ((SetupAdapter) getListAdapter()).pause();
        }
        initPreferences();
    }

    /**
     * get application preferences
     */
    private void initPreferences() {
        Toolbox.logTxt(this.getLocalClassName(), "onPreferences(...) called");

        // object for holding preferences-values
        mPreferences = PreferencesValues.getInstance();

        // get the preferences.xml preferences
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        // set preferences
        mPreferences.setApplicationFileLogging(prefs.getBoolean(
                "applicatiologging", false));
        mPreferences.setMonitoringPreference(prefs.getString(
                R.id.monitoringModeSettings + "", "IF-MAP"));
        mPreferences.setLocationTrackingType(prefs.getString(
                "locationPref", "GPS"));
        mPreferences.setEnableLocationTracking(prefs.getBoolean(
                "enableLocationTracking", false));

        mPreferences.setLogPath(prefs.getString("logPath", Environment.getExternalStorageDirectory() + "/ifmap-client/logs/"));

        mPreferences.setAutoUpdate(prefs.getBoolean("autoUpdate", false));

        mPreferences.setKeystorePath(prefs.getString("KeystorePath", Environment.getExternalStorageDirectory() + "/ifmap-client/keystore/keystore"));
        mPreferences.setKeystorePassword(prefs.getString("keystorepw", ""));

        mPreferences.setUseNonConformMetadata(prefs.getBoolean(
                R.id.esukomMetadataSettings + "", true));
        mPreferences.setDontSendApplicationsInfos(prefs.getBoolean(
                "sendNoAppsPreferences", false));
        mPreferences.setAutostart(prefs.getBoolean("autostartPreferences",
                false));
        mPreferences.setAutoconnect(prefs.getBoolean("autoconnectPreferences",
                false));
        mPreferences.setDontSendGoogleApps(prefs.getBoolean(
                "sendNoGoogleAppsPreferences", true));
        mPreferences.setAllowUnsafeSSLPreference(prefs.getBoolean(
                "allowUnsafeSSLPreference", true));
        mPreferences.setEnableNewAndEndSessionLog(prefs.getBoolean(
                "logNewsessionRequest", false));
        mPreferences
                .setEnablePollLog(prefs.getBoolean("logPollRequest", false));
        mPreferences.setEnableSubscribe(prefs.getBoolean("logSubscripeRequest",
                false));
        mPreferences.setEnableLocationTrackingLog(prefs.getBoolean(
                "logLocationTracking", false));
        mPreferences.setEnablePublishCharacteristicsLog(prefs.getBoolean(
                "logPublishCharacteristics", false));
        mPreferences.setEnableErrorMessageLog(prefs.getBoolean(
                "logErrorMessage", false));
        mPreferences.setEnableInvalideResponseLog(prefs.getBoolean(
                "logInvalideResponse", false));
        mPreferences.setEnableRenewRequestLog(prefs.getBoolean(
                "logRenewRequest", false));
        mPreferences.setUsernamePreference(prefs.getString(
                "usernamePreference", "user"));
        mPreferences.setPasswordPreference(prefs.getString(
                "passwordPreference", "password"));
        mPreferences.setIFMAPServerIpPreference(prefs.getString(
                "IF-MAPServeripPreference", ""));
        mPreferences.setIFMAPServerPortPreference(prefs.getString(
                "IF-MAPServerportPreference", "8443"));
        mPreferences.setIMonitorServerIpPreference(prefs.getString(
                "iMonitorServeripPreference", ""));
        mPreferences.setIMonitorServerPortPreference(prefs.getString(
                "iMonitorServerportPreference", "5667"));
        mPreferences.setNscaEncPreference(prefs.getString(
                "nscaEncPref", "1"));
        mPreferences.setNscaPassPreference(prefs.getString(
                "imonitorPassPreference", "icinga"));
        mPreferences.setIsPermantConnection(prefs.getBoolean(
                "permanantlyConectionPreferences", true));

        //mPreferences.setIsUseBasicAuth(prefs.getString("auth", "Basic-Authentication"));
        mPreferences.setUseBasicAuth(prefs.getString("authType", "Basic-Auth").equals("Basic-Auth"));

        // set update interval
        try {
            mPreferences.setUpdateInterval(Long.parseLong(prefs.getString(
                    "updateInterval", "600000")));
        } catch (NumberFormatException e) {
            // should not happen! just in case of...
            Toolbox.logTxt(
                    this.getLocalClassName(),
                    "initializing of update interval from preferences failed...using default (60000)");
            mPreferences.setUpdateInterval(60000L);
        }
        // check if update interval is above minimum, of not set it to
        // default minimum value
        if (mPreferences.getUpdateInterval() < 60000L) {
            mPreferences.setUpdateInterval(60000L);
            Toolbox.logTxt(this.getLocalClassName(),
                    "configured update interval is to short...using default (60000)");
        }

        // set renew session interval
        try {
            mPreferences.setRenewIntervalPreference(Long.parseLong(prefs
                    .getString("renewInterval", "10000l")));
        } catch (NumberFormatException e) {
            // should not happen! just in case of...
            Toolbox.logTxt(
                    this.getLocalClassName(),
                    "initializing of renew session interval from preferences failed...using default (10000)");
            mPreferences.setRenewIntervalPreference(10000L);
        }

        // check if renew-session interval is above minimum, of not set it to
        // default minimum value
        if (mPreferences.getRenewIntervalPreference() < 10000L) {
            mPreferences.setRenewIntervalPreference(10000L);
            Toolbox.logTxt(this.getLocalClassName(),
                    "configured renew session interval is to short...using default (10000)");
        }
    }

    // -------------------------------------------------------------------------
    // BUTTON HANDLING
    // -------------------------------------------------------------------------

//    /**
//     * we override the behavior of the back-button so that the application runs
//     * in the background (instead of destroying it) when pressing back (similar
//     * to the home button)
//     */
//    @Override
//    public void onBackPressed() {
//        Intent setIntent = new Intent(Intent.ACTION_MAIN);
//        setIntent.addCategory(Intent.CATEGORY_HOME);
//        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(setIntent);
//    }

    // -------------------------------------------------------------------------
    // ADAPTER HANDLING
    // -------------------------------------------------------------------------

    /**
     * setting own list adapter to use custom headers
     */
    public void setListAdapter(ListAdapter adapter) {
        int i, count;

        if (headers == null && adapter != null) {
            headers = new ArrayList<Header>();

            count = adapter.getCount();
            for (i = 0; i < count; ++i) {
                headers.add((Header) adapter.getItem(i));
            }
        }

        super.setListAdapter(new SetupAdapter(this, headers));
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

/*
 * MainActivity.java        0.2 2015-03-08
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
 * d
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.simu.decoit.android.decomap.activities;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import de.simu.decoit.android.decomap.database.LoggingDatabase;
import de.simu.decoit.android.decomap.device.DeviceProperties;
import de.simu.decoit.android.decomap.messaging.MessageParameter;
import de.simu.decoit.android.decomap.monitoring.MonitoringInterface;
import de.simu.decoit.android.decomap.monitoring.ifmap.IFMapMonitoring;
import de.simu.decoit.android.decomap.monitoring.imonitor.IMonitorMonitoring;
import de.simu.decoit.android.decomap.observer.battery.BatteryReceiver;
import de.simu.decoit.android.decomap.observer.camera.CameraReceiver;
import de.simu.decoit.android.decomap.observer.location.LocationObserver;
import de.simu.decoit.android.decomap.observer.sms.SMSObserver;
import de.simu.decoit.android.decomap.preferences.PreferenceInitializer;
import de.simu.decoit.android.decomap.preferences.PreferenceValidator;
import de.simu.decoit.android.decomap.preferences.PreferencesValues;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * Main Activity which handles the View of the first Tab
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Marcel Jahnke, Decoit GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class MainActivity extends Activity {

    private MonitoringInterface monitoringMode;

    // publisher id assigned from map-server
    public static String sCurrentPublisherId;

    // preferences
    private PreferencesValues mPreferences;

    // device properties
    private DeviceProperties mDeviceProperties;


    // buttons
    private Button mConnectButton;
    private Button mDisconnectButton;
    private Button mPublishDeviceCharacteristicsButton;

    // status message field
    private EditText mStatusMessageField;

    // database-manager
    private LoggingDatabase mLogDB = null;

    // location tracking objects
    private LocationManager mLocManager;
    private LocationObserver mLocListener;

    private final MessageParameter mp = MessageParameter.getInstance();

    private BatteryReceiver mBatteryReciever = null; //maybe useful later!

    // receiver for pictures taken with the camera
    private CameraReceiver mCameraReceiver = null; //maybe useful later!

    // -------------------------------------------------------------------------
    // ACTIVITY LIFECYCLE HANDLING
    // -------------------------------------------------------------------------

    /**
     * Called when the activity is first created
     *
     * @param savedInstanceState state-bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Toolbox.logTxt(this.getLocalClassName(), "onCreate(...) called");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        // initialize application
        initViews();
        initValues();

        // autoconnect at Startup
        if (mPreferences.isAutoconnect()) {
            // start connection service if all required preferences are set
            if (PreferenceValidator.incorrectPreferencesConfiguration(mPreferences, getResources(), mStatusMessageField)) {
                mStatusMessageField.append("\n"
                        + getResources().getString(
                        R.string.main_status_message_errorprefix)
                        + " "
                        + getResources().getString(
                        R.string.main_default_wrongconfig_message));
            } else if (mPreferences.getMonitoringPreference()
                    .equalsIgnoreCase(getResources().getTextArray(R.array.preferences_value_serverForm)[1].toString())) {
                monitoringMode = new IFMapMonitoring(this, mPreferences, mStatusMessageField);
                monitoringMode.autoConnection();
            } else if (mPreferences.getMonitoringPreference().equalsIgnoreCase(getResources().getTextArray(R.array.preferences_value_serverForm)[0].toString())) {
                monitoringMode = new IMonitorMonitoring(this, mPreferences, mStatusMessageField);
                monitoringMode.autoConnection();
            } else {
                mStatusMessageField.append("\n"
                        + getResources().getString(
                        R.string.main_status_message_errorprefix)
                        + getResources().getString(R.string.main_status_message_invalid_monitoring_mode));
            }
        }

        // enable/disable buttons depending on connection state
        if (monitoringMode != null) {
            changeButtonStates(monitoringMode.isConnected());
        } else {
            changeButtonStates(false);
        }

        // show initial notification
        Toolbox.showNotification(
                getResources().getString(R.string.notification_initial_label),
                getResources().getString(R.string.notification_initial_messgae),
                getApplicationContext());
    }

    /**
     * Called when the activity is started
     */
    @Override
    public void onStart() {
        Toolbox.logTxt(this.getLocalClassName(), "onStart() called");
        super.onStart();
    }

    /**
     * Called when the activity is brought back to foreground
     */
    @Override
    public void onResume() {
        Toolbox.logTxt(this.getLocalClassName(), "onResume() called");
        super.onResume();

        // re-initialize location tracking
        if (mPreferences.isEnableLocationTracking()) {
            initLocation();
        }
    }

    /**
     * Called when the activity is on pause
     */
    @Override
    protected void onPause() {
        Toolbox.logTxt(this.getLocalClassName(), "onPause() called");
        super.onPause();

        // get the intent of the upper activity (tablayout) and fill it
        if (mp.getLatitude() != null) {
            super.getParent().getIntent().putExtra("latitude", mp.getLatitude());
            super.getParent().getIntent().putExtra("longitude", mp.getLongitude());
        }
    }

    /**
     * Called when the activity is shut down
     */
    @Override
    protected void onDestroy() {
        Toolbox.logTxt(this.getLocalClassName(), "onDestroy() called");
        super.onDestroy();

        // "unlock locked" preferences
        mPreferences.setLockPreferences(false);
//        PreferencesValues.sLockConnectionPreferences = false;
//        PreferencesValues.sLockLocationTrackingOptions = false;

        // remove updates from location manager
        if (mLocManager != null) {
            try {
                mLocManager.removeUpdates(mLocListener);
            } catch (NullPointerException e) {
                Toolbox.logTxt(this.getClass().getName(),
                        "error on destroy: " + e);
                mStatusMessageField.append("\n"
                        + getResources().getString(
                        R.string.main_status_message_errorprefix) + " " + e);
            }
        }

        if (monitoringMode != null) {
            monitoringMode.onDestroy();
        }
        // delete last notification message when application is shut down
        Toolbox.cancelNotification();
    }

    // -------------------------------------------------------------------------
    // ACTIVITY INITIALISATION HANDLING
    // -------------------------------------------------------------------------

    /**
     * find all view-elements required by activity
     */
    private void initViews() {
        Toolbox.logTxt(this.getLocalClassName(), "initViews...) called");

        mStatusMessageField = (EditText) findViewById(R.id.RequestStatus_EditText);
        mConnectButton = (Button) findViewById(R.id.Connect_Button);
        mDisconnectButton = (Button) findViewById(R.id.Disconnect_Button);
        mPublishDeviceCharacteristicsButton = (Button) findViewById(R.id.PublishDeviceCharacteristics_Button);
    }

    /**
     * initialize required application values
     */
    private void initValues() {
        Toolbox.logTxt(this.getLocalClassName(), "initValues(...) called");

        mPreferences = PreferencesValues.getInstance();
        PreferenceInitializer.initPreferences(this);

        mDeviceProperties = new DeviceProperties(this);

        // create new database connection
        mLogDB = new LoggingDatabase(this);

        // create receivers
        mBatteryReciever = new BatteryReceiver(this.getApplicationContext());

        // default status message on startup
        mStatusMessageField.append(getResources().getString(
                R.string.main_status_message_prefix)
                + " "
                + getResources()
                .getString(R.string.main_default_status_message));

        // initialize sms-observing
        SMSObserver mSmsObserver = new SMSObserver(getApplicationContext());
        mSmsObserver.registerReceivedSmsBroadcastReceiver();
        mSmsObserver.registerSentSmsContentObserver();

        // initialize camera-receiver
        mCameraReceiver = new CameraReceiver();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // register camera useage
            CameraManager cam_manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            CameraManager.AvailabilityCallback camAvailCallback = new CameraManager.AvailabilityCallback() {

                public void onCameraAvailable(@NonNull String cameraId) {
                    mPreferences.setCamActiv(cameraId, false);
                }

                public void onCameraUnavailable(@NonNull String cameraId) {
                    mPreferences.setCamActiv(cameraId, true);
                }
            };

            cam_manager.registerAvailabilityCallback(camAvailCallback, null);
        }
    }

    /**
     * initialize the LocationManager class to obtain GPS locations
     */
    private void initLocation() {
        Toolbox.logTxt(this.getLocalClassName(), "initLocation(...) called");

        // delete previous location-tracking-data
        mp.resetCurrentLocation();

        // initialize location manager
        mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocListener = new LocationObserver();
        mLocListener.setAppllicationContext(this);

        // gps
        if (mPreferences.getLocationTrackingType().equalsIgnoreCase("gps")) {
            mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    mPreferences.getUpdateInterval(), 0, mLocListener);
        }

        // cell based
        else {
            mLocManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    mPreferences.getUpdateInterval(), 0, mLocListener);
        }
    }

    // -------------------------------------------------------------------------
    // BUTTON HANDLING
    // -------------------------------------------------------------------------

    /**
     * enable/disable buttons depending on current application state (detected
     * by mIsConnected-Flag)
     *
     * @param isConnected boolean indicating current connection state
     */
    public void changeButtonStates(boolean isConnected) {
        // connected to if map server
        if (isConnected) {
            mConnectButton.setEnabled(false);
            mDisconnectButton.setEnabled(true);
            mPublishDeviceCharacteristicsButton.setEnabled(!(mPreferences.isAutoUpdate() || monitoringMode instanceof IMonitorMonitoring));
        }
        // not connected
        else {
            mConnectButton.setEnabled(true);
            mDisconnectButton.setEnabled(false);
            mPublishDeviceCharacteristicsButton.setEnabled(false);
        }
    }

    /**
     * disable all connection buttons!
     */
    public void disableButtons() {
        mConnectButton.setEnabled(false);
        mDisconnectButton.setEnabled(false);
        mPublishDeviceCharacteristicsButton.setEnabled(false);
    }

    /**
     * Handler for Main-Tab Buttons
     *
     * @param view element that originated the call
     */
    public void mainTabButtonHandler(View view) {
        if (mPreferences.getMonitoringPreference().equalsIgnoreCase(getResources().getTextArray(R.array.preferences_value_serverForm)[1].toString())) {
            if (monitoringMode == null || !(monitoringMode instanceof IFMapMonitoring)) {
                if (monitoringMode != null) {
                    monitoringMode.onDestroy();
                }
                monitoringMode = new IFMapMonitoring(this, mPreferences, mStatusMessageField);
            }
            monitoringMode.mainTabButtonHandler(view);
        } else if (mPreferences.getMonitoringPreference().equalsIgnoreCase(getResources().getTextArray(R.array.preferences_value_serverForm)[0].toString())) {
            if (monitoringMode == null || !(monitoringMode instanceof IMonitorMonitoring)) {
                if (monitoringMode != null) {
                    monitoringMode.onDestroy();
                }
                monitoringMode = new IMonitorMonitoring(this, mPreferences, mStatusMessageField);
            }
            monitoringMode.mainTabButtonHandler(view);
        } else {
            mStatusMessageField.append("\n"
                    + getResources().getString(
                    R.string.main_status_message_errorprefix)
                    + getResources().getString(R.string.main_status_message_invalid_monitoring_mode));
        }
    }

    /**
     * Returning device properties
     *
     * @return own device properties
     */
    public DeviceProperties getDeviceProperties() {
        return mDeviceProperties;
    }

    /**
     * return logging database
     *
     * @return logging database
     */
    public LoggingDatabase getLogDB() {
        return mLogDB;
    }
}

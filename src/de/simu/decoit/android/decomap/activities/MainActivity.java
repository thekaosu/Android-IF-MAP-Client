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
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources.NotFoundException;
import android.hardware.camera2.CameraManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.googlecode.jsendnsca.core.Encryption;

import java.util.regex.Matcher;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import de.hshannover.f4.trust.ifmapj.IfmapJHelper;
import de.hshannover.f4.trust.ifmapj.channel.SSRC;
import de.hshannover.f4.trust.ifmapj.channel.SsrcImpl;
import de.hshannover.f4.trust.ifmapj.exception.InitializationException;
import de.hshannover.f4.trust.ifmapj.messages.PublishRequest;
import de.simu.decoit.android.decomap.connection.ConnectionObjects;
import de.simu.decoit.android.decomap.database.LoggingDatabase;
import de.simu.decoit.android.decomap.device.DeviceProperties;
import de.simu.decoit.android.decomap.logging.LogMessage;
import de.simu.decoit.android.decomap.logging.LogMessageHelper;
import de.simu.decoit.android.decomap.messaging.EventParameters;
import de.simu.decoit.android.decomap.messaging.MessageHandler;
import de.simu.decoit.android.decomap.messaging.MessageParametersGenerator;
import de.simu.decoit.android.decomap.messaging.ResponseParameters;
import de.simu.decoit.android.decomap.observer.battery.BatteryReceiver;
import de.simu.decoit.android.decomap.observer.camera.CameraReceiver;
import de.simu.decoit.android.decomap.observer.location.LocationObserver;
import de.simu.decoit.android.decomap.observer.sms.SMSObserver;
import de.simu.decoit.android.decomap.preferences.PreferenceInitializer;
import de.simu.decoit.android.decomap.preferences.PreferencesValues;
import de.simu.decoit.android.decomap.services.NscaService;
import de.simu.decoit.android.decomap.services.NscaService.LocalBinder;
import de.simu.decoit.android.decomap.services.PermanentConnectionService;
import de.simu.decoit.android.decomap.services.RenewConnectionService;
import de.simu.decoit.android.decomap.services.binder.BinderClass;
import de.simu.decoit.android.decomap.services.binder.UnbinderClass;
import de.simu.decoit.android.decomap.services.local.LocalServiceParameters;
import de.simu.decoit.android.decomap.services.local.LocalServicePermanent;
import de.simu.decoit.android.decomap.services.local.LocalServiceSynchronous;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * Main Activity wich handles the communication with the MAP-Server
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Marcel Jahnke, Decoit GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class MainActivity extends Activity {

    // local services definitions
    public static RenewConnectionService.LocalBinder sBoundRenewConnService;
    public static PermanentConnectionService.LocalBinder sBoundPermConnService;

    // publisher id assigned from map-server
    public static String sCurrentPublisherId;

    // fields for location-tracking-values, declared static so that
// the status-activity can access them
    public static String sLatitude = null;
    public static String sLongitude = null;
    public static String sAltitude = null;

    // ssrc-connection-object
    private static SSRC sSsrcConnection;

    // preferences
    private PreferencesValues mPreferences;

    // device properties
    private DeviceProperties mDeviceProperties;

    // manage nsca connection for iMonitor
    private NscaService mNscaServiceBind;

    // buttons
    private Button mConnectButton;
    private Button mDisconnectButton;
    private Button mPublishDeviceCharacteristicsButton;

    // status message field
    private EditText mStatusMessageField;

    // progress dialog and notifications
    private ProgressDialog myProgressDialog = null;

    // current if-map session and publisher id
    private String mCurrentSessionId;

    // application/connection states
    private boolean mIsConnected = false;

    // local services
    private ServiceConnection mConnection;
    private ServiceConnection mPermConnection;

    private final int timeout = 12000;

    // local services states
    private boolean mIsBound;

    // current messaging type (as defined in Message-Handler-Class)
    private byte mMessageType;

    // callback-handler for local-services
    private final Handler mMsgHandler = new Handler();

    // database-manager
    private LoggingDatabase mLogDB = null;

    // parameters for local service classes
    private LocalServiceParameters mLocalServicePreferences;

    // type of server-response
    private byte mResponseType;

    // location tracking objects
    private LocationManager mLocManager;
    private LocationObserver mLocListener;

    // message-parameter-generator
    MessageParametersGenerator<PublishRequest> parameters;

    private BatteryReceiver mBatteryReciever = null;

    // observer for incoming and outgoing sms-messages
    private SMSObserver mSmsObserver = null;

    // receiver for pictures taken with the camera
    private CameraReceiver mCameraReceiver = null;

    private CameraManager cam_manager;

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

        setContentView(R.layout.tab1);

        // initialize application
        initViews();
        initValues();

        // generator for if-map-messages to be published
        parameters = new MessageParametersGenerator<PublishRequest>(this);

        // initialize sms-observing
        mSmsObserver = new SMSObserver(getApplicationContext());
        mSmsObserver.registerReceivedSmsBroadcastReceiver();
        mSmsObserver.registerSentSmsContentObserver();

        // initialize camera-receiver
        mCameraReceiver = new CameraReceiver();

        // register camera useage
        cam_manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CameraManager.AvailabilityCallback camAvailCallback = new CameraManager.AvailabilityCallback() {

                public void onCameraAvailable(String cameraId) {
                    mPreferences.setCamActiv(cameraId, false);
                    Toolbox.logTxt(MainActivity.this.getLocalClassName(), "Camera is not in use!");
                }

                public void onCameraUnavailable(String cameraId) {
                    mPreferences.setCamActiv(cameraId, true);
                    Toolbox.logTxt(MainActivity.this.getLocalClassName(), "Camera is in use!");

                }
            };

            cam_manager.registerAvailabilityCallback(camAvailCallback, null);
        }

        // autoconnect at Startup
        if (mPreferences.isAutoconnect()) {
            // start connection service if all required preferences are set
            if (!validatePreferences()) {
                mStatusMessageField.append("\n"
                        + getResources().getString(
                        R.string.main_status_message_errorprefix)
                        + " "
                        + getResources().getString(
                        R.string.main_default_wrongconfig_message));
            } else if (mPreferences.getMonitoringPreference()
                    .equalsIgnoreCase("IF-MAP")) {
                // set status message to-text-output-field
                mStatusMessageField.append("\n"
                        + getResources().getString(
                        R.string.main_status_message_prefix) + " "
                        + "Sending Request to "
                        + mPreferences.getIFMAPServerIpPreference() + ":"
                        + mPreferences.getIFMAPServerPortPreference());

                // start new session
                mMessageType = MessageHandler.MSG_TYPE_REQUEST_NEWSESSION;
                if (initIFMAPConnection()) {
                    startIFMAPConnectionService();
                }
            } else if (mPreferences.getMonitoringPreference().equalsIgnoreCase("iMonitor")) {
                connectNSCA();
            } else {
                mStatusMessageField.append("\n"
                        + getResources().getString(
                        R.string.main_status_message_errorprefix)
                        + " monitoring mode is invalid!");
            }
        }

        // enable/disable buttons depending on connection state
        changeButtonStates(mIsConnected);

        // show initial notification
        Toolbox.showNotification(
                getResources().getString(R.string.notification_initial_label),
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
        if (sLatitude != null) {
            super.getParent().getIntent().putExtra("latitude", sLatitude);
            super.getParent().getIntent().putExtra("longitude", sLongitude);
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
                e.printStackTrace();
            }
        }

        // reset connection-objects
        ConnectionObjects.setSsrcConnection(null);

        // delete last notification message when application is shut down
        Toolbox.cancelNotification();

        // check and unbind all local services and handlers
        if (sBoundRenewConnService != null) {
            mIsBound = UnbinderClass.doUnbindConnectionService(
                    getApplicationContext(), mConnection, myProgressDialog,
                    mIsBound);
            sBoundRenewConnService = null;
        }
        if (sBoundPermConnService != null) {
            mIsBound = UnbinderClass.doUnbindConnectionService(
                    getApplicationContext(), mPermConnection, myProgressDialog,
                    mIsBound);
            sBoundPermConnService = null;
        }
        if (mRenewHandler != null) {
            mRenewHandler.removeCallbacksAndMessages(null);
            mRenewHandler = null;
        }
        if (mUpdateHandler != null) {
            mUpdateHandler.removeCallbacksAndMessages(null);
            mUpdateHandler = null;
        }
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
        PreferenceInitializer.initPreferences(getBaseContext());

        // create new database connection
        mLogDB = new LoggingDatabase(this);

        // get device-properties-object
        mDeviceProperties = new DeviceProperties(this);

        // create receivers
        mBatteryReciever = new BatteryReceiver(this.getApplicationContext());

        // default status message on startup
        mStatusMessageField.append(getResources().getString(
                R.string.main_status_message_prefix)
                + " "
                + getResources()
                .getString(R.string.main_default_status_message));
    }

    /**
     * Initialize the connection object if not already initialized, else assign
     * already existing connection object
     */
    private boolean initIFMAPConnection() {
        Toolbox.logTxt(this.getLocalClassName(), "initIFMAPConnection(...) called");
        if (ConnectionObjects.getSsrcConnection() == null
                || (mResponseType == MessageHandler.MSG_TYPE_ERRORMSG)) {
            try {

                // if unsafe ssl is activated, set related properties for ifmapj
                if (mPreferences.isAllowUnsafeSSLPreference()) {
                    Toolbox.logTxt(this.getLocalClassName(),
                            "using unsafe ssl - verifypeercert and host set to false");
                    System.setProperty("ifmapj.communication.verifypeercert",
                            "false");
                    System.setProperty("ifmapj.communication.verifypeerhost",
                            "false");
                } else {
                    Toolbox.logTxt(this.getLocalClassName(), "using safe ssl");
                    System.setProperty("ifmapj.communication.verifypeercert",
                            "true");
                    System.setProperty("ifmapj.communication.verifypeerhost",
                            "");
                }

                TrustManager[] trustManagers = IfmapJHelper.getTrustManagers(getResources().openRawResource(R.raw.keystore),
                        "androidmap");
                if (mPreferences.isUseBasicAuth()) {
                    // create ssrc-connection using basic-authentication
                    Toolbox.logTxt(this.getLocalClassName(),
                            "initializing ssrc-connecion using basic-auth");
                    sSsrcConnection = new SsrcImpl(
                            "https://"
                                    + mPreferences.getIFMAPServerIpPreference() + ":"
                                    + mPreferences.getIFMAPServerPortPreference(), mPreferences.getUsernamePreference(), mPreferences
                            .getPasswordPreference(), trustManagers, timeout);
                } else {
                    // create ssrc-connection using certificates
                    Toolbox.logTxt(this.getLocalClassName(),
                            "initializing ssrc-connecion using certificate-based-auth");
                    KeyManager[] keyManagers = IfmapJHelper.getKeyManagers(mPreferences.getKeystorePath(),
                            mPreferences.getKeystorePassword());
                    sSsrcConnection = new SsrcImpl("https://"
                            + mPreferences.getIFMAPServerIpPreference() + ":"
                            + mPreferences.getIFMAPServerPortPreference(), keyManagers, trustManagers, timeout);
                }

                mResponseType = 0;
            } catch (InitializationException e) {
                mStatusMessageField.append("\n"
                        + getResources().getString(
                        R.string.main_status_message_errorprefix) + " " + e.getMessage());
                return false;
            } catch (NotFoundException e) {
                mStatusMessageField.append("\n"
                        + getResources().getString(
                        R.string.main_status_message_errorprefix) + " " + e.getMessage());
                return false;
            }
            ConnectionObjects.setSsrcConnection(sSsrcConnection);
        } else {
            sSsrcConnection = ConnectionObjects.getSsrcConnection();
        }
        return true;
    }

    /**
     * initialize the LocationManager class to obtain GPS locations
     */
    private void initLocation() {
        Toolbox.logTxt(this.getLocalClassName(), "initLocation(...) called");

        // delete previous location-tracking-data
        sLongitude = null;
        sLatitude = null;
        sAltitude = null;

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
    private void changeButtonStates(boolean isConnected) {
        // connected to if map server
        if (isConnected) {
            mConnectButton.setEnabled(false);
            mDisconnectButton.setEnabled(true);
            if (mPreferences.isAutoUpdate()) {
                mPublishDeviceCharacteristicsButton.setEnabled(false);
            } else {
                mPublishDeviceCharacteristicsButton.setEnabled(true);
            }
        }
        // not connected
        else {
            mConnectButton.setEnabled(true);
            mDisconnectButton.setEnabled(false);
            mPublishDeviceCharacteristicsButton.setEnabled(false);
        }
    }

    /**
     * Handler for Main-Tab Buttons
     *
     * @param view element that originated the call
     */
    public void mainTabButtonHandler(View view) {
        if (mPreferences.getMonitoringPreference().equalsIgnoreCase("IF-MAP")) {
            mainTabButtonHandlerIfmap(view);
        } else if (mPreferences.getMonitoringPreference().equalsIgnoreCase("iMonitor")) {
            mainTabButtonHandlerIMonitor(view);
        } else {
            mStatusMessageField.append("\n"
                    + getResources().getString(
                    R.string.main_status_message_errorprefix)
                    + " monitoring mode is invalid!");
        }
    }

    private void mainTabButtonHandlerIMonitor(View view) {
        if (mIsConnected) {
            switch (view.getId()) {
                case R.id.Disconnect_Button:
                    disconnectNSCA();

                    break;
            }
        }

        // not connected to imonitor
        else {
            switch (view.getId()) {
                case R.id.Connect_Button:
                    connectNSCA();

                    break;
            }
        }
    }

    private void connectNSCA() {
        // start connection service if all required preferences are set
        if (!validatePreferences()) {
            mStatusMessageField.append("\n"
                    + getResources().getString(
                    R.string.main_status_message_errorprefix)
                    + " "
                    + getResources().getString(
                    R.string.main_default_wrongconfig_message));
        } else {

            // set status message to-text-output-field
            mStatusMessageField.append("\n"
                    + getResources().getString(
                    R.string.main_status_message_prefix) + " "
                    + "Sending data to "
                    + mPreferences.getIMonitorServerIpPreference() + ":"
                    + mPreferences.getIMonitorServerPortPreference());

            mIsBound = BinderClass.doBindNscaService(getApplicationContext(),
                    mNscaConnection);

            mPreferences.setLockPreferences(true);
//            PreferencesValues.sLockConnectionPreferences = true;

//                    true, new DialogInterface.OnCancelListener() {
//                        @Override
//                        public void onCancel(DialogInterface dialog) {
//                            if (PreferencesValues.sMonitoringPreference.equalsIgnoreCase("iMonitor")) {
//                                disconnectNSCA();
//                            }
//                        }
//                    });

            mIsConnected = true;
            mConnectButton.setEnabled(false);
            mDisconnectButton.setEnabled(true);

            myProgressDialog = ProgressDialog.show(
                    MainActivity.this,
                    getResources().getString(
                            R.string.main_progressbar_message_srcrequest_1),
                    getResources().getString(
                            R.string.main_progressbar_message_srcrequest_2), true, false);

        }
    }

    private void disconnectNSCA() {
        mNscaServiceBind.stopMonitor();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMonitorEventReceiver);
        if (mIsBound) {
            mIsBound = UnbinderClass.doUnbindConnectionService(
                    getApplicationContext(), mNscaConnection, myProgressDialog,
                    mIsBound);
        }
        mConnectButton.setEnabled(true);
        mDisconnectButton.setEnabled(false);
        mIsConnected = false;
        mStatusMessageField.append("\n"
                + getResources().getString(
                R.string.main_status_message_prefix) + " "
                + "disconnected.");
        Toolbox.showNotification(
                getResources().getString(R.string.notification_nsca_label),
                getResources().getString(R.string.notification_nsca_label),
                getResources().getString(R.string.notification_disconnect),
                getApplicationContext());

        mPreferences.setLockPreferences(false);
//        PreferencesValues.sLockConnectionPreferences = false;
    }

    private void mainTabButtonHandlerIfmap(View view) {
        if (mIsConnected) {
            switch (view.getId()) {
                // close session button
                case R.id.Disconnect_Button:
                    mMessageType = MessageHandler.MSG_TYPE_REQUEST_ENDSESSION;
                    break;
                // publish device characteristics-button
                case R.id.PublishDeviceCharacteristics_Button:

                    // disable buttons before starting request-generation
                    mConnectButton.setEnabled(false);
                    mDisconnectButton.setEnabled(false);
                    mPublishDeviceCharacteristicsButton.setEnabled(false);

                    mMessageType = MessageHandler.MSG_TYPE_PUBLISH_CHARACTERISTICS;
                    break;
            }
        }

        // not connected to map server
        else {
            switch (view.getId()) {
                // start new session button
                case R.id.Connect_Button:
                    mMessageType = MessageHandler.MSG_TYPE_REQUEST_NEWSESSION;
                    break;
            }
        }

        // start connection service if all required preferences are set
        if (!validatePreferences()) {
            mStatusMessageField.append("\n"
                    + getResources().getString(
                    R.string.main_status_message_errorprefix)
                    + " "
                    + getResources().getString(
                    R.string.main_default_wrongconfig_message));
        } else {
            // set status message to-text-output-field
            mStatusMessageField.append("\n"
                    + getResources().getString(
                    R.string.main_status_message_prefix) + " "
                    + "Sending Request to "
                    + mPreferences.getIFMAPServerIpPreference() + ":"
                    + mPreferences.getIFMAPServerPortPreference());
            if (initIFMAPConnection()) {
                startIFMAPConnectionService();
            }
        }
    }


    // -------------------------------------------------------------------------
    // CONNECTION/SERVICE-START HANDLING
    // -------------------------------------------------------------------------

    /**
     * check if the preference values are valid
     */
    public boolean validatePreferences() {

        if (mPreferences.getMonitoringPreference().equalsIgnoreCase("iMonitor")) {
            // validate password (defaults always to "icinga")
            if (mPreferences.getNscaPassPreference() == null
                    || !(mPreferences.getNscaPassPreference().length() > 0)) {
                mStatusMessageField.append("\n"
                        + getResources().getString(
                        R.string.main_status_message_errorprefix)
                        + " NSCA Password is null or empty!");
                return false;
            }

            // validate ip-setting from preferences
            if (mPreferences.getIMonitorServerIpPreference() == null
                    || !(mPreferences.getIMonitorServerIpPreference().length() > 0)) {
                mStatusMessageField.append("\n"
                        + getResources().getString(
                        R.string.main_status_message_errorprefix)
                        + " iMonitor ip is null or empty!");
                return false;
            } else {
                Matcher ipMatcher = Toolbox.getIpPattern().matcher(
                        mPreferences.getIMonitorServerIpPreference());
                if (!ipMatcher.find()) {
                    mStatusMessageField.append("\n"
                            + getResources().getString(
                            R.string.main_status_message_errorprefix)
                            + " iMonitor ip is not valid!");
                    return false;
                }
            }
            // validate portnumber
            if (mPreferences.getIMonitorServerPortPreference() == null ||
                    !(mPreferences.getIMonitorServerPortPreference().length() > 0)) {
                mStatusMessageField.append("\n"
                        + getResources().getString(
                        R.string.main_status_message_errorprefix)
                        + " iMonitor port is null or empty!");
                return false;
            } else {
                try {
                    int d = Integer.parseInt(mPreferences.getIMonitorServerPortPreference());
                } catch (NumberFormatException nfe) {
                    mStatusMessageField.append("\n"
                            + getResources().getString(
                            R.string.main_status_message_errorprefix)
                            + " iMonitor port is not a number!");
                    return false;
                }
            }
        } else if (mPreferences.getMonitoringPreference().equalsIgnoreCase("IF-MAP")) {

            if (mPreferences.isUseBasicAuth()) {
                // validate username
                if (mPreferences.getUsernamePreference() == null
                        || !(mPreferences.getUsernamePreference().length() > 0)) {
                    mStatusMessageField.append("\n"
                            + getResources().getString(
                            R.string.main_status_message_errorprefix)
                            + " basic auth username is null or empty!");
                    return false;
                }
                // validate password
                if (mPreferences.getPasswordPreference() == null
                        || !(mPreferences.getPasswordPreference().length() > 0)) {
                    mStatusMessageField.append("\n"
                            + getResources().getString(
                            R.string.main_status_message_errorprefix)
                            + " basic auth password is null or empty!");
                    return false;
                }
            } else {

            }

            // validate ip-setting from preferences
            if (mPreferences.getIFMAPServerIpPreference() == null
                    || !(mPreferences.getIFMAPServerIpPreference().length() > 0)) {
                mStatusMessageField.append("\n"
                        + getResources().getString(
                        R.string.main_status_message_errorprefix)
                        + " IF-MAP ip is null or empty!");
                return false;
            } else {
                Matcher ipMatcher = Toolbox.getIpPattern().matcher(
                        mPreferences.getIFMAPServerIpPreference());
                if (!ipMatcher.find()) {
                    mStatusMessageField.append("\n"
                            + getResources().getString(
                            R.string.main_status_message_errorprefix)
                            + " IF-MAP ip is not valid!");
                    return false;
                }
            }
            // validate portnumber
            if (mPreferences.getIFMAPServerPortPreference() == null ||
                    !(mPreferences.getIFMAPServerPortPreference().length() > 0)) {
                mStatusMessageField.append("\n"
                        + getResources().getString(
                        R.string.main_status_message_errorprefix)
                        + " IF-MAP port is null or empty!");
                return false;
            } else {
                try {
                    int d = Integer.parseInt(mPreferences.getIFMAPServerPortPreference());
                } catch (NumberFormatException nfe) {
                    mStatusMessageField.append("\n"
                            + getResources().getString(
                            R.string.main_status_message_errorprefix)
                            + " IF-MAP port is not a number!");
                    return false;
                }
            }
        } else {
            mStatusMessageField.append("\n"
                    + getResources().getString(
                    R.string.main_status_message_errorprefix)
                    + " Monitoring-mode is unknown!");
            return false;
        }

        return true;
    }

    /**
     * start the connection-service to connect to the Map-Server
     */
    public void startIFMAPConnectionService() {
        Toolbox.logTxt(this.getLocalClassName(),
                "startIFMAPConnectionService(...) called");

        // disable buttons before starting request-generation
        mConnectButton.setEnabled(false);
        mDisconnectButton.setEnabled(false);
        mPublishDeviceCharacteristicsButton.setEnabled(false);

        mStatusMessageField.append("\n"
                + getResources().getString(R.string.main_status_message_prefix)
                + " " + "preparing data for request");

        PublishRequest publishReq = parameters.generateSRCRequestParamteres(
                mMessageType, mDeviceProperties,
                mPreferences.isUseNonConformMetadata(),
                mPreferences.isDontSendApplicationsInfos(),
                mPreferences.isDontSendGoogleApps());

        if (!mPreferences.isPermantConnection()) {
            // gather parameters for local service
            mLocalServicePreferences = new LocalServiceParameters(
                    LocalServiceParameters.SERVICE_BINDER_TYPE_RENEW_CONNECTION_SERVICE,
                    mPreferences, mDeviceProperties.getSystemProperties()
                    .getLocalIpAddress(), mMessageType, publishReq,
                    mMsgHandler);

            // initialize and bind local service
            mConnection = LocalServiceSynchronous.getConnection(
                    getApplicationContext(), mLocalServicePreferences,
                    new SynchronousRunnable(), Toolbox
                            .generateRequestLogMessageFromPublishRequest(
                                    mMessageType, publishReq));
            mIsBound = BinderClass.doBindRenewConnectionService(
                    getApplicationContext(), mConnection);

        } else {
            // gather parameters for permanent connection
            mLocalServicePreferences = new LocalServiceParameters(
                    LocalServiceParameters.SERVICE_BINDER_TYPE_PERMANENT_CONNECTION_SERVICE,
                    mPreferences, mDeviceProperties.getSystemProperties()
                    .getLocalIpAddress(), mMessageType, publishReq,
                    mMsgHandler);

            // initialize and bind local service
            mPermConnection = LocalServicePermanent.getPermConnection(
                    getApplicationContext(), mLocalServicePreferences,
                    new PermanentRunnable(), Toolbox
                            .generateRequestLogMessageFromPublishRequest(
                                    mMessageType, publishReq));

            mIsBound = BinderClass.doBindPermConnectionService(
                    getApplicationContext(), mPermConnection);
        }

        // show progress dialog
        myProgressDialog = ProgressDialog.show(
                MainActivity.this,
                getResources().getString(
                        R.string.main_progressbar_message_srcrequest_1),
                getResources().getString(
                        R.string.main_progressbar_message_srcrequest_2), true,
                false);
    }

// -------------------------------------------------------------------------
// SERVICE CALLBACK HANDLING
// -------------------------------------------------------------------------

    /**
     * Callback-Handler for Local Service that handles synchronous
     * Server-Responses (renew-session-method)
     */
    public class SynchronousRunnable implements Runnable {
        public ResponseParameters msg;
        public byte responseType;
        public LogMessage logRequestMsg;
        public LogMessage logResponseMsg;

        @Override
        public void run() {

            // process server-result
            processSRCResponseParameters(responseType, msg, logRequestMsg,
                    logResponseMsg);

            // unbind service
            if (sBoundRenewConnService != null) {
                mIsBound = UnbinderClass.doUnbindConnectionService(
                        getApplicationContext(), mConnection, myProgressDialog,
                        mIsBound);
                sBoundRenewConnService = null;
            }
        }
    }

    /**
     * Callback-Handler for Local Service that handles synchronous
     * Server-Responses (permanent-connection-method)
     */
    public class PermanentRunnable implements Runnable {
        public ResponseParameters msg;
        public byte responseType;
        public LogMessage logRequestMsg;
        public LogMessage logResponseMsg;

        @Override
        public void run() {
            // process server-result
            processSRCResponseParameters(responseType, msg, logRequestMsg,
                    logResponseMsg);
            mResponseType = responseType;

            // unbind service
            if (sBoundPermConnService != null) {
                mIsBound = UnbinderClass.doUnbindConnectionService(
                        getApplicationContext(), mPermConnection,
                        myProgressDialog, mIsBound);
                sBoundPermConnService = null;
            }
        }

    }

    // -------------------------------------------------------------------------
    // RESPONSE-MESSAGE-HANDLING
    // -------------------------------------------------------------------------

    /**
     * process incoming response-parameters from Map-Server Response on
     * src-channel and set resulting application states
     *
     * @param messageType type of message to process
     * @param msg         response-parameters-object to process
     * @param requestMsg  log-message containing outgoing parameters send to MAP-Server
     * @param responseMsg log-message containing incoming parameters received from
     *                    MAP-Server
     */
    public void processSRCResponseParameters(byte messageType,
                                             ResponseParameters msg, LogMessage requestMsg,
                                             LogMessage responseMsg) {
        switch (messageType) {

            // -----> NEW SESSION RESPONSE <-----
            case MessageHandler.MSG_TYPE_REQUEST_NEWSESSION:
                mIsConnected = true;

                // lock parts of preferences-tab that cannot be changed as
                // long as a connection is established
                mPreferences.setLockPreferences(true);
//                PreferencesValues.sLockConnectionPreferences = true;
//                PreferencesValues.sLockLocationTrackingOptions = true;

                mCurrentSessionId = msg
                        .getParameter(ResponseParameters.RESPONSE_PARAMS_SESSIONID);
                sCurrentPublisherId = msg
                        .getParameter(ResponseParameters.RESPONSE_PARAMS_PUBLISHERID);

                // in case of renew-session method, start renew-session-handler
                // to periodically send renew-session-requests
                if (sBoundRenewConnService != null) {

                    // (re)initialize handler for periodically sending location-data
                    if (mRenewHandler == null) {
                        mRenewHandler = new Handler();
                    }
                    mRenewHandler.removeCallbacks(mUpdateRenewTimeTask);
                    mRenewHandler.postDelayed(mUpdateRenewTimeTask, 15000);
                }

                // (re)initialize handler for periodically sending meta-data
                if (mUpdateHandler == null) {
                    mUpdateHandler = new Handler();
                }

                mUpdateHandler.removeCallbacks(mUpdateTimeTask);
                mUpdateHandler.postDelayed(mUpdateTimeTask, 2000);
                break;

            // -----> END SESSION RESPONSE <-----
            case MessageHandler.MSG_TYPE_REQUEST_ENDSESSION:
                mIsConnected = false;

                // "unlock" some parts of preferences
                mPreferences.setLockPreferences(false);
//                PreferencesValues.sLockConnectionPreferences = false;
//                PreferencesValues.sLockLocationTrackingOptions = false;
                mCurrentSessionId = null; // session has ended!

                // deactivate handler for sending metadata and renew-messages to
                // server
                if (mRenewHandler != null) {
                    mRenewHandler.removeCallbacksAndMessages(null);
                    mRenewHandler = null;
                }
                if (mUpdateHandler != null) {
                    mUpdateHandler.removeCallbacksAndMessages(null);
                    mUpdateHandler = null;
                }
                break;

            // -----> ERROR-MESSAGE RESPONSE <-----
            case MessageHandler.MSG_TYPE_ERRORMSG:
                // error-response, reset all messaging-related values
                mIsConnected = false;
                mPreferences.setLockPreferences(false);
//                PreferencesValues.sLockLocationTrackingOptions = false;
                mCurrentSessionId = null;
                MessageParametersGenerator.sInitialDevCharWasSend = false;

                // deactivate handler for sending renew-messages to server
                if (mRenewHandler != null) {
                    mRenewHandler.removeCallbacksAndMessages(null);
                    mRenewHandler = null;
                }
                break;

            // -----> PUBLISH DEVICE CHARACTERISTICS RESPONSE <-----
            case MessageHandler.MSG_TYPE_PUBLISH_CHARACTERISTICS:
                mIsConnected = true;
                mPreferences.setLockPreferences(true);
                break;
        }

        // set output to main-text-output-field
        if (messageType != MessageHandler.MSG_TYPE_ERRORMSG) {
            mStatusMessageField
                    .append("\n"
                            + getResources().getString(
                            R.string.main_status_message_prefix)
                            + " "
                            + (msg.getParameter(ResponseParameters.RESPONSE_PARAMS_MSGCONTENT)));
        } else {
            // in case of error message, add a text-prefix
            mStatusMessageField
                    .append("\n"
                            + getResources().getString(
                            R.string.main_status_message_errorprefix)
                            + " "
                            + (msg.getParameter(ResponseParameters.RESPONSE_PARAMS_MSGCONTENT)));
        }

        // set notification about incoming response
        Toolbox.showNotification(
                getResources().getString(
                        R.string.main_notification_message_label),
                getResources().getString(
                        R.string.main_notification_message_message),
                msg.getParameter(ResponseParameters.RESPONSE_PARAMS_STATUSMSG),
                getApplicationContext());

        // add collected Log Messages from Request/Response to Log-Message-List
        if (messageType == MessageHandler.MSG_TYPE_PUBLISH_CHARACTERISTICS
                || messageType == MessageHandler.MSG_TYPE_METADATA_UPDATE) {
            // currently, the esukom-specific data is too much for the db to
            // handle
            // so for now we disable logging in this case
            if (mPreferences.isUseNonConformMetadata()) {
                requestMsg
                        .setMsg("logging of esukom specific metadata is currently not supported!\n");
                responseMsg
                        .setMsg("logging of esukom specific metadata is currently not supported!\n");
            }
        }
        LogMessageHelper.getInstance().logMessage(messageType, requestMsg,
                responseMsg, mPreferences, mLogDB);

        // change activity button states
        changeButtonStates(mIsConnected);
    }

    // -------------------------------------------------------------------------
    // RENEW SESSION HANDLING
    // -------------------------------------------------------------------------

    /**
     * handler which is executed at predefined interval, sends renews-session
     * messages
     */
    private Handler mRenewHandler = new Handler();
    private Runnable mUpdateRenewTimeTask = new Runnable() {
        public void run() {
            sendRenewSessionToServer();
            mRenewHandler.postDelayed(this,
                    mPreferences.getRenewRequestMinInterval());
        }
    };

    /**
     * Triggers the sending of the renew-session message
     */
    public void sendRenewSessionToServer() {
        Toolbox.logTxt(this.getLocalClassName(), "sendRenewSession(...) called");
        mMessageType = MessageHandler.MSG_TYPE_REQUEST_RENEWSESSION;
        // generate publish-request-object
        PublishRequest publishReq = parameters.generateSRCRequestParamteres(
                mMessageType, mDeviceProperties,
                mPreferences.isUseNonConformMetadata(),
                mPreferences.isDontSendApplicationsInfos(),
                mPreferences.isDontSendGoogleApps());

        if (!mIsBound) {
            // renew-session-connection
            // gather local service parameters
            mLocalServicePreferences = new LocalServiceParameters(
                    LocalServiceParameters.SERVICE_BINDER_TYPE_RENEW_CONNECTION_SERVICE,
                    mPreferences, mDeviceProperties.getSystemProperties()
                    .getLocalIpAddress(), mMessageType, publishReq,
                    mMsgHandler);

            // initialize and bind service
            mConnection = LocalServiceSynchronous.getConnection(
                    getApplicationContext(), mLocalServicePreferences,
                    new SynchronousRunnable(), Toolbox
                            .generateRequestLogMessageFromPublishRequest(
                                    mMessageType, publishReq));
            mIsBound = BinderClass.doBindRenewConnectionService(
                    getApplicationContext(), mConnection);
        }
    }

    // -------------------------------------------------------------------------
    // LOCATION TRACKING HANDLING
    // -------------------------------------------------------------------------

    /**
     * Lets set the current position in the status tab if it is already active
     *
     * @param latitude  current latitude value
     * @param longitude current longitude value
     * @param altitude  current altitude value
     */
    public void setCurrentLocation(double latitude, double longitude,
                                   double altitude) {
        sLatitude = String.valueOf(latitude);
        sLongitude = String.valueOf(longitude);
        sAltitude = String.valueOf(altitude);

        // if status activity is active, pass current location values to it
        if (StatusActivity.sIsActivityActive) {
            StatusActivity.setCurrentLocation(latitude, longitude, altitude);
        }
    }

    // -------------------------------------------------------------------------
    // AUTO-UPDATE-HANDLING
    // -------------------------------------------------------------------------

    /**
     * handler which is executed at predefined interval, sends the current
     * location to the map-server
     */
    private Handler mUpdateHandler = new Handler();
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (mPreferences.isAutoUpdate()) {
                sendMetadataUpdateToServer();
                mUpdateHandler.postDelayed(this,
                        mPreferences.getUpdateInterval());
            }
        }
    };

    /**
     * Triggers the sending of device characteristics and location-metadata, if
     * a connection to the server is established
     */
    public void sendMetadataUpdateToServer() {
        if (mIsConnected) {
            mMessageType = MessageHandler.MSG_TYPE_METADATA_UPDATE;
            startIFMAPConnectionService();
        }
    }

    // -------------------------------------------------------------------------
    // iMonitor-Connection
    // -------------------------------------------------------------------------

    /**
     * create service connection
     * send InfoEvent and AppEvent on first connect
     */
    private ServiceConnection mNscaConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            mNscaServiceBind = binder.getService();

            Encryption mNscaEncryption = null;
            switch (mPreferences.getNscaEncPreference()) {
                case "0":
                    mNscaEncryption = Encryption.NO_ENCRYPTION;
                    break;
                case "1":
                    mNscaEncryption = Encryption.XOR_ENCRYPTION;
                    break;
                case "2":
                    mNscaEncryption = Encryption.TRIPLE_DES_ENCRYPTION;
                    break;
            }

            mNscaServiceBind.setupConnection(mPreferences.getIMonitorServerIpPreference(), mPreferences.getIMonitorServerPortPreference(), mPreferences.getNscaPassPreference(), mNscaEncryption);

            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMonitorEventReceiver, new IntentFilter("iMonitor-Event"));
            EventParameters eP = new EventParameters(mDeviceProperties);
            mNscaServiceBind.publish(eP.genInfoEvent());
            mNscaServiceBind.publish(eP.genAppEvents());

            mNscaServiceBind.startMonitor(mPreferences.getUpdateInterval());
            myProgressDialog.dismiss();
            mStatusMessageField.append("\n"
                    + getResources().getString(R.string.main_status_message_prefix)
                    + " " + "Connection established");
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mIsBound = false;
        }
    };

    /**
     * receive (local) intents to generate and publish new events or drop connection
     */
    private BroadcastReceiver mMonitorEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventParameters eP = new EventParameters(mDeviceProperties);
            String type = intent.getStringExtra("Event");
            if (type != null) {
                switch (type) {
                    case "AppEvent":
                        mNscaServiceBind.publish(eP.genAppEvents());
                        mStatusMessageField.append("\n"
                                + getResources().getString(
                                R.string.main_status_message_prefix) + " "
                                + "AppEvent sent.");
                        break;
                    case "MonitorEvent":
                        mNscaServiceBind.publish(eP.genMonitorEvent());
                        mStatusMessageField.append("\n"
                                + getResources().getString(
                                R.string.main_status_message_prefix) + " "
                                + "MonitorEvent sent.");
                        break;
                    case "ConnectionError":
                        mStatusMessageField
                                .append("\n"
                                        + getResources()
                                        .getString(
                                                R.string.main_default_connectionerror_nsca));
                        disconnectNSCA();
                        break;
                }
            }
        }
    };

}

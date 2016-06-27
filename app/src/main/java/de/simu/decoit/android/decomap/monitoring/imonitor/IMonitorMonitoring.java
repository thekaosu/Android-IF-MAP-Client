/*
 * IMonitorMonitoring..java          0.3 2015-03-08
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
 * d
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.simu.decoit.android.decomap.monitoring.imonitor;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.googlecode.jsendnsca.encryption.Encryption;

import de.simu.decoit.android.decomap.activities.MainActivity;
import de.simu.decoit.android.decomap.activities.R;
import de.simu.decoit.android.decomap.logging.LogMessage;
import de.simu.decoit.android.decomap.logging.LogMessageHelper;
import de.simu.decoit.android.decomap.messaging.EventParameters;
import de.simu.decoit.android.decomap.messaging.MessageHandler;
import de.simu.decoit.android.decomap.monitoring.MonitoringInterface;
import de.simu.decoit.android.decomap.preferences.PreferenceValidator;
import de.simu.decoit.android.decomap.preferences.PreferencesValues;
import de.simu.decoit.android.decomap.services.NscaService;
import de.simu.decoit.android.decomap.services.binder.BinderClass;
import de.simu.decoit.android.decomap.services.binder.UnbinderClass;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * IMonitorMonitoring which handles all iMonitor monitoring mode based connection functionality
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Marcel Jahnke, Decoit GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.3
 */
public class IMonitorMonitoring implements MonitoringInterface {

    private boolean isConnected = false;

    // progress dialog and notifications
    private ProgressDialog myProgressDialog = null;

    // local services states
    private boolean mIsBound;

    // manage nsca connection for iMonitor
    private NscaService mNscaServiceBind;

    private final MainActivity activity;
    private final PreferencesValues mPreferences;
    private final TextView mStatusMessageField;

    public IMonitorMonitoring(MainActivity activity, PreferencesValues mPreferences, TextView mStatusMessageField) {
        this.activity = activity;
        this.mPreferences = mPreferences;
        this.mStatusMessageField = mStatusMessageField;
    }

    public void mainTabButtonHandler(View view) {
        if (isConnected) {
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

    @Override
    public void onDestroy() {
        if (isConnected) {
            disconnectNSCA();
        }
    }

    @Override
    public void autoConnection() {
        connectNSCA();
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    private void connectNSCA() {
        // start connection service if all required preferences are set
        if (PreferenceValidator.incorrectPreferencesConfiguration(mPreferences, activity.getResources(), mStatusMessageField)) {
            mStatusMessageField.append("\n"
                    + activity.getResources().getString(
                    R.string.main_status_message_errorprefix)
                    + " "
                    + activity.getResources().getString(
                    R.string.main_default_wrongconfig_message));
        } else {

            // set status message to-text-output-field
            mStatusMessageField.append("\n"
                    + activity.getResources().getString(
                    R.string.main_status_message_prefix) + " "
                    + activity.getResources().getString(R.string.main_status_message_addition_sending_Data)
                    + mPreferences.getIMonitorServerIpPreference() + ":"
                    + mPreferences.getIMonitorServerPortPreference());

            mIsBound = BinderClass.doBindNscaService(activity.getApplicationContext(),
                    mNscaConnection);

            mPreferences.setLockPreferences(true);

            isConnected = true;
            activity.changeButtonStates(true);

            myProgressDialog = ProgressDialog.show(
                    activity,
                    activity.getResources().getString(
                            R.string.main_progressbar_message_srcrequest_1),
                    activity.getResources().getString(
                            R.string.main_progressbar_message_srcrequest_2), true, false);

        }
    }

    private void disconnectNSCA() {
        mNscaServiceBind.stopMonitor();
        LocalBroadcastManager.getInstance(activity.getApplicationContext()).unregisterReceiver(mMonitorEventReceiver);
        mIsBound = UnbinderClass.doUnbindConnectionService(
                activity.getApplicationContext(), mNscaConnection, myProgressDialog,
                mIsBound);
        isConnected = false;
        activity.changeButtonStates(false);
        mStatusMessageField.append("\n"
                + activity.getResources().getString(
                R.string.main_status_message_prefix) + " "
                + activity.getResources().getString(R.string.main_status_message_addition_disconnected));
        Toolbox.showNotification(
                activity.getResources().getString(R.string.notification_nsca_label),
                activity.getResources().getString(R.string.notification_disconnect),
                activity.getApplicationContext());

        mPreferences.setLockPreferences(false);
//        PreferencesValues.sLockConnectionPreferences = false;
    }

    // -------------------------------------------------------------------------
    // iMonitor-Connection
    // -------------------------------------------------------------------------

    /**
     * create service connection
     * send InfoEvent and AppEvent on first connect
     */
    private final ServiceConnection mNscaConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            NscaService.LocalBinder binder = (NscaService.LocalBinder) service;
            mNscaServiceBind = binder.getService();

            Encryption mNscaEncryption = null;
            switch (mPreferences.getNscaEncPreference()) {
                case "0":
                    mNscaEncryption = Encryption.NONE;
                    break;
                case "1":
                    mNscaEncryption = Encryption.XOR;
                    break;
                case "2":
                    mNscaEncryption = Encryption.TRIPLE_DES;
                    break;
            }

            mNscaServiceBind.setupConnection(mPreferences.getIMonitorServerIpPreference(), mPreferences.getIMonitorServerPortPreference(), mPreferences.getNscaPassPreference(), mNscaEncryption);

            LocalBroadcastManager.getInstance(activity.getApplicationContext()).registerReceiver(mMonitorEventReceiver, new IntentFilter("iMonitor-Event"));
            EventParameters eP = new EventParameters(activity.getDeviceProperties());

            mNscaServiceBind.startMonitor(mPreferences.getUpdateInterval());

            mNscaServiceBind.publish(eP.genInfoEvent());
            mNscaServiceBind.publish(eP.genAppEvents());


            mStatusMessageField.append("\n"
                    + activity.getResources().getString(R.string.main_status_message_prefix)
                    + " " + activity.getResources().getString(R.string.main_status_message_connection_established));
            myProgressDialog.dismiss();
        }

        public void onServiceDisconnected(ComponentName arg0) {
            mIsBound = false;
        }
    };

    /**
     * receive (local) intents to generate and publish new events or drop connection
     */
    private final BroadcastReceiver mMonitorEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            EventParameters eP = new EventParameters(activity.getDeviceProperties());
            String type = intent.getStringExtra("Event");
            byte responseType = 0;
            String status = "";
            if (type != null) {
                switch (type) {
                    case "CONNECTION READY":
                        status = activity.getResources().getString(R.string.main_default_connection_established);
                        responseType = MessageHandler.MSG_TYPE_REQUEST_NEWSESSION;
                        break;
                    case "APP EVENT":
                        mNscaServiceBind.publish(eP.genAppEvents());
                        mStatusMessageField.append("\n"
                                + activity.getResources().getString(
                                R.string.main_status_message_prefix) + " "
                                + "AppEvent sent.");
                        status = activity.getResources().getString(R.string.main_default_sending_appevent);
                        responseType = MessageHandler.MSG_TYPE_PUBLISH_CHARACTERISTICS;
                        break;
                    case "MONITOR EVENT":
                        mNscaServiceBind.publish(eP.genMonitorEvent());
                        mStatusMessageField.append("\n"
                                + activity.getResources().getString(
                                R.string.main_status_message_prefix) + " "
                                + "MonitorEvent sent.");
                        status = activity.getResources().getString(R.string.main_default_sending_monitorevent);
                        responseType = MessageHandler.MSG_TYPE_PUBLISH_CHARACTERISTICS;
                        break;
                    case "PUBLISH SUCCESS":
                        status = activity.getResources().getString(R.string.main_default_publish_passiv_nsca_check);
                        responseType = MessageHandler.MSG_TYPE_PUBLISH_CHARACTERISTICS;
                        break;
                    case "CONNECTION ERROR":
                        status = activity.getResources().getString(R.string.main_default_connectionerror_nsca_status);
                        responseType = MessageHandler.MSG_TYPE_ERRORMSG;
                        mStatusMessageField
                                .append("\n" + activity.getResources().getString(
                                        R.string.main_status_message_errorprefix)
                                        + activity.getResources()
                                        .getString(
                                                R.string.main_default_connectionerror_nsca));
                        disconnectNSCA();
                        break;
                    case "CONNECTION CLOSED":
                        status = activity.getResources().getString(R.string.main_default_connection_closed);
                        responseType = MessageHandler.MSG_TYPE_REQUEST_ENDSESSION;
                        break;
                    default:
                        status = "unknown";
                        responseType = MessageHandler.MSG_TYPE_ERRORMSG;
                }
            }

            String timestamp = intent.getStringExtra("Timestamp");
            String msg = intent.getStringExtra("Msg");
            String target = intent.getStringExtra("Target");

            LogMessage logMsg = new LogMessage(timestamp, msg, type, target, status);
            LogMessageHelper.getInstance().logMessage(responseType, logMsg, mPreferences, activity.getLogDB());
        }
    };
}

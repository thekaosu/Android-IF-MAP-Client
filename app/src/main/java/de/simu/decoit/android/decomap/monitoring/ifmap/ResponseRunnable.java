/*
 * ResponseRunnable..java          0.3 2015-03-08
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
package de.simu.decoit.android.decomap.monitoring.ifmap;

import android.os.Handler;

import de.simu.decoit.android.decomap.activities.MainActivity;
import de.simu.decoit.android.decomap.activities.R;
import de.simu.decoit.android.decomap.logging.LogMessage;
import de.simu.decoit.android.decomap.logging.LogMessageHelper;
import de.simu.decoit.android.decomap.messaging.MessageHandler;
import de.simu.decoit.android.decomap.messaging.MessageParametersGenerator;
import de.simu.decoit.android.decomap.messaging.ResponseParameters;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * Callback-Handler for Local Service that handles Server-Responses
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Marcel Jahnke, Decoit GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.3
 */
public abstract class ResponseRunnable implements Runnable {

    protected final IFMapMonitoring monitor;

    public ResponseParameters msg;
    public byte responseType;
    public LogMessage logRequestMsg;
    public LogMessage logResponseMsg;

    /**
     * constructor
     *
     * @param monitor IFMap Monitor to get values
     */
    public ResponseRunnable(IFMapMonitoring monitor) {
        this.monitor = monitor;
    }

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
    protected void processSRCResponseParameters(byte messageType,
                                                ResponseParameters msg, LogMessage requestMsg,
                                                LogMessage responseMsg) {
        switch (messageType) {

            // -----> NEW SESSION RESPONSE <-----
            case MessageHandler.MSG_TYPE_REQUEST_NEWSESSION:
                monitor.isConnected = true;

                // lock parts of preferences-tab that cannot be changed as
                // long as a connection is established
                monitor.mPreferences.setLockPreferences(true);

                monitor.mCurrentSessionId = msg
                        .getParameter(ResponseParameters.RESPONSE_PARAMS_SESSIONID);
                MainActivity.sCurrentPublisherId = msg
                        .getParameter(ResponseParameters.RESPONSE_PARAMS_PUBLISHERID);

                // in case of renew-session method, start renew-session-handler
                // to periodically send renew-session-requests
                if (IFMapMonitoring.sBoundRenewConnService != null) {

                    // (re)initialize handler for periodically sending location-data
                    if (monitor.mRenewHandler == null) {
                        monitor.mRenewHandler = new Handler();
                    }
                    monitor.mRenewHandler.removeCallbacks(monitor.mUpdateRenewTimeTask);
                    monitor.mRenewHandler.postDelayed(monitor.mUpdateRenewTimeTask, monitor.mPreferences.getRenewIntervalPreference());
                }

                // (re)initialize handler for periodically sending meta-data
                if (monitor.mUpdateHandler == null) {
                    monitor.mUpdateHandler = new Handler();
                }

                monitor.mUpdateHandler.removeCallbacks(monitor.mUpdateTimeTask);
                monitor.mUpdateHandler.postDelayed(monitor.mUpdateTimeTask,  monitor.mPreferences.getUpdateInterval());
                break;

            // -----> END SESSION RESPONSE <-----
            case MessageHandler.MSG_TYPE_REQUEST_ENDSESSION:
                monitor.isConnected = false;

                // "unlock" some parts of preferences
                monitor.mPreferences.setLockPreferences(false);
                monitor.mCurrentSessionId = null; // session has ended!

                monitor.onDestroy();
                break;

            // -----> ERROR-MESSAGE RESPONSE <-----
            case MessageHandler.MSG_TYPE_ERRORMSG:
                // error-response, reset all messaging-related values
                monitor.isConnected = false;
                monitor.mPreferences.setLockPreferences(false);
                monitor.mCurrentSessionId = null;
                MessageParametersGenerator.sInitialDevCharWasSend = false;

                monitor.onDestroy();
                break;

            // -----> PUBLISH DEVICE CHARACTERISTICS RESPONSE <-----
            case MessageHandler.MSG_TYPE_PUBLISH_CHARACTERISTICS:
                monitor.isConnected = true;
                monitor.mPreferences.setLockPreferences(true);
                break;
        }

        // set output to main-text-output-field
        if (messageType != MessageHandler.MSG_TYPE_ERRORMSG) {
            monitor.mStatusMessageField
                    .append("\n"
                            + monitor.activity.getResources().getString(
                            R.string.main_status_message_prefix)
                            + " "
                            + (msg.getParameter(ResponseParameters.RESPONSE_PARAMS_STATUSMSG)));
        } else {
            // in case of error message, add a text-prefix
            monitor.mStatusMessageField
                    .append("\n"
                            + monitor.activity.getResources().getString(
                            R.string.main_status_message_errorprefix)
                            + " "
                            + (msg.getParameter(ResponseParameters.RESPONSE_PARAMS_MSGCONTENT)));
        }

        // set notification about incoming response
        Toolbox.showNotification(
                monitor.activity.getResources().getString(
                        R.string.main_notification_message_message),
                msg.getParameter(ResponseParameters.RESPONSE_PARAMS_STATUSMSG),
                monitor.activity.getApplicationContext());

        // add collected Log Messages from Request/Response to Log-Message-List
        if (messageType == MessageHandler.MSG_TYPE_PUBLISH_CHARACTERISTICS
                || messageType == MessageHandler.MSG_TYPE_METADATA_UPDATE) {
            // currently, the esukom-specific data is too much for the db to
            // handle
            // so for now we disable logging in this case
            if (monitor.mPreferences.isUseNonConformMetadata()) {
                requestMsg
                        .setMsg(monitor.activity.getResources().getString(R.string.main_status_message_esukom_metadata_not_supportet));
                responseMsg
                        .setMsg(monitor.activity.getResources().getString(R.string.main_status_message_esukom_metadata_not_supportet));
            }
        }
        LogMessageHelper.getInstance().logMessage(messageType, requestMsg,
                responseMsg, monitor.mPreferences, monitor.activity.getLogDB());

        // change activity button states
        monitor.activity.changeButtonStates(monitor.isConnected);
    }
}

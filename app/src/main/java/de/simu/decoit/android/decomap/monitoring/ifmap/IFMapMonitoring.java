/*
 * IFMapMonitoring..java          0.3 2015-03-08
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

import android.app.ProgressDialog;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import de.hshannover.f4.trust.ifmapj.IfmapJHelper;
import de.hshannover.f4.trust.ifmapj.channel.SSRC;
import de.hshannover.f4.trust.ifmapj.channel.SsrcImpl;
import de.hshannover.f4.trust.ifmapj.exception.InitializationException;
import de.hshannover.f4.trust.ifmapj.messages.PublishRequest;
import de.simu.decoit.android.decomap.activities.MainActivity;
import de.simu.decoit.android.decomap.activities.R;
import de.simu.decoit.android.decomap.connection.ConnectionObjects;
import de.simu.decoit.android.decomap.messaging.MessageHandler;
import de.simu.decoit.android.decomap.messaging.MessageParametersGenerator;
import de.simu.decoit.android.decomap.monitoring.MonitoringInterface;
import de.simu.decoit.android.decomap.preferences.PreferenceValidator;
import de.simu.decoit.android.decomap.preferences.PreferencesValues;
import de.simu.decoit.android.decomap.services.PermanentConnectionService;
import de.simu.decoit.android.decomap.services.RenewConnectionService;
import de.simu.decoit.android.decomap.services.binder.BinderClass;
import de.simu.decoit.android.decomap.services.binder.UnbinderClass;
import de.simu.decoit.android.decomap.services.local.LocalServiceParameters;
import de.simu.decoit.android.decomap.services.local.LocalServicePermanent;
import de.simu.decoit.android.decomap.services.local.LocalServiceSynchronous;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * IFMapMonitoring which handles all IF-Map monitoring mode based connection functionality
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Marcel Jahnke, Decoit GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.3
 */
public class IFMapMonitoring implements MonitoringInterface {

    // local services definitions
    public static RenewConnectionService.LocalBinder sBoundRenewConnService;
    public static PermanentConnectionService.LocalBinder sBoundPermConnService;

    boolean isConnected = false;

    // local services states
    boolean mIsBound;

    // progress dialog and notifications
    ProgressDialog myProgressDialog = null;

    // current if-map session and publisher id
    public String mCurrentSessionId;

    // local services
    ServiceConnection mConnection;

    // current messaging type (as defined in Message-Handler-Class)
    byte mMessageType;

    // type of server-response
    byte mResponseType;

    // callback-handler for local-services
    private final Handler mMsgHandler = new Handler();

    final MainActivity activity;
    final PreferencesValues mPreferences;
    final TextView mStatusMessageField;

    // handler which is executed at predefined interval, sends metadata to the map-server
    Handler mUpdateHandler = new Handler();

    // handler which is executed at predefined interval, sends renews-session messages
    Handler mRenewHandler = new Handler();

    final Runnable mUpdateTimeTask = new AutoUpdateTask(this, mUpdateHandler);
    final Runnable mUpdateRenewTimeTask = new UpdateRenewTask(this, mRenewHandler);

    // message-parameter-generator
    private final MessageParametersGenerator<PublishRequest> parameters;

    /**
     * constructor
     * @param activity MainActivity in which the class is running
     * @param mPreferences preference instance
     * @param mStatusMessageField status message field to show errors
     */
    public IFMapMonitoring(MainActivity activity, PreferencesValues mPreferences, TextView mStatusMessageField) {
        this.activity = activity;
        this.mPreferences = mPreferences;
        this.mStatusMessageField = mStatusMessageField;

        // generator for if-map-messages to be published
        parameters = new MessageParametersGenerator<>();
    }

    /**
     * Handling Button pressing
     *
     * @param view view with buttons
     */
    public void mainTabButtonHandler(View view) {
        if (isConnected) {
            switch (view.getId()) {
                // close session button
                case R.id.Disconnect_Button:
                    mMessageType = MessageHandler.MSG_TYPE_REQUEST_ENDSESSION;
                    break;
                // publish device characteristics-button
                case R.id.PublishDeviceCharacteristics_Button:

                    // disable buttons before starting request-generation
                    activity.disableButtons();

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
                    + activity.getResources().getString(R.string.main_status_message_addition_sending_request_to)
                    + mPreferences.getIFMAPServerIpPreference() + ":"
                    + mPreferences.getIFMAPServerPortPreference());
            if (initIFMAPConnection()) {
                startIFMAPConnectionService();
            }
        }
    }

    @Override
    public void onDestroy() {
        // reset connection-objects
        ConnectionObjects.setSsrcConnection(null);

        // check and unbind all local services and handlers
        if (sBoundRenewConnService != null || sBoundPermConnService != null) {
            mIsBound = UnbinderClass.doUnbindConnectionService(
                    activity.getApplicationContext(), mConnection, myProgressDialog,
                    mIsBound);
            sBoundRenewConnService = null;
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

    @Override
    public void autoConnection() {
        // set status message to-text-output-field
        mStatusMessageField.append("\n"
                + activity.getResources().getString(
                R.string.main_status_message_prefix) + " "
                + activity.getResources().getString(R.string.main_status_message_addition_sending_request)
                + mPreferences.getIFMAPServerIpPreference() + ":"
                + mPreferences.getIFMAPServerPortPreference());

        // start new session
        mMessageType = MessageHandler.MSG_TYPE_REQUEST_NEWSESSION;
        if (initIFMAPConnection()) {
            startIFMAPConnectionService();
        }
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Initialize the connection object if not already initialized, else assign
     * already existing connection object
     */
    private boolean initIFMAPConnection() {
        Toolbox.logTxt(this.getClass().getName(), "initIFMAPConnection(...) called");
        SSRC sSsrcConnection;
        if (ConnectionObjects.getSsrcConnection() == null
                || (mResponseType == MessageHandler.MSG_TYPE_ERRORMSG)) {
            try {

                // if unsafe ssl is activated, set related properties for ifmapj
                if (mPreferences.isAllowUnsafeSSLPreference()) {
                    Toolbox.logTxt(this.getClass().getName(),
                            "using unsafe ssl - verifypeercert and host set to false");
                    System.setProperty("ifmapj.communication.verifypeercert",
                            "false");
                    System.setProperty("ifmapj.communication.verifypeerhost",
                            "false");
                } else {
                    Toolbox.logTxt(this.getClass().getName(), "using safe ssl");
                    System.setProperty("ifmapj.communication.verifypeercert",
                            "true");
                    System.setProperty("ifmapj.communication.verifypeerhost",
                            "");
                }

                TrustManager[] trustManagers = IfmapJHelper.getTrustManagers(activity.getResources().openRawResource(R.raw.keystore),
                        "androidmap");
                if (mPreferences.isUseBasicAuth()) {
                    // create ssrc-connection using basic-authentication
                    Toolbox.logTxt(this.getClass().getName(),
                            "initializing ssrc-connecion using basic-auth");
                    sSsrcConnection = new SsrcImpl(
                            "https://"
                                    + mPreferences.getIFMAPServerIpPreference() + ":"
                                    + mPreferences.getIFMAPServerPortPreference(), mPreferences.getUsernamePreference(), mPreferences
                            .getPasswordPreference(), trustManagers, mPreferences.getConnectionTimeout());
                } else {
                    // create ssrc-connection using certificates
                    Toolbox.logTxt(this.getClass().getName(),
                            "initializing ssrc-connecion using certificate-based-auth");
                    KeyManager[] keyManagers = IfmapJHelper.getKeyManagers(mPreferences.getKeystorePath(),
                            mPreferences.getKeystorePassword());
                    sSsrcConnection = new SsrcImpl("https://"
                            + mPreferences.getIFMAPServerIpPreference() + ":"
                            + mPreferences.getIFMAPServerPortPreference(), keyManagers, trustManagers, mPreferences.getConnectionTimeout());
                }

                mResponseType = 0;
            } catch (InitializationException e) {
                Toolbox.logTxt(this.getClass().getName(),
                        "error on connection initialization: " + e);
                mStatusMessageField.append("\n"
                        + activity.getResources().getString(
                        R.string.main_status_message_errorprefix) + " " + e.getMessage());
                return false;
            } catch (Resources.NotFoundException e) {
                Toolbox.logTxt(this.getClass().getName(),
                        "not found error while initialization of connection: " + e);
                mStatusMessageField.append("\n"
                        + activity.getResources().getString(
                        R.string.main_status_message_errorprefix) + " " + e.getMessage());
                return false;
            }
            ConnectionObjects.setSsrcConnection(sSsrcConnection);
        }

        return true;
    }

    /**
     * start the connection-service to connect to the Map-Server
     */
    void startIFMAPConnectionService() {
        Toolbox.logTxt(this.getClass().getName(),
                "startIFMAPConnectionService(...) called");

        // disable buttons before starting request-generation
        activity.disableButtons();

        mStatusMessageField.append("\n"
                + activity.getResources().getString(R.string.main_status_message_prefix)
                + " " + activity.getResources().getString(R.string.main_status_message_preparing_data));

        PublishRequest publishReq = parameters.generateSRCRequestParamteres(
                mMessageType, activity.getDeviceProperties(),
                mPreferences.isUseNonConformMetadata(),
                mPreferences.isDontSendApplicationsInfos(),
                mPreferences.isDontSendGoogleApps());

        // gather parameters for connection
        LocalServiceParameters mLocalServicePreferences = new LocalServiceParameters(
                mPreferences, activity.getDeviceProperties().getSystemProperties()
                .getLocalIpAddress(), mMessageType, publishReq,
                mMsgHandler);

        ResponseRunnable responseRunnable;
        if (!mPreferences.isPermantConnection()) {
            responseRunnable = new SynchronousRunnable(this);

            // initialize and bind local service
            mConnection = LocalServiceSynchronous.getConnection(
                    mLocalServicePreferences, responseRunnable
                    , Toolbox
                            .generateRequestLogMessageFromPublishRequest(
                                    mMessageType, publishReq));
            mIsBound = BinderClass.doBindRenewConnectionService(
                    activity.getApplicationContext(), mConnection);

        } else {
            responseRunnable = new PermanentRunnable(this);

            // initialize and bind local service
            mConnection = LocalServicePermanent.getPermConnection(
                    mLocalServicePreferences,
                    responseRunnable, Toolbox
                            .generateRequestLogMessageFromPublishRequest(
                                    mMessageType, publishReq));

            mIsBound = BinderClass.doBindPermConnectionService(
                    activity.getApplicationContext(), mConnection);
        }

        if (mMessageType != MessageHandler.MSG_TYPE_REQUEST_RENEWSESSION) {
            // show progress dialog
            myProgressDialog = ProgressDialog.show(
                    activity,
                    activity.getResources().getString(
                            R.string.main_progressbar_message_srcrequest_1),
                    activity.getResources().getString(
                            R.string.main_progressbar_message_srcrequest_2), true,
                    false);

        }
    }

}

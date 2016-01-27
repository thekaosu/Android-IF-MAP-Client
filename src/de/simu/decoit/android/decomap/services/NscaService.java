/* 
 * NscaService.java
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

package de.simu.decoit.android.decomap.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.googlecode.jsendnsca.MessagePayload;
import com.googlecode.jsendnsca.NagiosException;
import com.googlecode.jsendnsca.NagiosPassiveCheckSender;
import com.googlecode.jsendnsca.NagiosSettings;
import com.googlecode.jsendnsca.builders.MessagePayloadBuilder;
import com.googlecode.jsendnsca.builders.NagiosSettingsBuilder;
import com.googlecode.jsendnsca.encryption.Encryption;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Service to manage communication with NSCA/iMonitor
 *
 * @author Markus Schölzel, Decoit GmbH
 */

public class NscaService extends Service {
    private final IBinder mBinder = new LocalBinder();

    // some connection properties (used for connecting and log messages)
    private String mServerIP;
    private int mServerPort;
    private String mServerPass;
    private Encryption mServerEnc;
    private NagiosSettings mNagiosSettings;
    private NagiosPassiveCheckSender sender;

    private boolean readyToSend = false;

    private Handler mMonitorHandler;
    private long mMonitorInterval;
    private boolean mMonitorRunning = false;

    /**
     * start "monitoring" to generate MonitorEvents in the background
     */

    public void startMonitor(long interval) {
        if (!mMonitorRunning) {
            this.mMonitorInterval = interval;
            this.mMonitorHandler = new Handler();
            mMonitorHandler.postDelayed(runMonitorBackground, interval);
            mMonitorRunning = true;
        }
    }

    /**
     * stop generating MonitorEvents
     */
    public void stopMonitor() {
        generateIntent("CONNECTION CLOSED", "Connection Closed");
        mMonitorHandler.removeCallbacks(runMonitorBackground);
        mMonitorRunning = false;
        readyToSend = false;
    }

    /**
     * tidy way to send string to the nsca server
     */
    public void publish(String event) {
        NscaPublishThread mNscaPublishThread = new NscaPublishThread();
        if (readyToSend) {
            mNscaPublishThread.execute(event);
        }
    }

    /**
     * send multiple strings to the nsca server
     */
    public void publish(List<String> eventList) {
        for (String event : eventList)
            this.publish(event);
    }

    /**
     * create local intent to generate a new MonitorEvent
     */
    private void generateIntent(String event, String msg) {
        Intent intent = new Intent("iMonitor-Event");
        intent.putExtra("Event", event);
        intent.putExtra("Target", mServerIP + ":" + mServerPort);
        intent.putExtra("Timestamp", new Timestamp(new Date().getTime()).toString());
        intent.putExtra("Msg", msg);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * set up the nsca connection properties
     */
    public void setupConnection(String host, String port, String password,
                                Encryption enc) {

        this.mServerIP = host;
        this.mServerPort = Integer.parseInt(port);
        this.mServerPass = password;
        this.mServerEnc = enc;

        NscaConnectionSetupThread mNscaConnectionSetupThread = new NscaConnectionSetupThread();
        mNscaConnectionSetupThread.execute();
    }

    /**
     * set up the nsca connection properties in background using jsendnsca
     */
    private class NscaConnectionSetupThread extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mNagiosSettings = new NagiosSettingsBuilder()
                    .withNagiosHost(mServerIP).withPort(mServerPort)
                    .withEncryption(mServerEnc).withPassword(mServerPass)
                    .withLargeMessageSupportEnabled().create();

            sender = new NagiosPassiveCheckSender(mNagiosSettings);
            readyToSend = true;
            generateIntent("CONNECTION READY", "Established Connection");
            return null;
        }

    }

    /**
     * send nsca messages (events) using jsendnsca
     */
    private class NscaPublishThread extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            if (readyToSend) {
                MessagePayload payload = new MessagePayloadBuilder()
                        .withHostname("iMonitor-Sensors")
                        .withServiceName("Android Event").withLevel(0)
                        .withMessage(params[0]).create();

                try {
                    sender.send(payload);
                    generateIntent("PUBLISH SUCCESS", payload.toString().replace(",", ",\n"));
                } catch (NagiosException e) {
                    readyToSend = false;
                    generateIntent("CONNECTION ERROR", e.getMessage());
                } catch (IOException e) {
                    readyToSend = false;
                    generateIntent("CONNECTION ERROR", e.getMessage());
                }
            }
            return null;
        }
    }

    /**
     * run check on events in background (iMonitor/NSCA)
     */
    private Runnable runMonitorBackground = new Runnable() {
        @Override
        public void run() {
            generateIntent("MONITOR EVENT", "Sending MonitorEvent");
            mMonitorHandler.postDelayed(this, mMonitorInterval);
        }
    };

    public class LocalBinder extends Binder {
        public NscaService getService() {
            return NscaService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}

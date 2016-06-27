/*
 * LocalServiceParameters..java          0.3 2015-03-08
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

package de.simu.decoit.android.decomap.services.local;

import android.os.Handler;

import de.hshannover.f4.trust.ifmapj.messages.PublishRequest;
import de.simu.decoit.android.decomap.messaging.MessageHandler;
import de.simu.decoit.android.decomap.preferences.PreferencesValues;

/**
 * Container-Class that holds several parameters that are required for creating
 * a new local-service-object
 *
 * @author Dennis Dunekacke, DECOIT GmbH
 * @author Marcel Jahnke, DECOIT GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @version 0.3
 */
public class LocalServiceParameters {

    // local-service-type
    //public static final int SERVICE_BINDER_TYPE_PERMANENT_CONNECTION_SERVICE = 0;
    //public static final int SERVICE_BINDER_TYPE_RENEW_CONNECTION_SERVICE = 1;
    //public static final int SERVICE_BINDER_TYPE_TIME_COUNTING_SERVICE = 2;

    private byte mMessageType = 0;
    private String mServerPort = "0";
    private String mServerIpPreference = null;
    private String mIpAddress = null;
    private PublishRequest mReguestParamsPublish = null;
    private Handler mMsgHandler = null;

    // private TimeCountingService.LocalBinder timeCountingServiceBinder;

    /**
     * constructor
     *
     * @param prefs         preferences-object containing application-settings
     * @param ipAddress     ip-address of the client
     * @param messageType   type of message {@link MessageHandler}
     * @param reguestParams publish request parameter
     * @param msgHandler    callback-handler for local-service
     */
    public LocalServiceParameters(PreferencesValues prefs, String ipAddress, byte messageType,
                                  PublishRequest reguestParams, Handler msgHandler) {
        this.mServerIpPreference = prefs.getIFMAPServerIpPreference();
        this.mServerPort = prefs.getIFMAPServerPortPreference();
        this.mIpAddress = ipAddress;
        this.mMessageType = messageType;
        this.mReguestParamsPublish = reguestParams;
        this.mMsgHandler = msgHandler;
    }


    /**
     * @return the mMessageType
     */
    public byte getmMessageType() {
        return mMessageType;
    }

    /**
     * @return the mServerPort
     */
    public String getmServerPort() {
        return mServerPort;
    }

    /**
     * @return the mServerIpPreference
     */
    public String getmServerIpPreference() {
        return mServerIpPreference;
    }

    /**
     * @return the mIpAddress
     */
    public String getmIpAddress() {
        return mIpAddress;
    }

    /**
     * @return the mReguestParamsPublish
     */
    public PublishRequest getmReguestParamsPublish() {
        return mReguestParamsPublish;
    }

    /**
     * @return the mMsgHandler
     */
    public Handler getmMsgHandler() {
        return mMsgHandler;
    }

}

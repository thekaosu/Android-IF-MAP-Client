/*
 * MessageParameter..java          0.3 2015-03-08
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
package de.simu.decoit.android.decomap.messaging;

import android.content.Context;

import java.util.Date;
import java.util.Vector;

import de.simu.decoit.android.decomap.activities.StatusActivity;
import de.simu.decoit.android.decomap.observer.sms.SMSObserver;

/**
 * Storing some phone status information
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.3
 */
public class MessageParameter {

    private String currentBatteryLevel;
    private Date lastPictureTakenDate = null;

    // global sms-information
    private int smsSentCount = 0;
    private int smsInCount = 0;
    private Date lastSendDate;
    private final Vector<SMSObserver.SmsInfos> outgoingSms = new Vector<>();
    private final Vector<SMSObserver.SmsInfos> incomingSms = new Vector<>();

    // fields for location-tracking-values, declared static so that
    // the status-activity can access them
    private String latitude = null;
    private String longitude = null;
    private String altitude = null;


    private static MessageParameter instance;

    /**
     * Private singelton constructor
     */
    private MessageParameter() {
    }


    /**
     * Singelton instance getter
     *
     * @return Singelton instance of Messageparameter
     */
    public static synchronized MessageParameter getInstance() {
        if (instance == null) {
            instance = new MessageParameter();
        }
        return instance;
    }

    /**
     * Lets set the current position in the status tab if it is already active
     *
     * @param latitude  current latitude value
     * @param longitude current longitude value
     * @param altitude  current altitude value
     */
    public void setCurrentLocation(Context context, double latitude, double longitude,
                                   double altitude) {
        this.latitude = String.valueOf(latitude);
        this.longitude = String.valueOf(longitude);
        this.altitude = String.valueOf(altitude);

        // if status activity is active, pass current location values to it
        if (StatusActivity.sIsActivityActive) {
            StatusActivity.setCurrentLocation(context, latitude, longitude, altitude);
        }
    }

    /**
     * reseting current location
     */
    public void resetCurrentLocation(){
        latitude = null;
        longitude = null;
        altitude = null;
    }

    /**
     * removes all SmsInfos. This should be called after the respective
     * features have been created and sent to the MAPS.
     */
    public void resetSmsInfos() {
        incomingSms.clear();
        outgoingSms.clear();
    }

    public String getLongitude() {
        return longitude;
    }

    public String getAltitude() {
        return altitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public Vector<SMSObserver.SmsInfos> getIncomingSms() {
        return incomingSms;
    }

    public Vector<SMSObserver.SmsInfos> getOutgoingSms() {
        return outgoingSms;
    }

    public Date getLastSendDate() {
        return lastSendDate;
    }

    public void setLastSendDate(Date lastSendDate) {
        this.lastSendDate = lastSendDate;
    }

    public int getSmsInCount() {
        return smsInCount;
    }

    public void setSmsInCount(int smsInCount) {
        this.smsInCount = smsInCount;
    }

    public int getSmsSentCount() {
        return smsSentCount;
    }

    public void setSmsSentCount(int smsSentCount) {
        this.smsSentCount = smsSentCount;
    }

    public Date getLastPictureTakenDate() {
        return lastPictureTakenDate;
    }

    public void setLastPictureTakenDate(Date lastPictureTakenDate) {
        this.lastPictureTakenDate = lastPictureTakenDate;
    }

    public String getCurrentBatteryLevel() {
        return currentBatteryLevel;
    }

    public void setCurrentBatteryLevel(String currentBatteryLevel) {
        this.currentBatteryLevel = currentBatteryLevel;
    }
}

/* 
 * 
 * Toolbox.java
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
package de.simu.decoit.android.decomap.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.StringWriter;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import de.hshannover.f4.trust.ifmapj.messages.PublishRequest;
import de.simu.decoit.android.decomap.activities.MainActivity;
import de.simu.decoit.android.decomap.activities.R;
import de.simu.decoit.android.decomap.activities.TabLayout;
import de.simu.decoit.android.decomap.messaging.MessageHandler;
import de.simu.decoit.android.decomap.messaging.ReadOutMessages;
import de.simu.decoit.android.decomap.preferences.PreferencesValues;


/**
 * Class for providing several Helper-Methods
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @version 0.2
 */
public class Toolbox {

    private static NotificationManager mNotificationManager = null;
    private static final int SIMPLE_NOTFICATION_ID = 1;
    public static boolean sLogFolderExists = false;
    public static final String CONTENT_SMS = "content://sms";

    /**
     * IPv4 Regex.-pattern
     */
    private final static String REGEX_IP4 = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    public static Pattern getIpPattern() {
        return Pattern.compile(REGEX_IP4);
    }

    /**
     * current time/date as string
     * format string
     */
    public static final String DATE_FORMAT_NOW_DEFAULT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_NOW_EXPORT = "yyyy-MM-dd_HH-mm-ss";

    public static String now(String formatString) {
        if (formatString.equals(DATE_FORMAT_NOW_DEFAULT) || formatString.equals(DATE_FORMAT_NOW_EXPORT)) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW_EXPORT, Locale.getDefault());
            return sdf.format(cal.getTime());
        }
        return null;
    }

    /**
     * output log message
     *
     * @param tag      tag of message
     * @param activity message to log
     */
    public static void logTxt(String tag, String activity) {
        Log.d(tag, activity);

        // log to file
        if (PreferencesValues.getInstance().isApplicationFileLogging()) {
            CustomLogger.logTxt(tag, activity);
        }
    }

    /**
     * generate log message from passed in PublishRequest
     *
     * @param msgType    byte indicating message-type
     * @param publishReq PublishRequest to generate log-message from
     * @return String containing generated log message
     */
    public static String generateRequestLogMessageFromPublishRequest(byte msgType, PublishRequest publishReq) {
        // get logging message from publish-request-object
        if (msgType != MessageHandler.MSG_TYPE_REQUEST_RENEWSESSION && msgType != MessageHandler.MSG_TYPE_REQUEST_NEWSESSION
                && msgType != MessageHandler.MSG_TYPE_REQUEST_ENDSESSION) {
            String result = "";
            ArrayList<HashMap<String, String>> requestStringList = ReadOutMessages.readOutRequest(publishReq);
            for (int i = 0; i < requestStringList.size(); i++) {
                if (requestStringList.get(i).toString() != null || !requestStringList.get(i).toString().equals("")) {
                    result = requestStringList.get(i).toString();
                }
            }
            return result.replace("{", "").replace("}", "").replace(", ", "\n");
        } else if (msgType == MessageHandler.MSG_TYPE_REQUEST_NEWSESSION) {
            return "new-session request";
        } else if (msgType == MessageHandler.MSG_TYPE_REQUEST_RENEWSESSION) {
            return "renew-session request";
        } else {
            return "end-session request";
        }
    }

    /**
     * show passed in message as notification
     *
     * @param displayTitle displayed title
     * @param displayText  displayed text
     * @param appContext   context
     */
    public static void showNotification(String displayTitle,
                                        String displayText, Context appContext) {

        // initialize notification manager
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) appContext
                    .getSystemService(MainActivity.NOTIFICATION_SERVICE);
        }

        // set notification about incoming response
        PendingIntent intent = PendingIntent.getActivity(appContext,
                SIMPLE_NOTFICATION_ID,
                (new Intent()).setClass(appContext, TabLayout.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // using compat as easy way to stay compatible
        Notification mNotification = new NotificationCompat.Builder(appContext)
                .setContentTitle(displayTitle).setContentText(displayText)
                .setContentIntent(intent).setSmallIcon(R.drawable.icon).build();

        mNotificationManager.notify(SIMPLE_NOTFICATION_ID, mNotification);
    }

    /**
     * delete last notification messagn
     */
    public static void cancelNotification() {
        // delete last notification message when application is shut down
        if (mNotificationManager != null) {
            mNotificationManager.cancel(SIMPLE_NOTFICATION_ID);
        }
    }

    /**
     * check if folder at passed in path exists, if not create folder
     *
     * @param path folder-path
     * @return true if folder exists or has been created
     */
    public static boolean createDirIfNotExists(String path) {
        boolean ret = true;
        File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                ret = false;
            }
        }
        return ret;
    }

    /**
     * JSONify Object to string
     *
     * @param input object to JSONify
     * @return JSON string representation of input
     */

    public static String toJSON(Object input) {
        StringWriter mJSON = new StringWriter();
        ObjectMapper objectMapper = new ObjectMapper();

        // generate JSON object
        try {
            objectMapper.writeValue(mJSON, input);
        } catch (Exception e) {
            logTxt("error", "Error while parsing JSON. Error Message: " + e);
        }

        return mJSON.toString();
    }

    /**
     * Returns MAC address of the given interface name.
     *
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return mac address or empty string
     */
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface netInterf : interfaces) {
                if (interfaceName != null) {
                    if (!netInterf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = netInterf.getHardwareAddress();

                if (mac == null) {
                    return "UNKNOWN EMULATOR";
                }
                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception ex) {
            return "UNKNOWN EMULATOR";
        }
        return "UNKNOWN EMULATOR";
    }
}

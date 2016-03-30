/* 
 * LogMessageHelper.java        0.2 2015-03-08
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
package de.simu.decoit.android.decomap.logging;

import java.sql.Timestamp;
import java.util.Date;

import de.simu.decoit.android.decomap.database.LoggingDatabase;
import de.simu.decoit.android.decomap.messaging.MessageHandler;
import de.simu.decoit.android.decomap.messaging.ResponseParameters;
import de.simu.decoit.android.decomap.preferences.PreferencesValues;

/**
 * class for logging if-map-messages
 *
 * @author Dennis Dunekacke, DECOIT GmbH
 * @version 0.2
 */
public class LogMessageHelper {

    // singleton
    static private LogMessageHelper _instance;

    private LogMessageHelper() {
        // create new database connection
    }

    /**
     * get an instance of the logger
     *
     * @return Logger
     */
    static public LogMessageHelper getInstance() {
        if (_instance == null) {
            _instance = new LogMessageHelper();
        }
        return _instance;
    }

    /**
     * generate response log message
     *
     * @param msgType  type of response
     * @param params   response parameters
     * @param clientIP ip-address of client
     * @return resulting log message
     */
    public LogMessage generateResponseLogMessage(byte msgType, ResponseParameters params, String clientIP) {
        // generate time stamp
        Timestamp tStamp = new Timestamp(new Date().getTime());

        // get message type-string and assign message
        String msgtype;
        switch (msgType) {
            case MessageHandler.MSG_TYPE_REQUEST_ENDSESSION:
                msgtype = "END SESSION RESPONSE";
                break;
            case MessageHandler.MSG_TYPE_REQUEST_NEWSESSION:
                msgtype = "NEWS SESSION RESPONSE";
                break;
            case MessageHandler.MSG_TYPE_PUBLISH_CHARACTERISTICS:
                msgtype = "PUBLISH CHARACTERISTICS RESPONSE";
                break;
            case MessageHandler.MSG_TYPE_REQUEST_RENEWSESSION:
                msgtype = "RENEW SESSION RESPONSE";
                break;
            case MessageHandler.MSG_TYPE_METADATA_UPDATE:
                msgtype = "METADATA AUTO-UPDATE RESPONSE";
                break;
            case MessageHandler.MSG_TYPE_INVALID_RESPONSE:
                msgtype = "INVALID SERVER RESPONSE";
                break;
            case MessageHandler.MSG_TYPE_ERRORMSG:
                msgtype = "ERROR MESSAGE";
                break;
            default:
                msgtype = "UNKNOWN MSG TYPE!";
                break;
        }

        // assign message content
        String msg = params.getParameter(ResponseParameters.RESPONSE_PARAMS_MSGCONTENT);

        // get client/server infos
        String statusMsg = params.getParameter(ResponseParameters.RESPONSE_PARAMS_STATUSMSG);
        String targetPort = "";
        String target = clientIP + ":" + targetPort;

        // create new log message
        return new LogMessage(tStamp.toString(), msg, msgtype, target, statusMsg);
    }

    /**
     * generate request log message
     *
     * @param msgType   type of request
     * @param reqMsg    message to be logged
     * @param serverIp  ip-address of server
     * @param serverPrt port of server
     * @return resulting log message
     */
    public LogMessage generateRequestLogMessage(byte msgType, String reqMsg, String serverIp, String serverPrt) {
        // generate time stamp
        Timestamp tStamp = new Timestamp(new Date().getTime());

        // get message type-string and assign message
        String msgtype;
        String msg;

        switch (msgType) {
            case MessageHandler.MSG_TYPE_REQUEST_ENDSESSION:
                msgtype = "END SESSION REQUEST";
                break;
            case MessageHandler.MSG_TYPE_REQUEST_NEWSESSION:
                msgtype = "NEWS SESSION REQUEST";
                break;
            case MessageHandler.MSG_TYPE_PUBLISH_CHARACTERISTICS:
                msgtype = "PUBLISH CHARACTERISTICS REQUEST";
                break;
            case MessageHandler.MSG_TYPE_REQUEST_RENEWSESSION:
                msgtype = "RENEW SESSION REQUEST";
                break;
            case MessageHandler.MSG_TYPE_METADATA_UPDATE:
                msgtype = "METADATA AUTO-UPDATE";
                break;
            default:
                msgtype = "UNKNOWN MESSAGE TYPE!";
                break;
        }
        msg = reqMsg;
        // get server infos
        String status = "Request Created";

        String target = serverIp + ":" + serverPrt;
        // create new log message
        return new LogMessage(tStamp.toString(), msg, msgtype, target, status);
    }

    /**
     * add collected Log Messages from Request/Response to Log-Message-List
     *
     * @param responseType type of the responded message
     * @param requestMsg message of the request
     * @param responseMsg responded message
     * @param prefs preferences
     * @param db database
     */
    public void logMessage(byte responseType, LogMessage requestMsg, LogMessage responseMsg, PreferencesValues prefs,
                           LoggingDatabase db) {
        logMessage(responseType, requestMsg, prefs, db);
        logMessage(responseType, responseMsg, prefs, db);
    }

    public void logMessage(byte responseType, LogMessage logMsg, PreferencesValues prefs,
                           LoggingDatabase db) {
        if ((responseType == MessageHandler.MSG_TYPE_REQUEST_NEWSESSION && prefs.isEnableNewAndEndSessionLog())
                || (responseType == MessageHandler.MSG_TYPE_REQUEST_ENDSESSION && prefs.isEnableNewAndEndSessionLog())) {
            db.insertMessage(db.getWritableDatabase(), logMsg);
        } else if (responseType == MessageHandler.MSG_TYPE_REQUEST_RENEWSESSION && prefs.isEnableRenewRequestLog()) {
            db.insertMessage(db.getWritableDatabase(), logMsg);
        } else if (responseType == MessageHandler.MSG_TYPE_METADATA_UPDATE && prefs.isEnableLocationTrackingLog()) {
            db.insertMessage(db.getWritableDatabase(), logMsg);
        } else if (responseType == MessageHandler.MSG_TYPE_PUBLISH_CHARACTERISTICS
                && prefs.isEnablePublishCharacteristicsLog()) {
            db.insertMessage(db.getWritableDatabase(), logMsg);
        } else if (responseType == MessageHandler.MSG_TYPE_ERRORMSG && prefs.isEnableErrorMessageLog()) {
            db.insertMessage(db.getWritableDatabase(), logMsg);
        } else if (responseType == MessageHandler.MSG_TYPE_INVALID_RESPONSE && prefs.isEnableInvalideResponseLog()) {
            db.insertMessage(db.getWritableDatabase(), logMsg);
        }
    }
}
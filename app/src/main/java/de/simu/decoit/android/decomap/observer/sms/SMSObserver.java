/* 
 * SMSObserver..java          0.3 2015-03-08
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
package de.simu.decoit.android.decomap.observer.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.util.Date;

import de.simu.decoit.android.decomap.messaging.MessageParameter;
import de.simu.decoit.android.decomap.util.CryptoUtil;
import de.simu.decoit.android.decomap.util.Toolbox;

// http://gbandroid.googlecode.com/svn-history/r46/trunk/MobileSpy/src/org/ddth/android/monitor/observer/AndroidSmsWatcher.java
// http://stackoverflow.com/questions/7012703/android-detect-sms-outgoing-incorrect-count

/**
 * Class to Observe SMS-Communication (incoming and outgoing)
 *
 * @author Dennis Dunekacke, DECOIT GmbH
 * @version 0.3
 */
public class SMSObserver {

    // intent for received-sms-event
    private final String ACTION_RECEIVE_SMS = "android.provider.Telephony.SMS_RECEIVED";

    private final MessageParameter mp = MessageParameter.getInstance();

    // application context
    private final Context mAppContext;

    // outgoing sms-observer
    private ContentObserver mSmsSentObserver;

    // incoming sms-receiver
    private SMSBroadcastReceiver mSMSBroadcastReceiver;

    /**
     * constructor
     *
     * @param context Application-Context
     */
    public SMSObserver(Context context) {
        mAppContext = context;
    }

    /**
     * register an receiver for listening to incoming sms messages
     */
    public void registerReceivedSmsBroadcastReceiver() {
        if (mSMSBroadcastReceiver != null) {
            return;
        }
        final IntentFilter intentFilter = new IntentFilter(ACTION_RECEIVE_SMS);
        mSMSBroadcastReceiver = new SMSBroadcastReceiver();
        mAppContext.registerReceiver(mSMSBroadcastReceiver, intentFilter);

    }

    /**
     * register an observer for listening to outgoing sms messages
     */
    public void registerSentSmsContentObserver() {
        if (mSmsSentObserver != null) {
            return;
        }

        mSmsSentObserver = new ContentObserver(null) {
            public void onChange(boolean selfChange) {
                // check for sent smd-message
                if (checkForSentSms(mAppContext)) {
                    // increase sms-sent count
                    mp.setSmsSentCount(mp.getSmsSentCount() + 1);
                }
            }
        };

        // register new sms-outgoing-observer
        mAppContext.getContentResolver().registerContentObserver(
                Uri.parse(Toolbox.CONTENT_SMS), true, mSmsSentObserver);
    }

    /**
     * check if a new sms-message has been send if true, additionally set the
     * "last sent"-Date
     *
     * @param context Application-Context
     * @return true, if a new sms has been sent
     */
    private boolean checkForSentSms(Context context) {
        Cursor cursor = context.getContentResolver().query(
                Uri.parse(Toolbox.CONTENT_SMS), null, null, null, null);
        boolean isNewSmsSent = false;
        if (cursor != null) {
            if (cursor.moveToNext()) {
                // check if sms is outgoing and if it was sent successfully
                String protocol = cursor.getString(cursor
                        .getColumnIndex("protocol"));
                int type = cursor.getInt(cursor.getColumnIndex("type"));
                if (protocol != null || type != 2) {
                    // no new message...
                    return false;
                }

                // update last-sent-date
                int dateColumn = cursor.getColumnIndex("date");
                mp.setLastSendDate(new Date(cursor.getLong(dateColumn)));

                isNewSmsSent = true;

                // remember the sent message
                int addressColumn = cursor.getColumnIndex("address");
                String address = cursor.getString(addressColumn);
                address = CryptoUtil.sha256(address).substring(0, 8);
                mp.getOutgoingSms().add(new SmsInfos(mp.getLastSendDate(), address));
            }
            cursor.close();
        }
        return isNewSmsSent;
    }

    /**
     * broadcast receiver for incoming sms-messages
     *
     * @author Dennis Dunekacke, DECOIT GmbH
     * @version 0.1
     */
    private class SMSBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context ctxt, Intent intent) {
            mp.setSmsInCount(mp.getSmsInCount()+1);

            // remember the incoming message
            if (Build.VERSION.SDK_INT >= 19) {
                SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);

                for (final SmsMessage message : msgs) {
                    final Date smsdate = new Date(message.getTimestampMillis());
                    final String address = CryptoUtil.sha256(
                            message.getOriginatingAddress()).substring(0, 8);

                    mp.getIncomingSms().add(new SmsInfos(smsdate, address));
                }

            } else {
                final Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object pdus[] = (Object[]) bundle.get("pdus");
                    if (pdus != null) {
                        for (final Object pdu : pdus) {
                            @SuppressWarnings("deprecation")
                            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                            final Date smsdate = new Date(smsMessage.getTimestampMillis());
                            final String address = CryptoUtil.sha256(
                                    smsMessage.getOriginatingAddress()).substring(0, 8);

                            mp.getIncomingSms().add(new SmsInfos(smsdate, address));
                        }
                    }
                }
            }
        }
    }

    /**
     * encapsulates information on incoming and outgoing SMS
     *
     * @author ib
     */
    public class SmsInfos {
        private final Date mDate;
        private final String mAddress;

        public Date getDate() {
            return mDate;
        }

        public String getAddress() {
            return mAddress;
        }


        private SmsInfos(Date date, String address) {
            this.mDate = date;
            this.mAddress = address;
        }

    }
}

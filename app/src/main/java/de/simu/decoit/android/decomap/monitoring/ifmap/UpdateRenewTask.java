/*
 * UpdateRenewTask..java          0.3 2015-03-08
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

import de.simu.decoit.android.decomap.messaging.MessageHandler;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * UpdateRenewTask which renew the session
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Marcel Jahnke, Decoit GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.3
 */
class UpdateRenewTask implements Runnable {

    private final IFMapMonitoring monitor;
    private final Handler renewHandler;

    public UpdateRenewTask(IFMapMonitoring monitor, Handler renewHandler) {
        this.monitor = monitor;
        this.renewHandler = renewHandler;
    }

    @Override
    public void run() {
            sendRenewSessionToServer();
            renewHandler.postDelayed(this,
                    monitor.mPreferences.getRenewIntervalPreference());
    }

    /**
     * Triggers the sending of the renew-session message
     */
    private void sendRenewSessionToServer() {
        Toolbox.logTxt(this.getClass().getName(), "sendRenewSession(...) called");
        monitor.mMessageType = MessageHandler.MSG_TYPE_REQUEST_RENEWSESSION;
        if(!monitor.mIsBound) {
            monitor.startIFMAPConnectionService();
        }
    }
}

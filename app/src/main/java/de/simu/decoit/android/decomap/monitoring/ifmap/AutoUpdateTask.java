/*
 * AutoUpdateTask.java        0.2 2015-03-08
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

/**
 * AutoUpdateTask which update metadata
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Marcel Jahnke, Decoit GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
class AutoUpdateTask implements Runnable{

    private final IFMapMonitoring monitor;
    private final Handler mUpdateHandler;

    /**
     * constructor
     *
     * @param monitor IFMapMonitor monitor instance
     * @param mUpdateHandler Updatehandler for metadata updates!
     */
    public AutoUpdateTask(IFMapMonitoring monitor, Handler mUpdateHandler) {
        this.monitor = monitor;
        this.mUpdateHandler = mUpdateHandler;
    }

    @Override
    public void run() {
        if (monitor.mPreferences.isAutoUpdate()) {
            sendMetadataUpdateToServer();
            mUpdateHandler.postDelayed(this,
                    monitor.mPreferences.getUpdateInterval());
        }
    }


    /**
     * Triggers the sending of device characteristics and location-metadata, if
     * a connection to the server is established
     */
    private void sendMetadataUpdateToServer() {
        if (monitor.isConnected()) {
            monitor.mMessageType = MessageHandler.MSG_TYPE_METADATA_UPDATE;
            monitor.startIFMAPConnectionService();
        }
    }
}

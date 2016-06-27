/*
 * PermanentRunnable..java          0.3 2015-03-08
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

import de.simu.decoit.android.decomap.services.binder.UnbinderClass;

/**
 * Callback-Handler for Local Service that handles synchronous
 * Server-Responses (permanent-connection-method)
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Marcel Jahnke, Decoit GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.3
 */
class PermanentRunnable extends ResponseRunnable {

    /**
     * constructor
     *
     * @param monitor IFMapMonitor
     */
    public PermanentRunnable(IFMapMonitoring monitor) {
        super(monitor);
    }

    @Override
    public void run() {
        // process server-result
        processSRCResponseParameters(responseType, msg, logRequestMsg,
                logResponseMsg);
        monitor.mResponseType = responseType;

        // unbind service
        if (IFMapMonitoring.sBoundPermConnService != null) {
            monitor.mIsBound = UnbinderClass.doUnbindConnectionService(
                    monitor.activity.getApplicationContext(), monitor.mConnection,
                    monitor.myProgressDialog, monitor.mIsBound);
            IFMapMonitoring.sBoundPermConnService = null;
        }
    }

}

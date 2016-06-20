/*
 * PermanentRunnable.java        0.2 2015-03-08
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
package de.simu.decoit.android.decomap.monitoring;

import android.view.View;

/**
 * Interface for different monitoring modes
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Marcel Jahnke, Decoit GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public interface MonitoringInterface {

    /**
     * Handling Button pressing
     *
     * @param view view with buttons
     */
    void mainTabButtonHandler(View view);

    /**
     * should be called to clear resources
     */
    void onDestroy();

    /**
     * should be called if autoconnection is active
     */
    void autoConnection();

    /**
     * returns if there is a connection established
     *
     * @return is a connection established
     */
    boolean isConnected();
}

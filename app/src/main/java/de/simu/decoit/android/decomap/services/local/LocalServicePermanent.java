/*
 * LocalServicePermanent..java          0.3 2015-03-08
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

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import de.simu.decoit.android.decomap.monitoring.ifmap.IFMapMonitoring;
import de.simu.decoit.android.decomap.monitoring.ifmap.ResponseRunnable;
import de.simu.decoit.android.decomap.services.PermanentConnectionService;

/**
 * This class is used for the outsourcing of the instantiation of
 * mPermConnection variable and his overriden methods
 * 
 * @author  Marcel Jahnke, DECOIT GmbH
 * @author  Dennis Dunekacke, Decoit GmbH
 * @version 0.3
 */
public class LocalServicePermanent {

	/**
	 * get local service for synchronous communication using the
	 * "permanent-session" approach
	 * 
	 * @return bound local service
	 */
	public static ServiceConnection getPermConnection(final LocalServiceParameters values,
													  final ResponseRunnable callbackHandler, final String msgContext) {
		return new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				IFMapMonitoring.sBoundPermConnService = (PermanentConnectionService.LocalBinder) service;
				IFMapMonitoring.sBoundPermConnService.setActivityCallbackHandler(values.getmMsgHandler());
				IFMapMonitoring.sBoundPermConnService.setRunnable(callbackHandler);

				// connect to local service
				IFMapMonitoring.sBoundPermConnService
						.connect(values.getmServerIpPreference(), values.getmServerPort(), values.getmIpAddress(),
								values.getmMessageType(), values.getmReguestParamsPublish(), msgContext);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				IFMapMonitoring.sBoundPermConnService.setRunnable(null);
				IFMapMonitoring.sBoundPermConnService = null;
			}
		};
	}
}

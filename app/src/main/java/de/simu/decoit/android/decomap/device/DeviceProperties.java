/* 
 * DeviceProperties..java          0.3 2015-03-08
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

package de.simu.decoit.android.decomap.device;

import android.content.Context;
import android.os.Build;

import java.lang.reflect.Constructor;

import de.simu.decoit.android.decomap.device.application.ApplicationProperties;
import de.simu.decoit.android.decomap.device.phone.PhoneProperties;
import de.simu.decoit.android.decomap.device.system.SystemProperties;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * class for reading out device properties
 * 
 * @author Dennis Dunekacke, DECOIT GmbH
 * @author Marcel Jahnke, DECOIT GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @version 0.1.4.2
 */
public class DeviceProperties {

    private final ApplicationProperties appProperties;
    private final SystemProperties systemProperties;
    private PhoneProperties phoneProperties;

    /**
     * constructor
     * 
     * @param appContext
     *            current application context
     */
    public DeviceProperties(Context appContext) {
        appProperties = new ApplicationProperties(appContext);
        systemProperties = new SystemProperties(appContext);

        /*
         * some calls inside phone-properties class differs in some versions of android, so we use reflection to get the required class
         */

        int sdkVersion = Build.VERSION.SDK_INT;
        String className;
        if (sdkVersion < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            className = "de.simu.decoit.android.decomap.device.phone.PhonePropertiesLegacy";
        } else {
            className = "de.simu.decoit.android.decomap.device.phone.PhonePropertiesLatest";
        }

        /*
         * Find the required class by name and instantiate it.
         */
        try {
            Class<?> myClass = Class.forName(className);

            Constructor<?> constructor = myClass.getConstructor(Context.class);
            phoneProperties = (PhoneProperties) constructor.newInstance(appContext);
        } catch (Exception e) {
            Toolbox.logTxt(this.getClass().getName(),
                    "error while loading PhoneProperties-Implementation using reflection: " + e);
        }
    }

    /**
     * get application properties-object
     * 
     * @return application-properties-object
     */
    public ApplicationProperties getApplicationProperties() {
        return this.appProperties;
    }

    /**
     * get system properties-object
     * 
     * @return system-properties-object
     */
    public SystemProperties getSystemProperties() {
        return this.systemProperties;
    }

    /**
     * get phone properties-object
     * 
     * @return phone-properties-object
     */
    public PhoneProperties getPhoneProperties() {
        return this.phoneProperties;
    }
}

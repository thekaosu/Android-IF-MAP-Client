/*
 * PreferencesValues.java       0.2 2015-03-08
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

package de.simu.decoit.android.decomap.preferences;

import java.util.HashMap;

/**
 * Object for holding Preferences
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class PreferencesValues {

    private boolean lockPreferences = false;

    // monitoring mode
    private String monitoringPreference = null;

    // location tracking
    private String locationTrackingType = null;
    private boolean enableLocationTracking = false;

    // auto update
    private boolean autoUpdate = false;
    private long updateInterval = 60000;


    // path for files
    private String logPath;
    private String keystorePath;
    private String keystorePassword;

    // application-settings
    private boolean useNonConformMetadata;
    private boolean dontSendApplicationsInfos;
    private boolean dontSendGoogleApps;
    private boolean autostart;
    private boolean autoconnect;

    // server-settings
    private String iFMAPServerIpPreference;
    private String iFMAPServerPortPreference;

    private String iMonitorServerIpPreference;
    private String iMonitorServerPortPreference;

    // imonitor-settings
    private String nscaPassPreference;
    private String nscaEncPreference;

    // user-settings
    private boolean useBasicAuth;
    private String usernamePreference;
    private String passwordPreference;

    // connection-settings
    private boolean allowUnsafeSSLPreference;
    private boolean isPermantConnection;
    private Long renewIntervalPreference;
    private long renewRequestMinInterval = 10000; // default minimum

    // logging-settings
    private boolean applicationFileLogging = false;
    private boolean enableNewAndEndSessionLog;
    private boolean enableSubscribe;
    private boolean enablePollLog;

    private boolean enableLocationTrackingLog;
    private boolean enablePublishCharacteristicsLog;
    private boolean enableErrorMessageLog;
    private boolean enableInvalideResponseLog;
    private boolean enableRenewRequestLog;

    //Session values
    private final HashMap<String, Boolean> camActiv = new HashMap<String, Boolean>();

    private static PreferencesValues instance;

    /**
     * Private singelton constructor
     */
    private PreferencesValues() {
    }


    public static synchronized PreferencesValues getInstance() {
        if (instance == null) {
            instance = new PreferencesValues();
        }
        return instance;
    }

    public boolean isLockPreferences() {
        return lockPreferences;
    }

    public void setLockPreferences(boolean lockPreferences) {
        this.lockPreferences = lockPreferences;
    }

    public String getMonitoringPreference() {
        return monitoringPreference;
    }

    public void setMonitoringPreference(String monitoringPreference) {
        this.monitoringPreference = monitoringPreference;
    }

    public String getLocationTrackingType() {
        return locationTrackingType;
    }

    public void setLocationTrackingType(String locationTrackingType) {
        this.locationTrackingType = locationTrackingType;
    }

    public boolean isEnableLocationTracking() {
        return enableLocationTracking;
    }

    public void setEnableLocationTracking(boolean enableLocationTracking) {
        this.enableLocationTracking = enableLocationTracking;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(long updateInterval) {
        this.updateInterval = updateInterval;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    public boolean isUseNonConformMetadata() {
        return useNonConformMetadata;
    }

    public void setUseNonConformMetadata(boolean useNonConformMetadata) {
        this.useNonConformMetadata = useNonConformMetadata;
    }

    public boolean isDontSendApplicationsInfos() {
        return dontSendApplicationsInfos;
    }

    public void setDontSendApplicationsInfos(boolean dontSendApplicationsInfos) {
        this.dontSendApplicationsInfos = dontSendApplicationsInfos;
    }

    public boolean isDontSendGoogleApps() {
        return dontSendGoogleApps;
    }

    public void setDontSendGoogleApps(boolean dontSendGoogleApps) {
        this.dontSendGoogleApps = dontSendGoogleApps;
    }

    public boolean isAutostart() {
        return autostart;
    }

    public void setAutostart(boolean autostart) {
        this.autostart = autostart;
    }

    public boolean isAutoconnect() {
        return autoconnect;
    }

    public void setAutoconnect(boolean autoconnect) {
        this.autoconnect = autoconnect;
    }

    public String getIFMAPServerIpPreference() {
        return iFMAPServerIpPreference;
    }

    public void setIFMAPServerIpPreference(String iFMAPServerIpPreference) {
        this.iFMAPServerIpPreference = iFMAPServerIpPreference;
    }

    public String getIFMAPServerPortPreference() {
        return iFMAPServerPortPreference;
    }

    public void setIFMAPServerPortPreference(String iFMAPServerPortPreference) {
        this.iFMAPServerPortPreference = iFMAPServerPortPreference;
    }

    public String getIMonitorServerIpPreference() {
        return iMonitorServerIpPreference;
    }

    public void setIMonitorServerIpPreference(String iMonitorServerIpPreference) {
        this.iMonitorServerIpPreference = iMonitorServerIpPreference;
    }

    public String getIMonitorServerPortPreference() {
        return iMonitorServerPortPreference;
    }

    public void setIMonitorServerPortPreference(String iMonitorServerPortPreference) {
        this.iMonitorServerPortPreference = iMonitorServerPortPreference;
    }

    public String getNscaPassPreference() {
        return nscaPassPreference;
    }

    public void setNscaPassPreference(String nscaPassPreference) {
        this.nscaPassPreference = nscaPassPreference;
    }

    public String getNscaEncPreference() {
        return nscaEncPreference;
    }

    public void setNscaEncPreference(String nscaEncPreference) {
        this.nscaEncPreference = nscaEncPreference;
    }

    public boolean isUseBasicAuth() {
        return useBasicAuth;
    }

    public void setUseBasicAuth(boolean useBasicAuth) {
        this.useBasicAuth = useBasicAuth;
    }

    public String getUsernamePreference() {
        return usernamePreference;
    }

    public void setUsernamePreference(String usernamePreference) {
        this.usernamePreference = usernamePreference;
    }

    public String getPasswordPreference() {
        return passwordPreference;
    }

    public void setPasswordPreference(String passwordPreference) {
        this.passwordPreference = passwordPreference;
    }

    public boolean isAllowUnsafeSSLPreference() {
        return allowUnsafeSSLPreference;
    }

    public void setAllowUnsafeSSLPreference(boolean allowUnsafeSSLPreference) {
        this.allowUnsafeSSLPreference = allowUnsafeSSLPreference;
    }

    public boolean isPermantConnection() {
        return isPermantConnection;
    }

    public void setIsPermantConnection(boolean isPermantConnection) {
        this.isPermantConnection = isPermantConnection;
    }

    public Long getRenewIntervalPreference() {
        return renewIntervalPreference;
    }

    public void setRenewIntervalPreference(Long renewIntervalPreference) {
        this.renewIntervalPreference = renewIntervalPreference;
    }

    public long getRenewRequestMinInterval() {
        return renewRequestMinInterval;
    }

    public void setRenewRequestMinInterval(long renewRequestMinInterval) {
        this.renewRequestMinInterval = renewRequestMinInterval;
    }

    public boolean isApplicationFileLogging() {
        return applicationFileLogging;
    }

    public void setApplicationFileLogging(boolean applicationFileLogging) {
        this.applicationFileLogging = applicationFileLogging;
    }

    public boolean isEnableNewAndEndSessionLog() {
        return enableNewAndEndSessionLog;
    }

    public void setEnableNewAndEndSessionLog(boolean enableNewAndEndSessionLog) {
        this.enableNewAndEndSessionLog = enableNewAndEndSessionLog;
    }

    public boolean isEnableSubscribe() {
        return enableSubscribe;
    }

    public void setEnableSubscribe(boolean enableSubscribe) {
        this.enableSubscribe = enableSubscribe;
    }

    public boolean isEnablePollLog() {
        return enablePollLog;
    }

    public void setEnablePollLog(boolean enablePollLog) {
        this.enablePollLog = enablePollLog;
    }

    public boolean isEnableLocationTrackingLog() {
        return enableLocationTrackingLog;
    }

    public void setEnableLocationTrackingLog(boolean enableLocationTrackingLog) {
        this.enableLocationTrackingLog = enableLocationTrackingLog;
    }

    public boolean isEnablePublishCharacteristicsLog() {
        return enablePublishCharacteristicsLog;
    }

    public void setEnablePublishCharacteristicsLog(boolean enablePublishCharacteristicsLog) {
        this.enablePublishCharacteristicsLog = enablePublishCharacteristicsLog;
    }

    public boolean isEnableErrorMessageLog() {
        return enableErrorMessageLog;
    }

    public void setEnableErrorMessageLog(boolean enableErrorMessageLog) {
        this.enableErrorMessageLog = enableErrorMessageLog;
    }

    public boolean isEnableInvalideResponseLog() {
        return enableInvalideResponseLog;
    }

    public void setEnableInvalideResponseLog(boolean enableInvalideResponseLog) {
        this.enableInvalideResponseLog = enableInvalideResponseLog;
    }

    public boolean isEnableRenewRequestLog() {
        return enableRenewRequestLog;
    }

    public void setEnableRenewRequestLog(boolean enableRenewRequestLog) {
        this.enableRenewRequestLog = enableRenewRequestLog;
    }

    public void setCamActiv(String camId, boolean isCamActiv) {
        camActiv.put(camId, isCamActiv);
    }

    public boolean isCamActiv(String camId) {
        return camActiv.get(camId);
    }

    public boolean isAnyCamActiv(){
        for(String key : camActiv.keySet()){
            if(camActiv.get(key)){
                return true;
            }
        }
        return false;
    }
}

package de.simu.decoit.android.decomap.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import de.simu.decoit.android.decomap.activities.R;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * Initializer for Preferences
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class PreferenceInitializer {

    private static final PreferencesValues mPreferences = PreferencesValues.getInstance();

    /**
     * init application preferences
     */
    public static void initPreferences(Context baseContext) {
        Toolbox.logTxt(PreferenceInitializer.class.toString(), "onPreferences(...) called");

        // get the preferences.xml preferences
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(baseContext);

        // set preferences
        mPreferences.setApplicationFileLogging(prefs.getBoolean(
                "applicatiologging", false));
        mPreferences.setMonitoringPreference(prefs.getString(
                R.id.monitoringModeSettings + "", "IF-MAP"));
        mPreferences.setLocationTrackingType(prefs.getString(
                "locationPref", "GPS"));
        mPreferences.setEnableLocationTracking(prefs.getBoolean(
                "enableLocationTracking", false));

        mPreferences.setLogPath(prefs.getString("logPath", Environment.getExternalStorageDirectory() + "/ifmap-client/logs/"));

        mPreferences.setAutoUpdate(prefs.getBoolean("autoUpdate", false));

        mPreferences.setKeystorePath(prefs.getString("KeystorePath", Environment.getExternalStorageDirectory() + "/ifmap-client/keystore/keystore"));
        mPreferences.setKeystorePassword(prefs.getString("keystorepw", ""));

        mPreferences.setUseNonConformMetadata(prefs.getBoolean(
                R.id.esukomMetadataSettings + "", false));
        mPreferences.setDontSendApplicationsInfos(prefs.getBoolean(
                "sendNoAppsPreferences", false));
        mPreferences.setAutostart(prefs.getBoolean("autostartPreferences",
                false));
        mPreferences.setAutoconnect(prefs.getBoolean("autoconnectPreferences",
                false));
        mPreferences.setDontSendGoogleApps(prefs.getBoolean(
                "sendNoGoogleAppsPreferences", true));
        mPreferences.setAllowUnsafeSSLPreference(prefs.getBoolean(
                "allowUnsafeSSLPreference", true));
        mPreferences.setEnableNewAndEndSessionLog(prefs.getBoolean(
                "logNewsessionRequest", false));
        mPreferences
                .setEnablePollLog(prefs.getBoolean("logPollRequest", false));
        mPreferences.setEnableSubscribe(prefs.getBoolean("logSubscripeRequest",
                false));
        mPreferences.setEnableLocationTrackingLog(prefs.getBoolean(
                "logLocationTracking", false));
        mPreferences.setEnablePublishCharacteristicsLog(prefs.getBoolean(
                "logPublishCharacteristics", false));
        mPreferences.setEnableErrorMessageLog(prefs.getBoolean(
                "logErrorMessage", false));
        mPreferences.setEnableInvalideResponseLog(prefs.getBoolean(
                "logInvalideResponse", false));
        mPreferences.setEnableRenewRequestLog(prefs.getBoolean(
                "logRenewRequest", false));
        mPreferences.setUsernamePreference(prefs.getString(
                "usernamePreference", "user"));
        mPreferences.setPasswordPreference(prefs.getString(
                "passwordPreference", "password"));
        mPreferences.setIFMAPServerIpPreference(prefs.getString(
                "IF-MAPServeripPreference", ""));
        mPreferences.setIFMAPServerPortPreference(prefs.getString(
                "IF-MAPServerportPreference", "8443"));
        mPreferences.setIMonitorServerIpPreference(prefs.getString(
                "iMonitorServeripPreference", ""));
        mPreferences.setIMonitorServerPortPreference(prefs.getString(
                "iMonitorServerportPreference", "5667"));
        mPreferences.setNscaEncPreference(prefs.getString(
                "nscaEncPref", "1"));
        mPreferences.setNscaPassPreference(prefs.getString(
                "nscaPassPreference", ""));
        mPreferences.setIsPermantConnection(prefs.getBoolean(
                "permanantlyConectionPreferences", true));

        //mPreferences.setIsUseBasicAuth(prefs.getString("auth", "Basic-Authentication"));
        mPreferences.setUseBasicAuth(prefs.getString("authType", "Basic-Auth").equals("Basic-Auth"));

        // set update interval
        try {
            mPreferences.setUpdateInterval(Long.parseLong(prefs.getString(
                    "updateInterval", "600000")));
        } catch (NumberFormatException e) {
            // should not happen! just in case of...
            Toolbox.logTxt(
                    PreferenceInitializer.class.toString(),
                    "initializing of update interval from preferences failed...using default (60000)");
            mPreferences.setUpdateInterval(60000L);
        }
        // check if update interval is above minimum, of not set it to
        // default minimum value
        if (mPreferences.getUpdateInterval() < 60000L) {
            mPreferences.setUpdateInterval(60000L);
            Toolbox.logTxt(PreferenceInitializer.class.toString(),
                    "configured update interval is to short...using default (60000)");
        }

        // set renew session interval
        try {
            mPreferences.setRenewIntervalPreference(Long.parseLong(prefs
                    .getString("renewInterval", "10000l")));
        } catch (NumberFormatException e) {
            // should not happen! just in case of...
            Toolbox.logTxt(
                    PreferenceInitializer.class.toString(),
                    "initializing of renew session interval from preferences failed...using default (10000)");
            mPreferences.setRenewIntervalPreference(10000L);
        }

        // check if renew-session interval is above minimum, of not set it to
        // default minimum value
        if (mPreferences.getRenewIntervalPreference() < 10000L) {
            mPreferences.setRenewIntervalPreference(10000L);
            Toolbox.logTxt(PreferenceInitializer.class.toString(),
                    "configured renew session interval is to short...using default (10000)");
        }
    }
}

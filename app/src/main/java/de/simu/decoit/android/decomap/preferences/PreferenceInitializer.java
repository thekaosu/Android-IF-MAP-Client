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
    public static void initPreferences(Context windowContext, Context baseContext) {
        Toolbox.logTxt(PreferenceInitializer.class.toString(), "onPreferences(...) called");

        // get the preferences.xml preferences
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(baseContext);

        try {
            // set preferences
            mPreferences.setApplicationFileLogging(prefs.getBoolean(
                    "applicatiologging", true));
            mPreferences.setMonitoringPreference(prefs.getString(
                    R.id.monitoringModeSettings + "", windowContext.getResources().getTextArray(R.array.preferences_value_serverForm)[1].toString()));
            mPreferences.setLocationTrackingType(prefs.getString(
                    "locationPref", "GPS"));
            mPreferences.setEnableLocationTracking(prefs.getBoolean(
                    "enableLocationTracking", false));

            mPreferences.setLogPath(prefs.getString("logPath", Environment.getExternalStorageDirectory() + "/ifmap-client/logs/"));

            mPreferences.setAutoUpdate(prefs.getBoolean("autoUpdate", true));

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
                    "logNewsessionRequest", true));
            mPreferences.setUpdateConnectionLog(prefs.getBoolean(
                    "logUpdateConnection", true));
            mPreferences.setEnablePublishCharacteristicsLog(prefs.getBoolean(
                    "logPublishCharacteristics", true));
            mPreferences.setEnableErrorMessageLog(prefs.getBoolean(
                    "logErrorMessage", true));
            mPreferences.setEnableInvalideResponseLog(prefs.getBoolean(
                    "logInvalideResponse", true));
            mPreferences.setEnableRenewRequestLog(prefs.getBoolean(
                    "logRenewRequest", true));
            mPreferences.setUsernamePreference(prefs.getString(
                    "usernamePreference", "user"));
            mPreferences.setPasswordPreference(prefs.getString(
                    "passwordPreference", ""));
            mPreferences.setIFMAPServerIpPreference(prefs.getString(
                    "IF-MAPServeripPreference", "127.0.0.1"));
            mPreferences.setIFMAPServerPortPreference(prefs.getString(
                    "IF-MAPServerportPreference", "8443"));
            mPreferences.setIMonitorServerIpPreference(prefs.getString(
                    "iMonitorServeripPreference", "127.0.0.1"));
            mPreferences.setIMonitorServerPortPreference(prefs.getString(
                    "iMonitorServerportPreference", "5667"));
            mPreferences.setNscaEncPreference(prefs.getString(
                    "nscaEncPref", "1"));
            mPreferences.setNscaPassPreference(prefs.getString(
                    "nscaPassPreference", ""));
            mPreferences.setIsPermantConnection(prefs.getBoolean(
                    "permanantlyConectionPreferences", true));

            //mPreferences.setIsUseBasicAuth(prefs.getString("auth", "Basic-Authentication"));
            mPreferences.setUseBasicAuth(prefs.getString("authType", windowContext.getResources().getTextArray(R.array.preferences_auth_types)[0].toString()).equals(windowContext.getResources().getTextArray(R.array.preferences_auth_types)[0].toString()));

            // set update interval
            mPreferences.setUpdateInterval(Long.parseLong(prefs.getString(
                    "updateInterval", "600000")));
            // check if update interval is above minimum, of not set it to
            // default minimum value
            if (mPreferences.getUpdateInterval() < 60000L) {
                mPreferences.setUpdateInterval(60000L);
                Toolbox.logTxt(PreferenceInitializer.class.toString(),
                        "configured update interval is to short...using default (60000)");
            }

            // set renew session interval
            mPreferences.setRenewIntervalPreference(Long.parseLong(prefs
                    .getString("renewInterval", "10000")));

            // check if renew-session interval is above minimum, of not set it to
            // default minimum value
            if (mPreferences.getRenewIntervalPreference() < 10000L) {
                mPreferences.setRenewIntervalPreference(10000L);
                Toolbox.logTxt(PreferenceInitializer.class.toString(),
                        "configured renew session interval is to short...using default (10000)");
            }

        } catch (ClassCastException | NumberFormatException e) {
            Toolbox.showError(windowContext,
                    PreferenceInitializer.class.toString(),
                    "Error while loading preferences! Loading default values! \n\nError:\n" + e.getMessage());

            //set default values!
            mPreferences.setApplicationFileLogging(true);
            mPreferences.setMonitoringPreference(windowContext.getResources().getTextArray(R.array.preferences_value_serverForm)[1].toString());
            mPreferences.setLocationTrackingType("GPS");
            mPreferences.setEnableLocationTracking(false);
            mPreferences.setLogPath(Environment.getExternalStorageDirectory() + "/ifmap-client/logs/");
            mPreferences.setAutoUpdate(true);
            mPreferences.setKeystorePath(Environment.getExternalStorageDirectory() + "/ifmap-client/keystore/keystore");
            mPreferences.setKeystorePassword("");
            mPreferences.setUseNonConformMetadata(false);
            mPreferences.setDontSendApplicationsInfos(false);
            mPreferences.setAutostart(false);
            mPreferences.setAutoconnect(false);
            mPreferences.setDontSendGoogleApps(true);
            mPreferences.setAllowUnsafeSSLPreference(true);
            mPreferences.setEnableNewAndEndSessionLog(true);
            mPreferences.setUpdateConnectionLog(true);
            mPreferences.setEnablePublishCharacteristicsLog(true);
            mPreferences.setEnableErrorMessageLog(true);
            mPreferences.setEnableInvalideResponseLog(true);
            mPreferences.setEnableRenewRequestLog(true);
            mPreferences.setUsernamePreference("user");
            mPreferences.setPasswordPreference("");
            mPreferences.setIFMAPServerIpPreference("127.0.0.1");
            mPreferences.setIFMAPServerPortPreference("8443");
            mPreferences.setIMonitorServerIpPreference("127.0.0.1");
            mPreferences.setIMonitorServerPortPreference("5667");
            mPreferences.setNscaEncPreference("1");
            mPreferences.setNscaPassPreference("");
            mPreferences.setIsPermantConnection(true);
            mPreferences.setUseBasicAuth(true);
            mPreferences.setUpdateInterval(600000L);
            mPreferences.setRenewIntervalPreference(10000L);
        }
    }
}

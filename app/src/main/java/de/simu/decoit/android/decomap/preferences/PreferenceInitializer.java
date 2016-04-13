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
    public static void initPreferences(Context windowContext) {
        Toolbox.logTxt(PreferenceInitializer.class.toString(), "onPreferences(...) called");

        // get the preferences.xml preferences
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(windowContext);

        try {
            // set preferences
            mPreferences.setApplicationFileLogging(prefs.getBoolean(
                    windowContext.getResources().getString(R.string.preferences_keys_applicatiologging), windowContext.getResources().getBoolean(R.bool.preferences_default_values_application_file_logging)));
            mPreferences.setMonitoringPreference(prefs.getString(
                    R.id.monitoringModeSettings + "", windowContext.getResources().getTextArray(R.array.preferences_value_serverForm)[1].toString()));
            mPreferences.setLocationTrackingType(prefs.getString(
                    windowContext.getResources().getString(R.string.preferences_keys_location_type), windowContext.getResources().getString(R.string.preferences_default_values_location_tracking)));
            mPreferences.setEnableLocationTracking(prefs.getBoolean(
                    windowContext.getResources().getString(R.string.preferences_keys_location_tracking_enabled), windowContext.getResources().getBoolean(R.bool.preferences_default_values_enabled_location_tracking)));

            mPreferences.setLogPath(prefs.getString(windowContext.getResources().getString(R.string.preferences_keys_logpath), Environment.getExternalStorageDirectory() + windowContext.getResources().getString(R.string.preferences_default_values_logpath)));

            mPreferences.setAutoUpdate(prefs.getBoolean(windowContext.getResources().getString(R.string.preferences_keys_automatic_update), windowContext.getResources().getBoolean(R.bool.preferences_default_values_autoupdate)));

            mPreferences.setKeystorePath(prefs.getString(windowContext.getResources().getString(R.string.preferences_keys_keystore_path), Environment.getExternalStorageDirectory() + windowContext.getResources().getString(R.string.preferences_default_values_keystore_path)));
            mPreferences.setKeystorePassword(prefs.getString(windowContext.getResources().getString(R.string.preferences_keys_keystore_password), windowContext.getResources().getString(R.string.preferences_default_values_keystore_password)));
            mPreferences.setUseNonConformMetadata(prefs.getBoolean(
                    R.id.esukomMetadataSettings + "", windowContext.getResources().getBoolean(R.bool.preferences_default_values_use_non_conform_metadata)));


            mPreferences.setDontSendApplicationsInfos(prefs.getBoolean(
                    windowContext.getResources().getString(R.string.preferences_keys_send_no_Apps), windowContext.getResources().getBoolean(R.bool.preferences_default_values_dont_send_application_infos)));
            mPreferences.setAutostart(prefs.getBoolean(windowContext.getResources().getString(R.string.preferences_keys_autostart),
                    windowContext.getResources().getBoolean(R.bool.preferences_default_values_autostart)));
            mPreferences.setAutoconnect(prefs.getBoolean(windowContext.getResources().getString(R.string.preferences_keys_autoconnect),
                    windowContext.getResources().getBoolean(R.bool.preferences_default_values_autoconnect)));
            mPreferences.setDontSendGoogleApps(prefs.getBoolean(windowContext.getResources().getString(R.string.preferences_keys_send_no_google_apps_preferences), windowContext.getResources().getBoolean(R.bool.preferences_default_values_dont_send_google_apps)));
            mPreferences.setAllowUnsafeSSLPreference(prefs.getBoolean(
                    windowContext.getResources().getString(R.string.preferences_keys_allow_unsafe_ssl), windowContext.getResources().getBoolean(R.bool.preferences_default_values_allow_unsafe_ssl)));
            mPreferences.setEnableNewAndEndSessionLog(prefs.getBoolean(
                    windowContext.getResources().getString(R.string.preferences_keys_logNewsessionRequest), windowContext.getResources().getBoolean(R.bool.preferences_default_values_log_new_and_end_seassion)));
            mPreferences.setUpdateConnectionLog(prefs.getBoolean(
                    windowContext.getResources().getString(R.string.preferences_keys_log_update_connection), windowContext.getResources().getBoolean(R.bool.preferences_default_values_log_update)));
            mPreferences.setEnablePublishCharacteristicsLog(prefs.getBoolean(
                    windowContext.getResources().getString(R.string.preferences_keys_log_publish_characteristics), windowContext.getResources().getBoolean(R.bool.preferences_default_values_log_publish)));
            mPreferences.setEnableErrorMessageLog(prefs.getBoolean(
                    windowContext.getResources().getString(R.string.preferences_keys_log_error_message), windowContext.getResources().getBoolean(R.bool.preferences_default_values_log_error_messages)));
            mPreferences.setEnableInvalideResponseLog(prefs.getBoolean(
                    windowContext.getResources().getString(R.string.preferences_keys_log_invalide_response), windowContext.getResources().getBoolean(R.bool.preferences_default_values_log_invalid_response)));
            mPreferences.setEnableRenewRequestLog(prefs.getBoolean(
                    windowContext.getResources().getString(R.string.preferences_keys_logRenewRequest), windowContext.getResources().getBoolean(R.bool.preferences_default_values_log_renew_request)));
            mPreferences.setUsernamePreference(prefs.getString(
                    windowContext.getResources().getString(R.string.preferences_keys_username), windowContext.getResources().getString(R.string.preferences_default_values_username)));
            mPreferences.setPasswordPreference(prefs.getString(
                    windowContext.getResources().getString(R.string.preferences_keys_password), windowContext.getResources().getString(R.string.preferences_default_values_password)));
            mPreferences.setIFMAPServerIpPreference(prefs.getString(
                    windowContext.getResources().getString(R.string.preferences_keys_ifmap_mapserver_ip), windowContext.getResources().getString(R.string.preferences_default_values_ifmap_ip)));
            mPreferences.setIFMAPServerPortPreference(prefs.getString(
                    windowContext.getResources().getString(R.string.preferences_keys_ifmap_mapserver_port), windowContext.getResources().getString(R.string.preferences_default_values_ifmap_port)));
            mPreferences.setIMonitorServerIpPreference(prefs.getString(
                    windowContext.getResources().getString(R.string.preferences_keys_imonitor_server_ip), windowContext.getResources().getString(R.string.preferences_default_values_imonitor_ip)));
            mPreferences.setIMonitorServerPortPreference(prefs.getString(
                    windowContext.getResources().getString(R.string.preferences_keys_imonitor_server_port), windowContext.getResources().getString(R.string.preferences_default_values_imonitor_port)));
            mPreferences.setNscaEncPreference(prefs.getString(
                    windowContext.getResources().getString(R.string.preferences_keys_nsca_encryption), windowContext.getResources().getString(R.string.preferences_default_values_nsca_encription)));
            mPreferences.setNscaPassPreference(prefs.getString(
                    windowContext.getResources().getString(R.string.preferences_keys_nsca_password), windowContext.getResources().getString(R.string.preferences_default_values_nsca_password)));
            mPreferences.setIsPermantConnection(prefs.getBoolean(
                    windowContext.getResources().getString(R.string.preferences_keys_permanent_connection), windowContext.getResources().getBoolean(R.bool.preferences_default_values_permanent_connection)));

            //mPreferences.setIsUseBasicAuth(prefs.getString("auth", "Basic-Authentication"));
            mPreferences.setUseBasicAuth(prefs.getString(windowContext.getResources().getString(R.string.preferences_keys_authentication_type), windowContext.getResources().getTextArray(R.array.preferences_auth_types)[0].toString()).equals(windowContext.getResources().getTextArray(R.array.preferences_auth_types)[0].toString()));

            // set update interval
            mPreferences.setUpdateInterval(Long.parseLong(prefs.getString(
                    windowContext.getResources().getString(R.string.preferences_keys_updateinterval), windowContext.getResources().getString(R.string.preferences_default_values_update_interval))));
            // check if update interval is above minimum, of not set it to
            // default minimum value
            if (mPreferences.getUpdateInterval() < 60000L) {
                mPreferences.setUpdateInterval(Long.parseLong(windowContext.getResources().getString(R.string.preferences_default_values_update_interval)));
                Toolbox.logTxt(PreferenceInitializer.class.toString(),
                        "configured update interval is to short...using default (" + windowContext.getResources().getString(R.string.preferences_default_values_update_interval) + ")");
            }

            // set renew session interval
            mPreferences.setRenewIntervalPreference(Long.parseLong(prefs
                    .getString(windowContext.getResources().getString(R.string.preferences_keys_renew_intervall), windowContext.getResources().getString(R.string.preferences_default_values_renew_interval))));

            // check if renew-session interval is above minimum, of not set it to
            // default minimum value
            if (mPreferences.getRenewIntervalPreference() < 10000L) {
                mPreferences.setRenewIntervalPreference(Long.parseLong(windowContext.getResources().getString(R.string.preferences_default_values_renew_interval)));
                Toolbox.logTxt(PreferenceInitializer.class.toString(),
                        "configured renew session interval is to short...using default (" + windowContext.getResources().getString(R.string.preferences_default_values_renew_interval) + ")");
            }

        } catch (ClassCastException | NumberFormatException e) {
            Toolbox.showError(windowContext,
                    PreferenceInitializer.class.toString(),
                    "Error while loading preferences! Loading default values! \n\nError:\n" + e.getMessage());

            //set default values!
            mPreferences.setApplicationFileLogging(windowContext.getResources().getBoolean(R.bool.preferences_default_values_application_file_logging));
            mPreferences.setMonitoringPreference(windowContext.getResources().getTextArray(R.array.preferences_value_serverForm)[1].toString());
            mPreferences.setLocationTrackingType(windowContext.getResources().getString(R.string.preferences_default_values_location_tracking));
            mPreferences.setEnableLocationTracking(windowContext.getResources().getBoolean(R.bool.preferences_default_values_enabled_location_tracking));
            mPreferences.setLogPath(Environment.getExternalStorageDirectory() + windowContext.getResources().getString(R.string.preferences_default_values_logpath));
            mPreferences.setAutoUpdate(windowContext.getResources().getBoolean(R.bool.preferences_default_values_autoupdate));
            mPreferences.setKeystorePath(Environment.getExternalStorageDirectory() + windowContext.getResources().getString(R.string.preferences_default_values_keystore_path));
            mPreferences.setKeystorePassword(windowContext.getResources().getString(R.string.preferences_default_values_keystore_password));
            mPreferences.setUseNonConformMetadata(windowContext.getResources().getBoolean(R.bool.preferences_default_values_use_non_conform_metadata));
            mPreferences.setDontSendApplicationsInfos(windowContext.getResources().getBoolean(R.bool.preferences_default_values_dont_send_application_infos));
            mPreferences.setAutostart(windowContext.getResources().getBoolean(R.bool.preferences_default_values_autostart));
            mPreferences.setAutoconnect(windowContext.getResources().getBoolean(R.bool.preferences_default_values_autoconnect));
            mPreferences.setDontSendGoogleApps(windowContext.getResources().getBoolean(R.bool.preferences_default_values_dont_send_google_apps));
            mPreferences.setAllowUnsafeSSLPreference(windowContext.getResources().getBoolean(R.bool.preferences_default_values_allow_unsafe_ssl));
            mPreferences.setEnableNewAndEndSessionLog(windowContext.getResources().getBoolean(R.bool.preferences_default_values_log_new_and_end_seassion));
            mPreferences.setUpdateConnectionLog(windowContext.getResources().getBoolean(R.bool.preferences_default_values_log_update));
            mPreferences.setEnablePublishCharacteristicsLog(windowContext.getResources().getBoolean(R.bool.preferences_default_values_log_publish));
            mPreferences.setEnableErrorMessageLog(windowContext.getResources().getBoolean(R.bool.preferences_default_values_log_error_messages));
            mPreferences.setEnableInvalideResponseLog(windowContext.getResources().getBoolean(R.bool.preferences_default_values_log_invalid_response));
            mPreferences.setEnableRenewRequestLog(windowContext.getResources().getBoolean(R.bool.preferences_default_values_log_renew_request));
            mPreferences.setUsernamePreference(windowContext.getResources().getString(R.string.preferences_default_values_username));
            mPreferences.setPasswordPreference(windowContext.getResources().getString(R.string.preferences_default_values_password));
            mPreferences.setIFMAPServerIpPreference(windowContext.getResources().getString(R.string.preferences_default_values_ifmap_ip));
            mPreferences.setIFMAPServerPortPreference(windowContext.getResources().getString(R.string.preferences_default_values_ifmap_port));
            mPreferences.setIMonitorServerIpPreference(windowContext.getResources().getString(R.string.preferences_default_values_imonitor_ip));
            mPreferences.setIMonitorServerPortPreference(windowContext.getResources().getString(R.string.preferences_default_values_imonitor_port));
            mPreferences.setNscaEncPreference(windowContext.getResources().getString(R.string.preferences_default_values_nsca_encription));
            mPreferences.setNscaPassPreference(windowContext.getResources().getString(R.string.preferences_default_values_nsca_password));
            mPreferences.setIsPermantConnection(windowContext.getResources().getBoolean(R.bool.preferences_default_values_permanent_connection));
            mPreferences.setUseBasicAuth(windowContext.getResources().getBoolean(R.bool.preferences_default_values_use_basic_auth));
            mPreferences.setUpdateInterval(Long.parseLong(windowContext.getResources().getString(R.string.preferences_default_values_update_interval)));
            mPreferences.setRenewIntervalPreference(Long.parseLong(windowContext.getResources().getString(R.string.preferences_default_values_renew_interval)));
        }
    }
}

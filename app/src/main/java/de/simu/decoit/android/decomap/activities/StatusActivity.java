/* 
 * StatusActivity.java        0.2 2015-03-08
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

package de.simu.decoit.android.decomap.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.simu.decoit.android.decomap.device.DeviceProperties;
import de.simu.decoit.android.decomap.device.ListEntry;
import de.simu.decoit.android.decomap.device.StatusMessageAdapter;
import de.simu.decoit.android.decomap.device.system.SystemProperties;
import de.simu.decoit.android.decomap.dialogs.MessageDialog;
import de.simu.decoit.android.decomap.messaging.MessageParameter;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * Activity for showing Device-Status, current location and Applications Permission
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Marcel Jahnke, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @version 0.1.5
 */
public class StatusActivity extends Activity implements OnItemClickListener {

    // static values for onClick-handler, used to determine which
    // list item has been clicked by using item position in list
    private static final byte LIST_POSITION_IP = 0;
    private static final byte LIST_POSITION_MAC = 1;
    private static final byte LIST_POSITION_IMEI = 2;
    private static final byte LIST_POSITION_IMSI = 3;
    //public static final byte LIST_POSITION_KERNEL_VERSION = 4;
    private static final byte LIST_POSITION_FIRMWARE_VERSION = 5;
    private static final byte LIST_POSITION_BUILD_NUMBER = 6;
    private static final byte LIST_POSITION_BASEBAND_VERSION = 7;
    private static final byte LIST_POSITION_DEVICE_BRANDING = 8;
    private static final byte LIST_POSITION_DEVICE_MANUFACTURER = 9;
    private static final byte LIST_POSITION_DEVICE_PHONENUMBER = 10;
    private static final byte LIST_POSITION_DEVICE_SMSCOUNT_IN = 11;
    private static final byte LIST_POSITION_DEVICE_SMSCOUNT_OUT = 12;
    private static final byte LIST_POSITION_DEVICE_SMSSENDDATE = 13;
    private static final byte LIST_POSITION_DEVICE_LASTCAMERAUSE = 14;
    private static final byte LIST_POSITION_BLUEATOOTH_ENABLED = 15;
    private static final byte LIST_POSITION_MICROPHONE_MUTED = 16;
    private static final byte LIST_POSITION_BATTERY_LEVEL = 17;
    private static final byte LIST_POSITION_RECEIVED_BYTES = 18;
    private static final byte LIST_POSITION_TRANSFERED_BYTES = 19;
    private static final byte LIST_POSITION_CPU_LOAD = 20;
    private static final byte LIST_POSITION_RAM_FREE = 21;
    private static final byte LIST_POSITION_PROCESS_COUNT = 22;
    private static final byte LIST_POSITION_RUNNING_PROCESSES = 23;
    private static final byte LIST_POSITION_INSTALLED_APPS = 24;
    private static final byte LIST_POSITION_INSTALLED_APPS_WITH_PERMS = 25;
    private static final byte LIST_POSITION_PERMISSIONS = 26;
    private static final byte LIST_POSITION_LONGITUDE = 27;
    private static final byte LIST_POSITION_LATITUDE = 28;
    private static final byte LIST_POSITION_ALTITUDE = 29;

    // required by location manager from MainActivity in order to detect
    // if this Activity is already initialized before sending location-data
    // for displaying the location-data in status-list
    public static boolean sIsActivityActive = false;

    private final MessageParameter mp = MessageParameter.getInstance();

    // location properties
    private static TextView sLocationLongitude;
    private static TextView sLocationLatitude;
    private static TextView sLocationAltitude;

    // location tracking default values
    private static String sLatitudeValue;
    private static String sLongitudeValue;
    private static String sAltitudeValue;

    // device properties
    private DeviceProperties mDeviceProperties;

    private ArrayList<ListEntry> mListArray;
    private StatusMessageAdapter mStatusAdapter;

    // -------------------------------------------------------------------------
    // ACTIVITY LIFECYCLE HANDLING
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbox.logTxt(this.getLocalClassName(), "onCreate(...) called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_activity);
    }

    @Override
    public void onStart() {
        Toolbox.logTxt(this.getLocalClassName(), "onStart() called");
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Toolbox.logTxt(this.getLocalClassName(), "onResume() called");
        super.onStart();

        if (sLatitudeValue == null) {
            sLatitudeValue = getString(R.string.status_default_undetected);
        }
        if (sLongitudeValue == null) {
            sLongitudeValue = getString(R.string.status_default_undetected);
        }
        if (sAltitudeValue == null) {
            sAltitudeValue = getString(R.string.status_default_undetected);
        }

        try {
            if (super.getParent().getIntent().getExtras().size() > 2) {
                sLatitudeValue = (String) super.getParent().getIntent().getExtras().get("latitude");
                sLongitudeValue = (String) super.getParent().getIntent().getExtras().get("longitude");
            }
        } catch (NullPointerException e) {
            Toolbox.logTxt(this.getClass().getName(), "StatusActivity.onResume(...) getExtras() " + e);
        }
        initValues();
        initListAdapter();
    }

    // -------------------------------------------------------------------------
    // ACTIVITY INITIALISATION HANDLING
    // -------------------------------------------------------------------------

    /**
     * initialize required application values
     */
    private void initValues() {

        // location properties
        sLocationLongitude = (TextView) findViewById(R.id.LongitudeLabel_TextView);
        sLocationLatitude = (TextView) findViewById(R.id.LatitudeLabel_TextView);
        sLocationAltitude = (TextView) findViewById(R.id.AltitudeLabel_TextView);

        // get device properties and initialize text-fields
        mDeviceProperties = new DeviceProperties(this);

        // Add Items to ListView
        mListArray = new ArrayList<>();

        // add values
        addValueToListEntry(getString(R.string.info_label_value_ip), mDeviceProperties.getSystemProperties().getLocalIpAddress());
        addValueToListEntry(getString(R.string.info_label_value_mac), mDeviceProperties.getSystemProperties().getMAC());
        addValueToListEntry(getString(R.string.info_label_value_deviceimei), mDeviceProperties.getPhoneProperties().getIMEI());
        addValueToListEntry(getString(R.string.info_label_value_deviceimsi), mDeviceProperties.getPhoneProperties().getIMSI());
        addValueToListEntry(getString(R.string.info_label_value_kernelversion), mDeviceProperties.getSystemProperties().getKernelVersion());
        addValueToListEntry(getString(R.string.info_label_value_version), mDeviceProperties.getPhoneProperties().getFirmwareVersion());
        addValueToListEntry(getString(R.string.info_label_value_buildnumber), mDeviceProperties.getPhoneProperties().getBuildNumber());
        addValueToListEntry(getString(R.string.info_label_value_basebandversion), mDeviceProperties.getPhoneProperties()
                .getBasebandVersion());
        addValueToListEntry(getString(R.string.info_label_value_branding), mDeviceProperties.getPhoneProperties().getBranding());
        addValueToListEntry(getString(R.string.info_label_value_manufacturer), mDeviceProperties.getPhoneProperties().getManufacturer());
        addValueToListEntry(getString(R.string.info_label_value_phonenumber), mDeviceProperties.getPhoneProperties().getPhonenumber());
        addValueToListEntry(getString(R.string.info_label_value_smscount), Integer.valueOf(mp.getSmsInCount()).toString());
        addValueToListEntry(getString(R.string.info_label_value_smscount_out), Integer.valueOf(mp.getSmsSentCount()).toString());
        addValueToListEntry(getString(R.string.info_label_value_smscount_lastsend), convertLastSentDate(mp.getLastSendDate()));
        addValueToListEntry(getString(R.string.info_label_value_camera_lastused), convertLastSentDate(mp.getLastPictureTakenDate()));
        addValueToListEntry(getString(R.string.info_label_value_bluetooth), mDeviceProperties.getPhoneProperties()
                .getBluetoothActiveStatusString());
        addValueToListEntry(getString(R.string.info_label_value_microphone), mDeviceProperties.getPhoneProperties()
                .getMicrophoneActiveString());
        addValueToListEntry(getString(R.string.info_label_value_battery), mp.getCurrentBatteryLevel() + "%");
        addValueToListEntry(getString(R.string.info_label_value_received_total_bytes), SystemProperties.getTotalRxKBytes()
                + " kb");
        addValueToListEntry(getString(R.string.info_label_value_transferred_total_bytes), SystemProperties.getTotalTxKBytes()
                + " kb");
        addValueToListEntry(getString(R.string.info_label_value_cpu_load), "touch to update");
        addValueToListEntry(getString(R.string.info_label_value_ram_free), mDeviceProperties.getSystemProperties().getFormattedFreeRam());
        addValueToListEntry(getString(R.string.info_label_value_process_count),
                String.valueOf(mDeviceProperties.getApplicationProperties().getRuningProcCount()));
        addValueToListEntry(getString(R.string.info_label_value_running_process_names), "touch to show");
        addValueToListEntry(getString(R.string.info_label_apps), "touch to show");
        addValueToListEntry(getString(R.string.info_label_appswithperms), "touch to show");
        addValueToListEntry(getString(R.string.info_label_perms), "touch to show");
        addValueToListEntry(getString(R.string.info_label_latitude), sLatitudeValue);
        addValueToListEntry(getString(R.string.info_label_longitude), sLongitudeValue);
        addValueToListEntry(getString(R.string.info_label_altitude), sAltitudeValue);

        sIsActivityActive = true;
    }

    /**
     * initialize adapter for status-list
     */
    private void initListAdapter() {
        ListView mList = (ListView) findViewById(R.id.status_ListView);
        mStatusAdapter = new StatusMessageAdapter(this, mListArray);
        mList.setAdapter(mStatusAdapter);
        mList.setOnItemClickListener(this);
    }

    // -------------------------------------------------------------------------
    // BUTTON HANDLING
    // -------------------------------------------------------------------------

    /**
     * Handler for update status information button
     *
     * @param view element that originated the call
     */
    public void updateStatusButtonHandler(View view) {
        // update status information
        Toast.makeText(this, "updating status information...", Toast.LENGTH_SHORT).show();
        updateEntry(mDeviceProperties.getSystemProperties().getLocalIpAddress(), LIST_POSITION_IP);
        updateEntry(String.valueOf(mp.getSmsInCount()), LIST_POSITION_DEVICE_SMSCOUNT_IN);
        updateEntry(String.valueOf(mp.getSmsSentCount()), LIST_POSITION_DEVICE_SMSCOUNT_OUT);
        updateEntry(convertLastSentDate(mp.getLastSendDate()), LIST_POSITION_DEVICE_SMSSENDDATE);
        updateEntry(convertLastSentDate(mp.getLastPictureTakenDate()), LIST_POSITION_DEVICE_LASTCAMERAUSE);
        updateEntry(mDeviceProperties.getPhoneProperties().getBluetoothActiveStatusString(), LIST_POSITION_BLUEATOOTH_ENABLED);
        updateEntry(mDeviceProperties.getPhoneProperties().getMicrophoneActiveString(), LIST_POSITION_MICROPHONE_MUTED);
        updateEntry(mp.getCurrentBatteryLevel() + "%", LIST_POSITION_BATTERY_LEVEL);
        updateEntry(SystemProperties.getTotalRxKBytes() + " kb", LIST_POSITION_RECEIVED_BYTES);
        updateEntry(SystemProperties.getTotalTxKBytes() + " kb", LIST_POSITION_TRANSFERED_BYTES);
        updateEntry(sLongitudeValue, LIST_POSITION_LONGITUDE);
        updateEntry(sLatitudeValue, LIST_POSITION_LATITUDE);
        updateEntry(sLongitudeValue, LIST_POSITION_LONGITUDE);
        updateEntry(sAltitudeValue, LIST_POSITION_ALTITUDE);
        updateEntry(mDeviceProperties.getSystemProperties().getFormattedCurCpuLoadPercent(), LIST_POSITION_CPU_LOAD);
        updateEntry(mDeviceProperties.getSystemProperties().getFormattedFreeRam(), LIST_POSITION_RAM_FREE);
        updateEntry(String.valueOf(mDeviceProperties.getApplicationProperties().getRuningProcCount()), LIST_POSITION_PROCESS_COUNT);
        mStatusAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
        ListEntry mEntry = (ListEntry) mStatusAdapter.getItem(position);
        if (position != LIST_POSITION_INSTALLED_APPS && position != LIST_POSITION_INSTALLED_APPS_WITH_PERMS
                && position != LIST_POSITION_PERMISSIONS) {
            Toast.makeText(this, "updating " + mEntry.getTitle().replace(":", "") + " value...", Toast.LENGTH_SHORT).show();
        }

        // update list-entries
        switch (position) {
            case LIST_POSITION_IP:
                updateEntry(mDeviceProperties.getSystemProperties().getLocalIpAddress(), position);
                break;
            case LIST_POSITION_MAC:
                updateEntry(mDeviceProperties.getSystemProperties().getMAC(), position);
                break;
            case LIST_POSITION_IMSI:
                updateEntry(mDeviceProperties.getPhoneProperties().getIMSI(), position);
                break;
            case LIST_POSITION_IMEI:
                updateEntry(mDeviceProperties.getPhoneProperties().getIMEI(), position);
                break;
            case LIST_POSITION_BUILD_NUMBER:
                updateEntry(mDeviceProperties.getPhoneProperties().getBuildNumber(), position);
                break;
            case LIST_POSITION_DEVICE_BRANDING:
                updateEntry(mDeviceProperties.getPhoneProperties().getBranding(), position);
                break;
            case LIST_POSITION_DEVICE_MANUFACTURER:
                updateEntry(mDeviceProperties.getPhoneProperties().getManufacturer(), position);
                break;
            case LIST_POSITION_DEVICE_PHONENUMBER:
                updateEntry(mDeviceProperties.getPhoneProperties().getPhonenumber(), position);
                break;
            case LIST_POSITION_FIRMWARE_VERSION:
                updateEntry(mDeviceProperties.getPhoneProperties().getFirmwareVersion(), position);
                break;
            case LIST_POSITION_BASEBAND_VERSION:
                updateEntry(mDeviceProperties.getPhoneProperties().getBasebandVersion(), position);
                break;
            case LIST_POSITION_DEVICE_SMSCOUNT_IN:
                updateEntry(String.valueOf(mp.getSmsInCount()), position);
                break;
            case LIST_POSITION_DEVICE_SMSCOUNT_OUT:
                updateEntry(String.valueOf(mp.getSmsSentCount()), position);
                break;
            case LIST_POSITION_DEVICE_SMSSENDDATE:
                updateEntry(convertLastSentDate(mp.getLastSendDate()), position);
                break;
            case LIST_POSITION_DEVICE_LASTCAMERAUSE:
                updateEntry(convertLastSentDate(mp.getLastPictureTakenDate()), position);
                break;
            case LIST_POSITION_BLUEATOOTH_ENABLED:
                String bluetoothEntryValue = mDeviceProperties.getPhoneProperties().getBluetoothActiveStatusString();
                updateEntry(bluetoothEntryValue, position);
                break;
            case LIST_POSITION_MICROPHONE_MUTED:
                String microphoneEntryValue = mDeviceProperties.getPhoneProperties().getMicrophoneActiveString();
                updateEntry(microphoneEntryValue, position);
                break;
            case LIST_POSITION_BATTERY_LEVEL:
                updateEntry(mp.getCurrentBatteryLevel() + "%", position);
                break;
            case LIST_POSITION_RECEIVED_BYTES:
                updateEntry(SystemProperties.getTotalRxKBytes() + " kb", position);
                break;
            case LIST_POSITION_TRANSFERED_BYTES:
                updateEntry(SystemProperties.getTotalTxKBytes() + " kb", position);
                break;
            case LIST_POSITION_LATITUDE:
                updateEntry(sLongitudeValue, position);
                break;
            case LIST_POSITION_LONGITUDE:
                updateEntry(sLatitudeValue, position);
                break;
            case LIST_POSITION_ALTITUDE:
                updateEntry(sAltitudeValue, position);
                break;
            case LIST_POSITION_INSTALLED_APPS:
                showApplicationsInformations(LIST_POSITION_INSTALLED_APPS);
                break;
            case LIST_POSITION_INSTALLED_APPS_WITH_PERMS:
                showApplicationsInformations(LIST_POSITION_INSTALLED_APPS_WITH_PERMS);
                break;
            case LIST_POSITION_PERMISSIONS:
                showApplicationsInformations(LIST_POSITION_PERMISSIONS);
                break;
            case LIST_POSITION_CPU_LOAD:
                updateEntry(mDeviceProperties.getSystemProperties().getFormattedCurCpuLoadPercent(), position);
                break;
            case LIST_POSITION_RAM_FREE:
                updateEntry(mDeviceProperties.getSystemProperties().getFormattedFreeRam(), position);
                break;
            case LIST_POSITION_PROCESS_COUNT:
                updateEntry(String.valueOf(mDeviceProperties.getApplicationProperties().getRuningProcCount()), position);
                break;
            case LIST_POSITION_RUNNING_PROCESSES:
                showApplicationsInformations(LIST_POSITION_RUNNING_PROCESSES);
                break;
        }
        mListArray.set(position, mEntry);
        mStatusAdapter.notifyDataSetChanged();
    }

    /**
     * show applications-informations inside a dialog-box
     *
     * @param infoType type of applications-informations
     */
    private void showApplicationsInformations(int infoType) {
        String title = null;
        String msgToShow;
        ArrayList<String> appsettings = null;
        switch (infoType) {
            case LIST_POSITION_INSTALLED_APPS:
                // gather installed applications informations
                // and show them in a simple dialog-box
                // getFormattedApplicationList(boolean excludeNativeApplications, boolean includeVersionNumber,
                // boolean includePermissions)
                appsettings = mDeviceProperties.getApplicationProperties().getFormattedApplicationList(true, true, false, true);
                title = "Installed Applications";
                break;
            case LIST_POSITION_INSTALLED_APPS_WITH_PERMS:
                // gather installed applications and show them in a simple
                // dialog-box
                appsettings = mDeviceProperties.getApplicationProperties().getFormattedApplicationList(false, true, true, true);
                title = "Installed Applications";
                break;
            case LIST_POSITION_PERMISSIONS:
                appsettings = mDeviceProperties.getApplicationProperties().getFormattedPermissionsList();
                title = "Permissions";
                break;
            case LIST_POSITION_RUNNING_PROCESSES:
                appsettings = mDeviceProperties.getApplicationProperties().getFormattedRunningAppProcessNamesList();
                title = "Running Processes";
                break;
        }

        if (appsettings != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < appsettings.size() - 1; i++) {
                sb.append(appsettings.get(i));
                sb.append("\n");
            }
            msgToShow = sb.toString();
            MessageDialog dialog = new MessageDialog(this, msgToShow, title);
            dialog.show();
        }
    }

    // -------------------------------------------------------------------------
    // STATUS-LIST OPERATIONS
    // -------------------------------------------------------------------------

    /**
     * add a new value to device properties list
     *
     * @param label label of device properties list-entry
     * @param entry value of properties-list-entry
     */
    private void addValueToListEntry(String label, String entry) {
        if (entry == null || entry.length() == 0) {
            entry = getString(R.string.status_default_undetected_updatable);
        }
        mListArray.add(new ListEntry(label, entry));
    }

    /**
     * set updated value to device properties list
     *
     * @param value    new value for entry
     * @param position position for list entry
     */
    private void updateEntry(String value, int position) {
        ListEntry current = (ListEntry) mStatusAdapter.getItem(position);
        if (value == null || value.length() == 0) {
            value = getString(R.string.status_default_undetected_updatable);
        }
        current.setValue(value);

        mListArray.set(position, current);
    }

    /**
     * show the current location of the user
     *
     * @param latitude  current latitude value
     * @param longitude current longitude value
     * @param altitude  current altitude value
     */
    public static void setCurrentLocation(Context context, double latitude, double longitude, double altitude) {
        sLocationLatitude.setText(context.getString(R.string.info_label_longitude_double, latitude));
        sLocationLongitude.setText(context.getString(R.string.info_label_latitude_double, longitude));
        sLocationAltitude.setText(context.getString(R.string.info_label_altitude_double, altitude));
        sLatitudeValue = Double.valueOf(latitude).toString();
        sLongitudeValue = Double.valueOf(longitude).toString();
        sAltitudeValue = Double.valueOf(altitude).toString();
    }

    /**
     * helper function to convert the passed in date to timestamp-string
     *
     * @param date date to convert
     * @return date as timestamp-string
     */
    private String convertLastSentDate(Date date) {
        if (date == null) {
            return "not available";
        } else {
            DateFormat format = DateFormat.getDateInstance();
            return format.format(date);
        }
    }
}

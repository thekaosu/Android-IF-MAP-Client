/* 
 * MessageParametersGenerator.java        0.2 2015-03-08
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

package de.simu.decoit.android.decomap.messaging;

import android.hardware.Camera;
import android.os.Build;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hshannover.f4.trust.ifmapj.IfmapJ;
import de.hshannover.f4.trust.ifmapj.binding.IfmapStrings;
import de.hshannover.f4.trust.ifmapj.identifier.Device;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.identifier.Identity;
import de.hshannover.f4.trust.ifmapj.identifier.IpAddress;
import de.hshannover.f4.trust.ifmapj.messages.MetadataLifetime;
import de.hshannover.f4.trust.ifmapj.messages.PublishDelete;
import de.hshannover.f4.trust.ifmapj.messages.PublishRequest;
import de.hshannover.f4.trust.ifmapj.messages.PublishUpdate;
import de.hshannover.f4.trust.ifmapj.messages.Requests;
import de.hshannover.f4.trust.ifmapj.metadata.Cardinality;
import de.hshannover.f4.trust.ifmapj.metadata.LocationInformation;
import de.hshannover.f4.trust.ifmapj.metadata.StandardIfmapMetadataFactory;
import de.simu.decoit.android.decomap.activities.MainActivity;
import de.simu.decoit.android.decomap.device.DeviceProperties;
import de.simu.decoit.android.decomap.device.application.ApplicationListEntry;
import de.simu.decoit.android.decomap.device.application.Permission;
import de.simu.decoit.android.decomap.device.system.SystemProperties;
import de.simu.decoit.android.decomap.observer.sms.SMSObserver.SmsInfos;
import de.simu.decoit.android.decomap.preferences.PreferencesValues;
import de.simu.decoit.android.decomap.util.DateUtil;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * A generic class that generates message-parameters for the IfmapJ-lib
 *
 * @param <T>
 * @author Marcel Jahnke, DECOIT GmbH
 * @author Dennis Dunekacke, DECOIT GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class MessageParametersGenerator<T> {

    // some predefined values
    final static String OTHER_TYPE_DEFINITION = "32939:category";
    final static String NAMESPACE = "http://www.esukom.de/2012/ifmap-metadata/1";
    final static String NAMESPACE_PREFIX = "esukom";
    final static String QUANT = "quantitive";
    final static String ARBIT = "arbitrary";
    final static String QUALI = "qualified";

    // flag detecting that a previous publish-request has been send
    // (to handle deletion of previous send data)
    private static boolean sInitialLocationWasSend = false;
    public static boolean sInitialDevCharWasSend = false;

    // create device identifier
    private Device deviceIdentifier;

    // location information
    private String mLastLatitude;
    private String mLastLongitude;

    // request-type
    private T mRequest = null;

    private Identity mPhoneSystemCat;
    private Identity mPhoneDeviceCat;
    private Identity mPhoneAndroidCat;
    private Identity mPhoneOsCat;
    private Identity mPhoneSensorCat;
    private Identity mPhoneSMSCat;
    private Identity mPhoneIpCat;
    private Identity mPhoneBatteryCat;
    private Identity mPhoneMemoryCat;

    private Document mSubCategoryOf;

    // values for features that need to be republished because they can change at runtime
    private String mLastBatStat;
    private String mLastCpuLoad;
    private String mLastFreeRam;
    private String mLastProcCount;
    private String mLastSmsRecCount;
    private String mLastSmsSentCount;
    private String mLastSmsSentDate;
    private String mLastRxBytesTotal;
    private String mLastTxBytesTotal;
    private ArrayList<ApplicationListEntry> mLastAppList;
    private String mLastIpAddress;
    private boolean mLastCameraIsUsed;

    private final PreferencesValues mPreferences = PreferencesValues.getInstance();
    private final MessageParameter mp = MessageParameter.getInstance();
    private final IfMapMetadataDocumentCreator ifMapCreator = IfMapMetadataDocumentCreator.getInstance();

    /**
     * generate request-parameters for use with ssrc-connection
     *
     * @param messageType      type of message to generate
     * @param deviceProperties DeviceProperties object that contains the properties of the device
     * @return A PublishRequest or SubscribeRequest object message
     */
    @SuppressWarnings("unchecked")
    public T generateSRCRequestParamteres(byte messageType, DeviceProperties deviceProperties, boolean useNonConformMetadata,
                                          boolean dontSendAppInfos, boolean dontSendGoogleApps) {

        // this hack is needed in order to match this data to our training data
        // initialize device identifier, no real salt useful for us
        //String imei = deviceProperties.getPhoneProperties().getIMEI();
        //deviceIdentifier = Identifiers.createDev(anonymize(imei));

        // changed value of device-identifier to mac-address as value
        // (changes due to demonstrator - usecase3)
        if (deviceProperties.getSystemProperties().getMAC() == null) {
            String mac = Toolbox.getMACAddress("eth0");
            if (mac.equals("UNKNOWN EMULATOR")) {
                mac = Toolbox.getMACAddress("wlan0");
            }
            deviceIdentifier = Identifiers.createDev(mac);
        } else {
            deviceIdentifier = Identifiers.createDev(deviceProperties.getSystemProperties().getMAC());
        }

        // create metadata-factory
        StandardIfmapMetadataFactory metadataFactory = IfmapJ.createStandardMetadataFactory();

        // Current time in specified format according to ifmap-spec
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ssZ", Locale.getDefault());
        Date nowTime = new Date();

        // create ip-address identifier
        IpAddress ipAddress = Identifiers.createIp4(deviceProperties.getSystemProperties().getLocalIpAddress());

        switch (messageType) {

        /* no parameters needed for new session request */
            case MessageHandler.MSG_TYPE_REQUEST_NEWSESSION:
                break;

        /* no parameters needed for renew session */
            case MessageHandler.MSG_TYPE_REQUEST_RENEWSESSION:
                break;

        /* end session request */
            case MessageHandler.MSG_TYPE_REQUEST_ENDSESSION:
                sInitialDevCharWasSend = false;
                sInitialLocationWasSend = false;
                restLastValues();
                break;

        /* send location data request */
            case MessageHandler.MSG_TYPE_METADATA_UPDATE:
                PublishRequest publishRequest = Requests.createPublishReq();
                PublishUpdate publishLocationUpdate = Requests.createPublishUpdate();


                // append device-characteristics-metadata
                createDeviceCharacteristicsMetadataGraph(publishRequest, deviceIdentifier, deviceProperties, simpledateformat, nowTime,
                        ipAddress);

                // also append esukom-specific data if related option is set
                if (useNonConformMetadata) {
                    createEsukomSpecificDeviceCharacteristicsMetadataGraph(publishRequest, deviceIdentifier, nowTime,
                            deviceProperties, dontSendAppInfos, dontSendGoogleApps);
                }

                if (mPreferences.isEnableLocationTracking() && (mp.getLatitude() != null && mp.getLongitude() != null)) {
                    // if a previous location has been send, add publish-delete
                    if (sInitialLocationWasSend) {
                        PublishDelete deleteLastLocation = Requests.createPublishDelete();
                        deleteLastLocation.addNamespaceDeclaration(IfmapStrings.STD_METADATA_PREFIX, IfmapStrings.STD_METADATA_NS_URI);
                        String string_filter = "meta:location[" + "@ifmap-publisher-id=\'" + MainActivity.sCurrentPublisherId + "\']";
                        deleteLastLocation.setFilter(string_filter);
                        deleteLastLocation.setIdentifier1(ipAddress);
                        publishRequest.addPublishElement(deleteLastLocation);
                    }

                    // build publish-location-update-request
                    publishLocationUpdate.setIdentifier1(ipAddress);
                    List<LocationInformation> locationInfoList = new ArrayList<>();
                    LocationInformation locationInformation = new LocationInformation(mPreferences.getLocationTrackingType(),
                            mp.getLatitude() + " " + mp.getLongitude());
                    locationInfoList.add(locationInformation);
                    Document location = metadataFactory.createLocation(locationInfoList, simpledateformat.format(nowTime),
                            MainActivity.sCurrentPublisherId);
                    publishLocationUpdate.addMetadata(location);
                    publishRequest.addPublishElement(publishLocationUpdate);
                }

                sInitialDevCharWasSend = true;
                sInitialLocationWasSend = true;
                mRequest = (T) publishRequest;
                break;

        /* send device characteristics data request */
            case MessageHandler.MSG_TYPE_PUBLISH_CHARACTERISTICS:
                publishRequest = Requests.createPublishReq();

                // append device-characteristics-metadata
                createDeviceCharacteristicsMetadataGraph(publishRequest, deviceIdentifier, deviceProperties, simpledateformat, nowTime,
                        ipAddress);

                // also append esukom-specific data if related option is set
                if (useNonConformMetadata) {
                    createEsukomSpecificDeviceCharacteristicsMetadataGraph(publishRequest, deviceIdentifier, nowTime,
                            deviceProperties, dontSendAppInfos, dontSendGoogleApps);
                }


                sInitialDevCharWasSend = true;
                mRequest = (T) publishRequest;
                break;

        /* if all fails */
            default:
                Toolbox.logTxt(this.getClass().getName(),
                        "Error while building publish request...Messagetype for publish-request could not be found!");
                break;
        }

        return mRequest;
    }

    /**
     * reset all mLast* values
     */
    private void restLastValues() {
        mLastLatitude = null;
        mLastAppList = null;
        mLastBatStat = null;
        mLastCpuLoad = null;
        mLastFreeRam = null;
        mLastIpAddress = null;
        mLastLongitude = null;
        mLastProcCount = null;
        mLastRxBytesTotal = null;
        mLastRxBytesTotal = null;
        mLastSmsRecCount = null;
        mLastSmsSentCount = null;
        mLastSmsSentDate = null;
        mLastTxBytesTotal = null;
    }

    /**
     * create device-characteristics-metadata and append it to passes in publish-request-object
     *
     * @param publishRequest   publish-request to send
     * @param deviceIdentifier root-identifier for the device
     * @param deviceProperties device-properties object containing device-related informations to be appended to the metadata-graph
     * @param simpledateformat date-formatter
     * @param nowTime          current time
     * @param ipAddress        IP-Identifier
     */
    private void createDeviceCharacteristicsMetadataGraph(PublishRequest publishRequest, Device deviceIdentifier,
                                                          DeviceProperties deviceProperties, SimpleDateFormat simpledateformat, Date nowTime, IpAddress ipAddress) {
        Document document = ifMapCreator.createStdSingleElementDocument("device-characteristic", Cardinality.multiValue);
        Element root = (Element) document.getFirstChild();

        // append standard conform meta-data to device-characteristics-metadata
        // the order is important(!) and must match the order from the spec.
        ifMapCreator.appendTextElementIfNotNull(document, root, "manufacturer", deviceProperties.getPhoneProperties().getManufacturer());
        ifMapCreator.appendTextElementIfNotNull(document, root, "model", deviceProperties.getPhoneProperties().getManufacturer());
        ifMapCreator.appendTextElementIfNotNull(document, root, "os", "Android");
        ifMapCreator.appendTextElementIfNotNull(document, root, "os-version", deviceProperties.getPhoneProperties().getFirmwareVersion());
        ifMapCreator.appendTextElementIfNotNull(document, root, "device-type", "remote-access-device");
        ifMapCreator.appendTextElementIfNotNull(document, root, "discovered-time", simpledateformat.format(nowTime));
        ifMapCreator.appendTextElementIfNotNull(document, root, "discoverer-id", MainActivity.sCurrentPublisherId);
        ifMapCreator.appendTextElementIfNotNull(document, root, "discovery-method", "scan");

        // add device and ip-identifier to update, append meta-data
        addToUpdateRequest(publishRequest, ipAddress, deviceIdentifier, document, MetadataLifetime.session, true);
    }

    /**
     * create metadata-graph for esukom-specific metadata
     *
     * @param publishRequest publish-request to send
     * @param deviceIdent    root-identifier for the device
     * @param nowTime        current time
     * @param devProps       device-properties object containing device-related informations to be appended to the metadata-graph
     */
    private void createEsukomSpecificDeviceCharacteristicsMetadataGraph(PublishRequest publishRequest, Device deviceIdent,
                                                                        Date nowTime, DeviceProperties devProps, boolean dontSendAppInfos, boolean dontSendGoogleApps) {

        // feature-document and current time
        Document fe;
        String time = DateUtil.getTimestampXsd(nowTime.getTime());

        // republish informations-flag
        boolean locationChanged = false;
        if (mPreferences.isEnableLocationTracking() && (mp.getLatitude() != null && mp.getLongitude() != null)) {
            if (!mLastLatitude.equals(mp.getLatitude()) || !mLastLongitude.equals(mp.getLongitude())) {
                mLastLatitude = mp.getLatitude();
                mLastLongitude = mp.getLongitude();
                locationChanged = true;
            }
        }

        // only send in initial publish
        if (!sInitialDevCharWasSend) {
            // categories
            Identity mPhoneCat = ifMapCreator.createCategory("smartphone", deviceIdentifier.getName());
            mPhoneSystemCat = ifMapCreator.createCategory("smartphone.system", deviceIdentifier.getName());
            mPhoneDeviceCat = ifMapCreator.createCategory("smartphone.device", deviceIdentifier.getName());
            mPhoneAndroidCat = ifMapCreator.createCategory("smartphone.android", deviceIdentifier.getName());
            mPhoneOsCat = ifMapCreator.createCategory("smartphone.android.os", deviceIdentifier.getName());
            mPhoneSensorCat = ifMapCreator.createCategory("smartphone.sensor", deviceIdentifier.getName());
            Identity mPhoneCommunicationCat = ifMapCreator.createCategory("smartphone.communication", deviceIdentifier.getName());
            mPhoneSMSCat = ifMapCreator.createCategory("smartphone.communication.sms", deviceIdentifier.getName());
            mPhoneIpCat = ifMapCreator.createCategory("smartphone.communication.ip", deviceIdentifier.getName());
            mPhoneBatteryCat = ifMapCreator.createCategory("smartphone.system.battery", deviceIdentifier.getName());
            mPhoneMemoryCat = ifMapCreator.createCategory("smartphone.system.memory", deviceIdentifier.getName());
            Document mDeviceCategory = ifMapCreator.createCategoryLink("device-category");
            mSubCategoryOf = ifMapCreator.createCategoryLink("subcategory-of");

            addToUpdateRequest(publishRequest, deviceIdent, mPhoneCat, mDeviceCategory, MetadataLifetime.session, false);
            addToUpdateRequest(publishRequest, mPhoneCat, mPhoneSystemCat, mSubCategoryOf, MetadataLifetime.session, false);
            addToUpdateRequest(publishRequest, mPhoneCat, mPhoneDeviceCat, mSubCategoryOf, MetadataLifetime.session, false);
            addToUpdateRequest(publishRequest, mPhoneCat, mPhoneAndroidCat, mSubCategoryOf, MetadataLifetime.session, false);
            addToUpdateRequest(publishRequest, mPhoneAndroidCat, mPhoneOsCat, mSubCategoryOf, MetadataLifetime.session, false);
            addToUpdateRequest(publishRequest, mPhoneCat, mPhoneSensorCat, mSubCategoryOf, MetadataLifetime.session, false);
            addToUpdateRequest(publishRequest, mPhoneCat, mPhoneCommunicationCat, mSubCategoryOf, MetadataLifetime.session, false);
            addToUpdateRequest(publishRequest, mPhoneCommunicationCat, mPhoneSMSCat, mSubCategoryOf, MetadataLifetime.session, false);
            addToUpdateRequest(publishRequest, mPhoneCommunicationCat, mPhoneIpCat, mSubCategoryOf, MetadataLifetime.session, false);
            addToUpdateRequest(publishRequest, mPhoneSystemCat, mPhoneBatteryCat, mSubCategoryOf, MetadataLifetime.session, false);
            addToUpdateRequest(publishRequest, mPhoneSystemCat, mPhoneMemoryCat, mSubCategoryOf, MetadataLifetime.session, false);
        }

        // the following features are static and will not change during runtime
        // only republish them if the current location has been changed
        if (!sInitialDevCharWasSend || locationChanged) {
            fe = ifMapCreator.createFeature("MAC", time, devProps.getSystemProperties().getMAC(), QUALI);
            addToUpdateRequest(publishRequest, mPhoneDeviceCat, null, fe, MetadataLifetime.session, true);
            fe = ifMapCreator.createFeature("Manufacturer", time, devProps.getPhoneProperties().getManufacturer(), ARBIT);
            addToUpdateRequest(publishRequest, mPhoneDeviceCat, null, fe, MetadataLifetime.session, true);
            fe = ifMapCreator.createFeature("Branding", time, devProps.getPhoneProperties().getBranding(), ARBIT);
            addToUpdateRequest(publishRequest, mPhoneDeviceCat, null, fe, MetadataLifetime.session, true);
            fe = ifMapCreator.createFeature("IMEI", time, ifMapCreator.anonymize(devProps.getPhoneProperties().getIMEI()), QUANT);
            addToUpdateRequest(publishRequest, mPhoneDeviceCat, null, fe, MetadataLifetime.session, true);
            fe = ifMapCreator.createFeature("IMSI", time, ifMapCreator.anonymize(devProps.getPhoneProperties().getIMSI()), QUANT);
            addToUpdateRequest(publishRequest, mPhoneDeviceCat, null, fe, MetadataLifetime.session, true);
            fe = ifMapCreator.createFeature("KernelVersion", time, devProps.getSystemProperties().getKernelVersion(), ARBIT);
            addToUpdateRequest(publishRequest, mPhoneOsCat, null, fe, MetadataLifetime.session, true);
            fe = ifMapCreator.createFeature("FirmwareVersion", time, devProps.getPhoneProperties().getFirmwareVersion(), ARBIT);
            addToUpdateRequest(publishRequest, mPhoneOsCat, null, fe, MetadataLifetime.session, true);
            fe = ifMapCreator.createFeature("BasebandVersion", time, devProps.getPhoneProperties().getBasebandVersion(), ARBIT);
            addToUpdateRequest(publishRequest, mPhoneOsCat, null, fe, MetadataLifetime.session, true);
            fe = ifMapCreator.createFeature("BuildNumber", time, devProps.getPhoneProperties().getBuildNumber(), ARBIT);
            addToUpdateRequest(publishRequest, mPhoneOsCat, null, fe, MetadataLifetime.session, true);
        }

        // features that can change during runtime

        // Battery
        String mCurBatStat = mp.getCurrentBatteryLevel();
        if (!mCurBatStat.equals(mLastBatStat) || locationChanged) {
            fe = ifMapCreator.createFeature("Level", time, mCurBatStat, QUANT);
            addToUpdateRequest(publishRequest, mPhoneBatteryCat, null, fe, MetadataLifetime.session, true);
            mLastBatStat = mCurBatStat;
        }

        // IP
        String currentIpAddress = devProps.getSystemProperties().getLocalIpAddress();
        if (!currentIpAddress.equals(mLastIpAddress) || locationChanged) {
            fe = ifMapCreator.createFeature("IpAddress", time, currentIpAddress, QUALI);
            addToUpdateRequest(publishRequest, mPhoneDeviceCat, null, fe, MetadataLifetime.session, true);
            mLastIpAddress = currentIpAddress;
        } else {
            Toolbox.logTxt("MessageParametersGenerator", "IpAddress unchanged.");
        }


        String mCurCpuLoad = String.valueOf(devProps.getSystemProperties().getCurCpuLoadPercent());
        if (!mCurCpuLoad.equals(mLastCpuLoad) || locationChanged) {
            fe = ifMapCreator.createFeature("CPULoad", time, mCurCpuLoad, QUANT);
            addToUpdateRequest(publishRequest, mPhoneSystemCat, null, fe, MetadataLifetime.session, true);
            mLastCpuLoad = mCurCpuLoad;
        }

        String mCurFreeRam = String.valueOf(devProps.getSystemProperties().getFreeRamInBytes());
        if (!mCurFreeRam.equals(mLastFreeRam) || locationChanged) {
            fe = ifMapCreator.createFeature("MemoryAvailable", time, mCurFreeRam, QUANT);
            addToUpdateRequest(publishRequest, mPhoneMemoryCat, null, fe, MetadataLifetime.session, true);
            mLastFreeRam = mCurFreeRam;
        }

        String mCurProcCount = String.valueOf(devProps.getApplicationProperties().getRuningProcCount());
        if (!mCurProcCount.equals(mLastProcCount) || locationChanged) {
            fe = ifMapCreator.createFeature("ProcessCount", time, mCurProcCount, QUANT);
            addToUpdateRequest(publishRequest, mPhoneSystemCat, null, fe, MetadataLifetime.session, true);
            mLastProcCount = mCurProcCount;
        }

        /* traffic stuff rx/tx 3g / other */
        String currentTxBytesTotal = String.valueOf(SystemProperties.getTotalTxBytes());
        String currentRxBytesTotal = String.valueOf(SystemProperties.getTotalRxBytes());
        if (!currentTxBytesTotal.equals(mLastTxBytesTotal) ||
                !currentRxBytesTotal.equals(mLastRxBytesTotal) ||
                locationChanged) {
            // we need to sent new features
            fe = ifMapCreator.createFeature("Rx3g", time, String.valueOf(SystemProperties.getRxBytes3G()), QUANT);
            addToUpdateRequest(publishRequest, mPhoneIpCat, null, fe, MetadataLifetime.session, true);
            fe = ifMapCreator.createFeature("Tx3g", time, String.valueOf(SystemProperties.getTxBytes3G()), QUANT);
            addToUpdateRequest(publishRequest, mPhoneIpCat, null, fe, MetadataLifetime.session, true);
            fe = ifMapCreator.createFeature("RxOther", time, String.valueOf(SystemProperties.getRxBytesOther()), QUANT);
            addToUpdateRequest(publishRequest, mPhoneIpCat, null, fe, MetadataLifetime.session, true);
            fe = ifMapCreator.createFeature("TxOther", time, String.valueOf(SystemProperties.getTxBytesOther()), QUANT);
            addToUpdateRequest(publishRequest, mPhoneIpCat, null, fe, MetadataLifetime.session, true);

            // remember last state
            mLastRxBytesTotal = currentRxBytesTotal;
            mLastTxBytesTotal = currentTxBytesTotal;
        }

        // added outgoing_sms and incoming_sms
        for (SmsInfos smsInfo : mp.getIncomingSms()) {
            fe = ifMapCreator.createFeature("IncomingSms", DateUtil.getTimestampXsd(smsInfo.getDate().getTime()), "Incoming SMS to " + smsInfo.getAddress(), ARBIT);
            addToUpdateRequest(publishRequest, mPhoneSMSCat, null, fe, MetadataLifetime.session, true);
        }
        for (SmsInfos smsInfo : mp.getOutgoingSms()) {
            fe = ifMapCreator.createFeature("OutgoingSms", DateUtil.getTimestampXsd(smsInfo.getDate().getTime()), "Outgoing SMS to " + smsInfo.getAddress(), ARBIT);
            addToUpdateRequest(publishRequest, mPhoneSMSCat, null, fe, MetadataLifetime.session, true);
        }
        mp.resetSmsInfos();

        String mCurSmsRecCount = String.valueOf(mp.getSmsInCount());
        if (!mCurSmsRecCount.equals(mLastSmsRecCount) || locationChanged) {
            fe = ifMapCreator.createFeature("ReceivedCount", time, mCurSmsRecCount, QUANT);
            addToUpdateRequest(publishRequest, mPhoneSMSCat, null, fe, MetadataLifetime.session, true);
            mLastSmsRecCount = mCurSmsRecCount;
        }

        String mCurSmsSentCount = String.valueOf(mp.getSmsSentCount());
        if (!mCurSmsSentCount.equals(mLastSmsSentCount) || locationChanged) {
            fe = ifMapCreator.createFeature("SentCount", time, mCurSmsSentCount, QUANT);
            addToUpdateRequest(publishRequest, mPhoneSMSCat, null, fe, MetadataLifetime.session, true);
            mLastSmsSentCount = mCurSmsSentCount;
        }

        Date lastSendDate = mp.getLastSendDate();
        if (lastSendDate != null) {
            String mCurSmsSentDate = DateFormat.getInstance().format(mp.getLastSendDate());
            if (!mCurSmsSentDate.equals(mLastSmsSentDate) || locationChanged) {
                fe = ifMapCreator.createFeature("LastSent", time, mCurSmsSentDate, QUANT);
                addToUpdateRequest(publishRequest, mPhoneSMSCat, null, fe, MetadataLifetime.session, true);
                mLastSmsSentDate = mCurSmsSentDate;
            }
        }

        // last sensor that was used. currently only the camera sensor can be used so
        // we just use that...as soon as other sensors are observed, we need a method
        // to compare the dates of the sensors that are used to find out which of them
        // was the last one in use...
        Date lastCameraUsedDate = mp.getLastPictureTakenDate();
        if (lastCameraUsedDate != null) {
            fe = ifMapCreator.createFeature("NewPicture", time, "true", QUALI);
            addToUpdateRequest(publishRequest, mPhoneSensorCat, null, fe, MetadataLifetime.session, true);
        }

        // back camera used?
//        MainActivity.checkCameraActive();
        boolean isUsed = isCameraUsed();
        String isUsedStr = isUsed ? "true" : "false";
        Toolbox.logTxt("MessageParametersGenerator", "CameraIsUsed = " + isUsed);
        if (isUsed != mLastCameraIsUsed || !sInitialLocationWasSend) {
            //Log.i("###", "MUST PUBLISH HERE");
            fe = ifMapCreator.createFeature("CameraIsUsed", time, isUsedStr, QUALI);
            addToUpdateRequest(publishRequest, mPhoneSensorCat, null, fe, MetadataLifetime.session, true);
            mLastCameraIsUsed = isUsed;
        }


        // send application information?
        if (!dontSendAppInfos) {
            ArrayList<ApplicationListEntry> currentAppList = devProps.getApplicationProperties().getApplicationList(dontSendGoogleApps,
                    true, true, true);

            for (int i = 0; i < currentAppList.size(); i++) {
                // create app-category-identifier
                Identity phoneAppCat = ifMapCreator.createCategory("smartphone.android.app:" + i, deviceIdent.getName());

                boolean appEntryExists = (mLastAppList != null && i < mLastAppList.size() && mLastAppList.get(i) != null);
                /*
                 * if the name of the application differs from last application-list, then the application seems to have changed and
                 * therefore we need to (re)publish all application-informations
                 */
                boolean entryNameChanged = false;

                // smartphone.android.app.Name
                if (!sInitialDevCharWasSend || locationChanged
                        || (appEntryExists && !mLastAppList.get(i).getName().equals(currentAppList.get(i).getName()))) {
                    addToUpdateRequest(publishRequest, mPhoneAndroidCat, phoneAppCat, mSubCategoryOf, MetadataLifetime.session, false);
                    entryNameChanged = true;
                    fe = ifMapCreator.createFeature("Name", time, currentAppList.get(i).getName(), ARBIT);
                    addToUpdateRequest(publishRequest, phoneAppCat, null, fe, MetadataLifetime.session, true);
                }

                // smartphone.android.app.Installer
                if (!sInitialDevCharWasSend || entryNameChanged || (appEntryExists && !mLastAppList.get(i).getInstallerPackageName().equals(currentAppList.get(i).getInstallerPackageName()))) {
                    fe = ifMapCreator.createFeature("Installer", time, currentAppList.get(i).getInstallerPackageName(), ARBIT);
                    addToUpdateRequest(publishRequest, phoneAppCat, null, fe, MetadataLifetime.session, true);
                }

                // smartphone.android.app.VersionName and VersionCode
                if (!sInitialDevCharWasSend || entryNameChanged || (appEntryExists && !mLastAppList.get(i).getVersionName().equals(currentAppList.get(i).getVersionName()))) {
                    fe = ifMapCreator.createFeature("VersionName", time, currentAppList.get(i).getVersionName(), ARBIT);
                    addToUpdateRequest(publishRequest, phoneAppCat, null, fe, MetadataLifetime.session, true);
                    fe = ifMapCreator.createFeature("VersionCode", time, currentAppList.get(i).getVersionCode() + "", QUANT);
                    addToUpdateRequest(publishRequest, phoneAppCat, null, fe, MetadataLifetime.session, true);
                }

                // smartphone.android.app.IsRunning
                if (!sInitialDevCharWasSend || entryNameChanged || (appEntryExists && !mLastAppList.get(i).isCurrentlyRunning() == currentAppList.get(i).isCurrentlyRunning())) {
                    fe = ifMapCreator.createFeature("IsRunning", time,
                            String.valueOf(currentAppList.get(i).isCurrentlyRunning()), ARBIT);
                    addToUpdateRequest(publishRequest, phoneAppCat, null, fe, MetadataLifetime.session, true);
                }

                // compare current permissions with "old" permission
                ArrayList<Permission> oldPermissionList = null;
                if (appEntryExists) {
                    oldPermissionList = mLastAppList.get(i).getPermissions();
                }
                ArrayList<Permission> newPermissionList = currentAppList.get(i).getPermissions();

                for (int j = 0; j < newPermissionList.size(); j++) {
                    if (!sInitialDevCharWasSend || entryNameChanged || (!appEntryExists || !oldPermissionList.contains(newPermissionList.get(j)))) {

                        // smartphone.android.app.permission
                        Identity phoneAppPermCat = ifMapCreator.createCategory("smartphone.android.app:" + i + ".permission:" + j,
                                deviceIdent.getName());
                        addToUpdateRequest(publishRequest, phoneAppCat, phoneAppPermCat, mSubCategoryOf, MetadataLifetime.session, false);

                        if (newPermissionList.get(j).getPermissionType() == Permission.PERMISSIONTYPE_GRANTED) {
                            // smartphone.app.permission.granted
                            fe = ifMapCreator.createFeature("Granted", time, newPermissionList.get(j)
                                    .getPermissionName(), ARBIT);
                            addToUpdateRequest(publishRequest, phoneAppPermCat, null, fe, MetadataLifetime.session, true);
                        } else {
                            // smartphone.app.permission.requries, currently the same as permissions.Granted
                            fe = ifMapCreator.createFeature("Required", time, newPermissionList.get(j)
                                    .getPermissionName(), ARBIT);
                            addToUpdateRequest(publishRequest, phoneAppPermCat, null, fe, MetadataLifetime.session, true);
                        }
                    }
                }
            }
            mLastAppList = currentAppList;
        }
    }

    /**
     * Is the Camera at the moment in use
     * <p/>
     * If Buildversion is lower than Lollipop, then the camera status need to be get manually!
     *
     * @return Is the Camera at the moment in use
     */
    @SuppressWarnings("deprecation")
    private boolean isCameraUsed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Camera cam;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int cameraCount = Camera.getNumberOfCameras();
            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    try {
                        cam = Camera.open(camIdx);
                        mPreferences.setCamActiv(camIdx + "", false);
                        cam.release();
                    } catch (RuntimeException e) {
                        Toolbox.logTxt(this.getClass().getName(),
                                "Camera failed to open: "
                                        + e.getLocalizedMessage());
                        mPreferences.setCamActiv(camIdx + "", true);
                    }
                }
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        cam = Camera.open(camIdx);
                        mPreferences.setCamActiv(camIdx + "", false);
                        cam.release();
                    } catch (RuntimeException e) {
                        Toolbox.logTxt(this.getClass().getName(),
                                "Camera failed to open: "
                                        + e.getLocalizedMessage());
                        mPreferences.setCamActiv(camIdx + "", true);
                    }
                }
            }
        }
        return mPreferences.isAnyCamActiv();
    }

    /**
     * create new publish-update and add it to passed in publish-request if the data has already been send, append a publish-delete to erase
     * the previously published data
     *
     * @param request          publish-request-object to add updates to
     * @param ident1           first identifier for update-request
     * @param ident2           second identifier for update-request
     * @param metadata         metadata to append to identifier(s)
     * @param metadataLifeTime lifetime of metadata
     * @param doDelete         flag to decide if an automatic delete request is appended
     */
    private void addToUpdateRequest(PublishRequest request, Identifier ident1, Identifier ident2, Document metadata,
                                    MetadataLifetime metadataLifeTime, boolean doDelete) {
        // add publish-update to request
        PublishUpdate publishUpdate = Requests.createPublishUpdate();
        publishUpdate.setIdentifier1(ident1);
        if (ident2 != null) {
            publishUpdate.setIdentifier2(ident2);
        }
        publishUpdate.addMetadata(metadata);
        publishUpdate.setLifeTime(metadataLifeTime);

        // if a previous message has been send, add publish-delete
        if (sInitialDevCharWasSend && doDelete) {
            PublishDelete publishDelete = Requests.createPublishDelete();
            // standard-metadata
            if (metadata.getChildNodes().item(0).getPrefix().equals("meta")) {
                publishDelete.addNamespaceDeclaration(IfmapStrings.STD_METADATA_PREFIX, IfmapStrings.STD_METADATA_NS_URI);
                publishDelete.setFilter(metadata.getChildNodes().item(0).getPrefix() + ":"
                        + metadata.getChildNodes().item(0).getLocalName() + "[" + "@ifmap-publisher-id=\'"
                        + MainActivity.sCurrentPublisherId + "\']");
            }
            // esukom-specific-metadata
            else if (metadata.getElementsByTagName("id").item(0) != null) {
                publishDelete.addNamespaceDeclaration(NAMESPACE_PREFIX, NAMESPACE);
                publishDelete.setFilter(metadata.getChildNodes().item(0).getPrefix() + ":"
                        + metadata.getChildNodes().item(0).getLocalName() + "[" + "id='"
                        + metadata.getElementsByTagName("id").item(0).getTextContent() + "'" + " and " + "@ifmap-publisher-id=\'"
                        + MainActivity.sCurrentPublisherId + "\']");
            }
            publishDelete.setIdentifier1(ident1);
            if (ident2 != null) {
                publishDelete.setIdentifier2(ident2);
            }
            request.addPublishElement(publishDelete);
        }
        request.addPublishElement(publishUpdate);
    }
}

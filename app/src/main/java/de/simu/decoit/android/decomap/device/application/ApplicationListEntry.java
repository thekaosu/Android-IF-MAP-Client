/* 
 * ApplicationListEntry..java          0.3 2015-03-08
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
package de.simu.decoit.android.decomap.device.application;

import java.util.ArrayList;

/**
 * class representing an application-list-entry
 *
 * @author Dennis Dunekacke, Decoit Gmbh
 * @version 0.3
 */
public class ApplicationListEntry {

    private final String name;
    private String versionName;
    private int versionCode;
    private String installerPackageName;
    private final ArrayList<Permission> permissions = new ArrayList<>();
    private boolean isCurrentlyRunning = false;

    /**
     * constructor
     *
     * @param name application-name
     */
    public ApplicationListEntry(String name) {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the android:versionName as specified in the manifest
     */
    public String getVersionName() {
        return versionName;
    }

    /**
     * @param versionName the android:versionName as specified in the manifest
     */
    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    /**
     * @return the android:versionCode as specified in the manifest
     */
    public int getVersionCode() {
        return versionCode;
    }

    /**
     * @param versionCode the android:versionCode as specified in the manifest
     */
    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    /**
     * @return the permissions
     */
    public ArrayList<Permission> getPermissions() {
        return permissions;
    }


    /**
     * add a permission-string to permissions-lists depending
     * on type of the permission (granted/required)
     *
     * @param permissionName permission-string to be added
     */
    public void addPermission(String permissionName) {
        // granted permission
        if (permissionName.startsWith("android.permission")) {
            this.permissions.add(new Permission(permissionName, Permission.PERMISSIONTYPE_GRANTED));
        }
        // required permission
        else {
            this.permissions.add(new Permission(permissionName, Permission.PERMISSIONTYPE_REQUIRED));
        }
    }

    /**
     * @return the isCurrentlyRunning
     */
    public boolean isCurrentlyRunning() {
        return isCurrentlyRunning;
    }

    /**
     * @param isCurrentlyRunning the isCurrentlyRunning to set
     */
    public void setCurrentlyRunning(boolean isCurrentlyRunning) {
        this.isCurrentlyRunning = isCurrentlyRunning;
    }

    /**
     * @return the installerPackageName
     */
    public String getInstallerPackageName() {
        if (installerPackageName != null) {
            return installerPackageName;
        } else {
            return "";
        }
    }

    /**
     * @param installerPackageName the installerPackageName to set
     */
    public void setInstallerPackageName(String installerPackageName) {
        this.installerPackageName = installerPackageName;
    }
}
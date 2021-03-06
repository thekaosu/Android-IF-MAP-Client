/* 
 * Permission..java          0.3 2015-03-08
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

/**
 * class representing an permission
 * 
 * @version 0.3
 * @author Dennis Dunekacke, Decoit Gmbh
 */
public class Permission {

	public static final byte PERMISSIONTYPE_GRANTED = 0;
	public static final byte PERMISSIONTYPE_REQUIRED = 1;

	private final String permissionName;
	private final byte permissionType;

	/**
	 * constructor
	 * 
	 * @param name
	 *            permission name
	 * @param type
	 *            permission type
	 */
	public Permission(String name, byte type) {
		this.permissionName = name;
		this.permissionType = type;
	}

	/**
	 * @return the permissionName
	 */
	public String getPermissionName() {
		return permissionName;
	}

	/**
	 * @return the permissionType
	 */
	public byte getPermissionType() {
		return permissionType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final Permission permission = (Permission) o;

		return !(permissionName != null ? !permissionName
				.equals(permission.permissionName)
				: permission.permissionName != null);
	}
}

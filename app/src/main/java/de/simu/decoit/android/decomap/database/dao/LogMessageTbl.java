/* 
 * LogMessageTbl..java          0.3 2015-03-08
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
package de.simu.decoit.android.decomap.database.dao;

/**
 * class containing some static predefined values and sql-querys
 * for the log-message table
 *
 * @author Dennis Dunekacke, Decoit GmH
 * @version 0.3
 */
public final class LogMessageTbl implements LogMessageColums {

    public static final String TABLE_NAME = "logmessages";

    public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + TIMESTAMP + " TEXT NOT NULL," + MESSAGETYPE + " TEXT NOT NULL,"
            + MESSAGECONTENT + " TEXT," + TARGET + " TEXT,"
            + STATUS + " TEXT" + ");";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String STMT_LOGMESSAGE_INSERT = "INSERT INTO " + TABLE_NAME
            + " (" + TIMESTAMP + "," + MESSAGETYPE + "," + MESSAGECONTENT + "," + TARGET + "," + STATUS + ") " + "VALUES (?,?,?,?,?)";

    public static final String STMT_LOGMESSAGE_DELETE = "DELETE FROM " + TABLE_NAME;

    public static final String STMT_LOGMESSAGE_DELETE_BY_ID = "DELETE FROM " + TABLE_NAME
            + " WHERE " + ID + "= ?";

    public static final String[] ALL_COLUMS = new String[]{ID, TIMESTAMP,
            MESSAGETYPE, MESSAGECONTENT, TARGET, STATUS};                        //maybe usefull later
}

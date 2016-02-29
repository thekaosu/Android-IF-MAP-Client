/* 
 * LogActivity.java        0.2 2015-03-08
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
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import de.simu.decoit.android.decomap.database.LoggingDatabase;
import de.simu.decoit.android.decomap.logging.LogMessage;
import de.simu.decoit.android.decomap.logging.LogMessageAdapter;
import de.simu.decoit.android.decomap.logging.LogMessageDialog;
import de.simu.decoit.android.decomap.preferences.PreferencesValues;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * Activity for showing Log-Messages
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class LogActivity extends Activity {

    // database for log messages
    private LoggingDatabase mLogDB = null;
    private final PreferencesValues mPreferences = PreferencesValues.getInstance();

    //scroll position
    private ListView logViewList;
    private int scrollIndex = 0;
    private int scrollOffSet = 0;
    private int listCount = 0;
    // -------------------------------------------------------------------------
    // ACTIVITY LIFECYCLE HANDLING
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbox.logTxt(this.getClass().getName(), "LogActivity.OnCreate(...) called");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tab2);

        // create new database connection
        mLogDB = new LoggingDatabase(this);
    }

    @Override
    public void onResume() {
        Toolbox.logTxt(this.getClass().getName(), "LogActivity.OnResume(...) called");
        super.onResume();

        // re-create list adapter
        createListAdapter();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (logViewList != null) {
            scrollIndex = logViewList.getFirstVisiblePosition();
            View v = logViewList.getChildAt(0);
            if (v != null) {
                scrollOffSet = v.getTop() - logViewList.getPaddingTop();
            }

            listCount = logViewList.getCount();
        }
    }

    // -------------------------------------------------------------------------
    // LOG-ENTRIES-LIST OPERATIONS
    // -------------------------------------------------------------------------

    /**
     * create new log-message-list-adapter for list-view and fill it with
     * messages from database
     */
    public void createListAdapter() {
        // get view for log-msg list
        logViewList = (ListView) findViewById(R.id.logMessages_ListView);

        // get messages from database
        List<LogMessage> listOfLogMessages = getAllEntrys();

        // create and set new adapter
        LogMessageAdapter adapter = new LogMessageAdapter(this, listOfLogMessages);
        logViewList.setAdapter(adapter);

        //setting scroll position
        if (listCount == logViewList.getCount()) {
            logViewList.setSelectionFromTop(scrollIndex, scrollOffSet);
        } else {
            logViewList.setSelection(logViewList.getCount() - 1);
        }
    }

    /**
     * show message-content-Dialog-Box. Will be called by LogMessageAdapter to
     * show message-content/details
     *
     * @param msg message to show
     */
    public void showLogMessage(String msg) {
        LogMessageDialog dialog = new LogMessageDialog(this, msg);
        dialog.show();
    }

    /**
     * Handel the delete all button.
     * Deleting all log-messages.
     *
     * @return log-messages from database
     */
    public ArrayList<LogMessage> getAllEntrys() {
        // create log message list for list-view
        ArrayList<LogMessage> logMessageList = new ArrayList<>();

        // get db content
        Cursor resultCursor = null;
        try {
            resultCursor = mLogDB.getReadableDatabase().query(false, "logmessages",
                    new String[]{"_id", "timestamp", "msgtype", "msgcontent", "target", "status"}, null, null, null,
                    null, null, null);

            while (resultCursor.moveToNext()) {
                LogMessage lMsg = new LogMessage(resultCursor.getInt(0), // id
                        resultCursor.getString(1), // timestamp
                        resultCursor.getString(3), // msg-type
                        resultCursor.getString(2), // msg-content
                        resultCursor.getString(4), // target address
                        resultCursor.getString(5)); // msg-status
                logMessageList.add(lMsg);
            }
        } finally {
            if (resultCursor != null) {
                resultCursor.close();
            }
        }
        return logMessageList;
    }

    /**
     * delete log-message from database at passed in index
     *
     * @param id index to delete message from database
     */
    public void deleteEntry(int id) {
        mLogDB.deleteMessageAtId(mLogDB.getWritableDatabase(), Integer.valueOf(id).toString());
    }

    /**
     * delete all log messages from database
     */
    private void deleteLog() {
        mLogDB.deleteAllMassages(mLogDB.getWritableDatabase());
    }

    // -------------------------------------------------------------------------
    // BUTTON HANDLING
    // -------------------------------------------------------------------------

    /**
     * Handling remove all Button click.
     * Deleting log-messages
     *
     * @param view element that originated the call
     */
    public void removeAllLogEntrys(View view) {
        //Deleting all log-messages from the Database
        deleteLog();

        //updating view, by recreating the ListAdapter
        createListAdapter();
    }

    /**
     * Handling export button click.
     * Exporting all log-messages into a file
     *
     * @param view element that originated the call
     */
    public void exportLogEntrys(View view) {
// name for export file

        String exportFileName = "message_export_" + Toolbox.now(Toolbox.DATE_FORMAT_NOW_EXPORT) + ".txt";

        // prepare export
        BufferedWriter bw;
        ArrayList<LogMessage> exportList;
        LogMessageDialog dialog;


        try {
            if (!Toolbox.sLogFolderExists) {
                Toolbox.sLogFolderExists = Toolbox.createDirIfNotExists(mPreferences.getLogPath());
            }

            try {
                String path;
                if (mPreferences.getLogPath().endsWith("/")) {
                    path = mPreferences.getLogPath() + exportFileName;
                } else {
                    path = mPreferences.getLogPath() + "/" + exportFileName;
                }


                // write log-message to file
                bw = new BufferedWriter(new FileWriter(path, true));
                exportList = getAllEntrys();
                // write entries to file
                for (int i = 0; i < exportList.size(); i++) {
                    bw.newLine();
                    bw.write("----------------------------------------------");
                    bw.newLine();
                    bw.write("Type:   " + exportList.get(i).getMsgType());
                    bw.newLine();
                    bw.write("Target: " + exportList.get(i).getTarget());
                    bw.newLine();
                    bw.write("Time:   " + exportList.get(i).getTimestamp());
                    bw.newLine();
                    bw.write("----------------------------------------------");
                    bw.newLine();
                    bw.write(exportList.get(i).getMsg());
                    bw.newLine();
                }
                bw.close();

                // show quick success-message
                dialog = new LogMessageDialog(this,
                        "Messages have been successfully exported as \"" + exportFileName + "\" to the log directory " + mPreferences.getLogPath());
            } catch (Exception e) {
                dialog = new LogMessageDialog(this, "Export failed! \nReason: " + e.getMessage(), LogMessageDialog.WARNING_MESSAGE);
            }


        } catch (Exception e) {
            dialog = new LogMessageDialog(this, "Failed to create missing directory! \nReason: " + e.getMessage(), LogMessageDialog.WARNING_MESSAGE);
        }
        dialog.show();
    }

}

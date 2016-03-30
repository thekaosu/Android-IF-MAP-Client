/* 
 * LogMessageDialog.java        0.2 2015-03-08
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
package de.simu.decoit.android.decomap.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import de.simu.decoit.android.decomap.activities.R;

/**
 * class representing a log-message dialog view-element
 *
 * @author Dennis Dunekacke, DECOIT GmbH
 * @author Markus Schölzel, DECOIT GmbH
 * @version 0.2
 */
public class MessageDialog extends Dialog {

    private final String mMessage;
    private final String mTitle;

    public final static int WARNING_MESSAGE = 0;
    public final static int INFO_MESSAGE = 1;

    /**
     * constructor
     *
     * @param context current context
     * @param msg     message to display
     */
    public MessageDialog(Context context, String msg) {
        this(context, msg, INFO_MESSAGE);
    }

    /**
     * constructor
     *
     * @param context    current context
     * @param msg        message to display
     * @param dialogType typ of dialog
     */
    public MessageDialog(Context context, String msg, int dialogType) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mMessage = msg;

        if (dialogType == WARNING_MESSAGE) {
            mTitle = (String) context.getText(R.string.log_msgDialogBox_label_warn);
        } else if (dialogType == INFO_MESSAGE) {
            mTitle = (String) context.getText(R.string.log_msgDialogBox_label_info);
        } else {
            mTitle = (String) context.getText(R.string.log_msgDialogBox_label_info);
        }
    }

    public MessageDialog(Context context, String msg, String title) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mMessage = msg;
        mTitle = title;
    }

    /**
     * Called when the activity is first created
     *
     * @param savedInstanceState state-bundle
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_message_dialog);
        TextView title = ((TextView) findViewById(android.R.id.title));
        title.setText(mTitle);

        TextView twMsg = (TextView) findViewById(R.id.msgView);
        twMsg.setText(mMessage);
        Button buttonOK = (Button) findViewById(R.id.buttonOK);
        buttonOK.setOnClickListener(new OKListener());
    }

    /**
     * on-click listener class
     */
    private class OKListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // close dialog-box
            MessageDialog.this.dismiss();
        }
    }
}

/* 
 * SetupActivity.java        0.2 2015-03-08
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

package de.simu.decoit.android.decomap.activities.setupview;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * Activity for setting Preferences
 * 
 * @version 0.2
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Markus Schölzel, Decoit GmbH
 */
public class SetupActivity extends PreferenceActivity {

    // -------------------------------------------------------------------------
    // ACTIVITY LIFECYCLE HANDLING
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbox.logTxt(this.getClass().getName(), "SetupActivity.OnCreate(...) called");
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SetupFragment()).commit();
//        addPreferencesFromResource(R.xml.preferences);

    }

    @Override
    protected void onResume() {
        Toolbox.logTxt(this.getClass().getName(), "SetupActivity.OnResume(...) called");
        super.onResume();
    }

    // -------------------------------------------------------------------------
    // BUTTON HANDLING
    // -------------------------------------------------------------------------
    
    /**
     * we override the behavior of the back-button so that the application runs
     * in the background (instead of destroying it) when pressing back (similar
     * to the home button)
     */
    @Override
    public void onBackPressed() {
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }
}

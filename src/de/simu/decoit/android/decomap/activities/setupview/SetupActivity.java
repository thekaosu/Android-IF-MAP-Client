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

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

import de.simu.decoit.android.decomap.activities.R;
import de.simu.decoit.android.decomap.util.Toolbox;

/**
 * Activity for setting Preferences
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Markus Schölzel, Decoit GmbH
 * @version 0.2
 */
public class SetupActivity extends PreferenceActivity {

    private List<Header> headers;

    // -------------------------------------------------------------------------
    // ACTIVITY LIFECYCLE HANDLING
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbox.logTxt(this.getClass().getName(), "SetupActivity.OnCreate(...) called");
        super.onCreate(savedInstanceState);
//        getFragmentManager().beginTransaction().replace(android.R.id.content, new SetupFragment()).commit();
//        addPreferencesFromResource(R.xml.preferences);
        getListView().setPadding(0,0,0,0);
    }

    @Override
    protected void onResume() {
        Toolbox.logTxt(this.getClass().getName(), "SetupActivity.OnResume(...) called");
        super.onResume();

        if (getListAdapter() instanceof SetupAdapter) {
            ((SetupAdapter) getListAdapter()).resume();
        }
    }

    public void onBuildHeaders(List<Header> target) {
        // Called when the settings screen is up for the first time
        // we load the headers from our xml description

        loadHeadersFromResource(R.xml.preferences_header, target);

        headers = target;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getListAdapter() instanceof SetupAdapter) {
            ((SetupAdapter) getListAdapter()).pause();
        }
    }

    // -------------------------------------------------------------------------
    // BUTTON HANDLING
    // -------------------------------------------------------------------------

//    /**
//     * we override the behavior of the back-button so that the application runs
//     * in the background (instead of destroying it) when pressing back (similar
//     * to the home button)
//     */
//    @Override
//    public void onBackPressed() {
//        Intent setIntent = new Intent(Intent.ACTION_MAIN);
//        setIntent.addCategory(Intent.CATEGORY_HOME);
//        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(setIntent);
//    }

    // -------------------------------------------------------------------------
    // ADAPTER HANDLING
    // -------------------------------------------------------------------------

    /**
     * we are setting our own list adapter to use a toggle switch button
     */
    public void setListAdapter(ListAdapter adapter) {
        int i, count;

        if (headers == null) {
            headers = new ArrayList<Header>();
            // When the saved state provides the list of headers,
            // onBuildHeaders is not called
            // so we build it from the adapter given, then use our own adapter

            count = adapter.getCount();
            for (i = 0; i < count; ++i)
                headers.add((Header) adapter.getItem(i));
        }

        super.setListAdapter(new SetupAdapter(this, headers));
    }


    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}

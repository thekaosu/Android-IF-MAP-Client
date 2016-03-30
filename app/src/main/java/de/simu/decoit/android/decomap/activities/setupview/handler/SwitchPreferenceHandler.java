/*
 * SetupAdapter.java        0.2 2015-03-08
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

package de.simu.decoit.android.decomap.activities.setupview.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * ChangeListener for Switch Preferences
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class SwitchPreferenceHandler implements CompoundButton.OnCheckedChangeListener {
    private final Context context;
    private final String key;
    private Switch mSwitch;


    public SwitchPreferenceHandler(Context pContex, Switch pSwitch, String pKey) {
        context = pContex;
        key = pKey;
        setSwitch(pSwitch);
    }

    public void setSwitch(Switch pSwitch) {
        if (mSwitch == pSwitch) {
            return;
        }

        if (mSwitch != null) {
            mSwitch.setOnCheckedChangeListener(null);
        }
        mSwitch = pSwitch;
        mSwitch.setOnCheckedChangeListener(this);

        mSwitch.setChecked(isSwitchOn());
    }

    public Switch getSwitch(){
        return mSwitch;
    }

    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        SharedPreferences prefs;
        SharedPreferences.Editor editor;

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = prefs.edit();

        editor.putBoolean(key, isChecked);
        editor.apply();

    }

    public boolean isSwitchOn() {
        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            return prefs.getBoolean(key, false);
        } catch(ClassCastException e){
            return false;
        }
    }

    public void resume() {
        mSwitch.setOnCheckedChangeListener(this);
        mSwitch.setChecked(isSwitchOn());
    }

    public void pause() {
        mSwitch.setOnCheckedChangeListener(null);
    }
}

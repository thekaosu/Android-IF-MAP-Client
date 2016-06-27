/*
 * SetupAdapter..java          0.3 2015-03-08
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

package de.simu.decoit.android.decomap.activities.setupview.handler;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import de.simu.decoit.android.decomap.activities.R;
import de.simu.decoit.android.decomap.activities.setupview.SetupActivity;

/**
 * SelectedListener for monitoring mode spinner Preferences
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.3
 */
public class SpinnerSetupModePreferenceHandler implements AdapterView.OnItemSelectedListener {
    private final Context context;
    private final String key;
    private final ArrayAdapter<CharSequence> spinnerAdapter;
    private Spinner spinner;

    public SpinnerSetupModePreferenceHandler(Context pContex, Spinner pSpinner, String pKey) {
        context = pContex;
        key = pKey;
        setSpinner(pSpinner);

        spinnerAdapter = ArrayAdapter.createFromResource(pContex,
                R.array.preferences_value_serverForm, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        SharedPreferences prefs;
        SharedPreferences.Editor editor;

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        editor = prefs.edit();

        editor.putString(key, ((String) spinner.getSelectedItem()));

        editor.apply();

        if (context instanceof SetupActivity) {
            ((SetupActivity) context).refreshHeaders();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        spinner.setSelection(getSelectedItemPosition());
    }

    public void setSpinner(Spinner pSpinner) {
        if (spinner == pSpinner) {
            return;
        }
        if (spinner != null) {
            spinner.setOnItemSelectedListener(null);
        }
        spinner = pSpinner;
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(getSelectedItemPosition());
        spinner.setOnItemSelectedListener(this);
    }

    public Spinner getSpinner(){
        return spinner;
    }

    private String getSelectedItem() {
        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            return prefs.getString(key, context.getResources().getTextArray(R.array.preferences_value_serverForm)[1].toString());
        } catch (ClassCastException e){
            return context.getResources().getTextArray(R.array.preferences_value_serverForm)[1].toString();
        }
    }

    private int getSelectedItemPosition() {
        String[] modeArray = context.getResources().getStringArray(R.array.preferences_value_serverForm);
        int pos;
        String value = getSelectedItem();
        for (pos = 0; pos < modeArray.length; pos++) {
            if (value.equals(modeArray[pos])) {
                return pos;
            }
        }
        return pos;
    }

    public void resume() {
        spinner.setOnItemSelectedListener(this);
        spinner.setSelection(getSelectedItemPosition());
    }

    public void pause() {
        spinner.setOnItemSelectedListener(null);
    }
}
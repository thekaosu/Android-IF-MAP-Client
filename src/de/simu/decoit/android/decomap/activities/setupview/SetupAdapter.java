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

package de.simu.decoit.android.decomap.activities.setupview;

import android.content.Context;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.simu.decoit.android.decomap.activities.R;

/**
 * Adapter for Preferences activity
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2
 */
public class SetupAdapter extends ArrayAdapter<PreferenceActivity.Header> {

    static final int HEADER_TYPE_CATEGORY = 0;
    static final int HEADER_TYPE_NORMAL = 1;
    static final int HEADER_TYPE_SWITCH = 2;
    static final int HEADER_TYPE_SELECTION = 3;

    private LayoutInflater mInflater;

    private final long[] SWITCH_IDS = new long[]{R.id.esukomMetadataSettings, R.id.basicAuthSettings};
    private final long[] SELECTION_IDS = new long[]{R.id.monitoringModeSettings};

    private ArrayList<Long> switchIDS = new ArrayList<>();
    private ArrayList<Long> selectionIDS = new ArrayList<>();

    private HashMap<Long, SwitchPreferenceHandler> switchMap = new HashMap<Long, SwitchPreferenceHandler>();
    private HashMap<Long, SpinnerPreferenceHandler> selectionMap = new HashMap<Long, SpinnerPreferenceHandler>();

    public SetupAdapter(Context context, List<PreferenceActivity.Header> objects) {
        super(context, 0, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //putting ids into arrays
        for (long id : SWITCH_IDS) {
            switchIDS.add(id);
            switchMap.put(id, new SwitchPreferenceHandler(context, new Switch(context), id + ""));
        }
        for (long id : SELECTION_IDS) {
            selectionIDS.add(id);
            selectionMap.put(id, new SpinnerPreferenceHandler(context, new Spinner(context), id + ""));
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        PreferenceActivity.Header header = getItem(position);
        int headerType = getHeaderType(header);
        View view = null;

        switch (headerType) {
            case HEADER_TYPE_CATEGORY:
                view = mInflater.inflate(android.R.layout.preference_category, parent, false);
                ((TextView) view.findViewById(android.R.id.title)).setText(header.getTitle(getContext()
                        .getResources()));
                break;

            case HEADER_TYPE_SWITCH:
                if (header.getSummary(getContext().getResources()) == null || header.getSummary(getContext().getResources()).equals("")) {
                    view = mInflater.inflate(R.layout.preference_header_switch_item, parent, false);
                } else {
                    view = mInflater.inflate(R.layout.preference_header_summary_switch_item, parent, false);

                    ((TextView) view.findViewById(android.R.id.summary)).setText(header
                            .getSummary(getContext().getResources()));
                }
                ((ImageView) view.findViewById(android.R.id.icon)).setImageResource(header.iconRes);
                ((TextView) view.findViewById(android.R.id.title)).setText(header.getTitle(getContext()
                        .getResources()));

                switchMap.get(header.id).setSwitch((Switch) view.findViewById(R.id.switchWidget));

                break;
            case HEADER_TYPE_SELECTION:
                view = mInflater.inflate(R.layout.preference_header_selection_item, parent, false);
                ((TextView) view.findViewById(android.R.id.title)).setText(header.getTitle(getContext()
                        .getResources()));

                selectionMap.get(header.id).setSpinner((Spinner) view.findViewById(R.id.spinnerWidget));
                break;
            case HEADER_TYPE_NORMAL:
                if (header.getSummary(getContext().getResources()) == null || header.getSummary(getContext().getResources()).equals("")) {
                    view = mInflater.inflate(R.layout.preference_header_item, parent, false);
                } else {
                    view = mInflater.inflate(R.layout.preference_header_summary_item, parent, false);

                    ((TextView) view.findViewById(android.R.id.summary)).setText(header
                            .getSummary(getContext().getResources()));
                }
                ((ImageView) view.findViewById(android.R.id.icon)).setImageResource(header.iconRes);
                ((TextView) view.findViewById(android.R.id.title)).setText(header.getTitle(getContext()
                        .getResources()));
                break;
        }

        return view;
    }

    private int getHeaderType(PreferenceActivity.Header header) {
        if (selectionIDS.contains(header.id)) {
            return HEADER_TYPE_SELECTION;
        } else if ((header.fragment == null) && (header.intent == null)) {
            return HEADER_TYPE_CATEGORY;
        } else if (switchIDS.contains(header.id)) {
            return HEADER_TYPE_SWITCH;
        } else {
            return HEADER_TYPE_NORMAL;
        }
    }

    public void resume() {
        for (long handlerID : switchMap.keySet()) {
            switchMap.get(handlerID).resume();
        }
        for (long handlerID : selectionMap.keySet()) {
            selectionMap.get(handlerID).resume();
        }
    }

    public void pause() {
        for (long handlerID : switchMap.keySet()) {
            switchMap.get(handlerID).pause();
        }
        for (long handlerID : selectionMap.keySet()) {
            selectionMap.get(handlerID).pause();
        }
    }
}


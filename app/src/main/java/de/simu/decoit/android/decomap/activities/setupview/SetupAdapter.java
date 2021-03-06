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

package de.simu.decoit.android.decomap.activities.setupview;

import android.content.Context;
import android.preference.PreferenceActivity;
import android.support.v4.content.ContextCompat;
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
import de.simu.decoit.android.decomap.activities.setupview.handler.SpinnerSetupModePreferenceHandler;
import de.simu.decoit.android.decomap.activities.setupview.handler.SwitchPreferenceHandler;
import de.simu.decoit.android.decomap.preferences.PreferencesValues;

/**
 * Adapter for Preferences activity
 *
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.3
 */
public class SetupAdapter extends ArrayAdapter<PreferenceActivity.Header> {

    private static final int HEADER_TYPE_CATEGORY = 0;
    private static final int HEADER_TYPE_NORMAL = 1;
    private static final int HEADER_TYPE_SWITCH = 2;
    private static final int HEADER_TYPE_SELECTION = 3;

    private final LayoutInflater mInflater;

    //View id for the monitoring mode selection
    public static final long MONITORINGMODE_VIEW_ID = R.id.monitoringModeSettings;

    //Contains all view ids, which should not be disabled, while a connection is established
    private final long[] VIEW_WHILE_CONNECTION_WHITELIST = new long[]{R.id.loggingPreferences}; //should atleast contain 1 item! or else ther is a bug while using a multipanel device!

    private final ArrayList<Long> switchIDS = new ArrayList<>();

    private final HashMap<Long, SwitchPreferenceHandler> switchMap = new HashMap<>();
    private final SpinnerSetupModePreferenceHandler selectionHandler;
    private View selectionView;

    public SetupAdapter(Context context, List<PreferenceActivity.Header> objects) {
        super(context, 0, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //putting ids into arrays

        long[] SWITCH_IDS = new long[]{R.id.esukomMetadataSettings};
        for (long id : SWITCH_IDS) {
            switchIDS.add(id);
            switchMap.put(id, new SwitchPreferenceHandler(context, new Switch(context), String.valueOf(id)));
        }
        selectionHandler = new SpinnerSetupModePreferenceHandler(context, new Spinner(context), MONITORINGMODE_VIEW_ID + "");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PreferenceActivity.Header header = getItem(position);
        int headerType = getHeaderType(header);
        View view = null;

        TextView title = null;
        TextView summary = null;
        View mechanism = null;

        boolean enabled = isEnabled(position);

        switch (headerType) {
            case HEADER_TYPE_CATEGORY:
                view = mInflater.inflate(R.layout.preference_category, parent, false);
                title = ((TextView) view.findViewById(android.R.id.title));
                title.setText(header.getTitle(getContext()
                        .getResources()));
                break;

            case HEADER_TYPE_SWITCH:
                view = mInflater.inflate(R.layout.preference_header_switch_item, parent, false);
                summary = ((TextView) view.findViewById(android.R.id.summary));
                if (header.getSummary(getContext().getResources()) == null || header.getSummary(getContext().getResources()).equals("")) {
                    summary.setVisibility(View.GONE);
                } else {
                    summary.setText(header
                            .getSummary(getContext().getResources()));
                }
                ((ImageView) view.findViewById(android.R.id.icon)).setImageResource(header.iconRes);
                title = ((TextView) view.findViewById(android.R.id.title));
                title.setText(header.getTitle(getContext()
                        .getResources()));
                switchMap.get(header.id).setSwitch((Switch) view.findViewById(R.id.switchWidget));
                mechanism = switchMap.get(header.id).getSwitch();
                break;
            case HEADER_TYPE_SELECTION:
                if (selectionView == null) {
                    selectionView = mInflater.inflate(R.layout.preference_header_selection_item, parent, false);
                    title = ((TextView) selectionView.findViewById(android.R.id.title));
                    title.setText(header.getTitle(getContext()
                            .getResources()));
                    selectionHandler.setSpinner((Spinner) selectionView.findViewById(R.id.spinnerWidget));
                }
                if (title == null) {
                    title = ((TextView) selectionView.findViewById(android.R.id.title));
                }
                title.setTextColor(ContextCompat.getColor(getContext(), R.color.lblColor));
                view = selectionView;
                mechanism = selectionHandler.getSpinner();
                mechanism.setEnabled(true);
                break;
            case HEADER_TYPE_NORMAL:
                view = mInflater.inflate(R.layout.preference_header_item, parent, false);
                summary = ((TextView) view.findViewById(android.R.id.summary));
                if (header.getSummary(getContext().getResources()) == null || header.getSummary(getContext().getResources()).equals("")) {
                    summary.setVisibility(View.GONE);
                } else {
                    summary.setText(header
                            .getSummary(getContext().getResources()));
                }
                ((ImageView) view.findViewById(android.R.id.icon)).setImageResource(header.iconRes);
                title = ((TextView) view.findViewById(android.R.id.title));
                title.setText(header.getTitle(getContext()
                        .getResources()));

                break;
        }

        if (!enabled && headerType != HEADER_TYPE_CATEGORY) {
            if (summary != null) {
                summary.setTextColor(ContextCompat.getColor(getContext(), R.color.disabledText));
            }
            if (title != null) {
                title.setTextColor(ContextCompat.getColor(getContext(), R.color.disabledText));
            }
            if (mechanism != null) {
                mechanism.setEnabled(false);
            }
        }
        return view;
    }

    private int getHeaderType(PreferenceActivity.Header header) {
        if (MONITORINGMODE_VIEW_ID == header.id) {
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
        selectionHandler.resume();
        notifyDataSetChanged();
    }

    public void pause() {
        for (long handlerID : switchMap.keySet()) {
            switchMap.get(handlerID).pause();
        }
        selectionHandler.pause();
    }

    @Override
    public boolean isEnabled(int position) {
        if (PreferencesValues.getInstance().isLockPreferences()) {
            if (position > 0 && position < getCount()) {
                for (long id : VIEW_WHILE_CONNECTION_WHITELIST) {
                    if (getItem(position).id == id) {
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }
}


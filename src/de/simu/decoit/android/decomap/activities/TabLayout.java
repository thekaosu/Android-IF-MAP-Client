/* 
 * TabLayout.java        0.2 2015-03-08
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

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;

import de.simu.decoit.android.decomap.activities.setupview.SetupActivity;

/**
 * Tab.-Activity for Setting the different Activities to Tab-Widget
 *
 * @author Dennis Dunekacke, Decoit GmbH
 * @author Marcel Jahnke, Decoit GmbH
 * @author Leonid Schwenke, Decoit GmbH
 * @version 0.2.0.0
 */
@SuppressWarnings("deprecation")
public class TabLayout extends TabActivity {

    // gesture detection
    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_MAX_OFF_PATH = 200;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private static final int ANIMATIONTIME = 250;
    private View oldView;
    private View newView;
    private int currentTab;
    private GestureDetector mGestureScanner;

    private int maxTabs;

    private TabHost mTabHost;

    // -------------------------------------------------------------------------
    // ACTIVITY LIFECYCLE HANDLING
    // -------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTabHost = getTabHost(); // The activity TabHost

        // main-tab
        Intent intent = new Intent(this, MainActivity.class);
        mTabHost.addTab(mTabHost
                .newTabSpec(getResources().getString(R.string.tablayout_tabname_mainactivty))
                .setIndicator(getResources().getString(R.string.tablayout_tabname_mainactivty)).setContent(intent));

        // setup-tab
        Intent intent2 = new Intent(this, SetupActivity.class);
        mTabHost.addTab(mTabHost
                .newTabSpec(getResources().getString(R.string.tablayout_tabname_setupactivity))
                .setIndicator(getResources().getString(R.string.tablayout_tabname_setupactivity)).setContent(intent2));

        // device-status-tab
        Intent intent3 = new Intent(this, StatusActivity.class);
        mTabHost.addTab(mTabHost
                .newTabSpec(getResources().getString(R.string.tablayout_tabname_statusactivity))
                .setIndicator(getResources().getString(R.string.tablayout_tabname_statusactivity)).setContent(intent3));

        // log-messages-tab
        Intent intent4 = new Intent(this, LogActivity.class);
        mTabHost.addTab(mTabHost
                .newTabSpec(getResources().getString(R.string.tablayout_tabname_logactivity))
                .setIndicator(getResources().getString(R.string.tablayout_tabname_logactivity)).setContent(intent4));
        Intent intent5 = new Intent(this, InfoActivity.class);

        // about/info-tab
        mTabHost.addTab(mTabHost
                .newTabSpec(getResources().getString(R.string.tablayout_tabname_aboutactivity))
                .setIndicator(getResources().getString(R.string.tablayout_tabname_aboutactivity)).setContent(intent5));

        // set start tab
        mTabHost.setCurrentTab(0);

        //remove tab text padding
        for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
            mTabHost.getTabWidget().getChildAt(i).setPadding(0, 0, 0, 0);
        }

        // gesture-detection
        maxTabs = mTabHost.getTabWidget().getChildCount();
        mGestureScanner = new GestureDetector(this, new MyGestureDetector());

        // saving current tab for swipe animation
        newView = mTabHost.getCurrentView();
        currentTab = mTabHost.getCurrentTab();

        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                oldView = newView;
                newView = mTabHost.getCurrentView();
                if (mTabHost.getCurrentTab() > currentTab) {
                    oldView.setAnimation(outToLeftAnimation());
                    newView.setAnimation(inFromRightAnimation());
                } else {
                    oldView.setAnimation(outToRightAnimation());
                    newView.setAnimation(inFromLeftAnimation());
                }
                currentTab = mTabHost.getCurrentTab();
            }
        });
        // set tabs Colors
//        mTabHost.setBackgroundColor(Color.BLACK);
//        mTabHost.getTabWidget().setBackgroundColor(Color.BLACK);
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        return mGestureScanner.onTouchEvent(me);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        if (mGestureScanner != null) {
            if (mGestureScanner.onTouchEvent(ev))
                return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * get animation object for "tab in from right"-animation
     *
     * @return Animation
     */
    private Animation inFromRightAnimation() {
        Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, +1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration((long) ANIMATIONTIME);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    /**
     * get animation object for "tab in from right"-animation
     *
     * @return Animation
     */
    private Animation outToRightAnimation() {
        Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration((long) ANIMATIONTIME);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    /**
     * get animation object for "tab in from left"-animation
     *
     * @return Animation
     */
    private Animation inFromLeftAnimation() {

        Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        inFromRight.setDuration((long) ANIMATIONTIME);
        inFromRight.setInterpolator(new AccelerateInterpolator());
        return inFromRight;
    }

    /**
     * get animation object for "tab out to left"-animation
     *
     * @return Animation
     */
    private Animation outToLeftAnimation() {
        Animation outToLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f);
        outToLeft.setDuration((long) ANIMATIONTIME);
        outToLeft.setInterpolator(new AccelerateInterpolator());
        return outToLeft;
    }

    /**
     * create options menu
     *
     * @param menu options-menu to be creates
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * handler for actions performed when options-menu items is selected by the
     * user
     *
     * @param item options-item that has been selected
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.opt_quit:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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

    private class MyGestureDetector implements OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int currentTab = mTabHost.getCurrentTab();
            // Check movement along the Y-axis. If it exceeds SWIPE_MAX_OFF_PATH,
            // then dismiss the swipe.
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
                return false;
            }

            // Swipe from right to left.
            // The swipe needs to exceed a certain distance (SWIPE_MIN_DISTANCE) and
            // a certain velocity (SWIPE_THRESHOLD_VELOCITY).
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                if (currentTab < maxTabs) {
//                    newView.setAnimation(outToLeftAnimation());

                    mTabHost.setCurrentTab(currentTab + 1);
//                    newView = mTabHost.getCurrentView();
//                    newView.setAnimation(inFromRightAnimation());
                }
                return true;
            }

            // Swipe from left to right.
            // The swipe needs to exceed a certain distance (SWIPE_MIN_DISTANCE) and
            // a certain velocity (SWIPE_THRESHOLD_VELOCITY).
            if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                if (currentTab > 0) {
//                    mTabHost.setAnimation(outToLeftAnimation());
//                    mTabHost.setAnimation(inFromRightAnimation());
                    mTabHost.setCurrentTab(currentTab - 1);
                }
            }

            return false;
        }
    }
}
/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.candroid.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.instructure.candroid.R;
import com.instructure.candroid.adapter.CommunicationChannelsAdapter;
import com.instructure.candroid.adapter.NotificationPreferencesRecyclerAdapter;
import com.instructure.candroid.delegate.APIContract;
import com.instructure.candroid.view.ActionbarSpinner;
import com.instructure.canvasapi.api.CommunicationChannelsAPI;
import com.instructure.canvasapi.model.CommunicationChannel;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandarecycler.PandaRecyclerView;
import com.instructure.pandarecycler.interfaces.EmptyViewInterface;
import com.instructure.pandautils.utils.Const;

import retrofit.client.Response;

public class NotificationPreferencesActivity extends ParentActivity implements APIContract {

    private PandaRecyclerView mRecyclerView;
    private ActionbarSpinner mSpinner;
    private NotificationPreferencesRecyclerAdapter mRecyclerAdapter;

    private CanvasCallback<CommunicationChannel[]> communicationChannelCallback;
    private CommunicationChannel[] allCommunicationChannels;
    private int selectedPosition = 0;
    private boolean routeToPush = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if(intent != null && intent.getExtras() != null) {
            routeToPush = intent.getBooleanExtra(Const.NOTIFICATION_PREFS_ROUTE_TO_PUSH, false);
        }

        configureRecyclerView();

        setupCallbacks();
        setupListeners();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if(savedInstanceState == null) {
            User user = APIHelpers.getCacheUser(getContext());
            if(user != null) {
                CommunicationChannelsAPI.getCommunicationChannels(user.getId(), communicationChannelCallback);
            }
        } else {
            selectedPosition = savedInstanceState.getInt(Const.POSITION, 0);
            allCommunicationChannels = (CommunicationChannel[])savedInstanceState.getParcelableArray(Const.ARRAY);
            setupSpinner();
            if(mSpinner != null) {
                mSpinner.setSelection(selectedPosition);
            }
        }
    }

    public void configureRecyclerView() {
        EmptyViewInterface emptyPandaView = (EmptyViewInterface)findViewById(R.id.emptyPandaView);
        mRecyclerView = (PandaRecyclerView)findViewById(R.id.listView);
        mRecyclerAdapter = new NotificationPreferencesRecyclerAdapter(getContext());
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setSelectionEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setEmptyView(emptyPandaView);
    }

    @Override
    public int contentResId() {
        return R.layout.notification_preferences_activity;
    }

    @Override
    public boolean showHomeAsUp() {
        return true;
    }

    @Override
    public boolean showTitleEnabled() {
        return true;
    }

    @Override
    public void onUpPressed() {
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Const.POSITION, selectedPosition);
        outState.putParcelableArray(Const.ARRAY, allCommunicationChannels);
    }

    private void setupActionbarSpinnerForCourse(final ActionBar actionBar, final CommunicationChannelsAdapter adapter) {
        final View spinnerContainer = LayoutInflater.from(actionBar.getThemedContext()).inflate(R.layout.actionbar_course_navigation_spinner, null);
        actionBar.setCustomView(spinnerContainer);

        mSpinner = (ActionbarSpinner) spinnerContainer.findViewById(R.id.actionbar_spinner);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedEvenIfUnchangedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fetchNotificationPreferences(adapter.getItem(position));
                selectedPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public void setupCallbacks() {
        communicationChannelCallback = new CanvasCallback<CommunicationChannel[]>(NotificationPreferencesActivity.this) {
            @Override
            public void firstPage(CommunicationChannel[] communicationChannels, LinkHeaders linkHeaders, Response response) {
                allCommunicationChannels = communicationChannels;
                setupSpinner();
                if(allCommunicationChannels != null && allCommunicationChannels.length > 0) {
                    if(routeToPush) {
                        for (int i = 0; i < communicationChannels.length; i++) {
                            if("push".equalsIgnoreCase(communicationChannels[i].getType())) {
                                selectedPosition = i;
                                mSpinner.setSelection(i);
                                break;
                            }
                        }
                    }
                    fetchNotificationPreferences(allCommunicationChannels[0]);
                }
            }

        };
    }

    private void setupSpinner(){
        if(allCommunicationChannels != null) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            CommunicationChannelsAdapter adapter = new CommunicationChannelsAdapter(this, android.R.layout.simple_spinner_dropdown_item, allCommunicationChannels);
            setupActionbarSpinnerForCourse(getSupportActionBar(), adapter);
        } else {
            fetchCommunicationChannels();
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(Const.ACTIONBAR_ELEVATION);
        }
    }

    private void fetchCommunicationChannels() {
        User user = APIHelpers.getCacheUser(getContext());
        if(user != null) {
            CommunicationChannelsAPI.getCommunicationChannels(user.getId(), communicationChannelCallback);
        }
    }

    public void fetchNotificationPreferences(CommunicationChannel channel) {
        mRecyclerAdapter.fetchNotificationPreferences(channel);
    }

    @Override
    public void setupListeners() {}


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

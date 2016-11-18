/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.androidfoosball.wear;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.wearable.DataMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.instructure.wearutils.WearClient;
import com.instructure.wearutils.WearConst;
import com.instructure.wearutils.interfaces.DataRequest;
import com.instructure.wearutils.interfaces.WearableCallbacks;
import com.instructure.wearutils.models.DataPage;
import com.instructure.wearutils.models.DataRow;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class WearActivity extends Activity implements
        WearClient.OnConnectedListener,
        WearableCallbacks,
        DataRequest {

    private WearClient mWearClient;
    private Map<String, String> mJsonData = new HashMap<>(3);

    @BindView(R.id.pager) GridViewPager mPager;
    @BindView(R.id.progress) ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                ButterKnife.bind(WearActivity.this, stub);
                mProgress.setVisibility(View.VISIBLE);
            }
        });
    }

    private class PagerAdapter extends FragmentGridPagerAdapter {

        private ArrayList<DataRow> mPages = new ArrayList<>();

        public PagerAdapter(FragmentManager fm, String winCount, String lossCount, List<DataPage> tables) {
            super(fm);
            initPages(winCount, lossCount, tables);
        }

        private void initPages(String winCount, String lossCount, List<DataPage> tables) {
            DataRow row1 = new DataRow();
            row1.addPages(new DataPage(getString(R.string.wins), winCount, R.drawable.page_bg_1, DataPage.WIN_LOSS));
            row1.addPages(new DataPage(getString(R.string.losses), lossCount, R.drawable.page_bg_2, DataPage.WIN_LOSS));

            DataRow row2 = new DataRow();
            for(DataPage page : tables) {
                if("FREE".equals(page.mText)) {
                    page.mBackgroundId = R.drawable.page_bg_1;
                    page.mText = getString(R.string.status_free);
                } else if("BUSY".equals(page.mText)) {
                    page.mBackgroundId = R.drawable.page_bg_2;
                    page.mText = getString(R.string.status_busy);
                } else {
                    page.mBackgroundId = R.drawable.page_bg_1;
                    page.mText = getString(R.string.status_unknown);
                }
                row2.addPages(page);
            }

            mPages.add(row1);
            mPages.add(row2);
        }

        @Override
        public Fragment getFragment(int row, int col) {
            DataPage page = (mPages.get(row)).getPages(col);
            if(page.type == DataPage.WIN_LOSS) {
                return WinLossCardFragment.newInstance(page.mTitle, page.mText);
            } else {
                return TableCardFragment.newInstance(page.mTitle, page.mText);
            }
        }

        @Override
        public Drawable getBackgroundForPage(int row, int col) {
            DataPage page = (mPages.get(row)).getPages(col);
            return ContextCompat.getDrawable(WearActivity.this, page.mBackgroundId);
        }

        @Override
        public int getRowCount() {
            return mPages.size();
        }

        @Override
        public int getColumnCount(int row) {
            return mPages.get(row).size();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getClient().connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        getClient().disconnect();
    }

    //region Data Request

    protected WearClient getClient() {
        if (mWearClient == null) {
            mWearClient = new WearClient(this, this, this);
        }
        return mWearClient;
    }

    @Override
    public void onClientConnected() {
        mJsonData.clear();
        Log.d("wear", "Requesting data...");
        getClient().sendMessage(WearConst.WEAR_DATA_REQUEST, null);
    }

    @Override
    public void onConnectionFailed() {}

    @Override
    public void sendDataRequest(String requestType, String json) {}

    @Override
    public void OnSyncDataItemTask(DataMap dataMap) {
        if (dataMap.containsKey(WearConst.DATA_ITEM_WIN_COUNT)) {
            final String json = dataMap.getString(WearConst.DATA_ITEM_WIN_COUNT);
            Log.d("wear", "Got win count: " + json);
            mJsonData.put(WearConst.DATA_ITEM_WIN_COUNT, json);
            dataReadyCheck();
        } else if (dataMap.containsKey(WearConst.DATA_ITEM_LOSS_COUNT)) {
            final String json = dataMap.getString(WearConst.DATA_ITEM_LOSS_COUNT);
            Log.d("wear", "Got loss count: " + json);
            mJsonData.put(WearConst.DATA_ITEM_LOSS_COUNT, json);
            dataReadyCheck();
        } else if(dataMap.containsKey(WearConst.DATA_ITEM_TABLES)) {
            final String json = dataMap.getString(WearConst.DATA_ITEM_TABLES);
            Log.d("wear", "Got tables: " + json);
            mJsonData.put(WearConst.DATA_ITEM_TABLES, json);
            dataReadyCheck();
        }
    }

    @Override
    public void OnGetMessageTask(String messagePath) {}

    private void dataReadyCheck() {
        if(mJsonData.size() >= 3) {

            String winCount = mJsonData.get(WearConst.DATA_ITEM_WIN_COUNT);
            String lossCount = mJsonData.get(WearConst.DATA_ITEM_LOSS_COUNT);
            String tableJson = mJsonData.get(WearConst.DATA_ITEM_TABLES);

            if(TextUtils.isEmpty(winCount)) {
                winCount = String.valueOf(0);
            }

            if(TextUtils.isEmpty(lossCount)) {
                lossCount = String.valueOf(0);
            }

            final Type type = new TypeToken<List<DataPage>>(){}.getType();
            List<DataPage> tables = new Gson().fromJson(tableJson, type);

            mPager.setAdapter(new PagerAdapter(getFragmentManager(), winCount, lossCount, tables));
            mProgress.setVisibility(View.INVISIBLE);
        }
    }

    //endregion
}

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

import android.os.Handler;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.instructure.androidfoosball.models.Table;
import com.instructure.androidfoosball.utils.Const;
import com.instructure.androidfoosball.utils.FireUtils;
import com.instructure.androidfoosball.utils.Prefs;
import com.instructure.wearutils.WearConst;
import com.instructure.wearutils.interfaces.WearableCallbacks;
import com.instructure.wearutils.models.DataPage;
import com.instructure.wearutils.services.BaseWearService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class WearService extends BaseWearService implements WearableCallbacks {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("wear", "Wear Service Created");
        setCallbacks(this);
    }

    @Override
    public void OnSyncDataItemTask(DataMap dataMap) {
        Log.d("wear", "OnSyncDataItemTask()");
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                //Action requests from wear device
            }
        });
    }

    @Override
    public void OnGetMessageTask(final String messagePath) {
        Log.d("wear", "OnGetMessageTask()");
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                //Data request from wear device
                if(WearConst.WEAR_DATA_REQUEST.equals(messagePath)) {

                    String userId = Prefs.get(getApplicationContext()).load(Const.USER_ID, "");
                    DatabaseReference database = FirebaseDatabase.getInstance().getReference();

                    FireUtils.getWinCount(userId, database, new FireUtils.OnIntValue() {
                        @Override
                        public void onValueFound(int value) {
                            Log.d("wear", "Sending win count data: " + value);
                            syncString(WearConst.DATA_ITEM_WIN_COUNT, String.valueOf(value));
                        }
                    });

                    FireUtils.getLossCount(userId, database, new FireUtils.OnIntValue() {
                        @Override
                        public void onValueFound(int value) {
                            Log.d("wear", "Sending loss count data: " + value);
                            syncString(WearConst.DATA_ITEM_LOSS_COUNT, String.valueOf(value));
                        }
                    });

                    FireUtils.getTables(database, new FireUtils.OnTablesValue() {
                        @Override
                        public void onValueFound(List<Table> values) {
                            Log.d("wear", "Sending table data");

                            final Type type = new TypeToken<List<DataPage>>(){}.getType();
                            final List<DataPage> pages = new ArrayList<>(values.size());

                            for(Table table : values) {
                                pages.add(new DataPage(table.getName(), table.getCurrentGame(), 0, DataPage.TABLE));
                            }

                            final String json = new Gson().toJson(pages, type);
                            syncString(WearConst.DATA_ITEM_TABLES, json);
                        }
                    });
                }
            }
        });
    }
}

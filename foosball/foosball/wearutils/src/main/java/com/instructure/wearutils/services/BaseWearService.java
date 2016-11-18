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

package com.instructure.wearutils.services;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.instructure.wearutils.interfaces.WearableCallbacks;

import java.util.Date;
import java.util.List;


public class BaseWearService extends WearableListenerService {

    private GoogleApiClient mGoogleApiClient;
    private WearableCallbacks mWearCallbacks;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    public void setCallbacks(WearableCallbacks callbacks) {
        mWearCallbacks = callbacks;
    }

    @Override
    public void onDataChanged(final DataEventBuffer dataEvents) {

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {

                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                final DataMap dataMap = dataMapItem.getDataMap();
                if (mWearCallbacks != null) {
                    mWearCallbacks.OnSyncDataItemTask(dataMap);
                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                if (event.getDataItem() != null) {

                }
            }
        }
    }

    //sync String

    public void syncString(String key, String item) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create("/" + key);
        putDataMapRequest.getDataMap().putLong("time", new Date().getTime());
        putDataMapRequest.getDataMap().putString(key, item);
        syncDataItem(putDataMapRequest);
    }

    //General method to sync data in the Data Layer
    public void syncDataItem(PutDataMapRequest putDataMapRequest) {

        PutDataRequest request = putDataMapRequest.asPutDataRequest();

        if (!mGoogleApiClient.isConnected()) {
            return;
        }

        //let's send the dataItem to the DataLayer API
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {

                    }
                });
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (mWearCallbacks != null) {
            String messagePath = messageEvent.getPath();
            mWearCallbacks.OnGetMessageTask(messagePath);
        }
    }
}

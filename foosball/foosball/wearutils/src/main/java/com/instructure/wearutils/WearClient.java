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

package com.instructure.wearutils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.instructure.wearutils.interfaces.WearableCallbacks;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;


public class WearClient implements
        DataApi.DataListener,
        MessageApi.MessageListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private OnConnectedListener mConnectionListenerCallback;

    public interface OnConnectedListener {
        void onClientConnected();
        void onConnectionFailed();
    }

    private GoogleApiClient mGoogleApiClient;

    private WearableCallbacks mWearCallbacks;

    public WearClient(Context context, OnConnectedListener listener, WearableCallbacks callbacks) {

        mConnectionListenerCallback = listener;
        mWearCallbacks = callbacks;

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void connect() {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Wearable.MessageApi.addListener(mGoogleApiClient, this);

        if(mConnectionListenerCallback != null) {
            mConnectionListenerCallback.onClientConnected();
        }
    }

    public void disconnect() {
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);

        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {

                DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                DataMap dataMap = dataMapItem.getDataMap();

                if(mWearCallbacks != null) {
                    mWearCallbacks.OnSyncDataItemTask(dataMap);
                }

            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                if(event.getDataItem() != null) {

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
        Wearable.DataApi.putDataItem(mGoogleApiClient, request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        if (!dataItemResult.getStatus().isSuccess()) {

                        }
                    }
                });
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }

        return results;
    }


    //Task to send messages to nodes
    private class StartTeleportMessageTask extends AsyncTask<Object, Void, Object> {

        @Override
        protected Void doInBackground(Object... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                Log.d("wear", "sending to nodes...");
                propagateMessageToNodes(node, (String) args[0], (byte[]) args[1]);
            }
            return null;
        }
    }

    //propagate message to nodes
    private void propagateMessageToNodes(String node, String path, byte[] payload) {
        Wearable.MessageApi.sendMessage(mGoogleApiClient, node, path, payload).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                        Log.d("wear", "Send Message Result: " + sendMessageResult.getStatus().getStatusMessage());
                    }
                }
        );
    }

    public void sendMessage(String path, byte[] payload) {
        new StartTeleportMessageTask().execute(path, payload);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("wear", "onMessageReceived()");
        if(mWearCallbacks != null) {
            String messagePath = messageEvent.getPath();
            mWearCallbacks.OnGetMessageTask(messagePath);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("wear", "onConnectionFailed(" + getErrorCodeString(connectionResult.getErrorCode()) + ")");
        if(mConnectionListenerCallback != null) {
            mConnectionListenerCallback.onConnectionFailed();
        }
    }

    private String getErrorCodeString(int errorCode) {
        switch (errorCode) {
            case 0:
                return "SUCCESS";
            case 1:
                return "SERVICE_MISSING";
            case 2:
                return "SERVICE_VERSION_UPDATE_REQUIRED";
            case 3:
                return "SERVICE_DISABLED";
            case 4:
                return "SIGN_IN_REQUIRED";
            case 5:
                return "INVALID_ACCOUNT";
            case 6:
                return "RESOLUTION_REQUIRED";
            case 7:
                return "NETWORK_ERROR";
            case 8:
                return "INTERNAL_ERROR";
            case 9:
                return "SERVICE_INVALID";
            case 10:
                return "DEVELOPER_ERROR";
            case 11:
                return "LICENSE_CHECK_FAILED";
            case 12:
                return "DATE_INVALID";
            case 13:
                return "CANCELED";
            case 14:
                return "TIMEOUT";
            case 15:
                return "INTERRUPTED";
            case 16:
                return "API_UNAVAILABLE";
            case 1500:
                return "DRIVE_EXTERNAL_STORAGE_REQUIRED";

        }
        return "UNKNOWN";
    }
}

/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvasapi2;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.instructure.canvasapi2.models.CanvasErrorCode;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.canvasapi2.utils.RetrofitCounter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public abstract class StatusCallback<DATA> implements Callback<DATA> {

    /**
     * Used for places we require information derived from a Context
     * Which cannot be done at initialization of the CanvasConfig
     * Typically real-time info like a network connection
     */
    public interface StatusDelegate {
        boolean hasNetworkConnection();
    }

    private boolean mIsApiCallInProgress = false;
    private LinkHeaders mLinkHeaders = null;
    private ArrayList<Call<DATA>> mCalls = new ArrayList<>();
    private StatusDelegate mStatusDelegate;

    @Deprecated
    public StatusCallback() {}

    public StatusCallback(StatusDelegate statusDelegate) {
        mStatusDelegate = statusDelegate;
    }

    @Override
    final public void onResponse(final Call<DATA> data, final Response<DATA> response) {
        mIsApiCallInProgress = true;
        RetrofitCounter.decrement();
        if(response != null && response.isSuccessful()) {
            publishHeaderResponseResults(response, response.raw(), APIHelper.parseLinkHeaderResponse(response.headers()));
        } else if(response != null && response.code() == 504) {
            //Cached response does not exist.
            Logger.e("StatusCallback: GOT A 504");
            //No response
            onCallbackFinished(ApiType.CACHE);
        } else {
            if(response != null) {
                onFail(data, new Throwable("StatusCallback: 40X Error"), response);
                EventBus.getDefault().postSticky(new CanvasErrorCode(response.code(), response.errorBody()));
            } else {
                onFail(data, new Throwable("StatusCallback: Unknown Code Error"));
            }
            //No response or no data
            onCallbackFinished(ApiType.API);
        }
    }

    @Override
    final public void onFailure(Call<DATA> data, Throwable t) {
        mIsApiCallInProgress = false;
        RetrofitCounter.decrement();
        if(data.isCanceled() || "Canceled".equals(t.getMessage())) {
            Logger.d("StatusCallback: callback(s) were cancelled");
            onCancelled();
        } else {
            Logger.e("StatusCallback: Failure: " + t.getMessage());
            onFail(data, t);
        }
    }

    final public void onCallbackStarted() {
        mIsApiCallInProgress = true;
        RetrofitCounter.increment();
        onStarted();
    }

    final public void onCallbackFinished(ApiType type) {
        mIsApiCallInProgress = false;
        onFinished(type);
    }

    final synchronized public boolean isCallInProgress() {
        return mIsApiCallInProgress;
    }

    /**
     * When all responses will report. Api or Cache.
     * @param response The data of the response
     * @param linkHeaders The link headers for the response, used for pagination
     * @param type The type of response, Cache or Api
     */
    public void onResponse(Response<DATA> response, LinkHeaders linkHeaders, ApiType type){}

    public void onResponse(Response<DATA> response, LinkHeaders linkHeaders, ApiType type, int code){
        onResponse(response, linkHeaders, type);
    }

    public void onFail(Call<DATA> response, Throwable error){}
    public void onFail(Call<DATA> response, Throwable error, int code){
        onFail(response, error);
    }
    public void onFail(Call<DATA> callResponse, Throwable error, Response response) {
        onFail(callResponse, error, response.code());
    }
    public void onCancelled(){}
    public void onStarted(){}
    public void onFinished(ApiType type){}

    private void publishHeaderResponseResults(@NonNull Response<DATA> response, @NonNull okhttp3.Response okResponse, @NonNull LinkHeaders linkHeaders) {
        setLinkHeaders(linkHeaders);
        final boolean isCacheResponse = APIHelper.isCachedResponse(okResponse);
        Logger.d("Is Cache Response? " + (isCacheResponse ? "YES" : "NO"));
        if (isCacheResponse) {
            onResponse(response, linkHeaders, ApiType.CACHE, okResponse.code());
            onCallbackFinished(ApiType.CACHE);
        } else {
            onResponse(response, linkHeaders, ApiType.API, okResponse.code());
            onCallbackFinished(ApiType.API);
        }
    }

    public static boolean moreCallsExist(@Nullable LinkHeaders...headers) {
        return (headers != null && headers.length > 0 && headers[0] != null && headers[0].nextUrl != null);
    }

    public static boolean isFirstPage(@Nullable LinkHeaders...headers) {
        return (headers == null || headers.length == 0 || headers[0] == null);
    }

    public void setLinkHeaders(@Nullable LinkHeaders linkHeaders) {
        mLinkHeaders = linkHeaders;
    }

    public @Nullable LinkHeaders getLinkHeaders() {
        return mLinkHeaders;
    }

    private void clearLinkHeaders() {
        mLinkHeaders = null;
    }

    /**
     * Used to reset a callback to it's former glory
     * Clears the LinkHeaders
     * Cancels any ongoing Calls
     * Clears all calls from the ArrayList of Calls
     */
    public void reset() {
        clearLinkHeaders();
        cancel();
        clearCalls();
    }

    public void clearCalls() {
        mCalls.clear();
    }

    public Call<DATA> addCall(Call<DATA> call) {
        mCalls.add(call);
        return call;
    }

    public void cancel() {
        for(Call<DATA> call : mCalls) {
            call.cancel();
        }
    }

    public static void cancelAllCalls() {
        // TODO: Figure out how to decrement RetrofitCounter accordingly
        CanvasRestAdapter.cancelAllCalls();
    }

    @NonNull
    protected StatusDelegate getStatusDelegate() {
        if(mStatusDelegate == null) {
            mStatusDelegate = new StatusDelegate() {
                @Override
                public boolean hasNetworkConnection() {
                    return true;
                }
            };
        }
        return mStatusDelegate;
    }
}

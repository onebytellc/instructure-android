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

import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.canvasapi2.utils.RetrofitCounter;

import retrofit2.Response;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * This class must be used for Expresso Testing to work.
 * Tracks onStart() and onComplete() with the RetrofitCounter
 */
public class StatusSubscriber<T extends Response> extends Subscriber<T> {

    private boolean mIsApiCallInProgress = false;
    private boolean mReportToWorker = false;

    protected StatusSubscriber() {
        super();
    }

    /**
     * Constructor for having cache and success report to a onResponse method
     * @param reportToWorker optional boolean which all calls report to one method called onResponse, false by default
     */
    protected StatusSubscriber(boolean reportToWorker) {
        super();
        mReportToWorker = reportToWorker;
    }

    protected StatusSubscriber(Subscriber<?> subscriber) {
        super(subscriber);
    }

    protected StatusSubscriber(Subscriber<?> subscriber, boolean shareSubscriptions) {
        super(subscriber, shareSubscriptions);
    }

    @Override
    final public void onStart() {
        RetrofitCounter.increment();
        onCallbackStarted();
        super.onStart();
    }

    @Override
    final public void onCompleted() {
        RetrofitCounter.decrement();
    }

    @Override
    final public void onError(Throwable e) {
        RetrofitCounter.decrement();
        Logger.e("ON_ERROR: " + e);
        onFail(e);
    }

    @Override
    final public void onNext(final T response) {
        if(response != null && response.isSuccessful()) {
            new APIHelper().processHeaders(response.raw()).subscribe(new Action1<LinkHeaders>() {
                @Override
                public void call(LinkHeaders linkHeaders) {
                    publishHeaderResponseResults(response, response.raw(), linkHeaders);
                }
            });
        } else if(response != null && response.code() == 504) {
            //Cached response does not exist? maybe
            Logger.e("GOT A 504, FIND OUT WHAT CAUSED ME AND WHAT TO DO ABOUT IT");
            //No response
            onCallbackFinished(ApiType.CACHE);
        } else {
            //No response
            onCallbackFinished(ApiType.API);
        }
    }

    final public void onCallbackStarted() {
        mIsApiCallInProgress = true;
        onStarted();
    }

    final public void onCallbackFinished(ApiType type) {
        mIsApiCallInProgress = false;
        onFinished(type);
    }

    final synchronized public boolean isCallInProgress() {
        return mIsApiCallInProgress;
    }

    //To be used as needed
    protected void onSuccess(T response, LinkHeaders linkHeaders){
        if(mReportToWorker) {
            worker(response, linkHeaders, ApiType.API);
        }
    }
    protected void onCache(T response, LinkHeaders linkHeaders){
        if(mReportToWorker) {
            worker(response, linkHeaders, ApiType.CACHE);
            return;
        }
        //cache will update onSuccess if super is called
        onSuccess(response, linkHeaders);
    }
    protected void worker(T response, LinkHeaders linkHeaders, ApiType type){}
    protected void onFail(Throwable error){}
    protected void onStarted(){}
    protected void onFinished(ApiType type){}

    private void publishHeaderResponseResults(T response, @NonNull okhttp3.Response okResponse, @NonNull LinkHeaders linkHeaders) {
        boolean isCache = APIHelper.isCachedResponse(okResponse);

        if (isCache) {
            onCache(response, linkHeaders);
        } else {
            onSuccess(response, linkHeaders);
        }
    }

    public boolean moreCallsExist(LinkHeaders linkHeaders) {
        return StatusCallback.moreCallsExist(linkHeaders);
    }
}

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

package com.instructure.canvasapi2.utils;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.models.CanvasModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;


public class DepaginatedCallback<MODEL extends CanvasModel> extends StatusCallback<List<MODEL>> {

    private StatusCallback<List<MODEL>> callback;
    private PageRequestCallback<MODEL> pageRequestCallback;
    private List<MODEL> networkItems = new ArrayList<>();
    private List<MODEL> cacheItems = new ArrayList<>();

    public interface PageRequestCallback<MODEL extends CanvasModel> {
        void getNextPage(DepaginatedCallback<MODEL> callback, String nextUrl, boolean isCached);
    }

    @Deprecated
    public DepaginatedCallback(StatusCallback<List<MODEL>> callback, PageRequestCallback<MODEL> pageRequestCallback) {
        this.callback = callback;
        this.pageRequestCallback = pageRequestCallback;

        if (this.pageRequestCallback == null) {
            throw new UnsupportedOperationException("PageRequestCallback cannot be null");
        }
    }

    public DepaginatedCallback(StatusCallback<List<MODEL>> callback, PageRequestCallback<MODEL> pageRequestCallback, StatusDelegate statusDelegate) {
        super(statusDelegate);
        this.callback = callback;
        this.pageRequestCallback = pageRequestCallback;

        if (this.pageRequestCallback == null) {
            throw new UnsupportedOperationException("PageRequestCallback cannot be null");
        }
    }

    @Override
    public void onResponse(Response<List<MODEL>> response, LinkHeaders linkHeaders, ApiType type) {
        super.onResponse(response, linkHeaders, type);

        if(type.isCache()) {
            cacheItems.addAll(response.body());
            if (StatusCallback.moreCallsExist(linkHeaders)) {
                pageRequestCallback.getNextPage(this, linkHeaders.nextUrl, true);
            } else {
                callback.onResponse(Response.success(cacheItems, response.raw()), linkHeaders, ApiType.CACHE);
            }
        } else {
            networkItems.addAll(response.body());

            if (StatusCallback.moreCallsExist(linkHeaders)) {
                pageRequestCallback.getNextPage(this, linkHeaders.nextUrl, false);
            } else {
                callback.onResponse(Response.success(networkItems, response.raw()), linkHeaders, ApiType.API);
            }
        }
    }

    @Override
    public void onFail(Call<List<MODEL>> data, Throwable t) {
        super.onFailure(data, t);
        callback.onFailure(data, t);
    }
}

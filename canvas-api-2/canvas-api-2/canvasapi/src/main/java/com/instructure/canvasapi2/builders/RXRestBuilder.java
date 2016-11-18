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

package com.instructure.canvasapi2.builders;

import android.support.annotation.NonNull;

import com.instructure.canvasapi2.CanvasConfig;
import com.instructure.canvasapi2.CanvasRestAdapter;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;


public class RXRestBuilder extends CanvasRestAdapter {


    public RXRestBuilder(@NonNull CanvasConfig canvasConfig) {
        super(canvasConfig, null);
    }

    public <T> T build(@NonNull Class<T> clazz, @NonNull RestParams params) {
        params = new RestParams.Builder(params).withForceReadFromCache(false).build();
        Retrofit restAdapter = buildAdapter(params);
        return restAdapter.create(clazz);
    }

    public <T> T buildCache(@NonNull Class<T> clazz, @NonNull RestParams params) {
        params = new RestParams.Builder(params).withForceReadFromCache(true).build();
        Retrofit restAdapter = buildAdapter(params);
        return restAdapter.create(clazz);
    }


    @Override
    protected Retrofit.Builder finalBuildAdapter(@NonNull RestParams params, String apiContext) {
        Retrofit.Builder builder = super.finalBuildAdapter(params, apiContext);
        builder.addCallAdapterFactory(RxJavaCallAdapterFactory.create());
        return builder;
    }
}

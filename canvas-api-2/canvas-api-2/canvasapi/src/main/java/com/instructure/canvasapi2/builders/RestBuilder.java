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

import android.content.Context;
import android.support.annotation.NonNull;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.CanvasConfig;
import com.instructure.canvasapi2.CanvasRestAdapter;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.utils.Logger;

import retrofit2.Retrofit;


public class RestBuilder extends CanvasRestAdapter {

    @Deprecated
    public RestBuilder(@NonNull CanvasConfig canvasConfig) {
        super(canvasConfig, new StatusCallback(new StatusCallback.StatusDelegate() {
            @Override
            public boolean hasNetworkConnection() {
                return true;
            }
        }) {});
    }

    public RestBuilder(final Context context, @NonNull CanvasConfig canvasConfig) {
        super(canvasConfig, new StatusCallback(new StatusCallback.StatusDelegate() {
            @Override
            public boolean hasNetworkConnection() {
                return AppManager.hasNetworkConnection(context);
            }
        }){});
    }

    public RestBuilder(@NonNull CanvasConfig canvasConfig, @NonNull StatusCallback callback) {
        super(canvasConfig, callback);
    }
    
    public <T> T build(@NonNull Class<T> clazz, @NonNull RestParams params) {
        params = new RestParams.Builder(params).withForceReadFromCache(false).build();
        Retrofit restAdapter = buildAdapter(params);
        return restAdapter.create(clazz);
    }

    public <T> T buildNoRedirects(@NonNull Class<T> clazz, @NonNull RestParams params) {
        params = new RestParams.Builder(params).withForceReadFromCache(false).build();
        Retrofit restAdapter = buildAdapterNoRedirects(params);
        return restAdapter.create(clazz);
    }

    public <T> T buildPing(@NonNull Class<T> clazz, @NonNull RestParams params) {
        Retrofit restAdapter = buildPingAdapter(params.getDomain());
        return restAdapter.create(clazz);
    }

    public static boolean clearCacheDirectory(@NonNull CanvasConfig config) {
        try {
            return CanvasRestAdapter.getCacheDirectory(config).delete();
        } catch (Exception e) {
            Logger.e("Could not delete cache " + e);
            return false;
        }
    }
}

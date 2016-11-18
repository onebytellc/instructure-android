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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.facebook.stetho.Stetho;
import com.instructure.canvasapi2.models.CanvasTheme;
import com.instructure.canvasapi2.utils.CanvasConfigManager;


public abstract class AppManager extends MultiDexApplication {

    private static CanvasConfigManager mCanvasConfig;
    private static @Nullable CanvasTheme mCanvasTheme;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        mCanvasConfig = new CanvasConfigManager(getApplicationContext());
    }

    public static CanvasConfigManager getConfig() {
        return mCanvasConfig;
    }

    public static void setCanvasTheme(@NonNull CanvasTheme theme) {
        mCanvasTheme = theme;
    }

    @Nullable
    public static CanvasTheme getCanvasTheme() {
        return mCanvasTheme;
    }

    /**
     * Used during logout so we clear the user
     */
    public static void resetConfigManager(Context context) {
        mCanvasConfig = new CanvasConfigManager(context);
    }

    public static boolean hasNetworkConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}

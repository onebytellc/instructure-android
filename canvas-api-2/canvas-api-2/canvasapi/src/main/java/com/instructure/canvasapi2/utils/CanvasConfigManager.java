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

import android.content.Context;
import android.support.annotation.NonNull;

import com.instructure.canvasapi2.CanvasConfig;
import com.instructure.canvasapi2.builders.RestParams;

import java.io.File;


public class CanvasConfigManager implements CanvasConfig {

    private String mFullDomain;
    private String mDomain;
    private String mToken;
    private String mProtocol;
    private String mUserAgent;
    private RestParams mParams;
    private File mCacheDir;
    private boolean mIsMasquerading;
    private long mMasqueradingId;

    public CanvasConfigManager(@NonNull Context context) {
        initConfig(context);
    }

    @Override
    public void initConfig(@NonNull Context context) {
        mFullDomain = APIHelper.getFullDomain(context);
        mUserAgent = APIHelper.getUserAgent(context);
        mCacheDir = context.getCacheDir();
        mDomain = APIHelper.getDomain(context);
        mToken = APIHelper.getToken(context);
        mProtocol = APIHelper.loadProtocol(context);
        mMasqueradingId = Masquerading.getMasqueradingId(context);
        mIsMasquerading = Masquerading.isMasquerading(context);
    }

    @Override
    public int perPageCount() {
        return 100;
    }

    @Override
    public File cacheDir() {
        return mCacheDir;
    }

    @NonNull
    @Override
    public String fullDomain() {
        if(mFullDomain == null) {
            return "";
        }
        return mFullDomain;
    }

    @NonNull
    @Override
    public String token() {
        if(mToken == null) {
            return "";
        }
        return mToken;
    }

    @NonNull
    @Override
    public String protocol() {
        if(mProtocol == null) {
            return "";
        }
        return mProtocol;
    }

    @NonNull
    @Override
    public String domain() {
        if(mDomain == null) {
            return "";
        }
        return mDomain;
    }

    @NonNull
    @Override
    public String userAgent() {
        if(mUserAgent == null) {
            return "";
        }
        return mUserAgent;
    }

    @NonNull
    @Override
    public RestParams getParams() {
        if(mParams == null) new RestParams.Builder().build();
        return mParams;
    }

    @Override
    public boolean isMasquerading() {
        return mIsMasquerading;
    }

    @Override
    public long masqueradingId() {
        return mMasqueradingId;
    }

    @Override
    public void setParams(RestParams params) {
        mParams = params;
    }

    public void setToken(@NonNull String token) {
        mToken = token;
    }

    @Override
    public void setIsMasquerading(boolean isMasquerading) {
        mIsMasquerading = isMasquerading;
    }

    @Override
    public void setMasqueradingId(long masqueradingId) {
        mMasqueradingId = masqueradingId;
    }
}

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

import android.app.Application;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.Locale;

import okhttp3.CacheControl;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class RequestInterceptor implements Interceptor {

    @Nullable
    private StatusCallback mCallback;

    public RequestInterceptor(StatusCallback callback) {
        mCallback = callback;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();
        Request.Builder builder = request.newBuilder();

        CanvasConfig config = AppManager.getConfig();

        final String token = config.token();
        final String userAgent = config.userAgent();
        final String domain = config.protocol() + "://" + config.domain();

        //Set the UserAgent
        if(!userAgent.equals("")) {
            builder.addHeader("User-Agent", userAgent);
        }

        //Authenticate if possible
        if(!config.getParams().shouldIgnoreToken() && !token.equals("")){
            builder.addHeader("Authorization", "Bearer " + token);
        }

        //Add Accept-Language header for a11y
        builder.addHeader("accept-language", getAcceptedLanguageString());

        //If the callback is null assume a network connection exists or check for realz
        final boolean hasNetworkConnection = mCallback == null || mCallback.getStatusDelegate().hasNetworkConnection();

        if(!hasNetworkConnection || config.getParams().isForceReadFromCache()) {
            //Offline or only want cached data
            builder.cacheControl(CacheControl.FORCE_CACHE);
        } else if(config.getParams().isForceReadFromNetwork()) {
            //Typical from a pull-to-refresh
            builder.cacheControl(CacheControl.FORCE_NETWORK);
        }

        //Fun Fact: HTTP referer (originally a misspelling of referrer) is an HTTP header field that identifies
        // the address of the webpage that linked to the resource being requested
        //Source: https://en.wikipedia.org/wiki/HTTP_referer
        //Institutions need the referrer for a variety of reasons - mostly for restricted content
        builder.addHeader("Referer", domain);

        request = builder.build();

        //Masquerade if necessary
        if (config.isMasquerading()) {
            HttpUrl url = request.url().newBuilder().addQueryParameter("as_user_id", Long.toString(config.masqueradingId())).build();
            request = request.newBuilder().url(url).build();
        }

        if(config.getParams().usePerPageQueryParam()) {
            HttpUrl url = request.url().newBuilder().addQueryParameter("per_page", Integer.toString(config.perPageCount())).build();
            request = request.newBuilder().url(url).build();
        }

        return chain.proceed(request);
    }

    public String getAcceptedLanguageString() {
        String language = Locale.getDefault().getLanguage();
        //This is kinda gross, but Android is terrible and doesn't use the standard for lang strings...
        String language3 = Locale.getDefault().toString().replace("_", "-");

        return language3 + "," + language;
    }
}

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

package com.instructure.canvasapi2.managers;

import android.support.annotation.NonNull;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.OAuthAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.OAuthToken;


public class OAuthManager {

    public static void deleteToken(@NonNull final StatusCallback.StatusDelegate delegate) {
        StatusCallback<Void> callback = new StatusCallback<Void>(delegate){};
        RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(false)
                .withForceReadFromCache(false)
                .withForceReadFromNetwork(true)
                .build();

        OAuthAPI.deleteToken(adapter, params, callback);
    }

    public static void getToken(String clientID, String clientSecret, String oAuthRequest, StatusCallback<OAuthToken> callback) {
        RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(false)
                .withForceReadFromCache(false)
                .withForceReadFromNetwork(true)
                .build();
        OAuthAPI.getToken(adapter, params, clientID, clientSecret, oAuthRequest, callback);
    }
}

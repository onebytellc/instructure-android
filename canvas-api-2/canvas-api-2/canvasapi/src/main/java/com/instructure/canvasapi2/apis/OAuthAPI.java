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

package com.instructure.canvasapi2.apis;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.OAuthToken;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.POST;
import retrofit2.http.Query;


public class OAuthAPI {

    interface OAuthInterface {
        @DELETE("/login/oauth2/token")
        Call<Void> deleteToken();

        @POST("/login/oauth2/token")
        Call<OAuthToken> getToken(@Query("client_id") String clientId, @Query("client_secret") String clientSecret, @Query("code") String oAuthRequest, @Query(value = "redirect_uri", encoded = true) String redirectURI);

    }

    public static void deleteToken(RestBuilder adapter, RestParams params, StatusCallback<Void> callback) {
        callback.addCall(adapter.build(OAuthInterface.class, params).deleteToken()).enqueue(callback);
    }

    public static void getToken(RestBuilder adapter, RestParams params, String clientID, String clientSecret, String oAuthRequest, StatusCallback<OAuthToken> callback) {
        callback.addCall(adapter.build(OAuthInterface.class, params).getToken(clientID, clientSecret, oAuthRequest, "urn:ietf:wg:oauth:2.0:oob")).enqueue(callback);
    }
}

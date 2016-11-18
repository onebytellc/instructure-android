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

import android.support.annotation.NonNull;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.AccountDomain;
import com.instructure.canvasapi2.utils.DepaginatedCallback;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;


public class AccountDomainAPI {

    private static final String DEFAULT_DOMAIN = "https://canvas.instructure.com/";

    public interface AccountDomainInterface {

        @GET("accounts/search")
        Call<List<AccountDomain>> getDomains();

        @GET
        Call<List<AccountDomain>> next(@Url String nextURL);

        @GET("accounts/search")
        Call<List<AccountDomain>> searchDomains(
                @Query("name") String campusName,
                @Query("domain") String domain,
                @Query("latitude") float latitude,
                @Query("longitude") float longitude);

    }

    public static void getFirstPageAccountDomains(
            @NonNull RestBuilder adapter,
            @NonNull StatusCallback<List<AccountDomain>> callback,
            @NonNull RestParams params) {

        callback.addCall(adapter.build(AccountDomainInterface.class, params).getDomains()).enqueue(callback);
    }

    public static void getNextPageAccountDomains(
            @NonNull RestBuilder adapter,
            @NonNull String nextUrl,
            @NonNull StatusCallback<List<AccountDomain>> callback,
            @NonNull RestParams params) {

        callback.addCall(adapter.build(AccountDomainInterface.class, params).next(nextUrl)).enqueue(callback);
    }

    public static void getAllAccountDomains(final StatusCallback<List<AccountDomain>> callback) {
        final RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
        final RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(true)
                .withPerPageQueryParam(true)
                .withForceReadFromCache(false)
                .withForceReadFromNetwork(true)
                .withDomain(DEFAULT_DOMAIN)
                .build();

        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(AccountDomainInterface.class, params).getDomains()).enqueue(callback);
        } else if (StatusCallback.moreCallsExist(callback.getLinkHeaders()) && callback.getLinkHeaders() != null) {
            callback.addCall(adapter.build(AccountDomainInterface.class, params).next(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }
}

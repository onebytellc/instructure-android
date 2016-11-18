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

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.Recipient;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;


public class RecipientAPI {

    interface RecipientInterface {
        @GET("search/recipients?synthetic_contexts=1")
        Call<List<Recipient>> getFirstPageRecipientList(@Query("search") String searchQuery, @Query(value = "context", encoded = true) String context);

        @GET
        Call<List<Recipient>> getNextPageRecipientList(@Url String url);
    }

    public static void getFirstPageRecipients(String searchQuery, String context, @NonNull StatusCallback<List<Recipient>> callback, @NonNull RestBuilder adapter, @NonNull RestParams params) {
        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(RecipientInterface.class, params).getFirstPageRecipientList(searchQuery, context)).enqueue(callback);
        } else if (StatusCallback.moreCallsExist(callback.getLinkHeaders()) && callback.getLinkHeaders() != null) {
            callback.addCall(adapter.build(RecipientInterface.class, params).getNextPageRecipientList(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }

}

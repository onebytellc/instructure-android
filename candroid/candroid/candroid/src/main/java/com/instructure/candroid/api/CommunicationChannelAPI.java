/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.candroid.api;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.instructure.candroid.model.CommunicationChannel;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.Masquerading;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Query;

public class CommunicationChannelAPI {

    interface CommunicationChannelInterface {
        @POST("/users/self/communication_channels?communication_channel[type]=push")
        void addPushCommunicationChannel(@Query("communication_channel[token]") String registrationId, @Body String body, Callback<Response> callback);

        @POST("/users/self/communication_channels?communication_channel[type]=push")
        Response addPushCommunicationChannel(@Query("communication_channel[token]") String registrationId, @Body String body);
    }

    /////////////////////////////////////////////////////////////////////////
    // Build Interface Helpers
    /////////////////////////////////////////////////////////////////////////
    private static CommunicationChannelInterface buildInterface(Context context) {
        RestAdapter restAdapter = CanvasRestAdapter.buildAdapter(context);
        return restAdapter.create(CommunicationChannelInterface.class);
    }

    /////////////////////////////////////////////////////////////////////////
    // API Calls
    /////////////////////////////////////////////////////////////////////////
    public static void addNewPushCommunicationChannel(String registrationId, Callback<Response> callback, Context context) {
        if (APIHelpers.paramIsNull(callback, registrationId)) {
            return;
        }
        buildInterface(context).addPushCommunicationChannel(registrationId, "", callback);
    }

    public static Response addNewPushCommunicationChannel(String registrationId, Context context) {
        if (APIHelpers.paramIsNull(registrationId)) {
            return null;
        }
        return buildInterface(context).addPushCommunicationChannel(registrationId, "");
    }
}

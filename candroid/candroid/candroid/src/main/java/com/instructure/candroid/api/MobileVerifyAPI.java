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
import com.instructure.candroid.model.DomainVerificationResult;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.RetrofitCounter;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.http.EncodedQuery;
import retrofit.http.GET;
import retrofit.http.Query;

public class MobileVerifyAPI
{

    private static RestAdapter getAuthenticationRestAdapter(final Context context){

        RetrofitCounter.increment();

        final String userAgent = APIHelpers.getUserAgent(context);

        return new RestAdapter.Builder()
                .setEndpoint("https://canvas.instructure.com/api/v1") // The base API endpoint.
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade requestFacade) {
                        if (!userAgent.equals("")) {
                            requestFacade.addHeader("User-Agent", APIHelpers.getUserAgent(context));
                        }
                    }
                })
                .build();
    }

    interface OAuthInterface {
        @GET("/mobile_verify.json")
        void mobileVerify(@EncodedQuery("domain")String domain, @Query("user_agent") String userAgent, CanvasCallback<DomainVerificationResult> callback);
    }

    public static void mobileVerify(String domain, CanvasCallback<DomainVerificationResult> callback) {
        if (APIHelpers.paramIsNull(callback, domain)) { return; }

        final String userAgent = APIHelpers.getUserAgent(callback.getContext());
        if(userAgent.equals("")){
            Log.d(APIHelpers.LOG_TAG, "User agent must be set for this API to work correctly!");
            return;
        }

        OAuthInterface oAuthInterface = getAuthenticationRestAdapter(callback.getContext()).create(OAuthInterface.class);
        oAuthInterface.mobileVerify(domain,userAgent, callback);
    }

}

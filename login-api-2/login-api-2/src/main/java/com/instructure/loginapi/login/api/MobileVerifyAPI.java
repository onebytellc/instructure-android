package com.instructure.loginapi.login.api;


import android.content.Context;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.canvasapi2.utils.RetrofitCounter;
import com.instructure.loginapi.login.model.DomainVerificationResult;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class MobileVerifyAPI {

    private static Retrofit getAuthenticationRetrofit(final Context context) {

        final String userAgent = APIHelper.getUserAgent(context);

        RetrofitCounter.increment();

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        if (!userAgent.equals("")) {
                            Request request = chain.request().newBuilder()
                                    .header("User-Agent", APIHelper.getUserAgent(context))
                                    .cacheControl(CacheControl.FORCE_NETWORK)
                                    .build();
                            return chain.proceed(request);
                        } else {
                            return chain.proceed(chain.request());
                        }
                    }
                })
                .addInterceptor(loggingInterceptor)
                .build();

        return new Retrofit.Builder()
                .baseUrl("https://canvas.instructure.com/api/v1/")
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    interface OAuthInterface {
        @GET("mobile_verify.json")
        Call<DomainVerificationResult> mobileVerify (@Query(value = "domain", encoded = false) String domain, @Query("user_agent") String userAgent);
    }

    public static void mobileVerify(Context context, String domain, StatusCallback<DomainVerificationResult> callback) {
        if (APIHelper.paramIsNull(callback, domain)) {
            return;
        }

        final String userAgent = APIHelper.getUserAgent(context);
        if (userAgent.equals("")) {
            Logger.d("User agent must be set for this API to work correctly!");
            return;
        }

        OAuthInterface oAuthInterface = getAuthenticationRetrofit(context).create(OAuthInterface.class);
        oAuthInterface.mobileVerify(domain, userAgent).enqueue(callback);
    }
}

package com.instructure.loginapi.login.api.zendesk.utilities;

import android.util.Base64;

import com.instructure.canvasapi2.utils.RetrofitCounter;
import com.instructure.loginapi.login.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class ZendeskRestAdapter {

    public static final String API_DOMAIN = BuildConfig.ZENDESK_DOMAIN;

    /**
     * Returns a Retrofit Instance for use with ZendDesk APIs
     */
    public static Retrofit buildAdapter() {

        RetrofitCounter.increment();

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.addInterceptor(new ZendeskRequestInterceptor());

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            httpClientBuilder.addInterceptor(loggingInterceptor);
        }

        return new Retrofit.Builder()
                .baseUrl(API_DOMAIN + "/api/v2/")
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Class that's used as to inject the user agent, token, and handles masquerading.
     */
    public static class ZendeskRequestInterceptor implements Interceptor {

        private String encodeCredentialsForBasicAuthorization() {
            return "Basic " + Base64.encodeToString(BuildConfig.ZENDESK_CREDENTIALS.getBytes(), Base64.NO_WRAP);
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request().newBuilder()
                    .addHeader("Authorization", encodeCredentialsForBasicAuthorization())
                    .addHeader("Content-Type", "application/json")
                    .build();
            return chain.proceed(request);
        }
    }
}

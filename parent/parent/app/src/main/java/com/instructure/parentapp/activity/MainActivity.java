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

package com.instructure.parentapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.AlertAPI;
import com.instructure.canvasapi2.apis.PingAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.RevokedTokenResponse;
import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.canvasapi2.utils.Logger;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.Prefs;
import com.instructure.parentapp.BuildConfig;
import com.instructure.parentapp.R;
import com.instructure.parentapp.util.ViewUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class MainActivity extends BaseParentActivity {

    private int mCallbackCount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.parentStatusBarColor));

        if(!TextUtils.isEmpty(APIHelper.getToken(MainActivity.this))) {
            checkSignedIn(true);
        } else if(!APIHelper.airwolfDomainExists(MainActivity.this)){
            checkRegion();
        } else {
            startActivity(ParentLoginActivity.createIntent(MainActivity.this));
            overridePendingTransition(0, 0);
            finish();
        }
    }


    void checkSignedIn(boolean runOnUIThread) {
        Thread backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //Now get it from the new place. This will be the true token whether they signed into dev/retrofit or the old way.
                String token = APIHelper.getToken(MainActivity.this);
                APIHelper.setProtocol("https", MainActivity.this);

                if (token != null && !token.equals("")) {
                    //We now need to get the cache user

                    Prefs prefs = new Prefs(MainActivity.this, getString(R.string.app_name_parent));
                    String parentId = prefs.load(Const.ID, "");

                    if(!TextUtils.isEmpty(parentId)) {

                        UserManager.getStudentsForParentAirwolf(
                                APIHelper.getAirwolfDomain(MainActivity.this),
                                parentId, new StatusCallback<List<Student>>(mStatusDelegate) {
                                    @Override
                                    public void onResponse(retrofit2.Response<List<Student>> response, LinkHeaders linkHeaders, ApiType type) {

                                        if (response.body() != null && !response.body().isEmpty()) {
                                            //they have students that they are observing, take them to that activity
                                            startActivity(StudentViewActivity.createIntent(MainActivity.this, response.body()));
                                            overridePendingTransition(0, 0);
                                            finish();

                                        } else {
                                            //Take the parent to the add user page.
                                            startActivity(DomainPickerActivity.createIntent(MainActivity.this, false, false, true));
                                            overridePendingTransition(0, 0);
                                            finish();
                                        }
                                    }
                                }
                        );
                    }
                }
            }
        });

        if (runOnUIThread) {
            backgroundThread.run();
        } else {
            backgroundThread.start();
        }
    }

    private void checkRegion() {
        //is the region set?

        if(BuildConfig.IS_TESTING) {
            Utils.d("QA Testing - Setting to Gamma Domain");
            APIHelper.setAirwolfDomain(MainActivity.this, BuildConfig.GAMMA_DOMAIN);

            startActivity(ParentLoginActivity.createIntent(MainActivity.this));
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        //keep track of how many api calls have finished
        mCallbackCount = 0;

        if(!APIHelper.isAirwolfDomainSet(MainActivity.this)) {
            //get the region
            final Map<String, ArrayList<Long>> pingMap = new HashMap<>();

            StatusCallback<Void> pingCallback = new StatusCallback<Void>(mStatusDelegate) {
                @Override
                public void onResponse(Response<Void> response, LinkHeaders linkHeaders, ApiType type, int code) {
                    mCallbackCount++;
                    checkPingTime(response);
                    checkCount();
                }

                @Override
                public void onFail(Call<Void> response, Throwable error, int code) {
                    mCallbackCount++;
                    checkCount();
                }

                private void checkPingTime(retrofit2.Response response) {
                    if(response == null) return;

                    try {

                        final okhttp3.Response okResponse = response.raw();
                        final String url = "https://" + okResponse.request().url().url().getHost();

                        ArrayList<Long> ping = pingMap.get(url);
                        if (ping == null) {
                            ping = new ArrayList<>();
                        }

                        if(response.code() >= 200 && response.code() < 300) {
                            //Only add pings that are valid 200s
                            ping.add(okResponse.receivedResponseAtMillis() - okResponse.sentRequestAtMillis());
                        }

                        pingMap.put(url, ping);

                    } catch (Exception e) {
                        Logger.e("Could not ping the pong.");
                    }
                }

                private void checkCount() {
                    //check to see if we've gone through all of the domains
                    // (The multiplier should be equal to the number of pings we do below)
                    if(mCallbackCount == (AlertAPI.AIRWOLF_DOMAIN_LIST.length * 5)) {
                        String bestRegion = "";
                        long bestTime = Long.MAX_VALUE;

                        for(String domain : pingMap.keySet()) {
                            ArrayList<Long> pings = pingMap.get(domain);
                            Long sum = 0L;
                            if(!pings.isEmpty()) {
                                for (Long ping : pings) {
                                    sum += ping;
                                }
                                long average  = sum / pings.size();

                                if(average < bestTime) {
                                    bestRegion = domain;
                                    bestTime = average;
                                    Log.d("Region", "New best time for region (" + bestTime + ") " + domain);
                                } else {
                                    Log.d("Region", "Region didn't make the cut for user " + domain);
                                }
                            } else {
                                Log.d("Region", "Failed to find pings for " + domain);
                            }
                        }

                        Log.d("Region", "Closest url is " + bestRegion);
                        //save the url with the lowest time
                        APIHelper.setAirwolfDomain(MainActivity.this, bestRegion);

                        startActivity(ParentLoginActivity.createIntent(MainActivity.this));
                        overridePendingTransition(0, 0);
                        finish();
                    }
                }
            };

            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), pingCallback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withAPIVersion("")
                    .build();

            //make the actual api calls
            for(String url : AlertAPI.AIRWOLF_DOMAIN_LIST) {
                PingAPI.getPing(url, adapter, pingCallback, params);
                PingAPI.getPing(url, adapter, pingCallback, params);
                PingAPI.getPing(url, adapter, pingCallback, params);
                PingAPI.getPing(url, adapter, pingCallback, params);
                PingAPI.getPing(url, adapter, pingCallback, params);
            }

        } else {
            startActivity(ParentLoginActivity.createIntent(MainActivity.this));
            overridePendingTransition(0, 0);
            finish();
        }
    }
    
    //region Intents

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }

    public static Intent createIntent(Context context, boolean showMessage, String message) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Const.SHOW_MESSAGE, showMessage);
        intent.putExtra(Const.MESSAGE_TO_USER, message);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }

    //endregion

    @Override
    public void unBundle(@NonNull Bundle extras) {

    }

    @Override
    public boolean applyThemeAutomagically() {
        return false;
    }
}

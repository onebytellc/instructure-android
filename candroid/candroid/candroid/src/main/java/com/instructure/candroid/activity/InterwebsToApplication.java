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

package com.instructure.candroid.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;


import com.instructure.candroid.R;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.pandautils.utils.Const;
import com.instructure.candroid.util.LoggingUtility;
import com.instructure.candroid.util.RouterUtils;
import com.instructure.canvasapi.utilities.APIHelpers;

import java.util.ArrayList;
import java.util.List;


public class InterwebsToApplication extends Activity {


    String host = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interwebs_to_application);

        final String url = getIntent().getDataString();

        if(TextUtils.isEmpty(url)) {
            finish();
            return;
        }

        final Uri data =  Uri.parse(url);

        if(data == null) {
            finish();
            return;
        }

        host = data.getHost(); // "mobiledev.instructure.com"

        //Log to GA.
        Analytics.trackAppFlow(this);

        //Do some logging
        LoggingUtility.Log(this, Log.WARN, data.toString());

        String token = APIHelpers.getToken(this);

        boolean signedIn = (token != null && token.length() != 0);
        String domain = APIHelpers.getDomain(InterwebsToApplication.this);
        if (!signedIn) {
            Intent intent = LoginActivity.createIntent(this, host, data, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (signedIn && (domain == null || !domain.contains(host))) {
            Intent intent = new Intent(this, NavigationActivity.getStartActivityClass());
            intent.putExtra(Const.MESSAGE, getString(R.string.differentDomainFromLink));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        } else {
            //Allow the UI to show.
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    RouterUtils.routeUrl(InterwebsToApplication.this, url, false);
                    finish();
                }
            }, 500);
        }
    }

    public static Intent createIntent(Context context, Uri uri) {
        Intent intent = new Intent(context, InterwebsToApplication.class);
        intent.setData(uri);
        return intent;
    }
}

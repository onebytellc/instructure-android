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

package com.instructure.candroid.receivers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.instructure.candroid.BuildConfig;
import com.instructure.candroid.model.PushNotification;
import com.instructure.candroid.service.PushService;
import com.instructure.candroid.util.Const;
import com.instructure.loginapi.login.util.Utils;

public class PushExternalReceiver extends WakefulBroadcastReceiver {

    private enum TYPE {RECEIVE, REGISTER, REGISTRATION, NONE}

    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.d("PushExternalReceiver onReceive()");
        boolean completeWakeful = true;
        if(intent != null) {
            Bundle extras = intent.getExtras();
            if(extras != null) {
                String html_url = extras.getString(PushNotification.HTML_URL, "");
                final String from = extras.getString(PushNotification.FROM, "");
                final String alert = extras.getString(PushNotification.ALERT, "");
                final String collapse_key = extras.getString(PushNotification.COLLAPSE_KEY, "");
                final String user_id = extras.getString(PushNotification.USER_ID, "");

                Utils.d("PUSH html_url: " + html_url);
                Utils.d("PUSH from: " + from);
                Utils.d("PUSH alert: " + alert);
                Utils.d("PUSH collapse_key: " + collapse_key);
                Utils.d("PUSH user_id: " + user_id);

                if(getActionType(intent.getAction()) == TYPE.RECEIVE) {
                    PushNotification push = new PushNotification(html_url, from, alert, collapse_key, user_id);
                    if(PushNotification.store(context, push)) {
                        Intent pushServiceIntent = new Intent(context, PushService.class);
                        pushServiceIntent.putExtra(Const.EXTRAS, extras);
                        context.startService(pushServiceIntent);
                        completeWakeful = false;
                    }
                }
            }
        }

        if(completeWakeful) {
            PushExternalReceiver.completeWakefulIntent(intent);
        }
    }

    private TYPE getActionType(String action) {
        if("com.google.android.c2dm.intent.RECEIVE".equals(action)) {
            return TYPE.RECEIVE;
        } else if("com.google.android.c2dm.intent.REGISTER".equals(action)) {
            return TYPE.REGISTER;
        } else if("com.google.android.c2dm.intent.REGISTRATION".equals(action)) {
            return TYPE.REGISTRATION;
        }
        return TYPE.NONE;
    }
}

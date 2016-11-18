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

package com.instructure.candroid.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.instructure.candroid.BuildConfig;
import com.instructure.candroid.R;
import com.instructure.candroid.activity.NavigationActivity;
import com.instructure.candroid.model.PushNotification;
import com.instructure.candroid.receivers.PushDeleteReceiver;
import com.instructure.candroid.receivers.PushExternalReceiver;
import com.instructure.candroid.util.Const;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.loginapi.login.util.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class PushService extends IntentService {

    public static final String NEW_PUSH_NOTIFICATION = "newPushNotification";
    private static final String GROUP_ID = "canvasPushNotificationsGroup";

    public PushService() {
        super("PushService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Bundle extras = intent.getBundleExtra(Const.EXTRAS);

        if(extras != null) {
            postNotification(getApplicationContext(), extras);
        }

        PushExternalReceiver.completeWakefulIntent(intent);
    }

    public static void postStoredNotifications(Context context) {
        postNotification(context, null);
    }

    public static void postNotification(Context context, Bundle extras){

        final User user = APIHelpers.getCacheUser(context);
        String userDomain = APIHelpers.getDomain(context);

        final String html_url = getHtmlUrl(extras);
        final String user_id = PushNotification.getUserIdFromPush(getUserId(extras));

        String incomingDomain = "";

        try {
            incomingDomain = new URL(html_url).getHost();
        } catch (MalformedURLException e) {
            Utils.e("HTML URL MALFORMED");
        } catch (NullPointerException e) {
            Utils.e("HTML URL IS NULL");
        }

        if(user != null && !TextUtils.isEmpty(user_id)) {
            String currentUserId = Long.toString(user.getId());
            if(!user_id.equalsIgnoreCase(currentUserId)) {
                Utils.e("USER IDS MISMATCHED");
                return;
            }
        } else {
            Utils.e("USER WAS NULL OR USER_ID WAS NULL");
            return;
        }

        if(BuildConfig.IS_DEBUG) {
            //userDomain = userDomain.replaceFirst(".beta", "");//Left for use as needed
        }

        if(TextUtils.isEmpty(incomingDomain) || TextUtils.isEmpty(userDomain) || !incomingDomain.equalsIgnoreCase(userDomain)) {
            Utils.e("DOMAINS DID NOT MATCH");
            return;
        }

        final NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        List<PushNotification> pushes = PushNotification.getStoredPushes(context);//Will never be null

        if(pushes.size() == 0 && extras == null) {
            //Nothing to post, situation would occur from the BootReceiver
            return;
        }

        final Intent contentIntent = new Intent(context, NavigationActivity.getStartActivityClass());
        contentIntent.putExtra(NEW_PUSH_NOTIFICATION, true);
        if(extras != null) {
            contentIntent.putExtras(extras);
        }

        final Intent deleteIntent = new Intent(context, PushDeleteReceiver.class);

        final PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        final Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_canvas_logo_white)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(getMessage(extras))
                .setContentIntent(contentPendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setGroup(GROUP_ID)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .build();

        mNotificationManager.notify(555443, notification);
    }

    private static String getMessage(Bundle extras) {
        if(extras == null) {
            return "";
        }
        return extras.getString(PushNotification.ALERT, "");
    }

    private static String getUserId(Bundle extras) {
        if(extras == null) {
            return "";
        }
        return extras.getString(PushNotification.USER_ID, "");
    }

    private static String getHtmlUrl(Bundle extras) {
        if(extras == null) {
            return "";
        }
        return extras.getString(PushNotification.HTML_URL, "");
    }

    public static Bundle createFakePushNotification(String alert, String url) {
        Bundle extras = new Bundle();
        extras.putString(PushNotification.FROM, "1234567890");
        extras.putString(PushNotification.ALERT, alert);
        extras.putString(PushNotification.HTML_URL, url);
        extras.putString(PushNotification.COLLAPSE_KEY, "collapseKeyData");
        extras.putString(PushNotification.USER_ID, "123456");
        return extras;
    }
}

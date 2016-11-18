/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.androidfoosball.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.instructure.androidfoosball.R;
import com.instructure.androidfoosball.activities.SignInActivity;


public class FireBasePushNotificationService extends FirebaseMessagingService {

    private static final int NOTIFICATION_ID = 444930;

    public FireBasePushNotificationService() {
        super();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("abcde", "From: " + remoteMessage.getFrom());
        //Get the channel the users subscribed too
        String pushId = remoteMessage.getFrom().replace("/topics/", "");
        //Unsubscribe
        FirebaseMessaging.getInstance().unsubscribeFromTopic(pushId);

        Intent notificationIntent = new Intent(getApplicationContext(), SignInActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

        //post notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setSmallIcon(R.drawable.ic_status_notification);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentIntent(intent);
        builder.setAutoCancel(true);
        final String tableName = remoteMessage.getData().get("message");
        final String contentText = String.format(getString(R.string.push_text), tableName);
        builder.setContentText(contentText);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());

        if (remoteMessage.getData().size() > 0) {
            Log.d("abcde", "Message data payload: " + remoteMessage.getData());
        }

        if (remoteMessage.getNotification() != null) {
            Log.d("abcde", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.d("abcde", "Push Message Deleted");
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        Log.d("abcde", "Push Message Sent: " + s);
    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
        Log.d("abcde", "Push Message Error: " + s);
    }
}

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

package com.instructure.parentapp.receivers;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.instructure.pandautils.utils.Const;
import com.instructure.parentapp.R;
import com.instructure.parentapp.activity.MainActivity;
import com.instructure.parentapp.database.DatabaseHandler;

import java.sql.SQLException;
import java.util.Calendar;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String title = intent.getStringExtra(Const.TITLE_TEXT);
        String subTitle = intent.getStringExtra(Const.SUBTITLE_TEXT);

        if(TextUtils.isEmpty(title)) {
            title = context.getResources().getString(R.string.app_name_parent);
        }
        if(TextUtils.isEmpty(subTitle)) {
            subTitle = "";
        }
        //set up the notification
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.notification_small_icon)
                        .setContentTitle(title)
                        .setContentText(subTitle);


        Intent resultIntent = new Intent(context, MainActivity.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);


        // Sets an ID for the notification
        // Make it unique based on the title and subtitle
        int mNotificationId = title.hashCode() + subTitle.hashCode();
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }

    public void setAlarm(Context context, Calendar calendar, long assignmentId, String title, String subTitle) {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmReceiver.class);
        i.putExtra(Const.TITLE_TEXT, title);
        i.putExtra(Const.SUBTITLE_TEXT, subTitle);
        //set alarms here

        //get the row id and set it as the request code so it will be unique
        int alarmId = -1;
        DatabaseHandler mDatabaseHandler = new DatabaseHandler(context);
        try {
            mDatabaseHandler.open();
            alarmId = mDatabaseHandler.getRowIdByAssignmentId(assignmentId);
            mDatabaseHandler.close();
        } catch (SQLException e) {
            //can't find the alarmId
        }

        //verify that we have a valid alarm id
        if(alarmId != -1) {
            PendingIntent pi = PendingIntent.getBroadcast(context, alarmId, i, PendingIntent.FLAG_UPDATE_CURRENT);
            am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
        }
    }

    public void cancelAlarm(Context context, long assignmentId, String title, String subTitle) {
        //need to create an intent that matches the one we want to cancel
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(Const.TITLE_TEXT, title);
        intent.putExtra(Const.SUBTITLE_TEXT, subTitle);

        int alarmId = -1;
        DatabaseHandler mDatabaseHandler = new DatabaseHandler(context);
        try {
            mDatabaseHandler.open();
            alarmId = mDatabaseHandler.getRowIdByAssignmentId(assignmentId);
            mDatabaseHandler.close();
        } catch (SQLException e) {

        }

        PendingIntent sender = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        sender.cancel();
        alarmManager.cancel(sender);
    }
}
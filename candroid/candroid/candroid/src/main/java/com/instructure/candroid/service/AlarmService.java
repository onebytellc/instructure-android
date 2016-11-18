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


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.instructure.candroid.adapter.CalendarListRecyclerAdapter;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.candroid.util.LoggingUtility;
import com.instructure.canvasapi.api.CalendarEventAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.ScheduleItem;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.Prefs;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit.client.Response;

public class AlarmService extends IntentService
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, APIStatusDelegate {

    public static final String ACTION_SCHEDULE_CALENDAR_EVENT_UPDATE =
            "com.instructure.candroid.ACTION_SCHEDULE_CALENDAR_EVENT_UPDATE";
    public static final String ACTION_NOTIFY_EVENT =
            "com.instructure.candroid.ACTION_NOTIFY_EVENT";

    public static final int NOTIFICATION_ID = 100;

    public static final String NOTIFICATION_PATH = "/wearable/notifications/";
    public static final String KEY_ID = "schedule-id";
    private static final String KEY_TITLE = "schedule-title";
    private static final String KEY_DESCRIPTION = "schedule-description";
    private static final String KEY_DATE = "schedule-date";
    private static final String KEY_COLOR = "schedule-color";

    private GoogleApiClient mGoogleApiClient;


    public AlarmService() {
        super("AlarmService");
    }

    public AlarmService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mGoogleApiClient.blockingConnect(2000, TimeUnit.MILLISECONDS);
        final String action = intent.getAction();

        LoggingUtility.LogConsole("AlarmService handling " + action);

        if (ACTION_SCHEDULE_CALENDAR_EVENT_UPDATE.equals(action)) {
            LoggingUtility.LogConsole("Scheduling calendar updates.");
            scheduleAlarm(getContext());
        } else if(ACTION_NOTIFY_EVENT.equals(action)) {
            LoggingUtility.LogConsole("Notifying event.");
            notifyEvent();
        }

    }

    public static void scheduleAlarm(Context context) {

        //we don't want to schedule an alarm if they have the wear notifications disabled
        if(!ApplicationManager.getPrefs(context).load(Const.WEAR_NOTIFICATIONS, false)) {
            return;
        }

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);

        //get the preferences of the user
        Prefs prefs = new Prefs(context, Const.WEAR_REMINDER);
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, prefs.load(Const.WEAR_REMINDER_HOUR, 0));
        cal.set(Calendar.MINUTE, prefs.load(Const.WEAR_REMINDER_MIN, 0));

        final Intent notifIntent = new Intent(
                ACTION_NOTIFY_EVENT,
                null,
                context,
                AlarmService.class);
        // Setting data to ensure intent's uniqueness for different session start times.
        notifIntent.setData(
                new Uri.Builder().authority("com.instructure.candroid")
                        .path(String.valueOf(cal.getTimeInMillis())).build()
        );

        PendingIntent pi = PendingIntent.getService(context,
                0,
                notifIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Schedule an alarm to be fired to notify user of added sessions are about to begin.
        LoggingUtility.LogConsole("-> Scheduling RTC alarm at " + cal.getTime().toString());
        am.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }

    private void notifyEvent() {

        //get the latest events for today
        Calendar startOfDay = Calendar.getInstance();
        startOfDay.setTime(new Date());

        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        Calendar endOfDay = Calendar.getInstance();
        endOfDay.setTime(new Date());
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);

        //if they have calendars set, use those
        if(CalendarListRecyclerAdapter.getFilterPrefs(getContext()).size() > 0) {
            CalendarEventAPI.getAllCalendarEventsExhaustive(CalendarEventAPI.EVENT_TYPE.ASSIGNMENT_EVENT,
                    APIHelpers.dateToString(startOfDay.getTime()),
                    APIHelpers.dateToString(endOfDay.getTime()),
                    CalendarListRecyclerAdapter.getFilterPrefs(getContext()), new CanvasCallback<ScheduleItem[]>(this) {
                @Override
                public void cache(ScheduleItem[] scheduleItems, LinkHeaders linkHeaders, Response response) {
                    // No super call disables cache
                }

                @Override
                public void firstPage(ScheduleItem[] scheduleItems, LinkHeaders linkHeaders, Response response) {
                    for (ScheduleItem scheduleItem : scheduleItems) {
                        int color = CanvasContextColor.getCachedColor(getContext(), CanvasContext.makeContextId(scheduleItem.getContextType(), scheduleItem.getContextId()));
                        setupNotificationOnWear(scheduleItem.getId(), scheduleItem.getTitle(), scheduleItem.getDescription(), scheduleItem.getStartDateString(getContext()), color);
                    }
                }
            });
        } else {
            //use the upcoming api endpoint
            CalendarEventAPI.getUpcomingEvents(new CanvasCallback<ScheduleItem[]>(this) {
                @Override
                public void cache(ScheduleItem[] scheduleItems, LinkHeaders linkHeaders, Response response) {
                    // No super call disables cache
                }

                @Override
                public void firstPage(ScheduleItem[] scheduleItems, LinkHeaders linkHeaders, Response response) {
                    for (ScheduleItem scheduleItem : scheduleItems) {
                        int color = CanvasContextColor.getCachedColor(getContext(), CanvasContext.makeContextId(scheduleItem.getContextType(), scheduleItem.getContextId()));
                        setupNotificationOnWear(scheduleItem.getId(), scheduleItem.getTitle(), scheduleItem.getDescription(), scheduleItem.getStartDateString(getContext()), color);
                    }
                }
            });

        }
    }

    /**
     * Builds corresponding notification for the Wear device that is paired to this handset. This
     * is done by adding a Data Item to teh Data Store; the Wear device will be notified to build a
     * local notification.
     */
    private void setupNotificationOnWear(long id, String title, String description, String date, int color) {
        if (!mGoogleApiClient.isConnected()) {
            Log.e("ALARM_SERVICE", "setupNotificationOnWear(): Failed to send data item since there was no "
                    + "connectivity to Google API Client");
            return;
        }
        PutDataMapRequest putDataMapRequest = PutDataMapRequest
                .create(NOTIFICATION_PATH + Long.toString(id));
        putDataMapRequest.getDataMap().putLong("time", new Date().getTime());
        putDataMapRequest.getDataMap().putString(KEY_ID, Long.toString(id));
        putDataMapRequest.getDataMap().putString(KEY_TITLE, title);
        putDataMapRequest.getDataMap().putString(KEY_DESCRIPTION, description);
        putDataMapRequest.getDataMap().putString(KEY_DATE, date);
        putDataMapRequest.getDataMap().putInt(KEY_COLOR, color);



        PutDataRequest request = putDataMapRequest.asPutDataRequest();

        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult) {
                        LoggingUtility.LogConsole("setupNotificationOnWear(): Sending notification result success:"
                                        + dataItemResult.getStatus().isSuccess()
                        );
                    }
                });
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onCallbackStarted() {

    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {

    }

    @Override
    public void onNoNetwork() {

    }

    @Override
    public Context getContext() {
        return AlarmService.this;
    }
}

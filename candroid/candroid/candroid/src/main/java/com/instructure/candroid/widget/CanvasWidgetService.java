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

package com.instructure.candroid.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.instructure.candroid.R;

public abstract class CanvasWidgetService extends Service {

    public final static String WIDGET_ERROR = "Canvas-Widget";
    public final static int TODO_REFRESH_ID = 1;
    public final static int GRADES_REFRESH_ID = 3;
    public final static int NOTIFICATIONS_REFRESH_ID = 4;
    public static int cycleBit = 100;
    private int widgetId;

    public int onStartCommand(Intent intent, int flags, int startId) {
        //intent is null if process is restarted
        if (intent == null) {
            this.stopSelf();
            return AppWidgetManager.INVALID_APPWIDGET_ID;
        }

        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            this.stopSelf();
            return AppWidgetManager.INVALID_APPWIDGET_ID;
        }

        AppWidgetManager manager = AppWidgetManager.getInstance(this);

        RemoteViews remoteViews = buildUpdate(manager);
        pushWidgetUpdate(remoteViews, widgetId);

        return START_STICKY;
    }

    protected void pushWidgetUpdate(RemoteViews remoteViews, int widgetId) {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        manager.updateAppWidget(widgetId, remoteViews);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //Cancel Refresh intents
       PendingIntent pendingIntent = PendingIntent.getBroadcast(this, getRefreshIntentID(), getRefreshIntent(), PendingIntent.FLAG_UPDATE_CURRENT);

        if(pendingIntent != null){
            pendingIntent.cancel();
        }

        // Update the widget
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        RemoteViews remoteViews = buildUpdate(manager);

        // Push update to homescreen
        pushWidgetUpdate(remoteViews, widgetId);

    }

    public RemoteViews buildUpdate(AppWidgetManager appWidgetManager) {
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.widget_homescreen);
        final int textColor = BaseRemoteViewsService.getWidgetTextColor(widgetId, getApplicationContext());
        setWidgetDependentViews(remoteViews, widgetId, textColor);

        //Setup Refresh

        PendingIntent pendingRefreshIntent = PendingIntent.getBroadcast(this, getRefreshIntentID(), getRefreshIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.widget_refresh, pendingRefreshIntent);

        appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.contentList);
        return remoteViews;
    }

    protected abstract void setWidgetDependentViews(RemoteViews remoteViews, int widgetId, int textColor);
    protected abstract int getRefreshIntentID();
    protected abstract Intent getRefreshIntent();
}

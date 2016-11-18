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
import android.content.Intent;
import android.widget.RemoteViews;

import com.instructure.candroid.R;
import com.instructure.candroid.activity.InterwebsToApplication;
import com.instructure.candroid.activity.LoginActivity;

public class TodoWidgetService extends CanvasWidgetService {

    @Override
    public Intent getRefreshIntent() {
        Intent updateIntent = new Intent(this, TodoWidgetProvider.class);
        updateIntent.setAction(TodoWidgetProvider.REFRESH);

        return updateIntent;
    }

    @Override
    public void setWidgetDependentViews(RemoteViews remoteViews, int appWidgetId, int textColor) {
        remoteViews.setRemoteAdapter(R.id.contentList, TodoViewWidgetService.createIntent(this, appWidgetId));
        remoteViews.setTextViewText(R.id.widget_title, this.getString(R.string.todoWidgetTitle));

        //Sets Titlebar to launch app when clicked
        Intent titleBarIntent = new Intent(this, LoginActivity.class);
        remoteViews.setOnClickPendingIntent(R.id.widget_logo, PendingIntent.getActivity(this, cycleBit++, titleBarIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        remoteViews.setInt(R.id.widget_root, "setBackgroundResource", BaseRemoteViewsService.getWidgetBackgroundResourceId(getApplicationContext(), appWidgetId));
        remoteViews.setTextColor(R.id.widget_title, textColor);

        Intent listViewItemIntent = new Intent(this, InterwebsToApplication.class);
        remoteViews.setPendingIntentTemplate(R.id.contentList, PendingIntent.getActivity(this, cycleBit++, listViewItemIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    @Override
    public int getRefreshIntentID() {
        return TODO_REFRESH_ID;
    }
}

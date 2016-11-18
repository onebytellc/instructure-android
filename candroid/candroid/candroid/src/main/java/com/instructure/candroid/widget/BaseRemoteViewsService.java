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

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViewsService;

import com.instructure.candroid.R;
import com.instructure.candroid.activity.WidgetSetupActivity;
import com.instructure.candroid.util.ApplicationManager;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class BaseRemoteViewsService extends RemoteViewsService {

    public static int getWidgetTextColor(int widgetId, Context context) {
        final String widgetBackgroundPref = getWidgetBackgroundPref(context, widgetId);
        return (widgetBackgroundPref.equalsIgnoreCase(WidgetSetupActivity.WIDGET_BACKGROUND_COLOR_LIGHT)) ? context.getResources().getColor(R.color.canvasTextDark) : context.getResources().getColor(R.color.white);
    }

    public static int getWidgetBackgroundResourceId(Context context, int widgetId) {
        final String widgetBackgroundPref = getWidgetBackgroundPref(context, widgetId);
        return (widgetBackgroundPref.equalsIgnoreCase(WidgetSetupActivity.WIDGET_BACKGROUND_COLOR_LIGHT) ? R.drawable.widget_light_bg : R.drawable.widget_dark_bg);
    }

    public static boolean shouldHideDetails(Context context, int appWidgetId) {
        return ApplicationManager.getPrefs(context).load( WidgetSetupActivity.WIDGET_DETAILS_PREFIX + appWidgetId, false);
    }

    //Data passed via the intent will get reused resulting in all widgets of this type having the same text color
    //unless the data is passed as part of a filter. When a filter is applied the intent does not get reused so data can be passed, like the widget id.
    // http://stackoverflow.com/questions/11350287/ongetviewfactory-only-called-once-for-multiple-widgets
    public static int getAppWidgetId(Intent intent) {
        return Integer.valueOf(intent.getData().getSchemeSpecificPart());
    }

    public static String getWidgetBackgroundPref(Context context, int widgetId) {
        return ApplicationManager.getPrefs(context).load(WidgetSetupActivity.WIDGET_BACKGROUND_PREFIX + widgetId, WidgetSetupActivity.WIDGET_BACKGROUND_COLOR_LIGHT);
    }


    /**
     * Gets the stored preferences for all known widgets and returns a map of the widget id and the preferred background style for that widget.
     * @param context Android Context
     * @return Map of known widget id and styles
     */
    public static Map<Integer, String> getStoredBackgroundPreferences(Context context) {

        final ComponentName gradesWidget = new ComponentName(context, GradesWidgetProvider.class);
        int [] gradesIds = AppWidgetManager.getInstance(context).getAppWidgetIds(gradesWidget);

        final ComponentName todoWidget = new ComponentName(context, TodoWidgetProvider.class);
        int [] todoWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(todoWidget);

        final ComponentName notificationWidget = new ComponentName(context, NotificationWidgetProvider.class);
        int [] notificationWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(notificationWidget);

        final Map<Integer, String> widgetIdsMap = new HashMap<>();

        for(int id : gradesIds) {
            widgetIdsMap.put(id, BaseRemoteViewsService.getWidgetBackgroundPref(context, id));
        }

        for(int id : todoWidgetIds) {
            widgetIdsMap.put(id, BaseRemoteViewsService.getWidgetBackgroundPref(context, id));
        }

        for(int id : notificationWidgetIds) {
            widgetIdsMap.put(id, BaseRemoteViewsService.getWidgetBackgroundPref(context, id));
        }

        return widgetIdsMap;
    }

    /**
     * Restores a set of background preferences for a particular widget id. Map<WidgetID, WidgetSetupActivity.WidgetBackgroundType>
     * @param context Android Context
     * @param map returns a map of Widget Ids and background types
     */
    public static void restoreWidgetBackgroundPreference(Context context, Map<Integer, String> map) {

        SharedPreferences sp = context.getSharedPreferences(ApplicationManager.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            editor.putString(WidgetSetupActivity.WIDGET_BACKGROUND_PREFIX + pairs.getKey(), pairs.getValue().toString());
        }

        editor.apply();
    }

    public static Map<Integer, Boolean> getShouldHideDetailsPreferences(Context context) {
        final ComponentName gradesWidget = new ComponentName(context, GradesWidgetProvider.class);
        int [] gradesIds = AppWidgetManager.getInstance(context).getAppWidgetIds(gradesWidget);

        final ComponentName todoWidget = new ComponentName(context, TodoWidgetProvider.class);
        int [] todoWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(todoWidget);

        final ComponentName notificationWidget = new ComponentName(context, NotificationWidgetProvider.class);
        int [] notificationWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(notificationWidget);

        final Map<Integer, Boolean> widgetIdsMap = new HashMap<>();

        for(int id : gradesIds) {
            widgetIdsMap.put(id, BaseRemoteViewsService.shouldHideDetails(context, id));
        }

        for(int id : todoWidgetIds) {
            widgetIdsMap.put(id, BaseRemoteViewsService.shouldHideDetails(context, id));
        }

        for(int id : notificationWidgetIds) {
            widgetIdsMap.put(id, BaseRemoteViewsService.shouldHideDetails(context, id));
        }

        return widgetIdsMap;
    }

    public static void restoreWidgetShouldHideDetailsPreference(Context context, Map<Integer, Boolean> map) {
        SharedPreferences sp = context.getSharedPreferences(ApplicationManager.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        Iterator it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            editor.putBoolean(WidgetSetupActivity.WIDGET_DETAILS_PREFIX + pairs.getKey(), Boolean.parseBoolean(pairs.getValue().toString()));
        }

        editor.apply();
    }
}

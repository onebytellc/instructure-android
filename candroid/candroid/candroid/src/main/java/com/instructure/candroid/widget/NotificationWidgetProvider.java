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

import android.content.Context;
import android.content.Intent;

public class NotificationWidgetProvider extends CanvasWidgetProvider {
    public final static String REFRESH = "com.instructure.candroid.widget.notificationwidget.REFRESH";
    private final static String SIMPLE_NAME = "Nofification Widget";

    @Override
    public String getWidgetSimpleName() {
        return SIMPLE_NAME;
    }

    @Override
    public String getRefreshString() { return REFRESH; }

    @Override
    public Intent getWidgetServiceIntent(Context context) {
        return new Intent(context, NotificationWidgetService.class);
    }
}

/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvasapi2.managers;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.CalendarEventAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.ScheduleItem;
import com.instructure.canvasapi2.tests.CalendarEventManager_Test;

import java.util.ArrayList;
import java.util.List;


public class CalendarEventManager extends BaseManager {

    private static boolean mTesting = false;

    public static void getCalendarEvents(
            int type,
            RestParams params,
            String startDate,
            String endDate,
            ArrayList<String> canvasContexts,
            StatusCallback<List<ScheduleItem>> callback) {

        if(!isEventTypeValid(type)) {
            throw new IllegalStateException("CalendarEventAPI.Type not valid");
        }

        if (isTesting() || mTesting) {
            CalendarEventManager_Test.getCalendarEvents(callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            CalendarEventAPI.getCalendarEvents(type, startDate, endDate, canvasContexts, adapter, callback, params);
        }
    }

    public static void getAllCalendarEventsWithSubmissionsAirwolf(
            String airwolfDomain,
            String parentId,
            String studentId,
            String startDate,
            String endDate,
            ArrayList<String> canvasContexts,
            boolean forceNetwork,
            StatusCallback<List<ScheduleItem>> callback) {

        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            CalendarEventAPI.getAllCalendarEventsWithSubmissionAirwolf(
                    parentId, studentId, startDate, endDate, canvasContexts, adapter, callback, params);
        }
    }

    public static void getCalendarEventAirwolf(
            String airwolfDomain,
            String parentId,
            String studentId,
            String eventId,
            StatusCallback<ScheduleItem> callback) {

        RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(false)
                .withDomain(airwolfDomain)
                .withAPIVersion("")
                .build();

        CalendarEventAPI.getCalendarEventAirwolf(parentId, studentId, eventId, adapter, callback, params);
    }

    private static boolean isEventTypeValid(int type) {
        return (type == CalendarEventAPI.ASSIGNMENT_TYPE || type == CalendarEventAPI.EVENT_TYPE);
    }
}

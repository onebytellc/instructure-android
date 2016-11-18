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

package com.instructure.canvasapi2.apis;

import android.support.annotation.NonNull;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.ScheduleItem;
import com.instructure.canvasapi2.utils.APIHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;


public class CalendarEventAPI {

    public static final int EVENT_TYPE = 0;
    public static final int ASSIGNMENT_TYPE = 1;

    interface CalendarEventInterface {

        @GET("users/{userId}/calendar_events/")
        Call<List<ScheduleItem>> getCalendarEventsForUser(
                @Path("userId") long userId,
                @Query("type") String type,
                @Query("start_date") String startDate,
                @Query("end_date") String endDate,
                @Query(value = "context_codes[]", encoded = true) ArrayList<String> contextCodes);

        @GET("calendar_events/")
        Call<List<ScheduleItem>>  getCalendarEvents(
                @Query("all_events") boolean allEvents,
                @Query("type") String type,
                @Query("start_date") String startDate,
                @Query("end_date") String endDate,
                @Query(value = "context_codes[]", encoded = true) ArrayList<String> contextCodes);


        @GET
        Call <List<ScheduleItem>> next(@Url String url);

        //region Airwolf

        @GET("canvas/{parentId}/{studentId}/calendar_events/{eventId}")
        Call<ScheduleItem> getCalendarEventAirwolf(
                @Path("parentId") String parentId,
                @Path("studentId") String studentId,
                @Path("eventId") String eventId);

        @GET("canvas/{parent_id}/{student_id}/calendar_events?include[]=submission")
        Call<List<ScheduleItem>> getCalendarEventsWithSubmissionsAirwolf(
                @Path("parent_id") String parentId,
                @Path("student_id") String studentId,
                @Query("start_date") String startDate,
                @Query("end_date") String endDate,
                @Query(value = "context_codes[]", encoded = true) ArrayList<String> contextCodes);

        //endregion
    }

    public static String getEventTypeParam(int eventType) {
        if(eventType == 0) {
            return "event";
        } else {
            return "assignment";
        }
    }

    public static void getCalendarEvents(final int eventType,
            @NonNull final String startDate,
            @NonNull final String endDate,
            @NonNull final ArrayList<String> canvasContexts,
            @NonNull final RestBuilder adapter,
            @NonNull StatusCallback<List<ScheduleItem>> callback,
            @NonNull final RestParams params) {

        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(CalendarEventInterface.class, params)
                    .getCalendarEvents(false, getEventTypeParam(eventType), startDate, endDate, canvasContexts)).enqueue(callback);
        } else if (StatusCallback.moreCallsExist(callback.getLinkHeaders()) && callback.getLinkHeaders() != null) {
            callback.addCall(adapter.build(CalendarEventInterface.class, params)
                    .next(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }

    //region Airwolf

    public static void getCalendarEventAirwolf(
            @NonNull String parentId,
            @NonNull String studentId,
            @NonNull String eventId,
            @NonNull final RestBuilder adapter,
            @NonNull StatusCallback<ScheduleItem> callback,
            @NonNull final RestParams params) {
        callback.addCall(adapter.build(CalendarEventInterface.class, params)
                .getCalendarEventAirwolf(parentId, studentId, eventId)).enqueue(callback);
    }

    public static void getAllCalendarEventsWithSubmissionAirwolf(
            @NonNull String parentId,
            @NonNull String studentId,
            @NonNull String startDate,
            @NonNull String endDate,
            @NonNull ArrayList<String> contextCodes,
            @NonNull final RestBuilder adapter,
            @NonNull StatusCallback<List<ScheduleItem>> callback,
            @NonNull final RestParams params) {

        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(CalendarEventInterface.class, params)
                    .getCalendarEventsWithSubmissionsAirwolf(parentId, studentId, startDate, endDate, contextCodes)).enqueue(callback);
        } else if (StatusCallback.moreCallsExist(callback.getLinkHeaders()) && callback.getLinkHeaders() != null) {
            callback.addCall(adapter.build(CalendarEventInterface.class, params)
                    .next(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }

    //endregion
}


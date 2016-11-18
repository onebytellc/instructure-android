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
import com.instructure.canvasapi2.models.Submission;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;


public class SubmissionAPI {

    interface SubmissionInterface {

        @GET("courses/{courseId}/students/submissions")
        Call<List<Submission>> getStudentSubmissionsForCourse(@Path("courseId") long courseId, @Query("student_ids[]") long studentId);

        @GET
        Call<List<Submission>> getNextPageSubmissions(@Url String nextUrl);
    }

    public static void getStudentSubmissionsForCourse(long courseId, long studentId, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<Submission>> callback, @NonNull RestParams params) {
        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(SubmissionInterface.class, params).getStudentSubmissionsForCourse(courseId, studentId)).enqueue(callback);
        } else if (callback.getLinkHeaders() != null && StatusCallback.moreCallsExist(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(SubmissionInterface.class, params).getNextPageSubmissions(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }
}

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

package com.instructure.canvasapi2.tests;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.GradingPeriod;
import com.instructure.canvasapi2.models.GradingPeriodResponse;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class CourseManager_Test {

    public static void getFavoriteCourses(StatusCallback<List<Course>> callback) {
        //TODO:
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        List<Course> courses = new ArrayList<>();

        retrofit2.Response<List<Course>> response1 = retrofit2.Response.success(courses, response);
        callback.onResponse(response1, new LinkHeaders(), ApiType.CACHE);
    }

    public static void getCourses(StatusCallback<List<Course>> callback) {
        //TODO:
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        List<Course> courses = new ArrayList<>();

        retrofit2.Response<List<Course>> response1 = retrofit2.Response.success(courses, response);
        callback.onResponse(response1, new LinkHeaders(), ApiType.CACHE);
    }

    public static void getGradingPeriodsForCourse(StatusCallback<GradingPeriodResponse> callback, long courseId) {
        //Create mock HTTP response
        Response httpResponse = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("test")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "test".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        //Create mock API response
        GradingPeriodResponse gradingPeriodResponse = new GradingPeriodResponse();
        ArrayList<GradingPeriod> gradingPeriods = new ArrayList<>();
        GradingPeriod gp1 = new GradingPeriod();
        gp1.setId(1);

        GradingPeriod gp2 = new GradingPeriod();
        gp2.setId(2);

        GradingPeriod gp3 = new GradingPeriod();
        gp3.setId(3);

        gradingPeriods.add(gp1);
        gradingPeriods.add(gp2);
        gradingPeriods.add(gp3);
        gradingPeriodResponse.setGradingPeriodList(gradingPeriods);

        retrofit2.Response<GradingPeriodResponse> response = retrofit2.Response.success(gradingPeriodResponse, httpResponse);
        callback.onResponse(response, new LinkHeaders(), ApiType.CACHE);
        callback.onFinished(ApiType.API);
    }

    public static void getCourse(long courseId, StatusCallback<Course> callback) {
        //TODO:
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        Course course = new Course();
        course.setId(courseId);

        retrofit2.Response<Course> response1 = retrofit2.Response.success(course, response);
        callback.onResponse(response1, new LinkHeaders(), ApiType.CACHE);
    }

    public static void getCourseStudents(long courseId, StatusCallback<List<User>> callback) {
        //TODO:
        Response response = new Response.Builder()
                .request(new Request.Builder().url("https://test.com").build())
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        List<User> users = new ArrayList<>();

        retrofit2.Response<List<User>> response1 = retrofit2.Response.success(users, response);
        callback.onResponse(response1, new LinkHeaders(), ApiType.CACHE);
    }

}

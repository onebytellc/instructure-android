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
import com.instructure.canvasapi2.models.QuizQuestion;
import com.instructure.canvasapi2.models.QuizSubmission;
import com.instructure.canvasapi2.models.QuizSubmissionQuestion;
import com.instructure.canvasapi2.models.QuizSubmissionQuestionResponse;
import com.instructure.canvasapi2.models.QuizSubmissionResponse;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class QuizManager_Test {

    public static void getQuizQuestions(long courseId, long quizId, StatusCallback<List<QuizQuestion>> callback) {
        //TODO:
        Response response = new Response.Builder()
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        List<QuizQuestion> quizQuestions = new ArrayList<>();

        retrofit2.Response<List<QuizQuestion>> response1 = retrofit2.Response.success(quizQuestions, response);
        callback.onResponse(response1, new LinkHeaders(), ApiType.CACHE);
    }

    public static void startQuizPreview(long courseId, long quizId, StatusCallback<QuizSubmissionResponse> callback) {
        //TODO:
        Response response = new Response.Builder()
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        QuizSubmissionResponse quizSubmissionResponse = new QuizSubmissionResponse();
        QuizSubmission quizSubmission = new QuizSubmission();

        quizSubmission.setId(8);

        List<QuizSubmission> quizSubmissions = new ArrayList<>();

        quizSubmissions.add(quizSubmission);
        quizSubmissionResponse.setQuizSubmissions(quizSubmissions);

        retrofit2.Response<QuizSubmissionResponse> response1 = retrofit2.Response.success(quizSubmissionResponse, response);
        callback.onResponse(response1, new LinkHeaders(), ApiType.CACHE);
    }

    public static void getQuizSubmissionQuestions(long quizSubmissionId, StatusCallback<QuizSubmissionQuestionResponse> callback) {
        //TODO:
        Response response = new Response.Builder()
                .code(200)
                .message("todo")
                .protocol(Protocol.HTTP_1_0)
                .body(ResponseBody.create(MediaType.parse("application/json"), "todo".getBytes()))
                .addHeader("content-type", "application/json")
                .build();

        QuizSubmissionQuestionResponse quizSubmissionQuestionResponse = new QuizSubmissionQuestionResponse();
        List<QuizSubmissionQuestion> quizQuestions = new ArrayList<>();

        QuizSubmissionQuestion question = new QuizSubmissionQuestion();
        question.setQuestionText("TEST");
        question.setQuestionType("essay_question");

        quizQuestions.add(question);

        quizSubmissionQuestionResponse.setQuizSubmissionQuestions(quizQuestions);
        retrofit2.Response<QuizSubmissionQuestionResponse> response1 = retrofit2.Response.success(quizSubmissionQuestionResponse, response);
        callback.onResponse(response1, new LinkHeaders(), ApiType.CACHE);
    }
}

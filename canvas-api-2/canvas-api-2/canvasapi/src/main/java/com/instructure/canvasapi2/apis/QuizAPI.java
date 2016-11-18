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
import com.instructure.canvasapi2.models.QuizQuestion;
import com.instructure.canvasapi2.models.QuizSubmissionQuestionResponse;
import com.instructure.canvasapi2.models.QuizSubmissionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;


public class QuizAPI {

    interface QuizInterface {
        @GET("courses/{courseId}/quizzes/{quizId}/questions")
        Call<List<QuizQuestion>> getFirstPageQuizQuestions(@Path("courseId") long contextId, @Path("quizId") long quizId);

        @GET
        Call<List<QuizQuestion>> getNextPageQuizQuestions(@Url String nextUrl);

        @POST("courses/{courseId}/quizzes/{quizId}/submissions?preview=1")
        Call<QuizSubmissionResponse> startQuizPreview(@Path("courseId") long contextId, @Path("quizId") long quizId);

        @GET("quiz_submissions/{quizSubmissionId}/questions")
        Call<QuizSubmissionQuestionResponse> getFirstPageSubmissionQuestions(@Path("quizSubmissionId") long quizSubmissionId);

        @GET
        Call<QuizSubmissionQuestionResponse> getNextPageSubmissionQuestions(@Url String nextURL);
    }

    public static void getQuizQuestions(long contextId, long quizId, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<QuizQuestion>> callback, @NonNull RestParams params) {
        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(QuizInterface.class, params).getFirstPageQuizQuestions(contextId, quizId)).enqueue(callback);
        } else if (StatusCallback.moreCallsExist(callback.getLinkHeaders()) && callback.getLinkHeaders() != null) {
            callback.addCall(adapter.build(QuizInterface.class, params).getNextPageQuizQuestions(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }

    /**
     * Start the quiz in preview mode. For teachers only.
     *
     * @param contextId
     * @param quizId
     * @param adapter
     * @param callback
     * @param params
     */
    public static void startQuizPreview(long contextId, long quizId, @NonNull RestBuilder adapter, @NonNull StatusCallback<QuizSubmissionResponse> callback, @NonNull RestParams params) {
        callback.addCall(adapter.build(QuizInterface.class, params).startQuizPreview(contextId, quizId)).enqueue(callback);
    }

    public static void getQuizSubmissionQuestions(long quizSubmissionId, @NonNull RestBuilder adapter, @NonNull StatusCallback<QuizSubmissionQuestionResponse> callback, @NonNull RestParams params) {
        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(QuizInterface.class, params).getFirstPageSubmissionQuestions(quizSubmissionId)).enqueue(callback);
        } else if (StatusCallback.moreCallsExist(callback.getLinkHeaders()) && callback.getLinkHeaders() != null) {
            callback.addCall(adapter.build(QuizInterface.class, params).getNextPageSubmissionQuestions(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }
}

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
import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.AssignmentGroup;
import com.instructure.canvasapi2.models.GradeableStudent;
import com.instructure.canvasapi2.models.RubricCriterion;
import com.instructure.canvasapi2.models.Submission;

import java.util.List;

import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.PUT;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;


public class AssignmentAPI {

    interface AssignmentInterface {

        @GET("courses/{courseId}/assignment_groups?include[]=assignments&include[]=discussion_topic&include[]=submission&override_assignment_dates=true")
        Call<AssignmentGroup[]> getAssignmentGroupListWithAssignments(@Path("courseId") long courseId);

        @GET("courses/{courseId}/assignment_groups?include[]=assignments&include[]=discussion_topic&include[]=submission&override_assignment_dates=true")
        Call<AssignmentGroup[]> getAssignmentGroupListWithAssignmentsForGradingPeriod(@Path("courseId") long courseId, @Query("grading_period_id") long gradingPeriodId);

        @GET
        Call<AssignmentGroup[]> getNextPage(@Url String nextUrl);

        @PUT("courses/{courseId}/assignments/{assignmentId}")
        Call<Assignment> editAssignment(@Path("courseId") long courseId, @Path("assignmentId") long assignmentId,
                            @Query("assignment[name]") String assignmentName,
                            @Query("assignment[assignment_group_id]") Long assignmentGroupId,
                            @Query(value = "assignment[submission_types][]") String submissionTypes,
                            @Query("assignment[peer_reviews]") Integer hasPeerReviews,
                            @Query("assignment[group_category_id]") Long groupId,
                            @Query("assignment[points_possible]") Double pointsPossible,
                            @Query("assignment[grading_type]") String gradingType,
                            @Query("assignment[due_at]") String dueAt,
                            @Query("assignment[description]") String description,
                            @Query("assignment[notify_of_update]") Integer notifyOfUpdate,
                            @Query("assignment[unlock_at]")String unlockAt,
                            @Query("assignment[lock_at]") String lockAt,
                            @Query(value = "assignment[html_url]") String htmlUrl,
                            @Query(value = "assignment[url]") String url,
                            @Query("assignment[quiz_id]") Long quizId,
                            @Query(value = "assignment[muted]") boolean isMuted,
                            @Query(value = "assignment[published]") Integer isPublished,
                            @Body String body);

        @DELETE("courses/{courseId}/assignments/{assignmentId}")
        Call<Assignment> deleteAssignment(@Path("courseId") long courseId, @Path("assignmentId") long assignmentId);

        @GET("courses/{courseId}/assignments/{assignmentId}/gradeable_students")
        Call<List<GradeableStudent>> getFirstPageGradeableStudentsForAssignment(@Path("courseId") long courseId, @Path("assignmentId") long assignmentId);

        @GET
        Call<List<GradeableStudent>> getNextPageGradeableStudents(@Url String nextUrl);

        @GET("courses/{courseId}/assignments/{assignmentId}/submissions")
        Call<List<Submission>> getFirstPageSubmissionsForAssignment(@Path("courseId") long courseId, @Path("assignmentId") long assignmentId);

        @GET
        Call<List<Submission>> getNextPageSubmissions(@Url String nextUrl);

        //region Airwolf

        @GET("canvas/{parentId}/{studentId}/courses/{courseId}/assignments/{assignmentId}?include[]=submission")
        Call<Assignment> getAssignmentAirwolf(@Path("parentId") String parentId, @Path("studentId") String studentId, @Path("courseId") String courseId, @Path("assignmentId") String assignmentId);


        //endregion
    }

    public static void getAssignmentGroupsWithAssignments(long courseId, @NonNull RestBuilder adapter, @NonNull StatusCallback<AssignmentGroup[]> callback, @NonNull RestParams params) {
        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(AssignmentInterface.class, params).getAssignmentGroupListWithAssignments(courseId)).enqueue(callback);
        } else if (callback.getLinkHeaders() != null && StatusCallback.moreCallsExist(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(AssignmentInterface.class, params).getNextPage(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }

    public static void getAssignmentGroupsWithAssignmentsForGradingPeriod(long courseId, RestBuilder adapter, StatusCallback<AssignmentGroup[]> callback, RestParams params, long gradingPeriodId) {
        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(AssignmentInterface.class, params).getAssignmentGroupListWithAssignmentsForGradingPeriod(courseId, gradingPeriodId)).enqueue(callback);
        } else if (callback.getLinkHeaders() != null && StatusCallback.moreCallsExist(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(AssignmentInterface.class, params).getNextPage(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }

    public static void editAssignment(long courseId, long assignmentId, String name, String description, String submissionTypes,
                                      String dueAt, double pointsPossible, String gradingType, String htmlUrl, String url,
                                      Long quizId, List<RubricCriterion> rubric, String[] allowedExtensions, Long assignmentGroupId, Integer hasPeerReviews,
                                      String lockAt, String  unlockAt, Long groupCategoryId, Integer notifyOfUpdate, boolean isMuted, Integer isPublished, RestBuilder adapter, final StatusCallback<Assignment> callback, RestParams params){

        callback.addCall(adapter.build(AssignmentInterface.class, params).editAssignment(courseId, assignmentId, name, assignmentGroupId, submissionTypes, hasPeerReviews,
                groupCategoryId, pointsPossible, gradingType, dueAt, description, notifyOfUpdate, unlockAt, lockAt, htmlUrl, url, quizId, isMuted, isPublished, "")).enqueue(callback);

    }

    public static void deleteAssignment(long courseId, long assignmentId, RestBuilder adapter, final StatusCallback<Assignment> callback, RestParams params){
        callback.addCall(adapter.build(AssignmentInterface.class, params).deleteAssignment(courseId, assignmentId)).enqueue(callback);

    }

    public static void getFirstPageGradeableStudentsForAssignment(long courseId, long assignmentId, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<GradeableStudent>> callback) {
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(true)
                .build();
        callback.addCall(adapter.build(AssignmentInterface.class, params).getFirstPageGradeableStudentsForAssignment(courseId, assignmentId)).enqueue(callback);
    }

    public static void getNextPageGradeableStudents(@NonNull String nextUrl, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<GradeableStudent>> callback) {
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(true)
                .build();
        callback.addCall(adapter.build(AssignmentInterface.class, params).getNextPageGradeableStudents(nextUrl)).enqueue(callback);
    }

    public static void getFirstPageSubmissionsForAssignment(long courseId, long assignmentId, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<Submission>> callback) {
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(true)
                .build();
        callback.addCall(adapter.build(AssignmentInterface.class, params).getFirstPageSubmissionsForAssignment(courseId, assignmentId)).enqueue(callback);
    }

    public static void getNextPageSubmissions(@NonNull String nextUrl, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<Submission>> callback) {
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(true)
                .build();
        callback.addCall(adapter.build(AssignmentInterface.class, params).getNextPageSubmissions(nextUrl)).enqueue(callback);
    }

    public static void getAssignmentAirwolf(
            @NonNull String parentId,
            @NonNull String studentId,
            @NonNull String courseId,
            @NonNull String assignmentId,
            @NonNull RestBuilder adapter,
            @NonNull StatusCallback<Assignment> callback,
            @NonNull RestParams params) {

        callback.addCall(adapter.build(AssignmentInterface.class, params).getAssignmentAirwolf(parentId, studentId, courseId, assignmentId)).enqueue(callback);
    }
}
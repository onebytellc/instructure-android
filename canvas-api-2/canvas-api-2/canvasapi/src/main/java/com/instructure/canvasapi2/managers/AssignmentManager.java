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

import android.support.annotation.NonNull;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.AssignmentAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.AssignmentGroup;
import com.instructure.canvasapi2.models.GradeableStudent;
import com.instructure.canvasapi2.models.RubricCriterion;
import com.instructure.canvasapi2.models.Submission;
import com.instructure.canvasapi2.tests.AssignmentManager_Test;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.DepaginatedCallback;

import java.util.Date;
import java.util.List;


public class AssignmentManager extends BaseManager {

    private static boolean mTesting = false;

    public static void getAssignmentGroupsWithAssignments(long courseId, boolean forceNetwork, StatusCallback<AssignmentGroup[]> callback) {
        if (isTesting() || mTesting) {
            AssignmentManager_Test.getAssignmentGroupsWithAssignments(courseId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();
            AssignmentAPI.getAssignmentGroupsWithAssignments(courseId, adapter, callback, params);
        }
    }

    public static void getAssignmentGroupsWithAssignmentsForGradingPeriod(long courseId, boolean forceNetwork, StatusCallback<AssignmentGroup[]> callback, long gradingPeriodId) {
        if (isTesting() || mTesting) {
            AssignmentManager_Test.getAssignmentGroupsWithAssignmentsForGradingPeriod(courseId, callback, gradingPeriodId);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();
            AssignmentAPI.getAssignmentGroupsWithAssignmentsForGradingPeriod(courseId, adapter, callback, params, gradingPeriodId);
        }
    }

    public static void deleteAssignment(long courseId, Assignment assignment, final StatusCallback<Assignment> callback){

        if (isTesting() || mTesting) {
            AssignmentManager_Test.deleteAssignment(assignment, callback);
        } else {

            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .build();

            AssignmentAPI.deleteAssignment(courseId, assignment.getId(), adapter, callback, params);
        }
    }

    public static void editAssignment(Assignment editedAssignment, Boolean notifyOfUpdate, final StatusCallback<Assignment> callback){

        if (isTesting() || mTesting) {
            AssignmentManager_Test.editAssignment(editedAssignment, callback);
        } else {
            Assignment.SUBMISSION_TYPE[] arrayOfSubmissionTypes = editedAssignment.getSubmissionTypes().toArray(new Assignment.SUBMISSION_TYPE[editedAssignment.getSubmissionTypes().size()]);
            String[] arrayOfAllowedExtensions = editedAssignment.getAllowedExtensions().toArray(new String[editedAssignment.getAllowedExtensions().size()]);
            editAssignment(editedAssignment.getCourseId(), editedAssignment.getId(), editedAssignment.getName(), editedAssignment.getDescription(), arrayOfSubmissionTypes,
                    editedAssignment.getDueAt(), editedAssignment.getPointsPossible(), editedAssignment.getGradingType(), editedAssignment.getHtmlUrl(), editedAssignment.getUrl(),
                    editedAssignment.getQuizId(), editedAssignment.getRubric(), arrayOfAllowedExtensions, editedAssignment.getAssignmentGroupId(), editedAssignment.isPeerReviews(),
                    editedAssignment.getLockAt(), editedAssignment.getUnlockAt(), null, notifyOfUpdate, editedAssignment.isMuted(), editedAssignment.isPublished(), callback);
        }
    }

    private static void editAssignment(long courseId, long assignmentId, String name, String description, Assignment.SUBMISSION_TYPE[] submissionTypes,
                                       Date dueAt, double pointsPossible, String gradingType, String htmlUrl, String url,
                                       Long quizId, List<RubricCriterion> rubric, String[] allowedExtensions, Long assignmentGroupId, Boolean hasPeerReviews,
                                       Date lockAt, Date unlockAt, Long groupCategoryId, boolean notifyOfUpdate, boolean isMuted, boolean isPublished, final StatusCallback<Assignment> callback){

        String stringDueAt = APIHelper.dateToString(dueAt);
        String stringUnlockAt = APIHelper.dateToString(unlockAt);
        String stringLockAt = APIHelper.dateToString(lockAt);
        String newSubmissionTypes = submissionTypeArrayToAPIQueryString(submissionTypes);

        Integer newHasPeerReviews = (hasPeerReviews == null) ? null : APIHelper.booleanToInt(hasPeerReviews);
        Integer newNotifyOfUpdate = APIHelper.booleanToInt(notifyOfUpdate);
        Integer newIsPublished = APIHelper.booleanToInt(isPublished);

        RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
        RestParams params = new RestParams.Builder()
                .withPerPageQueryParam(true)
                .withShouldIgnoreToken(false)
                .build();

        AssignmentAPI.editAssignment(courseId, assignmentId, name, description, newSubmissionTypes, stringDueAt, pointsPossible, gradingType, htmlUrl, url,
                quizId, rubric, allowedExtensions, assignmentGroupId, newHasPeerReviews, stringLockAt, stringUnlockAt, groupCategoryId, newNotifyOfUpdate, isMuted, newIsPublished, adapter, callback, params);
    }

    /*
   *Converts a SUBMISSION_TYPE[] to a queryString for the API
    */
    private static String submissionTypeArrayToAPIQueryString(Assignment.SUBMISSION_TYPE[] submissionTypes){
        if(submissionTypes == null || submissionTypes.length == 0){
            return null;
        }
        String submissionTypesQueryString =  "";

        for(int i =0; i < submissionTypes.length; i++){
            submissionTypesQueryString +=  Assignment.submissionTypeToAPIString(submissionTypes[i]);

            if(i < submissionTypes.length -1){
                submissionTypesQueryString += "&assignment[submission_types][]=";
            }
        }

        return submissionTypesQueryString;
    }

    public static void getAllGradeableStudentsForAssignment(long courseId, long assignmentId, StatusCallback<List<GradeableStudent>> callback) {
        if (isTesting() || mTesting) {
            AssignmentManager_Test.getAllGradeableStudentsForAssignment(courseId, assignmentId, callback);
        } else {
            final RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            StatusCallback<List<GradeableStudent>> depaginatedCallback = new DepaginatedCallback<>(callback, new DepaginatedCallback.PageRequestCallback<GradeableStudent>() {
                @Override
                public void getNextPage(DepaginatedCallback<GradeableStudent> callback, String nextUrl, boolean isCached) {
                    AssignmentAPI.getNextPageGradeableStudents(nextUrl, adapter, callback);
                }
            });
            adapter.setStatusCallback(depaginatedCallback);
            AssignmentAPI.getFirstPageGradeableStudentsForAssignment(courseId, assignmentId, adapter, depaginatedCallback);
        }
    }

    public static void getAllSubmissionsForAssignment(long courseId, long assignmentId, StatusCallback<List<Submission>> callback) {
        if (isTesting() || mTesting) {
            AssignmentManager_Test.getAllSubmissionsForAssignment(courseId, assignmentId, callback);
        } else {
            final RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            StatusCallback<List<Submission>> depaginatedCallback = new DepaginatedCallback<>(callback, new DepaginatedCallback.PageRequestCallback<Submission>() {
                @Override
                public void getNextPage(DepaginatedCallback<Submission> callback, String nextUrl, boolean isCached) {
                    AssignmentAPI.getNextPageSubmissions(nextUrl, adapter, callback);
                }
            });
            adapter.setStatusCallback(depaginatedCallback);
            AssignmentAPI.getFirstPageSubmissionsForAssignment(courseId, assignmentId, adapter, depaginatedCallback);
        }
    }

    public static void getAssignmentAirwolf(
            @NonNull String airwolfDomain,
            @NonNull String parentId,
            @NonNull String studentId,
            @NonNull String courseId,
            @NonNull String assignmentId,
            @NonNull StatusCallback<Assignment> callback) {

        RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
        RestParams params = new RestParams.Builder()
                .withShouldIgnoreToken(false)
                .withPerPageQueryParam(false)
                .withDomain(airwolfDomain)
                .withAPIVersion("")
                .build();

        AssignmentAPI.getAssignmentAirwolf(parentId, studentId, courseId, assignmentId, adapter, callback, params);
    }
}
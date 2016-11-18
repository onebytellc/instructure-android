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

package com.instructure.pandautils.utils;

import android.support.annotation.NonNull;

import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.Submission;

import java.util.Calendar;

@Deprecated
public class AssignmentUtils {

    public static final int ASSIGNMENT_STATE_UNKNOWN = -1;
    public static final int ASSIGNMENT_STATE_SUBMITTED = 1000;
    public static final int ASSIGNMENT_STATE_SUBMITTED_LATE = 1001;
    public static final int ASSIGNMENT_STATE_DUE = 1002;
    public static final int ASSIGNMENT_STATE_MISSING = 1003;
    public static final int ASSIGNMENT_STATE_GRADED = 1004;
    public static final int ASSIGNMENT_STATE_GRADED_LATE = 1005; //graded late -> submitted late but has been graded
    public static final int ASSIGNMENT_STATE_EXCUSED = 1006;
    public static final int ASSIGNMENT_STATE_IN_CLASS = 1007;
    public static final int ASSIGNMENT_STATE_DROPPED = 1008; //not yet used....

    public static int getAssignmentState(Assignment assignment, Submission submission) {
        //Case - Error
        if(assignment == null){
            return ASSIGNMENT_STATE_UNKNOWN;
        }

        //Case - We have an assignment with no submission
        //Result - MISSING or DUE
        if(submission == null){
            if(assignment.getDueDate() != null && assignment.getDueDate().getTime() >= Calendar.getInstance().getTimeInMillis()) {
                return checkInClassOrDue(assignment);
            } else {
                return checkInClassOrMissing(assignment);
            }
        } else {
            //Edge Case - Excused Assignment
            //Result - EXCUSED STATE
            if(assignment.getLastSubmission().isExcused()){
                return ASSIGNMENT_STATE_EXCUSED;
            }

            //Edge Case - Assignment with "fake submission" and no grade
            //Result - MISSING or DUE
            if(submission.getAttempt() == 0 && submission.getGrade() == null) {
                if(assignment.getDueDate() != null && assignment.getDueDate().getTime() >= Calendar.getInstance().getTimeInMillis()) {
                    return checkInClassOrDue(assignment);
                } else {
                    return checkInClassOrMissing(assignment);
                }
            }

            return checkOnTimeOrLate(assignment, hasNoGrade(assignment, submission));
        }

    }

    //Check to see if an assignment either
    //1. Has not been graded
    //2. Is "Pending Review"
    //3. Is muted
    private static boolean hasNoGrade(Assignment assignment, @NonNull Submission submission) {
        if(!submission.isGraded() || Const.PENDING_REVIEW.equals(submission.getWorkflowState()) || assignment.isMuted()) {
            return true;
        } else {
            return false;
        }
    }

    //Edge Case - Assignment is either due in the future or an unknown "paper" hand in
    //Result - IN_CLASS or DUE
    private static int checkInClassOrDue(Assignment assignment) {
        if(assignment.getSubmissionTypes().contains(Assignment.SUBMISSION_TYPE.ON_PAPER)) {
            return ASSIGNMENT_STATE_IN_CLASS;
        } else {
            return ASSIGNMENT_STATE_DUE;
        }
    }

    //Edge Case - Assignment is either past due or an unknown "paper" hand in
    //Result - IN_CLASS or MISSING
    private static int checkInClassOrMissing(Assignment assignment) {
        //Edge Case - Check for paper submission
        if(assignment.getSubmissionTypes().contains(Assignment.SUBMISSION_TYPE.ON_PAPER)) {
            return ASSIGNMENT_STATE_IN_CLASS;
        } else {
            return ASSIGNMENT_STATE_MISSING;
        }
    }

    private static int checkOnTimeOrLate(Assignment assignment, boolean hasNoGrade) {
        if(hasNoGrade) {
            //Case - Assignment with a submission but "no grade"
            //Result - SUBMITTED or SUBMITTED_LATE
            if(assignment.getLastSubmission().isLate()){
                return ASSIGNMENT_STATE_SUBMITTED_LATE;
            } else {
                return ASSIGNMENT_STATE_SUBMITTED;
            }
        } else {
            //Case - Assignment with a submission
            //Result - GRADED or GRADED_LATE
            if(assignment.getLastSubmission().isLate()){
                return ASSIGNMENT_STATE_GRADED_LATE;
            } else {
                return ASSIGNMENT_STATE_GRADED;
            }
        }
    }


}

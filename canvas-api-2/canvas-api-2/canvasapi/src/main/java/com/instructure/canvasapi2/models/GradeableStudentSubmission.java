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

package com.instructure.canvasapi2.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.Date;


public class GradeableStudentSubmission extends CanvasModel<GradeableStudentSubmission> implements Parcelable {

    private GradeableStudent student;
    private Submission submission;

    public GradeableStudentSubmission() {
    }

    @Override
    public long getId() {
        return student.getId();
    }

    public GradeableStudent getStudent() {
        return student;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setStudent(GradeableStudent student) {
        this.student = student;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        if (submission != null) {
            return submission.getComparisonDate();
        } else {
            return student.getComparisonDate();
        }
    }

    @Nullable
    @Override
    public String getComparisonString() {
        if (submission != null) {
            return submission.getComparisonString();
        } else {
            return student.getComparisonString();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(student, flags);
        dest.writeParcelable(submission, flags);
    }

    protected GradeableStudentSubmission(Parcel in) {
        student = in.readParcelable(User.class.getClassLoader());
        submission = in.readParcelable(Submission.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GradeableStudentSubmission> CREATOR = new Creator<GradeableStudentSubmission>() {
        @Override
        public GradeableStudentSubmission createFromParcel(Parcel in) {
            return new GradeableStudentSubmission(in);
        }

        @Override
        public GradeableStudentSubmission[] newArray(int size) {
            return new GradeableStudentSubmission[size];
        }
    };
}

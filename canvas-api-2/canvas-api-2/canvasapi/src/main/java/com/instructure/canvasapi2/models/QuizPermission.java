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
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;


public class QuizPermission extends CanvasModel<QuizPermission> {

    //whether the user can view the quiz
    private boolean read;

    //whether the user may submit a submission for the quiz
    private boolean submit;

    //whether the user may create a new quiz
    private boolean create;

    //whether the user may edit, update, or delete the quiz
    private boolean manage;

    //whether the user may view quiz statistics for this quiz
    @SerializedName("read_statistics")
    private boolean readStatistics;

    //whether the user may review grades for all quiz submissions for this quiz
    @SerializedName("review_grades")
    private boolean reviewGrades;

    //whether the user may update the quiz
    private boolean update;

    //whether the user may delete the quiz
    private boolean delete;

    //whether the user may grade the quiz
    private boolean grade;

    //whether the user can view answer audits
    @SerializedName("view_answer_audits")
    private boolean viewAnswerAudits;

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public boolean isSubmit() {
        return submit;
    }

    public void setSubmit(boolean submit) {
        this.submit = submit;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    public boolean isManage() {
        return manage;
    }

    public void setManage(boolean manage) {
        this.manage = manage;
    }

    public boolean isReadStatistics() {
        return readStatistics;
    }

    public void setReadStatistics(boolean readStatistics) {
        this.readStatistics = readStatistics;
    }

    public boolean isReviewGrades() {
        return reviewGrades;
    }

    public void setReviewGrades(boolean reviewGrades) {
        this.reviewGrades = reviewGrades;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public boolean isGrade() {
        return grade;
    }

    public void setGrade(boolean grade) {
        this.grade = grade;
    }

    public boolean isViewAnswerAudits() {
        return viewAnswerAudits;
    }

    public void setViewAnswerAudits(boolean viewAnswerAudits) {
        this.viewAnswerAudits = viewAnswerAudits;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Nullable
    @Override
    public String getComparisonString() {
        return null;
    }

    @Override
    public int compareTo(QuizPermission comparable) {
        return super.compareTo(comparable);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.read ? (byte) 1 : (byte) 0);
        dest.writeByte(this.submit ? (byte) 1 : (byte) 0);
        dest.writeByte(this.create ? (byte) 1 : (byte) 0);
        dest.writeByte(this.manage ? (byte) 1 : (byte) 0);
        dest.writeByte(this.readStatistics ? (byte) 1 : (byte) 0);
        dest.writeByte(this.reviewGrades ? (byte) 1 : (byte) 0);
        dest.writeByte(this.update ? (byte) 1 : (byte) 0);
        dest.writeByte(this.delete ? (byte) 1 : (byte) 0);
        dest.writeByte(this.grade ? (byte) 1 : (byte) 0);
        dest.writeByte(this.viewAnswerAudits ? (byte) 1 : (byte) 0);
    }

    public QuizPermission() {
    }

    protected QuizPermission(Parcel in) {
        this.read = in.readByte() != 0;
        this.submit = in.readByte() != 0;
        this.create = in.readByte() != 0;
        this.manage = in.readByte() != 0;
        this.readStatistics = in.readByte() != 0;
        this.reviewGrades = in.readByte() != 0;
        this.update = in.readByte() != 0;
        this.delete = in.readByte() != 0;
        this.grade = in.readByte() != 0;
        this.viewAnswerAudits = in.readByte() != 0;
    }

    public static final Creator<QuizPermission> CREATOR = new Creator<QuizPermission>() {
        @Override
        public QuizPermission createFromParcel(Parcel source) {
            return new QuizPermission(source);
        }

        @Override
        public QuizPermission[] newArray(int size) {
            return new QuizPermission[size];
        }
    };
}

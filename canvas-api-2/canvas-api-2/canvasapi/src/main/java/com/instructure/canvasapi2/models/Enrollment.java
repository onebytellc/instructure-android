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

import com.google.gson.annotations.SerializedName;

import java.util.Date;


public class Enrollment extends CanvasModel<Enrollment> implements Parcelable {

    private String role;
    private String type;

    // only included when we get enrollments using the user's url:
    // /users/self/enrollments
    private long id;
    @SerializedName("course_id")
    private long courseId;
    @SerializedName("course_section_id")
    private long courseSectionId;
    @SerializedName("enrollment_state")
    private String enrollmentState;
    @SerializedName("user_id")
    private long userId;
    private Grades grades;

    // only included when we get the enrollment with a course object
    @SerializedName("computed_current_score")
    private double computedCurrentScore;
    @SerializedName("computed_final_score")
    private double computedFinalScore;
    @SerializedName("computed_current_grade")
    private String computedCurrentGrade;
    @SerializedName("computed_final_grade")
    private String computedFinalGrade;
    @SerializedName("multiple_grading_periods_enabled")
    private boolean multipleGradingPeriodsEnabled;
    @SerializedName("totals_for_all_grading_periods_option")
    private boolean totalsForAllGradingPeriodsOption;
    @SerializedName("current_period_computed_current_score")
    private double currentPeriodComputedCurrentScore;
    @SerializedName("current_period_computed_final_score")
    private double currentPeriodComputedFinalScore;
    @SerializedName("current_period_computed_current_grade")
    private String currentPeriodComputedCurrentGrade;
    @SerializedName("current_period_computed_final_grade")
    private String currentPeriodComputedFinalGrade;
    @SerializedName("current_grading_period_id")
    private long currentGradingPeriodId;
    @SerializedName("current_grading_period_title")
    private String currentGradingPeriodTitle;
    //The unique id of the associated user. Will be null unless type is ObserverEnrollment.
    @SerializedName("associated_user_id")
    private long associatedUserId;

    public Enrollment(){
        type = "";
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return getType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Enrollment that = (Enrollment) o;

        if (!type.equals(that.type)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + type.hashCode();
        return result;
    }

    public boolean isStudent() {
        return ("student".equalsIgnoreCase(type) || "studentenrollment".equalsIgnoreCase(type));
    }

    public boolean isTeacher() {
        return ("teacher".equalsIgnoreCase(type) || "teacherenrollment".equalsIgnoreCase(type));
    }

    public boolean isObserver() {
        return ("observer".equalsIgnoreCase(type) || "observerenrollment".equalsIgnoreCase(type));
    }

    public boolean isTA() {
        return ("ta".equalsIgnoreCase(type) || "taenrollment".equalsIgnoreCase(type));
    }

    //region Getters

    public String getRole() {
        return role;
    }

    public String getType() {
        String enrollment = "enrollment";
        if(type.toLowerCase().endsWith(enrollment)){
            type = type.substring(0, type.length() - enrollment.length());
        }
        return type;
    }

    public long getCourseId() {
        return courseId;
    }

    public long getCourseSectionId() {
        return courseSectionId;
    }

    public String getEnrollmentState() {
        return enrollmentState;
    }

    public long getUserId() {
        return userId;
    }

    public Grades getGrades() {
        return grades;
    }

    public double getComputedCurrentScore() {
        return computedCurrentScore;
    }

    public double getComputedFinalScore() {
        return computedFinalScore;
    }

    public String getComputedCurrentGrade() {
        return computedCurrentGrade;
    }

    public String getComputedFinalGrade() {
        return computedFinalGrade;
    }

    public boolean isMultipleGradingPeriodsEnabled() {
        return multipleGradingPeriodsEnabled;
    }

    public boolean isTotalsForAllGradingPeriodsOption() {
        return totalsForAllGradingPeriodsOption;
    }

    public double getCurrentPeriodComputedCurrentScore() {
        return currentPeriodComputedCurrentScore;
    }

    public double getCurrentPeriodComputedFinalScore() {
        return currentPeriodComputedFinalScore;
    }

    public String getCurrentPeriodComputedCurrentGrade() {
        return currentPeriodComputedCurrentGrade;
    }

    public String getCurrentPeriodComputedFinalGrade() {
        return currentPeriodComputedFinalGrade;
    }

    public long getCurrentGradingPeriodId() {
        return currentGradingPeriodId;
    }

    public String getCurrentGradingPeriodTitle() {
        return currentGradingPeriodTitle;
    }

    public long getAssociatedUserId() {
        return associatedUserId;
    }

    public double getCurrentScore() {
        if (grades != null) {
            return grades.getCurrentScore();
        }
        return computedCurrentScore;
    }
    public double getFinalScore() {
        if (grades != null) {
            return grades.getFinalScore();
        }
        return computedFinalScore;
    }
    public String getCurrentGrade() {
        if (grades != null) {
            return grades.getCurrentGrade();
        }
        return computedCurrentGrade;
    }
    public String getFinalGrade() {
        if (grades != null) {
            return grades.getFinalGrade();
        }
        return computedFinalGrade;
    }

    //endregion

    //region Setters

    public void setRole(String role) {
        this.role = role;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public void setCourseSectionId(long courseSectionId) {
        this.courseSectionId = courseSectionId;
    }

    public void setEnrollmentState(String enrollmentState) {
        this.enrollmentState = enrollmentState;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setGrades(Grades grades) {
        this.grades = grades;
    }

    public void setComputedCurrentScore(double computedCurrentScore) {
        this.computedCurrentScore = computedCurrentScore;
    }

    public void setComputedFinalScore(double computedFinalScore) {
        this.computedFinalScore = computedFinalScore;
    }

    public void setComputedCurrentGrade(String computedCurrentGrade) {
        this.computedCurrentGrade = computedCurrentGrade;
    }

    public void setComputedFinalGrade(String computedFinalGrade) {
        this.computedFinalGrade = computedFinalGrade;
    }

    public void setMultipleGradingPeriodsEnabled(boolean multipleGradingPeriodsEnabled) {
        this.multipleGradingPeriodsEnabled = multipleGradingPeriodsEnabled;
    }

    public void setTotalsForAllGradingPeriodsOption(boolean totalsForAllGradingPeriodsOption) {
        this.totalsForAllGradingPeriodsOption = totalsForAllGradingPeriodsOption;
    }

    public void setCurrentPeriodComputedCurrentScore(double currentPeriodComputedCurrentScore) {
        this.currentPeriodComputedCurrentScore = currentPeriodComputedCurrentScore;
    }

    public void setCurrentPeriodComputedFinalScore(double currentPeriodComputedFinalScore) {
        this.currentPeriodComputedFinalScore = currentPeriodComputedFinalScore;
    }

    public void setCurrentPeriodComputedCurrentGrade(String currentPeriodComputedCurrentGrade) {
        this.currentPeriodComputedCurrentGrade = currentPeriodComputedCurrentGrade;
    }

    public void setCurrentPeriodComputedFinalGrade(String currentPeriodComputedFinalGrade) {
        this.currentPeriodComputedFinalGrade = currentPeriodComputedFinalGrade;
    }

    public void setCurrentGradingPeriodId(long currentGradingPeriodId) {
        this.currentGradingPeriodId = currentGradingPeriodId;
    }

    public void setCurrentGradingPeriodTitle(String currentGradingPeriodTitle) {
        this.currentGradingPeriodTitle = currentGradingPeriodTitle;
    }

    public void setAssociatedUserId(long associatedUserId) {
        this.associatedUserId = associatedUserId;
    }

    //endregion

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.role);
        dest.writeString(this.type);
        dest.writeLong(this.id);
        dest.writeLong(this.courseId);
        dest.writeLong(this.courseSectionId);
        dest.writeString(this.enrollmentState);
        dest.writeLong(this.userId);
        dest.writeParcelable(this.grades, flags);
        dest.writeDouble(this.computedCurrentScore);
        dest.writeDouble(this.computedFinalScore);
        dest.writeString(this.computedCurrentGrade);
        dest.writeString(this.computedFinalGrade);
        dest.writeByte(multipleGradingPeriodsEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(totalsForAllGradingPeriodsOption ? (byte) 1 : (byte) 0);
        dest.writeDouble(this.currentPeriodComputedCurrentScore);
        dest.writeDouble(this.currentPeriodComputedFinalScore);
        dest.writeString(this.currentPeriodComputedCurrentGrade);
        dest.writeString(this.currentPeriodComputedFinalGrade);
        dest.writeLong(this.currentGradingPeriodId);
        dest.writeString(this.currentGradingPeriodTitle);
        dest.writeLong(this.associatedUserId);
    }

    protected Enrollment(Parcel in) {
        this.role = in.readString();
        this.type = in.readString();
        this.id = in.readLong();
        this.courseId = in.readLong();
        this.courseSectionId = in.readLong();
        this.enrollmentState = in.readString();
        this.userId = in.readLong();
        this.grades = in.readParcelable(Grades.class.getClassLoader());
        this.computedCurrentScore = in.readDouble();
        this.computedFinalScore = in.readDouble();
        this.computedCurrentGrade = in.readString();
        this.computedFinalGrade = in.readString();
        this.multipleGradingPeriodsEnabled = in.readByte() != 0;
        this.totalsForAllGradingPeriodsOption = in.readByte() != 0;
        this.currentPeriodComputedCurrentScore = in.readDouble();
        this.currentPeriodComputedFinalScore = in.readDouble();
        this.currentPeriodComputedCurrentGrade = in.readString();
        this.currentPeriodComputedFinalGrade = in.readString();
        this.currentGradingPeriodId = in.readLong();
        this.currentGradingPeriodTitle = in.readString();
        this.associatedUserId = in.readLong();
    }

    public static final Creator<Enrollment> CREATOR = new Creator<Enrollment>() {
        @Override
        public Enrollment createFromParcel(Parcel source) {
            return new Enrollment(source);
        }

        @Override
        public Enrollment[] newArray(int size) {
            return new Enrollment[size];
        }
    };

    //endregion
}
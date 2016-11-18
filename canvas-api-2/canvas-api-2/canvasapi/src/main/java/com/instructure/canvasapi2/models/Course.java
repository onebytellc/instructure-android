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

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;


public class Course extends CanvasContext implements Comparable<CanvasContext>{

    private long id;
    private String name;
    @SerializedName("original_name")
    private String originalName;
    @SerializedName("course_code")
    private String courseCode;
    @SerializedName("start_at")
    private String startAt;
    @SerializedName("end_at")
    private String endAt;
    @SerializedName("syllabus_body")
    private String syllabusBody;
    @SerializedName("hide_final_grades")
    private boolean hideFinalGrades;
    @SerializedName("is_public")
    private boolean isPublic;
    private String license;
    private Term term;
    private List<Enrollment> enrollments = new ArrayList<>();
    @SerializedName("needs_grading_count")
    private long needsGradingCount;
    @SerializedName("apply_assignment_group_weights")
    private boolean applyAssignmentGroupWeights;
    private Double currentScore;
    private Double finalScore;
    private boolean checkedCurrentGrade;
    private boolean checkedFinalGrade;
    private String currentGrade;
    private String finalGrade;
    @SerializedName("is_favorite")
    private boolean isFavorite;
    @SerializedName("access_restricted_by_date")
    private boolean accessRestrictedByDate;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Type getType(){return Type.COURSE;}

    @Override
    public String getName() {
        return name;
    }

    public String getOriginalName() {
        return this.originalName;
    }

    //region Getters

    public String getCourseCode() {
        return courseCode;
    }

    public String getStartAt() {
        return startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public String getSyllabusBody() {
        return syllabusBody;
    }

    public boolean isHideFinalGrades() {
        return hideFinalGrades;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public Term getTerm() {
        return term;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public long getNeedsGradingCount() {
        return needsGradingCount;
    }

    public boolean isApplyAssignmentGroupWeights() {
        return applyAssignmentGroupWeights;
    }

    public boolean isCheckedCurrentGrade() {
        return checkedCurrentGrade;
    }

    public boolean isCheckedFinalGrade() {
        return checkedFinalGrade;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public boolean isAccessRestrictedByDate() {
        return accessRestrictedByDate;
    }

    //endregion

    //region Setters

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public void setSyllabusBody(String syllabusBody) {
        this.syllabusBody = syllabusBody;
    }

    public void setHideFinalGrades(boolean hideFinalGrades) {
        this.hideFinalGrades = hideFinalGrades;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    public void setNeedsGradingCount(long needsGradingCount) {
        this.needsGradingCount = needsGradingCount;
    }

    public void setApplyAssignmentGroupWeights(boolean applyAssignmentGroupWeights) {
        this.applyAssignmentGroupWeights = applyAssignmentGroupWeights;
    }

    public void setCurrentScore(Double currentScore) {
        this.currentScore = currentScore;
    }

    public void setFinalScore(Double finalScore) {
        this.finalScore = finalScore;
    }

    public void setCheckedCurrentGrade(boolean checkedCurrentGrade) {
        this.checkedCurrentGrade = checkedCurrentGrade;
    }

    public void setCheckedFinalGrade(boolean checkedFinalGrade) {
        this.checkedFinalGrade = checkedFinalGrade;
    }

    public void setCurrentGrade(String currentGrade) {
        this.currentGrade = currentGrade;
    }

    public void setFinalGrade(String finalGrade) {
        this.finalGrade = finalGrade;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public void setAccessRestrictedByDate(boolean accessRestrictedByDate) {
        this.accessRestrictedByDate = accessRestrictedByDate;
    }

    //endregion

    public Course() {}

    public boolean isStudent() {
        for(Enrollment enrollment : enrollments) {
            if(enrollment.isStudent()) {
                return true;
            }
        }
        return false;
    }

    public boolean isTeacher() {
        if (enrollments == null){
            return false;
        }
        for(Enrollment enrollment : enrollments) {
            if(enrollment.isTeacher()) {
                return true;
            }
        }
        return false;
    }

    public boolean isTA() {
        if (enrollments == null){
            return false;
        }
        for(Enrollment enrollment : enrollments) {
            if(enrollment.isTA()) {
                return true;
            }
        }
        return false;
    }

    public boolean isObserver() {
        for(Enrollment enrollment : enrollments) {
            if(enrollment.isObserver()) {
                return true;
            }
        }
        return false;
    }

    public List<Enrollment> getEnrollmentsNoDuplicates() {
        if(enrollments == null) {
            return null;
        }
        if(enrollments.size() <= 1) {
            return enrollments;
        }
        return new ArrayList<>(new LinkedHashSet<>(enrollments));
    }

    public double getCurrentScore() {
        if (currentScore == null) {
            for (Enrollment enrollment : enrollments) {
                if (enrollment.isStudent() || enrollment.isObserver()) {
                    if(enrollment.isMultipleGradingPeriodsEnabled()) {
                        currentScore = enrollment.getCurrentPeriodComputedCurrentScore();
                    } else {
                        currentScore = enrollment.getCurrentScore();
                    }
                    return currentScore;
                }
            }
            currentScore = 0.0;
        }
        return currentScore;
    }

    public String getCurrentGrade() {
        if (!checkedCurrentGrade) {
            checkedCurrentGrade = true;
            for (Enrollment enrollment : enrollments) {
                if (enrollment.isStudent() || enrollment.isObserver()) {
                    if (enrollment.isMultipleGradingPeriodsEnabled()) {
                        currentGrade = enrollment.getCurrentPeriodComputedCurrentGrade();
                    } else {
                        currentGrade = enrollment.getCurrentGrade();
                    }
                    return currentGrade;
                }
            }
        }
        return currentGrade;
    }

    public double getFinalScore() {
        if (finalScore == null) {
            for (Enrollment enrollment : enrollments) {
                if (enrollment.isStudent() || enrollment.isObserver()) {
                    if (enrollment.isMultipleGradingPeriodsEnabled()) {
                        finalScore = enrollment.getCurrentPeriodComputedFinalScore();
                    } else {
                        finalScore = enrollment.getFinalScore();
                    }
                    return finalScore;
                }
            }
            finalScore = 0.0;
        }
        return finalScore;
    }

    public String getFinalGrade() {
        if (!checkedFinalGrade) {
            checkedFinalGrade = true;
            for (Enrollment enrollment : enrollments) {
                if (enrollment.isStudent() || enrollment.isObserver()) {
                    if (enrollment.isMultipleGradingPeriodsEnabled()) {
                       finalGrade = enrollment.getCurrentPeriodComputedFinalGrade();
                    } else {
                        finalGrade = enrollment.getFinalGrade();
                    }
                }
            }
        }
        return finalGrade;
    }

    public void addEnrollment(Enrollment enrollment) {
        if (enrollments == null || enrollments.size() == 0) {
            enrollments = new ArrayList<>();
            enrollments.add(enrollment);
        } else {
            enrollments.add(enrollment);
        }
    }

    public enum LICENSE {
        PRIVATE_COPYRIGHTED,
        CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE,
        CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE,
        CC_ATTRIBUTION_NON_COMMERCIAL,
        CC_ATTRIBUTION_NO_DERIVATIVE,
        CC_ATTRIBUTION_SHARE_ALIKE,
        CC_ATTRIBUTION, PUBLIC_DOMAIN
    }

    public static String licenseToAPIString(LICENSE license){
        if(license == null){
            return null;
        }

        switch (license){
            case PRIVATE_COPYRIGHTED:
                return "private";
            case CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE:
                return "cc_by_nc_nd";
            case CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE:
                return "c_by_nc_sa";
            case CC_ATTRIBUTION_NON_COMMERCIAL:
                return "cc_by_nc";
            case CC_ATTRIBUTION_NO_DERIVATIVE:
                return "cc_by_nd";
            case CC_ATTRIBUTION_SHARE_ALIKE:
                return "cc_by_sa";
            case CC_ATTRIBUTION:
                return "cc_by";
            case PUBLIC_DOMAIN:
                return "public_domain";
            default:
                return "";
        }
    }

    public static String licenseToPrettyPrint(LICENSE license){
        switch (license){
            case PRIVATE_COPYRIGHTED:
                return "Private (Copyrighted)";
            case CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE:
                return "CC Attribution Non-Commercial No Derivatives";
            case CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE:
                return "CC Attribution Non-Commercial Share Alike";
            case CC_ATTRIBUTION_NON_COMMERCIAL:
                return "CC Attribution Non-Commercial";
            case CC_ATTRIBUTION_NO_DERIVATIVE:
                return "CC Attribution No Derivatives";
            case CC_ATTRIBUTION_SHARE_ALIKE:
                return "CC Attribution Share Alike";
            case CC_ATTRIBUTION:
                return "CC Attribution";
            case PUBLIC_DOMAIN:
                return "Public Domain";
            default:
                return "";
        }
    }

    public String getLicensePrettyPrint(){
        return licenseToPrettyPrint(getLicense());
    }

    public void setLicense(LICENSE license){
        this.license = licenseToAPIString(license);
    }

    public LICENSE getLicense(){
        if("public_domain".equals(license)){
            return LICENSE.PUBLIC_DOMAIN;
        } else if ("cc_by_nc_nd".equals(license)){
            return LICENSE.CC_ATTRIBUTION_NON_COMMERCIAL_NO_DERIVATIVE;
        } else if ("c_by_nc_sa".equals(license)){
            return LICENSE.CC_ATTRIBUTION_NON_COMMERCIAL_SHARE_ALIKE;
        } else if ("cc_by_nc".equals(license)){
            return LICENSE.CC_ATTRIBUTION_NON_COMMERCIAL;
        } else if ("cc_by_nd".equals(license)){
            return LICENSE.CC_ATTRIBUTION_NO_DERIVATIVE;
        } else if ("cc_by_sa".equals(license)){
            return LICENSE.CC_ATTRIBUTION_SHARE_ALIKE;
        } else if ("cc_by".equals(license)){
            return LICENSE.CC_ATTRIBUTION;
        } else {
            return LICENSE.PRIVATE_COPYRIGHTED;
        }
    }

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.originalName);
        dest.writeString(this.courseCode);
        dest.writeString(this.startAt);
        dest.writeString(this.endAt);
        dest.writeString(this.syllabusBody);
        dest.writeByte(this.hideFinalGrades ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isPublic ? (byte) 1 : (byte) 0);
        dest.writeString(this.license);
        dest.writeParcelable(this.term, flags);
        dest.writeTypedList(this.enrollments);
        dest.writeLong(this.needsGradingCount);
        dest.writeByte(this.applyAssignmentGroupWeights ? (byte) 1 : (byte) 0);
        dest.writeValue(this.currentScore);
        dest.writeValue(this.finalScore);
        dest.writeByte(this.checkedCurrentGrade ? (byte) 1 : (byte) 0);
        dest.writeByte(this.checkedFinalGrade ? (byte) 1 : (byte) 0);
        dest.writeString(this.currentGrade);
        dest.writeString(this.finalGrade);
        dest.writeByte(this.isFavorite ? (byte) 1 : (byte) 0);
        dest.writeByte(this.accessRestrictedByDate ? (byte) 1 : (byte) 0);
    }

    protected Course(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.originalName = in.readString();
        this.courseCode = in.readString();
        this.startAt = in.readString();
        this.endAt = in.readString();
        this.syllabusBody = in.readString();
        this.hideFinalGrades = in.readByte() != 0;
        this.isPublic = in.readByte() != 0;
        this.license = in.readString();
        this.term = in.readParcelable(Term.class.getClassLoader());
        this.enrollments = in.createTypedArrayList(Enrollment.CREATOR);
        this.needsGradingCount = in.readLong();
        this.applyAssignmentGroupWeights = in.readByte() != 0;
        this.currentScore = (Double) in.readValue(Double.class.getClassLoader());
        this.finalScore = (Double) in.readValue(Double.class.getClassLoader());
        this.checkedCurrentGrade = in.readByte() != 0;
        this.checkedFinalGrade = in.readByte() != 0;
        this.currentGrade = in.readString();
        this.finalGrade = in.readString();
        this.isFavorite = in.readByte() != 0;
        this.accessRestrictedByDate = in.readByte() != 0;
    }

    public static final Creator<Course> CREATOR = new Creator<Course>() {
        @Override
        public Course createFromParcel(Parcel source) {
            return new Course(source);
        }

        @Override
        public Course[] newArray(int size) {
            return new Course[size];
        }
    };

    //endregion
}

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
import android.support.annotation.NonNull;


public class RubricCriterionRating implements Comparable<RubricCriterionRating>,Parcelable {

    private String id;
    private String criterionId;
    private String description;
    private double points;
    private String comments;
    private boolean isGrade;
    private boolean isFreeFormComment;
    private double maxPoints;

    @Override
    public int compareTo(@NonNull RubricCriterionRating rating) {
        return this.getId().compareTo(rating.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RubricCriterionRating rating = (RubricCriterionRating) o;

        if (id != null ? !id.equals(rating.id) : rating.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public boolean isComment() {
        return getComments() != null && !getComments().equals("");
    }

    //region Getters

    public String getId() {
        return id;
    }

    public String getCriterionId() {
        return criterionId;
    }

    public String getDescription() {
        return description;
    }

    public double getPoints() {
        return points;
    }

    public String getComments() {
        return comments;
    }

    public boolean isGrade() {
        return isGrade;
    }

    public boolean isFreeFormComment() {
        return isFreeFormComment;
    }

    public double getMaxPoints() {
        return maxPoints;
    }

    //endregion

    //region Setters

    public void setId(String id) {
        this.id = id;
    }

    public void setCriterionId(String criterionId) {
        this.criterionId = criterionId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setGrade(boolean grade) {
        isGrade = grade;
    }

    public void setFreeFormComment(boolean freeFormComment) {
        isFreeFormComment = freeFormComment;
    }

    public void setMaxPoints(double maxPoints) {
        this.maxPoints = maxPoints;
    }


    //endregion

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.criterionId);
        dest.writeString(this.description);
        dest.writeDouble(this.points);
        dest.writeString(this.comments);
        dest.writeByte(isGrade ? (byte) 1 : (byte) 0);
        dest.writeByte(isFreeFormComment ? (byte) 1 : (byte) 0);
        dest.writeDouble(this.maxPoints);
    }

    public RubricCriterionRating() {
    }

    public RubricCriterionRating(String criterionId) {
        setGrade(false);
        setCriterionId(criterionId);
    }

    public RubricCriterionRating(RubricCriterion rubricCriterion) {
        setGrade(false);
        setCriterionId(rubricCriterion.getId());
    }

    protected RubricCriterionRating(Parcel in) {
        this.id = in.readString();
        this.criterionId = in.readString();
        this.description = in.readString();
        this.points = in.readDouble();
        this.comments = in.readString();
        this.isGrade = in.readByte() != 0;
        this.isFreeFormComment = in.readByte() != 0;
        this.maxPoints = in.readDouble();
    }

    public static final Parcelable.Creator<RubricCriterionRating> CREATOR = new Parcelable.Creator<RubricCriterionRating>() {
        @Override
        public RubricCriterionRating createFromParcel(Parcel source) {
            return new RubricCriterionRating(source);
        }

        @Override
        public RubricCriterionRating[] newArray(int size) {
            return new RubricCriterionRating[size];
        }
    };

    //endregion
}

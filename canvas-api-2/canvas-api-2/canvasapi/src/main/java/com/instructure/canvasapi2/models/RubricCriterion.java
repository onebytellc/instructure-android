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

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class RubricCriterion implements Comparable<RubricCriterion>, Parcelable {

    private String id;
    private Rubric rubric;
    private String description;
    @SerializedName("long_description")
    private String longDescription;
    private double points;
    private List<RubricCriterionRating> ratings = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RubricCriterion that = (RubricCriterion) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public RubricCriterionRating getGradedCriterionRating(){
        for(RubricCriterionRating rating : ratings){
            if(rating.isGrade()){
                return rating;
            }
        }
        return null;
    }

    /**
     *  Freeform rubric comments in canvas may contain RubricCriterionRatings not included in the assignment rubric.
     *  @return true if the rubric assessment contains a rating for the provided rubric criterion
     */
    public  boolean containsRubricCriterionRating(String ratingId, List<RubricCriterionRating> criterionRatings){
        for(RubricCriterionRating rating : criterionRatings){
            if(rating.getId().equals(ratingId)){
                return true;
            }
        }
        return false;
    }

    public void markGradeByPoints(double points){
        for (RubricCriterionRating criterionRating : ratings) {
            if (criterionRating.getPoints() == points) {
                criterionRating.setGrade(true);
            }else{
                criterionRating.setGrade(false);
            }
        }
    }

    public void handleComments(RubricCriterionRating rating){
        if (rating.isComment() && !ratings.contains(rating)) {
            rating.setDescription(rating.getComments());
            ratings.add(rating);
        }
    }

    public void markGrade(RubricCriterionRating rating) {
        markGradeByPoints(rating.getPoints());
        handleComments(rating);
    }


    public void markFreeformGrade(RubricCriterionRating rating, RubricCriterion criterion) {
        if(containsRubricCriterionRating(rating.getCriterionId(), criterion.getRatings())){
            markGradeByPoints(rating.getPoints());
        }
        else{
            rating.setGrade(true);
            ratings.add(rating);
        }

        handleComments(rating);
    }

    public void markGrades(RubricAssessment rubricAssessment, List<RubricCriterion> criteria) {
        if (rubricAssessment == null) { return; }

        for (RubricCriterionRating rating : rubricAssessment.getRatings()) {
            for (RubricCriterion criterion : criteria) {
                if (criterion.getId().equals(rating.getCriterionId())) {
                    criterion.markGrade(rating);
                    break;
                }
            }
        }
    }

    public void markGrades(RubricAssessment rubricAssessment, List<RubricCriterion> criteria, boolean isFreeFormComment) {
        if (rubricAssessment == null) { return; }

        for (RubricCriterionRating rating : rubricAssessment.getRatings()) {
            for (RubricCriterion criterion : criteria) {
                if (criterion.getId().equals(rating.getCriterionId())) {
                    if(isFreeFormComment){
                        criterion.markFreeformGrade(rating, criterion);
                    }
                    else{
                        criterion.markGrade(rating);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public int compareTo(@NonNull RubricCriterion rubricCriterion) {
        return this.getId().compareTo(rubricCriterion.getId());
    }

    //region Getters

    public String getId() {
        return id;
    }

    public Rubric getRubric() {
        return rubric;
    }

    public String getDescription() {
        return description;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public double getPoints() {
        return points;
    }

    public List<RubricCriterionRating> getRatings() {
        return ratings;
    }

    //endregion

    //region Setters

    public void setId(String id) {
        this.id = id;
    }

    public void setRubric(Rubric rubric) {
        this.rubric = rubric;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public void setRatings(List<RubricCriterionRating> ratings) {
        this.ratings = ratings;
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
        dest.writeParcelable(this.rubric, flags);
        dest.writeString(this.description);
        dest.writeString(this.longDescription);
        dest.writeDouble(this.points);
        dest.writeTypedList(ratings);
    }

    public RubricCriterion(Rubric rubric) {
        setRubric(rubric);
    }

    public RubricCriterion() {
    }

    protected RubricCriterion(Parcel in) {
        this.id = in.readString();
        this.rubric = in.readParcelable(Rubric.class.getClassLoader());
        this.description = in.readString();
        this.longDescription = in.readString();
        this.points = in.readDouble();
        this.ratings = in.createTypedArrayList(RubricCriterionRating.CREATOR);
    }

    public static final Parcelable.Creator<RubricCriterion> CREATOR = new Parcelable.Creator<RubricCriterion>() {
        @Override
        public RubricCriterion createFromParcel(Parcel source) {
            return new RubricCriterion(source);
        }

        @Override
        public RubricCriterion[] newArray(int size) {
            return new RubricCriterion[size];
        }
    };

    //endregion
}

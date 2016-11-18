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

import java.util.ArrayList;
import java.util.List;


public class Rubric implements Parcelable {

    private Assignment assignment;
    private List<RubricCriterion> criteria = new ArrayList<>();
    @SerializedName("free_form_criterion_comments")
    private boolean freeFormCriterionComments;

    //region Getter

    public Assignment getAssignment() {
        return assignment;
    }

    //endregion

    //region Setter

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public void setCriteria(List<RubricCriterion> criteria) {
        this.criteria = criteria;
    }

    public void setFreeFormCriterionComments(boolean freeFormCriterionComments) {
        this.freeFormCriterionComments = freeFormCriterionComments;
    }

    //endregion

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.assignment, flags);
        dest.writeTypedList(criteria);
        dest.writeByte(freeFormCriterionComments ? (byte) 1 : (byte) 0);
    }

    public Rubric() {
    }

    protected Rubric(Parcel in) {
        this.assignment = in.readParcelable(Assignment.class.getClassLoader());
        this.criteria = in.createTypedArrayList(RubricCriterion.CREATOR);
        this.freeFormCriterionComments = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Rubric> CREATOR = new Parcelable.Creator<Rubric>() {
        @Override
        public Rubric createFromParcel(Parcel source) {
            return new Rubric(source);
        }

        @Override
        public Rubric[] newArray(int size) {
            return new Rubric[size];
        }
    };

    //endregion
}

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
import java.util.Date;
import java.util.List;


public class AssignmentGroup extends CanvasModel<AssignmentGroup> {

    private long id;
    private String name;
    private int position;
    @SerializedName("group_weight")
    private double groupWeight;
    private List<Assignment> assignments = new ArrayList<>();

    //region Getters

    @Override
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public double getGroupWeight() { return groupWeight; }

    //endregion

    //region Setters

    public void setId(long id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }
    public void setGroupWeight(double group_weight) {this.groupWeight = group_weight;}

    //endregion

    public AssignmentGroup() {}

    public AssignmentGroup(String name, int position) {
        this.name = name;
        this.id = name.hashCode();
        this.position = position;
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return Integer.toString(position);
    }

    //region Parcelable

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeInt(this.position);
        dest.writeList(this.assignments);
        dest.writeDouble(this.groupWeight);
    }

    private AssignmentGroup(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.position = in.readInt();
        in.readList(this.assignments, Assignment.class.getClassLoader());
        this.groupWeight = in.readDouble();
    }

    public static Creator<AssignmentGroup> CREATOR = new Creator<AssignmentGroup>() {
        public AssignmentGroup createFromParcel(Parcel source) {
            return new AssignmentGroup(source);
        }

        public AssignmentGroup[] newArray(int size) {
            return new AssignmentGroup[size];
        }
    };

    //endregion
}

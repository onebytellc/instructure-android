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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.DateHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Section extends CanvasContext implements Comparable<CanvasContext> {

    private long id;
    private String name;
    @SerializedName("course_id")
    private long courseId;
    @SerializedName("start_at")
    private String startAt;
    @SerializedName("end_at")
    private String endAt;
    private List<User> students = new ArrayList<>();

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
        return getName();
    }

    @Override
    public Type getType() {
        return Type.SECTION;
    }

    //region Getters

    @Override
    public String getName() {
        return name;
    }

    public long getCourseId() {
        return courseId;
    }

    public @Nullable Date getStartAt() {
        return APIHelper.stringToDate(startAt);
    }

    public @Nullable Date getEndAt() {
        return APIHelper.stringToDate(endAt);
    }

    public List<User> getStudents() {
        if(students == null) {
            students = new ArrayList<>();
        }
        return students;
    }

    //endregion

    //region Setters

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public void setStartAt(Date startAt) {
        this.startAt = APIHelper.dateToString(startAt);
    }

    public void setEndAt(Date endAt) {
        this.endAt = APIHelper.dateToString(endAt);
    }

    public void setStudents(@NonNull List<User> students) {
        this.students = students;
    }

    //endregion

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeLong(this.courseId);
        dest.writeString(this.startAt);
        dest.writeString(this.endAt);
        dest.writeTypedList(students);
    }

    public Section() {
    }

    protected Section(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.courseId = in.readLong();
        this.startAt = in.readString();
        this.endAt = in.readString();
        this.students = in.createTypedArrayList(User.CREATOR);
    }

    public static final Creator<Section> CREATOR = new Creator<Section>() {
        @Override
        public Section createFromParcel(Parcel source) {
            return new Section(source);
        }

        @Override
        public Section[] newArray(int size) {
            return new Section[size];
        }
    };

    //endregion
}

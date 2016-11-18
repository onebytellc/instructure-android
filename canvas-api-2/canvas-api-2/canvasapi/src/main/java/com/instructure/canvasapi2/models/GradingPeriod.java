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

import java.util.Date;

public class GradingPeriod extends CanvasModel<GradingPeriod> {

    private long id;
    private String title;
    @SerializedName("start_date")
    private String startDate;
    @SerializedName("end_date")
    private String endDate;
    private int weight;

    //region Getters

    @Override
    public long getId() {
        return this.id;
    }

    public String getTitle() {
        return title;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public int getWeight() {
        return weight;
    }

    //endregion

    //region Setters

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    //endregion

    public GradingPeriod() {}

    public GradingPeriod(String title, long id) {
        this.title = title;
        this.id = id;
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //region Parcelable

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeString(this.startDate);
        dest.writeString(this.endDate);
        dest.writeInt(this.weight);
    }

    protected GradingPeriod(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.startDate = in.readString();
        this.endDate = in.readString();
        this.weight = in.readInt();
    }

    public static final Creator<GradingPeriod> CREATOR = new Creator<GradingPeriod>() {
        public GradingPeriod createFromParcel(Parcel source) {
            return new GradingPeriod(source);
        }

        public GradingPeriod[] newArray(int size) {
            return new GradingPeriod[size];
        }
    };

    //endregion
}

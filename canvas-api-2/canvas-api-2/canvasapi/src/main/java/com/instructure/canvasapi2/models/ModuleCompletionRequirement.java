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


public class ModuleCompletionRequirement extends CanvasModel<ModuleCompletionRequirement> {

    private long id;
    private String type;
    @SerializedName("min_score")
    private double minScore;
    @SerializedName("max_score")
    private double maxScore;

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
        return null;
    }

    //region Getters

    public String getType() {
        return type;
    }

    public double getMinScore() {
        return minScore;
    }

    public double getMaxScore() {
        return maxScore;
    }

    //endregion'

    //region Setters

    public void setId(long id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMinScore(double minScore) {
        this.minScore = minScore;
    }

    public void setMaxScore(double maxScore) {
        this.maxScore = maxScore;
    }

    //endregion

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.type);
        dest.writeDouble(this.minScore);
        dest.writeDouble(this.maxScore);
    }

    public ModuleCompletionRequirement() {
    }

    protected ModuleCompletionRequirement(Parcel in) {
        this.id = in.readLong();
        this.type = in.readString();
        this.minScore = in.readDouble();
        this.maxScore = in.readDouble();
    }

    public static final Creator<ModuleCompletionRequirement> CREATOR = new Creator<ModuleCompletionRequirement>() {
        @Override
        public ModuleCompletionRequirement createFromParcel(Parcel source) {
            return new ModuleCompletionRequirement(source);
        }

        @Override
        public ModuleCompletionRequirement[] newArray(int size) {
            return new ModuleCompletionRequirement[size];
        }
    };
}

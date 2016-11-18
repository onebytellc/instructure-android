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


public class NeedsGradingCount implements Parcelable, Comparable<NeedsGradingCount> {

    @SerializedName("section_id")
    private long sectionId;
    @SerializedName("needs_grading_count")
    private long needsGradingCount;

    @Override
    public int compareTo(@NonNull NeedsGradingCount another) {
        return String.valueOf(this.getSectionId()).compareTo(String.valueOf(another.getSectionId()));
    }

    //region Getters

    public long getSectionId() {
        return sectionId;
    }

    public long getNeedsGradingCount() {
        return needsGradingCount;
    }

    //endregion

    //region Setters

    public void setSectionId(long sectionId) {
        this.sectionId = sectionId;
    }

    public void setNeedsGradingCount(long needsGradingCount) {
        this.needsGradingCount = needsGradingCount;
    }

    //endregion

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.sectionId);
        dest.writeLong(this.needsGradingCount);
    }

    public NeedsGradingCount() {
    }

    protected NeedsGradingCount(Parcel in) {
        this.sectionId = in.readLong();
        this.needsGradingCount = in.readLong();
    }

    public static final Parcelable.Creator<NeedsGradingCount> CREATOR = new Parcelable.Creator<NeedsGradingCount>() {
        @Override
        public NeedsGradingCount createFromParcel(Parcel source) {
            return new NeedsGradingCount(source);
        }

        @Override
        public NeedsGradingCount[] newArray(int size) {
            return new NeedsGradingCount[size];
        }
    };

    //endregion
}

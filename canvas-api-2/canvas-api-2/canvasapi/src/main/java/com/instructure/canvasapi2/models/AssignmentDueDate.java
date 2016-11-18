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
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.instructure.canvasapi2.utils.APIHelper;

import java.util.Date;


public class AssignmentDueDate extends CanvasModel<AssignmentDueDate> {

    private long id;
    @SerializedName("due_at")
    private String dueAt;
    private String title;
    @SerializedName("unlock_at")
    private String unlockAt;
    @SerializedName("lock_at")
    private String lockAt;
    private boolean base;

    @Override
    public long getId() {
        return id;
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        return getDueAt();
    }

    @Nullable
    @Override
    public String getComparisonString() {
        return dueAt;
    }

    //region Getters

    public @Nullable Date getDueAt() {
        return APIHelper.stringToDate(dueAt);
    }

    public String getTitle() {
        return title;
    }

    public @Nullable Date getUnlockAt() {
        return APIHelper.stringToDate(unlockAt);
    }

    public @Nullable Date getLockAt() {
        return APIHelper.stringToDate(lockAt);
    }

    public boolean isBase() {
        return base;
    }

    //endregion

    //region Setters

    public void setId(long id) {
        this.id = id;
    }

    public void setDueAt(String dueAt) {
        this.dueAt = dueAt;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUnlockAt(String unlockAt) {
        this.unlockAt = unlockAt;
    }

    public void setLockAt(String lockAt) {
        this.lockAt = lockAt;
    }

    public void setBase(boolean base) {
        this.base = base;
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
        dest.writeString(this.dueAt);
        dest.writeString(this.title);
        dest.writeString(this.unlockAt);
        dest.writeString(this.lockAt);
        dest.writeByte(base ? (byte) 1 : (byte) 0);
    }

    public AssignmentDueDate() {
    }

    protected AssignmentDueDate(Parcel in) {
        this.id = in.readLong();
        this.dueAt = in.readString();
        this.title = in.readString();
        this.unlockAt = in.readString();
        this.lockAt = in.readString();
        this.base = in.readByte() != 0;
    }

    public static final Creator<AssignmentDueDate> CREATOR = new Creator<AssignmentDueDate>() {
        @Override
        public AssignmentDueDate createFromParcel(Parcel source) {
            return new AssignmentDueDate(source);
        }

        @Override
        public AssignmentDueDate[] newArray(int size) {
            return new AssignmentDueDate[size];
        }
    };

    //endregion
}

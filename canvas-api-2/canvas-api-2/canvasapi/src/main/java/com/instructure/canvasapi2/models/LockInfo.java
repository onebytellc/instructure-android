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

import java.util.ArrayList;
import java.util.Date;


public class LockInfo extends CanvasComparable<LockInfo> {

    private ArrayList<String> modulePrerequisiteNames = new ArrayList<>();
    private String lockedModuleName;
    @SerializedName("context_module")
    private LockedModule contextModule;
    @SerializedName("unlock_at")
    private String unlockAt;

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return lockedModuleName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //region Getters

    public boolean isEmpty(){
        return lockedModuleName == null
                && contextModule == null
                && (modulePrerequisiteNames == null || modulePrerequisiteNames.size() == 0)
                && unlockAt == null;
    }

    public String getLockedModuleName() {
        if (contextModule != null) {
            return contextModule.getName();
        } else {
            return "";
        }
    }

    public ArrayList<String> getModulePrerequisiteNames() {
        return modulePrerequisiteNames;
    }

    public LockedModule getContextModule() {
        return contextModule;
    }

    public @Nullable Date getUnlockAt() {
        return APIHelper.stringToDate(unlockAt);
    }

    //endregion

    //region Setters

    public void setModulePrerequisiteNames(ArrayList<String> modulePrerequisiteNames) {
        this.modulePrerequisiteNames = modulePrerequisiteNames;
    }

    public void setLockedModuleName(String lockedModuleName) {
        this.lockedModuleName = lockedModuleName;
    }

    public void setContextModule(LockedModule contextModule) {
        this.contextModule = contextModule;
    }

    public void setUnlockAt(String unlockAt) {
        this.unlockAt = unlockAt;
    }

    //endregion

    //region Parcelable

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(this.modulePrerequisiteNames);
        dest.writeString(this.lockedModuleName);
        dest.writeParcelable(this.contextModule, flags);
        dest.writeString(this.unlockAt);
    }

    public LockInfo() {
    }

    protected LockInfo(Parcel in) {
        this.modulePrerequisiteNames = in.createStringArrayList();
        this.lockedModuleName = in.readString();
        this.contextModule = in.readParcelable(LockedModule.class.getClassLoader());
        this.unlockAt = in.readString();
    }

    public static final Creator<LockInfo> CREATOR = new Creator<LockInfo>() {
        @Override
        public LockInfo createFromParcel(Parcel source) {
            return new LockInfo(source);
        }

        @Override
        public LockInfo[] newArray(int size) {
            return new LockInfo[size];
        }
    };

    //endregion
}

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
import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.instructure.canvasapi2.utils.TestHelpers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class LockedModule extends CanvasModel<LockedModule> {

    private long id;
    @SerializedName("context_id")
    private long contextId;
    @SerializedName("context_type")
    private String contextType;
    private String name;
    @SerializedName("unlock_at")
    private String unlockAt;
    @SerializedName("require_sequential_progress")
    private boolean requireSequentialProgress;

    private List<ModuleName> prerequisites = new ArrayList<>();
    @SerializedName("completion_requirements")
    private List<ModuleCompletionRequirement> completionRequirements = new ArrayList<>();

    private class ModuleName implements Serializable {

        private String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        private String name;
    }

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
        return name;
    }

    //region getters

    public long getContextId() {
        return contextId;
    }

    public String getContextType() {
        return contextType;
    }

    public String getName() {
        return name;
    }

    public String getUnlockAt() {
        return unlockAt;
    }

    public boolean isRequireSequentialProgress() {
        return requireSequentialProgress;
    }

    public List<ModuleName> getPrerequisites() {
        return prerequisites;
    }

    public List<ModuleCompletionRequirement> getCompletionRequirements() {
        return completionRequirements;
    }

    //endregion

    //region Setters

    public void setId(long id) {
        this.id = id;
    }

    public void setContextId(long contextId) {
        this.contextId = contextId;
    }

    public void setContextType(String contextType) {
        this.contextType = contextType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnlockAt(String unlockAt) {
        this.unlockAt = unlockAt;
    }

    public void setRequireSequentialProgress(boolean requireSequentialProgress) {
        this.requireSequentialProgress = requireSequentialProgress;
    }

    public void setPrerequisites(List<ModuleName> prerequisites) {
        this.prerequisites = prerequisites;
    }

    public void setCompletionRequirements(List<ModuleCompletionRequirement> completionRequirements) {
        this.completionRequirements = completionRequirements;
    }

    //endregion

    //region Unit Test

    public static boolean isLockedModuleValid(LockedModule lockedModule) {
        if(lockedModule.getContextId() <= 0) {
            Log.d(TestHelpers.UNIT_TEST_TAG, "Invalid LockedModule id");
            return false;
        }
        if(lockedModule.getName() == null) {
            Log.d(TestHelpers.UNIT_TEST_TAG, "Invalid LockedModule name");
            return false;
        }
        if(lockedModule.getUnlockAt() == null) {
            Log.d(TestHelpers.UNIT_TEST_TAG, "Invalid LockedModule unlock date");
            return false;
        }
        if(lockedModule.getPrerequisites() == null) {
            Log.d(TestHelpers.UNIT_TEST_TAG, "Invalid LockedModule prerequisites");
            return false;
        }
        for(int i = 0; i < lockedModule.getPrerequisites().size(); i++) {
            if(lockedModule.getPrerequisites().get(i).getName() == null) {
                Log.d(TestHelpers.UNIT_TEST_TAG, "Invalid LockedModule prereq name");
                return false;
            }
        }
        return true;
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
        dest.writeLong(this.contextId);
        dest.writeString(this.contextType);
        dest.writeString(this.name);
        dest.writeString(this.unlockAt);
        dest.writeByte(requireSequentialProgress ? (byte) 1 : (byte) 0);
        dest.writeList(this.prerequisites);
        dest.writeTypedList(completionRequirements);
    }

    public LockedModule() {
    }

    protected LockedModule(Parcel in) {
        this.id = in.readLong();
        this.contextId = in.readLong();
        this.contextType = in.readString();
        this.name = in.readString();
        this.unlockAt = in.readString();
        this.requireSequentialProgress = in.readByte() != 0;
        this.prerequisites = new ArrayList<ModuleName>();
        in.readList(this.prerequisites, ModuleName.class.getClassLoader());
        this.completionRequirements = in.createTypedArrayList(ModuleCompletionRequirement.CREATOR);
    }

    public static final Creator<LockedModule> CREATOR = new Creator<LockedModule>() {
        @Override
        public LockedModule createFromParcel(Parcel source) {
            return new LockedModule(source);
        }

        @Override
        public LockedModule[] newArray(int size) {
            return new LockedModule[size];
        }
    };

    //endregion
}

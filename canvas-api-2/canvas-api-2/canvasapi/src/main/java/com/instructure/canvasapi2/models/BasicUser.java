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

import java.util.Date;


public class BasicUser extends CanvasModel<BasicUser> {

    private long id;
    private String name;

    @SerializedName("avatar_url")
    private String avatarUrl;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    @Nullable
    @Override
    public String getComparisonString() {
        return null;
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        return null;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.avatarUrl);
    }

    public BasicUser() {
    }

    protected BasicUser(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.avatarUrl = in.readString();
    }

    public static final Creator<BasicUser> CREATOR = new Creator<BasicUser>() {
        @Override
        public BasicUser createFromParcel(Parcel source) {
            return new BasicUser(source);
        }

        @Override
        public BasicUser[] newArray(int size) {
            return new BasicUser[size];
        }
    };
}

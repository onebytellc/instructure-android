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
import java.util.HashMap;


public class Recipient extends CanvasComparable<Recipient> {

    public enum Type {group, metagroup, person}

    private String id;

    private String name;

    @SerializedName("user_count")
    private int userCount;

    @SerializedName("item_count")
    private int itemCount;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("common_courses")
    private HashMap<String, String[]> commonCourses;

    @SerializedName("common_groups")
    private HashMap<String, String[]> commonGroups;

    public Recipient() {
    }

    //region Getters

    public String getStringId() {
        return id;
    }

    public HashMap<String, String[]> getCommonCourses() {
        return commonCourses;
    }


    public HashMap<String, String[]> getCommonGroups() {
        return commonGroups;
    }

    public long getIdAsLong() {
        try {
            if (id.startsWith("group_") || id.startsWith("course_")) {
                int indexUnder = id.indexOf("_");
                return Long.parseLong(id.substring(indexUnder + 1, id.length()));
            }
            return Long.parseLong(id);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return id;
    }

    public int getUserCount() {
        return userCount;
    }

    public String getName() {
        return name;
    }

    public Type getRecipientType() {

        try {
            long tempId = Long.parseLong(id);
            return Type.person;
        } catch (Exception E) {
        }

        if (userCount > 0) {
            return Type.group;
        }

        return Type.metagroup;
    }

    public int getItemCount() {
        return itemCount;
    }

    public String getAvatarURL() {
        return avatarUrl;
    }

    //endregion

    //region Setters

    public void setStringId(String id) {
        this.id = id;
    }

    public void setCommonCourses(HashMap<String, String[]> commonCourses) {
        this.commonCourses = commonCourses;
    }

    public void setCommonGroups(HashMap<String, String[]> commonGroups) {
        this.commonGroups = commonGroups;
    }

    public void setAvatarURL(String avatar) {
        this.avatarUrl = avatar;
    }

    //endregion

    public Recipient(String _id, String _name, int _userCount, int _itemCount, int _enum) {
        id = _id;
        name = _name;

        userCount = _userCount;
        itemCount = _itemCount;

    }

    public static int recipientTypeToInt(Type t) {
        if (t == Type.group)
            return 0;
        else if (t == Type.metagroup)
            return 1;
        else if (t == Type.person)
            return 2;
        else
            return -1;
    }

    public static Type intToRecipientType(int i) {
        if (i == 0)
            return Type.group;
        else if (i == 1)
            return Type.metagroup;
        else if (i == 2)
            return Type.person;
        else
            return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        Recipient other = (Recipient) obj;

        return compareTo(other) == 0;
    }

    @Override
    public String toString() {
        return name;
    }

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeInt(this.userCount);
        dest.writeInt(this.itemCount);
        dest.writeString(this.name);
        dest.writeString(this.avatarUrl);
        dest.writeSerializable(this.commonCourses);
        dest.writeSerializable(this.commonGroups);
    }

    @SuppressWarnings("unchecked")
    protected Recipient(Parcel in) {
        this.id = in.readString();
        this.userCount = in.readInt();
        this.itemCount = in.readInt();
        this.name = in.readString();
        this.avatarUrl = in.readString();
        this.commonCourses = (HashMap<String, String[]>) in.readSerializable();
        this.commonGroups = (HashMap<String, String[]>) in.readSerializable();
    }

    public static final Creator<Recipient> CREATOR = new Creator<Recipient>() {
        @Override
        public Recipient createFromParcel(Parcel source) {
            return new Recipient(source);
        }

        @Override
        public Recipient[] newArray(int size) {
            return new Recipient[size];
        }
    };

    //endregion
}

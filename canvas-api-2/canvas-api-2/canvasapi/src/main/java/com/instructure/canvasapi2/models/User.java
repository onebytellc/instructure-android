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
import com.instructure.canvasapi2.utils.APIHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class User extends CanvasContext{

    private long id;
    private String name;
    @SerializedName("short_name")
    private String shortName;
    @SerializedName("login_id")
    private String loginId;
    @SerializedName("avatar_url")
    private String avatarUrl;
    @SerializedName("primary_email")
    private String primaryEmail;
    @SerializedName("sortable_name")
    private String sortableName;
    private String bio;
    private List<Enrollment> enrollments = new ArrayList<>();
    //Helper variable for the "specified" enrollment.
    private int enrollmentIndex;
    @SerializedName("last_login")
    private String lastLogin;

    public User() {}

    public User(long id) {
        this.id = id;
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
    public long getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        User other = (User) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public Type getType() {
        return Type.USER;
    }

    // User Permissions - defaults to false, returned with UserAPI.getSelfWithPermissions()
    public boolean canUpdateAvatar(){
        return getPermissions() != null && getPermissions().canUpdateAvatar();
    }
    public boolean canUpdateName(){
        return getPermissions() != null && getPermissions().canUpdateName();
    }

    //region Getters

    // Matches recipents common_courses or common_groups format
    public HashMap<String, String[]> getEnrollmentsHash() {
        HashMap<String, List<String>> enrollments = new HashMap<>();
        for (Enrollment enrollment: getEnrollments()) {
            String key = enrollment.getCourseId() + "";
            if (enrollments.containsKey(key)) {
                enrollments.get(key).add(enrollment.getRole());
            } else {
                List<String> newList = new ArrayList<>();
                newList.add(enrollment.getRole());
                enrollments.put(key, newList);
            }
        }

        HashMap<String, String[]> stringArrayEnrollments = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : enrollments.entrySet()) {
            stringArrayEnrollments.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
        }
        return stringArrayEnrollments;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public String getSortableName() {
        return sortableName;
    }

    public String getBio() {
        return bio;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public int getEnrollmentIndex() {
        return enrollmentIndex;
    }

    public Date getLastLogin() {
        return APIHelper.stringToDate(lastLogin);
    }

    //endregion

    //region Setters

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public void setSortableName(String sortableName) {
        this.sortableName = sortableName;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    public void setEnrollmentIndex(int enrollmentIndex) {
        this.enrollmentIndex = enrollmentIndex;
    }

    public void setLastLogin(Date date) {
        lastLogin = APIHelper.dateToString(date);
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
        dest.writeString(this.shortName);
        dest.writeString(this.loginId);
        dest.writeString(this.avatarUrl);
        dest.writeString(this.primaryEmail);
        dest.writeString(this.sortableName);
        dest.writeString(this.bio);
        dest.writeTypedList(enrollments);
        dest.writeInt(this.enrollmentIndex);
        dest.writeString(this.lastLogin);
    }

    protected User(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.shortName = in.readString();
        this.loginId = in.readString();
        this.avatarUrl = in.readString();
        this.primaryEmail = in.readString();
        this.sortableName = in.readString();
        this.bio = in.readString();
        this.enrollments = in.createTypedArrayList(Enrollment.CREATOR);
        this.enrollmentIndex = in.readInt();
        this.lastLogin = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    //endregion
}

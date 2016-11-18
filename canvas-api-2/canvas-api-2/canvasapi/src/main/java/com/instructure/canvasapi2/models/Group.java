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


public class Group extends CanvasContext{

    private long id;
    private String name;
    private String description;
    @SerializedName("avatar_url")
    private String avatarUrl;
    @SerializedName("is_public")
    private boolean isPublic;
    @SerializedName("followed_by_user")
    private boolean followedByUser;
    @SerializedName("members_count")
    private int membersCount;

    // * If "parent_context_auto_join", anyone can join and will be
    //   automatically accepted.
    // * If "parent_context_request", anyone  can request to join, which
    //   must be approved by a group moderator.
    // * If "invitation_only", only those how have received an
    //   invitation my join the group, by accepting that invitation.
    @SerializedName("join_level")
    private String joinLevel;
    @SerializedName("context_type")
    private String contextType;

    //At most, ONE of these will be set.
    @SerializedName("course_id")
    private long courseId;
    @SerializedName("account_id")
    private long accountId;

    // Certain types of groups have special role designations. Currently,
    // these include: "communities", "student_organized", and "imported".
    // Regular course/account groups have a role of null.
    private String role;
    @SerializedName("group_category_id")
    private long groupCategoryId;
    @SerializedName("storage_quota_mb")
    private long storageQuotaMb;
    @SerializedName("is_favorite")
    private boolean isFavorite;

    public enum JoinLevel {Automatic, Request, Invitation, Unknown}
    public enum GroupRole {Community, Student, Imported, Course}
    public enum GroupContext {Course,  Account, Other}

    @Override
    public long getId() { return id; }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return name;
    }

    @Override
    public Type getType() {return Type.GROUP;}

    //region Getters

    public JoinLevel getJoinLevel() {

        // * If "parent_context_auto_join", anyone can join and will be
        //   automatically accepted.
        // * If "parent_context_request", anyone  can request to join, which
        //   must be approved by a group moderator.
        // * If "invitation_only", only those how have received an
        //   invitation my join the group, by accepting that invitation.

        if("parent_context_auto_join".equalsIgnoreCase(joinLevel)){
            return JoinLevel.Automatic;
        } else if ("parent_context_request".equalsIgnoreCase(joinLevel)){
            return JoinLevel.Request;
        } else if ("invitation_only".equalsIgnoreCase(joinLevel)){
            return JoinLevel.Invitation;
        }

        return JoinLevel.Unknown;
    }

    public GroupContext getContextType() {

        if("course".equalsIgnoreCase(contextType)){
            return GroupContext.Course;
        } else if ("account".equalsIgnoreCase(contextType)){
            return GroupContext.Account;
        }
        return GroupContext.Other;
    }

    public GroupRole getRole() {
        // Certain types of groups have special role designations. Currently,
        // these include: "communities", "student_organized", and "imported".
        // Regular course/account groups have a role of null.

        if("communities".equalsIgnoreCase(role)){
            return GroupRole.Community;
        } else if ("student_organized".equals(role)){
            return GroupRole.Student;
        } else if ("imported".equals(role)){
            return GroupRole.Imported;
        }
        return GroupRole.Course;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isFollowedByUser() {
        return followedByUser;
    }

    public int getMembersCount() {
        return membersCount;
    }

    public long getCourseId() {
        return courseId;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getGroupCategoryId() {
        return groupCategoryId;
    }

    public long getStorageQuotaMb() {
        return storageQuotaMb;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    //endregion

    //region Setters

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public void setFollowedByUser(boolean followedByUser) {
        this.followedByUser = followedByUser;
    }

    public void setMembersCount(int membersCount) {
        this.membersCount = membersCount;
    }

    public void setJoinLevel(String joinLevel) {
        this.joinLevel = joinLevel;
    }

    public void setContextType(String contextType) {
        this.contextType = contextType;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setGroupCategoryId(long groupCategoryId) {
        this.groupCategoryId = groupCategoryId;
    }

    public void setStorageQuotaMb(long storageQuotaMb) {
        this.storageQuotaMb = storageQuotaMb;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
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
        dest.writeString(this.description);
        dest.writeString(this.avatarUrl);
        dest.writeByte(isPublic ? (byte) 1 : (byte) 0);
        dest.writeByte(followedByUser ? (byte) 1 : (byte) 0);
        dest.writeInt(this.membersCount);
        dest.writeString(this.joinLevel);
        dest.writeString(this.contextType);
        dest.writeLong(this.courseId);
        dest.writeLong(this.accountId);
        dest.writeString(this.role);
        dest.writeLong(this.groupCategoryId);
        dest.writeLong(this.storageQuotaMb);
        dest.writeByte(isFavorite ? (byte) 1 : (byte) 0);
    }

    public Group() {
    }

    protected Group(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.description = in.readString();
        this.avatarUrl = in.readString();
        this.isPublic = in.readByte() != 0;
        this.followedByUser = in.readByte() != 0;
        this.membersCount = in.readInt();
        this.joinLevel = in.readString();
        this.contextType = in.readString();
        this.courseId = in.readLong();
        this.accountId = in.readLong();
        this.role = in.readString();
        this.groupCategoryId = in.readLong();
        this.storageQuotaMb = in.readLong();
        this.isFavorite = in.readByte() != 0;
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel source) {
            return new Group(source);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };

    //endregion
}
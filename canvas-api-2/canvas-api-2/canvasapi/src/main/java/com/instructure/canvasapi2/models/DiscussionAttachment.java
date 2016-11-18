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

import java.util.Date;


public class DiscussionAttachment extends CanvasModel<DiscussionAttachment> {

    private long id;
    private boolean locked;
    private boolean hidden;
    @SerializedName("locked_for_user")
    private boolean lockedForUser;
    @SerializedName("hidden_for_user")
    private boolean hiddenForUser;
    private int size;
    @SerializedName("lock_at")
    private String lockAt;
    @SerializedName("unlock_at")
    private String unlockAt;
    @SerializedName("updated_at")
    private String updatedAt;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("display_name")
    private String displayName;
    private String filename;
    private String url;
    @SerializedName("content-type")
    private String contentType;
    @SerializedName("folder_id")
    private long folderId;
    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

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
        return filename;
    }

    public boolean shouldShowToUser() {
        if (hidden || hiddenForUser) {
            return false;
        } else if (locked || lockedForUser) {
            Date unlockAtDate = APIHelper.stringToDate(unlockAt);
            if (unlockAt == null) {
                return false;
            } else {
                return new Date().after(unlockAtDate);
            }
        } else {
            return true;
        }
    }

    //region Getters

    public boolean isLocked() {
        return locked;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isLockedForUser() {
        return lockedForUser;
    }

    public boolean isHiddenForUser() {
        return hiddenForUser;
    }

    public int getSize() {
        return size;
    }

    public String getLockAt() {
        return lockAt;
    }

    public String getUnlockAt() {
        return unlockAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFilename() {
        return filename;
    }

    public String getUrl() {
        return url;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFolderId() {
        return folderId;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    //endregion

    //region Setters

    public void setId(long id) {
        this.id = id;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setLockedForUser(boolean lockedForUser) {
        this.lockedForUser = lockedForUser;
    }

    public void setHiddenForUser(boolean hiddenForUser) {
        this.hiddenForUser = hiddenForUser;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setLockAt(String lockAt) {
        this.lockAt = lockAt;
    }

    public void setUnlockAt(String unlockAt) {
        this.unlockAt = unlockAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setDisplay_name(String displayName) {
        this.displayName = displayName;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
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
        dest.writeByte(locked ? (byte) 1 : (byte) 0);
        dest.writeByte(hidden ? (byte) 1 : (byte) 0);
        dest.writeByte(lockedForUser ? (byte) 1 : (byte) 0);
        dest.writeByte(hiddenForUser ? (byte) 1 : (byte) 0);
        dest.writeInt(this.size);
        dest.writeString(this.lockAt);
        dest.writeString(this.unlockAt);
        dest.writeString(this.updatedAt);
        dest.writeString(this.createdAt);
        dest.writeString(this.displayName);
        dest.writeString(this.filename);
        dest.writeString(this.url);
        dest.writeString(this.contentType);
        dest.writeLong(this.folderId);
        dest.writeString(this.thumbnailUrl);
    }

    public DiscussionAttachment() {
    }

    protected DiscussionAttachment(Parcel in) {
        this.id = in.readLong();
        this.locked = in.readByte() != 0;
        this.hidden = in.readByte() != 0;
        this.lockedForUser = in.readByte() != 0;
        this.hiddenForUser = in.readByte() != 0;
        this.size = in.readInt();
        this.lockAt = in.readString();
        this.unlockAt = in.readString();
        this.updatedAt = in.readString();
        this.createdAt = in.readString();
        this.displayName = in.readString();
        this.filename = in.readString();
        this.url = in.readString();
        this.contentType = in.readString();
        this.folderId = in.readLong();
        this.thumbnailUrl = in.readString();
    }

    public static final Creator<DiscussionAttachment> CREATOR = new Creator<DiscussionAttachment>() {
        @Override
        public DiscussionAttachment createFromParcel(Parcel source) {
            return new DiscussionAttachment(source);
        }

        @Override
        public DiscussionAttachment[] newArray(int size) {
            return new DiscussionAttachment[size];
        }
    };

    //endregion
}

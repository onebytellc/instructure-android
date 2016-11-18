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


public class RemoteFile extends CanvasModel<RemoteFile> {

    private long id;

    @SerializedName("folder_id")
    private long folderId;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("filename")
    private String fileName;

    @SerializedName("content-type")
    private String contentType;

    private String url;

    private long size;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("unlock_at")
    private String unlockAt;

    private boolean locked;

    private boolean hidden;

    @SerializedName("lock_at")
    private String lockAt;

    @SerializedName("hidden_for_user")
    private boolean hiddenForUser;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    @SerializedName("modified_at")
    private String modifiedAt;

    @SerializedName("locked_for_user")
    private boolean lockedForUser;

    @SerializedName("preview_url")
    private String previewUrl;

    //region Getters

    @Override
    public long getId() {
        return id;
    }

    public long getFolderId() {
        return folderId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public String getUrl() {
        return url;
    }

    public long getSize() {
        return size;
    }

    public Date getCreatedAt() {
        return APIHelper.stringToDate(createdAt);
    }

    public Date getUpdatedAt() {
        return APIHelper.stringToDate(updatedAt);
    }

    public Date getUnlockAt() {
        return APIHelper.stringToDate(unlockAt);
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isHidden() {
        return hidden;
    }

    public String getLockAt() {
        return lockAt;
    }

    public boolean isHiddenForUser() {
        return hiddenForUser;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Date getModifiedAt() {
        return APIHelper.stringToDate(modifiedAt);
    }

    public boolean isLockedForUser() {
        return lockedForUser;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    //endregion

    //region Setters

    public void setId(long id) {
        this.id = id;
    }

    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUnlockAt(String unlockAt) {
        this.unlockAt = unlockAt;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setLockAt(String lockAt) {
        this.lockAt = lockAt;
    }

    public void setHiddenForUser(boolean hiddenForUser) {
        this.hiddenForUser = hiddenForUser;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setModifiedAt(String modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public void setLockedForUser(boolean lockedForUser) {
        this.lockedForUser = lockedForUser;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    //endregion

    @Nullable
    @Override
    public Date getComparisonDate() {
        return getCreatedAt();
    }

    @Nullable
    @Override
    public String getComparisonString() {
        return getDisplayName();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    //region Parcelable

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.folderId);
        dest.writeString(this.displayName);
        dest.writeString(this.fileName);
        dest.writeString(this.contentType);
        dest.writeString(this.url);
        dest.writeLong(this.size);
        dest.writeString(this.createdAt);
        dest.writeString(this.updatedAt);
        dest.writeString(this.unlockAt);
        dest.writeByte(this.locked ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hidden ? (byte) 1 : (byte) 0);
        dest.writeString(this.lockAt);
        dest.writeByte(this.hiddenForUser ? (byte) 1 : (byte) 0);
        dest.writeString(this.thumbnailUrl);
        dest.writeString(this.modifiedAt);
        dest.writeByte(this.lockedForUser ? (byte) 1 : (byte) 0);
        dest.writeString(this.previewUrl);
    }

    public RemoteFile() {
    }

    protected RemoteFile(Parcel in) {
        this.id = in.readLong();
        this.folderId = in.readLong();
        this.displayName = in.readString();
        this.fileName = in.readString();
        this.contentType= in.readString();
        this.url = in.readString();
        this.size = in.readLong();
        this.createdAt = in.readString();
        this.updatedAt = in.readString();
        this.unlockAt = in.readString();
        this.locked = in.readByte() != 0;
        this.hidden = in.readByte() != 0;
        this.lockAt = in.readString();
        this.hiddenForUser = in.readByte() != 0;
        this.thumbnailUrl = in.readString();
        this.modifiedAt = in.readString();
        this.lockedForUser = in.readByte() != 0;
        this.previewUrl = in.readString();
    }

    public static final Creator<RemoteFile> CREATOR = new Creator<RemoteFile>() {
        @Override
        public RemoteFile createFromParcel(Parcel source) {
            return new RemoteFile(source);
        }

        @Override
        public RemoteFile[] newArray(int size) {
            return new RemoteFile[size];
        }
    };

    //endregion
}

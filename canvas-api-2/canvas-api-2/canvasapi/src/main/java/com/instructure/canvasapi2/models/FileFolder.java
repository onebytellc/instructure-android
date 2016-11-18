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



public class FileFolder extends CanvasModel<FileFolder> {

    // Common Attributes
    private long id;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("updated_at")
    private String updatedAt;
    @SerializedName("unlock_at")
    private String unlockAt;
    @SerializedName("lock_at")
    private String lockAt;
    private boolean locked;
    private boolean hidden;
    @SerializedName("locked_for_user")
    private boolean lockedForUser;
    @SerializedName("hidden_for_user")
    private boolean hiddenForUser;

    // File Attributes
    @SerializedName("folder_id")
    private long folderId;
    private long size;
    @SerializedName("content-type")
    private String contentType;
    private String url;
    @SerializedName("display_name")
    private String displayName;
    @SerializedName("thumbnail_url")
    private String thumbnailUrl;
    @SerializedName("lock_info")
    private LockInfo lockInfo;

    // Folder Attributes
    @SerializedName("parent_folder_id")
    private long parentFolderId;
    @SerializedName("context_id")
    private long contextId;
    @SerializedName("files_count")
    private int filesCount;
    private int position;
    @SerializedName("folders_count")
    private int foldersCount;
    @SerializedName("context_type")
    private String contextType;
    private String name;
    @SerializedName("folders_url")
    private String foldersUrl;
    @SerializedName("files_url")
    private String filesUrl;
    @SerializedName("full_name")
    private String fullName;

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

    public long getFolderId() {
        return folderId;
    }

    public long getSize() {
        return size;
    }

    public String getContentType() {
        return contentType;
    }

    public String getUrl() {
        return url;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public LockInfo getLockInfo() {
        return lockInfo;
    }

    public long getParentFolderId() {
        return parentFolderId;
    }

    public long getContextId() {
        return contextId;
    }

    public int getFilesCount() {
        return filesCount;
    }

    public int getPosition() {
        return position;
    }

    public int getFoldersCount() {
        return foldersCount;
    }

    public String getContextType() {
        return contextType;
    }

    public String getName() {
        return name;
    }

    public String getFoldersUrl() {
        return foldersUrl;
    }

    public String getFilesUrl() {
        return filesUrl;
    }

    public String getFullName() {
        return fullName;
    }

    //endregion

    //region Setters

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setUnlockAt(String unlockAt) {
        this.unlockAt = unlockAt;
    }

    public void setLockAt(String lockAt) {
        this.lockAt = lockAt;
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

    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setLockInfo(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }

    public void setParentFolderId(long parentFolderId) {
        this.parentFolderId = parentFolderId;
    }

    public void setContextId(long contextId) {
        this.contextId = contextId;
    }

    public void setFilesCount(int filesCount) {
        this.filesCount = filesCount;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setFoldersCount(int foldersCount) {
        this.foldersCount = foldersCount;
    }

    public void setContextType(String contextType) {
        this.contextType = contextType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFoldersUrl(String foldersUrl) {
        this.foldersUrl = foldersUrl;
    }

    public void setFilesUrl(String filesUrl) {
        this.filesUrl = filesUrl;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    //endregion

    // Common
    @Override
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public Date getCreatedAt() {
        return APIHelper.stringToDate(createdAt);
    }
    public void setCreatedAt(Date created_at) {
        this.createdAt = APIHelper.dateToString(created_at);
    }
    public Date getUpdatedAt() {
        return APIHelper.stringToDate(updatedAt);
    }
    public void setUpdatedAt(Date updated_at) {
        this.updatedAt = APIHelper.dateToString(updated_at);
    }
    public Date getUnlockAt() {
        return APIHelper.stringToDate(unlockAt);
    }
    public void setUnlockAt(Date unlock_at) {
        this.unlockAt = APIHelper.dateToString(unlock_at);
    }
    public Date getLockAt(){
        return APIHelper.stringToDate(lockAt);
    }
    public void setLockAt(Date lock_at){
        this.lockAt = APIHelper.dateToString(lock_at);
    }

    @Override
    public Date getComparisonDate() { return null;}
    @Override
    public String getComparisonString() {return null;}

    // we override compareTo instead of using Canvas Comparable methods
    @Override
    public int compareTo(FileFolder other) {
        // folders go before files

        // this is a folder and other is a file
        if (getFullName() != null && other.getFullName() == null) {
            return -1;
        } // this is a file and other is a folder
        else if (getFullName() == null && other.getFullName() != null) {
            return 1;
        }
        // both are folders
        if (getFullName() != null && other.getFullName() != null) {
            return getFullName().compareTo(other.getFullName());
        }
        // both are files
        return getDisplayName().compareTo(other.getDisplayName());
    }

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.createdAt);
        dest.writeString(this.updatedAt);
        dest.writeString(this.unlockAt);
        dest.writeString(this.lockAt);
        dest.writeByte(this.locked ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hidden ? (byte) 1 : (byte) 0);
        dest.writeByte(this.lockedForUser ? (byte) 1 : (byte) 0);
        dest.writeByte(this.hiddenForUser ? (byte) 1 : (byte) 0);
        dest.writeLong(this.folderId);
        dest.writeLong(this.size);
        dest.writeString(this.contentType);
        dest.writeString(this.url);
        dest.writeString(this.displayName);
        dest.writeString(this.thumbnailUrl);
        dest.writeParcelable(this.lockInfo, flags);
        dest.writeLong(this.parentFolderId);
        dest.writeLong(this.contextId);
        dest.writeInt(this.filesCount);
        dest.writeInt(this.position);
        dest.writeInt(this.foldersCount);
        dest.writeString(this.contextType);
        dest.writeString(this.name);
        dest.writeString(this.foldersUrl);
        dest.writeString(this.filesUrl);
        dest.writeString(this.fullName);
    }

    public FileFolder() {
    }

    protected FileFolder(Parcel in) {
        this.id = in.readLong();
        this.createdAt = in.readString();
        this.updatedAt = in.readString();
        this.unlockAt = in.readString();
        this.lockAt = in.readString();
        this.locked = in.readByte() != 0;
        this.hidden = in.readByte() != 0;
        this.lockedForUser = in.readByte() != 0;
        this.hiddenForUser = in.readByte() != 0;
        this.folderId = in.readLong();
        this.size = in.readLong();
        this.contentType = in.readString();
        this.url = in.readString();
        this.displayName = in.readString();
        this.thumbnailUrl = in.readString();
        this.lockInfo = in.readParcelable(LockInfo.class.getClassLoader());
        this.parentFolderId = in.readLong();
        this.contextId = in.readLong();
        this.filesCount = in.readInt();
        this.position = in.readInt();
        this.foldersCount = in.readInt();
        this.contextType = in.readString();
        this.name = in.readString();
        this.foldersUrl = in.readString();
        this.filesUrl = in.readString();
        this.fullName = in.readString();
    }

    public static final Creator<FileFolder> CREATOR = new Creator<FileFolder>() {
        @Override
        public FileFolder createFromParcel(Parcel source) {
            return new FileFolder(source);
        }

        @Override
        public FileFolder[] newArray(int size) {
            return new FileFolder[size];
        }
    };

    //endregion
}

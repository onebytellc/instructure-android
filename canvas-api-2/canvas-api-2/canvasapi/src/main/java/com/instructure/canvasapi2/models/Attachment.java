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


public class Attachment extends CanvasModel<Attachment> {

    private long id;
    @SerializedName("content-type")
    private String contentType;
    private String filename;
    @SerializedName("display_name")
    private String displayName;
    private String url;
    @SerializedName("thumbnail_url")
    private String thumbnailUrl;
    @SerializedName("preview_url")
    private String previewUrl;
    @SerializedName("created_at")
    private String createdAt;

    @Override
    public long getId() {
        return id;
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        return getCreatedAt();
    }

    @Nullable
    @Override
    public String getComparisonString() {
        return displayName;
    }

    //region Getter

    public String getContentType() {
        return contentType;
    }

    public String getFilename() {
        return filename;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUrl() {
        return url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public @Nullable Date getCreatedAt() {
        return APIHelper.stringToDate(createdAt);
    }


    //endregion

    //region Setter

    public void setId(long id) {
        this.id = id;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
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
        dest.writeString(this.contentType);
        dest.writeString(this.filename);
        dest.writeString(this.displayName);
        dest.writeString(this.url);
        dest.writeString(this.thumbnailUrl);
        dest.writeString(this.previewUrl);
        dest.writeString(this.createdAt);
    }

    public Attachment() {
    }

    protected Attachment(Parcel in) {
        this.id = in.readLong();
        this.contentType = in.readString();
        this.filename = in.readString();
        this.displayName = in.readString();
        this.url = in.readString();
        this.thumbnailUrl = in.readString();
        this.previewUrl = in.readString();
        this.createdAt = in.readString();
    }

    public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
        @Override
        public Attachment createFromParcel(Parcel source) {
            return new Attachment(source);
        }

        @Override
        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };

    //endregion
}

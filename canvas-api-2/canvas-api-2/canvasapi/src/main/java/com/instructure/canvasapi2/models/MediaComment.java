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
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.instructure.canvasapi2.utils.FileUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MediaComment implements Parcelable {

    public enum MediaType { AUDIO, VIDEO }

    @SerializedName("media_id")
    private String mediaId;
    @SerializedName("display_name")
    private String displayName;
    private String url;
    @SerializedName("media_type")
    private String mediaType;

    @SerializedName("content-type")
    private String contentType;

    //region Getters

    public String getMediaId() {
        return mediaId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUrl() {
        return url;
    }

    public String getContentType() {
        return contentType;
    }

    public MediaType getMediaType() {
        if("video".equals(mediaType)) {
            return MediaType.VIDEO;
        } else {
            return MediaType.AUDIO;
        }
    }

    public String getFileName(){
        if(mediaId == null || url == null){
            return null;
        }

        String[] split = url.split("=");
        return mediaId + "."+split[split.length-1];
    }

    //endregion

    //region Setters

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName(@NonNull Date createdAt) {
        if(displayName == null || displayName.equals("null")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMM yyyy", Locale.ENGLISH);
            return dateFormat.format(createdAt) + "." + FileUtils.getFileExtensionFromMimetype(contentType);
        } else {
            return displayName;
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    //endregion

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mediaId);
        dest.writeString(this.displayName);
        dest.writeString(this.url);
        dest.writeString(this.mediaType);
        dest.writeString(this.contentType);
    }

    public MediaComment() {
    }

    protected MediaComment(Parcel in) {
        this.mediaId = in.readString();
        this.displayName = in.readString();
        this.url = in.readString();
        this.mediaType = in.readString();
        this.contentType = in.readString();
    }

    public static final Parcelable.Creator<MediaComment> CREATOR = new Parcelable.Creator<MediaComment>() {
        @Override
        public MediaComment createFromParcel(Parcel source) {
            return new MediaComment(source);
        }

        @Override
        public MediaComment[] newArray(int size) {
            return new MediaComment[size];
        }
    };

    //endregion
}

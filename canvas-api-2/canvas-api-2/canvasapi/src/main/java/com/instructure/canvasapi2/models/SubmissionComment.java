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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class SubmissionComment extends CanvasComparable<SubmissionComment> {

    @SerializedName("author_id")
    private long authorId;
    @SerializedName("author_name")
    private String authorName;
    private String comment;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("media_comment")
    private MediaComment mediaComment;
    private List<Attachment> attachments = new ArrayList<>();
    private Author author;


    @Override
    public long getId() {
        return authorId;
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        return getCreatedAt();
    }

    @Nullable
    @Override
    public String getComparisonString() {
        return authorName;
    }

    //region Getters

    public long getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getComment() {
        return comment;
    }

    public @Nullable Date getCreatedAt() {
        return APIHelper.stringToDate(createdAt);
    }

    public MediaComment getMediaComment() {
        return mediaComment;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public Author getAuthor() {
        return author;
    }

    //endregion

    //region Setters

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setMediaComment(MediaComment mediaComment) {
        this.mediaComment = mediaComment;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    //endregion

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.authorId);
        dest.writeString(this.authorName);
        dest.writeString(this.comment);
        dest.writeString(this.createdAt);
        dest.writeParcelable(this.mediaComment, flags);
        dest.writeTypedList(attachments);
        dest.writeParcelable(this.author, flags);
    }

    public SubmissionComment() {
    }

    protected SubmissionComment(Parcel in) {
        this.authorId = in.readLong();
        this.authorName = in.readString();
        this.comment = in.readString();
        this.createdAt = in.readString();
        this.mediaComment = in.readParcelable(MediaComment.class.getClassLoader());
        this.attachments = in.createTypedArrayList(Attachment.CREATOR);
        this.author = in.readParcelable(Author.class.getClassLoader());
    }

    public static final Creator<SubmissionComment> CREATOR = new Creator<SubmissionComment>() {
        @Override
        public SubmissionComment createFromParcel(Parcel source) {
            return new SubmissionComment(source);
        }

        @Override
        public SubmissionComment[] newArray(int size) {
            return new SubmissionComment[size];
        }
    };

    //endregion
}

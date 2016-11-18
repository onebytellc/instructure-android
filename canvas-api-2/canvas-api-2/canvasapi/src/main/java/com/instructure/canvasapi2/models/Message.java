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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message extends CanvasModel<Message> {

    private long id;

    @SerializedName("created_at")
    private String createdAt;
    private String body;
    @SerializedName("author_id")
    private long authorId;
    private boolean generated;
    private List<Attachment> attachments = new ArrayList<>();
    @SerializedName("media_comment")
    private MediaComment mediaComment;
    private Submission submission;
    @SerializedName("forwarded_messages")
    private List<Message> forwardedMessages = new ArrayList<>();
    @SerializedName("participating_user_ids")
    private List<Long> participatingUserIds = new ArrayList<>();

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public MediaComment getMediaComment() {
        return mediaComment;
    }

    public void setMediaComment(MediaComment mediaComment) {
        this.mediaComment = mediaComment;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public List<Message> getForwardedMessages() {
        return forwardedMessages;
    }

    public void setForwardedMessages(List<Message> forwardedMessages) {
        this.forwardedMessages = forwardedMessages;
    }

    public List<Long> getParticipatingUserIds() {
        return participatingUserIds;
    }

    public void setParticipatingUserIds(List<Long> participatingUserIds) {
        this.participatingUserIds = participatingUserIds;
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
        dest.writeString(this.createdAt);
        dest.writeString(this.body);
        dest.writeLong(this.authorId);
        dest.writeByte(generated ? (byte) 1 : (byte) 0);
        dest.writeTypedList(attachments);
        dest.writeParcelable(this.mediaComment, flags);
        dest.writeParcelable(this.submission, flags);
        dest.writeTypedList(forwardedMessages);
        dest.writeList(this.participatingUserIds);
    }

    public Message() {
    }

    protected Message(Parcel in) {
        this.id = in.readLong();
        this.createdAt = in.readString();
        this.body = in.readString();
        this.authorId = in.readLong();
        this.generated = in.readByte() != 0;
        this.attachments = in.createTypedArrayList(Attachment.CREATOR);
        this.mediaComment = in.readParcelable(MediaComment.class.getClassLoader());
        this.submission = in.readParcelable(Submission.class.getClassLoader());
        this.forwardedMessages = in.createTypedArrayList(Message.CREATOR);
        this.participatingUserIds = new ArrayList<Long>();
        in.readList(this.participatingUserIds, Long.class.getClassLoader());
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel source) {
            return new Message(source);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}

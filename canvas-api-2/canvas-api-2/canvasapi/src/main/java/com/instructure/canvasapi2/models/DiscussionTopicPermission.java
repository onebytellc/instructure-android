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

import java.util.Date;


public class DiscussionTopicPermission extends CanvasModel<DiscussionTopicPermission> {

    private boolean attach = false;
    private boolean update = false;
    private boolean delete = false;
    private boolean reply = false;

    @Override
    public long getId() {
        return 0;
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return null;
    }

    //region Getters

    public boolean isAttach() {
        return attach;
    }

    public boolean isUpdate() {
        return update;
    }

    public boolean isDelete() {
        return delete;
    }

    public boolean canReply() {
        return reply;
    }

    //endregion

    //region Setters

    public void setAttach(boolean attach) {
        this.attach = attach;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public void setCanReply(boolean canReply) {
        this.reply = canReply;
    }

    //endregion

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(attach ? (byte) 1 : (byte) 0);
        dest.writeByte(update ? (byte) 1 : (byte) 0);
        dest.writeByte(delete ? (byte) 1 : (byte) 0);
        dest.writeByte(reply ? (byte) 1 : (byte) 0);
    }

    public DiscussionTopicPermission() {
    }

    protected DiscussionTopicPermission(Parcel in) {
        this.attach = in.readByte() != 0;
        this.update = in.readByte() != 0;
        this.delete = in.readByte() != 0;
        this.reply = in.readByte() != 0;
    }

    public static final Creator<DiscussionTopicPermission> CREATOR = new Creator<DiscussionTopicPermission>() {
        @Override
        public DiscussionTopicPermission createFromParcel(Parcel source) {
            return new DiscussionTopicPermission(source);
        }

        @Override
        public DiscussionTopicPermission[] newArray(int size) {
            return new DiscussionTopicPermission[size];
        }
    };

    //endregion
}

/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.candroid.model;

import android.content.Context;
import android.os.Parcel;
import com.instructure.candroid.interfaces.OpenableMedia;
import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.CanvasComparable;
import com.instructure.canvasapi.model.MediaComment;
import com.instructure.canvasapi.model.Message;
import com.instructure.canvasapi.utilities.APIHelpers;
import java.util.Date;

public class MessageAttachment extends CanvasComparable<MessageWithDepth> implements OpenableMedia {

    boolean isAttachment;
    private Attachment attachment;
    private MediaComment mediaComment;
    private Message parentMessage;
    private int order; // order attachment was added ?? not sure if there's a better way to do this.

    public MessageAttachment(Message msg, Attachment attachment, int order) {
        this.attachment = attachment;
        this.isAttachment = true;
        this.parentMessage = msg;
        this.order = order;
    }

    public MessageAttachment(Message msg, MediaComment mediaComment, int order){
        this.mediaComment = mediaComment;
        this.isAttachment = false;
        this.parentMessage = msg;
        this.order = order;
    }

    // Getters
    public long getId(){
        if(isAttachment){
            return attachment.getId();
        }else{
            return mediaComment.getId();
        }
    }

    public boolean isAttachment(){
        return isAttachment;
    }

    public MediaComment getMediaComment() {
        return mediaComment;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public int getOrder() {
        return order;
    }

    public String getThumbnailUrl(Context context) {
        if(isAttachment){
            return attachment.getThumbnailUrl();
        } else {
            return APIHelpers.getFullDomain(context) + "/media_objects/" + mediaComment.getMediaId() + "/thumbnail?height=180&width=180";
        }
    }

    // Openable implementations
    @Override
    public String getMimeType() {
        if(isAttachment){
            return attachment.getMimeType();
        }else{
            return mediaComment.getMimeType();
        }
    }

    @Override
    public String getUrl() {
        if(isAttachment){
            return attachment.getUrl();
        }else{
            return mediaComment.getUrl();
        }
    }

    @Override
    public String getImageUrl(Context context){
        if(isAttachment){
            return attachment.getUrl();
        }else{
            return APIHelpers.getFullDomain(context) + "/media_objects/" + mediaComment.getMediaId() + "/thumbnail?height=480&width=480";
        }
    }

    @Override
    public String getFileName() {
        if(isAttachment){
            return attachment.getFilename();
        }else{
            return mediaComment.getFileName();
        }
    }

    @Override
    public Date getComparisonDate() {
        return parentMessage.getComparisonDate();
    }

    @Override
    public String getComparisonString() {
        if(isAttachment()){
            return attachment.getComparisonString();
        }else{
            return mediaComment.getComparisonString();
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isAttachment ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.attachment, flags);
        dest.writeParcelable(this.mediaComment, flags);
        dest.writeParcelable(this.parentMessage, flags);
        dest.writeInt(this.order);
    }

    protected MessageAttachment(Parcel in) {
        this.isAttachment = in.readByte() != 0;
        this.attachment = in.readParcelable(Attachment.class.getClassLoader());
        this.mediaComment = in.readParcelable(MediaComment.class.getClassLoader());
        this.parentMessage = in.readParcelable(Message.class.getClassLoader());
        this.order = in.readInt();
    }

    public static final Creator<MessageAttachment> CREATOR = new Creator<MessageAttachment>() {
        @Override
        public MessageAttachment createFromParcel(Parcel source) {
            return new MessageAttachment(source);
        }

        @Override
        public MessageAttachment[] newArray(int size) {
            return new MessageAttachment[size];
        }
    };
}

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


public class Conversation extends CanvasModel<Conversation> {

    public enum WorkflowState {READ, UNREAD, ARCHIVED, UNKNOWN}

    private long id;                        // The unique id for the conversation.
    private String subject;                 // Message Subject

    @SerializedName("workflow_state")
    private String workflowState;          // The workflowState of the conversation (unread, read, archived)
    @SerializedName("last_message")
    private String lastMessage;            // 100 character preview of the last message.

    @SerializedName("last_message_at")
    private String lastMessageAt;         // Date of the last message sent.
    @SerializedName("last_authored_message_at")
    private String lastAuthoredMessageAt;
    @SerializedName("message_count")
    private int messageCount;              // Number of messages in the conversation.

    private boolean subscribed;             // Whether or not the user is subscribed to the current message.
    private boolean starred;                // Whether or not the message is starred.

    private List<String> properties = new ArrayList<String>();

    @SerializedName("avatar_url")
    private String avatarUrl;          // The avatar to display. Knows if group, user, etc.
    private boolean visible;            // Whether this conversation is visible in the current context. Not 100% what that means.

    // The IDs of all people in the conversation. EXCLUDING the current user unless it's a monologue.
    private List<Long> audience = new ArrayList<Long>();
    //TODO: Audience contexts.

    // The name and IDs of all participants in the conversation.
    private List<BasicUser> participants = new ArrayList<BasicUser>();

    // Messages attached to the conversation.
    private List<Message> messages = new ArrayList<Message>();

    @SerializedName("context_name")
    private String contextName;
    @SerializedName("context_code")
    private String contextCode;

    // helper variables
    private Date lastMessageDate;
    private boolean deleted = false; 	// Used to set whether or not we've determined it to be deleted with a failed retrofit call.
    private String deletedString = "";	// The string to show if something is deleted.


    public void setId(long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public WorkflowState getWorkflowState() {
        if ("unread".equalsIgnoreCase(workflowState)) {
            return WorkflowState.UNREAD;
        } else if ("archived".equalsIgnoreCase(workflowState)) {
            return WorkflowState.ARCHIVED;
        } else if ("read".equalsIgnoreCase(workflowState)) {
            return  WorkflowState.READ;
        } else {
            return WorkflowState.UNKNOWN;
        }
    }

    public void setWorkflowState(String workflowState) {
        this.workflowState = workflowState;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(String lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public String getLastAuthoredMessageAt() {
        return lastAuthoredMessageAt;
    }

    public void setLastAuthoredMessageAt(String lastAuthoredMessageAt) {
        this.lastAuthoredMessageAt = lastAuthoredMessageAt;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public String getContextName() {
        return contextName;
    }

    public String getContextCode() {
        return contextCode;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    public boolean isStarred() {
        return starred;
    }

    public void setStarred(boolean starred) {
        this.starred = starred;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public List<Long> getAudience() {
        return audience;
    }

    public void setAudience(List<Long> audience) {
        this.audience = audience;
    }

    public List<BasicUser> getParticipants() {
        return participants;
    }

    public void setParticipants(List<BasicUser> participants) {
        this.participants = participants;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Date getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(Date lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getDeletedString() {
        return deletedString;
    }

    public void setDeletedString(String deletedString) {
        this.deletedString = deletedString;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public void setContextCode(String contextCode) {
        this.contextCode = contextCode;
    }

    public boolean isMonologue (long myUserID) {
        return determineMonologue(myUserID);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    public boolean hasAttachments() {
        for(int i = 0; i < properties.size(); i++)
        {
            if(properties.get(i).equals("attachments")){
                return true;
            }
        }
        return false;
    }

    public boolean hasMedia() {
        for(int i = 0; i < properties.size(); i++)
        {
            if(properties.get(i).equals("media_objects")){
                return true;
            }
        }
        return false;
    }

    public String getLastMessagePreview() {
        if(deleted){
            return deletedString;
        }
        return lastMessage;
    }

    private boolean determineMonologue(long userID) {
        if(audience == null){
            return false;
        } else if (audience.size() == 0){
            return true;
        }

        for(int i = 0; i < audience.size(); i++){
            if(audience.get(i) == userID){
                return true;
            }
        }
        return false;
    }

    public Date getLastMessageSent() {
        if (lastMessageDate == null) {
            lastMessageDate = APIHelper.stringToDate(lastMessageAt);
        }
        return lastMessageDate;
    }

    public Date getLastAuthoredMessageSent() {
        Date lastAuthoredDate = null;
        if (lastAuthoredMessageAt != null) {
            lastAuthoredDate = APIHelper.stringToDate(lastAuthoredMessageAt);
        }
        return lastAuthoredDate;
    }

    @Nullable
    @Override
    public String getComparisonString() {
        return null;
    }

    @Nullable
    // We want opposite of natural sorting order of date since we want the newest one to come first
    @Override
    public Date getComparisonDate() {
        //sent messages have a last_authored_message_at that other messages won't. In that case last_message_at can be null,
        //but last_authored_message isn't
        if(lastMessageAt != null) {
            return getLastMessageSent();
        }
        else {
            return getLastAuthoredMessageSent();
        }
    }

    @Override
    public long getId() {
        return id;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.subject);
        dest.writeString(this.workflowState);
        dest.writeString(this.lastMessage);
        dest.writeString(this.lastMessageAt);
        dest.writeString(this.lastAuthoredMessageAt);
        dest.writeInt(this.messageCount);
        dest.writeByte(this.subscribed ? (byte) 1 : (byte) 0);
        dest.writeByte(this.starred ? (byte) 1 : (byte) 0);
        dest.writeStringList(this.properties);
        dest.writeString(this.avatarUrl);
        dest.writeByte(this.visible ? (byte) 1 : (byte) 0);
        dest.writeList(this.audience);
        dest.writeTypedList(this.participants);
        dest.writeTypedList(this.messages);
        dest.writeLong(this.lastMessageDate != null ? this.lastMessageDate.getTime() : -1);
        dest.writeByte(this.deleted ? (byte) 1 : (byte) 0);
        dest.writeString(this.deletedString);
        dest.writeString(this.contextName);
        dest.writeString(this.contextCode);
    }

    public Conversation() {
    }

    protected Conversation(Parcel in) {
        this.id = in.readLong();
        this.subject = in.readString();
        this.workflowState = in.readString();
        this.lastMessage = in.readString();
        this.lastMessageAt = in.readString();
        this.lastAuthoredMessageAt = in.readString();
        this.messageCount = in.readInt();
        this.subscribed = in.readByte() != 0;
        this.starred = in.readByte() != 0;
        this.properties = in.createStringArrayList();
        this.avatarUrl = in.readString();
        this.visible = in.readByte() != 0;
        this.audience = new ArrayList<Long>();
        in.readList(this.audience, Long.class.getClassLoader());
        this.participants = in.createTypedArrayList(BasicUser.CREATOR);
        this.messages = in.createTypedArrayList(Message.CREATOR);
        long tmpLastMessageDate = in.readLong();
        this.lastMessageDate = tmpLastMessageDate == -1 ? null : new Date(tmpLastMessageDate);
        this.deleted = in.readByte() != 0;
        this.deletedString = in.readString();
        this.contextName = in.readString();
        this.contextCode = in.readString();
    }

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel source) {
            return new Conversation(source);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };
}

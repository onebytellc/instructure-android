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

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DiscussionTopic implements Parcelable {

    //The user can't see it unless they post a high level reply (requireinitialpost).
    private boolean forbidden = false;

    //List of all the ids of the unread discussion entries.
    private List<Long> unread_entries = new ArrayList<>();

    //List of the participants.
    private List<DiscussionParticipant> participants = new ArrayList<>();
    private HashMap<Long, DiscussionParticipant> participantsMap = new HashMap<>();
    @SerializedName("unread_entriesMap")
    private HashMap<Long, Boolean> unreadEntriesMap = new HashMap<>();
    @SerializedName("entry_ratings")
    private HashMap<Long, Integer> entryRatings = new HashMap<>();

    //List of all the discussion entries (views)
    private List<DiscussionEntry> view = new ArrayList<>();

    //region Getters

    public boolean isForbidden() {
        return forbidden;
    }

    public List<Long> getUnreadEntries() {
        return unread_entries;
    }

    //This should only have to get built once.
    //    //MUCH faster for lookups.
    //So instead of n linear operations, we have 1 linear operations and (n-1) constant ones.
    public HashMap<Long,Boolean> getUnreadEntriesMap(){
        if (getUnreadEntries().size() != unreadEntriesMap.size()) {
            for (Long unreadEntry : getUnreadEntries()) {
                unreadEntriesMap.put(unreadEntry, true);
            }
        }
        return unreadEntriesMap;
    }

    //This should only have to get built once.
    //MUCH faster for lookups.
    //So instead of n linear operations, we have 1 linear operations and (n-1) constant ones.
    public HashMap<Long,DiscussionParticipant> getParticipantsMap(){
        if(participantsMap == null || participantsMap.isEmpty()){
            participantsMap = new HashMap<>();
            if(participants != null){
                for(DiscussionParticipant discussionParticipant : participants){
                    participantsMap.put(discussionParticipant.getId(), discussionParticipant);
                }
            }
        }
        return participantsMap;
    }

    public static String getDiscussionURL(String api_protocol,String domain, long courseId, long topicId) {
        //https://mobiledev.instructure.com/api/v1/courses/24219/discussion_topics/1129998/
        return api_protocol + "://" + domain + "/courses/"+courseId+"/discussion_topics/"+topicId;
    }

    public List<DiscussionParticipant> getParticipants() {
        return participants;
    }

    public List<DiscussionEntry> getViews() {
        return view;
    }

    public HashMap<Long, Integer> getEntryRatings() {
        return entryRatings;
    }

    //endregion

    //region Setters

    public void setViews(List<DiscussionEntry> views) {
        this.view = views;
    }

    public void setUnreadEntries(List<Long> unread_entries) {
        this.unread_entries = unread_entries;
    }

    public void setParticipants(List<DiscussionParticipant> participants) {
        this.participants = participants;
    }

    public void setEntryRatings(HashMap<Long, Integer> entryRatings) {
        this.entryRatings = entryRatings;
    }

    public void setForbidden(boolean forbidden) {
        this.forbidden = forbidden;
    }

    //endregion

    //region Parcelable

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(forbidden ? (byte) 1 : (byte) 0);
        dest.writeList(this.unread_entries);
        dest.writeTypedList(participants);
        dest.writeSerializable(this.participantsMap);
        dest.writeSerializable(this.unreadEntriesMap);
        dest.writeSerializable(this.entryRatings);
        dest.writeTypedList(view);
    }

    public DiscussionTopic() {
    }

    protected DiscussionTopic(Parcel in) {
        this.forbidden = in.readByte() != 0;
        this.unread_entries = new ArrayList<Long>();
        in.readList(this.unread_entries, Long.class.getClassLoader());
        this.participants = in.createTypedArrayList(DiscussionParticipant.CREATOR);
        this.participantsMap = (HashMap<Long, DiscussionParticipant>) in.readSerializable();
        this.unreadEntriesMap = (HashMap<Long, Boolean>) in.readSerializable();
        this.entryRatings = (HashMap<Long, Integer>) in.readSerializable();
        this.view = in.createTypedArrayList(DiscussionEntry.CREATOR);
    }

    public static final Parcelable.Creator<DiscussionTopic> CREATOR = new Parcelable.Creator<DiscussionTopic>() {
        @Override
        public DiscussionTopic createFromParcel(Parcel source) {
            return new DiscussionTopic(source);
        }

        @Override
        public DiscussionTopic[] newArray(int size) {
            return new DiscussionTopic[size];
        }
    };

    //endregion
}

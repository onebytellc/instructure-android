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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.instructure.canvasapi2.utils.APIHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ScheduleItem extends CanvasModel<ScheduleItem> {

    public enum Type { TYPE_ASSIGNMENT, TYPE_CALENDAR, TYPE_SYLLABUS }

    // from api
    private String id;
    private String title;
    private String description;
    @SerializedName("start_at")
    private String startAt;
    @SerializedName("end_at")
    private String endAt;
    @SerializedName("all_day")
    private boolean allDay;
    @SerializedName("all_day_date")
    private String allDayDate;
    @SerializedName("location_address")
    private String locationAddress;
    @SerializedName("location_name")
    private String locationName;
    @SerializedName("html_url")
    private String htmlUrl;
    @SerializedName("context_code")
    private String contextCode;
    @SerializedName("effective_context_code")
    private String effectiveContextCode;
    private boolean hidden;
    @SerializedName("assignment_overrides")
    private List<AssignmentOverride> assignmentOverrides;

    // helper variables
    private CanvasContext.Type contextType;
    private long userId = -1;
    private String userName;
    private long courseId = -1;
    private long groupId = -1;
    private Type itemType = Type.TYPE_CALENDAR;

    private List<Assignment.SUBMISSION_TYPE> submissionTypes = new ArrayList<>();
    private double pointsPossible;
    private long quizId = 0;
    private DiscussionTopicHeader discussionTopicHeader;
    private String lockedModuleName;
    private Assignment assignment;

    @Override
    public long getId() {
        //id can either be a regular long, or it could be prefixed by "assignment_".
        //for more info check out the upcoming_events api documentation
        try {
            return Long.parseLong(id);
        }
        catch(NumberFormatException e) {
            if(assignmentOverrides != null && !assignmentOverrides.isEmpty()) {
                long id = assignmentOverrides.get(0).id;
                setId(id);
                return id;
            } else {
                //it's a string with assignment_ as a prefix...hopefully
                try {
                    String stringId = id;
                    String tempId = stringId.replace("assignment_", "");
                    long assignmentId = Long.parseLong(tempId);
                    setId(assignmentId);
                    return assignmentId;
                } catch (Exception e1) {
                    setId(-1L);
                    return -1L;
                }
            }
        }
        catch(Exception e) {
            setId(-1L);
            return -1L;
        }
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        return getStartAt();
    }

    @Nullable
    @Override
    public String getComparisonString() {
        return title;
    }

    //region Getters

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Nullable
    public Date getStartAt() {
        return APIHelper.stringToDate(startAt);
    }

    @Nullable
    public Date getEndAt() {
        return APIHelper.stringToDate(endAt);
    }

    public boolean isAllDay() {
        return allDay;
    }

    @Nullable
    public Date getAllDayDate() {
        if(TextUtils.isEmpty(allDayDate)) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(allDayDate);
        } catch (Exception e) {
            return null;
        }
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public String getContextCode() {
        return contextCode;
    }

    public String getEffectiveContextCode() {
        return effectiveContextCode;
    }

    public boolean isHidden() {
        return hidden;
    }

    public CanvasContext.Type getContextType() {
        if(contextCode == null) {
            contextType = CanvasContext.Type.USER;
        } else if (contextType == null) {
            parseContextCode();
        }

        return contextType;
    }

    public long getUserId() {
        if (userId < 0) {
            parseContextCode();
        }
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public long getCourseId() {
        if (courseId < 0) {
            parseContextCode();
        }
        return courseId;
    }

    public long getGroupId() {
        return groupId;
    }

    public Type getItemType() {
        return itemType;
    }

    public List<Assignment.SUBMISSION_TYPE> getSubmissionTypes() {
        return submissionTypes;
    }

    public double getPointsPossible() {
        return pointsPossible;
    }

    public long getQuizId() {
        return quizId;
    }

    public DiscussionTopicHeader getDiscussionTopicHeader() {
        return discussionTopicHeader;
    }

    public String getLockedModuleName() {
        return lockedModuleName;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    @NonNull
    public List<AssignmentOverride> getAssignmentOverrides() {
        if(assignmentOverrides == null) {
            assignmentOverrides = new ArrayList<>();
        }
        return assignmentOverrides;
    }

    public boolean hasAssignmentOverrides() {
        return assignmentOverrides != null && !assignmentOverrides.isEmpty();
    }

    //endregion

    //region Setters

    public void setId(long id) {
        this.id = Long.toString(id);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStartAt(Date startAt) {
        this.startAt = APIHelper.dateToString(startAt);
    }

    public void setEndAt(Date endAt) {
        this.endAt = APIHelper.dateToString(endAt);
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public void setAllDayDate(Date allDayDate) {
        this.allDayDate = APIHelper.dateToString(allDayDate);
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public void setContextCode(String contextCode) {
        this.contextCode = contextCode;
    }

    public void setEffectiveContextCode(String effectiveContextCode) {
        this.effectiveContextCode = effectiveContextCode;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void setContextType(CanvasContext.Type contextType) {
        this.contextType = contextType;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setCourseId(long courseId) {
        this.courseId = courseId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public void setItemType(Type itemType) {
        this.itemType = itemType;
    }

    public void setSubmissionTypes(List<Assignment.SUBMISSION_TYPE> submissionTypes) {
        this.submissionTypes = submissionTypes;
    }

    public void setPointsPossible(double pointsPossible) {
        this.pointsPossible = pointsPossible;
    }

    public void setQuizId(long quizId) {
        this.quizId = quizId;
    }

    public void setDiscussionTopicHeader(DiscussionTopicHeader discussionTopicHeader) {
        this.discussionTopicHeader = discussionTopicHeader;
    }

    public void setLockedModuleName(String lockedModuleName) {
        this.lockedModuleName = lockedModuleName;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    //endregion

    public long getContextId() {
        switch (getContextType()) {
            case COURSE:
                return getCourseId();
            case GROUP:
                return getGroupId();
            case USER:
                return getUserId();
            default:
                return -1;
        }
    }

    private void parseContextCode() {
        if (effectiveContextCode != null) {
            parseContextCode(effectiveContextCode);
        } else {
            parseContextCode(contextCode);
        }
    }

    private void parseContextCode(String contextCode) {
        if (contextCode.startsWith("user_")) {
            setContextType(CanvasContext.Type.USER);
            String userId = contextCode.replace("user_", "");
            setUserId(Long.parseLong(userId));
        } else if (contextCode.startsWith("course_")) {
            setContextType(CanvasContext.Type.COURSE);
            String courseId = contextCode.replace("course_", "");
            setCourseId(Long.parseLong(courseId));
        } else if (contextCode.startsWith("group_")) {
            setContextType(CanvasContext.Type.GROUP);
            String groupId = contextCode.replace("group_", "");
            setGroupId(Long.parseLong(groupId));
        }
    }


    //region Parcelable

    public ScheduleItem() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.startAt);
        dest.writeString(this.endAt);
        dest.writeByte(this.allDay ? (byte) 1 : (byte) 0);
        dest.writeString(this.allDayDate);
        dest.writeString(this.locationAddress);
        dest.writeString(this.locationName);
        dest.writeString(this.htmlUrl);
        dest.writeString(this.contextCode);
        dest.writeString(this.effectiveContextCode);
        dest.writeByte(this.hidden ? (byte) 1 : (byte) 0);
        dest.writeTypedList(this.assignmentOverrides);
        dest.writeInt(this.contextType == null ? -1 : this.contextType.ordinal());
        dest.writeLong(this.userId);
        dest.writeString(this.userName);
        dest.writeLong(this.courseId);
        dest.writeLong(this.groupId);
        dest.writeInt(this.itemType == null ? -1 : this.itemType.ordinal());
        dest.writeList(this.submissionTypes);
        dest.writeDouble(this.pointsPossible);
        dest.writeLong(this.quizId);
        dest.writeParcelable(this.discussionTopicHeader, flags);
        dest.writeString(this.lockedModuleName);
        dest.writeParcelable(this.assignment, flags);
    }

    protected ScheduleItem(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.startAt = in.readString();
        this.endAt = in.readString();
        this.allDay = in.readByte() != 0;
        this.allDayDate = in.readString();
        this.locationAddress = in.readString();
        this.locationName = in.readString();
        this.htmlUrl = in.readString();
        this.contextCode = in.readString();
        this.effectiveContextCode = in.readString();
        this.hidden = in.readByte() != 0;
        this.assignmentOverrides = in.createTypedArrayList(AssignmentOverride.CREATOR);
        int tmpContextType = in.readInt();
        this.contextType = tmpContextType == -1 ? null : CanvasContext.Type.values()[tmpContextType];
        this.userId = in.readLong();
        this.userName = in.readString();
        this.courseId = in.readLong();
        this.groupId = in.readLong();
        int tmpItemType = in.readInt();
        this.itemType = tmpItemType == -1 ? null : Type.values()[tmpItemType];
        this.submissionTypes = new ArrayList<Assignment.SUBMISSION_TYPE>();
        in.readList(this.submissionTypes, Assignment.SUBMISSION_TYPE.class.getClassLoader());
        this.pointsPossible = in.readDouble();
        this.quizId = in.readLong();
        this.discussionTopicHeader = in.readParcelable(DiscussionTopicHeader.class.getClassLoader());
        this.lockedModuleName = in.readString();
        this.assignment = in.readParcelable(Assignment.class.getClassLoader());
    }

    public static final Creator<ScheduleItem> CREATOR = new Creator<ScheduleItem>() {
        @Override
        public ScheduleItem createFromParcel(Parcel source) {
            return new ScheduleItem(source);
        }

        @Override
        public ScheduleItem[] newArray(int size) {
            return new ScheduleItem[size];
        }
    };

    //endregion
}

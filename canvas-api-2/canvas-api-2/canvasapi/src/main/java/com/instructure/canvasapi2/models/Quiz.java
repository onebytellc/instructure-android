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


public class Quiz extends CanvasModel<Quiz> {

    public final static String TYPE_PRACTICE = "practice_quiz";
    public final static String TYPE_ASSIGNMENT = "assignment";
    public final static String TYPE_GRADED_SURVEY = "graded_survey";
    public final static String TYPE_SURVEY = "survey";

    public enum HIDE_RESULTS_TYPE { NULL, ALWAYS, AFTER_LAST_ATTEMPT }
    // API variables

    private long id;
    private String title;
    @SerializedName("mobile_url")
    private String mobileUrl;
    @SerializedName("html_url")
    private String htmlUrl;

    private String description;
    @SerializedName("quiz_type")
    private String quizType;
    @SerializedName("lock_info")
    private LockInfo lockInfo;
    private QuizPermission permissions;
    @SerializedName("allowed_attempts")
    private int allowedAttempts;
    @SerializedName("question_count")
    private int questionCount;
    @SerializedName("points_possible")
    private String pointsPossible;
    @SerializedName("due_at")
    private String dueAt;
    @SerializedName("time_limit")
    private int timeLimit;
    @SerializedName("access_code")
    private String accessCode;
    @SerializedName("ip_filter")
    private String ipFilter;
    @SerializedName("locked_for_user")
    private boolean lockedForUser;
    @SerializedName("lock_explanation")
    private String lockExplanation;
    @SerializedName("hide_results")
    private String hideResults;
    @SerializedName("unlock_at")
    private String unlockAt;
    @SerializedName("one_time_results")
    private boolean oneTimeResults;
    @SerializedName("lock_at")
    private String lockAt;
    @SerializedName("question_types")
    private List<String> questionTypes = new ArrayList<>();
    @SerializedName("has_access_code")
    private boolean hasAccessCode;
    @SerializedName("one_question_at_a_time")
    private boolean oneQuestionAtATime;
    @SerializedName("require_lockdown_broswer")
    private boolean requireLockdownBrowser;
    @SerializedName("require_lockdown_browser_for_results")
    private boolean requireLockdownBrowserForResults;

    // Helper variables

    private Assignment assignment;

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMobileUrl() {
        return mobileUrl;
    }

    public void setMobileUrl(String mobileUrl) {
        this.mobileUrl = mobileUrl;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQuizType() {
        return quizType;
    }

    public void setQuizType(String quizType) {
        this.quizType = quizType;
    }

    public LockInfo getLockInfo() {
        return lockInfo;
    }

    public void setLockInfo(LockInfo lockInfo) {
        this.lockInfo = lockInfo;
    }

    public QuizPermission getPermissions() {
        return permissions;
    }

    public void setPermissions(QuizPermission permissions) {
        this.permissions = permissions;
    }

    public int getAllowedAttempts() {
        return allowedAttempts;
    }

    public void setAllowedAttempts(int allowedAttempts) {
        this.allowedAttempts = allowedAttempts;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public String getPointsPossible() {
        return pointsPossible;
    }

    public void setPointsPossible(String pointsPossible) {
        this.pointsPossible = pointsPossible;
    }

    public String getDueAt() {
        return dueAt;
    }

    public void setDueAt(String dueAt) {
        this.dueAt = dueAt;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getIpFilter() {
        return ipFilter;
    }

    public void setIpFilter(String ipFilter) {
        this.ipFilter = ipFilter;
    }

    public boolean isLockedForUser() {
        return lockedForUser;
    }

    public void setLockedForUser(boolean lockedForUser) {
        this.lockedForUser = lockedForUser;
    }

    public String getLockExplanation() {
        return lockExplanation;
    }

    public void setLockExplanation(String lockExplanation) {
        this.lockExplanation = lockExplanation;
    }

    public String getHideResults() {
        return hideResults;
    }

    public void setHideResults(String hideResults) {
        this.hideResults = hideResults;
    }

    public String getUnlockAt() {
        return unlockAt;
    }

    public void setUnlockAt(String unlockAt) {
        this.unlockAt = unlockAt;
    }

    public boolean isOneTimeResults() {
        return oneTimeResults;
    }

    public void setOneTimeResults(boolean oneTimeResults) {
        this.oneTimeResults = oneTimeResults;
    }

    public String getLockAt() {
        return lockAt;
    }

    public void setLockAt(String lockAt) {
        this.lockAt = lockAt;
    }

    public List<String> getQuestionTypes() {
        return questionTypes;
    }

    public void setQuestionTypes(List<String> questionTypes) {
        this.questionTypes = questionTypes;
    }

    public boolean isHasAccessCode() {
        return hasAccessCode;
    }

    public void setHasAccessCode(boolean hasAccessCode) {
        this.hasAccessCode = hasAccessCode;
    }

    public boolean isOneQuestionAtATime() {
        return oneQuestionAtATime;
    }

    public void setOneQuestionAtATime(boolean oneQuestionAtATime) {
        this.oneQuestionAtATime = oneQuestionAtATime;
    }

    public boolean isRequireLockdownBrowser() {
        return requireLockdownBrowser;
    }

    public void setRequireLockdownBrowser(boolean requireLockdownBrowser) {
        this.requireLockdownBrowser = requireLockdownBrowser;
    }

    public boolean isRequireLockdownBrowserForResults() {
        return requireLockdownBrowserForResults;
    }

    public void setRequireLockdownBrowserForResults(boolean requireLockdownBrowserForResults) {
        this.requireLockdownBrowserForResults = requireLockdownBrowserForResults;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Nullable
    @Override
    public String getComparisonString() {
        if (getAssignment() != null) {
            return getAssignment().getName();
        }
        return getTitle();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeString(this.mobileUrl);
        dest.writeString(this.htmlUrl);
        dest.writeString(this.description);
        dest.writeString(this.quizType);
        dest.writeParcelable(this.lockInfo, flags);
        dest.writeParcelable(this.permissions, flags);
        dest.writeInt(this.allowedAttempts);
        dest.writeInt(this.questionCount);
        dest.writeString(this.pointsPossible);
        dest.writeString(this.dueAt);
        dest.writeInt(this.timeLimit);
        dest.writeString(this.accessCode);
        dest.writeString(this.ipFilter);
        dest.writeByte(this.lockedForUser ? (byte) 1 : (byte) 0);
        dest.writeString(this.lockExplanation);
        dest.writeString(this.hideResults);
        dest.writeString(this.unlockAt);
        dest.writeByte(this.oneTimeResults ? (byte) 1 : (byte) 0);
        dest.writeString(this.lockAt);
        dest.writeStringList(this.questionTypes);
        dest.writeByte(this.hasAccessCode ? (byte) 1 : (byte) 0);
        dest.writeByte(this.oneQuestionAtATime ? (byte) 1 : (byte) 0);
        dest.writeByte(this.requireLockdownBrowser ? (byte) 1 : (byte) 0);
        dest.writeByte(this.requireLockdownBrowserForResults ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.assignment, flags);
    }

    public Quiz() {
    }

    protected Quiz(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.mobileUrl = in.readString();
        this.htmlUrl = in.readString();
        this.description = in.readString();
        this.quizType = in.readString();
        this.lockInfo = in.readParcelable(LockInfo.class.getClassLoader());
        this.permissions = in.readParcelable(QuizPermission.class.getClassLoader());
        this.allowedAttempts = in.readInt();
        this.questionCount = in.readInt();
        this.pointsPossible = in.readString();
        this.dueAt = in.readString();
        this.timeLimit = in.readInt();
        this.accessCode = in.readString();
        this.ipFilter = in.readString();
        this.lockedForUser = in.readByte() != 0;
        this.lockExplanation = in.readString();
        this.hideResults = in.readString();
        this.unlockAt = in.readString();
        this.oneTimeResults = in.readByte() != 0;
        this.lockAt = in.readString();
        this.questionTypes = in.createStringArrayList();
        this.hasAccessCode = in.readByte() != 0;
        this.oneQuestionAtATime = in.readByte() != 0;
        this.requireLockdownBrowser = in.readByte() != 0;
        this.requireLockdownBrowserForResults = in.readByte() != 0;
        this.assignment = in.readParcelable(Assignment.class.getClassLoader());
    }

    public static final Creator<Quiz> CREATOR = new Creator<Quiz>() {
        @Override
        public Quiz createFromParcel(Parcel source) {
            return new Quiz(source);
        }

        @Override
        public Quiz[] newArray(int size) {
            return new Quiz[size];
        }
    };
}

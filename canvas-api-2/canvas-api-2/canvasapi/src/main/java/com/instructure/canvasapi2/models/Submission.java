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
import java.util.HashMap;


public class Submission extends CanvasModel<Submission> {

    private long id;
    private String grade;
    private double score;
    private long attempt;
    @SerializedName("submitted_at")
    private String submittedAt;
    @SerializedName("submission_comments")
    private ArrayList<SubmissionComment> submissionComments = new ArrayList<>();
    private Date commentCreated;
    private String mediaContentType;
    private String mediaCommentUrl;
    private String mediaCommentDisplay;
    @SerializedName("submission_history")
    private ArrayList<Submission> submissionHistory = new ArrayList<>();
    private ArrayList<Attachment> attachments = new ArrayList<>();
    private String body;
    @SerializedName("rubric_assessment")
    private HashMap<String,RubricCriterionRating> rubricAssessment = new HashMap<>();
    @SerializedName("grade_matches_current_submission")
    private boolean gradeMatchesCurrentSubmission;
    @SerializedName("workflow_state")
    private String workflowState;
    @SerializedName("submission_type")
    private String submissionType;
    @SerializedName("preview_url")
    private String previewUrl;
    private String url;
    private boolean late;
    private boolean excused;
    @SerializedName("media_comment")
    private MediaComment mediaComment;

    //Conversation Stuff
    @SerializedName("assignment_id")
    private long assignmentId;
    private Assignment assignment;
    @SerializedName("user_id")
    private long userId;
    @SerializedName("grader_id")
    private long graderId;
    private User user;

    @Override
    public long getId() {
        return id;
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        return getSubmittedAt();
    }

    @Nullable
    @Override
    public String getComparisonString() {
        return getSubmissionType();
    }

    //this value could be null. Currently will only be returned when getting the submission for
    //a user when the submission_type is discussion_topic
    @SerializedName("discussion_entries")
    private ArrayList<DiscussionEntry> discussionEntries = new ArrayList<>();

    // Group Info only available when including groups in the Submissions#index endpoint
    private Group group;


    public boolean isWithoutGradedSubmission() {
        return !isGraded() && getSubmissionType() == null;
    }

    public boolean isGraded() {
        return getGrade() != null;
    }

    public ArrayList<Long> getUserIds() {
        ArrayList<Long> ids = new ArrayList<>();
        for(int i = 0; i < submissionComments.size(); i++) {
            ids.add(submissionComments.get(i).getAuthorId());
        }
        return ids;
    }

    /*
     * Submissions will have dummy submissions if they grade an assignment with no actual submissions.
     * We want to see if any are not dummy submissions
     */
    public boolean hasRealSubmission(){
        if(submissionHistory != null) {
            for (Submission submission : submissionHistory) {
                if (submission != null && submission.getSubmissionType() != null) {
                    return true;
                }
            }
        }
        return false;
    }


    //region Getters

    public String getGrade() {
        return grade;
    }

    public double getScore() {
        return score;
    }

    public long getAttempt() {
        return attempt;
    }

    public @Nullable Date getSubmittedAt() {
        return APIHelper.stringToDate(submittedAt);
    }

    public ArrayList<SubmissionComment> getSubmissionComments() {
        return submissionComments;
    }

    public Date getCommentCreated() {
        return commentCreated;
    }

    public String getMediaContentType() {
        return mediaContentType;
    }

    public String getMediaCommentUrl() {
        return mediaCommentUrl;
    }

    public String getMediaCommentDisplay() {
        return mediaCommentDisplay;
    }

    public ArrayList<Submission> getSubmissionHistory() {
        return submissionHistory;
    }

    public ArrayList<Attachment> getAttachments() {
        return attachments;
    }

    public String getBody() {
        return body;
    }

    public HashMap<String, RubricCriterionRating> getRubricAssessment() {
        return rubricAssessment;
    }

    public boolean isGradeMatchesCurrentSubmission() {
        return gradeMatchesCurrentSubmission;
    }

    public String getWorkflowState() {
        return workflowState;
    }

    public String getSubmissionType() {
        return submissionType;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public String getUrl() {
        return url;
    }

    public boolean isLate() {
        return late;
    }

    public boolean isExcused() {
        return excused;
    }

    public MediaComment getMediaComment() {
        return mediaComment;
    }

    public long getAssignmentId() {
        return assignmentId;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public long getUserId() {
        return userId;
    }

    public long getGraderId() {
        return graderId;
    }

    public User getUser() {
        return user;
    }

    public ArrayList<DiscussionEntry> getDiscussionEntries() {
        return discussionEntries;
    }

    public Group getGroup() {
        return group;
    }

    //endregion

    //region Setters

    public void setId(long id) {
        this.id = id;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setAttempt(long attempt) {
        this.attempt = attempt;
    }

    public void setSubmittedAt(String submittedAt) {
        this.submittedAt = submittedAt;
    }

    public void setSubmittedAt(Date submittedAt) {
        this.submittedAt = APIHelper.dateToString(submittedAt);
    }

    public void setSubmissionComments(ArrayList<SubmissionComment> submissionComments) {
        this.submissionComments = submissionComments;
    }

    public void setCommentCreated(Date commentCreated) {
        this.commentCreated = commentCreated;
    }

    public void setMediaContentType(String mediaContentType) {
        this.mediaContentType = mediaContentType;
    }

    public void setMediaCommentUrl(String mediaCommentUrl) {
        this.mediaCommentUrl = mediaCommentUrl;
    }

    public void setMediaCommentDisplay(String mediaCommentDisplay) {
        this.mediaCommentDisplay = mediaCommentDisplay;
    }

    public void setSubmissionHistory(ArrayList<Submission> submissionHistory) {
        this.submissionHistory = submissionHistory;
    }

    public void setAttachments(ArrayList<Attachment> attachments) {
        this.attachments = attachments;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setRubricAssessment(HashMap<String, RubricCriterionRating> rubricAssessment) {
        this.rubricAssessment = rubricAssessment;
    }

    public void setGradeMatchesCurrentSubmission(boolean gradeMatchesCurrentSubmission) {
        this.gradeMatchesCurrentSubmission = gradeMatchesCurrentSubmission;
    }

    public void setWorkflowState(String workflowState) {
        this.workflowState = workflowState;
    }

    public void setSubmissionType(String submissionType) {
        this.submissionType = submissionType;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setLate(boolean late) {
        this.late = late;
    }

    public void setExcused(boolean excused) {
        this.excused = excused;
    }

    public void setMediaComment(MediaComment mediaComment) {
        this.mediaComment = mediaComment;
    }

    public void setAssignmentId(long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public void setGraderId(long graderId) {
        this.graderId = graderId;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setDiscussionEntries(ArrayList<DiscussionEntry> discussionEntries) {
        this.discussionEntries = discussionEntries;
    }

    public void setGroup(Group group) {
        this.group = group;
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
        dest.writeString(this.grade);
        dest.writeDouble(this.score);
        dest.writeLong(this.attempt);
        dest.writeString(this.submittedAt);
        dest.writeTypedList(submissionComments);
        dest.writeLong(commentCreated != null ? commentCreated.getTime() : -1);
        dest.writeString(this.mediaContentType);
        dest.writeString(this.mediaCommentUrl);
        dest.writeString(this.mediaCommentDisplay);
        dest.writeTypedList(submissionHistory);
        dest.writeTypedList(attachments);
        dest.writeString(this.body);
        dest.writeSerializable(this.rubricAssessment);
        dest.writeByte(gradeMatchesCurrentSubmission ? (byte) 1 : (byte) 0);
        dest.writeString(this.workflowState);
        dest.writeString(this.submissionType);
        dest.writeString(this.previewUrl);
        dest.writeString(this.url);
        dest.writeByte(late ? (byte) 1 : (byte) 0);
        dest.writeByte(excused ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.mediaComment, flags);
        dest.writeLong(this.assignmentId);
        dest.writeParcelable(this.assignment, flags);
        dest.writeLong(this.userId);
        dest.writeLong(this.graderId);
        dest.writeParcelable(this.user, flags);
        dest.writeTypedList(discussionEntries);
        dest.writeParcelable(this.group, flags);
    }

    public Submission() {
    }

    protected Submission(Parcel in) {
        this.id = in.readLong();
        this.grade = in.readString();
        this.score = in.readDouble();
        this.attempt = in.readLong();
        this.submittedAt = in.readString();
        this.submissionComments = in.createTypedArrayList(SubmissionComment.CREATOR);
        long tmpCommentCreated = in.readLong();
        this.commentCreated = tmpCommentCreated == -1 ? null : new Date(tmpCommentCreated);
        this.mediaContentType = in.readString();
        this.mediaCommentUrl = in.readString();
        this.mediaCommentDisplay = in.readString();
        this.submissionHistory = in.createTypedArrayList(Submission.CREATOR);
        this.attachments = in.createTypedArrayList(Attachment.CREATOR);
        this.body = in.readString();
        this.rubricAssessment = (HashMap<String, RubricCriterionRating>) in.readSerializable();
        this.gradeMatchesCurrentSubmission = in.readByte() != 0;
        this.workflowState = in.readString();
        this.submissionType = in.readString();
        this.previewUrl = in.readString();
        this.url = in.readString();
        this.late = in.readByte() != 0;
        this.excused = in.readByte() != 0;
        this.mediaComment = in.readParcelable(MediaComment.class.getClassLoader());
        this.assignmentId = in.readLong();
        this.assignment = in.readParcelable(Assignment.class.getClassLoader());
        this.userId = in.readLong();
        this.graderId = in.readLong();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.discussionEntries = in.createTypedArrayList(DiscussionEntry.CREATOR);
        this.group = in.readParcelable(Group.class.getClassLoader());
    }

    public static final Creator<Submission> CREATOR = new Creator<Submission>() {
        @Override
        public Submission createFromParcel(Parcel source) {
            return new Submission(source);
        }

        @Override
        public Submission[] newArray(int size) {
            return new Submission[size];
        }
    };

    //endregion
}

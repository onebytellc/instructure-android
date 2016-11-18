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

package com.instructure.pandautils.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import com.google.gson.annotations.SerializedName;
import com.instructure.canvasapi.api.ConversationAPI;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.SubmissionAPI;
import com.instructure.canvasapi.api.UserAPI;
import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.Conversation;
import com.instructure.canvasapi.model.FileUploadParams;
import com.instructure.canvasapi.model.Submission;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.pandautils.models.FileSubmitObject;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.R;
import com.instructure.pandautils.utils.FileUploadUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import retrofit.RetrofitError;

public class FileUploadService extends IntentService implements APIStatusDelegate{

    public FileUploadService() {
        this(FileUploadService.class.getSimpleName());
    }

    public FileUploadService(final String name) {
        super(name);
    }

    private int uploadCount;
    private static final int NOTIFICATION_ID = 1;

    // Upload broadcasts
    public static final String ALL_UPLOADS_COMPLETED = "ALL_UPLOADS_COMPLETED";
    public static final String QUIZ_UPLOAD_COMPLETE = "QUIZ_UPLOAD_COMPLETE";
    public static final String UPLOAD_COMPLETED      = "UPLOAD_COMPLETED";
    public static final String UPLOAD_ERROR          = "UPLOAD_ERROR";

    // Upload Actions
    public static final String ACTION_ASSIGNMENT_SUBMISSION = "ACTION_ASSIGNMENT_SUBMISSION";
    public static final String ACTION_MESSAGE_ATTACHMENTS   = "ACTION_MESSAGE_ATTACHMENTS";
    public static final String ACTION_COURSE_FILE           = "ACTION_COURSE_FILE";
    public static final String ACTION_USER_FILE             = "ACTION_USER_FILE";
    public static final String ACTION_QUIZ_FILE             = "ACTION_QUIZ_FILE";
    public static final String ACTION_DISCUSSION_ATTACHMENT = "ACTION_DISCUSSION_ATTACHMENT";

    public static final String MESSAGE_ATTACHMENT_PATH = "conversation attachments";
    public static final String DISCUSSION_ATTACHMENT_PATH = "discussion attachments";

    private NotificationCompat.Builder notificationBuilder;
    private NotificationManager        notificationManager;

    @Override
    protected void onHandleIntent(Intent intent) {
        final String action = intent.getAction();
        final Bundle bundle = intent.getExtras();
        final ArrayList<FileSubmitObject> fileSubmitObjects = bundle.getParcelableArrayList(Const.FILES);

        uploadCount = fileSubmitObjects.size();
        showNotification(uploadCount);

        startUploads(action, fileSubmitObjects, bundle);
    }

    ///////////////////////////////////////////////////////////////////////////
    // UPLOAD
    ///////////////////////////////////////////////////////////////////////////
    private void startUploads(String action, ArrayList<FileSubmitObject> fileSubmitObjects, Bundle bundle){
        ArrayList<String> attachmentsIds = new ArrayList<>();
        ArrayList<Attachment> attachments = new ArrayList<>();

        final long courseId       = bundle.getLong(Const.COURSE_ID);
        final long assignmentId   = bundle.getLong(Const.ASSIGNMENT_ID);
        final long conversationId = bundle.getLong(Const.CONVERSATION);
        final long quizQuestionId = bundle.getLong(Const.QUIZ_ANSWER_ID, -1);
        final long quizId         = bundle.getLong(Const.QUIZ);
        final int position        = bundle.getInt(Const.POSITION);
        final String messageText  = bundle.getString(Const.MESSAGE);

        Long parentFolderId = null;
        if(bundle.containsKey(Const.PARENT_FOLDER_ID)){
            parentFolderId = bundle.getLong(Const.PARENT_FOLDER_ID);
        }

        try{
            for(int i = 0; i < fileSubmitObjects.size(); i++){
                Long attachmentId = null;
                Attachment attachment = null;
                FileSubmitObject fso = fileSubmitObjects.get(i);

                updateNotificationCount(fso.getName(), i+1);

                if (ACTION_ASSIGNMENT_SUBMISSION.equals(action)) {
                    attachmentId = uploadSubmissionFiles(courseId, assignmentId, fso.getSize(), fso.getName(), fso.getContentType(), fso.getFullPath()).getId();
                }
                else if(ACTION_COURSE_FILE.equals(action)){
                    attachmentId = uploadCourseFiles(courseId, parentFolderId, fso.getSize(), fso.getName(), fso.getContentType(), fso.getFullPath()).getId();
                }
                else if(ACTION_USER_FILE.equals(action)){
                    attachmentId = uploadUserFiles(parentFolderId, fso.getSize(), fso.getName(), fso.getContentType(), fso.getFullPath()).getId();
                }
                else if(ACTION_QUIZ_FILE.equals(action)){
                    attachment = uploadQuizFile(courseId, quizId, fso.getName(), fso.getContentType(), fso.getFullPath());
                }
                else if(ACTION_MESSAGE_ATTACHMENTS.equals(action)){
                    attachmentId = uploadUserFilesByPath(fso.getSize(), fso.getName(), fso.getContentType(), fso.getFullPath()).getId();
                }
                else if(ACTION_DISCUSSION_ATTACHMENT.equals(action)){
                     attachment = uploadUserDiscussionFilesByPath(fso.getSize(), fso.getName(), fso.getContentType(), fso.getFullPath());
                }
                if(attachmentId != null){
                    attachmentsIds.add(String.valueOf(attachmentId));
                }
                if(attachment != null){
                    attachments.add(attachment);
                }
                broadCastUploadCompleted(fso);
            }
            //Submit fileIds to the assignmet
            if (ACTION_ASSIGNMENT_SUBMISSION.equals(action)) {
                submitAttachmentsForSubmission(courseId, assignmentId, attachmentsIds);
            } else if(ACTION_MESSAGE_ATTACHMENTS.equals(action)){
                submitAttachmentsForMessage(conversationId, messageText, attachmentsIds);
            } else if(ACTION_DISCUSSION_ATTACHMENT.equals(action)){
                broadCastDiscussionSuccess(attachments);
            } else if(ACTION_QUIZ_FILE.equals(action)){
                broadCastQuizSuccess(attachments.get(0), quizQuestionId, position);
            } else{
                updateNotificationComplete();
                broadCastAllUploadsCompleted();
            }
        }
        catch(RetrofitError error){
            if(quizQuestionId != -1) {
                broadCastQuizError(error.getMessage(), quizQuestionId, position);
            } else {
                handleRetrofitError(error);
            }
        }
        catch(Exception exception){
            updateNotification(getString(R.string.errorUploadingFile));
            if(quizQuestionId != -1) {
                broadCastQuizError(exception.getMessage(), quizQuestionId, position);
            } else {
                broadcastError(exception.getMessage());
            }
        }

        stopSelf();
    }

    private void submitAttachmentsForMessage(long conversationID, String messageText, ArrayList<String> attachmentsIds){
        Conversation conversation = null;
        try{
            conversation = ConversationAPI.addMessageToConversationSynchronous(this, conversationID, messageText, attachmentsIds);
        }catch(RetrofitError error){
            handleRetrofitError(error);
            broadcastError(getString(R.string.errorSendingMessage));
        }
        catch (Exception exception){
            broadcastError(getString(R.string.errorSendingMessage));
        }

        if(conversation != null){
            updateMessageComplete();
            broadCastMessageSuccess(conversation);
        }
    }

    private void submitAttachmentsForSubmission(long courseId, long assignmentId, ArrayList<String> attachmentsIds){
        Submission submission = SubmissionAPI.postSubmissionAttachments(getApplicationContext(), courseId, assignmentId, attachmentsIds);
        if(submission.getId() > 0){
            updateSubmissionComplete();
            broadCastAllUploadsCompleted();
        }else{
            broadcastError(getString(R.string.errorSubmittingFiles));
        }
    }

    private Attachment uploadSubmissionFiles(long courseId, long assignmentId, long size, String fileName, String contentType, String path){
        FileUploadParams fileUploadToken = SubmissionAPI.getFileUploadParams(getApplicationContext(), courseId, assignmentId, fileName, size, contentType);
        return  SubmissionAPI.uploadAssignmentSubmission(fileUploadToken.getUploadUrl(),
                fileUploadToken.getUploadParams(),
                contentType,
                new File(path));
    }

    private Attachment uploadCourseFiles(long courseId, Long parentFolderId, long size,  String fileName, String contentType, String path){
        FileUploadParams fileUploadToken = CourseAPI.getFileUploadParams(getApplicationContext(), courseId, parentFolderId, fileName, size, contentType);

        return CourseAPI.uploadCourseFile(getApplicationContext(),
                fileUploadToken.getUploadUrl(),
                fileUploadToken.getUploadParams(),
                contentType,
                new File(path));
    }

    private Attachment uploadUserFiles(Long parentFolderId, long size, String fileName, String contentType, String path){
        FileUploadParams fileUploadToken = UserAPI.getFileUploadParams(getApplicationContext(), fileName, size, contentType, parentFolderId);

        return UserAPI.uploadUserFile(fileUploadToken.getUploadUrl(),
                fileUploadToken.getUploadParams(),
                contentType,
                new File(path));
    }

    private Attachment uploadQuizFile(long courseId, long quizId, String fileName, String contentType, String path) {
        FileUploadParams fileUploadParams = CourseAPI.getQuizFileUploadParams(getApplicationContext(), courseId, quizId, fileName, Const.RENAME);

        return CourseAPI.uploadQuizFile(fileUploadParams.getUploadUrl(),
                fileUploadParams.getUploadParams(),
                contentType,
                new File(path));
    }

    private Attachment uploadUserFilesByPath(long size, String fileName, String contentType, String path){
        FileUploadParams fileUploadToken = UserAPI.getFileUploadParams(getApplicationContext(), fileName, size, contentType, MESSAGE_ATTACHMENT_PATH);

        return UserAPI.uploadUserFile(  fileUploadToken.getUploadUrl(),
                                        fileUploadToken.getUploadParams(),
                                        contentType,
                                        new File(path));
    }

    private Attachment uploadUserDiscussionFilesByPath(long size, String fileName, String contentType, String path){
        FileUploadParams fileUploadToken = UserAPI.getFileUploadParams(getApplicationContext(), fileName, size, contentType, DISCUSSION_ATTACHMENT_PATH);

        return UserAPI.uploadUserFile(  fileUploadToken.getUploadUrl(),
                fileUploadToken.getUploadParams(),
                contentType,
                new File(path));
    }

    ///////////////////////////////////////////////////////////////////////////
    // NOTIFICATIONS
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public Context getContext() {
        return null;
    }

    private void broadCastUploadCompleted(FileSubmitObject fso) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.FILENAME, fso);
        final Intent status = new Intent(UPLOAD_COMPLETED);
        status.putExtras(bundle);
        sendBroadcast(status);
    }

    private void broadCastAllUploadsCompleted() {
        final Intent status = new Intent(ALL_UPLOADS_COMPLETED);
        sendBroadcast(status);
    }

    private void broadCastMessageSuccess(Conversation conversation) {
        final Intent status = new Intent(ALL_UPLOADS_COMPLETED);
        status.putExtra(Const.CONVERSATION, (Parcelable) conversation);
        sendBroadcast(status);
    }

    private void broadCastDiscussionSuccess(ArrayList<Attachment> attachments){
        updateNotificationComplete();
        final Intent status = new Intent(ALL_UPLOADS_COMPLETED);
        status.putExtra(Const.ATTACHMENTS, attachments);
        sendBroadcast(status);
    }

    private void broadCastQuizSuccess(Attachment attachment, long quizQuestionId, int position){
        updateNotificationComplete();
        final Intent status = new Intent(QUIZ_UPLOAD_COMPLETE);
        status.putExtra(Const.ATTACHMENT, (Parcelable)attachment);
        status.putExtra(Const.QUIZ_ANSWER_ID, quizQuestionId);
        status.putExtra(Const.POSITION, position);
        sendBroadcast(status);
    }

    private void broadCastQuizError(String message, long quizQuestionId, int position) {
        updateNotificationError(message);
        Bundle bundle = new Bundle();
        Intent status = new Intent(UPLOAD_ERROR);
        status.putExtra(Const.QUIZ_ANSWER_ID, quizQuestionId);
        status.putExtra(Const.POSITION, position);
        bundle.putString(Const.MESSAGE, message);
        status.putExtras(bundle);
        sendBroadcast(status);
    }

    private void broadcastError(String message) {
        updateNotificationError(message);
        Bundle bundle = new Bundle();
        Intent status = new Intent(UPLOAD_ERROR);

        bundle.putString(Const.MESSAGE, message);
        status.putExtras(bundle);
        sendBroadcast(status);
    }

    private void showNotification(int size) {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications_launcher)
                .setContentTitle(String.format(Locale.US, getString(R.string.uploadingFileNum), 1, size))
                .setProgress(0, 0, true);
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void updateNotificationCount(final String fileName, int currentItem){

        notificationBuilder.setContentTitle(String.format(Locale.US, getString(R.string.uploadingFileNum), currentItem, uploadCount))
                           .setContentText(fileName);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }


    private void updateNotification(final String message){
        notificationBuilder.setContentText(message);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void updateNotificationError(final String message){
        notificationBuilder.setContentText(message)
                           .setProgress(0, 0, false);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void updateNotificationComplete(){
        notificationBuilder.setProgress(0, 0, false)
                           .setContentTitle(getString(R.string.filesUploadedSuccessfully));
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void updateSubmissionComplete(){
        notificationBuilder.setProgress(0,0,false)
                .setContentTitle(getString(R.string.filesSubmittedSuccessfully));
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }
    private void updateMessageComplete(){
        notificationBuilder.setProgress(0,0,false)
                .setContentTitle(getString(R.string.messageSentSuccessfully));
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onDestroy() {
        notificationBuilder.setOngoing(false);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        FileUploadUtils.deleteDirectory(FileUploadUtils.getTempFolder(this));
    }

    @Override
    public void onNoNetwork() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder
                .setSmallIcon(R.drawable.ic_notifications_launcher)
                .setContentText(getResources().getString(R.string.noNetwork))
                .setOngoing(false);
        startForeground(NOTIFICATION_ID, builder.build());
    }
    @Override
    public void onCallbackStarted() {}

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {}

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////
    public static Bundle getUserFilesBundle(ArrayList<FileSubmitObject> fileSubmitObjects, Long parentFolderID){
        //Long size, String fileName, String contentType, String path
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Const.FILES, fileSubmitObjects);

        if(parentFolderID != null){
            bundle.putLong(Const.PARENT_FOLDER_ID, parentFolderID);
        }

        return bundle;
    }

    public static Bundle getQuizFileBundle(ArrayList<FileSubmitObject> fileSubmitObjects, Long parentFolderID, long quizQuestionId, int position, long courseId, long quizId){
        //Long size, String fileName, String contentType, String path
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Const.FILES, fileSubmitObjects);
        bundle.putLong(Const.QUIZ_ANSWER_ID, quizQuestionId);
        bundle.putLong(Const.QUIZ, quizId);
        bundle.putLong(Const.COURSE_ID, courseId);
        bundle.putInt(Const.POSITION, position);

        if(parentFolderID != null){
            bundle.putLong(Const.PARENT_FOLDER_ID, parentFolderID);
        }

        return bundle;
    }

    public static Bundle getCourseFilesBundle(ArrayList<FileSubmitObject> fileSubmitObjects, long courseId, Long parentFolderID){
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Const.FILES, fileSubmitObjects);
        bundle.putLong(Const.COURSE_ID, courseId);
        if(parentFolderID != null){
            bundle.putLong(Const.PARENT_FOLDER_ID, parentFolderID);
        }

        return bundle;
    }

    public static Bundle getAssignmentSubmissionBundle(ArrayList<FileSubmitObject> fileSubmitObjects, long courseId, long assignmentId){
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Const.FILES, fileSubmitObjects);
        bundle.putLong(Const.COURSE_ID, courseId);
        bundle.putLong(Const.ASSIGNMENT_ID, assignmentId);
        return bundle;
    }

    public static Bundle getMessageBundle(ArrayList<FileSubmitObject> fileSubmitObjects, String messageText, long conversationId){
        //Long size, String fileName, String contentType, String path
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Const.FILES, fileSubmitObjects);
        bundle.putLong(Const.CONVERSATION, conversationId);
        bundle.putString(Const.MESSAGE, messageText);

        return bundle;
    }

    private void handleRetrofitError(RetrofitError error){
        String errorMessage = null;
        if(error.getBody() != null){
            // The error message returned by canvas is contained within the response body. Convert the body into a custom java pojo.
            UploadError errorBody = (UploadError) error.getBodyAs(UploadError.class);
            errorMessage = errorBody.message;
        }
        if(null == errorMessage || "".equals(errorMessage)){
            errorMessage = error.getMessage();
        }
        updateNotification(errorMessage);
        broadcastError(errorMessage);
    }

    // Used to parse the Retrofit response body error messages
    class UploadError {
        @SerializedName("message")
        public String message;
    }
}

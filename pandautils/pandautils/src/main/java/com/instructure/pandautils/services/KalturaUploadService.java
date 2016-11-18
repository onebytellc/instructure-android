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

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import com.instructure.canvasapi.api.DiscussionAPI;
import com.instructure.canvasapi.api.KalturaAPI;
import com.instructure.canvasapi.api.SubmissionAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.CanvasError;
import com.instructure.canvasapi.model.DiscussionEntry;
import com.instructure.canvasapi.model.KalturaConfig;
import com.instructure.canvasapi.model.KalturaSession;
import com.instructure.canvasapi.model.Submission;
import com.instructure.canvasapi.model.kaltura.xml;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.ErrorDelegate;
import com.instructure.canvasapi.utilities.FileUtilities;
import com.instructure.canvasapi.utilities.KalturaRestAdapter;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.R;
import com.instructure.pandautils.utils.Const;
import java.io.File;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class KalturaUploadService extends Service implements APIStatusDelegate {
    private final static int notificationId = 666;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private CanvasCallback<KalturaConfig> kalturaConfigCallback;
    private CanvasCallback<KalturaSession> kalturaSessionCallback;
    private CanvasCallback<xml> kalturaUploadTokenCanvasCallback;
    private CanvasCallback<Submission> submissionCanvasCallback;
    private CanvasCallback<DiscussionEntry> discussionEntryCanvasCallback;
    private String ksDomain;
    private String kalturaUploadToken;
    private Uri mediaUri;
    private ACTION action;
    private Assignment assignment;
    private DiscussionEntry discussionEntry;
    private long discussionId;
    private String message;
    private CanvasContext canvasContext;
    private long studentId;

    private Runnable fileUpload;

    ErrorDelegate errorDelegate;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null || intent.getSerializableExtra(Const.ACTION) == null) {
            return START_STICKY;
        }
        action = (ACTION) intent.getSerializableExtra(Const.ACTION);

        if (ACTION.isSubmissionComment(action)) {
            assignment = intent.getParcelableExtra(Const.ASSIGNMENT);
            studentId = intent.getLongExtra(Const.STUDENT_ID, APIHelpers.getCacheUser(this).getId());
        }
        else if (ACTION.isAssignmentSubmission(action)){
            assignment = intent.getParcelableExtra(Const.ASSIGNMENT);
        }
        else if (ACTION.isDiscussionAttachment(action)) {
            discussionEntry = intent.getParcelableExtra(Const.DISCUSSION_ENTRY);
            message = intent.getStringExtra(Const.MESSAGE);
            discussionId = intent.getLongExtra(Const.DISCUSSION_ID, 0);
            canvasContext = intent.getParcelableExtra(Const.CANVAS_CONTEXT);
        }

        builder = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.canvas_logo_white)
                        .setContentTitle(getString(R.string.notificationTitle))
                        .setContentText(getString(R.string.preparingUpload))
                        .setOngoing(true);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mediaUri = intent.getParcelableExtra(Const.URI);

        notificationManager.notify(notificationId, builder.build());

        startFileUpload();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //clean up
        builder.setOngoing(false);
        notificationManager.cancel(notificationId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        //clean up again in case of swipe to dismiss
        builder.setOngoing(false);
        notificationManager.cancel(notificationId);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startFileUpload() {
        fileUpload = new Runnable() {
            @Override
            public void run() {
                try {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    //Setup for api call
                    String kalturaToken = APIHelpers.getKalturaToken(getContext());
                    String baseUrl = APIHelpers.getFullKalturaDomain(getContext());
                    TypedFile typedFile = new TypedFile(FileUtilities.getMimeType(mediaUri.getPath()), new File(mediaUri.getPath()));
                    String fullUrl = baseUrl + "/api_v3/index.php?service=uploadtoken&action=upload";

                    uploadStartedToast();

                    Response response = KalturaAPI.uploadKalturaFile(kalturaToken, kalturaUploadToken, typedFile, fullUrl);
                    if (response != null && response.getStatus() == 201) {
                        callNextAPIcall(KalturaAPI.getMediaIdForUploadedFileTokenSynchronous(getContext(), kalturaToken, kalturaUploadToken, typedFile.fileName(), typedFile.mimeType()));
                    } else {
                        uploadError();
                    }
                } catch (Exception e){
                    uploadError();
                }
            }
        };

        setupCallbacks();

        KalturaAPI.getKalturaConfiguration(kalturaConfigCallback);
    }

    private void setupCallbacks() {
        errorDelegate = new ErrorDelegate() {
            @Override
            public void noNetworkError(RetrofitError error, Context context) {
                onNoNetwork();
            }

            @Override
            public void notAuthorizedError(RetrofitError error, CanvasError canvasError, Context context) {
                uploadError();
            }

            @Override
            public void invalidUrlError(RetrofitError error, Context context) {
                uploadError();
            }

            @Override
            public void serverError(RetrofitError error, Context context) {
                uploadError();
            }

            @Override
            public void generalError(RetrofitError error, CanvasError canvasError, Context context) {
                uploadError();
            }
        };

        kalturaConfigCallback = new CanvasCallback<KalturaConfig>(this, errorDelegate) {
            @Override
            public void cache(KalturaConfig kalturaConfig, LinkHeaders linkHeaders, Response response) {}

            @Override
            public void firstPage(KalturaConfig kalturaConfig, LinkHeaders linkHeaders, Response response) {
                if (kalturaConfig.isEnabled()) {
                    ksDomain = kalturaConfig.getDomain();
                    KalturaAPI.startKalturaSession(kalturaSessionCallback);
                } else {
                    uploadError();
                }

            }
        };

        kalturaSessionCallback = new CanvasCallback<KalturaSession>(this, errorDelegate) {
            @Override
            public void cache(KalturaSession kalturaSession, LinkHeaders linkHeaders, Response response) {}

            @Override
            public void firstPage(KalturaSession kalturaSession, LinkHeaders linkHeaders, Response response) {
                if (!KalturaRestAdapter.setupInstance(getContext(), kalturaSession.getKs(), ksDomain)) {
                    uploadError();
                }

                notificationManager.notify(
                        notificationId,
                        builder.build());
                KalturaAPI.getKalturaUploadToken(kalturaUploadTokenCanvasCallback);
            }
        };

        kalturaUploadTokenCanvasCallback = new CanvasCallback<xml>(this, errorDelegate) {

            @Override
            public void cache(xml xml, LinkHeaders linkHeaders, Response response) {}

            @Override
            public void firstPage(final xml xml, LinkHeaders linkHeaders, Response response) {

                builder.setContentText(getString(R.string.uploadingFile));
                builder.setProgress(0, 0, true);
                notificationManager.notify(
                        notificationId,
                        builder.build());

                if (xml.getResult() != null && xml.getResult().getKalturaError() == null) {
                    kalturaUploadToken = xml.getResult().getId();

                    new Thread(fileUpload).start();
                } else {
                    uploadError();
                }
            }
        };

        submissionCanvasCallback = new CanvasCallback<Submission>(this, errorDelegate) {

            @Override
            public void cache(Submission submission, LinkHeaders linkHeaders, Response response) {
                if (submission != null) {
                    builder.setContentText(getString(R.string.fileUploadSuccess))
                            .setProgress(100, 100, false)
                            .setOngoing(false);

                    notificationManager.notify(
                            notificationId,
                            builder.build());

                    Intent intent = new Intent(Const.SUBMISSION_COMMENT_SUBMITTED);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

                    Intent successIntent = new Intent(Const.UPLOAD_SUCCESS);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(successIntent);
                }

                //Stop the service
                stopSelf();
            }

            @Override
            public void firstPage(Submission submission, LinkHeaders linkHeaders, Response response) {
                cache(submission, linkHeaders, response);
            }
        };

        discussionEntryCanvasCallback = new CanvasCallback<DiscussionEntry>(this, errorDelegate) {

            @Override
            public void cache(DiscussionEntry discussionEntry, LinkHeaders linkHeaders, Response response) {}

            @Override
            public void firstPage(DiscussionEntry discussionEntry, LinkHeaders linkHeaders, Response response) {
                if (discussionEntry != null) {
                    builder.setContentText(getString(R.string.fileUploadSuccess))
                            .setProgress(100, 100, false)
                            .setOngoing(false);

                    notificationManager.notify(
                            notificationId,
                            builder.build());

                    Intent intent = new Intent(Const.DISCUSSION_REPLY_SUBMITTED);
                    intent.putExtra(Const.DISCUSSION_ENTRY, (Parcelable)discussionEntry);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);

                    Intent successIntent = new Intent(Const.UPLOAD_SUCCESS);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(successIntent);
                }

                //Stop the service
                stopSelf();
            }

        };
    }



    private void uploadError(){
        builder.setContentText(getString(R.string.errorUploadingFile))
                .setProgress(100, 100, false)
                .setOngoing(false);
        notificationManager.notify(
                notificationId,
                builder.build());
    }

    private void uploadStartedToast() {
        Handler handler = new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getContext(), R.string.uploadMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callNextAPIcall(xml xml) {
        CanvasContext course;
        String mediaId = xml.getResult().getId();

        long assignmentId = 0;
        if(assignment != null) {
            assignmentId = assignment.getId();
            course = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, assignment.getCourseId(), Const.COURSE);
        } else {
            course = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, canvasContext.getId(), Const.COURSE);
        }

        String mediaType = FileUtilities.mediaTypeFromKalturaCode(xml.getResult().getMediaType());
        if (ACTION.isSubmissionComment(action)) {
            SubmissionAPI.postMediaSubmissionComment(course, assignmentId, studentId, mediaId, mediaType, submissionCanvasCallback);
        }
        else if(ACTION.isAssignmentSubmission(action)){
            SubmissionAPI.postMediaSubmission(course, assignment.getId(), Const.MEDIA_RECORDING, mediaId, mediaType, submissionCanvasCallback);
        }
        else if(ACTION.isDiscussionAttachment(action)) {

            //this is the format that Canvas expects
            String attachment = String.format("<p><a id='media_comment_%s' " +
                    "class='instructure_inline_media_comment %s'" +
                    "href='/media_objects/%s'>this is a media comment</a></p>", mediaId, mediaType, mediaId);

            if(message == null) {
                message = "";
            }

            attachment += "\n" + message;

            if(discussionEntry.getParent() == null) {
                DiscussionAPI.postDiscussionEntry(canvasContext, discussionId, attachment, discussionEntryCanvasCallback);
            } else {
                DiscussionAPI.postDiscussionReply(canvasContext, discussionId, discussionEntry.getId(), attachment, discussionEntryCanvasCallback);
            }
        }
        else {
            uploadError();
        }
    }

    public enum ACTION {
        SUBMISSION_COMMENT,
        ASSIGNMENT_SUBMISSION,
        DISCUSSION_COMMENT;

        public static boolean isSubmissionComment(ACTION action) {
            return action == SUBMISSION_COMMENT;
        }

        public static boolean isAssignmentSubmission(ACTION action){
            return action == ASSIGNMENT_SUBMISSION;
        }

        public static boolean isDiscussionAttachment(ACTION action) {
            return action == DISCUSSION_COMMENT;
        }
    }

    @Override public void onCallbackStarted() { }

    @Override public void onCallbackFinished(CanvasCallback.SOURCE source) { }

    @Override
    public void onNoNetwork() {
        builder.setContentText(getString(R.string.noNetwork))
                .setOngoing(false);
        notificationManager.notify(
                notificationId,
                builder.build());
    }

    @Override
    public Context getContext() {
        return this;
    }
}
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

package com.instructure.pandautils.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.DiscussionEntry;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.pandautils.R;
import com.instructure.pandautils.services.KalturaUploadService;
import com.instructure.pandautils.utils.Const;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.pandautils.utils.FileUploadUtils;
import com.instructure.pandautils.utils.PermissionUtils;
import com.instructure.pandautils.utils.RequestCodes;
import com.instructure.pandautils.utils.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class KalturaMediaUploadPicker extends Activity {

    private Uri capturedImageURI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kaltura_media_upload_picker);

        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.rootView);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView takeVideo = (TextView) rootView.findViewById(R.id.takeVideo);
        takeVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newVideo();
            }
        });
        TextView selectMedia = (TextView) rootView.findViewById(R.id.chooseMedia);
        selectMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //only let the user use local images (no picasa...)
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.setType("video/*, audio/*");
                File file = new File("/image/*");
                intent.setDataAndType(Uri.fromFile(file), "video/*");
                startActivityForResult(intent, RequestCodes.SELECT_MEDIA);
            }
        });
    }

    @TargetApi(23)
    private void newVideo() {
        if(PermissionUtils.hasPermissions(KalturaMediaUploadPicker.this, PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA, PermissionUtils.RECORD_AUDIO)) {
            takeVideoBecausePermissionsAlreadyGranted();
        } else {
            requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA, PermissionUtils.RECORD_AUDIO), PermissionUtils.PERMISSION_REQUEST_CODE);
        }
    }

    private void takeVideoBecausePermissionsAlreadyGranted() {
        //check to see if the device has a camera
        if(!Utils.hasCameraAvailable(KalturaMediaUploadPicker.this)) {
            Toast.makeText(getApplicationContext(), R.string.noCameraOnDevice, Toast.LENGTH_LONG).show();
            return;
        }

        //create new Intent
        Intent cameraIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        capturedImageURI = getOutputMediaFileUri();  // create a file to save the video
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageURI);  // set the image file name
        startActivityForResult(cameraIntent, RequestCodes.TAKE_VIDEO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if(PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
                takeVideoBecausePermissionsAlreadyGranted();
            } else {
                Toast.makeText(KalturaMediaUploadPicker.this, R.string.permissionDenied, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri mediaUri = null;
        if (resultCode != Activity.RESULT_OK) {
            return;
        } else if (requestCode == RequestCodes.SELECT_MEDIA) {
            Uri tempMediaUri = data.getData();

            if(tempMediaUri == null){
                setResult(RESULT_CANCELED);
                finish();
            }

            mediaUri = Uri.parse(FileUploadUtils.getPath(this, tempMediaUri));
        } else if (requestCode == RequestCodes.TAKE_VIDEO) {
            mediaUri = capturedImageURI;
        }

        if (mediaUri != null) {
            Intent serviceIntent = new Intent(this, KalturaUploadService.class);
            serviceIntent.putExtra(Const.URI, (Parcelable) mediaUri);
            if (KalturaUploadService.ACTION.isSubmissionComment((KalturaUploadService.ACTION) getIntent().getSerializableExtra(Const.ACTION))) {
                serviceIntent.putExtra(Const.ACTION, KalturaUploadService.ACTION.SUBMISSION_COMMENT);
                serviceIntent.putExtra(Const.ASSIGNMENT, getIntent().getParcelableExtra(Const.ASSIGNMENT));
                serviceIntent.putExtra(Const.STUDENT_ID, getIntent().getLongExtra(Const.STUDENT_ID, APIHelpers.getCacheUser(this).getId()));
            } else if(KalturaUploadService.ACTION.isAssignmentSubmission((KalturaUploadService.ACTION) getIntent().getSerializableExtra(Const.ACTION))){
                serviceIntent.putExtra(Const.ACTION, KalturaUploadService.ACTION.ASSIGNMENT_SUBMISSION);
                serviceIntent.putExtra(Const.ASSIGNMENT, getIntent().getParcelableExtra(Const.ASSIGNMENT));
            } else if(KalturaUploadService.ACTION.isDiscussionAttachment((KalturaUploadService.ACTION) getIntent().getSerializableExtra(Const.ACTION))){
                serviceIntent.putExtra(Const.ACTION, KalturaUploadService.ACTION.DISCUSSION_COMMENT);
                serviceIntent.putExtra(Const.DISCUSSION_ENTRY, getIntent().getParcelableExtra(Const.DISCUSSION_ENTRY));
                serviceIntent.putExtra(Const.MESSAGE, getIntent().getStringExtra(Const.MESSAGE));
                serviceIntent.putExtra(Const.DISCUSSION_ID, getIntent().getLongExtra(Const.DISCUSSION_ID, 0));
                serviceIntent.putExtra(Const.CANVAS_CONTEXT, getIntent().getParcelableExtra(Const.CANVAS_CONTEXT));
            }

            startService(serviceIntent);
            setResult(RESULT_OK);
            Intent intent = new Intent(Const.UPLOAD_STARTED);
            LocalBroadcastManager.getInstance(KalturaMediaUploadPicker.this).sendBroadcast(intent);
            finish();
        }
    }

    /**
     * Create a File for saving an image or video
     */
    private Uri getOutputMediaFileUri() {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Canvas");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("pandaUtils", "KalturaMediaUploadPicker failed to make Dir.");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "canvas_media_comment" + timeStamp + ".mp4");

        return Uri.fromFile(mediaFile);
    }

    public static Intent createIntentForAssigmnetSubmission(Context context, Assignment assignment) {
        Intent intent = new Intent(context, KalturaMediaUploadPicker.class);
        intent.putExtra(Const.ACTION, KalturaUploadService.ACTION.ASSIGNMENT_SUBMISSION);
        intent.putExtra(Const.ASSIGNMENT, (Parcelable) assignment);
        return intent;
    }

    public static Intent createIntentForSubmissionComment(Context context, Assignment assignment) {
        Intent intent = new Intent(context, KalturaMediaUploadPicker.class);
        intent.putExtra(Const.ACTION, KalturaUploadService.ACTION.SUBMISSION_COMMENT);
        intent.putExtra(Const.ASSIGNMENT, (Parcelable) assignment);
        return intent;
    }

    public static Intent createIntentForTeacherSubmissionComment(Context context, Assignment assignment, long studentId) {
        Intent intent = new Intent(context, KalturaMediaUploadPicker.class);
        intent.putExtra(Const.ACTION, KalturaUploadService.ACTION.SUBMISSION_COMMENT);
        intent.putExtra(Const.ASSIGNMENT, (Parcelable) assignment);
        intent.putExtra(Const.STUDENT_ID, studentId);
        return intent;
    }

    public static Intent createIntentForDiscussionReply(Context context, DiscussionEntry discussionEntry, String message, long discussionId, CanvasContext canvasContext) {
        Intent intent = new Intent(context, KalturaMediaUploadPicker.class);
        intent.putExtra(Const.ACTION, KalturaUploadService.ACTION.DISCUSSION_COMMENT);
        intent.putExtra(Const.DISCUSSION_ENTRY, (Parcelable) discussionEntry);
        intent.putExtra(Const.MESSAGE, message);
        intent.putExtra(Const.DISCUSSION_ID, discussionId);
        intent.putExtra(Const.CANVAS_CONTEXT, (Parcelable) canvasContext);
        return intent;
    }
}

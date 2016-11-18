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

package com.instructure.candroid.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import com.instructure.candroid.R;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.pandautils.utils.PermissionUtils;
import com.instructure.pandautils.utils.RequestCodes;

import java.io.File;
import java.util.List;

public class FileSourceDialogStyled extends DialogFragment {

    public static final int CAMERA_REQUEST = 123;
    public static final int GALLERY_REQUEST = 124;
    public static final String TAG = "fileSourceDialog";

    //data
    private static boolean mCameraPermissions = false;
    private static boolean mGalleryPermissions = false;
    //Current Permission must be set before making a permission request
    private int mCurrentPermissionRequest = 0;
    private Uri mCapturedImageURI;
    private SourceClickedListener listener;
    public static FileSourceDialogStyled newInstance() {
        return new FileSourceDialogStyled();
    }

    public void setListener(SourceClickedListener listener){
        this.listener = listener;

    }
    public interface SourceClickedListener{
        public void onSourceSelected(Intent intent, int requestCode);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();
        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(activity)
                                  .title(activity.getString(R.string.chooseFileSource));

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.file_source_dialog, null);
        view.findViewById(R.id.fromCamera).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(PermissionUtils.hasPermissions(getActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA)) {
                    pickFromCameraBecausePermissionsAlreadyGranted();
                } else {
                    //Prepare state for onResume
                    mCurrentPermissionRequest = CAMERA_REQUEST;
                    requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA), PermissionUtils.PERMISSION_REQUEST_CODE);
                }
            }
        });

        view.findViewById(R.id.fromDevice).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                pickFromDevice();
            }
        });

        view.findViewById(R.id.fromOtherApplication).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                ShareInstructionsDialogStyled.show(getActivity());
            }
        });

        view.findViewById(R.id.fromPhotoGallery).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(PermissionUtils.hasPermissions(getActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA)) {
                    pickFromGallery();
                } else {
                    //Prepare state for onResume
                    mCurrentPermissionRequest = GALLERY_REQUEST;
                    requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA), PermissionUtils.PERMISSION_REQUEST_CODE);
                }
            }
        });

        builder.customView(view, false);
        return builder.build();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if(PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
                //Finalize state for onResume
                switch(mCurrentPermissionRequest) {
                    case (CAMERA_REQUEST):
                        mCameraPermissions = true;
                        break;
                    case (GALLERY_REQUEST): {
                        mGalleryPermissions = true;
                        break;
                    }
                }
            } else {
                Toast.makeText(getActivity(), R.string.permissionDenied, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //http://stackoverflow.com/questions/37164415/android-fatal-error-can-not-perform-this-action-after-onsaveinstancestate
        //The activity will be restarted as a result of these permission requests, both of these
        //cases require a clean activity as they perform fragment transactions
        if(mCameraPermissions) {
            pickFromCameraBecausePermissionsAlreadyGranted();
            mCameraPermissions = false;
        }
        if(mGalleryPermissions) {
            pickFromGallery();
            mGalleryPermissions = false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    private static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
        packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void pickFromCameraBecausePermissionsAlreadyGranted() {
      //let the user take a picture
        //get the location of the saved picture
        String fileName = "pic_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        try {
            mCapturedImageURI = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
        catch (Exception e) {
            Toast.makeText(getActivity(), R.string.errorOccurred, Toast.LENGTH_SHORT).show();
            return;
        }
        if(mCapturedImageURI != null) {
            //save the intent information in case we get booted from memory.
            SharedPreferences settings = getActivity().getSharedPreferences(ApplicationManager.PREF_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("ProfileFragment-URI", mCapturedImageURI.toString());
            editor.apply();
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
        if(isIntentAvailable(getActivity(), cameraIntent.getAction()) && listener != null){
            listener.onSourceSelected(cameraIntent, RequestCodes.CAMERA_PIC_REQUEST);
            dismiss();
        }
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        File file = new File("/image/*");
        intent.setDataAndType(Uri.fromFile(file), "image/*");
        if(listener != null){
            listener.onSourceSelected(intent, RequestCodes.PICK_IMAGE_GALLERY);
        }
        dismiss();

    }
    
    private void pickFromDevice() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if(listener != null){
            listener.onSourceSelected(intent, RequestCodes.PICK_FILE_FROM_DEVICE);
        }
        dismiss();
    }
}
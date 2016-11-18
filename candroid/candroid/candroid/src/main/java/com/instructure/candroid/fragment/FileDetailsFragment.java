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

package com.instructure.candroid.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.instructure.candroid.R;
import com.instructure.candroid.util.CanvasErrorDelegate;
import com.instructure.candroid.util.DownloadMedia;
import com.instructure.candroid.util.StringUtilities;
import com.instructure.candroid.view.ViewUtils;
import com.instructure.canvasapi.api.FileFolderAPI;
import com.instructure.canvasapi.api.ModuleAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.CanvasError;
import com.instructure.canvasapi.model.FileFolder;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.canvasapi.utilities.ErrorDelegate;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.PermissionUtils;
import com.squareup.picasso.Picasso;

import java.util.Date;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class FileDetailsFragment extends ParentFragment {
    // views
    private Button openButton;
    private Button downloadButton;
    private TextView fileNameTextView;
    private TextView fileTypeTextView;
    private ImageView icon;

    private long moduleId;
    private long itemId;

    private FileFolder file;
    private String fileUrl = "";

    private CanvasCallback<FileFolder> fileFolderCanvasCallback;
    private CanvasCallback<com.squareup.okhttp.Response> markReadCanvasCallback;

    //If the file doesn't exist, we don't want unexpected error. We want them to know it doesn't exist.
    private ErrorDelegate errorDelegate;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.DETAIL;
    }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.file);
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        // use display name if there isn't any lock info
        return file != null && file.getLockInfo() == null ? file.getDisplayName() : getFragmentTitle();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.file_details_fragment, container, false);
        setupViews(rootView);

        return rootView;
    }

    private void setupViews(View rootView) {
        fileNameTextView = (TextView) rootView.findViewById(R.id.fileName);
        fileTypeTextView = (TextView) rootView.findViewById(R.id.fileType);

        icon = (ImageView) rootView.findViewById(R.id.fileIcon);

        openButton = (Button) rootView.findViewById(R.id.openButton);
        downloadButton = (Button) rootView.findViewById(R.id.downloadButton);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //we need to get the file info based on the URL that we received.
        setUpErrorDelegate();
        setUpCallback();
        FileFolderAPI.getFileFolderFromURL(fileUrl, fileFolderCanvasCallback);
    }

    ///////////////////////////////////////////////////////////////////////////
    // View setup
    ///////////////////////////////////////////////////////////////////////////

    public void setupTextViews() {
        fileNameTextView.setText(file.getDisplayName());
        fileTypeTextView.setText(file.getContentType());
    }

    public void setupClickListeners() {
        openButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                openMedia(file.getContentType(), file.getUrl(), file.getDisplayName());

                //Mark the module as read
                ModuleAPI.markModuleItemRead(getCanvasContext(), moduleId, itemId, markReadCanvasCallback);
            }
        });

        downloadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (PermissionUtils.hasPermissions(getActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                    downloadFile();
                } else {
                    requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE);
                }
            }
        });
    }

    private void downloadFile() {
        DownloadMedia.downloadMedia(getActivity(), file.getUrl(), file.getDisplayName(), file.getName());

        //Mark the module as read
        ModuleAPI.markModuleItemRead(getCanvasContext(), moduleId, itemId, markReadCanvasCallback);
    }

    /////////////////////////////////////////////////////////////////////////// 
    // Callback
    ///////////////////////////////////////////////////////////////////////////

    public void setUpCallback() {
        fileFolderCanvasCallback = new CanvasCallback<FileFolder>(this, errorDelegate) {
            @Override
            public void cache(FileFolder fileFolder) {
            }

            @Override
            public void firstPage(FileFolder fileFolder, LinkHeaders linkHeaders, Response response) {
                if (!apiCheck()) {
                    return;
                }

                //set up everything else now, we should have a file
                file = fileFolder;

                if (file != null) {
                    if (file.getLockInfo() != null) {
                        //file is locked
                        icon.setImageResource(R.drawable.ic_cv_lock_dark);
                        openButton.setVisibility(View.GONE);
                        downloadButton.setVisibility(View.GONE);
                        fileTypeTextView.setVisibility(View.INVISIBLE);
                        String lockedMessage = "";

                        if (file.getLockInfo().getLockedModuleName() != null) {
                            lockedMessage = "<p>" + String.format(getActivity().getString(R.string.lockedFileDesc), "<b>" + file.getLockInfo().getLockedModuleName() + "</b>") + "</p>";
                        }
                        if (file.getLockInfo().getModulePrerequisiteNames().size() > 0) {
                            //we only want to add this text if there are module completion requirements
                            lockedMessage += getActivity().getString(R.string.mustComplete) + "<br>";
                            //textViews can't display <ul> and <li> tags, so we need to use "&#8226; " instead
                            for (int i = 0; i < file.getLockInfo().getModulePrerequisiteNames().size(); i++) {
                                lockedMessage += "&#8226; " + file.getLockInfo().getModulePrerequisiteNames().get(i);  //"&#8226; "
                            }
                            lockedMessage += "<br><br>";
                        }

                        //check to see if there is an unlocked date
                        if (file.getLockInfo().getUnlockedAt() != null && file.getLockInfo().getUnlockedAt().after(new Date())) {
                            lockedMessage += DateHelpers.createPrefixedDateTimeString(getContext(), getActivity().getString(R.string.unlockedAt) + "<br>&#8226; ", file.getLockInfo().getUnlockedAt());
                        }
                        fileNameTextView.setText(StringUtilities.simplifyHTML(Html.fromHtml(lockedMessage)));
                    } else {
                        setupTextViews();
                        setupClickListeners();
                        // if the file has a thumbnail then show it. Make it a little bigger since the thumbnail size is pretty small
                        if(!TextUtils.isEmpty(file.getThumbnailUrl())) {
                            int dp = (int)ViewUtils.convertDipsToPixels(150, getActivity());
                            Picasso.with(getActivity()).load(file.getThumbnailUrl()).resize(dp,dp).centerInside().into(icon);
                        }
                    }
                }
                setupTitle(getActionbarTitle());
            }
        };

        markReadCanvasCallback = new CanvasCallback<com.squareup.okhttp.Response>(this, errorDelegate) {
            @Override
            public void cache(com.squareup.okhttp.Response response) {
            }

            @Override
            public void firstPage(com.squareup.okhttp.Response response, LinkHeaders linkHeaders, Response response2) {
            }
        };
    }

    public void setUpErrorDelegate() {
        //If the file no longer exists (404), we want to show a different crouton than the default.
        final ErrorDelegate canvasErrorDelegate = new CanvasErrorDelegate();
        errorDelegate = new ErrorDelegate() {

            @Override
            public void noNetworkError(RetrofitError error, Context context) {
                canvasErrorDelegate.noNetworkError(error, context);
            }

            @Override
            public void notAuthorizedError(RetrofitError error, CanvasError canvasError, Context context) {
                canvasErrorDelegate.notAuthorizedError(error, canvasError, context);
            }

            @Override
            public void invalidUrlError(RetrofitError error, Context context) {
                if (context instanceof Activity) {
                    showToast(R.string.fileNoLongerExists);
                }
            }

            @Override
            public void serverError(RetrofitError error, Context context) {
                canvasErrorDelegate.serverError(error, context);

            }

            @Override
            public void generalError(RetrofitError error, CanvasError canvasError, Context context) {
                canvasErrorDelegate.generalError(error, canvasError, context);
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE) {
            if(PermissionUtils.permissionGranted(permissions, grantResults, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                downloadFile();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        fileUrl = extras.getString(Const.FILE_URL);
        moduleId = extras.getLong(Const.MODULE_ID);
        itemId = extras.getLong(Const.ITEM_ID);
    }


    public static Bundle createBundle(CanvasContext canvasContext, String fileUrl) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.FILE_URL, fileUrl);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, long moduleId, long itemId, String fileUrl) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.FILE_URL, fileUrl);
        extras.putLong(Const.MODULE_ID, moduleId);
        extras.putLong(Const.ITEM_ID, itemId);
        return extras;
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}

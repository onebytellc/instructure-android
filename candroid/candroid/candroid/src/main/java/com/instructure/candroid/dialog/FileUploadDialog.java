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

import android.animation.Animator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import com.instructure.candroid.R;
import com.instructure.candroid.adapter.FileUploadAdapter;
import com.instructure.candroid.util.AnimationHelpers;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.candroid.view.ViewUtils;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.loginapi.login.util.ProfileUtils;
import com.instructure.pandautils.models.FileSubmitObject;
import com.instructure.pandautils.services.FileUploadService;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;
import com.instructure.canvasapi.model.Course;
import com.instructure.pandautils.utils.FileUploadUtils;
import com.instructure.pandautils.utils.RequestCodes;
import java.io.File;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FileUploadDialog extends DialogFragment implements FileSourceDialogStyled.SourceClickedListener{

    public static final String TAG = "fileUploadDialog";

    public enum FileUploadType {ASSIGNMENT, COURSE, USER, MESSAGE, DISCUSSION, QUIZ}

    // Returns a list of FileSubmitObjects, in case FileUploadService is called from outside the dialog
    public interface FileSelectionInterface{
        void onFilesSelected(ArrayList<FileSubmitObject> fileSubmitObjects);
    }

    public interface FileUploadStartedInterface {
        void onFileUploadStarted(long questionId, int position);
    }

    public interface DialogLifecycleCallback {
        void onCancel(Dialog dialog);
        void onAllUploadsComplete(Dialog dialog);
    }

    // member variables
    private FileUploadType uploadType = FileUploadType.ASSIGNMENT;
    private CanvasContext canvasContext;
    private boolean isOneFileOnly = false;

    // bundled data
    private Assignment assignment;
    private @Nullable Course course;
    private @Nullable Long parentFolderID;
    private User user;
    private long quizQuestionId;
    private long quizId;
    private long courseId;
    private int position;

    /// views
    private ProgressDialog progressDialog;
    private RelativeLayout dialogContent;
    private CircleImageView avatar;
    private Button addFileButton;
    private TextView allowedExtensions;
    private TextView subtitle;
    private TextView title;

    // dialog header info
    private String dialogSubTitle;
    private String positiveText;
    private String dialogTitle;
    private int positiveColor;

    // data
    private FileUploadAdapter adapter;
    private ArrayList<FileSubmitObject> files = new ArrayList<>();
    private Uri submitUri;

    // receivers
    private BroadcastReceiver allUploadsCompleteBroadcastReceiver;
    private BroadcastReceiver uploadBroadcastReceiver;
    private BroadcastReceiver errorBroadcastReceiver;

    private boolean needsUnregister;
    private DialogLifecycleCallback dialogLifecycleCallback;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////
    public static FileUploadDialog newInstance(FragmentManager manager, Bundle bundle) {
        Fragment fragment = manager.findFragmentByTag(FileUploadDialog.TAG);
        if(fragment instanceof FileUploadDialog){
            FileUploadDialog dialog = (FileUploadDialog)fragment;
            dialog.dismissAllowingStateLoss();
        }

        FileUploadDialog uploadFileDialog = new FileUploadDialog();
        uploadFileDialog.setArguments(bundle);
        return uploadFileDialog;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////
    @Override public void onStart() {
        super.onStart();
        // Don't dim the background when the dialog is created.
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0f;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);
        registerReceivers();
        setDialogMargins();
    }

    @Override
    public void onStop() {
        unregisterReceivers(); // unregister here to avoid leaks (crashes the app)
        super.onStop();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (dialogLifecycleCallback != null) {
            dialogLifecycleCallback.onCancel(getDialog());
        }

        onDismiss(dialog);
    }

    public void onDismiss(DialogInterface dialog){
        unregisterReceivers();
        Activity activity = getActivity();
        if(activity instanceof ShareFileDestinationDialog.DialogCloseListener){
            ((ShareFileDestinationDialog.DialogCloseListener)activity).onCancel(dialog);
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()){
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setDialogMargins();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getDialog() != null){
            getDialog().getWindow().getAttributes().windowAnimations = R.style.FileUploadDialogAnimation;
            getDialog().getWindow().setWindowAnimations(R.style.FileUploadDialogAnimation);
        }
    }

    public void setDialogLifecycleCallback(DialogLifecycleCallback dialogLifecycleCallback) {
        this.dialogLifecycleCallback = dialogLifecycleCallback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        loadBundleData();
        initProgressDialog();
        handleUriContents();

        adapter = new FileUploadAdapter(getActivity(), canvasContext, files);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder .backgroundColorRes(R.color.white)
                .positiveText(positiveText)
                .positiveColor(positiveColor)
                .cancelable(true)
                .autoDismiss(false)
                .customView(initViews(), false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        positiveClicked();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        onCancel(dialog);
                        dismiss();
                    }
                 });

        if(uploadType != FileUploadType.MESSAGE){
            builder.negativeText(getActivity().getString(R.string.cancel))
                   .negativeColorRes(R.color.canvasTextMedium);
        }

        final MaterialDialog dialog = builder.build();

        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Upload
    ///////////////////////////////////////////////////////////////////////////
    private void positiveClicked(){
        if(uploadType == FileUploadType.MESSAGE){
            Fragment targetFragment = getTargetFragment();
            if(targetFragment instanceof  FileSelectionInterface){
                ((FileSelectionInterface)targetFragment).onFilesSelected(files);
                dismiss();
            }
        } else if(uploadType == FileUploadType.QUIZ) {
            Fragment targetFragment = getTargetFragment();
            if(targetFragment instanceof FileUploadStartedInterface){
                ((FileUploadStartedInterface)targetFragment).onFileUploadStarted(quizQuestionId, position);
                uploadFiles();
            }
        } else {
            uploadFiles();
        }
    }

    public void uploadFiles(){
        if (files.size() == 0) {
            Toast.makeText(getActivity(), R.string.noFilesUploaded, Toast.LENGTH_SHORT).show();
            return;
        }

        if (uploadType == FileUploadType.ASSIGNMENT) {
            if (!checkIfFileSubmissionAllowed()) { //see if we can actually submit files to this assignment
                Toast.makeText(getActivity(), R.string.fileUploadNotSupported, Toast.LENGTH_SHORT).show();
                return;
            }

            //make sure that what we've uploaded can still be uploaded (allowed extensions)
            for (int i = 0; i < files.size(); i++) {
                if (!isExtensionAllowed(files.get(i).getFullPath())) {
                    //didn't match any of the extensions, don't upload
                    Toast.makeText(getActivity(), R.string.oneOrMoreExtensionNotAllowed, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        startUploadService();
    }

    public void startUploadService(){
        setViewsToUploading();
        Bundle bundle;
        Intent intent = new Intent(getActivity(), FileUploadService.class);
        if (uploadType == FileUploadType.USER) {
            bundle = FileUploadService.getUserFilesBundle(files, parentFolderID);
            intent.setAction(FileUploadService.ACTION_USER_FILE);
        }
        else if (uploadType == FileUploadType.COURSE) {
            bundle = FileUploadService.getCourseFilesBundle(files, course.getId(), parentFolderID);
            intent.setAction(FileUploadService.ACTION_COURSE_FILE);
        }
        else if(uploadType == FileUploadType.MESSAGE){
            bundle = FileUploadService.getUserFilesBundle(files, null);
            intent.setAction(FileUploadService.ACTION_USER_FILE);
        }
        else if (uploadType == FileUploadType.DISCUSSION){
            bundle = FileUploadService.getUserFilesBundle(files, null);
            intent.setAction(FileUploadService.ACTION_DISCUSSION_ATTACHMENT);
        }
        else if (uploadType == FileUploadType.QUIZ){
            bundle = FileUploadService.getQuizFileBundle(files, parentFolderID, quizQuestionId, position, courseId, quizId);
            intent.setAction(FileUploadService.ACTION_QUIZ_FILE);
        }
        else {
            bundle = FileUploadService.getAssignmentSubmissionBundle(files, course.getId(), assignment.getId());
            intent.setAction(FileUploadService.ACTION_ASSIGNMENT_SUBMISSION);
        }
        intent.putExtras(bundle);
        getActivity().startService(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //no result
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }
        if (requestCode == RequestCodes.CAMERA_PIC_REQUEST) {
            //don't want to directly decode the stream because we could get out of memory errors
            //try to get the information from the intent that we saved earlier
            Uri mCapturedImageURI = null;
            if (mCapturedImageURI == null) {
                //save the intent information in case we get booted from memory.
                SharedPreferences settings = getActivity().getSharedPreferences(ApplicationManager.PREF_NAME, Context.MODE_PRIVATE);

                mCapturedImageURI = (Uri) Uri.parse(settings.getString("ProfileFragment-URI", null));
            }
            //if it's still null, tell the user there is an error and return.
            if (mCapturedImageURI == null) {
                Toast.makeText(getActivity(), R.string.errorGettingPhoto, Toast.LENGTH_SHORT).show();
                return;
            }

            new GetUriContentsAsyncTask(getActivity(), mCapturedImageURI).execute();
        }
        else{
            if (data != null && data.getData() != null) {
                Uri imageURI = data.getData();
                new GetUriContentsAsyncTask(getActivity(), imageURI).execute();
            }
        }
    }

    private void handleUriContents(){
        //we only want to open the dialog in the beginning if we're not coming from an external source (sharing)
        if(uploadType == FileUploadType.MESSAGE || uploadType == FileUploadType.DISCUSSION) {
            return; // Do nothing
        }else if (submitUri == null) {
            //open up the fragment dialog
            FileSourceDialogStyled newFragment = FileSourceDialogStyled.newInstance();
            newFragment.setListener(this);
            newFragment.setCancelable(true);
            //use this so we can call this fragment's onActivityResult function from the dialog
            newFragment.setTargetFragment(FileUploadDialog.this, 500);
            newFragment.show(getChildFragmentManager(), FileSourceDialogStyled.TAG);
        } else {
            new GetUriContentsAsyncTask(getActivity(), submitUri).execute();
        }
    }

    private void initProgressDialog(){
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.uploadingFile));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Views
    ///////////////////////////////////////////////////////////////////////////
    public void setDialogMargins(){
        Window window = getDialog().getWindow();
        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        float screenWidth = displayMetrics.widthPixels;
        float screenHeight = displayMetrics.heightPixels;
        int height = Math.round(screenHeight - (ViewUtils.convertDipsToPixels(12, getActivity()) * 2)); // * 2 for margin on each side
        int width = Math.round(screenWidth - (ViewUtils.convertDipsToPixels(2, getActivity()) * 2)); // * 2 for margin on each side
        window.setLayout(width, height);
    }

    private View initViews() {
        final LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View rootView = inflater.inflate(R.layout.dialog_file_upload, null);

        dialogContent = (RelativeLayout) rootView.findViewById(R.id.fileUploadContents);

        // animated header views
        title = (TextView) rootView.findViewById(R.id.dialogTitle);
        title.setText(dialogTitle);

        subtitle = (TextView) rootView.findViewById(R.id.dialogSubtitle);
        subtitle.setText(dialogSubTitle);

        avatar = (CircleImageView) rootView.findViewById(R.id.avatar);
        ProfileUtils.configureAvatarView(getActivity(), user, avatar);

        // footer
        addFileButton = (Button)rootView.findViewById(R.id.addFileButton);
        addFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Option to restrict to one file, alert user
                if(isOneFileOnly && !files.isEmpty()) {
                    Toast.makeText(getContext(), R.string.oneFileOnly, Toast.LENGTH_SHORT).show();
                } else {
                    FileSourceDialogStyled newFragment = FileSourceDialogStyled.newInstance();
                    newFragment.setCancelable(true);
                    newFragment.setListener(FileUploadDialog.this);
                    //use this so we can call this fragment's onActivityResult function from the dialog
                    newFragment.setTargetFragment(FileUploadDialog.this, 500);
                    newFragment.show(getChildFragmentManager(), FileSourceDialogStyled.TAG);
                }
            }
        });

        // listview
        ListView fileList = (ListView) rootView.findViewById(R.id.fileList);
        fileList.setAdapter(adapter);
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FileSubmitObject fileSubmitObject = files.get(i);

                Intent newIntent = new Intent(android.content.Intent.ACTION_VIEW);
                File file = new File(fileSubmitObject.getFullPath());
                file.setReadable(true);
                newIntent.setDataAndType(Uri.fromFile(file), files.get(i).getContentType());

                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    getActivity().startActivity(newIntent);
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), R.string.noApps, Toast.LENGTH_SHORT).show();
                }
            }
        });
        allowedExtensions = (TextView) rootView.findViewById(R.id.allowedExtensions);
        checkAllowedExtensions();
        setRevealContentsListener();

        return rootView;
    }

    private void setRevealContentsListener(){
        final Animation avatarAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.ease_in_shrink);
        final Animation titleAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.ease_in_bottom);
        avatar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                AnimationHelpers.removeGlobalLayoutListeners(avatar, this);
                ((MaterialDialog)getDialog()).getActionButton(DialogAction.POSITIVE).startAnimation(titleAnimation);
                ((MaterialDialog)getDialog()).getActionButton(DialogAction.NEGATIVE).startAnimation(titleAnimation);
                avatar.setVisibility(View.VISIBLE);
                avatar.startAnimation(avatarAnimation);
                subtitle.startAnimation(titleAnimation);
                title.startAnimation(titleAnimation);
            }
        });

        dialogContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                AnimationHelpers.removeGlobalLayoutListeners(dialogContent, this);

                final Animator revealAnimator = AnimationHelpers.createRevealAnimator(dialogContent);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialogContent.setVisibility(View.VISIBLE);
                        revealAnimator.start();
                    }
                }, 600);
            }
        });
    }

    private void setViewsToUploading(){
        adapter.setFilesToUploading();
        addFileButton.setEnabled(false);
        ((MaterialDialog)getDialog()).setActionButton(DialogAction.POSITIVE, getString(R.string.done));
        ((MaterialDialog)getDialog()).getActionButton(DialogAction.POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                Activity activity = getActivity();
                if(activity instanceof ShareFileDestinationDialog.DialogCloseListener){
                    ((ShareFileDestinationDialog.DialogCloseListener)activity).onCancel(getDialog());
                }
            }
        });
        ((MaterialDialog)getDialog()).getActionButton(DialogAction.NEGATIVE).setEnabled(false);

    }
    ///////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onSourceSelected(Intent intent, int requestCode) {
        if(getParentFragment() != null){
            getParentFragment().startActivityForResult(intent, requestCode);
        }else{
            FileUploadDialog.this.startActivityForResult(intent, requestCode);
        }
    }

    private void registerReceivers() {
        uploadBroadcastReceiver = getSingleUploadCompleted();
        errorBroadcastReceiver = getErrorReceiver();
        allUploadsCompleteBroadcastReceiver = getAllUploadsCompleted();

        getActivity().registerReceiver( uploadBroadcastReceiver, new IntentFilter(FileUploadService.UPLOAD_COMPLETED));
        getActivity().registerReceiver(errorBroadcastReceiver, new IntentFilter(FileUploadService.UPLOAD_ERROR));
        getActivity().registerReceiver(allUploadsCompleteBroadcastReceiver, new IntentFilter(FileUploadService.ALL_UPLOADS_COMPLETED));

        needsUnregister = true;
    }

    private void unregisterReceivers() {
        if(getActivity() == null || !needsUnregister){return;}

        if(uploadBroadcastReceiver != null){
            getActivity().unregisterReceiver(uploadBroadcastReceiver);
            uploadBroadcastReceiver = null;
        }

        if(errorBroadcastReceiver != null){
            getActivity().unregisterReceiver(errorBroadcastReceiver);
            errorBroadcastReceiver = null;
        }

        if(allUploadsCompleteBroadcastReceiver != null){
            getActivity().unregisterReceiver(allUploadsCompleteBroadcastReceiver);
            allUploadsCompleteBroadcastReceiver = null;
        }

        needsUnregister = false;
    }

    private BroadcastReceiver getSingleUploadCompleted() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if(!isAdded()){return;}

                final Bundle bundle = intent.getExtras();
                FileSubmitObject fso = bundle.getParcelable(Const.FILENAME);
                adapter.setFileState(fso, FileSubmitObject.STATE.COMPLETE);
            }
        };
    }

    private BroadcastReceiver getAllUploadsCompleted() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if(!isAdded()){return;}

                Toast.makeText(getActivity(), R.string.filesUploadedSuccessfully, Toast.LENGTH_SHORT).show();
                dismiss();
                if (dialogLifecycleCallback != null) {
                    dialogLifecycleCallback.onAllUploadsComplete(getDialog());
                }
            }
        };
    }

    private BroadcastReceiver getErrorReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if(!isAdded()){return;}

                final Bundle bundle = intent.getExtras();
                String errorMessage = bundle.getString(Const.MESSAGE);
                if(null == errorMessage || "".equals(errorMessage)){
                    errorMessage = getString(R.string.errorUploadingFile);
                }
                Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
                dismiss();
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////
    private boolean checkIfFileSubmissionAllowed() {
        if (assignment != null) {
            return (assignment.getSubmissionTypes().contains(Assignment.SUBMISSION_TYPE.ONLINE_UPLOAD));
        }
        return false;
    }

    //used when the user hits the submit button after sharing files, we want to make sure they are allowed
    private boolean isExtensionAllowed(String filePath) {
        if (assignment != null && (assignment.getAllowedExtensions() == null || assignment.getAllowedExtensions().size() == 0)) {
            //there is an assignment, but no extension restriction...
            return true;
        }
        //get the extension and compare it to the list of allowed extensions
        int index = filePath.lastIndexOf(".");
        if (assignment != null && index != -1) {
            String ext = filePath.substring(index + 1);
            for (int i = 0; i < assignment.getAllowedExtensions().size(); i++) {
                if (assignment.getAllowedExtensions().get(i).trim().equalsIgnoreCase(ext)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    private boolean addIfExtensionAllowed(FileSubmitObject fileSubmitObject) {
        if (assignment != null && (assignment.getAllowedExtensions() == null || assignment.getAllowedExtensions().size() == 0)) {
            addToFileSubmitObjects(fileSubmitObject);
            return true;
        }

        //get the extension and compare it to the list of allowed extensions
        int index = fileSubmitObject.getFullPath().lastIndexOf(".");
        if (assignment != null && index != -1) {
            String ext = fileSubmitObject.getFullPath().substring(index + 1);
            for (int i = 0; i < assignment.getAllowedExtensions().size(); i++) {
                if (assignment.getAllowedExtensions().get(i).trim().equalsIgnoreCase(ext)) {
                    addToFileSubmitObjects(fileSubmitObject);
                    return true;
                }
            }
            //didn't match any of the extensions, don't upload
            Toast.makeText(getActivity(), R.string.extensionNotAllowed, Toast.LENGTH_SHORT).show();
            return false;
        }

        //if we're sharing it from an external source we won't know which assignment they're trying to
        //submit to, so we won't know if there are any extension limits
        //also, the assignment and/or course could be null due to memory pressures
        if ((assignment == null || course == null)) {
            addToFileSubmitObjects(fileSubmitObject);

            return true;
        }
        //don't want to try to upload it since it's not allowed.
        Toast.makeText(getActivity(), R.string.extensionNotAllowed, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void addToFileSubmitObjects(FileSubmitObject fileSubmitObject) {
        files.add(fileSubmitObject);
        adapter.notifyDataSetChanged();
    }

    private void checkAllowedExtensions() {
        //if there are only certain file types that are allowed, let the user know
        if (assignment != null && assignment.getAllowedExtensions() != null && assignment.getAllowedExtensions().size() > 0) {
            allowedExtensions.setVisibility(View.VISIBLE);
            String extensions = getString(R.string.allowedExtensions);
            for (int i = 0; i < assignment.getAllowedExtensions().size(); i++) {
                extensions += assignment.getAllowedExtensions().get(i);
                if (assignment.getAllowedExtensions().size() > 1 && i < assignment.getAllowedExtensions().size() - 1) {
                    extensions += ",";
                }
            }
            allowedExtensions.setText(extensions);
        } else {
            allowedExtensions.setVisibility(View.GONE);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // AsyncTask
    ///////////////////////////////////////////////////////////////////////////
    private class GetUriContentsAsyncTask extends AsyncTask<Void, Void, FileSubmitObject> {

        private Context context;
        private Uri uri;

        GetUriContentsAsyncTask(Context context, Uri uri) {
            this.context = context;
            this.uri = uri;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage(getString(R.string.loadingFilesIndeterminate));
            progressDialog.show();
        }

        @Override
        protected FileSubmitObject doInBackground(Void... params) {
            ContentResolver cr = context.getContentResolver();
            String mimeType = FileUploadUtils.getFileMimeType(cr, uri);
            String fileName = FileUploadUtils.getFileNameWithDefault(cr, uri, mimeType);
            return FileUploadUtils.getFileSubmitObjectFromInputStream(context, uri, fileName, mimeType);
        }

        @Override
        protected void onPostExecute(FileSubmitObject submitObject) {
            progressDialog.dismiss();
            if("".equals(submitObject.getErrorMessage())){
                addIfExtensionAllowed(submitObject);
            }else{
                Toast.makeText(getActivity(), submitObject.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Data
    ///////////////////////////////////////////////////////////////////////////
    public static Bundle createBundle(Uri submitURI, FileUploadType type, Long parentFolderId) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.URI, submitURI);
        bundle.putSerializable(Const.UPLOAD_TYPE,type);
        if (parentFolderId != null) {
            bundle.putLong(Const.PARENT_FOLDER_ID, parentFolderId);
        }
        return bundle;
    }

    public static Bundle createAttachmentsBundle(String userName, ArrayList<FileSubmitObject> defaultFileList) {
        Bundle bundle = createBundle(null, FileUploadType.MESSAGE, null);
        bundle.putString(Const.USER, userName);
        bundle.putParcelableArrayList(Const.FILES, defaultFileList);
        return bundle;
    }

    public static Bundle createDiscussionsBundle(String userName, ArrayList<FileSubmitObject> defaultFileList) {
        Bundle bundle = createBundle(null, FileUploadType.DISCUSSION, null);
        bundle.putString(Const.USER, userName);
        bundle.putParcelableArrayList(Const.FILES, defaultFileList);
        return bundle;
    }

    public static Bundle createFilesBundle(Uri submitURI, Long parentFolderId) {
        Bundle bundle = createBundle(submitURI, FileUploadType.USER, parentFolderId);
        return bundle;
    }

    public static Bundle createCourseBundle(Uri submitURI, Course course, Long parentFolderId) {
        Bundle bundle = createBundle(submitURI, FileUploadType.COURSE, parentFolderId);
        bundle.putParcelable(Const.CANVAS_CONTEXT, course);
        return bundle;
    }

    public static Bundle createAssignmentBundle(Uri submitURI, Course course, Assignment assignment)  {
        Bundle bundle = createBundle(submitURI, FileUploadType.ASSIGNMENT, null);
        bundle.putParcelable(Const.CANVAS_CONTEXT, course);
        bundle.putParcelable(Const.ASSIGNMENT, assignment);
        return bundle;
    }

    public static Bundle createQuizFileBundle(long quizQuestionId, long courseId, long quizId, int position) {
        Bundle bundle = createBundle(null, FileUploadType.QUIZ, null);
        bundle.putLong(Const.QUIZ_ANSWER_ID, quizQuestionId);
        bundle.putLong(Const.QUIZ, quizId);
        bundle.putLong(Const.COURSE_ID, courseId);
        bundle.putInt(Const.POSITION, position);
        return bundle;
    }

    public void loadBundleData(){
        Bundle bundle = getArguments();

        user           = APIHelpers.getCacheUser(getActivity());
        uploadType     = (FileUploadType) bundle.getSerializable(Const.UPLOAD_TYPE);
        canvasContext  = bundle.getParcelable(Const.CANVAS_CONTEXT);
        assignment     = bundle.getParcelable(Const.ASSIGNMENT);
        submitUri      = bundle.getParcelable(Const.URI);
        quizQuestionId = bundle.getLong(Const.QUIZ_ANSWER_ID);
        quizId         = bundle.getLong(Const.QUIZ);
        courseId       = bundle.getLong(Const.COURSE_ID);
        position       = bundle.getInt(Const.POSITION);
        parentFolderID = (bundle.containsKey(Const.PARENT_FOLDER_ID)) ? bundle.getLong(Const.PARENT_FOLDER_ID) : null;
        course         = (uploadType != FileUploadType.USER)          ? (Course)canvasContext                  : null;

        // Get dialog headers
        if (uploadType == FileUploadType.ASSIGNMENT) {
            dialogTitle    = getString(R.string.assignmentHeader) +" " + assignment.getName();
            positiveText   = getString(R.string.turnIn);
            dialogSubTitle = canvasContext.getName();
            positiveColor = CanvasContextColor.getCachedColor(getActivity(), canvasContext);
        }
        else if(uploadType == FileUploadType.COURSE) {
            dialogTitle    = getString(R.string.uploadTo) +" "  + getString(R.string.uploadCourseFiles);
            positiveText   = getString(R.string.upload);
            dialogSubTitle = canvasContext.getName();
            positiveColor  = CanvasContextColor.getCachedColor(getActivity(), canvasContext);
        }
        else if(uploadType == FileUploadType.MESSAGE){
            dialogTitle    = getString(R.string.messageAttachments);
            positiveText   = getString(R.string.ok);
            dialogSubTitle = getString(R.string.with) + " " +bundle.getString(Const.USER);
            positiveColor  = getResources().getColor(R.color.courseGreen);
            files          = bundle.getParcelableArrayList(Const.FILES);
            positiveColor = CanvasContextColor.getCachedColor(getActivity(), canvasContext);
        }
        else if(uploadType == FileUploadType.DISCUSSION){
            dialogTitle    = getString(R.string.discussionAttachment);
            positiveText   = getString(R.string.ok);
            dialogSubTitle = getString(R.string.with) + " " +bundle.getString(Const.USER);
            positiveColor  = getResources().getColor(R.color.courseGreen);
            files          = bundle.getParcelableArrayList(Const.FILES);
            positiveColor = CanvasContextColor.getCachedColor(getActivity(), canvasContext);
        } else if (uploadType == FileUploadType.QUIZ) {
            isOneFileOnly  = true;
            dialogTitle    = getString(R.string.uploadTo)  +" " + getString(R.string.uploadMyFiles);
            positiveText   = getString(R.string.upload);
            dialogSubTitle = user.getName();
            positiveColor  = getResources().getColor(R.color.courseGreen);
        }
        else{
            dialogTitle    = getString(R.string.uploadTo)  +" " + getString(R.string.uploadMyFiles);
            positiveText   = getString(R.string.upload);
            dialogSubTitle = user.getName();
            positiveColor  = getResources().getColor(R.color.courseGreen);
        }
    }
}
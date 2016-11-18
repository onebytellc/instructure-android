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
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.loginapi.login.util.ColorUtils;
import com.instructure.pandautils.views.RippleView;
import com.instructure.candroid.R;
import com.instructure.candroid.activity.PandaAvatarActivity;
import com.instructure.candroid.activity.ProfileBackdropPickerActivity;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.candroid.util.FragUtils;
import com.instructure.candroid.util.LoggingUtility;
import com.instructure.candroid.view.CanvasEditTextView;
import com.instructure.canvasapi.api.AvatarAPI;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.UserAPI;
import com.instructure.canvasapi.api.compatibility_synchronous.UploadFileSynchronousAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Enrollment;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.canvasapi.utilities.Masquerading;
import com.instructure.canvasapi.utilities.UserCallback;
import com.instructure.loginapi.login.api.GlobalDataSyncAPI;
import com.instructure.loginapi.login.asynctasks.GlobalDataSyncPostTask;
import com.instructure.loginapi.login.model.GlobalDataSync;
import com.instructure.loginapi.login.util.ProfileUtils;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.FileUploadUtils;
import com.instructure.pandautils.utils.LoaderUtils;
import com.instructure.pandautils.utils.PermissionUtils;
import com.instructure.pandautils.utils.RequestCodes;
import com.instructure.pandautils.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.client.Response;


public class ProfileFragment extends OrientationChangeFragment implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<UploadFileSynchronousAPI.AvatarWrapper> {

    //View variables
    private View rootView, nameChangeWrapper;
    private EditText name;
    private RippleView files;
    private ImageView headerImage, nameChangeDone;
    private RelativeLayout clickContainer;
    private CircleImageView avatar;
    private Bundle loaderBundle = null;
    private TextView bio, enrollment;

    private Uri mCapturedImageURI;

    //Callbacks.
    private CanvasCallback<User> updateUserCallback;
    private UserCallback getUserCallback;
    private CanvasCallback<Course[]> coursesCallback;
    private CanvasCallback<User> updateCanvasCallback;
    private CanvasCallback<User> userPermissionCallback;

    //User
    private User user;

    //Logic
    ApplicationManager applicationManager;
    boolean hasNonTeacherEnrollment = false;
    boolean canUpdateName;
    boolean canUpdateAvatar;
    private boolean editMode;

    public static final String noPictureURL = "images/dotted_pic.png";

    private OnProfileChangedCallback onProfileChangedCallback;

    public interface OnProfileChangedCallback {
        public void onProfileChangedCallback();
        public void onProfileBackgroundImageChanged(String url);
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.DIALOG;
    }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.profile);
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(this, true);
    }

    @Override
    public View populateView(LayoutInflater inflater, ViewGroup container) {
        rootView = getLayoutInflater().inflate(R.layout.profile_fragment_layout, container, false);
        setupDialogToolbar(rootView);
        styleToolbar();
        clickContainer = (RelativeLayout)rootView.findViewById(R.id.clickContainer);
        name = (EditText) rootView.findViewById(R.id.userName);
        headerImage = (ImageView) rootView.findViewById(R.id.headerImage);
        nameChangeDone = (ImageView) rootView.findViewById(R.id.nameChangeDone);
        nameChangeDone.setImageDrawable(ColorUtils.colorIt(Color.BLACK, nameChangeDone.getDrawable()));
        nameChangeDone.setOnClickListener(nameChangedListener);
        nameChangeWrapper = rootView.findViewById(R.id.userNameWrapper);
        ColorDrawable colorDrawable = new ColorDrawable(Color.TRANSPARENT);
        avatar = (CircleImageView) rootView.findViewById(R.id.avatar);
        name.setBackgroundDrawable(colorDrawable);
        files = (RippleView) rootView.findViewById(R.id.files);

        enrollment = (TextView) rootView.findViewById(R.id.enrollment);
        bio = (TextView) rootView.findViewById(R.id.bio);
        bio.setMovementMethod(new ScrollingMovementMethod());

        if(!editMode){
            hideEditTextView();
        }

        String backgroundUrl = getImageBackgroundUrl(getActivity());
        loadBackdropImage(getContext(), backgroundUrl, headerImage);

        setUpCallbacks();
        setUpListeners();

        UserAPI.getSelf(getUserCallback);
        UserAPI.getSelfWithPermissions(userPermissionCallback);
        //Figure out if we have a non-student enrollment.
        CourseAPI.getAllFavoriteCourses(coursesCallback);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if(dialog != null && !isTablet(getActivity())) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(activity instanceof OnProfileChangedCallback){
            onProfileChangedCallback = ((OnProfileChangedCallback)activity);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RequestCodes.CAMERA_PIC_REQUEST && resultCode != Activity.RESULT_CANCELED) {
            Bitmap thumbnail = null;

            //don't want to directly decode the stream because we could get out of memory errors
            //if the URI is null, try to get the information from the intent that we saved earlier
            if(mCapturedImageURI == null) {
                //save the intent information in case we get booted from memory.
                SharedPreferences settings = getActivity().getSharedPreferences(ApplicationManager.PREF_NAME, 0);

                mCapturedImageURI = (Uri) Uri.parse(settings.getString("ProfileFragment-URI", null));
            }
            //if it's still null, tell the user there is an error and return.
            if(mCapturedImageURI == null) {
                showToast(R.string.errorGettingPhoto);
                return;
            }
            thumbnail = readBitmap(mCapturedImageURI, getActivity());

            if(thumbnail != null) {

                String[] projection = { MediaStore.Images.Media.DATA};
                String capturedImageFilePath = null;
                Cursor cursor = getActivity().getContentResolver().query(mCapturedImageURI, projection, null, null, null);
                int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if(cursor.moveToFirst()) {
                    capturedImageFilePath = cursor.getString(column_index_data);
                }

                int height = thumbnail.getHeight();
                int width = thumbnail.getWidth();
                int imageSize = getImageSize(height, width);
                float x = getStartX(width, imageSize);
                float y = getStartY(height, imageSize);

                thumbnail = cropThumbnail(x, y, height, width, imageSize, thumbnail);
                thumbnail = rotateIfNecessary(thumbnail, capturedImageFilePath);

                //overwrite the picture just taken
                try {
                    FileOutputStream out = new FileOutputStream(capturedImageFilePath);
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, out);
                } catch (Exception e) {
                    LoggingUtility.LogException(getContext(), e);
                }

                long size = thumbnail.getRowBytes() * thumbnail.getHeight();

                //let the bitmap be recycled
                thumbnail.recycle();

                String name = "profilePic.jpg";
                String contentType = "image/jpeg";

                //Start loader
                loaderBundle = createLoaderBundle(name, contentType, capturedImageFilePath, size);
                showProgressBar();
                LoaderUtils.restartLoaderWithBundle(getLoaderManager(), loaderBundle, this, R.id.avatarLoaderID);
            }
        }
        else if(requestCode == RequestCodes.PICK_IMAGE_GALLERY && resultCode != Activity.RESULT_CANCELED) {

            if(data.getData() != null) {
                Uri u = data.getData();
                String urlPath = u.getPath();

                if(u.getPath().contains("googleusercontent")){
                    urlPath = changeGoogleURL(urlPath);
                    ProfileFragment.this.user.setAvatarURL(urlPath);
                    AvatarAPI.updateAvatar(urlPath, updateCanvasCallback);
                    return;
                }

                Bitmap thumbnail = null;

                thumbnail = readBitmap(u, getActivity());

                String[] projection = { MediaStore.Images.Media.DATA};
                String capturedImageFilePath = null;
                CursorLoader loader = new CursorLoader(getContext(), u, projection, null, null, null);
                Cursor cursor = loader.loadInBackground();

                //on kindle fire's gallery app the cursor is null. according to http://stackoverflow.com/questions/9951006/android-image-picker-doesnt-work-on-kindle-fire
                //it's a firmware update's fault
                if(cursor == null) {
                    //let the user know the file wasn't updated
                    showToast(R.string.uploadAvatarFailMsg);
                    return;
                }
                int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                if(cursor.moveToFirst()) {
                    capturedImageFilePath = cursor.getString(column_index_data);
                }
                if(capturedImageFilePath == null && thumbnail == null) {
                    //let the user know the file wasn't updated
                    showToast(R.string.uploadAvatarFailMsg);
                    return;
                } else if(capturedImageFilePath == null) {
                    //it will get to this line with google photos cloud photos (pics that aren't stored on the device)
                    
                    // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
                    Uri tempUri = FileUploadUtils.getImageUri(getActivity().getContentResolver(), thumbnail);

                    // CALL THIS METHOD TO GET THE ACTUAL PATH
                    capturedImageFilePath = FileUploadUtils.getRealPathFromURI(getActivity().getContentResolver(), tempUri);
                }
                //don't want to overwrite the user's image, create a thumbnail copy...
                int dot = capturedImageFilePath.lastIndexOf('.');
                String newPath = capturedImageFilePath;
                if(dot != -1) {
                    String ext = capturedImageFilePath.substring(dot, capturedImageFilePath.length());
                    newPath = capturedImageFilePath.substring(0, dot - 1) + "_thumb" + ext;
                }
                //need to make a smaller version of the image...
                //keep the aspect ratio, but limit the height to 75
                int height = thumbnail.getHeight();
                int width = thumbnail.getWidth();
                int imageSize = getImageSize(height, width);
                float x = getStartX(width, imageSize);
                float y = getStartY(height, imageSize);

                thumbnail = cropThumbnail(x, y, height, width, imageSize, thumbnail);
                thumbnail = rotateIfNecessary(thumbnail, capturedImageFilePath);

                //save the smaller, rotated picture just selected
                try {
                    FileOutputStream out = new FileOutputStream(newPath);
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, out);
                } catch (Exception e) {
                    LoggingUtility.LogException(getActivity(), e);
                }

                long size = thumbnail.getRowBytes() * thumbnail.getHeight();

                //allow it to be garbage collected
                thumbnail.recycle();

                String name = "profilePic.jpg";
                String contentType = "image/jpeg";

                //Start loader
                loaderBundle = createLoaderBundle(name, contentType, newPath, size);
                showProgressBar();
                LoaderUtils.restartLoaderWithBundle(getLoaderManager(), loaderBundle, this, R.id.avatarLoaderID);
            }
        } else if(resultCode == Const.PROFILE_BACKGROUND_SELECTED_RESULT_CODE) {
            String backgroundUrl = data.getStringExtra(Const.URL);
            saveBackgroundImage(getContext(), ((TextUtils.isEmpty(backgroundUrl) ? "" : backgroundUrl)));
            loadBackdropImage(getContext(), backgroundUrl, headerImage);

            if(getActivity() instanceof OnProfileChangedCallback) {
                ((OnProfileChangedCallback)getActivity()).onProfileBackgroundImageChanged(backgroundUrl);
            }
        } else if(requestCode == Const.PANDA_AVATAR_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            if(data != null) {
                String pandaPath = data.getStringExtra(Const.PATH);
                long size = data.getLongExtra(Const.SIZE, 0);
                //the api will rename the avatar automatically for us
                loaderBundle = createLoaderBundle("pandaAvatar.png", "image/png", pandaPath, size);
                showProgressBar();
                LoaderUtils.restartLoaderWithBundle(getLoaderManager(), loaderBundle, this, R.id.avatarLoaderID);
            }
        }
    }

    /**
     *
     * @param x horizontal starting point for crop
     * @param y vertical starting point for crop
     * @param height height of thumbnail prior to crop
     * @param width width of thumbnail prior to crop
     * @param imageSize max size of square for cropping
     * @param thumbnail reference to bitmap
     * @return modified bitmap
     */
    private Bitmap cropThumbnail(float x, float y, int height, int width, int imageSize, Bitmap thumbnail){
        if(x == 0 && y != 0) {
            thumbnail = Bitmap.createScaledBitmap(thumbnail, width, imageSize, false);
        } else if(x != 0 && y == 0) {
            thumbnail = Bitmap.createScaledBitmap(thumbnail, imageSize, height, false);
        } else if(x == 0 && y == 0) {
            thumbnail = Bitmap.createScaledBitmap(thumbnail, imageSize, imageSize, false);
        } else {
            thumbnail = Bitmap.createBitmap(thumbnail, (int)x, (int)y, imageSize, imageSize);
        }

        return thumbnail;
    }

    private String changeGoogleURL(String url){
        int start = url.indexOf("http");
        int end = url.indexOf("-d");

        url = url.substring(start, end + 1);

        return url;
    }

    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);

        if (canUpdateName) {
            inflater.inflate(R.menu.menu_edit_username, menu);
        }

        if(canUpdateAvatar) {
            inflater.inflate(R.menu.menu_update_avatar, menu);
        }

        inflater.inflate(R.menu.menu_about_me, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Prevent all actions besides About when there is no network
        if(item.getItemId() != R.id.about && !CanvasRestAdapter.isNetworkAvaliable(getContext())) {
            Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_edit_username:
                showEditTextView();
                break;
            case R.id.menu_take_photo:
                newPhoto();
                break;
            case R.id.menu_choose_from_gallery:
                chooseFromGallery();
                break;
            case R.id.menu_set_to_default:
                AvatarAPI.updateAvatar(noPictureURL, updateCanvasCallback);
                break;
            case R.id.menu_choose_background_image:
                startActivityForResult(new Intent(getActivity(), ProfileBackdropPickerActivity.class), Const.PROFILE_BACKGROUND_SELECTED_RESULT_CODE);
                break;
            case R.id.menu_create_panda_avatar:
                startActivityForResult(new Intent(getActivity(), PandaAvatarActivity.class), Const.PANDA_AVATAR_RESULT_CODE);
                break;
            case R.id.about:
                if(user != null) {
                    AlertDialog dialog = new AlertDialog.Builder(getContext())
                            .setTitle(R.string.about)
                            .setView(R.layout.dialog_about)
                            .show();

                    if (dialog != null) {
                        TextView domain = (TextView) dialog.findViewById(R.id.domain);
                        TextView loginId = (TextView) dialog.findViewById(R.id.loginId);
                        TextView email = (TextView) dialog.findViewById(R.id.email);
                        TextView version = (TextView) dialog.findViewById(R.id.version);

                        domain.setText(APIHelpers.getDomain(getContext()));
                        loginId.setText(user.getLoginId());
                        email.setText(user.getEmail());
                        version.setText(getText(R.string.canvasVersionNum) + " " + ApplicationManager.getVersionName(getActivity()));
                    }
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void newPhoto(){
        //check to see if the device has a camera
        if(!Utils.hasCameraAvailable(getActivity())) {
        //this device doesn't have a camera, show a crouton that lets the user know
            showToast(R.string.noCameraOnDevice);
            return;
        }

        if(PermissionUtils.hasPermissions(getActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA)) {
            takeNewPhotoBecausePermissionsAlreadyGranted();
        } else {
            requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE, PermissionUtils.CAMERA), PermissionUtils.PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            if(PermissionUtils.allPermissionsGrantedResultSummary(grantResults)) {
                takeNewPhotoBecausePermissionsAlreadyGranted();
            } else {
                Toast.makeText(getActivity(), R.string.permissionDenied, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void takeNewPhotoBecausePermissionsAlreadyGranted() {
        //let the user take a picture
        //get the location of the saved picture
        String fileName = "profilePic_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);

        mCapturedImageURI = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (mCapturedImageURI != null) {
            //save the intent information in case we get booted from memory.
            SharedPreferences settings = getContext().getSharedPreferences(ApplicationManager.PREF_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("ProfileFragment-URI", mCapturedImageURI.toString());
            editor.apply();
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
        cameraIntent.putExtra(Const.IS_OVERRIDDEN, true);
        startActivityForResult(cameraIntent, RequestCodes.CAMERA_PIC_REQUEST);
    }

    private void chooseFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        File file = new File("/image/*");
        intent.setDataAndType(Uri.fromFile(file), "image/*");
        startActivityForResult(intent, RequestCodes.PICK_IMAGE_GALLERY);
    }


    private int getImageSize(int height, int width) {
        int imageSize = 512;

        //if the image is very small, we want to have a smaller "crop size"
        if(imageSize > width && imageSize > height){
            imageSize = 256;
        }
        int minSize = 0;

        if(height < width) {
            minSize = width;
        } else if(width <= height) {
            minSize = height;
        }

        if(minSize > imageSize) {
            minSize = imageSize;
        }
        return minSize;
    }

    private float getStartX(int width, int size){
        float x = 0;
        if(width > size){
            x = (width - size) / (float) 2;
        }

        return x;
    }

    private float getStartY(int height, int size){
        float y = 0;
        if(height > size){
            y = (height - size) / (float) 2;
        }

        return y;
    }

    private void styleToolbar() {
        if(getDialogToolbar() != null) {
            getDialogToolbar().setTitle("");
            getDialogToolbar().setBackgroundColor(getResources().getColor(R.color.semi_transparent));
        }
    }

    private void setUpListeners() {
        files.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                Navigation navigation = getNavigation();
                if(navigation != null) {
                    dismissAllowingStateLoss();
                    navigation.addFragment(FragUtils.getFrag(FileListFragment.class, getActivity()), Navigation.NavigationPosition.FILES);
                }
            }
        });

        clickContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editMode) {
                    hideEditTextView();
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        applicationManager = (ApplicationManager) getActivity().getApplication();

        if(savedInstanceState == null) {
            //if we rotate in edit mode we don't want it to kick us out of edit mode
            editMode = false;
        }

        //Restore loader if necessary
        LoaderUtils.restoreLoaderFromBundle(getLoaderManager(), savedInstanceState, this, R.id.avatarLoaderID);
        //Prevent the keyboard from popping up
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        LoaderUtils.saveLoaderBundle(outState, loaderBundle);
        super.onSaveInstanceState(outState);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Callbacks
    ///////////////////////////////////////////////////////////////////////////

    private View.OnClickListener nameChangedListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String nameText = name.getText().toString().trim();
            if(!TextUtils.isEmpty(nameText)){
                name.setText(nameText);
                hideEditTextView();

                UserAPI.updateShortName(nameText, updateUserCallback);
            }
        }
    };

    public void setUpCallbacks(){
        updateUserCallback = new CanvasCallback<User>(this) {

            @Override
            public void firstPage(User user, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()){
                    return;
                }
                name.setText(user.getShortName());
                APIHelpers.setCachedShortName(getContext(),user.getShortName());
                ProfileFragment.this.user.setShortName(user.getShortName());
                setUpUserAvatar();
                if(onProfileChangedCallback != null){
                    onProfileChangedCallback.onProfileChangedCallback();
                }
            }
        };

        getUserCallback = new UserCallback(this) {
            @Override
            public void cachedUser(User user) {
                //need to call cache in case we're masquerading or if there is no network
                if(Masquerading.isMasquerading(getActivity()) || !CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                    user(user, null);
                }
            }

            @Override
            public void user(User user, Response response) {
                ProfileFragment.this.user = user;
                setUpUserViews();
            }
        };

        coursesCallback = new CanvasCallback<Course[]>(this) {
            @Override
            public void firstPage(Course[] courses, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()){
                    return;
                }
                for(Course course: courses){
                    if(!course.isTeacher() && !course.isObserver()){
                        hasNonTeacherEnrollment = true;
                        break;
                    }
                }
            }
        };

        updateCanvasCallback = new CanvasCallback<User>(ProfileFragment.this) {
            @Override public void cache(User user) {}

            @Override
            public void firstPage(User user, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()){
                    return;
                }
                ProfileFragment.this.user = user;
                setUpUserAvatar();
                if(onProfileChangedCallback != null){
                    onProfileChangedCallback.onProfileChangedCallback();
                }
            }
        };

        userPermissionCallback = new CanvasCallback<User>(ProfileFragment.this) {
            @Override public void cache(User user) {}

            @Override
            public void firstPage(User user, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()){
                    return;
                }
                canUpdateAvatar = user.canUpdateAvatar();
                canUpdateName = user.canUpdateName();
                getActivity().invalidateOptionsMenu();
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Loader test
    /////////////////////////////////////////////////////////////////////////

    public static class PostAvatarLoader extends android.support.v4.content.AsyncTaskLoader<UploadFileSynchronousAPI.AvatarWrapper> {
        final String name;
        final String contentType;
        final String path;
        final long size;

        public PostAvatarLoader(Context context, String name, String contentType, String path, long size) {
            super(context);

            this.name = name;
            this.contentType = contentType;
            this.path = path;
            this.size = size;
        }

        @Override
        public UploadFileSynchronousAPI.AvatarWrapper loadInBackground() {
            return UploadFileSynchronousAPI.postAvatar(name, size, contentType, path, getContext());
        }

        @Override protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

    }

    /**
     * Used to instantiate the PostAvatarLoader
     * @param id - a unique ID
     * @param args - a bundle, containing:
     *             -String name
     *             -String content type
     *             -String path
     *             -int size
     * @return
     */
    @Override
    public android.support.v4.content.Loader<UploadFileSynchronousAPI.AvatarWrapper> onCreateLoader(int id, Bundle args) {
        return new PostAvatarLoader(getActivity(), args.getString(Const.NAME), args.getString(Const.CONTENT_TYPE), args.getString(Const.PATH), args.getLong(Const.SIZE));
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<UploadFileSynchronousAPI.AvatarWrapper> loader, UploadFileSynchronousAPI.AvatarWrapper data) {
        hideProgressBar();

        if(data != null && data.avatar != null) {
            ProfileFragment.this.user.setAvatarURL(data.avatar.getUrl());
            setUpUserAvatar();
            AvatarAPI.updateAvatar((data).avatar.getUrl(), updateCanvasCallback);
        }
        else if(data != null) {
            //check to see the error messages
            if(data.error == UploadFileSynchronousAPI.AvatarError.QUOTA_EXCEEDED) {
                showToast(R.string.fileQuotaExceeded);
            }
            else if(data.error == UploadFileSynchronousAPI.AvatarError.UNKNOWN) {
                showToast(R.string.errorUploadingFile);
            }
        }
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<UploadFileSynchronousAPI.AvatarWrapper> loader) {}


    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    private void showEditTextView(){
        editMode = true;
        nameChangeDone.setVisibility(View.VISIBLE);
        nameChangeWrapper.setBackgroundResource(R.drawable.profile_name_edit_bg);
        name.setEnabled(true);
        name.setSelection(name.getText().length());
    }

    private void hideEditTextView(){
        editMode = false;
        nameChangeDone.setVisibility(View.GONE);
        nameChangeWrapper.setBackgroundDrawable(null);
        name.setEnabled(false);
    }

    private void setUpUserViews(){
        name.setText(user.getShortName());

        String enrolledAs = "";
        List<Enrollment> enrollments = user.getEnrollments();
        for(Enrollment enrollment : enrollments) {
            enrolledAs = enrolledAs + enrollment.getType() + ",";
        }

        if(enrolledAs.endsWith(",")) {
            enrolledAs = enrolledAs.substring(0, enrolledAs.length() - 1);
        }

        if(TextUtils.isEmpty(enrolledAs)) {
            enrollment.setVisibility(View.GONE);
        } else {
            enrollment.setText(enrolledAs);
        }

        //show the bio if one exists
        if(!TextUtils.isEmpty(user.getBio()) && !user.getBio().equals("null")) {
            bio.setText(user.getBio());
        }
        setUpUserAvatar();
    }

    private void setUpUserAvatar(){
        ProfileUtils.configureAvatarView(getContext(), user, avatar);
    }

    // Helper method to read bitmap without using so much memory
    public static Bitmap readBitmap(Uri selectedImage, Activity activity) {
        Bitmap bm = null;
        BitmapFactory.Options options = new BitmapFactory.Options();

        //subsample original image
        options.inSampleSize = 5;
        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = activity.getContentResolver().openAssetFileDescriptor(selectedImage,"r");
        } catch (FileNotFoundException e) {
            LoggingUtility.LogException(activity, e);
            Toast.makeText(activity, R.string.fileNotFound, Toast.LENGTH_SHORT).show();
        }
        finally{
            try {
                bm = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor(), null, options);
                fileDescriptor.close();
            } catch (Exception e) {
                LoggingUtility.LogException(activity, e);
                Toast.makeText(activity, R.string.errorGettingPhoto, Toast.LENGTH_SHORT).show();
            }
        }
        return bm;
    }

    //helper method to rotate the images
    public Bitmap rotateIfNecessary(Bitmap thumbnail, String path) {
        int rotate = 0;
        File imageFile = new File(path);
        ExifInterface exif;
        try {
            exif = new ExifInterface(
                    imageFile.getAbsolutePath());

            int orientation2 = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation2) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (IOException e1) {
            LoggingUtility.LogException(getActivity(), e1);
        }
        //rotate the image if necessary
        Bitmap rotatedBitmap = null;

        /*
      	normal landscape: 0
		normal portrait: 90
		upside-down landscape: 180
		upside-down portrait: 270
		image not found: -1
         */

        if(rotate == 90) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            rotatedBitmap = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);
        }
        else if(rotate == 180) {
            Matrix matrix = new Matrix();
            matrix.postRotate(180);
            rotatedBitmap = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);
        }
        else if(rotate == 270) {
            Matrix matrix = new Matrix();
            matrix.postRotate(270);
            rotatedBitmap = Bitmap.createBitmap(thumbnail, 0, 0, thumbnail.getWidth(), thumbnail.getHeight(), matrix, true);
        }

        if(rotatedBitmap == null) {
            rotatedBitmap = thumbnail;
        }
        return rotatedBitmap;
    }

    private Bundle createLoaderBundle(String name, String contentType, String path, long size){
        Bundle bundle = new Bundle();
        bundle.putString(Const.NAME, name);
        bundle.putString(Const.CONTENT_TYPE, contentType);
        bundle.putString(Const.PATH, path);
        bundle.putLong(Const.SIZE, size);

        return bundle;
    }
    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        if (extras.containsKey(Const.USER)) {
            user = (User) extras.getParcelable(Const.USER);
        }
    }

    public static Bundle createBundle(User user, CanvasContext canvasContext, FRAGMENT_PLACEMENT placement) {
        Bundle extras = createBundle(canvasContext);
        extras.putParcelable(Const.USER, user);
        extras.putSerializable(Const.PLACEMENT, placement);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, FRAGMENT_PLACEMENT placement) {
        Bundle extras = createBundle(canvasContext);
        extras.putSerializable(Const.PLACEMENT, placement);
        return extras;
    }

    public static void saveBackgroundImage(Context context, String url) {
        new GlobalDataSyncPostTask(context, GlobalDataSyncAPI.NAMESPACE.MOBILE_CANVAS_USER_BACKDROP_IMAGE).execute(new GlobalDataSync(url));
    }

    public static String getImageBackgroundUrl(Context context) {
        GlobalDataSync data = GlobalDataSync.getCachedGlobalData(context, GlobalDataSyncAPI.NAMESPACE.MOBILE_CANVAS_USER_BACKDROP_IMAGE);
        return data.data;
    }

    public static void loadBackdropImage(Context context, String url, ImageView imageView) {
        if(TextUtils.isEmpty(url)) {
            imageView.setImageResource(R.drawable.default_backdrop_img);
        } else {
            Picasso.with(context)
                    .load(url)
                    .placeholder(R.drawable.ic_empty)
                    .fit()
                    .centerCrop()
                    .into(imageView);
        }
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}

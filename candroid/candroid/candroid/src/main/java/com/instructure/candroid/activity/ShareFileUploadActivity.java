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

package com.instructure.candroid.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;


import com.instructure.candroid.R;
import com.instructure.candroid.dialog.FileUploadDialog;
import com.instructure.candroid.dialog.ShareFileDestinationDialog;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.AnimationHelpers;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class ShareFileUploadActivity extends FragmentActivity implements ShareFileDestinationDialog.DialogCloseListener{

    private CanvasCallback<Course[]> courseCanvasCallback;
    private DialogFragment uploadFileSourceFragment;
    private ArrayList<Course> courses;

    private boolean isTeacher;
    private boolean isStudent;

    private Uri sharedURI;

    private View rootView;
    ///////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //This activity gets called by another application to share a file with ours.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_file);
        rootView = findViewById(R.id.share_activity_root);
        revealBackground();

        //Log to GA.
        Analytics.trackAppFlow(this);

        checkLoggedIn();
        setupCallbacks();

        sharedURI = parseIntentType();
        CourseAPI.getAllCourses(courseCanvasCallback);
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    private void revealBackground(){
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                AnimationHelpers.removeGlobalLayoutListeners(rootView, this);
                AnimationHelpers.createRevealAnimator(rootView).start();
            }
        });
    }

    private void checkLoggedIn(){
        final boolean isLoggedIn = ((ApplicationManager)getApplicationContext()).isUserLoggedIn();
        if(!isLoggedIn){
            Intent intent = LoginActivity.createIntent(this, true, getString(R.string.notLoggedIn));
            startActivity(intent);
            finish();
        }
    }

    public void setupCallbacks(){
        courseCanvasCallback = new CanvasCallback<Course[]> (APIHelpers.statusDelegateWithContext(this)) {
            @Override
            public void firstPage(Course[] courses, LinkHeaders linkHeaders, Response response) {
                ShareFileUploadActivity.this.courses = new ArrayList<>(Arrays.asList(courses));

                if(uploadFileSourceFragment == null){
                    showDestinationDialog();
                }
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                Toast.makeText(getContext(), R.string.uploadingFromSourceFailed, Toast.LENGTH_LONG).show();
                return true;
            }
        };
    }

    @Override
    public void onBackPressed() {
        if(uploadFileSourceFragment != null){
            uploadFileSourceFragment.dismissAllowingStateLoss();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if(uploadFileSourceFragment != null){uploadFileSourceFragment.dismissAllowingStateLoss();}

        super.onDestroy();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Logic
    ///////////////////////////////////////////////////////////////////////////
    private void showDestinationDialog(){
        for (Course course : courses) {
            if (course.isTeacher()) {
                isTeacher = true;
                if(isStudent){break;}
            }

            if (course.isStudent()) {
                isStudent = true;
                if(isTeacher){break;}
            }
        }

        if(sharedURI == null) {
            Toast.makeText(getApplicationContext(), R.string.uploadingFromSourceFailed, Toast.LENGTH_LONG).show();
        } else {
            uploadFileSourceFragment = ShareFileDestinationDialog.newInstance(ShareFileDestinationDialog.createBundle(sharedURI, courses, isTeacher, isStudent));
            uploadFileSourceFragment.show(getSupportFragmentManager(), ShareFileDestinationDialog.TAG);
        }
    }

    private Uri parseIntentType() {
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            return intent.getParcelableExtra(Intent.EXTRA_STREAM);
        }

        return null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        finish();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }

    @Override
    public void onNext(final Bundle bundle) {
        final int newColor = CanvasContextColor.getCachedColor(getApplicationContext(), (CanvasContext) bundle.getParcelable(Const.CANVAS_CONTEXT));
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), getResources().getColor(R.color.canvasRed), newColor);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                rootView.setBackgroundColor((Integer) animation.getAnimatedValue());
            }
        });
        colorAnimation.setDuration(500);
        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                FileUploadDialog uploadFileSourceFragment = FileUploadDialog.newInstance(getSupportFragmentManager(), bundle);
                uploadFileSourceFragment.show(getSupportFragmentManager(), FileUploadDialog.TAG);
            }
        });
        colorAnimation.start();

    }
}

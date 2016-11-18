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
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instructure.candroid.R;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.AnimationHelpers;
import com.instructure.canvasapi.api.CourseNicknameAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.CourseNickname;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.views.RippleView;

import retrofit.client.Response;

public class ColorPickerActivity extends FragmentActivity implements APIStatusDelegate {

    public static final Integer[] courseColors = {
            R.color.courseRed,
            R.color.courseHotPink,
            R.color.courseLavender,
            R.color.courseViolet,
            R.color.coursePurple,
            R.color.courseSlate,
            R.color.courseBlue,
            R.color.courseCyan,
            R.color.courseGreen,
            R.color.courseChartreuse,
            R.color.courseYellow,
            R.color.courseGold,
            R.color.courseOrange,
            R.color.coursePink,
            R.color.courseGrey};

    private View mRootView;
    private FrameLayout mColorPicker;
    private TextView mCourseName, mGroupName;
    private EditText mCourseNameEditText;
    private ImageView[] mColorChecks;
    private RelativeLayout mClickContainer;
    private View mCourseNameWrapper;
    private RippleView mDone;

    private CanvasContext mCanvasContext;
    private boolean mColorsChanged;
    private boolean mCourseNameChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }

        mCanvasContext = extras.getParcelable(Const.CANVAS_CONTEXT);

        if(savedInstanceState != null) {
            mCanvasContext = savedInstanceState.getParcelable(Const.CANVAS_CONTEXT);
            mColorsChanged = savedInstanceState.getBoolean(Const.COURSE_COLOR, false);
            mCourseNameChanged = savedInstanceState.getBoolean(Const.NAME, false);
        }

        final int[] currentColors = CanvasContextColor.getCachedColors(getApplicationContext(), mCanvasContext);

        initViews();
        setupViews(currentColors);

        if(savedInstanceState == null) {
            revealBackground();
        } else {
            mColorPicker.setVisibility(View.VISIBLE);
            mClickContainer.setVisibility(View.VISIBLE);
        }
    }

    private void initViews() {
        mRootView = findViewById(R.id.color_picker_activity_root);
        mDone = (RippleView) findViewById(R.id.doneRipple);
        mColorPicker = (FrameLayout) findViewById(R.id.colorPicker);
        mCourseName = (TextView) findViewById(R.id.courseName);
        mGroupName = (TextView) findViewById(R.id.groupName);
        mCourseNameEditText = (EditText) findViewById(R.id.courseNameEditText);
        mClickContainer = (RelativeLayout) findViewById(R.id.clickContainer);
        mCourseNameWrapper = findViewById(R.id.courseNameWrapper);
        mColorChecks = new ImageView[15];
        mColorChecks[0] = (ImageView) findViewById(R.id.color1);
        mColorChecks[1] = (ImageView) findViewById(R.id.color2);
        mColorChecks[2] = (ImageView) findViewById(R.id.color3);
        mColorChecks[3] = (ImageView) findViewById(R.id.color4);
        mColorChecks[4] = (ImageView) findViewById(R.id.color5);
        mColorChecks[5] = (ImageView) findViewById(R.id.color6);
        mColorChecks[6] = (ImageView) findViewById(R.id.color7);
        mColorChecks[7] = (ImageView) findViewById(R.id.color8);
        mColorChecks[8] = (ImageView) findViewById(R.id.color9);
        mColorChecks[9] = (ImageView) findViewById(R.id.color10);
        mColorChecks[10] = (ImageView) findViewById(R.id.color11);
        mColorChecks[11] = (ImageView) findViewById(R.id.color12);
        mColorChecks[12] = (ImageView) findViewById(R.id.color13);
        mColorChecks[13] = (ImageView) findViewById(R.id.color14);
        mColorChecks[14] = (ImageView) findViewById(R.id.color15);
    }

    private void setupViews(int[] currentColors) {
        //Setup our views
        setupViewsByColor(currentColors[0], currentColors[1], currentColors[0], currentColors[1]);

        if(mCanvasContext instanceof Course) {
            Course course = (Course)mCanvasContext;
            mCourseNameEditText.removeTextChangedListener(mCourseNameChangedTextWatcher);
            mCourseNameEditText.addTextChangedListener(mCourseNameChangedTextWatcher);

            //Original name will be null/empty if no nickname exists.
            if(TextUtils.isEmpty(course.getOriginalName())) {
                mCourseName.setText(course.getName());
            } else {
                mCourseName.setText(course.getOriginalName());
                mCourseNameEditText.setText(mCanvasContext.getName());
                mCourseNameEditText.setSelection(mCourseNameEditText.getText().length());
            }
        } else {
            mGroupName.setText(mCanvasContext.getName());
            mCourseName.setVisibility(View.INVISIBLE);
            mCourseNameEditText.setVisibility(View.INVISIBLE);
        }

        final int courseColor = CanvasContextColor.getCachedColor(getContext(), mCanvasContext);
        final Resources res = getContext().getResources();

        for (int i = 0; i < 15; i++) {
            mColorChecks[i].setTag(i);
            if (res.getColor(courseColors[i]) == courseColor) {
                mColorChecks[i].setImageResource(R.drawable.ic_cv_save_white_thin);
            } else {
                mColorChecks[i].setImageDrawable(null);
            }

            mColorChecks[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int[] oldColors = CanvasContextColor.getCachedColors(getApplicationContext(), mCanvasContext);
                    final int newColor = getResources().getColor(courseColors[(Integer) v.getTag()]);

                    setupViewsByColor(oldColors[0], oldColors[1], newColor, newColor);
                    CanvasContextColor.setNewColor(ColorPickerActivity.this, getContext(), mCanvasContext, newColor);
                    Analytics.trackColorSelected(ColorPickerActivity.this, newColor, CanvasContext.Type.isCourse(mCanvasContext));

                    setItemChecked((Integer) v.getTag());
                    mColorsChanged = true;
                }
            });
        }

        mDone.setOnRippleCompleteListener(new RippleView.OnRippleCompleteListener() {
            @Override
            public void onComplete(RippleView rippleView) {
                //Save the nickname if it's changed.
                String nickname = mCourseNameEditText.getText().toString();
                if(!nickname.equals(mCanvasContext.getName()) && nickname.length() > 0) {
                    CourseNicknameAPI.setNickname(mCanvasContext.getId(), nickname, new CanvasCallback<CourseNickname>(ColorPickerActivity.this) {
                        @Override
                        public void firstPage(CourseNickname courseNickname, LinkHeaders linkHeaders, Response response) {}
                    });
                }

                finish();
            }
        });


    }

    private TextWatcher mCourseNameChangedTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mCourseNameChanged = true;
        }
    };

    private void revealBackground() {
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                AnimationHelpers.removeGlobalLayoutListeners(mRootView, this);
                AnimationHelpers.createRevealAnimator(mRootView).start();
            }
        });

        mColorPicker.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                AnimationHelpers.removeGlobalLayoutListeners(mColorPicker, this);

                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.scale_slide_in_bottom_slow);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        mColorPicker.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        final Animator revealAnimator = AnimationHelpers.createRevealAnimator(mClickContainer);
                        mClickContainer.post(new Runnable() {
                            @Override
                            public void run() {
                                mClickContainer.setVisibility(View.VISIBLE);
                                revealAnimator.start();
                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mColorPicker.startAnimation(animation);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int statusBarColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && statusBarColor != Integer.MAX_VALUE) {
            getWindow().setStatusBarColor(statusBarColor);
            mColorPicker.setElevation(30);
        }
    }

    public void setupViewsByColor(final int oldColor, final int oldDarkColor, final int newColor, final int newDarkColor) {
        //Get the course/group color
        if (oldColor == newColor || oldDarkColor == newDarkColor) {
            mCourseNameWrapper.getBackground().setColorFilter(oldColor, PorterDuff.Mode.SRC_ATOP);
            mRootView.setBackgroundColor(oldDarkColor);
        } else {
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), oldColor, newColor);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final int color = (int) animation.getAnimatedValue();
                    mCourseNameWrapper.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                }
            });

            ValueAnimator backgroundColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), oldDarkColor, newDarkColor);
            backgroundColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    final int color = (int) animation.getAnimatedValue();
                    mRootView.setBackgroundColor(color);
                }
            });

            backgroundColorAnimation.setDuration(500);
            colorAnimation.setDuration(500);
            backgroundColorAnimation.start();
            colorAnimation.start();
        }
        setStatusBarColor(newColor);
    }

    public void setItemChecked(int position) {
        for(int i = 0; i < 15; i++) {
            if(i == position) {
                mColorChecks[i].setImageResource(R.drawable.ic_cv_save_white_thin);
            } else {
                mColorChecks[i].setImageDrawable(null);
            }
        }
    }

    @Override
    public void onCallbackStarted() {

    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {

    }

    @Override
    public void onNoNetwork() {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void finish() {
        Intent intent = new Intent(Const.COURSE_THING_CHANGED);
        Bundle extras = new Bundle();
        extras.putParcelable(Const.CANVAS_CONTEXT, mCanvasContext);
        extras.putInt(Const.POSITION, getIntent().getExtras().getInt(Const.POSITION, -1));
        extras.putInt(Const.NEW_COLOR, CanvasContextColor.getCachedColor(getContext(), mCanvasContext));
        extras.putBoolean(Const.COURSE_COLOR, mColorsChanged);
        if(mCourseNameChanged) {
            extras.putString(Const.NAME, mCourseNameEditText.getText().toString());
        }
        intent.putExtras(extras);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        super.finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Const.COURSE_COLOR, mColorsChanged);
        outState.putParcelable(Const.CANVAS_CONTEXT, mCanvasContext);
        outState.putBoolean(Const.NAME, mCourseNameChanged);
    }

    public static Intent createIntent(Context context, CanvasContext canvasContext, int itemPosition) {
        Bundle extras = new Bundle();
        extras.putParcelable(Const.CANVAS_CONTEXT, canvasContext);
        extras.putInt(Const.POSITION, itemPosition);

        Intent intent = new Intent(context, ColorPickerActivity.class);
        intent.putExtras(extras);
        return intent;
    }
}

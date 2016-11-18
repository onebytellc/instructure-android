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

package com.instructure.pandautils.utils;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.instructure.pandautils.R;
import com.instructure.pandautils.dialogs.TutorialDialog;

public class TutorialUtils {

    //NOTE: if you add something make sure you update ApplicationManager.safeClearSharedPreferences()
    public static enum TYPE {
        STAR_A_COURSE,
        COLOR_CHANGING_DIALOG,
        LANDING_PAGE,
        MY_COURSES,
        NOTIFICATION_PREFERENCES,
        NAVIGATION_SHORTCUTS,
        COURSE_GRADES,
        LOCK_SLIDING,
        HIDE_NAMES,
        FILTER_SECTIONS,
        SORTING_FAVORITES,
        ASSIGNMENT_DETAILS,
        SWIPE_BETWEEN_STUDENTS,
        FILTER_BY_GRADE,
        SUBMISSION_COMMENTS,
        NONE
    }

    private boolean mIsTestingModeOn = false;

    private static final String PREFIX = "tutorial_";
    private static final String MARK_ALL_AS_READ = "mark_all_as_read";
    private int TUTORIAL_ANIMATION = R.anim.pulse;

    private View mPulseContainer;
    private ImageView mPulse;
    private TYPE mType = TYPE.NONE;
    private FragmentActivity mContext;
    private String mMessage;
    private String mTitle;
    private  static Prefs mPrefs;

    public TutorialUtils(FragmentActivity context, Prefs prefs, ImageView pulse, TYPE type) {
        mPulse = pulse;
        mType = type;
        mPrefs = prefs;
        mContext = context;
    }

    /**
     * Overloaded constructor for TutorialUtils allows user to pass a container which holds the pulse imageview.
     * ClickListeners for tutorialdialog will be registered to the container, rather than the pulse imageview.
     * This is useful for overridden animations with translations.
     * @param context
     * @param prefs
     * @param pulseContainer the imageView for the pulse
     * @param type a type TutorialUtils.TYPE...
     */
    public TutorialUtils(FragmentActivity context, Prefs prefs, View pulseContainer, TYPE type) {
        mPulse = (ImageView) pulseContainer.findViewById(R.id.pulse);
        mType = type;
        mPrefs = prefs;
        mContext = context;
        mPulseContainer = pulseContainer;
    }

    public TutorialUtils setContent(String title, String message) {
        mTitle = title;
        mMessage = message;
        return this;
    }

    public TutorialUtils overrideAnimation(int animation){
        TUTORIAL_ANIMATION = animation;
        return this;
    }

    public TutorialUtils build() {
        // If a viewContainer was provided in the constructor, set click listeners to container. Otherwise, set them on the pulse imageview.
        final View view = (mPulseContainer == null) ? mPulse : mPulseContainer;
        if(!hasBeenViewed(mPrefs, mType) || mIsTestingModeOn) {
            final Animation pulse = getAnimation();
            mPulse.startAnimation(pulse);
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog();
                    setHasBeenViewed(mPrefs, mType, true);
                    pulse.cancel();
                    mPulse.clearAnimation();
                    view.setVisibility(View.GONE);
                }
            });
        } else {
            view.setVisibility(View.GONE);
        }

        return this;
    }

    private Animation getAnimation(){
        return AnimationUtils.loadAnimation(mContext, TUTORIAL_ANIMATION);
    }

    private void showDialog() {
        TutorialDialog dialog = new TutorialDialog();
        Bundle args = new Bundle();
        args.putString(Const.TITLE, mTitle);
        args.putString(Const.MESSAGE, mMessage);
        dialog.setArguments(args);
        dialog.show(mContext.getSupportFragmentManager(), TutorialDialog.class.getName());
    }

    public static boolean hasBeenViewed(Prefs prefs, TYPE type) {
        return prefs.load(PREFIX + type.toString(), false);
    }

    public static void setHasBeenViewed(Prefs prefs, TYPE type, boolean hasBeenViewed) {
        prefs.save(PREFIX + type.toString(), hasBeenViewed);
    }

    public static void resetAllTutorials(Prefs prefs) {
        updateTutorials(prefs, false);
    }

    public static void markAllTutorialsAsRead(Prefs prefs) {
        updateTutorials(prefs, true);
    }
    public static boolean areAllTutorialsRead(Prefs prefs) {
        return prefs.load(PREFIX + MARK_ALL_AS_READ, false);
    }

    private static void updateTutorials(Prefs prefs, boolean markAsRead) {
        for(TYPE type : TYPE.values()){
            prefs.save(PREFIX + type, markAsRead);
        }
        prefs.save(PREFIX + MARK_ALL_AS_READ, markAsRead);
    }
}

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

package com.instructure.parentapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.DiscussionTopicHeader;
import com.instructure.canvasapi2.models.ScheduleItem;
import com.instructure.canvasapi2.models.Student;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.utils.Const;
import com.instructure.parentapp.R;
import com.instructure.parentapp.fragments.AnnouncementFragment;
import com.instructure.parentapp.fragments.AssignmentFragment;
import com.instructure.parentapp.fragments.CourseSyllabusFragment;
import com.instructure.parentapp.fragments.CourseWeekFragment;
import com.instructure.parentapp.fragments.EventFragment;
import com.instructure.parentapp.fragments.ParentFragment;

import java.util.List;

/**
 * Copyright (c) 2014 Instructure. All rights reserved.
 */
public class DetailViewActivity extends BaseRouterActivity {

    public enum DETAIL_FRAGMENT {WEEK, ASSIGNMENT, EVENT, ANNOUNCEMENT, SYLLABUS}

    private boolean addFragmentEnabled = true;
    private static final int FRAGMENT_TRANSACTION_DELAY_TIME = 1000;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity_layout);

        if(savedInstanceState == null) {
            routeFromIntent(getIntent());
        }
    }

    private void routeFromIntent(Intent intent) {
        DETAIL_FRAGMENT fragmentType = (DETAIL_FRAGMENT) intent.getExtras().getSerializable(Const.FRAGMENT_TYPE);
        if(fragmentType != null) {
            Student student = intent.getExtras().getParcelable(Const.STUDENT);
            switch (fragmentType) {
                case WEEK:
                    Student user = intent.getExtras().getParcelable(Const.USER);
                    Course course = (Course) intent.getExtras().getSerializable(Const.COURSE);
                    if (user != null && course != null) {
                        addFragment(CourseWeekFragment.newInstance(user, course));
                    }
                    break;
                case ASSIGNMENT:
                    Assignment assignment = intent.getExtras().getParcelable(Const.ASSIGNMENT);
                    String courseName = intent.getExtras().getString(Const.NAME);
                    addFragment(AssignmentFragment.newInstance(assignment, courseName, student), false);
                    break;
                case ANNOUNCEMENT:
                    DiscussionTopicHeader announcement = intent.getExtras().getParcelable(Const.ANNOUNCEMENT);
                    String announcementCourseName = intent.getExtras().getString(Const.NAME);
                    addFragment(AnnouncementFragment.newInstance(announcement, announcementCourseName, student), false);
                    break;
                case EVENT:
                    ScheduleItem item = intent.getExtras().getParcelable(Const.SCHEDULE_ITEM);
                    addFragment(EventFragment.newInstance(item, student), false);
                    break;
                case SYLLABUS:
                    Course syllabusCourse = (Course) intent.getExtras().getSerializable(Const.COURSE);
                    addFragment(CourseSyllabusFragment.newInstance(syllabusCourse, student), false);
                    break;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        //A new assignment came in, set everything up again
        routeFromIntent(intent);
    }

    public Fragment getTopFragment() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            final List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if(!fragments.isEmpty()) {
                return fragments.get(getSupportFragmentManager().getBackStackEntryCount() - 1);
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        int entryCount = getSupportFragmentManager().getBackStackEntryCount();

        //If there is more than one fragment on the stack, we want to simply pop that fragment off
        if(entryCount > 1) {
            super.onBackPressed();
        }
        //If there is only one fragment on the stack, we want to close the activity containing it
        else if(entryCount == 1) {
            finish();
            overridePendingTransition(R.anim.none, R.anim.slide_to_bottom);
        }
    }

    public static Intent createIntent(Context context, DETAIL_FRAGMENT fragment){
        Intent intent = new Intent(context, DetailViewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Const.FRAGMENT_TYPE, fragment);

        return intent;
    }

    public static Intent createIntent(Context context, DETAIL_FRAGMENT fragment, ScheduleItem scheduleItem, Student student){
        Intent intent = createIntent(context, fragment);
        intent.putExtra(Const.SCHEDULE_ITEM, (Parcelable) scheduleItem);
        intent.putExtra(Const.STUDENT, (Parcelable) student);
        return intent;
    }

    public static Intent createIntent(Context context, DETAIL_FRAGMENT fragment, Student user, Course course){
        Intent intent = createIntent(context, fragment);
        intent.putExtra(Const.USER, (Parcelable) user);
        intent.putExtra(Const.COURSE, (Parcelable) course);
        return intent;
    }

    public static Intent createIntent(Context context, DETAIL_FRAGMENT fragment, Assignment assignment, String courseName, Student student){
        Intent intent = createIntent(context, fragment);
        intent.putExtra(Const.STUDENT, (Parcelable) student);
        intent.putExtra(Const.ASSIGNMENT, (Parcelable) assignment);
        intent.putExtra(Const.NAME, courseName);
        return intent;
    }

    public static Intent createIntent(Context context, DETAIL_FRAGMENT fragment, DiscussionTopicHeader announcement, String courseName, Student student){
        Intent intent = createIntent(context, fragment);
        intent.putExtra(Const.ANNOUNCEMENT, (Parcelable) announcement);
        intent.putExtra(Const.NAME, courseName);
        intent.putExtra(Const.STUDENT, (Parcelable) student);
        return intent;
    }

    public void addFragment(Fragment fragment) {
        if(fragment == null) {
            Utils.e("FAILED TO addFragmentToSomething with null fragment...");
            return;
        } else if(!addFragmentEnabled){
            Utils.e("FAILED TO addFragmentToSomething. Too many fragment transactions...");
            return;
        }

        setTransactionDelay();

        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.slide_from_bottom, android.R.anim.fade_out, R.anim.none, R.anim.slide_to_bottom);
            ft.add(R.id.fullscreen, fragment, fragment.getClass().getName());
            ft.addToBackStack(fragment.getClass().getName());
            ft.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Utils.e("Could not commit fragment transaction: " + e);
        }
    }

    public void addFragment(ParentFragment fragment, boolean ignoreDebounce){
        if(fragment == null) {
            Utils.e("FAILED TO addFragmentToSomething with null fragment...");
            return;
        } else if(!ignoreDebounce && !addFragmentEnabled){
            Utils.e("FAILED TO addFragmentToSomething. Too many fragment transactions...");
            return;
        }

        setTransactionDelay();

        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.slide_from_bottom, android.R.anim.fade_out, R.anim.none, R.anim.slide_to_bottom);
            ft.add(R.id.fullscreen, fragment, fragment.getClass().getName());
            ft.addToBackStack(fragment.getClass().getName());
            ft.commitAllowingStateLoss();
        } catch (IllegalStateException e) {
            Utils.e("Could not commit fragment transaction: " + e);
        }
    }


    private void setTransactionDelay() {
        addFragmentEnabled = false;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addFragmentEnabled = true;
            }
        }, FRAGMENT_TRANSACTION_DELAY_TIME);
    }
}

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

package com.instructure.androidpolling.app.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.asynctasks.LogoutAsyncTask;
import com.instructure.androidpolling.app.fragments.OpenPollExpandableListFragment;
import com.instructure.androidpolling.app.fragments.ParentFragment;
import com.instructure.androidpolling.app.fragments.ClosedPollListFragment;
import com.instructure.androidpolling.app.util.ApplicationManager;
import com.instructure.androidpolling.app.util.Constants;
import com.instructure.androidpolling.app.view.PagerSlidingTabStrip;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Poll;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.jfeinstein.jazzyviewpager.JazzyViewPager;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PollListActivity extends BaseActivity implements ParentFragment.OnUpdatePollListener, APIStatusDelegate {


    /**
     * The number of pages to show.
     */
    private static final int NUM_PAGES = 2;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    @BindView(R.id.pager) JazzyViewPager mPager;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @BindView(R.id.tabs) PagerSlidingTabStrip tabs;
    @BindView(R.id.no_poll_text) TextView noPollText;

    private CanvasCallback<Course[]> courseCallback;
    private ArrayList<Course> courseList;
    private User user;
    private boolean hasTeacherEnrollment;
    private ArrayList<Fragment> fragments;


    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle Overrides
    ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_list);
        ButterKnife.bind(this);

        //remember that the first view should be the student view
        ApplicationManager.setFirstView(this, false);

        user = APIHelpers.getCacheUser(PollListActivity.this);

        checkEnrollments(user);
        courseList = new ArrayList<Course>();
        courseList.addAll(Arrays.asList(ApplicationManager.getCourseList(this)));


        boolean pollsExist = checkPollsExist();

        if(!pollsExist) {
            displayEmptyState();
        }

        fragments = new ArrayList<Fragment>();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.COURSES_LIST, courseList);
        OpenPollExpandableListFragment openPollExpandableListFragment = new OpenPollExpandableListFragment();
        openPollExpandableListFragment.setArguments(bundle);

        fragments.add(openPollExpandableListFragment);

        //now add the closed list
        ClosedPollListFragment closedPollListFragment = new ClosedPollListFragment();
        Bundle closedBundle = new Bundle();
        closedBundle.putSerializable(Constants.COURSES_LIST, courseList);
        closedPollListFragment.setArguments(closedBundle);

        fragments.add(closedPollListFragment);

        // Instantiate a ViewPager and a PagerAdapter.
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setTransitionEffect(JazzyViewPager.TransitionEffect.Tablet);

        mPager.setAdapter(mPagerAdapter);
        // Bind the tabs to the ViewPager
        tabs.setViewPager(mPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(ApplicationManager.hasTeacherEnrollment(this)) {
            getMenuInflater().inflate(R.menu.switch_to_teacher_view, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_logout:
                LogoutAsyncTask logoutAsyncTask = new LogoutAsyncTask(this, null);
                logoutAsyncTask.execute();
                break;
            case R.id.action_switch_to_teacher:

                startActivity(FragmentManagerActivity.createIntent(this));
        }

        return super.onOptionsItemSelected(item);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    public void reloadData(View view) {
        ((ParentFragment)fragments.get(mPager.getCurrentItem())).reloadData();
    }

    private void displayEmptyState() {
        //we don't want to show the tabs
        mPager.setVisibility(View.GONE);
        tabs.setVisibility(View.GONE);
        noPollText.setVisibility(View.VISIBLE);
    }

    //TODO: API call to check for polls
    private boolean checkPollsExist() {
        return true;
    }


    /**
     * A simple pager adapter that represents 2 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        private final String[] TITLES = { getString(R.string.current_polls), getString(R.string.closed_polls) };

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            if(fragments == null) {
                return 0;
            }
            return fragments.size();
        }

        /**
         * Due to the limitations of the ViewPager class (which JazzyViewPager is built upon) in order to get the animations
         * to work correctly for more than 3 Views, you'll have to add the following to the instantiateItem method of your PagerAdapter.
         */
        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            Object obj = super.instantiateItem(container, position);
            mPager.setObjectForPosition(obj, position);
            return obj;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Override for OnUpdatePollListener
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onUpdatePoll(Poll poll, String fragmentTag) {

    }

    ///////////////////////////////////////////////////////////////////////////
    // Overrides for APIStatusDelegate
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {

    }

    @Override
    public void onNoNetwork() {

    }

    @Override
    public Context getContext() {
        return PollListActivity.this;
    }

    @Override
    public void onCallbackStarted() {

    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, PollListActivity.class);
        return intent;
    }

    public static Intent createIntent(Context context, Uri passedURI) {
        Intent intent = new Intent(context, PollListActivity.class);
        intent.putExtra(Constants.PASSED_URI, passedURI);
        return intent;
    }
}

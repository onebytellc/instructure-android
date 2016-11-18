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
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.instructure.candroid.R;
import com.instructure.candroid.activity.BaseRouterActivity;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.util.FragUtils;
import com.instructure.candroid.util.LoggingUtility;
import com.instructure.candroid.util.Param;
import com.instructure.canvasapi.api.AssignmentAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.StreamItem;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.RequestCodes;
import com.instructure.pandautils.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class AssignmentFragment extends ParentFragment implements SubmissionDetailsFragment.SubmissionDetailsFragmentCallback {

    public static final int ASSIGNMENT_TAB_DETAILS = 0;
    public static final int ASSIGNMENT_TAB_SUBMISSION = 1;
    public static final int ASSIGNMENT_TAB_GRADE = 2;
    public static final int NUMBER_OF_TABS = 3;

    private int currentTab = ASSIGNMENT_TAB_DETAILS;

    private TabLayout tabLayout;

    // views
    private ViewPager viewPager;

    // fragments
    private FragmentPagerDetailAdapter fragmentPagerAdapter;

    // model variables
    private Assignment assignment;
    private Course course;
    private long assignmentId;

    private String message;

    // callback
    private CanvasCallback<Assignment> assignmentCallback;
    private Bundle submissionFragmentBundle;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.DETAIL; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.assignment);
    }

    @Override
    protected String getActionbarTitle() {
        return assignment != null ? assignment.getName() : null;
    }
    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // DO NOT USE setRetainInstance. It breaks the FragmentStatePagerAdapter.
        // The error is "Attempt to invoke virtual method 'int java.util.ArrayList.size()' on a null object reference"
        // setRetainInstance(this, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        assignmentCallback = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.assignment_fragment, container, false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        tabLayout.setTabMode((!isTablet(getContext()) && !isLandscape(getContext()) ? TabLayout.MODE_SCROLLABLE : TabLayout.MODE_FIXED));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);
        viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setSaveFromParentEnabled(false); // Prevents a crash with FragmentStatePagerAdapter, when the EditAssignmentFragment is dismissed
        fragmentPagerAdapter = new FragmentPagerDetailAdapter(getChildFragmentManager(), false);
        viewPager.setAdapter(fragmentPagerAdapter);
        setupTabLayoutColors();
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setTabMode((!isTablet(getContext()) && !isLandscape(getContext()) ? TabLayout.MODE_SCROLLABLE : TabLayout.MODE_FIXED));
        tabLayout.setTabsFromPagerAdapter(fragmentPagerAdapter);
        tabLayout.setOnTabSelectedListener(tabSelectedListener);

        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getInt(Const.TAB_ID, 0);
        }
        // currentTab can either be save on orientation change or handleIntentExtras (when someone opens a link from an email)
        viewPager.setCurrentItem(currentTab);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(0);
            tabLayout.setElevation(Const.ACTIONBAR_ELEVATION);
        }
    }

    @Override
    public void onDestroyView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(Const.ACTIONBAR_ELEVATION);
        }
        super.onDestroyView();
    }

    private TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            viewPager.setCurrentItem(tab.getPosition(), true);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {}

        @Override
        public void onTabReselected(TabLayout.Tab tab) {}
    };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(Const.CANVAS_CONTEXT, course);
        savedInstanceState.putParcelable(Const.ASSIGNMENT, assignment);
        if (viewPager != null) {
            savedInstanceState.putInt(Const.TAB_ID, viewPager.getCurrentItem());
            currentTab = viewPager.getCurrentItem();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpCallback();
    }

    @Override
    public void onResume() {
        super.onResume();

        //Handle fragment detached exception. This can get called before onCreate's asynchronous finish happens.
        if(course != null) {
            AssignmentAPI.getAssignment(course.getId(), assignmentId, assignmentCallback);
        }
    }

    @Override
    public boolean handleBackPressed() {
        if (getAssignmentDetailsFragment() != null) {
            // Handles closing of fullscreen video (<sarcasm> Yay! nested fragments </sarcasm>)
            return getAssignmentDetailsFragment().handleBackPressed();
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RequestCodes.EDIT_ASSIGNMENT && resultCode == Activity.RESULT_OK && intent != null ) {
            if (intent.hasExtra(Const.ASSIGNMENT)) {
                this.assignment = intent.getParcelableExtra(Const.ASSIGNMENT);
                if (getAssignmentDetailsFragment() != null) {
                    getAssignmentDetailsFragment().setAssignmentWithNotification(this.assignment, this.message, false, false);
                }
            }
        }
   }

    ///////////////////////////////////////////////////////////////////////////
    // View
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);
        if (course != null && course.isTeacher()) {
            inflater.inflate(R.menu.edit_assignment_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.menu_edit_assignment:
                if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                    Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                    return true;
                }
                Navigation navigation = getNavigation();
                if(navigation != null){
                    navigation.addFragment(FragUtils.getFrag(EditAssignmentDetailsFragment.class, EditAssignmentDetailsFragment.createBundle(assignment, course)));
                }
                return true;
        }

        return super.onOptionsItemSelected(menuItem);
    }

    public void updatedAssignment(Assignment assignment) {
        populateFragments(assignment, false, false);
    }

    /**
     * For explanation of isWithinAnotherCallback and isCached refer to comment in {@link com.instructure.candroid.activity.CallbackActivity#getUserSelf}
     */
    private void populateFragments(Assignment assignment, boolean isWithinAnotherCallback, boolean isCached) {
        if (fragmentPagerAdapter == null) {
            return;
        }

        if (assignment.isLocked()) {
            fragmentPagerAdapter = new FragmentPagerDetailAdapter(getChildFragmentManager(), true); // recreate the adapter, because of slidingTabLayout's assumption that viewpager won't change size.
            viewPager.setAdapter(fragmentPagerAdapter);
        }

        for (int i = 0; i < NUMBER_OF_TABS; i++) {
            Fragment fragment = fragmentPagerAdapter.getRegisteredFragment(i);
            if (fragment == null) { continue; }
            if (fragment instanceof AssignmentDetailsFragment) {
                ((AssignmentDetailsFragment) fragment).setAssignment(assignment, isWithinAnotherCallback, isCached);
            } else if (fragment instanceof SubmissionDetailsFragment) {
                ((SubmissionDetailsFragment) fragment).setAssignmentFragment(new WeakReference<>(this));
                ((SubmissionDetailsFragment) fragment).setAssignment(assignment, isWithinAnotherCallback, isCached);
                ((SubmissionDetailsFragment) fragment).setSubmissionDetailsFragmentCallback(this);
            } else if (fragment instanceof RubricFragment) {
                ((RubricFragment) fragment).setAssignment(assignment, isWithinAnotherCallback, isCached);
            }
        }
    }

    private void setupTabLayoutColors() {
        int color = CanvasContextColor.getCachedColor(getActivity(), getCanvasContext());
        tabLayout.setBackgroundColor(color);
        tabLayout.setTabTextColors(getResources().getColor(R.color.glassWhite), Color.WHITE);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Callbacks
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public void updateSubmissionDate(Date submissionDate) {
        if (getAssignmentDetailsFragment() != null) {
            getAssignmentDetailsFragment().updateSubmissionDate(submissionDate);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source){
        if(source.isAPI()) {
            hideProgressBar();
        }
    }

    private boolean isOnDetailsTab(){
        return viewPager.getCurrentItem() == ASSIGNMENT_TAB_DETAILS;
    }

    private AssignmentDetailsFragment getAssignmentDetailsFragment(){
        if (fragmentPagerAdapter == null) { return null; }
        return (AssignmentDetailsFragment)fragmentPagerAdapter.getRegisteredFragment(ASSIGNMENT_TAB_DETAILS);
    }

    public SubmissionDetailsFragment getSubmissionDetailsFragment(){
        if (fragmentPagerAdapter == null) { return null; }
        return (SubmissionDetailsFragment)fragmentPagerAdapter.getRegisteredFragment(ASSIGNMENT_TAB_SUBMISSION);
    }

    public RubricFragment getRubricFragment(){
        if (fragmentPagerAdapter == null) { return null; }
        return (RubricFragment)fragmentPagerAdapter.getRegisteredFragment(ASSIGNMENT_TAB_GRADE);
    }

    @Override
    public Assignment getModelObject() {
        return assignment;
    }

    @Override
    public HashMap<String, String> getParamForBookmark() {
        HashMap<String, String> map = getCanvasContextParams();
        map.put(Param.ASSIGNMENT_ID, Long.toString(assignmentId));
        return map;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Adapter
    ///////////////////////////////////////////////////////////////////////////////////////////

    class FragmentPagerDetailAdapter extends FragmentStatePagerAdapter{
        // http://stackoverflow.com/questions/8785221/retrieve-a-fragment-from-a-viewpager
        SparseArray<WeakReference<Fragment>> registeredFragments;
        private boolean isLocked;

        public FragmentPagerDetailAdapter(FragmentManager fm, boolean isLocked) {
            super(fm);
            this.isLocked = isLocked;
            registeredFragments = new SparseArray<>();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, new WeakReference<>(fragment));
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public Fragment getRegisteredFragment(int position) {
            WeakReference<Fragment> weakReference = registeredFragments.get(position);
            if (weakReference != null) {
                return weakReference.get();
            }
            return null;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            Bundle bundle;

            switch (position) {
                case ASSIGNMENT_TAB_DETAILS:
                    bundle = AssignmentDetailsFragment.createBundle(getCanvasContext());
                    fragment = ParentFragment.createFragment(AssignmentDetailsFragment.class, bundle);
                    break;
                case ASSIGNMENT_TAB_SUBMISSION:
                    bundle = SubmissionDetailsFragment.createBundle(getCanvasContext());
                    fragment = ParentFragment.createFragment(SubmissionDetailsFragment.class, bundle);
                    break;
                case ASSIGNMENT_TAB_GRADE:
                    bundle = RubricFragment.createBundle(getCanvasContext());
                    fragment = ParentFragment.createFragment(RubricFragment.class, bundle);
                    break;
                default:
                    bundle = AssignmentDetailsFragment.createBundle(getCanvasContext());
                    fragment = ParentFragment.createFragment(AssignmentDetailsFragment.class, bundle);
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return isLocked ? 1 : NUMBER_OF_TABS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            CharSequence title;
            switch (position) {
                case ASSIGNMENT_TAB_DETAILS:
                    title = isLocked ? getString(R.string.assignmentLocked) : getString(AssignmentDetailsFragment.getTabTitle());
                    break;
                case ASSIGNMENT_TAB_SUBMISSION:
                    title = getString(SubmissionDetailsFragment.getTabTitle());
                    break;
                case ASSIGNMENT_TAB_GRADE:
                    title = getString(RubricFragment.getTabTitle());
                    break;
                default:
                    title = getString(AssignmentDetailsFragment.getTabTitle());
                    break;
            }
            return title;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // CallBack
    ///////////////////////////////////////////////////////////////////////////

    public void setUpCallback() {
        assignmentCallback = new CanvasCallback<Assignment>(this) {
            @Override
            public void cache(Assignment assignment) {
                //This is in place to avoid flickering in assignment webview
                if(!Utils.isNetworkAvailable(getContext())){
                    firstPage(assignment, null, null);
                }
            }

            @Override
            public void firstPage(Assignment assignment, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()){
                    return;
                }

                if(assignment != null) {
                    AssignmentFragment.this.assignment = assignment;
                    populateFragments(assignment, true, APIHelpers.isCachedResponse(response));
                    if (getAssignmentDetailsFragment() != null) {
                        getAssignmentDetailsFragment().setAssignmentWithNotification(assignment, message, true, APIHelpers.isCachedResponse(response));
                    }
                    setupTitle(getActionbarTitle());
                }
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                RubricFragment rubricFragment = getRubricFragment();
                if (rubricFragment != null) {
                    rubricFragment.onNoNetwork();
                }
                return super.onFailure(retrofitError);
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        if(extras == null){return;}

        course = (Course)getCanvasContext();

        if(extras.containsKey(Const.TAB_ID)) {
            currentTab = extras.getInt(Const.TAB_ID, 0);
        }

        if (extras.containsKey(Const.ASSIGNMENT)) {
            assignment =  extras.getParcelable(Const.ASSIGNMENT);
            assignmentId = assignment.getId();
            setupTitle(getActionbarTitle());

            //log the names and ids of the course and assignment for crash reporting purposes
            if(course != null){
                LoggingUtility.Log(getActivity(), Log.DEBUG, "Course id: " + course.getId() + " name: " + course.getName());
                LoggingUtility.Log(getActivity(), Log.DEBUG, "Assignment id: " + assignmentId + " name: " + assignment.getName());
            }
        } else if (getUrlParams() != null) {
            assignmentId = parseLong(getUrlParams().get(Param.ASSIGNMENT_ID), -1);
            if (BaseRouterActivity.SUBMISSIONS_ROUTE.equals(getUrlParams().get(Param.SLIDING_TAB_TYPE))) {
                currentTab = ASSIGNMENT_TAB_SUBMISSION;
            } else if (BaseRouterActivity.RUBRIC_ROUTE.equals(getUrlParams().get(Param.SLIDING_TAB_TYPE))) {
                currentTab = ASSIGNMENT_TAB_GRADE;
            }
        } else {
            assignmentId = extras.getLong(Const.ASSIGNMENT_ID, -1);
        }

        if(extras.containsKey(Const.MESSAGE)) {
            message = extras.getString(Const.MESSAGE);
        }
    }

    public static Bundle createBundle(Course course, Assignment assignment) {
        Bundle extras = createBundle(course);
        extras.putParcelable(Const.ASSIGNMENT, assignment);
        extras.putLong(Const.ASSIGNMENT_ID, assignment.getId());
        return extras;
    }

    public static Bundle createBundle(Course course, Assignment assignment, int tabId) {
        Bundle extras = createBundle(course);
        extras.putParcelable(Const.ASSIGNMENT, assignment);
        extras.putLong(Const.ASSIGNMENT_ID, assignment.getId());
        extras.putInt(Const.TAB_ID, tabId);

        return extras;
    }

    public static Bundle createBundle(CanvasContext course, long assignmentId) {
        Bundle extras = createBundle(course);
        extras.putLong(Const.ASSIGNMENT_ID, assignmentId);
        return extras;
    }

    public static Bundle createBundle(Context context, CanvasContext course, long assignmentId, StreamItem item) {
        Bundle extras = createBundle(course);
        extras.putLong(Const.ASSIGNMENT_ID, assignmentId);

        if(item != null) {
            extras.putString(Const.MESSAGE, item.getMessage(context));
        }
        return extras;
    }

    @Override
    public boolean allowBookmarking() {
        Navigation navigation = getNavigation();
        //navigation is a course, but isn't in notification list.
        return (navigation != null && navigationContextIsCourse() && !(navigation.getCurrentFragment() instanceof NotificationListFragment));
    }
}

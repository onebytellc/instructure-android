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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.antonyt.infiniteviewpager.InfiniteViewPager;
import com.instructure.candroid.R;
import com.instructure.candroid.adapter.CalendarListRecyclerAdapter;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.dialog.CalendarChooserDialogStyled;
import com.instructure.candroid.interfaces.AdapterToFragmentCallback;
import com.instructure.candroid.model.DateWindow;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.candroid.util.CanvasCalendarUtils;
import com.instructure.candroid.util.FragUtils;
import com.instructure.candroid.util.ListViewHelpers;
import com.instructure.candroid.util.RouterUtils;
import com.instructure.candroid.view.ViewUtils;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.ScheduleItem;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.utils.Const;
import com.roomorama.caldroid.CaldroidListener;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import hirondelle.date4j.DateTime;

public class CalendarListViewFragment extends OrientationChangeFragment {

    //region CalendarView Enum
    public enum CalendarView {
        DAY_VIEW, WEEK_VIEW, MONTH_VIEW;

        public static CalendarView fromInteger(int x){
            switch (x) {
                case 0:
                    return DAY_VIEW;
                case 1:
                    return WEEK_VIEW;
                case 2:
                    return MONTH_VIEW;
                default:
                    return DAY_VIEW;
            }
        }

        public static int toInteger(CalendarView calendarView){
            switch (calendarView) {
                case DAY_VIEW:
                    return 0;
                case WEEK_VIEW:
                    return 1;
                case MONTH_VIEW:
                    return 2;
                default:
                    return 0;
            }
        }
    }
    //endregion

    // region Calendar View State
    private CalendarView currentCalendarView = CalendarView.DAY_VIEW;
    // endregion

    // region Views
    private RelativeLayout mCalendarContainer;
    private CanvasCalendarFragment mCalendarFragment;
    private TextView mMonthText;
    private LinearLayout mEmptyPandaView;
    private TextView mEmptyTextView;
    private View mMonthContainer;
    private ImageView mMonthFlipper;
    // endregion

    private CalendarListRecyclerAdapter mRecyclerAdapter;
    private CalendarChooserDialogStyled.CalendarChooserCallback mDialogCallback;
    private CalendarListRecyclerAdapter.AdapterToCalendarCallback mAdapterToCalendarCallback;
    private AdapterToFragmentCallback<ScheduleItem> mAdapterToFragmentCallback;
    // endregion

    private boolean mIsFirstTimeCreation = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Most of this will never get called since we are using
        //onConfig change, however, it will be used to save state
        //in the case that the activity is destroyed.
        mCalendarFragment = new CanvasCalendarFragment();
        if(mRecyclerAdapter == null){
            setUpCallbacks();
            mRecyclerAdapter = new CalendarListRecyclerAdapter(getContext(), mAdapterToFragmentCallback, mAdapterToCalendarCallback);
        }
        configuration = getResources().getConfiguration().orientation;

        //Restore saved state
        int year = ApplicationManager.getPrefs(getContext()).load(Const.CALENDAR_YEAR_PREF, -1);
        int month = ApplicationManager.getPrefs(getContext()).load(Const.CALENDAR_MONTH_PREF, -1);
        int day = ApplicationManager.getPrefs(getContext()).load(Const.CALENDAR_DAY_PREF, -1);
        boolean flag = ApplicationManager.getPrefs(getContext()).load(Const.CALENDAR_PREF_FLAG, false);

        if(!flag && mRecyclerAdapter.getSelectedDay() == null && month != -1 && year != -1 && day != -1){
             mRecyclerAdapter.setSelectedDay(DateTime.forDateOnly(year, month, day));
        }

        // If Activity is created after rotation
        if (savedInstanceState != null) {
            mCalendarFragment.restoreStatesFromKey(savedInstanceState,
                    Const.CALENDAR_STATE);
        }

        // If activity is created from fresh
        else {
            if(!flag){
                //We are returning to a saved state
                currentCalendarView = CalendarView.fromInteger(ApplicationManager.getPrefs(getContext()).load(Const.CALENDAR_VIEW_PREF, 0));
                mCalendarFragment.setArguments(CanvasCalendarFragment.createBundle(getContext(), Calendar.getInstance(Locale.getDefault()), month, year));
            } else {
                //This will create default behavior
                mCalendarFragment.setArguments(CanvasCalendarFragment.createBundle(getContext(), Calendar.getInstance(Locale.getDefault()), -1, -1));
            }

            mRecyclerAdapter.setStartDayMonday(ApplicationManager.getPrefs(getContext()).load(Const.CALENDAR_START_DAY_PREFS, false));
        }

        mDialogCallback = new CalendarChooserDialogStyled.CalendarChooserCallback() {
            @Override
            public void onCalendarsSelected(List<String> subscribedContexts) {
                mRecyclerAdapter.updateSelectedCalendarContexts(subscribedContexts);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        //restore Fragment on resume
        if (mCalendarFragment != null) {
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            if (!mIsFirstTimeCreation) { // for resuming from sleep
                fragmentTransaction.replace(R.id.calendar1, mCalendarFragment).commit();
                mIsFirstTimeCreation = true;
            } else {
                if(mRecyclerAdapter.isStartDayChanged()){ //for resuming from settings change
                    getNavigation().updateCalendarStartDay();
                } else {
                    fragmentTransaction.attach(mCalendarFragment);
                    fragmentTransaction.commit();
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mRecyclerAdapter.getSelectedDay() != null){
            ApplicationManager.getPrefs(getContext()).save(Const.CALENDAR_YEAR_PREF, mRecyclerAdapter.getSelectedDay().getYear());
            ApplicationManager.getPrefs(getContext()).save(Const.CALENDAR_MONTH_PREF, mRecyclerAdapter.getSelectedDay().getMonth());
            ApplicationManager.getPrefs(getContext()).save(Const.CALENDAR_DAY_PREF, mRecyclerAdapter.getSelectedDay().getDay());
            ApplicationManager.getPrefs(getContext()).save(Const.CALENDAR_VIEW_PREF, CalendarView.toInteger(currentCalendarView));
            ApplicationManager.getPrefs(getContext()).save(Const.CALENDAR_PREF_FLAG, false);
        }
    }

    public void setupActionbar() {
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setupActionbarSpinnerForMonth();
    }

    @Override
    public void onFragmentActionbarSetupComplete(FRAGMENT_PLACEMENT placement) {
        super.onFragmentActionbarSetupComplete(placement);
        if(mRecyclerAdapter != null && mRecyclerAdapter.isCalendarViewCreated()){
            setupActionbar();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //restore Fragment on configuration change
        if (mCalendarFragment != null) {
            //The nested fragment needs to be detached then reattached on rotation
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.detach(mCalendarFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
        //Remove all the existing views from the root view and recreate them
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View rootView = populateView(inflater, (ViewGroup) getView().getParent());
        ViewGroup containerView = (ViewGroup) getView();
        containerView.removeAllViews();
        containerView.addView(rootView);
    }

    private void onRowClick(ScheduleItem scheduleItem, boolean closeSlidingPane) {
        Navigation navigation = getNavigation();
        if (navigation != null) {
            setCanvasContext(findContextForScheduleItem(scheduleItem));
            if (scheduleItem.getType() == ScheduleItem.Type.TYPE_ASSIGNMENT) {
                RouterUtils.routeUrl(getActivity(), scheduleItem.getHtmlUrl(), false);
            } else if (scheduleItem.getType() == ScheduleItem.Type.TYPE_CALENDAR || scheduleItem.getType() == ScheduleItem.Type.TYPE_SYLLABUS) {
                ParentFragment fragment = FragUtils.getFrag(CalendarEventFragment.class, CalendarEventFragment.createBundle(getCanvasContext(), scheduleItem));
                navigation.addFragment(fragment);
            }
        }
    }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.calendar);
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.MASTER;
    }

    @Override
    public View populateView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(R.layout.calendar_listview_fragment, container, false);
        mCalendarContainer = (RelativeLayout) rootView.findViewById(R.id.calendarContainer);
        final int span = isTablet(getContext()) ? 2 : 1;
        configureRecyclerViewAsGrid(rootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView, R.string.noItemsToDisplayShort, span);

        mEmptyPandaView = (LinearLayout)rootView.findViewById(R.id.emptyView);
        mEmptyTextView = (TextView) mEmptyPandaView.findViewById(R.id.noItems);

        //First time will replace, all others attach
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        if (!mIsFirstTimeCreation) {
            fragmentTransaction.replace(R.id.calendar1, mCalendarFragment).commit();
            mIsFirstTimeCreation = true;
        } else {
            fragmentTransaction.attach(mCalendarFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }
        setUpListeners();
        return rootView;
    }

    private void setUpCallbacks(){
        mAdapterToCalendarCallback = new CalendarListRecyclerAdapter.AdapterToCalendarCallback() {
            @Override
            public void showChooserDialog(boolean firstShow) {
                showCalendarChooserDialog(firstShow);
            }

            @Override
            public void hidePandaLoading() {
                hidePanda();
            }

            @Override
            public void showPandaLoading() {
                showPanda();
            }

            @Override
            public int getCurrentCalendarView() {
                return CalendarView.toInteger(currentCalendarView);
            }

            @Override
            public HashMap<String, Object> getExtraCalendarData() {
                return mCalendarFragment.getExtraData();
            }

            @Override
            public void refreshCalendarFragment() {
                mCalendarFragment.refreshView();
            }

            @Override
            public void setSelectedDates(Date d1, Date d2) {
                mCalendarFragment.setSelectedDates(d1, d2);
            }
        };

        mAdapterToFragmentCallback = new AdapterToFragmentCallback<ScheduleItem>() {
            @Override
            public void onRowClicked(ScheduleItem item, int position, boolean isOpenDetail) {
                onRowClick(item, isOpenDetail);
            }

            @Override
            public void onRefreshFinished() {
                setRefreshing(false);
            }
        };
    }

    @Override
    public boolean navigationContextIsCourse() {
        //When the user returns to calendar from viewing an event, NavigationActivity will think
        //the context is a Course, so we override this here.
        return false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRecyclerAdapter.loadData();
    }

    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.calendar_menu, menu);
        setCalendarViewTypeChecked(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.calendarToday:
                todayClick();
                break;
            case R.id.calendarDayView:
                dayClick(item);
                break;
            case R.id.calendarWeekView:
                weekClick(item);
                break;
            case R.id.calendarMonthView:
                monthClick(item);
                break;
            case R.id.selectCalendars:
                showCalendarChooserDialog(false);
                break;
            case R.id.createEvent:
                eventCreation();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void eventCreation(){
        if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
            Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
            return;
        }
        Analytics.trackAppFlow(getActivity(), CreateCalendarEventFragment.class);
        Navigation navigation = getNavigation();
        if (navigation != null) {
            ParentFragment fragment = FragUtils.getFrag(CreateCalendarEventFragment.class, CreateCalendarEventFragment.createBundle(getCanvasContext(), mRecyclerAdapter.getSelectedDay().getMilliseconds(TimeZone.getDefault())));
            navigation.addFragment(fragment);
        }
    }

    private void showCalendarChooserDialog(boolean firstShow){
        if(mRecyclerAdapter.getContextNames() != null){
            CalendarChooserDialogStyled.show(getFragmentActivity(), CalendarListRecyclerAdapter.getFilterPrefs(getContext()), mRecyclerAdapter.getContextNames(), firstShow, mDialogCallback);
        }
    }

    private void setUpListeners() {

        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onSelectDate(Date date, View view) {
                //New date selected, clear out prior
                mCalendarFragment.clearSelectedDates();
                mRecyclerAdapter.setSelectedDay(DateTime.forInstant(date.getTime(), TimeZone.getDefault()));

                if (currentCalendarView == CalendarView.DAY_VIEW) {
                    mCalendarFragment.setSelectedDates(date, date);
                } else if (currentCalendarView == CalendarView.WEEK_VIEW) {
                    DateWindow dateWindow = CanvasCalendarUtils.setSelectedWeekWindow(date,
                            mRecyclerAdapter.isStartDayMonday());
                    mCalendarFragment.setSelectedDates(dateWindow.getStart(), dateWindow.getEnd());
                }

                mCalendarFragment.refreshView();
                mRecyclerAdapter.refreshListView();
            }

            @Override
            public void onCaldroidViewCreated() {
                super.onCaldroidViewCreated();
                //Removing styling for upper buttons
                Button leftButton = mCalendarFragment.getLeftArrowButton();
                Button rightButton = mCalendarFragment.getRightArrowButton();
                TextView textView = mCalendarFragment.getMonthTitleTextView();
                leftButton.setVisibility(View.GONE);
                rightButton.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);

                //Initialize post view created mCalendarFragment elements
                InfiniteViewPager viewPager = mCalendarFragment.getDateViewPager();
                viewPager.setPageMargin((int) ViewUtils.convertDipsToPixels(32, getContext()));
                if (mRecyclerAdapter.getSelectedDay() == null){
                    mRecyclerAdapter.setSelectedDay(DateTime.today(TimeZone.getDefault()));
                    Date today = new Date(mRecyclerAdapter.getSelectedDay().getMilliseconds(TimeZone.getDefault()));
                    mCalendarFragment.setSelectedDates(today, today);
                }
                mRecyclerAdapter.setCalendarViewCreated(true);
                setupActionbar();
            }

            @Override
            public void onChangeMonth(int month, int year, boolean fromCreation) {
                super.onChangeMonth(month, year, fromCreation);
                if (mMonthText != null) {

                    if (fromCreation  || hasOrientationChanged()){
                        hidePanda();
                        return;
                    }

                    //Update Actionbar
                    mMonthText.setText(new DateFormatSymbols().getMonths()[month - 1] + " " + year);
                }

                //First time loading the calendar will trigger this, but the API calls have already been made
                if (!mRecyclerAdapter.isTodayPressed() && !fromCreation) { //Refresh for month, unless this was triggered by "today" button
                    mCalendarFragment.clearSelectedDates();
                    DateTime today = DateTime.today(TimeZone.getDefault());
                    if (today.getMonth() == month && today.getYear() == year){
                        mRecyclerAdapter.setSelectedDay(today);
                    } else {
                        mRecyclerAdapter.setSelectedDay(new DateTime(year, month, 1, null, null, null, null));
                    }

                    mRecyclerAdapter.refreshCalendar();
                }
            }
        };

        mCalendarFragment.setCaldroidListener(listener);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save current calendarState
        if (mCalendarFragment != null) {
            try {
                mCalendarFragment.saveStatesToKey(outState, Const.CALENDAR_STATE);
                FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
                fragmentTransaction.detach(mCalendarFragment);
                fragmentTransaction.commit();
            } catch (IllegalStateException e) {
                Utils.e("CalendarListViewFragment crash: " + e);
            }
        }

        if(mRecyclerAdapter.getSelectedDay() != null){
            ApplicationManager.getPrefs(getContext()).save(Const.CALENDAR_PREF_FLAG, true);
        }
    }

    /**
     * Helper method to configure actionbar button for calendar grid drop down
     */
    private void setupActionbarSpinnerForMonth() {
        mMonthContainer = LayoutInflater.from(getSupportActionBar().getThemedContext()).inflate(R.layout.actionbar_calendar_layout, null);
        getSupportActionBar().setCustomView(mMonthContainer);
        mMonthText = (TextView) mMonthContainer.findViewById(R.id.monthText);
        if(mCalendarFragment.getCurrentMonth() != -1){
            //this is month - 1 because DateFormatSymbols uses 0 indexed months, and DateTime uses
            // 1 indexed months.
            mMonthText.setText(new DateFormatSymbols().getMonths()[mCalendarFragment.getCurrentMonth() - 1] + " " + mCalendarFragment.getCurrentYear());
        }
        mMonthFlipper = (ImageView) mMonthContainer.findViewById(R.id.indicator);

        mMonthContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandOrCollapseCalendar(mCalendarContainer, mCalendarContainer.getVisibility() == View.GONE);
            }
        });
    }

    /**
     * Helper method to animate the calendar grid off or on to the screen.
     * Also animates associated arrow flipper.
     *
     * @param calendarView
     * @param isExpand
     */
    private void expandOrCollapseCalendar(final View calendarView, final boolean isExpand){
        final boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        // if we're not expanding, we translate to 0.
        // Otherwise, if landscape, then animate the width of the calendar, otherwise the height.
        final int translation = isExpand ? 0 : isLandscape ? -calendarView.getWidth() : -calendarView.getHeight();

        mMonthFlipper.animate().rotationBy(isExpand ? -180f : 180f);

        AnimatorListenerAdapter onFinished = new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                calendarView.setEnabled(false);
                mMonthContainer.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(!isExpand){
                    calendarView.setVisibility(View.GONE);
                }
                calendarView.setEnabled(true);
                mMonthContainer.setEnabled(true);
            }
        };

        if(isExpand){
            calendarView.setVisibility(View.VISIBLE);
        }

        if(isLandscape){
            calendarView.animate().translationX(translation).setListener(onFinished);

        }else{
            calendarView.animate().translationY(translation).setListener(onFinished);
        }
    }

    /**
     * Simple helper to show the loading panda
     */
    private void showPanda() {
        //This can be called while in transition to an event detail fragment, throws NPE
        if(!isAdded()){
            return;
        }

        mEmptyPandaView.setVisibility(View.VISIBLE);
        ListViewHelpers.changeEmptyViewToLoading(mEmptyPandaView);
    }

    /**
     * Simple helper to hide the loading panda and update the text for empty/un-selected
     */
    private void hidePanda() {
        //This can be called while in transition to an event detail fragment, throws NPE
        if(!isAdded()){
            return;
        }

        ListViewHelpers.changeEmptyViewToNoItems(mEmptyPandaView);
        if (mRecyclerAdapter.getSelectedDay() == null && currentCalendarView != CalendarView.MONTH_VIEW) {
            mEmptyTextView.setText(getResources().getString(R.string.noDatesSelected));
        } else if(mRecyclerAdapter.getItemCount() == 0){
            mEmptyPandaView.setVisibility(View.VISIBLE);
            switch(currentCalendarView){
                case DAY_VIEW:
                    if(mRecyclerAdapter.getSelectedDay() != null) {
                        Date date = new Date(mRecyclerAdapter.getSelectedDay().getMilliseconds(TimeZone.getDefault()));
                        String cleanDate = CanvasCalendarUtils.getSimpleDate(date);
                        mEmptyTextView.setText(getResources().getString(R.string.emptyCalendarDate) + " " + cleanDate);
                    }
                    break;
                case WEEK_VIEW:
                    mEmptyTextView.setText(getResources().getString(R.string.emptyCalendarWeek));
                    break;
                case MONTH_VIEW:
                    mEmptyTextView.setText(getResources().getString(R.string.emptyCalendarMonth));
                    break;
            }
        } else {
            mEmptyPandaView.setVisibility(View.GONE);
        }
    }

    /**
     * Finds the associated canvasContext for a given scheduleItem
     *
     * @param scheduleItem
     * @return
     */
    private CanvasContext findContextForScheduleItem(ScheduleItem scheduleItem) {
        for (CanvasContext context : mRecyclerAdapter.getCanvasContextItems()) {
            if (context.getId() == (scheduleItem.getContextId())) {
                return context;
            }
        }

        //todo this would represent an error occurring, need to handle gracefully
        return CanvasContext.emptyCourseContext();
    }

    /**
     * menu item 0 = Today Button
     * menu item 1 = Day View
     * menu item 2 = Week View
     * menu item 3 = Schedule View
     * menu item 4 = Calendar Settings
     *
     * @param menu
     */
    private void setCalendarViewTypeChecked(Menu menu) {
        menu.getItem(1).setChecked(currentCalendarView == CalendarView.DAY_VIEW);
        menu.getItem(2).setChecked(currentCalendarView == CalendarView.WEEK_VIEW);
        menu.getItem(3).setChecked(currentCalendarView == CalendarView.MONTH_VIEW);
    }

    ///////////////////////////////////////////////////////
    //           Overflow menu helper methods            //
    ///////////////////////////////////////////////////////

    private void todayClick() {
        mRecyclerAdapter.setTodayPressed(true);
        DateTime today = DateTime.today(TimeZone.getDefault());
        mRecyclerAdapter.setSelectedDay(today);
        mCalendarFragment.moveToDateTime(today);
        mRecyclerAdapter.refreshForTodayPressed();
        if (mCalendarContainer.getVisibility() == View.GONE) {
            expandOrCollapseCalendar(mCalendarContainer, true);
        }
    }

    private void monthClick(MenuItem item) {
        currentCalendarView = CalendarView.MONTH_VIEW;
        mCalendarFragment.clearSelectedDates();
        mRecyclerAdapter.refreshListView();
        mCalendarFragment.refreshView();
        if (mCalendarContainer.getVisibility() == View.GONE) {
            expandOrCollapseCalendar(mCalendarContainer, true);
        }
        item.setChecked(true);
    }

    private void weekClick(MenuItem item) {
        currentCalendarView = CalendarView.WEEK_VIEW;
        if (mRecyclerAdapter.getSelectedDay() != null) {
            DateWindow dateWindow = CanvasCalendarUtils.setSelectedWeekWindow(
                    new Date(mRecyclerAdapter.getSelectedDay().getMilliseconds(TimeZone.getDefault())),
                    mRecyclerAdapter.isStartDayMonday());
            mCalendarFragment.setSelectedDates(dateWindow.getStart(), dateWindow.getEnd());
        }
        mRecyclerAdapter.refreshListView();
        mCalendarFragment.refreshView();
        if (mCalendarContainer.getVisibility() == View.GONE) {
            expandOrCollapseCalendar(mCalendarContainer, true);
        }
        item.setChecked(true);
    }

    private void dayClick(MenuItem item) {
        currentCalendarView = CalendarView.DAY_VIEW;
        if (mRecyclerAdapter.getSelectedDay() != null) {
            Date today = new Date(mRecyclerAdapter.getSelectedDay().getMilliseconds(TimeZone.getDefault()));
            mCalendarFragment.setSelectedDates(today, today);
        }
        mRecyclerAdapter.refreshListView();
        mCalendarFragment.refreshView();
        if (mCalendarContainer.getVisibility() == View.GONE) {
            expandOrCollapseCalendar(mCalendarContainer, true);
        }
        item.setChecked(true);
    }

    private int configuration;

    private boolean hasOrientationChanged(){
        if (configuration != getResources().getConfiguration().orientation){
            configuration = getResources().getConfiguration().orientation;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }

    /**
     * Allows creation and deletion of events to trigger a refresh
     */
    public void refreshCalendar(ScheduleItem item, boolean delete){

        if(mRecyclerAdapter != null && item != null){
            mRecyclerAdapter.setRefresh(true);
            if(delete){
                mRecyclerAdapter.removeItem(item);
            }
            mRecyclerAdapter.refreshCalendar();
        }
    }
}

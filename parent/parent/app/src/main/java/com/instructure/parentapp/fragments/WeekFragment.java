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

package com.instructure.parentapp.fragments;

import android.annotation.TargetApi;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.ScheduleItem;
import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.pandarecycler.decorations.SpacesItemDecoration;
import com.instructure.pandarecycler.interfaces.ViewHolderHeaderClicked;
import com.instructure.pandautils.fragments.BaseExpandableSyncFragment;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.Prefs;
import com.instructure.parentapp.R;
import com.instructure.parentapp.activity.DetailViewActivity;
import com.instructure.parentapp.adapter.CalendarWeekRecyclerAdapter;
import com.instructure.parentapp.binders.CalendarWeekBinder;
import com.instructure.parentapp.factorys.WeekViewPresenterFactory;
import com.instructure.parentapp.interfaces.AdapterToFragmentCallback;
import com.instructure.parentapp.models.WeekHeaderItem;
import com.instructure.parentapp.presenters.WeekPresenter;
import com.instructure.parentapp.util.AnalyticUtils;
import com.instructure.parentapp.util.RecyclerViewUtils;
import com.instructure.parentapp.util.ViewUtils;
import com.instructure.parentapp.view.EmptyPandaView;
import com.instructure.parentapp.viewinterface.WeekView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import instructure.androidblueprint.PresenterFactory;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class WeekFragment extends BaseExpandableSyncFragment<WeekHeaderItem, ScheduleItem, WeekView, WeekPresenter, RecyclerView.ViewHolder, CalendarWeekRecyclerAdapter>
        implements WeekView {

    //region Binding

    @BindView(R.id.weekViewBackground) LinearLayout mWeekBackground;
    @BindView(R.id.prevWeek) View mPrevWeek;
    @BindView(R.id.nextWeek) View mNextWeek;
    @BindView(R.id.weekText1) TextView mWeekText1;
    @BindView(R.id.weekText2) TextView mWeekText2;
    @BindView(R.id.weekTextSwitcher) TextSwitcher mTextSwitcher;
    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.emptyPandaView) EmptyPandaView mEmptyPandaView;
    @Nullable
    @BindView(R.id.toolbar) Toolbar mToolbar;
    //endregion

    private Student mStudent;
    private Course mCourse;

    public static WeekFragment newInstance(Student student) {
        Bundle args = new Bundle();
        args.putParcelable(Const.STUDENT, student);
        WeekFragment fragment = new WeekFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int layoutResId() {
        return R.layout.fragment_week;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStudent = getArguments().getParcelable(Const.STUDENT);
        mCourse = getArguments().getParcelable(Const.COURSE);
    }

    public void setWeekViewBackground(int color) {
        if(mWeekBackground != null) {
            mWeekBackground.setBackgroundColor(color);
        }
    }

    public Student getStudent() {
        return mStudent;
    }

    public void refreshWithStudent(@NonNull Student student, boolean refresh) {
        if(getPresenter() != null) {
            getArguments().putParcelable(Const.STUDENT, student);
            getPresenter().setStudent(student, refresh);
        }
    }

    private AdapterToFragmentCallback<ScheduleItem> mAdapterCallback = new AdapterToFragmentCallback<ScheduleItem>() {
        @Override
        public void onRowClicked(ScheduleItem item, int position, boolean isOpenDetail) {
            AnalyticUtils.trackFlow(AnalyticUtils.WEEK_FLOW, AnalyticUtils.WEEK_VIEW_SELECTED);
            if(item.getAssignment() != null){
                //if we're already in a detailViewActivity we don't need to add another one
                if(getActivity() instanceof DetailViewActivity) {
                    ((DetailViewActivity)getActivity()).addFragment(AssignmentFragment.newInstance(item.getAssignment(),
                            CalendarWeekBinder.getCourseNameById(getPresenter().getCoursesMap(), item.getCourseId()), mStudent), false);
                } else {
                    startActivity(DetailViewActivity.createIntent(getContext(), DetailViewActivity.DETAIL_FRAGMENT.ASSIGNMENT, item.getAssignment(),
                            CalendarWeekBinder.getCourseNameById(getPresenter().getCoursesMap(), item.getCourseId()), mStudent));
                    getActivity().overridePendingTransition(R.anim.slide_from_bottom, android.R.anim.fade_out);
                }
            } else {
                //if we're already in a detailViewActivity we don't need to add another one
                if(getActivity() instanceof DetailViewActivity) {
                    ((DetailViewActivity)getActivity()).addFragment(EventFragment.newInstance(item, mStudent), false);
                } else {
                    startActivity(DetailViewActivity.createIntent(getContext(), DetailViewActivity.DETAIL_FRAGMENT.EVENT, item, mStudent));
                    getActivity().overridePendingTransition(R.anim.slide_from_bottom, android.R.anim.fade_out);
                }
            }
        }
    };

    private ViewHolderHeaderClicked<WeekHeaderItem> mAdapterHeaderCallback = new ViewHolderHeaderClicked<WeekHeaderItem>() {
        @Override
        public void viewClicked(View view, WeekHeaderItem weekHeaderItem) {

        }
    };

    private void setupListeners() {
        mPrevWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPresenter().prevWeekClicked();
            }
        });

        mNextWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPresenter().nextWeekClicked();
            }
        });
    }


    public void setActionbarColor(int actionBarColor) {
        if(mToolbar != null) {
            ColorDrawable colorDrawable = new ColorDrawable(actionBarColor);
            mToolbar.setBackgroundDrawable(colorDrawable);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int statusBarColor) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && statusBarColor != Integer.MAX_VALUE) {
            //make the status bar darker than the toolbar
            getActivity().getWindow().setStatusBarColor(ViewUtils.darker(statusBarColor, 0.85f));
        }
    }

    // Sync

    @Override
    public void onCreateView(View view) {
        ButterKnife.bind(this, view);

        //set the color of the weekBackground
        Prefs prefs = new Prefs(getActivity(), com.instructure.parentapp.util.Const.CANVAS_PARENT_SP);
        int color = prefs.load(Const.NEW_COLOR, -1);
        if(color != -1) {
            mWeekBackground.setBackgroundColor(color);
        }

        if (mToolbar != null) {
            mToolbar.setVisibility(View.VISIBLE);

            mToolbar.setNavigationIcon(R.drawable.ic_close_white);
            mToolbar.setNavigationContentDescription(R.string.close);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });

            if(color != -1) {
                setStatusBarColor(color);
                setActionbarColor(color);
            }
        }
    }

    @Override
    protected void onReadySetGo(WeekPresenter presenter) {
        setupListeners();
        mRecyclerView.setAdapter(getAdapter());
        presenter.loadData(false);
    }

    @Override
    protected PresenterFactory<WeekPresenter> getPresenterFactory() {
        return new WeekViewPresenterFactory(mStudent, mCourse);
    }

    @Override
    protected void onPresenterPrepared(WeekPresenter presenter) {
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(mRootView, getContext(), getAdapter(),
                presenter, R.id.swipeRefreshLayout, R.id.recyclerView, R.id.emptyPandaView, getString(R.string.weekEmptyView));
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(getContext(), R.dimen.med_padding));
        addSwipeToRefresh(mSwipeRefreshLayout);
        addPagination();
    }

    @Override
    protected CalendarWeekRecyclerAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new CalendarWeekRecyclerAdapter(
                    getActivity(),
                    getPresenter(),
                    getPresenter().getCourses(),
                    getPresenter().getStudent(),
                    mAdapterCallback,
                    mAdapterHeaderCallback);
        }
        return mAdapter;
    }

    @Override
    public boolean withPagination() {
        return true;
    }

    @NonNull
    @Override
    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    public ProgressBar getProgressBar() {
        return mEmptyPandaView.getProgressBar();
    }

    @Override
    protected int perPageCount() {
        return AppManager.getConfig().perPageCount();
    }

    @Override
    public void onRefreshStarted() {
        mEmptyPandaView.setLoading();
    }

    @Override
    public void onRefreshFinished() {
        mSwipeRefreshLayout.setRefreshing(false);
        //update the adapter with the courses that the presenter retrieved
        getAdapter().setCourses(getPresenter().getCourses());
    }

    @Override
    public void checkIfEmpty() {
        RecyclerViewUtils.checkIfEmpty(mEmptyPandaView, mRecyclerView, mSwipeRefreshLayout, getAdapter(), getPresenter().isEmpty());
    }

    @Override
    public void updateWeekText(ArrayList<GregorianCalendar> dates) {
        final GregorianCalendar start = dates.get(0);
        final GregorianCalendar end = dates.get(1);

        if(start.get(Calendar.MONTH) == end.get(Calendar.MONTH)) {
            //Format as: Mar 7 - 10
            int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH;
            String monthAndDayText = DateUtils.formatDateTime(getContext(), start.getTimeInMillis(), flags);
            String monthAndDayTextEnd = Integer.toString(end.get(Calendar.DAY_OF_MONTH));
            mTextSwitcher.setText(String.format(getResources().getString(R.string.date_bar), monthAndDayText, monthAndDayTextEnd));
        } else {
            //Format as: Mar 7 - Apr 10
            int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH;
            String monthAndDayText = DateUtils.formatDateTime(getContext(), start.getTimeInMillis(), flags);
            String monthAndDayTextEnd = DateUtils.formatDateTime(getContext(), end.getTimeInMillis(), flags);
            mTextSwitcher.setText(String.format(getResources().getString(R.string.date_bar), monthAndDayText, monthAndDayTextEnd));
        }
    }

    @Override
    public String airwolfDomain() {
        return APIHelper.getAirwolfDomain(getContext());
    }

    //For Testing
    private void logDate(String prefix, Calendar date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Log.d("date", prefix + " " + format.format(date.getTime()));
    }
}
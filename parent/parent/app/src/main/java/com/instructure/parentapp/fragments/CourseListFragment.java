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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.pandarecycler.decorations.SpacesItemDecoration;
import com.instructure.pandautils.fragments.BaseSyncFragment;
import com.instructure.pandautils.utils.Const;
import com.instructure.parentapp.R;
import com.instructure.parentapp.activity.DetailViewActivity;
import com.instructure.parentapp.adapter.CourseListRecyclerAdapter;
import com.instructure.parentapp.factorys.CourseListPresenterFactory;
import com.instructure.parentapp.holders.CourseViewHolder;
import com.instructure.parentapp.interfaces.AdapterToFragmentUpdateListCallback;
import com.instructure.parentapp.presenters.CourseListPresenter;
import com.instructure.parentapp.util.AnalyticUtils;
import com.instructure.parentapp.util.ApplicationManager;
import com.instructure.parentapp.util.RecyclerViewUtils;
import com.instructure.parentapp.view.EmptyPandaView;
import com.instructure.parentapp.viewinterface.CourseListView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import instructure.androidblueprint.PresenterFactory;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class CourseListFragment extends BaseSyncFragment<Course, CourseListPresenter, CourseListView, CourseViewHolder, CourseListRecyclerAdapter>
        implements CourseListView {

    //region Binding

    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.emptyPandaView) EmptyPandaView mEmptyPandaView;
    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;

    //endregion

    private Student mStudent;

    public static CourseListFragment newInstance(Student student){
        Bundle args = new Bundle();
        args.putParcelable(Const.STUDENT, student);
        CourseListFragment fragment = new CourseListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int layoutResId() {
        return R.layout.course_list_view;
    }

    @Override
    public void onCreateView(View view) {
        ButterKnife.bind(this, view);
        addSwipeToRefresh(mSwipeRefreshLayout);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStudent = getArguments().getParcelable(Const.STUDENT);
    }

    public void refreshWithStudent(@NonNull Student student, boolean refresh) {
        if(getPresenter() != null) {
            getArguments().putParcelable(Const.STUDENT, student);
            getPresenter().setStudent(student, refresh);
        }
    }

    public Student getStudent() {
        return getPresenter().getStudent();
    }

    @NonNull
    @Override
    public String airwolfDomain() {
        return APIHelper.getAirwolfDomain(getContext());
    }

    @NonNull
    @Override
    public String parentId() {
        return ApplicationManager.getParentId(getContext());
    }

    @Override
    protected void onReadySetGo(CourseListPresenter presenter) {
        mRecyclerView.setAdapter(getAdapter());
        presenter.loadData(false);
    }

    @Override
    protected PresenterFactory<CourseListPresenter> getPresenterFactory() {
        return new CourseListPresenterFactory(mStudent);
    }

    @Override
    protected void onPresenterPrepared(CourseListPresenter presenter) {
        mRecyclerView = RecyclerViewUtils.buildRecyclerView(mRootView, getContext(), getAdapter(),
                presenter, R.id.swipeRefreshLayout, R.id.recyclerView, R.id.emptyPandaView, getString(R.string.noCourses));
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(getContext(), R.dimen.med_padding));
        addSwipeToRefresh(mSwipeRefreshLayout);
        addPagination();
    }

    @Override
    protected CourseListRecyclerAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new CourseListRecyclerAdapter(getActivity(), getPresenter(), getPresenter().getStudent(),
                    new AdapterToFragmentUpdateListCallback<Student, Course>() {
                @Override
                public void onRowClicked(Student student, int position, boolean isOpenDetail) {

                    AnalyticUtils.trackFlow(AnalyticUtils.COURSE_FLOW, AnalyticUtils.COURSE_SELECTED);

                    startActivity(DetailViewActivity.createIntent(getContext(),
                            DetailViewActivity.DETAIL_FRAGMENT.WEEK, student, getAdapter().getItemAtPosition(position)));
                    getActivity().overridePendingTransition(R.anim.slide_from_bottom, android.R.anim.fade_out);
                }

                @Override
                public void onContentUpdated(List<Course> items) {

                }
            });
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
    }

    @Override
    public void checkIfEmpty() {
        RecyclerViewUtils.checkIfEmpty(mEmptyPandaView, mRecyclerView, mSwipeRefreshLayout, getAdapter(), getPresenter().isEmpty());
    }
}
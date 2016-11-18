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

package com.instructure.parentapp.adapter;

import android.content.Context;
import android.view.View;

import com.instructure.canvasapi2.models.Alert;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Student;
import com.instructure.parentapp.binders.AlertBinder;
import com.instructure.parentapp.holders.AlertViewHolder;
import com.instructure.parentapp.interfaces.AdapterToFragmentBadgeCallback;
import com.instructure.parentapp.presenters.AlertPresenter;

import java.util.ArrayList;
import java.util.List;

import instructure.androidblueprint.SyncPresenter;
import instructure.androidblueprint.SyncRecyclerAdapter;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class AlertListRecyclerAdapter extends SyncRecyclerAdapter<Alert, AlertViewHolder> {

    private Student mStudent;
    private AdapterToFragmentBadgeCallback<Alert> mAdapterToFragmentCallback;
    private ItemDismissedInterface mItemDismissedInterface;

    private boolean mHasUpdatedCourses = false;
    private boolean mHasUpdatedUser = false;

    public interface ItemDismissedInterface {
        void itemDismissed(Alert item, AlertViewHolder holder);
    }

    public AlertListRecyclerAdapter(
            Context context,
            AlertPresenter presenter,
            AdapterToFragmentBadgeCallback<Alert> adapterToFragmentBadgeCallback,
            ItemDismissedInterface itemDismissedInterface) {
        super(context, presenter);
        mAdapterToFragmentCallback = adapterToFragmentBadgeCallback;
        mItemDismissedInterface = itemDismissedInterface;
    }

    //Sync

    @Override
    public void bindHolder(Alert alert, AlertViewHolder holder, int position) {
        AlertBinder.bind(getContext(), alert, holder, mAdapterToFragmentCallback, mItemDismissedInterface);
    }

    @Override
    public AlertViewHolder createViewHolder(View v, int viewType) {
        return new AlertViewHolder(v);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return AlertViewHolder.holderResId();
    }





    /* This is the real constructor and should be called to create instances of this adapter */
//    public AlertListRecyclerAdapter(Context context, Student student, AdapterToFragmentBadgeCallback<Alert> adapterToFragmentCallback){
//        this(context, student, adapterToFragmentCallback, false);
//    }

//    /* This overloaded constructor is for testing purposes ONLY, and should not be used to create instances of this adapter. */
//
//    protected AlertListRecyclerAdapter(Context context, Student student, AdapterToFragmentBadgeCallback<Alert> adapterToFragmentCallback, final boolean isLoadData){
//        super(context, Alert.class);
//
//        mStudent = student;
//        mAdapterToFragmentCallback = adapterToFragmentCallback;
//        setItemCallback(new ItemComparableCallback<Alert>() {
//            @Override
//            public int compare(Alert o1, Alert o2) {
//                return sortAlerts(o1, o2);
//            }
//
//            @Override
//            public boolean areContentsTheSame(Alert oldItem, Alert newItem) {
//                return compareAlerts(oldItem, newItem);
//            }
//
//            @Override
//            public boolean areItemsTheSame(Alert item1, Alert item2) {
//                return item1.getId() == item2.getId();
//            }
//
//            @Override
//            public long getUniqueItemId(Alert alert) {
//                return alert.getId();
//            }
//        });
//
//
//        if(isLoadData){
//            loadData();
//        }
//    }
//
//    @Override
//    public AlertViewHolder createViewHolder(View v, int viewType) {
//        return new AlertViewHolder(v);
//    }
//
//    @Override
//    public int itemLayoutResId(int viewType) {
//        return AlertViewHolder.holderResId();
//    }
//
//    @Override
//    public void contextReady() {
//
//    }
//
//
//    @Override
//    public void setupCallbacks() {
//        mAlertsCallback = new StatusCallback<List<Alert>>(){
//            @Override
//            public void onResponse(retrofit2.Response<List<Alert>> response, LinkHeaders linkHeaders, ApiType type) {
//                for(Alert alert : response.body()) {
//                    if(!alert.isDismissed()) {
//                        add(alert);
//                    }
//                }
//
//                updateUnreadCount();
//                if(!APIHelper.isCachedResponse(response)) {
//                    setAllPagesLoaded(true);
//
//                    mAdapterToFragmentCallback.onRefreshFinished();
//                }
//            }
//        };
//
//        mItemDismissedInterface = new ItemDismissedInterface() {
//            @Override
//            public void itemDismissed(Alert item, AlertViewHolder holder) {
//                remove(item);
//
//                AlertManager.markAlertAsDismissed(
//                        APIHelper.getAirwolfDomain(getContext()),
//                        mStudent.getParentId(),
//                        item.getStringId(),
//                        new StatusCallback<retrofit2.Response>(){}
//                );
//
//                if(!item.isMarkedRead()) {
//                    updateUnreadCount();
//                }
//            }
//        };
//    }
//
//    private void updateUnreadCount() {
//        int unreadCount = 0;
//        for(int i = 0; i < getItemCount(); i++) {
//            if(!getItemAtPosition(i).isMarkedRead()) {
//                unreadCount++;
//            }
//        }
//        mAdapterToFragmentCallback.onUpdateUnreadCount(unreadCount);
//    }
//
//    @Override
//    public void loadData() {
//        if(mStudent == null){
//            return;
//        }
//        AlertManager.getAlertsExhaustive(
//                APIHelper.getAirwolfDomain(getContext()),
//                mStudent.getParentId(),
//                mStudent.getStudentId(),
//                mAlertsCallback
//        );
//    }
//
//    @Override
//    public void bindHolder(Alert alert, AlertViewHolder holder, int position) {
//        AlertBinder.bind(alert, holder, mCourseList, mAdapterToFragmentCallback, mItemDismissedInterface, mContext);
//    }
//
//    /**
//     * Force a refresh of the adapter. Currently used when the user taps the alert
//     */
//    public void syncRefresh() {
//        if(mHasUpdatedCourses && mHasUpdatedUser) {
//            refresh();
//            mHasUpdatedCourses = false;
//            mHasUpdatedUser = false;
//        }
//    }
//
//    @Override
//    public void refresh() {
//
//        //cancel any currently running callbacks
//        if (mAlertsCallback != null) {
//            mAlertsCallback.cancel();
//            setupCallbacks();
//        }
//
//        getAdapterToRecyclerViewCallback().setDisplayNoConnection(false);
//        resetData();
//        loadData();
//        setRefresh(true);
//        //calling super.refresh() here resets the refresh flag and stops the empty view from showing the loading dialog, even though it should
//    }
//
//    public void setStudent(Student student, boolean refresh){
//        mHasUpdatedUser = true;
//        mStudent = student;
//
//        if(refresh) {
//            //we want to make sure the courses update before we get any new data
//            mHasUpdatedCourses = false;
//            //cancel any currently running callbacks
//            if (mAlertsCallback != null) {
//                mAlertsCallback.cancel();
//                setupCallbacks();
//            }
//
//            //update api
//            syncRefresh();
//        }
//    }
//
//    public void updateCourses(List<Course> courses) {
//        mHasUpdatedCourses = true;
//        mCourseList.clear();
//        mCourseList.addAll(courses);
//        syncRefresh();
//    }
//
//
//    private boolean compareAlerts(Alert oldAlert, Alert newAlert){
//        if(oldAlert.getTitle() != null && newAlert.getTitle() != null){
//            boolean sameTitle = oldAlert.getTitle().equals(newAlert.getTitle());
//            boolean sameState = oldAlert.isMarkedRead() == newAlert.isMarkedRead();
//            return sameState && sameTitle;
//        }
//        return false;
//    }
//
//    private int sortAlerts(Alert o1, Alert o2){
//        //First compare the read status of the alerts
//        int firstCompare = (o1.isMarkedRead() == o2.isMarkedRead() ? 0 : (o2.isMarkedRead() ? -1 : 1));
//        if(firstCompare != 0) {
//            //If they read status doesn't match, use that
//            return firstCompare;
//        } else {
//            //otherwise, check if the date is null
//            if(o1.getActionDate() == null && o2.getActionDate() == null) {
//                return 0;
//            } else if(o1.getActionDate() == null && o2.getActionDate() != null) {
//                return -1;
//            } else if(o1.getActionDate() != null && o2.getActionDate() == null) {
//                return 1;
//            } else {
//                //If the read status is the same, and the dates aren't null, compare them
//                return o2.getActionDate().compareTo(o1.getActionDate());
//            }
//        }
//    }
}

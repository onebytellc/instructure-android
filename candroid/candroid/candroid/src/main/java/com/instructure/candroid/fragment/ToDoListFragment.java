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

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.instructure.candroid.R;
import com.instructure.candroid.adapter.TodoListRecyclerAdapter;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.interfaces.NotificationAdapterToFragmentCallback;
import com.instructure.pandarecycler.PandaRecyclerView;
import com.instructure.pandautils.utils.Const;
import com.instructure.candroid.util.FragUtils;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.model.ScheduleItem;
import com.instructure.canvasapi.model.ToDo;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.pandautils.utils.RequestCodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

public class ToDoListFragment extends ParentFragment {

    private View mRootView;
    private View mEditOptions;
    private NotificationAdapterToFragmentCallback<ToDo> mAdapterToFragmentCallback;
    private TodoListRecyclerAdapter mRecyclerAdapter;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.toDoList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = getLayoutInflater().inflate(R.layout.fragment_list_todo, container, false);

        mAdapterToFragmentCallback = new NotificationAdapterToFragmentCallback<ToDo>() {
            @Override
            public void onRowClicked(ToDo todo, int position, boolean isOpenDetail) {
                mRecyclerAdapter.setSelectedPosition(position);
                onRowClick(todo);
            }
            @Override
            public void onRefreshFinished() {
                setRefreshing(false);
                mEditOptions.setVisibility(View.GONE);
            }

            @Override
            public void onShowEditView(boolean isVisible) {
               mEditOptions.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onShowErrorCrouton(int message) {
                // do nothing
            }
        };
        mRecyclerAdapter = new TodoListRecyclerAdapter(getContext(), getCanvasContext(), mAdapterToFragmentCallback);
        configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView);
        PandaRecyclerView pandaRecyclerView = (PandaRecyclerView) mRootView.findViewById(R.id.listView);
        pandaRecyclerView.setSelectionEnabled(false);
        configureViews(mRootView);

        return mRootView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView);
    }

    private void onRowClick(ToDo toDo) {
        if (toDo == null) { return; }
        Bundle bundle = new Bundle();

        bundle.putParcelable(Const.SELECTED_ITEM, toDo);

        Navigation navigation = getNavigation();
        if(navigation != null){
            if (toDo.getAssignment() != null) {
                //Launch assignment details fragment.
                navigation.addFragment(FragUtils.getFrag(AssignmentFragment.class, createBundle(toDo.getCanvasContext(), toDo.getAssignment())));
            } else if (toDo.getScheduleItem() != null) {
                //It's a Calendar event from the Upcoming API.
                ScheduleItem scheduleItem = toDo.getScheduleItem();
                String actionBarTitle = "";
                if (scheduleItem.getContextType() == CanvasContext.Type.COURSE) {
                    actionBarTitle = toDo.getCanvasContext().getName();
                } else if (scheduleItem.getContextType() == CanvasContext.Type.USER) {
                    actionBarTitle = getContext().getString(R.string.PersonalCalendar);
                }
                navigation.addFragment(FragUtils.getFrag(CalendarEventFragment.class, createBundle(toDo.getCanvasContext(), actionBarTitle, toDo.getScheduleItem())));
            }
        }
    }

    private void configureViews(View rootView) {
        mEditOptions = rootView.findViewById(R.id.editOptions);
        Button confirmButton = (Button) rootView.findViewById(R.id.confirmButton);
        confirmButton.setText(getString(R.string.markAsDone));
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerAdapter.confirmButtonClicked();
            }
        });


        Button cancelButton = (Button) rootView.findViewById(R.id.cancelButton);
        cancelButton.setText(R.string.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerAdapter.cancelButtonClicked();
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        if(extras.containsKey(Const.SELECTED_ITEM)){
            ToDo todo = extras.getParcelable(Const.SCHEDULE_ITEM);
            setDefaultSelectedId(todo.getId());
        }
    }

    public static Bundle createBundle(CanvasContext canvasContext,  Assignment assignment) {
        Bundle extras = createBundle(canvasContext);
        extras.putParcelable(Const.ASSIGNMENT, assignment);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String title, ScheduleItem scheduleItem) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.TITLE, title);
        extras.putParcelable(Const.SCHEDULE_ITEM, scheduleItem);
        return extras;
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}

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

package com.instructure.candroid.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.instructure.candroid.R;
import com.instructure.candroid.binders.ExpandableHeaderBinder;
import com.instructure.candroid.binders.TodoBinder;
import com.instructure.candroid.holders.ExpandableViewHolder;
import com.instructure.candroid.holders.TodoViewHolder;
import com.instructure.candroid.interfaces.NotificationAdapterToFragmentCallback;
import com.instructure.canvasapi.api.CalendarEventAPI;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.GroupAPI;
import com.instructure.canvasapi.api.ToDoAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.model.ScheduleItem;
import com.instructure.canvasapi.model.ToDo;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class TodoListRecyclerAdapter extends ExpandableRecyclerAdapter<Date, ToDo, RecyclerView.ViewHolder> {

    private NotificationAdapterToFragmentCallback<ToDo> mAdapterToFragmentCallback;
    private TodoCheckboxCallback mTodoCheckboxCallback;

    private Map<Long, Course> mCourseMap;
    private Map<Long, Group> mGroupMap;
    private ArrayList<ToDo> mTodoList;
    private ArrayList<ToDo> mScheduleList;


    private CanvasCallback<ScheduleItem[]> mScheduleItemCallback;
    private CanvasCallback<ToDo[]> mTodoCallback;
    private CanvasCallback<Course[]> mCoursesCallback;
    private CanvasCallback<Group[]> mGroupsCallback;
    private CanvasContext mCanvasContext;

    private HashSet<ToDo> mCheckedTodos = new HashSet<>();
    private HashSet<ToDo> mDeletedTodos = new HashSet<>();

    private boolean mIsEditMode;
    private boolean mIsNoNetwork; // With multiple callbacks, some could fail while others don't. This manages when to display no connection when offline

    // region Interfaces
    public interface TodoCheckboxCallback {
        void onCheckChanged(ToDo todo, boolean isChecked, int position);
        boolean isEditMode();
    }

    // endregion

    /* For testing purposes only */
    protected TodoListRecyclerAdapter(Context context){
        super(context, Date.class, ToDo.class);
    }

    public TodoListRecyclerAdapter(Context context, CanvasContext canvasContext, NotificationAdapterToFragmentCallback<ToDo> adapterToFragmentCallback) {
        super(context, Date.class, ToDo.class);
        mCanvasContext = canvasContext;
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        mIsEditMode = false;
        setExpandedByDefault(true);
        loadData();
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return new ExpandableViewHolder(v);
        } else {
            return new TodoViewHolder(v);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return ExpandableViewHolder.holderResId();
        } else {
            return TodoViewHolder.holderResId();
        }
    }

    @Override
    public void contextReady() {

    }

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, Date date, ToDo todo) {
        TodoBinder.bind(getContext(), (TodoViewHolder) holder, todo, mAdapterToFragmentCallback, mTodoCheckboxCallback);
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, Date date, boolean isExpanded) {
        ExpandableHeaderBinder.bind(getContext(), mCanvasContext, (ExpandableViewHolder) holder, date, DateHelpers.getFormattedDate(getContext(), date), isExpanded, getViewHolderHeaderClicked());
    }

    @Override
    public void loadData() {
        CourseAPI.getAllCourses(mCoursesCallback);
        GroupAPI.getAllGroups(mGroupsCallback);

        ToDoAPI.getTodos(mCanvasContext, mTodoCallback);
        CalendarEventAPI.getUpcomingEvents(mScheduleItemCallback);
    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {
        // Workaround for the multiple callbacks, some will succeed while others don't
        setLoadedFirstPage(true);
        shouldShowLoadingFooter();
        AdapterToRecyclerViewCallback adapterToRecyclerViewCallback = getAdapterToRecyclerViewCallback();
        if(adapterToRecyclerViewCallback != null){
            if (!mIsNoNetwork) { // double negative, only happens when there is network
                adapterToRecyclerViewCallback.setDisplayNoConnection(false);
                getAdapterToRecyclerViewCallback().setIsEmpty(isAllPagesLoaded() && size() == 0);
            }
        }
    }

    @Override
    public void onNoNetwork() {
        super.onNoNetwork();
        mIsNoNetwork = true;
    }

    @Override
    public void refresh() {
        mIsNoNetwork = false;
        getAdapterToRecyclerViewCallback().setDisplayNoConnection(false);
        super.refresh();
    }

    private void populateAdapter() {
        if(mTodoList == null || mScheduleList == null || mCourseMap == null || mGroupMap == null) {
            return;
        }

        ArrayList<ToDo> todos = ToDoAPI.mergeToDoUpcoming(mTodoList, mScheduleList);

        // now populate the todoList and upcomingList with the course information
        for(ToDo toDo : todos) {
            ToDo.setContextInfo(toDo, mCourseMap, mGroupMap);
            addOrUpdateItem(Utils.getCleanDate(toDo.getComparisonDate().getTime()), toDo);
        }
        mAdapterToFragmentCallback.onRefreshFinished();
        setAllPagesLoaded(true);

        //reset the lists
        mTodoList = null;
        mScheduleList = null;
        mCourseMap = null;
        mGroupMap = null;
    }

    @Override
    public void setupCallbacks() {
        mTodoCheckboxCallback = new TodoCheckboxCallback() {
            @Override
            public void onCheckChanged(ToDo todo, boolean isChecked, int position) {
                // We don't want to let them try to delete things while offline because they
                // won't actually delete them from the server
                if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                    Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                    return;
                }

                todo.setChecked(isChecked);
                if (isChecked && !mDeletedTodos.contains(todo)) {
                    mCheckedTodos.add(todo);
                } else {
                    mCheckedTodos.remove(todo);
                }

                //If we aren't in the edit mode, enable edit mode for future clicks

                if(!mIsEditMode){
                    mIsEditMode = true;
                } else if (mCheckedTodos.size() == 0){ //if this was the last item, cancel
                    mIsEditMode = false;
                }

                mAdapterToFragmentCallback.onShowEditView(mCheckedTodos.size() > 0);
                notifyItemChanged(position);
            }

            @Override
            public boolean isEditMode() {
                return mIsEditMode;
            }
        };

        mCoursesCallback = new CanvasCallback<Course[]>(this) {
            @Override
            public void firstPage(Course[] courses, LinkHeaders linkHeaders, Response response) {
                mCourseMap = CourseAPI.createCourseMap(courses);
                populateAdapter();
            }
        };

        mGroupsCallback = new CanvasCallback<Group[]>(this) {
            @Override
            public void firstPage(Group[] groups, LinkHeaders linkHeaders, Response response) {
                mGroupMap = GroupAPI.createGroupMap(groups);
                populateAdapter();
            }
        };

        mTodoCallback = new CanvasCallback<ToDo[]>(this) {

            @Override
            public void firstPage(ToDo[] toDos, LinkHeaders linkHeaders, Response response) {
                mTodoList = new ArrayList<ToDo>(Arrays.asList(toDos));
                populateAdapter();

                // remove the todos that have been deleted
                for (ToDo toDo : mDeletedTodos) {
                    removeItem(toDo);
                }
            }
        };

        mScheduleItemCallback = new CanvasCallback<ScheduleItem[]>(this) {

            @Override
            public void firstPage(ScheduleItem[] upcoming, LinkHeaders linkHeaders, Response response) {
                mScheduleList = new ArrayList<>();
                for(ScheduleItem scheduleItem : upcoming) {
                    if (mCanvasContext.getType() == CanvasContext.Type.USER) {
                        mScheduleList.add(ToDo.toDoWithScheduleItem(scheduleItem));
                    } else if (scheduleItem.getContextId() == mCanvasContext.getId()) { // filter out the upcoming events just for the context
                        mScheduleList.add(ToDo.toDoWithScheduleItem(scheduleItem));
                    }
                }
                populateAdapter();
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                if (retrofitError.getResponse() != null && !APIHelpers.isCachedResponse(retrofitError.getResponse()) || !CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                    getAdapterToRecyclerViewCallback().setIsEmpty(true);
                }
                return super.onFailure(retrofitError);
            }
        };

    }

    // endregion

    // region Data

    public void confirmButtonClicked() {
        for (ToDo todo : mCheckedTodos) {
            hideTodoItem(todo);
            mDeletedTodos.add(todo);
        }
        mIsEditMode = false;
        clearMarked();
    }

    public void cancelButtonClicked() {
        for (ToDo todo : mCheckedTodos) {
            todo.setChecked(false);
        }
        mIsEditMode = false;
        clearMarked();
        notifyDataSetChanged();
    }

    public void clearMarked() {
        mCheckedTodos.clear();
        mAdapterToFragmentCallback.onShowEditView(mCheckedTodos.size() > 0);
    }


    private void hideTodoItem(final ToDo todo) {
        ToDoAPI.dismissTodo(getContext(), todo, new CanvasCallback<Response>(this) {
            @Override public void cache(Response response) {}

            @Override
            public void firstPage(Response response, LinkHeaders linkHeaders, Response response2) {
                removeItem(todo);
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                mDeletedTodos.remove(todo);
                return false;
            }
        });
    }

    // endregion

    // region Expandable Callbacks

    @Override
    public GroupSortedList.GroupComparatorCallback<Date> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<Date>() {
            @Override
            public int compare(Date o1, Date o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(Date oldGroup, Date newGroup) {
                return oldGroup.equals(newGroup);
            }

            @Override
            public boolean areItemsTheSame(Date group1, Date group2) {
                return group1.getTime() == group2.getTime();
            }

            @Override
            public long getUniqueGroupId(Date group) {
                return group.getTime();
            }

            @Override
            public int getGroupType(Date group) {
                return Types.TYPE_HEADER;
            }
        };
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<Date, ToDo> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<Date, ToDo>() {
            @Override
            public int compare(Date group, ToDo o1, ToDo o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(ToDo oldItem, ToDo newItem) {
                return oldItem.getTitle().equals(newItem.getTitle()); // cache is not used
            }

            @Override
            public boolean areItemsTheSame(ToDo item1, ToDo item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public long getUniqueItemId(ToDo item) {
                return item.getId();
            }

            @Override
            public int getChildType(Date group, ToDo item) {
                return Types.TYPE_ITEM;
            }
        };
    }

    // endregion
}

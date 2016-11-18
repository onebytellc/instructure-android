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

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.instructure.candroid.R;
import com.instructure.candroid.binders.CourseBinder;
import com.instructure.candroid.holders.CourseHeaderViewHolder;
import com.instructure.candroid.holders.CourseViewHolder;
import com.instructure.candroid.interfaces.CourseAdapterToFragmentCallback;
import com.instructure.candroid.model.CourseToggleHeader;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.GroupAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.CanvasModel;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;
import com.instructure.pandautils.utils.Const;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import retrofit.client.Response;

public class CourseRecyclerAdapter extends ExpandableRecyclerAdapter<CourseToggleHeader, CanvasContext, RecyclerView.ViewHolder>{

    private CourseAdapterToFragmentCallback mAdapterToFragmentCallback;

    public static final int ALL_COURSES_ID = 1111;
    public static final int FAV_COURSES_ID = 2222;
    private static final int ALL_GROUPS_ID = 3333;
    private static final int FAV_GROUPS_ID = 4444;

    private Map<Integer, ArrayList<CanvasContext>> mCallbackSyncHash = new HashMap<>();
    private Map<CanvasContext, Boolean> mGradesTabHidden = new HashMap<>();

    private CourseToggleHeader mGroupHeader;
    private CourseToggleHeader mCourseHeader;

    //callbacks
    private CanvasCallback<Course[]> mAllCoursesCallback;
    private CanvasCallback<Group[]> mGroupsCallback;
    private ArrayList<Course> mCourseList = new ArrayList<>();

    private boolean mShowGrades = false;

    public CourseRecyclerAdapter(Activity context, CourseAdapterToFragmentCallback adapterToFragmentCallback) {
        super(context, CourseToggleHeader.class, CanvasModel.class);
        mGroupHeader = makeHeader(R.string.groups, false);
        mCourseHeader = makeHeader(R.string.courses, true);
        mShowGrades = ApplicationManager.getPrefs(context).load(Const.SHOW_GRADES_ON_CARD, true);
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        setExpandedByDefault(true);
        loadData();
    }

    public CourseToggleHeader getItemGroupHeader(@Nullable CanvasContext canvasContext) {
        if(canvasContext instanceof Course) {
            return mCourseHeader;
        } else if(canvasContext instanceof Group) {
            return mGroupHeader;
        }
        return null;
    }

    public void setShowGrades(boolean showGrades) {
        mShowGrades = showGrades;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        switch (viewType) {
            case Types.TYPE_ITEM:
                return new CourseViewHolder(v);
            default:
                return new CourseHeaderViewHolder(v);
        }
    }

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, CourseToggleHeader header, final CanvasContext canvasContext) {
        if(allDataAvailable()) {
            boolean gradesTabHidden = false;
            if(mGradesTabHidden.containsKey(canvasContext)) {
                //TODO when we get an include=tabs option in courses/groups use this to determine if the grade should be clickable to route users.
                gradesTabHidden = mGradesTabHidden.get(canvasContext);
            }
            CourseBinder.bind((Activity)getContext(), canvasContext, (CourseViewHolder) holder, mCallbackSyncHash.get(ALL_COURSES_ID), mShowGrades, gradesTabHidden, mAdapterToFragmentCallback);
        }
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, CourseToggleHeader header, boolean isExpanded) {
        CourseBinder.bindHeader(header, (CourseHeaderViewHolder) holder);
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<CourseToggleHeader, CanvasContext> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<CourseToggleHeader, CanvasContext>() {
            @Override
            public int compare(CourseToggleHeader group, CanvasContext o1, CanvasContext o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(CanvasContext oldItem, CanvasContext newItem) {
                return false;
            }

            @Override
            public boolean areItemsTheSame(CanvasContext item1, CanvasContext item2) {
                return item1.getContextId().hashCode() == item2.getContextId().hashCode();
            }

            @Override
            public long getUniqueItemId(CanvasContext item) {
                return item.getContextId().hashCode();
            }

            @Override
            public int getChildType(CourseToggleHeader group, CanvasContext item) {
                return Types.TYPE_ITEM;
            }
        };
    }

    @Override
    public GroupSortedList.GroupComparatorCallback<CourseToggleHeader> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<CourseToggleHeader>() {
            @Override
            public int compare(CourseToggleHeader o1, CourseToggleHeader o2) {
                return 0;
            }

            @Override
            public boolean areContentsTheSame(CourseToggleHeader oldGroup, CourseToggleHeader newGroup) {
                return oldGroup.equals(newGroup);
            }

            @Override
            public boolean areItemsTheSame(CourseToggleHeader group1, CourseToggleHeader group2) {
                return group1.equals(group2);
            }

            @Override
            public long getUniqueGroupId(CourseToggleHeader group) {
                return group.hashCode();
            }

            @Override
            public int getGroupType(CourseToggleHeader group) {
                return Types.TYPE_HEADER;
            }
        };
    }

    @Override
    public void loadData() {
        CourseAPI.getAllCourses(mAllCoursesCallback);
        GroupAPI.getAllGroups(mGroupsCallback);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        switch (viewType) {
            case Types.TYPE_ITEM:
                return CourseViewHolder.holderResId();
            default:
                return CourseHeaderViewHolder.holderResId();
        }
    }

    @Override
    public void contextReady() {}

    @Override
    public void setupCallbacks() {
        mAllCoursesCallback = new CanvasCallback<Course[]>(this) {
            @Override
            public void firstPage(Course[] courses, LinkHeaders linkHeaders, Response response) {
                mCallbackSyncHash.put(ALL_COURSES_ID, new ArrayList<CanvasContext>(Arrays.asList(courses)));
                mCallbackSyncHash.put(FAV_COURSES_ID, getFavoritesFromAllCourses(courses));
                boolean isCache = APIHelpers.isCachedResponse(response);
                syncCallbacks(isCache ? SOURCE.CACHE : SOURCE.API);
                if (!isCache) {
                    Analytics.trackDomain((Activity) getContext());
                    Analytics.trackEnrollment((Activity) getContext(), courses);
                }
            }
        };

        mGroupsCallback = new CanvasCallback<Group[]>(this) {
            @Override
            public void firstPage(Group[] groups, LinkHeaders linkHeaders, Response response) {
                mCallbackSyncHash.put(ALL_GROUPS_ID, new ArrayList<CanvasContext>(Arrays.asList(groups)));
                mCallbackSyncHash.put(FAV_GROUPS_ID, getFavoritesFromAllGroups(groups));
                boolean isCache = APIHelpers.isCachedResponse(response);
                syncCallbacks(isCache ? SOURCE.CACHE : SOURCE.API);
            }
        };
    }

    public void removeCallbacks() {
        if(mGroupsCallback != null) {
            mGroupsCallback.cancel();
        }
        if(mAllCoursesCallback != null) {
            mAllCoursesCallback.cancel();
        }
    }

    public void refreshAdapter() {
        if(allDataAvailable()) {
            clear();
            addOrUpdateAllItems(mGroupHeader, mCallbackSyncHash.get(FAV_GROUPS_ID));
            addOrUpdateAllItems(mCourseHeader, mCallbackSyncHash.get(FAV_COURSES_ID));

            notifyDataSetChanged();
            setAllPagesLoaded(true);
            if(getItemCount() == 0) {
                getAdapterToRecyclerViewCallback().setIsEmpty(true);
            }
        }
    }

    private CourseToggleHeader makeHeader(int textResId, boolean clickable) {
        CourseToggleHeader header = new CourseToggleHeader();
        header.text = getContext().getResources().getString(textResId);
        header.clickable = clickable;
        return header;
    }

    @Override
    public void refresh() {
        mCallbackSyncHash.clear();
        setupCallbacks();
        super.refresh();
    }

    private void syncCallbacks(CanvasCallback.SOURCE source) {
        if(!allDataAvailable()) {
            return;
        }

        refreshAdapter();
        mAdapterToFragmentCallback.onRefreshFinished();
    }

    private ArrayList<CanvasContext> getFavoritesFromAllCourses(Course[] courses) {
        ArrayList<CanvasContext> favs = new ArrayList<>();
        for(CanvasContext canvasContext : courses) {
            if(((Course)canvasContext).isFavorite()) {
                favs.add(canvasContext);
                mCourseList.add((Course)canvasContext);
            }
        }
        return favs;
    }

    private ArrayList<CanvasContext> getFavoritesFromAllGroups(Group[] groups) {
        ArrayList<CanvasContext> favs = new ArrayList<>();
        for(CanvasContext canvasContext : groups) {
            if(((Group)canvasContext).isFavorite()) {
                favs.add(canvasContext);
            }
        }
        return favs;
    }

    private boolean allDataAvailable() {
        return mCallbackSyncHash.containsKey(ALL_COURSES_ID) && mCallbackSyncHash.containsKey(ALL_GROUPS_ID);
    }
}

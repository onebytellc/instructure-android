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

import com.instructure.candroid.R;
import com.instructure.candroid.binders.ExpandableHeaderBinder;
import com.instructure.candroid.binders.SyllabusBinder;
import com.instructure.candroid.holders.ExpandableViewHolder;
import com.instructure.candroid.holders.SyllabusItemViewHolder;
import com.instructure.candroid.holders.SyllabusViewHolder;
import com.instructure.candroid.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi.api.CalendarEventAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.ScheduleItem;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;
import com.instructure.pandautils.utils.CanvasContextColor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit.client.Response;

public class SyllabusRecyclerAdapter extends ExpandableRecyclerAdapter<String, ScheduleItem, RecyclerView.ViewHolder> {

    private boolean mAddSyllabus = true;
    private CanvasContext mCanvasContext;
    // region callback
    private int mApiCallbackCount = 0;
    private long EVENTS_ID = 2222;
    private long ASSIGNMENTS_ID = 3333;
    private CanvasCallback<ScheduleItem[]> mScheduleCallback;
    private CanvasCallback<ScheduleItem[]> mAssignmentCallback;
    private HashMap<Long, ScheduleItem[]> mCallbackSyncHash = new HashMap<>(); // schedule and assignment have their own ids. When both callbacks return, the items are added to the adapter.
    // endregion
    private AdapterToFragmentCallback<ScheduleItem> mAdapterToFragmentCallback;
    private String mPast;
    private String mNext7Days;
    private String mFuture;
    private String mNoDate;
    private String mSyllabus;

    /* For testing purposes only */
    protected SyllabusRecyclerAdapter(Context context){
        super(context, String.class, ScheduleItem.class);

    }

    public SyllabusRecyclerAdapter(
            Context context,
            CanvasContext canvasContext,
            AdapterToFragmentCallback<ScheduleItem> adapterToFragmentCallback) {

        super(context, String.class, ScheduleItem.class);
        mCanvasContext = canvasContext;
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        mPast = mContext.getString(R.string.past);
        mNext7Days = mContext.getString(R.string.next7Days);
        mFuture = mContext.getString(R.string.future);
        mNoDate = mContext.getString(R.string.noDate);
        mSyllabus = mContext.getString(R.string.syllabus);
        setExpandedByDefault(true);
        loadData();
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return new ExpandableViewHolder(v);
        } else if(viewType == Types.TYPE_SYLLABUS) {
            return new SyllabusItemViewHolder(v);
        } else {
            return new SyllabusViewHolder(v);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return ExpandableViewHolder.holderResId();
        } else if(viewType == Types.TYPE_SYLLABUS) {
            return SyllabusItemViewHolder.holderResId();
        } else {
            return SyllabusViewHolder.holderResId();
        }
    }

    @Override
    public void contextReady() {

    }

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, String s, ScheduleItem scheduleItem) {
        if(scheduleItem != null) {
            final int courseColor = CanvasContextColor.getCachedColor(getContext(), mCanvasContext);
            if (scheduleItem.getType() == ScheduleItem.Type.TYPE_SYLLABUS) {
                SyllabusBinder.bindSyllabusItem(getContext(), (SyllabusItemViewHolder) holder, courseColor, scheduleItem, mAdapterToFragmentCallback);
            } else {
                SyllabusBinder.bind(getContext(), (SyllabusViewHolder) holder, courseColor, scheduleItem, mAdapterToFragmentCallback);
            }
        }
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, String s, boolean isExpanded) {
        ExpandableHeaderBinder.bind(getContext(), mCanvasContext, (ExpandableViewHolder) holder, s, s, isExpanded, getViewHolderHeaderClicked());
    }



    // region Data


    @Override
    public void refresh() {
        mCallbackSyncHash.clear();
        mApiCallbackCount = 0;
        super.refresh();
    }

    @Override
    public void loadData() {
        CalendarEventAPI.getAllCalendarEventsExhaustive(CalendarEventAPI.EVENT_TYPE.ASSIGNMENT_EVENT, getContextCodes(), mAssignmentCallback);
        CalendarEventAPI.getAllCalendarEventsExhaustive(CalendarEventAPI.EVENT_TYPE.CALENDAR_EVENT, getContextCodes(), mScheduleCallback);
    }

    private void populateAdapter(ScheduleItem[] scheduleItems) {
        if (mAddSyllabus && mCanvasContext.getType() == CanvasContext.Type.COURSE) {
            Course course = (Course)mCanvasContext;
            ScheduleItem syllabus = ScheduleItem.createSyllabus(course.getName(), course.getSyllabusBody());
            addOrUpdateItem(mSyllabus, syllabus);
        }
        Date curDate = new Date();
        //set a future date 7 days in the future, make it the end of the day to include every assignment within the next 7 days,
        //including assignments that are due at the end of the 7th day
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 7);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);

        Date weekFutureDate = calendar.getTime();

        for (ScheduleItem scheduleItem : scheduleItems) {

            //if it is hidden we don't want to show it. This can happen when an event has multiple sections
            if((scheduleItem.isHidden())) {
                continue;
            }
            Date dueDate = scheduleItem.getStartDate();

            if(dueDate == null) {
                addOrUpdateItem(mNoDate, scheduleItem);
            } else if(dueDate.before(curDate)) {
                addOrUpdateItem(mPast, scheduleItem);
            } else if(((dueDate.after(curDate) && (dueDate.before(weekFutureDate))) || dueDate.equals(weekFutureDate)) ) {
                addOrUpdateItem(mNext7Days, scheduleItem);
            } else if(dueDate.after(weekFutureDate)) {
                addOrUpdateItem(mFuture, scheduleItem);
            }
        }
    }

    private void syncCallbacks(CanvasCallback.SOURCE source) {
        if (mCallbackSyncHash.keySet().size() < 2) {
            return;
        }

        for (Map.Entry<Long, ScheduleItem[]> entry : mCallbackSyncHash.entrySet()) {
            populateAdapter(entry.getValue());
        }

        if (source.isAPI()) {
            mAdapterToFragmentCallback.onRefreshFinished();
        }
    }

    public void removeCallbacks() {
        if(mScheduleCallback != null) {
            mScheduleCallback.cancel();
        }

        if(mAssignmentCallback != null) {
            mAssignmentCallback.cancel();
        }
    }

    @Override
    public void setupCallbacks() {
        mScheduleCallback = new CanvasCallback<ScheduleItem[]>(this) {
            @Override
            public void cache(ScheduleItem[] scheduleItems, LinkHeaders linkHeaders, Response response) {
                mCallbackSyncHash.put(EVENTS_ID, scheduleItems);
                syncCallbacks(SOURCE.CACHE);
            }

            @Override
            public void firstPage(ScheduleItem[] scheduleItems, LinkHeaders linkHeaders, Response response) {
                mCallbackSyncHash.put(EVENTS_ID, scheduleItems);
                syncCallbacks(SOURCE.API);
            }
        };

        mAssignmentCallback = new CanvasCallback<ScheduleItem[]>(this) {
            @Override
            public void cache(ScheduleItem[] scheduleItems, LinkHeaders linkHeaders, Response response) {
                mCallbackSyncHash.put(ASSIGNMENTS_ID, scheduleItems);
                syncCallbacks(SOURCE.CACHE);
            }

            @Override
            public void firstPage(ScheduleItem[] scheduleItems, LinkHeaders linkHeaders, Response response) {
                mCallbackSyncHash.put(ASSIGNMENTS_ID, scheduleItems);
                syncCallbacks(SOURCE.API);
            }
        };
    }

    private ArrayList<String> getContextCodes() {
        return new ArrayList<String>() {{ add(mCanvasContext.getContextId()); }};
    }

    // endregion

    // region Expandable Callbacks

    private int getGroupPosition(String groupHeader) {
        if (mSyllabus.equals(groupHeader)) {
            return 0;
        } else if (mNext7Days.equals(groupHeader)) {
            return 1;
        } else if (mFuture.equals(groupHeader)) {
            return 2;
        } else if (mNoDate.equals(groupHeader)) {
            return 3;
        } else {
            return 4;
        }
    }

    @Override
    public GroupSortedList.GroupComparatorCallback<String> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<String>() {
            @Override
            public int compare(String o1, String o2) {
                return getGroupPosition(o1) - getGroupPosition(o2);
            }

            @Override
            public boolean areContentsTheSame(String oldGroup, String newGroup) {
                return oldGroup.equals(newGroup);
            }

            @Override
            public boolean areItemsTheSame(String group1, String group2) {
                return group1.equals(group2);
            }

            @Override
            public long getUniqueGroupId(String group) {
                return group.hashCode();
            }

            @Override
            public int getGroupType(String group) {
                return Types.TYPE_HEADER;
            }
        };
    }

    private boolean isNullableChanged(Object o1, Object o2) {
        return (o1 == null && o2 != null) || (o1 !=null && o2 == null);
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<String, ScheduleItem> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<String, ScheduleItem>() {
            @Override
            public int compare(String group, ScheduleItem o1, ScheduleItem o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(ScheduleItem oldItem, ScheduleItem newItem) {
                boolean isStartDateTheSame = true;
                if (isNullableChanged(oldItem.getStartDate(), newItem.getStartDate())) {
                    return false;
                } else if (oldItem.getStartDate() != null && newItem.getStartDate() != null) {
                    isStartDateTheSame = oldItem.getStartDate().equals(newItem.getStartDate());
                }
                return isStartDateTheSame && oldItem.getTitle().equals(newItem.getTitle());
            }

            @Override
            public boolean areItemsTheSame(ScheduleItem item1, ScheduleItem item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public long getUniqueItemId(ScheduleItem item) {
                return item.getId();
            }

            @Override
            public int getChildType(String group, ScheduleItem item) {
                if(item.getType() == ScheduleItem.Type.TYPE_SYLLABUS) {
                    return Types.TYPE_SYLLABUS;
                }
                return Types.TYPE_ITEM;
            }
        };
    }
    // endregion
}

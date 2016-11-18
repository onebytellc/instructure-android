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
import com.instructure.candroid.binders.NotificationBinder;
import com.instructure.candroid.fragment.NotificationListFragment;
import com.instructure.candroid.holders.ExpandableViewHolder;
import com.instructure.candroid.holders.NotificationViewHolder;
import com.instructure.candroid.interfaces.NotificationAdapterToFragmentCallback;
import com.instructure.canvasapi.api.ConversationAPI;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.GroupAPI;
import com.instructure.canvasapi.api.StreamAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Conversation;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.model.HiddenStreamItem;
import com.instructure.canvasapi.model.StreamItem;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class NotificationListRecyclerAdapter extends ExpandableRecyclerAdapter<Date, StreamItem, RecyclerView.ViewHolder> {

    private NotificationAdapterToFragmentCallback<StreamItem> mAdapterToFragmentCallback;
    private NotificationListFragment.OnNotificationCountInvalidated mOnNotificationCountInvalidated;
    private NotificationCheckboxCallback mNotificationCheckboxCallback;

    private StreamItem[] mStreamItems;
    private Map<Long, Course> mCourseMap;
    private Map<Long, Group> mGroupMap;

    private CanvasCallback<StreamItem[]> mStreamCallback;
    private CanvasCallback<Course[]> mCoursesCallback;
    private CanvasCallback<Group[]> mGroupsCallback;
    private CanvasContext mCanvasContext;

    private HashSet<StreamItem> mCheckedStreamItems = new HashSet<>();
    private HashSet<StreamItem> mDeletedStreamItems = new HashSet<>();

    private boolean mIsEditMode;
    private boolean mIsNoNetwork; // With multiple callbacks, some could fail while others don't. This manages when to display no connection when offline

    // region Interfaces
    public interface NotificationCheckboxCallback {
        void onCheckChanged(StreamItem streamItem, boolean isChecked, int position);
        boolean isEditMode();
    }

    // endregion

    public NotificationListRecyclerAdapter(
            Context context,
            CanvasContext canvasContext,
            NotificationListFragment.OnNotificationCountInvalidated onNotificationCountInvalidated,
            NotificationAdapterToFragmentCallback<StreamItem> adapterToFragmentCallback) {
        super(context, Date.class, StreamItem.class);
        mCanvasContext = canvasContext;
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        mOnNotificationCountInvalidated = onNotificationCountInvalidated;
        mIsEditMode = false;
        setExpandedByDefault(true);
        loadData();
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return new ExpandableViewHolder(v);
        } else {
            return new NotificationViewHolder(v);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return ExpandableViewHolder.holderResId();
        } else {
            return NotificationViewHolder.holderResId();
        }
    }

    @Override
    public void contextReady() {

    }

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, Date date, StreamItem streamItem) {
        NotificationBinder.bind(getContext(), (NotificationViewHolder) holder, streamItem, mNotificationCheckboxCallback, mAdapterToFragmentCallback);
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, Date date, boolean isExpanded) {
        ExpandableHeaderBinder.bind(getContext(), mCanvasContext, (ExpandableViewHolder) holder, date, DateHelpers.getFormattedDate(getContext(), date), isExpanded, getViewHolderHeaderClicked());
    }

    // region Pagination

    @Override
    public boolean isPaginated() {
        return true;
    }

    @Override
    public void loadFirstPage() {
        CourseAPI.getAllCourses(mCoursesCallback);
        GroupAPI.getAllGroups(mGroupsCallback);

        if (mCanvasContext.getType() == CanvasContext.Type.USER) {
            StreamAPI.getFirstPageUserStream(mStreamCallback);
        } else {
            StreamAPI.getFirstPageCourseStream(mCanvasContext, mStreamCallback);
        }
    }

    @Override
    public void loadNextPage(String nextURL) {
        StreamAPI.getNextPageStream(nextURL, mStreamCallback);
    }

    @Override
    public void setupCallbacks() {
        mNotificationCheckboxCallback = new NotificationCheckboxCallback() {
            @Override
            public void onCheckChanged(StreamItem streamItem, boolean isChecked, int position) {
                streamItem.setChecked(isChecked);
                if (isChecked && !mDeletedStreamItems.contains(streamItem)) {
                    mCheckedStreamItems.add(streamItem);
                } else {
                    mCheckedStreamItems.remove(streamItem);
                }

                //If we aren't in the edit mode, enable edit mode for future clicks
                if(!mIsEditMode){
                    mIsEditMode = true;
                } else if (mCheckedStreamItems.size() == 0){ //if this was the last item, cancel
                    mIsEditMode = false;
                }

                mAdapterToFragmentCallback.onShowEditView(mCheckedStreamItems.size() > 0);
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
                populateActivityStreamAdapter();
            }
        };

        mGroupsCallback = new CanvasCallback<Group[]>(this) {
            @Override
            public void firstPage(Group[] groups, LinkHeaders linkHeaders, Response response) {
                mGroupMap = GroupAPI.createGroupMap(groups);
                populateActivityStreamAdapter();
            }
        };

        mStreamCallback = new CanvasCallback<StreamItem[]>(this) {

            private void checkPreviouslyCheckedItems(StreamItem[] items) {
                for (StreamItem item : items) {
                    if (mCheckedStreamItems.contains(item)) {
                        // update it do the actual item (the right object reference)
                        mCheckedStreamItems.remove(item);
                        mCheckedStreamItems.add(item);

                        item.setChecked(true);
                    }
                }
            }

            @Override
            public void cache(StreamItem[] streamItems, LinkHeaders linkHeaders, Response response) {
                checkPreviouslyCheckedItems(streamItems);

                mStreamItems = streamItems;
                populateActivityStreamAdapter();

                //Remove items from the adapter that have been deleted.
                for (StreamItem streamItem : mDeletedStreamItems){
                    removeItem(streamItem);
                }
                setNextUrl(linkHeaders.nextURL);
            }

            @Override
            public void firstPage(StreamItem[] items, LinkHeaders linkHeaders, Response response) {
                // we could have had changed dates since the last time we loaded
                checkPreviouslyCheckedItems(items);

                mStreamItems = items;
                populateActivityStreamAdapter();
                mAdapterToFragmentCallback.onRefreshFinished();

                //Clear out the cached deleted items.
                mDeletedStreamItems.clear();
                setNextUrl(linkHeaders.nextURL);
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
        for (StreamItem streamItem : mCheckedStreamItems) {
            hideStreamItem(streamItem);
            mDeletedStreamItems.add(streamItem);
        }
        mIsEditMode = false;
        clearMarked();
    }

    public void cancelButtonClicked() {
        for (StreamItem streamItem : mCheckedStreamItems) {
            streamItem.setChecked(false);
        }
        mIsEditMode = false;
        clearMarked();
        notifyDataSetChanged();
    }

    public void clearMarked() {
        mCheckedStreamItems.clear();
        mAdapterToFragmentCallback.onShowEditView(mCheckedStreamItems.size() > 0);
    }

    @Override
    public void onNoNetwork() {
        super.onNoNetwork();
        mIsNoNetwork = true;
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
    public void refresh() {
        mIsNoNetwork = false;
        getAdapterToRecyclerViewCallback().setDisplayNoConnection(false);
        super.refresh();
    }

    public void populateActivityStreamAdapter() {
        if (mIsNoNetwork) { // workaround for the multiple callbacks, which mess up the generic solution
            getAdapterToRecyclerViewCallback().setDisplayNoConnection(true);
            getAdapterToRecyclerViewCallback().setIsEmpty(size() == 0);
        }

        // wait until all calls return;
        if (mCourseMap == null || mGroupMap == null || mStreamItems == null) {
            return;
        }

        for (final StreamItem streamItem : mStreamItems) {
            streamItem.setCanvasContextFromMap(mCourseMap, mGroupMap);

            // load conversations if needed
            if (streamItem.getType() == StreamItem.Type.CONVERSATION) {

                ConversationAPI.getDetailedConversation(new CanvasCallback<Conversation>(this) {
                    @Override
                    public void cache(Conversation conversation) {
                        streamItem.setConversation(getContext(), conversation, APIHelpers.getCacheUser(getContext()).getId(), getContext().getString(R.string.monologue));
                        notifyDataSetChanged();
                    }

                    @Override
                    public void firstPage(Conversation conversation, LinkHeaders linkHeaders, Response response) {
                        streamItem.setConversation(getContext(), conversation, APIHelpers.getCacheUser(getContext()).getId(), getContext().getString(R.string.monologue));
                        notifyDataSetChanged();
                    }

                    @Override
                    public boolean onFailure(RetrofitError error) {
                        //Show crouton if it's a network error
                        if (error.isNetworkError()) {
                            mAdapterToFragmentCallback.onShowErrorCrouton(R.string.noDataConnection);
                        }
                        //Otherwise show that it's been deleted.
                        else {
                            Conversation conversation = new Conversation(true, getContext().getString(R.string.deleted));
                            streamItem.setConversation(getContext(), conversation, APIHelpers.getCacheUser(getContext()).getId(), getContext().getString(R.string.monologue));
                            notifyDataSetChanged();
                        }
                        return true;
                    }
                }, streamItem.getConversationId(), false);
            }

            //make sure there's something there
            if(streamItem.getUpdatedAtDate() == null) {
                continue;
            }

            addOrUpdateItem(Utils.getCleanDate(streamItem.getUpdatedAtDate().getTime()), streamItem);
        }

        mStreamItems = null;


        // update count in dashboard
        if (mOnNotificationCountInvalidated != null) {
            mOnNotificationCountInvalidated.invalidateNotificationsCount();
        }
    }

    private void hideStreamItem(final StreamItem streamItem) {
        StreamAPI.hideStreamItem(streamItem.getId(), new CanvasCallback<HiddenStreamItem>(this) {
            @Override
            public void firstPage(HiddenStreamItem hiddenStreamItem, LinkHeaders linkHeaders, Response response) {
                if(hiddenStreamItem.isHidden()){
                    removeItem(streamItem);

                    if(mOnNotificationCountInvalidated != null){
                        mOnNotificationCountInvalidated.invalidateNotificationsCount();
                    }
                }
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                mDeletedStreamItems.remove(streamItem);
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
                return o2.compareTo(o1);
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
    public GroupSortedList.ItemComparatorCallback<Date, StreamItem> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<Date, StreamItem>() {
            @Override
            public int compare(Date group, StreamItem o1, StreamItem o2) {
                return o1.compareTo(o2);
            }

            @Override
            public boolean areContentsTheSame(StreamItem oldItem, StreamItem newItem) {
                return oldItem.getTitle(getContext()).equals(newItem.getTitle(getContext()));
            }

            @Override
            public boolean areItemsTheSame(StreamItem item1, StreamItem item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public long getUniqueItemId(StreamItem item) {
                return item.getId();
            }

            @Override
            public int getChildType(Date group, StreamItem item) {
                return Types.TYPE_ITEM;
            }
        };
    }

    // endregion
}

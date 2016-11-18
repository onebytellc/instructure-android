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

import com.instructure.candroid.binders.ExpandableHeaderBinder;
import com.instructure.candroid.binders.NotificationPreferenceBinder;
import com.instructure.candroid.holders.ExpandableViewHolder;
import com.instructure.candroid.holders.NotificationPreferencesViewHolder;
import com.instructure.candroid.interfaces.NotifyChecked;
import com.instructure.candroid.model.NotificationCategoryHeader;
import com.instructure.candroid.model.NotificationSubCategory;
import com.instructure.candroid.util.NotificationPreferenceUtils;
import com.instructure.canvasapi.api.NotificationPreferencesAPI;
import com.instructure.canvasapi.model.CommunicationChannel;
import com.instructure.canvasapi.model.NotificationPreference;
import com.instructure.canvasapi.model.NotificationPreferenceResponse;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;

import java.util.List;
import java.util.Map;

import retrofit.client.Response;

/**
 * Handles two different view types.
 * 1. A Header item
 * 2. A NotificationPreferenceWithChannel item
 */
public class NotificationPreferencesRecyclerAdapter extends ExpandableRecyclerAdapter<NotificationCategoryHeader, NotificationSubCategory, RecyclerView.ViewHolder> {

    private CommunicationChannel mCurrentChannel;

    public NotificationPreferencesRecyclerAdapter(Context context) {
        super(context, NotificationCategoryHeader.class, NotificationSubCategory.class);
        setExpandedByDefault(true);
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if(viewType == Types.TYPE_HEADER) {
            return new ExpandableViewHolder(v);
        } else {
            return new NotificationPreferencesViewHolder(v);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if(viewType == Types.TYPE_HEADER) {
            return ExpandableViewHolder.holderResId();
        } else {
            return NotificationPreferencesViewHolder.holderResId();
        }
    }

    @Override
    public void contextReady() {
        //This is the soonest the context will be ready for use
    }

    @Override
    public void onBindChildHolder(final RecyclerView.ViewHolder baseHolder, NotificationCategoryHeader notificationCategoryHeader, final NotificationSubCategory notificationSubCategory) {
        final NotificationPreferencesViewHolder holder = (NotificationPreferencesViewHolder) baseHolder;
        NotificationPreferenceBinder.bind(holder, notificationSubCategory, new NotifyChecked() {
            @Override
            public void notifyCheckChanged(final NotificationSubCategory notifSubCategory, final boolean isChecked) {
                NotificationPreferencesAPI.updateMultipleNotificationPreferences(mCurrentChannel.id, notifSubCategory.notifications, getFrequency(isChecked), new CanvasCallback<NotificationPreferenceResponse>(NotificationPreferencesRecyclerAdapter.this) {

                    @Override
                    public void cache(NotificationPreferenceResponse notificationPreferenceResponse, LinkHeaders linkHeaders, Response response) {
                        updateItemCheckedState(notifSubCategory, isChecked);
                    }

                    @Override
                    public void firstPage(NotificationPreferenceResponse notificationPreferenceResponse, LinkHeaders linkHeaders, Response response) {
                        cache(notificationPreferenceResponse, linkHeaders, response);
                    }
                });
            }
        });
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, NotificationCategoryHeader notificationCategoryHeader, boolean isExpanded) {
        ExpandableHeaderBinder.bind(getContext(), null, (ExpandableViewHolder) holder, notificationCategoryHeader, notificationCategoryHeader.title, isExpanded, getViewHolderHeaderClicked());
    }

    // region Expandable callbacks
    @Override
    public GroupSortedList.GroupComparatorCallback<NotificationCategoryHeader> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<NotificationCategoryHeader>() {
            @Override
            public int compare(NotificationCategoryHeader o1, NotificationCategoryHeader o2) {
                return o1.position - o2.position;
            }

            @Override
            public boolean areContentsTheSame(NotificationCategoryHeader oldGroup, NotificationCategoryHeader newGroup) {
                return false;
            }

            @Override
            public boolean areItemsTheSame(NotificationCategoryHeader group1, NotificationCategoryHeader group2) {
                return group1.getId() == group2.getId();
            }

            @Override
            public long getUniqueGroupId(NotificationCategoryHeader group) {
                return group.getId();
            }

            @Override
            public int getGroupType(NotificationCategoryHeader group) {
                return Types.TYPE_HEADER;
            }
        };
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<NotificationCategoryHeader, NotificationSubCategory> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<NotificationCategoryHeader, NotificationSubCategory>() {
            @Override
            public int compare(NotificationCategoryHeader group, NotificationSubCategory o1, NotificationSubCategory o2) {
                return o1.position - o2.position;
            }

            @Override
            public boolean areContentsTheSame(NotificationSubCategory oldItem, NotificationSubCategory newItem) {
                return false;
            }

            @Override
            public boolean areItemsTheSame(NotificationSubCategory item1, NotificationSubCategory item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public long getUniqueItemId(NotificationSubCategory item) {
                return item.getId();
            }

            @Override
            public int getChildType(NotificationCategoryHeader group, NotificationSubCategory item) {
                return Types.TYPE_ITEM;
            }
        };
    }

    // endregion


    protected void updateItemCheckedState(NotificationSubCategory notificationSubCategory, final boolean isChecked) {
        notificationSubCategory.frequency = getFrequency(isChecked);
    }

    protected String getFrequency(boolean isChecked) {
        return isChecked ? NotificationPreferencesAPI.IMMEDIATELY : NotificationPreferencesAPI.NEVER;
    }

    public void fetchNotificationPreferences(CommunicationChannel channel) {
        mCurrentChannel = channel;
        clear();

        NotificationPreferencesAPI.getNotificationPreferences(channel.user_id, channel.id,
                new CanvasCallback<NotificationPreferenceResponse>(this) {

                    @Override
                    public void cache(NotificationPreferenceResponse notificationPreferenceResponse, LinkHeaders linkHeaders, Response response) {
                        groupNotifications(notificationPreferenceResponse.notification_preferences);
                    }

                    @Override
                    public void firstPage(NotificationPreferenceResponse notificationPreferenceResponse, LinkHeaders linkHeaders, Response response) {
                        cache(notificationPreferenceResponse, linkHeaders, response);
                    }
                });
    }

    private void groupNotifications(List<NotificationPreference> items) {
        //Populate the names for our headers

        Map<String, NotificationPreferenceUtils.SubCategorySortingHelper> categoriesMap = NotificationPreferenceUtils.loadCategoryMap();
        Map<String, String> subCategoryTitles = NotificationPreferenceUtils.loadSubCategoryTitleMap(getContext());
        List<NotificationCategoryHeader> headers = NotificationPreferenceUtils.getCategoryHeaders(getContext());

        for(NotificationPreference preference : items) { //List from api

            final NotificationPreferenceUtils.SubCategorySortingHelper itemCategory = categoriesMap.get(preference.category);

            for(NotificationCategoryHeader header : headers) { //List of headers
                if(itemCategory != null && header.headerCategory == itemCategory.categories) {
                    if (header.subCategories.containsKey(preference.category)) {
                        //If the category exists add the notification to the subcategory
                        NotificationSubCategory subCategory = header.subCategories.get(preference.category);
                        subCategory.notifications.add(preference.notification);
                        subCategory.position = itemCategory.position;

                        header.subCategories.put(preference.category, subCategory);
                        break;
                    } else {
                        //The category does not exist add a new one
                        NotificationSubCategory subCategory = new NotificationSubCategory();

                        subCategory.frequency = preference.frequency;
                        subCategory.notifications.add(preference.notification);
                        subCategory.title = subCategoryTitles.get(preference.category);
                        subCategory.position = itemCategory.position;

                        header.subCategories.put(preference.category, subCategory);
                        break;
                    }
                }
            }
            for(NotificationCategoryHeader header : headers) {
                for (Map.Entry<String, NotificationSubCategory> entry : header.subCategories.entrySet()) {
                    addOrUpdateItem(header, entry.getValue());
                }
            }
        }
    }
}

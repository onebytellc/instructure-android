/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package instructure.androidblueprint;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.instructure.canvasapi2.models.CanvasComparable;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public abstract class SyncExpandableRecyclerAdapter<GROUP, MODEL extends CanvasComparable, HOLDER extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<HOLDER>{

    public abstract HOLDER createViewHolder(View v, int viewType);
    public abstract int itemLayoutResId(int viewType);
    public abstract void onBindHeaderHolder(RecyclerView.ViewHolder holder, GROUP group, boolean isExpanded);
    public abstract void onBindChildHolder(RecyclerView.ViewHolder holder, GROUP group, MODEL item);

    public void onBindEmptyHolder(RecyclerView.ViewHolder holder, GROUP group) {}

    private WeakReference<Context> mContext;
    private SyncExpandablePresenter mPresenter;

    public SyncExpandableRecyclerAdapter(final Context context, final SyncExpandablePresenter presenter) {
        mContext = new WeakReference<>(context);
        mPresenter = presenter;
        setExpandedByDefault(expandByDefault());
        mPresenter.setListChangeCallback(new ListChangeCallback() {
            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }
        });
        notifyDataSetChanged();
    }

    @Override
    public HOLDER onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResId(viewType), parent, false);
        return createViewHolder(v, viewType);
    }

    @Override
    public void onBindViewHolder(HOLDER baseHolder, int position) {
        GROUP group = getList().getGroup(position);
        GroupSortedList<GROUP, MODEL> list = getList();

        if (list.isVisualEmptyItemPosition(position)) {
            onBindEmptyHolder(baseHolder, group);
        } else if (list.isVisualGroupPosition(position)) {
            onBindHeaderHolder(baseHolder, group, list.isGroupExpanded(group));
        } else {
            onBindChildHolder(baseHolder, group, list.getItem(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (getList().isVisualEmptyItemPosition(position)) {
            return Types.TYPE_EMPTY_CELL;
        }
        return getList().getItemViewType(position);
    }

    public int size() {
        if(mPresenter == null) return 0;
        return mPresenter.getData().size();
    }

    @Override
    public int getItemCount() {
        return size();
    }

    // region GROUP, MODEL Helpers

    @SuppressWarnings("unchecked")
    public @NonNull GroupSortedList<GROUP, MODEL> getList() {
        return mPresenter.getData();
    }

    public void addOrUpdateAllItems(GROUP group, List<MODEL> items) {
        getList().addOrUpdateAllItems(group, items);
    }

    public void addOrUpdateAllItems(GROUP group, MODEL[] items) {
        getList().addOrUpdateAllItems(group, items);
    }

    public void addOrUpdateItem(GROUP group, MODEL item) {
        getList().addOrUpdateItem(group, item);
    }

    public boolean removeItem(MODEL item) {
        return getList().removeItem(item);
    }

    public MODEL getItem(GROUP group, int storedPosition) {
        return getList().getItem(group, storedPosition);
    }

    public MODEL getItem(int visualPosition){
        return getList().getItem(visualPosition);
    }

    public long getChildItemId(int position){
        return getList().getItemId(getList().getItem(position));
    }

    @Override
    public long getItemId(int position) {
        throw new UnsupportedOperationException("Method getItemId() is unimplemented in BaseExpandableRecyclerAdapter. Use getChildItemId instead.");
    }

    public ArrayList<MODEL> getItems(GROUP group) {
        return getList().getItems(group);
    }

    public int storedIndexOfItem(GROUP group, MODEL item) {
        return getList().storedIndexOfItem(group, item);
    }

    public void addOrUpdateAllGroups(GROUP[] groups) {
        getList().addOrUpdateAllGroups(groups);
    }

    public void addOrUpdateGroup(GROUP group) {
        getList().addOrUpdateGroup(group);
    }

    public GROUP getGroup(long groupId) {
        return getList().getGroup(groupId);
    }

    public GROUP getGroup(int position) {
        return getList().getGroup(position);
    }

    public ArrayList<GROUP> getGroups() {
        return getList().getGroups();
    }

    public int getGroupCount() { return getList().getGroupCount(); }

    public int getGroupItemCount(GROUP group) {
        return getList().getGroupItemCount(group);
    }

    public void expandCollapseGroup(GROUP group) {
        getList().expandCollapseGroup(group);
    }

    public void collapseAll() {
        getList().collapseAll();
    }

    public void expandAll() {
        getList().expandAll();
    }

    public void expandGroup(GROUP group) {
        getList().expandGroup(group);
    }

    public void expandGroup(GROUP group, boolean isNotifyGroupChange) {
        getList().expandGroup(group, isNotifyGroupChange);
    }

    public void collapseGroup(GROUP group) {
        getList().collapseGroup(group);
    }

    public void collapseGroup(GROUP group, boolean isNotifyGroupChange) {
        getList().collapseGroup(group, isNotifyGroupChange);
    }

    public boolean isGroupExpanded(GROUP group) {
        return getList().isGroupExpanded(group);
    }

    public int getGroupVisualPosition(int position) {
        return getList().getGroupVisualPosition(position);
    }

    public boolean isPositionGroupHeader(int position) {
        return getList().isVisualGroupPosition(position);
    }

    public void clear() {
        getList().clearAll();
        notifyDataSetChanged();
    }

    //endregion

    protected @Nullable
    Context getContext() {
        if(mContext != null) {
            return mContext.get();
        }
        return null;
    }

    public void setExpandedByDefault(boolean isExpandedByDefault) {
        getList().setExpandedByDefault(isExpandedByDefault);
    }

    public boolean expandByDefault() {
        return true;
    }
}

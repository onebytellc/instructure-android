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
import android.view.View;
import android.widget.Toast;

import com.instructure.candroid.R;
import com.instructure.candroid.binders.MessageBinder;
import com.instructure.candroid.fragment.MessageListFragment;
import com.instructure.candroid.holders.MessageViewHolder;
import com.instructure.candroid.util.DebounceMessageToAdapterListener;
import com.instructure.canvasapi.api.ConversationAPI;
import com.instructure.canvasapi.model.Conversation;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.LinkHeaders;

import java.util.List;

import retrofit.client.Response;

public class MessageListRecyclerAdapter extends MultiSelectRecyclerAdapter<Conversation, MessageViewHolder> {

    //model
    private ConversationAPI.ConversationScope mMessageType;
    private long mMyUserID;

    //callbacks and interfaces
    private CanvasCallback<Conversation[]> mInboxConversationCallback;
    private CanvasCallback<Conversation[]> mUnreadConversationCallback;
    private CanvasCallback<Conversation[]> mArchivedConversationCallback;
    private CanvasCallback<Conversation[]> mSentConversationCallback;

    private DebounceMessageToAdapterListener mAdapterToFragmentCallback;
    private ItemClickedInterface mItemClickedInterface;
    private MessageListFragment.OnUnreadCountInvalidated mUnReadCountInvalidated;

    public interface ItemClickedInterface{
        void itemClick(Conversation item, MessageViewHolder viewHolder);
        void itemLongClick(Conversation item, MessageViewHolder viewHolder);
    }

    public MessageListRecyclerAdapter(Context context, List items,
                                      ConversationAPI.ConversationScope messageType,
                                      MultiSelectCallback multiSelectCallback, long myUserID,
                                      DebounceMessageToAdapterListener adapterToFragmentCallback,
                                      MessageListFragment.OnUnreadCountInvalidated unreadCountInvalidated) {

        super(context, Conversation.class, items, multiSelectCallback);
        mMessageType = messageType;
        mMyUserID = myUserID;
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        mUnReadCountInvalidated = unreadCountInvalidated;
        setItemCallback(new ItemComparableCallback<Conversation>() {
            @Override
            public int compare(Conversation o1, Conversation o2) {
                return o2.getComparisonDate().compareTo(o1.getComparisonDate());
            }

            @Override
            public boolean areContentsTheSame(Conversation oldItem, Conversation newItem) {
                if(containsNull(oldItem.getLastMessagePreview(), newItem.getLastMessagePreview()) || !oldItem.getWorkflowState().equals(newItem.getWorkflowState())){
                    return false;
                }
                return oldItem.getLastMessagePreview().equals(newItem.getLastMessagePreview());
            }

            @Override
            public boolean areItemsTheSame(Conversation item1, Conversation item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public long getUniqueItemId(Conversation conversation) {
                return conversation.getId();
            }

        });
        loadData();
    }

    private boolean containsNull(Object oldItem, Object newItem) {
        return (oldItem == null || newItem == null);
    }

    @Override
    public void bindHolder(Conversation conversation, MessageViewHolder viewHolder, int position) {
        MessageBinder
                .bind(  viewHolder,
                        conversation,
                        mContext,
                        mMyUserID,
                        isItemSelected(conversation),
                        mItemClickedInterface);
    }

    @Override
    public MessageViewHolder createViewHolder(View v, int viewType) {
        return new MessageViewHolder(v);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return MessageViewHolder.holderResId();
    }

    @Override
    public void contextReady() {
        setupCallbacks();
    }

    @Override
    public void setupCallbacks() {
        mInboxConversationCallback = createCallback(ConversationAPI.ConversationScope.ALL);
        mUnreadConversationCallback = createCallback(ConversationAPI.ConversationScope.UNREAD);
        mArchivedConversationCallback = createCallback(ConversationAPI.ConversationScope.ARCHIVED);
        mSentConversationCallback = createCallback(ConversationAPI.ConversationScope.SENT);

        mItemClickedInterface = new ItemClickedInterface() {
            @Override
            public void itemClick(Conversation item, MessageViewHolder viewHolder) {
                int prevPosition = getSelectedPosition();
                if(isMultiSelectMode()) {
                    toggleSelection(item);
                    clearSelectedPosition();
                    notifyItemChanged(prevPosition);
                    notifyItemChanged(viewHolder.getAdapterPosition());
                } else {
                    mAdapterToFragmentCallback.onClick(item, viewHolder.getAdapterPosition(), viewHolder.userAvatar, true);
                }
            }

            @Override
            public void itemLongClick(Conversation item, MessageViewHolder viewHolder) {
                if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                    Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!isMultiSelectMode()) {
                    setMultiSelectMode(true);
                }
                toggleSelection(item);
                int prevPosition = getSelectedPosition();
                clearSelectedPosition();
                notifyItemChanged(prevPosition);
                notifyItemChanged(viewHolder.getAdapterPosition());
            }
        };
    }

    @Override
    public boolean isPaginated() {
        return true;
    }

    @Override
    public void loadFirstPage() {
        if(mMessageType.equals(ConversationAPI.ConversationScope.ALL)){
            ConversationAPI.getFirstPageConversations(mInboxConversationCallback, mMessageType);
        } else if (mMessageType.equals(ConversationAPI.ConversationScope.UNREAD)){
            ConversationAPI.getFirstPageConversations(mUnreadConversationCallback, mMessageType);
        } else if (mMessageType.equals(ConversationAPI.ConversationScope.ARCHIVED)){
            ConversationAPI.getFirstPageConversations(mArchivedConversationCallback, mMessageType);
        } else {
            ConversationAPI.getFirstPageConversations(mSentConversationCallback, mMessageType);
        }
    }

    @Override
    public void loadNextPage(String nextURL) {
        if(mMessageType.equals(ConversationAPI.ConversationScope.ALL)){
            ConversationAPI.getNextPageConversations(mInboxConversationCallback, nextURL);
        } else if (mMessageType.equals(ConversationAPI.ConversationScope.UNREAD)){
            ConversationAPI.getNextPageConversations(mUnreadConversationCallback, nextURL);
        } else if (mMessageType.equals(ConversationAPI.ConversationScope.ARCHIVED)){
            ConversationAPI.getNextPageConversations(mArchivedConversationCallback, nextURL);
        } else {
            ConversationAPI.getNextPageConversations(mSentConversationCallback, nextURL);
        }
    }

    public void clearSelectedPosition(){
        setSelectedPosition(-1);
    }

    public void setMessageType(ConversationAPI.ConversationScope messageType){
        mMessageType = messageType;
    }

    public void removeConversation(Conversation conversation){
        if(conversation != null){
            mSelectedItems.add(conversation);
        }
    }

    private CanvasCallback<Conversation[]> createCallback(final ConversationAPI.ConversationScope messageType){
        return new CanvasCallback<Conversation[]>(this) {

            @Override
            public void firstPage(Conversation[] conversations, LinkHeaders linkHeaders, Response response) {
                if(!mMessageType.equals(messageType)){
                    return;
                }
                addAll(conversations);
                setNextUrl(linkHeaders.nextURL);

                //update unread count
                if (mUnReadCountInvalidated != null && !APIHelpers.isCachedResponse(response)) {
                    mUnReadCountInvalidated.invalidateUnreadCount();
                }

                notifyDataSetChanged();
                //notify swipe to refresh layout
                mAdapterToFragmentCallback.onRefreshFinished();
            }

            @Override
            public void nextPage(Conversation[] conversations, LinkHeaders linkHeaders, Response response) {
                addAll(conversations);
                setNextUrl(linkHeaders.nextURL);
            }
        };
    }
}

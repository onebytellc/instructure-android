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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.instructure.candroid.R;
import com.instructure.candroid.adapter.MessageListRecyclerAdapter;
import com.instructure.candroid.adapter.MultiSelectRecyclerAdapter;
import com.instructure.candroid.decorations.DividerDecoration;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.util.DebounceMessageToAdapterListener;
import com.instructure.candroid.util.FragUtils;
import com.instructure.canvasapi.api.ConversationAPI;
import com.instructure.canvasapi.api.ConversationAPI.ConversationScope;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Conversation;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandarecycler.PandaRecyclerView;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.RequestCodes;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class MessageListFragment extends ParentFragment implements MultiSelectRecyclerAdapter.MultiSelectCallback {

    //Model
    private ConversationScope messageType;
    private ActionMode mode;

    //Listeners
    private OnUnreadCountInvalidated onUnreadCountInvalidated;

    private MessageListRecyclerAdapter mRecyclerAdapter;

    //Callbacks for adapter/list
    private DebounceMessageToAdapterListener mAdapterToFragmentCallback;

    public interface MessageAdapterToFragmentCallback <MODEL> {
        void onRowClicked(MODEL model, int position, View sharedElement, boolean isOpenDetail);
        void onRefreshFinished();
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.inbox);
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Handle activity destruction
        if(savedInstanceState != null){
            messageType = (ConversationScope)savedInstanceState.getSerializable(Const.SCOPE);
        }

        setUpCallbacks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = getLayoutInflater().inflate(R.layout.fragment_message_list, container, false);

        mRecyclerAdapter = new MessageListRecyclerAdapter(getContext(),
                new ArrayList<Conversation>(),
                messageType, this,
                APIHelpers.getCacheUser(getContext()).getId(),
                mAdapterToFragmentCallback,
                onUnreadCountInvalidated);
        mRecyclerAdapter.setSelectedItemId(getDefaultSelectedId());

        PandaRecyclerView recyclerView = configureRecyclerView(rootView, getContext(), mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.messageEmptyPandaView, R.id.listView);
        recyclerView.addItemDecoration(new DividerDecoration(getContext()));
        recyclerView.setSelectionEnabled(false);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        if(mode != null) {
            mode.finish();
        }
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putSerializable(Const.SCOPE, messageType);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onUnreadCountInvalidated = (OnUnreadCountInvalidated) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnUnreadCountInvalidated");
        }
    }

    // Assumes selected item is the one to be updated, does not update a message based on conversationId param
    public void setConversationState(Conversation conversation, Conversation.WorkflowState state){
        if (conversation != null) {
            conversation.setWorkflowState(state);
            mRecyclerAdapter.add(conversation);
            if(onUnreadCountInvalidated != null) {
                onUnreadCountInvalidated.invalidateUnreadCount();
            }
            if(messageType == ConversationScope.UNREAD && state == Conversation.WorkflowState.READ) {
                removeConversation(conversation);
                reloadData();
            }
        }
    }

    public void removeConversation(Conversation conversation) {
        if(mRecyclerAdapter != null) {
            mRecyclerAdapter.remove(conversation);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCodes.COMPOSE_MESSAGE) {
            ChooseMessageRecipientsFragment.allRecipients.clear();
        }

        //If they've selected a message and come back, check to see how many unread there are now; it may have changed.
        if(onUnreadCountInvalidated != null) {
            onUnreadCountInvalidated.invalidateUnreadCount();
        }
    }

    private void setUpCallbacks(){
        mAdapterToFragmentCallback = new DebounceMessageToAdapterListener() {
            @Override
            public void onRowClicked(Conversation conversation, int position, View sharedElement, boolean isOpenDetail) {
                boolean isUnread = false;
                if (conversation.getWorkflowState() == Conversation.WorkflowState.UNREAD) {
                    isUnread = true;
                }
                mRecyclerAdapter.setSelectedItemId(conversation.getId());
                showConversation(conversation, sharedElement, isUnread);
            }

            @Override
            public void onRefreshFinished() {
                setRefreshing(false);
            }
        };
    }

    private void showConversation(Conversation conversation, View avatar, boolean isUnread) {
        FragmentActivity activity = getActivity();
        if (activity instanceof Navigation) {

            boolean waitForTransition = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
            ParentFragment fragment = FragUtils.getFrag(DetailedConversationFragment.class,
                                                        DetailedConversationFragment.createBundle(conversation,
                                                        conversation.getMessageTitle(APIHelpers.getCacheUser(getContext()).getId(), getString(R.string.monologue)),
                                                        isUnread, waitForTransition));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // sets the sharedElement name on the fragment. The caller and called shared views must have the same sharedElementId
                ((DetailedConversationFragment)fragment).setSharedElementId(avatar.getTransitionName());

                ((Navigation) activity).addFragment(fragment,
                                                    R.transition.circular_image_transform,
                                                    avatar);
            }else{
                ((Navigation)activity).addFragment(fragment);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Interface
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void reloadData() {
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.refresh();
        }
    }

    public interface OnUnreadCountInvalidated {
        void invalidateUnreadCount();
    }

    private Callback mActionModeCallback = new Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items

            mode.setTitle("0 " + getResources().getString(R.string.selected));

            //change the color of the icons to white so they're visible
            Drawable icon = CanvasContextColor.getColoredDrawable(getActivity(), R.drawable.ic_cv_messages, Color.WHITE);
            menu.add(0, R.id.menu_inbox_mark_unread, 0, R.string.markAsUnread).setIcon(icon).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            if (messageType != ConversationScope.ARCHIVED && messageType != ConversationScope.SENT) {
                icon = CanvasContextColor.getColoredDrawable(getActivity(), R.drawable.ic_cv_archive, Color.WHITE);
                menu.add(0, R.id.menu_inbox_archive, 0, R.string.archive).setIcon(icon).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }

            if(messageType == ConversationScope.ARCHIVED && messageType == ConversationScope.SENT) {
                icon = CanvasContextColor.getColoredDrawable(getActivity(), R.drawable.ic_cv_unarchive, Color.WHITE);
                menu.add(0, R.id.menu_inbox_unarchive, 0, R.string.moveToInbox).setIcon(icon).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }

            icon = CanvasContextColor.getColoredDrawable(getActivity(), R.drawable.ic_delete_white_24dp, Color.WHITE);
            menu.add(0, R.id.menu_inbox_delete, 0, R.string.delete).setIcon(icon).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            return true;
        }

        // Called each time the action mode is shown. Always called after
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_inbox_mark_unread:
                    markMessagesAsUnread();
                    return true;
                case R.id.menu_inbox_archive:
                    archiveMessages();
                    return true;
                case R.id.menu_inbox_unarchive:
                    unarchiveMessages();
                    return true;
                case R.id.menu_inbox_delete:
                    deleteMessages();
                    return true;
            }

            mode.finish();
            return true;
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (mode != null){
                mode.finish();
                mRecyclerAdapter.setMultiSelectMode(false);
            }
        }
    };

    ///////////////////////////////////////////////////////////////////////////
    //  Helpers
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void startMultiSelectMode() {
        if(getActivity() instanceof AppCompatActivity) {
            mode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
        }
    }

    @Override
    public void endMultiSelectMode() {
        if(mode != null){
            mode.finish();
        }
    }

    @Override
    public void setMultiSelectCount(int selectedCount) {
        if(mode != null) {
            mode.setTitle(selectedCount + " " + getString(R.string.selected));
        }
    }

    void deleteMessages() {
        if (mRecyclerAdapter.isMultiSelectMode()) {
            new EditMessages(this, EDIT_TYPE.DELETE);
            mode.finish();
        }
    }

    void markMessagesAsUnread(){
        if(mRecyclerAdapter.isMultiSelectMode()){
            new EditMessages(this, EDIT_TYPE.MARK_AS_UNREAD);
            mode.finish();
        }
    }

    void archiveMessages() {
        if (mRecyclerAdapter.isMultiSelectMode()) {
            new EditMessages(this,EDIT_TYPE.ARCHIVE);
            mode.finish();
        }
    }

    void unarchiveMessages() {
        if (mRecyclerAdapter.isMultiSelectMode()) {
            new EditMessages(this, EDIT_TYPE.UNARCHIVE);
            mode.finish();
        }
    }

    public enum EDIT_TYPE {ARCHIVE, UNARCHIVE, DELETE, MARK_AS_UNREAD}

    public class EditMessages extends CanvasCallback<Response> {

        private boolean updateUnreadTab = false;
        private boolean updateArchivedTab = false;

        public EditMessages(APIStatusDelegate statusDelegate, EDIT_TYPE editType) {
            super(statusDelegate);

            final List<Conversation> conversationList = mRecyclerAdapter.getSelectedItems();
            for(Conversation conversation : conversationList){
                switch (editType){
                    case ARCHIVE:
                        ConversationAPI.archiveConversation(this, conversation.getId());
                        updateArchivedTab = true;
                        break;
                    case UNARCHIVE:
                        ConversationAPI.unArchiveConversation(this, conversation.getId());
                        break;
                    case DELETE:
                        ConversationAPI.deleteConversation(this, conversation.getId());
                        break;
                    case MARK_AS_UNREAD:
                        ConversationAPI.markConversationAsUnread(this, conversation.getId());
                        updateUnreadTab = true;
                        break;
                }
                if (messageType.equals(ConversationScope.ALL) && editType.equals(EDIT_TYPE.MARK_AS_UNREAD)){
                    //but not from ALL or INBOX scope, matching web canvas behavior
                    conversation.setWorkflowState(Conversation.WorkflowState.UNREAD);
                }else{
                    mRecyclerAdapter.remove(conversation);
                }
            }
        }

        @Override
        public void firstPage(Response response, LinkHeaders linkHeaders, Response response2) {
            if(!apiCheck()){ return; }
            if(onUnreadCountInvalidated != null){
                onUnreadCountInvalidated.invalidateUnreadCount();
            }
            if(updateUnreadTab && getParentFragment() instanceof InboxFragment) {
                ((InboxFragment)getParentFragment()).updateUnreadTab();
                updateUnreadTab = false;
            }
            if(updateArchivedTab && getParentFragment() instanceof InboxFragment) {
                ((InboxFragment)getParentFragment()).updateArchivedTab();
                updateArchivedTab = false;
            }

            mRecyclerAdapter.refresh();
        }

        @Override
        public boolean onFailure(RetrofitError retrofitError){
            //if the edit attempt fails, we need to refresh the data to restore accuracy
            mRecyclerAdapter.resetData();
            mRecyclerAdapter.refresh();
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Bundle
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        if (extras == null) {
            return;
        }

        if (extras.containsKey(Const.SCOPE)) {
            messageType = (ConversationScope)extras.getSerializable(Const.SCOPE);
        } else if (messageType == null) {
            messageType = ConversationScope.ALL;
        }
    }

    public static Bundle createBundle(CanvasContext canvasContext, boolean isDashboard) {
        return createBundle(canvasContext);
    }

    public static Bundle createBundle(CanvasContext canvasContext, boolean isDashboard, ConversationScope scope) {
        Bundle extras = createBundle(canvasContext, isDashboard);
        extras.putSerializable(Const.SCOPE,scope);
        return extras;
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}

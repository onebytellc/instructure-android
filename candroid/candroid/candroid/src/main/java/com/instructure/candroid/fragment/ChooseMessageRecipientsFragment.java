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
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.instructure.candroid.R;
import com.instructure.candroid.adapter.ChooseMessageRecipientRecyclerAdapter;
import com.instructure.candroid.decorations.DividerDecoration;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.interfaces.RecipientAdapterToFragmentCallback;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Recipient;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.pandarecycler.PandaRecyclerView;
import com.instructure.pandautils.utils.Const;

import java.util.HashSet;
import java.util.Stack;

public class ChooseMessageRecipientsFragment extends ParentFragment {
    private ChooseMessageRecipientRecyclerAdapter mRecyclerAdapter;
    private RecipientAdapterToFragmentCallback mAdapterToFragmentCallback;
    private PandaRecyclerView mRecyclerView;
    private Stack<StackEntry> mBackStack = new Stack<>(); // Used to navigate the user back in group selection
    private Menu mMenu;
    private boolean mIsDoneButtonPressed = false;

    private static class StackEntry {
        public Recipient recipientGroup;
        public StackEntry(Recipient recipient) {
            this.recipientGroup = recipient;
        }
    }
    //The static list of ALL currently selected recipients.
    public static HashSet<Recipient> allRecipients = new HashSet<Recipient>(); // probably shouldn't be static, but oh well ;-)
    public static CanvasContext canvasContext; // Also shouldn't be static, but oh well

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        if (isTablet(context)) {
            return FRAGMENT_PLACEMENT.DIALOG;
        }
        return FRAGMENT_PLACEMENT.DETAIL;
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.inbox);
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        return getString(R.string.selectPeopleInbox);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = getLayoutInflater().inflate(R.layout.fragment_list_with_toolbar, container, false);
        rootView.setBackgroundColor(Color.WHITE);
        setupDialogToolbar(rootView);
        mAdapterToFragmentCallback = new RecipientAdapterToFragmentCallback<Recipient>() {
            @Override
            public void onRowClicked(Recipient recipient, int position, boolean isOpenDetail, boolean isCheckbox) {
                if(recipient.getRecipientType() == Recipient.Type.person) {
                    if (isSelfSelected(recipient.getStringId())) {
                        return;
                    }
                    //select and deselect individuals.
                    addOrRemoveRecipient(recipient, position);
                } else if (recipient.getRecipientType() == Recipient.Type.metagroup) {
                    //always go to a metagroup
                    setNewAdapter(recipient, true);
                } else if(recipient.getRecipientType() == Recipient.Type.group) {
                    //If it's a group, make sure there are actually users in that group.
                    if(recipient.getUser_count() > 0) {
                        if (isCheckbox) {
                            addOrRemoveRecipient(recipient, position);
                        } else {
                            // filter down to the group
                            if (allRecipients.contains(recipient)) {
                                showToast(R.string.entireGroupSelected);
                            } else {
                                setNewAdapter(recipient, true);
                            }
                        }
                    } else {
                        showToast(R.string.noUsersInGroup);
                    }
                }
            }

            @Override
            public void onRefreshFinished() {
                setRefreshing(false);
            }

            @Override
            public boolean isRecipientSelected(Recipient recipient) {
                return allRecipients.contains(recipient);
            }

            @Override
            public boolean isRecipientCurrentUser(Recipient recipient) {
                return isSelfSelected(recipient.getStringId());
            }
        };
        if (getCanvasContext() == null && savedInstanceState != null) {
            setCanvasContext((CanvasContext)savedInstanceState.getParcelable(Const.CANVAS_CONTEXT));
        }
        canvasContext = getCanvasContext();
        mRecyclerAdapter = new ChooseMessageRecipientRecyclerAdapter(getContext(), getCanvasContext().getContextId(), null, mAdapterToFragmentCallback);

        configureRecyclerView(rootView, getContext(), mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView);
        mRecyclerView = (PandaRecyclerView) rootView.findViewById(R.id.listView);
        mRecyclerView.setSelectionEnabled(false);
        mRecyclerView.addItemDecoration(new DividerDecoration(getContext()));

        setNewAdapter(null, true); // keeps the backstack item

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Const.CANVAS_CONTEXT, getCanvasContext());
    }

    private void addOrRemoveRecipient(Recipient recipient, int position) {
        //select and deselect individuals.
        if(!allRecipients.add(recipient)) {
            allRecipients.remove(recipient);
        }
        if (position != -1) {
            mRecyclerAdapter.notifyItemChanged(position);
        }
    }

    private boolean isSelfSelected(String stringId) {
        try{
            if(Long.parseLong(stringId) == APIHelpers.getCacheUser(getContext()).getId()){
                return true;
            }
        } catch(NumberFormatException e) { }
        return false;
    }

    private void setNewAdapter(Recipient recipient, boolean isBackStacked) {
        mRecyclerAdapter = new ChooseMessageRecipientRecyclerAdapter(getContext(), getCanvasContext().getContextId(), recipient, mAdapterToFragmentCallback);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        if (isBackStacked) {
            mBackStack.add(new StackEntry(recipient));
        }
    }

    @Override
    public boolean handleBackPressed() {
        if (mBackStack.size() > 1) {
            mBackStack.pop(); // this is the recipient group that is being display, so just pop it.
            setNewAdapter(mBackStack.peek().recipientGroup, false);
            return true;
        } else {
            if (!mIsDoneButtonPressed) {
                allRecipients.clear();
            }
            return super.handleBackPressed();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helper Methods
    ///////////////////////////////////////////////////////////////////////////
    public static String getRecipientsTitle(String noRecipients, String users) {
        if (allRecipients.isEmpty()) {
            return noRecipients;
        } else if (allRecipients.size() == 1) {
            String title = "";
            for(Recipient r: allRecipients){
                title = r.getName();
            }
            return title;
        } else {
            int count = 0;
            String title = "";
            for(Recipient r: allRecipients) {
                if(r.getRecipientType() == Recipient.Type.person){
                    count++;
                } else{
                    count+= r.getUser_count();
                }
            }
            title += String.format("%d "+users, count);

            return title;
        }
    }

    //Determine if we're sending the message to more than one user...
    public static boolean isPossibleGroupMessage() {
        if(allRecipients.size() > 1){
            return true;
        }
        boolean group = false;
        for(Recipient r: allRecipients) {
            group =  group || (r.getRecipientType() == Recipient.Type.group && r.getUser_count() > 1);
        }
        return group;
    }

    ///////////////////////////////////////////////////////////////////////////
    // ActionBar Stuff
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_done_generic, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_done:
                mBackStack.clear();
                mIsDoneButtonPressed = true;
                Navigation navigation = getNavigation();
                if(navigation != null){
                    Fragment fragment = navigation.getPeekingFragment();
                    if(fragment instanceof ComposeNewMessageFragment) {
                        ((ComposeNewMessageFragment)fragment).populateRecipients();
                    }
                }
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

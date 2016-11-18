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
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


import com.instructure.candroid.R;
import com.instructure.candroid.activity.ParentActivity;
import com.instructure.candroid.adapter.NotificationListRecyclerAdapter;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.interfaces.NotificationAdapterToFragmentCallback;
import com.instructure.candroid.util.FragUtils;
import com.instructure.candroid.util.RouterUtils;
import com.instructure.canvasapi.model.BasicUser;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Conversation;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.model.StreamItem;
import com.instructure.canvasapi.model.Submission;
import com.instructure.canvasapi.model.Tab;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.pandarecycler.PandaRecyclerView;
import com.instructure.pandautils.utils.Const;

import java.util.ArrayList;
import java.util.List;

public class NotificationListFragment extends ParentFragment {

    //View
    private View mRootView;
    private View mEditOptions;

    private NotificationAdapterToFragmentCallback<StreamItem> mAdapterToFragmentCallback;
    private NotificationListRecyclerAdapter mRecyclerAdapter;

    private OnNotificationCountInvalidated onNotificationCountInvalidated;

    public interface OnNotificationCountInvalidated {
        void invalidateNotificationsCount();
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.notifications);
    }

    @Override
    public boolean navigationContextIsCourse() {
        if(getCanvasContext() instanceof Course || getCanvasContext() instanceof Group) {
            return true;
        }
        return false;
    }

    public String getTabId() {
        return Tab.NOTIFICATIONS_ID;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(this, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = getLayoutInflater().inflate(R.layout.fragment_list_notification, container, false);

        mAdapterToFragmentCallback = new NotificationAdapterToFragmentCallback<StreamItem>() {
            @Override
            public void onRowClicked(StreamItem streamItem, int position, boolean isOpenDetail) {
                mRecyclerAdapter.setSelectedPosition(position);
                Navigation navigation = getNavigation();
                if(navigation != null){
                    onRowClick(streamItem, isOpenDetail);
                }
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
                showToast(message);
            }
        };
        mRecyclerAdapter = new NotificationListRecyclerAdapter(getContext(), getCanvasContext(), onNotificationCountInvalidated, mAdapterToFragmentCallback);
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

    public String getTitle() {
        if (getCanvasContext() != null && getCanvasContext().getType() == CanvasContext.Type.COURSE) {
            return getString(R.string.Notifications);
        }
        return "";
    }

    /* fixme not sure Unread count is even used anymore, but the code is still here if it is ever put in again
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onNotificationCountInvalidated = (OnNotificationCountInvalidated) activity;
        } catch (ClassCastException e) {
            onNotificationCountInvalidated = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If they've selected a message and come back, check to see how many unread there are now; it may have changed.
        if(onNotificationCountInvalidated != null) {
            onNotificationCountInvalidated.invalidateNotificationsCount();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                new IntentFilter("updateUnreadCount"));

    }

    @Override
    public void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    // handler for received Intents for the "updateUnreadCount" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Don't need any data from the intent, we'll just set the selected item as unread
            // and update the list. We could also reloadData to pull the actual data from the
            // server which would have this item marked as unread. But that also clears the
            // adapter and if the user was scrolled down it would send them back up to the top
            // of the list

            StreamItem item = getSelectedItem();
            item.setReadState(true);
            notifyDataSetChanged();
        }
    };
    */

    // region Handle StreamItem

    public boolean onRowClick(StreamItem streamItem, boolean closeSlidingPane) {
        //TODO: pass should show first to addFragmentForStreamItem so we only open the sliding pane for the actual click events

        //if the course/group is null, this will crash. This happened in one case because the api returned an assignment for a course
        //that is concluded.
        if(streamItem.getCanvasContext() == null && streamItem.getContextType() != CanvasContext.Type.USER) {
            if(streamItem.getContextType() == CanvasContext.Type.COURSE) {
                showToast(R.string.could_not_find_course);
            } else if(streamItem.getContextType() == CanvasContext.Type.GROUP) {
                showToast(R.string.could_not_find_group);
            }
            return false;
        }
        addFragmentForStreamItem(streamItem, (ParentActivity)getActivity(), false);

        return true;
    }

    public static DialogFragment addFragmentForStreamItem(StreamItem streamItem, FragmentActivity activity, boolean fromWidget){
        ParentFragment fragment = null;

        if(streamItem == null || activity == null){
            return null;
        }

        String unsupportedLabel = null;

        switch (streamItem.getType()) {
            case SUBMISSION:
                if (streamItem.getAssignment() == null) {
                    fragment = FragUtils.getFrag(AssignmentFragment.class, AssignmentFragment.createBundle((Course)streamItem.getCanvasContext(), streamItem.getAssignmentId()));
                } else {
                    //add an empty submission with the grade to the assignment so that we can see the score.
                    Submission emptySubmission = new Submission();
                    emptySubmission.setGrade(streamItem.getGrade());
                    streamItem.getAssignment().setLastSubmission(emptySubmission);
                    fragment = FragUtils.getFrag(AssignmentFragment.class, AssignmentFragment.createBundle((Course)streamItem.getCanvasContext(), streamItem.getAssignment()));
                }
                break;
            case ANNOUNCEMENT:
                fragment = FragUtils.getFrag(DetailedDiscussionFragment.class, DetailedDiscussionFragment.createBundle(streamItem.getCanvasContext(), streamItem.getDiscussionTopicId(), true));
                break;
            case CONVERSATION:
                Conversation conversation = streamItem.getConversation();

                //Check to see if the conversation has been deleted.
                if(conversation != null && conversation.isDeleted()){
                    Toast.makeText(activity, R.string.deleteConversation, Toast.LENGTH_SHORT).show();
                   return null;
                }

                //Check to see if it's unread.
                boolean hasUnread = false;
                boolean isStarred = false;
                if (conversation != null && conversation.getWorkflowState() == Conversation.WorkflowState.UNREAD) {
                    hasUnread = true;
                    isStarred = conversation.isStarred();
                }
                long userId = APIHelpers.getCacheUser(activity.getApplicationContext()).getId();
                String messageTitle = "";
                if (conversation != null) {
                    messageTitle = conversation.getMessageTitle(userId, activity.getString(R.string.monologue));
                }

                Bundle extras = DetailedConversationFragment.createBundle(streamItem.getCanvasContext(), conversation, messageTitle, hasUnread);

                List<BasicUser> conversations = new ArrayList<>();
                if(conversation != null && conversation.getAllParticipants() != null) {
                    conversations = conversation.getAllParticipants();
                }
                extras.putParcelableArrayList(Const.FROM_PEOPLE, new ArrayList<Parcelable>(conversations));

                fragment = FragUtils.getFrag(DetailedConversationFragment.class, extras);
                break;
            case DISCUSSION_TOPIC:
                fragment = FragUtils.getFrag(DetailedDiscussionFragment.class, DetailedDiscussionFragment.createBundle(streamItem.getCanvasContext(), streamItem.getDiscussionTopicId(), false));
                break;
            case MESSAGE:
                if(streamItem.getAssignmentId() > 0) {
                    fragment = FragUtils.getFrag(AssignmentFragment.class, AssignmentFragment.createBundle(activity, (Course)streamItem.getCanvasContext(), streamItem.getAssignmentId(), streamItem));
                } else{
                    fragment = FragUtils.getFrag(UnknownItemFragment.class, UnknownItemFragment.createBundle(streamItem.getCanvasContext(), streamItem));
                }
                break;
            case COLLABORATION:
                unsupportedLabel = activity.getString(R.string.collaborations);
                fragment = FragUtils.getFrag(UnSupportedTabFragment.class, UnSupportedTabFragment.createBundle(streamItem.getCanvasContext(), FRAGMENT_PLACEMENT.DETAIL, Tab.COLLABORATIONS_ID));
                break;
            case CONFERENCE:
                unsupportedLabel = activity.getString(R.string.conferences);
                fragment = FragUtils.getFrag(UnSupportedTabFragment.class, UnSupportedTabFragment.createBundle(streamItem.getCanvasContext(), FRAGMENT_PLACEMENT.DETAIL, Tab.CONFERENCES_ID));
                break;
            default:
                unsupportedLabel = streamItem.getType().toString();

                fragment = FragUtils.getFrag(UnSupportedTabFragment.class, UnSupportedTabFragment.createBundle(streamItem.getCanvasContext(), FRAGMENT_PLACEMENT.DETAIL, unsupportedLabel));
                break;
        }

        if (unsupportedLabel != null) {
            if(activity instanceof Navigation) {
                ((Navigation)activity).addFragment(fragment);
            }
        } else {

            if(activity instanceof Navigation) {
                ((Navigation)activity).addFragment(fragment);
            }

            if(fromWidget){
                if(streamItem.getUrl() != null){
                    RouterUtils.routeUrl(activity, streamItem.getUrl(), false);
                }else{
                    RouterUtils.routeUrl(activity, streamItem.getHtmlUrl(), false);
                }
            }
        }

        return null;
    }

    // endregion

    // region Edit view

    public void configureViews(View rootView) {
        mEditOptions = rootView.findViewById(R.id.editOptions);

        Button confirmButton = (Button) rootView.findViewById(R.id.confirmButton);
        confirmButton.setText(getString(R.string.delete));
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerAdapter.confirmButtonClicked();
            }
        });

        Button cancelButton = (Button) rootView.findViewById(R.id.cancelButton);
        cancelButton.setText(getString(R.string.cancel));
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
        if (extras == null) {return;}

        if(extras.containsKey(Const.SELECTED_ITEM)){
            StreamItem streamItem = (StreamItem)extras.getSerializable(Const.SELECTED_ITEM);
            setDefaultSelectedId(streamItem.getId());
        }
    }

    @Override
    public boolean allowBookmarking() {
        if(getCanvasContext() instanceof Course || getCanvasContext() instanceof Group) {
            return true;
        }
        return false;
    }
}

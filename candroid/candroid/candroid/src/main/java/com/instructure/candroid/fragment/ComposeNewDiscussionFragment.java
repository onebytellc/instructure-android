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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


import com.instructure.candroid.R;
import com.instructure.candroid.util.Analytics;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.pandautils.utils.Const;
import com.instructure.canvasapi.api.DiscussionAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.DiscussionTopicHeader;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class ComposeNewDiscussionFragment extends ParentFragment {

	private boolean isAnnouncement, isEditing;

	private EditText title;
	private EditText message;
	private CheckBox threaded;
    private CheckBox publish;

    private DiscussionTopicHeader discussionTopicHeader;    //will be null if we're creating a new discussionTopic

    CanvasCallback<DiscussionTopicHeader> discussionTopicHeaderCanvasCallback;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        if (isTablet(context)) {
            return FRAGMENT_PLACEMENT.DIALOG;
        }
        return FRAGMENT_PLACEMENT.DETAIL;
    }

    @Override
    public String getFragmentTitle() {
        if (isAnnouncement) {
            return getString(R.string.composeAnnouncement);
        } else {
            return getString(R.string.composeDiscussion);
        }
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        return getFragmentTitle();
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = getLayoutInflater().inflate(R.layout.compose_discussion, container, false);
        setupDialogToolbar(rootView);
        message = (EditText) rootView.findViewById(R.id.message);
        threaded = (CheckBox) rootView.findViewById(R.id.threadedCheckbox);
        publish = (CheckBox) rootView.findViewById(R.id.publishCheckbox);
        title = (EditText) rootView.findViewById(R.id.title);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isAnnouncement) {
            threaded.setVisibility(View.GONE);
            publish.setVisibility(View.GONE);
        } else {
            threaded.setVisibility(View.VISIBLE);
            publish.setVisibility(View.VISIBLE);
        }

        if (getCanvasContext() instanceof Course) {
            Course course = (Course) getCanvasContext();
            if (((course.isStudent() && !course.isTeacher()))){
                //Students cannot post draft discussions.
                //We force it checked for when the api call is made and hide the option.
                publish.setChecked(true);
                publish.setVisibility(View.GONE);
            }
        }

        setUpCallback();

        if(discussionTopicHeader != null) {
            populateViewsWithData();
        } else {
            //clear out views
            initViews();
        }
    }

    @Override
    public void onPause() {
        // we only want to save the title and message if the user is creating a new discussion
        if(discussionTopicHeader == null) {
            dataLossPause(title, isAnnouncement ? Const.DATA_LOSS_ANNOUNCEMENT_TITLE : Const.DATA_LOSS_DISCUSSION_TITLE);
            dataLossPause(message, isAnnouncement ? Const.DATA_LOSS_ANNOUNCEMENT_MESSAGE : Const.DATA_LOSS_DISCUSSION_MESSAGE);
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(discussionTopicHeader == null) {
            dataLossResume(title, isAnnouncement ? Const.DATA_LOSS_ANNOUNCEMENT_TITLE : Const.DATA_LOSS_DISCUSSION_TITLE);
            dataLossResume(message, isAnnouncement ? Const.DATA_LOSS_ANNOUNCEMENT_MESSAGE : Const.DATA_LOSS_DISCUSSION_MESSAGE);
            dataLossAddTextWatcher(title, isAnnouncement ? Const.DATA_LOSS_ANNOUNCEMENT_TITLE: Const.DATA_LOSS_DISCUSSION_TITLE);
            dataLossAddTextWatcher(message, isAnnouncement ? Const.DATA_LOSS_ANNOUNCEMENT_MESSAGE : Const.DATA_LOSS_DISCUSSION_MESSAGE);
        }
    }

    private void populateViewsWithData() {
        title.setText(discussionTopicHeader.getTitle());
        message.setText(Html.fromHtml(discussionTopicHeader.getMessage()));
        if(discussionTopicHeader.getType() == DiscussionTopicHeader.DiscussionType.THREADED) {
            threaded.setChecked(true);
        }
    }

    private void initViews() {
        if(title.getText() != null) {
            title.getText().clear();
        }
        if(message.getText() != null) {
            message.getText().clear();
        }
        threaded.setChecked(false);
    }

    public void setUpCallback(){
        discussionTopicHeaderCanvasCallback = new CanvasCallback<DiscussionTopicHeader>(this) {
            @Override
            public void firstPage(final DiscussionTopicHeader discussionTopicHeaderResult, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()){
                    return;
                }

                if (discussionTopicHeaderResult.unauthorized) {
                    String message;
                    if (isAnnouncement) {
                        message = getResources().getString(R.string.notAuthorizedAnnouncement);
                    } else {
                        message = getResources().getString(R.string.notAuthorizedDiscussion);
                    }

                    showToast(message);
                } else {

                    //Let the discussion list know to update itself
                    String message;
                    if (isAnnouncement) {
                        message = getResources().getString(R.string.postAnnouncementSuccess);
                    } else {
                        if(discussionTopicHeader == null) {
                            //this is a new discussion, check if it's published
                            if(discussionTopicHeaderResult.isPublished()) {
                                message = getResources().getString(R.string.postDiscussionSuccess);
                            } else {
                                message = getResources().getString(R.string.draftDiscussionSuccess);
                            }
                        }
                        else {
                            //we're updating an existing discussion
                            message = getResources().getString(R.string.updateDiscussionSuccess);
                        }
                    }

                    showToast(message);
                }

                if (ComposeNewDiscussionFragment.this.title != null && ComposeNewDiscussionFragment.this.message != null) {
                    ComposeNewDiscussionFragment.this.title.setText("");
                    ComposeNewDiscussionFragment.this.message.setText("");
                }

                dataLossDeleteStoredData(isAnnouncement ? Const.DATA_LOSS_ANNOUNCEMENT_TITLE : Const.DATA_LOSS_DISCUSSION_TITLE);
                dataLossDeleteStoredData(isAnnouncement ? Const.DATA_LOSS_ANNOUNCEMENT_MESSAGE : Const.DATA_LOSS_DISCUSSION_MESSAGE);

                closeKeyboard();
                Intent refreshIntent = new Intent(Const.REFRESH);
                LocalBroadcastManager.getInstance(getContext()).sendBroadcast(refreshIntent);
                getActivity().onBackPressed();
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                String message;
                if(isAnnouncement){
                    message = getResources().getString(R.string.errorPostingAnnouncement);
                } else {
                    message = getResources().getString(R.string.errorPostingDiscussion);
                }

                showToast(message);
                return true;
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // ActionBar
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);

        if(isAnnouncement) {
            inflater.inflate(R.menu.menu_post_announcement, menu);
        } else {
            inflater.inflate(R.menu.menu_post_discussion, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_post_announcement || item.getItemId() == R.id.menu_post_discussion) {
            if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                return true;
            }
            final String cleanedTitle = title.getText().toString().trim();
            final String cleanedMessage = message.getText().toString().trim();

            if(TextUtils.isEmpty(cleanedTitle)) {
                showToast(R.string.titleBlank);
            } else if (isAnnouncement && TextUtils.isEmpty(cleanedMessage)) {
                showToast(R.string.messageBlank);
            } else {
                if(isEditing && !isAnnouncement) {
                    //Is a discussion and we are editing it
                    DiscussionAPI.updateDiscussionTopic(getCanvasContext(), discussionTopicHeader.getId(), cleanedTitle, cleanedMessage, threaded.isChecked(), publish.isChecked(), discussionTopicHeaderCanvasCallback);
                } else {
                    if (discussionTopicHeader == null) {
                        //announcements auto publish
                        if (isAnnouncement) {
                            DiscussionAPI.postNewDiscussion(getCanvasContext(), cleanedTitle, cleanedMessage, threaded.isChecked(), isAnnouncement, discussionTopicHeaderCanvasCallback);

                        } else {
                            //we haven't created this topic yet, so do it now
                            DiscussionAPI.postNewDiscussionAndPublish(getCanvasContext(), cleanedTitle, cleanedMessage, threaded.isChecked(), isAnnouncement, publish.isChecked(), discussionTopicHeaderCanvasCallback);
                        }
                    } else {
                        //we're editing this unpublished discussion/announcement, so just update it
                        DiscussionAPI.updateDiscussionTopic(getCanvasContext(), discussionTopicHeader.getId(), cleanedTitle, cleanedMessage, threaded.isChecked(), publish.isChecked(), discussionTopicHeaderCanvasCallback);
                    }
                }
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        isAnnouncement = extras.getBoolean(Const.ANNOUNCEMENT);
        discussionTopicHeader = extras.getParcelable(Const.DISCUSSION_HEADER);
        isEditing = extras.getBoolean(Const.IN_EDIT_MODE, false);
    }

    public static Bundle createBundle(CanvasContext canvasContext, boolean isAnnouncement) {
        Bundle extras = createBundle(canvasContext);
        extras.putBoolean(Const.ANNOUNCEMENT, isAnnouncement);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, boolean isAnnouncement, DiscussionTopicHeader topic) {
        Bundle extras = createBundle(canvasContext);
        extras.putBoolean(Const.ANNOUNCEMENT, isAnnouncement);
        extras.putParcelable(Const.DISCUSSION_HEADER, topic);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, boolean isAnnouncement, DiscussionTopicHeader topic, boolean isEditing) {
        Bundle extras = createBundle(canvasContext);
        extras.putBoolean(Const.ANNOUNCEMENT, isAnnouncement);
        extras.putParcelable(Const.DISCUSSION_HEADER, topic);
        extras.putBoolean(Const.IN_EDIT_MODE, isEditing);
        return extras;
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}

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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.instructure.candroid.R;
import com.instructure.candroid.adapter.ConversationParticipantsAdapter;
import com.instructure.candroid.adapter.DetailedConversationRecyclerAdapter;
import com.instructure.candroid.decorations.ConversationDecorator;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.dialog.FileUploadDialog;
import com.instructure.candroid.interfaces.DetailedConversationAdapterToFragmentCallback;
import com.instructure.candroid.interfaces.TransitionListenerProvider;
import com.instructure.candroid.model.MessageAttachment;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.DownloadMedia;
import com.instructure.candroid.util.Param;
import com.instructure.candroid.view.ActionbarSpinner;
import com.instructure.candroid.view.CanvasEditTextView;
import com.instructure.candroid.view.EmptyPandaView;
import com.instructure.candroid.view.ViewUtils;
import com.instructure.canvasapi.api.ConversationAPI;
import com.instructure.canvasapi.model.BasicUser;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Conversation;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandarecycler.PandaRecyclerView;
import com.instructure.pandautils.models.FileSubmitObject;
import com.instructure.pandautils.services.FileUploadService;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.PermissionUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class DetailedConversationFragment extends ParentFragment
        implements CanvasEditTextView.CanvasEditTextViewRightListener, TransitionListenerProvider, CanvasEditTextView.CanvasEditTextViewLeftListener,
        FileUploadDialog.FileSelectionInterface{

    public interface UpdateMessageStateListener {
        void updateMessageState(Conversation conversation, Conversation.WorkflowState state);
        void removeMessage(Conversation conversation);
    }

    // Attachment Uploads
    private ArrayList<FileSubmitObject> mAttachmentsList = new ArrayList<>();
    private FileUploadDialog mUploadFileSourceFragment;
    private BroadcastReceiver errorBroadcastReceiver;
    private BroadcastReceiver allUploadsCompleteBroadcastReceiver;
    private boolean needsUnregister;

    // Views
    private View mRootView;
    private CanvasEditTextView mCanvasEditTextView;
    private PandaRecyclerView mRecyclerView;
    private ActionbarSpinner mSpinner;

    // Listview & Pagination Stuff
    private DetailedConversationRecyclerAdapter mRecyclerAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // interfaces
    private DetailedConversationAdapterToFragmentCallback mAdapterToFragmentCallback;
    private CanvasCallback<Response> mDeleteConversationCallback;
    private CanvasCallback<Response> mArchiveConversationCallback;
    private UpdateMessageStateListener mUpdateUnreadCallback;

    // data
    private String mSharedElementId;
    private long conversationID;
    private boolean isUnread;
    private boolean isStarred;
    private List<BasicUser> mParticipants;

    private MessageAttachment mCurrentAttachment;
    private String avatarURL;
    private String mUserName = "";
    private long myUserId;
    private Conversation conversation;

    private View mSpinnerContainer;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Required Overrides
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.DETAIL; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.inbox);
    }

    @Override
    public boolean allowBookmarking() {
        Navigation navigation = getNavigation();
        //navigation is a course, but isn't in notification list.
        return navigationContextIsCourse() && navigation != null && !(navigation.getCurrentFragment() instanceof NotificationListFragment);
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        return mUserName;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Lifecycle Overrides
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        EmptyPandaView emptyPandaView;

        mRootView = getLayoutInflater().inflate(R.layout.detailed_conversation_list, container, false);
        emptyPandaView = (EmptyPandaView) mRootView.findViewById(R.id.emptyPandaView);
        mRecyclerView = (PandaRecyclerView) mRootView.findViewById(R.id.listView);

        configureMessageView();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setEmptyView(emptyPandaView);
        mRecyclerView.addItemDecoration(new ConversationDecorator());
        mRecyclerView.setSelectionEnabled(false);

        configureRecyclerView();

        return mRootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupCallbacks();
        myUserId = APIHelpers.getCacheUser(getContext()).getId();
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceivers();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceivers();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(Const.CONVERSATION_ID, conversationID);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof UpdateMessageStateListener){
            mUpdateUnreadCallback = (UpdateMessageStateListener) activity;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        mCurrentAttachment = (MessageAttachment) v.getTag();
        menu.add(getResources().getString(R.string.open));

        if (mCurrentAttachment != null){
            menu.add(getResources().getString(R.string.download));
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        if (item.getTitle().equals(getResources().getString(R.string.open))) {
            //Open media
            openMedia(mCurrentAttachment.getMimeType(), mCurrentAttachment.getUrl(), mCurrentAttachment.getFileName());
        } else if (item.getTitle().equals(getResources().getString(R.string.download))) {
            if(PermissionUtils.hasPermissions(getActivity(), PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                //Download media
                DownloadMedia.downloadMedia(getActivity(), mCurrentAttachment.getUrl(),  mCurrentAttachment.getFileName(),  mCurrentAttachment.getFileName());
            } else {
                requestPermissions(PermissionUtils.makeArray(PermissionUtils.WRITE_EXTERNAL_STORAGE), PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE);
            }
        }
        return true;
    }

    @Override
    public void onFragmentActionbarSetupComplete(FRAGMENT_PLACEMENT placement) {
        setupSpinner();
    }

    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_detailed_conversations, menu);

        // manually add this menu item since it's dyanamic
        int resId = isStarred ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp;
        menu.add(0, R.id.menu_star_conversation, 0, getString(R.string.starConversation))
                .setIcon(resId)
                .setTitle(getString(R.string.starConversation))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    private void setupSpinner() {

        if(mParticipants == null) {
            mParticipants = new ArrayList<>();
        }

        if(mSpinnerContainer == null) {
            mSpinnerContainer = LayoutInflater.from(getContext()).inflate(R.layout.actionbar_course_navigation_spinner, null);
            mSpinner = (ActionbarSpinner) mSpinnerContainer.findViewById(R.id.actionbar_spinner);
        }
        // Sometimes the participants come later from an API call, so create a new adapter for the spinner.
        final ConversationParticipantsAdapter adapter = new ConversationParticipantsAdapter(getContext(), mParticipants, mUserName);
        mSpinner.setAdapter(adapter);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(mSpinnerContainer);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_people:
                if(mSpinner != null){
                    mSpinner.performClick();
                }
                break;
            case R.id.menu_star_conversation:
                if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                    Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                    return true;
                }
                ConversationAPI.starConversation(conversationID, !isStarred, new CanvasCallback<Conversation>(APIHelpers.statusDelegateWithContext(getContext())) {
                    @Override
                    public void firstPage(Conversation conversation, LinkHeaders linkHeaders, Response response) {
                        isStarred = conversation.isStarred();
                        getActivity().invalidateOptionsMenu();
                    }
                });
                break;
            case R.id.menu_delete:
                if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                    Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                    return true;
                }
                ConversationAPI.deleteConversation(mDeleteConversationCallback, conversationID);
                break;
            case R.id.menu_archive:
                if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                    Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                    return true;
                }
                ConversationAPI.archiveConversation(mArchiveConversationCallback, conversationID);
                break;
        }
        return true;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void setupCallbacks(){
        mAdapterToFragmentCallback = new DetailedConversationAdapterToFragmentCallback() {
            @Override
            public void onRowClicked(Conversation conversation, int position, boolean isOpenDetail) {}

            @Override
            public void onRefreshFinished() {
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void openMediaFromRow(String mime, String url, String filetype) {
                openMedia(mime, url, filetype);
            }

            @Override
            public void register(View view) {
                registerForContextMenu(view);
            }

            @Override
            public void conversationWasFetched(Conversation conversation) {
                if (!apiCheck()) {
                    return;
                }
                isStarred = conversation.isStarred();
                avatarURL = conversation.getAvatarURL();
                mParticipants = conversation.getAllParticipants();
                mUserName = conversation.getMessageTitle(myUserId, getString(R.string.monologue));
                onFragmentActionbarSetupComplete(getFragmentPlacement(getContext()));
            }
        };

        mDeleteConversationCallback = new CanvasCallback<Response>(this) {

            @Override
            public void firstPage(Response response, LinkHeaders linkHeaders, Response response2) {

                // Remove the message from the list
                if (mUpdateUnreadCallback != null) {
                    mUpdateUnreadCallback.removeMessage(conversation);
                }

                showToast(R.string.deletedConversation);
                getActivity().onBackPressed();
            }
        };

        mArchiveConversationCallback = new CanvasCallback<Response>(this) {

            @Override
            public void firstPage(Response response, LinkHeaders linkHeaders, Response response2) {

                // Remove the message from the list
                if (mUpdateUnreadCallback != null) {
                    mUpdateUnreadCallback.removeMessage(conversation);
                }

                showToast(R.string.archived);
                getActivity().onBackPressed();
            }
        };
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Views
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void configureMessageView() {
        mCanvasEditTextView = (CanvasEditTextView) mRootView.findViewById(R.id.canvasEditTextView);
        mCanvasEditTextView.setEditTextLeftListener(this);
        mCanvasEditTextView.setEditTextRightListener(this);
        mCanvasEditTextView.setLeftButtonImage(R.drawable.ic_cv_attachment);
        mCanvasEditTextView.setRightButtonImage(R.drawable.ic_action_send);
        mCanvasEditTextView.setHideRightBeforeText();
    }

    private void configureRecyclerView(){
        // Create our adapter in a separate thread to make our transitions smoother.
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(getActivity() != null){
                    final DetailedConversationRecyclerAdapter recyclerAdapter = new DetailedConversationRecyclerAdapter(getContext(), conversationID,
                            mUpdateUnreadCallback,
                            mAdapterToFragmentCallback, isUnread);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerAdapter = recyclerAdapter;
                            mRecyclerView.setAdapter(mRecyclerAdapter);
                            mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipeRefreshLayout);
                            mSwipeRefreshLayout.setProgressViewOffset(false, 0, (int)ViewUtils.convertDipsToPixels(160, getContext()));
                            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                @Override
                                public void onRefresh() {
                                    mRecyclerAdapter.refresh();
                                }
                            });
                        }
                    });
                }
            }
        }).start();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region send message
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onRightButtonClicked() {
        sendMessage();
    }

    private void sendMessage() {
        String message = mCanvasEditTextView.getText().trim();
        if (TextUtils.isEmpty(message)){
            showToast(R.string.emptyMessage);
            return;
        }
        //disable the send button
        mCanvasEditTextView.disableRightButton();
        mCanvasEditTextView.setRightProgressBarLoading(true);

        // If we have attachments, start our FileUploadService to submit our message, otherwise, send it normally
        if(mAttachmentsList.size() == 0){
            CanvasCallback<Conversation> conversationCallback = new CanvasCallback<Conversation>(this) {
                @Override
                public void firstPage(Conversation conversation, LinkHeaders linkHeaders, Response response) {
                    if(!apiCheck()){
                        return;
                    }
                    //re-enable the send button
                    mCanvasEditTextView.enableRightButton();

                    handleNewConversation(conversation);
                }

                @Override
                public boolean onFailure(RetrofitError retrofitError) {
                    //re-enable the send button
                    mCanvasEditTextView.enableRightButton();
                    mCanvasEditTextView.setRightProgressBarLoading(false);
                    showToast(R.string.errorSendingMessage);
                    return true;
                }
            };

            ConversationAPI.addMessageToConversation(conversationCallback, conversationID, message);

        }else{
            Intent intent = new Intent(getActivity(), FileUploadService.class);
            Bundle bundle = FileUploadService.getMessageBundle(mAttachmentsList, message, conversationID);
            intent.setAction(FileUploadService.ACTION_MESSAGE_ATTACHMENTS);
            intent.putExtras(bundle);
            getActivity().startService(intent);
            mAttachmentsList.clear();
        }
    }

    private void handleNewConversation(Conversation conversation){
        mCanvasEditTextView.setRightProgressBarLoading(false);
        if (conversation != null && conversation.getMessageCount() > 0) {

            // Notify parent message was read.
            if (mUpdateUnreadCallback != null) {
                mUpdateUnreadCallback.updateMessageState(conversation, Conversation.WorkflowState.READ);
            }

            showToast(R.string.successSendingMessage);
            mCanvasEditTextView.setText("", false);
            //add the new message....
            mRecyclerAdapter.addMessage(conversation.getMessages().get(0), 0);
        } else {
            showToast(R.string.errorSendingMessage);
        }
        scrollToBottom();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Attachments
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void onLeftButtonClicked() {
        Bundle bundle = FileUploadDialog.createAttachmentsBundle(mUserName, mAttachmentsList);
        mUploadFileSourceFragment = FileUploadDialog.newInstance(getChildFragmentManager(),bundle);
        mUploadFileSourceFragment.setTargetFragment(this, 1337);
        mUploadFileSourceFragment.show(getChildFragmentManager(), FileUploadDialog.TAG);
    }

    @Override
    public void onFilesSelected(ArrayList<FileSubmitObject> fileSubmitObjects) {
        mAttachmentsList = fileSubmitObjects;
        mCanvasEditTextView.getLeftButtonIndicator().setVisibility(fileSubmitObjects.size() > 0 ? View.VISIBLE : View.GONE);
        mCanvasEditTextView.getLeftButtonIndicator().setText("" + fileSubmitObjects.size());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(mUploadFileSourceFragment != null){
            mUploadFileSourceFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void registerReceivers() {
        errorBroadcastReceiver = getErrorReceiver();
        allUploadsCompleteBroadcastReceiver = getAllUploadsCompleted();

        getActivity().registerReceiver(errorBroadcastReceiver, new IntentFilter(FileUploadService.UPLOAD_ERROR));
        getActivity().registerReceiver(allUploadsCompleteBroadcastReceiver, new IntentFilter(FileUploadService.ALL_UPLOADS_COMPLETED));

        needsUnregister = true;
    }

    private void unregisterReceivers() {
        if(getActivity() == null || !needsUnregister){return;}

        if(errorBroadcastReceiver != null){
            getActivity().unregisterReceiver(errorBroadcastReceiver);
            errorBroadcastReceiver = null;
        }

        if(allUploadsCompleteBroadcastReceiver != null){
            getActivity().unregisterReceiver(allUploadsCompleteBroadcastReceiver);
            allUploadsCompleteBroadcastReceiver = null;
        }

        needsUnregister = false;
    }

    private void scrollToBottom() {
        // after a message added we'll want to scroll down
        if (mRecyclerView != null && mRecyclerAdapter != null) {
            mRecyclerView.smoothScrollToPosition(mRecyclerAdapter.size() - 1);
        }
    }

    private BroadcastReceiver getAllUploadsCompleted() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if(!isAdded()){return;}
                Conversation conversation = intent.getParcelableExtra(Const.CONVERSATION);
                handleNewConversation(conversation);
                showToast(R.string.filesUploadedSuccessfully);
                mCanvasEditTextView.getLeftButtonIndicator().setVisibility(View.GONE);
            }
        };
    }

    private BroadcastReceiver getErrorReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if(!isAdded()){return;}

                final Bundle bundle = intent.getExtras();
                String errorMessage = bundle.getString(Const.MESSAGE);
                if(null == errorMessage || "".equals(errorMessage)){
                    errorMessage = getString(R.string.errorUploadingFile);
                }
                showToast(errorMessage);
                mCanvasEditTextView.setRightProgressBarLoading(false);
                mCanvasEditTextView.getLeftButtonIndicator().setVisibility(View.GONE);
                mCanvasEditTextView.enableRightButton();
            }
        };
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Transitions
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void setSharedElementId(String newId){
        mSharedElementId = newId;
    }

    @TargetApi(21)
    @Override
    public Transition.TransitionListener getTransitionListener(){
        Transition.TransitionListener listener = new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {}

            @Override
            public void onTransitionEnd(Transition transition) {
                if(mRootView != null){
                    configureRecyclerView();
                }
            }

            @Override
            public void onTransitionCancel(Transition transition) {}

            @Override
            public void onTransitionPause(Transition transition) {}

            @Override
            public void onTransitionResume(Transition transition) {}
        };

        return listener;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //region Data
    ////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
        if (extras == null) {
            return;
        }

        if (getUrlParams() != null) {
            conversationID = parseLong(getUrlParams().get(Param.CONVERSATION_ID), 0);
        } else {
            conversationID = extras.getLong(Const.CONVERSATION_ID);
        }
        isStarred = extras.getBoolean(Const.IS_STARRED, false);
        isUnread = extras.getBoolean(Const.IS_UNREAD, false);
        avatarURL = extras.getString(Const.URL);
        mUserName = extras.getString(Const.NAME);
        mParticipants = extras.getParcelableArrayList(Const.FROM_PEOPLE);
        conversation = extras.getParcelable(Const.CONVERSATION);
        if(getContext() != null) {
            Picasso.with(getContext()).load(avatarURL).fetch();
        }
    }

    public static Bundle createBundle(Conversation conversation, String conversationTitle, boolean isUnread, boolean waitForTransition){
        Bundle extras = createBundle(CanvasContext.emptyUserContext());

        extras.putLong(Const.CONVERSATION_ID, conversation.getId());
        extras.putBoolean(Const.IS_UNREAD, isUnread);
        extras.putString(Const.URL, conversation.getAvatarURL());
        extras.putString(Const.NAME, conversationTitle);
        extras.putBoolean(Const.IS_STARRED, conversation.isStarred());
        extras.putBoolean(Const.WAIT_FOR_TRANSITION, waitForTransition);
        extras.putParcelableArrayList(Const.FROM_PEOPLE, new ArrayList<Parcelable>(conversation.getAllParticipants()));
        extras.putParcelable(Const.CONVERSATION, conversation);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, Conversation conversation, String conversationTitle, boolean isUnread) {
        Bundle extras = createBundle(canvasContext);
        if (conversation != null) {
            extras.putLong(Const.CONVERSATION_ID, conversation.getId());
            extras.putBoolean(Const.IS_STARRED, conversation.isStarred());
            extras.putString(Const.URL, conversation.getAvatarURL());
            extras.putParcelableArrayList(Const.FROM_PEOPLE, new ArrayList<Parcelable>(conversation.getAllParticipants()));
            extras.putParcelable(Const.CONVERSATION, conversation);
        }
        extras.putBoolean(Const.IS_UNREAD, isUnread);
        extras.putString(Const.NAME, conversationTitle);
        extras.putBoolean(Const.WAIT_FOR_TRANSITION, false);
        return extras;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PermissionUtils.WRITE_FILE_PERMISSION_REQUEST_CODE) {
            if(PermissionUtils.permissionGranted(permissions, grantResults, PermissionUtils.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(getActivity(), R.string.filePermissionGranted, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), R.string.filePermissionDenied, Toast.LENGTH_LONG).show();
            }
        }
    }
}

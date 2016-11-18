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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import com.instructure.candroid.R;
import com.instructure.candroid.api.CanvasAPI;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.dialog.FileUploadDialog;
import com.instructure.candroid.model.FormatHtmlObject;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.DiscussionEntryHTMLHelper;
import com.instructure.candroid.util.FragUtils;
import com.instructure.candroid.util.LockInfoHTMLHelper;
import com.instructure.candroid.util.LoggingUtility;
import com.instructure.candroid.util.NoNetworkErrorDelegate;
import com.instructure.candroid.util.Param;
import com.instructure.candroid.util.RouterUtils;
import com.instructure.candroid.view.AdvancedViewFlipper;
import com.instructure.candroid.view.CanvasEditTextView;
import com.instructure.candroid.view.CanvasLoading;
import com.instructure.candroid.view.CanvasWebView;
import com.instructure.canvasapi.api.DiscussionAPI;
import com.instructure.canvasapi.api.GroupAPI;
import com.instructure.canvasapi.api.compatibility_synchronous.DiscussionSynchronousAPI;
import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.DiscussionEntry;
import com.instructure.canvasapi.model.DiscussionParticipant;
import com.instructure.canvasapi.model.DiscussionTopic;
import com.instructure.canvasapi.model.DiscussionTopicHeader;
import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.activities.KalturaMediaUploadPicker;
import com.instructure.pandautils.models.FileSubmitObject;
import com.instructure.pandautils.services.FileUploadService;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.LoaderUtils;
import com.instructure.pandautils.utils.RequestCodes;
import com.instructure.pandautils.utils.view.PandaLoading;
import com.video.ActivityContentVideoViewClient;
import com.video.ContentVideoViewClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class DetailedDiscussionFragment extends ParentFragment implements
        LoaderManager.LoaderCallbacks<FormatHtmlObject>,
        FileUploadDialog.FileSelectionInterface,
        CanvasEditTextView.CanvasEditTextViewRightListener,
        CanvasEditTextView.CanvasEditTextViewLeftListener {

    /** Dear Person maintaining discussions,

     tl;dr Each paragraph describes certain design decisions behind discussions.

     First off, Congrats on receiving this assignment, <sarcasm> you are one lucky soul. </sarcasm>.  This is written in hopes to save you a few hours.

     When a user clicks a row in the DiscussionList, {@link #getFullDiscussionHeader()} is called or {@link #parseHeader(DiscussionTopicHeader)} if the header is already loaded.

     For a full discussion {@link #parseDiscussionTopic}
         To determine if a discussions is unread, the API returns an array of discussion IDs. Discussion objects are recursively initiated, and isUnread on the discussion object is set during that process.

     The discussion are displayed as html in a webview, which is generated in a few places, noteably {@link DetailedDiscussionFragment} or {@link DiscussionEntryHTMLHelper}

     Each page of discussions displays a Parent or Child. A Child also has children
        To handle the nested replies, when a user clicks a child, the child becomes the parent, and its children are displayed in a NEW WebView (as in a new webview is created).
        The {@link AdvancedViewFlipper} handles all of the newly created WebViews and maintains a history of where the user has been (it also provides transistion animations)

    Links are opened in an their own {@link InternalWebviewFragment}, because the JSInterface has potential security leaks, when loading untrusted content.

     Since discussions create so many different webviews, it makes it difficult to have DiscussionWebView extend CanvasWebView. In order to make discussions handle some
        of the media (video, pdfs, etc.), some of the features from CanvasWebView were copied. That being said, if something related to media is broken
        in Discussions, but seems to work in other places, it may be related to the fact that discussions do not use CanvasWebView

     That is all.
     */

    public interface UpdateUnreadListener {
        void updateUnread(long topicID, int unreadCount);
    }

    UpdateUnreadListener updateUnreadListener;

    private final static String fileAssestURL = "file:///android_asset/";
    private ContentVideoViewClient videoViewClient;

    // views
    private View mRootView;
    private CanvasEditTextView mCanvasEditTextView;
    private AdvancedViewFlipper avf;
    private CanvasLoading canvasLoading;
    private Button mediaComment;

    // logic
    private ArrayList<Long> aboutToMarkRead = new ArrayList<Long>();
    private DiscussionTopicHeader header;
    private DiscussionTopic topic;
    private long topicID;
    private DiscussionEntry currentEntry;
    private Handler mHandler;
    private final Object LOCK = new Object();
    private Group[] groups;

    private User user;

    private ArrayList<FileSubmitObject> mAttachmentsList = new ArrayList<>();
    private FileUploadDialog mUploadFileSourceFragment;
    private BroadcastReceiver errorBroadcastReceiver;
    private BroadcastReceiver allUploadsCompleteBroadcastReceiver;
    private boolean needsUnregister;

    private CanvasCallback<DiscussionEntry> sendMessageCanvasCallback;
    private CanvasCallback<DiscussionTopicHeader> discussionTopicHeaderCanvasCallback;
    private CanvasCallback<DiscussionTopic> discussionTopicCanvasCallback;
    private CanvasCallback<Group[]> userGroupsCallback;
    private CanvasCallback<DiscussionTopicHeader> pinDiscussionCanvasCallback;
    private CanvasContext mActiveCanvasContext;

    private View sendMessageWebView;
    private DiscussionWebView discussionWebView; // A new discussionWebView is created for each locally generated web page, if an external link is clicked it uses ExternalWebView (for security reasons related to JSInterface)
    private WebChromeClient discussionWebChromeClient;

    private Bundle loaderBundle = null;

    private DiscussionEntry queuedSentDiscussionEntry;
    private boolean isAnnouncement;


    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.DETAIL;
    }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.discussion);
    }


    private BroadcastReceiver discussionReplyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DiscussionEntry discussionEntry = intent.getParcelableExtra(Const.DISCUSSION_ENTRY);

            if(discussionEntry != null) {
                addSentDiscussionEntry(discussionEntry, false, false);
            }

        }
    };

    @Override
    public HashMap<String, String> getParamForBookmark() {
        HashMap<String, String> map = getCanvasContextParams();
        map.put(Param.MESSAGE_ID, Long.toString(topicID));
        return map;
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        return header != null ? header.getTitle() : null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    /////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize.
        mHandler = new Handler();
        videoViewClient = new ActivityContentVideoViewClient(getActivity());
        discussionWebChromeClient = new DiscussionChromeClient();

        setRetainInstance(this, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mRootView = getLayoutInflater().inflate(R.layout.detailed_discussion_fragment, container, false);
        avf = (AdvancedViewFlipper) mRootView.findViewById(R.id.viewFlipper);
        avf.setInAnimation(AnimationUtils.loadAnimation(getActivity(),
                R.anim.slow_push_left_in));
        avf.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),
                R.anim.slow_push_left_out));

        canvasLoading = (CanvasLoading)mRootView.findViewById(R.id.loading);

        configureMessageView();

        return mRootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof UpdateUnreadListener) {
            updateUnreadListener = (UpdateUnreadListener) activity;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (discussionWebView != null) {
            discussionWebView.onResume();
        }
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(discussionReplyReceiver,
                new IntentFilter(Const.DISCUSSION_REPLY_SUBMITTED));
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
    public void onPause() {
        super.onPause();
        if (discussionWebView != null) {
            discussionWebView.onPause();
        }
    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {
        super.onCallbackFinished(source);
        if (canvasLoading != null) {
            canvasLoading.displayNoConnection(false);
        }
    }

    @Override
    public void onNoNetwork() {
        if (canvasLoading != null) {
            canvasLoading.displayNoConnection(true);
        }
        super.onNoNetwork();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setUpCallbacks();

        user = APIHelpers.getCacheUser(getContext());

        LoaderUtils.restoreLoaderFromBundle(getActivity().getSupportLoaderManager(), savedInstanceState, this, R.id.formatLoaderID);

        // anything that relies on intent data belongs here
        if (header == null) {
            getFullDiscussionHeader();
        } else {
            parseHeader(header, false, false);
            setupTitle(getActionbarTitle());
        }
    }

    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);

        if(header != null && getCanvasContext() instanceof Course) {
            boolean isTeacher = ((Course) getCanvasContext()).isTeacher();
            if(isTeacher) {
                inflater.inflate((header.isPinned() ? R.menu.menu_pin_discussion : R.menu.menu_unpin_discussion), menu);
            }
        }

        if(header != null && header.getCreator() != null && header.getPermission() != null && user != null) {
            if(header.getPermission().canUpdate() && header.getCreator().getId() == user.getId()) {
                //allow discussion post editing
                inflater.inflate(R.menu.menu_discussion_post_editing, menu);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(header != null) {
            switch (item.getItemId()) {
                case R.id.menu_pin:
                    if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                        Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    if(header.isPinned()) {
                        DiscussionAPI.pinDiscussion(getCanvasContext(), header.getId(), false, pinDiscussionCanvasCallback);
                    }
                    return true;
                case R.id.menu_unpin:
                    if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                        Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    if(!header.isPinned()) {
                        DiscussionAPI.pinDiscussion(getCanvasContext(), header.getId(), true, pinDiscussionCanvasCallback);
                    }
                    return true;
                case R.id.discussionPostEdit:
                    if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                        Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    Navigation navigation = getNavigation();
                    if(navigation != null) {
                        navigation.addFragment(FragUtils.getFrag(ComposeNewDiscussionFragment.class,
                                ComposeNewDiscussionFragment.createBundle(getCanvasContext(), false, header, true)));
                    }
                    return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        LoaderUtils.saveLoaderBundle(outState, loaderBundle);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(mUploadFileSourceFragment != null){
            mUploadFileSourceFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void configureMessageView() {
        mCanvasEditTextView = (CanvasEditTextView) mRootView.findViewById(R.id.canvasEditTextView);
        if(header != null && header.getPermission() != null && !header.getPermission().canAttach()) {
            mCanvasEditTextView.hideLeftImage();
        } else {
            mCanvasEditTextView.setEditTextLeftListener(this);
            mCanvasEditTextView.setLeftButtonImage(R.drawable.ic_cv_attachment);
        }
        mCanvasEditTextView.setEditTextRightListener(this);
        mCanvasEditTextView.setRightButtonImage(R.drawable.ic_action_send);
        mCanvasEditTextView.setHideRightBeforeText();
    }

    private CanvasContext getActiveCanvasContext() {
        if (mActiveCanvasContext == null) {
            return getCanvasContext();
        }
        return mActiveCanvasContext;
    }

    @Override
    public void onLeftButtonClicked() {
        View v = avf.getCurrentView();

        if (v instanceof WebView) {
            sendMessageWebView = avf.getCurrentView();
        } else {
            return;
        }

        new MaterialDialog.Builder(getContext())
                .title(R.string.discussionUploadDialog)
                .items(R.array.discussion_attachments)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        switch(i){
                            case 0:
                                //Use File Upload Dialog
                                showUploadFileDialog();
                                break;
                            case 1:
                                //Use Kaltura
                                String message = mCanvasEditTextView.getText().toString();
                                Intent intent = KalturaMediaUploadPicker.createIntentForDiscussionReply(getContext(), currentEntry, message, topicID, getCanvasContext());
                                startActivityForResult(intent, RequestCodes.KALTURA_REQUEST);
                                break;
                            default:
                                break;
                        }
                    }
                })
                .show();
    }

    @Override
    public void onRightButtonClicked() {
        sendMessage(mCanvasEditTextView.getText());
    }

    @Override
    protected DiscussionTopicHeader getModelObject() {
        return header;
    }

    private void showUploadFileDialog(){
        //we don't want to remember what was previously there
        mAttachmentsList.clear();
        Bundle bundle = FileUploadDialog.createDiscussionsBundle(APIHelpers.getCacheUser(getContext()).getShortName(), mAttachmentsList);
        mUploadFileSourceFragment = FileUploadDialog.newInstance(getChildFragmentManager(),bundle);
        mUploadFileSourceFragment.setTargetFragment(this, 1337);

        mUploadFileSourceFragment.show(getChildFragmentManager(), FileUploadDialog.TAG);
    }

    @Override
    public void onFilesSelected(ArrayList<FileSubmitObject> fileSubmitObjects) {
        mAttachmentsList = fileSubmitObjects;
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

        if(discussionReplyReceiver != null){
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(discussionReplyReceiver);
        }

        needsUnregister = false;
    }

    //Creates new discussion reply with attachments for the user
    private BroadcastReceiver getAllUploadsCompleted() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if(!isAdded()){return;}
                showToast(R.string.filesUploadedSuccessfully);
                if(intent != null && intent.hasExtra(Const.ATTACHMENTS)) {
                    ArrayList<Attachment> attachments = (ArrayList<Attachment>) intent.getExtras().get(Const.ATTACHMENTS);
                    if (attachments != null && attachments.size() > 0) {
                        String message = "";
                        for (int i = 0; i < attachments.size(); i++) {
                            Attachment attachment = attachments.get(i);
                            message += "<a href=\"" + attachment.getUrl() + "\">" + getTextForAttachment() + " " + (i + 1) + "</a> <br>";
                        }
                        //Sends the attachments with pre-appended text from the edit text + a line break
                        sendMessage(mCanvasEditTextView.getText() + " <br> " + message);
                    }
                }
            }
        };
    }

    private String getTextForAttachment(){
        return getContext().getResources().getString(R.string.discussionAttachmentLabel);
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
            }
        };
    }

    public void sendMessage(String reply) {
        if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
            Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
            return;
        }

        if (reply.trim().length() == 0)
            showToast(R.string.emptyMessage);
        else {

            View v = avf.getCurrentView();

            if (v instanceof WebView) {
                sendMessageWebView = avf.getCurrentView();
            } else {
                return;
            }

            reply = reply.replaceAll("\\n", "<br/>");

            //disable the send button and start the progress spinner
            mCanvasEditTextView.disableRightButton();
            mCanvasEditTextView.setRightProgressBarLoading(true);

            if (currentEntry.getParent() == null) {
                DiscussionAPI.postDiscussionEntry(getActiveCanvasContext(), topicID, reply, sendMessageCanvasCallback);
            } else {
                DiscussionAPI.postDiscussionReply(getActiveCanvasContext(), topicID, currentEntry.getId(), reply, sendMessageCanvasCallback);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // region Web View
    ///////////////////////////////////////////////////////////////////////////

    //Used for showing external links.
    //Used for showing local generated discussion html.
    private class DiscussionWebView extends WebView {
        public DiscussionWebView(Context context) {
            super(context);
            this.setBackgroundDrawable(getResources().getDrawable(R.drawable.triangle_bg));
            this.getSettings().setJavaScriptEnabled(true);
            this.addJavascriptInterface(new JSInterface(), "accessor");
            // DO NOT use getSettings().setLayoutAlgorithm, it messes up the layout on API 16 ish devices

            this.getSettings().setUseWideViewPort(true);
            this.getSettings().setAllowFileAccess(true);
            this.getSettings().setLoadWithOverviewMode(true);

            this.setWebViewClient(new DiscussionWebViewClient());
            this.setWebChromeClient(discussionWebChromeClient);
            this.setDownloadListener(new DiscussionDownloadListener());

		/*
         * Bug fix for the text box not bring up the keyboard.
		 * http://stackoverflow.com/questions/3460915/webview-textarea-doesnt-pop-up-the-keyboard
		 */
            this.requestFocus(View.FOCUS_DOWN);
            this.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_UP:
                            if (!v.hasFocus()) {
                                v.requestFocus();
                            }
                            break;
                    }
                    return false;
                }
            });
        }

        @Override
        public void onPause() {
            try {
                super.onPause();
            } catch (NullPointerException e) {
                // Catch for older devices and webkit
                LoggingUtility.LogException(getContext(), e);
            } catch (Exception E) {
                LoggingUtility.LogException(getContext(), E);
            }
        }
    }

    private class DiscussionChromeClient extends WebChromeClient {
        private CustomViewCallback mCallback;
        @Override
        public void onProgressChanged(WebView view, final int newProgress) {
            // check to see if user exited activity
            if (getActivity() == null) {
                return;
            }

            if (newProgress == 100) {  //It's done
                //Stop indeterminate progress circle.
                hideProgressBar();
            } else {
                //Start indeterminate progress circle.
                showProgressBar();
            }
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            mCallback = callback;
            videoViewClient.onShowCustomView(view);
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            if (mCallback != null) {
                mCallback.onCustomViewHidden();
            }
            videoViewClient.onDestroyContentVideoView();
        }
    }

    private class DiscussionDownloadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            //get the filename
            String temp = "filename";
            int index = contentDisposition.indexOf(temp) + temp.length();
            String filename = "file";
            if (index > -1) {
                int end = contentDisposition.indexOf(";", index);
                if (end > -1) {
                    //+1 and -1 to remove the quotes
                    filename = contentDisposition.substring(index + 1, end - 1);
                }
                //make the filename unique to this assignment and course
                filename = getActiveCanvasContext().toAPIString() + "_" + topicID + "_" + filename;
            }

            openMedia(mimetype, url, filename);
        }
    }

    //Generate the html and load the page before pushing onto the stack.
    private class DiscussionWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            for (int i = 0; i < avf.getChildCount(); i++) { // workaround: Don't add the same view if its already there (Happens when links/embedded videos are opened) on samsung
                if (avf.getChildAt(i) == view) {
                    return;
                }
            }
            avf.addView(view);
            avf.showNext();
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!url.equals(fileAssestURL)) {
                //it's external. Load it in a different webview.
                if (!RouterUtils.canRouteInternally(getActivity(), url, APIHelpers.getDomain(getActivity()), true)) {
                    InternalWebviewFragment.loadInternalWebView(getActivity(), (Navigation) getActivity(), InternalWebviewFragment.createBundle(getCanvasContext(), url, false));
                }
                return true;
            }

            return super.shouldOverrideUrlLoading(view, url);
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // endregion
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // region Javascript Interface
    ///////////////////////////////////////////////////////////////////////////
    //javascript interface used by the webview.
    private class JSInterface {

        @JavascriptInterface
        public String getInViewPort() {
            synchronized (LOCK) {
                //Build a comma separated list of all unread ids.
                if (currentEntry == null)
                    return "";

                String array = "";
                if (currentEntry.isUnread()) {
                    if (array.length() != 0)
                        array += ",";
                    array += (currentEntry.getId());
                }

                for (DiscussionEntry reply : currentEntry.getReplies()) {
                    if (reply.isUnread()) {
                        if (array.length() != 0)
                            array += ",";
                        array += (reply.getId());
                    }
                }


                return array;
            }
        }

        @JavascriptInterface
        public void inViewPort(final String string) {
            if (string.equals(""))
                return;

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    String[] temp = (string.split(","));

                    ArrayList<Long> inView = new ArrayList<Long>();

                    for (String string : temp) {
                        try {
                            inView.add(Long.parseLong(string));
                        } catch (Exception E) {
                        }
                    }

                    ArrayList<Long> markAsReadNOW = new ArrayList<Long>();

                    while (aboutToMarkRead.size() > 0) {
                        int indexInView = inView.indexOf(aboutToMarkRead.get(0));
                        if (indexInView != -1) {
                            //They actually need to be deleted.
                            markAsReadNOW.add(aboutToMarkRead.get(0));
                            inView.remove(indexInView);
                        }
                        aboutToMarkRead.remove(0);
                    }

                    //The rest of them are on the chopping block next time.
                    aboutToMarkRead.addAll(inView);

                    if (markAsReadNOW.size() > 0) {
                        //Start the async task...
                        Long[] asyncTaskMarked = new Long[markAsReadNOW.size()];
                        for (int i = 0; i < markAsReadNOW.size(); i++) {
                            asyncTaskMarked[i] = markAsReadNOW.get(i);
                        }

                        //Check if it's a webview.
                        View v = avf.getCurrentView();
                        if (v instanceof WebView && CanvasRestAdapter.isNetworkAvaliable(v.getContext())) {
                            new MarkAsReadAsyncTask(((WebView) v)).execute(asyncTaskMarked);
                        }
                    }
                }
            });

        }

        @JavascriptInterface
        public void onPressed(final String msg) {

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (getActivity() == null) return;

                        int i = Integer.parseInt(msg);
                        if (i >= 0 && i < currentEntry.getReplies().size()) {

                            if (header.getType() == DiscussionTopicHeader.DiscussionType.SIDE_COMMENT && currentEntry.getDepth() >= 1) {
                                //:One level deep for side comments.
                                showToast(R.string.sideCommentEntry);
                            } else if (currentEntry.getReplies().get(i).isDeleted() && currentEntry.getReplies().get(i).getTotalChildren() == 0) {
                                //Can't go into deleted entries with no children.
                                showToast(R.string.deletedEntryNoChildren);
                            } else {
                                currentEntry = currentEntry.getReplies().get(i);

                                if (currentEntry.isUnread()) {
                                    //Mark as read locally
                                    currentEntry.setUnread(false);

                                    View v = avf.getCurrentView();
                                    if (v instanceof WebView && CanvasRestAdapter.isNetworkAvaliable(v.getContext())) {
                                        new MarkAsReadAsyncTask(((WebView) v)).execute(currentEntry.getId());
                                    }
                                }
                                //Generate the HTML
                                loaderBundle = createLoaderBundle(currentEntry, topic, header);
                                discussionWebView = new DiscussionWebView(getActivity());
                                LoaderUtils.restartLoaderWithBundle(getLoaderManager(), loaderBundle, DetailedDiscussionFragment.this, R.id.formatLoaderID);
                            }
                        }
                    } catch (Exception E) {
                    }

                }
            });
        }

        @JavascriptInterface
        public void onRatePressed(final String string) {

            mHandler.post(new Runnable() {
                int rating = 0;
                DiscussionEntry entry = null;
                HashMap<Long, Integer> ratingsMap = topic.getEntryRatings();

                @Override
                public void run() {
                    try {
                        if (getActivity() == null) return;

                        final long entryId = Long.parseLong(string);


                        //get the current rating
                        if(ratingsMap != null) {
                            if (ratingsMap.containsKey(entryId)) {
                                rating = ratingsMap.get(entryId);
                            }
                        } else {
                            ratingsMap = new HashMap<>();
                        }

                        //now switch the rating so we pass the correct parameter to the API
                        if(rating == 1) {
                            rating = 0;
                        } else {
                            rating = 1;
                        }

                        if(currentEntry.getReplies().size() > 0) {
                            for (int i = 0; i < currentEntry.getReplies().size(); i++) {
                                if (currentEntry.getReplies().get(i).getId() == entryId) {
                                    entry = currentEntry.getReplies().get(i);
                                }
                            }
                        } else if(currentEntry.getId() == entryId) {
                            entry = currentEntry;
                        }



                        DiscussionAPI.rateDiscussionEntry(getActiveCanvasContext(), header.getId(), entryId, rating, new CanvasCallback<Response>(DetailedDiscussionFragment.this) {
                            @Override
                            public void firstPage(Response response, LinkHeaders linkHeaders, Response response2) {
                                if(response.getStatus() == 204) {
                                    //success, update the like info
                                    if(entry != null) {
                                        if (rating == 0) {
                                            //subtract a rating from the rating sum
                                            entry.setRatingSum(entry.getRatingSum() - 1);
                                        } else {
                                            entry.setRatingSum(entry.getRatingSum() + 1);
                                        }
                                    }

                                    String likeString = getLikeString(getActivity(), entry);
                                    discussionWebView.loadUrl("javascript:updateLikeNums('" + entryId + "','" + likeString + "')");

                                    //update the ratings map
                                    ratingsMap.put(entryId, rating);
                                    topic.setEntryRatings(ratingsMap);

                                }
                            }
                        });

                    } catch (Exception E) {
                    }

                }
            });
        }
    }

    private static String getLikeString(Context context, DiscussionEntry entry) {
        String likeString = "";
        if(entry != null) {

            if (entry.getRatingSum() == 1) {
                likeString = entry.getRatingSum() + " " + context.getString(R.string.like);
            } else if (entry.getRatingSum() > 1) {
                likeString = entry.getRatingSum() + " " + context.getString(R.string.likes);
            }
        }
        return likeString;
    }
    ///////////////////////////////////////////////////////////////////////////
    // endregion
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Logic
    ///////////////////////////////////////////////////////////////////////////

    void parseDiscussionTopic(DiscussionTopic topic) {
        if (topic == null) {
            return;
        } else {
            this.topic = topic;

            currentEntry.setReplies(new ArrayList<DiscussionEntry>());

            //init it all up.
            for (DiscussionEntry discussionEntry : topic.getViews()) {
                discussionEntry.init(topic, currentEntry);
                currentEntry.addReply(discussionEntry);
            }


            //It's possible they sent a new message, but it's not available via API yet.
            //Discussions use cached data not necessarily live data.
            if (queuedSentDiscussionEntry != null) {

                boolean hasMatchedID = false;

                //See if we already have the message in the replies list.
                if (currentEntry.getReplies() != null) {
                    for (DiscussionEntry reply : currentEntry.getReplies()) {
                        if (reply.getId() == queuedSentDiscussionEntry.getId()) {
                            hasMatchedID = true;
                            break;
                        }
                    }
                }

                //If none of the replies matched our queued entry, add it.
                if (!hasMatchedID) {
                    addDiscussionEntryToCurrent(queuedSentDiscussionEntry);
                }

                //It no longer needs to be cached.
                queuedSentDiscussionEntry = null;
            }

            int totalChildren = 0;
            int unreadChildren = 0;

            for (DiscussionEntry reply : currentEntry.getReplies()) {
                totalChildren += reply.getTotalChildren() + 1;
                unreadChildren += reply.getUnreadChildren();

                if (reply.isUnread()) {
                    unreadChildren++;
                }
            }

            currentEntry.setTotalChildren(totalChildren);
            currentEntry.setUnreadChildren(unreadChildren);
        }
    }

    /**
     * For explanation of isWithinAnotherCallback and isCached refer to comment in {@link com.instructure.candroid.activity.CallbackActivity#getUserSelf}
     */
    void parseHeader(DiscussionTopicHeader header, boolean isWithinAnotherCallback, boolean isCached) {
        if (header == null || getActivity() == null) {
            return;
        }

        if (header.getLockInfo() != null) {
            DiscussionWebView dwv = new DiscussionWebView(getActivity());
            String html = CanvasAPI.getAssetsFile(getActivity(), "html_wrapper.html");

            //It's a module lock
            html = html.replace("{$CONTENT$}", LockInfoHTMLHelper.getLockedInfoHTML(header.getLockInfo(), getActivity(), R.string.lockedDiscussionDesc, R.string.lockedAssignmentDescLine2));

            //loadDataWIthBaseURL works better than loadData

            // BaseURL is set as Referer. Referer needed for some vimeo videos to play
            dwv.loadDataWithBaseURL(CanvasWebView.getRefererDomain(getContext()), html, "text/html", "utf-8", null);

            //if the discussion is locked we don't want to let the user reply to the discussion
            mCanvasEditTextView.setVisibility(View.GONE);
        } else {
            this.header = header;
            setupTitle(getActionbarTitle());
            getSupportActionBar().invalidateOptionsMenu();
            configureMessageView();
            currentEntry = header.convertToDiscussionEntry(getResources().getString(R.string.gradedDiscussion), getResources().getString(R.string.pointsPossible));

            Course course = getCanvasContext() instanceof Course ? (Course) getCanvasContext() : null;

            // If there is a groupCategoyId, that means its part of a group discussion.
            // Students should be directed to their groups discussion (a student can only be in one group and only StudentEnrollments are allowed to be members of a group).
            if (header.getGroupCategoryId() != null && course != null &&
                    (course.isStudent() && !course.isTeacher() && !course.isTA())) { // Edge case where a teacher is a student in the same course, only want students
                if (isWithinAnotherCallback) {
                    GroupAPI.getGroupsForUserChained(userGroupsCallback, isCached);
                } else {
                    GroupAPI.getGroupsForUser(userGroupsCallback);
                }
            } else {
                mActiveCanvasContext = getCanvasContext();
                getFullDiscussion(mActiveCanvasContext, isWithinAnotherCallback, isCached);
            }
        }
    }

    void markUnreadRecursively(DiscussionEntry current, List<Long> ids, boolean isUnread) {
        if (ids.contains(current.getId())) {
            current.setUnread(isUnread);
        }

        for (DiscussionEntry reply : current.getReplies()) {
            markUnreadRecursively(reply, ids, isUnread);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // region Callbacks
    ///////////////////////////////////////////////////////////////////////////

    void getFullDiscussionHeader() {
        DiscussionAPI.getDetailedDiscussion(getCanvasContext(), topicID, discussionTopicHeaderCanvasCallback);
    }

    /**
    * For explanation of isWithinAnotherCallback and isCached refer to comment in {@link com.instructure.candroid.activity.CallbackActivity#getUserSelf}
    */
    void getFullDiscussion(CanvasContext canvasContext, boolean isWithinAnotherCallback, boolean isCached) {
        if (isWithinAnotherCallback) {
            DiscussionAPI.getFullDiscussionTopicChained(canvasContext, topicID, discussionTopicCanvasCallback, isCached);
        } else {
            DiscussionAPI.getFullDiscussionTopic(canvasContext, topicID, discussionTopicCanvasCallback);
        }
    }

    private void getStudentGroupDiscussionChained(final Group group, boolean isCached) {
        DiscussionAPI.getStudentGroupDiscussionTopicHeaderChained(group, group.getGroupCategoryId(), new CanvasCallback<DiscussionTopicHeader[]>(DetailedDiscussionFragment.this) {
            @Override
            public void firstPage(DiscussionTopicHeader[] discussionTopicHeaders, LinkHeaders linkHeaders, Response response) {
                if (!apiCheck()) {
                    return;
                }
                // The topics are returned in an array, just use the first one.
                if (discussionTopicHeaders.length > 0) {
                    DiscussionTopicHeader header = discussionTopicHeaders[0];
                    topicID = header.getId();
                    getFullDiscussion(group, true, APIHelpers.isCachedResponse(response));
                } else {
                    // There wasn't any results, just show the top level discussion
                    getFullDiscussion(getCanvasContext(), true, APIHelpers.isCachedResponse(response));
                }
            }
        }, isCached);
    }

    public void changeActionBarColor() {
        Navigation navigation =  getNavigation();
        if (navigation != null) {
            int color = CanvasContextColor.getCachedColor(getContext(), getActiveCanvasContext());
            navigation.setActionBarStatusBarColors(color, color);
        }
    }

    public void setUpCallbacks() {

        discussionTopicHeaderCanvasCallback = new CanvasCallback<DiscussionTopicHeader>(this) {
            @Override
            public void firstPage(DiscussionTopicHeader discussionTopicHeader, LinkHeaders linkHeaders, Response response) {
                if (!apiCheck()) {
                    return;
                }

                parseHeader(discussionTopicHeader, true, APIHelpers.isCachedResponse(response));
            }
        };


        //We need to use a NoNetworkErrorDelegate because when a Discussion is hidden, we get a 403 forbidden error.
        //We don't want the normal behavior to display a crouton.
        discussionTopicCanvasCallback = new CanvasCallback<DiscussionTopic>(this, new NoNetworkErrorDelegate()) {
            @Override
            public void firstPage(DiscussionTopic discussionTopic, LinkHeaders linkHeaders, Response response) {
                if (!apiCheck()) {
                    return;
                }

                parseDiscussionTopic(discussionTopic);

                loadResultsIntoWebview();
            }

            @Override
            public boolean onFailure(RetrofitError error) {

                //If it's not a network error, it failed because it's forbidden.
                if (error != null && error.getResponse() != null && error.getResponse().getStatus() == 403) {
                    //It's currently forbidden.
                    topic = new DiscussionTopic();
                    topic.setForbidden(true);

                    //It's locked. Load the header anyways.
                    loadResultsIntoWebview();
                }

                // Make sure failure runs so we log to the appropriate place.
                return false;
            }

            public void loadResultsIntoWebview() {
                loaderBundle = createLoaderBundle(currentEntry, topic, header);
                discussionWebView = new DiscussionWebView(getActivity());
                LoaderUtils.restartLoaderWithBundle(getLoaderManager(), loaderBundle, DetailedDiscussionFragment.this, R.id.formatLoaderID);
            }
        };

        sendMessageCanvasCallback = new CanvasCallback<DiscussionEntry>(this) {
            @Override
            public void firstPage(DiscussionEntry discussionEntry, LinkHeaders linkHeaders, Response response) {
                if (!apiCheck()) {
                    return;
                }

                //Clear the textbox and attachments (if any)
                mCanvasEditTextView.setText("", false);
                if(mAttachmentsList.size() > 0){
                    mAttachmentsList.clear();
                }
                //enable the send button
                mCanvasEditTextView.enableRightButton();
                mCanvasEditTextView.setRightProgressBarLoading(false);


                addSentDiscussionEntry(discussionEntry, true, APIHelpers.isCachedResponse(response));
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                //enable the send button
                mCanvasEditTextView.enableRightButton();
                mCanvasEditTextView.setRightProgressBarLoading(false);

                return super.onFailure(retrofitError);
            }
        };

        userGroupsCallback = new CanvasCallback<Group[]>(this) {
            @Override
            public void firstPage(Group[] groups, LinkHeaders linkHeaders, Response response) {
                if (!apiCheck()) {
                    return;
                }

                boolean isCached = APIHelpers.isCachedResponse(response);

                DetailedDiscussionFragment.this.groups = groups;
                if (header != null) {
                    for (Group group : groups) {
                        // a discussion header only has one groupCategoryId and a group can only be in one group category
                        if (parseLong(header.getGroupCategoryId(), -1) == group.getGroupCategoryId()) {
                            mActiveCanvasContext = group;
                            setCanvasContext(mActiveCanvasContext); // set the context for when bookmarks are created
                            changeActionBarColor(); // For a student, they see a discussion in a group context, so change the color to that group
                            getStudentGroupDiscussionChained(group, isCached);
                            return;
                        }
                    }
                }
                // If the group can't be found, just show the top level group discussion
                getFullDiscussion(getCanvasContext(), true, isCached);
            }
        };

        pinDiscussionCanvasCallback = new CanvasCallback<DiscussionTopicHeader>(this) {
            @Override
            public void firstPage(DiscussionTopicHeader topicHeader, LinkHeaders linkHeaders, Response response) {
                if (!apiCheck()) {
                    return;
                }

                if(response.getStatus() == 200) {
                    header.setPinned(topicHeader.isPinned());
                    Toast.makeText(getContext(), topicHeader.isPinned() ? R.string.discussion_pinned : R.string.discussion_unpinned, Toast.LENGTH_SHORT).show();
                    getSupportActionBar().invalidateOptionsMenu();
                }
            }
        };
    }

    /**
     * Add the discussionEntry to the current list of discussionEntries
     *
     * @param discussionEntry DiscussionEntry to add
     *
     * For explanation of isWithinAnotherCallback and isCached refer to comment in {@link com.instructure.candroid.activity.CallbackActivity#getUserSelf}
     */
    private void addSentDiscussionEntry(final DiscussionEntry discussionEntry, boolean isWithinAnotherCallback, boolean isCached) {
        if (topic.isForbidden()) {
            topic.setForbidden(false);

            //Queue the sent message as it takes a moment to be available from the API.
            //This is due to discussions using a cache model instead of live data.
            queuedSentDiscussionEntry = discussionEntry;

            //Sets the discussion context based on either a group or the course
            getFullDiscussion(getActiveCanvasContext(), isWithinAnotherCallback, isCached);
        } else {
            addDiscussionEntryToCurrent(discussionEntry);

            //make the unread color the color of the course.
            //we use the substring to remove the alpha channel because font color doesn't work with alpha
            final int color = CanvasContextColor.getCachedColor(getContext(), getActiveCanvasContext());
            final String colorString = CanvasContextColor.getColorStringFromInt(color, true);

            HashMap<Long, Integer> ratingsMap = topic.getEntryRatings();
            int rating = 0;
            if(ratingsMap != null) {
                if (ratingsMap.containsKey(discussionEntry.getId())) {
                    rating = ratingsMap.get(discussionEntry.getId());
                }
            }
            boolean shouldShowRating = shouldAllowRating(getActiveCanvasContext(), header) && !isAnnouncement;
            String html = DiscussionEntryHTMLHelper.getHTML(discussionEntry, getActivity(), currentEntry.getReplies().size() - 1, getString(R.string.deleted), colorString, shouldShowRating, rating, getLikeString(getActivity(), discussionEntry));

            html = html.replace("'", "\\'").replace("\"", "\\\"");
            if (sendMessageWebView instanceof WebView) {
                WebView web = (WebView) sendMessageWebView;
                web.loadUrl("javascript:appendHTML('" + html + "')");
                if(shouldShowRating) {
                    web.loadUrl("javascript:updateLikeNums('" + discussionEntry.getId() + "','" + getLikeString(getActivity(), discussionEntry) + "')");
                }
                //Scroll to the bottom of the webview.
                web.pageDown(true);
            }

            //One more child all the way up.
            DiscussionEntry current = discussionEntry;
            DiscussionEntry previous = discussionEntry;
            int wvIndex = avf.getChildCount();
            while (current.getParent() != null) {
                previous = current;
                current = current.getParent();
                current.setTotalChildren(current.getTotalChildren() + 1);
                wvIndex--;
                try {
                    String prevHTML = DiscussionEntryHTMLHelper.getUnreadAndTotalLabelHtml(previous, false).replace("'", "\\'").replace("\"", "\\\"");
                    String currentHTML = DiscussionEntryHTMLHelper.getUnreadAndTotalLabelHtml(current, false).replace("'", "\\'").replace("\"", "\\\"");

                    View v = avf.getChildAt(wvIndex);
                    if (v instanceof WebView) {
                        ((WebView) v).loadUrl("javascript:setCountLabels('" + previous.getId() + "','" + prevHTML + "')");
                        ((WebView) v).loadUrl("javascript:setCountLabels('" + current.getId() + "','" + currentHTML + "')");
                    }
                } catch (Exception E) {
                }

            }

        }
    }

    private static boolean shouldAllowRating(CanvasContext canvasContext, DiscussionTopicHeader header) {
        if(header.shouldAllowRating()) {
            if(header.isOnlyGradersCanRate()) {
                if (canvasContext.getType() == CanvasContext.Type.COURSE) {
                    return ((Course) canvasContext).isTeacher() || ((Course) canvasContext).isTA();
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public void addDiscussionEntryToCurrent(DiscussionEntry discussionEntry) {
        currentEntry.addReply(discussionEntry);
        discussionEntry.setParent(currentEntry);

        HashMap<Long, DiscussionParticipant> participantHashMap = topic.getParticipantsMap();
        DiscussionParticipant discussionParticipant = participantHashMap.get(discussionEntry.getUserId());
        if (discussionParticipant != null) {
            discussionEntry.setAuthor(discussionParticipant);
        } else {
            //If we didn't find an author, it must be us.
            //If this is our first post, we won't be in the author's list.
            long id = user.getId();

            DiscussionParticipant dp = new DiscussionParticipant(id);
            dp.setAvatarUrl(user.getAvatarURL());
            dp.setDisplayName(user.getName());
            dp.setHtmlUrl("");
            discussionEntry.setAuthor(dp);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // endregion
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // region AsyncTask
    ///////////////////////////////////////////////////////////////////////////

    private class MarkAsReadAsyncTask extends AsyncTask<Long, Void, ArrayList<Long>> {

        WebView webView;

        public MarkAsReadAsyncTask(WebView webview) {
            webView = webview;
        }

        @Override
        protected ArrayList<Long> doInBackground(Long... params) {
            ArrayList<Long> successful = new ArrayList<Long>();

            // check to see if user exited activity
            if (getActivity() == null) {
                return successful;
            }

            for (Long id : params) {
                if (id == 0) {
                    if (DiscussionSynchronousAPI.markDiscussionReplyAsRead(getActiveCanvasContext(), topicID, getActivity())) {
                        successful.add(id);
                    }
                } else {
                    if (DiscussionSynchronousAPI.markDiscussionEntryAsRead(getActiveCanvasContext(), topicID, id, getActivity())) {
                        successful.add(id);
                    }
                }
            }

            return successful;
        }

        @Override
        protected void onPostExecute(ArrayList<Long> results) {
            //These are the ones that just got marked as read.

            //Null pointer check
            if (getActivity() == null) {
                return;
            }

            if (results.size() == 0) {
                return;
            }

            //Mark the model.
            DiscussionEntry entry = currentEntry;
            while (entry.getParent() != null) {
                entry = entry.getParent();
            }

            //Save the parent.
            DiscussionEntry parent = entry;

            markUnreadRecursively(entry, results, false);

            DiscussionEntry current = currentEntry;

            for (DiscussionEntry reply : currentEntry.getReplies()) {
                if (results.contains(reply.getId())) {
                    current = reply;
                    break;
                }
            }

            //One more child all the way up.
            DiscussionEntry previous = current;
            int wvIndex = avf.getChildCount();
            while (current.getParent() != null) {
                previous = current;
                current = current.getParent();
                current.setUnreadChildren(current.getUnreadChildren() - results.size());
                wvIndex--;
                try {
                    String prevHTML = DiscussionEntryHTMLHelper.getUnreadAndTotalLabelHtml(previous, false).replace("'", "\\'").replace("\"", "\\\"");
                    String currentHTML = DiscussionEntryHTMLHelper.getUnreadAndTotalLabelHtml(current, false).replace("'", "\\'").replace("\"", "\\\"");

                    View v = avf.getChildAt(wvIndex);
                    String previousLoadUrl = String.format("javascript:setCountLabels('%d','%s')", previous.getId(), prevHTML);
                    String currentLoadUrl = String.format("javascript:setCountLabels('%d','%s')", current.getId(), currentHTML);
                    if (v instanceof WebView) {
                        ((WebView) v).loadUrl(previousLoadUrl, Utils.getReferer(getContext()));
                        ((WebView) v).loadUrl(currentLoadUrl, Utils.getReferer(getContext()));
                    }
                } catch (Exception E) {
                }
            }

            if (webView != null) {
                for (int i = 0; i < results.size(); i++) {
                    webView.loadUrl("javascript:markAsRead('" + results.get(i) + "')", Utils.getReferer(getContext()));
                }
            }

            //Let the list know that the unread count has changed.
            if (updateUnreadListener != null) {
                updateUnreadListener.updateUnread(topicID, parent.getUnreadChildren());
            }
        }
    }

    public static class FormatHtmlLoader extends AsyncTaskLoader<FormatHtmlObject> {
        final DiscussionEntry discussionEntry;
        final DiscussionTopic discussionTopic;
        final CanvasContext canvasContext;
        final DiscussionTopicHeader discussionTopicHeader;
        final boolean isAnnouncement;

        public FormatHtmlLoader(Context context, CanvasContext canvasContext, DiscussionEntry discussionEntry, DiscussionTopic discussionTopic, DiscussionTopicHeader discussionTopicHeader, boolean isAnnouncement) {
            super(context);
            this.discussionEntry = discussionEntry;
            this.discussionTopic = discussionTopic;
            this.canvasContext = canvasContext;
            this.discussionTopicHeader = discussionTopicHeader;
            this.isAnnouncement = isAnnouncement;
        }

        @Override
        public FormatHtmlObject loadInBackground() {

            String html = CanvasAPI.getAssetsFile(getContext(), "discussion_html_header.html");

            final int color = CanvasContextColor.getCachedColor(getContext(), canvasContext);
            final String colorString = CanvasContextColor.getColorStringFromInt(color, true);

            HashMap<Long, Integer> ratingsMap = discussionTopic.getEntryRatings();
            int rating = 0;
            if(ratingsMap != null) {
                if (ratingsMap.containsKey(discussionEntry.getId())) {
                    rating = ratingsMap.get(discussionEntry.getId());
                }
            }

            html = html.replaceAll("#0076A3", colorString);

            html += DiscussionEntryHTMLHelper.getHTML(discussionEntry, getContext(), -1, getContext().getString(R.string.deleted), colorString, discussionTopic.isForbidden(), !isAnnouncement && shouldAllowRating(canvasContext, discussionTopicHeader), rating, getLikeString(getContext(), discussionEntry));

            //Now get children.
            for (int i = 0; i < discussionEntry.getReplies().size(); i++) {
                rating = 0;
                if(ratingsMap != null) {
                    if (ratingsMap.containsKey(discussionEntry.getReplies().get(i).getId())) {
                        rating = ratingsMap.get(discussionEntry.getReplies().get(i).getId());
                    }
                }
                html += DiscussionEntryHTMLHelper.getHTML(discussionEntry.getReplies().get(i), getContext(), i, getContext().getString(R.string.deleted), colorString, !isAnnouncement && shouldAllowRating(canvasContext, discussionTopicHeader), rating, getLikeString(getContext(),discussionEntry.getReplies().get(i)));

            }

            html = CanvasWebView.applyWorkAroundForDoubleSlashesAsUrlSource(html);
            html += CanvasAPI.getAssetsFile(getContext(), "discussion_html_footer.html");

            return new FormatHtmlObject(html, null);
        }
    }

    @Override
    public Loader<FormatHtmlObject> onCreateLoader(int id, Bundle args) {
        return new FormatHtmlLoader(getContext(), getActiveCanvasContext(), (DiscussionEntry)args.getParcelable("entry"), (DiscussionTopic)args.getParcelable("topic"), (DiscussionTopicHeader)args.getParcelable(Const.DISCUSSION_HEADER), isAnnouncement);
    }

    @Override
    public void onLoadFinished(Loader<FormatHtmlObject> loader, FormatHtmlObject data) {
        if (discussionWebView != null) {
            // BaseURL is set as Referer. Referer needed for some vimeo videos
            discussionWebView.loadDataWithBaseURL(CanvasWebView.getRefererDomain(getContext()), data.html, "text/html", "utf-8", null);
        }

        //we're done with the bundle
        loaderBundle = null;

            /*
                There are three related scenarios in which we don't want users to be able to reply.
                   so we check that none of these conditions exist
                1.) The discussion is locked for an unknown reason.
                2.) It's locked due to a module/etc.
                3.) User is an Observer in a course.
            */

        if (!header.isLocked() && (header.getLockInfo() == null || header.getLockInfo().isEmpty()) && (getCanvasContext().getType() != CanvasContext.Type.COURSE || !((Course) getCanvasContext()).isObserver())) {
            mCanvasEditTextView.setVisibility(View.VISIBLE);
        }
    }

    private Bundle createLoaderBundle(DiscussionEntry discussionEntry, DiscussionTopic discussionTopic, DiscussionTopicHeader discussionTopicHeader) {
        Bundle loaderBundle = new Bundle();
        loaderBundle.putParcelable("entry", discussionEntry);
        loaderBundle.putParcelable("topic", discussionTopic);
        loaderBundle.putParcelable(Const.DISCUSSION_HEADER, discussionTopicHeader);
        return loaderBundle;
    }

    @Override
    public void onLoaderReset(Loader<FormatHtmlObject> loader) {
    }

    ///////////////////////////////////////////////////////////////////////////
    // endregion
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean handleBackPressed() {
        if (videoViewClient.isFullscreen()) {
            discussionWebChromeClient.onHideCustomView();
            return true;
        }
        //it's a local webview. pop off the view stack.
        if (currentEntry != null && currentEntry.getParent() != null) {


            //we aren't at the top.
            //Show previous. Then delete the final one.
            currentEntry = currentEntry.getParent();

            View current = avf.getCurrentView();

            avf.showPrevious(AnimationUtils.loadAnimation(getActivity(),
                    R.anim.slow_push_right_in), AnimationUtils.loadAnimation(getActivity(),
                    R.anim.slow_push_right_out));

            avf.removeView(current);

            //get the curreunt displayed view and update the liked/unliked count and states
            View v = avf.getCurrentView();

            if (v instanceof WebView) {
                HashMap<Long, Integer> ratingsMap = topic.getEntryRatings();

                discussionWebView = (DiscussionWebView)v;
                int rating = 0;
                if(ratingsMap != null) {
                    if (ratingsMap.containsKey(currentEntry.getId())) {
                        rating = ratingsMap.get(currentEntry.getId());
                        if(rating == 0) {
                            discussionWebView.loadUrl("javascript:setUnliked('" + currentEntry.getId() + "')");
                        } else {
                            discussionWebView.loadUrl("javascript:setLiked('" + currentEntry.getId() + "')");
                        }
                        if(!isAnnouncement && shouldAllowRating(getCanvasContext(), header)) {
                            discussionWebView.loadUrl("javascript:updateLikeNums('" + currentEntry.getId() + "','" + getLikeString(getActivity(), currentEntry) + "')");
                        }
                    }
                }

                //Now update the children.
                for (int i = 0; i < currentEntry.getReplies().size(); i++) {
                    if (ratingsMap != null) {
                        if (ratingsMap.containsKey(currentEntry.getReplies().get(i).getId())) {
                            rating = ratingsMap.get(currentEntry.getReplies().get(i).getId());
                            if(rating == 0) {
                                discussionWebView.loadUrl("javascript:setUnliked('" + currentEntry.getReplies().get(i).getId() + "')");
                            } else {
                                discussionWebView.loadUrl("javascript:setLiked('" + currentEntry.getReplies().get(i).getId() + "')");
                            }
                            if(!isAnnouncement && shouldAllowRating(getCanvasContext(), header)) {
                                discussionWebView.loadUrl("javascript:updateLikeNums('" + currentEntry.getReplies().get(i).getId() + "','" + getLikeString(getActivity(), currentEntry.getReplies().get(i)) + "')");
                            }
                        }
                    }
                }
            }

        } else {
            //it's a local webview and we're showing the root.
            return false;
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
        if (extras.containsKey(Const.TOPIC_ID)) {
            topicID = extras.getLong(Const.TOPIC_ID, -1);
        } else if (getUrlParams() != null) {
            topicID = parseLong(getUrlParams().get(Param.MESSAGE_ID), -1);
        } else {
            header =  extras.getParcelable(Const.TOPIC_HEADER);
            topicID = header.getId();
        }
        isAnnouncement = extras.getBoolean(Const.ANNOUNCEMENT, false);
    }

    public static Bundle createBundle(CanvasContext canvasContext, DiscussionTopicHeader topic, boolean isAnnouncement) {
        Bundle extras = createBundle(canvasContext);
        extras.putParcelable(Const.TOPIC_HEADER, topic);
        extras.putBoolean(Const.ANNOUNCEMENT, isAnnouncement);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, long topicId, boolean isAnnouncement) {
        Bundle extras = createBundle(canvasContext);
        extras.putLong(Const.TOPIC_ID, topicId);
        extras.putBoolean(Const.ANNOUNCEMENT, isAnnouncement);
        return extras;
    }

    @Override
    public boolean allowBookmarking() {
        Navigation navigation = getNavigation();
        //navigation is a course, but isn't in notification list.
        return (navigation != null && navigationContextIsCourse() && !(navigation.getCurrentFragment() instanceof NotificationListFragment));
    }
}


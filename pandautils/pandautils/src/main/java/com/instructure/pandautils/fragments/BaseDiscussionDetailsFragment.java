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

package com.instructure.pandautils.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.DiscussionManager;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.DiscussionEntry;
import com.instructure.canvasapi2.models.DiscussionParticipant;
import com.instructure.canvasapi2.models.DiscussionTopic;
import com.instructure.canvasapi2.models.DiscussionTopicHeader;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.canvasapi2.utils.NetworkUtils;
import com.instructure.pandautils.R;
import com.instructure.pandautils.loaders.FormatHtmlLoader;
import com.instructure.pandautils.models.FormatHtmlObject;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.DiscussionEntryHTMLHelper;
import com.instructure.pandautils.utils.LoaderUtils;
import com.instructure.pandautils.utils.ThemeUtils;
import com.instructure.pandautils.video.ActivityContentVideoViewClient;
import com.instructure.pandautils.video.ContentVideoViewClient;
import com.instructure.pandautils.views.AdvancedViewFlipper;
import com.instructure.pandautils.views.CanvasWebView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Response;


public abstract class BaseDiscussionDetailsFragment extends BaseFragment implements
        LoaderManager.LoaderCallbacks<FormatHtmlObject>{

    private static final boolean DEBUG = false;

    protected static final int REQUEST_CODE_NEW_REPLY_TO_ENTRY = 1080;
    protected static final int REQUEST_CODE_NEW_REPLY_TO_TOPIC = 1090;

    public abstract void loadExternalUrl(String url);
    public abstract void openMedia(String mimeType, String fileName, String url);
    public abstract void replyToEntry(long headerId, DiscussionEntry entry);
    public abstract void replyToTopic(long headerId);

    private static final String ENTRY = "entry";
    private static final String TOPIC = "topic";

    private AdvancedViewFlipper mFlipperFlopper;
    private ProgressBar mProgressBar;
    private Toolbar mToolbar;

    private final Object LOCK = new Object();
    private Handler mHandler;
    private Bundle mLoaderBundle = null;
    private ArrayList<Long> mAboutToMarkRead = new ArrayList<>();

    private CanvasContext mActiveCanvasContext;
    private DiscussionTopicHeader mDiscussionTopicHeader;
    private DiscussionTopic mDiscussionTopic;
    private DiscussionWebView mDiscussionWebView;
    private WebChromeClient mDiscussionWebChromeClient;
    private ContentVideoViewClient mVideoViewClient;
    private DiscussionEntry mCurrentEntry;
    private DiscussionEntry mQueuedSentDiscussionEntry;

    private boolean mLikeInProgressDebounce = false;

    public interface UpdateUnreadListener {
        void updateUnread(long topicID, int unreadCount);
    }

    private UpdateUnreadListener mUpdateUnreadListener;

    @Override
    public int layoutResId() {
        return R.layout.fragment_html_discussions;
    }

    @Override
    public void onCreateView(View view) {
        mFlipperFlopper = (AdvancedViewFlipper) view.findViewById(R.id.view_flipper);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);

        addUpIndicatorToExit(mToolbar);
        themeToolbar(mToolbar);
        mFlipperFlopper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slow_push_left_in));
        mFlipperFlopper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slow_push_left_out));
        mHandler = new Handler();
        mVideoViewClient = new ActivityContentVideoViewClient(getActivity());
        mDiscussionWebChromeClient = new DiscussionChromeClient();
        loadDiscussions();
    }

    @Override
    public void unBundle(@NonNull Bundle extras) {
        super.unBundle(extras);
        mActiveCanvasContext = getCanvasContext();
        setCanvasContext(mActiveCanvasContext);
        mDiscussionTopicHeader = extras.getParcelable(Const.DISCUSSION_HEADER);
    }

    private void loadDiscussions() {
        mToolbar.setTitle(mDiscussionTopicHeader.getTitle());
        setCurrentEntry(mDiscussionTopicHeader.convertToDiscussionEntry(getResources().getString(R.string.gradedDiscussion), getResources().getString(R.string.pointsPossible)));
        mDiscussionTopicHeader.getPermissions().setCanReply(FormatHtmlLoader.canReplyHack(mDiscussionTopicHeader, getCanvasContext()));//TODO: remove when permissions return "reply"
        getFullDiscussionTopic();
    }


    //region Getters & Setters

    public DiscussionTopicHeader getHeader() {
        return mDiscussionTopicHeader;
    }

    public DiscussionTopic getTopic() {
        return mDiscussionTopic;
    }

    public void setTopic(DiscussionTopic topic) {
        mDiscussionTopic = topic;
    }

    public DiscussionEntry getCurrentEntry() {
        return mCurrentEntry;
    }

    public void setCurrentEntry(DiscussionEntry entry) {
        mCurrentEntry = entry;
    }

    public boolean isAnnouncement() {
        return false;
    }

    private CanvasContext getActiveCanvasContext() {
        if (mActiveCanvasContext == null) {
            return getCanvasContext();
        }
        return mActiveCanvasContext;
    }

    //endregion

    //region Lifecycle

    @Override
    public void onPause() {
        super.onPause();
        if (mDiscussionWebView != null) {
            mDiscussionWebView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDiscussionWebView != null) {
            mDiscussionWebView.onResume();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof UpdateUnreadListener) {
            mUpdateUnreadListener = (UpdateUnreadListener)context;
        }
    }

    //endregion

    //region WebViews

    private class DiscussionWebView extends CanvasWebView {

        public DiscussionWebView(Context context) {
            super(context);
            this.setBackgroundColor(Color.WHITE);
            this.getSettings().setJavaScriptEnabled(true);
            this.addJavascriptInterface(new JSInterface(), "accessor");
            // DO NOT use getSettings().setLayoutAlgorithm, it messes up the layout on API 16 ish devices

            this.getSettings().setUseWideViewPort(true);
            this.getSettings().setAllowFileAccess(true);
            this.getSettings().setLoadWithOverviewMode(true);

            this.setWebViewClient(new DiscussionWebViewClient());
            this.setWebChromeClient(mDiscussionWebChromeClient);
            this.setDownloadListener(new DiscussionDownloadListener());

		/*
         * Bug fix for the text box not bring up the keyboard.
		 * http://stackoverflow.com/questions/3460915/webview-textarea-doesnt-pop-up-the-keyboard
		 */
            this.requestFocus(View.FOCUS_DOWN);
            this.setOnTouchListener(new OnTouchListener() {
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
    }

    //Generate the html and load the page before pushing onto the stack.
    private class DiscussionWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            for (int i = 0; i < mFlipperFlopper.getChildCount(); i++) { // workaround: Don't add the same view if its already there (Happens when links/embedded videos are opened) on samsung
                if (mFlipperFlopper.getChildAt(i) == view) {
                    return;
                }
            }
            mFlipperFlopper.addView(view);
            mFlipperFlopper.showNext();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (!url.startsWith("file:///")) {
                loadExternalUrl(url);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    private class DiscussionChromeClient extends WebChromeClient {

        private CustomViewCallback mCallback;

        @Override
        public void onProgressChanged(WebView view, final int newProgress) {
            if (getActivity() == null) return;

            if (newProgress == 100) {  //It's done
                //Stop indeterminate progress circle.
                mProgressBar.setVisibility(View.GONE);
            } else {
                //Start indeterminate progress circle.
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            mCallback = callback;
            mVideoViewClient.onShowCustomView(view);
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            if (mCallback != null) {
                mCallback.onCustomViewHidden();
            }
            mVideoViewClient.onDestroyContentVideoView();
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
                filename = getActiveCanvasContext().toAPIString() + "_" + getHeader().getId() + "_" + filename;
            }
            openMedia(mimetype, filename, url);
        }
    }

    //endregion

    //region JSInterface

    private class JSInterface {

        @JavascriptInterface
        public String getInViewPort() {
            synchronized (LOCK) {
                //Build a comma separated list of all unread ids.
                if (getCurrentEntry() == null) return "";

                String array = "";
                if (getCurrentEntry().isUnread()) {
                    if (array.length() != 0)
                        array += ",";
                    array += (getCurrentEntry().getId());
                }

                for (DiscussionEntry reply : getCurrentEntry().getReplies()) {
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
            if (string.equals("")) return;

            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    String[] temp = (string.split(","));

                    ArrayList<Long> inView = new ArrayList<>();

                    for (String string : temp) {
                        try {
                            inView.add(Long.parseLong(string));
                        } catch (NumberFormatException e) {
                            //do nothing
                        }
                    }

                    ArrayList<Long> markAsReadNOW = new ArrayList<>();

                    while (mAboutToMarkRead.size() > 0) {
                        int indexInView = inView.indexOf(mAboutToMarkRead.get(0));
                        if (indexInView != -1) {
                            //They actually need to be deleted.
                            markAsReadNOW.add(mAboutToMarkRead.get(0));
                            inView.remove(indexInView);
                        }
                        mAboutToMarkRead.remove(0);
                    }

                    //The rest of them are on the chopping block next time.
                    mAboutToMarkRead.addAll(inView);

                    if (markAsReadNOW.size() > 0) {
                        //Start the async task...
                        Long[] asyncTaskMarked = new Long[markAsReadNOW.size()];
                        for (int i = 0; i < markAsReadNOW.size(); i++) {
                            asyncTaskMarked[i] = markAsReadNOW.get(i);
                        }

                        //Check if it's a webview.
                        View v = mFlipperFlopper.getCurrentView();
                        if (v instanceof WebView && NetworkUtils.isNetworkAvailable(v.getContext())) {
                            new MarkAsReadAsyncTask(((WebView) v)).execute(asyncTaskMarked);
                        }
                    }
                }
            });
        }

        @JavascriptInterface
        public void onReplyPressed(final String msg) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;

                    int i = Integer.parseInt(msg);
                    if (i >= 0 && i < getCurrentEntry().getReplies().size()) {
                        final DiscussionEntry entry = getCurrentEntry().getReplies().get(i);
                        replyToEntry(getHeader().getId(), entry);
                    } else if(i == -1) {
                        //Parent
                        replyToTopic(getHeader().getId());
                    }
                }
            });
        }

        @JavascriptInterface
        public void onPressed(final String msg) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (getActivity() == null) return;

                    int i = Integer.parseInt(msg);
                    if (i >= 0 && i < getCurrentEntry().getReplies().size()) {

                        int totalChildren = getCurrentEntry().getReplies().get(i).getReplies().size();

                        if (getHeader().getType() == DiscussionTopicHeader.DiscussionType.SIDE_COMMENT && getCurrentEntry().getDepth() >= 1) {
                            //One level deep for side comments.
                            showToast(R.string.sideCommentEntry);
                        } else if (getCurrentEntry().getReplies().get(i).isDeleted() && totalChildren == 0) {
                            //Can't go into deleted entries with no children.
                            showToast(R.string.deletedEntryNoChildren);
                        } else if(totalChildren == 0) {
                            //Do nothing, no nested comments to view.
                        } else {
                            setCurrentEntry(getCurrentEntry().getReplies().get(i));

                            if (getCurrentEntry().isUnread()) {
                                //Mark as read locally
                                getCurrentEntry().setUnread(false);

                                View v = mFlipperFlopper.getCurrentView();
                                if (v instanceof WebView && NetworkUtils.isNetworkAvailable(v.getContext())) {
                                    new MarkAsReadAsyncTask(((WebView) v)).execute(getCurrentEntry().getId());
                                }
                            }
                            //Generate the HTML
                            mLoaderBundle = createLoaderBundle();
                            mDiscussionWebView = new DiscussionWebView(getActivity());
                            CookieManager.getInstance().setAcceptCookie(true);
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                DiscussionWebView.setWebContentsDebuggingEnabled(DEBUG);
                            }
                            LoaderUtils.restartLoaderWithBundle(getLoaderManager(), mLoaderBundle, BaseDiscussionDetailsFragment.this, R.id.formatLoaderID);
                        }
                    }
                }
            });
        }

        @JavascriptInterface
        public void onLikePressed(final String msg) {
            //TODO: only_graders_can_rate needs to be accounted for when permissions tell is if they are a grader
            mHandler.post(new Runnable() {
                int rating = 0;
                HashMap<Long, Integer> ratingsMap = getTopic().getEntryRatings();

                @Override
                public void run() {
                    if (getActivity() == null || mLikeInProgressDebounce) return;

                    int i = Integer.parseInt(msg);
                    if (i >= 0 && i < getCurrentEntry().getReplies().size()) {
                        final DiscussionEntry entry = getCurrentEntry().getReplies().get(i);

                        //get the current rating
                        if(ratingsMap != null) {
                            if (ratingsMap.containsKey(entry.getId())) {
                                rating = ratingsMap.get(entry.getId());
                            }
                        } else {
                            ratingsMap = new HashMap<>();
                        }

                        //now switch the rating so we pass the correct parameter to the API
                        rating = rating == 1 ? 0 : 1;

                        DiscussionManager.rateDiscussionEntry(getActiveCanvasContext(), getHeader().getId(), entry.getId(), rating, new StatusCallback<Void>(mStatusDelegate){

                            @Override
                            public void onStarted() {
                                mLikeInProgressDebounce = true;
                            }

                            @Override
                            public void onFinished(ApiType type) {
                                if(type == ApiType.API) {
                                    mLikeInProgressDebounce = false;
                                }
                            }

                            @Override
                            public void onResponse(Response<Void> response, LinkHeaders linkHeaders, ApiType type) {
                                super.onResponse(response, linkHeaders, type);
                                if(type == ApiType.API && response != null && response.code() == 204) {
                                    if(rating == 0) {
                                        entry.setRatingSum(entry.getRatingSum() - 1);
                                    } else {
                                        entry.setRatingSum(entry.getRatingSum() + 1);
                                    }

                                    ratingsMap.put(entry.getId(), rating);
                                    getTopic().setEntryRatings(ratingsMap);
                                    String likeString = FormatHtmlLoader.getLikeString(getActivity(), true, entry);
                                    mDiscussionWebView.loadUrl("javascript:updateLikeNums('" + entry.getId() + "','" + likeString + "')");
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    //endregion

    //region Loader

    @Override
    public Loader<FormatHtmlObject> onCreateLoader(int id, Bundle args) {
        return new FormatHtmlLoader(getContext(), getActiveCanvasContext(), (DiscussionEntry)args.getParcelable(ENTRY), (DiscussionTopic)args.getParcelable(TOPIC), (DiscussionTopicHeader)args.getParcelable(Const.DISCUSSION_HEADER), isAnnouncement());
    }

    @Override
    public void onLoadFinished(Loader<FormatHtmlObject> loader, FormatHtmlObject data) {
        if (mDiscussionWebView != null) {
            // BaseURL is set as Referer. Referer needed for some vimeo videos
            mDiscussionWebView.loadDataWithBaseURL(CanvasWebView.getRefererDomain(getContext()), data.html, "text/html", "utf-8", null);
        }
        mLoaderBundle = null;
    }

    @Override
    public void onLoaderReset(Loader<FormatHtmlObject> loader) {}

    private Bundle createLoaderBundle() {
        Bundle loaderBundle = new Bundle();
        loaderBundle.putParcelable(ENTRY, getCurrentEntry());
        loaderBundle.putParcelable(TOPIC, getTopic());
        loaderBundle.putParcelable(Const.DISCUSSION_HEADER, getHeader());
        return loaderBundle;
    }

    //endregion

    //region Adding Entries

    public void addDiscussionEntryToCurrent(DiscussionEntry discussionEntry) {
        getCurrentEntry().addReply(discussionEntry);
        getHeader().setDiscussionSubentryCount(getHeader().getDiscussionSubentryCount() + 1);
        discussionEntry.setParent(getCurrentEntry());

        HashMap<Long, DiscussionParticipant> participantHashMap = getTopic().getParticipantsMap();
        DiscussionParticipant discussionParticipant = participantHashMap.get(discussionEntry.getUserId());
        if (discussionParticipant != null) {
            discussionEntry.setAuthor(discussionParticipant);
        } else {
            //If we didn't find an author, it must be us.
            //If this is our first post, we won't be in the author's list.
            User user = APIHelper.getCacheUser(getContext());
            DiscussionParticipant dp = new DiscussionParticipant(user.getId());
            dp.setAvatarImageUrl(user.getAvatarUrl());
            dp.setDisplayName(user.getName());
            dp.setHtmlUrl("");
            discussionEntry.setAuthor(dp);
        }
    }

    public void addDiscussionEntryToEntry(DiscussionEntry parentEntry, DiscussionEntry toAdd) {
        toAdd.setParent(parentEntry);
        getCurrentEntry().addInnerReply(parentEntry, toAdd);

        HashMap<Long, DiscussionParticipant> participantHashMap = getTopic().getParticipantsMap();
        DiscussionParticipant discussionParticipant = participantHashMap.get(toAdd.getUserId());
        if (discussionParticipant != null) {
            toAdd.setAuthor(discussionParticipant);
        } else {
            //If we didn't find an author, it must be us.
            //If this is our first post, we won't be in the author's list.
            User user = APIHelper.getCacheUser(getContext());
            DiscussionParticipant dp = new DiscussionParticipant(user.getId());
            dp.setAvatarImageUrl(user.getAvatarUrl());
            dp.setDisplayName(user.getName());
            dp.setHtmlUrl("");
            toAdd.setAuthor(dp);
        }
    }

    private void addSentDiscussionEntry(final DiscussionEntry discussionEntry) {
        if (!getTopic().isForbidden()) {

            addDiscussionEntryToCurrent(discussionEntry);

            HashMap<Long, Integer> ratingsMap = getTopic().getEntryRatings();
            int rating = 0;
            if(ratingsMap != null) {
                if (ratingsMap.containsKey(discussionEntry.getId())) {
                    rating = ratingsMap.get(discussionEntry.getId());
                }
            }

            final boolean shouldAllowLiking = FormatHtmlLoader.shouldAllowLiking(getActiveCanvasContext(), getHeader()) && !isAnnouncement();
            final String colorString = CanvasContextColor.getColorStringFromInt(ThemeUtils.getAccent(getContext()), true);
            final boolean canReply = FormatHtmlLoader.canReplyHack(getHeader(), getActiveCanvasContext());

            String html = DiscussionEntryHTMLHelper.getHTML(
                    discussionEntry, getContext(),
                    getCurrentEntry().getReplies().size() - 1,
                    getString(R.string.deleted),
                    colorString, shouldAllowLiking,
                    canReply, rating,
                    FormatHtmlLoader.getLikeString(getActivity(), shouldAllowLiking, discussionEntry),
                    discussionEntry.getTotalChildren());

            html = html.replace("'", "\\'").replace("\"", "\\\"");
            if (mFlipperFlopper.getCurrentView() instanceof WebView) {
                WebView web = (WebView) mFlipperFlopper.getCurrentView();
                //Adds the new entry
                web.loadUrl("javascript:appendHTML('" + html + "')");
                //Updates the topic comment count
                updateCommentCountForItem(getCurrentEntry(), getHeader().getDiscussionSubentryCount());
            }
        }
    }

    private void updateCommentCountForItem(DiscussionEntry entry, int commentCount) {
        if (mFlipperFlopper.getCurrentView() instanceof WebView) {
            WebView web = (WebView) mFlipperFlopper.getCurrentView();
            //Updates the comment count
            web.loadUrl("javascript:updateCommentsCount('" + entry.getId() + "','" + DiscussionEntryHTMLHelper.getCommentsCountHtmlLabel(getContext(), entry, commentCount) + "')");
        }
    }

    //endregion

    //region Mark as Read

    private class MarkAsReadAsyncTask extends AsyncTask<Long, Void, ArrayList<Long>> {

        WebView webView;

        public MarkAsReadAsyncTask(WebView webview) {
            this.webView = webview;
        }

        @Override
        protected ArrayList<Long> doInBackground(Long... params) {
            if(getContext() == null) return new ArrayList<>();

            final ArrayList<Long> successful = new ArrayList<>(params.length);
            for (final Long id : params) {
                if(id == 0) {
                    Response<Void> response = DiscussionManager.markAllDiscussionTopicEntriesReadSynchronously(getActiveCanvasContext(), getHeader().getId());
                    if(response != null && response.code() == 204) {
                        successful.add(id);
                    }
                } else {
                    Response<Void> response = DiscussionManager.markDiscussionTopicEntryReadSynchronously(getActiveCanvasContext(), getHeader().getId(), id);
                    if(response != null && response.code() == 204) {
                        successful.add(id);
                    }
                }
            }

            return successful;
        }

        @Override
        protected void onPostExecute(ArrayList<Long> successful) {
            if (getContext() == null || successful.isEmpty()) return;

            DiscussionEntry entry = getCurrentEntry();
            while (entry.getParent() != null) {
                entry = entry.getParent();
            }

            markUnreadRecursively(entry, successful, false);

            if (webView != null) {
                for (int i = 0; i < successful.size(); i++) {
                    webView.loadUrl("javascript:markAsRead('" + successful.get(i) + "')", APIHelper.getReferrer(getContext()));
                }
            }

            //Let the list know that the unread count has changed.
            if (mUpdateUnreadListener != null) {
                mUpdateUnreadListener.updateUnread(mDiscussionTopicHeader.getId(), entry.getUnreadChildren());
            }
        }
    }

    private void markUnreadRecursively(DiscussionEntry current, List<Long> ids, boolean isUnread) {
        if (ids.contains(current.getId())) {
            current.setUnread(isUnread);
        }

        for (DiscussionEntry reply : current.getReplies()) {
            markUnreadRecursively(reply, ids, isUnread);
        }
    }

    //endregion

    //region Discussion Topic & Entry

    private void getFullDiscussionTopic() {
        DiscussionManager.getFullDiscussionTopic(getActiveCanvasContext(), mDiscussionTopicHeader.getId(), new StatusCallback<DiscussionTopic>(mStatusDelegate) {

            @Override
            public void onResponse(Response<DiscussionTopic> response, LinkHeaders linkHeaders, ApiType type) {
                if(response.code() == 403) {
                    DiscussionTopic topic = new DiscussionTopic();
                    topic.setForbidden(true);
                    setTopic(topic);
                    loadResultsIntoWebView();
                    return;
                }

                setTopic(response.body());

                getCurrentEntry().setReplies(new ArrayList<DiscussionEntry>());

                //init it all up.
                for (DiscussionEntry discussionEntry : getTopic().getViews()) {
                    discussionEntry.init(getTopic(), getCurrentEntry());
                    getCurrentEntry().addReply(discussionEntry);
                }

                //It's possible they sent a new message, but it's not available via API yet.
                //Discussions use cached data not necessarily live data.
                if (mQueuedSentDiscussionEntry != null) {

                    boolean hasMatchedID = false;

                    //See if we already have the message in the replies list.
                    if (getCurrentEntry().getReplies() != null) {
                        for (DiscussionEntry reply : getCurrentEntry().getReplies()) {
                            if (reply.getId() == mQueuedSentDiscussionEntry.getId()) {
                                hasMatchedID = true;
                                break;
                            }
                        }
                    }

                    //If none of the replies matched our queued entry, add it.
                    if (!hasMatchedID) {
                        addDiscussionEntryToCurrent(mQueuedSentDiscussionEntry);
                    }

                    //It no longer needs to be cached.
                    mQueuedSentDiscussionEntry = null;
                }

                int totalChildren = 0;
                int unreadChildren = 0;

                for (DiscussionEntry reply : getCurrentEntry().getReplies()) {
                    totalChildren += reply.getTotalChildren() + 1;
                    unreadChildren += reply.getUnreadChildren();

                    if (reply.isUnread()) {
                        unreadChildren++;
                    }
                }

                getCurrentEntry().setTotalChildren(totalChildren);
                getCurrentEntry().setUnreadChildren(unreadChildren);

                loadResultsIntoWebView();
            }

            private void loadResultsIntoWebView() {
                mLoaderBundle = createLoaderBundle();
                mDiscussionWebView = new DiscussionWebView(getActivity());
                CookieManager.getInstance().setAcceptCookie(true);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    DiscussionWebView.setWebContentsDebuggingEnabled(DEBUG);
                }
                LoaderUtils.restartLoaderWithBundle(getLoaderManager(), mLoaderBundle, BaseDiscussionDetailsFragment.this, R.id.formatLoaderID);
            }
        });
    }

    //endregion

    //region Back Press

    @Override
    public boolean onHandleBackPressed() {
        if (mVideoViewClient.isFullscreen()) {
            mDiscussionWebChromeClient.onHideCustomView();
            return true;
        }

        //it's a local webview. pop off the view stack.
        if (getCurrentEntry() != null && getCurrentEntry().getParent() != null) {


            //we aren't at the top.
            //Show previous. Then delete the final one.
            setCurrentEntry(getCurrentEntry().getParent());

            View current = mFlipperFlopper.getCurrentView();

            mFlipperFlopper.showPrevious(
                    AnimationUtils.loadAnimation(getActivity(), R.anim.slow_push_right_in),
                    AnimationUtils.loadAnimation(getActivity(), R.anim.slow_push_right_out));

            mFlipperFlopper.removeView(current);

            //get the curreunt displayed view and update the liked/unliked count and states
            View v = mFlipperFlopper.getCurrentView();

            if (v instanceof WebView) {
                HashMap<Long, Integer> ratingsMap = getTopic().getEntryRatings();

                mDiscussionWebView = (DiscussionWebView)v;
                int rating = 0;
                if(ratingsMap != null) {
                    if (ratingsMap.containsKey(getCurrentEntry().getId())) {
                        rating = ratingsMap.get(getCurrentEntry().getId());
                        if(rating == 0) {
                            mDiscussionWebView.loadUrl("javascript:setUnliked('" + getCurrentEntry().getId() + "')");
                        } else {
                            mDiscussionWebView.loadUrl("javascript:setLiked('" + getCurrentEntry().getId() + "')");
                        }
                        if(!isAnnouncement() && FormatHtmlLoader.shouldAllowLiking(getCanvasContext(), getHeader())) {
                            mDiscussionWebView.loadUrl("javascript:updateLikeNums('" + getCurrentEntry().getId() + "','" + FormatHtmlLoader.getLikeString(getActivity(), true, getCurrentEntry()) + "')");
                        }
                    }
                }

                //Now update the children.
                for (int i = 0; i < getCurrentEntry().getReplies().size(); i++) {
                    if (ratingsMap != null) {
                        if (ratingsMap.containsKey(getCurrentEntry().getReplies().get(i).getId())) {
                            rating = ratingsMap.get(getCurrentEntry().getReplies().get(i).getId());
                            if(rating == 0) {
                                mDiscussionWebView.loadUrl("javascript:setUnliked('" + getCurrentEntry().getReplies().get(i).getId() + "')");
                            } else {
                                mDiscussionWebView.loadUrl("javascript:setLiked('" + getCurrentEntry().getReplies().get(i).getId() + "')");
                            }
                            if(!isAnnouncement() && FormatHtmlLoader.shouldAllowLiking(getCanvasContext(), getHeader())) {
                                mDiscussionWebView.loadUrl("javascript:updateLikeNums('" + getCurrentEntry().getReplies().get(i).getId() + "','" + FormatHtmlLoader.getLikeString(getActivity(), true, getCurrentEntry().getReplies().get(i)) + "')");
                            }
                        }
                    }
                }
            }
            return true;

        } else {
            //it's a local webview and we're showing the root.
            return super.onHandleBackPressed();
        }
    }

    //endregion

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Message was successful, add entry to webview
        if(REQUEST_CODE_NEW_REPLY_TO_ENTRY == requestCode && resultCode == Activity.RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            DiscussionEntry newEntry = extras.getParcelable(Const.ITEM);
            if(newEntry == null) return;

            if(extras.containsKey(Const.DISCUSSION_ENTRY)) {
                DiscussionEntry wasAddedTooEntry = extras.getParcelable(Const.DISCUSSION_ENTRY);
                if(wasAddedTooEntry == null) return;

                //If we reply to an entry we update the Comments count
                addDiscussionEntryToEntry(wasAddedTooEntry, newEntry);
                updateCommentCountForItem(wasAddedTooEntry, wasAddedTooEntry.getReplies().size() + 1);
            }
        } else if(REQUEST_CODE_NEW_REPLY_TO_TOPIC == requestCode && resultCode == Activity.RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            DiscussionEntry newEntry = extras.getParcelable(Const.ITEM);
            addSentDiscussionEntry(newEntry);
        }
    }
}

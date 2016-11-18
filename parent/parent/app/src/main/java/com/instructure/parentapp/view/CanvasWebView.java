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

package com.instructure.parentapp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.parentapp.util.StringUtilities;
import com.instructure.parentapp.video.ContentVideoViewClient;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class CanvasWebView extends WebView {

    private final String encoding = "UTF-8";

    public interface CanvasWebViewClientCallback {
        void openMediaFromWebView(String mime, String url, String filename);
        void onPageStartedCallback(WebView webView, String url);
        void onPageFinishedCallback(WebView webView, String url);
        void routeInternallyCallback(String url);
        boolean canRouteInternallyDelegate(String url);
        String studentDomainReferrer();
    }

    public interface CanvasEmbeddedWebViewCallback {
        boolean shouldLaunchInternalWebViewFragment(String url);
        void launchInternalWebViewFragment(String url);
    }

    public interface CanvasWebChromeClientCallback {
        void onProgressChangedCallback(WebView view, final int newProgress);
    }

    private CanvasWebViewClientCallback mCanvasWebViewClientCallback;
    private CanvasEmbeddedWebViewCallback mCanvasEmbeddedWebViewCallback;
    private CanvasWebChromeClientCallback mCanvasWebChromeClientCallback;

    private Context mContext;
    private ContentVideoViewClient mClient;
    private WebChromeClient mWebChromeClient;

    public CanvasWebView(Context context) {
        super(context);
        init(context);
    }

    public CanvasWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CanvasWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setBuiltInZoomControls(true);
        // Hide the zoom controls
        this.getSettings().setDisplayZoomControls(false);

        this.getSettings().setUseWideViewPort(true);
        this.setWebViewClient(new CanvasWebViewClient());

        mWebChromeClient = new CanvasWebChromeClient();
        this.setWebChromeClient(mWebChromeClient);

        this.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                //get the filename
                String filename = "file" + contentLength;
                if (contentDisposition != null) {
                    String temp = "filename=";
                    int index = contentDisposition.indexOf(temp) + temp.length();

                    if (index > -1) {
                        int end = contentDisposition.indexOf(";", index);
                        if (end > -1) {
                            //+1 and -1 to remove the quotes
                            filename = contentDisposition.substring(index + 1, end - 1);
                        }
                        //make the filename unique
                        filename = String.format("%s_%d", filename, url.hashCode());
                    }

                    if (mCanvasWebViewClientCallback != null) {
                        mCanvasWebViewClientCallback.openMediaFromWebView(mimetype, url, filename);
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        // Calling onPause will stop Video's sound, but onResume must be called if resumed, otherwise the second time onPause is called it won't work
        try {
            super.onPause();
        } catch (NullPointerException e) {
            // Catch for API 16 devices (and perhaps others) and webkit
        } catch (Exception E) {
            //do nothing
        }
    }

    /**
     * Handles back presses for the CanvasWebView and the lifecycle of the ActivityContentVideoViewClient
     *
     * Use instead of goBack and canGoBack
     *
     * @return true if handled; false otherwise
     */
    public boolean handleGoBack() {
        if (mClient.isFullscreen()) {
            mWebChromeClient.onHideCustomView();
            return true;
        } else if (super.canGoBack()) {
            super.goBack();
            return true;
        }
        return false;
    }


    public String getRefererDomain() {
        // Mainly for embedded content such as vimeo, youtube, video tags, iframes, etc
        return mCanvasWebViewClientCallback.studentDomainReferrer();
    }

    public Map<String, String> getReferer(){
        Map<String, String> extraHeaders = new HashMap<>();
        if(mCanvasWebViewClientCallback != null) {
            extraHeaders.put("Referer", mCanvasWebViewClientCallback.studentDomainReferrer());
        }
        return extraHeaders;
    }

    public static String applyWorkAroundForDoubleSlashesAsUrlSource(String html) {
        // Fix for embedded videos that have // instead of http://
        html = html.replaceAll("href=\"//", "href=\"http://");
        html = html.replaceAll("href='//", "href='http://");
        html = html.replaceAll("src=\"//", "src=\"http://");
        html = html.replaceAll("src='//", "src='http://");
        return html;
    }


    public class CanvasWebChromeClient extends WebChromeClient {
        private CustomViewCallback mCallback;
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);
            mCallback = callback;
            mClient.onShowCustomView(view);
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            if (mCallback != null) {
                mCallback.onCustomViewHidden();
            }
            mClient.onDestroyContentVideoView();
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }


    }

    public class CanvasWebViewClient extends WebViewClient {

        public CanvasWebViewClient() {}

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            //check to see if we need to do anything with the link that was clicked

            if (mCanvasWebViewClientCallback != null) {
                //Is the URL something we can link to inside our application?
                if (mCanvasWebViewClientCallback.canRouteInternallyDelegate(url)) {
                    mCanvasWebViewClientCallback.routeInternallyCallback(url);
                    return true;
                }
            }

            // Handle the embedded webview case (Its not within the InternalWebViewFragment)
            if (mCanvasEmbeddedWebViewCallback != null && mCanvasEmbeddedWebViewCallback.shouldLaunchInternalWebViewFragment(url)) {
                String contentTypeGuess = URLConnection.guessContentTypeFromName(url);
                // null when type can't be determined, launchInternalWebView anyway
                // When contentType has 'application', it typically means it's a pdf or some type of document that needs to be downloaded,
                //   so allow the embedded webview to open the url, which will trigger the DownloadListener. If for some reason the content can
                //   be loaded in the webview, the content will just load in the embedded webview (which isn't ideal, but in majority of cases it won't happen).
                if (contentTypeGuess == null || (contentTypeGuess != null && !contentTypeGuess.contains("application"))) {
                    mCanvasEmbeddedWebViewCallback.launchInternalWebViewFragment(url);
                    return true;
                }
            }

            view.loadUrl(url, getReferer());
            //we're handling the url ourselves, so return true.
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (mCanvasWebViewClientCallback != null) {
                mCanvasWebViewClientCallback.onPageStartedCallback(view, url);
            }
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            // Clear the history if formatHtml was called more than once. Refer to formatHtml's NOTE
            if (url.startsWith(getHtmlAsUrl("", encoding))) {
                view.clearHistory();
            }
            super.doUpdateVisitedHistory(view, url, isReload);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (mCanvasWebViewClientCallback != null) {
                mCanvasWebViewClientCallback.onPageFinishedCallback(view, url);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            if (failingUrl != null && failingUrl.startsWith("file://")) {
                failingUrl = failingUrl.replaceFirst("file://", "https://");
                view.loadUrl(failingUrl, Utils.getReferer(getContext()));
            }
        }
    }
    @Override
    public void loadUrl(String url) {
        super.loadUrl(url);
    }


    @Override
    public void loadData(String data, String mimeType, String encoding) {
        super.loadData(data, mimeType, encoding);
    }

    /**
     * Makes html content somewhat suitable for mobile
     *
     * NOTE: The web history is cleared when formatHtml is called. Only the loaded page will appear in the webView.copyBackForwardList()
     *       Back history will not work with multiple pages. This allows for formatHtml to be called several times without causing the user to
     *          press back 2 or 3 times.
     *
     * @param content
     * @param title
     * @return
     */
    public String formatHTML(String content, String title) {
        String html = APIHelper.getAssetsFile(getContext(), "html_wrapper.html");

        content = CanvasWebView.applyWorkAroundForDoubleSlashesAsUrlSource(content);

        String result = html.replace("{$CONTENT$}", content);

        // BaseURL is set as Referer. Referer needed for some vimeo videos to play
        this.loadDataWithBaseURL(getRefererDomain(), result, "text/html", encoding, getHtmlAsUrl(result, encoding));

        setupAccessibilityContentDescription(result, title);

        return result;
    }

    /*
     *  Work around for API 16 devices (and perhaps others). When pressing back the webview was loading 'about:blank' instead of the custom html
     */
    private String getHtmlAsUrl(String html, String encoding) {
        return String.format("data:text/html; charset=%s, %s", encoding, html);

    }

    private void setupAccessibilityContentDescription(String formattedHtml, String title) {
        //Remove all html tags and set content description for accessibility
        // call toString on fromHTML because certain Spanned objects can cause this to crash
        String contentDescription = formattedHtml;
        if (title != null) {
            contentDescription = title + " " + formattedHtml;
        }
        this.setContentDescription(StringUtilities.simplifyHTML(Html.fromHtml(contentDescription)));
    }

    // region Getter & Setters

    public CanvasEmbeddedWebViewCallback getCanvasEmbeddedWebViewCallback() {
        return mCanvasEmbeddedWebViewCallback;
    }

    public void setCanvasEmbeddedWebViewCallback(CanvasEmbeddedWebViewCallback mCanvasEmbeddedWebViewCallback) {
        this.mCanvasEmbeddedWebViewCallback = mCanvasEmbeddedWebViewCallback;
    }

    public CanvasWebViewClientCallback getCanvasWebViewClientCallback() {
        return mCanvasWebViewClientCallback;
    }

    public void setCanvasWebViewClientCallback(CanvasWebViewClientCallback canvasWebViewClientCallback) {
        this.mCanvasWebViewClientCallback = canvasWebViewClientCallback;
    }

    public CanvasWebChromeClientCallback getCanvasWebChromeClientCallback() {
        return mCanvasWebChromeClientCallback;
    }

    public void setCanvasWebChromeClientCallback(CanvasWebChromeClientCallback mCanvasWebChromeClientCallback) {
        this.mCanvasWebChromeClientCallback = mCanvasWebChromeClientCallback;
    }

    public ContentVideoViewClient getClient() {
        return mClient;
    }

    public void setClient(ContentVideoViewClient mClient) {
        this.mClient = mClient;
    }

    // endregion
}


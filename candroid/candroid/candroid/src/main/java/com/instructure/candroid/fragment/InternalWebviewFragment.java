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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.google.android.gms.common.api.Api;
import com.instructure.candroid.R;
import com.instructure.candroid.activity.InternalWebViewActivity;
import com.instructure.candroid.api.CanvasAPI;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.view.CanvasLoading;
import com.instructure.canvasapi.api.compatibility_synchronous.APIHttpResponse;
import com.instructure.canvasapi.api.compatibility_synchronous.HttpHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.Masquerading;
import com.instructure.pandautils.utils.Const;
import com.instructure.candroid.util.FragUtils;
import com.instructure.candroid.util.RouterUtils;
import com.instructure.candroid.view.CanvasWebView;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.loginapi.login.util.Utils;
import com.video.ActivityContentVideoViewClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InternalWebviewFragment extends ParentFragment {

    private String url;
    private String actionBarTitle;
    private String html;
    public boolean authenticate;
    private boolean shouldRouteInternally = true;
    private boolean isUnsupportedFeature;
    private boolean shouldLoadUrl = true;
    private boolean isLTITool = false;

    protected CanvasWebView canvasWebView;
    protected CanvasLoading canvasLoading;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        if(getActivity() instanceof InternalWebViewActivity) {
            return FRAGMENT_PLACEMENT.MASTER;
        }
        return FRAGMENT_PLACEMENT.DETAIL;
    }

    @Override
    public String getFragmentTitle() {
        return actionBarTitle;
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        return actionBarTitle;
    }

    public void setShouldRouteInternally(boolean shouldRouteInternally) {
        this.shouldRouteInternally = shouldRouteInternally;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onPause() {
        super.onPause();
        if (canvasWebView != null) {
            canvasWebView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (canvasWebView != null) {
            canvasWebView.onResume();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // notify that we have action bar items
        setRetainInstance(this, true);

        if(getArguments().containsKey(Const.IS_EXTERNAL_TOOL)) {
            isLTITool = getArguments().getBoolean(Const.IS_EXTERNAL_TOOL);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (canvasWebView != null) {
            canvasWebView.saveState(outState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.webview_fragment, container, false);
        canvasLoading = (CanvasLoading)view.findViewById(R.id.loading);

        canvasWebView = (CanvasWebView)view.findViewById(R.id.internal_webview);
        canvasWebView.getSettings().setLoadWithOverviewMode(true);
        canvasWebView.getSettings().setDisplayZoomControls(false);
        canvasWebView.getSettings().setSupportZoom(true);
        canvasWebView.setClient(new ActivityContentVideoViewClient(getActivity()));

        canvasWebView.setCanvasWebViewClientCallback(new CanvasWebView.CanvasWebViewClientCallback() {
            @Override
            public void openMediaFromWebView(String mime, String url, String filename) {
                openMedia(mime, url, filename);
            }

            @Override
            public void onPageFinishedCallback(WebView webView, String url) {
                canvasLoading.setVisibility(View.GONE);
                hideProgressBar();
            }

            @Override
            public void onPageStartedCallback(WebView webView, String url) {
                canvasLoading.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean canRouteInternallyDelegate(String url) {
                return shouldRouteInternally && !isUnsupportedFeature && RouterUtils.canRouteInternally(getActivity(), url, APIHelpers.getDomain(getActivity()), false);
            }

            @Override
            public void routeInternallyCallback(String url) {
                RouterUtils.canRouteInternally(getActivity(), url, APIHelpers.getDomain(getActivity()), true);
            }
        });

        if (canvasWebView != null) {
            canvasWebView.restoreState(savedInstanceState);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (shouldLoadUrl) {
            loadUrl(url);
        }
    }

    @Override
    public boolean handleBackPressed() {
        if(canvasWebView != null) {
            return canvasWebView.handleGoBack();
        }
        return false;
    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {
        if (canvasLoading != null) {
            canvasLoading.displayNoConnection(false);
        }
        super.onCallbackStarted();
    }

    @Override
    public void onNoNetwork() {
        super.onNoNetwork();
        if (canvasLoading != null) {
            canvasLoading.displayNoConnection(true);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Logic
    ///////////////////////////////////////////////////////////////////////////
    public boolean isShouldLoadUrl() {
        return shouldLoadUrl;
    }

    public void setShouldLoadUrl(boolean shouldLoadUrl) {
        this.shouldLoadUrl = shouldLoadUrl;
    }

    protected void loadUrl(String url) {
        if(html != null){
            loadHtml(html);
            return;
        }
        this.url = url;
        if(!TextUtils.isEmpty(url)) {
            if (authenticate) {
                new LoadUrlKindaSafely(url).execute();
            } else {
                if (isAdded()) {
                    canvasWebView.loadUrl(url, Utils.getReferer(getContext()));
                } else {
                    showToast(R.string.errorOccurred);
                }
            }
        }
    }

    public void populateWebView(String content) {
        populateWebView(content, null);
    }

    public void populateWebView(String content, String title) {
        canvasWebView.formatHTML(content, title);
    }

    public void loadHtml(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        // BaseURL is set as Referer. Referer needed for some vimeo videos to play
        canvasWebView.loadDataWithBaseURL(APIHelpers.getFullDomain(getContext()), data, mimeType, encoding, historyUrl);
    }

    public void loadHtml(String html) {
        // BaseURL is set as Referer. Referer needed for some vimeo videos to play
        canvasWebView.loadDataWithBaseURL(APIHelpers.getFullDomain(getContext()), APIHelpers.getAssetsFile(getContext(), "html_text_submission_wrapper.html").replace("{$CONTENT$}", html), "text/html", "UTF-8", null);
    }

    ///////////////////////////////////////////////////////////////////////////
    // AsyncTask
    ///////////////////////////////////////////////////////////////////////////

    private class LoadUrlKindaSafely extends AsyncTask<Void, Void, APIHttpResponse> {

        private String urlString = "";
        private boolean isUnauthorized = false;

        public LoadUrlKindaSafely(String urlString) {
            this.urlString = urlString;
            this.isUnauthorized = false;
        }

        @Override
        protected APIHttpResponse doInBackground(Void... params) {

            try {
                String html = HttpHelpers.getHtml(urlString);
                if(!TextUtils.isEmpty(html) && html.contains("\"status\":\"unauthenticated\"")) {
                    //if the url is a redirect url from a module item, we need to set the headers so it will be authenticated
                    //This is because some module items have a completion requirement (like must view item) and canvas needs to
                    //know who looked at it.
                    isUnauthorized = true;
                }
                return HttpHelpers.externalHttpGet(getContext(), urlString, false);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(APIHttpResponse response) {
            if(getActivity() == null || response == null){return;}

            if (response.responseCode > 401 || isUnauthorized) {
                canvasWebView.loadUrl(url, CanvasAPI.getAuthenticatedURL(getActivity()));
            } else {
                if(Masquerading.isMasquerading(getActivity())) {
                    canvasWebView.loadUrl(urlString, Utils.getReferer(getContext()));
                } else {
                    //If the url we are loading is the same domain lets try to add Authentication and referrer headers and load in a borderless way
                    String domain = APIHelpers.getDomain(getActivity());
                    Uri uri = Uri.parse(urlString).buildUpon().appendQueryParameter("display", "borderless").build();
                    if (uri.getHost().equals(domain)) {
                        //add authentication headers...
                        Map<String, String> authHeaders = Utils.getRefererAndAuthentication(getContext());
                        canvasWebView.loadUrl(uri.toString(), authHeaders);
                    } else {
                        //IF not same domain load the url as we normally do
                        canvasWebView.loadUrl(urlString, Utils.getReferer(getContext()));
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
        if(extras == null) { return; }

        url = extras.getString(Const.INTERNAL_URL);
        actionBarTitle = extras.getString(Const.ACTION_BAR_TITLE);
        authenticate = extras.getBoolean(Const.AUTHENTICATE);
        isUnsupportedFeature = extras.getBoolean(Const.IS_UNSUPPORTED_FEATURE);
        html = extras.getString(Const.HTML);
    }

    public static Bundle createDefaultBundle(CanvasContext canvasContext){
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.INTERNAL_URL, "https://play.google.com/store/apps/details?id=com.instructure.candroid");
        return extras;
    }

    /*
     * Do not use this method if the InternalWebViewFragment has the ActionBar DropDownMenu visable,
     * Otherwise the canvasContext won't be saved and will cause issues with the dropdown navigation
     * -dw
     */
    public static Bundle createBundle(String url, String title, boolean authenticate, String html) {
        Bundle extras = createBundle(CanvasContext.emptyUserContext());
        extras.putString(Const.INTERNAL_URL, url);
        extras.putString(Const.ACTION_BAR_TITLE, title);
        extras.putBoolean(Const.AUTHENTICATE, authenticate);
        extras.putString(Const.HTML, html);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url, String title, boolean authenticate, String html) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.INTERNAL_URL, url);
        extras.putString(Const.ACTION_BAR_TITLE, title);
        extras.putBoolean(Const.AUTHENTICATE, authenticate);
        extras.putString(Const.HTML, html);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url, String title, boolean authenticate, boolean isUnsupportedFeature) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.INTERNAL_URL, url);
        extras.putString(Const.ACTION_BAR_TITLE, title);
        extras.putBoolean(Const.AUTHENTICATE, authenticate);
        extras.putBoolean(Const.IS_UNSUPPORTED_FEATURE, isUnsupportedFeature);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url, String title, boolean authenticate, boolean isUnsupportedFeature, boolean isLTITool) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.INTERNAL_URL, url);
        extras.putString(Const.ACTION_BAR_TITLE, title);
        extras.putBoolean(Const.AUTHENTICATE, authenticate);
        extras.putBoolean(Const.IS_UNSUPPORTED_FEATURE, isUnsupportedFeature);
        extras.putBoolean(Const.IS_EXTERNAL_TOOL, isLTITool);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url, String title, boolean authenticate) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.INTERNAL_URL, url);
        extras.putBoolean(Const.AUTHENTICATE, authenticate);
        extras.putString(Const.ACTION_BAR_TITLE, title);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url, boolean authenticate) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.INTERNAL_URL, url);
        extras.putBoolean(Const.AUTHENTICATE, authenticate);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url, boolean authenticate, boolean isUnsupportedFeature) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.INTERNAL_URL, url);
        extras.putBoolean(Const.AUTHENTICATE, authenticate);
        extras.putBoolean(Const.IS_UNSUPPORTED_FEATURE, isUnsupportedFeature);
        return extras;
    }

    public static Bundle createBundleHTML(CanvasContext canvasContext, String html){
        return createBundle(canvasContext, null, null, false, html);
    }

    public static void loadInternalWebView(FragmentActivity activity, Navigation navigation, Bundle bundle) {
        if(activity == null || navigation == null) {
            Utils.e("loadInternalWebView could not complete, activity or navigation null");
            return;
        }

        navigation.addFragment(FragUtils.getFrag(InternalWebviewFragment.class, bundle));
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }

    protected boolean isLTITool() {
        return this.isLTITool;
    }
}

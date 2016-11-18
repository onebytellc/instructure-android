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

package com.instructure.parentapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.utils.Const;
import com.instructure.parentapp.R;
import com.instructure.parentapp.util.RouterUtils;
import com.instructure.parentapp.video.ActivityContentVideoViewClient;
import com.instructure.parentapp.view.CanvasWebView;

/**
 * Copyright (c) 2014 Instructure. All rights reserved.
 */
public class InternalWebviewFragment extends ParentFragment {

    private String mUrl;
    private String mHtml;
    private CanvasWebView mCanvasWebView;
    private Student mStudent;

    @Override
    protected int getRootLayout() {
        return R.layout.webview_fragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCanvasWebView != null) {
            mCanvasWebView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCanvasWebView != null) {
            mCanvasWebView.onResume();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        handleIntentExtras(getArguments());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCanvasWebView != null) {
            mCanvasWebView.saveState(outState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(getRootLayout(), container, false);

        mCanvasWebView = (CanvasWebView)view.findViewById(R.id.internal_webview);
        mCanvasWebView.getSettings().setLoadWithOverviewMode(true);
        mCanvasWebView.getSettings().setDisplayZoomControls(false);
        mCanvasWebView.getSettings().setSupportZoom(true);
        mCanvasWebView.setClient(new ActivityContentVideoViewClient(getActivity()));

        if(mStudent != null) {
            mCanvasWebView.setCanvasWebViewClientCallback(new CanvasWebView.CanvasWebViewClientCallback() {
                @Override
                public void openMediaFromWebView(String mime, String url, String filename) {
                }

                @Override
                public void onPageStartedCallback(WebView webView, String url) {
                }

                @Override
                public void onPageFinishedCallback(WebView webView, String url) {

                }

                @Override
                public boolean canRouteInternallyDelegate(String url) {
                    return RouterUtils.canRouteInternally(null, url, mStudent, studentDomainReferrer(), false);
                }

                @Override
                public void routeInternallyCallback(String url) {
                    RouterUtils.canRouteInternally(getActivity(), url, mStudent, studentDomainReferrer(), true);
                }

                @Override
                public String studentDomainReferrer() {
                    return mStudent.getStudentDomain();
                }
            });
        }

        if (mCanvasWebView != null) {
            mCanvasWebView.restoreState(savedInstanceState);
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadUrl(mUrl);
    }

    protected void loadUrl(String url) {
        if(mHtml != null){
            loadHtml(mHtml);
            return;
        }
        this.mUrl = url;
        if(!TextUtils.isEmpty(url)) {
            mCanvasWebView.loadUrl(url, Utils.getReferer(getContext()));
        }
    }


    public void loadHtml(String html) {
        // BaseURL is set as Referer. Referer needed for some vimeo videos to play
        mCanvasWebView.loadDataWithBaseURL(APIHelper.getFullDomain(getContext()),
                APIHelper.getAssetsFile(getContext(), "html_wrapper.html").replace("{$CONTENT$}", html), "text/mHtml", "UTF-8", null);
    }

    public void handleIntentExtras(Bundle extras) {
        if(extras == null) { return; }

        mUrl = extras.getString(Const.INTERNAL_URL);
        mHtml = extras.getString(Const.HTML);

        if(extras.containsKey(Const.STUDENT)) {
            mStudent = extras.getParcelable(Const.STUDENT);
        }
    }

    public static Bundle createBundle(String url, String title,  String html, @Nullable Student student) {
        Bundle extras = new Bundle();
        if(student != null) {
            extras.putParcelable(Const.STUDENT, student);
        }
        extras.putString(Const.INTERNAL_URL, url);
        extras.putString(Const.ACTION_BAR_TITLE, title);
        extras.putString(Const.HTML, html);
        return extras;
    }
}

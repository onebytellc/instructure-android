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

package com.instructure.parentapp.activity;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.OAuthManager;
import com.instructure.canvasapi2.models.OAuthToken;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.parentapp.BuildConfig;
import com.instructure.parentapp.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import retrofit2.Call;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public abstract class BaseLoginActivity extends BaseParentActivity {

    protected String mSuccessURL = "/login/oauth2/auth?code=";
    protected String mErrorURL = "/login/oauth2/auth?error=access_denied";

    private String client_id = BuildConfig.LOGIN_CLIENT_ID;
    private String client_secret = BuildConfig.LOGIN_CLIENT_SECRET;

    private int canvas_login = 0;
    private String mAuthenticationURL;
    private WebView mWeb;
    private StatusCallback<OAuthToken> mGetToken;
    private TextView mDomainHint;

    protected abstract @NonNull String userName();
    protected abstract @NonNull String userAgent();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_login);
        setupViews();
        setupCallbacks();
    }

    private void setupViews() {
        mWeb = (WebView) findViewById(R.id.webView);
        mDomainHint = (TextView) findViewById(R.id.domain);
        loadAuthenticatedURL();
    }

    private void setupCallbacks() {
        mGetToken = new StatusCallback<OAuthToken>(mStatusDelegate){
            @Override
            public void onResponse(retrofit2.Response<OAuthToken> response, LinkHeaders linkHeaders, ApiType type) {
                //Set up the rest adapter and such.
                //Leave as getDomain as it is a regular Canvas API call, not one that routes via Airwolf.
                APIHelper.setToken(BaseLoginActivity.this, response.body().getAccessToken());
            }

            @Override
            public void onFail(Call<OAuthToken> response, Throwable error) {
                Toast.makeText(BaseLoginActivity.this, R.string.errorOccurred, Toast.LENGTH_SHORT).show();
                mWeb.loadUrl(mAuthenticationURL);
            }
        };
    }

    protected void setDomainHint(String domain) {
        if(!TextUtils.isEmpty(domain)) {
            mDomainHint.setText(domain);
            mDomainHint.setVisibility(View.VISIBLE);
        }
    }

    protected void clearCookies() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {
                    //do nothing
                }
            });
        } else {
            CookieSyncManager.createInstance(BaseLoginActivity.this);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
        }
    }

    protected String createUserAgent(String userAgentString) {
        String userAgent;
        try {
            userAgent =  userAgentString + "/" +
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionName + " (" +
                    getPackageManager().getPackageInfo(getPackageName(), 0).versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            userAgent = userAgentString;
        }
        APIHelper.setUserAgent(BaseLoginActivity.this, userAgent);
        return userAgent;
    }

    private void loadAuthenticatedURL() {
        //Get device name for the login request.
        String deviceName = Build.MODEL;
        if(deviceName == null || deviceName.equals("")){
            deviceName = getString(R.string.unknownDevice);
        }

        //Remove spaces
        deviceName = deviceName.replace(" ", "_");

        String userName = userName();

        try {
            //encode the username in case it has symbols (like +)
            userName = URLEncoder.encode(userName, "utf-8");
        } catch (UnsupportedEncodingException e) {
            //do nothing
        }
        //changed for the online update to have an actual formatted login page pre-pended with username
        mAuthenticationURL = "https://" + APIHelper.getDomain(BaseLoginActivity.this) + "/login/oauth2/auth?client_id=" +
                client_id + "&response_type=code" + "&unique_id=" + userName
                + "&redirect_uri=urn:ietf:wg:oauth:2.0:oob&mobile=1";
        mAuthenticationURL += "&purpose="+ deviceName;

        if (canvas_login == 1) {
            mAuthenticationURL += "&canvas_login=1";
        } else if (canvas_login == 2) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setCookie("http://" + APIHelper.getDomain(BaseLoginActivity.this), "canvas_sa_delegated=1");
        }

        mWeb.loadUrl(mAuthenticationURL);
    }

    protected WebView getWebView() {
        return mWeb;
    }

    protected WebView initWebView() {
        clearCookies();
        mWeb.getSettings().setUserAgentString(userAgent());
        mWeb.getSettings().setLoadWithOverviewMode(true);
        mWeb.getSettings().setJavaScriptEnabled(true);
        mWeb.getSettings().setBuiltInZoomControls(true);
        mWeb.getSettings().setUseWideViewPort(true);
        mWeb.getSettings().setSavePassword(false);
        mWeb.getSettings().setSaveFormData(false);
        mWeb.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWeb.getSettings().setAppCacheEnabled(false);
        //don't need a user agent here for the parent login
        mWeb.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(mSuccessURL)) {
                    String oAuthRequest = url.substring(url.indexOf(mSuccessURL) + mSuccessURL.length());
                    OAuthManager.getToken(client_id, client_secret, oAuthRequest, mGetToken);
                } else if (url.contains(mErrorURL)) {
                    clearCookies();
                    view.loadUrl(mAuthenticationURL);
                } else {
                    view.loadUrl(url);
                }

                return true; // then it is not handled by default action
            }

        });
        return mWeb;
    }
}

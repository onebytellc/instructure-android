package com.instructure.parentapp.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.webkit.WebView;
import com.instructure.parentapp.R;


/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 *
 * Catches the case where the forgot password link would be
 * filtered by a local install of the Cavnas App
 */

public class RouteForgotPasswordActivity extends AppCompatActivity{

    private WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_password_activity);
        mWebView = (WebView)findViewById(R.id.route_webview);
        final String url = getIntent().getDataString();

        if(TextUtils.isEmpty(url)) {
            finish();
            return;
        }

        mWebView.loadUrl(url);
    }
}

package com.instructure.parentapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.Prefs;
import com.instructure.parentapp.R;
import com.instructure.parentapp.util.ApplicationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.instructure.parentapp.util.Const.USER_AGENT;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class CanvasLoginActivity extends BaseLoginActivity {

    private String successURL = "/oauthSuccess";
    private String cancelURL = "/oauth2/deny";
    private String errorURL = "/oauthFailure";
    private String tokenURL = "/canvas/tokenReady";

    private String mDomain;
    private String mDomainHint;

    private TextView mInstructions;

    public String getDomain() {
        if(mDomain == null) {
            mDomain = getIntent().getExtras().getString(Const.DOMAIN);
        }
        return mDomain;
    }

    public String getDomainHint() {
        if(mDomainHint == null) {
            mDomainHint = getIntent().getExtras().getString(Const.DOMAIN_FOR_DISPLAY);
        }
        return mDomainHint;
    }

    public boolean isStudent() {
        return getIntent().getExtras().getBoolean(Const.IS_STUDENT);
    }

    @NonNull
    @Override
    protected String userName() {
        return "";
    }

    @NonNull
    @Override
    protected String userAgent() {
        return createUserAgent(USER_AGENT);
    }

    public static Intent createIntent(Context context, String domain, String domainForDisplay, boolean isStudent) {
        Intent intent = new Intent(context, CanvasLoginActivity.class);
        intent.putExtra(Const.DOMAIN, domain);
        intent.putExtra(Const.DOMAIN_FOR_DISPLAY, domainForDisplay);
        intent.putExtra(Const.IS_STUDENT, isStudent);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupWebView();
        getWebView().loadUrl(getDomain(), getHeaders());
        setDomainHint(getDomainHint());
        mInstructions = (TextView) findViewById(R.id.instructions);

        if(!isStudent()) {
            mInstructions.setText(getString(R.string.canvas_login_text));
        }
    }

    private void setupWebView() {
        initWebView();
        getWebView().setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(successURL)) {
                    getStudents();
                } else if (url.contains(cancelURL)) {
                    //FIXME
                } else if (url.contains(errorURL)) {
                    clearCookies();
                    if(!isStudent()) {
                        Toast.makeText(CanvasLoginActivity.this, R.string.onlyCanvasObservers, Toast.LENGTH_SHORT).show();
                    }
                    view.loadUrl(getDomain(), getHeaders());
                } else if (url.contains(tokenURL)) {
                    //when a parent logs in with observer credentials
                    //get the parent id from the url
                    String parentId = "parent_id=";
                    String token = "token=";
                    int index = url.indexOf(parentId);
                    if(index != -1) {
                        int endIndex = url.indexOf("&", index);
                        Prefs prefs = new Prefs(CanvasLoginActivity.this, com.instructure.parentapp.util.Const.CANVAS_PARENT_SP);
                        prefs.save(Const.ID, url.substring(index + parentId.length(), endIndex));
                    }
                    index = url.indexOf(token);
                    if(index != -1) {
                        APIHelper.setToken(CanvasLoginActivity.this, url.substring(index + token.length()));
                        AppManager.getConfig().setToken(url.substring(index + token.length()));
                    }

                    getStudents();

                } else {
                    view.loadUrl(url, getHeaders());
                }

                return true; // then it is not handled by default action
            }
        });
    }

    private void getStudents() {
        UserManager.getStudentsForParentAirwolf(
                APIHelper.getAirwolfDomain(CanvasLoginActivity.this),
                ApplicationManager.getParentId(CanvasLoginActivity.this),
                new StatusCallback<List<Student>>(mStatusDelegate){
                    @Override
                    public void onResponse(retrofit2.Response<List<Student>> response, LinkHeaders linkHeaders, ApiType type) {
                        if(isStudent()) {
                            if (type == ApiType.API) {
                                //they have students that they are observing
                                clearCookies(); //clear cookies for security

                                Intent intent = new Intent();
                                intent.putParcelableArrayListExtra(Const.STUDENT, new ArrayList<Parcelable>(response.body()));
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        } else {

                            clearCookies(); //clear cookies for security

                            if (response.body() != null && !response.body().isEmpty()) {
                                //finish the activity so they can't hit the back button and see the login screen again
                                //they have students that they are observing, take them to that activity
                                Intent intent = StudentViewActivity.createIntent(CanvasLoginActivity.this, response.body());
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                startActivity(intent);
                                overridePendingTransition(0, 0);
                                finish();
                            } else {
                                //Take the parent to the add user page.
                                Intent intent = DomainPickerActivity.createIntent(CanvasLoginActivity.this, false, false, true);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                startActivity(intent);
                                overridePendingTransition(0, 0);
                                finish();
                            }
                        }
                    }
                }
        );
    }

    public Map<String, String> getHeaders() {
        Map<String, String> extraHeaders = new HashMap<>();
        extraHeaders.put("accept-language", CanvasRestAdapter.getAcceptedLanguageString());
        return extraHeaders;
    }


        @Override
    public void unBundle(@NonNull Bundle extras) {

    }

    @Override
    public boolean applyThemeAutomagically() {
        return false;
    }
}

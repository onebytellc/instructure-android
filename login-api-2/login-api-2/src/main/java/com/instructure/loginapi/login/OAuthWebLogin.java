package com.instructure.loginapi.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.amazon.android.webkit.AmazonCookieManager;
import com.amazon.android.webkit.AmazonHttpAuthHandler;
import com.amazon.android.webkit.AmazonWebKitFactories;
import com.amazon.android.webkit.AmazonWebKitFactory;
import com.amazon.android.webkit.AmazonWebResourceResponse;
import com.amazon.android.webkit.AmazonWebView;
import com.amazon.android.webkit.AmazonWebViewClient;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.CanvasRestAdapter;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.OAuthManager;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.OAuthToken;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.canvasapi2.utils.Masquerading;
import com.instructure.loginapi.login.api.CanvasAPI;
import com.instructure.loginapi.login.api.MobileVerifyAPI;
import com.instructure.loginapi.login.model.DomainVerificationResult;
import com.instructure.loginapi.login.model.SignedInUser;
import com.instructure.loginapi.login.util.Const;
import com.instructure.loginapi.login.util.SavedDomains;
import com.instructure.pandautils.activities.BaseActivity;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class OAuthWebLogin extends BaseActivity {

    private String url;
    private String authenticationURL;

    private boolean isAmazonDevice = false;

    private WebView web;
    private AmazonWebView amazonWeb;
    private AmazonWebKitFactory factory;
    private static boolean factoryInit = false;

    private String successURL = "/login/oauth2/auth?code=";
    private String errorURL = "/login/oauth2/auth?error=access_denied";

    private String client_id;
    private String client_secret;
    private String api_protocol;

    private int canvas_login = 0;

    boolean specialCase = false;

    private HttpAuthHandler httpAuthHandler;
    private AmazonHttpAuthHandler httpAmazonAuthHandler;

    private StatusCallback<DomainVerificationResult> mobileVerifyCallback;
    private StatusCallback<OAuthToken> getToken;

    public final static String OAUTH_URL = "OAuthWebLogin-url";
    public final static String OAUTH_CANVAS_LOGIN = "OAuthWebLogin-canvas_login";

    private static String prefFileName;
    private static String prefNamePreviousDomain;
    private static String prefNameOtherSignedInUsers;
    private static String prefMultiSignedInUsers;
    public final static int SIGNED_IN = 5000;

    private Uri passedURI;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);
        }
        setContentView(R.layout.activity_oauth_web_login);
        handleIntent();

        isAmazonDevice = com.instructure.pandautils.utils.Utils.isAmazonDevice();

        FrameLayout container = (FrameLayout) findViewById(R.id.container);
        if (isAmazonDevice) {
            container.addView(createAmazonWebView());
        } else {
            container.addView(createWebView());
        }

        ((TextView)findViewById(R.id.domain)).setText(url);

        setupCallback();
        MobileVerifyAPI.mobileVerify(this, url, mobileVerifyCallback);
    }

    void clearCookies() {
        if (isAmazonDevice) {
            AmazonCookieManager.getInstance().removeAllCookie();
        } else {
            CookieSyncManager.createInstance(OAuthWebLogin.this);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        //save the intent information in case we get booted from memory.
        SharedPreferences settings = getSharedPreferences(prefFileName, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(OAUTH_URL, url);
        editor.putInt(OAUTH_CANVAS_LOGIN, canvas_login);
        editor.apply();

        //we don't want the onPostExecute to be called in mobileVerifyAT if we're leaving this function. Without cancelling this
        //it would try to create a fragment and not have anything to attach it to and it would crash. (insert frowny face)
        if (mobileVerifyCallback != null) {
            mobileVerifyCallback.cancel();
        }
    }

    private WebView createWebView() {
        web = new WebView(this);
        clearCookies();
        CookieManager.getInstance().setAcceptCookie(true);
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setBuiltInZoomControls(true);
        web.getSettings().setUseWideViewPort(true);
        web.getSettings().setSavePassword(false);
        web.getSettings().setSaveFormData(false);
        web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        web.getSettings().setAppCacheEnabled(false);
        web.getSettings().setUserAgentString(CanvasAPI.getCandroidUserAgent("candroid", OAuthWebLogin.this));
        web.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.contains(successURL)) {
                    String oAuthRequest = url.substring(url.indexOf(successURL) + successURL.length());
                    OAuthManager.getToken(client_id, client_secret, oAuthRequest, getToken);
                } else if (url.contains(errorURL)) {
                    clearCookies();
                    view.loadUrl(authenticationURL);
                } else {
                    view.loadUrl(url);
                }

                return true; // then it is not handled by default action
            }

            public void onPageFinished(WebView view, String url) {
                setProgressBarIndeterminateVisibility(false);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (url.contains("idp.sfcollege.edu/idp/santafe")) {
                    specialCase = true;
                    String oAuthRequest = url.substring(url.indexOf("hash=") + "hash=".length());
                    OAuthManager.getToken(client_id, client_secret, oAuthRequest, getToken);
                }
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                httpAuthHandler = handler;
                showAuthenticationDialog();
            }
        });
        return web;
    }

    private AmazonWebView createAmazonWebView() {

        if (!factoryInit) {
            factory = AmazonWebKitFactories.getDefaultFactory();
            if (factory.isRenderProcess(this)) {
                return amazonWeb;
            }
            factory.initialize(this.getApplicationContext());
            factoryInit = !factoryInit;
            // factory configuration is done here, for example:
            factory.getCookieManager().setAcceptCookie(true);
        } else {
            factory = AmazonWebKitFactories.getDefaultFactory();
        }

        amazonWeb = new AmazonWebView(this);
        factory.initializeWebView(amazonWeb, Color.WHITE, false, null);
        clearCookies();
        amazonWeb.getSettings().setJavaScriptEnabled(true);
        amazonWeb.getSettings().setBuiltInZoomControls(true);
        amazonWeb.getSettings().setUseWideViewPort(true);
        amazonWeb.getSettings().setSavePassword(false);
        amazonWeb.getSettings().setSaveFormData(false);
        amazonWeb.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        amazonWeb.getSettings().setAppCacheEnabled(false);
        amazonWeb.getSettings().setUserAgentString(CanvasAPI.getCandroidUserAgent("candroid", OAuthWebLogin.this));
        amazonWeb.setWebViewClient(new AmazonWebViewClient() {

            public boolean shouldOverrideUrlLoading(AmazonWebView view, String url) {
                if (url.contains(successURL)) {
                    String oAuthRequest = url.substring(url.indexOf(successURL) + successURL.length());
                    OAuthManager.getToken(client_id, client_secret, oAuthRequest, getToken);
                } else if (url.contains(errorURL)) {
                    clearCookies();
                    view.loadUrl(authenticationURL);
                } else {
                    view.loadUrl(url);
                }

                return true; // then it is not handled by default action
            }

            public void onPageFinished(AmazonWebView view, String url) {
                setProgressBarIndeterminateVisibility(false);
            }

            @Override
            public AmazonWebResourceResponse shouldInterceptRequest(AmazonWebView webview, String url) {
                if (url.contains("idp.sfcollege.edu/idp/santafe")) {
                    specialCase = true;
                    String oAuthRequest = url.substring(url.indexOf("hash=") + "hash=".length());
                    OAuthManager.getToken(client_id, client_secret, oAuthRequest, getToken);
                }
                return super.shouldInterceptRequest(webview, url);
            }

            @Override
            public void onReceivedHttpAuthRequest(AmazonWebView view, AmazonHttpAuthHandler handler, String host, String realm) {
                super.onReceivedHttpAuthRequest(view, handler, host, realm);
                httpAmazonAuthHandler = handler;
                showAuthenticationDialog();
            }
        });
        return amazonWeb;
    }

    private void showAuthenticationDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(OAuthWebLogin.this);
        builder.title(R.string.authenticationRequired);
        builder.customView(R.layout.auth_dialog, true);
        builder.cancelable(true);
        builder.positiveText(R.string.done);
        builder.negativeText(R.string.cancel);
        builder.positiveColor(Color.BLACK);
        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                View dialogView = dialog.getCustomView();
                if (dialogView != null) {
                    EditText username = (EditText) dialogView.findViewById(R.id.username);
                    EditText password = (EditText) dialogView.findViewById(R.id.password);
                    if (!TextUtils.isEmpty(username.getText()) && !TextUtils.isEmpty(password.getText())) {
                        if (httpAuthHandler != null) {
                            httpAuthHandler.proceed(username.getText().toString(), password.getText().toString());
                        } else if (httpAmazonAuthHandler != null) {
                            httpAmazonAuthHandler.proceed(username.getText().toString(), password.getText().toString());
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.invalidEmailPassword, Toast.LENGTH_SHORT).show();
                    }
                }
                super.onPositive(dialog);
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                onBackPressed();
                super.onNegative(dialog);
            }
        });
        MaterialDialog dialog = builder.build();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void setupCallback() {
        mobileVerifyCallback = new StatusCallback<DomainVerificationResult>(mStatusDelegate) {

            @Override
            public void onResponse(Response<DomainVerificationResult> response, LinkHeaders linkHeaders, ApiType type) {
                super.onResponse(response, linkHeaders, type);

                if (type.isAPI()) {

                    DomainVerificationResult domainVerificationResult = response.body();

                    if (domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.Success) {
                        //Domain is now verified.
                        //save domain to the preferences.
                        String domain = "";

                        //mobile verify can change the hostname we need to use
                        if (domainVerificationResult.getBase_url() != null && !domainVerificationResult.getBase_url().equals("")) {
                            domain = domainVerificationResult.getBase_url();
                        } else {
                            domain = url;
                        }

                        //The domain gets set afterwards in SetUpInstance, but domain is required a bit before that works.
                        APIHelper.setDomain(OAuthWebLogin.this, domain);

                        client_id = domainVerificationResult.getClient_id();
                        client_secret = domainVerificationResult.getClient_secret();

                        //Get the protocol
                        api_protocol = domainVerificationResult.getProtocol();

                        //Set the protocol
                        APIHelper.setProtocol(domainVerificationResult.getProtocol(), OAuthWebLogin.this);

                        //Get device name for the login request.
                        String deviceName = Build.MODEL;
                        if (deviceName == null || deviceName.equals("")) {
                            deviceName = getString(R.string.unknownDevice);
                        }

                        //Remove spaces
                        deviceName = deviceName.replace(" ", "_");

                        //changed for the online update to have an actual formatted login page
                        authenticationURL = api_protocol + "://" + domain + "/login/oauth2/auth?client_id=" +
                                client_id + "&response_type=code&redirect_uri=urn:ietf:wg:oauth:2.0:oob&mobile=1";
                        authenticationURL += "&purpose=" + deviceName;

                        if (canvas_login == 1) {
                            authenticationURL += "&canvas_login=1";
                        } else if (canvas_login == 2) {
                            if (isAmazonDevice) {
                                AmazonCookieManager cookieManager = AmazonCookieManager.getInstance();
                                cookieManager.setCookie(api_protocol + "://" + domain, "canvas_sa_delegated=1");
                            } else {
                                CookieManager cookieManager = CookieManager.getInstance();
                                cookieManager.setCookie(api_protocol + "://" + domain, "canvas_sa_delegated=1");
                            }
                        }

                        if (isAmazonDevice) {
                            amazonWeb.loadUrl(authenticationURL);
                        } else {
                            web.loadUrl(authenticationURL);
                        }
                    } else {
                        //Error message
                        int errorId;

                        if (domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.GeneralError) {
                            errorId = R.string.mobileVerifyGeneral;
                        } else if (domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.DomainNotAuthorized) {
                            errorId = R.string.mobileVerifyDomainUnauthorized;
                        } else if (domainVerificationResult.getResult() == DomainVerificationResult.DomainVerificationCode.UnknownUserAgent) {
                            errorId = R.string.mobileVerifyUserAgentUnauthorized;
                        } else {
                            errorId = R.string.mobileVerifyUnknownError;
                        }

                        MaterialDialog.Builder builder = new MaterialDialog.Builder(OAuthWebLogin.this);
                        builder.title(R.string.errorOccurred);
                        builder.content(errorId);
                        builder.cancelable(true);
                        builder.positiveColor(Color.BLACK);
                        MaterialDialog dialog = builder.build();
                        dialog.show();
                    }
                }
            }

        };

        getToken = new StatusCallback<OAuthToken>(mStatusDelegate) {

            @Override
            public void onResponse(retrofit2.Response<OAuthToken> response, LinkHeaders linkHeaders, ApiType type) {
                super.onResponse(response, linkHeaders, type);

                if(type.isAPI()) {
                    OAuthToken token = response.body();
                    Context ctx = OAuthWebLogin.this;

                    CanvasRestAdapter.saveLoginInfo(ctx, token.getAccessToken(), APIHelper.loadProtocol(ctx) + "://" + APIHelper.getDomain(ctx));

                    //save the successful domain to be remembered for later
                    JSONArray domains = SavedDomains.getSavedDomains(OAuthWebLogin.this, prefNamePreviousDomain);

                    String domain = APIHelper.getDomain(OAuthWebLogin.this);
                    domains.put(domain);
                    SavedDomains.setSavedDomains(OAuthWebLogin.this, domains, prefNamePreviousDomain); //save the new domain

                    //Set the last used domain.
                    setLastSignedInDomain(domain, OAuthWebLogin.this);

                    //We now need to get the cache user
                    UserManager.getSelf(new StatusCallback<User>(mStatusDelegate) {

                        @Override
                        public void onResponse(retrofit2.Response<User> response, LinkHeaders linkHeaders, ApiType type) {
                            super.onResponse(response, linkHeaders, type);

                            if(type.isAPI()) {
                                APIHelper.setCacheUser(OAuthWebLogin.this, response.body());

                                Intent intent = OAuthWebLogin.this.getIntent();
                                intent.putExtra(URLSignIn.loggedInIntent, true);
                                if (passedURI != null) {
                                    intent.putExtra(Const.PASSED_URI, passedURI);
                                }

                                OAuthWebLogin.this.setResult(RESULT_OK, intent);
                                OAuthWebLogin.this.finish();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFail(Call<OAuthToken> response, Throwable error) {
                super.onFail(response, error);
                if (!specialCase) {
                    Toast.makeText(OAuthWebLogin.this, R.string.errorOccurred, Toast.LENGTH_SHORT).show();
                } else {
                    specialCase = false;
                }

                if (isAmazonDevice) {
                    amazonWeb.loadUrl(authenticationURL);
                } else {
                    web.loadUrl(authenticationURL);
                }
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Multi User Sign In
    ///////////////////////////////////////////////////////////////////////////

    //Used for MultipleUserSignIn
    public static String getGlobalUserId(String domain, User user) {
        if (user == null) {
            return "";
        }
        return domain + "-" + user.getId();
    }

    public static ArrayList<SignedInUser> getPreviouslySignedInUsers(Context context) {
        return getPreviouslySignedInUsers(context, prefNameOtherSignedInUsers);
    }

    //Does the CURRENT user support Multiple Users.
    public static ArrayList<SignedInUser> getPreviouslySignedInUsers(Context context, String preferenceKey) {

        if (TextUtils.isEmpty(preferenceKey)) {
            prefNameOtherSignedInUsers = Const.KEY_OTHER_SIGNED_IN_USERS_PREF_NAME;
            preferenceKey = prefNameOtherSignedInUsers;
        }

        ArrayList<SignedInUser> signedInUsers = new ArrayList<SignedInUser>();

        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceKey, Context.MODE_PRIVATE);
        Map<String, ?> keys = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            SignedInUser signedInUser = null;

            try {
                signedInUser = new Gson().fromJson(entry.getValue().toString(), SignedInUser.class);
            } catch (IllegalStateException ignore) {
            } catch (JsonSyntaxException ignore) {
                //Once in a great while some bad formatted json get stored, if that happens we end up here.
            }

            if (signedInUser != null) {
                signedInUsers.add(signedInUser);
            }
        }

        //Sort by last signed in date.
        Collections.sort(signedInUsers);
        return signedInUsers;
    }

    //Remove user from PreviouslySignedInUsers
    public static boolean removeFromPreviouslySignedInUsers(SignedInUser signedInUser, Context context) {
        return removeFromPreviouslySignedInUsers(signedInUser, context, prefNameOtherSignedInUsers);
    }

    public static boolean removeFromPreviouslySignedInUsers(SignedInUser signedInUser, final Context context, String preferenceKey) {

        if (TextUtils.isEmpty(preferenceKey)) {
            prefNameOtherSignedInUsers = Const.KEY_OTHER_SIGNED_IN_USERS_PREF_NAME;
            preferenceKey = prefNameOtherSignedInUsers;
        }

        // Delete Access Token. We don't care about the result.
        OAuthManager.deleteToken(new StatusCallback.StatusDelegate() {
            @Override
            public boolean hasNetworkConnection() {
                return AppManager.hasNetworkConnection(context);
            }
        });

        //Save Signed In User to sharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceKey, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(getGlobalUserId(signedInUser.domain, signedInUser.user));
        return editor.commit();
    }

    //Add user to PreviouslySignedInUsers
    public static boolean addToPreviouslySignedInUsers(SignedInUser signedInUser, Context context) {
        return addToPreviouslySignedInUsers(signedInUser, context, prefNameOtherSignedInUsers);
    }

    public static boolean addToPreviouslySignedInUsers(SignedInUser signedInUser, Context context, String preferenceKey) {

        if (TextUtils.isEmpty(preferenceKey)) {
            prefNameOtherSignedInUsers = Const.KEY_OTHER_SIGNED_IN_USERS_PREF_NAME;
            preferenceKey = prefNameOtherSignedInUsers;
        }

        String signedInUserJSON = new Gson().toJson(signedInUser);

        //Save Signed In User to sharedPreferences
        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceKey, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getGlobalUserId(APIHelper.getDomain(context), APIHelper.getCacheUser(context)), signedInUserJSON);
        return editor.commit();
    }

    public static boolean setIsMultipleUsersSupported(boolean multipleUserSupported, Context context) {
        return setIsMultipleUsersSupported(multipleUserSupported, context, prefMultiSignedInUsers);
    }

    public static boolean setIsMultipleUsersSupported(boolean multipleUserSupported, Context context, String preferenceKey) {

        if (TextUtils.isEmpty(preferenceKey)) {
            prefMultiSignedInUsers = Const.KEY_MULTI_SIGN_IN_PREF_NAME;
            preferenceKey = prefMultiSignedInUsers;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceKey, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String globalID = getGlobalUserId(APIHelper.getDomain(context), APIHelper.getCacheUser(context));

        if (multipleUserSupported) {
            editor.putBoolean(globalID, multipleUserSupported);
        } else {
            editor.remove(globalID);
        }
        return editor.commit();
    }

    public static boolean isMultipleUsersSupported(Context context) {
        return isMultipleUsersSupported(context, prefMultiSignedInUsers);
    }

    public static boolean isMultipleUsersSupported(Context context, String preferenceKey) {

        if (TextUtils.isEmpty(preferenceKey)) {
            prefMultiSignedInUsers = Const.KEY_MULTI_SIGN_IN_PREF_NAME;
            preferenceKey = prefMultiSignedInUsers;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(preferenceKey, MODE_PRIVATE);
        return sharedPreferences.getBoolean(getGlobalUserId(APIHelper.getDomain(context), APIHelper.getCacheUser(context)), false);
    }

    public static boolean isUserLoggedIn(Context context) {
        String token = APIHelper.getToken(context);
        return (token != null && token.length() != 0);
    }

    /**
     * Helper method to retrieve a users shared prefs for calendar
     *
     * @return
     */
    public static ArrayList<String> getCalendarFilterPrefs(Context context) {
        if (TextUtils.isEmpty(prefFileName)) {
            prefFileName = Const.KEY_PREF_FILE_NAME;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(prefFileName, MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet(com.instructure.pandautils.utils.Const.FILTER_PREFS_KEY, new HashSet<String>());
        ArrayList<String> prefs = new ArrayList<>();
        if (set.size() != 0) {
            for (String s : set) {
                if (s != null) {
                    prefs.add(s);
                }
            }
        }
        return prefs;
    }

    /**
     * Helper method to set a users shared prefs for calendar
     *
     * @return
     */
    public static void setCalendarFilterPrefs(ArrayList<String> filterPrefs, Context context) {
        Set<String> set = new HashSet<>();

        if (filterPrefs == null) {
            filterPrefs = new ArrayList<>();
        }
        for (String s : filterPrefs) {
            if (s != null) {
                set.add(s);
            }
        }

        if (TextUtils.isEmpty(prefFileName)) {
            prefFileName = Const.KEY_PREF_FILE_NAME;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(prefFileName, MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(com.instructure.pandautils.utils.Const.FILTER_PREFS_KEY, set);
        editor.apply();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Domain
    ///////////////////////////////////////////////////////////////////////////

    public static String getLastSignedInDomain(Context context) {
        //get the Domain
        if (TextUtils.isEmpty(prefFileName)) {
            prefFileName = Const.KEY_PREF_FILE_NAME;
        }
        SharedPreferences settings = context.getSharedPreferences(prefFileName, MODE_PRIVATE);
        return settings.getString(Const.LAST_DOMAIN, "");
    }

    public static void setLastSignedInDomain(String domain, Context context) {
        if (Masquerading.isMasquerading(context)) {
            return;
        }

        if (TextUtils.isEmpty(prefFileName)) {
            prefFileName = Const.KEY_PREF_FILE_NAME;
        }

        //save the OAuthToken
        SharedPreferences settings = context.getSharedPreferences(prefFileName, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Const.LAST_DOMAIN, domain);
        editor.apply();
    }


    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    protected void handleIntent() {
        Intent intent = getIntent();
        //Make sure we weren't booted from memory
        if (intent != null && getIntent().hasExtra(Const.PREF_FILE_NAME) &&
                getIntent().hasExtra(Const.PREF_NAME_PREVIOUS_DOMAIN) &&
                getIntent().hasExtra(Const.PREF_OTHER_SIGNED_IN_USERS)) {

            prefFileName = getIntent().getStringExtra(Const.PREF_FILE_NAME);
            prefNamePreviousDomain = getIntent().getStringExtra(Const.PREF_NAME_PREVIOUS_DOMAIN);
            prefNameOtherSignedInUsers = getIntent().getStringExtra(Const.PREF_OTHER_SIGNED_IN_USERS);
            prefMultiSignedInUsers = getIntent().getStringExtra(Const.PREF_MULTI_SIGN_IN);
        }

        if (intent == null || !intent.hasExtra(Const.HOST)) {
            SharedPreferences settings = getSharedPreferences(prefFileName, MODE_PRIVATE);
            url = settings.getString(OAUTH_URL, "");
        } else {
            url = intent.getStringExtra(Const.HOST);
        }

        if (intent == null || !intent.hasExtra(Const.CANVAS_LOGIN)) {
            SharedPreferences settings = getSharedPreferences(prefFileName, MODE_PRIVATE);
            canvas_login = settings.getInt(OAUTH_CANVAS_LOGIN, 0);
        } else {
            canvas_login = intent.getIntExtra(Const.CANVAS_LOGIN, 0);
        }

        if (intent != null && getIntent().hasExtra(Const.URI)) {
            passedURI = (Uri) getIntent().getParcelableExtra(Const.URI);
        }
    }

    public static Intent createIntent(
            Context context,
            String host,
            int canvasLogin,
            String preferenceName,
            String preferenceNamePreviousDomain,
            String preferenceOtherSignedInUsers,
            String preferenceMultiSignedInUsers,
            Uri uri) {

        Intent intent = new Intent(context, OAuthWebLogin.class);
        intent.putExtra(Const.URI, uri);
        intent.putExtra(Const.CANVAS_LOGIN, canvasLogin);
        intent.putExtra(Const.HOST, host);
        intent.putExtra(Const.PREF_FILE_NAME, preferenceName);
        intent.putExtra(Const.PREF_NAME_PREVIOUS_DOMAIN, preferenceNamePreviousDomain);
        intent.putExtra(Const.PREF_OTHER_SIGNED_IN_USERS, preferenceOtherSignedInUsers);
        intent.putExtra(Const.PREF_MULTI_SIGN_IN, preferenceMultiSignedInUsers);
        return intent;
    }

    public static Intent createIntent(
            Context context,
            String host,
            int canvasLogin,
            String preferenceName,
            String preferenceNamePreviousDomain,
            String preferenceOtherSignedInUsers,
            String preferenceMultiSignedInUsers) {

        Intent intent = new Intent(context, OAuthWebLogin.class);
        intent.putExtra(Const.HOST, host);
        intent.putExtra(Const.CANVAS_LOGIN, canvasLogin);
        intent.putExtra(Const.PREF_FILE_NAME, preferenceName);
        intent.putExtra(Const.PREF_NAME_PREVIOUS_DOMAIN, preferenceNamePreviousDomain);
        intent.putExtra(Const.PREF_OTHER_SIGNED_IN_USERS, preferenceOtherSignedInUsers);
        intent.putExtra(Const.PREF_MULTI_SIGN_IN, preferenceMultiSignedInUsers);
        return intent;
    }

    @Override
    public void unBundle(@NonNull Bundle extras) {

    }

    @Override
    public boolean applyThemeAutomagically() {
        return false;
    }
}

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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.instructure.candroid.R;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.util.FragUtils;
import com.instructure.candroid.util.LockInfoHTMLHelper;
import com.instructure.candroid.util.Param;
import com.instructure.candroid.util.RouterUtils;
import com.instructure.canvasapi.api.QuizAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Quiz;
import com.instructure.canvasapi.model.Tab;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.utils.Const;

import java.util.HashMap;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class BasicQuizViewFragment extends InternalWebviewFragment {

    private String baseURL;
    private String apiURL;
    private Quiz quiz;
    private long quizId = -1;

    private CanvasCallback<Quiz> canvasCallback;
    private CanvasCallback<Quiz> getDetailedQuizCallback;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.DETAIL; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.quizzes);
    }

    @Override
    public String getTabId() {
        return Tab.QUIZZES_ID;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //we need to set the webviewclient before we get the quiz so it doesn't try to open the
        //quiz in a different browser
        if(baseURL == null) {
            //if the baseURL is null something went wrong, nothing will show here
            //but at least it won't crash
            return;
        }
        final Uri uri = Uri.parse(baseURL);
        final String host = uri.getHost();
        canvasWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        canvasWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri currentUri = Uri.parse(url);

                if (url.contains(host)) { //we need to handle it.
                    if (currentUri != null && currentUri.getPathSegments().size() >= 3 && currentUri.getPathSegments().get(2).equals("quizzes")) {  //if it's a quiz, stay here.
                        view.loadUrl(url, Utils.getReferer(getContext()));
                        return true;
                    }
                    //might need to log in to take the quiz -- the url would say domain/login. If we just use the AppRouter it will take the user
                    //back to the dashboard. This check will keep them here and let them log in and take the quiz
                    else if (currentUri != null && currentUri.getPathSegments().size() >= 1 && currentUri.getPathSegments().get(0).equalsIgnoreCase("login")) {
                        view.loadUrl(url, Utils.getReferer(getContext()));
                        return true;
                    } else { //It's content but not a quiz. Could link to a discussion (or whatever) in a quiz. Route
                        return RouterUtils.canRouteInternally(getActivity(), url, APIHelpers.getDomain(getActivity()), true);
                    }
                }

                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                canvasLoading.setVisibility(View.GONE);
                Navigation navigation = getNavigation();
                if (navigation != null) {
                    navigation.redrawScreen();
                }
            }
        });
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpCallback();
        // anything that relies on intent data belongs here
        if(apiURL != null) {
            QuizAPI.getDetailedQuizFromURL(apiURL, canvasCallback);
            return;
        } else if(quiz != null && quiz.getLockInfo() != null) {
            populateWebView(LockInfoHTMLHelper.getLockedInfoHTML(quiz.getLockInfo(),getActivity(), R.string.lockedQuizDesc, R.string.lockedAssignmentDescLine2));
        } else if (quizId != -1) {
            QuizAPI.getDetailedQuiz(getCanvasContext(), quizId, getDetailedQuizCallback);
        } else {
            authenticate = true;
            loadUrl(baseURL);
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
    protected Quiz getModelObject() {
        return quiz;
    }
    ///////////////////////////////////////////////////////////////////////////
    // View
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Callback
    ///////////////////////////////////////////////////////////////////////////


    public void setUpCallback(){
        getDetailedQuizCallback =  new CanvasCallback<Quiz>(this) {
            Quiz cacheQuiz;

            @Override
            public void cache(Quiz quiz, LinkHeaders linkHeaders, Response response) {
                cacheQuiz = quiz;
            }

            @Override
            public void firstPage(Quiz quiz, LinkHeaders linkHeaders, Response response) {
                if(quiz == null) return;
                
                BasicQuizViewFragment.this.quiz = quiz;
                BasicQuizViewFragment.this.baseURL = quiz.getUrl();

                if (shouldShowNatively(quiz)) { return; }

                if (quiz.getLockInfo() != null) {
                    populateWebView(LockInfoHTMLHelper.getLockedInfoHTML(quiz.getLockInfo(),getActivity(), R.string.lockedQuizDesc, R.string.lockedAssignmentDescLine2));
                } else {
                    canvasWebView.loadUrl(quiz.getUrl(), Utils.getReferer(getContext()));
                }
            }
            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                firstPage(cacheQuiz, null, null);
                return true;
            }
        };
        canvasCallback = new CanvasCallback<Quiz>(this) {

            @Override
            public void firstPage(Quiz quiz, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()){
                    return;
                }

                BasicQuizViewFragment.this.quiz = quiz;
                if (shouldShowNatively(quiz)) { return; }

                if (quiz.getLockInfo() != null) {
                    populateWebView(LockInfoHTMLHelper.getLockedInfoHTML(quiz.getLockInfo(),getActivity(), R.string.lockedQuizDesc, R.string.lockedAssignmentDescLine2));
                } else {
                    String url = quiz.getUrl();
                    if (TextUtils.isEmpty(url)) {
                        url = baseURL;
                    }
                    canvasWebView.loadUrl(url, Utils.getReferer(getContext()));
                }
            }
        };
    }

    /**
     * When we route we always route quizzes here, so this checks to see if we support
     * native quizzes and if we do then we'll show it natively
     * @param quiz
     * @return
     */
    private boolean shouldShowNatively(Quiz quiz) {
        if(QuizListFragment.isNativeQuiz(getCanvasContext(), quiz)) {

            //take them to the quiz start fragment instead, let them take it natively
            Navigation navigation = getNavigation();
            if (navigation != null) {
                navigation.popCurrentFragment();
                Bundle bundle = QuizStartFragment.createBundle(getCanvasContext(), quiz);
                navigation.addFragment(FragUtils.getFrag(QuizStartFragment.class, bundle), true);
                return true;
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        if (getUrlParams() != null) {
            quizId = parseLong(getUrlParams().get(Param.QUIZ_ID), -1);
        } else {
            baseURL = extras.getString(Const.URL);
            apiURL = extras.getString(Const.API_URL);
            quiz = extras.getParcelable(Const.QUIZ);
        }
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.URL, url);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url, Quiz quiz) {
        Bundle extras = createBundle(canvasContext);
        extras.putParcelable(Const.QUIZ, quiz);
        extras.putString(Const.URL, url);
        return extras;
    }

    //for module progression we need the api url too
    public static Bundle createBundle(CanvasContext canvasContext, String url, String apiURL) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.URL, url);
        extras.putString(Const.API_URL, apiURL);
        return extras;
    }

    //Currently there isn't a way to know how to decide if we want to route
    //to this fragment or the QuizStartFragment.
    @Override
    public boolean allowBookmarking() {
        return false;
    }

    @Override
    public HashMap<String, String> getParamForBookmark() {
        HashMap<String, String> map = getCanvasContextParams();
        if(quiz != null) {
            map.put(Param.QUIZ_ID, Long.toString(quiz.getId()));
        } else if(quizId != -1) {
            map.put(Param.QUIZ_ID, Long.toString(quizId));
        }
        return map;
    }
}

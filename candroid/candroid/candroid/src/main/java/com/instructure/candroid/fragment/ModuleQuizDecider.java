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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.instructure.candroid.R;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.util.FragUtils;
import com.instructure.candroid.util.RouterUtils;
import com.instructure.candroid.view.CanvasWebView;
import com.instructure.canvasapi.api.QuizAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Quiz;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.Const;

import retrofit.client.Response;


public class ModuleQuizDecider extends ParentFragment {

    private String baseURL;
    private String apiURL;
    private Quiz mQuiz;

    private TextView quizTitle;
    private CanvasWebView quizDetails;
    private TextView quizDueDateDetails;
    private Button goToQuizBtn;

    private CanvasCallback<Quiz> quizCanvasCallback;
    private CanvasWebView.CanvasWebViewClientCallback webViewClientCallback;
    private CanvasWebView.CanvasEmbeddedWebViewCallback embeddedWebViewCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.module_quiz_decider, container, false);
        setupViews(rootView);
        setupCallbacks();

        return rootView;
    }

    private void setupViews(View rootView) {
        quizTitle = (TextView) rootView.findViewById(R.id.quiz_title);
        quizDetails = (CanvasWebView) rootView.findViewById(R.id.quiz_details);
        quizDueDateDetails = (TextView) rootView.findViewById(R.id.quiz_due_details);
        goToQuizBtn = (Button) rootView.findViewById(R.id.goToQuiz);

        goToQuizBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mQuiz != null) {
                    if(!QuizListFragment.isNativeQuiz(getCanvasContext(), mQuiz)) {
                        Navigation navigation = getNavigation();
                        if (navigation != null) {
                            Bundle bundle = BasicQuizViewFragment.createBundle(getCanvasContext(), baseURL, mQuiz);
                            navigation.addFragment(FragUtils.getFrag(BasicQuizViewFragment.class, bundle), true);
                        }
                    } else {
                        //take them to the quiz start fragment instead, let them take it natively
                        Navigation navigation = getNavigation();
                        if (navigation != null) {
                            Bundle bundle = QuizStartFragment.createBundle(getCanvasContext(), mQuiz);
                            navigation.addFragment(FragUtils.getFrag(QuizStartFragment.class, bundle), true);
                        }
                    }

                }
            }
        });
    }
    private void setupCallbacks() {
        webViewClientCallback = new CanvasWebView.CanvasWebViewClientCallback() {
            @Override
            public void openMediaFromWebView(String mime, String url, String filename) {
                openMedia(mime, url, filename);
            }

            @Override
            public void onPageFinishedCallback(WebView webView, String url) {

            }

            @Override
            public void onPageStartedCallback(WebView webView, String url) {


            }

            @Override
            public boolean canRouteInternallyDelegate(String url) {
                return RouterUtils.canRouteInternally(null, url, APIHelpers.getDomain(getActivity()), false);
            }

            @Override
            public void routeInternallyCallback(String url) {
                RouterUtils.canRouteInternally(getActivity(), url, APIHelpers.getDomain(getActivity()), true);
            }
        };

        embeddedWebViewCallback = new CanvasWebView.CanvasEmbeddedWebViewCallback() {
            @Override
            public void launchInternalWebViewFragment(String url) {

                InternalWebviewFragment.loadInternalWebView(getActivity(), getNavigation(), InternalWebviewFragment.createBundle(getCanvasContext(), url, false));
            }

            @Override
            public boolean shouldLaunchInternalWebViewFragment(String url) {
                return true;
            }
        };

        quizCanvasCallback = new CanvasCallback<Quiz>(this) {
            @Override
            public void cache(Quiz quiz) {

            }

            @Override
            public void firstPage(Quiz quiz, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()) { return; }

                if (quiz != null) {
                    mQuiz = quiz;
                    quizTitle.setText(quiz.getTitle());
                    if (quiz.getDueAt() != null) {
                        quizDueDateDetails.setText(DateHelpers.getDateTimeString(getActivity(), quiz.getDueAt()));
                    } else {
                        quizDueDateDetails.setText(getString(R.string.toDoNoDueDate));
                    }
                    quizDetails.formatHTML(quiz.getDescription(), "");
                    quizDetails.setBackgroundColor(getResources().getColor(R.color.transparent));
                    //set some callbacks in case there is a link in the quiz description. We want it to open up in a new
                    //InternalWebViewFragment
                    quizDetails.setCanvasEmbeddedWebViewCallback(embeddedWebViewCallback);

                    quizDetails.setCanvasWebViewClientCallback(webViewClientCallback);
                }
            }

        };
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        QuizAPI.getDetailedQuizFromURL(apiURL, quizCanvasCallback);
    }

    @Override
    public String getFragmentTitle() {
        return null;
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.DETAIL;
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        baseURL = extras.getString(Const.URL);
        apiURL = extras.getString(Const.API_URL);
    }

    //for module progression we need the api url too
    public static Bundle createBundle(CanvasContext canvasContext, String url, String apiURL) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.URL, url);
        extras.putString(Const.API_URL, apiURL);
        return extras;
    }
}

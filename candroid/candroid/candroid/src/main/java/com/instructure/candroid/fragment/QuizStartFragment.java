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
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instructure.candroid.R;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.util.FragUtils;
import com.instructure.candroid.util.Param;
import com.instructure.candroid.util.RouterUtils;
import com.instructure.candroid.view.CanvasLoading;
import com.instructure.candroid.view.CanvasWebView;
import com.instructure.canvasapi.api.QuizAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Quiz;
import com.instructure.canvasapi.model.QuizSubmission;
import com.instructure.canvasapi.model.QuizSubmissionResponse;
import com.instructure.canvasapi.model.QuizSubmissionTime;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.Const;

import java.util.HashMap;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class QuizStartFragment extends ParentFragment {

    private TextView quizTitle;
    private CanvasWebView quizDetails;

    private TextView quizTurnedIn;
    private TextView quizTimeLimit;

    //detail textViews
    private TextView quizPointsPossibleDetails;
    private TextView quizQuestionCountDetails;
    private TextView quizAttemptDetails;
    private TextView quizDueDateDetails;
    private TextView quizTurnedInDetails;
    private TextView quizUnlockedDetails;
    private TextView quizTimeLimitDetails;

    private RelativeLayout quizUnlockedContainer;
    private RelativeLayout quizTimeLimitContainer;
    private RelativeLayout quizTurnedInContainer;

    private Button viewResults;
    private TextView quizUnlocked;
    private CanvasLoading canvasLoading;

    private Button next;

    private Course course;
    private Quiz quiz;

    private QuizSubmission quizSubmission;
    private boolean shouldStartQuiz = false;
    private boolean shouldLetAnswer = true;
    private QuizSubmissionTime quizSubmissionTime;

    private CanvasCallback<QuizSubmissionResponse> quizSubmissionResponseCanvasCallback;
    private CanvasCallback<Response> quizStartResponseCallback;
    private CanvasCallback<Response> quizStartSessionCallback;
    private CanvasCallback<QuizSubmissionTime> quizSubmissionTimeCanvasCallback;

    private CanvasWebView.CanvasWebViewClientCallback webViewClientCallback;
    private CanvasWebView.CanvasEmbeddedWebViewCallback embeddedWebViewCallback;

    @Override
    public String getFragmentTitle() {
        return getString(R.string.quizzes);
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.DETAIL;
    }

    //Currently there isn't a way to know how to decide if we want to route
    //to this fragment or the BasicQuizViewFragment.
    @Override
    public boolean allowBookmarking() {
        return false;
    }

    @Override
    public HashMap<String, String> getParamForBookmark() {
        HashMap<String, String> map = getCanvasContextParams();
        map.put(Param.QUIZ_ID, Long.toString(quiz.getId()));

        return map;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.quiz_start, container, false);
        setupViews(rootView);
        setupCallbacks();

        return rootView;
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        return quiz != null ? quiz.getTitle() : null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        canvasLoading.setVisibility(View.VISIBLE);
        if(savedInstanceState != null){
            course = (Course)getCanvasContext();
            quiz = savedInstanceState.getParcelable(Const.QUIZ);
        }
        QuizAPI.getFirstPageQuizSubmissions(course, quiz.getId(), quizSubmissionResponseCanvasCallback);
    }

    //called after submitting a quiz
    public void updateQuizInfo() {
        canvasLoading.setVisibility(View.VISIBLE);
        //don't let them try to start the quiz until the data loads
        next.setEnabled(false);
        QuizAPI.getFirstPageQuizSubmissions(course, quiz.getId(), quizSubmissionResponseCanvasCallback);
    }
    private void setupViews(View rootView) {

        quizTitle = (TextView) rootView.findViewById(R.id.quiz_title);
        quizDetails = (CanvasWebView) rootView.findViewById(R.id.quiz_details);

        quizTurnedIn = (TextView) rootView.findViewById(R.id.quiz_turned_in);
        quizUnlocked = (TextView) rootView.findViewById(R.id.quiz_unlocked);
        quizTimeLimit = (TextView) rootView.findViewById(R.id.quiz_time_limit);

        quizPointsPossibleDetails = (TextView) rootView.findViewById(R.id.quiz_points_details);
        quizAttemptDetails = (TextView) rootView.findViewById(R.id.quiz_attempt_details);
        quizQuestionCountDetails = (TextView) rootView.findViewById(R.id.quiz_question_count_details);
        quizDueDateDetails = (TextView) rootView.findViewById(R.id.quiz_due_details);
        quizTurnedInDetails = (TextView) rootView.findViewById(R.id.quiz_turned_in_details);
        quizUnlockedDetails = (TextView) rootView.findViewById(R.id.quiz_unlocked_details);
        quizTimeLimitDetails = (TextView) rootView.findViewById(R.id.quiz_time_limit_details);

        quizTurnedInContainer = (RelativeLayout) rootView.findViewById(R.id.quiz_turned_in_container);
        quizUnlockedContainer = (RelativeLayout) rootView.findViewById(R.id.quiz_unlocked_container);
        quizTimeLimitContainer = (RelativeLayout) rootView.findViewById(R.id.quiz_time_limit_container);

        canvasLoading = (CanvasLoading) rootView.findViewById(R.id.loading);


        next = (Button) rootView.findViewById(R.id.next);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(quiz.isLockedForUser()) {
                    if(quiz.getLockExplanation() != null) {
                        showToast(quiz.getLockExplanation());
                    }

                    return;
                }

                if(shouldStartQuiz) {
                    QuizAPI.startQuiz(course, quiz.getId(), quizStartResponseCallback);
                    //if the user hits the back button, we don't want them to try to start the quiz again
                    shouldStartQuiz = false;
                }
                else if(quizSubmission != null) {
                    showQuiz();
                } else {
                    getLockedMessage();
                }
            }
        });

        viewResults = (Button) rootView.findViewById(R.id.quiz_results);

        viewResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = InternalWebviewFragment.createBundle(course, quiz.getUrl(), false);

                Navigation navigation = getNavigation();
                if(navigation != null){

                    InternalWebviewFragment fragment = FragUtils.getFrag(InternalWebviewFragment.class, bundle);
                    //we don't want it to route internally, it will pop open the sliding drawer and route back the to same place
                    fragment.setShouldRouteInternally(false);
                    navigation.addFragment(fragment);
                }
            }
        });
    }

    public void populateQuizInfo() {
        quizTitle.setText(quiz.getTitle());
        setupTitle(getActionbarTitle());

        quizDetails.formatHTML(quiz.getDescription(), "");
        quizDetails.setBackgroundColor(getResources().getColor(R.color.transparent));
        //set some callbacks in case there is a link in the quiz description. We want it to open up in a new
        //InternalWebViewFragment
        quizDetails.setCanvasEmbeddedWebViewCallback(embeddedWebViewCallback);

        quizDetails.setCanvasWebViewClientCallback(webViewClientCallback);

        quizQuestionCountDetails.setText(Integer.toString(quiz.getQuestionCount()));

        quizPointsPossibleDetails.setText(quiz.getPointsPossible());

        if(quiz.getAllowedAttempts() == -1) {
            quizAttemptDetails.setText(getString(R.string.unlimited));
        } else {
            quizAttemptDetails.setText(Integer.toString(quiz.getAllowedAttempts()));
        }

        if(quiz.getDueAt() != null) {
            quizDueDateDetails.setText(DateHelpers.getDateTimeString(getActivity(), quiz.getDueAt()));
        } else {
            quizDueDateDetails.setText(getString(R.string.toDoNoDueDate));
        }

        if(quiz.getUnlockAt() != null) {
            quizUnlocked.setText(getString(R.string.unlockedAt));
            quizUnlockedDetails.setText(DateHelpers.getDateTimeString(getActivity(), quiz.getUnlockAt()));
        } else {
            quizUnlockedContainer.setVisibility(View.GONE);
        }

        if(quiz.getTimeLimit() != 0) {
            quizTimeLimit.setText(getString(R.string.timeLimit));
            quizTimeLimitDetails.setText(Integer.toString(quiz.getTimeLimit()));
        } else {
            quizTimeLimitContainer.setVisibility(View.GONE);
        }
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

                InternalWebviewFragment.loadInternalWebView(getActivity(), getNavigation(), InternalWebviewFragment.createBundle(course, url, false));
            }

            @Override
            public boolean shouldLaunchInternalWebViewFragment(String url) {
                return true;
            }
        };

        quizSubmissionTimeCanvasCallback = new CanvasCallback<QuizSubmissionTime>(this) {

            @Override
            public void cache(QuizSubmissionTime quizSubmissionTime, LinkHeaders linkHeaders, Response response2) {}

            @Override
            public void firstPage(QuizSubmissionTime quizSubmissionTime, LinkHeaders linkHeaders, Response response) {

                QuizStartFragment.this.quizSubmissionTime = quizSubmissionTime;
                QuizAPI.getFirstPageQuizSubmissions(course, quiz.getId(), quizSubmissionResponseCanvasCallback);
            }
        };
        quizSubmissionResponseCanvasCallback = new CanvasCallback<QuizSubmissionResponse>(this) {

            @Override
            public void firstPage(QuizSubmissionResponse quizSubmissionResponse, LinkHeaders linkHeaders, Response response) {

                if (quizSubmissionResponse != null && quizSubmissionResponse.getQuizSubmissions() == null || quizSubmissionResponse.getQuizSubmissions().size() == 0) {
                    //no quiz submissions, let the user start the quiz.

                    //they haven't turned it in yet, so don't show the view
                    quizTurnedInContainer.setVisibility(View.GONE);
                    shouldStartQuiz = true;

                } else {
                    //we should have at least 1 item in the array due to the check in the if statement above
                    quizSubmission = quizSubmissionResponse.getQuizSubmissions().get(quizSubmissionResponse.getQuizSubmissions().size() - 1);

                    next.setEnabled(true);

                    final boolean hasUnlimitedAttempts = quiz.getAllowedAttempts() == -1;
                    final boolean teacherUnlockedQuizAttempts = quizSubmission.isManuallyUnlocked();
                    final boolean hasMoreAttemptsLeft = quizSubmission.getAttemptsLeft() > 0;

                    final boolean canTakeQuizAgain = hasUnlimitedAttempts | teacherUnlockedQuizAttempts | hasMoreAttemptsLeft;

                    if(quiz.getHideResults() == Quiz.HIDE_RESULTS_TYPE.ALWAYS && !canTakeQuizAgain) {
                        //don't let the user see the questions if they've exceeded their attempts
                        next.setVisibility(View.GONE);
                    } else if(quiz.getHideResults() == Quiz.HIDE_RESULTS_TYPE.AFTER_LAST_ATTEMPT && !canTakeQuizAgain) {
                        //they can only see the results after their last attempt, and that hasn't happened yet
                        next.setVisibility(View.GONE);
                    }

                    // -1 allowed attempts == unlimited
                    if (quizSubmission.getFinishedAt() != null && !canTakeQuizAgain) {

                        //they can't take the quiz anymore, let them see results
                        next.setText(getString(R.string.viewQuestions));
                        shouldLetAnswer = false;

                    } else {
                        //they are allowed to take the quiz...
                        if(quizSubmission.getFinishedAt() != null) {
                            shouldStartQuiz = true;
                            next.setText(getString(R.string.takeQuizAgain));
                        } else {
                            //let the user resume their quiz
                            next.setText(getString(R.string.resumeQuiz));
                        }
                    }

                    if(quizSubmission.getFinishedAt() != null) {
                        quizTurnedIn.setText(getString(R.string.turnedIn));
                        quizTurnedInDetails.setText(DateHelpers.getDateTimeString(getActivity(), quizSubmission.getFinishedAt()));
                        //the user has turned in the quiz, let them see the results
                        viewResults.setVisibility(View.VISIBLE);

                    } else {
                        quizTurnedInContainer.setVisibility(View.GONE);
                    }

                    //weird hack where if the time expires and the user hasn't submitted it doesn't let you start the quiz
                    if(quizSubmission.getWorkflowState() == QuizSubmission.WORKFLOW_STATE.UNTAKEN && (quizSubmission.getEndAt() != null && (quizSubmissionTime != null && quizSubmissionTime.getTimeLeft() > 0))) {
                        next.setEnabled(false);
                        //submit the quiz for them
                        QuizAPI.postQuizSubmit(course, quizSubmission, new CanvasCallback<QuizSubmissionResponse>(QuizStartFragment.this) {
                            @Override
                            public void cache(QuizSubmissionResponse quizSubmissionResponse, LinkHeaders linkHeaders, Response response) {

                            }

                            @Override
                            public void firstPage(QuizSubmissionResponse quizSubmissionResponse, LinkHeaders linkHeaders, Response response) {
                                //the user has turned in the quiz, let them see the results
                                viewResults.setVisibility(View.VISIBLE);
                                next.setEnabled(true);
                                shouldStartQuiz = true;
                                next.setText(getString(R.string.takeQuizAgain));
                                if (quizSubmissionResponse != null && quizSubmissionResponse.getQuizSubmissions() != null && quizSubmissionResponse.getQuizSubmissions().size() > 0) {
                                    quizSubmission = quizSubmissionResponse.getQuizSubmissions().get(quizSubmissionResponse.getQuizSubmissions().size() - 1);
                                }
                            }
                        });
                    }

                    //if the user can only see results once and they have seen it, don't let them view the questions
                    if(quiz.isOneTimeResults() && quizSubmission.hasSeenResults()) {
                        next.setVisibility(View.GONE);
                    }


                    if(quiz.isLockedForUser()) {
                        shouldStartQuiz = false;
                        next.setText(getString(R.string.assignmentLocked));
                    }
                }

                populateQuizInfo();

                canvasLoading.setVisibility(View.GONE);

            }

            @Override
            public void failure(RetrofitError retrofitError) {
                canvasLoading.setVisibility(View.GONE);

                //if a quiz is excused we get a 401 error when trying to get the submissions. This is a workaround until we have an excused field
                //on quizzes.
                if(retrofitError != null && retrofitError.getResponse() != null && retrofitError.getResponse().getStatus() == 401) {
                    populateQuizInfo();
                    //there is a not authorized error, so don't let them start the quiz
                    next.setVisibility(View.GONE);
                    return;
                }
                super.failure(retrofitError);
            }

        };

        quizStartResponseCallback = new CanvasCallback<Response>(this) {
            @Override
            public void cache(Response response, LinkHeaders linkHeaders, Response response2) {}

            @Override
            public void firstPage(Response response, LinkHeaders linkHeaders, Response response2) {
                if(response.getStatus() == 200) {
                    //we want to show the quiz here, but we need to get the quizSubmissionId first so our
                    //api call for the QuizQuestionsFragment knows which questions to get
                    CanvasCallback<QuizSubmissionResponse> quizSubmissionResponseCallback = new CanvasCallback<QuizSubmissionResponse>(QuizStartFragment.this) {

                        @Override
                        public void firstPage(QuizSubmissionResponse quizSubmissionResponse, LinkHeaders linkHeaders, Response response) {
                            if(quizSubmissionResponse != null && quizSubmissionResponse.getQuizSubmissions() != null &&
                                    quizSubmissionResponse.getQuizSubmissions().size() > 0) {
                                quizSubmission = quizSubmissionResponse.getQuizSubmissions().get(quizSubmissionResponse.getQuizSubmissions().size() - 1);
                                if(quizSubmission != null) {
                                    showQuiz();
                                } else {
                                    getLockedMessage();
                                }
                            }
                        }
                    };

                    QuizAPI.getFirstPageQuizSubmissions(course, quiz.getId(), quizSubmissionResponseCallback);

                }
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                if(retrofitError.getResponse() != null && retrofitError.getResponse().getStatus() == 403) {
                    //forbidden
                    //check to see if it's because of IP restriction or bad access code or either
                    getLockedMessage();
                    return true;
                }
                return super.onFailure(retrofitError);
            }
        };

        quizStartSessionCallback = new CanvasCallback<Response>(this) {
            @Override
            public void cache(Response response, LinkHeaders linkHeaders, Response response2) {}

            @Override
            public void firstPage(Response response, LinkHeaders linkHeaders, Response response2) {
                if(response.getStatus() == 204) {
                    //success
                }
                //alerting the user that we couldn't post the start session event doesn't really make sense. If something went wrong the logs will
                //be off on the admin/teacher side
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                super.failure(retrofitError);
            }
        };
    }


    private void getLockedMessage() {
        //check to see if it's because of IP restriction or bad access code or either
        if(quiz.getIPFilter() != null && quiz.getAccessCode() == null) {
            showToast(R.string.lockedIPAddress);
        } else if(quiz.getIPFilter() == null && quiz.getAccessCode() != null) {
            showToast(R.string.lockedInvalidAccessCode);
        } else {
            //something went wrong (no data possibly)
            showToast(R.string.cantStartQuiz);
        }
    }

    private void showQuiz() {
        Navigation navigation = getNavigation();
        if(navigation != null){

            //post the android session started event
            QuizAPI.postQuizStartedEvent(getCanvasContext(), quizSubmission, APIHelpers.getUserAgent(getContext()), quizStartSessionCallback);

            Bundle bundle = QuizQuestionsFragment.createBundle(getCanvasContext(), quiz, quizSubmission, shouldLetAnswer);

            navigation.addFragment(
                    FragUtils.getFrag(QuizQuestionsFragment.class, bundle));
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Const.QUIZ, quiz);
    }

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        if(extras == null){return;}

        course = (Course)getCanvasContext();
        quiz = extras.getParcelable(Const.QUIZ);
    }

    public static Bundle createBundle(CanvasContext canvasContext, Quiz quiz) {
        Bundle extras = createBundle(canvasContext);
        extras.putParcelable(Const.QUIZ, quiz);
        return extras;
    }
}

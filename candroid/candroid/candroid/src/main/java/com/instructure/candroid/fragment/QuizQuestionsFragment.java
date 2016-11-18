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

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instructure.candroid.R;
import com.instructure.candroid.adapter.QuizSubmissionQuestionListRecyclerAdapter;
import com.instructure.candroid.dialog.FileUploadDialog;
import com.instructure.candroid.dialog.QuizQuestionDialog;
import com.instructure.candroid.interfaces.QuizFileUploadListener;
import com.instructure.candroid.util.PreCachingLayoutManager;
import com.instructure.candroid.view.ViewUtils;
import com.instructure.canvasapi.api.QuizAPI;
import com.instructure.canvasapi.model.Attachment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Quiz;
import com.instructure.canvasapi.model.QuizSubmission;
import com.instructure.canvasapi.model.QuizSubmissionQuestion;
import com.instructure.canvasapi.model.QuizSubmissionQuestionResponse;
import com.instructure.canvasapi.model.QuizSubmissionResponse;
import com.instructure.canvasapi.model.QuizSubmissionTime;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.loginapi.login.dialog.GenericDialogStyled;
import com.instructure.pandautils.services.FileUploadService;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit.client.Response;

public class QuizQuestionsFragment extends ParentFragment implements FileUploadDialog.FileUploadStartedInterface {

    private final int MILLISECOND = 1000;
    private final int SECONDS_IN_MINUTE = 60;
    private final int FIRST_WARNING_SECONDS = 30;
    private final int LAST_WARNING_SECONDS = 5;

    private enum AUTO_SUBMIT_REASON { TIMED_QUIZ, DUE_DATE, LOCK_DATE }

    private AUTO_SUBMIT_REASON auto_submit_reason;

    private RecyclerView recyclerView;
    private QuizSubmissionQuestionListRecyclerAdapter quizQuestionAdapter;

    private FileUploadDialog mUploadFileSourceFragment;
    private BroadcastReceiver errorBroadcastReceiver;
    private BroadcastReceiver allUploadsCompleteBroadcastReceiver;

    private Course course;
    private Quiz quiz;
    private QuizSubmission quizSubmission;
    private boolean shouldLetAnswer;
    private boolean shouldShowTimer = true;

    private RelativeLayout timerLayout;
    private TextView timer;
    private Chronometer chronometer;
    private ValueAnimator anim;
    private CountDownTimer countDownTimer;
    private QuizSubmissionTime mQuizSubmissionTime;

    //callback
    private CanvasCallback<QuizSubmissionQuestionResponse> quizSubmissionQuestionResponseCanvasCallback;
    private CanvasCallback<QuizSubmissionResponse> submitQuizCallback;
    private CanvasCallback<QuizSubmissionTime> quizSubmissionTimeCanvasCallback;
    private QuizFileUploadListener quizFileUploadListener;

    @Override
    public String getFragmentTitle() {
        return getString(R.string.quizzes);
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        return quiz != null ? quiz.getTitle() : null;
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.DETAIL;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(this, true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null){
            course = (Course)getCanvasContext();
            quiz = savedInstanceState.getParcelable(Const.QUIZ);
            quizSubmission = savedInstanceState.getParcelable(Const.QUIZ_SUBMISSION);
            shouldLetAnswer = savedInstanceState.getBoolean(Const.QUIZ_SHOULD_LET_ANSWER);
        }

        if(quizQuestionAdapter != null) {
            //we might be coming back from a link that opened a webview, and we don't want to
            //add duplicates.
            quizQuestionAdapter.clear();
        }

        if(quizSubmission != null && !(quiz.getRequireLockdownBrowserForResults())) {
            QuizAPI.getFirstPageSubmissionQuestions(quizSubmission.getId(), quizSubmissionQuestionResponseCanvasCallback);
        } else {
            showToast(R.string.cantStartQuiz);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.quiz_list, container, false);

        setupTitle(getActionbarTitle());
        setupViews(rootView);
        setupCallbacks();

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        QuizAPI.getQuizSubmissionTime(getCanvasContext(), quizSubmission, quizSubmissionTimeCanvasCallback);
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceivers();
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceivers();
    }


    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);

        Drawable d = CanvasContextColor.getColoredDrawable(getActivity(), R.drawable.ic_av_timer_white, getResources().getColor(R.color.white));
        menu.add(Menu.NONE, R.id.toggleTimer, 0, R.string.toggleTimer)
                .setIcon(d)
                .setTitle(getString(R.string.toggleTimer))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        d = CanvasContextColor.getColoredDrawable(getActivity(), R.drawable.ic_bookmark_outline_white, getResources().getColor(R.color.white));
        menu.add(Menu.NONE, R.id.showFlaggedQuestions, 1, R.string.showFlaggedQuestions)
                .setIcon(d)
                .setTitle(getString(R.string.showFlaggedQuestions))
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.toggleTimer) {
            if (shouldShowTimer) {
                shouldShowTimer = false;

                //setup animation
                //hide the timer by making the height 0
                if (anim == null) {
                    anim = ValueAnimator.ofInt(timerLayout.getMeasuredHeight(), 0);
                    anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            int val = (Integer) valueAnimator.getAnimatedValue();
                            ViewGroup.LayoutParams layoutParams = timerLayout.getLayoutParams();
                            layoutParams.height = val;
                            timerLayout.setLayoutParams(layoutParams);
                        }
                    });
                    anim.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
                }
                anim.start();


            } else {
                shouldShowTimer = true;

                //show the timer again by increasing the height
                anim.reverse();
            }
        } else if(item.getItemId() == R.id.showFlaggedQuestions) {
            if(quizQuestionAdapter == null) {
                showToast(R.string.errorOccurred);
                return true;
            }
            ArrayList<QuizSubmissionQuestion> questions = new ArrayList<>();

            for(int i = 0; i < quizQuestionAdapter.size(); i++) {
                questions.add(quizQuestionAdapter.getItemAtPosition(i));
            }

            QuizQuestionDialog dialog = QuizQuestionDialog.newInstance(quizQuestionAdapter.getAnsweredQuestions(), questions, course);
            dialog.setLayoutManager(recyclerView.getLayoutManager());
            dialog.show(getActivity().getSupportFragmentManager(), QuizQuestionDialog.TAG);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViews(View rootView) {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        //Setup layout manager
        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setExtraLayoutSpace(ViewUtils.getWindowHeight(getActivity()));

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        timerLayout = (RelativeLayout) rootView.findViewById(R.id.timer_layout);
        timer = (TextView) rootView.findViewById(R.id.timer);
        chronometer = (Chronometer) rootView.findViewById(R.id.chronometer);
    }

    private void setupCallbacks() {
        quizFileUploadListener = new QuizFileUploadListener() {
            @Override
            public void onFileUploadClicked(long quizQuestionId, int position) {
                Bundle bundle = FileUploadDialog.createQuizFileBundle(quizQuestionId, course.getId(), quiz.getId(), position);
                mUploadFileSourceFragment = FileUploadDialog.newInstance(getChildFragmentManager(),bundle);
                mUploadFileSourceFragment.setTargetFragment(QuizQuestionsFragment.this, 1234);
                mUploadFileSourceFragment.show(getChildFragmentManager(), FileUploadDialog.TAG);
            }
        };

        quizSubmissionQuestionResponseCanvasCallback = new CanvasCallback<QuizSubmissionQuestionResponse>(this) {

            @Override
            public void cache(QuizSubmissionQuestionResponse quizSubmissionQuestionResponse, LinkHeaders linkHeaders, Response response) {}

            @Override
            public void firstPage(QuizSubmissionQuestionResponse quizSubmissionQuestionResponse, LinkHeaders linkHeaders, Response response) {

                List<QuizSubmissionQuestion> questions = quizSubmissionQuestionResponse.getQuizSubmissionQuestions();

                //sort the questions based on position that is part of the model object
                if(questions != null) {
                    Collections.sort(questions, new Comparator<QuizSubmissionQuestion>() {
                        @Override
                        public int compare(QuizSubmissionQuestion lh, QuizSubmissionQuestion rh) {
                            if(lh != null && rh != null) {
                                if(lh.getPosition() < rh.getPosition()) {
                                    return -1;
                                } else if(lh.getPosition() > rh.getPosition()) {
                                    return 1;
                                }
                            }
                            return 0;
                        }
                    });
                }
                if(quizQuestionAdapter == null) {
                    quizQuestionAdapter = new QuizSubmissionQuestionListRecyclerAdapter(getActivity(),
                            questions, course, shouldLetAnswer, quizSubmission, quizFileUploadListener);
                } else {
                    quizQuestionAdapter.addAll(questions);
                }
                //cache the views after we view them
                recyclerView.setItemViewCacheSize(quizSubmissionQuestionResponse.getQuizSubmissionQuestions().size());

                recyclerView.setAdapter(quizQuestionAdapter);

                if(linkHeaders.nextURL != null) {
                    QuizAPI.getNextPageSubmissionQuestions(linkHeaders.nextURL, quizSubmissionQuestionResponseCanvasCallback);
                }
            }
        };

        submitQuizCallback = new CanvasCallback<QuizSubmissionResponse>(this) {

            @Override
            public void cache(QuizSubmissionResponse quizSubmissionResponse, LinkHeaders linkHeaders, Response response) {}

            @Override
            public void firstPage(QuizSubmissionResponse quizSubmissionResponse, LinkHeaders linkHeaders, Response response) {
                //submitted! Let the user know.
                switch (auto_submit_reason) {
                    case TIMED_QUIZ:
                        showToast(R.string.submitReasonTimedQuiz);
                        break;
                    case  DUE_DATE:
                        showToast(R.string.quizSubmittedSuccessfully);
                        break;
                    case LOCK_DATE:
                        showToast(R.string.submitReasonLockAt);
                        break;
                    default:
                        showToast(R.string.quizSubmittedSuccessfully);

                }
                if(chronometer != null) {
                    chronometer.stop();
                }
                if(countDownTimer != null) {
                    countDownTimer.cancel();
                }

                getActivity().onBackPressed();

            }
        };

        quizSubmissionTimeCanvasCallback = new CanvasCallback<QuizSubmissionTime>(this) {
            @Override
            public void cache(QuizSubmissionTime quizSubmissionTime, LinkHeaders linkHeaders, Response response) {

            }

            @Override
            public void firstPage(QuizSubmissionTime quizSubmissionTime, LinkHeaders linkHeaders, Response response) {

                mQuizSubmissionTime = quizSubmissionTime;
                if(shouldLetAnswer && isAdded()) {

                    if (quiz.getTimeLimit() == 0 && quiz.getDueAt() == null && quiz.getLockAt() == null) {
                        normalTimer();

                    } else if (quiz.getTimeLimit() > 0) {
                        timeLimitCountDown(quizSubmissionTime.getTimeLeft());
                    } else if (quiz.getDueAt() != null && (quiz.getLockAt() == null || quiz.getDueAt().before(quiz.getLockAt()))) {
                        //if we have a due date, we want to give them an option to submit it when it gets to that point, but only if the due date is before the lock date
                        auto_submit_reason = AUTO_SUBMIT_REASON.DUE_DATE;

                        countDownForSubmit(quizSubmissionTime.getTimeLeft() * MILLISECOND);

                    } else if (quiz.getLockAt() != null) {
                        auto_submit_reason = AUTO_SUBMIT_REASON.LOCK_DATE;

                        countDownForSubmit(quizSubmissionTime.getTimeLeft() * MILLISECOND);
                    }
                } else {
                    int minutes = (int)Math.ceil((double)(quizSubmission.getTimeSpent()) / 60);

                    chronometer.setVisibility(View.GONE);
                    timer.setText(String.format(Locale.getDefault(), getString(R.string.timeSpentFormat), minutes));
                }
            }
        };

    }

    //just count up, no due date, no lock date
    private void normalTimer() {
        timer.setVisibility(View.GONE);
        //count up if they haven't turned it in

        chronometer.setVisibility(View.VISIBLE);

        chronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer mChronometer) {

                long elapsed = System.currentTimeMillis() - quizSubmission.getStartedAt().getTime();
                // if the user has set a custom time the date on their device could be set to before the started time of the quiz.
                // if this is the case then just start from 0.
                if(elapsed >= 0) {
                    long seconds = elapsed / 1000;
                    long minutes = seconds / 60;
                    long hours = minutes / 60;
                    long days = hours / 24;
                    String timeValue;
                    if (days == 1) {
                        timeValue = String.format("%d %s %02d:%02d:%02d", days, getString(R.string.calendarDay), hours % 24, minutes % SECONDS_IN_MINUTE, seconds % SECONDS_IN_MINUTE);
                    } else if (days > 1) {
                        timeValue = String.format("%d %s %02d:%02d:%02d", days, getString(R.string.days), hours % 24, minutes % SECONDS_IN_MINUTE, seconds % SECONDS_IN_MINUTE);
                    } else {
                        timeValue = String.format("%02d:%02d:%02d", hours, minutes, seconds % SECONDS_IN_MINUTE);
                    }
                    chronometer.setText(timeValue);
                }
            }
        });
        chronometer.start();
    }

    //quiz has a time limit, count down
    private void timeLimitCountDown(int seconds) {
        chronometer.setVisibility(View.GONE);
        timer.setVisibility(View.VISIBLE);
        //need to start a countdown

        //a quiz might be overdue, but they can still take it again. In this case we'll set the timer to be
        //the time limit
        if (seconds < 0) {
            seconds = quiz.getTimeLimit() *  MILLISECOND;
        } else {
            seconds = seconds * MILLISECOND;
        }
        countDownTimer = new CountDownTimer(seconds, MILLISECOND) {

            public void onTick(long millisUntilFinished) {

                //subtract the warning seconds. We try to submit the quiz at 5 seconds, but the user might try to wait until the last second
                //to submit it, and this would lead to unexpected behavior. So we'll subtract 5 seconds from the timer so it will submit when
                //the timer reaches 0
                int seconds = (int) (millisUntilFinished / MILLISECOND) - LAST_WARNING_SECONDS;
                String minutesString = String.format("%02d:%02d", seconds / SECONDS_IN_MINUTE, seconds % SECONDS_IN_MINUTE);
                timer.setText(minutesString);

                //if we're at 30 seconds, warn the user
                if (seconds == FIRST_WARNING_SECONDS) {
                    showToast(R.string.thirtySecondWarning);
                }
                //there are actually 5 seconds left, but the timer will show 0 seconds because we subtract it when we set the variable "seconds"
                if (seconds == 0) {
                    //auto-submit the quiz.
                    QuizAPI.postQuizSubmit(course, quizSubmission, submitQuizCallback);
                    auto_submit_reason = AUTO_SUBMIT_REASON.TIMED_QUIZ;
                    showToast(R.string.autoSubmitting);
                    if(countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                }
            }

            public void onFinish() {
                timer.setText(getString(R.string.done));
            }
        }.start();
    }

    private void countDownForSubmit(long minutesMS) {
        //count up
        normalTimer();

        countDownTimer = new CountDownTimer(minutesMS, MILLISECOND) {

            public void onTick(long millisUntilFinished) {

                int seconds = (int) (millisUntilFinished / MILLISECOND) - LAST_WARNING_SECONDS;

                //if we're at 30 seconds, warn the user
                if (seconds == FIRST_WARNING_SECONDS - LAST_WARNING_SECONDS) {
                    showToast(R.string.thirtySecondWarning);
                    // if it's a due date, let them decide if they want to submit, so show a dialog
                    if(auto_submit_reason == AUTO_SUBMIT_REASON.DUE_DATE) {
                        GenericDialogStyled dialogStyled = GenericDialogStyled.newInstance(true, R.string.almostDue, R.string.almostDueMsg, R.string.logout_yes, R.string.logout_no, R.drawable.ic_cv_alert, new GenericDialogStyled.GenericDialogListener() {
                            @Override
                            public void onPositivePressed() {
                                //submit the quiz
                                QuizAPI.postQuizSubmit(course, quizSubmission, submitQuizCallback);
                            }

                            @Override
                            public void onNegativePressed() {
                                //do nothing
                            }
                        });
                        dialogStyled.setCancelable(true);
                        dialogStyled.show((getActivity()).getSupportFragmentManager(), "tag");
                    }
                }
                if (seconds == 0) {

                    //auto-submit the quiz if it's a lock_date type. We don't auto submit at the due date
                    if(auto_submit_reason == AUTO_SUBMIT_REASON.LOCK_DATE) {
                        QuizAPI.postQuizSubmit(course, quizSubmission, submitQuizCallback);
                        showToast(R.string.autoSubmitting);
                        if(countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                    }
                }
            }

            public void onFinish() {
                timer.setText(getString(R.string.done));
            }
        }.start();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(mUploadFileSourceFragment != null){
            mUploadFileSourceFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void registerReceivers() {
        errorBroadcastReceiver = getErrorReceiver();
        allUploadsCompleteBroadcastReceiver = getAllUploadsCompleted();

        getActivity().registerReceiver(errorBroadcastReceiver, new IntentFilter(FileUploadService.UPLOAD_ERROR));
        getActivity().registerReceiver(allUploadsCompleteBroadcastReceiver, new IntentFilter(FileUploadService.QUIZ_UPLOAD_COMPLETE));
    }

    private void unregisterReceivers() {
        if(getActivity() == null){return;}

        if(errorBroadcastReceiver != null){
            getActivity().unregisterReceiver(errorBroadcastReceiver);
            errorBroadcastReceiver = null;
        }

        if(allUploadsCompleteBroadcastReceiver != null){
            getActivity().unregisterReceiver(allUploadsCompleteBroadcastReceiver);
            allUploadsCompleteBroadcastReceiver = null;
        }
    }

    //Creates new discussion reply with attachments for the user
    private BroadcastReceiver getAllUploadsCompleted() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if(!isAdded()){return;}
                if(intent != null && intent.hasExtra(Const.ATTACHMENT)) {
                    long questionId = intent.getLongExtra(Const.QUIZ_ANSWER_ID, -1);
                    int position = intent.getIntExtra(Const.POSITION, -1);
                    Attachment attachment = (Attachment)intent.getExtras().get(Const.ATTACHMENT);
                    if (attachment != null && questionId != -1) {
                        quizQuestionAdapter.setFileUploadForQuiz(questionId, attachment, position);
                    }
                }
            }
        };
    }

    private BroadcastReceiver getErrorReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if(!isAdded()){return;}

                final Bundle bundle = intent.getExtras();
                String errorMessage = bundle.getString(Const.MESSAGE);
                if(null == errorMessage || "".equals(errorMessage)){
                    errorMessage = getString(R.string.errorUploadingFile);
                }
                showToast(errorMessage);

                long questionId = intent.getLongExtra(Const.QUIZ_ANSWER_ID, -1);
                int position = intent.getIntExtra(Const.POSITION, -1);
                if(questionId != -1){
                    quizQuestionAdapter.setIsLoading(questionId, false, position);
                }

                //todo Update progress for upload file?
            }
        };
    }

    @Override
    public void onFileUploadStarted(long questionId, int position) {
        //update the adapter item
        quizQuestionAdapter.setIsLoading(questionId, true, position);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(Const.QUIZ, quiz);
        outState.putParcelable(Const.QUIZ_SUBMISSION, quizSubmission);
        outState.putBoolean(Const.QUIZ_SHOULD_LET_ANSWER, shouldLetAnswer);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        if(extras == null){return;}

        course = (Course)getCanvasContext();
        quiz = extras.getParcelable(Const.QUIZ);
        quizSubmission = extras.getParcelable(Const.QUIZ_SUBMISSION);
        shouldLetAnswer = extras.getBoolean(Const.QUIZ_SHOULD_LET_ANSWER);
    }

    public static Bundle createBundle(CanvasContext canvasContext, Quiz quiz, QuizSubmission quizSubmission, boolean shouldLetAnswer) {
        Bundle extras = createBundle(canvasContext);
        extras.putParcelable(Const.QUIZ, quiz);
        extras.putParcelable(Const.QUIZ_SUBMISSION, quizSubmission);
        extras.putBoolean(Const.QUIZ_SHOULD_LET_ANSWER, shouldLetAnswer);
        return extras;
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}

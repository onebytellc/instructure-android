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

package com.instructure.androidpolling.app.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.devspark.appmsg.AppMsg;
import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.model.AnswerValue;
import com.instructure.androidpolling.app.rowfactories.StudentPollResultsRowFactory;
import com.instructure.androidpolling.app.rowfactories.StudentPollRowFactory;
import com.instructure.androidpolling.app.util.ApplicationManager;
import com.instructure.androidpolling.app.util.Constants;
import com.instructure.canvasapi.api.PollChoiceAPI;
import com.instructure.canvasapi.api.PollSessionAPI;
import com.instructure.canvasapi.api.PollSubmissionAPI;
import com.instructure.canvasapi.model.Poll;
import com.instructure.canvasapi.model.PollChoice;
import com.instructure.canvasapi.model.PollChoiceResponse;
import com.instructure.canvasapi.model.PollSession;
import com.instructure.canvasapi.model.PollSessionResponse;
import com.instructure.canvasapi.model.PollSubmission;
import com.instructure.canvasapi.model.PollSubmissionResponse;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;

import java.util.List;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class StudentPollFragment extends ParentFragment {

    @BindView(R.id.poll_question)
    TextView pollQuestion;

    @BindView(R.id.listView)
    ListView listView;

    @BindView(R.id.submit_poll)
    Button submitPoll;

    @BindView(R.id.rootView)
    RelativeLayout rootLayout;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    private Poll poll;
    private PollSession pollSession;
    private TreeSet<AnswerValue> answers;
    //check to see if there is a correct answer
    boolean hasCorrectAnswer = false;

    private AnswerAdapter answerAdapter;
    private boolean showResults = false;
    private boolean hasSubmitted = false;
    private boolean isPublished = false;

    private PollSubmission pollSubmission;
    private int totalResults = 0;

    //callbacks
    private CanvasCallback<PollChoiceResponse> pollChoiceCallback;
    private CanvasCallback<PollSubmissionResponse> pollSubmissionCallback;
    private CanvasCallback<PollSessionResponse> pollSessionCallback;

    public static final String TAG = "StudentPollFragment";
    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle Overrides
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_student_poll, container, false);
        ButterKnife.bind(this, rootView);
        setupViews();


        setupCallbacks();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        handleArguments(getArguments());

        if(poll != null) {
            updateViews(poll);
            PollChoiceAPI.getFirstPagePollChoices(poll.getId(), pollChoiceCallback);

        }
        if(pollSession != null) {
            showResults = pollSession.has_public_results();
            isPublished = pollSession.is_published();
            getTotalResults();
        }
        if(hasSubmitted || !isPublished) {
            updateViewsSubmitted();
        }
        //set an animation for adding list items
        LayoutAnimationController controller
                = AnimationUtils.loadLayoutAnimation(
                getActivity(), R.anim.list_layout_controller);

        listView.setLayoutAnimation(controller);
        setupListeners();

    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    private void setupViews() {

        answers = new TreeSet<AnswerValue>();
        answerAdapter = new AnswerAdapter(getActivity(), answers);
        listView.setAdapter(answerAdapter);
//        swipeRefreshLayout.setColorScheme(R.color.polling_aqua, R.color.polling_green, R.color.polling_purple, R.color.canvaspollingtheme_color);
    }

    private void setupListeners() {
        submitPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //make sure the user has something selected and get which item the user selected
                boolean hasSelected = false;
                pollSubmission = new PollSubmission();
                for(int i = 0; i < answerAdapter.getCount(); i++) {
                    AnswerValue answerValue = (AnswerValue)answerAdapter.getItem(i);
                    if(answerValue.isSelected()) {
                        hasSelected = true;
                        //API call to submit poll
                        PollSubmissionAPI.createPollSubmission(poll.getId(), pollSession.getId(), answerValue.getPollChoiceId(), pollSubmissionCallback);
                        break;
                    }
                }

                if(!hasSelected) {
                    AppMsg.makeText(getActivity(), getString(R.string.mustSelect), AppMsg.STYLE_WARNING).show();
                    return;
                }


            }
        });

        if(isPublished && !hasSubmitted) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    ((AnswerValue) answerAdapter.getItem(position)).setSelected(true);
                    //uncheck all the other ones
                    for (int i = 0; i < answerAdapter.getCount(); i++) {
                        ((AnswerValue) answerAdapter.getItem(i)).setSelected(false);
                    }
                    //make sure this one is checked
                    ((AnswerValue) answerAdapter.getItem(position)).setSelected(true);
                    answerAdapter.notifyDataSetChanged();
                }
            });
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //cancel the callbacks in case the user pulls to refresh A LOT
                pollChoiceCallback.cancel();
                pollSessionCallback.cancel();
                setupCallbacks();

                totalResults = 0;
                //API call to check if we can show results
                PollSessionAPI.getSinglePollSession(poll.getId(), pollSession.getId(), pollSessionCallback);
                PollChoiceAPI.getFirstPagePollChoices(poll.getId(), pollChoiceCallback);
                swipeRefreshLayout.setRefreshing(false);

            }
        });
    }

    private void shouldShowResults() {
        if(showResults && hasSubmitted) {
            //now show the actual results of the poll
            answerAdapter.notifyDataSetChanged();
        }
        else if(!showResults && hasSubmitted) {
            //make all items not selected disabled
            listView.setOnItemClickListener(null);

            answerAdapter.notifyDataSetChanged();

        }
    }

    private void updateViews(Poll poll) {

        pollQuestion.setText(poll.getQuestion());
    }

    /**
     * Update the views if the student has already submitted the poll
     */
    private void updateViewsSubmitted() {
        //make the submit button tell the user they've submitted the poll
        if(hasSubmitted) {
            submitPoll.setText(getString(R.string.alreadyAnswered));
        }
        else {
            submitPoll.setText(getString(R.string.closedPoll));
        }
        submitPoll.setEnabled(false);
        submitPoll.setClickable(false);

        //make the background white
        rootLayout.setBackgroundColor(getResources().getColor(R.color.white));

        //remove the background on the question
        pollQuestion.setBackgroundColor(getResources().getColor(R.color.white));

        shouldShowResults();
    }

    private void addAnswer(String answer, long id, boolean isCorrect, int position) {
        AnswerValue value = new AnswerValue();
        value.setValue(answer);
        value.setPollChoiceId(id);
        value.setCorrect(isCorrect);
        value.setPosition(position);
        answerAdapter.addItem(value);
    }

    private void getTotalResults() {
        totalResults = 0;
        if(pollSession.getResults() != null) {
            for (Integer count : pollSession.getResults().values()) {
                totalResults += count;
            }
            answerAdapter.notifyDataSetChanged();
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // Adapter
    ///////////////////////////////////////////////////////////////////////////

    private class AnswerAdapter extends BaseAdapter {
        private Context context;
        private TreeSet<AnswerValue> views;
        private LayoutInflater layoutInflater;
        private AnswerValue[] cachedItems;
        private boolean invalid = false;

        public AnswerAdapter(Activity context, TreeSet<AnswerValue> views) {
            this.context = context;
            this.views = views;
            layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void addItem(AnswerValue choice) {
            invalid = true;
            boolean added = answers.add(choice);
            // replace it with the new one
            if (!added) {
                answers.remove(choice);
                answers.add(choice);
            }
            notifyDataSetChanged();
        }

        private AnswerValue itemAtPosition(int position) {
            if (invalid || cachedItems == null) {
                cachedItems = answers.toArray(new AnswerValue[answers.size()]);
                invalid = false;
            }
            if (position < answers.size()) {
                return cachedItems[position];
            }
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (showResults && hasSubmitted) {
                return 0;
            }
            return 1;
        }

        @Override
        public int getCount() {
            return answers.size();
        }

        @Override
        public Object getItem(int i) {
            return itemAtPosition(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup viewGroup) {

            if (getItemViewType(position) == 0) {
                boolean selected = false;
                //see if this answer is selected
                AnswerValue answerValue = itemAtPosition(position);
                if (answerValue.getPollChoiceId() == ApplicationManager.getPollSubmissionId(getActivity(), pollSession.getId())) {
                    selected = true;
                }
                //get the number answered
                float numAnswered = 0;
                if(pollSession.getResults().containsKey(answerValue.getPollChoiceId()) && totalResults > 0) {
                    numAnswered = (float)(pollSession.getResults().get(answerValue.getPollChoiceId())) / totalResults;
                }

                return StudentPollResultsRowFactory.buildRowView(layoutInflater, getActivity(), answerValue.getValue(), (int)(numAnswered*100), answerValue.isCorrect(), selected, hasCorrectAnswer, convertView, position);
            } else {

                return StudentPollRowFactory.buildRowView(layoutInflater, getActivity(), itemAtPosition(position), position, hasSubmitted, pollSession.is_published(), convertView);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Callbacks
    ///////////////////////////////////////////////////////////////////////////

    private void setupCallbacks() {
        pollChoiceCallback = new CanvasCallback<PollChoiceResponse>(this) {
            @Override
            public void cache(PollChoiceResponse pollChoiceResponse) {

            }

            @Override
            public void firstPage(PollChoiceResponse pollChoiceResponse, LinkHeaders linkHeaders, Response response) {
                List<PollChoice> pollChoices = pollChoiceResponse.getPollChoices();
                if(pollChoices != null) {
                    for (PollChoice pollChoice : pollChoices) {
                        addAnswer(pollChoice.getText(), pollChoice.getId(), pollChoice.is_correct(), pollChoice.getPosition());
                        if(pollChoice.is_correct()) {
                            hasCorrectAnswer = true;
                        }
                    }

                    //if this has already been submitted, we want to have the poll choice that the user selected actually selected
                    if(hasSubmitted) {
                        long checkedId = ApplicationManager.getPollSubmissionId(getActivity(), pollSession.getId());
                        if(checkedId != -1) {
                            for(AnswerValue answerValue : answers) {
                                if(answerValue.getPollChoiceId() == checkedId) {
                                    answerValue.setSelected(true);
                                    break;
                                }
                            }
                        }
                    }
                }
                if(linkHeaders.nextURL != null) {
                    PollChoiceAPI.getNextPagePollChoices(linkHeaders.nextURL, pollChoiceCallback);
                }
            }
        };

        pollSubmissionCallback = new CanvasCallback<PollSubmissionResponse>(this) {
            @Override
            public void cache(PollSubmissionResponse pollSubmissionResponse) {

            }

            @Override
            public void firstPage(PollSubmissionResponse pollSubmissionResponse, LinkHeaders linkHeaders, Response response) {
                //successful submission, let the user know
                AppMsg.makeText(getActivity(), getString(R.string.successfullySubmitted), AppMsg.STYLE_SUCCESS).show();

                //save the actual poll submission so we know which id they selected.
                ApplicationManager.savePollSubmission(getActivity(), pollSession.getId(), pollSubmissionResponse.getPollSubmissions().get(0).getPoll_choice_id());

                hasSubmitted = true;
                updateViewsSubmitted();

                //We need to update the session so that we know how many users have submitted and their answers if the teacher has chosen to share results
                PollSessionAPI.getSinglePollSession(poll.getId(), pollSession.getId(), pollSessionCallback);

                getActivity().setResult(Constants.SUBMIT_POLL_SUCCESS);
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                AppMsg.makeText(getActivity(), getString(R.string.errorSubmittingPoll), AppMsg.STYLE_ERROR).show();
                return super.onFailure(retrofitError);
            }
        };

        pollSessionCallback = new CanvasCallback<PollSessionResponse>(this) {
            @Override
            public void cache(PollSessionResponse pollSessionResponse) {

            }

            @Override
            public void firstPage(PollSessionResponse pollSessionResponse, LinkHeaders linkHeaders, Response response) {
                List<PollSession> pollSessions = pollSessionResponse.getPollSessions();
                if(pollSessions != null) {
                    showResults = pollSessions.get(0).has_public_results();
                    pollSession = pollSessions.get(0);

                    getTotalResults();
                    shouldShowResults();
                    //update the data so user has the most up to date results
                    answerAdapter.notifyDataSetChanged();
                }
            }
        };
    }

    @Override
    public void onCallbackStarted() {

    }

    ///////////////////////////////////////////////////////////////////////////
    // Bundle
    ///////////////////////////////////////////////////////////////////////////

    private void handleArguments(Bundle bundle) {
        if(bundle != null) {
            poll = bundle.getParcelable(Constants.POLL_DATA);
            pollSession = bundle.getParcelable(Constants.POLL_SESSION);
            hasSubmitted = bundle.getBoolean(Constants.HAS_SUBMITTED);
        }
    }

}

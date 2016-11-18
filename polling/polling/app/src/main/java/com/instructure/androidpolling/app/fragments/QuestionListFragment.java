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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.devspark.appmsg.AppMsg;
import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.activities.BaseActivity;
import com.instructure.androidpolling.app.activities.FragmentManagerActivity;
import com.instructure.androidpolling.app.rowfactories.QuestionRowFactory;
import com.instructure.androidpolling.app.util.Constants;
import com.instructure.androidpolling.app.util.SwipeDismissListViewTouchListener;
import com.instructure.androidpolling.app.view.CircleButton;
import com.instructure.canvasapi.api.PollAPI;
import com.instructure.canvasapi.api.PollChoiceAPI;
import com.instructure.canvasapi.api.PollSessionAPI;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Poll;
import com.instructure.canvasapi.model.PollChoice;
import com.instructure.canvasapi.model.PollChoiceResponse;
import com.instructure.canvasapi.model.PollResponse;
import com.instructure.canvasapi.model.PollSession;
import com.instructure.canvasapi.model.PollSessionResponse;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class QuestionListFragment extends PaginatedExpandableListFragment<String, Poll> implements APIStatusDelegate{
    //callback
    private CanvasCallback<PollResponse> pollCallback;
    private CanvasCallback<Response> responseCanvasCallback;
    private CanvasCallback<PollSessionResponse> pollSessionCallback;
    private CanvasCallback<PollChoiceResponse> pollChoiceCallback;

    @BindView(R.id.empty_state)
    RelativeLayout emptyState;

    @BindView(R.id.expandableListView)
    ExpandableListView expandableListView;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.addQuestion)
    CircleButton addQuestion;

    private boolean hasTeacherEnrollment;
    private SwipeDismissListViewTouchListener touchListener;

    public static final String TAG = "QuestionListFragment";

    private Map<Long, PollSession> openSessions = new HashMap<Long, PollSession>();
    private Map<Long, PollSession> closedSessions = new HashMap<Long, PollSession>();

    private ArrayList<Poll> pollList = new ArrayList<Poll>();
    private ArrayList<PollChoice> pollChoiceArrayList = new ArrayList<PollChoice>();

    private Poll pollToDelete;
    private Poll selectedPoll;
    private String nextUrl;
    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        //clear the list so we don't get duplicates
        pollList.clear();
        openSessions.clear();
        closedSessions.clear();

        setupClickListeners();
        touchListener =
                new SwipeDismissListViewTouchListener(
                        expandableListView,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    //set the poll that we want to remove after the api call returns successfully
                                    pollToDelete = (Poll)expandableListView.getItemAtPosition(position);

                                    confirmDelete();

                                }

                            }
                        });
        expandableListView.setOnTouchListener(touchListener);

        expandableListView.setOnScrollListener(touchListener.makeScrollListener());

        //set an animation for adding list items
        LayoutAnimationController controller
                = AnimationUtils.loadLayoutAnimation(
                getActivity(), R.anim.list_layout_controller);

        expandableListView.setLayoutAnimation(controller);

        ((BaseActivity)getActivity()).setActionBarTitle(getString(R.string.pollQuestions));
    }


    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    private void setupClickListeners() {
        addQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open the add question fragment
                AddQuestionFragment addQuestionFragment = new AddQuestionFragment();
                ((FragmentManagerActivity)getActivity()).swapFragments(addQuestionFragment, AddQuestionFragment.TAG, R.anim.slide_in_from_bottom, 0, 0, R.anim.slide_out_to_bottom);
            }
        });
    }
    //we need to know if the user is a teacher in any course
    private void checkEnrollments(Course[] courses) {

        for(Course course: courses) {
            if(course.isTeacher()) {
                hasTeacherEnrollment = true;
                //update the actionbar so the icon shows if we need it
                getActivity().invalidateOptionsMenu();
                return;
            }
        }
        hasTeacherEnrollment = false;
        //update the actionbar so the icon shows if we need it
        getActivity().invalidateOptionsMenu();
    }
    private void displayEmptyState() {
        emptyState.setVisibility(View.VISIBLE);
    }

    //make the teacher confirm that they want to delete the poll
    private void confirmDelete() {
        AlertDialog confirmDeleteDialog =new AlertDialog.Builder(getActivity())
                //set message, title, and icon
                .setTitle(getString(R.string.delete))
                .setMessage(getString(R.string.confirmDelete))
                .setIcon(R.drawable.ic_cv_delete)

                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //remove the item from the list
                        removeItem(pollToDelete);

                        //delete the poll from canvas
                        PollAPI.deletePoll(pollToDelete.getId(), responseCanvasCallback);
                        dialog.dismiss();

                        //if there are any empty groups we want to remove them
                        removeEmptyGroups();
                        //check if all the items are gone
                        if(getGroupCount() == 0) {
                            //show the empty state again
                            displayEmptyState();
                        }
                    }

                })

                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        pollToDelete = null;
                        dialog.dismiss();
                    }
                })
                .create();

        confirmDeleteDialog.show();

    }
    ///////////////////////////////////////////////////////////////////////////
    // Update Poll
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void updatePoll(Poll poll) {
        //add the poll to the top of the list.
        //after we have apis here we may want to just pull to refresh to get the latest data
        reloadData();
    }


    ///////////////////////////////////////////////////////////////////////////
    // Overridden methods from PaginatedListFragment
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void configureViews(View rootView) {
        ButterKnife.bind(this, rootView);
    }


    @Override
    public int getRootLayoutCode() {
        return R.layout.fragment_question_list;
    }

    @Override
    public View getRowViewForItem(Poll item, View convertView, int groupPosition, int childPosition, boolean isLastRowInGroup, boolean isLastRow) {
        boolean hasActiveSession = false;
        if(openSessions.containsKey(item.getId())) {
            hasActiveSession = true;
        }
        return QuestionRowFactory.buildRowView(getLayoutInflater(), getActivity(), item.getQuestion(), hasActiveSession, convertView);
    }

    @Override
    public View getGroupViewForItem(String groupItem, View convertView, int groupPosition, boolean isExpanded) {
        return QuestionRowFactory.buildGroupView(getLayoutInflater(), groupItem, convertView);
    }

    @Override
    public boolean areGroupsSorted() {
        return true;
    }

    @Override
    public boolean areGroupsReverseSorted() {
        return false;
    }

    @Override
    protected boolean areGroupsCollapsible() {
        return true;
    }

    @Override
    public int getFooterLayoutCode() {
        return 0;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public boolean onRowClick(Poll item) {
        //if the poll is in the draft section, we want to take the user to the edit poll screen
        if(!openSessions.containsKey(item.getId()) && !closedSessions.containsKey(item.getId())) {
            selectedPoll = item;
            pollChoiceArrayList.clear();
            PollChoiceAPI.getFirstPagePollChoices(selectedPoll.getId(), pollChoiceCallback);
            return true;
        }

        //send the poll data to the results screen
        PollSessionListFragment pollSessionListFragment = new PollSessionListFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.POLL_DATA, item);
        pollSessionListFragment.setArguments(bundle);
        ((FragmentManagerActivity)getActivity()).swapFragments(pollSessionListFragment, PollSessionListFragment.TAG);

        return true;
    }

    @Override
    public boolean areItemsSorted() {
        return true;
    }

    @Override
    public void loadFirstPage() {
        PollAPI.getFirstPagePoll(pollCallback);
    }

    @Override
    public void loadNextPage(String nextURL) {
        PollAPI.getNextPagePoll(nextURL, pollCallback);
    }

    @Override
    public String getNextURL() {
        return nextUrl;
    }

    @Override
    public void setNextURLNull() {
        nextUrl = null;
    }

    @Override
    public void resetData() {
        pollSessionCallback.cancel();
        pollCallback.cancel();
        openSessions.clear();
        pollList.clear();
    }

    @Override
    public void setupCallbacks() {

        pollCallback = new CanvasCallback<PollResponse>(this) {
            @Override
            public void cache(PollResponse pollResponse) {

            }

            @Override
            public void firstPage(PollResponse pollResponse, LinkHeaders linkHeaders, Response response) {
                if(getActivity() == null) return;
                nextUrl = linkHeaders.nextURL;
                if(pollResponse.getPolls().size() == 0) {
                    displayEmptyState();
                }
                else {
                    List<Poll> polls = pollResponse.getPolls();
                    for(Poll poll: polls) {
                        //add all the polls to a list. we'll use the list later to populate the
                        //different groups after we get some session information about each poll
                        pollList.add(poll);
                        PollSessionAPI.getFirstPagePollSessions(poll.getId(), pollSessionCallback);
                    }
                }
            }
        };

        responseCanvasCallback = new CanvasCallback<Response>(this) {
            @Override
            public void cache(Response response) {

            }

            @Override
            public void firstPage(Response response, LinkHeaders linkHeaders, Response response2) {
                //204 means success
                if(response.getStatus() == 204) {
                    if(pollToDelete != null) {
                        //reset it so we don't try to remove it from the list again
                        pollToDelete = null;
                    }
                }
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                AppMsg.makeText(getActivity(), getString(R.string.errorDeletingPoll), AppMsg.STYLE_ERROR).show();
                //we didn't actually delete anything, but we removed the item from the list to make the animation smoother, so now
                //lets get the polls again
                reloadData();
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
                for(PollSession session : pollSessions) {
                    if(session.is_published()) {
                        openSessions.put(session.getPoll_id(), session);
                        //we only care about there being one active poll session
                        break;
                    }
                    else {
                        closedSessions.put(session.getPoll_id(), session);
                    }
                }
                //if the poll has an active session, remove it from the list (from the "inactive" group)
                //and add it to the "active" group
                for(Poll poll : pollList) {
                    if(openSessions.containsKey(poll.getId())) {
                        removeItem(poll);
                        addItem(getString(R.string.active), poll);
                    }
                    //if the poll doesn't have an open session or any closed sessions, it is still in the draft state
                    else if(!closedSessions.containsKey(poll.getId())) {
                        removeItem(poll);
                        addItem(getString(R.string.draft), poll);
                    }
                    else {
                        removeItem(poll);
                        addItem(getString(R.string.inactive), poll);
                    }
                }
                expandAllGroups();
                if(linkHeaders.nextURL != null) {
                    PollSessionAPI.getNextPagePollSessions(linkHeaders.nextURL, pollSessionCallback);
                }
                notifyDataSetChanged();
            }
        };

        pollChoiceCallback = new CanvasCallback<PollChoiceResponse>(this) {
            @Override
            public void cache(PollChoiceResponse pollChoiceResponse) {

            }

            @Override
            public void firstPage(PollChoiceResponse pollChoiceResponse, LinkHeaders linkHeaders, Response response) {
                if (getActivity() == null) return;


                List<PollChoice> pollChoices = pollChoiceResponse.getPollChoices();
                if (pollChoices != null) {
                    pollChoiceArrayList.addAll(pollChoices);
                }

                //if linkHeaders.nextURL is null it means we have all the choices, so we can go to the edit poll page now
                //or generate the CSV, depending on which action they selected
                if (linkHeaders.nextURL == null) {

                    AddQuestionFragment addQuestionFragment = new AddQuestionFragment();
                    //populate the current data with the bundle
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.POLL_BUNDLE, selectedPoll);
                    bundle.putParcelableArrayList(Constants.POLL_CHOICES, pollChoiceArrayList);
                    addQuestionFragment.setArguments(bundle);
                    ((FragmentManagerActivity) getActivity()).swapFragments(addQuestionFragment, AddQuestionFragment.TAG);

                } else {
                    //otherwise, get the next group of poll choices.
                    PollChoiceAPI.getNextPagePollChoices(linkHeaders.nextURL, pollChoiceCallback);
                }

                //onCallbackFinished();
            }
        };
    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {
        if(pollSessionCallback.isFinished() && pollCallback.isFinished()) {
            swipeRefreshLayout.setRefreshing(false);
            super.onCallbackFinished(source);
        }
    }

    @Override
    public void onCallbackStarted() {

    }
}

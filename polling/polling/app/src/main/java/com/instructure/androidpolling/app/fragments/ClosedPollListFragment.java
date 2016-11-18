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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.activities.BaseActivity;
import com.instructure.androidpolling.app.activities.StudentPollActivity;
import com.instructure.androidpolling.app.rowfactories.PollRowFactory;
import com.instructure.androidpolling.app.util.Constants;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.PollAPI;
import com.instructure.canvasapi.api.PollSessionAPI;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Poll;
import com.instructure.canvasapi.model.PollResponse;
import com.instructure.canvasapi.model.PollSession;
import com.instructure.canvasapi.model.PollSessionResponse;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.client.Response;

public class ClosedPollListFragment extends PaginatedListFragment<PollSession> {

    //callback
    private CanvasCallback<PollSessionResponse> pollSessionCallback;
    private CanvasCallback<PollResponse> pollCallback;

    private Map<Long, Course> courseMap;
    private Map<Long, Poll> pollMap;

    public static final String TAG = "ClosedPollListFragment";


    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Constants.SUBMIT_POLL_SUCCESS) {
            //success! we just submitted a poll
            //refresh everything so we have up to date info
            reloadData();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((BaseActivity)getActivity()).setActionBarTitle(getString(R.string.studentView));
    }

    ///////////////////////////////////////////////////////////////////////////
    // PaginatedExpandableListFragment Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void configureViews(View rootView) {

        if(getArguments() != null) {
            ArrayList<Course> courseList = (ArrayList<Course>)getArguments().getSerializable(Constants.COURSES_LIST);
            if(courseList != null) {
                courseMap = CourseAPI.createCourseMap(courseList.toArray(new Course[0]));
            }
            else {
                courseMap = new HashMap<Long, Course>();
            }

        }
        else {
            courseMap = new HashMap<Long, Course>();
        }

        //set an animation for adding list items
        LayoutAnimationController controller
                = AnimationUtils.loadLayoutAnimation(
                getActivity(), R.anim.list_layout_controller);

        getListView().setLayoutAnimation(controller);

        pollMap = new HashMap<Long, Poll>();
    }

    @Override
    public View getRowViewForItem(PollSession item, View convertView, int childPosition) {
        Poll poll = pollMap.get(item.getPoll_id());
        String pollName = "";
        if(poll != null) {
            pollName = poll.getQuestion();
        }
        String courseName = "";
        if(courseMap.containsKey(item.getCourse_id())) {
            courseName = courseMap.get(item.getCourse_id()).getName();
        }
        return PollRowFactory.buildRowView(getLayoutInflater(), courseName, pollName, convertView, getActivity(), item.getCreated_at());
    }

    @Override
    public int getEmptyViewLayoutCode() {
        return R.layout.empty_view_student_polls;
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
    public boolean onRowClick(PollSession item, int position) {
        //we haven't gotten the poll data yet, don't let it go any farther
        if(!pollMap.containsKey(item.getPoll_id())) {
            return true;
        }

        startActivityForResult(StudentPollActivity.createIntent(getActivity(), pollMap.get(item.getPoll_id()), item, item.isHas_submitted()), Constants.SUBMIT_POLL_REQUEST);
        return true;
    }

    @Override
    public boolean areItemsSorted() {
        return true;
    }

    @Override
    public void loadFirstPage() {
         PollSessionAPI.getClosedSessions(pollSessionCallback);
    }

    @Override
    public void loadNextPage(String nextURL) {
    }

    @Override
    public String getNextURL() {
        return null;
    }

    @Override
    public void setNextURLNull() {
    }

    @Override
    public void resetData() {

    }

    @Override
    public void setupCallbacks() {

        pollCallback = new CanvasCallback<PollResponse>(this) {
            @Override
            public void cache(PollResponse pollResponse) {
                List<Poll> polls = pollResponse.getPolls();
                if(polls != null) {
                    for (Poll poll : polls) {
                        pollMap.put(poll.getId(), poll);
                    }
                    //now we have the poll question data, so update the list
                    notifyDataSetChanged();
                }

            }

            @Override
            public void firstPage(PollResponse pollResponse, LinkHeaders linkHeaders, Response response) {
                cache(pollResponse);
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
                    for (PollSession pollSession : pollSessions) {
                        PollAPI.getSinglePoll(pollSession.getPoll_id(), pollCallback);
                        addItem(pollSession);
                    }
                }
            }
        };

    }

}

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

package com.instructure.androidpolling.app.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.devspark.appmsg.AppMsg;
import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.util.ApplicationManager;
import com.instructure.androidpolling.app.util.Constants;
import com.instructure.canvasapi.api.PollSessionAPI;
import com.instructure.canvasapi.api.SectionAPI;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.PollSession;
import com.instructure.canvasapi.model.PollSessionResponse;
import com.instructure.canvasapi.model.Section;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PublishPollActivity extends FragmentActivity implements APIStatusDelegate{

    @BindView(R.id.courses_spinner)
    Spinner coursesSpinner;

    @BindView(R.id.section_list_view)
    ListView sectionListView;

    @BindView(R.id.publishPoll)
    Button publishPoll;

    @BindView(R.id.sectionLabel)
    TextView sectionLabel;

    private long pollID;
    private Course currentCourse;
    //adapters
    private CourseSpinnerAdapter courseAdapter;
    private SectionListAdapter sectionAdapter;
    //callbacks
    private CanvasCallback<Section[]> sectionCallback;
    private CanvasCallback<PollSessionResponse> pollSessionCallback;
    private CanvasCallback<Response> publishPollCallback;
    private CanvasCallback<PollSessionResponse> openPollSessionCallback;

    private int sessionCount = 0;
    private int sessionCreatedCount = 0;

    private ArrayList<PollSession> openPollSessions = new ArrayList<PollSession>();

    private PollSession singlePollSession;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish_poll);
        ButterKnife.bind(this);

        pollID = getIntent().getExtras().getLong(Constants.POLL_ID);

        setupClickListeners();
        setupCallbacks();

        PollSessionAPI.getFirstPagePollSessions(pollID, openPollSessionCallback);

        setupCourseSpinner(ApplicationManager.getCourseList(PublishPollActivity.this));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////
    private void setupCourseSpinner(Course[] courses) {
        //Can't call .add() on a List. It has to be an arraylist.
        ArrayList<Course> courseList = new ArrayList<Course>();
        courseList.addAll(Arrays.asList(courses));

        //We only want courses we're a teacher for if we're trying to publish a poll

        Iterator<Course> iterator = courseList.iterator();
        while(iterator.hasNext()){
            if(!iterator.next().isTeacher()){
                iterator.remove();
            }
        }


        if(courseList.size() == 0 || !courseList.get(0).getName().equals(getString(R.string.selectCourse))) {
            Course selectCourse = new Course();
            selectCourse.setId(Long.MIN_VALUE);
            selectCourse.setName(getString(R.string.selectCourse));
            courseList.add(0, selectCourse);
        }

        courseAdapter = new CourseSpinnerAdapter(this, android.R.layout.simple_spinner_dropdown_item, courseList);

        coursesSpinner.setAdapter(courseAdapter);
        coursesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Course course = (Course)parent.getAdapter().getItem(position);

                //Handle the loading cell.
                if(course == null || course.getEnrollments() == null){
                    return;
                }
                if(!isCourseTermActive(course)) {
                    AppMsg.makeText(PublishPollActivity.this, getString(R.string.courseTermInactive), AppMsg.STYLE_WARNING).show();
                    return;
                }
                currentCourse = course;

                //unselect all the selections, if we don't it still thinks some are selected when we go to another course
                for(int i = 0; i < sectionListView.getCount(); i++) {
                    sectionListView.setItemChecked(i, false);
                }

                SectionAPI.getFirstPageSectionsList(course, sectionCallback);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setupSectionAdapter(Section[] sections) {
        sectionLabel.setVisibility(View.VISIBLE);
        //Can't call .add() on a List. It has to be an arraylist.
        final ArrayList<Section> sectionList = new ArrayList<Section>();
        sectionList.addAll(Arrays.asList(sections));

        sectionAdapter = new SectionListAdapter(this, android.R.layout.simple_spinner_dropdown_item, sectionList);

        if(openPollSessions.size() < sections.length && sectionList.size() > 1 && !sectionList.get(0).getName().equals(getString(R.string.entireCourse))) {
            Section section = new Section();
            section.setId(Long.MIN_VALUE);
            section.setName(getString(R.string.entireCourse));
            sectionList.add(0, section);
            sectionAdapter.notifyDataSetChanged();
        }

        sectionListView.setAdapter(sectionAdapter);
        sectionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SparseBooleanArray checked = sectionListView.getCheckedItemPositions();
                if(!hasSectionUnpublished()) {
                    //we don't want them to check anything
                    sectionListView.setItemChecked(position, false);
                    AppMsg.makeText(PublishPollActivity.this, getString(R.string.alreadyPublishedForSection), AppMsg.STYLE_WARNING).show();
                    return;
                }
                if(checked.get(0) && position == 0) {
                    //if the "entire course" is selected, select all the other items
                    for(int i = 0; i < sectionListView.getCount(); i++){
                        if(!isSectionPublished((Section)sectionListView.getItemAtPosition(i))) {
                            sectionListView.setItemChecked(i, true);
                        }
                    }
                }
                else if(!checked.get(0) && position == 0) {
                    //if the "entire course" is unselected, unselect everything
                    for(int i = 0; i < sectionListView.getCount(); i++){
                        if(!isSectionPublished((Section)sectionListView.getItemAtPosition(i))) {
                            sectionListView.setItemChecked(i, false);
                        }
                    }
                }
                else if(position != 0) {
                    //if we unselect one of the sections, we don't want to have the "entire course" item selected.
                    //If they aren't all selected, unselect "entire course"
                    for(int i = 1; i < sectionListView.getCount(); i++){
                        if(!checked.get(i)) {
                            sectionListView.setItemChecked(0, false);
                        }
                        //if a session is already published for a section, don't let them select it again
                        if(isSectionPublished((Section)sectionListView.getItemAtPosition(i))) {
                            sectionListView.setItemChecked(i, false);
                            if(i == position) {
                                AppMsg.makeText(PublishPollActivity.this, getString(R.string.alreadyPublishedForSection), AppMsg.STYLE_WARNING).show();
                            }
                        }
                    }
                }

            }
        });

        //if there is only one section, auto select it
        if(sectionListView.getAdapter().getCount() == 1) {
            sectionListView.setItemChecked(0,true);
        }

    }

    private void setupClickListeners() {
        publishPoll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Section> sections = new ArrayList<Section>();
                //make sure we have something selected
                Course course = (Course)coursesSpinner.getSelectedItem();
                if(course.getId() != Long.MIN_VALUE) {
                    //we have a valid course
                    //now, check which sections are selected
                    SparseBooleanArray checked = sectionListView.getCheckedItemPositions();

                    if(sectionListView.getCheckedItemCount() == 0) {
                        //the user didn't select a section
                        AppMsg.makeText(PublishPollActivity.this, getString(R.string.pleaseSelectSection), AppMsg.STYLE_WARNING).show();
                        return;
                    }
                    sessionCount = 0;
                    sessionCreatedCount = 0;
                    //get how many actual sections are checked
                    for(int i = 0; i < sectionListView.getAdapter().getCount(); i++) {
                        if(checked.get(i)) {
                            if(sectionAdapter.getItem(i).getId() != Long.MIN_VALUE) {
                                sessionCreatedCount++;
                                sections.add(sectionAdapter.getItem(i));

                            }
                        }
                    }
                    //don't want to let the user submit multiple times
                    publishPoll.setEnabled(false);
                    for(int i = 0; i < sessionCreatedCount; i++) {
                        PollSessionAPI.createPollSession(pollID, course.getId(), sections.get(i).getId(), pollSessionCallback);
                    }
                }
                else {
                    //the user didn't select a course
                    AppMsg.makeText(PublishPollActivity.this, getString(R.string.pleaseSelectCourse), AppMsg.STYLE_WARNING).show();
                }
            }
        });
    }

    private boolean isSectionPublished(Section section) {
        boolean isPublishedInSection = false;
        for(PollSession session : openPollSessions) {
            if(session.is_published() && session.getCourse_section_id() == section.getId()) {
                isPublishedInSection = true;
                break;
            }
        }

        return isPublishedInSection;
    }

    //check to see if there is a section that is unpublished
    private boolean hasSectionUnpublished() {

        //we'll just use the count of open polls vs. sections in the course. If there are the same
        //amount of sections and sessions, then there all the poll sessions are published
        int sectionCount = sectionAdapter.getCount();
        if(sectionAdapter.getItem(0).getId() == Long.MIN_VALUE) {
            sectionCount--;
        }
        //now get the open sessions for this course
        int courseSessionCount = 0;
        if(currentCourse != null) {
            for (PollSession pollSession : openPollSessions) {
                if (pollSession.getCourse_id() == currentCourse.getId()) {
                    courseSessionCount++;
                }
            }
        }
        if(courseSessionCount < sectionCount) {
            return true;
        }
        else {
            return false;
        }

    }

    private boolean isCourseTermActive(Course course) {
        if(course.getTerm() != null && course.getTerm().getEndAt() != null && course.getTerm().getEndAt().before(new Date())) {
            return false;
        }
        else {
            return true;
        }
    }
    ///////////////////////////////////////////////////////////////////////////
    // Adapters
    ///////////////////////////////////////////////////////////////////////////

    public class CourseSpinnerAdapter extends ArrayAdapter<Course> {

        private List<Course> courses = new ArrayList<Course>();

        public CourseSpinnerAdapter(Context context, int textViewResourceId,
                                    List<Course> courses) {
            super(context, textViewResourceId, courses);
            this.courses = courses;
        }

        public Course[] getCourses(){
            return courses.toArray(new Course[courses.size()]);
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {

            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {


            CourseViewHolder holder = null;

            if ( convertView == null )  {
				/* There is no view at this position, we create a new one.
		           In this case by inflating an xml layout */
                convertView = (LinearLayout)(getLayoutInflater().inflate(R.layout.spinner_row_generic, null));

                holder = new CourseViewHolder();
                holder.courseName = (TextView)convertView.findViewById(R.id.name);

                convertView.setTag (holder);
            }
            else {
				/* We recycle a View that already exists */
                holder = (CourseViewHolder) convertView.getTag ();
            }

            if(courses.get(position) != null) {
                if(!isCourseTermActive(courses.get(position))) {
                    holder.courseName.setEnabled(false);
                }
                holder.courseName.setText(courses.get(position).getName());
            }
            return convertView;
        }
    }
    private static class CourseViewHolder {
        TextView courseName;
    }

    public class SectionListAdapter extends ArrayAdapter<Section> {

        private List<Section> sections = new ArrayList<Section>();

        public SectionListAdapter(Context context, int textViewResourceId,
                                  List<Section> sections) {
            super(context, textViewResourceId, sections);
            this.sections = sections;
        }

        public Section[] getSections(){
            return sections.toArray(new Section[sections.size()]);
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {

            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            return getCustomView(position, convertView, parent);
        }

        @Override
        public Section getItem(int position) {
            return sections.get(position);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {


            SectionViewHolder holder = null;

            if ( convertView == null ) {
				/* There is no view at this position, we create a new one.
		           In this case by inflating an xml layout */
                convertView = (getLayoutInflater().inflate(android.R.layout.simple_list_item_multiple_choice, null));

                holder = new SectionViewHolder();
                holder.sectionName = (CheckedTextView)convertView.findViewById(android.R.id.text1);

                convertView.setTag (holder);
            }
            else {
				/* We recycle a View that already exists */
                holder = (SectionViewHolder) convertView.getTag ();
            }

            if(sections.get(position) != null) {
                holder.sectionName.setText(sections.get(position).getName());
            }

            if(isSectionPublished(getItem(position))) {
                holder.sectionName.setEnabled(false);
                holder.sectionName.setChecked(false);
            }
            else {
                holder.sectionName.setEnabled(true);
            }
            return convertView;
        }
    }
    private static class SectionViewHolder {
        CheckedTextView sectionName;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Callbacks
    ///////////////////////////////////////////////////////////////////////////


    private void setupCallbacks() {

        sectionCallback = new CanvasCallback<Section[]>(this) {
            @Override
            public void cache(Section[] sections) {

            }

            @Override
            public void firstPage(Section[] sections, LinkHeaders linkHeaders, Response response) {
                setupSectionAdapter(sections);
                ApplicationManager.saveSections(PublishPollActivity.this, sections, ((Course)coursesSpinner.getSelectedItem()).getId());
            }
        };

        pollSessionCallback = new CanvasCallback<PollSessionResponse>(this) {
            @Override
            public void cache(PollSessionResponse pollSessionResponse) {

            }

            @Override
            public void firstPage(PollSessionResponse pollSessionResponse, LinkHeaders linkHeaders, Response response) {
                List<PollSession> pollSession = pollSessionResponse.getPollSessions();
                singlePollSession = pollSession.get(0);
                //publish all the sessions
                PollSessionAPI.openPollSession(pollID, pollSession.get(0).getId(), publishPollCallback);
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                //re-enable the publish button since there was an error
                publishPoll.setEnabled(true);
                return super.onFailure(retrofitError);
            }
        };

        publishPollCallback = new CanvasCallback<Response>(this) {
            @Override
            public void cache(Response response) {

            }

            @Override
            public void firstPage(Response response1, LinkHeaders linkHeaders, Response response) {
                sessionCount++;
                //publish all the sessions
                if(sessionCount == sessionCreatedCount) {

                    //if only one session was created we want to send the user to the results screen.
                    if(sessionCount == 1) {
                        if(singlePollSession != null) {
                            Intent intent = getIntent();
                            intent.putExtra(Constants.POLL_SESSION, (Parcelable) singlePollSession);
                            intent.putExtra(Constants.POLL_ID, pollID);
                            setResult(Constants.PUBLISH_POLL_SUCCESS, intent);
                        }
                    }
                    else {
                        //then close this screen
                        setResult(Constants.PUBLISH_POLL_SUCCESS_MULTIPLE);
                    }
                    finish();
                }
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                //re-enable the publish button since there was an error
                publishPoll.setEnabled(true);
                return super.onFailure(retrofitError);
            }
        };

        openPollSessionCallback = new CanvasCallback<PollSessionResponse>(this) {
            @Override
            public void cache(PollSessionResponse pollSessionResponse) {

            }

            @Override
            public void firstPage(PollSessionResponse pollSessionResponse, LinkHeaders linkHeaders, Response response) {
                List<PollSession> pollSessions = pollSessionResponse.getPollSessions();
                if(pollSessions != null) {
                    for(PollSession pollSession : pollSessions) {
                        if(pollSession.is_published()) {
                            openPollSessions.add(pollSession);
                        }
                    }
                }

                if(linkHeaders.nextURL != null) {
                    PollSessionAPI.getNextPagePollSessions(linkHeaders.nextURL, openPollSessionCallback);
                }
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // APIStatusDelegate Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {

    }

    @Override
    public void onCallbackStarted() {

    }

    @Override
    public void onNoNetwork() {

    }

    @Override
    public Context getContext() {
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    public static Intent createIntent(Context context, long pollId) {
        Intent intent = new Intent(context, PublishPollActivity.class);
        intent.putExtra(Constants.POLL_ID, pollId);
        return intent;
    }
}

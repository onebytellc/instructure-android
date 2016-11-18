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
import android.net.Uri;
import android.os.Bundle;

import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.util.ApplicationManager;
import com.instructure.androidpolling.app.util.Constants;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;

import retrofit.client.Response;

public class StartingActivity extends BaseActivity implements APIStatusDelegate{

    private CanvasCallback<Course[]> allCoursesCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //we need course info every time for teachers and students
        setupCallback();
        CourseAPI.getAllCourses(allCoursesCallback);
    }


    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    private void setupCallback() {
        allCoursesCallback = new CanvasCallback<Course[]>(this) {
            @Override
            public void cache(Course[] courses) { }

            @Override
            public void firstPage(Course[] courses, LinkHeaders linkHeaders, Response response) {
                ApplicationManager.saveCourses(StartingActivity.this, courses);
                checkEnrollments(courses);
            }
        };
    }
    //we need to know if the user is a teacher in any course
    //track the enrollments of the user as well
    private void checkEnrollments(Course[] courses) {

        int teacherCount = 0;
        int studentCount = 0;

        for(Course course: courses) {
            if(course.isTeacher()) {
                teacherCount++;
                ApplicationManager.setHasTeacherEnrollment(getApplicationContext());
            }
            else {
                studentCount++;
                ApplicationManager.setHasStudentEnrollment(getApplicationContext());
            }
        }


        String enrollmentType = "";
        if(teacherCount > 0) {
            enrollmentType = getString(R.string.teacher);
        }
        else if(studentCount > 0) {
            enrollmentType = getString(R.string.student);
        }
        else {
            enrollmentType = getString(R.string.other);
        }

        // Send it
        //TODO: re-add analytics if we work on this thing again


        if(ApplicationManager.hasViewPreference(this)) {
            if(ApplicationManager.shouldShowTeacherView(this)) {
                startActivity(FragmentManagerActivity.createIntent(getApplicationContext()));
            }
            else {
                startActivity(PollListActivity.createIntent(getApplicationContext()));
            }
            finish();
            return;
        }
        else if(ApplicationManager.hasTeacherEnrollment(getApplicationContext())) {
            startActivity(FragmentManagerActivity.createIntent(getApplicationContext()));
        }
        else {
            startActivity(PollListActivity.createIntent(getApplicationContext()));
        }
        finish();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Overrides for APIStatusDelegate
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {

    }

    @Override
    public void onNoNetwork() {

    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, StartingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }

    public static Intent createIntent(Context context, Uri passedURI) {
        Intent intent = new Intent(context, StartingActivity.class);
        intent.putExtra(Constants.PASSED_URI, passedURI);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }

    @Override
    public void onCallbackStarted() {

    }
}

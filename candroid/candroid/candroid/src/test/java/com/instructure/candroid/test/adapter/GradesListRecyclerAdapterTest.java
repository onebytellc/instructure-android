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

package com.instructure.candroid.test.adapter;

import android.content.Context;
import android.test.InstrumentationTestCase;
import com.instructure.candroid.adapter.GradesListRecyclerAdapter;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.Submission;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricTestRunner.class)
public class GradesListRecyclerAdapterTest extends InstrumentationTestCase {
    private GradesListRecyclerAdapter mAdapter;

    /**
     * Make it so the protected constructor can be called
     */
    public static class GradesListRecyclerAdapterWrapper extends GradesListRecyclerAdapter {
        protected GradesListRecyclerAdapterWrapper(Context context) {
            super(context);
        }
    }

    @Before
    public void setup(){
        mAdapter = new GradesListRecyclerAdapterWrapper(RuntimeEnvironment.application.getApplicationContext());
    }

    @Test
    public void testAreContentsTheSame_SameNameAndPoints(){
        Assignment assignment = new Assignment();
        assignment.setName("assignment");
        assignment.setPointsPossible(0.0);
        assertTrue(mAdapter.createItemCallback().areContentsTheSame(assignment, assignment));
    }
    
    @Test
    public void testAreContentsTheSame_DifferentName(){
        Assignment assignment1 = new Assignment();
        assignment1.setName("assignment1");
        assignment1.setPointsPossible(0.0);

        Assignment assignment2 = new Assignment();
        assignment2.setName("assignment2");
        assignment2.setPointsPossible(0.0);

        assertFalse(mAdapter.createItemCallback().areContentsTheSame(assignment1, assignment2));
    }

    @Test
    public void testAreContentsTheSame_DifferentScore(){
        Assignment assignment1 = new Assignment();
        assignment1.setName("assignment1");
        assignment1.setPointsPossible(0.0);

        Assignment assignment2 = new Assignment();
        assignment2.setName("assignment1");
        assignment2.setPointsPossible(1.0);

        assertFalse(mAdapter.createItemCallback().areContentsTheSame(assignment1, assignment2));
    }

    @Test
    public void testAreContentsTheSame_SameWithSubmission(){
        Assignment assignment = new Assignment();
        assignment.setName("assignment");
        assignment.setPointsPossible(0.0);
        Submission submission = new Submission();
        submission.setGrade("A");
        assignment.setLastSubmission(submission);

        assertTrue(mAdapter.createItemCallback().areContentsTheSame(assignment, assignment));
    }

    @Test
    public void testAreContentsTheSame_SameWithSubmissionNullChange(){
        Assignment assignment1 = new Assignment();
        assignment1.setName("assignment");
        assignment1.setPointsPossible(0.0);
        Submission submission1 = new Submission();
        submission1.setGrade("A");
        assignment1.setLastSubmission(submission1);

        Assignment assignment2 = new Assignment();
        assignment2.setName("assignment1");
        assignment2.setPointsPossible(0.0);
        assignment2.setLastSubmission(null);

        assertFalse(mAdapter.createItemCallback().areContentsTheSame(assignment1, assignment2));
    }

    @Test
    public void testAreContentsTheSame_SameWithSubmissionNullGrade(){
        Assignment assignment1 = new Assignment();
        assignment1.setName("assignment");
        assignment1.setPointsPossible(0.0);
        Submission submission1 = new Submission();
        submission1.setGrade("A");
        assignment1.setLastSubmission(submission1);

        Assignment assignment2 = new Assignment();
        assignment2.setName("assignment1");
        assignment2.setPointsPossible(0.0);
        Submission submission2 = new Submission();
        submission1.setGrade(null);
        assignment1.setLastSubmission(submission2);

        assertFalse(mAdapter.createItemCallback().areContentsTheSame(assignment1, assignment2));
    }
}

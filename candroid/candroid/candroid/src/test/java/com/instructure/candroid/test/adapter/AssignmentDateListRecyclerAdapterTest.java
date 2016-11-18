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

import com.instructure.candroid.adapter.AssignmentDateListRecyclerAdapter;
import com.instructure.candroid.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi.model.Assignment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Date;

@Config(sdk = 17)
@RunWith(RobolectricTestRunner.class)
public class AssignmentDateListRecyclerAdapterTest extends InstrumentationTestCase {
    private AssignmentDateListRecyclerAdapter mAdapter;
    private AdapterToFragmentCallback<Assignment> mCallback;

    /**
     * Make it so the protected constructor can be called
     */
    public static class AssignmentDateListRecyclerAdapterWrapper extends AssignmentDateListRecyclerAdapter {
        protected AssignmentDateListRecyclerAdapterWrapper(Context context) {
            super(context);
        }
    }

    @Before
    public void setup() {
        mAdapter = new AssignmentDateListRecyclerAdapterWrapper(RuntimeEnvironment.application.getApplicationContext());
    }

    @Test
    public void testAreContentsTheSame_sameName() {
        Assignment assignment  = new Assignment();
        assignment.setName("Assign1");
        assertTrue(mAdapter.createItemCallback().areContentsTheSame(assignment, assignment));
    }

    @Test
    public void testAreContentsTheSame_differentName() {
        Assignment assignment1  = new Assignment();
        assignment1.setName("Assign1");
        Assignment assignment2  = new Assignment();
        assignment2.setName("Assign2");
        assertFalse(mAdapter.createItemCallback().areContentsTheSame(assignment1, assignment2));
    }

    @Test
    public void testAreContentsTheSame_oneNullDueDate() {
        Assignment assignmentDueDate  = new Assignment();
        assignmentDueDate.setName("Assign1");
        assignmentDueDate.setDueDate(new Date());
        Assignment assignment1  = new Assignment();
        assignment1.setName("Assign1");
        assertFalse(mAdapter.createItemCallback().areContentsTheSame(assignmentDueDate, assignment1));
        assertFalse(mAdapter.createItemCallback().areContentsTheSame(assignment1, assignmentDueDate));
        assertTrue(mAdapter.createItemCallback().areContentsTheSame(assignmentDueDate, assignmentDueDate));
    }
}

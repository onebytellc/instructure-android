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

import com.instructure.candroid.adapter.CalendarListRecyclerAdapter;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.ScheduleItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.Date;

@Config(sdk = 17)
@RunWith(RobolectricTestRunner.class)
public class CalendarListRecyclerAdapterTest extends InstrumentationTestCase {
    private CalendarListRecyclerAdapter mAdapter;

    /**
     * Make it so the protected constructor can be called
     */
    public static class CalendarListRecyclerAdapterWrapper extends CalendarListRecyclerAdapter {
        protected CalendarListRecyclerAdapterWrapper(Context context) {
            super(context);
        }
    }

    @Before
    public void setup(){
        mAdapter = new CalendarListRecyclerAdapterWrapper(RuntimeEnvironment.application.getApplicationContext());
    }

    @Test
    public void testAreContentsTheSame_noAssignmentSame(){
        ScheduleItem scheduleItem1 = new ScheduleItem();
        scheduleItem1.setTitle("ScheduleItem1");
        scheduleItem1.setStartDate(new Date());
        assertTrue(mAdapter.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem1));
    }

    @Test
    public void testAreContentsTheSame_noAssignmentDifferentName(){
        ScheduleItem scheduleItem1 = new ScheduleItem();
        scheduleItem1.setTitle("ScheduleItem1a");
        Date date = new Date();
        scheduleItem1.setStartDate(date);
        ScheduleItem scheduleItem2 = new ScheduleItem();
        scheduleItem2.setTitle("ScheduleItem1b");
        scheduleItem2.setStartDate(date);
        assertFalse(mAdapter.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem2));
    }

    @Test
    public void testAreContentsTheSame_noAssignmentDifferentDate(){
        ScheduleItem scheduleItem1 = new ScheduleItem();
        scheduleItem1.setTitle("ScheduleItem1a");
        scheduleItem1.setStartDate(new Date(Calendar.getInstance().getTimeInMillis() - 1000));
        ScheduleItem scheduleItem2 = new ScheduleItem();
        scheduleItem2.setTitle("ScheduleItem1a");
        scheduleItem2.setStartDate(new Date(Calendar.getInstance().getTimeInMillis() + 1000));
        assertFalse(mAdapter.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem2));
    }

    @Test
    public void testAreContentsTheSame_sameAssignment(){
        ScheduleItem scheduleItem1 = new ScheduleItem();
        scheduleItem1.setTitle("ScheduleItem1");
        scheduleItem1.setStartDate(new Date());
        Assignment assignment1 = new Assignment();
        assignment1.setDueDate(new Date());
        scheduleItem1.setAssignment(assignment1);
        assertTrue(mAdapter.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem1));
    }

    @Test
    public void testAreContentsTheSame_differentAssignment(){
        ScheduleItem scheduleItem1 = new ScheduleItem();
        scheduleItem1.setTitle("ScheduleItem1");
        Date date = new Date();
        scheduleItem1.setStartDate(date);
        Assignment assignment1 = new Assignment();
        assignment1.setDueDate(new Date(Calendar.getInstance().getTimeInMillis() - 1000));
        scheduleItem1.setAssignment(assignment1);

        ScheduleItem scheduleItem2 = new ScheduleItem();
        scheduleItem2.setTitle("ScheduleItem1");
        scheduleItem2.setStartDate(date);
        Assignment assignment2 = new Assignment();
        assignment2.setDueDate(new Date(Calendar.getInstance().getTimeInMillis() + 1000));
        scheduleItem2.setAssignment(assignment2);

        assertFalse(mAdapter.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem2));
    }

    @Test
    public void testAreContentsTheSame_nullAssignment() {
        ScheduleItem scheduleItem1 = new ScheduleItem();
        scheduleItem1.setTitle("ScheduleItem1");
        Date date = new Date();
        scheduleItem1.setStartDate(date);
        Assignment assignment1 = new Assignment();
        assignment1.setDueDate(date);
        scheduleItem1.setAssignment(assignment1);

        ScheduleItem scheduleItem2 = new ScheduleItem();
        scheduleItem2.setTitle("ScheduleItem1");
        scheduleItem2.setStartDate(date);
        Assignment assignment2 = null;
        scheduleItem2.setAssignment(assignment2);

        assertFalse(mAdapter.createItemCallback().areContentsTheSame(scheduleItem1, scheduleItem2));
    }
}

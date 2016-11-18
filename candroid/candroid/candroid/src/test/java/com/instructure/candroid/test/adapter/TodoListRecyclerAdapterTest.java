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
import com.instructure.candroid.adapter.TodoListRecyclerAdapter;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.ScheduleItem;
import com.instructure.canvasapi.model.ToDo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricTestRunner.class)
public class TodoListRecyclerAdapterTest extends InstrumentationTestCase {
    private TodoListRecyclerAdapter mAdapter;

    /**
     * Make it so the protected constructor can be called
     */
    public static class TodoListRecyclerAdapterWrapper extends TodoListRecyclerAdapter {
        protected TodoListRecyclerAdapterWrapper(Context context) {
            super(context);
        }
    }

    @Before
    public void setup(){
        mAdapter = new TodoListRecyclerAdapterWrapper(RuntimeEnvironment.application.getApplicationContext());
    }

    @Test
    public void testAreContentsTheSame_SameTitleFromAssignment(){
        ToDo item = new ToDo();
        Assignment assignment = new Assignment();
        assignment.setName("item");
        item.setAssignment(assignment);

        assertTrue(mAdapter.createItemCallback().areContentsTheSame(item, item));
    }

    @Test
    public void testAreContentsTheSame_SameTitleFromSchedule(){
        ToDo item = new ToDo();
        ScheduleItem scheduleItem = new ScheduleItem();
        scheduleItem.setTitle("item");
        item.setScheduleItem(scheduleItem);

        assertTrue(mAdapter.createItemCallback().areContentsTheSame(item, item));
    }

    @Test
    public void testAreContentsTheSame_DifferentTitleFromAssignment(){
        ToDo item = new ToDo();
        Assignment assignment = new Assignment();
        assignment.setName("item");
        item.setAssignment(assignment);
        ToDo item1 = new ToDo();
        Assignment assignment1 = new Assignment();
        assignment1.setName("item1");
        item1.setAssignment(assignment1);

        assertFalse(mAdapter.createItemCallback().areContentsTheSame(item, item1));
    }

    @Test
    public void testAreContentsTheSame_DifferentTitleFromSchedule(){
        ToDo item = new ToDo();
        ScheduleItem scheduleItem = new ScheduleItem();
        scheduleItem.setTitle("item");
        item.setScheduleItem(scheduleItem);
        ToDo item1 = new ToDo();
        ScheduleItem scheduleItem1 = new ScheduleItem();
        scheduleItem1.setTitle("item1");
        item1.setScheduleItem(scheduleItem1);

        assertFalse(mAdapter.createItemCallback().areContentsTheSame(item, item1));
    }
}


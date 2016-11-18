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
import com.instructure.candroid.adapter.SyllabusRecyclerAdapter;
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
public class SyllabusRecyclerAdapterTest extends InstrumentationTestCase{
    private SyllabusRecyclerAdapter mAdapter;

    /**
     * Make it so the protected constructor can be called
     */
    public static class SyllabusRecyclerAdapterWrapper extends SyllabusRecyclerAdapter {
        protected SyllabusRecyclerAdapterWrapper(Context context) {
            super(context);
        }
    }

    @Before
    public void setup(){
        mAdapter = new SyllabusRecyclerAdapterWrapper(RuntimeEnvironment.application.getApplicationContext());
    }

    @Test
    public void areContentsTheSame_NotNullSameDate(){
        ScheduleItem item = new ScheduleItem();
        item.setTitle("item");
        item.setStartDate(new Date());

        assertTrue(mAdapter.createItemCallback().areContentsTheSame(item, item));
    }

    @Test
    public void areContentsTheSame_NotNullDifferentDate(){
        ScheduleItem item = new ScheduleItem();
        item.setTitle("item");
        item.setStartDate(new Date(Calendar.getInstance().getTimeInMillis() + 1000));

        ScheduleItem item1 = new ScheduleItem();
        item1.setTitle("item");
        item1.setStartDate(new Date(Calendar.getInstance().getTimeInMillis() - 1000));

        assertFalse(mAdapter.createItemCallback().areContentsTheSame(item, item1));
    }

    @Test
    public void areContentsTheSame_NullDate(){
        ScheduleItem item = new ScheduleItem();
        item.setTitle("item");
        item.setStartDate(new Date());

        ScheduleItem item1 = new ScheduleItem();
        item1.setTitle("item");
        item1.setStartDate(null);

        assertFalse(mAdapter.createItemCallback().areContentsTheSame(item, item1));
    }

}

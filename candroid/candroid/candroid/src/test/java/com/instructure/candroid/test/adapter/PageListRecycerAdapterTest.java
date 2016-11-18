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
import com.instructure.candroid.adapter.PageListRecyclerAdapter;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Page;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricTestRunner.class)
public class PageListRecycerAdapterTest extends InstrumentationTestCase {
    private PageListRecyclerAdapter mAdapter;

    public static class PageListRecyclerAdapterWrapper extends PageListRecyclerAdapter {
        protected PageListRecyclerAdapterWrapper(Context context) { super(context, CanvasContext.emptyCourseContext(), null, "", false);}
    }

    @Before
    public void setup() {
        mAdapter = new PageListRecyclerAdapterWrapper(RuntimeEnvironment.application);
    }

    @Test
    public void testAreContentsTheSame_titleSame() {
        Page page = new Page();
        page.setTitle("HI");
        assertTrue(mAdapter.getItemCallback().areContentsTheSame(page, page));
    }

    @Test
    public void testAreContentsTheSame_titleDifferent() {
        Page page1 = new Page();
        page1.setTitle("HI");
        Page page2 = new Page();
        page1.setTitle("HI I AM SUPER DIFFERENT");
        assertFalse(mAdapter.getItemCallback().areContentsTheSame(page1, page2));
    }

}

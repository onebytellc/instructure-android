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

import com.instructure.candroid.adapter.DiscussionListRecyclerAdapter;
import com.instructure.canvasapi.model.DiscussionTopicHeader;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Date;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

@Config(sdk = 17)
@RunWith(RobolectricTestRunner.class)
public class DiscussionListRecyclerAdapterTest extends InstrumentationTestCase {
    private DiscussionListRecyclerAdapter mAdapter;


    public static class DiscussionListRecyclerAdapterWrapper extends DiscussionListRecyclerAdapter {
        protected DiscussionListRecyclerAdapterWrapper(Context context) { super(context); }
    }

    @Before
    public void setup(){
        mAdapter = new DiscussionListRecyclerAdapterWrapper(RuntimeEnvironment.application);
    }

    @Test
    public void testAreContentsTheSame_SameTitle(){
        DiscussionTopicHeader discussionTopicHeader = new DiscussionTopicHeader();
        discussionTopicHeader.setTitle("discussion");
        assertTrue(mAdapter.createItemCallback().areContentsTheSame(discussionTopicHeader, discussionTopicHeader));
    }

    @Test
    public void testAreContentsTheSame_DifferentTitle(){
        DiscussionTopicHeader discussionTopicHeader1 = new DiscussionTopicHeader();
        discussionTopicHeader1.setTitle("discussion1");
        DiscussionTopicHeader discussionTopicHeader2 = new DiscussionTopicHeader();
        discussionTopicHeader2.setTitle("discussion2");
        assertFalse(mAdapter.createItemCallback().areContentsTheSame(discussionTopicHeader1, discussionTopicHeader2));
    }

    // region Compare tests

    @Test
    public void testCompare_bothHaveNullDates() {
        DiscussionTopicHeader onlyTitle1 = new DiscussionTopicHeader();
        onlyTitle1.setTitle("discussion1");
        DiscussionTopicHeader onlyTitle2 = new DiscussionTopicHeader();
        onlyTitle2.setTitle("discussion2");

        assertEquals(-1, mAdapter.createItemCallback().compare("", onlyTitle1, onlyTitle2));
        assertEquals(1, mAdapter.createItemCallback().compare("", onlyTitle2, onlyTitle1));
        assertEquals(0, mAdapter.createItemCallback().compare("", onlyTitle1, onlyTitle1));
    }

    @Test
    public void testCompare_oneNullDateLastReply() {
        DiscussionTopicHeader d1 = new DiscussionTopicHeader();
        d1.setTitle("discussion1");
        DateTime dateTime2 = new DateTime("2014-12-29");
        Date date = new Date(dateTime2.getMilliseconds(TimeZone.getDefault()));
        d1.setLastReply(date);

        DiscussionTopicHeader d2 = new DiscussionTopicHeader();
        d2.setTitle("discussion2");

        assertEquals(-1, mAdapter.createItemCallback().compare("", d1, d2));
        assertEquals(1, mAdapter.createItemCallback().compare("", d2, d1));
        assertEquals(0, mAdapter.createItemCallback().compare("", d1, d1));
    }

    @Test
    public void testCompare_oneNullDatePostedAt() {
        DiscussionTopicHeader d1 = new DiscussionTopicHeader();
        d1.setTitle("discussion1");
        DateTime dateTime2 = new DateTime("2014-12-29");
        Date date = new Date(dateTime2.getMilliseconds(TimeZone.getDefault()));
        d1.setPostedAt(date);

        DiscussionTopicHeader d2 = new DiscussionTopicHeader();
        d2.setTitle("discussion2");

        assertEquals(-1, mAdapter.createItemCallback().compare("", d1, d2));
        assertEquals(1, mAdapter.createItemCallback().compare("", d2, d1));
        assertEquals(0, mAdapter.createItemCallback().compare("", d1, d1));
    }

    @Test
    public void testCompare_bothHaveDates() {
        DiscussionTopicHeader d1 = new DiscussionTopicHeader();
        d1.setTitle("discussion1");
        DateTime dateTime1 = new DateTime("2014-12-27");
        Date date1 = new Date(dateTime1.getMilliseconds(TimeZone.getDefault()));
        d1.setLastReply(date1);
        DiscussionTopicHeader d2 = new DiscussionTopicHeader();
        DateTime dateTime2 = new DateTime("2014-12-29");
        Date date2 = new Date(dateTime2.getMilliseconds(TimeZone.getDefault()));
        d2.setLastReply(date2);
        d2.setTitle("discussion2");

        // callback sorts most recent date first
        assertEquals(1, mAdapter.createItemCallback().compare("", d1, d2));
        assertEquals(-1, mAdapter.createItemCallback().compare("", d2, d1));
        assertEquals(0, mAdapter.createItemCallback().compare("", d1, d1));
    }
    // endregion
}

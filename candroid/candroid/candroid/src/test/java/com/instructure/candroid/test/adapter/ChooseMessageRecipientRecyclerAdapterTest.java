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

import com.instructure.candroid.adapter.ChooseMessageRecipientRecyclerAdapter;
import com.instructure.canvasapi.model.Recipient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Config(sdk = 17)
@RunWith(RobolectricTestRunner.class)
public class ChooseMessageRecipientRecyclerAdapterTest extends InstrumentationTestCase {
    private ChooseMessageRecipientRecyclerAdapter mAdapter;

    public static class ChooseMessageRecipientRecyclerAdapterWrapper extends ChooseMessageRecipientRecyclerAdapter {
        protected ChooseMessageRecipientRecyclerAdapterWrapper(Context context) { super(context, "", null, null, false ); }
    }

    @Before
    public void setup(){
        mAdapter = new ChooseMessageRecipientRecyclerAdapterWrapper(RuntimeEnvironment.application);
    }

    @Test
    public void testAreContentsTheSame_SameName(){
        Recipient recipient = new Recipient("", "name", 0, 0, 0);
        assertTrue(mAdapter.getItemCallback().areContentsTheSame(recipient, recipient));
    }

    @Test
    public void testAreContentsTheSame_DifferentName(){
        Recipient recipient1 = new Recipient("", "name", 0, 0, 0);
        Recipient recipient2 = new Recipient("", "hodor", 0, 0, 0);
        assertFalse(mAdapter.getItemCallback().areContentsTheSame(recipient1, recipient2));
    }
}

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

package com.instructure.candroid.fragment;

import android.content.Context;
import android.os.Bundle;

import com.instructure.candroid.R;
import com.instructure.candroid.util.Param;
import com.instructure.pandautils.utils.Const;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Tab;

public class AnnouncementListFragment extends DiscussionListFragment {

    public static Bundle createBundle(CanvasContext canvasContext) {
        Bundle extras = ParentFragment.createBundle(canvasContext);
        extras.putString(Const.TAB_ID, Tab.ANNOUNCEMENTS_ID);
        return extras;
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.announcements);
    }

    @Override
    protected String getSelectedParamName() {
        return Param.MESSAGE_ID;
    }

    @Override
    public String getTabId() {
        return Tab.ANNOUNCEMENTS_ID;
    }

    @Override
    protected boolean isAnnouncement() {
        return true;
    }
}

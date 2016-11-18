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

package com.instructure.candroid.delegate;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.View;

import com.instructure.candroid.adapter.CourseNavigationAdapter;
import com.instructure.candroid.fragment.ParentFragment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Tab;

public interface Navigation {
    public enum NavigationPosition {
        UNKNOWN, PROFILE, COURSES, NOTIFICATIONS, TODO, INBOX, GRADES, BOOKMARKS, CALENDAR, SETTINGS, SPEEDGRADER, FILES
    }

    public void addFragment(ParentFragment fragment);
    public void addFragment(ParentFragment fragment, boolean ignoreDebounce);
    public void addFragment(ParentFragment fragment, int inAnimation, int outAnimation);
    public void addFragment(ParentFragment fragment, NavigationPosition selectedPosition);
    public void addFragment(ParentFragment fragment, NavigationPosition selectedPosition, boolean ignoreDebounce);
    public void addFragment(ParentFragment fragment, int transitionId, View sharedElement);

    //Course Navigation
    public CourseNavigationAdapter getCourseNavigationAdapter();

    public void setActionBarStatusBarColors(int actionBarColor, int statusBarColor);

    public boolean isNavigationDrawerOpen();
    public void requestNavigationDrawerClose();
    public ActionBar getSupportActionBar();

    public void updateCalendarStartDay();
    public void redrawNavigationShortcuts();
    public void courseNameChanged(CanvasContext canvasContext);
    public void redrawScreen();

    public Fragment getTopFragment();
    public Fragment getPeekingFragment();
    public Fragment getCurrentFragment();

    public void  popCurrentFragment();

    public void addBookmark();
}

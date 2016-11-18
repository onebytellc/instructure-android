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

package com.instructure.candroid.view;

import android.content.Context;
import android.util.AttributeSet;

public class AllCoursesPeekListViewCell extends PeekListViewCell {

    ///////////////////////////////////////////////////////////////////////////
    // Factory Method and Constructors.
    ///////////////////////////////////////////////////////////////////////////

    public AllCoursesPeekListViewCell(Context context) {
        super(context);
    }

    public AllCoursesPeekListViewCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AllCoursesPeekListViewCell(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * We don't want to ever show the peeking view when it's the "View All Courses" row
     * @param enabled
     */
    @Override
    public void setPeekingEnabled(boolean enabled) {
        super.setPeekingEnabled(false);
    }
}

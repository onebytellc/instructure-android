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

package com.instructure.parentapp.factorys;

import android.support.annotation.Nullable;

import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Student;
import com.instructure.parentapp.presenters.WeekPresenter;

import instructure.androidblueprint.PresenterFactory;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class WeekViewPresenterFactory implements PresenterFactory<WeekPresenter> {

    private Student mStudent;
    private Course mCourse;

    public WeekViewPresenterFactory(Student student, @Nullable Course course) {
        mStudent = student;
        mCourse = course;
    }

    @Override
    public WeekPresenter create() {
        return new WeekPresenter(mStudent, mCourse);
    }
}

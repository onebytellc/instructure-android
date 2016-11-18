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

package com.instructure.parentapp.adapter;

import android.content.Context;
import android.view.View;

import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Student;
import com.instructure.parentapp.binders.CourseBinder;
import com.instructure.parentapp.holders.CourseViewHolder;
import com.instructure.parentapp.interfaces.AdapterToFragmentUpdateListCallback;

import instructure.androidblueprint.SyncPresenter;
import instructure.androidblueprint.SyncRecyclerAdapter;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class CourseListRecyclerAdapter extends SyncRecyclerAdapter<Course, CourseViewHolder> {

    private Student mStudent;
    private AdapterToFragmentUpdateListCallback<Student, Course> mAdapterToFragmentCallback;

    public CourseListRecyclerAdapter(
            Context context,
            SyncPresenter presenter,
            Student student,
            AdapterToFragmentUpdateListCallback<Student, Course> callback) {
        super(context, presenter);
        mStudent = student;
        mAdapterToFragmentCallback = callback;
    }

    @Override
    public void bindHolder(Course course, CourseViewHolder holder, int position) {
        CourseBinder.bind(holder, course, mStudent, getContext(), mAdapterToFragmentCallback);
    }

    @Override
    public CourseViewHolder createViewHolder(View v, int viewType) {
        return new CourseViewHolder(v);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return CourseViewHolder.holderResId();
    }

//    public CourseListRecyclerAdapter(Context context, Student student, AdapterToFragmentUpdateListCallback<Student, Course> adapterToFragmentCallback) {
//        super(context, Course.class);
//        setItemCallback(new ItemComparableCallback<Course>() {
//            @Override
//            public int compare(Course o1, Course o2) {
//                return o1.compareTo(o2);
//            }
//
//            @Override
//            public boolean areContentsTheSame(Course oldItem, Course newItem) {
//                return false;
//            }
//
//            @Override
//            public boolean areItemsTheSame(Course item1, Course item2) {
//                return item1.getContextId().hashCode() == item2.getContextId().hashCode();
//            }
//
//            @Override
//            public long getUniqueItemId(Course item) {
//                return item.getContextId().hashCode();
//            }
//        });
//
//        mAdapterToFragmentCallback = adapterToFragmentCallback;
//        mStudent = student;
//        mContext = context;
//    }
}

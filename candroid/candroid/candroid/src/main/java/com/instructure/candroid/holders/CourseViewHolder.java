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

package com.instructure.candroid.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.instructure.pandautils.views.RippleView;
import com.instructure.candroid.R;
import com.instructure.candroid.view.EllipsizingTextView;

public class CourseViewHolder extends RecyclerView.ViewHolder {

    public RippleView overflowRipple, gradeRipple;
    public TextView name;
    public TextView courseCode, grade;
    public ImageView pulseOveflow, pulseGrade, overflow;
    public RippleView clickItem;

    public CourseViewHolder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.courseName);
        grade = (TextView) itemView.findViewById(R.id.courseGrade);
        courseCode = (TextView) itemView.findViewById(R.id.courseCode);
        overflowRipple = (RippleView) itemView.findViewById(R.id.overflowRipple);
        gradeRipple = (RippleView) itemView.findViewById(R.id.gradeRipple);
        overflow = (ImageView) itemView.findViewById(R.id.overflow);
        pulseOveflow = (ImageView) itemView.findViewById(R.id.pulseOveflow);
        pulseGrade = (ImageView) itemView.findViewById(R.id.pulseGrade);
        clickItem = (RippleView)itemView.findViewById(R.id.clickItem);
    }

    public static int holderResId() {
        return R.layout.viewholder_course_card;
    }
}

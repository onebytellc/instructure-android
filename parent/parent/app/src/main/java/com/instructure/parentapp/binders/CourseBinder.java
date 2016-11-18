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

package com.instructure.parentapp.binders;

import android.content.Context;
import android.view.View;

import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.Student;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.Prefs;
import com.instructure.pandautils.utils.Utils;
import com.instructure.parentapp.BuildConfig;
import com.instructure.parentapp.R;
import com.instructure.parentapp.holders.CourseViewHolder;
import com.instructure.parentapp.interfaces.AdapterToFragmentUpdateListCallback;

import java.text.DecimalFormat;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class CourseBinder extends BaseBinder {

    public static void bind(
            final CourseViewHolder holder,
            final Course course,
            final Student user,
            final Context context,
            final AdapterToFragmentUpdateListCallback<Student, Course> adapterToFragmentCallback) {
        holder.courseTitle.setText(course.getName());
        Utils.testSafeContentDescription(holder.courseTitle,
                String.format(context.getString(R.string.course_title_content_desc), holder.getAdapterPosition()),
                course.getName(),
                BuildConfig.IS_TESTING);
        holder.courseCode.setText(course.getCourseCode());

        holder.gradeText.setVisibility(View.VISIBLE);
        holder.scoreText.setVisibility(View.VISIBLE);

        Prefs prefs = new Prefs(context, com.instructure.parentapp.util.Const.CANVAS_PARENT_SP);
        int color = prefs.load(Const.NEW_COLOR, -1);

        if(color != -1) {
            holder.gradeText.setTextColor(color);
            holder.scoreText.setTextColor(color);
        }
        if (!course.isHideFinalGrades()) {
            Double grade = course.getCurrentScore();
            String gradeStr = new DecimalFormat("##.##").format(grade);
            if (course.getCurrentGrade() != null) {
                holder.gradeText.setText(course.getCurrentGrade());
                Utils.testSafeContentDescription(holder.gradeText,
                        String.format(context.getString(R.string.grade_text_content_desc), holder.getAdapterPosition()),
                        course.getCurrentGrade(),
                        BuildConfig.IS_TESTING);
            } else {
                holder.gradeText.setVisibility(View.GONE);
            }

            holder.scoreText.setText(gradeStr + "%");
            Utils.testSafeContentDescription(holder.scoreText,
                    String.format(context.getString(R.string.score_text_content_desc), holder.getAdapterPosition()),
                    gradeStr + "%",
                    BuildConfig.IS_TESTING);
        } else {
            holder.gradeText.setText("-");
            holder.scoreText.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterToFragmentCallback.onRowClicked(user, holder.getAdapterPosition(), false);
            }
        });
    }

}

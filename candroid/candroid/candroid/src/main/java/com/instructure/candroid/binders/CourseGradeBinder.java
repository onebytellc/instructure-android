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

package com.instructure.candroid.binders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Toast;

import com.instructure.candroid.R;
import com.instructure.candroid.holders.CourseGradeViewHolder;
import com.instructure.candroid.interfaces.CourseAdapterToFragmentCallback;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.pandautils.utils.CanvasContextColor;

import java.text.DecimalFormat;

public class CourseGradeBinder {

    public static void bind(
            final Context context,
            final CanvasContext canvasContext,
            final CourseGradeViewHolder holder,
            final boolean gradesTabExists,
            final boolean isAllGradingPeriodsShown,
            final CourseAdapterToFragmentCallback adapterToFragmentCallback) {

        if(gradesTabExists) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapterToFragmentCallback.onRowClicked(canvasContext);
                }
            });
        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, R.string.gradesAreHidden, Toast.LENGTH_LONG).show();
                }
            });
        }

        if(CanvasContext.Type.isCourse(canvasContext)){

            Course course = (Course)canvasContext;

            Double grade = ((Course)canvasContext).getCurrentScore();
            String gradeStr = new DecimalFormat("##.#").format(grade);
            final int color = CanvasContextColor.getCachedColor(context, canvasContext);

            if(course.isTeacher()) {
                holder.gradeWrapper.setVisibility(View.GONE);
            } else {
                holder.gradeWrapper.setVisibility(View.VISIBLE);
                if (course.isFinalGradeHidden() || !isAllGradingPeriodsShown) {
                    holder.letterGrade.setVisibility(View.GONE);
                    holder.percentGrade.setVisibility(View.GONE);
                    holder.lockedGrade.setVisibility(View.VISIBLE);
                    Drawable lockDrawable = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_locked_fill, color);
                    holder.lockedGrade.setImageDrawable(lockDrawable);
                } else {
                    holder.lockedGrade.setVisibility(View.GONE);
                    holder.percentGrade.setVisibility(View.VISIBLE);
                    holder.percentGrade.setTextColor(color);
                    if (course.getCurrentScore() == 0.0 && (course.getCurrentGrade() == null || "null".equals(course.getCurrentGrade()))) {
                       holder.percentGrade.setText(context.getString(R.string.noGradeText));
                    } else {
                        holder.percentGrade.setText(gradeStr + "%");
                    }

                    if (((Course) canvasContext).getCurrentGrade() != null) {
                        holder.letterGrade.setVisibility(View.VISIBLE);
                        holder.letterGrade.setTextColor(color);
                        holder.letterGrade.setText(((Course) canvasContext).getCurrentGrade());
                    } else {
                        holder.letterGrade.setVisibility(View.GONE);
                    }
                }
            }
            holder.courseName.setText(canvasContext.getName());
        }
    }
}

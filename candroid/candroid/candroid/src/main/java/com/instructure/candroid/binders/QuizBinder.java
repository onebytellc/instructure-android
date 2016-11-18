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
import android.text.TextUtils;
import android.view.View;

import com.instructure.candroid.R;
import com.instructure.candroid.holders.QuizViewHolder;
import com.instructure.candroid.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi.model.Quiz;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.pandautils.utils.CanvasContextColor;

import java.util.Date;

public class QuizBinder extends BaseBinder{

    public static void bind(
            final QuizViewHolder holder,
            final Quiz item,
            final AdapterToFragmentCallback<Quiz> adapterToFragmentCallback,
            final Context context,
            final int courseColor) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterToFragmentCallback.onRowClicked(item, holder.getAdapterPosition(), true);
            }
        });

        holder.title.setText(item.getTitle());

        String description = getHtmlAsText(item.getDescription());
        if(!TextUtils.isEmpty(description)) {
            holder.description.setText(description);
            setVisible(holder.description);
        } else {
            holder.description.setText("");
            setGone(holder.description);
        }

        if (item.getAssignment() != null && item.getAssignment().getDueDate() != null) {
            String dueDate = DateHelpers.createPrefixedDateTimeString(context, R.string.toDoDue, item.getAssignment().getDueDate());
            holder.date.setText(dueDate);
        } else {
            holder.date.setText("");
        }

        Drawable drawable = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_quizzes_fill, courseColor);
        holder.icon.setImageDrawable(drawable);

        String points = item.getPointsPossible();
        if (!TextUtils.isEmpty(points)) {
            setGrade(null, Double.parseDouble(points), holder.points, context);
            setVisible(holder.points);
        } else {
            holder.points.setText("");
            setGone(holder.points);
        }

        final int questionCount = item.getQuestionCount();
        final int postFixResId = (questionCount == 1) ? R.string.question : R.string.questions;
        holder.questions.setText(questionCount + " " + context.getString(postFixResId));

        Date dueDate = item.getDueAt();

        if (dueDate != null) {
            holder.date.setText(DateHelpers.createPrefixedDateTimeString(context, R.string.toDoDue, dueDate));
            setVisible(holder.date);
        } else {
            holder.date.setText("");
            setGone(holder.date);
        }

        Date lockDate = item.getLockAt();
        Date today = new Date();
        if((lockDate != null && today.after(lockDate)) || (item.getRequireLockdownBrowserForResults())) {
            holder.status.setText(R.string.closed);
            holder.status.setTextColor(courseColor);
        } else {
            holder.status.setText("");
        }
    }
}

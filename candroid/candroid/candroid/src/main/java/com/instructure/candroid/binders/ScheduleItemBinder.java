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
import android.widget.TextView;

import com.instructure.candroid.R;
import com.instructure.candroid.holders.ScheduleItemViewHolder;
import com.instructure.candroid.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.ScheduleItem;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.pandautils.utils.CanvasContextColor;

import java.util.Date;

public class ScheduleItemBinder extends BaseBinder {

    public static void bind(
            final ScheduleItemViewHolder holder,
            final ScheduleItem item,
            final Context context,
            final int courseColor,
            final String contextName,
            final AdapterToFragmentCallback<ScheduleItem> adapterToFragmentCallback) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterToFragmentCallback.onRowClicked(item, holder.getAdapterPosition(), false);
            }
        });

        switch (item.getType()) {

            case TYPE_SYLLABUS: {
                holder.title.setText(context.getString(R.string.syllabus));

                Drawable drawable = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_syllabus_fill, courseColor);
                holder.icon.setImageDrawable(drawable);
                break;
            }
            case TYPE_CALENDAR: {
                Drawable drawable = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_calendar_fill, courseColor);
                holder.icon.setImageDrawable(drawable);

                holder.title.setText(item.getTitle());

                holder.date.setText(item.getStartString(context));

                String description = getHtmlAsText(item.getDescription());
                setupDescription(description, holder.description);

                break;
            }
            case TYPE_ASSIGNMENT:
                holder.title.setText(item.getTitle());

                Drawable drawable;
                Assignment assignment = item.getAssignment();

                if(assignment != null) {

                    final int drawableResId = getAssignmentIcon(assignment);
                    drawable = CanvasContextColor.getColoredDrawable(context, drawableResId, courseColor);
                    holder.icon.setImageDrawable(drawable);

                    Date dueDate = assignment.getDueDate();
                    if(dueDate != null) {
                        String dateString = DateHelpers.createPrefixedDateTimeString(context, R.string.toDoDue, dueDate);
                        holder.date.setText(dateString);
                    } else {
                        holder.date.setText(context.getResources().getString(R.string.toDoNoDueDate));
                    }

                    String description = getHtmlAsText(assignment.getDescription());
                    setupDescription(description, holder.description);

                    //submissions aren't included with the assignments in the api call, so we don't get grades
                    //so we'll never see the grade
                    setInvisible(holder.points);

                } else {

                    drawable = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_calendar_fill, courseColor);
                    holder.icon.setImageDrawable(drawable);

                    holder.date.setText(item.getStartString(context));

                    String description = getHtmlAsText(item.getDescription());
                    setupDescription(description, holder.description);
                }

                break;

        }
    }

    private static void setupDescription(String description, TextView textView) {
        if (!TextUtils.isEmpty(description)) {
            textView.setText(description);
            setVisible(textView);
        } else {
            textView.setText("");
            setGone(textView);
        }
    }
}

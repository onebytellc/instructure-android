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

import com.instructure.candroid.R;
import com.instructure.candroid.holders.SyllabusItemViewHolder;
import com.instructure.candroid.holders.SyllabusViewHolder;
import com.instructure.candroid.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.ScheduleItem;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.utils.CanvasContextColor;

import java.util.Date;

public class SyllabusBinder extends BaseBinder {

    public static void bind(
            final Context context,
            final SyllabusViewHolder holder,
            final int courseColor,
            final ScheduleItem item,
            final AdapterToFragmentCallback<ScheduleItem> adapterToFragmentCallback) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterToFragmentCallback.onRowClicked(item, holder.getAdapterPosition(), false);
            }
        });

        switch (item.getType()) {

            case TYPE_CALENDAR:
            case TYPE_ASSIGNMENT:

                holder.title.setText(item.getTitle());

                Drawable drawable;
                Assignment assignment = item.getAssignment();

                if(assignment != null) {
                    int drawableResId = getAssignmentIcon(assignment);
                    drawable = CanvasContextColor.getColoredDrawable(context, drawableResId, courseColor);
                    holder.icon.setImageDrawable(drawable);

                    Date dueDate = assignment.getDueDate();
                    holder.date.setTextColor(context.getResources().getColor(R.color.secondaryText));
                    if(dueDate != null) {
                        String dateString = DateHelpers.createPrefixedDateTimeString(context, R.string.toDoDue, dueDate);
                        holder.date.setText(dateString);
                    } else {
                        holder.date.setText(context.getResources().getString(R.string.toDoNoDueDate));
                    }

                    setCleanText(holder.description, getHtmlAsText(assignment.getDescription()));

                    //currently submissions aren't returned for the syllabus fragment, so points will be null.
                    setGone(holder.points);

                } else {

                    drawable = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_calendar_fill, courseColor);
                    holder.icon.setImageDrawable(drawable);

                    setCleanText(holder.date, item.getStartDateString(context));
                    setCleanText(holder.description, getHtmlAsText(item.getDescription()));
                    holder.points.setText("");
                }

                break;
            default:
                Utils.d("UNSUPPORTED TYPE FOUND IN SYLLABUS BINDER");
                break;
        }
    }

    public static void bindSyllabusItem(
            final Context context,
            final SyllabusItemViewHolder holder,
            final int courseColor,
            final ScheduleItem item,
            final AdapterToFragmentCallback<ScheduleItem> adapterToFragmentCallback) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterToFragmentCallback.onRowClicked(item, holder.getAdapterPosition(), false);
            }
        });

        holder.title.setText(context.getString(R.string.syllabus));
        Drawable drawable = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_syllabus_fill, courseColor);
        holder.icon.setImageDrawable(drawable);
    }
}


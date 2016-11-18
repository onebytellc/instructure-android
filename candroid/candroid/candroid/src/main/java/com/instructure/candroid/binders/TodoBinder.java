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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import com.instructure.candroid.R;
import com.instructure.candroid.adapter.TodoListRecyclerAdapter;
import com.instructure.candroid.holders.TodoViewHolder;
import com.instructure.candroid.interfaces.NotificationAdapterToFragmentCallback;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.ToDo;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.pandautils.utils.CanvasContextColor;

public class TodoBinder extends BaseBinder {

    public static void bind(
            final Context context,
            final TodoViewHolder holder,
            final ToDo item,
            final NotificationAdapterToFragmentCallback<ToDo> adapterToFragmentCallback,
            final TodoListRecyclerAdapter.TodoCheckboxCallback checkboxCallback) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkboxCallback.isEditMode()){
                    checkboxCallback.onCheckChanged(item, !item.isChecked(), holder.getAdapterPosition());
                } else {
                    adapterToFragmentCallback.onRowClicked(item, holder.getAdapterPosition(), true);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(item.getIgnore() == null){
                    return false;
                }
                checkboxCallback.onCheckChanged(item, !item.isChecked(), holder.getAdapterPosition());
                return true;
            }
        });

        if(item.getCanvasContext() != null && item.getCanvasContext().getName() != null) {
            holder.course.setText(item.getCanvasContext().getName());
        } else if (item.getScheduleItem() != null && item.getScheduleItem().getContextType() == CanvasContext.Type.USER) {
            holder.course.setText(context.getString(R.string.PersonalCalendar));
        } else {
            holder.course.setText("");
        }

        //Get courseColor
        int courseColor = context.getResources().getColor(R.color.defaultPrimary);

        if(item.getCanvasContext() != null && item.getCanvasContext().getType() != CanvasContext.Type.USER) {
            courseColor = CanvasContextColor.getCachedColor(context, item.getCanvasContext());
        }

        if(item.isChecked()) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.lightgray));
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        String todoDetails = "";
        String titlePrefix = "";
        switch (item.getType()) {
            case SUBMITTING:
                titlePrefix = context.getString(R.string.toDoTurnIn) + " ";
                // don't break, just continue
            case UPCOMING_ASSIGNMENT:
                // upcoming assignments can be either grading or submitting and we don't know, so they have no prefix;
                holder.title.setText(titlePrefix + item.getTitle());
                todoDetails = DateHelpers.createPrefixedDateTimeString(context, R.string.dueAt, item.getComparisonDate());

                break;
            case GRADING:
                holder.title.setText(context.getResources().getString(R.string.grade) + " " + item.getTitle());
                //need to check how many there are so we put "needs grading" instead of "need grading" if there is only 1

                if (item.getNeedsGradingCount() == 1) {
                    todoDetails = (item.getNeedsGradingCount() + " " + context.getResources().getString(R.string.toDoNeedsGrading));
                } else if (item.getNeedsGradingCount() > 1) {
                    todoDetails = (item.getNeedsGradingCount() + " " + context.getResources().getString(R.string.toDoNeedGrading));
                }
                break;
            case UPCOMING_EVENT:
                holder.title.setText(item.getTitle());
                todoDetails = item.getScheduleItem().getStartToEndString(context);
                break;
            default:
                break;
        }

        if(!TextUtils.isEmpty(todoDetails)) {
            holder.description.setText(todoDetails);
            setVisible(holder.description);
        } else {
            holder.description.setText("");
            setGone(holder.description);
        }

        int drawableResId;

        if(item.getType() == ToDo.Type.UPCOMING_EVENT) {
            drawableResId = R.drawable.ic_cv_calendar_fill;
        } else if (item.getAssignment().getQuizId() > 0) {
            drawableResId = R.drawable.ic_cv_quizzes_fill;
        } else if (item.getAssignment().getDiscussionTopicHeader() != null) {
            drawableResId = R.drawable.ic_cv_discussions_fill;
        } else {
            drawableResId = R.drawable.ic_cv_assignments_fill;
        }

        Drawable drawable = CanvasContextColor.getColoredDrawable(context, drawableResId, courseColor);
        holder.icon.setImageDrawable(drawable);

    }
}

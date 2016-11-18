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
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;

import com.instructure.candroid.R;
import com.instructure.candroid.adapter.NotificationListRecyclerAdapter;
import com.instructure.candroid.holders.NotificationViewHolder;
import com.instructure.candroid.interfaces.NotificationAdapterToFragmentCallback;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.StreamItem;
import com.instructure.pandautils.utils.CanvasContextColor;

public class NotificationBinder extends BaseBinder {

    public static void bind(
            final Context context,
            final NotificationViewHolder holder,
            final StreamItem item,
            final NotificationListRecyclerAdapter.NotificationCheckboxCallback checkboxCallback,
            final NotificationAdapterToFragmentCallback<StreamItem> adapterToFragmentCallback) {

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                checkboxCallback.onCheckChanged(item, !item.isChecked(), holder.getAdapterPosition());
                return true;
            }
        });

        holder.title.setText(item.getTitle(context));

        int courseColor = CanvasContextColor.getCachedColor(context, item.getCanvasContext());

        //Course Name
        String courseName = "";
        if (item.getContextType() == CanvasContext.Type.COURSE && item.getCanvasContext() != null) {
            courseName = item.getCanvasContext().getSecondaryName();
        } else if(item.getContextType() == CanvasContext.Type.GROUP && item.getCanvasContext() != null) {
            courseName = item.getCanvasContext().getName();
        } else {
            courseName = "";
        }
        holder.course.setText(courseName);

        //Description
        if (!TextUtils.isEmpty(item.getMessage(context))) {
            holder.description.setText(getHtmlAsText(item.getMessage(context)));
            setVisible(holder.description);
        } else {
            holder.description.setText("");
            setGone(holder.description);
        }

        if(item.isChecked()) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.lightgray));
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        //Icon
        int drawableResId = 0;
        switch (item.getType()) {
            case DISCUSSION_TOPIC:
                drawableResId = R.drawable.ic_cv_discussions_fill;
                holder.icon.setContentDescription(context.getString(R.string.discussionIcon));
                break;
            case ANNOUNCEMENT:
                drawableResId = R.drawable.ic_cv_announcements_fill;
                holder.icon.setContentDescription(context.getString(R.string.announcementIcon));
                break;
            case SUBMISSION:
                drawableResId = R.drawable.ic_cv_assignments_fill;
                holder.icon.setContentDescription(context.getString(R.string.assignmentIcon));

                //need to prepend "Grade" in the message if there is a valid score
                if (item.getScore() != -1.0) {
                    //if the submission has a grade (like a letter or percentage) display it
                    if (item.getGrade() != null
                            && !item.getGrade().equals("")
                            && !item.getGrade().equals("null")) {
                        holder.description.setText(context.getResources().getString(R.string.grade) + ": " + item.getGrade());
                    } else {
                        holder.description.setText(context.getResources().getString(R.string.grade) + holder.description.getText());
                    }
                }
                break;
            case CONVERSATION:
                drawableResId = R.drawable.ic_cv_messages_fill;
                holder.icon.setContentDescription(context.getString(R.string.conversationIcon));
                break;
            case MESSAGE:
                if (item.getContextType() == CanvasContext.Type.COURSE) {
                    drawableResId = R.drawable.ic_cv_assignments_fill;
                    holder.icon.setContentDescription(context.getString(R.string.assignmentIcon));
                } else if (item.getNotificationCategory().toLowerCase().contains("assignment graded")) {
                    drawableResId = R.drawable.ic_cv_grades_fill;
                    holder.icon.setContentDescription(context.getString(R.string.gradesIcon));
                } else {
                    drawableResId = R.drawable.ic_cv_student_fill;
                    holder.icon.setContentDescription(context.getString(R.string.defaultIcon));
                }
                break;
            case CONFERENCE:
                drawableResId = R.drawable.ic_cv_conference_fill;
                holder.icon.setContentDescription(context.getString(R.string.icon));
                break;
            case COLLABORATION:
                drawableResId = R.drawable.ic_cv_collaboration_fill;
                holder.icon.setContentDescription(context.getString(R.string.icon));
                break;
            case COLLECTION_ITEM:
            default:
                drawableResId = R.drawable.ic_cv_peer_review_fill;
                break;
        }

        Drawable drawable = CanvasContextColor.getColoredDrawable(context, drawableResId, courseColor);
        holder.icon.setImageDrawable(drawable);

        //Read/Unread
        if (item.isReadState()) {
            holder.title.setTypeface(null, Typeface.NORMAL);
        } else {
            holder.title.setTypeface(null, Typeface.BOLD);
        }
    }
}

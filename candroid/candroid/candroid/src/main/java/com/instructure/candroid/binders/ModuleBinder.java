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
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;

import com.instructure.candroid.R;
import com.instructure.candroid.holders.ModuleSubHeaderViewHolder;
import com.instructure.candroid.holders.ModuleViewHolder;
import com.instructure.candroid.interfaces.ModuleAdapterToFragmentCallback;
import com.instructure.candroid.util.ModuleUtility;
import com.instructure.canvasapi.model.ModuleContentDetails;
import com.instructure.canvasapi.model.ModuleItem;
import com.instructure.canvasapi.model.ModuleObject;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.pandautils.utils.CanvasContextColor;

public class ModuleBinder extends BaseBinder {

    public static void bind(
            final ModuleViewHolder holder,
            final ModuleObject moduleObject,
            final ModuleItem moduleItem,
            final Context context,
            final ModuleAdapterToFragmentCallback adapterToFragmentCallback,
            final boolean isSequentiallEnabled,
            final int courseColor,
            final boolean isFirstItem,
            final boolean isLastItem) {

        boolean isLocked = ModuleUtility.isGroupLocked(moduleObject);

        holder.clickContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterToFragmentCallback.onRowClicked(moduleObject, moduleItem, holder.getAdapterPosition(), true);
            }
        });

        //Title
        holder.title.setText(moduleItem.getTitle());

        if(moduleItem.getType().equals(ModuleItem.TYPE.Locked.toString()) || moduleItem.getType().equals(ModuleItem.TYPE.ChooseAssignmentGroup.toString())) {
            holder.title.setTypeface(null, Typeface.ITALIC);
            holder.title.setTextColor(context.getResources().getColor(R.color.secondaryText));
        } else {
            holder.title.setTypeface(null, Typeface.NORMAL);
            holder.title.setTextColor(context.getResources().getColor(R.color.primaryText));
        }
        //Description
        if (moduleItem.getCompletionRequirement() != null && moduleItem.getCompletionRequirement().getType() != null) {
            setVisible(holder.description);
            holder.description.setTextColor(context.getResources().getColor(R.color.canvasTextMedium));
            String requireText = moduleItem.getCompletionRequirement().getType();
            if (requireText.equals(ModuleObject.STATE.must_submit.toString())) {
                if(moduleItem.getCompletionRequirement().isCompleted()){
                    holder.description.setText(context.getString(R.string.moduleItemSubmitted));
                    holder.description.setTextColor(courseColor);
                }else{
                    holder.description.setText(context.getString(R.string.moduleItemSubmit));
                }
            } else if (requireText.equals(ModuleObject.STATE.must_view.toString())) {
                if(moduleItem.getCompletionRequirement().isCompleted()){
                    holder.description.setText(context.getString(R.string.moduleItemViewed));
                }else{
                    holder.description.setText(context.getString(R.string.moduleItemMustView));
                }
            } else if (requireText.equals(ModuleObject.STATE.must_contribute.toString())) {
                if(moduleItem.getCompletionRequirement().isCompleted()){
                    holder.description.setText(context.getString(R.string.moduleItemContributed));
                }else{
                    holder.description.setText(context.getString(R.string.moduleItemContribute));
                }
            }
            //min_score only present when type == 'min_score'
            else if (requireText.equals(ModuleObject.STATE.min_score.toString())) {
                if(moduleItem.getCompletionRequirement().isCompleted()){
                    holder.description.setText(context.getString(R.string.moduleItemMinScoreMet));
                }else{
                    holder.description.setText(context.getString(R.string.moduleItemMinScore) + " " + moduleItem.getCompletionRequirement().getMin_score());
                }
            } else {
                holder.description.setText("");
                setGone(holder.description);
            }

        } else {
            holder.description.setText("");
            setGone(holder.description);
        }

        //Indicator
        setGone(holder.indicator);
        if (moduleItem.getCompletionRequirement() != null && moduleItem.getCompletionRequirement().isCompleted()) {
            Drawable drawable = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_save_white_thin, courseColor);
            holder.indicator.setImageDrawable(drawable);
            setVisible(holder.indicator);
        }

        if(isLocked) {
            Drawable drawable = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_locked_fill, courseColor);
            holder.indicator.setImageDrawable(drawable);
            setVisible(holder.indicator);
        }

        //Icon
        int drawableResource = -1;
        if (moduleItem.getType().equals(ModuleItem.TYPE.Assignment.toString())) {
            drawableResource = R.drawable.ic_cv_assignments_fill;
        } else if (moduleItem.getType().equals(ModuleItem.TYPE.Discussion.toString())) {
            drawableResource = R.drawable.ic_cv_discussions_fill;
        } else if (moduleItem.getType().equals(ModuleItem.TYPE.File.toString())) {
            drawableResource = R.drawable.ic_cv_download_dark;
        } else if (moduleItem.getType().equals(ModuleItem.TYPE.Page.toString())) {
            drawableResource = R.drawable.ic_cv_page;
        } else if (moduleItem.getType().equals(ModuleItem.TYPE.SubHeader.toString())) {
            setGone(holder.icon);
        } else if (moduleItem.getType().equals(ModuleItem.TYPE.Quiz.toString())) {
            drawableResource = R.drawable.ic_cv_quizzes_fill;
        } else if (moduleItem.getType().equals(ModuleItem.TYPE.ExternalUrl.toString())) {
            drawableResource = R.drawable.ic_cv_link_fill;
        } else if (moduleItem.getType().equals(ModuleItem.TYPE.ExternalTool.toString())) {
            drawableResource = R.drawable.ic_cv_tools_fill;
        } else if (moduleItem.getType().equals(ModuleItem.TYPE.Locked.toString())) {
            drawableResource = R.drawable.ic_cv_locked_fill;
        } else if (moduleItem.getType().equals(ModuleItem.TYPE.ChooseAssignmentGroup.toString())) {
            drawableResource = R.drawable.ic_cv_page;
        }

        if(drawableResource == -1) {
            setGone(holder.icon);
        } else {
            Drawable drawable = CanvasContextColor.getColoredDrawable(context, drawableResource, courseColor);
            holder.icon.setImageDrawable(drawable);
        }

        //Details
        ModuleContentDetails details = moduleItem.getModuleDetails();
        if(details != null) {
            if (details.getDueDate() != null) {
                holder.date.setText(DateHelpers.createPrefixedDateTimeString(context, R.string.toDoDue, details.getDueDate()));
                setVisible(holder.date);
            } else {
                holder.date.setText("");
                setGone(holder.date);
            }

            String points = details.getPointsPossible();
            if(!TextUtils.isEmpty(points)) {
                setGrade(null, Double.parseDouble(points), holder.points, context);
                setVisible(holder.points);
            } else {
                holder.points.setText("");
                setGone(holder.points);
            }
        } else {
            holder.points.setText("");
            holder.date.setText("");
            setGone(holder.date);
            setGone(holder.points);
        }

        updateShadows(isFirstItem, isLastItem, holder.shadowTop, holder.shadowBottom);
    }

    public static void bindSubHeader(
            final ModuleSubHeaderViewHolder holder,
            final ModuleObject moduleObject,
            final ModuleItem moduleItem,
            final boolean isFirstItem,
            final boolean isLastItem) {

        if(moduleItem.getType().equals(ModuleItem.TYPE.SubHeader.toString())) {
            holder.subTitle.setText(moduleItem.getTitle());
        }

        updateShadows(isFirstItem, isLastItem, holder.shadowTop, holder.shadowBottom);
    }
}

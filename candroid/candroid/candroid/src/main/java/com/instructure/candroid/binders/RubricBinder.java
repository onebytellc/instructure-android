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
import com.instructure.candroid.holders.RubricViewHolder;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.RubricCriterionRating;
import com.instructure.pandautils.utils.CanvasContextColor;

import java.text.DecimalFormat;
import java.util.Locale;

public class RubricBinder extends BaseBinder {

    public static void bind(Context context, final RubricViewHolder holder, final RubricCriterionRating rating, final CanvasContext canvasContext) {
        switch(holder.rubricType){
            case RubricViewHolder.TYPE_ITEM_COMMENT:
                bindComments(context, holder, rating, canvasContext);
                break;
            default:
                bindPoints(context, holder, rating, canvasContext);
        }
    }

    private static void bindPoints(Context context, final RubricViewHolder holder, final RubricCriterionRating rating, final CanvasContext canvasContext) {
        int color = rating.isGrade() ? CanvasContextColor.getCachedColor(context, canvasContext) : context.getResources().getColor(R.color.canvasTextMedium);

        //if the course color is gray it is hard/impossible to tell which one is selected, so make it black
        if(rating.isGrade() && CanvasContextColor.getCachedColor(context, canvasContext) == context.getResources().getColor(R.color.courseGrey)) {
            color = context.getResources().getColor(R.color.black);
        }
        holder.descriptionView.setText(rating.getRatingDescription());
        holder.descriptionView.setTextColor(color);
        holder.pointView.setText(getScoreText(context, rating, holder.rubricType));
        holder.pointView.setTextColor(color);
        holder.checkmark.setBackgroundColor(color);
    }

    private static void bindComments(Context context, final RubricViewHolder holder, final RubricCriterionRating rating, final CanvasContext canvasContext) {
        int color = CanvasContextColor.getCachedColor(context, canvasContext);
        Drawable d = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_chat_fill, color);

        holder.descriptionView.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
        holder.descriptionView.setText(rating.getComments());

        if(rating.isComment() && !rating.isFreeFormComment()){
            holder.pointView.setVisibility(View.GONE);
        }else{
            holder.pointView.setVisibility(View.VISIBLE);
            holder.pointView.setText(getScoreText(context, rating, holder.rubricType));
        }
    }

    private static String getScoreText(final Context context, final RubricCriterionRating rating, final int rubricType){
        final double value = rating.getPoints();
        final double maxValue = rating.getMaxPoints();

        String points = "";
        if(rating.isFreeFormComment()){
            DecimalFormat format = new DecimalFormat("0.#");
            points = String.format( Locale.getDefault(),
                    context.getString(R.string.freeFormRubricPoints),
                    format.format(value),
                    format.format(maxValue));
        }
        else if (Math.floor(value) == value) {
            points += (int)value;
        }
        else {
            points += value;
        }

        return points;
    }
}

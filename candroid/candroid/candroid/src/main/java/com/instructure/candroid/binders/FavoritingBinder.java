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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.instructure.candroid.R;
import com.instructure.candroid.holders.FavoritingViewHolder;
import com.instructure.candroid.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Group;
import com.instructure.loginapi.login.util.ColorUtils;
import com.instructure.pandautils.utils.CanvasContextColor;

public class FavoritingBinder extends BaseBinder{

    public static void bind(
            final Context context,
            final CanvasContext canvasContext,
            final FavoritingViewHolder holder,
            final AdapterToFragmentCallback<CanvasContext> adapterToFragmentCallback) {

        final int color = CanvasContextColor.getCachedColor(context, canvasContext);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterToFragmentCallback.onRowClicked(canvasContext, holder.getAdapterPosition(), false);
            }
        });

        Drawable drawable = holder.courseColorIndicator.getBackground();
        holder.courseColorIndicator.setBackgroundDrawable(ColorUtils.colorIt(color, drawable));
        holder.courseName.setText(canvasContext.getName());

        if(canvasContext instanceof Course) {
            //Is a course
            if(((Course)canvasContext).isFavorite()) {
                holder.courseColorIndicator.setImageDrawable(ColorUtils.colorIt(context, Color.WHITE, R.drawable.ic_star));
            } else {
                holder.courseColorIndicator.setImageDrawable(null);
            }
        } else if(canvasContext instanceof Group){
            //Is a Group
            if(((Group)canvasContext).isFavorite()) {
                holder.courseColorIndicator.setImageDrawable(ColorUtils.colorIt(context, Color.WHITE, R.drawable.ic_star));
            } else {
                holder.courseColorIndicator.setImageDrawable(null);
            }
        }
    }
}

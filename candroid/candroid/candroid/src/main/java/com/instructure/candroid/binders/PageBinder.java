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
import android.view.View;

import com.instructure.candroid.R;
import com.instructure.candroid.holders.PageViewHolder;
import com.instructure.candroid.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi.model.Page;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.pandautils.utils.CanvasContextColor;

public class PageBinder extends BaseBinder {

    public static void bind(
            final Context context,
            final PageViewHolder holder,
            final Page page,
            final int courseColor,
            final AdapterToFragmentCallback<Page> adapterToFragmentCallback) {

        holder.title.setText(page.getTitle());

        if (page.isFrontPage()) {
            holder.icon.setImageDrawable(CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_page_fill, courseColor));
        } else {
            holder.icon.setImageDrawable(CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_document_fill, courseColor));
        }

        holder.modifiedDate.setText(DateHelpers.getFormattedDate(context, page.getUpdated_at()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterToFragmentCallback.onRowClicked(page, holder.getAdapterPosition(), true);
            }
        });
    }
}

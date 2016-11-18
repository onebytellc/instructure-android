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
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.instructure.candroid.R;
import com.instructure.candroid.holders.FileViewHolder;
import com.instructure.candroid.interfaces.AdapterToFragmentLongClick;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.FileFolder;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

public class FileBinder extends  BaseBinder{

    public static void bind(
            final FileViewHolder holder,
            final FileFolder item,
            Context context,
            final CanvasContext canvasContext,
            final AdapterToFragmentLongClick<FileFolder> adapterToFragmentLongClick){

        int color = CanvasContextColor.getCachedColor(context, canvasContext);

        //set the default padding and margins of the different layouts. When there is a .jpg or .png
        //we make that image bigger and change margins/padding to keep the text aligned.
        ViewGroup.LayoutParams params = holder.icon.getLayoutParams();
        params.height = (int)context.getResources().getDimension(R.dimen.gridIconSize);
        params.width = (int)context.getResources().getDimension(R.dimen.gridIconSize);
        holder.icon.setLayoutParams(params);

        int defaultPadding = (int)context.getResources().getDimension(R.dimen.card_header_margins);
        holder.rootView.setPadding(defaultPadding, 0, defaultPadding, 0);
        RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams)holder.textContainer.getLayoutParams();
        linearParams.setMargins(defaultPadding, defaultPadding, defaultPadding, defaultPadding);
        holder.textContainer.setLayoutParams(linearParams);

        if(item.getDisplayName() != null) {
            //this means it is a item object
            holder.fileName.setText(item.getDisplayName());
            holder.fileDetails.setText(readableFileSize(item.getSize()));

            if(item.isLocked() || item.isLockedForUser()) {
                Drawable drawable = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_locked_fill, color);
                holder.icon.setImageDrawable(drawable);
            } else {
                if(item.getThumbnailUrl() != null) {
                    // we have a thumbnail url, display it
                    // Adjust the margins and padding so we can make the image bigger but keep alignment the same
                    params.height = (int)context.getResources().getDimension(R.dimen.listHeaderHeight);
                    params.width = (int)context.getResources().getDimension(R.dimen.listHeaderHeight);
                    holder.icon.setLayoutParams(params);
                    Picasso.with(context).load(item.getThumbnailUrl()).into(holder.icon);
                    int padding = (int)context.getResources().getDimension(R.dimen.colorItemMarginLeftRight);
                    holder.rootView.setPadding(padding, 0, padding, 0);
                    linearParams.setMargins(padding, padding, padding, padding);
                    holder.textContainer.setLayoutParams(linearParams);
                } else {
                    Drawable drawable = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_document_fill, color);
                    holder.icon.setImageDrawable(drawable);
                }
            }
        } else {
            //folder object
            holder.fileName.setText(item.getName());
            int itemCount = item.getFilesCount() + item.getFolders_count();
            String text;
            if(itemCount == 1) {
                text = itemCount + " " + context.getResources().getString(R.string.singleItem);
            }
            else {
                text = itemCount + " " + context.getResources().getString(R.string.multipleItems);
            }
            holder.fileDetails.setText(text);

            if(item.isLocked() || item.isLockedForUser()) {
                Drawable drawable = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_locked_fill, color);
                holder.icon.setImageDrawable(drawable);
            } else {
                Drawable drawable = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_cv_folder_fill, color);
                holder.icon.setImageDrawable(drawable);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapterToFragmentLongClick.onRowClicked(item, holder.getAdapterPosition(), false);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                adapterToFragmentLongClick.onRowLongClicked(item, holder.getAdapterPosition());
                return false;
            }
        });


    }

    //helper function to make the size of a file look better
    public static String readableFileSize(long size) {
        if(size <= 0) return "0 bytes";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

}

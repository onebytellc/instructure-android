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

package com.instructure.androidpolling.app.rowfactories;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.instructure.androidpolling.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PollSessionRowFactory {

    static class ViewHolder {
        @BindView(R.id.title)
        TextView txtTitle;

        @BindView(R.id.sectionName)
        TextView sectionName;

        @BindView(R.id.isPublished)
        ImageView isPublished;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public static View buildRowView(LayoutInflater layoutInflater, Context context, String courseName, String sectionName, boolean isPublished, View convertView) {
        ViewHolder holder = null;

        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listview_item_poll_session, null, false);

            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.txtTitle.setText(courseName);
        holder.sectionName.setText(sectionName);
        if(isPublished) {
            holder.isPublished.setVisibility(View.VISIBLE);
            holder.isPublished.setColorFilter(context.getResources().getColor(R.color.canvaspollingtheme_color), PorterDuff.Mode.SRC_IN);
        }
        else {
            holder.isPublished.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }
}

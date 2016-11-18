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

public class QuestionRowFactory {

    static class ViewHolder {
        @BindView(R.id.title)
        TextView txtTitle;

        @BindView(R.id.isPublished)
        ImageView selected;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    static class GroupViewHolder {
        @BindView(R.id.groupText)
        TextView groupText;

        public GroupViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public static View buildRowView(LayoutInflater layoutInflater, Context context, String question, boolean hasActiveSession, View convertView) {
        ViewHolder holder = null;

        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listview_item_question, null, false);

            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.txtTitle.setText(question);
        if(hasActiveSession) {
            holder.selected.setVisibility(View.VISIBLE);
            holder.selected.getDrawable().setColorFilter(context.getResources().getColor(R.color.canvaspollingtheme_color), PorterDuff.Mode.SRC_IN);
        }
        else {
            holder.selected.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    public static View buildGroupView(LayoutInflater inflater, String groupName, View convertView) {
        GroupViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_group_questions, null);
            holder = new GroupViewHolder(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (GroupViewHolder) convertView.getTag();
        }

        holder.groupText.setText(groupName);

        return convertView;
    }
}

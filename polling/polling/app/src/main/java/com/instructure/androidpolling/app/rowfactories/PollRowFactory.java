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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.instructure.androidpolling.app.R;
import com.instructure.canvasapi.utilities.APIHelpers;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PollRowFactory {

    ///////////////////////////////////////////////////////////////////////////
    // View Holder
    ///////////////////////////////////////////////////////////////////////////



    static class ViewHolder {
        @BindView(R.id.title)
        TextView title;

        @BindView(R.id.sectionName)
        TextView sectionName;

        @BindView(R.id.createdDate)
        TextView createdDate;

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

    public static View buildRowView(LayoutInflater inflater, String sectionName, String question, View convertView, Context context, Date createdAt) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_item_poll_session_student, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(question);
        holder.sectionName.setText(sectionName);
        holder.createdDate.setText(APIHelpers.dateToDayMonthYearString(context, createdAt));

        return convertView;
    }


    public static View buildGroupView(LayoutInflater inflater, String groupName, View convertView) {
        GroupViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_group_open_polls, null);
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

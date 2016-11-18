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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.model.AnswerValue;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StudentPollRowFactory {

    public static View buildRowView(LayoutInflater layoutInflater, Context context, AnswerValue answerValue, final int position, boolean hasSubmitted, boolean isPublished, View convertView) {
        ViewHolder holder = null;
        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listview_item_student_answer, null, false);

            holder = new ViewHolder(convertView);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.answerText.setText(answerValue.getValue());


        if(answerValue.isSelected()) {
            holder.selected.setChecked(true);
        }
        else {
            holder.selected.setChecked(false);
        }

        //if we've submitted stuff, we want to gray everything out and remove the background
        if(hasSubmitted || !isPublished) {
            holder.containerLayout.setBackgroundColor(context.getResources().getColor(R.color.white));
            //don't want to let it be clicked if the user has submitted the poll
            holder.selected.setEnabled(false);
            if(!answerValue.isSelected()) {
                holder.answerText.setTextColor(context.getResources().getColor(R.color.gray));
            }
        }
        else {
            holder.selected.setEnabled(true);
        }
        return convertView;

    }

    static class ViewHolder {

        @BindView(R.id.add_answer_container)
        LinearLayout containerLayout;

        @BindView(R.id.correctAnswer)
        RadioButton selected;

        @BindView(R.id.answer_text)
        TextView answerText;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}

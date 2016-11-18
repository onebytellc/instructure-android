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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import com.instructure.androidpolling.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PollResultsRowFactory {

     static class ViewHolder {
        @BindView(R.id.answer)
        TextView answer;

        @BindView(R.id.numAnswered)
        TextView numAnswered;

        @BindView(R.id.correct_answer)
        RadioButton correctAnswer;

        @BindView(R.id.percent_answered)
        ProgressBar percentAnswered;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public static View buildRowView(LayoutInflater layoutInflater, Context context, String answer, int percentAnswered, boolean isCorrect, View convertView, int position) {
        ViewHolder holder = null;

        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.listview_item_poll_result, null, false);

            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.numAnswered.setText(Integer.toString(percentAnswered) + "%");
        holder.answer.setText(answer);
        holder.percentAnswered.setProgress(percentAnswered);
        holder.correctAnswer.setChecked(isCorrect);
        if(!isCorrect) {
            switch (position%3) {
                case 0:
                    holder.percentAnswered.getProgressDrawable().setColorFilter(context.getResources().getColor(R.color.polling_aqua), PorterDuff.Mode.SRC_IN);
                    break;
                case 1:
                    holder.percentAnswered.getProgressDrawable().setColorFilter(context.getResources().getColor(R.color.polling_green), PorterDuff.Mode.SRC_IN);
                    break;
                case 2:
                    holder.percentAnswered.getProgressDrawable().setColorFilter(context.getResources().getColor(R.color.canvaspollingtheme_color), PorterDuff.Mode.SRC_IN);
                    break;
                default:
                    holder.percentAnswered.getProgressDrawable().setColorFilter(context.getResources().getColor(R.color.canvaspollingtheme_color), PorterDuff.Mode.SRC_IN);
            }


        }
        else {
            holder.percentAnswered.getProgressDrawable().setColorFilter(context.getResources().getColor(R.color.polling_purple), PorterDuff.Mode.SRC_IN);
        }
        //we don't want the user to be able to click it
        holder.correctAnswer.setEnabled(false);

        return convertView;
    }
}

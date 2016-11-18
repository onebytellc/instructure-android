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

package com.instructure.candroid.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.instructure.candroid.R;

public class GradeViewHolder extends RecyclerView.ViewHolder {

    public TextView title, date, points;
    public ImageView icon, edit;

    public GradeViewHolder(View itemView) {
        super(itemView);
        title = (TextView)itemView.findViewById(R.id.title);
        date = (TextView)itemView.findViewById(R.id.date);
        points = (TextView)itemView.findViewById(R.id.points);
        icon = (ImageView)itemView.findViewById(R.id.icon);
        edit = (ImageView)itemView.findViewById(R.id.edit);
    }

    public static int holderResId(){
        return R.layout.viewholder_grade;
    }
}

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

package com.instructure.parentapp.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.instructure.parentapp.R;

public class AlertViewHolder extends RecyclerView.ViewHolder {

    public ImageView icon, closeButton;
    public FrameLayout iconWrapper;
    public TextView title, description, grade, date;

    public AlertViewHolder(View itemView) {
        super(itemView);

        icon = (ImageView)itemView.findViewById(R.id.icon);
        iconWrapper = (FrameLayout)itemView.findViewById(R.id.iconWrapper);
        closeButton = (ImageView)itemView.findViewById(R.id.closeButton);

        title = (TextView)itemView.findViewById(R.id.title);
        description = (TextView)itemView.findViewById(R.id.description);
        grade = (TextView)itemView.findViewById(R.id.grade);
        date = (TextView)itemView.findViewById(R.id.date);

    }

    public static int holderResId() {
        return R.layout.viewholder_alert_card;
    }
}

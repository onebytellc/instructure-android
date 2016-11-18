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
import android.widget.CompoundButton;
import android.widget.TextView;

import com.instructure.candroid.R;
import com.instructure.candroid.holders.RecipientViewHolder;
import com.instructure.candroid.interfaces.AdapterToFragmentCallback;
import com.instructure.candroid.interfaces.RecipientAdapterToFragmentCallback;
import com.instructure.canvasapi.model.Page;
import com.instructure.canvasapi.model.Recipient;

public class RecipientBinder {
    public static void bind(Context context, final RecipientViewHolder holder, final Recipient recipient, final RecipientAdapterToFragmentCallback<Recipient> adapterToFragmentCallback, boolean isSelected) {
        holder.title.setText(recipient.getName());

        if(recipient.getRecipientType() == Recipient.Type.group) {
            holder.userCount.setText(Integer.toString(recipient.getUser_count()));
        } else {
            holder.userCount.setText("");
        }
        boolean isCurrentUser =adapterToFragmentCallback.isRecipientCurrentUser(recipient);
        if (recipient.getRecipientType() == Recipient.Type.metagroup || isCurrentUser) {
            holder.checkBox.setVisibility(View.INVISIBLE); // layout depends on the checkbox, so just make it invisible
        } else {
            holder.checkBox.setVisibility(View.VISIBLE);
        }


        if (isSelected) {
            holder.itemView.setBackgroundResource(R.color.canvasBlueGreyRowSelected);
        } else {
            if (isCurrentUser) {
                holder.itemView.setBackgroundResource(R.color.lightgray);
            } else {
                holder.itemView.setBackgroundResource(R.color.white);
            }
        }
        holder.checkBox.setChecked(isSelected);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterToFragmentCallback.onRowClicked(recipient, holder.getAdapterPosition(), false, false);
            }
        });

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!buttonView.isPressed()) return;
                adapterToFragmentCallback.onRowClicked(recipient, holder.getAdapterPosition(), false, true);
            }
        });
    }
}

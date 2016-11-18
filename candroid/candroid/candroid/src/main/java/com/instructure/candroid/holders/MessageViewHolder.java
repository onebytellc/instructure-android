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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instructure.candroid.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView userAvatar;
    public ImageView attachmentImage;
    public TextView userNameText;
    public TextView lastMessageText;
    public TextView subject;
    public TextView dateText;
    public View itemView;
    public RelativeLayout rootView;

    public MessageViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        this.rootView = (RelativeLayout) itemView.findViewById(R.id.rootView);
        userAvatar = (CircleImageView) itemView.findViewById(R.id.userAvatar);
        attachmentImage = (ImageView) itemView.findViewById(R.id.attachment);
        userNameText = (TextView) itemView.findViewById(R.id.userNameText);
        lastMessageText = (TextView) itemView.findViewById(R.id.lastMessageText);
        dateText = (TextView) itemView.findViewById(R.id.dateText);
        subject = (TextView) itemView.findViewById(R.id.subjectText);
    }

    public static int holderResId() {
        return R.layout.viewholder_message;
    }

}

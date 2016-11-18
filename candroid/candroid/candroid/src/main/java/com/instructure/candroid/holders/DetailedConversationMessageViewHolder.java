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

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instructure.candroid.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailedConversationMessageViewHolder extends RecyclerView.ViewHolder {
    public static final int TYPE_LEFT_TEXT = 33;
    public static final int TYPE_RIGHT_TEXT = 23;

    public RelativeLayout messageWrap;
    public CircleImageView avatar;
    public TextView messageTextView;
    public TextView subjectTextView;
    public TextView username;
    public TextView participants;
    public TextView dateText;
    public CardView cardView;

    public DetailedConversationMessageViewHolder(View itemView) {
        super(itemView);
        messageWrap = (RelativeLayout) itemView.findViewById(R.id.messageWrap);
        avatar = (CircleImageView) itemView.findViewById(R.id.avatar);
        messageTextView = (TextView) itemView.findViewById(R.id.message);
        subjectTextView = (TextView) itemView.findViewById(R.id.subject);
        username = (TextView) itemView.findViewById(R.id.username);
        participants = (TextView) itemView.findViewById(R.id.participants);
        dateText = (TextView) itemView.findViewById(R.id.dateText);
        cardView = (CardView) itemView.findViewById(R.id.cardContainer);
    }

    public static int holderResId(int viewType) {
        return viewType == TYPE_LEFT_TEXT ? R.layout.viewholder_conversation_left_avatar : R.layout.viewholder_conversation_right_avatar;
    }
}

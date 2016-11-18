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
import android.widget.ImageView;
import android.widget.TextView;
import com.instructure.candroid.R;

public class DetailedConversationAttachmentViewHolder extends RecyclerView.ViewHolder {
    public static final int TYPE_LEFT_ATTACHMENT = 4033;
    public static final int TYPE_RIGHT_ATTACHMENT = 4323;

    public ImageView attachment;
    public CardView cardView;
    public TextView username;
    public TextView attachmentName;
    public TextView participants;

    public DetailedConversationAttachmentViewHolder(View itemView) {
        super(itemView);
        cardView = (CardView) itemView.findViewById(R.id.cardContainer);
        attachment = (ImageView) itemView.findViewById(R.id.attachment);
        username = (TextView) itemView.findViewById(R.id.username);
        participants = (TextView) itemView.findViewById(R.id.participants);
        attachmentName = (TextView) itemView.findViewById(R.id.attachmentName);
    }

    public static int holderResId(int viewType){
        return viewType == TYPE_LEFT_ATTACHMENT ? R.layout.viewholder_conversation_left_attachment : R.layout.viewholder_conversation_right_attachment;
    }
}

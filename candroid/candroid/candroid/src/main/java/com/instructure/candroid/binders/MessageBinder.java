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
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import com.instructure.candroid.R;
import com.instructure.candroid.adapter.MessageListRecyclerAdapter;
import com.instructure.candroid.holders.MessageViewHolder;
import com.instructure.candroid.util.Const;
import com.instructure.canvasapi.model.BasicUser;
import com.instructure.canvasapi.model.Conversation;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.loginapi.login.util.ProfileUtils;
import com.instructure.pandautils.utils.CanvasContextColor;

import java.util.Date;
import java.util.List;

public class MessageBinder extends BaseBinder{

    public static void bind(
            final MessageViewHolder holder,
            final Conversation item,
            final Context context,
            final long myUserID,
            final boolean isItemMultiSelected,
            final MessageListRecyclerAdapter.ItemClickedInterface itemClickedInterface) {

        setVisible(holder.userNameText);

        String messageTitle = item.getMessageTitle(myUserID, context.getString(R.string.monologue));

        ProfileUtils.configureAvatarView(context, messageTitle, item.getAvatarURL(), holder.userAvatar, isGroupMessage(item.getAllParticipants()));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.userAvatar.setTransitionName(Const.MESSAGE +String.valueOf(item.getId()));
        }

        holder.userNameText.setText(messageTitle);
        holder.lastMessageText.setText(item.getLastMessagePreview());

        if (item.hasAttachments() || item.hasMedia()) {
            Drawable attachment = CanvasContextColor.getColoredDrawable(context, R.drawable.conversation_attachment, context.getResources().getColor(R.color.canvasTextMedium));
            holder.attachmentImage.setImageDrawable(attachment);
            holder.attachmentImage.setVisibility(View.VISIBLE);
        } else {
            holder.attachmentImage.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(item.getLastMessageAt())) {
            holder.dateText.setText(getParsedDate(context, item.getLastMessageAt()));
        } else {
            holder.dateText.setText(getParsedDate(context, item.getLastAuthoredMessageAt()));
        }

        if(!TextUtils.isEmpty(item.getSubject())){
            holder.subject.setText(item.getSubject());
        } else {
            holder.subject.setText(context.getString(R.string.noSubject));
        }

        if (item.getWorkflowState() == Conversation.WorkflowState.UNREAD) {
            holder.userNameText.setTextColor(context.getResources().getColor(R.color.canvasTextDark));
            holder.userNameText.setTypeface(null, Typeface.BOLD);
            holder.subject.setTextColor(context.getResources().getColor(R.color.canvasTextDark));
            holder.dateText.setTypeface(null, Typeface.BOLD);
            holder.dateText.setTextColor(context.getResources().getColor(R.color.canvas_blue_discussion_unread));
        } else {
            holder.userNameText.setTextColor(context.getResources().getColor(R.color.canvasTextDark));
            holder.userNameText.setTypeface(null, Typeface.NORMAL);
            holder.subject.setTextColor(context.getResources().getColor(R.color.canvasTextMedium));
            holder.dateText.setTextColor(context.getResources().getColor(R.color.canvasTextMedium));
            holder.dateText.setTypeface(null, Typeface.NORMAL);
        }

        if (isItemMultiSelected) {
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.canvasBlueRowSelected));
        } else {
            holder.rootView.setBackgroundDrawable(getSelectableBackgroundDrawable(context));
        }

        ifHasTextSetVisibleElseGone(holder.lastMessageText);
        ifHasTextSetVisibleElseGone(holder.dateText);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickedInterface.itemClick(item, holder);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                itemClickedInterface.itemLongClick(item, holder);
                return true;
            }
        });
    }

    private static Drawable getSelectableBackgroundDrawable(Context context){
        int[] attrs = { android.R.attr.selectableItemBackground };
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs);
        return ta.getDrawable(0);
    }

    private static boolean isGroupMessage(List<BasicUser> participants){
        return participants.size() > 2;
    }

    private static String getParsedDate(Context context, String messageDate){
        Date date = APIHelpers.stringToDate(messageDate);
        return DateHelpers.getDayMonthDateString(context, date);
    }
}

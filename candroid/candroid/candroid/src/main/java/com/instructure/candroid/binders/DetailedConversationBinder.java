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
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.instructure.candroid.R;
import com.instructure.candroid.holders.DetailedConversationAttachmentViewHolder;
import com.instructure.candroid.holders.DetailedConversationMessageViewHolder;
import com.instructure.candroid.interfaces.DetailedConversationAdapterToFragmentCallback;
import com.instructure.candroid.model.MessageAttachment;
import com.instructure.candroid.model.MessageWithDepth;
import com.instructure.canvasapi.model.BasicUser;
import com.instructure.canvasapi.model.Message;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.loginapi.login.util.ProfileUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

// At one point, there was only two xml layouts. One for messages, one for attachments. There were some cases with adjusting the layout for left and right, where it
// became difficult to manage. Thus, now there is 4 xml layouts.
public class DetailedConversationBinder extends BaseBinder {

    public static void bindMessageText(DetailedConversationMessageViewHolder holder, MessageWithDepth item, Context context, List<BasicUser> allParticipants){
        Message message = item.message;

        BasicUser user = getBasicUser(allParticipants, message.getAuthorID());
        final boolean showName = shouldShowName(message, null);
        if (null != user) {
            ProfileUtils.configureAvatarView(context, user.getUsername(), user.getAvatarUrl(), holder.avatar, false);
            if(showName){
                holder.username.setText(user.getUsername());
                holder.username.setVisibility(View.VISIBLE);
            } else {
                holder.username.setVisibility(View.GONE);
            }
        } else {
            holder.username.setVisibility(View.GONE);
        }
        String dateString = DateHelpers.getMessageDateString(context, message.getCreationDate());
        holder.dateText.setText(dateString);
        holder.dateText.setVisibility(View.VISIBLE);

        final boolean isUser = isUser(context, item.message.getAuthorID());

        if (isUser) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.conversation_blue_bg));
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.conversation_gray_bg));
        }

        if(item.isAllParticipants){
            holder.participants.setVisibility(View.GONE);
        }else{
            if(showName){
                holder.participants.setVisibility(View.VISIBLE);
                holder.participants.setText(item.participantsString);
            }else{
                holder.participants.setVisibility(View.GONE);
            }
        }

        //Sets up the message text
        holder.messageTextView.setText(message.getBody());
    }

    public static void bindAttachment(
            Context context,
            DetailedConversationAttachmentViewHolder holder,
            MessageWithDepth item,
            final MessageAttachment attachment,
            List<BasicUser> allParticipants,
            final DetailedConversationAdapterToFragmentCallback detailedConversationCallback) {

        BasicUser user = getBasicUser(allParticipants, item.message.getAuthorID());
        if (null != user && shouldShowName(item.message, attachment.getUrl())) {
            holder.username.setText(user.getUsername());
            holder.username.setVisibility(View.VISIBLE);
        } else{
            holder.username.setVisibility(View.GONE);
        }

        final boolean isUser = isUser(context, item.message.getAuthorID());
        final boolean showName = shouldShowName(item.message, attachment.getUrl());

        if (isUser) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.conversation_blue_bg));
            holder.attachment.setBackgroundColor(context.getResources().getColor(R.color.conversation_blue_bg));
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.conversation_gray_bg));
            holder.attachment.setBackgroundColor(context.getResources().getColor(R.color.conversation_gray_bg));
        }

        if(item.isAllParticipants){
            holder.participants.setVisibility(View.GONE);
        }else{
            if(showName){
                holder.participants.setVisibility(View.VISIBLE);
                holder.participants.setText(item.participantsString);
            }else{
                holder.participants.setVisibility(View.GONE);
            }
        }

        if(attachment.getThumbnailUrl(context) != null) {
            holder.attachmentName.setVisibility(View.GONE);
            holder.attachment.setVisibility(View.VISIBLE);
            Picasso.with(context).load(attachment.getImageUrl(context)).fit().centerCrop().into(holder.attachment);
        } else {
            holder.attachmentName.setVisibility(View.VISIBLE);
            holder.attachmentName.setText(attachment.getFileName());
            holder.attachmentName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cv_attachment, 0, 0, 0);
            holder.attachment.setVisibility(View.GONE);
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (detailedConversationCallback != null) {
                    detailedConversationCallback.openMediaFromRow(attachment.getMimeType(), attachment.getUrl(), attachment.getFileName());
                }
            }
        });
        holder.cardView.setTag(attachment);
        detailedConversationCallback.register(holder.cardView);
    }

    private static BasicUser getBasicUser(List<BasicUser> allParticipants, long authorId){
        BasicUser user = new BasicUser(authorId, null);
        int index = allParticipants.indexOf(user);

        if (index != -1) {
            //Sets up the Avatar
            return allParticipants.get(index);
        }
        return null;
    }

    private static boolean isUser(Context context, long authorId){
        return authorId == APIHelpers.getCacheUser(context.getApplicationContext()).getId();
    }

    private static boolean shouldShowName(Message message, @Nullable String currentAttachmentUrl){
        if (currentAttachmentUrl != null &&
                message.getMediaComment() != null &&
                message.getMediaComment().getUrl() != null &&
                message.getMediaComment().getUrl().equalsIgnoreCase(currentAttachmentUrl)){
            // media comments show up on top. So always show username for mediacomments.
            return true;
        } else if(currentAttachmentUrl != null &&
                message.getMediaComment() == null &&
                message.getAttachments().get(message.getAttachments().size()-1).getUrl() != null &&
                message.getAttachments().get(message.getAttachments().size()-1).getUrl().equalsIgnoreCase(currentAttachmentUrl)){
            // this is the last attachment and no media comment exists. Show the username
            return true;
        } else if(currentAttachmentUrl == null && message.getAttachments().size() == 0 && message.getMediaComment() == null){
            // this is a message view and no attachments or media comments exist.
            return true;
        }
        return false;
    }
}

//    TODO : documentation says this has been deprecated, but could be potentially be undeprecated(or already has?? need more clarification)
//    private static void populateSubmission(Message message, DetailedConversationViewHolder holder , List<BasicUser> allParticipants, final Context context){}
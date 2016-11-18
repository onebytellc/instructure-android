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

package com.instructure.candroid.decorations;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.instructure.candroid.adapter.DetailedConversationRecyclerAdapter;
import com.instructure.candroid.holders.DetailedConversationAttachmentViewHolder;
import com.instructure.candroid.holders.DetailedConversationMessageViewHolder;
import com.instructure.candroid.view.ViewUtils;
import com.instructure.pandarecycler.util.Types;

public class ConversationDecorator extends RecyclerView.ItemDecoration {

    private static final int TEXT_MARGIN_SIZE = 12;
    private static final int ATTACHMENT_MARGIN_SIZE = 2;

    private static Integer messageMarginInDips;
    private static Integer attachmentMarginInDips;

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        final Context context = view.getContext().getApplicationContext();
        final int index       = parent.getChildAdapterPosition(view);
        final int viewType    = parent.getAdapter().getItemViewType(index);

        if( isTextMessage(viewType) ){
            // if it's a header with attachments. We don't want to apply a large margin
            outRect.top    = hasAttachments(parent, index) ? getAttachmentMargin(context) : getTextMarginSize(context);
            outRect.bottom = getTextMarginSize(context);
        } else {
            outRect.top    = getAttachmentMargin(context);
            outRect.bottom = getAttachmentMargin(context);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // region Helpers
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private static int getAttachmentMargin(final Context context){
        if(attachmentMarginInDips == null){
            attachmentMarginInDips = (int) ViewUtils.convertDipsToPixels(ATTACHMENT_MARGIN_SIZE, context.getApplicationContext());
        }
        return attachmentMarginInDips;
    }

    private static int getTextMarginSize(final Context context){
        if(messageMarginInDips == null){
            messageMarginInDips = (int) ViewUtils.convertDipsToPixels(TEXT_MARGIN_SIZE, context.getApplicationContext());
        }
        return messageMarginInDips;
    }

    private static boolean hasAttachments(final RecyclerView parent, final int index){
        if(index == 0){
            return false;
        }
        // get the viewType above our current index and see if it's an attachment. (Probably a better way to do this?)
        int type = parent.getAdapter().getItemViewType(index-1);
        return type == DetailedConversationAttachmentViewHolder.TYPE_LEFT_ATTACHMENT || type == DetailedConversationAttachmentViewHolder.TYPE_RIGHT_ATTACHMENT;
    }

    private static boolean isTextMessage(final int viewType){
        return viewType == DetailedConversationMessageViewHolder.TYPE_LEFT_TEXT || viewType == DetailedConversationMessageViewHolder.TYPE_RIGHT_TEXT;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    //endregion
    ////////////////////////////////////////////////////////////////////////////////////////////////
}

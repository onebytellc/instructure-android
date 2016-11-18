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
import android.util.Log;
import android.view.View;

import com.instructure.candroid.R;
import com.instructure.candroid.adapter.RubricRecyclerAdapter;
import com.instructure.candroid.fragment.RubricFragment;
import com.instructure.candroid.holders.ExpandableViewHolder;
import com.instructure.candroid.holders.RubricTopHeaderViewHolder;
import com.instructure.candroid.holders.RubricViewHolder;
import com.instructure.candroid.view.ViewUtils;
import com.instructure.pandarecycler.util.Types;

public class RubricDecorator extends RecyclerView.ItemDecoration{

    private Context mContext;

    public RubricDecorator(Context context) {
        mContext = context;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int index = parent.getChildAdapterPosition(view);

        RecyclerView.ViewHolder viewHolder = parent.getChildViewHolder(view);
        if(viewHolder instanceof ExpandableViewHolder || viewHolder instanceof RubricTopHeaderViewHolder){
            return;
        }

        if (index != 0) {
            outRect.top =  (int) mContext.getResources().getDimension(R.dimen.card_header_checkbox_margins);
        }

        int margin = (int)mContext.getResources().getDimension(R.dimen.card_header_checkbox_margins);
        outRect.left = margin;
        outRect.right = margin;
    }

    private static boolean isGroupViewType(final int viewType){
        return viewType == Types.TYPE_HEADER || viewType == Types.TYPE_TOP_HEADER;
    }
}
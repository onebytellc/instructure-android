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

package com.instructure.candroid.util;

import android.os.Handler;
import android.view.View;

import com.instructure.candroid.fragment.MessageListFragment;
import com.instructure.canvasapi.model.Conversation;

public abstract class DebounceMessageToAdapterListener implements MessageDebounceClickInterface, MessageListFragment.MessageAdapterToFragmentCallback<Conversation>{

    private boolean isEnabled = true;

    @Override
    public void onClick(Conversation conversation, int position, View sharedElement, boolean isOpenDetail) {
        if(isEnabled){
            isEnabled = false;
            onRowClicked(conversation, position, sharedElement, isOpenDetail);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isEnabled = true;
            }
        }, getDelayTime());
    }

    @Override
    public int getDelayTime() {
        return 2000;
    }
}

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

package com.instructure.candroid.interfaces;

import android.view.View;

import com.instructure.canvasapi.model.Conversation;

public interface DetailedConversationAdapterToFragmentCallback {
    void onRowClicked(Conversation conversation, int position, boolean isOpenDetail);
    void onRefreshFinished();
    void openMediaFromRow(String mime, String url, String filetype);
    void register(View view);
    /**
     * Used for when conversation is loaded from an email link outside the app
     * @param conversation
     */
    void conversationWasFetched(Conversation conversation);

}

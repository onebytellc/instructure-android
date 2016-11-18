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

package com.instructure.candroid.model;

import android.os.Parcel;

import com.instructure.canvasapi.model.CanvasComparable;
import com.instructure.canvasapi.model.Message;

import java.util.Date;

public class MessageWithDepth extends CanvasComparable<MessageWithDepth> {

    public int depth;
    public Message message;
    public boolean isAllParticipants;
    public String participantsString; // String data for message participants, used if message's participants != all participants

    @Override
    public Date getComparisonDate() {
        return message.getComparisonDate();
    }

    @Override
    public String getComparisonString() {
        return message.getComparisonString();
    }

    @Override
    public int compareTo(MessageWithDepth comparable) {
        return message.compareTo(comparable.message);
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}

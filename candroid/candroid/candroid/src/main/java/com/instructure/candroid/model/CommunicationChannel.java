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

import com.instructure.canvasapi.model.CanvasModel;

import java.util.Date;

public class CommunicationChannel  extends CanvasModel<CommunicationChannel> {
    private long id;
    private int position;
    private long user_id;
    private String address;
    //"email", "sms", "chat", "facebook" or "twitter"
    private String type;
    //"unconfirmed" or "active".
    private String workflow_state;
    public enum ChannelState {Unconfirmed, Active, Unknown}
    public enum ChannelType {Push, Email, SMS, Chat, Facebook, Twitter, Unknown}
    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public long getId() {
        return id;
    }
    @Override
    public Date getComparisonDate() {
        return null;
    }
    @Override
    public String getComparisonString() {
        return null;
    }
    public int getPosition() {
        return position;
    }
    public long getUserId() {
        return user_id;
    }
    public String getAddress() {
        return address;
    }
    public ChannelType getType() {
        //"email", "sms", "chat", "facebook" or "twitter"
        if(type == null){
            return null;
        }
        if("push".equals(type)){
            return ChannelType.Push;
        } else if("email".equals(type)){
            return ChannelType.Email;
        } else if ("sms".equals(type)){
            return ChannelType.SMS;
        } else if ("chat".equals(type)){
            return ChannelType.Chat;
        } else if ("facebook".equals(type)){
            return ChannelType.Facebook;
        } else if ("twitter".equals(type)){
            return ChannelType.Twitter;
        }
        return ChannelType.Unknown;
    }
    public ChannelState getWorkflowState() {
        if("active".equals(workflow_state)){
            return ChannelState.Active;
        } else if ("unconfirmed".equals(workflow_state)){
            return ChannelState.Unconfirmed;
        }
        return ChannelState.Unknown;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.position);
        dest.writeLong(this.user_id);
        dest.writeString(this.address);
        dest.writeString(this.type);
        dest.writeString(this.workflow_state);
    }

    private CommunicationChannel(Parcel in) {
        this.id = in.readLong();
        this.position = in.readInt();
        this.user_id = in.readLong();
        this.address = in.readString();
        this.type = in.readString();
        this.workflow_state = in.readString();
    }

    public static Creator<CommunicationChannel> CREATOR = new Creator<CommunicationChannel>() {
        public CommunicationChannel createFromParcel(Parcel source) {
            return new CommunicationChannel(source);
        }

        public CommunicationChannel[] newArray(int size) {
            return new CommunicationChannel[size];
        }
    };
}

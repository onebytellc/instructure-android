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
import android.os.Parcelable;

import com.instructure.canvasapi.model.CanvasModel;

import java.util.ArrayList;
import java.util.Date;

public class NotificationSubCategory extends CanvasModel<NotificationSubCategory> {

    private long id = -1;
    public String title;
    public String frequency;
    public int position;
    public ArrayList<String> notifications = new ArrayList<>();

    @Override
    public long getId() {
        if (id != -1) {
            return id;
        } else {
            return title != null ? title.hashCode() : -1;
        }
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Override
    public String getComparisonString() {
        return null;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.frequency);
        dest.writeInt(this.position);
        dest.writeSerializable(this.notifications);
    }

    public NotificationSubCategory() {
    }

    private NotificationSubCategory(Parcel in) {
        this.title = in.readString();
        this.frequency = in.readString();
        this.position = in.readInt();
        this.notifications = (ArrayList<String>) in.readSerializable();
    }

    public static final Creator<NotificationSubCategory> CREATOR = new Creator<NotificationSubCategory>() {
        public NotificationSubCategory createFromParcel(Parcel source) {
            return new NotificationSubCategory(source);
        }

        public NotificationSubCategory[] newArray(int size) {
            return new NotificationSubCategory[size];
        }
    };
}

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

import com.instructure.candroid.util.NotificationPreferenceUtils;
import com.instructure.canvasapi.model.CanvasModel;

import java.util.Date;
import java.util.HashMap;

public class NotificationCategoryHeader extends CanvasModel<NotificationCategoryHeader> {

    private long id = -1;
    public String title;
    public int position;
    public NotificationPreferenceUtils.CATEGORIES headerCategory = NotificationPreferenceUtils.CATEGORIES.NONE;
    public HashMap<String, NotificationSubCategory> subCategories = new HashMap<>();


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
        dest.writeInt(this.headerCategory == null ? -1 : this.headerCategory.ordinal());
        dest.writeInt(this.position);
        dest.writeSerializable(this.subCategories);
    }

    public NotificationCategoryHeader() {
    }

    private NotificationCategoryHeader(Parcel in) {
        this.title = in.readString();
        int tmpHeaderCategory = in.readInt();
        this.headerCategory = tmpHeaderCategory == -1 ? null : NotificationPreferenceUtils.CATEGORIES.values()[tmpHeaderCategory];
        this.position = in.readInt();
        this.subCategories = (HashMap<String, NotificationSubCategory>) in.readSerializable();
    }

    public static final Creator<NotificationCategoryHeader> CREATOR = new Creator<NotificationCategoryHeader>() {
        public NotificationCategoryHeader createFromParcel(Parcel source) {
            return new NotificationCategoryHeader(source);
        }

        public NotificationCategoryHeader[] newArray(int size) {
            return new NotificationCategoryHeader[size];
        }
    };
}

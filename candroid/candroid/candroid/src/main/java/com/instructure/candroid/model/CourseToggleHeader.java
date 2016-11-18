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

public class CourseToggleHeader extends CanvasModel<CourseToggleHeader> implements android.os.Parcelable {

    public String text;
    public boolean clickable = false;

    public CourseToggleHeader() {

    }

    public CourseToggleHeader(String headerText) {
        this.text = headerText;
    }

    @Override
    public long getId() {
        return text != null ? text.hashCode() : -1;
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
        dest.writeString(this.text);
        dest.writeByte(clickable ? (byte) 1 : (byte) 0);
    }

    private CourseToggleHeader(Parcel in) {
        this.text = in.readString();
        this.clickable = in.readByte() != 0;
    }

    public static final Creator<CourseToggleHeader> CREATOR = new Creator<CourseToggleHeader>() {
        public CourseToggleHeader createFromParcel(Parcel source) {
            return new CourseToggleHeader(source);
        }

        public CourseToggleHeader[] newArray(int size) {
            return new CourseToggleHeader[size];
        }
    };
}

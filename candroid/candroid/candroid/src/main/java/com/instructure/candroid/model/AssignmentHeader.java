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

import com.instructure.canvasapi.api.AssignmentAPI;
import com.instructure.canvasapi.model.CanvasModel;

import java.util.Date;

public class AssignmentHeader extends CanvasModel<AssignmentHeader> {

    private String headerText;
    private AssignmentAPI.ASSIGNMENT_BUCKET_TYPE bucketType;

    public AssignmentHeader(String headerText, AssignmentAPI.ASSIGNMENT_BUCKET_TYPE bucketType) {
        this.headerText = headerText;
        this.bucketType = bucketType;
    }

    @Override
    public long getId() {
        return headerText != null ? headerText.hashCode() : -1;
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

    public AssignmentAPI.ASSIGNMENT_BUCKET_TYPE getBucketType() {
        return bucketType;
    }

    public void setBucketType(AssignmentAPI.ASSIGNMENT_BUCKET_TYPE bucketType) {
        this.bucketType = bucketType;
    }

    public String getHeaderText() {
        return headerText;
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.headerText);
    }

    public AssignmentHeader() {
    }

    private AssignmentHeader(Parcel in) {
        this.headerText = in.readString();
        // FIXME no bucket type
    }

    public static final Creator<AssignmentHeader> CREATOR = new Creator<AssignmentHeader>() {
        public AssignmentHeader createFromParcel(Parcel source) {
            return new AssignmentHeader(source);
        }

        public AssignmentHeader[] newArray(int size) {
            return new AssignmentHeader[size];
        }
    };
}

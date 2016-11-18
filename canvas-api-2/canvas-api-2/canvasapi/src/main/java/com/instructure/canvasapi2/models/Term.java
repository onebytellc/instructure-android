/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.canvasapi2.models;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;
import com.instructure.canvasapi2.utils.DateHelper;

import java.util.Date;


public class Term extends CanvasModel<Term>{

    //currently only part of a course
	/*
	    term: {
		    id: 1,
		    name: 'Default Term',
		    start_at: "2012-06-01T00:00:00-06:00",
		    end_at: null
	    }
	 */

    // Variables from API
    private long id;
    private String name;
    @SerializedName("start_at")
    private String startAt;
    @SerializedName("end_at")
    private String endAt;
    // Helper variables
    private Date startDate;
    private Date endDate;
    private boolean isGroupTerm = false;

    public Term() {}

    public Term(String name) {
        this.name = name;
    }

    public Term(boolean isGroupTerm, String name) {
        id = Long.MAX_VALUE;
        this.startDate = null;
        this.name = name;

        this.isGroupTerm = isGroupTerm;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Date getComparisonDate() {
        return getStartAt();
    }

    @Override
    public String getComparisonString() {
        return getName();
    }

    @Override
    public int compareTo(Term term) {

        if (isGroupTerm && term.isGroupTerm) {
            return 0;
        } else if (isGroupTerm) {
            return 1;
        } else if (term.isGroupTerm) {
            return -1;
        }

        return ((CanvasComparable)this).compareTo(term);
    }

    //region Getters

    public String getName() {
        return name;
    }

    public Date getEndAt() {
        if (endDate == null) {
            endDate = DateHelper.stringToDate(endAt);
        }
        return endDate;
    }

    public Date getStartAt() {
        if (startDate == null) {
            startDate = DateHelper.stringToDate(startAt);
        }
        return startDate;
    }

    //endregion

    //region Setters

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setGroupTerm(boolean groupTerm) {
        isGroupTerm = groupTerm;
    }

    //endregion

    //region Parcelable

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.name);
        dest.writeString(this.startAt);
        dest.writeString(this.endAt);
        dest.writeLong(startDate != null ? startDate.getTime() : -1);
        dest.writeLong(endDate != null ? endDate.getTime() : -1);
        dest.writeByte(isGroupTerm ? (byte) 1 : (byte) 0);
    }

    private Term(Parcel in) {
        this.id = in.readLong();
        this.name = in.readString();
        this.startAt = in.readString();
        this.endAt = in.readString();
        long tmpStartDate = in.readLong();
        this.startDate = tmpStartDate == -1 ? null : new Date(tmpStartDate);
        long tmpEndDate = in.readLong();
        this.endDate = tmpEndDate == -1 ? null : new Date(tmpEndDate);
        this.isGroupTerm = in.readByte() != 0;
    }

    public static Creator<Term> CREATOR = new Creator<Term>() {
        public Term createFromParcel(Parcel source) {
            return new Term(source);
        }

        public Term[] newArray(int size) {
            return new Term[size];
        }
    };

    //endregion
}

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
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.Date;


public class QuizSubmissionMatch extends CanvasModel<QuizSubmissionMatch> {

    private String text;
    @SerializedName("match_id")
    private int matchId;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    @Override
    public long getId() {
        return 0;
    }

    @Nullable
    @Override
    public Date getComparisonDate() {
        return null;
    }

    @Nullable
    @Override
    public String getComparisonString() {
        return null;
    }

    @Override
    public int compareTo(QuizSubmissionMatch comparable) {
        return super.compareTo(comparable);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.text);
        dest.writeInt(this.matchId);
    }

    public QuizSubmissionMatch() {
    }

    protected QuizSubmissionMatch(Parcel in) {
        this.text = in.readString();
        this.matchId = in.readInt();
    }

    public static final Creator<QuizSubmissionMatch> CREATOR = new Creator<QuizSubmissionMatch>() {
        @Override
        public QuizSubmissionMatch createFromParcel(Parcel source) {
            return new QuizSubmissionMatch(source);
        }

        @Override
        public QuizSubmissionMatch[] newArray(int size) {
            return new QuizSubmissionMatch[size];
        }
    };
}

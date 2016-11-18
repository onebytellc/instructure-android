/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.wearutils.models;

import android.os.Parcel;
import android.os.Parcelable;


public class DataPage implements Parcelable {

    public static final int WIN_LOSS = 1, TABLE = 2;

    public int type = WIN_LOSS;
    public String mTitle;
    public String mText;
    public int mBackgroundId;

    public DataPage(String title, String text, int backgroundId, int type) {
        this.mTitle = title;
        this.mText = text;
        this.mBackgroundId = backgroundId;
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeString(this.mTitle);
        dest.writeString(this.mText);
        dest.writeInt(this.mBackgroundId);
    }

    protected DataPage(Parcel in) {
        this.type = in.readInt();
        this.mTitle = in.readString();
        this.mText = in.readString();
        this.mBackgroundId = in.readInt();
    }

    public static final Parcelable.Creator<DataPage> CREATOR = new Parcelable.Creator<DataPage>() {
        @Override
        public DataPage createFromParcel(Parcel source) {
            return new DataPage(source);
        }

        @Override
        public DataPage[] newArray(int size) {
            return new DataPage[size];
        }
    };
}

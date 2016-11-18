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

package com.instructure.androidfoosball.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;


public class TableTeam implements Parcelable {

    private float averageWinRate;
    private String customName;
    private ArrayList<User> users;

    public float getAverageWinRate() {
        return averageWinRate;
    }

    public void setAverageWinRate(float averageWinRate) {
        this.averageWinRate = averageWinRate;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.averageWinRate);
        dest.writeString(this.customName);
        dest.writeTypedList(this.users);
    }

    public TableTeam() {
    }

    protected TableTeam(Parcel in) {
        this.averageWinRate = in.readFloat();
        this.customName = in.readString();
        this.users = in.createTypedArrayList(User.CREATOR);
    }

    public static final Creator<TableTeam> CREATOR = new Creator<TableTeam>() {
        @Override
        public TableTeam createFromParcel(Parcel source) {
            return new TableTeam(source);
        }

        @Override
        public TableTeam[] newArray(int size) {
            return new TableTeam[size];
        }
    };
}

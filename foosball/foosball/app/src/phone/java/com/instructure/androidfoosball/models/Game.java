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


public class Game implements Parcelable {

    private ArrayList<String> teams;
    private String table;

    public Game() { }

    public Game(ArrayList<String> teamList, String table) {
        this.teams = teamList;
        this.table = table;
    }

    public ArrayList<String> getTeamList() {
        return teams;
    }

    public void setTeamList(ArrayList<String> teamList) {
        this.teams = teamList;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(this.teams);
        dest.writeString(this.table);
    }

    protected Game(Parcel in) {
        this.teams = in.createStringArrayList();
        this.table = in.readString();
    }

    public static final Creator<Game> CREATOR = new Creator<Game>() {
        @Override
        public Game createFromParcel(Parcel source) {
            return new Game(source);
        }

        @Override
        public Game[] newArray(int size) {
            return new Game[size];
        }
    };
}

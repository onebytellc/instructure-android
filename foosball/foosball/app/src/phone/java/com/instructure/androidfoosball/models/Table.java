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


public class Table implements Parcelable {

    private String id;
    private String currentBestOf;
    private String currentGame;
    private String currentPointsToWin;
    private String currentRound;
    private String currentScoreTeamOne;
    private String currentScoreTeamTwo;
    private String name;
    private String sideOneColor;
    private String sideOneName;
    private String sideTwoColor;
    private String sideTwoName;
    private String pushId;
    private TableTeam teamOne;
    private TableTeam teamTwo;

    public Table() { }

    public Table(String name, String sideOneColor, String sideTwoColor) {
        this.name = name;
        this.sideOneColor = sideOneColor;
        this.sideTwoColor = sideTwoColor;
    }

    //Used by FireBase
    public Table(
            String currentBestOf,
            String currentGame,
            String currentPointsToWin,
            String currentRound,
            String currentScoreTeamOne,
            String currentScoreTeamTwo,
            String name,
            String pushId,
            String sideOneColor,
            String sideTwoColor,
            String sideOneName,
            String sideTwoName,
            TableTeam teamOne,
            TableTeam teamTwo) {

        this.currentBestOf = currentBestOf;
        this.currentGame = currentGame;
        this.currentPointsToWin = currentPointsToWin;
        this.currentRound = currentRound;
        this.currentScoreTeamOne = currentScoreTeamOne;
        this.currentScoreTeamTwo = currentScoreTeamTwo;
        this.name = name;
        this.pushId = pushId;
        this.sideOneColor = sideOneColor;
        this.sideOneName = sideOneName;
        this.sideTwoColor = sideTwoColor;
        this.sideTwoName = sideTwoName;
        this.teamOne = teamOne;
        this.teamTwo = teamTwo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSideOneColor() {
        return sideOneColor;
    }

    public void setSideOneColor(String sideOneColor) {
        this.sideOneColor = sideOneColor;
    }

    public String getSideTwoColor() {
        return sideTwoColor;
    }

    public void setSideTwoColor(String sideTwoColor) {
        this.sideTwoColor = sideTwoColor;
    }

    public String getSideOneName() {
        return sideOneName;
    }

    public void setSideOneName(String sideOneName) {
        this.sideOneName = sideOneName;
    }

    public String getSideTwoName() {
        return sideTwoName;
    }

    public void setSideTwoName(String sideTwoName) {
        this.sideTwoName = sideTwoName;
    }

    public String getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(String currentGame) {
        this.currentGame = currentGame;
    }

    public String getCurrentBestOf() {
        return currentBestOf;
    }

    public String getCurrentPointsToWin() {
        return currentPointsToWin;
    }

    public String getCurrentRound() {
        return currentRound;
    }

    public String getCurrentScoreTeamOne() {
        return currentScoreTeamOne;
    }

    public String getCurrentScoreTeamTwo() {
        return currentScoreTeamTwo;
    }

    public void setCurrentBestOf(String currentBestOf) {
        this.currentBestOf = currentBestOf;
    }

    public void setCurrentPointsToWin(String currentPointsToWin) {
        this.currentPointsToWin = currentPointsToWin;
    }

    public void setCurrentRound(String currentRound) {
        this.currentRound = currentRound;
    }

    public void setCurrentScoreTeamOne(String currentScoreTeamOne) {
        this.currentScoreTeamOne = currentScoreTeamOne;
    }

    public void setCurrentScoreTeamTwo(String currentScoreTeamTwo) {
        this.currentScoreTeamTwo = currentScoreTeamTwo;
    }

    public String getPushId() {
        return pushId;
    }

    public void setPushId(String pushId) {
        this.pushId = pushId;
    }

    public TableTeam getTeamOne() {
        return teamOne;
    }

    public void setTeamOne(TableTeam teamOne) {
        this.teamOne = teamOne;
    }

    public TableTeam getTeamTwo() {
        return teamTwo;
    }

    public void setTeamTwo(TableTeam teamTwo) {
        this.teamTwo = teamTwo;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.currentBestOf);
        dest.writeString(this.currentGame);
        dest.writeString(this.currentPointsToWin);
        dest.writeString(this.currentRound);
        dest.writeString(this.currentScoreTeamOne);
        dest.writeString(this.currentScoreTeamTwo);
        dest.writeString(this.name);
        dest.writeString(this.sideOneColor);
        dest.writeString(this.sideOneName);
        dest.writeString(this.sideTwoColor);
        dest.writeString(this.sideTwoName);
        dest.writeString(this.pushId);
        dest.writeParcelable(this.teamOne, flags);
        dest.writeParcelable(this.teamTwo, flags);
    }

    protected Table(Parcel in) {
        this.id = in.readString();
        this.currentBestOf = in.readString();
        this.currentGame = in.readString();
        this.currentPointsToWin = in.readString();
        this.currentRound = in.readString();
        this.currentScoreTeamOne = in.readString();
        this.currentScoreTeamTwo = in.readString();
        this.name = in.readString();
        this.sideOneColor = in.readString();
        this.sideOneName = in.readString();
        this.sideTwoColor = in.readString();
        this.sideTwoName = in.readString();
        this.pushId = in.readString();
        this.teamOne = in.readParcelable(TableTeam.class.getClassLoader());
        this.teamTwo = in.readParcelable(TableTeam.class.getClassLoader());
    }

    public static final Creator<Table> CREATOR = new Creator<Table>() {
        @Override
        public Table createFromParcel(Parcel source) {
            return new Table(source);
        }

        @Override
        public Table[] newArray(int size) {
            return new Table[size];
        }
    };
}

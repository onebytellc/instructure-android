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

package com.instructure.androidfoosball.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.instructure.androidfoosball.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class TableViewHolder extends RecyclerView.ViewHolder {

    public @BindView(R.id.tableLabel) TextView tableLabel;
    public @BindView(R.id.tableStatus) TextView tableStatus;
    public @BindView(R.id.tableStatusResult) TextView tableStatusResult;
    public @BindView(R.id.cardGameState) View cardGameState;
    public @BindView(R.id.bestOfCount) TextView bestOfCount;
    public @BindView(R.id.roundCount) TextView roundCount;
    public @BindView(R.id.pointsCount) TextView pointsCount;
    public @BindView(R.id.teamOneScore) TextView teamOneScore;
    public @BindView(R.id.teamTwoScore) TextView teamTwoScore;
    public @BindView(R.id.buttonNotifyWhenDone) Button buttonNotifyWhenDone;
    public @BindView(R.id.playerOne) CircleImageView playerOne;
    public @BindView(R.id.playerTwo) CircleImageView playerTwo;
    public @BindView(R.id.playerThree) CircleImageView playerThree;
    public @BindView(R.id.playerFour) CircleImageView playerFour;

    public TableViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}

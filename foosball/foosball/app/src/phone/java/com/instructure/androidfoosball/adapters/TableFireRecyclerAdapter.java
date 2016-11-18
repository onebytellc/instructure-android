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

package com.instructure.androidfoosball.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.instructure.androidfoosball.R;
import com.instructure.androidfoosball.holders.TableViewHolder;
import com.instructure.androidfoosball.models.Table;
import com.instructure.androidfoosball.models.User;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

import de.hdodenhof.circleimageview.CircleImageView;


public class TableFireRecyclerAdapter extends FirebaseRecyclerAdapter<Table, TableViewHolder> {

    private WeakReference<Context> mContext;

    public TableFireRecyclerAdapter(Context context, Query ref) {
        super(Table.class, R.layout.adapter_table, TableViewHolder.class, ref);
        mContext = new WeakReference<>(context);
    }

    public TableFireRecyclerAdapter(Context context, DatabaseReference ref) {
        super(Table.class, R.layout.adapter_table, TableViewHolder.class, ref);
        mContext = new WeakReference<>(context);
    }

    @Override
    protected void populateViewHolder(final TableViewHolder holder, Table item, int position) {
        holder.tableLabel.setText(item.getName());
        String currentGame = item.getCurrentGame();

        Context context = mContext.get();
        if(context != null) {
            if("FREE".equals(currentGame)) {
                //Table not busy
                holder.tableStatusResult.setText(context.getString(R.string.status_free));
                holder.cardGameState.setVisibility(View.GONE);
            } else if ("BUSY".equals(currentGame)) {
                //Table busy
                holder.tableStatusResult.setText(context.getString(R.string.status_busy));
                holder.cardGameState.setVisibility(View.VISIBLE);
                holder.bestOfCount.setText(item.getCurrentBestOf());
                holder.roundCount.setText(item.getCurrentRound());
                holder.pointsCount.setText(item.getCurrentPointsToWin());
                holder.teamOneScore.setText(item.getCurrentScoreTeamOne());
                holder.teamTwoScore.setText(item.getCurrentScoreTeamTwo());

                //FIXME: needs to show empty person view - bad decision Matt gezzz
                if(item.getTeamOne() != null && item.getTeamTwo() != null) {
                    if(item.getTeamOne().getUsers().size() == 1) {
                        holder.playerOne.setVisibility(View.INVISIBLE);
                        setAvatar(item.getTeamOne().getUsers().get(0), holder.playerTwo);
                    } else if(item.getTeamOne().getUsers().size() == 2) {
                        setAvatar(item.getTeamOne().getUsers().get(0), holder.playerOne);
                        setAvatar(item.getTeamOne().getUsers().get(1), holder.playerTwo);
                    } else {
                        holder.playerOne.setVisibility(View.INVISIBLE);
                        holder.playerTwo.setVisibility(View.INVISIBLE);
                    }

                    if(item.getTeamTwo().getUsers().size() == 1) {
                        holder.playerFour.setVisibility(View.INVISIBLE);
                        setAvatar(item.getTeamTwo().getUsers().get(0), holder.playerThree);
                    } else if(item.getTeamTwo().getUsers().size() == 2) {
                        setAvatar(item.getTeamTwo().getUsers().get(0), holder.playerThree);
                        setAvatar(item.getTeamTwo().getUsers().get(1), holder.playerFour);
                    } else {
                        holder.playerThree.setVisibility(View.INVISIBLE);
                        holder.playerFour.setVisibility(View.INVISIBLE);
                    }
                } else {
                    holder.playerOne.setVisibility(View.INVISIBLE);
                    holder.playerTwo.setVisibility(View.INVISIBLE);
                    holder.playerThree.setVisibility(View.INVISIBLE);
                    holder.playerFour.setVisibility(View.INVISIBLE);
                }

                holder.buttonNotifyWhenDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Table table = getItem(holder.getAdapterPosition());
                        if(!TextUtils.isEmpty(table.getPushId())) {
                            FirebaseMessaging.getInstance().subscribeToTopic(table.getPushId());
                        } else {
                            Log.e("push", "Table PushId was null cannot subscribe to topic");
                        }
                    }
                });

            } else {
                holder.tableStatusResult.setText(context.getString(R.string.status_unknown));
                holder.cardGameState.setVisibility(View.GONE);
            }
        }
    }

    private void setAvatar(User user, CircleImageView imageView) {
        if(user == null) {
            imageView.setVisibility(View.INVISIBLE);
        }
        if(TextUtils.isEmpty(user.getAvatar())) {
            imageView.setVisibility(View.INVISIBLE);
        }
        else {
            imageView.setVisibility(View.VISIBLE);
            Picasso.with(mContext.get()).load(user.getAvatar()).error(R.drawable.sadpanda).into(imageView);
        }
    }
}

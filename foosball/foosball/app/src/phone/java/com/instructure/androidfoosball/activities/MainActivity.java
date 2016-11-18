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

package com.instructure.androidfoosball.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.instructure.androidfoosball.R;
import com.instructure.androidfoosball.models.User;
import com.instructure.androidfoosball.utils.Const;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    //region Binding

    @BindView(R.id.avatar) CircleImageView mAvatar;
    @BindView(R.id.name) TextView mName;
    @BindView(R.id.winCount) TextView mWinCount;
    @BindView(R.id.lossCount) TextView mLossCount;

    //endregion

    private User mUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        handleIntent(getIntent().getExtras());

        if(mUser != null) {
            if(!TextUtils.isEmpty(mUser.getAvatar())) {
                Picasso.with(this).load(mUser.getAvatar()).placeholder(R.drawable.sadpanda).error(R.drawable.sadpanda).into(mAvatar);
            } else {
                Picasso.with(this).load(R.drawable.sadpanda).into(mAvatar);
            }
            mName.setText(mUser.getName());
            setWinsAndLosses(Integer.toString(mUser.getWins()), Integer.toString(mUser.getLosses()));

        }

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child("users").child(mUser.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                if(user != null) {
                    setWinsAndLosses(Integer.toString(user.getWins()), Integer.toString(user.getLosses()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setWinsAndLosses(String text, String text2) {
        mWinCount.setText(text);
        mLossCount.setText(text2);
    }

    private void handleIntent(Bundle bundle) {
        mUser = bundle.getParcelable(Const.USER);
    }

    public static Intent createIntent(Context context, User user) {
        Intent intent = new Intent(context, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.USER, user);
        intent.putExtras(bundle);
        return intent;
    }
}

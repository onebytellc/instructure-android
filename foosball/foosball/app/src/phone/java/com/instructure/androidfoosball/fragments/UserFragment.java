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

package com.instructure.androidfoosball.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.instructure.androidfoosball.R;
import com.instructure.androidfoosball.activities.ChangeAvatarActivity;
import com.instructure.androidfoosball.activities.Mode;
import com.instructure.androidfoosball.interfaces.FragmentCallbacks;
import com.instructure.androidfoosball.interfaces.TextEditCallback;
import com.instructure.androidfoosball.models.User;
import com.instructure.androidfoosball.utils.AnimUtils;
import com.instructure.androidfoosball.utils.FireUtils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;


public class UserFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static UserFragment newInstance() {
        return new UserFragment();
    }

    private static final int REQUEST_CODE_TAKE_PICTURE = 1337;

    private FragmentCallbacks mCallbacks;
    private TextEditCallback mTextEditCallbacks;

    //region Binding

    @BindView(R.id.swipeRefreshLayout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.avatar) CircleImageView mAvatar;
    @BindView(R.id.win) TextView mWinCount;
    @BindView(R.id.loss) TextView mLossCount;
    @BindView(R.id.startupPhrase) TextView mStartupPhraseText;
    @BindView(R.id.victoryPhrase) TextView mVictoryPhraseText;
    @BindView(R.id.startupPhraseCard) CardView mStartupPhraseCard;
    @BindView(R.id.victoryPhraseCard) CardView mVictoryPhraseCard;
    @BindView(R.id.userEmail) TextView mUserEmail;
    @BindView(R.id.startupEdit) ImageView mStartupEdit;
    @BindView(R.id.victoryEdit) ImageView mVictoryEdit;
    @BindView(R.id.winProgress) ProgressBar mWinProgress;
    @BindView(R.id.lossProgress) ProgressBar mLossProgress;

    //endregion

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = ((FragmentCallbacks)context);
        mTextEditCallbacks = ((TextEditCallback)context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user, container, false);
        ButterKnife.bind(this, rootView);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    private void loadData() {
        mWinProgress.setVisibility(View.VISIBLE);
        mLossProgress.setVisibility(View.VISIBLE);

        User user = mCallbacks.getUser();
        if(user != null) {
            mUserEmail.setText(user.getEmail());
            setupAvatar(user);
            setupWinLossCount(user);
            setupPhrase(user);
        }
    }

    private void setupAvatar(@NonNull User user) {
        DatabaseReference ref = mCallbacks.getDatabase().child("users").child(user.getId()).child("avatar");
        ref.keepSynced(false);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String url = (String)dataSnapshot.getValue();
                Picasso.with(getContext()).load(url).error(R.drawable.sadpanda).into(mAvatar);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Picasso.with(getContext()).load(R.drawable.sadpanda).into(mAvatar);
            }
        });
        if(!TextUtils.isEmpty(user.getAvatar())) {
            Picasso.with(getContext()).load(user.getAvatar()).placeholder(R.drawable.sadpanda).error(R.drawable.sadpanda).into(mAvatar);
        } else {
            Picasso.with(getContext()).load(R.drawable.sadpanda).into(mAvatar);
        }

        mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallbacks.getUser() != null) {
                    startActivityForResult(new Intent(ChangeAvatarActivity.Companion
                            .createIntent(getActivity(), mCallbacks.getUser().getId(), Mode.CAMERA)), REQUEST_CODE_TAKE_PICTURE);
                }
            }
        });
    }

    private void setAvatar(String url) {
        if(!TextUtils.isEmpty(url)) {
            Picasso.with(getContext()).load(url).placeholder(R.drawable.sadpanda).error(R.drawable.sadpanda).into(mAvatar);
        } else {
            Picasso.with(getContext()).load(R.drawable.sadpanda).into(mAvatar);
        }
    }

    private void setupWinLossCount(@NonNull User user) {

        FireUtils.getWinCount(user.getId(), mCallbacks.getDatabase(), new FireUtils.OnIntValue() {
            @Override
            public void onValueFound(int value) {
                mWinCount.setText(String.valueOf(value));
                mWinProgress.setVisibility(View.INVISIBLE);
            }
        });

        FireUtils.getLossCount(mCallbacks.getUser().getId(), mCallbacks.getDatabase(), new FireUtils.OnIntValue() {
            @Override
            public void onValueFound(int value) {
                mLossCount.setText(String.valueOf(value));
                mLossProgress.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void setupPhrase(@NonNull User user) {
        FireUtils.getVictoryPhrase(user.getId(), mCallbacks.getDatabase(), new FireUtils.OnStringValue() {
            @Override
            public void onValueFound(String value) {
                mVictoryPhraseText.setText(value);
                mSwipeRefreshLayout.setRefreshing(false);
                AnimUtils.fadeIn(320, mVictoryPhraseCard);
            }
        });

        FireUtils.getStartupPhrase(user.getId(), mCallbacks.getDatabase(), new FireUtils.OnStringValue() {
            @Override
            public void onValueFound(String value) {
                mStartupPhraseText.setText(value);
                AnimUtils.fadeIn(320, mStartupPhraseCard);
            }
        });

        mVictoryEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextEditCallbacks.requestTextEdit(mVictoryPhraseText.getId(), mVictoryPhraseText.getText().toString());
            }
        });

        mStartupEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextEditCallbacks.requestTextEdit(mStartupPhraseText.getId(), mStartupPhraseText.getText().toString());
            }
        });
    }

    public void updateStartupPhraseText(String text) {
        mStartupPhraseText.setText(text);
    }

    public void updateVictoryPhraseText(String text) {
        mVictoryPhraseText.setText(text);
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        loadData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE_TAKE_PICTURE && resultCode == Activity.RESULT_OK && data != null) {
            String avatarUrl = data.getStringExtra(ChangeAvatarActivity.Companion.getEXTRA_AVATAR_URL());
            if(!TextUtils.isEmpty(avatarUrl)) {
                Picasso.with(getContext()).load(avatarUrl).placeholder(R.drawable.sadpanda).error(R.drawable.sadpanda).into(mAvatar);
            }
        }
    }
}

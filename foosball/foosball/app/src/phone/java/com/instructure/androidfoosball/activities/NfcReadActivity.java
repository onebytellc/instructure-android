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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.instructure.androidfoosball.R;
import com.instructure.androidfoosball.models.Table;
import com.instructure.androidfoosball.utils.Prefs;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class NfcReadActivity extends AppCompatActivity {

    private static final String TAG = "FoosNFC";

    @BindView(R.id.loadingView) CardView mLoadingView;
    @BindView(R.id.successView) CardView mSuccessView;
    @BindView(R.id.errorView) CardView mErrorView;

    @BindView(R.id.tableLabel) TextView mTableLabel;
    @BindView(R.id.teamLabel) TextView mTeamLabel;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseUser mUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(200);

        setContentView(R.layout.activity_nfc_read);
        ButterKnife.bind(this);

        /* Check if logged in */
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    // User is signed in
                    mUser = firebaseUser;
                    parseNfc();
                } else {
                    // User is signed out
                    startActivity(new Intent(NfcReadActivity.this, SignInActivity.class));
                    finish();
                }
            }
        };

    }

    private void parseNfc() {
        try {
            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefRecord foosRecord = ((NdefMessage) rawMsgs[0]).getRecords()[0];

            Uri uri = foosRecord.toUri();
            List<String> segments = uri.getPathSegments();

            Log.i(TAG, "Read NFC Tag for table " + segments.get(0) + ", side " + segments.get(1));

            assignTeam(segments.get(0), Integer.parseInt(segments.get(1)));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void assignTeam(final String tableId, final int side) {

        final DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.child("tables").child(tableId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Table table = dataSnapshot.getValue(Table.class);

                String colorString;
                int teamColor;

                colorString = side == 0 ? table.getSideOneColor() : table.getSideTwoColor();
                teamColor = Color.parseColor(colorString);
                String teamName = side == 0 ? table.getSideOneName() : table.getSideTwoName();
                String userId = new Prefs(NfcReadActivity.this).load("userId", "");
                Log.v(TAG, "Assigning Team " + side);
                db.child("incoming").child(tableId).child(side == 0 ? "sideOne" : "sideTwo").setValue(userId);

                success(table.getName(), teamName, teamColor);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                fail(databaseError.getMessage());
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void success(String tableName, String teamName, int teamColor) {
        mSuccessView.setCardBackgroundColor(teamColor);
        mTableLabel.setText(tableName);
        mTeamLabel.setText(teamName);
        mLoadingView.setVisibility(View.GONE);
        mSuccessView.setVisibility(View.VISIBLE);
        mErrorView.setVisibility(View.GONE);
        finishAfterDelay();
    }


    private void fail(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        mLoadingView.setVisibility(View.GONE);
        mSuccessView.setVisibility(View.GONE);
        mErrorView.setVisibility(View.VISIBLE);
        finishAfterDelay();
    }

    private void finishAfterDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 3000);
    }

    @Override
    public void onBackPressed() {
        // Do nothing. TRAP THE USER!!!!
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

}

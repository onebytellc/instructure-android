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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.instructure.androidfoosball.R;
import com.instructure.androidfoosball.models.User;
import com.instructure.androidfoosball.utils.Const;
import com.instructure.androidfoosball.utils.Prefs;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SignInActivity extends BaseFireBaseActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "abcde";

    @BindView(R.id.sign_in_button) SignInButton mSignInButton;

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);
        setupListeners();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        initFireBase();
    }

    @Override
    protected void onAuthStateChange(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            // User is signed in
            Log.d(TAG, "onAuthStateChanged:signed_in:" + firebaseUser.getUid());
            finish();
            overridePendingTransition(0,0);
            String userId = new Prefs(SignInActivity.this).load("userId", "");
            String photo = "";
            if(firebaseUser.getPhotoUrl() != null) {
                photo = firebaseUser.getPhotoUrl().toString();
            }

            Prefs.get(SignInActivity.this).save(Const.USER_ID, userId);

            User user = new User(
                    userId,
                    firebaseUser.getDisplayName(),
                    firebaseUser.getEmail(),
                    photo);

            startActivity(PrimaryActivity.createIntent(SignInActivity.this, user));
        } else {
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct, final User user) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            postUser(acct, user);
                        }
                        hideProgressDialog();
                    }
                });
    }

    private void postUser(final GoogleSignInAccount acct, final User user) {
        mDatabase.child("users").orderByChild("email").equalTo(user.getEmail()).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    mDatabase.child("users").child(acct.getId()).setValue(user);
                }
                new Prefs(SignInActivity.this).save("userId", acct.getId());
                finish();
                overridePendingTransition(0,0);
                startActivity(PrimaryActivity.createIntent(SignInActivity.this, user));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }

        });
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection failed: " + connectionResult.getErrorMessage(), Toast.LENGTH_LONG).show();
    }


    private void setupListeners() {
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(GoogleSignInResult result, User user) {
        Log.d("abcde", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Toast.makeText(SignInActivity.this, "Signed in " + acct.getDisplayName() + " " + acct.getPhotoUrl(), Toast.LENGTH_SHORT).show();
            firebaseAuthWithGoogle(acct, user);

            //updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.
            //updateUI(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //add the user to the database
            User user = new User();
            if(result.getSignInAccount() != null) {
                String photo = "";
                if(result.getSignInAccount().getPhotoUrl() != null) {
                    photo = result.getSignInAccount().getPhotoUrl().toString();
                }
                user = new User(
                        result.getSignInAccount().getId(),
                        result.getSignInAccount().getDisplayName(),
                        result.getSignInAccount().getEmail(),
                        photo);
            }
            handleSignInResult(result, user);
        }
    }
}

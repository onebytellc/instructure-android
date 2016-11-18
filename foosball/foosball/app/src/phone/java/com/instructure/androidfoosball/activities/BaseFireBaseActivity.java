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

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.instructure.androidfoosball.interfaces.FragmentCallbacks;
import com.instructure.androidfoosball.models.User;

public abstract class BaseFireBaseActivity extends AppCompatActivity implements FragmentCallbacks {

    private User mUser;
    protected FirebaseAuth mAuth;
    protected DatabaseReference mDatabase;

    protected abstract void onAuthStateChange(@NonNull FirebaseAuth firebaseAuth);

    protected void initFireBase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private FirebaseAuth.AuthStateListener mAuthStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            onAuthStateChange(firebaseAuth);
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthStateListener != null && mAuth != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public User getUser() {
        return mUser;
    }

    @Override
    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public void setUser(User user) {
        this.mUser = user;
    }

    @Override
    public DatabaseReference getDatabase() {
        return mDatabase;
    }
}

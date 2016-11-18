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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.instructure.androidfoosball.R;
import com.instructure.androidfoosball.fragments.TableFragment;
import com.instructure.androidfoosball.fragments.UserFragment;
import com.instructure.androidfoosball.interfaces.TextEditCallback;
import com.instructure.androidfoosball.models.User;
import com.instructure.androidfoosball.utils.AnimUtils;
import com.instructure.androidfoosball.utils.Const;
import com.instructure.androidfoosball.utils.FireUtils;
import com.instructure.androidfoosball.utils.Prefs;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PrimaryActivity extends BaseFireBaseActivity implements TextEditCallback {

    //region Binding

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.pager) ViewPager mPager;
    @BindView(R.id.tabs) TabLayout mTabs;
    @BindView(R.id.phraseEditWrapper) View mPhraseEditWrapper;
    @BindView(R.id.phraseDone) ImageView mPhraseDone;
    @BindView(R.id.phraseEditText) EditText mPhraseEditText;

    //endregion

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_primary);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        initFireBase();
        setupUser();

        mPager.setOffscreenPageLimit(2);
        mPager.setAdapter(new TabsPagerAdapter(getSupportFragmentManager()));
        mTabs.setupWithViewPager(mPager);

        mPhraseDone.setOnClickListener(mPhraseDoneClickListener);
    }

    private void setupUser() {
        User user = getIntent().getExtras().getParcelable(Const.USER);
        if(user == null) {
            startActivity(new Intent(PrimaryActivity.this, SignInActivity.class));
            finish();
        } else {
            setUser(user);
        }
    }

    @Override
    protected void onAuthStateChange(@NonNull FirebaseAuth firebaseAuth) {
        if(firebaseAuth.getCurrentUser() == null) {
            Prefs.get(PrimaryActivity.this).load(Const.USER_ID, "");
            startActivity(new Intent(PrimaryActivity.this, SignInActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logout, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.logout) {
            mAuth.signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class TabsPagerAdapter extends FragmentPagerAdapter {

        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return UserFragment.newInstance();
                default:
                    return TableFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getUser().getName().toUpperCase(Locale.getDefault());
                default:
                    return getString(R.string.tables).toUpperCase(Locale.getDefault());
            }
        }
    }

    public static Intent createIntent(Context context, User user) {
        Intent intent = new Intent(context, PrimaryActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.USER, user);
        intent.putExtras(bundle);
        return intent;
    }

    //region TextEditCallbacks

    @Override
    public void requestTextEdit(int resId_requester, String text) {
        mPhraseDone.setTag(resId_requester);
        mPhraseEditText.setText(text);
        mPhraseEditText.setSelection(mPhraseEditText.getText().length());
        if(mPhraseEditWrapper.getVisibility() != View.VISIBLE) {
            showKeyboard();
        }
        AnimUtils.fadeIn(360, mPhraseEditWrapper);
    }

    private View.OnClickListener mPhraseDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int resId_requester = (int)v.getTag();
            final Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + 0);
            switch (resId_requester) {
                case R.id.startupPhrase: {
                    FireUtils.setStartupPhrase(getUser().getId(), mDatabase, mPhraseEditText.getText().toString());
                    if (fragment instanceof UserFragment) {
                        ((UserFragment) fragment).updateStartupPhraseText(mPhraseEditText.getText().toString());
                    }
                    break;
                }
                case R.id.victoryPhrase: {
                    FireUtils.setVictoryPhrase(getUser().getId(), mDatabase, mPhraseEditText.getText().toString());
                    if (fragment instanceof UserFragment) {
                        ((UserFragment) fragment).updateVictoryPhraseText(mPhraseEditText.getText().toString());
                    }
                    break;
                }
            }
            hideKeyboard();
            AnimUtils.fadeOut(360, mPhraseEditWrapper);
        }
    };

    //endregion

    @Override
    public void onBackPressed() {
        if(mPhraseEditWrapper.getVisibility() == View.VISIBLE) {
            AnimUtils.fadeOut(360, mPhraseEditWrapper);
            return;
        }
        super.onBackPressed();
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}

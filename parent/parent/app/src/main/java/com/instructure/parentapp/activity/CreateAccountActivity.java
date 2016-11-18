/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.parentapp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.Parent;
import com.instructure.canvasapi2.models.ParentResponse;
import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.Prefs;
import com.instructure.parentapp.R;
import com.instructure.parentapp.util.ViewUtils;

import java.util.List;

import retrofit2.Call;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class CreateAccountActivity extends BaseParentActivity {

    private EditText mPassword;
    private EditText mPasswordConfirm;
    private EditText mEmail;
    private EditText mFirstName;
    private EditText mLastName;
    private Button mNext;

    public static Intent createIntent(Context context) {
        return new Intent(context, CreateAccountActivity.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.parentStatusBarColor));
        setContentView(R.layout.activity_create_account);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupViews();
        setupListeners();
    }

    private void setupViews() {
        mPassword = (EditText) findViewById(R.id.createAccountPassword);
        mPasswordConfirm = (EditText) findViewById(R.id.createAccountPasswordConfirm);
        //Setting edit text to password here prevents the font from being changed
        mPassword.setTransformationMethod(new PasswordTransformationMethod());
        mPasswordConfirm.setTransformationMethod(new PasswordTransformationMethod());
        //we don't want the keyboard to display suggestions for passwords
        mPassword.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mPasswordConfirm.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mEmail = (EditText) findViewById(R.id.email);
        mNext = (Button) findViewById(R.id.next);
        mFirstName = (EditText) findViewById(R.id.first_name);
        mLastName = (EditText) findViewById(R.id.last_name);
    }

    private boolean passwordsEqual() {
        return (mPassword.getText() != null && !TextUtils.isEmpty(mPassword.getText().toString()) &&
                mPasswordConfirm.getText() != null && !TextUtils.isEmpty(mPasswordConfirm.getText().toString()) &&
                mPassword.getText().toString().equals(mPasswordConfirm.getText().toString()));
    }

    private void setupListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0 && mEmail.getText() != null && !TextUtils.isEmpty(mEmail.getText().toString())
                        && mFirstName.getText() != null && !TextUtils.isEmpty(mFirstName.getText().toString())
                        && mLastName.getText() != null && !TextUtils.isEmpty(mLastName.getText().toString())
                        && passwordsEqual()) {
                    mNext.setEnabled(true);
                    mNext.setTextColor(getResources().getColor(R.color.white));
                } else {
                    mNext.setEnabled(false);
                    mNext.setTextColor(getResources().getColor(R.color.parentButtonTextDisabled));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        };

        mPassword.addTextChangedListener(textWatcher);
        mPasswordConfirm.addTextChangedListener(textWatcher);

        mEmail.addTextChangedListener(textWatcher);
        mFirstName.addTextChangedListener(textWatcher);
        mLastName.addTextChangedListener(textWatcher);

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Parent parent = new Parent();
                parent.setUsername(mEmail.getText().toString());
                parent.setPassword(mPassword.getText().toString());
                parent.setFirstName(mFirstName.getText().toString());
                parent.setLastName(mLastName.getText().toString());

                UserManager.addParentAirwolf(APIHelper.getAirwolfDomain(CreateAccountActivity.this),
                        parent,
                        new StatusCallback<ParentResponse>(mStatusDelegate){
                    @Override
                    public void onResponse(retrofit2.Response<ParentResponse> response, LinkHeaders linkHeaders, ApiType type) {
                        Prefs prefs = new Prefs(CreateAccountActivity.this, com.instructure.parentapp.util.Const.CANVAS_PARENT_SP);
                        prefs.save(Const.ID, response.body().getParentId());
                        prefs.save(Const.NAME, mEmail.getText().toString());

                        //success. Save the id and token
                        APIHelper.setToken(CreateAccountActivity.this, response.body().getToken());

                        AppManager.getConfig().setToken(response.body().getToken());
                        //Take the parent to the add user page.
                        //We want to refresh cache so the main activity can load quickly with accurate information
                        UserManager.getStudentsForParentAirwolf(
                                APIHelper.getAirwolfDomain(CreateAccountActivity.this),
                                response.body().getParentId(),
                                new StatusCallback<List<Student>>(mStatusDelegate){
                            @Override
                            public void onResponse(retrofit2.Response<List<Student>> response, LinkHeaders linkHeaders, ApiType type) {
                                //restart the main activity
                                Intent intent = MainActivity.createIntent(CreateAccountActivity.this);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(0, 0);
                            }
                        });
                    }

                    @Override
                    public void onFail(Call<ParentResponse> response, Throwable error, int code) {
                        if (code == 400) {
                            Toast.makeText(CreateAccountActivity.this, getString(R.string.email_already_exists), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        mPasswordConfirm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    //create account
                    mNext.performClick();
                }
                return false;
            }
        });
    }

    @Override
    public void unBundle(@NonNull Bundle extras) {

    }

    @Override
    public boolean applyThemeAutomagically() {
        return false;
    }
}

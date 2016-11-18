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
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.UserManager;
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
public class ParentLoginActivity extends AppCompatActivity {

    private EditText mUserName;
    private EditText mPassword;
    private Button mNext;
    private RelativeLayout mCanvasLogin;
    private TextView mCreateAccount;
    private TextView mForgotPassword;

    public static Intent createIntent(Context context) {
        return new Intent(context, ParentLoginActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.parentStatusBarColor));
        setContentView(R.layout.activity_parent_login);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupViews();
        setupListeners();
    }

    private void setupViews() {
        mUserName = (EditText) findViewById(R.id.userName);
        mPassword = (EditText) findViewById(R.id.password);
        //Setting edit text to password here prevents the font from being changed
        mPassword.setTransformationMethod(new PasswordTransformationMethod());
        //we don't want the keyboard to display suggestions for passwords
        mPassword.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mNext = (Button) findViewById(R.id.next);
        mCanvasLogin = (RelativeLayout) findViewById(R.id.canvasLogin);
        mCreateAccount = (TextView) findViewById(R.id.createAccount);
        mForgotPassword = (TextView) findViewById(R.id.forgotPassword);
    }

    private void setupListeners() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0 &&
                        mUserName.getText() != null &&
                        !TextUtils.isEmpty(mUserName.getText().toString()) &&
                        mPassword.getText() != null && !TextUtils.isEmpty(mPassword.getText().toString())) {
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

        mUserName.addTextChangedListener(textWatcher);
        mPassword.addTextChangedListener(textWatcher);

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserManager.authenticateParentAirwolf(
                        APIHelper.getAirwolfDomain(ParentLoginActivity.this),
                        mUserName.getText().toString(),
                        mPassword.getText().toString(),
                        new StatusCallback<ParentResponse>(mStatusDelegate){
                            @Override
                            public void onResponse(retrofit2.Response<ParentResponse> response, LinkHeaders linkHeaders, ApiType type) {
                                APIHelper.setToken(ParentLoginActivity.this, response.body().getToken());
                                Prefs prefs = new Prefs(ParentLoginActivity.this, getString(R.string.app_name_parent));
                                prefs.save(Const.ID, response.body().getParentId());
                                prefs.save(Const.NAME, mUserName.getText().toString());

                                AppManager.getConfig().setToken(response.body().getToken());

                                UserManager.getStudentsForParentAirwolf(
                                        APIHelper.getAirwolfDomain(ParentLoginActivity.this),
                                        response.body().getParentId(),
                                        new StatusCallback<List<Student>>(mStatusDelegate){
                                            @Override
                                            public void onResponse(retrofit2.Response<List<Student>> response, LinkHeaders linkHeaders, ApiType type) {
                                                if (!APIHelper.isCachedResponse(response)) {
                                                    if (response.body() != null && !response.body().isEmpty()) {
                                                        //finish the activity so they can't hit the back button and see the login screen again
                                                        //they have students that they are observing, take them to that activity
                                                        startActivity(StudentViewActivity.createIntent(ParentLoginActivity.this, response.body()));
                                                        overridePendingTransition(0, 0);
                                                        finish();
                                                    } else {
                                                        //Take the parent to the add user page.
                                                        startActivity(DomainPickerActivity.createIntent(ParentLoginActivity.this, false, false, true));
                                                        overridePendingTransition(0, 0);
                                                        finish();
                                                    }
                                                }
                                            }
                                        }
                                );
                            }

                            @Override
                            public void onFail(Call<ParentResponse> response, Throwable error) {
                                Toast.makeText(ParentLoginActivity.this, getString(R.string.invalid_username_password), Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            }
        });

        mCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(CreateAccountActivity.createIntent(ParentLoginActivity.this));
            }
        });

        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(ForgotPasswordActivity.createIntent(ParentLoginActivity.this));
            }
        });

        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    //log in
                    mNext.performClick();
                }
                return false;
            }
        });

        mCanvasLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(DomainPickerActivity.createIntent(ParentLoginActivity.this, false, false, false));
            }
        });
    }

    private StatusCallback.StatusDelegate mStatusDelegate = new StatusCallback.StatusDelegate() {
        @Override
        public boolean hasNetworkConnection() {
            return AppManager.hasNetworkConnection(getApplicationContext());
        }
    };
}

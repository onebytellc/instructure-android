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
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.parentapp.R;
import com.instructure.parentapp.util.ViewUtils;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class ForgotPasswordActivity extends BaseParentActivity {

    private EditText mEmail;
    private Button mRequestPassword;

    public static Intent createIntent(Context context) {
        return new Intent(context, ForgotPasswordActivity.class);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.parentStatusBarColor));
        setContentView(R.layout.activity_forgot_password);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupViews();
        setupListeners();
    }

    private void setupViews() {
        mEmail = (EditText) findViewById(R.id.email);
        mRequestPassword = (Button) findViewById(R.id.requestPassword);
    }

    private void setupListeners() {
        mEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    mRequestPassword.setEnabled(true);
                    mRequestPassword.setTextColor(getResources().getColor(R.color.white));
                } else {
                    mRequestPassword.setEnabled(false);
                    mRequestPassword.setTextColor(getResources().getColor(R.color.parentButtonTextDisabled));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        mRequestPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserManager.sendPasswordResetForParentAirwolf(
                        APIHelper.getAirwolfDomain(ForgotPasswordActivity.this),
                        mEmail.getText().toString(),
                        new StatusCallback<ResponseBody>(mStatusDelegate){
                            @Override
                            public void onResponse(retrofit2.Response<ResponseBody> response, LinkHeaders linkHeaders, ApiType type) {
                                if(response != null && response.code() == 200) {
                                    //successfully sent, let the user know.
                                    closeKeyboard();
                                    Toast.makeText(ForgotPasswordActivity.this, getString(R.string.password_reset_success), Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }

                            @Override
                            public void onFail(Call<ResponseBody> response, Throwable error, int code) {
                                if(code == 404) {
                                    Toast.makeText(ForgotPasswordActivity.this, getString(R.string.password_reset_no_user), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        mEmail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_GO){
                    //reset password
                    mRequestPassword.performClick();
                }
                return false;
            }
        });
    }

    public void closeKeyboard() {
        //close the keyboard
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(mRequestPassword.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void unBundle(@NonNull Bundle extras) {

    }

    @Override
    public boolean applyThemeAutomagically() {
        return false;
    }
}

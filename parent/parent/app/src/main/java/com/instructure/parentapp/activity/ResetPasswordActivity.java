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

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.ResetParent;
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
 *
 * This activity will show when the user clicks the link in their email that is sent when
 * they try to reset their password.
 *
 */
public class ResetPasswordActivity extends BaseParentActivity {

    private EditText mPassword;
    private EditText mPasswordConfirm;
    private Button mResetPassword;

    private String mHost;
    private String mToken;
    private String mParentId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        final String url = getIntent().getDataString();

        if(TextUtils.isEmpty(url)) {
            finish();
            return;
        }

        final Uri data =  Uri.parse(url);

        if(data == null) {
            finish();
            return;
        }

        mHost = data.getHost();


        //try to get the parent id
        int index = url.indexOf("username");
        if(index != -1) {
            index += "username=".length();

            int endIndex = url.indexOf("&", index);

            if(endIndex != -1) {
                mParentId = url.substring(index, endIndex);
            }
        }

        //try to get the token
        index = url.indexOf("recovery_token");
        if(index != -1) {
            index += "recovery_token=".length();

            int endIndex = url.length();

            if(endIndex != -1) {
                mToken = url.substring(index, endIndex);
            }
        }

        setupViews();
        setupListeners();

        //make the status bar cyan
        ViewUtils.setStatusBarColor(this, getResources().getColor(R.color.parentStatusBarColor));
    }

    private void setupViews() {
        mPassword = (EditText) findViewById(R.id.resetPasswordEditText);
        mPasswordConfirm = (EditText) findViewById(R.id.resetPasswordConfirmEditText);
        mResetPassword = (Button) findViewById(R.id.resetPasswordButton);

        //Setting edit text to password here prevents the font from being changed
        mPassword.setTransformationMethod(new PasswordTransformationMethod());
        mPasswordConfirm.setTransformationMethod(new PasswordTransformationMethod());
        //we don't want the keyboard to display suggestions for passwords
        mPassword.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mPasswordConfirm.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

    private void setupListeners() {
        mResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //set the user's token to the temporary token
                APIHelper.setToken(ResetPasswordActivity.this, mToken);

                UserManager.resetParentPasswordAirwolf(
                        APIHelper.getAirwolfDomain(ResetPasswordActivity.this),
                        mParentId,
                        mPassword.getText().toString(),
                        new StatusCallback<ResetParent>(mStatusDelegate){
                            @Override
                            public void onResponse(retrofit2.Response<ResetParent> response, LinkHeaders linkHeaders, ApiType type) {
                                //success, now set the user's token to the one just created
                                APIHelper.setToken(ResetPasswordActivity.this, response.body().getToken());

                                Prefs prefs = new Prefs(ResetPasswordActivity.this, getString(R.string.app_name_parent));
                                prefs.save(Const.ID, response.body().getParentId());

                                //try to get the students. when we start the main activity it will check the cached values,
                                //which at this point there won't be any
                                UserManager.getStudentsForParentAirwolf(
                                        APIHelper.getAirwolfDomain(ResetPasswordActivity.this),
                                        response.body().getParentId(),
                                        new StatusCallback<List<Student>>(mStatusDelegate){
                                            @Override
                                            public void onResponse(retrofit2.Response<List<Student>> response, LinkHeaders linkHeaders, ApiType type) {
                                                //start the main activity
                                                finish();
                                                startActivity(MainActivity.createIntent(ResetPasswordActivity.this));
                                            }
                                        }
                                );
                            }

                            @Override
                            public void onFail(Call<ResetParent> response, Throwable error) {
                                //reset the token to be nothing so when we open the app it won't try to use the token
                                APIHelper.resetToken(ResetPasswordActivity.this);
                            }
                        }
                );
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0 && mPassword.getText() != null && mPasswordConfirm.getText() != null &&
                        mPassword.getText().toString().equals(mPasswordConfirm.getText().toString())) {
                    mResetPassword.setEnabled(true);
                    mResetPassword.setTextColor(getResources().getColor(R.color.white));

                } else {
                    mResetPassword.setEnabled(false);
                    mResetPassword.setTextColor(getResources().getColor(R.color.parentButtonTextDisabled));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        mPassword.addTextChangedListener(textWatcher);
        mPasswordConfirm.addTextChangedListener(textWatcher);
    }

    @Override
    public void unBundle(@NonNull Bundle extras) {

    }

    @Override
    public boolean applyThemeAutomagically() {
        return false;
    }
}

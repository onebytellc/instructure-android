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

package com.instructure.candroid.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.candroid.R;

public class AuthDialogStyled extends DialogFragment {

    private AuthDialogCallback callback;

    public interface AuthDialogCallback {
        public void onLoginClick(String username, String password);
        public void onCancelClick();
    }

    public static AuthDialogStyled newInstance() {
        return new AuthDialogStyled();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof AuthDialogCallback) {
            callback = (AuthDialogCallback)activity;
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = (FragmentActivity)getActivity();

        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(activity)
                                  .title(activity.getString(R.string.authenticationRequired))
                                  .positiveText(activity.getString(R.string.confirm))
                                  .negativeText(activity.getString(R.string.cancel));

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.auth_dialog, null);

        final EditText username = (EditText)view.findViewById(R.id.username);
        final EditText password = (EditText)view.findViewById(R.id.password);

        builder.customView(view, false);

        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                if (callback != null) {
                    final String usernameResult = username.getText().toString();
                    final String passwordResult = password.getText().toString();
                    if (!TextUtils.isEmpty(usernameResult) && !TextUtils.isEmpty(passwordResult)) {
                        callback.onLoginClick(usernameResult, passwordResult);
                        dismiss();
                    } else {
                        Toast.makeText(getActivity(), R.string.invalidEmailPassword, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onNegative(dialog);
                if (callback != null) {
                    callback.onCancelClick();
                }
                dismiss();
            }
        });

        final MaterialDialog dialog = builder.build();

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}

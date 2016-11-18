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

import com.instructure.candroid.R;
import com.instructure.candroid.fragment.ParentFragment;
import com.instructure.loginapi.login.materialdialogs.CustomDialog;

import java.io.Serializable;

public class DataLossFragmentStyled extends DialogFragment {

    public static final String TAG = "dataLossFragment";
    public static final int DATA_LOSS_FRAGMENT_STYLED_TARGET = 8291;

    public static void show(ParentFragment fragment) {
        DataLossFragmentStyled dataLossFragment = new DataLossFragmentStyled();
        dataLossFragment.setTargetFragment(fragment, DATA_LOSS_FRAGMENT_STYLED_TARGET);
        dataLossFragment.show(fragment.getFragmentManager(), TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = (FragmentActivity)getActivity();

        CustomDialog.Builder builder = new CustomDialog.Builder(activity,
                activity.getString(R.string.unsavedProgress),
                activity.getString(R.string.okay));
        builder.darkTheme(false);
        builder.content(activity.getString(R.string.informationLost));
        builder.negativeText(R.string.cancel);

        final CustomDialog dialog = builder.build();


        dialog.setClickListener(new CustomDialog.ClickListener() {
            @Override
            public void onConfirmClick() {
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, getActivity().getIntent());
                dismissAllowingStateLoss();
            }

            @Override
            public void onCancelClick() {
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, getActivity().getIntent());
                dismissAllowingStateLoss();
            }
        });

        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }
}
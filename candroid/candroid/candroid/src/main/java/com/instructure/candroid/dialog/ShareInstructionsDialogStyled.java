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

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.candroid.R;

public class ShareInstructionsDialogStyled extends DialogFragment {

    public static final String TAG = "shareInstructorDialog";

    public static ShareInstructionsDialogStyled show(FragmentActivity activity) {
        ShareInstructionsDialogStyled frag = new ShareInstructionsDialogStyled();
        frag.show(activity.getSupportFragmentManager(), TAG);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final FragmentActivity activity = (FragmentActivity)getActivity();

        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(activity)
                                  .title(activity.getString(R.string.fromOtherApplication))
                                  .positiveText(activity.getString(R.string.okay));

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.share_dialog_instructions, null);

        builder.customView(view, false);

        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                dismissAllowingStateLoss();
            }
        });

        final MaterialDialog dialog = builder.build();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }
}


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

package com.instructure.parentapp.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputType;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.parentapp.R;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class StudentThresholdDialog extends DialogFragment {

    private static final String TITLE = "TITLE";
    private static final String THRESHOLD = "THRESHOLD";
    private static final String THRESHOLD_TYPE = "THRESHOLD_TYPE";

    private StudentThresholdChanged mCallback;

    public interface StudentThresholdChanged {
        void handlePositiveThreshold(int thresholdType, String value);
        void handleNeutralThreshold(int thresholdType);
    }

    public static StudentThresholdDialog newInstance(String title, String currentThreshold, int thresholdType) {
        StudentThresholdDialog dialog = new StudentThresholdDialog();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(THRESHOLD, currentThreshold);
        args.putInt(THRESHOLD_TYPE, thresholdType);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof StudentThresholdChanged) {
            mCallback = (StudentThresholdChanged)context;
        } else {
            throw new IllegalStateException("Caller must implement StudentThresholdChange callback.");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String title = getArguments().getString(TITLE, "");
        final String threshold = getArguments().getString(THRESHOLD, "");

        return new MaterialDialog.Builder(getActivity())
                .title(title)
                .titleColor(getResources().getColor(R.color.canvasTextDark))
                .titleGravity(GravityEnum.CENTER)
                .positiveText(getResources().getString(R.string.save))
                .negativeText(getResources().getString(R.string.cancel))
                .neutralText(getResources().getString(R.string.never))
                .buttonsGravity(GravityEnum.CENTER)
                .inputRange(1, 3)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .input(getResources().getString(R.string.enterThreshold), (threshold.equals(getResources().getString(R.string.never)) ? "" : threshold), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                        //Input will return here, when we eventually add input checking
                        //We can filter input by calling alwaysCallInputCallback() and check the
                        //input here
                        final int thresholdType = getArguments().getInt(THRESHOLD_TYPE);
                        mCallback.handlePositiveThreshold(thresholdType, charSequence.toString());
                    }
                })
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        if (dialogAction.equals(DialogAction.NEUTRAL)) {
                            final int thresholdType = getArguments().getInt(THRESHOLD_TYPE);
                            mCallback.handleNeutralThreshold(thresholdType);
                        }
                    }
                })
                .show();
    }
}

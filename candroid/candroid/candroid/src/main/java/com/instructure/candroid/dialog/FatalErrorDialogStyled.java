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
import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.candroid.R;

public class FatalErrorDialogStyled extends DialogFragment {

    public static final String TAG = "fatalErrorDialog";

    private static final String TITLE = "title";
    private static final String MESSAGE = "message";
    private static final String ICON = "icon";
    private static final String SHOULD_DISMISS = "shouldDismiss";

    public static FatalErrorDialogStyled newInstance(int title, int message, int icon) {
        FatalErrorDialogStyled frag = new FatalErrorDialogStyled();
        Bundle args = new Bundle();
        args.putInt(TITLE, title);
        args.putInt(MESSAGE, message);
        args.putInt(ICON, icon);
        args.putBoolean(SHOULD_DISMISS, false);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    /*
     @param shouldDismiss: if true dismiss the dialog, otherwise finish the activity
     */
    public static FatalErrorDialogStyled newInstance(int title, int message, int icon, boolean shouldDismiss) {
        FatalErrorDialogStyled frag = newInstance(title, message, icon);
        Bundle args = frag.getArguments();
        args.putBoolean(SHOULD_DISMISS, shouldDismiss);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final FragmentActivity activity = (FragmentActivity)getActivity();
        Bundle args = getArguments();

        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(activity)
                                  .title(args.getInt(TITLE))
                                  .positiveText(activity.getString(R.string.okay))
                                  .content(args.getInt(MESSAGE));

        final boolean shouldDismiss = args.getBoolean(SHOULD_DISMISS, false);

        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                if(shouldDismiss) {
                    dialog.dismiss();
                } else {
                    activity.finish();
                }
            }
        });

        final MaterialDialog dialog = builder.build();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
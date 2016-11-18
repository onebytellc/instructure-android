/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.pandautils.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.R;

public class TutorialDialog extends DialogFragment {

    private String mTitle = "";
    private String mMessage = "";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final FragmentActivity activity = (FragmentActivity)getActivity();

        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(activity)
                                  .title(mTitle)
                                  .positiveText(getString(R.string.done))
                                  .content(mMessage);

        final MaterialDialog dialog = builder.build();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        Bundle args = getArguments();
        if(args != null) {
            mTitle = args.getString(com.instructure.pandautils.utils.Const.TITLE, "");
            mMessage = args.getString(com.instructure.pandautils.utils.Const.MESSAGE, "");
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        //if the tutorial was in the actionbar we want to refresh it so it draws the correct icon
        getActivity().invalidateOptionsMenu();
    }
}

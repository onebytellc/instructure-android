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
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.candroid.R;
import com.instructure.pandautils.utils.Const;
import com.instructure.canvasapi.model.Assignment;

public class WhatIfDialogStyled extends DialogFragment {

    public static final String TAG = "whatIfDialog";

    private static String totalScore;
    private static Assignment assignment;
    private static WhatIfDialogCallback callback;
    private static int courseColor;
    private static int position;

    //views
    private EditText whatIfScore;
    private EditText totalScoreEdit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle args = getArguments();
        if(args != null) {
            assignment = args.getParcelable(Const.ASSIGNMENT);
            totalScore = args.getString(Const.SCORE);
            courseColor = args.getInt(Const.COURSE_COLOR);
            position = args.getInt(Const.POSITION);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final FragmentActivity activity = getActivity();

        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
                .title(activity.getString(R.string.whatIfDialogText))
                .positiveText(R.string.done)
                .negativeText(R.string.cancel)
                .autoDismiss(false);

        View view = LayoutInflater.from(activity).inflate(R.layout.what_if_dialog, null);

        totalScoreEdit = (EditText)view.findViewById(R.id.totalScore);
        whatIfScore = (EditText)view.findViewById(R.id.currentScore);
        totalScoreEdit.setText(totalScore);

        builder.customView(view, false);

        if(courseColor != 0){
            builder.positiveColor(courseColor);
            builder.negativeColor(courseColor);
        }

        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                super.onPositive(dialog);
                if(callback != null){
                    final String whatIf = whatIfScore.getText().toString();
                    callback.onOkayClick(whatIf, Double.parseDouble(totalScore), assignment, position);
                }
                dismissAllowingStateLoss();
            }

            @Override
            public void onNegative(MaterialDialog dialog) {
                super.onNegative(dialog);
                dismissAllowingStateLoss();
            }
        });

        final MaterialDialog dialog = builder.build();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    public interface WhatIfDialogCallback{
        void onOkayClick(String whatIf, double total, Assignment assignment, int position);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    public static void show(FragmentActivity activity, double score, WhatIfDialogCallback callback, Assignment assignment, int courseColor, int position)  {
        WhatIfDialogStyled frag = new WhatIfDialogStyled();
        frag.callback = callback;

        Bundle args = new Bundle();
        args.putInt(Const.COURSE_COLOR, courseColor);
        args.putInt(Const.POSITION, position);
        args.putParcelable(Const.ASSIGNMENT, assignment);
        args.putString(Const.SCORE, Double.toString(score));
        frag.setArguments(args);

        frag.show(activity.getSupportFragmentManager(), TAG);
    }
}

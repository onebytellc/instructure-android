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
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.candroid.R;
import com.instructure.candroid.activity.InternalWebViewActivity;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.marcoscg.easylicensesdialog.EasyLicensesDialog;

public class LegalDialogStyled extends DialogFragment {

    public static final String TAG = "legalDialog";


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final FragmentActivity activity = getActivity();

        MaterialDialog.Builder builder =
                new MaterialDialog.Builder(activity)
                                  .title(activity.getString(R.string.legal));
        View view = LayoutInflater.from(activity).inflate(R.layout.legal, null);
        Drawable drawable = CanvasContextColor.getColoredDrawable(getActivity(), R.drawable.ic_cv_document_fill, R.color.defaultPrimary);
        TextView eula = (TextView)view.findViewById(R.id.eula);
        eula.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

        TextView termsOfUse = (TextView)view.findViewById(R.id.termsOfUse);
        termsOfUse.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

        TextView privacyPolicy = (TextView)view.findViewById(R.id.privacyPolicy);
        privacyPolicy.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

        TextView openSource = (TextView)view.findViewById(R.id.openSource);
        openSource.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

        builder.customView(view, true);

        final MaterialDialog dialog = builder.build();


        termsOfUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = InternalWebViewActivity.createIntent(getActivity(), "http://www.canvaslms.com/policies/terms-of-use", activity.getString(R.string.termsOfUse), false);
                getActivity().startActivity(intent);
                dialog.dismiss();
            }
        });

        eula.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = InternalWebViewActivity.createIntent(getActivity(), "http://www.canvaslms.com/policies/end-user-license-agreement", activity.getString(R.string.EULA), false);
                getActivity().startActivity(intent);
                dialog.dismiss();
            }
        });

        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = InternalWebViewActivity.createIntent(getActivity(), "https://www.canvaslms.com/policies/privacy", activity.getString(R.string.privacyPolicy), false);
                getActivity().startActivity(intent);
                dialog.dismiss();
            }
        });

        openSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EasyLicensesDialog(getActivity()).show();
                dialog.dismiss();
            }
        });

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

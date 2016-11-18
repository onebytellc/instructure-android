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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.candroid.R;
import com.instructure.candroid.activity.InternalWebViewActivity;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.LoggingUtility;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.loginapi.login.api.zendesk.utilities.ZendeskDialogStyled;
import com.instructure.loginapi.login.rating.RatingDialog;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.utils.Const;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HelpDialogStyled extends DialogFragment {

    public static final String TAG = "helpDialogStyled";
    private static String SHOW_ASK_INSTRUCTOR = "showAskInstructor";

    private LinearLayout askInstructor;
    private LinearLayout searchGuides;
    private LinearLayout reportProblem;
    private LinearLayout requestFeature;
    private LinearLayout showLove;

    //check if the user is only a teacher. if they only have teacher enrollments we don't want to show the askInstructor button
    private boolean showAskInstructor = false; 

    public static HelpDialogStyled show(FragmentActivity activity, boolean showAskInstructor) {
        HelpDialogStyled helpDialogStyled = new HelpDialogStyled();
        Bundle args = new Bundle();
        args.putBoolean(SHOW_ASK_INSTRUCTOR, showAskInstructor);
        helpDialogStyled.setArguments(args);
        helpDialogStyled.show(activity.getSupportFragmentManager(), TAG);
        return helpDialogStyled;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity()).title(getActivity().getString(R.string.help));

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.help_dialog, null);
        askInstructor = (LinearLayout) view.findViewById(R.id.ask_instructor);
        askInstructor.setVisibility(View.GONE);
        searchGuides = (LinearLayout) view.findViewById(R.id.search_guides);
        reportProblem = (LinearLayout) view.findViewById(R.id.report_problem);
        requestFeature = (LinearLayout) view.findViewById(R.id.request_feature);
        showLove = (LinearLayout) view.findViewById(R.id.share_love);

        builder.customView(view, true);

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

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showAskInstructor = getArguments().getBoolean(SHOW_ASK_INSTRUCTOR, false);
        if (showAskInstructor) {
            askInstructor.setVisibility(View.VISIBLE);
        }
        setupListeners();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    private void setupListeners() {
        askInstructor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open the ask instructor dialog
                new AskInstructorDialogStyled().show(getFragmentManager(), AskInstructorDialogStyled.TAG);

                //Log to GA
                Analytics.trackButtonPressed(getActivity(), "[HelpDialog] AskInstructor", null);
            }
        });

        searchGuides.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Search guides
                startActivity(InternalWebViewActivity.createIntent(getActivity(), Const.CANVAS_USER_GUIDES, getString(R.string.canvasGuides), false));

                //Log to GA
                Analytics.trackButtonPressed(getActivity(), "Search Guides", 0L);
            }
        });

        reportProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZendeskDialogStyled dialog = new ZendeskDialogStyled();

                int colorBlack = getResources().getColor(R.color.black);
                dialog.setArguments(ZendeskDialogStyled.createBundle(colorBlack, colorBlack, colorBlack));
                dialog.show(getActivity().getSupportFragmentManager(), ZendeskDialogStyled.TAG);

                //Log to GA
                Analytics.trackButtonPressed(getActivity(), "Show Zendesk", 0L);
            }
        });

        requestFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //let the user open their favorite mail client

                Intent intent = populateMailIntent(getActivity().getString(R.string.featureSubject), getActivity().getString(R.string.understandRequest), false);

                startActivity(Intent.createChooser(intent, getActivity().getString(R.string.sendMail)));

                //Log to GA
                Analytics.trackButtonPressed(getActivity(), "RequestFeature", null);
            }
        });

        showLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.goToAppStore(RatingDialog.APP_NAME.CANDROID, getActivity());

                //Log to GA
                Analytics.trackButtonPressed(getActivity(), "Feedback", null);
            }
        });
    }

    /*
        Pass in the subject and first line of the e-mail, all the other data is the same
     */
    private Intent populateMailIntent(String subject, String title, boolean supportFlag) {
        //let the user open their favorite mail client
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        if(supportFlag){
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getActivity().getString(R.string.supportEmailAddress)});
        }else{
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getActivity().getString(R.string.mobileSupportEmailAddress)});
        }
        //try to get the version number and version code
        PackageInfo pInfo = null;
        String versionName = "";
        int versionCode = 0;
        try {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            versionName = pInfo.versionName;
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            LoggingUtility.LogConsole(e.getMessage());
        }

        intent.putExtra(Intent.EXTRA_SUBJECT, "[" + subject + "] Issue with Canvas [Android] " + versionName);

        User user = APIHelpers.getCacheUser(getActivity());
        //populate the email body with information about the user
        String emailBody = "";
        emailBody += title + "\n";
        emailBody += getActivity().getString(R.string.help_userId) + " " + user.getId() + "\n";
        emailBody += getActivity().getString(R.string.help_email) + " " + user.getEmail() + "\n";
        emailBody += getActivity().getString(R.string.help_domain) + " " + APIHelpers.getDomain(getActivity()) + "\n";
        emailBody += getActivity().getString(R.string.help_versionNum) + " " + versionName + " " + versionCode + "\n";
        emailBody += getActivity().getString(R.string.help_locale) + " " + Locale.getDefault() + "\n";
        emailBody += getActivity().getString(R.string.installDate) + " " + getInstallDateString() + "\n";
        emailBody += "----------------------------------------------\n";

        intent.putExtra(Intent.EXTRA_TEXT, emailBody);

        return intent;
    }

    private String getInstallDateString() {
        try {
            long installed = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0)
                    .firstInstallTime;
            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
            return format.format(new Date(installed));
        } catch (Exception e) {
            return "";
        }
    }

}

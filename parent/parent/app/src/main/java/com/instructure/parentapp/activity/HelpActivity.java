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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.loginapi.login.api.zendesk.utilities.ZendeskDialogStyled;
import com.instructure.loginapi.login.rating.RatingDialog;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.Prefs;
import com.instructure.parentapp.BuildConfig;
import com.instructure.parentapp.R;
import com.instructure.parentapp.util.ApplicationManager;
import com.marcoscg.easylicensesdialog.EasyLicensesDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class HelpActivity extends AppCompatActivity implements
        ZendeskDialogStyled.ZendeskDialogResultListener {

    //FIXME: probably broke with removing status delegate

    private TextView mParentEmail;
    private LinearLayout mSearchGuides;
    private LinearLayout mReportProblem;
    private LinearLayout mRequestFeature;
    private LinearLayout mShowLove;
    private LinearLayout mOpenSource;
    private String mEmailAddress;

    public static Intent createIntent(Context context) {
        return new Intent(context, HelpActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setupViews();
        setupListeners();
    }

    private void setupViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white);
        toolbar.setNavigationContentDescription(R.string.close);
        toolbar.setTitle(R.string.help);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mParentEmail = (TextView) findViewById(R.id.parentEmail);

        Prefs prefs = new Prefs(HelpActivity.this, com.instructure.parentapp.util.Const.CANVAS_PARENT_SP);
        mEmailAddress = prefs.load(Const.NAME, "");
        mParentEmail.setText(mEmailAddress);


        mSearchGuides = (LinearLayout) findViewById(R.id.search_guides);
        mReportProblem = (LinearLayout) findViewById(R.id.report_problem);
        mRequestFeature = (LinearLayout) findViewById(R.id.request_feature);
        mShowLove = (LinearLayout) findViewById(R.id.share_love);
        mOpenSource = (LinearLayout) findViewById(R.id.open_source);

    }

    private void setupListeners() {

        mSearchGuides.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Search guides
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(Const.CANVAS_USER_GUIDES));
                startActivity(intent);

            }
        });

        mReportProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ZendeskDialogStyled dialog = new ZendeskDialogStyled();

                //set the cached user so the dialog can use the cached user's email address
                User user = new User();
                user.setPrimaryEmail(mEmailAddress);
                APIHelper.setCacheUser(HelpActivity.this, user);
                int colorBlack = getResources().getColor(R.color.defaultPrimary);
                dialog.setArguments(ZendeskDialogStyled.createBundle(colorBlack, colorBlack, colorBlack, false, true));
                dialog.show(getSupportFragmentManager(), ZendeskDialogStyled.TAG);

            }
        });

        mRequestFeature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //let the user open their favorite mail client
                Intent intent = populateMailIntent(getString(R.string.featureSubject), getString(R.string.understandRequest), false);
                startActivity(Intent.createChooser(intent, getString(R.string.sendMail)));

            }
        });

        mShowLove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.goToAppStore(RatingDialog.APP_NAME.PARENT, HelpActivity.this);

            }
        });

        mOpenSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new EasyLicensesDialog(HelpActivity.this).show();
            }
        });
    }

    //region Zendesk
    @Override
    public void onTicketPost() {
        Toast.makeText(this, R.string.zendesk_feedbackThankyou, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onTicketError() {
        Toast.makeText(this, R.string.errorOccurred, Toast.LENGTH_LONG).show();
    }
    //endregion

    /*
        Pass in the subject and first line of the e-mail, all the other data is the same
     */
    private Intent populateMailIntent(String subject, String title, boolean supportFlag) {
        //let the user open their favorite mail client
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        if(supportFlag){
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.supportEmailAddress)});
        }else{
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.mobileSupportEmailAddress)});
        }
        //try to get the version number and version code
        PackageInfo pInfo = null;
        String versionName = "";
        int versionCode = 0;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
            versionCode = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("ParentApp", e.getMessage());
        }

        intent.putExtra(Intent.EXTRA_SUBJECT, "[" + subject + "] Issue with ParentApp [Android] " + versionName);

        String parentId = ApplicationManager.getParentId(HelpActivity.this);
        //populate the email body with information about the user
        String emailBody = "";
        emailBody += title + "\n";
        emailBody += getString(R.string.help_userId) + " " + parentId + "\n";
        emailBody += getString(R.string.help_email) + " " + mEmailAddress + "\n";
        emailBody += getString(R.string.help_domain) + " " + APIHelper.getAirwolfDomain(HelpActivity.this) + "\n";
        emailBody += getString(R.string.help_versionNum) + " " + versionName + " " + versionCode + "\n";
        emailBody += getString(R.string.help_locale) + " " + Locale.getDefault() + "\n";
        emailBody += getString(R.string.installDate) + " " + getInstallDateString() + "\n";
        emailBody += "----------------------------------------------\n";

        intent.putExtra(Intent.EXTRA_TEXT, emailBody);

        return intent;
    }

    private String getInstallDateString() {
        try {
            long installed = getPackageManager()
                    .getPackageInfo(getPackageName(), 0)
                    .firstInstallTime;
            SimpleDateFormat format = new SimpleDateFormat("dd MMM yyyy");
            return format.format(new Date(installed));
        } catch (Exception e) {
            return "";
        }
    }
}

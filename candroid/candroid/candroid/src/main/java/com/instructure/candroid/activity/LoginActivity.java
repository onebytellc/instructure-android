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

package com.instructure.candroid.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import com.instructure.candroid.R;
import com.instructure.candroid.service.PushRegistrationService;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.candroid.util.CanvasErrorDelegate;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.ErrorDelegate;
import com.instructure.loginapi.login.URLSignIn;
import com.instructure.loginapi.login.api.zendesk.utilities.ZendeskDialogStyled;
import io.fabric.sdk.android.Fabric;

public class LoginActivity extends URLSignIn implements
        ZendeskDialogStyled.ZendeskDialogResultListener {

    private PopupWindow popupMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //change the help icon to red
        Drawable d = CanvasContextColor.getColoredDrawable(this, R.drawable.ic_cv_help, getResources().getColor(R.color.canvasRed));
        ((ImageView)getCanvasHelpIconView()).setImageDrawable(d);
        ((ImageView)getCanvasHelpView().findViewById(R.id.image)).setImageDrawable(d);
    }

    @Override
    public ErrorDelegate getErrorDelegate() {
        return new CanvasErrorDelegate();
    }

    @Override
    public void startNextActivity() {
        if(!PushRegistrationService.hasTokenBeenSentToServer(getContext())) {
            startService(new Intent(this, PushRegistrationService.class));//Register Push Notifications
        }
        Intent intent = new Intent(this, NavigationActivity.getStartActivityClass());
        if(getIntent() != null && getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void startNextActivity(Uri passedURI) {
        startActivity(InterwebsToApplication.createIntent(LoginActivity.this, passedURI));
        finish();
    }

    @Override
    public void initializeLoggingForUser(boolean isAnonymousDomain, User signedInUser) {

        //Initialize Crashlytics
        if (!isAnonymousDomain) {
            Crashlytics.setUserIdentifier(Long.toString(signedInUser.getId()));
        } else {
            //Clear context for Crashlytics.
            Crashlytics.setUserIdentifier("");
        }
        Crashlytics.setString("domain", APIHelpers.getDomain(LoginActivity.this));
    }

    @Override
    public void startCrashlytics() {
        Fabric.with(this, new Crashlytics());
    }

    @Override
    public void startHelpShift() { }

    @Override
    public void startGoogleAnalytics() {

    }

    @Override
    public void showHelpShiftSupport() {
        //we're going to show a pop up window that will let the user either search the guides or
        //report a problem
        final int height = getResources().getDimensionPixelSize(R.dimen.popup_height);
        final int width = getResources().getDimensionPixelSize(R.dimen.popup_width);

        final View popup = LayoutInflater.from(LoginActivity.this).inflate(R.layout.popup_window_help_options, null);
        Button searchGuides = (Button)popup.findViewById(R.id.search_guides);
        Button reportProblem = (Button)popup.findViewById(R.id.report_problem);

        searchGuides.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMessage.dismiss();
                //Search guides
                startActivity(InternalWebViewActivity.createIntent(LoginActivity.this, "https://community.canvaslms.com/docs/DOC-1543", getString(R.string.canvasGuides), false));
            }
        });
        reportProblem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMessage.dismiss();

                ZendeskDialogStyled dialog = new ZendeskDialogStyled();
                int colorBlack = getResources().getColor(R.color.black);
                dialog.setArguments(ZendeskDialogStyled.createBundle(colorBlack, colorBlack, colorBlack, true));
                dialog.show(getSupportFragmentManager(), ZendeskDialogStyled.TAG);
            }
        });
        popupMessage = new PopupWindow(popup, width, height, true);
        popupMessage.setOutsideTouchable(true);
        popupMessage.setFocusable(true);
        popupMessage.setBackgroundDrawable(new BitmapDrawable(getResources(), ""));

        if(getCanvasHelpIconView().getVisibility() == View.VISIBLE) {
            //show it right below the help icon
            popupMessage.showAsDropDown(getCanvasHelpIconView(), 0, 0);
        }
        else {
            //show it below the help row that shows up in the adapter
            popupMessage.showAsDropDown(getCanvasHelpView(), 0, 0);
        }
    }

    @Override
    public void trackAppFlow(Activity activity) {
        Analytics.trackAppFlow(LoginActivity.this);
    }

    @Override
    public void displayMessage(String message, int messageType) {
        if(!TextUtils.isEmpty(message)){
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getRootLayout() {
        return R.layout.url_sign_in;
    }

    @Override
    public String getPrefsFileName() {
        return ApplicationManager.PREF_FILE_NAME;
    }

    @Override
    public String getPrefsPreviousDomainKey() {
        return ApplicationManager.PREF_NAME_PREVIOUS_DOMAINS;
    }

    @Override
    public String getPrefsOtherSignedInUsersKey() {
        return ApplicationManager.OTHER_SIGNED_IN_USERS_PREF_NAME;
    }

    @Override
    public String getPrefsMultiUserKey() {
        return ApplicationManager.MULTI_SIGN_IN_PREF_NAME;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Zendesk Dialog Result
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onTicketPost() {
        Toast.makeText(getContext(), R.string.zendesk_feedbackThankyou, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onTicketError() {
        Toast.makeText(getContext(), R.string.errorOccurred, Toast.LENGTH_LONG).show();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intents
    ///////////////////////////////////////////////////////////////////////////

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }

    public static Intent createIntent(Context context, Uri passedURI) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Const.PASSED_URI, passedURI);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }


    public static Intent createIntent(Context context, boolean showMessage, String message) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Const.SHOW_MESSAGE, showMessage);
        intent.putExtra(Const.MESSAGE_TO_USER, message);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }
}

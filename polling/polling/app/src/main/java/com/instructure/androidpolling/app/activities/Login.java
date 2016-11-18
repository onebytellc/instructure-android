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

package com.instructure.androidpolling.app.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.devspark.appmsg.AppMsg;
import com.instructure.androidpolling.app.R;
import com.instructure.androidpolling.app.util.ApplicationManager;
import com.instructure.androidpolling.app.util.CanvasErrorDelegate;
import com.instructure.androidpolling.app.util.Constants;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.ErrorDelegate;
import com.instructure.loginapi.login.URLSignIn;


public class Login extends URLSignIn {

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    public ErrorDelegate getErrorDelegate() {
        return new CanvasErrorDelegate();
    }


    public void startNextActivity() {
        startActivity(StartingActivity.createIntent(getApplicationContext()));
        finish();
    }

    public void startNextActivity(Uri passedURI) {
        startActivity(StartingActivity.createIntent(getApplicationContext(), passedURI));
        finish();
    }

    public void initializeLoggingForUser(boolean isSimonFraser, User signedInUser) {

    }

    public void startCrashlytics() {

    }

    public void startHelpShift() {

    }

    public void startGoogleAnalytics() {

    }

    public void showHelpShiftSupport() {

    }

    public void trackAppFlow(Activity activity) {
        //TODO: wire up analytics if we ever work on this app again.
    }

    @Override
    public String getUserAgent() {
        return "androidPolling";
    }

    @Override
    public int getRootLayout() { return R.layout.polls_url_sign_in; }
    public void displayMessage(String message, int messageType) {
        AppMsg.Style style = AppMsg.STYLE_ERROR;
        switch (messageType) {
            case 0:
                style = AppMsg.STYLE_ERROR;
                break;
            case 1:
                style = AppMsg.STYLE_WARNING;
                break;
            case 2:
                style = AppMsg.STYLE_SUCCESS;
                break;
        }
        AppMsg.makeText(this, message, style).show();
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
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        return intent;
    }

    public static Intent createIntent(Context context, Uri passedURI) {
        Intent intent = new Intent(context, Login.class);
        intent.putExtra(Constants.PASSED_URI, passedURI);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        return intent;
    }


}

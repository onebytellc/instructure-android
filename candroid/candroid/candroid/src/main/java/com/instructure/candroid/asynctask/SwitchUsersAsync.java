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

package com.instructure.candroid.asynctask;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;


import com.instructure.candroid.R;
import com.instructure.candroid.activity.LoginActivity;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.candroid.view.CanvasRecipientManager;
import com.instructure.candroid.widget.CanvasWidgetProvider;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.loginapi.login.OAuthWebLogin;
import com.instructure.loginapi.login.model.SignedInUser;
import com.instructure.pandautils.utils.Utils;

import java.io.File;

public class SwitchUsersAsync extends AsyncTask<Void, Void, Boolean> {

    public interface SwitchUsersAsyncCallbacks {
        public void preExecute();
        public void postExecute();
    }

    private SwitchUsersAsyncCallbacks mCallbacks;
    private Activity mActivity;
    private SignedInUser mSignedInUser = null;

    public SwitchUsersAsync(Activity activity) {
        mActivity = activity;
    }

    public SwitchUsersAsync(Activity activity, SignedInUser signedInUser) {
        mActivity = activity;
        mSignedInUser = signedInUser;
    }

    public SwitchUsersAsync(Activity activity, SwitchUsersAsyncCallbacks callbacks) {
        mActivity = activity;
        mCallbacks = callbacks;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(mCallbacks != null) {
            mCallbacks.preExecute();
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if(mActivity == null) return false;

        return ((ApplicationManager) mActivity.getApplication()).switchUsers();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if(mCallbacks != null) {
            mCallbacks.postExecute();
        }

        if(mActivity == null) return;

        if (result) {
            File exCacheDir = Utils.getAttachmentsDirectory(mActivity);
            //need to delete the contents of the external cache folder so previous user's results don't show up on incorrect user
            com.instructure.canvasapi.utilities.FileUtilities.deleteAllFilesInDirectory(exCacheDir);

            if(mSignedInUser == null) {
                startNewActivity();
            } else {
                CookieSyncManager.createInstance(mActivity);
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.removeAllCookie();

                APIHelpers.setProtocol(mSignedInUser.protocol, mActivity);
                APIHelpers.setCacheUser(mActivity, mSignedInUser.user);
                CanvasRestAdapter.setupInstance(mActivity, mSignedInUser.token, mSignedInUser.domain);

                //Set previously signed in domain.
                OAuthWebLogin.setLastSignedInDomain(mSignedInUser.domain, mActivity);
                OAuthWebLogin.setCalendarFilterPrefs(mSignedInUser.calendarFilterPrefs, mActivity);

                startNewActivity();
            }
            mActivity.sendBroadcast(new Intent(CanvasWidgetProvider.REFRESH_ALL));
            CanvasRecipientManager.getInstance(mActivity).clearCache();
        } else {
            Toast.makeText(mActivity, R.string.noDataConnection, Toast.LENGTH_SHORT).show();
        }
    }

    private void startNewActivity() {
        Intent intent = LoginActivity.createIntent(mActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mActivity.startActivity(intent);
        mActivity.finish();
    }


}

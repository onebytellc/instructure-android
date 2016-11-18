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

package com.instructure.parentapp.asynctask;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.instructure.canvasapi2.utils.FileUtils;
import com.instructure.pandautils.utils.Utils;
import com.instructure.parentapp.R;
import com.instructure.parentapp.activity.MainActivity;
import com.instructure.parentapp.util.ApplicationManager;

import java.io.File;

public class LogoutAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private Activity mParentActivity;
    private String mMessageToUser;

    public LogoutAsyncTask(Activity parentActivity, String messageToUser) {
        mParentActivity = parentActivity;
        mMessageToUser = messageToUser;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (mParentActivity == null) return false;

        return ((ApplicationManager) mParentActivity.getApplication()).logoutUser();
    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (mParentActivity == null) return;

        if (result) {
            File cacheDir = new File(mParentActivity.getFilesDir(), "cache");
            File exCacheDir = Utils.getAttachmentsDirectory(mParentActivity);
            //remove the cached stuff for masqueraded user
            File masqueradeCacheDir = new File(mParentActivity.getFilesDir(), "cache_masquerade");
            //need to delete the contents of the internal cache folder so previous user's results don't show up on incorrect user
            //need to delete the contents of the external cache folder so previous user's results don't show up on incorrect user
            FileUtils.deleteAllFilesInDirectory(masqueradeCacheDir);
            FileUtils.deleteAllFilesInDirectory(cacheDir);
            FileUtils.deleteAllFilesInDirectory(exCacheDir);

            //TODO: this MOST LIKELY won't work if there are items on the back-stack.
            Intent intent;
            if(mMessageToUser != null){
                intent = MainActivity.createIntent(mParentActivity, true, mMessageToUser);
            } else {
                intent = MainActivity.createIntent(mParentActivity);
            }

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

            mParentActivity.startActivity(intent);
            mParentActivity.finish();

        } else {
            Toast.makeText(mParentActivity, R.string.noDataConnection, Toast.LENGTH_SHORT).show();
        }
    }
}
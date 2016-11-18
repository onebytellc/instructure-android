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

package com.instructure.candroid.util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.widget.Toast;

import com.instructure.candroid.R;
import com.instructure.candroid.activity.NavigationActivity;
import com.instructure.candroid.fragment.ParentFragment;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.canvasapi.utilities.Masquerading;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class MasqueradingCallbackUtil {

    public static CanvasCallback<User> getMasqueradingCallback(final ParentFragment parentFragment){
        final Activity context = parentFragment.getActivity();
        return new CanvasCallback<User>(APIHelpers.statusDelegateWithContext(context)) {
            @Override
            public void cache(User user) {}

            @Override
            public void firstPage(User user, LinkHeaders linkHeaders, Response response) {

                //Make sure we got a valid user back.
                if(user != null && user.getId() > 0) {

                    APIHelpers.setCacheUser(context, user);

                    //totally restart the app so the masquerading will apply
                    Intent mStartActivity = new Intent(context, NavigationActivity.getStartActivityClass());
                    PendingIntent mPendingIntent = PendingIntent.getActivity(context, Const.MASQUERADING_PENDING_INTENT_ID,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                    System.exit(0);
                }
                else{
                    onFailure(null);
                }
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                Masquerading.stopMasquerading(context);
                Toast.makeText(context, R.string.masqueradeFail, Toast.LENGTH_SHORT).show();
                return true;
            }
        };
    }

}

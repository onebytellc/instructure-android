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

package com.instructure.parentapp.utils;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;

import com.instructure.parentapp.asynctask.LogoutAsyncTask;

import static android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;

public abstract class Reset {

    public static void run(ActivityTestRule activityRule) {
        final Activity activity = activityRule.getActivity();
        new LogoutAsyncTask(activity, "").execute();

        // Unlock & wake up via WindowManager flags
        activity.runOnUiThread(new Runnable() {
            public void run() {
                activity.getWindow().addFlags(FLAG_DISMISS_KEYGUARD |
                        FLAG_TURN_SCREEN_ON |
                        FLAG_SHOW_WHEN_LOCKED |
                        FLAG_KEEP_SCREEN_ON);
            }
        });

    }
}

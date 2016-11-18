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

package com.instructure.candroid.test;

import android.support.test.runner.AndroidJUnit4;

import com.instructure.candroid.activity.LoginActivity;
import com.instructure.candroid.test.page.PageObjects;
import com.instructure.espresso.Device;
import com.instructure.espresso.ScreenshotActivityTestRule;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;


@RunWith(AndroidJUnit4.class)
public class DeviceTest extends PageObjects {
    @Rule
    public ScreenshotActivityTestRule<LoginActivity> mActivityRule =
            new ScreenshotActivityTestRule<>(LoginActivity.class);

    @Test
    public void checkAnimationsOff() {
        Device.verifyAnimationsDisabled();
    }
}

/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.pandautils.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;

import com.instructure.canvasapi.utilities.APIHelpers;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Utils {

    /**
     * Check if the device has a camera. If it doesn't, return false
     * and show a crouton so the user can see something
     *
     */
    public static boolean hasCameraAvailable(Activity activity) {
        PackageManager pm = activity.getPackageManager();

        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA) || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return true;
        }
        return false;
    }

    public static File getAttachmentsDirectory(Context context) {
        File file;
        if (context.getExternalCacheDir() != null) {
            file = new File(context.getExternalCacheDir(), "attachments");
        } else {
            file = context.getFilesDir();
        }
        return file;
    }

    public static boolean isAmazonDevice() {
        String manufacture = Build.MANUFACTURER.toLowerCase(Locale.getDefault());
        String brand = Build.BRAND.toLowerCase(Locale.getDefault());
        String board = Build.BOARD.toLowerCase(Locale.getDefault());
        String device = Build.DEVICE.toLowerCase(Locale.getDefault());

        if(!TextUtils.isEmpty(manufacture) && manufacture.contains("amazon")) {
            return true;
        }

        if(!TextUtils.isEmpty(brand) && brand.contains("amazon")) {
            return true;
        }

        if(!TextUtils.isEmpty(board) && board.contains("amazon")) {
            return true;
        }

        if(!TextUtils.isEmpty(device) && device.contains("amazon")) {
            return true;
        }

        return false;
    }

    public static boolean isNetworkAvailable(Context context) {
        if(context == null) return false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * The fromHTML method can cause a character that looks like [obj]
     * to show up. This is undesired behavior most of the time.
     *
     * Replace the [obj] with an empty space
     * [obj] is char 65532 and an empty space is char 32
     * @param sequence The fromHTML typically
     * @return The modified charSequence
     */
    public static String simplifyHTML(CharSequence sequence) {
        if(sequence != null) {
            CharSequence toReplace = sequence;
            toReplace = toReplace.toString().replace(((char) 65532), (char) 32).trim();
            return toReplace.toString();
        }
        return "";
    }

    public static Map<String, String> getReferer(Context context){
        Map<String, String> extraHeaders = new HashMap<String, String>();
        extraHeaders.put("Referer", APIHelpers.getDomain(context));

        return extraHeaders;
    }

    /**
     * Sets a views content description based on the build flavor
     * 
     * @param view view to be set
     * @param testDescription String for testers, standard is text_#position
     * @param a11yDescription String to display for a11y
     * @param isDebug BuildConfig.DEBUG
     */
    public static void testSafeContentDescription(View view, String testDescription, String a11yDescription, boolean isDebug) {
        if(isDebug) {
            view.setContentDescription(testDescription);
        } else {
            view.setContentDescription(a11yDescription);
        }
    }
}

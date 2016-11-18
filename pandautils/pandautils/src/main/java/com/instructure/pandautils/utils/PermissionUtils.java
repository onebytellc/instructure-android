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

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

public class PermissionUtils {

    public static final int PERMISSION_REQUEST_CODE = 78;
    public static final int WRITE_FILE_PERMISSION_REQUEST_CODE = 98;

    public static final String LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String CAMERA = Manifest.permission.CAMERA;
    public static final String RECORD_AUDIO = Manifest.permission.RECORD_AUDIO;

    /**
     * Checks to see if we have the necessary permissions.
     * @param activity A context in the form of an activity
     * @param permissions A string of permissions (we have hard coded values in PermissionUtils)
     * @return a boolean telling if the user has the necessary permissions
     */
    public static boolean hasPermissions(Activity activity, String...permissions) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean hasAllPermissions = true;
            for(String permission : permissions) {
                boolean hasPermission = activity.checkSelfPermission(permission) ==  PackageManager.PERMISSION_GRANTED;
                if(!hasPermission) {
                    hasAllPermissions = false;
                    break;
                }
            }
            return hasAllPermissions;
        }
        return true;
    }

    //Helper to make an array
    public static String[] makeArray(String...items) {
        return items;
    }

    /**
     * Returns a summary of weather all of the permissions were granted.
     * @param grantResults the array returned from onRequestPermissionsResult()
     * @return the result telling if all permissions were granted
     */
    public static boolean allPermissionsGrantedResultSummary(int[] grantResults) {
        boolean allPermissionsGranted = true;
        for(int i = 0; i < grantResults.length; i++) {
            if(grantResults[i] == PackageManager.PERMISSION_DENIED) {
                allPermissionsGranted = false;
                break;
            }
        }
        return allPermissionsGranted;
    }

    public static boolean permissionGranted(String[] permissions, int[] grantResults, String permission) {
        boolean permissionsGranted = false;

        if(permissions.length == grantResults.length && !TextUtils.isEmpty(permission)) {
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(permission)) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        permissionsGranted = true;
                    }
                }
            }
        }

        return permissionsGranted;
    }
}

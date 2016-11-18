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

package com.instructure.canvasapi2.managers;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.AlertThresholdAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.AlertThreshold;

import java.util.List;

import okhttp3.ResponseBody;


public class AlertThresholdManager extends BaseManager {

    private static boolean mTesting = false;

    public static void createAlertThreshold(String airwolfDomain, String parentId, String studentId, String alertType, StatusCallback<AlertThreshold> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();

            AlertThresholdAPI.createAlertThreshold(airwolfDomain, adapter, params, parentId, studentId, alertType, callback);
        }
    }

    public static void createAlertThreshold(String airwolfDomain, String parentId, String studentId, String alertType, String threshold, StatusCallback<AlertThreshold> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();

            AlertThresholdAPI.createAlertThreshold(airwolfDomain, adapter, params, parentId, studentId, alertType, threshold, callback);
        }
    }

    public static void getAlertThresholdsForStudent(String airwolfDomain, String parentId, String studentId, StatusCallback<List<AlertThreshold>> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();

            AlertThresholdAPI.getAlertThresholdsForStudent(airwolfDomain, adapter, params, parentId, studentId, callback);
        }
    }

    public static void updateAlertThreshold(String airwolfDomain, String parentId, String thresholdId, String alertType, String threshold, StatusCallback<AlertThreshold> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();

            AlertThresholdAPI.updateAlertThreshold(airwolfDomain, adapter, params, parentId, thresholdId, alertType, threshold, callback);
        }
    }

    public static void updateAlertThreshold(String airwolfDomain, String parentId, String thresholdId, String alertType, StatusCallback<AlertThreshold> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();

            AlertThresholdAPI.updateAlertThreshold(airwolfDomain, adapter, params, parentId, thresholdId, alertType, callback);
        }
    }

    public static void deleteAlertThreshold(String airwolfDomain, String parentId, String thresholdId, StatusCallback<ResponseBody> callback) {
        if (isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();

            AlertThresholdAPI.deleteAlertThreshold(airwolfDomain, adapter, params, parentId, thresholdId, callback);
        }
    }
}

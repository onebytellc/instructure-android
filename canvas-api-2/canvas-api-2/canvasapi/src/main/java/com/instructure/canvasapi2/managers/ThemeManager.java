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
import com.instructure.canvasapi2.apis.ThemeAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.CanvasTheme;
import com.instructure.canvasapi2.tests.ThemeManager_Test;


public class ThemeManager extends BaseManager {

    private static boolean mTesting = false;

    public static void getTheme(final StatusCallback<CanvasTheme> callback) {

        if(mTesting || isTesting()) {
            ThemeManager_Test.getTheme(callback);
        } else {

            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder().build();

            ThemeAPI.getTheme(adapter, callback, params);
        }
    }
}

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

import android.support.annotation.NonNull;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.AnnouncementAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.DiscussionTopicHeader;
import com.instructure.canvasapi2.tests.AnnouncementManager_Test;

import java.util.ArrayList;
import java.util.List;


public class AnnouncementManager extends DiscussionManager {

    private static final boolean mTesting = false;

    public static void getAnnouncements(long courseId, StatusCallback<List<DiscussionTopicHeader>> callback) {
        if(isTesting() || mTesting) {
            AnnouncementManager_Test.getAnnouncements(courseId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .build();

            AnnouncementAPI.getAnnouncements(courseId, adapter, callback, params);
        }
    }

    /**
     * Get announcements for the given context codes and date range. Currently this only works for
     * courses. All non-course context codes will be ignored.
     * @param contextCodes List of context_codes to retrieve announcements for (for example,
     *                     course_123). Only courses are presently supported and all non-course
     *                     context codes will be ignored.
     * @param startDate Only return announcements posted since the start_date (inclusive). May be null,
     *                  defaults to 14 days ago. The value should be formatted as: yyyy-mm-dd
     *                  or ISO 8601 YYYY-MM-DDTHH:MM:SSZ.
     * @param endDate Only return announcements posted before the end_date (inclusive). May be null,
     *                defaults to 28 days from start_date. The value should be formatted as: yyyy-mm-dd
     *                or ISO 8601 YYYY-MM-DDTHH:MM:SSZ. Announcements scheduled for future posting
     *                will only be returned to course administrators.
     * @param params RestParams
     * @param callback StatusCallback
     */
    public static void getAnnouncements(@NonNull List<String> contextCodes, String startDate, String endDate, @NonNull RestParams params, @NonNull StatusCallback<List<DiscussionTopicHeader>> callback) {
        // Use only course context codes
        ArrayList<String> courseContextCodes = new ArrayList<>();
        for (String code : contextCodes) {
            if (code != null && code.startsWith("course")) {
                courseContextCodes.add(code);
            }
        }

        //noinspection PointlessBooleanExpression
        if(isTesting() || mTesting) {
            AnnouncementManager_Test.getAnnouncements(courseContextCodes, startDate, endDate, callback, params);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            AnnouncementAPI.getAnnouncements(courseContextCodes, startDate, endDate, adapter, callback, params);
        }
    }

    @Override
    public boolean isAnnouncement() {
        return true;
    }
}

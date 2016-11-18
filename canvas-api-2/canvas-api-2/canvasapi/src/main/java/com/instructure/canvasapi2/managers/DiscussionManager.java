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

import android.support.annotation.Nullable;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.DiscussionAPI;
import com.instructure.canvasapi2.builders.RXRestBuilder;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.DiscussionEntry;
import com.instructure.canvasapi2.models.DiscussionTopic;
import com.instructure.canvasapi2.models.DiscussionTopicHeader;

import java.io.File;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import rx.Observable;


public class DiscussionManager extends BaseManager {

    private static boolean mTesting = false;

    private static boolean mIsAnnouncement = false;

    public Observable<Response<DiscussionTopicHeader>> createCourseDiscussion(long contextId, DiscussionTopicHeader newDiscussionHeader, @Nullable MultipartBody.Part attachment) {
        return DiscussionAPI.createCourseDiscussion(
                new RXRestBuilder(AppManager.getConfig()),
                new RestParams.Builder().withShouldIgnoreToken(false).build(),
                contextId,
                newDiscussionHeader,
                attachment
        );
    }

    public static void getDiscussionTopicHeaders(long contextId, StatusCallback<List<DiscussionTopicHeader>> callback) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(StatusCallback.isFirstPage(callback.getLinkHeaders()))
                    .build();

            DiscussionAPI.getDiscussionTopicHeaders(contextId, adapter, callback, params);
        }
    }

    public static void getFullDiscussionTopic(CanvasContext canvasContext, long topicId, StatusCallback<DiscussionTopic> callback) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            DiscussionAPI.getFullDiscussionTopic(adapter, canvasContext, topicId, callback, new RestParams.Builder().build());
        }
    }

    public static void getDetailedDiscussion(CanvasContext canvasContext, long topicId, StatusCallback<DiscussionTopicHeader> callback) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            DiscussionAPI.getDetailedDiscussion(adapter, canvasContext, topicId, callback, new RestParams.Builder().build());
        }
    }

    public static void rateDiscussionEntry(CanvasContext canvasContext, long topicId, long entryId, int rating, StatusCallback<Void> callback) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            DiscussionAPI.rateDiscussionEntry(adapter, canvasContext, topicId, entryId, rating, callback, new RestParams.Builder().build());
        }
    }

    @Nullable
    public static Response<Void> rateDiscussionEntrySynchronously(CanvasContext canvasContext, long topicId, long entryId, int rating) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig());
            return DiscussionAPI.rateDiscussionEntrySynchronously(adapter, canvasContext, topicId, entryId, rating, new RestParams.Builder().build());
        }
        return null;
    }

    public static void markDiscussionTopicEntryRead(CanvasContext canvasContext, long topicId, long entryId, StatusCallback<Void> callback) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            DiscussionAPI.markDiscussionTopicEntryRead(adapter, canvasContext, topicId, entryId, callback, new RestParams.Builder().build());
        }
    }

    @Nullable
    public static Response<Void> markDiscussionTopicEntryReadSynchronously(CanvasContext canvasContext, long topicId, long entryId) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig());
            return DiscussionAPI.markDiscussionTopicEntryReadSynchronously(adapter, canvasContext, topicId, entryId, new RestParams.Builder().build());
        }
        return null;
    }

    public static void markAllDiscussionTopicEntriesRead(CanvasContext canvasContext, long topicId, StatusCallback<Void> callback) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            DiscussionAPI.markAllDiscussionTopicEntriesRead(adapter, canvasContext, topicId, callback, new RestParams.Builder().build());
        }
    }

    @Nullable
    public static Response<Void> markAllDiscussionTopicEntriesReadSynchronously(CanvasContext canvasContext, long topicId) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig());
            return DiscussionAPI.markAllDiscussionTopicEntriesReadSynchronously(adapter, canvasContext, topicId, new RestParams.Builder().build());
        }
        return null;
    }

    public static void getDiscussionEntries(CanvasContext canvasContext, long topicId, StatusCallback<List<DiscussionEntry>> callback) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder().withPerPageQueryParam(true).build();

            DiscussionAPI.getDiscussionEntries(adapter, canvasContext, topicId, callback, params);
        }
    }

    public static void replyToDiscussionEntry(CanvasContext canvasContext, long topicId, long entryId, String message, StatusCallback<DiscussionEntry> callback) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder().build();

            DiscussionAPI.replyToDiscussionEntry(adapter, canvasContext, topicId, entryId, message, callback, params);
        }
    }

    public static void replyToDiscussionEntry(CanvasContext canvasContext, long topicId, long entryId, String message, File attachment, StatusCallback<DiscussionEntry> callback) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder().build();

            DiscussionAPI.replyToDiscussionEntryWithAttachment(adapter, canvasContext, topicId, entryId, message, attachment, callback, params);
        }
    }

    public static void postToDiscussionTopic(CanvasContext canvasContext, long topicId, String message, StatusCallback<DiscussionEntry> callback) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder().build();

            DiscussionAPI.postToDiscussionTopic(adapter, canvasContext, topicId, message, callback, params);
        }
    }

    public static void postToDiscussionTopic(CanvasContext canvasContext, long topicId, String message, File attachment, StatusCallback<DiscussionEntry> callback) {
        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder().build();

            DiscussionAPI.postToDiscussionTopicWithAttachment(adapter, canvasContext, topicId, message, attachment, callback, params);
        }
    }

    public boolean isAnnouncement() {
        return mIsAnnouncement;
    }

    public static void getDetailedDiscussionAirwolf(String airwolfDomain, String parentId, String studentId, String courseId, String discussionTopicId, StatusCallback<DiscussionTopicHeader> callback) {

        if(isTesting() || mTesting) {
            //TODO:
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();

            DiscussionAPI.getDetailedDiscussionAirwolf(adapter, parentId, studentId, courseId, discussionTopicId, callback, params);
        }
    }
}

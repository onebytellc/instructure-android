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

package com.instructure.canvasapi2.apis;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RXRestBuilder;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.DiscussionEntry;
import com.instructure.canvasapi2.models.DiscussionTopic;
import com.instructure.canvasapi2.models.DiscussionTopicHeader;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class DiscussionAPI {

    interface DiscussionInterface {
        @Multipart
        @POST("courses/{contextId}/discussion_topics")
        Observable<Response<DiscussionTopicHeader>> createCourseDiscussion(
                @Path("contextId") long contextId,
                @Part("title") String title,
                @Part("message") String message,
                @Part("is_announcement") boolean isAnnouncement,
                @Part("delayed_post_at") String delayedPostAt,
                @Part("published") boolean isPublished,
                @Part MultipartBody.Part attachment);

        @GET("courses/{contextId}/discussion_topics")
        Call<List<DiscussionTopicHeader>> getFirstPageDiscussionTopicHeaders(@Path("contextId") long contextId);

        @GET("{contextType}/{contextId}/discussion_topics/{topicId}")
        Call<DiscussionTopicHeader> getDetailedDiscussion(@Path("contextType") String contextType, @Path("contextId") long contextId, @Path("topicId") long topicId);

        @GET("{contextType}/{contextId}/discussion_topics/{topicId}/entries")
        Call<List<DiscussionEntry>> getDiscussionEntries(@Path("contextType") String contextType, @Path("contextId") long contextId, @Path("topicId") long topicId);

        @GET("{contextType}/{contextId}/discussion_topics/{topicId}/view")
        Call<DiscussionTopic> getFullDiscussionTopic(@Path("contextType") String contextType, @Path("contextId") long contextId, @Path("topicId") long topicId);

        @POST("{contextType}/{contextId}/discussion_topics/{topicId}/entries/{entryId}/rating")
        Call<Void> rateDiscussionEntry(@Path("contextType") String contextType, @Path("contextId") long contextId, @Path("topicId") long topicId, @Path("entryId") long entryId, @Query("rating") int rating);

        @PUT("{contextType}/{contextId}/discussion_topics/{topicId}/read_all")
        Call<Void> markDiscussionTopicEntriesRead(@Path("contextType") String contextType, @Path("contextId") long contextId, @Path("topicId") long topicId);

        @PUT("{contextType}/{contextId}/discussion_topics/{topicId}/entries/{entryId}/read")
        Call<Void> markDiscussionTopicEntryRead(@Path("contextType") String contextType, @Path("contextId") long contextId, @Path("topicId") long topicId, @Path("entryId") long entryId);

        @Multipart
        @POST("{contextType}/{contextId}/discussion_topics/{topicId}/entries/{entryId}/replies")
        Call<DiscussionEntry> postDiscussionReply(@Path("contextType") String contextType, @Path("contextId") long contextId, @Path("topicId") long topicId, @Path("entryId") long entryId, @Part("message") RequestBody message);

        @Multipart
        @POST("{contextType}/{contextId}/discussion_topics/{topicId}/entries/{entryId}/replies")
        Call<DiscussionEntry> postDiscussionReplyWithAttachment(@Path("contextType") String contextType, @Path("contextId") long contextId,
                                                                @Path("topicId") long topicId, @Path("entryId") long entryId,
                                                                @Part("message") RequestBody message, @Part MultipartBody.Part attachment);

        @Multipart
        @POST("{contextType}/{contextId}/discussion_topics/{topicId}/entries")
        Call<DiscussionEntry> postDiscussionEntry(@Path("contextType") String contextType, @Path("contextId") long contextId, @Path("topicId") long topicId, @Part("message") RequestBody message);

        @Multipart
        @POST("{contextType}/{contextId}/discussion_topics/{topicId}/entries")
        Call<DiscussionEntry> postDiscussionEntryWithAttachment(@Path("contextType") String contextType, @Path("contextId") long contextId,
                                                                @Path("topicId") long topicId, @Part("message") RequestBody message, @Part MultipartBody.Part attachment);

        @GET
        Call<List<DiscussionTopicHeader>> getNextPage(@Url String nextUrl);

        @GET
        Call<List<DiscussionEntry>> getNextPageEntries(@Url String nextUrl);

        //region Airwolf

        @GET("canvas/{parentId}/{studentId}/courses/{courseId}/discussion_topics/{discussionTopicId}")
        Call<DiscussionTopicHeader> getDetailedDiscussionAirwolf(@Path("parentId") String parentId, @Path("studentId") String studentId, @Path("courseId") String courseId, @Path("discussionTopicId") String discussionTopicId);

        //endregion
    }

    public static
    @Nullable
    Observable<Response<DiscussionTopicHeader>> createCourseDiscussion(@NonNull RXRestBuilder adapter, @NonNull RestParams params, long courseId, DiscussionTopicHeader newDiscussionHeader, @Nullable MultipartBody.Part attachment) {
        return adapter.build(DiscussionInterface.class, params)
                .createCourseDiscussion(
                        courseId,
                        newDiscussionHeader.getTitle(),
                        newDiscussionHeader.getMessage(),
                        newDiscussionHeader.isAnnouncement(),
                        newDiscussionHeader.getDelayedPostAt() == null ? null : newDiscussionHeader.getDelayedPostAt().toString(),
                        newDiscussionHeader.isPublished(),
                        attachment)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static void getDiscussionTopicHeaders(long contextId, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<DiscussionTopicHeader>> callback, @NonNull RestParams params) {
        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(DiscussionInterface.class, params).getFirstPageDiscussionTopicHeaders(contextId)).enqueue(callback);
        } else if (StatusCallback.moreCallsExist(callback.getLinkHeaders()) && callback.getLinkHeaders() != null) {
            callback.addCall(adapter.build(DiscussionInterface.class, params).getNextPage(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }

    public static void getFullDiscussionTopic(@NonNull RestBuilder adapter, CanvasContext canvasContext, long topicId, StatusCallback<DiscussionTopic> callback, @NonNull RestParams params) {
        final String contextType = CanvasContext.getApiContext(canvasContext);
        callback.addCall(adapter.build(DiscussionInterface.class, params).getFullDiscussionTopic(contextType, canvasContext.getId(), topicId)).enqueue(callback);
    }

    public static void getDetailedDiscussion(@NonNull RestBuilder adapter, CanvasContext canvasContext, long topicId, StatusCallback<DiscussionTopicHeader> callback, @NonNull RestParams params) {
        final String contextType = CanvasContext.getApiContext(canvasContext);
        callback.addCall(adapter.build(DiscussionInterface.class, params).getDetailedDiscussion(contextType, canvasContext.getId(), topicId)).enqueue(callback);
    }

    public static void replyToDiscussionEntry(@NonNull RestBuilder adapter, CanvasContext canvasContext, long topicId, long entryId, String message, StatusCallback<DiscussionEntry> callback, @NonNull RestParams params) {
        final String contextType = CanvasContext.getApiContext(canvasContext);

        RequestBody messagePart = RequestBody.create(MediaType.parse("multipart/form-data"), message);
        callback.addCall(adapter.build(DiscussionInterface.class, params).postDiscussionReply(contextType, canvasContext.getId(), topicId, entryId, messagePart)).enqueue(callback);
    }

    public static void replyToDiscussionEntryWithAttachment(@NonNull RestBuilder adapter, CanvasContext canvasContext, long topicId, long entryId,
                                                            String message, File attachment, StatusCallback<DiscussionEntry> callback, @NonNull RestParams params) {
        final String contextType = CanvasContext.getApiContext(canvasContext);

        RequestBody messagePart = RequestBody.create(MediaType.parse("multipart/form-data"), message);

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), attachment);
        MultipartBody.Part attachmentPart = MultipartBody.Part.createFormData("attachment", attachment.getName(), requestFile);

        callback.addCall(adapter.build(DiscussionInterface.class, params).postDiscussionReplyWithAttachment(contextType, canvasContext.getId(), topicId, entryId, messagePart, attachmentPart)).enqueue(callback);
    }

    public static void postToDiscussionTopic(@NonNull RestBuilder adapter, CanvasContext canvasContext, long topicId, String message, StatusCallback<DiscussionEntry> callback, @NonNull RestParams params) {
        final String contextType = CanvasContext.getApiContext(canvasContext);

        RequestBody messagePart = RequestBody.create(MediaType.parse("multipart/form-data"), message);
        callback.addCall(adapter.build(DiscussionInterface.class, params).postDiscussionEntry(contextType, canvasContext.getId(), topicId, messagePart)).enqueue(callback);
    }

    public static void postToDiscussionTopicWithAttachment(@NonNull RestBuilder adapter, CanvasContext canvasContext, long topicId, String message, File attachment, StatusCallback<DiscussionEntry> callback, @NonNull RestParams params) {
        final String contextType = CanvasContext.getApiContext(canvasContext);

        RequestBody messagePart = RequestBody.create(MediaType.parse("multipart/form-data"), message);

        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), attachment);
        MultipartBody.Part attachmentPart = MultipartBody.Part.createFormData("attachment", attachment.getName(), requestFile);

        callback.addCall(adapter.build(DiscussionInterface.class, params).postDiscussionEntryWithAttachment(contextType, canvasContext.getId(), topicId, messagePart, attachmentPart)).enqueue(callback);
    }

    public static void rateDiscussionEntry(@NonNull RestBuilder adapter, CanvasContext canvasContext, long topicId, long entryId, int rating, StatusCallback<Void> callback, @NonNull RestParams params) {
        final String contextType = CanvasContext.getApiContext(canvasContext);
        callback.addCall(adapter.build(DiscussionInterface.class, params).rateDiscussionEntry(contextType, canvasContext.getId(), topicId, entryId, rating)).enqueue(callback);
    }

    public static Response<Void> rateDiscussionEntrySynchronously(@NonNull RestBuilder adapter, CanvasContext canvasContext, long topicId, long entryId, int rating, @NonNull RestParams params) {
        final String contextType = CanvasContext.getApiContext(canvasContext);
        try {
            return adapter.build(DiscussionInterface.class, params).rateDiscussionEntry(contextType, canvasContext.getId(), topicId, entryId, rating).execute();
        } catch (IOException e) {
            return null;
        }
    }

    public static void markAllDiscussionTopicEntriesRead(@NonNull RestBuilder adapter, @NonNull CanvasContext canvasContext, long topicId, StatusCallback<Void> callback, @NonNull RestParams params) {
        final String contextType = CanvasContext.getApiContext(canvasContext);
        callback.addCall(adapter.build(DiscussionInterface.class, params).markDiscussionTopicEntriesRead(contextType, canvasContext.getId(), topicId)).enqueue(callback);
    }

    @Nullable
    public static Response<Void> markAllDiscussionTopicEntriesReadSynchronously(@NonNull RestBuilder adapter, @NonNull CanvasContext canvasContext, long topicId, @NonNull RestParams params) {
        final String contextType = CanvasContext.getApiContext(canvasContext);
        try {
            return adapter.build(DiscussionInterface.class, params).markDiscussionTopicEntriesRead(contextType, canvasContext.getId(), topicId).execute();
        } catch (IOException e) {
            return null;
        }
    }

    public static void markDiscussionTopicEntryRead(@NonNull RestBuilder adapter, @NonNull CanvasContext canvasContext, long topicId, long entryId, StatusCallback<Void> callback, @NonNull RestParams params) {
        final String contextType = CanvasContext.getApiContext(canvasContext);
        callback.addCall(adapter.build(DiscussionInterface.class, params).markDiscussionTopicEntryRead(contextType, canvasContext.getId(), topicId, entryId)).enqueue(callback);
    }

    @Nullable
    public static Response<Void> markDiscussionTopicEntryReadSynchronously(@NonNull RestBuilder adapter, @NonNull CanvasContext canvasContext, long topicId, long entryId, @NonNull RestParams params) {
        final String contextType = CanvasContext.getApiContext(canvasContext);
        try {
            return adapter.build(DiscussionInterface.class, params).markDiscussionTopicEntryRead(contextType, canvasContext.getId(), topicId, entryId).execute();
        } catch (IOException e) {
            return null;
        }
    }

    public static void getDiscussionEntries(@NonNull RestBuilder adapter, CanvasContext canvasContext, long topicId, StatusCallback<List<DiscussionEntry>> callback, @NonNull RestParams params) {
        final String contextType = CanvasContext.getApiContext(canvasContext);

        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            callback.addCall(adapter.build(DiscussionInterface.class, params).getDiscussionEntries(contextType, canvasContext.getId(), topicId)).enqueue(callback);
        } else if (StatusCallback.moreCallsExist(callback.getLinkHeaders()) && callback.getLinkHeaders() != null) {
            callback.addCall(adapter.build(DiscussionInterface.class, params).getNextPageEntries(callback.getLinkHeaders().nextUrl)).enqueue(callback);
        }
    }

    public static void getDetailedDiscussionAirwolf(
            @NonNull RestBuilder adapter,
            @NonNull String parentId,
            @NonNull String studentId,
            @NonNull String courseId,
            @NonNull String discussionTopicId,
            @NonNull StatusCallback<DiscussionTopicHeader> callback,
            @NonNull RestParams params) {

        callback.addCall(adapter.build(DiscussionInterface.class, params).getDetailedDiscussionAirwolf(parentId, studentId, courseId, discussionTopicId)).enqueue(callback);
    }
}
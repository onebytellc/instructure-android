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
import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.utils.LinkHeaders;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class ConversationAPI {

    public enum ConversationScope { ALL,UNREAD,ARCHIVED,STARRED,SENT }
    private static String conversationScopeToString(ConversationScope scope){
        if(scope == ConversationScope.UNREAD) {
            return "unread";
        } else if (scope == ConversationScope.STARRED) {
            return "starred";
        } else if (scope == ConversationScope.ARCHIVED) {
            return "archived";
        } else if (scope == ConversationScope.SENT) {
            return "sent";
        }
        return "";
    }

    public enum WorkflowState {READ,UNREAD,ARCHIVED}

    private static String conversationStateToString(WorkflowState state){
        if(state == WorkflowState.UNREAD) {
            return "unread";
        } else if (state == WorkflowState.READ) {
            return "read";
        } else if (state == WorkflowState.ARCHIVED) {
            return "archived";
        }
        return "";
    }

    interface ConversationsInterface {

        @GET("conversations/?interleave_submissions=1")
        Call<List<Conversation>> getConversations(@Query("scope") String scope);

        @GET
        Call<List<Conversation>> getNextPage(@Url String nextURL);

        @GET("conversations/?interleave_submissions=1")
        Observable<Response<List<Conversation>>> getFirstPageConversationList(@Query("scope") String scope);

        @GET
        Observable<Response<List<Conversation>>> getNextPageConversations(@Url String nextURL);

        @POST("conversations?group_conversation=true")
        Call<List<Conversation>> createConversation(@Query("recipients[]") List<String> recipients, @Query("body") String message, @Query("subject") String subject, @Query("context_code") String contextCode, @Query("attachment_ids[]") long[] attachmentIds, @Query("bulk_message") int isGroup);

        @GET("conversations/{conversationId}")
        Call<Conversation> getConversation(@Path("conversationId") long conversationId);

        @PUT("conversations/{conversationId}")
        Call<Conversation> updateConversation(@Path("conversationId") long conversationId, @Query("conversation[workflow_state]") String workflowState, @Query("conversation[starred]") Boolean isStarred);

        @DELETE("conversations/{conversationId}")
        Call<Conversation> deleteConversation(@Path("conversationId") long conversationId);

        @POST("conversations/{conversationId}/remove_messages")
        Call<Conversation> deleteMessages(@Path("conversationId") long conversationId, @Query("remove[]") List<Long> messageIds);

        @POST("conversations/{conversationId}/add_message?group_conversation=true")
        Call<Conversation> addMessage(@Path("conversationId") long conversationId, @Query("recipients[]") List<String> recipientIds, @Query("body") String body, @Query("included_messages[]") long[] includedMessageIds, @Query("attachment_ids[]") long[] attachmentIds);
    }

    public static void getConversation(@NonNull RestBuilder adapter, @NonNull StatusCallback<Conversation> callback, @NonNull RestParams params, long conversationId) {
        callback.addCall(adapter.build(ConversationsInterface.class, params).getConversation(conversationId)).enqueue(callback);
    }

    public static void getConversations(@NonNull ConversationScope scope, @NonNull RestBuilder adapter, @NonNull StatusCallback<List<Conversation>> callback, @NonNull RestParams params) {
        if (StatusCallback.isFirstPage(callback.getLinkHeaders())) {
            adapter.build(ConversationsInterface.class, params).getConversations(conversationScopeToString(scope)).enqueue(callback);
        } else if (StatusCallback.moreCallsExist(callback.getLinkHeaders()) && callback.getLinkHeaders() != null) {
            adapter.build(ConversationsInterface.class, params).getNextPage(callback.getLinkHeaders().nextUrl).enqueue(callback);
        }
    }

    public static
    @Nullable
    Observable<Response<List<Conversation>>> getConversations(@NonNull RXRestBuilder adapter, @NonNull RestParams params, ConversationScope scope, LinkHeaders... headers) {
        if (StatusCallback.isFirstPage(headers)) {
            return adapter.build(ConversationsInterface.class, params).getFirstPageConversationList(conversationScopeToString(scope))
                    .startWith(adapter.buildCache(ConversationsInterface.class, params).getFirstPageConversationList(conversationScopeToString(scope)))
                    .onErrorResumeNext(Observable.<Response<List<Conversation>>>empty())
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        } else if (StatusCallback.moreCallsExist(headers)) {
            return adapter.build(ConversationsInterface.class, params).getNextPageConversations(headers[0].nextUrl)
                    .startWith(adapter.buildCache(ConversationsInterface.class, params).getNextPageConversations(headers[0].nextUrl))
                    .onErrorResumeNext(Observable.<Response<List<Conversation>>>empty())
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
        return null;
    }

    public static void createConversation(@NonNull RestBuilder adapter, @NonNull RestParams params, ArrayList<String> userIDs, String message, String subject, String contextId, long[] attachmentIds, boolean isGroup, StatusCallback<List<Conversation>> callback) {
        //The message has to be sent to somebody.
        if(userIDs.size() == 0){ return; }

        callback.addCall(adapter.build(ConversationsInterface.class, params).createConversation(userIDs, message, subject, contextId, attachmentIds, isGroup ? 0 : 1)).enqueue(callback);
    }

    public static void updateConversation(@NonNull RestBuilder adapter, @NonNull StatusCallback<Conversation> callback, @NonNull RestParams params, long conversationId, @Nullable WorkflowState workflowState, @Nullable Boolean starred) {
        callback.addCall(adapter.build(ConversationsInterface.class, params).updateConversation(conversationId, conversationStateToString(workflowState), starred)).enqueue(callback);
    }

    public static void deleteConversation(@NonNull RestBuilder adapter, @NonNull StatusCallback<Conversation> callback, @NonNull RestParams params, long conversationId) {
        callback.addCall(adapter.build(ConversationsInterface.class, params).deleteConversation(conversationId)).enqueue(callback);
    }

    public static void deleteMessages(@NonNull RestBuilder adapter, @NonNull StatusCallback<Conversation> callback, @NonNull RestParams params, long conversationId, List<Long> messageIds) {
        callback.addCall(adapter.build(ConversationsInterface.class, params).deleteMessages(conversationId, messageIds)).enqueue(callback);
    }

    public static void addMessage(@NonNull RestBuilder adapter, @NonNull StatusCallback<Conversation> callback, @NonNull RestParams params, long conversationId, List<String> recipientIds, String message, long[] includedMessageIds, long[] attachmentIds) {
        callback.addCall(adapter.build(ConversationsInterface.class, params).addMessage(conversationId, recipientIds, message, includedMessageIds, attachmentIds)).enqueue(callback);
    }

}

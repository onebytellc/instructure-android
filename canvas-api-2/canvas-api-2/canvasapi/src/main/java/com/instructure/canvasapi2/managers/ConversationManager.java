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
import com.instructure.canvasapi2.apis.ConversationAPI;
import com.instructure.canvasapi2.builders.RXRestBuilder;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.Conversation;
import com.instructure.canvasapi2.tests.ConversationManager_Test;
import com.instructure.canvasapi2.utils.LinkHeaders;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import rx.Observable;


public class ConversationManager extends BaseManager {

    private static boolean mTesting = false;

    public void createConversation(ArrayList<String> userIDs, String message, String subject, String contextId, long[] attachmentIds, boolean isGroup, StatusCallback<List<Conversation>> callback) {
        if(isTesting() || mTesting) {
            //todo
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .build();

            ConversationManager_Test.createConversation(adapter, params, userIDs, message, subject, contextId, isGroup, callback);

        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .build();

            ConversationAPI.createConversation(adapter, params, userIDs, message, subject, contextId, attachmentIds, isGroup, callback);
        }
    }

    public static void getConversations(ConversationAPI.ConversationScope scope, boolean forceNetwork, StatusCallback<List<Conversation>> callback) {

        if(isTesting() || mTesting) {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            ConversationManager_Test.getConversations(scope, adapter, callback, params);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(true)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            ConversationAPI.getConversations(scope, adapter, callback, params);
        }
    }

    public static void getConversation(long conversationId, boolean forceNetwork, StatusCallback<Conversation> callback) {
        if (isTesting() || mTesting) {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();
            ConversationManager_Test.getConversation(adapter, callback, params, conversationId);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withShouldIgnoreToken(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();
            ConversationAPI.getConversation(adapter, callback, params, conversationId);
        }
    }

    public static void starConversation(long conversationId, boolean starred, StatusCallback<Conversation> callback) {
        if (isTesting() || mTesting) {
            ConversationManager_Test.updateConversation(conversationId, null, starred, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .build();
            ConversationAPI.updateConversation(adapter, callback, params, conversationId, null, starred);
        }
    }

    public static void archiveConversation(long conversationId, boolean archive, StatusCallback<Conversation> callback) {
        if (isTesting() || mTesting) {
            ConversationManager_Test.updateConversation(conversationId, archive ? ConversationAPI.WorkflowState.ARCHIVED : ConversationAPI.WorkflowState.READ, null, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .build();
            ConversationAPI.updateConversation(adapter, callback, params, conversationId, archive ? ConversationAPI.WorkflowState.ARCHIVED : ConversationAPI.WorkflowState.READ, null);
        }
    }

    public static void deleteConversation(long conversationId, StatusCallback<Conversation> callback) {
        if (isTesting() || mTesting) {
            ConversationManager_Test.deleteConversation(conversationId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .build();
            ConversationAPI.deleteConversation(adapter, callback, params, conversationId);
        }
    }

    public static void deleteMessages(long conversationId, List<Long> messageIds, StatusCallback<Conversation> callback) {
        if (isTesting() || mTesting) {
            ConversationManager_Test.deleteMessages(conversationId, messageIds, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .build();
            ConversationAPI.deleteMessages(adapter, callback, params, conversationId, messageIds);
        }
    }

    public static void addMessage(long conversationId, String message, List<String> recipientIds, long[] includedMessageIds, long[] attachmentIds, StatusCallback<Conversation> callback) {
        if (isTesting() || mTesting) {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .build();
            ConversationManager_Test.addMessage(adapter, callback, params, conversationId, recipientIds, message, includedMessageIds, attachmentIds);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .build();
            ConversationAPI.addMessage(adapter, callback, params, conversationId, recipientIds, message, includedMessageIds, attachmentIds);
        }
    }
}

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
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.apis.UserAPI;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.CanvasColor;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.FileUploadParams;
import com.instructure.canvasapi2.models.Parent;
import com.instructure.canvasapi2.models.ParentResponse;
import com.instructure.canvasapi2.models.RemoteFile;
import com.instructure.canvasapi2.models.ResetParent;
import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.tests.UserManager_Test;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;


public class UserManager extends BaseManager {

    private static boolean mTesting = false;

    public static void getColors(@NonNull StatusCallback<CanvasColor> callback) {
        RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
        RestParams params = new RestParams.Builder().withPerPageQueryParam(false).build();

        if(isTesting() || mTesting) {
            UserManager_Test.getColors(adapter, callback, params);
        } else {
            UserAPI.getColors(adapter, callback, params);
        }
    }

    public static void setColors(@NonNull StatusCallback<CanvasColor> callback, @NonNull String contextId, int color) {
        RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);

        if(isTesting() || mTesting) {
            UserManager_Test.setColors(adapter, callback, contextId, color);
        } else {
            UserAPI.setColor(adapter, callback, contextId, color);
        }
    }

    @Nullable
    @WorkerThread
    public static RemoteFile uploadUserFileSynchronous(String uploadUrl, LinkedHashMap<String, RequestBody> uploadParams, String mimeType, File file) throws IOException {
        RestBuilder adapter = new RestBuilder(AppManager.getConfig());
        if (isTesting() || mTesting) {
            return UserManager_Test.uploadUserFileSynchronous(adapter, uploadUrl, uploadParams, mimeType, file);
        } else {
            return UserAPI.uploadUserFileSynchronous(adapter, uploadUrl, uploadParams, mimeType, file);
        }
    }

    @Nullable
    @WorkerThread
    public static FileUploadParams getFileUploadParamsSynchronous(String fileName, long size, String contentType, Long parentFolderId) throws IOException {
        RestBuilder adapter = new RestBuilder(AppManager.getConfig());
        if (isTesting() || mTesting) {
            return UserManager_Test.getFileUploadParamsSynchronous(adapter, fileName, size, contentType, parentFolderId);
        } else {
            return UserAPI.getFileUploadParamsSynchronous(adapter, fileName, size, contentType, parentFolderId);
        }
    }

    @Nullable
    @WorkerThread
    public static FileUploadParams getFileUploadParamsSynchronous(String fileName, long size, String contentType, String parentFolderPath) throws IOException {
        RestBuilder adapter = new RestBuilder(AppManager.getConfig());
        if (isTesting() || mTesting) {
            return UserManager_Test.getFileUploadParamsSynchronous(adapter, fileName, size, contentType, parentFolderPath);
        } else {
            return UserAPI.getFileUploadParamsSynchronous(adapter, fileName, size, contentType, parentFolderPath);
        }
    }

    public static void getSelf(StatusCallback<User> callback) {
        if (isTesting() || mTesting) {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .build();
            UserManager_Test.getSelf(adapter, params, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .build();
            UserAPI.getSelf(adapter, params, callback );
        }
    }

    public static void getUser(String userId, StatusCallback<User> callback, boolean forceNetwork) {
        if (isTesting() || mTesting) {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();
            UserManager_Test.getUser(adapter, params, userId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();
            UserAPI.getUser(adapter, params, userId, callback);
        }
    }

    public static void getPeopleList(CanvasContext canvasContext, StatusCallback<List<User>> callback, boolean forceNetwork) {

        if (isTesting() || mTesting) {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            UserManager_Test.getPeopleList(adapter, params, canvasContext, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withPerPageQueryParam(false)
                    .withForceReadFromNetwork(forceNetwork)
                    .build();

            UserAPI.getPeopleList(adapter, params, canvasContext, callback);
        }
    }

    public static void addStudentToParentAirwolf(String airwolfDomain, String parentId, String studentDomain, StatusCallback<ResponseBody> callback) {

        if (isTesting() || mTesting) {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withDomain(airwolfDomain)
                    .withPerPageQueryParam(false)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();
            UserManager_Test.addStudentToParentAirwolf(airwolfDomain, adapter, params, parentId, studentDomain, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withDomain(airwolfDomain)
                    .withPerPageQueryParam(false)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();
            UserAPI.addStudentToParentAirwolf(adapter, params, parentId, studentDomain, callback);
        }
    }

    public static void removeStudentAirwolf(String airwolfDomain, String parentId, String studentId, StatusCallback<ResponseBody> callback) {

        if (isTesting() || mTesting) {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();
            UserManager_Test.removeStudentAirwolf(airwolfDomain, adapter, params, parentId, studentId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();
            UserAPI.removeStudentAirwolf(adapter, params, parentId, studentId, callback);
        }
    }

    public static void addParentAirwolf(String airwolfDomain, Parent parent, StatusCallback<ParentResponse> callback) {
        if (isTesting() || mTesting) {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();
            UserManager_Test.addParentAirwolf(airwolfDomain, adapter, params, parent, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();
            UserAPI.addParentAirwolf(adapter, params, parent, callback);
        }
    }

    public static void getStudentsForParentAirwolf(String airwolfDomain, String parentId, StatusCallback<List<Student>> callback) {
        if (isTesting() || mTesting) {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(true)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .build();
            UserManager_Test.getStudentsForParentAirwolf(airwolfDomain, adapter, params, parentId, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(true)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();
            UserAPI.getStudentsForParentAirwolf(adapter, params, parentId, callback);
        }
    }

    public static void sendPasswordResetForParentAirwolf(String airwolfDomain, String userName, StatusCallback<ResponseBody> callback) {
        if (isTesting() || mTesting) {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(true)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();
            UserManager_Test.sendPasswordResetForParentAirwolf(airwolfDomain, adapter, params, userName, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(true)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();
            UserAPI.sendPasswordResetForParentAirwolf(adapter, params, userName, callback);
        }
    }

    public static void authenticateParentAirwolf(String airwolfDomain, String email, String password, StatusCallback<ParentResponse> callback) {
        if (isTesting() || mTesting) {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();
            UserManager_Test.authenticateParentAirwolf(airwolfDomain, adapter, params, email, password, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();
            UserAPI.authenticateParentAirwolf(adapter, params, email, password, callback);
        }
    }

    public static void resetParentPasswordAirwolf(String airwolfDomain, String email, String password, StatusCallback<ResetParent> callback) {
        if (isTesting() || mTesting) {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();
            UserManager_Test.resetParentPassword(airwolfDomain, adapter, params, email, password, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();
            UserAPI.resetParentPassword(adapter, params, email, password, callback);
        }
    }

    public static void authenticateCanvasParentAirwolf(String airwolfDomain, String domain, StatusCallback<ParentResponse> callback) {
        if (isTesting() || mTesting) {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();
            UserManager_Test.authenticateCanvasParentAirwolf(domain, adapter, params, callback);
        } else {
            RestBuilder adapter = new RestBuilder(AppManager.getConfig(), callback);
            RestParams params = new RestParams.Builder()
                    .withShouldIgnoreToken(false)
                    .withPerPageQueryParam(false)
                    .withDomain(airwolfDomain)
                    .withAPIVersion("")
                    .withForceReadFromNetwork(true)
                    .build();
            UserAPI.authenticateCavnasParentAirwolf(adapter, params, domain, callback);
        }
    }
}
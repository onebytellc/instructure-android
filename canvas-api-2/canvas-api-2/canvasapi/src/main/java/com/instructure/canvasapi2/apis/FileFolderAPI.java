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

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.builders.RestBuilder;
import com.instructure.canvasapi2.builders.RestParams;
import com.instructure.canvasapi2.models.FileFolder;
import com.instructure.canvasapi2.utils.APIHelper;

import java.util.List;

import retrofit.RestAdapter;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;



public class FileFolderAPI {

    interface FilesFoldersInterface {
        @GET("{contextId}/folders/root")
        Call<FileFolder> getRootFolderForContext(@Path("contextId") long context_id);

        @GET("self/folders/root")
        Call<FileFolder> getRootUserFolder();

        @GET("folders/{folderId}/folders")
        Call<List<FileFolder>> getFirstPageFolders(@Path("folderId") long folder_id);

        @GET("folders/{folderId}/files")
        Call<List<FileFolder>> getFirstPageFiles(@Path("folderId") long folder_id);

        @GET("{fileUrl}")
        Call<FileFolder> getFileFolderFromURL(@Path(value = "fileUrl", encoded = false) String fileURL);

        @GET("{next}")
        Call<List<FileFolder>> getNextPageFileFoldersList(@Path(value = "next", encoded = false) String nextURL);

        @DELETE("files/{fileId}")
        Call<Response> deleteFile(@Path("fileId")long fileId);
    }

    public static void getFileFolderFromURL(@NonNull RestBuilder adapter, String url, StatusCallback<FileFolder> callback, @NonNull RestParams params) {
        if (APIHelper.paramIsNull(callback, url)) {
            return;
        }
        //TODO: add pagination
        callback.addCall(adapter.build(FilesFoldersInterface.class, params).getFileFolderFromURL(url)).enqueue(callback);
    }
}

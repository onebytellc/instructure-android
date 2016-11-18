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

package com.instructure.annotations_library;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class CanvasPDFCache {

    public static final String TAG = "SpeedGrader";

    private static final int DEFAULT_DISK_CACHE_MAX_SIZE_MB = 10;
    private static final int MEGABYTE = 1024 * 1024;
    private static final int DEFAULT_DISK_CACHE_SIZE = DEFAULT_DISK_CACHE_MAX_SIZE_MB * MEGABYTE;

    /**
     *  Application Version, you can define it by default otherwise you can
     * get it from Android Manifest
     */
    private static int APP_VERSION = 1;

    /**
     * There has to be only an instance of this class, that´s why we
     * use singleton pattern
     */

    private static CanvasPDFCache mInstance = null;

    /** SimpleDiskCache is an easy class to work with
     * JakeWharton DiskLruCache:
     * https://github.com/JakeWharton/DiskLruCache
     */
    private SimpleDiskCache mSimpleDiskCache;



    public static CanvasPDFCache getInstance(Context context){
        if(mInstance == null){
            mInstance = new CanvasPDFCache(context);
        }
        return mInstance;
    }

    //Constructor
    private CanvasPDFCache(Context context){
        try{
            final File diskCacheDir = getDiskCacheDir(context, TAG );
            mSimpleDiskCache = SimpleDiskCache.open(diskCacheDir, APP_VERSION, DEFAULT_DISK_CACHE_SIZE);
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
    }

    public String getKeyForUrl(String url){
        return mSimpleDiskCache.toInternalKey(url);
    }

    public void getInputStream(Context context, String url, FetchFileAsyncTask.FetchFileCallback callback){
        FetchFileAsyncTask.download(context, mSimpleDiskCache, url, callback);
    }

    /**
     * Check if media is mounted or storage is built-in, if so, try and use external cache dir
     * otherwise use internal cache dir
     * @param context
     * @param uniqueName
     * @return
     */
    private File getDiskCacheDir(Context context, String uniqueName) {
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable() ?
                        getPDFCacheDir(context).getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    public static boolean isExternalStorageRemovable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    public static File getPDFCacheDir(Context context) {
        if (hasExternalCacheDir()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

}
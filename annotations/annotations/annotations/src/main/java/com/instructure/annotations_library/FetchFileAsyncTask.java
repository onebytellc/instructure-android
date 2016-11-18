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
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class FetchFileAsyncTask extends AsyncTask<Void, Void, File> {

    private static final String LOG_TAG = "SpeedGrader.FetchTask";
    private final FetchFileCallback mCallback;
    private String mUrl;
    private final SimpleDiskCache mCache;
    private Context mContext;

    public interface FetchFileCallback {
        void onFileLoaded(File fileInputStream);
    }

    private FetchFileAsyncTask(Context context, SimpleDiskCache cache, String url, FetchFileCallback callback) {
        mCallback = callback;
        mUrl  = url;
        mCache = cache;
        mContext = context;
    }

    public static void download(Context context, SimpleDiskCache cache, String url, FetchFileCallback callback) {
        new FetchFileAsyncTask(context, cache, url, callback).execute();
    }

    @Override
    protected File doInBackground(Void... params) {
        try {
            SimpleDiskCache.InputStreamEntry entry =  mCache.getInputStream(mUrl);

            if(entry != null){
                return IOUtil.getFileFromInputStream(mContext, entry.getInputStream());
            }else{
                return downloadAndCacheFile(mUrl);
            }
        } catch (IOException e) {
            Log.d(LOG_TAG, "Download failed!" +mUrl, e);
            return null;
        }
    }

    private File downloadAndCacheFile( String downloadUrl){
        File result = null;
        InputStream remoteInputStream = null;
        OutputStream outputStream = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(downloadUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            //set up some things on the connection
            urlConnection.setRequestMethod("GET");

            //and connect!
            urlConnection.connect();
            connection = redirectURL(urlConnection);

            //this will be used in reading the uri from the internet
            remoteInputStream = new BufferedInputStream(connection.getInputStream());

            result = File.createTempFile(LOG_TAG, null, mContext.getCacheDir());

            outputStream = new FileOutputStream(result);
            outputStream.flush();
            IOUtil.copy(remoteInputStream, outputStream);

            // Add to cache
            mCache.put(downloadUrl, new FileInputStream(result));

        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, "File not Found Exception");
            e.printStackTrace();
        } catch (ProtocolException e) {
            Log.d(LOG_TAG, "ProtocolException : " + downloadUrl);
            e.printStackTrace();
        } catch (MalformedURLException e) {
            Log.d(LOG_TAG, "MalformedURLException" + downloadUrl);
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(LOG_TAG, "Failed to save inputStream to cache");
            e.printStackTrace();
        }finally {
            if (connection != null) {
                connection.disconnect();
            }
            IOUtil.closeStream(remoteInputStream);
            IOUtil.closeStream(outputStream);
        }
        return result;
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        if(mCallback != null){
            mCallback.onFileLoaded(file);
        }
    }

    public static HttpURLConnection redirectURL(HttpURLConnection urlConnection) {
        HttpURLConnection.setFollowRedirects(true);
        try {
            urlConnection.connect();

            String currentURL = urlConnection.getURL().toString();
            do
            {
                urlConnection.getResponseCode();
                currentURL = urlConnection.getURL().toString();
                urlConnection = (HttpURLConnection) new URL(currentURL).openConnection();
            }
            while (!urlConnection.getURL().toString().equals(currentURL));
        } catch(Exception E){}
        return urlConnection;
    }
}

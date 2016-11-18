/*
 * Copyright (C) 2016 - present  Instructure, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, version 3 of the License.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.instructure.candroid.util;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.instructure.candroid.R;

public class DownloadMedia {

	//Make sure that there is a download manager available.

	public static void downloadMedia(Context context, String url, String filenameForDownload, String downloadDescription) {

        //if downloadDescription is empty we can set the description as filename
        if(StringUtilities.isEmpty(url, filenameForDownload)){
            //let the user know something went wrong
            Toast.makeText(context, R.string.unexpectedErrorDownloadingFile, Toast.LENGTH_SHORT).show();
            return;
        }
		DownloadManager.Request request;

		//Some older phones don't support https downloading... 3.1 and older do I believe...
		try {
			request = new DownloadManager.Request(Uri.parse(url));
		} catch(Exception e) {
            // certain urls are crashing here. So temporarily we have this extra try/catch to log more
            // information so we can fix it
            try {
                request = new DownloadManager.Request(Uri.parse(url.replaceFirst("https://", "http://")));
            } catch (Exception e2) {
                Toast.makeText(context, R.string.unexpectedErrorDownloadingFile, Toast.LENGTH_SHORT).show();
                LoggingUtility.Log(context, Log.ERROR, "Can't download url: " + url);
                LoggingUtility.LogExceptionPlusCrashlytics(context, e2);
                return;
            }
		}

        if(!TextUtils.isEmpty(downloadDescription)) {
            request.setDescription(downloadDescription);
        }
        else {
            request.setDescription(filenameForDownload);

        }
		request.setTitle(filenameForDownload);
		// in order for this if to run, you must use the android 3.2 to compile your app
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			request.allowScanningByMediaScanner();
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		}
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,filenameForDownload);

		// get download service and enqueue file
		DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		manager.enqueue(request);
	}
}

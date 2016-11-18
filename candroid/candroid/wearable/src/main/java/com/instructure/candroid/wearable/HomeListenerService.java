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

package com.instructure.candroid.wearable;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.Html;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.WearableListenerService;
import com.instructure.candroid.R;

/**
 * A {@link com.google.android.gms.wearable.WearableListenerService} service that is invoked upon
 * receiving a DataItem from the handset for session feedback notifications, or the dismissal of a
 * notification. Handset application creates a Data Item that will then trigger the invocation of
 * this service. That will result in creation of a wearable notification.
 */
public class HomeListenerService extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG ="HomeListenerService";
    private static final String GROUP_ID = "wearable_group";
    public static final String KEY_ID = "schedule-id";
    public static final String KEY_TITLE = "schedule-title";
    public static final String KEY_DESCRIPTION = "schedule-description";
    public static final String KEY_DATE = "schedule-date";
    public static final String KEY_NOTIFICATION_ID = "notification-id";
    public static final String KEY_COLOR = "schedule-color";


    public final static String ACTION_DISMISS
            = "com.instructure.candroid.ACTION_DISMISS";

    public static final String NOTIFICATION_PATH = "/wearable/notifications/";


    public final static int NOTIFICATION_ID = 10;


    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        return Service.START_NOT_STICKY;
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged: " + dataEvents + " for " + getPackageName());

        for (DataEvent event : dataEvents) {
            Uri uri = event.getDataItem().getUri();
            Log.d(TAG, "onDataChanged(): Received a data item change with uri: " + uri.toString());
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                if (uri.getPath().startsWith(NOTIFICATION_PATH)) {
                    setupNotification(event.getDataItem());
                }
            }
        }
    }

    /**
     * Builds notification for wear based on the data in the Data Item that is passed in.
     */
    private void setupNotification(DataItem dataItem) {
        Log.d(TAG, "setupNotification(): DataItem=" + dataItem.getUri());
        PutDataMapRequest putDataMapRequest = PutDataMapRequest
                .createFromDataMapItem(DataMapItem.fromDataItem(dataItem));
        final DataMap dataMap = putDataMapRequest.getDataMap();
        String id = dataMap.getString(KEY_ID);
        String title = dataMap.getString(KEY_TITLE);
        String description = dataMap.getString(KEY_DESCRIPTION);
        String date = dataMap.getString(KEY_DATE);
        int color = dataMap.getInt(KEY_COLOR);

        Log.d("NOTIFICATION", "Title " + title + " " + "Desc " + description + " " + "Date " + date);
        Intent intent = new Intent(ACTION_DISMISS);
        intent.putExtra(KEY_ID, dataMap.getString(KEY_ID));


        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
        bigStyle.bigText(getString(R.string.due) + " " + date + "\n\n" + Html.fromHtml(description));

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.triangle_background);


        //we need to create a copy of the bitmap because we'll be modifying it to tint the color
        Bitmap resultBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth() - 1, bitmap.getHeight() - 1);
        Paint p = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 1);
        p.setColorFilter(filter);

        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, p);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.canvas_logo)
                .setLargeIcon(resultBitmap)
                .setContentTitle(title)
                .setContentText(date)
                .setGroup(GROUP_ID)
                .extend(new NotificationCompat.WearableExtender()
                        .setBackground(resultBitmap))
                .setStyle(bigStyle);

        NotificationManagerCompat.from(this)
                .notify(id, NOTIFICATION_ID, builder.build());
    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
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

import android.content.Context;
import android.util.Log;

import com.instructure.canvasapi.model.CanvasError;

import retrofit.RetrofitError;

/**
 * Error delegate that ONLY shows croutons when there is a no network error.
 * All other cases just log.
 */
public class NoNetworkErrorDelegate extends CanvasErrorDelegate {

    @Override
    public void noNetworkError(RetrofitError error, Context context) {
        super.noNetworkError(error,context);
    }

    @Override
    public void invalidUrlError(RetrofitError error, Context context) {
        LoggingUtility.Log(context, Log.ERROR, "Invalid URL Error ( Status Code: " +error.getResponse().getStatus() +", URL: " +error.getUrl() +")");
    }

    @Override
    public void serverError(RetrofitError error, Context context) {
        LoggingUtility.Log(context, Log.ERROR, "Invalid URL Error ( Status Code: " +error.getResponse().getStatus() +", URL: " +error.getUrl() +")");
    }

    @Override
    public void generalError(RetrofitError error, CanvasError canvasError, Context context) {
        LoggingUtility.Log(context, Log.ERROR, "Invalid URL Error ( Status Code: " +error.getResponse().getStatus() +", URL: " +error.getUrl() +")");
    }
}

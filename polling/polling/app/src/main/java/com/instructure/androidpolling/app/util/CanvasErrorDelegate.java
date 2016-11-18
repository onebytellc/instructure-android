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

package com.instructure.androidpolling.app.util;

import android.content.Context;

import com.instructure.canvasapi.model.CanvasError;
import com.instructure.canvasapi.utilities.ErrorDelegate;

import retrofit.RetrofitError;

public class CanvasErrorDelegate implements ErrorDelegate {

    @Override
    public void noNetworkError(RetrofitError error, Context context) {

    }

    @Override
    public void notAuthorizedError(RetrofitError error, CanvasError canvasError, Context context) {

    }

    @Override
    public void invalidUrlError(RetrofitError error, Context context) {

    }

    @Override
    public void serverError(RetrofitError error, Context context) {

    }

    @Override
    public void generalError(RetrofitError error, CanvasError canvasError, Context context) {

    }
}

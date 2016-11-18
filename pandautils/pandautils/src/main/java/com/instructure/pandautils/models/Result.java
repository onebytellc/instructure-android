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

package com.instructure.pandautils.models;

import android.app.Activity;
import android.os.Bundle;

public class Result {
    private int resultCode;
    private Bundle data;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    public int getResultCode() {
        return resultCode;
    }
    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }
    public Bundle getData() {
        return data;
    }
    public void setData(Bundle data) {
        this.data = data;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Constructors and builders
    ///////////////////////////////////////////////////////////////////////////

    public Result(int resultCode, Bundle data) {
        this.resultCode = resultCode;
        this.data = data;
    }

    public static Result resultOk() {
        return new Result(Activity.RESULT_OK, null);
    }

    public static Result resultCanceled() {
        return new Result(Activity.RESULT_CANCELED, null);
    }
}

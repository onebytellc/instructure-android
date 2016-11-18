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

package com.instructure.canvasapi2;

import android.content.Context;
import android.support.annotation.NonNull;

import com.instructure.canvasapi2.builders.RestParams;

import java.io.File;


public interface CanvasConfig {

    int perPageCount();
    File cacheDir();
    @NonNull String fullDomain();
    @NonNull String token();
    @NonNull String protocol();
    @NonNull String domain();
    @NonNull String userAgent();
    @NonNull RestParams getParams();
    boolean isMasquerading();
    long masqueradingId();

    void setParams(RestParams params);
    void setToken(@NonNull String token);
    void setMasqueradingId(long id);
    void setIsMasquerading(boolean isMasquerading);

    void initConfig(@NonNull Context context);
}

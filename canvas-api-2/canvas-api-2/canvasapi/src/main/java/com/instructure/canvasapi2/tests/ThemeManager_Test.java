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

package com.instructure.canvasapi2.tests;

import com.google.gson.Gson;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.BaseManager;
import com.instructure.canvasapi2.models.CanvasTheme;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;


public class ThemeManager_Test extends BaseManager {

    public static void getTheme(final StatusCallback<CanvasTheme> callback) {
        CanvasTheme theme = new Gson().fromJson(json, CanvasTheme.class);

        retrofit2.Response<CanvasTheme> response = retrofit2.Response.success(theme);
        callback.onResponse(response, new LinkHeaders(), ApiType.CACHE);
    }

    private static final String json = "{\n" +
            "\"ic-brand-primary\": \"#6066db\",\n" +
            "\"ic-brand-button--primary-bgd\": \"#6066db\",\n" +
            "\"ic-brand-button--primary-text\": \"#ffffff\",\n" +
            "\"ic-brand-button--secondary-bgd\": \"#333333\",\n" +
            "\"ic-brand-button--secondary-text\": \"#ffffff\",\n" +
            "\"ic-link-color\": \"#0081bd\",\n" +
            "\"ic-brand-global-nav-bgd\": \"#be1c00\",\n" +
            "\"ic-brand-global-nav-ic-icon-svg-fill\": \"#00fdf9\",\n" +
            "\"ic-brand-global-nav-ic-icon-svg-fill--active\": \"#6066db\",\n" +
            "\"ic-brand-global-nav-menu-item__text-color\": \"#44f900\",\n" +
            "\"ic-brand-global-nav-menu-item__text-color--active\": \"#0081bd\",\n" +
            "\"ic-brand-global-nav-avatar-border\": \"#ffffff\",\n" +
            "\"ic-brand-global-nav-menu-item__badge-bgd\": \"#6066db\",\n" +
            "\"ic-brand-global-nav-menu-item__badge-text\": \"#ffffff\",\n" +
            "\"ic-brand-global-nav-logo-bgd\": \"#34444f\",\n" +
            "\"ic-brand-header-image\": \"https://instructure-uploads.s3.amazonaws.com/account_99298/attachments/85642422/Icons_1.png?AWSAccessKeyId=AKIAJFNFXH2V2O7RPCAA&Expires=1936450791&Signature=8oglFcVa4pawJAbuaSWBPfB3nJw%3D&response-cache-control=Cache-Control%3Amax-age%3D473364000.0%2C%20public&response-expires=473364000.0\",\n" +
            "\"ic-brand-watermark\": \"\",\n" +
            "\"ic-brand-watermark-opacity\": \"1\",\n" +
            "\"ic-brand-favicon\": \"/dist/images/favicon-e10d657a73.ico\",\n" +
            "\"ic-brand-apple-touch-icon\": \"/dist/images/apple-touch-icon-585e5d997d.png\",\n" +
            "\"ic-brand-msapplication-tile-color\": \"#6066db\",\n" +
            "\"ic-brand-msapplication-tile-square\": \"/dist/images/windows-tile-f2359ad914.png\",\n" +
            "\"ic-brand-msapplication-tile-wide\": \"/dist/images/windows-tile-wide-52212226d6.png\",\n" +
            "\"ic-brand-right-sidebar-logo\": \"\",\n" +
            "\"ic-brand-Login-body-bgd-color\": \"#34444f\",\n" +
            "\"ic-brand-Login-body-bgd-image\": \"\",\n" +
            "\"ic-brand-Login-body-bgd-shadow-color\": \"#182025\",\n" +
            "\"ic-brand-Login-logo\": \"/dist/images/login/canvas-logo-a66b946d8d.svg\",\n" +
            "\"ic-brand-Login-Content-bgd-color\": \"none\",\n" +
            "\"ic-brand-Login-Content-border-color\": \"none\",\n" +
            "\"ic-brand-Login-Content-inner-bgd\": \"none\",\n" +
            "\"ic-brand-Login-Content-inner-border\": \"none\",\n" +
            "\"ic-brand-Login-Content-inner-body-bgd\": \"none\",\n" +
            "\"ic-brand-Login-Content-inner-body-border\": \"none\",\n" +
            "\"ic-brand-Login-Content-label-text-color\": \"#ffffff\",\n" +
            "\"ic-brand-Login-Content-password-text-color\": \"#ffffff\",\n" +
            "\"ic-brand-Login-Content-button-bgd\": \"#0096db\",\n" +
            "\"ic-brand-Login-Content-button-text\": \"#ffffff\",\n" +
            "\"ic-brand-Login-footer-link-color\": \"#ffffff\",\n" +
            "\"ic-brand-Login-footer-link-color-hover\": \"#ffffff\",\n" +
            "\"ic-brand-Login-instructure-logo\": \"#ffffff\"\n" +
            "}";
}

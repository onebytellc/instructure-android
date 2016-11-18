package com.instructure.loginapi.login.api;

import android.content.Context;
import android.content.pm.PackageManager;

import com.instructure.canvasapi2.utils.APIHelper;

import java.util.HashMap;
import java.util.Map;

public class CanvasAPI {

    public static String getCandroidUserAgent(String userAgentString, Context context) {
        String userAgent;
        try {
            userAgent = userAgentString + "/" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName + " (" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            userAgent = userAgentString;
        }
        return userAgent;
    }

    public static Map<String, String> getAuthenticatedURL(Context context) {
        String token = APIHelper.getToken(context);
        String headerValue = null;
        if (token != null) {
            headerValue = String.format("Bearer %s", token);
        }
        Map<String, String> map = new HashMap<>();
        map.put("Authorization", headerValue);
        return map;
    }
}

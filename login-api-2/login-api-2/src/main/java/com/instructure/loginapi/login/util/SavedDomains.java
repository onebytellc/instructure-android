package com.instructure.loginapi.login.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.instructure.loginapi.login.OAuthWebLogin;
import com.instructure.loginapi.login.URLSignIn;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Nathan Button on 4/1/14.
 */
public class SavedDomains {
    /**
     * Retrieves the previously successful domains from saved preferences.
     *
     * @param context
     * @return
     */
    public static JSONArray getSavedDomains(Context context, String preferenceName) {
        SharedPreferences prefs = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        JSONArray domains = null;
        try {
            domains = new JSONArray(prefs.getString(URLSignIn.URL_ENTRIES, "[]"));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return domains;
    }

    /**
     * Saves the domain array to saved preferences using json array syntax.
     *
     * @param ctx
     * @param values
     * @return True if the values were successfully committed to storage.
     */
    public static boolean setSavedDomains(Context ctx, JSONArray values, String preferenceName) {
        SharedPreferences prefs = ctx.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(URLSignIn.URL_ENTRIES, values.toString());
        return editor.commit();
    }

}

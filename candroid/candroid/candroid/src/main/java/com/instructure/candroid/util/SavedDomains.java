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
import android.content.SharedPreferences;

import com.instructure.loginapi.login.URLSignIn;
import com.instructure.loginapi.login.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;

public class SavedDomains {
    /**
     * Retrieves the previously successful domains from saved preferences.
     *
     * @param context
     * @return
     */
    public static JSONArray getSavedDomains(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(ApplicationManager.PREF_NAME_PREVIOUS_DOMAINS, Context.MODE_PRIVATE);
        JSONArray domains = null;
        try {
            domains = new JSONArray(prefs.getString(URLSignIn.URL_ENTRIES, "[]"));
        } catch (JSONException e) {
            Utils.e("JSONException: " + e);
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
    public static boolean setSavedDomains(Context ctx, JSONArray values) {
        SharedPreferences prefs = ctx.getSharedPreferences(ApplicationManager.PREF_NAME_PREVIOUS_DOMAINS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(URLSignIn.URL_ENTRIES, values.toString());
        return editor.commit();
    }

}

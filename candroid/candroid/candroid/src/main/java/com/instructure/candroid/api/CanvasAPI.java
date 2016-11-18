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

package com.instructure.candroid.api;

import android.content.Context;
import android.content.pm.PackageManager;
import com.instructure.canvasapi.api.compatibility_synchronous.HttpHelpers;
import com.instructure.canvasapi.model.Tab;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.loginapi.login.util.Utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class CanvasAPI 
{

    public  static String getCandroidUserAgent(Context context){
        String userAgent;
        try {
            userAgent =  "candroid/" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName + " (" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            userAgent = "candroid";
        }
        return userAgent;
    }

	public static String getAssetsFile(Context context, String s)
	{
		try {

			String file = "";
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(context.getAssets().open(s)));

			// do reading
			String line = "";
			while(line != null)
			{
				file+=line;

				line = reader.readLine();
			}

			reader.close();
			return file;

		} catch (Exception e) {
			return "";
		}
	}
	
	public static Map<String,String> getAuthenticatedURL(Context context) {
	       
        String token = APIHelpers.getToken(context);
        String headerValue = null;
        if(token != null)
        {
            headerValue = String.format("Bearer %s", token);
        }
        Map<String,String> map = new HashMap<String,String>();
        map.put("Authorization", headerValue);
        return map;
    }

    public static String getLTIUrlForTab(Context context, Tab tab) {
        try {

            String result = HttpHelpers.externalHttpGet(context, tab.getLTIUrl(), true).responseBody;

            String ltiUrl = null;
            if (result != null) {
                JSONObject ltiJSON = new JSONObject(result);
                ltiUrl = ltiJSON.getString("url");
            }

            return ltiUrl;
        } catch (Exception E) {
            return null;
        }
    }
}

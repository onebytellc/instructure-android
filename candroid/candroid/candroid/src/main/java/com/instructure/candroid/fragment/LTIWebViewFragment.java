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

package com.instructure.candroid.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannedString;
import android.text.TextUtils;

import com.instructure.candroid.R;
import com.instructure.candroid.api.CanvasAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.pandautils.utils.Const;
import com.instructure.canvasapi.model.Tab;

public class LTIWebViewFragment extends InternalWebviewFragment {

    private Tab ltiTab;
    private String url;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    @Override
    public String getFragmentTitle() {
        return getContext().getString(R.string.link);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setShouldRouteInternally(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            if(ltiTab == null) {
                if(!TextUtils.isEmpty(url)) {
                    //modify the url
                    Uri uri =  Uri.parse(url).buildUpon().appendQueryParameter("display", "borderless").build();
                    loadUrl(uri.toString());
                } else {
                    SpannedString spannedString = new SpannedString(getString(R.string.errorOccurred));
                    loadHtml(Html.toHtml(spannedString));
                }
            } else {
                new GetLtiURL().execute();
            }
        } catch (Exception e) {
            //if it gets here we're in trouble and won't know what the tab is, so just display an error message
            SpannedString spannedString = new SpannedString(getString(R.string.errorOccurred));
            loadHtml(Html.toHtml(spannedString));
        }
    }

    public static Bundle createBundle(CanvasContext canvasContext, Tab ltiTab) {
        Bundle extras = createBundle(canvasContext);
        extras.putBoolean(Const.AUTHENTICATE, false);
        extras.putParcelable(Const.TAB, ltiTab);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String url) {
        Bundle extras = createBundle(canvasContext);
        extras.putBoolean(Const.AUTHENTICATE, false);
        extras.putString(Const.URL, url);
        return extras;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Asynctask
    ///////////////////////////////////////////////////////////////////////////

    private class GetLtiURL extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressBar();
        }

        @Override
        protected String doInBackground(Void... params) {
            return CanvasAPI.getLTIUrlForTab(getContext(), ltiTab);
        }

        @Override
        protected void onPostExecute(String result) {
            if(getActivity() == null){return;}

            hideProgressBar();
            loadUrl(result);
        }
    }

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
        if(extras.containsKey(Const.TAB)) {
            ltiTab = extras.getParcelable(Const.TAB);
        }

        url = extras.getString(Const.URL, "");
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}
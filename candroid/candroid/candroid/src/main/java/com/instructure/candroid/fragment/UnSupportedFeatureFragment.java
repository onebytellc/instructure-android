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
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.instructure.candroid.R;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Tab;
import com.instructure.pandautils.utils.Const;

public class UnSupportedFeatureFragment extends OrientationChangeFragment {

    private View rootView;

    private String featureName;
    private String url;
    protected String tabId;

    private Button openLink;
    private TextView featureText;
    private ImageView sadPanda;

    private CardView cardView;

    private FRAGMENT_PLACEMENT placement = FRAGMENT_PLACEMENT.MASTER;

    @Override
    public String getFragmentTitle() {
        return getString(R.string.unsupported);
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return placement; }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public View populateView(LayoutInflater inflater, ViewGroup container) {
        rootView = inflater.inflate(R.layout.unsupported_feature_fragment_layout, container, false);

        openLink = (Button) rootView.findViewById(R.id.openInBrowser);
        featureText = (TextView) rootView.findViewById(R.id.featureText);
        sadPanda = (ImageView) rootView.findViewById(R.id.sadPanda);
        boolean funModeDisabled = ApplicationManager.getPrefs(getContext()).load(Const.FUN_MODE, false);
        sadPanda.setVisibility(funModeDisabled ? View.GONE : View.VISIBLE);
        initViews();
        return rootView;
    }

    ///////////////////////////////////////////////////////////////////////////
    // View
    ///////////////////////////////////////////////////////////////////////////

    private void initViews() {

        //Set the text
        if (featureName != null) {
            featureText.setText(featureName + " " + getString(R.string.areNotSupported));
        } else {
            featureText.setText(getString(R.string.isNotSupported));
        }

        openLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (featureName != null) {
                    Analytics.trackUnsupportedFeature(getActivity(), featureName);
                } else if(url != null){
                    Analytics.trackUnsupportedFeature(getActivity(), url);
                }
                //the last parameter needs to be true so the webpage will try to authenticate
                InternalWebviewFragment.loadInternalWebView(getActivity(), ((Navigation) getActivity()), InternalWebviewFragment.createBundle(getCanvasContext(), url, true, true));
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // Getters & Setters
    ///////////////////////////////////////////////////////////////////////////

    public void setFeature(String featureName, String url) {
        this.featureName = featureName;
        this.url = url;

        initViews();
    }

    public void setFeatureText(String text) {
        featureText.setText(text);
    }

    public void removeSadPanda() {
        sadPanda.setVisibility(View.INVISIBLE);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
        featureName = extras.getString(Const.FEATURE_NAME);
        url = extras.getString(Const.URL);
        tabId = extras.getString(Const.TAB_ID);

        if(extras.containsKey(Const.PLACEMENT)) {
            placement = (FRAGMENT_PLACEMENT)extras.getSerializable(Const.PLACEMENT);
        }
    }

    public static Bundle createBundle(CanvasContext canvasContext, String featureName, String url, String tabId) {
        Bundle extras = createBundle(canvasContext);
        extras.putString(Const.FEATURE_NAME,featureName);
        extras.putString(Const.URL,url);
        extras.putString(Const.TAB_ID, tabId);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, String featureName, String url, Tab tab) {
        Bundle extras = createBundle(canvasContext, tab);
        extras.putString(Const.FEATURE_NAME,featureName);
        extras.putString(Const.URL,url);
        extras.putString(Const.TAB_ID, tab.getTabId());
        return extras;
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }

    @Override
    public String getTabId() {
        if(!TextUtils.isEmpty(tabId)) {
            return tabId;
        }
        return null;
    }
}

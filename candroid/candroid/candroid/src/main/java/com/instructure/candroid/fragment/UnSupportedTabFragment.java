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

import android.os.Bundle;
import android.text.TextUtils;

import com.instructure.candroid.R;
import com.instructure.pandautils.utils.Const;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Tab;
import com.instructure.canvasapi.utilities.APIHelpers;

public class UnSupportedTabFragment extends UnSupportedFeatureFragment {

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    private int featureName = 0;

    public String getTabId() {
        return tabId;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String url = APIHelpers.loadProtocol(getContext()) + "://" + APIHelpers.getDomain(getContext());

        if (tabId.equalsIgnoreCase(Tab.CONFERENCES_ID)) {
            url += getCanvasContext().toAPIString() + "/conferences";
            featureName = R.string.conferences;
        } else if (tabId.equalsIgnoreCase(Tab.COLLABORATIONS_ID)) {
            url += getCanvasContext().toAPIString() + "/collaborations";
            featureName = R.string.collaborations;
        } else if (tabId.equalsIgnoreCase(Tab.OUTCOMES_ID)) {
            url += getCanvasContext().toAPIString() + "/outcomes";
            featureName = R.string.outcomes;
        }

        if(featureName == 0) {
            setFeature(null, url);
        } else {
            setFeature(getString(featureName), url);
        }
    }

    @Override
    public String getFragmentTitle() {
        if(featureName == 0) return "";

        return getString(featureName);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    public static Bundle createCollaborationsBundle(CanvasContext canvasContext, FRAGMENT_PLACEMENT placement) {
        Bundle bundle = createBundle(canvasContext);
        bundle.putString(Const.TAB_ID, Tab.COLLABORATIONS_ID);
        bundle.putSerializable(Const.PLACEMENT, placement);
        return bundle;
    }

    public static Bundle createConferencesBundle(CanvasContext canvasContext, FRAGMENT_PLACEMENT placement) {
        Bundle bundle = createBundle(canvasContext);
        bundle.putString(Const.TAB_ID, Tab.CONFERENCES_ID);
        bundle.putSerializable(Const.PLACEMENT, placement);
        return bundle;
    }

    public static Bundle createOutcomesBundle(CanvasContext canvasContext, FRAGMENT_PLACEMENT placement) {
        Bundle bundle = createBundle(canvasContext);
        bundle.putString(Const.TAB_ID, Tab.OUTCOMES_ID);
        bundle.putSerializable(Const.PLACEMENT, placement);
        return bundle;
    }

    public static Bundle createBundle(CanvasContext canvasContext, FRAGMENT_PLACEMENT placement, String tab) {
        Bundle bundle = createBundle(canvasContext);
        bundle.putString(Const.TAB_ID, tab);
        bundle.putSerializable(Const.PLACEMENT, placement);
        return bundle;
    }
}

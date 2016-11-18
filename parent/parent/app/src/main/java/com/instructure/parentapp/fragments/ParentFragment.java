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

package com.instructure.parentapp.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.Prefs;
import com.instructure.parentapp.R;
import com.instructure.parentapp.util.ViewUtils;

/**
 * Copyright (c) 2014 Instructure. All rights reserved.
 */
public class ParentFragment extends Fragment {

    private Toolbar mDialogToolbar;

    public void setActionbarColor(int actionBarColor) {
        if(mDialogToolbar != null) {
            ColorDrawable colorDrawable = new ColorDrawable(actionBarColor);
            mDialogToolbar.setBackgroundDrawable(colorDrawable);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int statusBarColor) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && statusBarColor != Integer.MAX_VALUE) {
            //make the status bar darker than the toolbar
            getActivity().getWindow().setStatusBarColor(ViewUtils.darker(statusBarColor, 0.85f));
        }
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    protected int getRootLayout(){
        return -1;
    }

    protected void setupDialogToolbar(View rootView) {
        mDialogToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (mDialogToolbar != null) {
            mDialogToolbar.setVisibility(View.VISIBLE);

            mDialogToolbar.setNavigationIcon(R.drawable.ic_close_white);
            mDialogToolbar.setNavigationContentDescription(R.string.close);
            mDialogToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });

            Prefs prefs = new Prefs(getActivity(), com.instructure.parentapp.util.Const.CANVAS_PARENT_SP);
            int color = prefs.load(Const.NEW_COLOR, -1);

            if(color != -1) {
                setStatusBarColor(color);
                setActionbarColor(color);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    public StatusCallback.StatusDelegate mStatusDelegate = new StatusCallback.StatusDelegate() {
        @Override
        public boolean hasNetworkConnection() {
            return AppManager.hasNetworkConnection(getContext());
        }
    };
}

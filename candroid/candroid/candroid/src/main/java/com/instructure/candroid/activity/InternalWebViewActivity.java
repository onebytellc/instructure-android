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

package com.instructure.candroid.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.instructure.candroid.R;
import com.instructure.candroid.fragment.InternalWebviewFragment;
import com.instructure.pandautils.activities.BaseActionBarActivity;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;
import com.instructure.candroid.util.FragUtils;
import com.instructure.canvasapi.model.CanvasContext;

public class InternalWebViewActivity extends BaseActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            Bundle bundle = getIntent().getBundleExtra(Const.EXTRAS);
            InternalWebviewFragment fragment = FragUtils.getFrag(InternalWebviewFragment.class, bundle);

            if(bundle.containsKey(Const.ACTION_BAR_TITLE)) {
                getSupportActionBar().setTitle(bundle.getString(Const.ACTION_BAR_TITLE, getString(R.string.app_name)));
                setActionBarStatusBarColors(getResources().getColor(R.color.defaultPrimary), getResources().getColor(R.color.defaultPrimaryDark));
            }
            else if(bundle.containsKey(Const.CANVAS_CONTEXT)) {
                CanvasContext canvasContext = bundle.getParcelable(Const.CANVAS_CONTEXT);
                if(canvasContext != null) {
                    final int[] colors = CanvasContextColor.getCachedColors(getApplicationContext(), canvasContext);
                    setActionBarStatusBarColors(colors[0], colors[1]);
                    getSupportActionBar().setTitle(canvasContext.getName());
                }
            }
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.container, fragment, InternalWebviewFragment.class.getName());
            ft.commitAllowingStateLoss();
        }
    }

    @Override
    public int contentResId() {
        return R.layout.base_layout;
    }

    @Override
    public boolean showHomeAsUp() {
        return true;
    }

    @Override
    public boolean showTitleEnabled() {
        return true;
    }

    @Override
    public void onUpPressed() {
        finish();
    }

    public static Intent createIntent(Context context, String url, String title, boolean authenticate) {
        // Assumes no canvasContext
        Bundle extras = InternalWebviewFragment.createBundle(null, url, title, authenticate);

        Intent intent = new Intent(context, InternalWebViewActivity.class);
        intent.putExtra(Const.EXTRAS, extras);

        return intent;
    }

    public static Intent createIntent(Context context, CanvasContext canvasContext, String url, boolean authenticate) {
        Bundle extras = InternalWebviewFragment.createBundle(canvasContext, url, authenticate);
        Intent intent = new Intent(context, InternalWebViewActivity.class);
        intent.putExtra(Const.EXTRAS, extras);
        return intent;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if(fragment instanceof InternalWebviewFragment) {
            InternalWebviewFragment webviewFragment = (InternalWebviewFragment)fragment;
            if(!webviewFragment.handleBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // ACTION BAR STATUS BAR COLORS
    ///////////////////////////////////////////////////////////////////////////

    public void setActionBarStatusBarColors(int actionBarColor, int statusBarColor) {
        setActionbarColor(actionBarColor);
        setStatusBarColor(statusBarColor);
    }

    public void setActionbarColor(int actionBarColor) {
        ColorDrawable colorDrawable = new ColorDrawable(actionBarColor);
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int statusBarColor) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && statusBarColor != Integer.MAX_VALUE) {
            getWindow().setStatusBarColor(statusBarColor);
        }
    }
}

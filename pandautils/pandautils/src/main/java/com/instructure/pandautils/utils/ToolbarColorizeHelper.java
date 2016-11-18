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

package com.instructure.pandautils.utils;

import android.app.Activity;
import android.os.Build;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.instructure.pandautils.R;

import java.util.ArrayList;

/**
 * https://gist.github.com/peterkuterna/f808854cb2f7b23e861d
 */
public class ToolbarColorizeHelper {

    /**
     * Use this method to colorize toolbar icons to the desired target color
     * @param toolbarView toolbar view being colored
     * @param toolbarIconsColor the target color of toolbar icons
     * @param activity reference to activity needed to register observers
     */
    public static void colorizeToolbar(Toolbar toolbarView, final int toolbarIconsColor, Activity activity) {
        for(int i = 0; i < toolbarView.getChildCount(); i++) {
            final View v = toolbarView.getChildAt(i);

            //Step 1 : Changing the color of back button (or open drawer button).
            if(v instanceof ImageButton) {
                //Action Bar back button
                ColorUtils.tintIt(toolbarIconsColor, ((ImageButton)v).getDrawable());
            } else if(v instanceof TextView) {
                ((TextView) v).setTextColor(toolbarIconsColor);
            } else if(v instanceof ActionMenuView) {
                for(int j = 0; j < ((ActionMenuView)v).getChildCount(); j++) {

                    //Step 2: Changing the color of any ActionMenuViews - icons that are not back button, nor text, nor overflow menu icon.
                    //Colorize the ActionViews -> all icons that are NOT: back button | overflow menu
                    final View innerView = ((ActionMenuView)v).getChildAt(j);
                    if(innerView instanceof ActionMenuItemView) {
                        //Sets text color for ActionMenuItemView when icon is not present
                        ((ActionMenuItemView) innerView).setTextColor(toolbarIconsColor);
                        for(int k = 0; k < ((ActionMenuItemView)innerView).getCompoundDrawables().length; k++) {
                            if(((ActionMenuItemView)innerView).getCompoundDrawables()[k] != null) {
                                final int finalK = k;

                                //Important to set the color filter in separate thread, by adding it to the message queue
                                //Won't work otherwise.
                                innerView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ColorUtils.tintIt(toolbarIconsColor, ((ActionMenuItemView) innerView).getCompoundDrawables()[finalK]);
                                    }
                                });
                            }
                        }
                    }
                }
            }

            //Step 3: Changing the color of title and subtitle.
            toolbarView.setTitleTextColor(toolbarIconsColor);
            toolbarView.setSubtitleTextColor(toolbarIconsColor);

            //Step 4: Changing the color of the Overflow Menu icon.
            setOverflowButtonColor(toolbarIconsColor, activity);

            //Step 5: When not using setSupportActionbar this method is used to color the overflow icon
            setOverflowIconColor(toolbarView, toolbarIconsColor);
        }
    }

    private static void setOverflowIconColor(Toolbar toolbarView, final int toolbarIconsColor) {
        toolbarView.setOverflowIcon(ColorUtils.tintIt(toolbarIconsColor, toolbarView.getOverflowIcon()));
    }

    /**
     * It's important to set overflowDescription atribute in styles, so we can grab the reference
     * to the overflow icon. Check: res/values/styles.xml
     * @param toolbarIconsColor The color to color
     * @param activity A context with reference to a window object
     */
    private static void setOverflowButtonColor(final int toolbarIconsColor, final Activity activity) {
        final String overflowDescription = activity.getString(R.string.accessibility_overflow);
        final ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        final ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final ArrayList<View> outViews = new ArrayList<>();
                decorView.findViewsWithText(outViews, overflowDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                if (outViews.isEmpty()) {
                    removeOnGlobalLayoutListener(decorView,this);
                    return;
                }
                ImageView overflow = (ImageView) outViews.get(0);
                ColorUtils.tintIt(toolbarIconsColor, overflow.getDrawable());
                removeOnGlobalLayoutListener(decorView,this);
            }
        });
    }

    private static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }
}

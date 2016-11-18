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

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;

import com.instructure.canvasapi2.models.CanvasTheme;
import com.instructure.pandautils.R;

/**
 * Used to color get the Canvas Themes
 */
public class ThemeUtils {

    public static int getPrimary(Context context) {
        CanvasTheme theme = com.instructure.canvasapi2.AppManager.getCanvasTheme();
        if(theme != null) {
            try {
                return Color.parseColor(theme.getPrimary());
            } catch (IllegalArgumentException e) {
                //do nothing
            }
        }
        return context.getResources().getColor(R.color.canvas_default_primary);
    }

    public static int getPrimaryText(Context context) {
        CanvasTheme theme = com.instructure.canvasapi2.AppManager.getCanvasTheme();
        if(theme != null) {
            try {
                return Color.parseColor(theme.getPrimaryText());
            } catch (IllegalArgumentException e) {
                //do nothing
            }
        }
        return context.getResources().getColor(R.color.canvas_default_primary_text);
    }

    public static int getAccent(Context context) {
        CanvasTheme theme = com.instructure.canvasapi2.AppManager.getCanvasTheme();
        if(theme != null) {
            try {
                return Color.parseColor(theme.getAccent());
            } catch (IllegalArgumentException e) {
                //do nothing
            }
        }
        return context.getResources().getColor(R.color.canvas_default_accent);
    }

    public static int getButton(Context context) {
        CanvasTheme theme = com.instructure.canvasapi2.AppManager.getCanvasTheme();
        if(theme != null) {
            try {
                return Color.parseColor(theme.getButton());
            } catch (IllegalArgumentException e) {
                //do nothing
            }
        }
        return context.getResources().getColor(R.color.canvas_default_button);
    }

    public static int getButtonText(Context context) {
        CanvasTheme theme = com.instructure.canvasapi2.AppManager.getCanvasTheme();
        if(theme != null) {
            try {
                return Color.parseColor(theme.getButtonText());
            } catch (IllegalArgumentException e) {
                //do nothing
            }
        }
        return context.getResources().getColor(R.color.canvas_default_button_text);
    }

    public static String getLogoUrl() {
        CanvasTheme theme = com.instructure.canvasapi2.AppManager.getCanvasTheme();
        if(theme != null) {
            return theme.getLogoUrl();
        }
        return "";
    }

    /**
     * Returns darker version of specified <code>color</code>.
     * StatusBar color example would be 0.85F
     */
    public static int darker (int color, float factor) {
        final int a = Color.alpha(color);
        final int r = Color.red(color);
        final int g = Color.green( color );
        final int b = Color.blue( color );
        return Color.argb(
                a,
                Math.max((int)(r * factor), 0),
                Math.max((int)(g * factor), 0),
                Math.max((int)(b * factor), 0));
    }

    public static void themeViewBackground(@NonNull final View view, final int color) {
        final ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                removeOnGlobalLayoutListener(view, this);
                Drawable wrappedDrawable = DrawableCompat.wrap(view.getBackground());
                if(wrappedDrawable != null) {
                    DrawableCompat.setTint(wrappedDrawable.mutate(), color);
                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                        view.setBackground(wrappedDrawable);
                    }else{
                        view.setBackgroundDrawable(wrappedDrawable);
                    }
                }
            }
        });
    }

    public static void themeEditTextBackground(@NonNull final EditText editText, final int color) {
        editText.setTextColor(color);
        themeViewBackground(editText, color);
    }

    private static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }
}

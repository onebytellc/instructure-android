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
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.instructure.canvasapi.api.UserAPI;
import com.instructure.canvasapi.model.CanvasColor;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.R;

import java.util.HashMap;
import java.util.Map;

import retrofit.client.Response;


public class CanvasContextColor {

    private static Prefs mPrefs = null;
    private static int mDefaultPrimaryColor;
    private static int mDefaultPrimaryDarkColor;
    public enum COLOR_TINT {DARKER, LIGHTER}
    private static String TAG = "CanvasContextColor";

    public interface CanvasColorCallback {
        void gotColor(int color);
    }

    public interface CanvasColorsCallback {
        void gotColor(int[] colors);
    }

    private static CanvasColor mCachedColors = null;

    /**
     * Returns a color string in hex format with no alpha
     * @param color a valid color in the form of an Integer
     * @return A hex string form of the color with no alpha
     */
    public static String getColorStringFromInt(int color, boolean prefixWithHashTag) {
        String colorStr = Integer.toHexString(color);
        colorStr = colorStr.substring(colorStr.length() - 6);
        if(prefixWithHashTag && !colorStr.startsWith("#")) {
            return "#" + colorStr;
        } else {
            return colorStr;
        }
    }

    /**
     * Gets a color from a url string
     * @param context android context
     * @param context_id canvas context ID = course_111111
     * @return Returns a cached color that is figured out from a url works with Groups and Courses
     */
    public static int getCachedColorForUrl(final Context context, String context_id) {
        CanvasColor canvasColor = getCachedColors();
        if (canvasColor.getColors().containsKey(context_id)) {
            return parseColor(canvasColor.getColors().get(context_id));
        }
        return context.getResources().getColor(mDefaultPrimaryColor);
    }

    /**
     * Returns a cached color for a canvas context
     * @param context android context
     * @param canvasContext the canvasContext
     * @return a color
     */
    public static int getCachedColor(final Context context, final CanvasContext canvasContext) {
        if(canvasContext == null) {
            return context.getResources().getColor(mDefaultPrimaryColor);
        }

        //First check the cache for the course color
        //If nothing, generate a color

        CanvasColor cachedColors = getCachedColors();
        if(!cachedColors.getColors().isEmpty()) {
            Integer color = findColorInCanvasColors(cachedColors, canvasContext);
            if(color != null) {
                return color;
            }
        }

        //We should only generate colors when there is no color from the web, and no cached color.
        return generateColor(context, canvasContext)[0];
    }

    /**
     * Returns a cached color for a canvas context
     * @param context android context
     * @param context_id the canvasContext context_id, course_123456 or group_12345
     * @return a color
     */
    public static int getCachedColor(final Context context, final String context_id) {
        if(TextUtils.isEmpty(context_id)) {
            return context.getResources().getColor(mDefaultPrimaryColor);
        }

        //First check the cache for the course color
        //If nothing, generate a color

        CanvasColor cachedColors = getCachedColors();
        if(!cachedColors.getColors().isEmpty()) {
            Integer color = findColorInCanvasColors(cachedColors, context_id);
            if(color != null) {
                return color;
            }
        }

        CanvasContext genericContext = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, 0, "");

        //We should only generate colors when there is no color from the web, and no cached color.
        return generateColor(context, genericContext)[0];
    }

    /**
     * Returns an array of cached colors for a canvas context
     * @param context android context
     * @param canvasContext a canvasContext
     * @return an array of integers containing a normal and dark color in that order
     */
    public static int[] getCachedColors(final Context context, final CanvasContext canvasContext) {
        if (canvasContext == null) {
            return new int[] { mDefaultPrimaryColor, mDefaultPrimaryDarkColor };
        }

        //First check the cache for the course color
        //If nothing, generate a color

        CanvasColor cachedColors = getCachedColors();
        if(!cachedColors.getColors().isEmpty()) {
            Integer color = findColorInCanvasColors(cachedColors, canvasContext);
            if(color != null) {
                return new int[]{color, color};
            }
        }

        //We should only generate colors when there is no color from the web, and no cached color.
        return generateColor(context, canvasContext);
    }

    /**
     * Returns a color via a callback, if no color is in the cache it will pull from canvas via the api
     * If nothing is found in the cache or api a color is generated
     * @param context android context
     * @param canvasContext canvasContext
     * @param callback the callback the color will return to
     */
    public static void getColor(final Context context, final CanvasContext canvasContext, final CanvasColorCallback callback) {
        if(canvasContext == null && callback == null) {
            return;
        }

        //First check the cache for the course color
        //Next do an api call for the color
        //If nothing, generate a color

        CanvasColor cachedColors = getCachedColors();
        if(!cachedColors.getColors().isEmpty()) {
            Integer color = findColorInCanvasColors(cachedColors, canvasContext);
            if(color != null) {
                callback.gotColor(color);
                return;
            }
        }

        //No cached color found do api call for color
        UserAPI.getColors(context, new CanvasCallback<CanvasColor>(new APIStatusDelegate() {
            @Override
            public void onCallbackStarted() {
            }

            @Override
            public void onCallbackFinished(CanvasCallback.SOURCE source) {
            }

            @Override
            public void onNoNetwork() {
            }

            @Override
            public Context getContext() {
                return context;
            }

        }) {
            @Override
            public void cache(CanvasColor canvasColor) {
            }

            @Override
            public void firstPage(CanvasColor canvasColor, LinkHeaders linkHeaders, Response response) {
                if (response.getStatus() == 200) {
                    addToCache(canvasColor);
                    Integer color = findColorInCanvasColors(canvasColor, canvasContext);
                    if (color != null) {
                        callback.gotColor(color);
                    } else {
                        //Nothing found return the generated color
                        callback.gotColor(generateColor(context, canvasContext)[0]);
                    }
                }
            }
        });
    }

    /**
     * Returns a color array for a canvas context via a callback
     * @param context android context
     * @param canvasContext a valid canvas context
     * @param callback the callback the color will return too
     */
    public static void getColors(final Context context, final CanvasContext canvasContext, final CanvasColorsCallback callback) {
        if (canvasContext == null && callback == null) {
            return;
        }

        getColor(context, canvasContext, new CanvasColorCallback() {
            @Override
            public void gotColor(int color) {
                callback.gotColor(new int[]{color, color});
            }
        });
    }

    /**
     * change the color of the drawable and return it
     * @param context android context
     * @param resource the resource to color
     * @param color = getResources().getColor(r.color)
     * @return
     */
    public static Drawable getColoredDrawable(Context context, int resource, int color) {
        Drawable drawable = context.getResources().getDrawable(resource);
        drawable = drawable.mutate();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        return drawable;
    }

    public static Drawable getColoredDrawable(final Context context, final int resource, @Nullable final CanvasContext canvasContext){
        return getColoredDrawable(context, resource, getCachedColor(context, canvasContext));
    }

    /**
     *
     * @param context android context
     * @param resource a resource to color
     * @param canvasContextId a course id, NOT a group or other canvasContext id
     * @return a drawable colored like the rainbow
     */
    public static Drawable getColoredDrawable(final Context context, final int resource, final long canvasContextId){
        CanvasContext canvasContext = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, canvasContextId, "GenericCanvasContext");
        return getColoredDrawable(context, resource, getCachedColor(context, canvasContext));
    }

    /**
     * Sets a new color to the api and caches the result
     * @param delegate api delegate for doing api calls
     * @param context android context
     * @param canvasContext canvasContext
     * @param newColor the new color to set
     */
    public static void setNewColor(final APIStatusDelegate delegate, final Context context, final CanvasContext canvasContext, final int newColor) {

        if(delegate == null || canvasContext == null) {
            return;
        }

        addToCache(canvasContext, newColor);
        UserAPI.setColor(context, canvasContext, newColor, new CanvasCallback<CanvasColor>(delegate) {
            @Override
            public void cache(CanvasColor canvasColor) {

            }

            @Override
            public void firstPage(CanvasColor canvasColor, LinkHeaders linkHeaders, Response response) {

            }
        });
    }

    public static void addToCache(CanvasColor canvasColor) {
        CanvasColor existingCanvasColor = getCachedColors();
        Map<String, String> colorsMap = new HashMap<>();
        colorsMap.putAll(existingCanvasColor.getColors());
        colorsMap.putAll(canvasColor.getColors());
        saveCourseColorsToCache(new CanvasColor(colorsMap));
    }

    /**
     * Will add a color to the existing cache or overwrite the cached value if it already exists.
     * @param canvasContext a canvascontext
     * @param color a valid color, not a resource id.
     */
    public static void addToCache(CanvasContext canvasContext, int color) {
        addToCache(canvasContext.getContextId(), color);
    }

    /**
     * Will add a color to the existing cache or overwrite the cached value if it already exists.
     * @param canvasContextId a context ID like course_12345
     * @param color a valid color, not a resource id.
     */
    public static void addToCache(String canvasContextId, int color) {
        CanvasColor existingCanvasColor = getCachedColors();
        existingCanvasColor.getColors().put(canvasContextId, getColorStringFromInt(color, true));
        saveCourseColorsToCache(existingCanvasColor);
    }

    /**
     * Helper to find a color in a canvas color object
     * @param canvasColor
     * @param canvasContext
     * @return a color value in Int form, may be null. If null no color was found for the canvas context.
     */
    private static Integer findColorInCanvasColors(CanvasColor canvasColor, CanvasContext canvasContext) {
        return findColorInCanvasColors(canvasColor, canvasContext.getContextId());
    }

    private static Integer findColorInCanvasColors(CanvasColor canvasColor, String context_id) {
        String colorVal = canvasColor.getColors().get(context_id);
        if(!TextUtils.isEmpty(colorVal)) {
            return parseColor(colorVal);
        }
        return null;
    }


    /**
     * Parses a color in a safe way
     * @param hexColor a hex string which can have an optional # at the start
     * @return an Integer value of the color, can be null
     */
    private static Integer parseColor(String hexColor) {
        if(!hexColor.startsWith("#")) {
            hexColor = "#" + hexColor;
        }

        try {
            return Color.parseColor(hexColor);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Could not parse color (" + hexColor + ") " + e);
            return null;
        }
    }

    /**
     * Generates a generic color based on the canvas context id, this will produce consistant colors for a given course
     * @param context
     * @param canvasContext a valid canvas context
     * @return an integer array of the normal and dark colors for a course
     */
    private static int[] generateColor(Context context, CanvasContext canvasContext) {
        int[] color = new int[] { context.getResources().getColor(mDefaultPrimaryColor), context.getResources().getColor(mDefaultPrimaryDarkColor) };

        if(canvasContext.getType() == CanvasContext.Type.USER) {
            return color;
        }

        if(TextUtils.isEmpty(canvasContext.getName())) {
            addToCache(canvasContext.getContextId(), color[0]);
            return color;
        }

        int index = Math.abs(canvasContext.getName().hashCode() % 13);
        switch (index) {
            case 0:
                color[0] = context.getResources().getColor(R.color.courseOrange);
                color[1] = color[0];
                break;
            case 1:
                color[0] = context.getResources().getColor(R.color.courseBlue);
                color[1] = color[0];
                break;
            case 2:
                color[0] = context.getResources().getColor(R.color.courseGreen);
                color[1] = color[0];
                break;
            case 3:
                color[0] = context.getResources().getColor(R.color.coursePurple);
                color[1] = color[0];
                break;
            case 4:
                color[0] = context.getResources().getColor(R.color.courseGold);
                color[1] = color[0];
                break;
            case 5:
                color[0] = context.getResources().getColor(R.color.courseRed);
                color[1] = color[0];
                break;
            case 6:
                color[0] = context.getResources().getColor(R.color.courseChartreuse);
                color[1] = color[0];
                break;
            case 7:
                color[0] = context.getResources().getColor(R.color.courseCyan);
                color[1] = color[0];
                break;
            case 8:
                color[0] = context.getResources().getColor(R.color.courseSlate);
                color[1] = color[0];
                break;
            case 9:
                color[0] = context.getResources().getColor(R.color.coursePink);
                color[1] = color[0];
                break;
            case 10:
                color[0] = context.getResources().getColor(R.color.courseViolet);
                color[1] = color[0];
                break;
            case 11:
                color[0] = context.getResources().getColor(R.color.courseGrey);
                color[1] = color[0];
                break;
            case 12:
                color[0] = context.getResources().getColor(R.color.courseYellow);
                color[1] = color[0];
                break;
            case 13:
                color[0] = context.getResources().getColor(R.color.courseLavender);
                color[1] = color[0];
                break;
            default:
                color[0] = context.getResources().getColor(R.color.courseHotPink);
                color[1] = color[0];
                break;
        }

        addToCache(canvasContext.getContextId(), color[0]);
        return color;
    }

    /**
     * Gets the course colors from a cache (Shared Prefs)
     * @return returns a cached context for colors pulled from shared prefs
     */
    public static CanvasColor getCachedColors() {
        if(mPrefs == null){
            throw new IllegalStateException("You must first call init() before using CanvasContextColor");
        }
        if(mCachedColors == null) {
            String json = mPrefs.load(Const.COURSE_COLORS, "");
            if (!TextUtils.isEmpty(json)) {
                mCachedColors = new Gson().fromJson(json, CanvasColor.class);
            }
        }

        if(mCachedColors == null) {
            mCachedColors = new CanvasColor();
        }

        return mCachedColors;
    }

    public static void invalidateColorsCache() {
        mCachedColors = null;
    }

    /**
     * Saves the course colors to a cache (Shared Prefs)
     * @param canvasColor
     */
    public static void saveCourseColorsToCache(CanvasColor canvasColor) {
        if(mPrefs == null){
            throw new IllegalStateException("You must first call init() before using CanvasContextColor");
        }
        final String json = new Gson().toJson(canvasColor, CanvasColor.class);
        mPrefs.save(Const.COURSE_COLORS, json);
        invalidateColorsCache();
    }

    public static void init(Prefs prefs, int defaultPrimaryColor, int defaultPrimaryDarkColor){
        mPrefs = prefs;
        mDefaultPrimaryColor = defaultPrimaryColor;
        mDefaultPrimaryDarkColor = defaultPrimaryDarkColor;
    }
}

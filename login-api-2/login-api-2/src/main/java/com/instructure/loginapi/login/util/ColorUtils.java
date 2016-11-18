package com.instructure.loginapi.login.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.ImageView;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 * TODO: remove and move to panda utils version
 */
@Deprecated
public class ColorUtils {

    public static Drawable tintIt(int color, Drawable drawable) {
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }

    public static Drawable tintIt(Context context, int color, @DrawableRes int drawableResId) {
        Drawable wrappedDrawable = DrawableCompat.wrap(context.getResources().getDrawable(drawableResId));
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }

    public static Drawable colorIt(int color, Drawable drawable) {
        drawable.mutate().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        return drawable;
    }

    public static void colorIt(int color, ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if(drawable == null) return;

        drawable.mutate().setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        imageView.setImageDrawable(drawable);
    }

    public static Drawable colorIt(Context context, int color, int drawableResId) throws NullPointerException {
        Drawable drawable = context.getResources().getDrawable(drawableResId);
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        return drawable;
    }

    public static Bitmap colorIt(int color, Bitmap map) {
        Canvas canvas = new Canvas(map);
        Paint paint = new Paint();
        paint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(map, 0, 0, paint);
        return map;
    }
}

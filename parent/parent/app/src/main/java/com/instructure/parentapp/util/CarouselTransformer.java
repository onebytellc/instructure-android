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

package com.instructure.parentapp.util;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.view.View;

public class CarouselTransformer implements ViewPager.PageTransformer {

    private float MIN_SCALE = .02f;

    @Override
    public void transformPage(View page, float position) {
        final float normalizedposition = Math.abs(Math.abs(position) - 1);
        float scaleFactor = MIN_SCALE
                + (1 - MIN_SCALE) * (1 - Math.abs(position * 2));
        if(position != 0 && scaleFactor >= 1) {
            return;
        }
        if(position == 0){
            ViewCompat.setAlpha(page, normalizedposition);
        } else {
            float alphaVal = normalizedposition - (Math.abs(Math.abs(1 - normalizedposition))) * 3;
            if(alphaVal < 0.2f) {
                alphaVal = .2f;
            }
            ViewCompat.setAlpha(page, alphaVal);
        }
        ViewCompat.setScaleX(page, scaleFactor);
        ViewCompat.setScaleY(page, scaleFactor);
    }
}

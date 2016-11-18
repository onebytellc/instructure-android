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

package com.instructure.speedgrader.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class HelveticaTextView extends TextView {
    public HelveticaTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HelveticaTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public HelveticaTextView(Context context) {
        super(context);
    }

    public void setTypeface(Typeface tf, int style){
        if(style == Typeface.BOLD){
            setTypeface(Typeface.createFromAsset(getContext().getAssets(), "HelveticaNeueLTCom-BdCn.ttf"));
        }else if(style == Typeface.NORMAL){
            setTypeface(Typeface.createFromAsset(getContext().getAssets(),"HelveticaNeueLTCom-MdCn.ttf"));
        }else if(style == Typeface.ITALIC){
            setTypeface(Typeface.createFromAsset(getContext().getAssets(),"HelveticaNeueLTCom-LtIt.ttf"));
        }
    }
}

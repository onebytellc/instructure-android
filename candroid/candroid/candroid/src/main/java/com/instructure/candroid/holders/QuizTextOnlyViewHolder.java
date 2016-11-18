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

package com.instructure.candroid.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.instructure.candroid.R;
import com.instructure.candroid.view.CanvasWebView;

public class QuizTextOnlyViewHolder extends RecyclerView.ViewHolder {

    public CanvasWebView text;

    public QuizTextOnlyViewHolder(View v) {
        super(v);
        text = (CanvasWebView) v.findViewById(R.id.quiz_text);
    }

    public static int adapterResId() {
        return R.layout.quiz_text_only;
    }

}

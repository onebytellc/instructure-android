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

package com.instructure.candroid.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.instructure.candroid.R;
import com.instructure.pandautils.utils.CanvasContextColor;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Custom adapter that sets the tag of the button to the
 * index of the item in the adapter. This is used to know
 * which item to remove when the button is pressed, because
 * they all call onSuggestionRemoved(View v).
 **/
public class DomainSuggestionAdapter extends ArrayAdapter<String> {

    private ArrayList<String> data;

    public DomainSuggestionAdapter(Context context, String serialzedData) {
        super(context, R.layout.edittext_suggestion_dark, R.id.suggestion_text);
        try {
            data = new ArrayList<String>();
            JSONArray json = new JSONArray(serialzedData);
            for (int i = 0; i < json.length(); i++) {
                this.add(json.getString(i));
            }
            notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(String item) {
        if (!contains(item)) {
            super.add(item);
            data.add(item);
        }
    }

    @Override
    public void remove(String item) {
        super.remove(item);
        data.remove(item);
    }

    public String getJSON() {
        return new JSONArray(data).toString();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View temp = super.getView(position, convertView, parent);
        ImageView cancelButton = (ImageView) temp.findViewById(R.id.cancel_button);
        cancelButton.setTag(((TextView) temp.findViewById(R.id.suggestion_text)).getText());

        cancelButton.setImageDrawable(CanvasContextColor.getColoredDrawable(getContext(), R.drawable.ic_cv_cancel_white_thin, Color.BLACK));

        return temp;
    }

    /**
     * Do a simple iteration to determine if the adapter already has the
     * specified string.
     *
     * @param s
     * @return
     */
    public boolean contains(String s) {
        return data.contains(s);
    }

}
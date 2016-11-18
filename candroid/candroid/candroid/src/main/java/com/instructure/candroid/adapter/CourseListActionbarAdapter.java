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
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.instructure.candroid.R;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.pandautils.utils.TutorialUtils;

import java.util.ArrayList;

public class CourseListActionbarAdapter extends BaseAdapter {

    private ArrayList<String> items = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;

    public CourseListActionbarAdapter(Context context) {
        super();
        this.context = context;
        inflater = LayoutInflater.from(context);
        items.add(context.getString(R.string.myCourses));
        items.add(context.getString(R.string.allCourses));
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null) {
            view = inflater.inflate(R.layout.actionbar_course_list_spinner, parent, false);
        }
        String item = (String)getItem(position);
        TextView text = (TextView)view.findViewById(R.id.text);
        text.setText(item);

        createTutorial((FragmentActivity)context, view);

        return view;
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {

        view = inflater.inflate(R.layout.actionbar_course_list_spinner_dropdown, parent, false);

        String item = (String)getItem(position);
        TextView text = (TextView)view.findViewById(R.id.text);
        text.setText(item);
        return view;
    }

    public void clear() {
        items.clear();
    }

    private void createTutorial(FragmentActivity content, View view){
        ImageView pulse = (ImageView)view.findViewById(R.id.pulse);
        new TutorialUtils(content, ApplicationManager.getPrefs(content), pulse, TutorialUtils.TYPE.MY_COURSES)
                .setContent(content.getString(R.string.tutorial_tipMyCoursesTitle), content.getString(R.string.tutorial_tipMyCoursesMessage))
                .build();
    }
}

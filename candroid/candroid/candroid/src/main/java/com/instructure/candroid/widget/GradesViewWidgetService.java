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

package com.instructure.candroid.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;
import com.instructure.candroid.R;
import com.instructure.candroid.activity.InterwebsToApplication;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.utilities.APIHelpers;

import java.io.Serializable;

public class GradesViewWidgetService extends BaseRemoteViewsService implements Serializable {

    public static Intent createIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, GradesViewWidgetService.class);
        intent.setAction(GradesWidgetProvider.REFRESH);
        intent.setData(Uri.fromParts("appWidgetId", String.valueOf(appWidgetId), null));
        return intent;
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GradesRowFactory(this.getApplicationContext(), intent);
    }

    private class GradesRowFactory extends CanvasWidgetRowFactory<Course>{

        private Intent intent;

        public GradesRowFactory(Context context, Intent intent) {
            this.mContext = context;
            this.intent = intent;
        }

        @Override
        protected int giveMeAppWidgetId() {
            return getAppWidgetId(intent);
        }

        @Override
        protected Intent createIntent(Course course){
            String domain = APIHelpers.getFullDomain(mContext);

            //Construct URL to route to grades page
            String courseUrl = Const.COURSE_URL + course.getId();
            String url = domain + courseUrl + Const.GRADE_URL;
            Uri uri = Uri.parse(url);

            return InterwebsToApplication.createIntent(this.mContext, uri);
        }

        @Override
        public Course[] makeApiCalls(){
            //Get Course List
           return CourseAPI.getFavCoursesSynchronous(mContext);
        }

        @Override
        public int getLayoutId(){
            if(intent != null) {
                if (shouldHideDetails(getApplicationContext(), getAppWidgetId(intent))) {
                    return R.layout.listview_widget_grades_minimum_item_row;
                }
            }
            return R.layout.listview_widget_grades_item_row;
        }

        @Override
        public void setViewData(Course course, RemoteViews row){
            if(course == null || row == null || course.getTerm() == null){
                return;
            }

            row.setViewVisibility(R.id.course_indicator, View.VISIBLE);

            int appWidgetId = getAppWidgetId(intent);
            row.setTextColor(R.id.course_name, getWidgetTextColor(appWidgetId, getApplicationContext()));
            row.setTextViewText(R.id.course_name, course.getName());

            if(!shouldHideDetails(getApplicationContext(), appWidgetId)) {
                row.setTextViewText(R.id.course_term, course.getTerm().getName());
            }

            if(course.isTeacher()){
                row.setViewVisibility(R.id.course_grade, View.GONE);
            } else {
                row.setViewVisibility(R.id.course_grade, View.VISIBLE);
                row.setTextViewText(R.id.course_grade, course.getCurrentScore() + "%");
                row.setTextColor(R.id.course_grade, CanvasContextColor.getCachedColor(mContext, course));
            }
            row.setOnClickFillInIntent(R.id.widget_root, createIntent(course));

            row.setInt(R.id.course_indicator,"setColorFilter", CanvasContextColor.getCachedColor(mContext, course));
        }

        @Override
        protected void clearViewData(RemoteViews row) {
            row.setTextViewText(R.id.course_grade,"");
            row.setTextViewText(R.id.course_term, "");
            row.setTextViewText(R.id.courseName, "");
            row.setViewVisibility(R.id.course_indicator, View.GONE);
        }
    }
}



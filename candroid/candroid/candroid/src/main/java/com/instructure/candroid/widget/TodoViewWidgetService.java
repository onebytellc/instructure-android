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
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.instructure.candroid.R;
import com.instructure.candroid.activity.InterwebsToApplication;
import com.instructure.canvasapi.api.CalendarEventAPI;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.GroupAPI;
import com.instructure.canvasapi.api.ToDoAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.model.ScheduleItem;
import com.instructure.canvasapi.model.ToDo;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.pandautils.utils.CanvasContextColor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TodoViewWidgetService extends BaseRemoteViewsService implements Serializable  {

    public static Intent createIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, TodoViewWidgetService.class);
        intent.setAction(TodoWidgetProvider.REFRESH);
        intent.setData(Uri.fromParts("appWidgetId", String.valueOf(appWidgetId), null));
        return intent;
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TodoViewsFactory(this.getApplicationContext(), intent);
    }

	private class TodoViewsFactory extends CanvasWidgetRowFactory<ToDo> {

        private Intent intent;

		public TodoViewsFactory(Context context, Intent intent) {
			this.mContext = context;
            this.intent = intent;
		}

        @Override
        protected int giveMeAppWidgetId() {
            return getAppWidgetId(intent);
        }

        @Override
		public ToDo[] makeApiCalls() {

            // get courses, data and to do items
            Course[] courses = CourseAPI.getAllCoursesSynchronous(mContext);
            Group[] groups = GroupAPI.getAllGroupsSynchronous(mContext);

            CanvasContext userContext = CanvasContext.emptyUserContext();
            ToDo[] todos = ToDoAPI.getTodosSynchronous(getApplicationContext(), userContext);
            ScheduleItem[] scheduleItems = CalendarEventAPI.getUpcomingEventsSynchronous(getApplicationContext());

            if(courses == null || groups == null || todos == null || scheduleItems == null){
                return null;
            }

            Map<Long, Course> courseMap = CourseAPI.createCourseMap(courses);
            Map<Long, Group> groupMap = GroupAPI.createGroupMap(groups);
            ArrayList<ToDo> todosList = new ArrayList<ToDo>(Arrays.asList(todos));

            ArrayList<ToDo> upcomingList = new ArrayList<ToDo>();
            List<ScheduleItem> items = Arrays.asList (scheduleItems);
            for(ScheduleItem scheduleItem : items) {
                //As a user
                upcomingList.add(ToDo.toDoWithScheduleItem(scheduleItem));
            }

            ArrayList<ToDo> eventArrayList = ToDoAPI.mergeToDoUpcoming(todosList,upcomingList);
           ToDo[] events = new ToDo[eventArrayList.size()];
            events = eventArrayList.toArray(events);


            for(ToDo todo : events){
                ToDo.setContextInfo(todo, courseMap, groupMap);
            }

            return events;
        }

        @Override
        public int getLayoutId() {
            if(intent != null) {
                if (shouldHideDetails(getApplicationContext(), getAppWidgetId(intent))) {
                    return R.layout.listview_widget_todo_minimum_item_row;
                }
            }
            return R.layout.listview_widget_todo_item_row;
        }

        @Override
        public void setViewData(ToDo event, RemoteViews row) {

            row.setViewVisibility(R.id.icon, View.VISIBLE);


            if(event.getType() == ToDo.Type.UPCOMING_EVENT) {
                row.setImageViewResource(R.id.icon, R.drawable.ic_cv_calendar_fill);
            } else if (event.getAssignment().getQuizId() > 0) {
                row.setImageViewResource(R.id.icon, R.drawable.ic_cv_quizzes_fill);
            } else if (event.getAssignment().getDiscussionTopicHeader() != null) {
                row.setImageViewResource(R.id.icon, R.drawable.ic_cv_discussions_fill);
            } else {
                row.setImageViewResource(R.id.icon, R.drawable.ic_cv_assignments_fill);
            }

            if(event.getCanvasContext() != null && event.getCanvasContext().getType() != CanvasContext.Type.USER){
                row.setInt(R.id.icon,"setColorFilter", CanvasContextColor.getCachedColor(mContext, event.getCanvasContext()));
            } else {
                row.setInt(R.id.icon,"setColorFilter", R.color.canvasRed);
            }

            int appWidgetId = getAppWidgetId(intent);
            row.setTextColor(R.id.title, getWidgetTextColor(appWidgetId, getApplicationContext()));

            String title = event.getTitle();
            row.setTextViewText(R.id.title, title);

            if(shouldHideDetails(getApplicationContext(), appWidgetId)) {
                if(event.getDueDate() != null) {
                    String formattedDueDate = DateHelpers.getDateTimeString(mContext, event.getDueDate());
                    row.setTextViewText(R.id.message, formattedDueDate);
                    row.setViewVisibility(R.id.message, View.VISIBLE);
                } else {
                    row.setViewVisibility(R.id.message, View.GONE);
                }
            } else {
                String message = formatDetailsString(event, getCourseCode(event));
                if(!TextUtils.isEmpty(message)) {
                    row.setTextViewText(R.id.message, message);
                    row.setViewVisibility(R.id.message, View.VISIBLE);
                } else {
                    row.setViewVisibility(R.id.message, View.GONE);
                }

                Date dueDate = event.getDueDate();
                if (dueDate != null) {
                    String  formattedDueDate = DateHelpers.getDateTimeString(mContext, event.getDueDate());
                    row.setTextViewText(R.id.course_and_date, formattedDueDate);
                    row.setViewVisibility(R.id.course_and_date, View.VISIBLE);
                } else {
                    row.setViewVisibility(R.id.course_and_date, View.GONE);
                }
            }

            //get assignment description
            row.setOnClickFillInIntent(R.id.widget_root, createIntent(event));
        }

        @Override
        protected void clearViewData(RemoteViews row) {
            row.setTextViewText(R.id.course_and_date, "");
            row.setTextViewText(R.id.message, "");
            row.setTextViewText(R.id.title, "");
            row.setViewVisibility(R.id.icon, View.GONE);

        }

        protected Intent createIntent(ToDo event) {
            //Its possible we cant route to to do so we just go to the list.
            String url = "";
            if (event.getAssignment() != null) {
                //Launch assignment details fragment.
                url = event.getAssignment().getHtmlUrl();
            } else if (event.getScheduleItem() != null) {
                //It's a Calendar event from the Upcoming API.
                url = event.getScheduleItem().getHtmlUrl();
            }

            Uri uri = Uri.parse(url);
            return InterwebsToApplication.createIntent(mContext, uri);
        }

        ////////////////////////////////////////////////////////////////////////////////////////
        // Helper Methods
        ////////////////////////////////////////////////////////////////////////////////////////

        private @Nullable String formatDetailsString(ToDo toDo, String courseCode) {
            String todoDetails;
            switch (toDo.getType()) {
                case GRADING:
                    todoDetails = toDo.getNeedsGradingCount() + " ";
                    if (toDo.getNeedsGradingCount() == 1) {
                        todoDetails += getResources().getString(R.string.toDoNeedsGrading);
                    } else {
                        todoDetails += getResources().getString(R.string.toDoNeedGrading);
                    }
                    todoDetails += " - " + courseCode;
                    break;
                default:
                    todoDetails = courseCode;
                    break;
            }

            return todoDetails;
        }

        private @Nullable String getCourseCode(ToDo event) {
            if(event == null || event.getCanvasContext() == null || event.getCanvasContext().getName() == null ){
                return null;
            }
            return  event.getCanvasContext().getName();
        }
    }
}


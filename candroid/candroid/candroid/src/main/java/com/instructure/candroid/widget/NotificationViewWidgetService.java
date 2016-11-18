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
import android.text.Html;
import android.view.View;
import android.widget.RemoteViews;
import com.instructure.candroid.R;
import com.instructure.candroid.activity.NotificationWidgetRouter;
import com.instructure.candroid.util.StringUtilities;
import com.instructure.canvasapi.api.ConversationAPI;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.GroupAPI;
import com.instructure.canvasapi.api.StreamAPI;
import com.instructure.canvasapi.model.*;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.pandautils.utils.CanvasContextColor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class NotificationViewWidgetService extends BaseRemoteViewsService implements Serializable {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new NotificationsRowFactory(this.getApplicationContext(), intent);
    }

    public static Intent createIntent(Context mContext, int appWidgetId) {
        Intent intent = new Intent(mContext, NotificationViewWidgetService.class);
        intent.setAction(NotificationWidgetProvider.REFRESH);
        intent.setData(Uri.fromParts("appWidgetId", String.valueOf(appWidgetId), null));
        return intent;
    }

    private class NotificationsRowFactory extends CanvasWidgetRowFactory <StreamItem>{

        private Intent intent;

        private final int numberToReturn = 25;
        public NotificationsRowFactory(Context context, Intent intent){
            this.mContext = context;
            this.intent = intent;
        }

        @Override
        protected int giveMeAppWidgetId() {
            return getAppWidgetId(intent);
        }

        @Override
        protected StreamItem[] makeApiCalls() {
            // get courses, data and to do items
            Course[] courses = CourseAPI.getAllCoursesSynchronous(mContext);
            Group[] groups = GroupAPI.getAllGroupsSynchronous(mContext);

            if(courses == null || groups == null){
                return null;
            }

            Map<Long, Course> courseMap = CourseAPI.createCourseMap(courses);
            Map<Long, Group> groupMap = GroupAPI.createGroupMap(groups);



            ArrayList<StreamItem> streamItemArrayList;
            StreamItem[] streamItemsArray = StreamAPI.getUserStreamSynchronous(mContext, numberToReturn);
            //If the API returns a null array we need to return a non-null array or we will have NP crash later.
            if(streamItemsArray == null){
                return new StreamItem[0];
            }
            streamItemArrayList = new ArrayList<StreamItem>(Arrays.asList(streamItemsArray));

            Collections.sort(streamItemArrayList);
            Collections.reverse(streamItemArrayList);
            StreamItem[] streamItems = new StreamItem[streamItemArrayList.size()];

           streamItems = streamItemArrayList.toArray(streamItems);

            populateActivityStreamAdapter(courseMap, groupMap, streamItems);

            return streamItems;
        }

        @Override
        protected int getLayoutId(){
            if(intent != null) {
                int appWidgetId = Integer.valueOf(intent.getData().getSchemeSpecificPart());
                if (shouldHideDetails(getApplicationContext(), appWidgetId)) {
                    return R.layout.listview_widget_notifications_minimum_item_row;
                }
            }
            return R.layout.listview_widget_notifications_item_row;

        }

        @Override
        protected void setViewData(StreamItem streamItem, RemoteViews row){

            int appWidgetId = getAppWidgetId(intent);

            row.setViewVisibility(R.id.icon, View.VISIBLE);
            row.setImageViewResource(R.id.icon, getDrawableId(streamItem));

            row.setTextViewText(R.id.title, streamItem.getTitle(mContext));
            row.setTextColor(R.id.title, getWidgetTextColor(appWidgetId, getApplicationContext()));

            if(streamItem.getCanvasContext() != null && streamItem.getCanvasContext().getType() != CanvasContext.Type.USER){
                row.setInt(R.id.icon,"setColorFilter", CanvasContextColor.getCachedColor(mContext, streamItem.getCanvasContext()));
            } else if(streamItem.getType() == StreamItem.Type.CONVERSATION){
                row.setInt(R.id.icon,"setColorFilter", getWidgetTextColor(appWidgetId, mContext));
            } else {
                row.setInt(R.id.icon,"setColorFilter", R.color.canvasRed);
            }

            if(!shouldHideDetails(getApplicationContext(), appWidgetId)) {
                if (streamItem.getMessage(mContext) != null) {
                    row.setTextViewText(R.id.message, StringUtilities.simplifyHTML(Html.fromHtml(streamItem.getMessage(mContext))));
                } else {
                    row.setTextViewText(R.id.message, "");
                    row.setViewVisibility(R.id.message, View.GONE);
                }
            }

            String courseAndDate = "";
            if (streamItem.getContextType() == CanvasContext.Type.COURSE && streamItem.getCanvasContext() != null) {
                courseAndDate = streamItem.getCanvasContext().getSecondaryName() + " ";
            }
            courseAndDate += DateHelpers.getDateTimeString(mContext, streamItem.getUpdatedAtDate());
            row.setTextViewText(R.id.course_and_date, courseAndDate);

            row.setOnClickFillInIntent(R.id.widget_root, createIntent(streamItem));

        }

        @Override
        protected Intent createIntent(StreamItem streamItem) {
           return NotificationWidgetRouter.createIntent(mContext, streamItem);
        }

        @Override
        protected void clearViewData(RemoteViews row) {
            row.setTextViewText(R.id.course_and_date, "");
            row.setTextViewText(R.id.message, "");
            row.setTextViewText(R.id.title, "");
            row.setViewVisibility(R.id.icon, View.GONE);
        }

        private   int getDrawableId(StreamItem streamItem) {
            switch (streamItem.getType()) {
                case DISCUSSION_TOPIC:
                    return  R.drawable.ic_cv_discussions_fill;

                case ANNOUNCEMENT:
                    return R.drawable.ic_cv_announcements_fill;

                case SUBMISSION:
                    return R.drawable.ic_cv_assignments_fill;

                case CONVERSATION:
                    return R.drawable.ic_cv_messages_fill;

                case MESSAGE:
                    //a message could be related to an assignment, check the category

                    if(streamItem.getContextType() == CanvasContext.Type.COURSE) {
                        return R.drawable.ic_cv_assignments_fill;
                    } else if(streamItem.getNotificationCategory().toLowerCase().contains("assignment graded")) {
                        return R.drawable.ic_cv_grades_fill;
                    } else {
                        return R.drawable.ic_cv_user_fill;
                    }

                case CONFERENCE:
                    return R.drawable.ic_cv_conference_fill;
                case COLLABORATION:
                    return R.drawable.ic_cv_collaboration_fill;
                case COLLECTION_ITEM:
                default:
                    break;
            }

            return R.drawable.ic_cv_announcements_fill;

        }

        public void populateActivityStreamAdapter(Map<Long, Course> courseMap, Map<Long, Group> groupMap, StreamItem[] streamItems) {
            // wait until both calls return;
            if (courseMap == null || groupMap == null || streamItems == null) {
                return;
            }

            for (final StreamItem streamItem : streamItems) {
                streamItem.setCanvasContextFromMap(courseMap, groupMap);

                // load conversations if needed
                if (streamItem.getType() == StreamItem.Type.CONVERSATION) {

                    Conversation conversation = ConversationAPI.getDetailedConversationSynchronous(mContext, streamItem.getConversationId());

                    streamItem.setConversation(mContext, conversation, APIHelpers.getCacheUser(mContext).getId(), mContext.getResources().getString(R.string.monologue));

                }
            }
        }
    }
}

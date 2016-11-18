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

package com.instructure.candroid.util;

import android.content.Context;

import com.instructure.candroid.R;
import com.instructure.candroid.model.NotificationCategoryHeader;
import com.instructure.canvasapi.model.NotificationPreference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationPreferenceUtils {

    public enum CATEGORIES {COURSE_ACTIVITIES, DISCUSSIONS, CONVERSATIONS, SCHEDULING, GROUPS, ALERTS, NONE}

    public static Map<String, String> loadSubCategoryTitleMap(Context context) {
        Map<String, String> map = new HashMap<>();

        map.put("due_date", context.getString(R.string.notification_pref_due_date));
        map.put("grading_policies", context.getString(R.string.notification_pref_grading_policies));
        map.put("course_content", context.getString(R.string.notification_pref_course_content));
        map.put("files", context.getString(R.string.notification_pref_files));
        map.put("announcement", context.getString(R.string.notification_pref_announcement));
        map.put("announcement_created_by_you", context.getString(R.string.notification_pref_announcement_created_by_you));
        map.put("grading", context.getString(R.string.notification_pref_grading));
        map.put("invitation", context.getString(R.string.notification_pref_invitation));
        map.put("all_submissions", context.getString(R.string.notification_pref_all_submissions));
        map.put("late_grading", context.getString(R.string.notification_pref_late_grading));
        map.put("submission_comment", context.getString(R.string.notification_pref_submission_comment));

        //DISCUSSIONS
        map.put("discussion", context.getString(R.string.notification_pref_discussion));
        map.put("discussion_entry", context.getString(R.string.notification_pref_discussion_post));

        //CONVERSATIONS
        map.put("added_to_conversation", context.getString(R.string.notification_pref_add_to_conversation));
        map.put("conversation_message", context.getString(R.string.notification_pref_conversation_message));
        map.put("conversation_created", context.getString(R.string.notification_pref_conversations_created_by_you));

        //SCHEDULING
        map.put("student_appointment_signups", context.getString(R.string.notification_pref_student_appointment_signups));
        map.put("appointment_signups", context.getString(R.string.notification_pref_appointment_signups));
        map.put("appointment_cancelations", context.getString(R.string.notification_pref_appointment_cancelations));
        map.put("appointment_availability", context.getString(R.string.notification_pref_appointment_availability));
        map.put("calendar", context.getString(R.string.notification_pref_calendar));

        //GROUPS
        map.put("membership_update", context.getString(R.string.notification_pref_membership_update));

        //ALERTS
//        map.put("alert", context.getString(R.string.notification_pref_admin));
//
//        map.put("migration", "? migration");
//        map.put("summaries", "? summaries");
//        map.put("other", "? other");
//        map.put("registration", "? registration");
//
//        //Deprecated
//        map.put("reminder", "");
        return map;
    }

    public static Map<String, SubCategorySortingHelper> loadCategoryMap() {

        Map<String, SubCategorySortingHelper> map = new HashMap<>();

        //COURSE ACTIVITIES
        map.put("due_date", new SubCategorySortingHelper(CATEGORIES.COURSE_ACTIVITIES, 1));
        map.put("grading_policies", new SubCategorySortingHelper(CATEGORIES.COURSE_ACTIVITIES, 2));
        map.put("course_content", new SubCategorySortingHelper(CATEGORIES.COURSE_ACTIVITIES, 3));
        map.put("files", new SubCategorySortingHelper(CATEGORIES.COURSE_ACTIVITIES, 4));
        map.put("announcement", new SubCategorySortingHelper(CATEGORIES.COURSE_ACTIVITIES, 5));
        map.put("announcement_created_by_you", new SubCategorySortingHelper(CATEGORIES.COURSE_ACTIVITIES, 6));
        map.put("grading", new SubCategorySortingHelper(CATEGORIES.COURSE_ACTIVITIES, 7));
        map.put("invitation", new SubCategorySortingHelper(CATEGORIES.COURSE_ACTIVITIES, 8));
        map.put("all_submissions", new SubCategorySortingHelper(CATEGORIES.COURSE_ACTIVITIES, 9));
        map.put("late_grading", new SubCategorySortingHelper(CATEGORIES.COURSE_ACTIVITIES, 10));
        map.put("submission_comment", new SubCategorySortingHelper(CATEGORIES.COURSE_ACTIVITIES, 11));

        //DISCUSSIONS
        map.put("discussion", new SubCategorySortingHelper(CATEGORIES.DISCUSSIONS, 1));
        map.put("discussion_entry", new SubCategorySortingHelper(CATEGORIES.DISCUSSIONS, 2));

        //CONVERSATIONS
        map.put("added_to_conversation", new SubCategorySortingHelper(CATEGORIES.CONVERSATIONS, 1));
        map.put("conversation_message", new SubCategorySortingHelper(CATEGORIES.CONVERSATIONS, 2));
        map.put("conversation_created", new SubCategorySortingHelper(CATEGORIES.CONVERSATIONS, 3));

        //SCHEDULING
        map.put("student_appointment_signups", new SubCategorySortingHelper(CATEGORIES.SCHEDULING, 1));
        map.put("appointment_signups", new SubCategorySortingHelper(CATEGORIES.SCHEDULING, 2));
        map.put("appointment_cancelations", new SubCategorySortingHelper(CATEGORIES.SCHEDULING, 3));
        map.put("appointment_availability", new SubCategorySortingHelper(CATEGORIES.SCHEDULING, 4));
        map.put("calendar", new SubCategorySortingHelper(CATEGORIES.SCHEDULING, 5));

        //GROUPS
        map.put("membership_update", new SubCategorySortingHelper(CATEGORIES.GROUPS, 1));

        //ALERTS
//        map.put("alert", new SubCategorySortingHelper(CATEGORIES.ALERTS, 1));
//        map.put("migration", new SubCategorySortingHelper(CATEGORIES.ALERTS, 1));
//        map.put("registration", new SubCategorySortingHelper(CATEGORIES.ALERTS, 1));
//        map.put("summaries", new SubCategorySortingHelper(CATEGORIES.ALERTS, 1));
//        map.put("other", new SubCategorySortingHelper(CATEGORIES.ALERTS, 1));

        //Deprecated
        map.put("reminder", new SubCategorySortingHelper(CATEGORIES.NONE, 1));
        return map;
    }

    public static List<NotificationCategoryHeader> getCategoryHeaders(Context context) {
        List<NotificationCategoryHeader> headers = new ArrayList<>();

        NotificationCategoryHeader header = new NotificationCategoryHeader();
        header.title = context.getString(R.string.notification_cat_course_activities);
        header.headerCategory = NotificationPreferenceUtils.CATEGORIES.COURSE_ACTIVITIES;
        header.position = 0;
        headers.add(header);

        header = new NotificationCategoryHeader();
        header.title = context.getString(R.string.notification_cat_discussions);
        header.headerCategory = NotificationPreferenceUtils.CATEGORIES.DISCUSSIONS;
        header.position = 1;
        headers.add(header);

        header = new NotificationCategoryHeader();
        header.title = context.getString(R.string.notification_cat_conversations);
        header.headerCategory = NotificationPreferenceUtils.CATEGORIES.CONVERSATIONS;
        header.position = 2;
        headers.add(header);

        header = new NotificationCategoryHeader();
        header.title = context.getString(R.string.notification_cat_scheduling);
        header.headerCategory = NotificationPreferenceUtils.CATEGORIES.SCHEDULING;
        header.position = 3;
        headers.add(header);

        header = new NotificationCategoryHeader();
        header.title = context.getString(R.string.notification_cat_groups);
        header.headerCategory = NotificationPreferenceUtils.CATEGORIES.GROUPS;
        header.position = 4;
        headers.add(header);

//        header = new NotificationCategoryHeader();
//        header.title = context.getString(R.string.notification_cat_alerts);
//        header.headerCategory = NotificationPreferenceUtils.CATEGORIES.ALERTS;
//        headers.add(header);
        return headers;
    }

    // Used to match web sorting
    public static class SubCategorySortingHelper {
        public CATEGORIES categories;
        public int position;
        public SubCategorySortingHelper(CATEGORIES categories, int position) {
            this.categories = categories;
            this.position = position;
        }
    }

    public static ArrayList<NotificationPreference> getCleanedNotifications(List<NotificationPreference> preferences, String frequency) {
        ArrayList<NotificationPreference> notifications = new ArrayList<>();
        for(NotificationPreference preference : preferences) {
            if(isValid(preference)) {
                preference.frequency = frequency;
                notifications.add(preference);
            }
        }
        return notifications;
    }

    private static boolean isValid(NotificationPreference preference) {
        //These types are no longer supported and should not be enabled.
        if("registration".equals(preference.category) ||
                "summaries".equals(preference.category) ||
                "other".equals(preference.category) ||
                "migration".equals(preference.category) ||
                "alert".equals(preference.category) ||
                "reminder".equals(preference.category) ||
                "recording_ready".equals(preference.category)) {
            return false;
        }
        return true;
    }
}

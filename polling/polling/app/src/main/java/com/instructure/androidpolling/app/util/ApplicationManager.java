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

package com.instructure.androidpolling.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.Fragment;
import android.view.ViewConfiguration;

import com.instructure.canvasapi.api.OAuthAPI;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Section;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.FileUtilities;
import com.instructure.canvasapi.utilities.LinkHeaders;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import retrofit.client.Response;

public class ApplicationManager extends MultiDexApplication {

    private static final String COURSE_MAP = "course_map";
    private static final String SECTION_MAP = "section_map";

    public final static String PREF_FILE_NAME = "polling";
    public final static String OTHER_SIGNED_IN_USERS_PREF_NAME = "poll_other_signed_in_users";
    public final static String PREF_NAME_PREVIOUS_DOMAINS = "poll_name_prev_domains";
    public final static String MULTI_SIGN_IN_PREF_NAME = "poll_multi_pref_name";


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * This is pretty hacky, but it tells the device (Samsung) that there isn't a menu button
         * so that we will have an overflow menu. This is important in our design since the first
         * screen teachers will see is an image pointing to the + sign that lets them add a question.
         *
         * This is/was against android design guidelines, but they've changed it in 4.4, See
         * http://stackoverflow.com/questions/20444596/how-to-force-action-bar-overflow-icon-to-show
         */
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Google Analytics
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
    }

    public static void trackActivity(Activity activity) {
        //TODO: re-add analytics if we work on this thing again
    }

    public static void trackFragment(Activity activity, Fragment fragment) {
        //TODO: re-add analytics if we work on this thing again
    }
    /**
     * Log out the currently signed in user. Permanently remove credential information.
     *
     * @return
     */
    public boolean logoutUser() {

        //It is possible for multiple APIs to come back 'simultaneously' as HTTP401s causing a logout
        //if this has already ran, data is already cleared causing null pointer exceptions
        if (APIHelpers.getToken(this) != null && !APIHelpers.getToken(this).equals("")) {

            //Delete token from server
            //We don't actually care about this coming back. Fire and forget.
            CanvasCallback<Response> deleteTokenCallback = new CanvasCallback<Response>(APIHelpers.statusDelegateWithContext(this)) {
                @Override
                public void cache(Response response) {
                }

                @Override
                public void firstPage(Response response, LinkHeaders linkHeaders, Response response2) {
                }
            };

            OAuthAPI.deleteToken(deleteTokenCallback);

            //Clear shared preferences,
            //Get the Shared Preferences
            SharedPreferences settings = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.clear();
            editor.commit();

            //Clear all Shared Preferences.
            APIHelpers.clearAllData(this);

        }

        return true;
    }

    public static void setHasTeacherEnrollment(Context context) {
        //Get the Shared Preferences
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.HAS_TEACHER_ENROLLMENT, true);
        editor.commit();
    }

    public static void setHasStudentEnrollment(Context context) {
        //Get the Shared Preferences
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(Constants.HAS_STUDENT_ENROLLMENT, true);
        editor.commit();
    }

    public static boolean hasTeacherEnrollment(Context context) {
        //Get the Shared Preferences
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        return settings.getBoolean(Constants.HAS_TEACHER_ENROLLMENT, false);
    }

    public static boolean hasStudentEnrollment(Context context) {
        //Get the Shared Preferences
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        return settings.getBoolean(Constants.HAS_STUDENT_ENROLLMENT, false);
    }

    public static boolean hasViewPreference(Context context) {
        //Get the Shared Preferences
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        if(settings.contains(Constants.SHOW_TEACHER_VIEW)) {
            return true;
        }
        else {
            return false;
        }
    }
    public static boolean shouldShowTeacherView(Context context) {
        //Get the Shared Preferences
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        if(settings.getBoolean(Constants.SHOW_TEACHER_VIEW, false)) {
            return true;
        }
        else {
            return false;
        }

    }

    /**
     * Set whether the first view the user sees is the teacher view or student view
     */
    public static void setFirstView(Context context, boolean isTeacher) {
        //Get the Shared Preferences
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        if(isTeacher) {
            editor.putBoolean(Constants.SHOW_TEACHER_VIEW, true);
        }
        else {
            editor.putBoolean(Constants.SHOW_TEACHER_VIEW, false);
        }
        editor.commit();

    }
    public static File getAttachmentsDirectory(Context context) {
        File file;
        if (context.getExternalCacheDir() != null) {
            file = new File(context.getExternalCacheDir(), "attachments");
        } else {
            file = context.getFilesDir();
        }
        return file;

    }

    public static void saveCourses(Context context, Course[] courses) {

        FileUtilities.SerializableToFile(context, COURSE_MAP, courses);
    }

    public static void saveSections(Context context, Section[] sections, long courseId) {
        FileUtilities.SerializableToFile(context, SECTION_MAP + "_" + courseId, sections);
    }

    public static Course[] getCourseList(Context context) {
        return (Course[])FileUtilities.FileToSerializable(context, COURSE_MAP);
    }

    public static Section[] getSectionList(Context context, long courseId) {
        return (Section[])FileUtilities.FileToSerializable(context, SECTION_MAP + "_" + courseId);
    }


    /**
     *
     * Save and get the submitted poll ids. There isn't an api endpoint yet to determine that a user has already
     * submitted a poll, so we need to keep track of that ourselves.
     */
    public static void saveSubmittedPollIds(Context context, long poll_session_id) {
        ArrayList<Long> id_list = (ArrayList<Long>)FileUtilities.FileToSerializable(context, Constants.SUBMITTED_POLLS);
        if(id_list == null) {
            id_list = new ArrayList<Long>();
        }
        id_list.add(poll_session_id);
        FileUtilities.SerializableToFile(context, Constants.SUBMITTED_POLLS, id_list);

    }

    public static ArrayList<Long> getSubmittedPollIds(Context context) {
        ArrayList<Long> id_list = (ArrayList<Long>)FileUtilities.FileToSerializable(context, Constants.SUBMITTED_POLLS);
        if(id_list == null) {
            id_list = new ArrayList<Long>();
        }
        return id_list;
    }

    public static void savePollSubmission(Context context, long poll_session_id, long poll_choice_id) {
        //Get the Shared Preferences
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(Long.toString(poll_session_id), poll_choice_id);
        editor.apply();
    }

    public static long getPollSubmissionId(Context context, long poll_session_id) {
        //Get the Shared Preferences
        SharedPreferences settings = context.getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE);
        return settings.getLong(Long.toString(poll_session_id), -1);
    }

}

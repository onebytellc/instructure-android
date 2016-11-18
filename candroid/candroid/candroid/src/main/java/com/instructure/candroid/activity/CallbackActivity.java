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

package com.instructure.candroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.instructure.candroid.delegate.APIContract;
import com.instructure.canvasapi.api.AccountNotificationAPI;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.UserAPI;
import com.instructure.canvasapi.model.AccountNotification;
import com.instructure.canvasapi.model.CanvasColor;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Enrollment;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.canvasapi.utilities.Masquerading;
import com.instructure.canvasapi.utilities.UserCallback;
import com.instructure.loginapi.login.api.GlobalDataSyncAPI;
import com.instructure.loginapi.login.asynctasks.GlobalDataSyncGetTask;
import com.instructure.loginapi.login.asynctasks.GlobalDataSyncPostTask;
import com.instructure.loginapi.login.model.GlobalDataSync;
import com.instructure.loginapi.login.rating.RatingDialog;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;

import retrofit.client.Response;

/**
 * This class is responsible for handling any API requests that base activities may require.
 */
public abstract class CallbackActivity extends ParentActivity implements
        APIContract {

    protected UserCallback userCallback;
    protected CanvasCallback<Course[]> coursesCallback;
    protected CanvasCallback<Course[]> coursesNoCacheCallback;
    protected CanvasCallback<Enrollment[]> getUserEnrollments;
    protected CanvasCallback<AccountNotification[]> accountNotificationCallback;
    protected CanvasCallback<CanvasColor> courseColorsCallback;

    protected User mUser;
    protected boolean hasNonTeacherEnrollment = false;

    private boolean isSavedInstanceStateNull = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isSavedInstanceStateNull = (savedInstanceState == null);

        if(isSavedInstanceStateNull) {
            //Gets and caches the global data
            new GlobalDataSyncGetTask(this, new GlobalDataSyncGetTask.GlobalDataSyncCallbacks() {
                @Override
                public void globalDataResults(GlobalDataSync data) {
                    if(data != null) {
                        onProfileBackdropUpdate(data.data);
                    }
                }
            }, GlobalDataSyncAPI.NAMESPACE.MOBILE_CANVAS_USER_BACKDROP_IMAGE).execute();

            if (CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                new GlobalDataSyncGetTask(this, new GlobalDataSyncGetTask.GlobalDataSyncCallbacks() {
                    @Override
                    public void globalDataResults(GlobalDataSync data) {
                        if (data == null && !isFinishing()) {
                            showPushNotificationDialog();

                            //Sets a flag on the user to not show this dialog again in the future.
                            new GlobalDataSyncPostTask(getContext(),
                                    GlobalDataSyncAPI.NAMESPACE.MOBILE_CANVAS_USER_NOTIFICATION_STATUS_SETUP).execute(
                                    new GlobalDataSync(Boolean.toString(true)));
                        }
                    }
                }, GlobalDataSyncAPI.NAMESPACE.MOBILE_CANVAS_USER_NOTIFICATION_STATUS_SETUP).execute();
            }
        }

        RatingDialog.showRatingDialog(CallbackActivity.this, RatingDialog.APP_NAME.CANDROID);
    }

    public abstract void onProfileBackdropUpdate(String url);
    public abstract void showPushNotificationDialog();

    /**
     * Sometimes getUserSelf is not called in a callback (in onCreate) other times its called from a callback's first page
     *
     * In the case that it is called from within a callback, the booleans help make it so that there aren't duplicate callbacks
     *
     * Duplicate callbacks would look like the following
     *                                                                   Cache 1
     *                                   cache -> getCourseFavorites /
     *            cache -> getUserSelf /                             \   Network 1
     * getCourse /                     \
     *           \                       network -> getCourseFavorites / Cache 2`
     *            \                                                    \ Network 2
     *             \
     *              network -> getUserSelf / ... etc            Cache 3
     *                                     \                    Network 3
     *                                                          Cache 4
     *                                                          Network 4
     *
     * With the booleans and API chained methods, it'll avoid having the duplicate calls to the network, and will look like the following
     *
     *                                                                  Cache 1
     *                                   cache -> getCourseFavorites /
     *            cache -> getUserSelf /
     * getCourse /
     *           \
     *            \
     *             \
     *              network -> getUserSelf
     *                                     \
     *                                      network -> getCourseFavorites
     *                                                                   \
     *                                                                    network 1
     *
     * @param isWithinAnotherCallback Means that getUserSelf has been called from a callback (firstpage)
     * @param isCached Helps determine the path the call should take
     */
    public void getUserSelf(boolean isWithinAnotherCallback, boolean isCached) {
        setupCallbacks();
        setupListeners();
        UserAPI.getSelf(userCallback);
        if (isWithinAnotherCallback) {
            CourseAPI.getAllFavoriteCoursesChained(coursesCallback, isCached);
        } else {
            CourseAPI.getAllFavoriteCourses(coursesCallback);
        }
        getAccountNotifications(isWithinAnotherCallback, isCached);
    }

    // isCached is only used when isWithinAnotherCallback
    public void getAccountNotifications(boolean isWithinAnotherCallback, boolean isCached) {
        if (isWithinAnotherCallback) {
            AccountNotificationAPI.getAccountNotificationsChained(accountNotificationCallback, isCached);
        } else {
            AccountNotificationAPI.getAccountNotifications(accountNotificationCallback);
        }
    }

    @Override
    public void setupCallbacks() {

        getUserEnrollments = new CanvasCallback<Enrollment[]>(CallbackActivity.this) {
            @Override
            public void firstPage(Enrollment[] enrollments, LinkHeaders linkHeaders, Response response) {
                if(enrollments != null) {
                    gotEnrollments(enrollments);
                }
            }
        };

        userCallback = new UserCallback(CallbackActivity.this) {
            @Override
            public void cachedUser(User user) {
                //We don't load from cache on this because it will load the users avatar two times and cause world hunger.
                //but if we're masquerading we want to, because masquerading can't get user info, so we need to read it from
                //cache
                if(Masquerading.isMasquerading(CallbackActivity.this)) {
                    user(APIHelpers.getCacheUser(CallbackActivity.this), null);
                } else if(!CanvasRestAdapter.isNetworkAvaliable(CallbackActivity.this)) {
                    user(user, null);
                }
            }

            @Override
            public void user(User user, Response response) {
                if (user != null) {
                    mUser = user;
                    UserAPI.getColors(getApplicationContext(), courseColorsCallback);
                    UserAPI.getSelfEnrollments(getUserEnrollments);
                    onUserCallbackFinished(mUser);
                }
            }
        };

        coursesCallback = new CanvasCallback<Course[]>(this) {
            @Override
            public void firstPage(Course[] courses, LinkHeaders linkHeaders, Response response) {
                for(Course course: courses){
                    if(!course.isTeacher()){
                        hasNonTeacherEnrollment = true;
                        break;
                    }
                }
                onCourseFavoritesFinished(courses);
            }
        };

        coursesNoCacheCallback = new CanvasCallback<Course[]>(this) {
            @Override
            public void firstPage(Course[] courses, LinkHeaders linkHeaders, Response response) {
                for(Course course: courses){
                    if(!course.isTeacher()){
                        hasNonTeacherEnrollment = true;
                        break;
                    }
                }
                onCourseFavoritesFinished(courses);
            }

            @Override
            public void cache(Course[] courses, LinkHeaders linkHeaders, Response response) {

            }
        };

        accountNotificationCallback = new CanvasCallback<AccountNotification[]>(this) {
            @Override
            public void firstPage(AccountNotification[] accountNotificationsArray, LinkHeaders linkHeaders, Response response) {
                gotNotifications(accountNotificationsArray);
            }
        };

        courseColorsCallback = new CanvasCallback<CanvasColor>(this) {

            @Override
            public void firstPage(CanvasColor canvasColor, LinkHeaders linkHeaders, Response response) {
                if(response.getStatus() == 200) {
                    //Replaces the current cache with the updated fresh one from the api.
                    CanvasContextColor.addToCache(canvasColor);
                    //Sends a broadcast so the course grid can refresh it's colors if needed.
                    //When first logging in this will probably get called/return after the courses.
                    Intent intent = new Intent(Const.COURSE_THING_CHANGED);
                    Bundle extras = new Bundle();
                    extras.putBoolean(Const.COURSE_COLOR, true);
                    intent.putExtras(extras);
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                }
            }
        };
    }

    public abstract void gotEnrollments(Enrollment[] enrollments);
    public abstract void gotNotifications(AccountNotification[] accountNotifications);
    public abstract void onUserCallbackFinished(User user);

    public void onCourseFavoritesFinished(Course[] courses) {}

    @Override
    public void setupListeners() {}

    ///////////////////////////////////////////////////////////////////////////
    // APIStatusDelegate
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {
        if(userCallback != null && userCallback.isFinished()) {
            super.onCallbackFinished(source);
        }
    }
}

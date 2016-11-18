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

import android.app.Activity;
import android.view.View;

import com.instructure.candroid.binders.CourseGradeBinder;
import com.instructure.candroid.holders.CourseGradeViewHolder;
import com.instructure.candroid.interfaces.CourseAdapterToFragmentCallback;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.MGPUtils;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.TabAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Enrollment;
import com.instructure.canvasapi.model.Tab;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.LinkHeaders;

import java.util.HashMap;
import java.util.Map;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class CourseGradeRecyclerAdapter extends BaseListRecyclerAdapter<CanvasContext, CourseGradeViewHolder> {

    private CourseAdapterToFragmentCallback mAdapterToFragmentCallback;
    private CanvasCallback<Course[]> mFavoriteCoursesCallback;
    private Map<CanvasContext, Boolean> mGradesTabVisibilityStatus = new HashMap<>();

    public CourseGradeRecyclerAdapter(Activity context, CourseAdapterToFragmentCallback adapterToFragmentCallback) {
        super(context, CanvasContext.class);
        //Context passed in must be of type Activity
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        loadData();
    }

    @Override
    public CourseGradeViewHolder createViewHolder(View v, int viewType) {
        return new CourseGradeViewHolder(v);
    }

    @Override
    public void bindHolder(CanvasContext canvasContext, CourseGradeViewHolder holder, int position) {
        boolean gradesTabExists = true;
        if(mGradesTabVisibilityStatus.containsKey(canvasContext)) {
            gradesTabExists = mGradesTabVisibilityStatus.get(canvasContext);
        }

        boolean isAllGradingPeriodsShown = MGPUtils.isAllGradingPeriodsShown((Course)canvasContext);
        CourseGradeBinder.bind(getContext(), canvasContext, holder, gradesTabExists, isAllGradingPeriodsShown, mAdapterToFragmentCallback);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return CourseGradeViewHolder.holderResId();
    }

    @Override
    public void contextReady() {

    }

    @Override
    public void setupCallbacks() {
        mFavoriteCoursesCallback = new CanvasCallback<Course[]>(this) {

            @Override
            public void cache(Course[] courses, LinkHeaders linkHeaders, Response response) {
                if (size() == 0) {
                    for (final Course course : courses) {
                        TabAPI.getTabs(course, new CanvasCallback<Tab[]>(CourseGradeRecyclerAdapter.this) {
                            @Override
                            public void cache(Tab[] tabs, LinkHeaders linkHeaders, Response response) {
                                boolean gradesTabExists = false;
                                for (Tab tab : tabs) {
                                    //we need to check if the tab exists and hidden is false
                                    if(Tab.GRADES_ID.equals(tab.getTabId()) && !tab.isHidden()) {
                                        gradesTabExists = true;
                                        break;
                                    }
                                }
                                mGradesTabVisibilityStatus.put(course, gradesTabExists);
                                add(course);
                            }

                            @Override
                            public void firstPage(Tab[] tabs, LinkHeaders linkHeaders, Response response) {
                                cache(tabs, linkHeaders, response);
                            }
                        });
                    }
                }
            }

            @Override
            public void firstPage(Course[] courses, LinkHeaders linkHeaders, Response response) {
                Analytics.trackEnrollment((Activity)getContext(), courses);
                Analytics.trackDomain((Activity)getContext());
                setNextUrl(linkHeaders.nextURL);
                cache(courses, linkHeaders, response);
                mAdapterToFragmentCallback.onRefreshFinished();
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                if (retrofitError.getResponse() != null && !APIHelpers.isCachedResponse(retrofitError.getResponse()) || !CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                    getAdapterToRecyclerViewCallback().setIsEmpty(true);
                }
                return super.onFailure(retrofitError);
            }
        };
    }

    @Override
    public void loadData() {
        CourseAPI.getAllFavoriteCourses(mFavoriteCoursesCallback);
    }
}

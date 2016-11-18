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
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.instructure.candroid.R;
import com.instructure.candroid.util.LoggingUtility;
import com.instructure.canvasapi.api.TabAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.model.Tab;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.CanvasContextColor;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit.client.Response;

public class CourseNavigationAdapter extends BaseAdapter {

    public interface OnTabsLoaded{
        public void onTabsLoadFinished();
    }

    private int color;
    private CanvasContext canvasContext;
    private Context mContext;
    private LayoutInflater inflater;
    private OnTabsLoaded mCallback;

    private List<Tab> mItems = new ArrayList<>();
    private CanvasCallback<Tab[]> tabCallback;

    public CourseNavigationAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        mContext = context;
    }

    public void updateCanvasContext(CanvasContext canvasContext) {
        if(canvasContext != null && canvasContext.getId() != 0) {
            this.canvasContext = canvasContext;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null) {
            view = inflater.inflate(R.layout.actionbar_course_spinner, parent, false);
        }

        final TextView mTitle = (TextView) view.findViewById(R.id.text1);
        final TextView mSubTitle = (TextView) view.findViewById(R.id.text2);

        Tab tab = getItem(position);
        if(tab != null) {
            final String title = tab.getLabel();
            final String subTitle = getSubTitle();
            if (mTitle != null) {
                if (!TextUtils.isEmpty(title)) {
                    mTitle.setText(title);
                }
            }

            if (mSubTitle != null) {
                if (!TextUtils.isEmpty(subTitle)) {
                    mSubTitle.setText(subTitle);
                    mSubTitle.setVisibility(View.VISIBLE);
                } else {
                    mSubTitle.setVisibility(View.GONE);
                }
            }
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {

        Tab tab = getItem(position);

        view = inflater.inflate(R.layout.viewholder_course_navigation, parent, false);

        TextView text = (TextView)view.findViewById(R.id.title);
        ImageView image = (ImageView)view.findViewById(R.id.icon);

        text.setText(tab.getLabel());
        image.setImageDrawable(CanvasContextColor.getColoredDrawable(mContext, getIconResIdForTab(tab), color));

        return view;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Tab getItem(int position) {
        if(position < 0 || mItems.size() < position) {
            return null;
        }
        return mItems.get(position);
    }

    private void clear() {
        mItems.clear();
    }

    public Tab getTab(int position) {
        return getItem(position);
    }

    private void addItems(List<Tab> tabs) {
        clear();
        mItems.addAll(tabs);
        notifyDataSetChanged();

        if(mCallback != null) {
            mCallback.onTabsLoadFinished();
        }
    }

    public String getSubTitle() {
        return canvasContext == null ? "" : canvasContext.getName();
    }

    private static int getIconResIdForTab(Tab tab) {
        final String id = tab.getTabId();

        switch (id) {
            case Tab.HOME_ID:
                return R.drawable.ic_cv_home_fill;
            case Tab.GRADES_ID:
                return R.drawable.ic_cv_grades_fill;
            case Tab.SYLLABUS_ID:
                return R.drawable.ic_cv_syllabus_fill;
            case Tab.MODULES_ID:
                return R.drawable.ic_cv_modules_fill;
            case Tab.ASSIGNMENTS_ID:
                return R.drawable.ic_cv_assignments_fill;
            case Tab.QUIZZES_ID:
                return R.drawable.ic_cv_quizzes_fill;
            case Tab.PAGES_ID:
                return R.drawable.ic_cv_pages_fill;
            case Tab.ANNOUNCEMENTS_ID:
                return R.drawable.ic_cv_announcements_fill;
            case Tab.PEOPLE_ID:
                return R.drawable.ic_cv_user_fill;
            case Tab.COLLABORATIONS_ID:
                return R.drawable.ic_cv_collaboration_fill;
            case Tab.CONFERENCES_ID:
                return R.drawable.ic_cv_conference_fill;
            case Tab.DISCUSSIONS_ID:
                return R.drawable.ic_cv_discussions_fill;
            case Tab.OUTCOMES_ID:
                return R.drawable.ic_cv_outcomes_fill;
            case Tab.FILES_ID:
                return R.drawable.ic_cv_folder_fill;
            case Tab.NOTIFICATIONS_ID:
                return R.drawable.ic_cv_notifications_fill;
            case Tab.SETTINGS_ID:
                return R.drawable.ic_cv_settings_fill;
            case Tab.TYPE_EXTERNAL:
                return R.drawable.ic_cv_application_fill;
            default:
                return R.drawable.ic_cv_courses_fill;
        }
    }

    /**
     * Make sure that for LTI (External) tabs you get the actual tab id, External is not sufficient because there can be unlimited External Tabs.
     * @param tabId
     * @return
     */
    public int getTabPosition(final String tabId) {
        for(int i = 0; i < getCount(); i++) {
            if(getItem(i).getTabId().equals(tabId)) {
                return i;
            }
        }
        return -1;
    }

    public Tab getTab(final String tabId) {
        for(int i = 0; i < getCount(); i++) {
            if(getItem(i).getTabId().equals(tabId)) {
                return getItem(i);
            }
        }
        return null;
    }

    public void loadWithCanvasContext(final CanvasContext canvasContext, final OnTabsLoaded callback) {
        if (canvasContext instanceof User) {
            return;
        }
        mCallback = callback;

        //if the user goes into a course/group without data it will create a navigation adapter but not have any
        //tabs. If they get data and go back into the course right after it won't show anything, which is why the
        //getCount() method is added.
        if ((this.canvasContext == null && canvasContext != null && canvasContext.getId() != 0) ||
                (canvasContext != null && this.canvasContext != null && canvasContext.getId() != 0 &&
                        !this.canvasContext.getContextId().equals(canvasContext.getContextId())) || getCount() == 0) {

            this.canvasContext = canvasContext;
            setupCallbacks(mContext);
            color = CanvasContextColor.getCachedColor(mContext, canvasContext);
            TabAPI.getTabs(canvasContext, tabCallback);

        } else if (canvasContext != null) {
            color = CanvasContextColor.getCachedColor(mContext, canvasContext);
        }
    }

    public void setupCallbacks(final Context context) {
        tabCallback = new CanvasCallback<Tab[]>(APIHelpers.statusDelegateWithContext(context)) {

            public void handleTabs(Tab[] tabs, boolean cache){
                if(canvasContext != null){
                    LoggingUtility.Log(context, Log.DEBUG, "Course/Group: " + canvasContext.getName() + "(" + canvasContext.getId() + ")");
                }

                String source = cache ? "Cache: " : "FirstPage: ";

                LoggingUtility.Log(context, Log.DEBUG, source + Arrays.asList(tabs));
                formatAvailableTabs(context, tabs);
            }

            @Override
            public void cache(Tab[] tabs, LinkHeaders linkHeaders, Response response) {
                handleTabs(tabs, true);
            }

            @Override
            public void firstPage(Tab[] tabs, LinkHeaders linkHeaders, Response response) {
                handleTabs(tabs, false);
            }
        };
    }

    private void formatAvailableTabs(Context context, Tab[] result) {
        //result might be null if the API returns null
        if (result == null) {
            return;
        }

        ArrayList<Tab> tabs = new ArrayList<>();
        for(Tab tab : result) {
            //Excludes tabs that are hidden, see api for a huge lack of detail on what that means.
            if(!tab.isHidden()) {
                tabs.add(tab);
            }
        }

        //add course navigation in, which is not returned by the server
        tabs.add(Tab.newInstance(Tab.NOTIFICATIONS_ID, context.getString(R.string.Notifications)));
        addItems(tabs);
    }
}

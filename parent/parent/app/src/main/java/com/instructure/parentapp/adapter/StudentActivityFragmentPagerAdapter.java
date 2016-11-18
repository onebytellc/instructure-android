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

package com.instructure.parentapp.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.instructure.parentapp.R;
import com.instructure.parentapp.activity.StudentViewActivity;
import com.instructure.parentapp.fragments.AlertFragment;
import com.instructure.parentapp.fragments.CourseListFragment;
import com.instructure.parentapp.fragments.PageFragment;
import com.instructure.parentapp.fragments.WeekFragment;
import com.instructure.parentapp.util.ViewUtils;

import java.lang.ref.WeakReference;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class StudentActivityFragmentPagerAdapter extends FragmentStatePagerAdapter {
    final int PAGE_COUNT = 3;

    private int mTabTitles[] = new int[] { R.string.courses , R.string.week, R.string.alerts };
    private int mImageResId[] = new int[] { R.drawable.vd_courses, R.drawable.vd_calendar_month, R.drawable.vd_alert};
    private Context mContext;
    private int mUnreadCount;

    // http://stackoverflow.com/questions/8785221/retrieve-a-fragment-from-a-viewpager
    private SparseArray<WeakReference<Fragment>> mRegisteredFragments;

    private SparseArray<WeakReference<View>> mTabs;

    public StudentActivityFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mRegisteredFragments = new SparseArray<>();
        mTabs = new SparseArray<>();
        mContext = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        mRegisteredFragments.put(position, new WeakReference<>(fragment));
        return fragment;
    }

    @Nullable
    public Fragment getRegisteredFragment(int position) {
        WeakReference<Fragment> weakReference = mRegisteredFragments.get(position);
        if (weakReference != null) {
            return weakReference.get();
        }
        return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mRegisteredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 2){
            //alert view
            //get the fragment from the array if it's there
            if(mRegisteredFragments.size() == 3 && mRegisteredFragments.get(2).get() != null) {
                return mRegisteredFragments.get(2).get();
            }
            return AlertFragment.newInstance(((StudentViewActivity) mContext).getCurrentStudent());
        }

        if(position == 1) {
            //week view
            //get the fragment from the array if it's there
            if(mRegisteredFragments.size() == 3 && mRegisteredFragments.get(1).get() != null) {
                return mRegisteredFragments.get(1).get();
            }
            return WeekFragment.newInstance(((StudentViewActivity) mContext).getCurrentStudent());
        }
        if(position == 0){
            //course list fragment
            //get the fragment from the array if it's there
            if(mRegisteredFragments.size() == 3 && mRegisteredFragments.get(0).get() != null) {
                return mRegisteredFragments.get(0).get();
            }
            return CourseListFragment.newInstance(((StudentViewActivity) mContext) .getCurrentStudent());
        }

        return PageFragment.newInstance(position + 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return mContext.getString(mTabTitles[position]);
    }

    public WeekFragment getWeekFragment() {

        return (WeekFragment)getRegisteredFragment(1);
    }
    public CourseListFragment getCourseListFragment() {

        return (CourseListFragment)getRegisteredFragment(0);
    }
    public AlertFragment getAlertFragment(){
        return (AlertFragment)getRegisteredFragment(2);
    }

    public int getAlertFragmentUnreadCount() {
        return mUnreadCount;
    }

    public void setAlertFragmentUnreadCount(int unreadCount) {
        View alertView = mTabs.get(2).get();
        mUnreadCount = unreadCount;
        if(unreadCount > 0) {
            alertView.findViewById(R.id.unreadCountWrapper).setVisibility(View.VISIBLE);
            TextView unreadCountTextView = (TextView) alertView.findViewById(R.id.unreadCount);
            if(unreadCount > 9) {
                unreadCountTextView.setText(mContext.getString(R.string.greaterThan9));
            } else {
                unreadCountTextView.setText(String.valueOf(unreadCount));
            }
        } else {
            alertView.findViewById(R.id.unreadCountWrapper).setVisibility(View.GONE);
        }
    }

    public View  getTabView(int position) {
        // Given you have a custom layout in `res/layout/tab_layout.xml` with a TextView and ImageView
        View v = LayoutInflater.from(mContext).inflate(R.layout.tab_layout, null);
        TextView tv = (TextView) v.findViewById(R.id.tabName);
        tv.setText(ViewUtils.applyKerning(mContext.getString(mTabTitles[position]), .5f));
        //a11y!
        v.setContentDescription(mContext.getString(mTabTitles[position]));
        ImageView img = (ImageView) v.findViewById(R.id.icon);
        img.setImageResource(mImageResId[position]);
        img.setContentDescription(mContext.getString(mTabTitles[position]));
        mTabs.put(position, new WeakReference<>(v));
        return v;
    }
}
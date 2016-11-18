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

package com.roomorama.caldroid;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * MonthPagerAdapter holds 4 fragments, which provides fragment for current
 * month, previous month and next month. The extra fragment helps for recycle
 * fragments.
 */
public class MonthPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<DateGridFragment> fragments;

    // Lazily create the fragments
    public ArrayList<DateGridFragment> getFragments() {
        if (fragments == null) {
            fragments = new ArrayList<DateGridFragment>();
            for (int i = 0; i < getCount(); i++) {
                fragments.add(new DateGridFragment());
            }
        }
        return fragments;
    }

    public void setFragments(ArrayList<DateGridFragment> fragments) {
        this.fragments = fragments;
    }

    public MonthPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        DateGridFragment fragment = getFragments().get(position);
        return fragment;
    }

    @Override
    public int getCount() {
        // We need 4 gridviews for previous month, current month and next month,
        // and 1 extra fragment for fragment recycle
        return CaldroidFragment.NUMBER_OF_PAGES;
    }

}

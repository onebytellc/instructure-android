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

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.instructure.candroid.R;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.util.RouterUtils;

import java.util.Locale;

public class SearchableActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        if("com.google.android.gms.actions.SEARCH_ACTION".equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            routeQuery(query);
        } else {
            finish();
        }
    }

    public void routeQuery(String query) {

        if(TextUtils.isEmpty(query)) {
            finish();
        }

        query = query.toLowerCase(Locale.getDefault());

        if(getString(R.string.search_course).toLowerCase(Locale.getDefault()).contains(query)) {
            RouterUtils.routeToNavigationMenuItem(this, Navigation.NavigationPosition.COURSES);
        }

        else if(getString(R.string.notifications).toLowerCase(Locale.getDefault()).contains(query)) {
            RouterUtils.routeToNavigationMenuItem(this, Navigation.NavigationPosition.NOTIFICATIONS);
        }

        else if(getString(R.string.search_todo).toLowerCase(Locale.getDefault()).contains(query)) {
            RouterUtils.routeToNavigationMenuItem(this, Navigation.NavigationPosition.TODO);
        }

        else if(getString(R.string.inbox).toLowerCase(Locale.getDefault()).contains(query)) {
            RouterUtils.routeToNavigationMenuItem(this, Navigation.NavigationPosition.INBOX);
        }

        else if(getString(R.string.search_grades).toLowerCase(Locale.getDefault()).contains(query)) {
            RouterUtils.routeToNavigationMenuItem(this, Navigation.NavigationPosition.GRADES);
        }

        else if(getString(R.string.calendar).toLowerCase(Locale.getDefault()).contains(query)) {
            RouterUtils.routeToNavigationMenuItem(this, Navigation.NavigationPosition.CALENDAR);
        }

        finish();
    }
}

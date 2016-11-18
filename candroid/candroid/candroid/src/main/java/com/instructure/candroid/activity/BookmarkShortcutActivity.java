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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.instructure.candroid.R;
import com.instructure.candroid.fragment.BookmarksFragment;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.RouterUtils;
import com.instructure.canvasapi.model.Bookmark;
import com.instructure.loginapi.login.util.ColorUtils;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;

public class BookmarkShortcutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmarks_shortcut_activity_card_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.bookmarkShortcut);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(R.id.container, new BookmarksFragment(), BookmarksFragment.class.getSimpleName());
        ft.commitAllowingStateLoss();
    }

    public void bookmarkSelected(Bookmark bookmark) {

        //Log to GA
        Analytics.trackButtonPressed(this, "Bookmark Selected", null);
        Intent launchIntent = new Intent(this, LoginActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        launchIntent.putExtra(Const.BOOKMARK, bookmark.getName());
        launchIntent.putExtra(Const.URL, bookmark.getUrl());

        final int color = CanvasContextColor.getCachedColorForUrl(this, RouterUtils.getContextIdFromURL(bookmark.getUrl()));
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        Bitmap bitIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bookmark, options);


        Intent shortcutIntent = new Intent();
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, bookmark.getName());
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, ColorUtils.colorIt(color, bitIcon));
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
        setResult(RESULT_OK, shortcutIntent);
        finish();
    }
}

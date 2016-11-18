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
import android.view.View;

import com.instructure.candroid.binders.BookmarkBinder;
import com.instructure.candroid.holders.BookmarkViewHolder;
import com.instructure.candroid.interfaces.BookmarkAdapterToFragmentCallback;
import com.instructure.canvasapi.api.BookmarkAPI;
import com.instructure.canvasapi.model.Bookmark;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.LinkHeaders;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class BookmarkRecyclerAdapter extends BaseListRecyclerAdapter<Bookmark, BookmarkViewHolder> {

    private CanvasCallback<Bookmark[]> bookmarksCallback;
    private BookmarkAdapterToFragmentCallback<Bookmark> mAdapterToFragmentCallback;
    private boolean mIsShortcutActivity = false;

    public BookmarkRecyclerAdapter(Context context, boolean isShortcutActivity, BookmarkAdapterToFragmentCallback<Bookmark> mAdapterToFragmentCallback) {
        super(context, Bookmark.class);
        mIsShortcutActivity = isShortcutActivity;
        setItemCallback(new ItemComparableCallback<Bookmark>() {
            @Override
            public int compare(Bookmark o1, Bookmark o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }

            @Override
            public boolean areContentsTheSame(Bookmark item1, Bookmark item2) {
                return item1.getName().toLowerCase().equals(item2.getName().toLowerCase());
            }

            @Override
            public long getUniqueItemId(Bookmark bookmark) {
                return bookmark.getId();
            }
        });
        this.mAdapterToFragmentCallback = mAdapterToFragmentCallback;
        setupCallbacks();
        loadData();
    }

    @Override
    public void bindHolder(Bookmark bookmark, BookmarkViewHolder holder, int position) {
        BookmarkBinder.bind(getContext(), mIsShortcutActivity, holder, bookmark, mAdapterToFragmentCallback);
    }

    @Override
    public BookmarkViewHolder createViewHolder(View v, int viewType) {
        return new BookmarkViewHolder(v);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return BookmarkViewHolder.holderResId();
    }

    @Override
    public void contextReady() {

    }

    @Override
    public void setupCallbacks() {
        bookmarksCallback = new CanvasCallback<Bookmark[]>(this) {
            @Override
            public void firstPage(Bookmark[] bookmarks, LinkHeaders linkHeaders, Response response) {

                addAll(bookmarks);
                setNextUrl(linkHeaders.nextURL);

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
        BookmarkAPI.getBookmarks(bookmarksCallback);
    }
}

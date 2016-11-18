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

package com.instructure.candroid.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.candroid.R;
import com.instructure.candroid.activity.BookmarkShortcutActivity;
import com.instructure.candroid.adapter.BookmarkRecyclerAdapter;
import com.instructure.candroid.decorations.DividerDecoration;
import com.instructure.candroid.interfaces.BookmarkAdapterToFragmentCallback;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.RouterUtils;
import com.instructure.canvasapi.api.BookmarkAPI;
import com.instructure.canvasapi.model.Bookmark;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandarecycler.PandaRecyclerView;
import com.instructure.pandautils.utils.CanvasContextColor;

import retrofit.client.Response;

public class BookmarksFragment extends OrientationChangeFragment {

    private View mRootView;
    private PandaRecyclerView mRecyclerView;
    private BookmarkRecyclerAdapter mRecyclerAdapter;

    @Override
    public String getFragmentTitle() {
        return getString(R.string.bookmarksTitle);
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.MASTER;
    }

    @Override
    public boolean navigationContextIsCourse() {
        return false;
    }

    @Override
    public View populateView(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.bookmarks_fragment_layout, container, false);
        mRecyclerView = (PandaRecyclerView) mRootView.findViewById(R.id.listView);
        configureRecyclerAdapter();
        configureRecyclerView(mRootView, getContext(), mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView, R.string.no_bookmarks);
        mRecyclerView.addItemDecoration(new DividerDecoration(getContext()));
        mRecyclerView.setSelectionEnabled(false);
        return mRootView;
    }

    private void configureRecyclerAdapter() {
        if(mRecyclerAdapter == null) {
            mRecyclerAdapter = new BookmarkRecyclerAdapter(getContext(),
                    (getActivity() instanceof BookmarkShortcutActivity), new BookmarkAdapterToFragmentCallback<Bookmark>() {
                @Override
                public void onRowClicked(Bookmark bookmark, int position, boolean isOpenDetail) {
                    if(getActivity() instanceof BookmarkShortcutActivity) {
                        //Log to GA
                        Analytics.trackButtonPressed(getActivity(), "Bookmark selected from shortcut", null);
                        ((BookmarkShortcutActivity)getActivity()).bookmarkSelected(bookmark);
                    } else {
                        //Log to GA
                        Analytics.trackButtonPressed(getActivity(), "Bookmark selected from list", null);
                        RouterUtils.routeUrl(getActivity(), bookmark.getUrl(), true);
                    }
                }

                @Override
                public void onRefreshFinished() {
                    setRefreshing(false);
                }

                @Override
                public void onOverflowClicked(final Bookmark bookmark, int position, View v) {
                    //Log to GA
                    PopupMenu popup = new PopupMenu(getContext(), v);
                    popup.getMenuInflater().inflate(R.menu.edit_delete, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.menu_edit:
                                    //Log to GA
                                    editBookmark(bookmark);
                                    return true;
                                case R.id.menu_delete:
                                    //Log to GA
                                    deleteBookmark(bookmark);
                                    return true;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            });
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle(getFragmentTitle());
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }

    private void editBookmark(final Bookmark bookmark) {
        final int color = CanvasContextColor.getCachedColorForUrl(getContext(), RouterUtils.getContextIdFromURL(bookmark.getUrl()));
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
        builder.title(R.string.bookmarkEdit);
        builder.cancelable(true);
        builder.positiveText(R.string.done);
        builder.positiveColor(color);
        builder.customView(R.layout.dialog_bookmark, false);
        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                View view = dialog.getCustomView();
                EditText editText = (EditText) view.findViewById(R.id.bookmarkEditText);
                String text = editText.getText().toString();
                if(!TextUtils.isEmpty(text)) {
                    Bookmark bookmarkCopy = copyBookmark(bookmark);
                    bookmarkCopy.setName(text);
                    BookmarkAPI.update(bookmarkCopy, new CanvasCallback<Bookmark>(mRecyclerAdapter) {
                        @Override
                        public void cache(Bookmark bookmarks) {
                        }

                        @Override
                        public void firstPage(Bookmark bookmark1, LinkHeaders linkHeaders, Response response) {
                            if (response.getStatus() == 200 && apiCheck()) {
                                bookmark1.setCourseId(bookmark.getCourseId());
                                mRecyclerAdapter.add(bookmark1);
                                showToast(R.string.bookmarkUpdated);
                            }
                        }
                    });
                    super.onPositive(dialog);
                } else {
                    showToast(R.string.bookmarkTitleRequired);
                }
            }
        });
        MaterialDialog dialog = builder.build();
        View view = dialog.getCustomView();
        EditText editText = (EditText) view.findViewById(R.id.bookmarkEditText);
        editText.setText(bookmark.getName());
        editText.setSelection(editText.getText().length());
        dialog.show();
    }

    private Bookmark copyBookmark(Bookmark bookmark){
        Bookmark bookmarkCopy = new Bookmark();
        bookmarkCopy.setCourseId(bookmark.getCourseId());
        bookmarkCopy.setName(bookmark.getName());
        bookmarkCopy.setId(bookmark.getId());
        bookmarkCopy.setPosition(bookmark.getPosition());
        return bookmarkCopy;
    }

    private void deleteBookmark(final Bookmark bookmark) {
        final int color = CanvasContextColor.getCachedColorForUrl(getContext(), RouterUtils.getContextIdFromURL(bookmark.getUrl()));
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getContext());
        builder.title(R.string.bookmarkDelete);
        builder.content(bookmark.getName());
        builder.cancelable(true);
        builder.positiveText(R.string.logout_yes);
        builder.negativeText(R.string.logout_no);
        builder.positiveColor(color);
        builder.negativeColor(color);
        builder.callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                BookmarkAPI.deleteBookmark(bookmark, new CanvasCallback<Bookmark>(mRecyclerAdapter) {
                    @Override
                    public void cache(Bookmark bookmarks) {}

                    @Override
                    public void firstPage(Bookmark bookmark, LinkHeaders linkHeaders, Response response) {
                        if (apiCheck() && response.getStatus() == 200) {
                            mRecyclerAdapter.remove(bookmark);
                            showToast(R.string.bookmarkDeleted);
                        }
                    }
                });
                super.onPositive(dialog);
            }
        });
        builder.build().show();
    }
}

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

import com.instructure.candroid.binders.PageBinder;
import com.instructure.candroid.holders.PageViewHolder;
import com.instructure.candroid.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi.api.PageAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Page;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.CanvasContextColor;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class PageListRecyclerAdapter  extends BaseListRecyclerAdapter<Page, PageViewHolder> {
    public static final String FRONT_PAGE_DETERMINER = "";
    private CanvasCallback<Page[]> mPageListCallback;
    private CanvasContext mCanvasContext;
    private AdapterToFragmentCallback<Page> mAdapterToFragmentCallback;
    private String mSelectedPageTitle = FRONT_PAGE_DETERMINER; // Page urls only specify the title, not the pageId
    private int mCourseColor;

    /* This is the real constructor and should be called to create instances of this adapter */
    public PageListRecyclerAdapter(Context context, CanvasContext canvasContext, AdapterToFragmentCallback<Page> adapterToFragmentCallback, String selectedPageTitle) {
        this(context, canvasContext, adapterToFragmentCallback, selectedPageTitle, true);
        mCourseColor = CanvasContextColor.getCachedColor(context, canvasContext);
    }

    /* This overloaded constructor is for testing purposes ONLY, and should not be used to create instances of this adapter. */
    protected PageListRecyclerAdapter(Context context, CanvasContext canvasContext, AdapterToFragmentCallback<Page> adapterToFragmentCallback, String selectedPageTitle, boolean isLoadData) {
        super(context, Page.class);
        setItemCallback(new ItemComparableCallback<Page>() {
            @Override
            public int compare(Page o1, Page o2) {
                return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
            }

            @Override
            public boolean areContentsTheSame(Page item1, Page item2) {
                return item1.getTitle().equals(item2.getTitle());
            }

            @Override
            public long getUniqueItemId(Page page) {
                return page.getPageId();
            }
        });
        mSelectedPageTitle = selectedPageTitle;
        mCanvasContext = canvasContext;
        mAdapterToFragmentCallback = adapterToFragmentCallback;

        if(isLoadData){
            loadData();
        }
    }

    @Override
    public PageViewHolder createViewHolder(View v, int viewType) {
        return new PageViewHolder(v);
    }

    @Override
    public void bindHolder(Page page, PageViewHolder holder, int position) {
        PageBinder.bind(getContext(), holder, page, mCourseColor, mAdapterToFragmentCallback);
    }

    @Override
    public int itemLayoutResId(int viewType) {
        return PageViewHolder.holderResId();
    }

    @Override
    public void contextReady() {

    }

    @Override
    public void add(Page item) {
        if  (mSelectedPageTitle != null) {
            if (mSelectedPageTitle.equals(item.getUrl())) {
                setSelectedItemId(item.getPageId());
            } else if (mSelectedPageTitle.equals(FRONT_PAGE_DETERMINER) && item.isFrontPage()) {
                setSelectedItemId(item.getPageId());
            }
        }
        super.add(item);
    }

    // region Pagination

    @Override
    public boolean isPaginated() {
        return true;
    }

    @Override
    public void setupCallbacks() {
        mPageListCallback = new CanvasCallback<Page[]>(this) {
            @Override
            public void firstPage(Page[] pages, LinkHeaders linkHeaders, Response response) {
                setNextUrl(linkHeaders.nextURL);
                addAll(pages);
                mAdapterToFragmentCallback.onRefreshFinished();

            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                // When a course has a page set as the home screen that the user first sees but the teacher
                // hides the pages tab the user will see a 404 error every time they go into the course. There
                // isn't anything the user can do differently, so just suppress the error crouton.
                if (retrofitError.getResponse() != null && !APIHelpers.isCachedResponse(retrofitError.getResponse()) || !CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                    getAdapterToRecyclerViewCallback().setIsEmpty(true);
                }
                return true;
            }
        };
    }

    @Override
    public void loadFirstPage() {
        PageAPI.getFirstPagePages(mCanvasContext, mPageListCallback);
    }

    @Override
    public void loadNextPage(String nextURL) {
        PageAPI.getNextPagePages(nextURL, mPageListCallback);
    }

    // endregion
}

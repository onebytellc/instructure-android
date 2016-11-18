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

package com.instructure.candroid.interfaces;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.instructure.pandarecycler.BaseRecyclerAdapter;
import com.instructure.pandarecycler.PandaRecyclerView;

public interface ConfigureRecyclerView {

    //As Grid
    void configureRecyclerViewAsGrid(
            View rootView,
            BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId);

    void configureRecyclerViewAsGrid(
            View rootView,
            BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            int emptyViewStringResId,
            int span);

    void configureRecyclerViewAsGrid(
            View rootView,
            BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            int emptyViewStringResId,
            Drawable...emptyImage);

    void configureRecyclerViewAsGrid(
            View rootView,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            int emptyViewStringResId,
            View.OnClickListener emptyImageClickListener,
            Drawable...emptyImage);

    void configureRecyclerViewAsGrid(
            View rootView,
            BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            int emptyViewStringResId,
            int span,
            View.OnClickListener emptyImageClickListener,
            Drawable...emptyImage);

    //As List
    PandaRecyclerView configureRecyclerView(
            View rootView,
            Context context,
            BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            String emptyViewString,
            boolean withDivider);

    PandaRecyclerView configureRecyclerView(
            View rootView,
            Context context,
            BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            boolean withDividers);

    PandaRecyclerView configureRecyclerView(
            View rootView,
            Context context,
            BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            String emptyViewString);

    PandaRecyclerView configureRecyclerView(
            View rootView,
            Context context,
            BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            int emptyViewStringResId);

    PandaRecyclerView configureRecyclerView(
            View rootView,
            Context context,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId);
}

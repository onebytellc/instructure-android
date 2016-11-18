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

package com.instructure.parentapp.util;

import android.app.Activity;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.instructure.pandarecycler.BaseRecyclerAdapter;
import com.instructure.pandarecycler.PaginatedRecyclerAdapter;
import com.instructure.pandarecycler.PandaRecyclerView;
import com.instructure.pandarecycler.interfaces.EmptyInterface;
import com.instructure.pandarecycler.interfaces.EmptyViewInterface;
import com.instructure.pandarecycler.util.Types;
import com.instructure.parentapp.R;
import com.instructure.parentapp.view.EmptyPandaView;

import instructure.androidblueprint.SyncExpandablePresenter;
import instructure.androidblueprint.SyncExpandableRecyclerAdapter;
import instructure.androidblueprint.SyncPresenter;
import instructure.androidblueprint.SyncRecyclerAdapter;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class RecyclerViewUtils {

    public static PandaRecyclerView buildRecyclerView(
            View rootView,
            final Context context,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeToRefreshLayoutResId,
            int recyclerViewResId,
            int emptyViewResId,
            String emptyViewText){

        EmptyViewInterface emptyViewInterface = (EmptyViewInterface)rootView.findViewById(emptyViewResId);
        PandaRecyclerView pandaRecyclerView = (PandaRecyclerView)rootView.findViewById(recyclerViewResId);
        emptyViewInterface.emptyViewText(emptyViewText);
        emptyViewInterface.setNoConnectionText(context.getString(R.string.noConnection));

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        pandaRecyclerView = (PandaRecyclerView)rootView.findViewById(R.id.recyclerView);
        pandaRecyclerView.setLayoutManager(layoutManager);
        pandaRecyclerView.setEmptyView(emptyViewInterface);
        pandaRecyclerView.setItemAnimator(new DefaultItemAnimator());
        pandaRecyclerView.setAdapter(baseRecyclerAdapter);
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(swipeToRefreshLayoutResId);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!com.instructure.pandautils.utils.Utils.isNetworkAvailable(context)) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    baseRecyclerAdapter.refresh();
                }
            }
        });

        return pandaRecyclerView;
    }

    public static PandaRecyclerView configureRecyclerViewAsGrid(
            View rootView,
            final Context context,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            final int span,
            String emptyViewString) {


        EmptyViewInterface emptyViewInterface = (EmptyViewInterface)rootView.findViewById(emptyViewResId);
        final PandaRecyclerView recyclerView = (PandaRecyclerView)rootView.findViewById(recyclerViewResId);
        emptyViewInterface.emptyViewText(emptyViewString);
        emptyViewInterface.setNoConnectionText(context.getString(R.string.noConnection));

        GridLayoutManager layoutManager = new GridLayoutManager(context, span, GridLayoutManager.VERTICAL, false);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position < recyclerView.getAdapter().getItemCount()) {
                    int viewType = recyclerView.getAdapter().getItemViewType(position);
                    if (Types.TYPE_HEADER == viewType || PaginatedRecyclerAdapter.LOADING_FOOTER_TYPE == viewType) {
                        return 1;
                    }
                } else {
                    //if something goes wrong it will take up the entire space, but at least it won't crash
                    return 1;
                }
                return 1;
            }
        });

        
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setEmptyView(emptyViewInterface);
        recyclerView.setAdapter(baseRecyclerAdapter);
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(swipeRefreshLayoutResId);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!com.instructure.pandautils.utils.Utils.isNetworkAvailable(context)) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    baseRecyclerAdapter.refresh();
                }
            }
        });

        return recyclerView;
    }

    public static PandaRecyclerView configureRecyclerViewAsGrid(
            final Activity context,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            final int span,
            String emptyViewString) {


        EmptyViewInterface emptyViewInterface = (EmptyViewInterface) context.findViewById(emptyViewResId);
        final PandaRecyclerView recyclerView = (PandaRecyclerView)context.findViewById(recyclerViewResId);
        emptyViewInterface.emptyViewText(emptyViewString);
        emptyViewInterface.setNoConnectionText(context.getString(R.string.noConnection));

        GridLayoutManager layoutManager = new GridLayoutManager(context, span, GridLayoutManager.VERTICAL, false);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (position < recyclerView.getAdapter().getItemCount()) {
                    int viewType = recyclerView.getAdapter().getItemViewType(position);
                    if (Types.TYPE_HEADER == viewType || PaginatedRecyclerAdapter.LOADING_FOOTER_TYPE == viewType) {
                        return 1;
                    }
                } else {
                    //if something goes wrong it will take up the entire space, but at least it won't crash
                    return 1;
                }
                return 1;
            }
        });


        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setEmptyView(emptyViewInterface);
        recyclerView.setAdapter(baseRecyclerAdapter);
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) context.findViewById(swipeRefreshLayoutResId);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!com.instructure.pandautils.utils.Utils.isNetworkAvailable(context)) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    baseRecyclerAdapter.refresh();
                }
            }
        });

        return recyclerView;
    }


    //Sync

    public static RecyclerView buildRecyclerView(
            View rootView,
            final Context context,
            final SyncRecyclerAdapter recyclerAdapter,
            final SyncPresenter presenter,
            int swipeToRefreshLayoutResId,
            int recyclerViewResId,
            int emptyViewResId,
            String emptyViewText){

        EmptyInterface emptyInterface = (EmptyInterface)rootView.findViewById(emptyViewResId);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(recyclerViewResId);
        emptyInterface.setTitleText(emptyViewText);
        emptyInterface.setNoConnectionText(context.getString(R.string.noConnection));

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(swipeToRefreshLayoutResId);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!com.instructure.pandautils.utils.Utils.isNetworkAvailable(context)) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    presenter.refresh(true);
                }
            }
        });

        return recyclerView;
    }

    public static RecyclerView buildRecyclerView(
            final Context context,
            final SyncRecyclerAdapter recyclerAdapter,
            final SyncPresenter presenter,
            final SwipeRefreshLayout swipeToRefreshLayout,
            final RecyclerView recyclerView,
            final EmptyInterface emptyViewInterface,
            final String emptyViewText){

        emptyViewInterface.setTitleText(emptyViewText);
        emptyViewInterface.setNoConnectionText(context.getString(R.string.noConnection));

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);

        swipeToRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!com.instructure.pandautils.utils.Utils.isNetworkAvailable(context)) {
                    swipeToRefreshLayout.setRefreshing(false);
                } else {
                    presenter.refresh(true);
                }
            }
        });

        return recyclerView;
    }

    public static void checkIfEmpty(EmptyPandaView emptyPandaView,
                                    RecyclerView recyclerView,
                                    SwipeRefreshLayout swipeRefreshLayout,
                                    SyncRecyclerAdapter adapter,
                                    boolean isEmpty) {
        if (emptyPandaView != null && adapter != null) {
            if (adapter.size() == 0 && !swipeRefreshLayout.isRefreshing()) {
                recyclerView.setVisibility(View.GONE);
                emptyPandaView.setVisibility(View.VISIBLE);
                if (isEmpty) {
                    emptyPandaView.setListEmpty();
                } else {
                    emptyPandaView.setLoading();
                }
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyPandaView.setVisibility(View.GONE);
            }
        }
    }

    public static RecyclerView buildRecyclerView(
            View rootView,
            final Context context,
            final SyncExpandableRecyclerAdapter recyclerAdapter,
            final SyncExpandablePresenter presenter,
            int swipeToRefreshLayoutResId,
            int recyclerViewResId,
            int emptyViewResId,
            String emptyViewText){

        EmptyInterface emptyInterface = (EmptyInterface)rootView.findViewById(emptyViewResId);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(recyclerViewResId);
        emptyInterface.setTitleText(emptyViewText);
        emptyInterface.setNoConnectionText(context.getString(R.string.noConnection));

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(recyclerAdapter);
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(swipeToRefreshLayoutResId);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!com.instructure.pandautils.utils.Utils.isNetworkAvailable(context)) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    presenter.refresh(true);
                }
            }
        });

        return recyclerView;
    }

    public static void checkIfEmpty(
            EmptyPandaView emptyPandaView,
            RecyclerView recyclerView,
            SwipeRefreshLayout swipeRefreshLayout,
            SyncExpandableRecyclerAdapter adapter,
            boolean isEmpty) {
        if (emptyPandaView != null && adapter != null) {
            if (adapter.size() == 0 && !swipeRefreshLayout.isRefreshing()) {
                recyclerView.setVisibility(View.GONE);
                emptyPandaView.setVisibility(View.VISIBLE);
                if (isEmpty) {
                    emptyPandaView.setListEmpty();
                } else {
                    emptyPandaView.setLoading();
                }
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyPandaView.setVisibility(View.GONE);
            }
        }
    }
}

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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.instructure.candroid.R;
import com.instructure.candroid.adapter.CourseRecyclerAdapter;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.holders.CourseViewHolder;
import com.instructure.candroid.interfaces.CourseAdapterToFragmentCallback;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.candroid.util.TabHelper;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Tab;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.loginapi.login.util.ColorUtils;
import com.instructure.pandarecycler.PandaRecyclerView;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.TutorialUtils;

public class CourseGridFragment extends ParentFragment {

    private View mRootView;
    private CourseRecyclerAdapter mRecyclerAdapter;
    private PandaRecyclerView mRecyclerView;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.MASTER;
    }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.dashboard);
    }

    @Override
    public boolean navigationContextIsCourse() {
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = getLayoutInflater().inflate(R.layout.course_grid_fragment, container, false);
        mRecyclerAdapter = new CourseRecyclerAdapter(getActivity(), new CourseAdapterToFragmentCallback() {
            @Override
            public void onRefreshFinished() {
                setRefreshing(false);
            }

            @Override
            public void onRowClicked(CanvasContext canvasContext) {
                if (getActivity() instanceof Navigation) {
                    final Navigation navigation = getNavigation();
                    if (navigation != null) {
                        setCanvasContext(canvasContext);
                        Tab homeTab = Tab.newInstance(canvasContext.getHomePageID(), getString(R.string.home));
                        navigation.addFragment(TabHelper.getFragmentByTab(homeTab, canvasContext));
                    }
                }
            }

            @Override
            public void setupTutorial(CourseViewHolder holder) {
                new TutorialUtils(getActivity(), ApplicationManager.getPrefs(getContext()), holder.pulseOveflow, TutorialUtils.TYPE.COLOR_CHANGING_DIALOG)
                        .setContent(getActivity().getString(R.string.tutorial_tipColorPickerTitle), getActivity().getString(R.string.tutorial_tipColorPickerMessage))
                        .build();

                if(!TextUtils.isEmpty(holder.grade.getText())) {
                    new TutorialUtils(getActivity(), ApplicationManager.getPrefs(getContext()), holder.pulseGrade, TutorialUtils.TYPE.COURSE_GRADES)
                            .setContent(getActivity().getString(R.string.tutorial_tipCourseGradesTitle), getActivity().getString(R.string.tutorial_tipCourseGradesMessage))
                            .build();
                }
            }
        });

        mRecyclerView = (PandaRecyclerView)mRootView.findViewById(R.id.listView);
        configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView, R.string.no_courses,
                mEmptyImageListener, ColorUtils.colorIt(getActivity(), getResources().getColor(R.color.canvasTextDark), R.drawable.ic_cv_add_to_list_lg));
        mRecyclerView.setSelectionEnabled(false);

        return mRootView;
    }

    private View.OnClickListener mEmptyImageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Navigation navigation = getNavigation();
            if(navigation != null) {
                navigation.addFragment(new FavoritingFragment());
            }
        }
    };

    @Override
    public void onFragmentActionbarSetupComplete(FRAGMENT_PLACEMENT placement) {
        super.onFragmentActionbarSetupComplete(placement);
        setupTitle(getString(R.string.dashboard));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView, R.string.no_courses);
    }

    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_show_grades_checkable, menu);
        inflater.inflate(R.menu.menu_favorite, menu);

        MenuItem item = menu.findItem(R.id.showGrades);
        if(item != null) {
            //Setup Checkbox
            item.setChecked(ApplicationManager.getPrefs(getContext()).load(Const.SHOW_GRADES_ON_CARD, true));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.showGrades) {
            boolean showGrades = ApplicationManager.getPrefs(getContext()).load(Const.SHOW_GRADES_ON_CARD, true);
            ApplicationManager.getPrefs(getContext()).save(Const.SHOW_GRADES_ON_CARD, !showGrades);
            getSupportActionBar().invalidateOptionsMenu();
            mRecyclerAdapter.setShowGrades(!showGrades);
            return true;
        } else if(item.getItemId() == R.id.selectFavorites) {
            if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                return true;
            }
            Navigation navigation = getNavigation();
            if(navigation != null) {
                navigation.addFragment(new FavoritingFragment());
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(somethingChangedReceiver, new IntentFilter(Const.COURSE_THING_CHANGED));
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(somethingChangedReceiver);
    }

    @Override
    public void onDestroy() {
        if(mRecyclerAdapter != null) {
            mRecyclerAdapter.removeCallbacks();
        }
        super.onDestroy();
    }

    private BroadcastReceiver somethingChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null && mRecyclerAdapter != null) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    int position = extras.getInt(Const.POSITION, -1);
                    boolean courseColorChanged = extras.getBoolean(Const.COURSE_COLOR, false);
                    boolean courseFavoriteChanged = extras.getBoolean(Const.COURSE_FAVORITES, false);
                    String courseName = extras.getString(Const.NAME, "");
                    boolean needToNotifyDataSetChanged = courseColorChanged | courseFavoriteChanged | courseName.length() > 0;

                    if(courseFavoriteChanged) {
                        mRecyclerAdapter.refresh();
                    }

                    if(courseColorChanged) {
                        mRecyclerAdapter.refreshAdapter();
                    }

                    if(position != -1 && courseName.length() > 0) {
                        CanvasContext canvasContext = extras.getParcelable(Const.CANVAS_CONTEXT);
                        if(canvasContext instanceof Course) {
                            ((Course)canvasContext).setName(courseName);
                            mRecyclerAdapter.addOrUpdateItem(mRecyclerAdapter.getItemGroupHeader(canvasContext), canvasContext);
                            mRecyclerAdapter.notifyItemChanged(position);

                            Navigation navigation = getNavigation();
                            if(navigation != null) {
                                navigation.courseNameChanged(canvasContext);
                            }
                        }
                    }

                    if(needToNotifyDataSetChanged) {
                        mRecyclerAdapter.notifyDataSetChanged();
                    }

                    if(courseFavoriteChanged || courseColorChanged) {
                        Navigation navigation = getNavigation();
                        if(navigation != null) {
                            //Redraws the course navigation shortcuts in the nav drawer
                            navigation.redrawNavigationShortcuts();
                        }
                    }
                }
            }
        }
    };
}

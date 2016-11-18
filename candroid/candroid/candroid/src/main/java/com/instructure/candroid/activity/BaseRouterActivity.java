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
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.Toast;

import com.instructure.candroid.R;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.fragment.CalendarListViewFragment;
import com.instructure.candroid.fragment.CourseGridFragment;
import com.instructure.candroid.fragment.GradesGridFragment;
import com.instructure.candroid.fragment.InternalWebviewFragment;
import com.instructure.candroid.fragment.LTIWebViewRoutingFragment;
import com.instructure.candroid.fragment.MessageListFragment;
import com.instructure.candroid.fragment.NotificationListFragment;
import com.instructure.candroid.fragment.ParentFragment;
import com.instructure.candroid.fragment.ToDoListFragment;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.CanvasErrorDelegate;
import com.instructure.candroid.util.FileUtils;
import com.instructure.candroid.util.FragUtils;
import com.instructure.candroid.util.LoggingUtility;
import com.instructure.candroid.util.Param;
import com.instructure.candroid.util.RouterUtils;
import com.instructure.candroid.util.TabHelper;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.FileFolderAPI;
import com.instructure.canvasapi.api.GroupAPI;
import com.instructure.canvasapi.api.TabAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.CanvasError;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.FileFolder;
import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.model.Tab;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.ErrorDelegate;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.LoaderUtils;

import retrofit.RetrofitError;
import retrofit.client.Response;


//Intended to handle all routing to fragments from links both internal and external
public abstract class BaseRouterActivity extends CallbackActivity {

    protected abstract void routeFragment(ParentFragment fragment, Navigation.NavigationPosition position);
    protected abstract void routeFragment(ParentFragment fragment);
    protected abstract int existingFragmentCount();
    protected abstract void routeToLandingPage(boolean ignoreDebounce);

    // region Used for param handling
    public static String SUBMISSIONS_ROUTE = "submissions";
    public static String RUBRIC_ROUTE = "rubric";
    // endregion

    // region OpenMediaAsyncTaskLoader
    private Bundle openMediaBundle;
    private LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> openMediaCallbacks;
    private ProgressDialog progressDialog;
    // endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.d("BaseRouterActivity: onCreate()");

        if(savedInstanceState == null) {
            parse(getIntent());
        }
        LoaderUtils.restoreLoaderFromBundle(this.getSupportLoaderManager(), savedInstanceState, getLoaderCallbacks(), R.id.openMediaLoaderID, Const.OPEN_MEDIA_LOADER_BUNDLE);
        if (savedInstanceState != null && savedInstanceState.getBundle(Const.OPEN_MEDIA_LOADER_BUNDLE) != null) {
            showProgressDialog();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        LoaderUtils.saveLoaderBundle(outState, openMediaBundle, Const.OPEN_MEDIA_LOADER_BUNDLE);
        dismissProgressDialog();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Utils.d("BaseRouterActivity: onNewIntent()");
        parse(intent);
    }

    /**
     * Handles the Route based on Navigation context, route type, and master/detail classes
     * Use RouterUtils.canRouteInternally()
     * @param route
     */
    public void handleRoute(RouterUtils.Route route) {
        try {
            if (route.getParamsHash().containsKey(Param.COURSE_ID)) {
                long courseId = Long.parseLong(route.getParamsHash().get(Param.COURSE_ID));

                if (RouterUtils.ROUTE_TYPE.FILE_DOWNLOAD == route.getRouteType()) {
                    if (route.getQueryParamsHash().containsKey(Param.VERIFIER) && route.getQueryParamsHash().containsKey(Param.DOWNLOAD_FRD)) {
                        openMedia(CanvasContext.getGenericContext(CanvasContext.Type.COURSE, courseId, ""), route.getUrl());
                        return;
                    }
                    handleSpecificFile(courseId, route.getParamsHash().get(Param.FILE_ID));
                    return;
                }


                if(RouterUtils.ROUTE_TYPE.LTI == route.getRouteType()) {
                    routeLTI(courseId, route);
                } else {
                    Tab tab = TabHelper.getTabForType(this, route.getTabId());
                    if (route.getContextType() == CanvasContext.Type.COURSE) {
                        routeToCourse(courseId, route, tab);
                    } else if (route.getContextType() == CanvasContext.Type.GROUP) {
                        routeToGroup(courseId, route, tab);
                    }
                }
                return; // do not remove return
            }

            CanvasContext canvasContext = CanvasContext.emptyUserContext();
            if (RouterUtils.ROUTE_TYPE.FILE_DOWNLOAD == route.getRouteType()) {
                openMedia(canvasContext, route.getUrl());
                return;
            }

            if(RouterUtils.ROUTE_TYPE.NOTIFICATION_PREFERENCES == route.getRouteType()) {
                Analytics.trackAppFlow(BaseRouterActivity.this, NotificationPreferencesActivity.class);
                startActivity(new Intent(getContext(), NotificationPreferencesActivity.class));
                return;
            }

            if (route.getMasterCls() != null) {
                Bundle bundle = ParentFragment.createBundle(canvasContext, route.getParamsHash(), route.getQueryParamsHash(), route.getUrl(), null);
                if (route.getDetailCls() != null) {
                    if(existingFragmentCount() == 0) {
                        //Add the landing page fragment, then the detials fragment.
                        routeToLandingPage(true);
                    }
                    routeFragment(FragUtils.getFrag(route.getDetailCls(), bundle), route.getNavigationPosition());
                } else {
                    routeFragment(FragUtils.getFrag(route.getMasterCls(), bundle), route.getNavigationPosition());
                }
            }

        } catch (Exception e) {
            LoggingUtility.LogExceptionPlusCrashlytics(BaseRouterActivity.this, e);
            Utils.e("Could not parse and route url in BaseRouterActivity");
            routeToCourseGrid();
        }
    }

    /**
     * The intent will have information about the url to open (usually from clicking on a link in an email)
     * @param intent
     */
    private void parse(Intent intent) {
        if(intent == null) {
            Utils.d("INTENT WAS NULL");
            return;
        }
        Utils.d("INTENT ACTION WAS: " + intent.getAction());
        if(intent.getExtras() == null) {
            Utils.d("INTENT EXTRAS WERE NULL");
            return;
        }

        final Bundle extras = intent.getExtras();
        Utils.logBundle(extras);

        if(extras.containsKey(Const.GOOGLE_NOW_VOICE_SEARCH)) {
            Navigation.NavigationPosition position = (Navigation.NavigationPosition)extras.getSerializable(Const.PARSE);
            if(position == Navigation.NavigationPosition.COURSES) {
                routeFragment(FragUtils.getFrag(CourseGridFragment.class, this), position);
            } else if(position == Navigation.NavigationPosition.NOTIFICATIONS) {
                routeFragment(FragUtils.getFrag(NotificationListFragment.class, this), position);
            } else if(position == Navigation.NavigationPosition.TODO) {
                routeFragment(FragUtils.getFrag(ToDoListFragment.class, this), position);
            } else if(position == Navigation.NavigationPosition.INBOX) {
                routeFragment(FragUtils.getFrag(MessageListFragment.class, this), position);
            } else if(position == Navigation.NavigationPosition.GRADES) {
                routeFragment(FragUtils.getFrag(GradesGridFragment.class, this), position);
            } else if(position == Navigation.NavigationPosition.CALENDAR) {
                routeFragment(FragUtils.getFrag(CalendarListViewFragment.class, this), position);
            }

            return;
        }


        if(extras.containsKey(Const.MESSAGE) && extras.containsKey(Const.MESSAGE_TYPE)) {
            showMessage(extras.getString(Const.MESSAGE));
        }

        if(extras.containsKey(Const.PARSE)) {
            final String url = extras.getString(Const.URL);
            RouterUtils.routeUrl(this, url, false);
        } else if(extras.containsKey(Const.BOOKMARK)) {
            final String url = extras.getString(Const.URL);
            RouterUtils.routeUrl(this, url, false);
        }
    }

    private void routeLTI(final long courseId, final RouterUtils.Route route) {
        //Since we do not know if the LTI is a tab we load in a details fragment.
        if (route.getContextType() == CanvasContext.Type.COURSE) {
            CourseAPI.getCourseWithGrade(courseId, new CanvasCallback<Course>(BaseRouterActivity.this) {

                private boolean routedWithCache = false;

                @Override
                public void cache(final Course course, LinkHeaders linkHeaders, Response response) {
                    // In order to avoid adding fragments twice, just route with the cache
                    if (routedWithCache) {
                        return;
                    }
                    routedWithCache = true;
                    if (course != null) {
                        getUserSelf(true, true);
                        routeFragment(ParentFragment.createFragment(LTIWebViewRoutingFragment.class,
                                LTIWebViewRoutingFragment.createBundle(course, route.getUrl())));
                    }
                }

                @Override
                public void firstPage(final Course course, LinkHeaders linkHeaders, Response response) {
                    if (routedWithCache) {
                        return;
                    }
                    routedWithCache = true;
                    if (course == null) {
                        showMessage(getString(R.string.could_not_route_course));
                    } else {
                        getUserSelf(true, false);
                        routeFragment(ParentFragment.createFragment(LTIWebViewRoutingFragment.class,
                                LTIWebViewRoutingFragment.createBundle(course, route.getUrl())));
                    }
                }
            });
        } else if (route.getContextType() == CanvasContext.Type.GROUP) {
            GroupAPI.getDetailedGroup(courseId, new CanvasCallback<Group>(BaseRouterActivity.this) {

                private boolean routedWithCache = false;

                @Override
                public void cache(final Group group, LinkHeaders linkHeaders, Response response) {
                    // In order to avoid adding fragments twice, just route with the cache
                    if (routedWithCache) {
                        return;
                    }
                    routedWithCache = true;
                    if (group != null) {
                        getUserSelf(true, true);
                        routeFragment(ParentFragment.createFragment(LTIWebViewRoutingFragment.class,
                                LTIWebViewRoutingFragment.createBundle(group, route.getUrl())));
                    }
                }

                @Override
                public void firstPage(final Group group, LinkHeaders linkHeaders, Response response) {
                    if (routedWithCache) {
                        return;
                    }
                    routedWithCache = true;
                    if (group == null) {
                        showMessage(getString(R.string.could_not_route_group));
                    } else {
                        getUserSelf(true, false);
                        routeFragment(ParentFragment.createFragment(LTIWebViewRoutingFragment.class,
                                LTIWebViewRoutingFragment.createBundle(group, route.getUrl())));
                    }
                }
            });
        }
    }

    private void routeToCourseGrid() {
        Utils.d("routeToCourseGrid()");
        routeFragment(FragUtils.getFrag(CourseGridFragment.class, this));
    }

    private void routeMasterDetail(CanvasContext canvasContext, RouterUtils.Route route, Tab tab) {
        Utils.d("routing with tab: " + (tab == null ? "??" : tab.getTabId()));
        Bundle bundle = ParentFragment.createBundle(canvasContext, route.getParamsHash(), route.getQueryParamsHash(), route.getUrl(), tab);
        if (route.getDetailCls() != null) {
            if(existingFragmentCount() == 0) {
                //Add the landing page fragment, then the detials fragment.
                routeToLandingPage(true);
            }
            routeFragment(FragUtils.getFrag(route.getDetailCls(), bundle));
        } else {
            if (route.getMasterCls() != null) {
                routeFragment(FragUtils.getFrag(route.getMasterCls(), bundle));//TODO: test this, not sure if that is correct but if we have a tab probably in a course.
            } else { // Used for Tab.Home (so that no masterCls has to be set)
                routeFragment(TabHelper.getFragmentByTab(tab, canvasContext));
            }
        }
    }

    private void routeToCourse(long id, final RouterUtils.Route route, final Tab tab) {
        CourseAPI.getCourseWithGrade(id, new CanvasCallback<Course>(BaseRouterActivity.this) {
            Course cacheCourse;

            @Override
            public void cache(Course course, LinkHeaders linkHeaders, Response response) {
                cacheCourse = course;
            }

            @Override
            public void firstPage(final Course course, LinkHeaders linkHeaders, Response response) {
                if (course == null) {
                    Utils.d("Course was null, could not route.");
                    showMessage(getString(R.string.could_not_route_course));
                } else {
                    routeToCourseOrGroupWithTabCheck(course, route, tab);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                //we don't want to go first page on a 504, it just means we haven't cached the data yet
                if(error.getResponse() != null && error.getResponse().getStatus() != 504) {
                    firstPage(cacheCourse, null, null);
                }
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                //we don't want to go first page on a 504, it just means we haven't cached the data yet
                if(retrofitError.getResponse() != null && retrofitError.getResponse().getStatus() != 504) {
                    firstPage(cacheCourse, null, null);
                }
                return true;
            }
        });
    }

    private void routeToGroup(long id, final RouterUtils.Route route, final Tab tab) {
        Utils.d("routeToGroup()");
        GroupAPI.getDetailedGroup(id, new CanvasCallback<Group>(BaseRouterActivity.this) {
            Group cacheGroup;

            @Override
            public void cache(Group group, LinkHeaders linkHeaders, Response response) {
                cacheGroup = group;
            }

            @Override
            public void firstPage(final Group group, LinkHeaders linkHeaders, Response response) {
                if (group == null) {
                    Utils.d("Group was null, could not route.");
                    showMessage(getString(R.string.could_not_route_group));
                } else {
                    routeToCourseOrGroupWithTabCheck(group, route, tab);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                //we don't want to go first page on a 504, it just means we haven't cached the data yet
                if(error.getResponse() != null && error.getResponse().getStatus() != 504) {
                    firstPage(cacheGroup, null, null);
                }
            }
        });
    }

    private void routeToCourseOrGroupWithTabCheck(final CanvasContext canvasContext, final RouterUtils.Route route, final Tab tab) {
        TabAPI.getTabs(canvasContext, new CanvasCallback<Tab[]>(BaseRouterActivity.this) {
            @Override
            public void cache(Tab[] tabs, LinkHeaders linkHeaders, Response response) {

                if(Tab.SYLLABUS_ID.equals(tab.getTabId())) {
                    //We do not allow routing to the syllabus if it's hidden
                    boolean tabExistsForCourse = false;
                    for (Tab t : tabs) {
                        if (t.getTabId().equals(tab.getTabId())) {
                            tabExistsForCourse = true;
                            break;
                        }
                    }

                    if(tabExistsForCourse) {
                        //Route cause tab exists
                        Utils.d("Attempting to route to group: " + canvasContext.getName());
                        getUserSelf(true, true);
                        routeMasterDetail(canvasContext, route, tab);
                    } else {
                        Utils.d("Course/Group tab hidden, or locked.");
                        showMessage(getString(R.string.could_not_route_locked));
                    }

                } else {
                    Utils.d("Attempting to route to group: " + canvasContext.getName());
                    getUserSelf(true, true);
                    routeMasterDetail(canvasContext, route, tab);
                }
                cancel();
            }

            @Override
            public void firstPage(Tab[] tabs, LinkHeaders linkHeaders, Response response) {
                cache(tabs, linkHeaders, response);
            }
        });
    }

    private void handleSpecificFile(long courseId, String fileID) {
        final CanvasContext canvasContext = CanvasContext.getGenericContext(CanvasContext.Type.COURSE, courseId, "");
        Utils.d("handleSpecificFile()");
        //If the file no longer exists (404), we want to show a different crouton than the default.
        final ErrorDelegate canvasErrorDelegate = new CanvasErrorDelegate();
        ErrorDelegate errorDelegate = new ErrorDelegate() {
            @Override
            public void noNetworkError(RetrofitError error, Context context) {
                canvasErrorDelegate.noNetworkError(error, context);
            }

            @Override
            public void notAuthorizedError(RetrofitError error, CanvasError canvasError, Context context) {
                canvasErrorDelegate.notAuthorizedError(error, canvasError, context);
            }

            @Override
            public void invalidUrlError(RetrofitError error, Context context) {
                if (context instanceof Activity) {
                    Toast.makeText(getContext(), R.string.fileNoLongerExists, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void serverError(RetrofitError error, Context context) {
                canvasErrorDelegate.serverError(error, context);

            }

            @Override
            public void generalError(RetrofitError error, CanvasError canvasError, Context context) {
                canvasErrorDelegate.generalError(error, canvasError, context);
            }
        };

        CanvasCallback<FileFolder> fileFolderCanvasCallback = new CanvasCallback<FileFolder>(BaseRouterActivity.this, errorDelegate) {
            @Override
            public void cache(FileFolder fileFolder) {
            }

                @Override
                public void firstPage(FileFolder fileFolder, LinkHeaders linkHeaders, Response response) {
                    if (fileFolder.isLocked() || fileFolder.isLockedForUser()) {
                        Toast.makeText(getContext(), String.format(getContext().getString(R.string.fileLocked), (fileFolder.getDisplayName() == null) ? getString(R.string.file) : fileFolder.getDisplayName()), Toast.LENGTH_LONG).show();
                    } else {
                        openMedia(canvasContext, fileFolder.getContentType(), fileFolder.getUrl(), fileFolder.getDisplayName());
                    }
                }
            };
            FileFolderAPI.getFileFolderFromURL("files/" + fileID, fileFolderCanvasCallback);
        }

    ///////////////////////////////////////////////////////////////////////////
    // OpenMediaAsyncTaskLoader
    ///////////////////////////////////////////////////////////////////////////

    private LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> getLoaderCallbacks() {
        if (openMediaCallbacks == null) {
            openMediaCallbacks = new LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>() {
                @Override
                public Loader<OpenMediaAsyncTaskLoader.LoadedMedia> onCreateLoader(int id, Bundle args) {
                    showProgressDialog();
                    return new OpenMediaAsyncTaskLoader(getContext(), args);
                }

                @Override
                public void onLoadFinished(Loader<OpenMediaAsyncTaskLoader.LoadedMedia> loader, OpenMediaAsyncTaskLoader.LoadedMedia loadedMedia) {
                    dismissProgressDialog();

                    try {
                        if (loadedMedia.isError()) {
                            Toast.makeText(getContext(), getString(loadedMedia.getErrorMessage()), Toast.LENGTH_LONG).show();
                        } else if (loadedMedia.isHtmlFile()) {
                            InternalWebviewFragment.loadInternalWebView(BaseRouterActivity.this, (Navigation) BaseRouterActivity.this, loadedMedia.getBundle());
                        } else if (loadedMedia.getIntent() != null) {
                            if(loadedMedia.getIntent().getType().contains("pdf")){
                                //show pdf with PSPDFkit
                                Uri uri = loadedMedia.getIntent().getData();
                                FileUtils.showPdfDocument(uri, loadedMedia, getContext());
                            } else {
                                getContext().startActivity(loadedMedia.getIntent());
                            }
                        }
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getContext(), R.string.noApps, Toast.LENGTH_LONG).show();
                    }
                    openMediaBundle = null; // set to null, otherwise the progressDialog will appear again
                }

                @Override
                public void onLoaderReset(Loader<OpenMediaAsyncTaskLoader.LoadedMedia> loader) {

                }
            };
        }
        return openMediaCallbacks;
    }

    public void openMedia(CanvasContext canvasContext, String url) {
        openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(canvasContext, url);
        LoaderUtils.restartLoaderWithBundle(this.getSupportLoaderManager(), openMediaBundle, getLoaderCallbacks(), R.id.openMediaLoaderID);
    }

    public void openMedia(CanvasContext canvasContext, String mime, String url, String filename) {
        openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(canvasContext, mime, url, filename);
        LoaderUtils.restartLoaderWithBundle(this.getSupportLoaderManager(), openMediaBundle, getLoaderCallbacks(), R.id.openMediaLoaderID);
    }

    // ProgressDialog
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setMessage(getString(R.string.opening));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dismissProgressDialog();
                openMediaBundle = null; // set to null, otherwise the progressDialog will appear again
                BaseRouterActivity.this.getSupportLoaderManager().destroyLoader(R.id.openMediaLoaderID);
            }
        });
        progressDialog.setCanceledOnTouchOutside(true);
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            initProgressDialog();
        }
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}


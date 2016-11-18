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
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.loginapi.login.interfaces.AnalyticsEventHandling;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.utils.Const;
import com.instructure.parentapp.activity.BaseRouterActivity;
import com.instructure.parentapp.activity.DetailViewActivity;
import com.instructure.parentapp.activity.MainActivity;
import com.instructure.parentapp.fragments.AnnouncementFragment;
import com.instructure.parentapp.fragments.AssignmentFragment;
import com.instructure.parentapp.fragments.CourseSyllabusFragment;
import com.instructure.parentapp.fragments.CourseWeekFragment;
import com.instructure.parentapp.fragments.EventFragment;
import com.instructure.parentapp.fragments.InternalWebviewFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class RouterUtils {

    private static final List<Route> sRoutes = new ArrayList<>();
    public enum ROUTE_TYPE {INTERNALLY_ROUTED, FILE_DOWNLOAD, NOT_INTERNALLY_ROUTED, NOTIFICATION_PREFERENCES};


    private static String COURSE_REGEX = "/(?:courses|groups)";
    private static String getCourseFromRoute(String route) {
        return COURSE_REGEX + route;
    }

    static {

        // Course
        //FIXME
        sRoutes.add(new Route(getCourseFromRoute("/:course_id"), CourseWeekFragment.class));

        // Announcements
        sRoutes.add(new Route(getCourseFromRoute("/:course_id/announcements/:announcement_id"), AnnouncementFragment.class));
        sRoutes.add(new Route(getCourseFromRoute("/:course_id/discussion_topics/:announcement_id"), AnnouncementFragment.class));


        // Calendar
        sRoutes.add(new Route(getCourseFromRoute("/:course_id/calendar_events/:event_id"), EventFragment.class));

        // Syllabus
        sRoutes.add(new Route(getCourseFromRoute("/:course_id/assignments/syllabus"), CourseSyllabusFragment.class));

        // Assignments
        sRoutes.add(new Route(getCourseFromRoute("/:course_id/assignments/:assignment_id"), AssignmentFragment.class));

        //Files
        sRoutes.add(new Route(getCourseFromRoute("/:course_id/files/:file_id/download"), ROUTE_TYPE.FILE_DOWNLOAD)); // trigger webview's download listener
        sRoutes.add(new Route("/files/:file_id/download", ROUTE_TYPE.FILE_DOWNLOAD));

    }


    /**
     * Returns true if url can be routed to a fragment, false otherwise
     * @param activity
     * @param url
     * @param routeIfPossible
     * @return
     */
    public static boolean canRouteInternally(Activity activity, String url, @Nullable Student user, String domain, boolean routeIfPossible) {
        boolean canRoute = getInternalRoute(url, domain) != null;

        if (canRoute && activity != null && routeIfPossible) {
            routeUrl(activity, url, user, domain, true);
        }
        return canRoute;
    }

    /**
     * Gets the Route, null if route cannot be handled internally
     * @param url
     * @return Route if application can handle link internally; null otherwise
     */
    public static Route getInternalRoute(String url, String domain) {
        UrlValidity urlValidity = new UrlValidity(url, domain);

        if (!urlValidity.isHostForLoggedInUser() || !urlValidity.isValid()) {
            return null;
        }

        Route route = null;
        for (Route r : sRoutes) {
            if (r.apply(url)) {
                if (ROUTE_TYPE.NOT_INTERNALLY_ROUTED == r.getRouteType()) {
                    return null; // open in a webview
                }
                route = r;
                break; // Do NOT remove break (matches the first route it comes across, more specific routes must come first)
            }
        }
        return route;
    }

    /**
     * Determines if a url can be loaded with a fragment in the app.
     * Example: /courses/1234/assignments/5678 would be routed to AssignmentListFragment (master) and AssignmentFragment (detail)
     *
     * Routing is determined by the following:
     *      1. Url has the logged in user's domain
     *      2. Url has a matching route which specifies which fragment to load
     *      3. Url is from a third party domain such as youtube.com or wikipedia.com
     *
     * if 1 && 2
     *      Routes to fragment
     * else  (3)
     *      open internal webview fragment
     *
     * @param activity
     * @param url
     * @param animate
     * @param domain
     * @return
     */
    public static void routeUrl(Activity activity, String url, @Nullable Student user, String domain, boolean animate) {
        if (activity == null) {
            return;
        }
        boolean isReceivedFromOutsideOfApp = !(activity instanceof BaseRouterActivity);

        UrlValidity urlValidity;
        if(TextUtils.isEmpty(domain)) {
            urlValidity = new UrlValidity(url, APIHelper.getAirwolfDomain(activity));
        } else {
            urlValidity = new UrlValidity(url, domain);
        }

        if (!urlValidity.isValid()) {
            routeToMainPage(activity, isReceivedFromOutsideOfApp);
        }

        boolean isHostForLoggedInUser = urlValidity.isHostForLoggedInUser();

        String host = urlValidity.getUri().getHost();
        if(host == null) {
            url = APIHelper.getAirwolfDomain(activity) + url;
            urlValidity = new UrlValidity(url, APIHelper.getAirwolfDomain(activity));
        }
        //if host is null that means they didn't pass the domain with the url (so it's something like /courses/alerts/....)
        if (isHostForLoggedInUser || host == null) {

            Application application = activity.getApplication();
            if(application instanceof AnalyticsEventHandling) {
                ((AnalyticsEventHandling)application).trackScreen("AppRouter");
            }

            if (isReceivedFromOutsideOfApp) {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(Const.PARSE, true);
                intent.putExtra(Const.URL, url);
                intent.putExtra(Const.RECEIVED_FROM_OUTSIDE, isReceivedFromOutsideOfApp);

                activity.startActivity(intent);
            } else {
                BaseRouterActivity baseRouterActivity = (BaseRouterActivity)activity;
                Route route = getInternalRoute(url, urlValidity.getUri().getHost());

                if (route != null) {
                    baseRouterActivity.handleRoute(route, user);
                }
            }
        } else {
            openInInternalWebViewFragment(activity, url, isReceivedFromOutsideOfApp);
        }
    }

    private static void openInInternalWebViewFragment(Context context, String url, final boolean isReceivedFromOutsideOfApp) {
        Utils.d("couldNotParseUrl()");
        // TODO test if this works
        Bundle bundle = InternalWebviewFragment.createBundle(url, null, null, null);

        Intent intent = new Intent(context, DetailViewActivity.class);
        if(isReceivedFromOutsideOfApp) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private static void routeToMainPage(Context context, boolean isReceivedFromOutsideOfApp) {
        Utils.d("routeToMainPage()");

        Intent intent = new Intent(context, MainActivity.class);
        if(isReceivedFromOutsideOfApp) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        context.startActivity(intent);
    }

    private static class UrlValidity {
        private boolean mIsHostForLoggedInUser = false;
        private boolean mIsValid = false;
        private Uri mUri;

        public UrlValidity(String url, String userDomain) {
            mUri =  Uri.parse(url);
            if (mUri != null) {
                mIsValid = true;
            }

            String host = mUri.getHost();
            mIsHostForLoggedInUser = isLoggedInUserHost(host, userDomain);
        }

        private boolean isLoggedInUserHost(String host, String userDomain) {
            // Assumes user is already signed in (InterwebsToApplication does a signin check)
            return ((userDomain != null && userDomain.equals(host)));
        }

        // region Getter && Setters
        public boolean isHostForLoggedInUser() {
            return mIsHostForLoggedInUser;
        }

        public boolean isValid() {
            return mIsValid;
        }

        public Uri getUri() {
            return mUri;
        }

        // endregion
    }
    /**
     * A Route defines urls with params to be matched and parsed.
     * The params are denoted by a ':' then the name of the param. A paramHash is then created based off how the route is specified.
     *
     * Example: If the route is /courses/:course_id/assignments/:this_is_the_name_i_choose and the url /courses/1234/assignments/5678 is applied.
     * The paramsHash will contain "course_id" -> "1234", "this_is_the_name_i_choose" -> "5678"
     */
    public static class Route {
        // region Route specific (never changes after route is created)
        private String mRoute;
        private Pattern mRoutePattern;
        private ArrayList<String> mParamNames = new ArrayList<>();
        private List<String> mQueryParamNames; // used to validate a route that requires certain query params
        private ROUTE_TYPE mRouteType = ROUTE_TYPE.INTERNALLY_ROUTED;
        private Class<? extends Fragment> mFragCls;

        // endregion

        // region Refers to the url that was applied to the route
        private HashMap<String, String> mParamsHash;
        private HashMap<String, String> mQueryParamsHash;
        private String mUrl;
        private Uri mUri;
        // endregion

        // region Constructors


        public Route(String route, List<String> queryParamNames) {
            this(route);
            mQueryParamNames = queryParamNames;
        }




        public Route(String route, ROUTE_TYPE route_type, List<String> queryParamNames) {
            this(route);
            mRouteType = route_type;
            mQueryParamNames = queryParamNames;
        }

        public <Type extends Fragment> Route(String route, Class<Type> cls) {
            this(route);
            mFragCls = cls;
        }

        /**
         * Used for routes that should be treated as an external links
         *
         * @param route
         * @param route_type
         */
        public Route(String route, ROUTE_TYPE route_type) {
            this(route);
            mRouteType = route_type;
        }

        /**
         * All constructors eventually get here
         *
         * @param route
         */
        public Route(String route) {
            mRoute = route;

            Matcher matcher = Pattern.compile("/:([^/]*)").matcher(route); // match anything but a slash after a colon and create a group for the name of the param

            // Get the names of the params
            while (matcher.find()) {
                mParamNames.add(matcher.group(1));
            }

            Matcher paramValueMatcher = Pattern.compile("/:[^/]*").matcher(route); // match a slash, colon and then anything but a slash. Matched value is replaced so the param value can be parsed
            if (paramValueMatcher.find()) {
                String paramValueRegex = paramValueMatcher.replaceAll("/([^/]*)"); // Create a group where the param was, so the value can be located
                paramValueRegex = addLineMatchingAndOptionalEndSlash(paramValueRegex);
                mRoutePattern = Pattern.compile(paramValueRegex);
            } else { // does not contain params, just look for exact match
                mRoutePattern = Pattern.compile(addLineMatchingAndOptionalEndSlash(mRoute));
            }
        }

        /** When a route is a match, the paramsHash, queryParamsHash, and Uri are set.
            * @param url
            * @return true is route is a match, false otherwise
        */
        public boolean apply(String url) {
            if (url == null) {
                return false;
            }
            Uri parsedUri = Uri.parse(url);
            String path = parsedUri.getPath();
            boolean isMatch = mRoutePattern.matcher(path).find();
            if (isMatch) {
                if (ROUTE_TYPE.NOT_INTERNALLY_ROUTED.equals(mRouteType)) {
                    return true; // getInternalRoute will handle it
                }

                mUri = parsedUri;
                mParamsHash = createParamsHash(path);
                mQueryParamsHash = createQueryParamsHash();

                if (mQueryParamNames != null) {
                    return checkQueryParamNamesExist(mQueryParamNames, mQueryParamsHash.keySet());
                }

                mUrl = url;
            }
            return isMatch;
        }

        public boolean apply(Class<? extends Fragment> masterCls) {
            boolean isMatch = (!ROUTE_TYPE.NOT_INTERNALLY_ROUTED.equals(mRouteType) && masterCls == mFragCls);
            return isMatch;
        }

        public String createUrl(HashMap<String, String> replacementParams) {
            if(replacementParams == null) {
                return null;
            }

            String url = mRoute;
            for (String key : replacementParams.keySet()) {
                String keyParamIndicator = key;
                if (!keyParamIndicator.startsWith(":")) {
                    keyParamIndicator = ":" + key;
                }
                url = url.replaceAll(keyParamIndicator, replacementParams.get(key));
            }

            return url;
        }

        private boolean checkQueryParamNamesExist(List<String> expectedQueryParams, Set<String> actualQueryParams) {
            for (String expectedKey : expectedQueryParams) {
                if (!actualQueryParams.contains(expectedKey)) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Adds '^' and '$' to regex for line matching
         * Also makes ending slash and api/v1 optional
         * @param regex
         * @return
         */
        private String addLineMatchingAndOptionalEndSlash(String regex) {
            if (regex.endsWith("/")) {
                regex = String.format("^(?:/api/v1)?%s?$", regex);
            } else {
                regex = String.format("^(?:/api/v1)?%s/?$", regex);
            }
            return regex;
        }

        /**
         * A param hash contains the key and values for the route
         * Example: If the route is /courses/:course_id and the url /courses/1234 is applied. The paramsHash will contain "course_id" -> "1234"
         * @param url
         * @return
         */
        private HashMap<String, String> createParamsHash(String url) {
            // TODO make this more bullet proof
            HashMap<String, String> params = new HashMap<>();
            Matcher matcher = mRoutePattern.matcher(url);
            ArrayList<String> paramValues = new ArrayList<>();
            if (matcher.find()) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    try {
                        // index 0 is the original string that was matched. Just get the group values
                        paramValues.add(matcher.group(i + 1));
                    } catch (Exception e) {

                    }
                }
            }
            for (int i = 0; i < mParamNames.size(); i++) {
                if (i < paramValues.size()) {
                    params.put(mParamNames.get(i), paramValues.get(i));
                }
            }

            return params;
        }

        /**
         * Query params for the url.
         * Example: The url /courses/1234?hello=world would have a Query params hash containing "hello" -> "world"
         * @return
         */
        private HashMap<String, String> createQueryParamsHash() {
            HashMap<String, String> queryParams = new HashMap<>();
            if (getUri() != null) {
                for (String param : getUri().getQueryParameterNames()) {
                    queryParams.put(param, getUri().getQueryParameter(param));
                }
            }
            return queryParams;
        }

        public CanvasContext.Type getContextType() {
            Matcher coursesMatcher = Pattern.compile("^/courses/?").matcher(mUri.getPath());
            if(coursesMatcher.find()) {
                return CanvasContext.Type.COURSE;
            }

            Matcher groupsMatcher = Pattern.compile("^/groups/?").matcher(mUri.getPath());
            if(groupsMatcher.find()) {
                return CanvasContext.Type.GROUP;
            }

            Matcher usersMatcher = Pattern.compile("^/users/?").matcher(mUri.getPath());
            if(usersMatcher.find()) {
                return CanvasContext.Type.USER;
            }

            return CanvasContext.Type.UNKNOWN;
        }

        // region Getter & Setters

        public ROUTE_TYPE getRouteType() {
            return mRouteType;
        }

        public <Type extends Fragment> Class<Type> getFragCls() {
            return (Class<Type>) mFragCls;
        }

        public HashMap<String, String> getParamsHash() {
            return mParamsHash;
        }

        public String getFragmentIdentifier() {
            return getUri().getFragment();
        }

        public String getQueryString() {
            return getUri().getQuery();
        }

        public HashMap<String, String> getQueryParamsHash() {
            return mQueryParamsHash;
        }

        public String getUrl() {
            return mUrl;
        }

        public Uri getUri() {
            return mUri;
        }

        // endregion
    }
        // endregion
}

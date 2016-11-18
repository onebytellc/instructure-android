/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.pandautils.utils;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.utilities.APIHelpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Router {

    public static final String ASSIGNMENT_ID = "assignment_id";
    public static final String EVENT_ID = "event_id";
    public static final String CONVERSATION_ID = "conversation_id";
    public static final String COURSE_ID = "course_id";
    public static final String FILE_ID = "file_id";
    public static final String GRADE_ID = "grade_id";
    public static final String MESSAGE_ID = "message_id"; // used by discussions and announcements
    public static final String MODULE_ID = "module_id";
    public static final String MODULE_ITEM_ID = "module_item_id";
    public static final String PAGE_ID = "page_id";
    public static final String QUIZ_ID = "quiz_id";
    public static final String USER_ID = "user_id";
    public static final String SLIDING_TAB_TYPE = "sliding_tab_type";
    public static final String SUBMISSION_ID = "submission_id";
    public static final String VERIFIER = "verifier";
    public static final String DOWNLOAD_FRD = "download_frd";
    public static final String MODULE_TYPE_SLASH_ID = "module_type_slash_id";

    protected static String COURSE_OR_GROUP_REGEX = "/(?:courses|groups)";
    protected static String courseOrGroup(String route) {
        return COURSE_OR_GROUP_REGEX + route;
    }

    protected static Router mRouter;
    protected RouteProvider mRouteProvider;
    protected RouterCallback mCallbacks;

    public static Router getInstance(RouteProvider routeProvider, RouterCallback callback) {
        if(mRouter == null) {
            mRouter = new Router(routeProvider, callback);
        }
        return mRouter;
    }

    public Router(@NonNull RouteProvider routeProvider, @NonNull RouterCallback callback) {
        mRouteProvider = routeProvider;
        mCallbacks = callback;
    }

    public interface RouterCallback {
        void route(Route route);
        void routeToLanding();
        void routeToWebView(String url);
    }

    public interface RouteProvider {
        List<Route> provideRoutes();
    }

    public enum ROUTE_TYPE { INTERNALLY_ROUTED, FILE_DOWNLOAD, LTI, NOT_INTERNALLY_ROUTED, NOTIFICATION_PREFERENCES }

    private @NonNull List<Route> getRoutes() {
        if(mRouteProvider != null) {
            return mRouteProvider.provideRoutes();
        }
        return new ArrayList<>();
    }

    /**
     * Gets the Route, null if route cannot be handled internally
     * @param url
     * @return Route if application can handle link internally; null otherwise
     */
    public Route getInternalRoute(String url, String domain) {
        UrlValidity urlValidity = new UrlValidity(url, domain);

        if (!urlValidity.isHostForLoggedInUser() || !urlValidity.isValid()) {
            return null;
        }

        Route route = null;
        for (Route r : getRoutes()) {
            if (r.apply(url)) {
                if (ROUTE_TYPE.NOT_INTERNALLY_ROUTED == r.getRouteType()) {
                    return null; // returning null allows for routes that are not supported to skip the unsupported fragment and are usually just opened in a webview
                }
                route = r;
                break; // Do NOT remove break (matches the first route it comes across, more specific routes must come first)
            }
        }
        return route;
    }

    public Route getInternalRoute(Class<? extends Fragment> masterCls, Class<? extends Fragment> detailCls) {
        Route route = null;
        for (Route r : getRoutes()) {
            if (r.apply(masterCls, detailCls)) {
                route = r;
                break; // Do NOT remove break (matches the first route it comes across, more specific routes must come first)
            }
        }
        return route;
    }

    public String createUrl(Context context, CanvasContext.Type type, Class<? extends Fragment> masterCls, HashMap<String, String> replacementParams) {
        return createUrl(context, type, masterCls, null, replacementParams, null);
    }

    public String createUrl(Context context, CanvasContext.Type type, Class<? extends Fragment> masterCls, Class<? extends Fragment> detailCls, HashMap<String, String> replacementParams, HashMap<String, String> queryParams) {
        if(replacementParams == null || replacementParams.isEmpty()) {
            return null;
        }

        String domain = APIHelpers.getFullDomain(context);
        Route urlRoute = getInternalRoute(masterCls, detailCls);
        if (urlRoute != null) {
            String path = urlRoute.createUrl(replacementParams);
            if (path.contains(COURSE_OR_GROUP_REGEX)) {
                Pattern pattern = Pattern.compile(COURSE_OR_GROUP_REGEX, Pattern.LITERAL);
                Matcher matcher = pattern.matcher(path);
                switch (type) {
                    case COURSE:
                        path = matcher.replaceAll("/courses");
                        break;
                    case GROUP:
                        path = matcher.replaceAll("/groups");
                        break;
                }
            }
            return createQueryParamString((domain + path), queryParams);
        }
        return null;
    }

    /**
     * Returns true if url can be routed to a fragment, false otherwise
     * @param activity
     * @param url
     * @param routeIfPossible
     * @return
     */
    public boolean canRouteInternally(Activity activity, String url, String domain, boolean routeIfPossible) {
        boolean canRoute = getInternalRoute(url, domain) != null;

        if (canRoute && activity != null && routeIfPossible) {
            routeUrl(activity, url);
        }
        return canRoute;
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
     * else if 1 && not 2
     *      Routes to unsupported fragment with an option to open the url with web canvas
     * else  (3)
     *      open internal webview fragment
     *
     * @param activity
     * @param url
     * @return
     */
    public void routeUrl(Activity activity, String url) {
        if (activity == null) {
            return;
        }

        UrlValidity urlValidity = new UrlValidity(url, APIHelpers.getDomain(activity));

        if (!urlValidity.isValid()) {
            if(mCallbacks != null) {
                mCallbacks.routeToLanding();
            }
        }

        boolean isHostForLoggedInUser = urlValidity.isHostForLoggedInUser();

        if(mCallbacks != null) {
            if (isHostForLoggedInUser) {
                Route route = getInternalRoute(url, APIHelpers.getDomain(activity));
                if(route != null) {
                    mCallbacks.route(route);
                } else {
                    mCallbacks.routeToWebView(url);
                }
            } else {
                mCallbacks.routeToWebView(url);
            }
        }
    }

    private static String createQueryParamString(String url, HashMap<String, String> queryParams) {
        if(queryParams != null && !queryParams.isEmpty()) {
            Uri.Builder uri = Uri.parse(url).buildUpon();
            for (HashMap.Entry<String, String> entry : queryParams.entrySet()) {
                uri.appendQueryParameter(entry.getKey(), entry.getValue());
            }
            return uri.build().toString();
        }
        return url;
    }

    private class UrlValidity {
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
        private Class<? extends Fragment> mMasterCls;
        private Class<? extends Fragment> mDetailCls;
        private String mTabId;
        private ROUTE_TYPE mRouteType = ROUTE_TYPE.INTERNALLY_ROUTED;
        // endregion

        // region Refers to the url that was applied to the route
        private HashMap<String, String> mParamsHash = new HashMap<>();
        private HashMap<String, String> mQueryParamsHash = new HashMap<>();
        private String mUrl;
        private Uri mUri;
        // endregion

        // region Constructors
        public Route(String route, Class<? extends Fragment> masterCls, Class<? extends Fragment> detailCls, String tabId) {
            this(route, masterCls, tabId);
            mDetailCls = detailCls;
        }

        public Route(String route, Class<? extends Fragment> masterCls, Class<? extends Fragment> detailCls, String tabId, List<String> queryParamNames) {
            this(route, masterCls, tabId);
            mDetailCls = detailCls;
            mQueryParamNames = queryParamNames;
        }

        public <Type extends Fragment> Route(String route, Class<Type> cls, String tabId) {
            this(route);
            mMasterCls = cls;
            mTabId = tabId;
        }

        public <Type extends Fragment> Route(String route, Class<Type> cls) {
            this(route);
            mMasterCls = cls;
        }

        public Route(String route, ROUTE_TYPE route_type, List<String> queryParamNames) {
            this(route);
            mRouteType = route_type;
            mQueryParamNames = queryParamNames;
        }

        /**
         * Used for routes that should be treated as an external links
         * @param route
         * @param route_type
         */
        public Route(String route, ROUTE_TYPE route_type) {
            this(route);
            mRouteType = route_type;
        }

        /**
         * All constructors eventually get here
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
        // endregion

        /**
         * When a route is a match, the paramsHash, queryParamsHash, and Uri are set.
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
                    return true; // recognized as a match so the unsupported fragment doesn't match it, then getInternalRoute will handle it
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

        public boolean apply(Class<? extends Fragment> masterCls, Class<? extends Fragment> detailCls) {
            return  (!ROUTE_TYPE.NOT_INTERNALLY_ROUTED.equals(mRouteType) && masterCls == mMasterCls && detailCls == mDetailCls);
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
            HashMap<String, String> params = new HashMap<>();
            Matcher matcher = mRoutePattern.matcher(url);
            ArrayList<String> paramValues = new ArrayList<>();
            if (matcher.find()) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    try {
                        // index 0 is the original string that was matched. Just get the group values
                        paramValues.add(matcher.group(i + 1));
                    } catch (Exception e) {
                        //do nothing
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

        @SuppressWarnings("unchecked")
        public <Type extends Fragment> Class<Type> getMasterCls() {
            return (Class<Type>) mMasterCls;
        }

        @SuppressWarnings("unchecked")
        public <Type extends Fragment> Class<Type> getDetailCls() {
            return (Class<Type>)mDetailCls;
        }

        public String getTabId() {
            return mTabId;
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

    /**
     * Gets a course id from a url, if url is invalid or could not be parsed a 0 will return.
     * @param url
     * @return
     */
    public long getCourseIdFromURL(String url) {
        if(TextUtils.isEmpty(url)) {
            return 0L;
        }

        try {
            HashMap<String, String> params = new HashMap<>();
            for (Route r : getRoutes()) {
                if (r.apply(url)) {
                    params = r.getParamsHash();
                    break;
                }
            }
            return Long.parseLong(params.get(COURSE_ID));
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Gets a course id from a url, if url is invalid or could not be parsed a 0 will return.
     * @param urlString a valid url in the form of a string
     * @return a CanvasContext context_id (group_12345, course_12345)
     */
    public String getContextIdFromURL(String urlString) {
        if(TextUtils.isEmpty(urlString)) {
            return "";
        }

        try {
            HashMap<String, String> params = new HashMap<>();
            Route route = null;
            for (Route r : getRoutes()) {
                if (r.apply(urlString)) {
                    route = r;
                    params = r.getParamsHash();
                    break;
                }
            }

            if(route == null) return "";

            return CanvasContext.makeContextId(route.getContextType(), Long.parseLong(params.get(COURSE_ID)));
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isGroup(Uri uri) {
        Pattern pattern = Pattern.compile("^/group/?");
        Matcher matcher = pattern.matcher(uri.getPath());
        return matcher.find();
    }
}

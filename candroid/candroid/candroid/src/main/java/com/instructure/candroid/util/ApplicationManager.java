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

package com.instructure.candroid.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.webkit.WebView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.instructure.candroid.BuildConfig;
import com.instructure.candroid.adapter.CalendarListRecyclerAdapter;
import com.instructure.canvasapi.model.CanvasColor;
import com.google.gson.Gson;
import com.instructure.candroid.R;
import com.instructure.candroid.fragment.ApplicationSettingsFragment;
import com.instructure.candroid.widget.BaseRemoteViewsService;
import com.instructure.canvasapi.api.OAuthAPI;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.canvasapi.utilities.Masquerading;
import com.instructure.loginapi.login.interfaces.AnalyticsEventHandling;
import com.instructure.loginapi.login.model.SignedInUser;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.Prefs;
import com.instructure.pandautils.utils.TutorialUtils;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.pspdfkit.PSPDFKit;
import com.pspdfkit.exceptions.PSPDFInitializationFailedException;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import io.fabric.sdk.android.Fabric;
import retrofit.client.Response;

public class ApplicationManager extends MultiDexApplication implements AnalyticsEventHandling {

    public final static String PREF_CHANGELOG_NAME = "candroid_changelog_SP";
    public final static String PREF_NAME = "candroidSP";
    public final static String PREF_FILE_NAME = "candroidSP";
    public final static String MULTI_SIGN_IN_PREF_NAME = "multipleSignInCandroidSP";
    public final static String OTHER_SIGNED_IN_USERS_PREF_NAME = "otherSignedInUsersCandroidSP";
    public final static String MASQ_PREF_NAME = "masqueradeCandroidSP";
    public final static String PREF_NAME_PREVIOUS_DOMAINS = "candroidSP_previous_domains";
    public final static String WIDGET_PREF = "candroidSP_widget";

    private static CanvasColor mCachedColors = null;
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        initPSPDFKit();
        Fabric.with(this, new Crashlytics());

        CanvasContextColor.init(getPrefs(getApplicationContext()), R.color.defaultPrimary, R.color.defaultPrimaryDark);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // there appears to be a bug when the user is installing/updating the android webview stuff.
            // http://code.google.com/p/android/issues/detail?id=175124
            try {
                WebView.setWebContentsDebuggingEnabled(true);
            } catch (Exception e) {
                Crashlytics.log("Exception trying to setWebContentsDebuggingEnabled");
            }
        }
        SharedPreferences pref = this.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        //If we don't have one, generate one.
        if (!pref.contains("APID")) {
            String uuid = UUID.randomUUID().toString();
            LoggingUtility.LogConsole("NEW APID:" + uuid);

            SharedPreferences.Editor editor = pref.edit();
            editor.putString("APID", uuid);
            editor.apply();
        }
        loadLanguage(getApplicationContext());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        loadLanguage(getApplicationContext());
    }

    public static Prefs getPrefs(Context context){
        return new Prefs(context, PREF_NAME);
    }

    public static String getVersionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (Exception E) {
            return "";
        }
    }

    /**
     * Gets the course colors from a cache (Shared Prefs)
     * @return returns a cached context for colors pulled from shared prefs
     */
    public static CanvasColor getCachedColors(Context context) {
        if(mCachedColors == null) {
            String json = getPrefs(context).load(Const.COURSE_COLORS, "");
            if (!TextUtils.isEmpty(json)) {
                mCachedColors = new Gson().fromJson(json, CanvasColor.class);
            }
        }

        if(mCachedColors == null) {
            mCachedColors = new CanvasColor();
        }

        return mCachedColors;
    }

    public static void invalidateColorsCache() {
        mCachedColors = null;
    }

    /**
     * Saves the course colors to a cache (Shared Prefs)
     * @param canvasColor an object to represent all course colors
     */
    public static void saveCourseColorsToCache(Context context, CanvasColor canvasColor) {
        final String json = new Gson().toJson(canvasColor, CanvasColor.class);
        getPrefs(context).save(Const.COURSE_COLORS, json);
        invalidateColorsCache();
    }

    /**
     * Switch out the current signed in user. Temporarily remove credentials. Save them elsewhere so we can repopulate it when necessary.
     *
     * @return
     */
    public boolean switchUsers() {

        //backup widget background preferences
        Map<Integer, String> widgetBackgroundMap = BaseRemoteViewsService.getStoredBackgroundPreferences(getApplicationContext());
        Map<Integer, Boolean> widgetShouldHideDetailsMap = BaseRemoteViewsService.getShouldHideDetailsPreferences(getApplicationContext());

        //Prepare the user to be saved...
        SignedInUser signedInUser = new SignedInUser();
        signedInUser.user = APIHelpers.getCacheUser(this);
        signedInUser.domain = APIHelpers.getDomain(this);
        signedInUser.protocol = APIHelpers.loadProtocol(this);
        signedInUser.token = APIHelpers.getToken(this);
        signedInUser.calendarFilterPrefs = CalendarListRecyclerAdapter.getFilterPrefs(getApplicationContext());
        signedInUser.lastLogoutDate = new Date();

        //Save Signed In User to sharedPreferences
        addToPreviouslySignedInUsers(signedInUser);

        //Clear shared preferences, but keep the important stuff.
        safeClearSharedPreferences();

        //CLear masquerading preferences.
        clearMasqueradingPreferences();

        //Clear all Shared Preferences.
        APIHelpers.clearAllData(this);

        //restore widget background preferences
        BaseRemoteViewsService.restoreWidgetBackgroundPreference(getApplicationContext(), widgetBackgroundMap);
        BaseRemoteViewsService.restoreWidgetShouldHideDetailsPreference(getApplicationContext(), widgetShouldHideDetailsMap);

        return true;
    }


    /**
     * Log out the currently signed in user. Permanently remove credential information.
     *
     * @return
     */
    public boolean logoutUser() {

        //It is possible for multiple APIs to come back 'simultaneously' as HTTP401s causing a logout
        //if this has already ran, data is already cleared causing null pointer exceptions
        if (APIHelpers.getToken(this) != null && !APIHelpers.getToken(this).equals("")) {

            //Delete token from server
            //We don't actually care about this coming back. Fire and forget.
            CanvasCallback<Response> deleteTokenCallback = new CanvasCallback<Response>(APIHelpers.statusDelegateWithContext(this)) {
                @Override
                public void cache(Response response) {
                }

                @Override
                public void firstPage(Response response, LinkHeaders linkHeaders, Response response2) {
                }
            };

            OAuthAPI.deleteToken(deleteTokenCallback);
            //Remove Signed In User from sharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences(OTHER_SIGNED_IN_USERS_PREF_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(getGlobalUserId(APIHelpers.getDomain(this), APIHelpers.getCacheUser(this)));
            editor.apply();

            //Clear shared preferences, but keep the important stuff.
            safeClearSharedPreferences();

            //CLear masquerading preferences.
            clearMasqueradingPreferences();

            //Clear all Shared Preferences.
            APIHelpers.clearAllData(this);
        }
        return true;
    }

    public void clearMasqueradingPreferences() {
        //stop masquerading
        Masquerading.stopMasquerading(this);

        //clear any shared preferences for the masqueraded user
        SharedPreferences masq_settings = getSharedPreferences(ApplicationManager.MASQ_PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor masq_editor = masq_settings.edit();
        masq_editor.clear();
        masq_editor.apply();
    }

    public void safeClearSharedPreferences() {
        //Get the Shared Preferences
        SharedPreferences settings = getSharedPreferences(ApplicationManager.PREF_NAME, MODE_PRIVATE);

        //Don't make them redo tutorials
        boolean doneStreamTutorial = settings.getBoolean("stream_tutorial_v2", false);
        boolean doneConversationListTutorial = settings.getBoolean("conversation_list_tutorial_v2", false);
        boolean featureSlides = settings.getBoolean("feature_slides_shown", false);
        String lastDomain = settings.getString("last-domain", "");
        String UUID = settings.getString("APID", null);
        int landingPage = settings.getInt(ApplicationSettingsFragment.LANDING_PAGE, 0);
        boolean drawerLearned = settings.getBoolean(Const.PREF_USER_LEARNED_DRAWER, false);
        boolean tutorialViewed = settings.getBoolean(Const.TUTORIAL_VIEWED, false);
        boolean newGroupsViewed = settings.getBoolean(Const.VIEWED_NEW_FEATURE_BANNER, false);
        boolean showGrades = settings.getBoolean(Const.SHOW_GRADES_ON_CARD, true);
        boolean pandasFlying = settings.getBoolean(Const.FUN_MODE, false);

        boolean tutorial_1 = TutorialUtils.hasBeenViewed(getPrefs(getApplicationContext()), TutorialUtils.TYPE.STAR_A_COURSE);
        boolean tutorial_2 = TutorialUtils.hasBeenViewed(getPrefs(getApplicationContext()), TutorialUtils.TYPE.COLOR_CHANGING_DIALOG);
        boolean tutorial_3 = TutorialUtils.hasBeenViewed(getPrefs(getApplicationContext()), TutorialUtils.TYPE.LANDING_PAGE);
        boolean tutorial_5 = TutorialUtils.hasBeenViewed(getPrefs(getApplicationContext()), TutorialUtils.TYPE.MY_COURSES);
        boolean tutorial_6 = TutorialUtils.hasBeenViewed(getPrefs(getApplicationContext()), TutorialUtils.TYPE.NOTIFICATION_PREFERENCES);
        boolean tutorial_9 = TutorialUtils.hasBeenViewed(getPrefs(getApplicationContext()), TutorialUtils.TYPE.NAVIGATION_SHORTCUTS);
        boolean tutorial_10 = TutorialUtils.hasBeenViewed(getPrefs(getApplicationContext()), TutorialUtils.TYPE.COURSE_GRADES);

        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();

        //Replace the information about tutorials/last domain
        editor.putBoolean("stream_tutorial_v2", doneStreamTutorial);
        editor.putBoolean("conversation_list_tutorial_v2", doneConversationListTutorial);
        editor.putBoolean("feature_slides_shown", featureSlides);
        editor.putString("last-domain", lastDomain);
        editor.putInt(ApplicationSettingsFragment.LANDING_PAGE, landingPage);
        editor.putBoolean(Const.PREF_USER_LEARNED_DRAWER, drawerLearned);
        editor.putBoolean(Const.TUTORIAL_VIEWED, tutorialViewed);
        editor.putBoolean(Const.VIEWED_NEW_FEATURE_BANNER, newGroupsViewed);
        editor.putBoolean(Const.SHOW_GRADES_ON_CARD, showGrades);
        editor.putBoolean(Const.FUN_MODE, pandasFlying);

        TutorialUtils.setHasBeenViewed(getPrefs(getApplicationContext()), TutorialUtils.TYPE.STAR_A_COURSE, tutorial_1);
        TutorialUtils.setHasBeenViewed(getPrefs(getApplicationContext()), TutorialUtils.TYPE.COLOR_CHANGING_DIALOG, tutorial_2);
        TutorialUtils.setHasBeenViewed(getPrefs(getApplicationContext()), TutorialUtils.TYPE.LANDING_PAGE, tutorial_3);
        TutorialUtils.setHasBeenViewed(getPrefs(getApplicationContext()), TutorialUtils.TYPE.MY_COURSES, tutorial_5);
        TutorialUtils.setHasBeenViewed(getPrefs(getApplicationContext()), TutorialUtils.TYPE.NOTIFICATION_PREFERENCES, tutorial_6);
        TutorialUtils.setHasBeenViewed(getPrefs(getApplicationContext()), TutorialUtils.TYPE.NAVIGATION_SHORTCUTS, tutorial_9);
        TutorialUtils.setHasBeenViewed(getPrefs(getApplicationContext()), TutorialUtils.TYPE.COURSE_GRADES, tutorial_10);


        if (UUID != null) {
            editor.putString("APID", UUID);
        }

        editor.apply();
    }


    //Add user to PreviouslySignedInUsers
    public boolean addToPreviouslySignedInUsers(SignedInUser signedInUser) {

        //Get the JSON.
        Gson gson = CanvasRestAdapter.getGSONParser();
        String signedInUserJSON = gson.toJson(signedInUser);

        //Save Signed In User to sharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(OTHER_SIGNED_IN_USERS_PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getGlobalUserId(APIHelpers.getDomain(this), APIHelpers.getCacheUser(this)), signedInUserJSON);
        return editor.commit();
    }

    public void setCalendarStartWithMonday(boolean startWeekMonday){
        getPrefs(getApplicationContext()).save(Const.CALENDAR_START_DAY_PREFS, startWeekMonday);
    }

    public String getGlobalUserId(String domain, User user) {
        return domain + "-" + user.getId();
    }

    public boolean isUserLoggedIn() {
        String token = APIHelpers.getToken(this);
        return (token != null && token.length() != 0);
    }

    public boolean shouldShowGrades() {
        SharedPreferences settings;
        if (Masquerading.isMasquerading(this)) {
            settings = getSharedPreferences(ApplicationManager.MASQ_PREF_NAME, MODE_PRIVATE);
        } else {
            settings = getSharedPreferences(ApplicationManager.PREF_NAME, MODE_PRIVATE);
        }
        return settings.getBoolean("show_grades", true);
    }

    /**
     * @param context used to check the device version and DownloadManager information
     * @return true if the download manager is available
     */
    public static boolean isDownloadManagerAvailable(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Tablet Check
    ///////////////////////////////////////////////////////////////////////////

    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    //region Analytics Event Handling

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.analytics);
        }
        return mTracker;
    }

    @Override
    public void trackButtonPressed(String buttonName, Long buttonValue) {
        if(buttonName == null) return;

        if(buttonValue == null) {
            getDefaultTracker().send(new HitBuilders.EventBuilder()
                    .setCategory("UI Actions")
                    .setAction("Button Pressed")
                    .setLabel(buttonName)
                    .build());
        } else {
            getDefaultTracker().send(new HitBuilders.EventBuilder()
                    .setCategory("UI Actions")
                    .setAction("Button Pressed")
                    .setLabel(buttonName)
                    .setValue(buttonValue)
                    .build());
        }
    }

    @Override
    public void trackScreen(String screenName) {
        if(screenName == null) return;

        Tracker tracker = getDefaultTracker();
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void trackEnrollment(String enrollmentType) {
        if(enrollmentType == null) return;

        getDefaultTracker().send(new HitBuilders.AppViewBuilder()
                .setCustomDimension(1, enrollmentType)
                .build());
    }

    @Override
    public void trackDomain(String domain) {
        if(domain == null) return;

        getDefaultTracker().send(new HitBuilders.AppViewBuilder()
                .setCustomDimension(2, domain)
                .build());
    }

    @Override
    public void trackEvent(String category, String action, String label, long value) {
        if(category == null || action == null || label == null) return;

        Tracker tracker = getDefaultTracker();
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
    }

    @Override
    public void trackUIEvent(String action, String label, long value) {
        if(action == null || label == null) return;

        getDefaultTracker().send(new HitBuilders.EventBuilder()
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
    }

    @Override
    public void trackTiming(String category, String name, String label, long duration) {
        if(category == null || name == null || label == null) return;

        Tracker tracker = getDefaultTracker();
        tracker.send(new HitBuilders.TimingBuilder()
                .setCategory(category)
                .setLabel(label)
                .setVariable(name)
                .setValue(duration)
                .build());
    }

    //endregion

    private void initPSPDFKit() {
        try {
            PSPDFKit.initialize(this, BuildConfig.PSPDFKIT_LICENSE_KEY);
        } catch (PSPDFInitializationFailedException e) {
            Utils.e("Current device is not compatible with PSPDFKIT!");
        }
    }

    /**
     * Pass the position from the language array to set the language
     * @param position
     */
    public static void setLanguage(Context context, int position) {
        ApplicationManager.getPrefs(context).save(ApplicationSettingsFragment.LANGUAGE, position);
    }

    /**
     * Pass the current context to load the stored language
     * @param context
     */
    private void loadLanguage(@NonNull Context context){
        int language = ApplicationManager.getPrefs(context).load(ApplicationSettingsFragment.LANGUAGE, 0);
        if (language >= context.getResources().getStringArray(R.array.supported_languages).length) {
            language = 0;
            ApplicationManager.getPrefs(context).save(ApplicationSettingsFragment.LANGUAGE, 0);
        }

        Locale locale;
        String languageToLoad = generateLanguageString(language);
        if(languageToLoad.equals("zh")) {
            locale = Locale.SIMPLIFIED_CHINESE;
        } else if(languageToLoad.equals("zh_TW")) {
            locale = Locale.TRADITIONAL_CHINESE;
        } else if(languageToLoad.equals("pt_BR")){
            locale = new Locale("pt", "BR");
        } else if(languageToLoad.equals("root")){
            locale = Locale.ROOT;
        } else {
            locale = new Locale(languageToLoad);
        }

        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    /**
     * The position provided needs to correspond to the position in the supported languages
     * array found in the values folder.
     * @param position
     * @return
     */
    private static String generateLanguageString(int position) {
        switch (position) {
            case 0:
                return "root";
            case 1:
                return "ar";
            case 2:
                return "zh_TW";
            case 3:
                return "zh";
            case 4:
                return "da";
            case 5:
                return "nl";
            case 6:
                return "en_AU";
            case 7:
                return "en_GB";
            case 8:
                return "en_US";
            case 9:
                return "fr";
            case 10:
                return "de";
            case 11:
                return "ja";
            case 12:
                return "mi";
            case 13:
                return "nb";
            case 14:
                return "pl";
            case 15:
                return "pt";
            case 16:
                return "pt_BR";
            case 17:
                return "ru";
            case 18:
                return "es";
            case 19:
                return "sv";
             //aka system default
            default:
                return "root";
        }
    }

}

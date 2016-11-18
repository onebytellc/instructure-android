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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.instructure.candroid.R;
import com.instructure.candroid.activity.LoginActivity;
import com.instructure.candroid.activity.NavigationActivity;
import com.instructure.candroid.activity.NotificationPreferencesActivity;
import com.instructure.candroid.activity.TutorialActivity;
import com.instructure.candroid.dialog.LegalDialogStyled;
import com.instructure.candroid.service.AlarmService;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.candroid.util.MasqueradingCallbackUtil;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.Masquerading;
import com.instructure.loginapi.login.util.ColorUtils;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.Prefs;
import com.instructure.pandautils.utils.TutorialUtils;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApplicationSettingsFragment extends OrientationChangeFragment implements TimePickerDialog.OnTimeSetListener{

    public static final String LANDING_PAGE = "landing_page";
    public static final String LANGUAGE = "language";
    public static final String TIMEPICKER_TAG = "timepicker";
    private static final long CONNECTION_TIME_OUT_MS = 1000;

    private CheckBox funModeCheckBox;
    private CheckBox calendarStartDayCheckBox;
    private View legal;
    private Spinner landingPageSpinner;
    private Spinner languageSpinner;
    private View rootView;
    private View wearReminder, wearNotifications;
    private CheckBox wearNotificationsCheckBox;
    private TimePickerDialog timePickerDialog;
    private GoogleApiClient client;
    private CheckBox showTutorialCB;

    //for masquerading
    private LinearLayout masquerade;
    private Button btnMasquerade;
    private Button btnMasqueradeSwitch;
    private GestureDetector gesture;
    private View.OnTouchListener gestureListener;
    private long first = 0;
    private long second = 0;
    private boolean firstFree = true;
    private EditText masqueradeId;
    private EditText masqueradeDomain;
    private View notificationPreferences;
    private ScrollView scrollView;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.DETAIL; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.settings);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if(savedInstanceState != null) {
            boolean showMasquerade = savedInstanceState.getBoolean(Const.MASQUERADE_VISIBLE, false);
            if(showMasquerade && masquerade != null) {
                masquerade.setVisibility(View.VISIBLE);
            }
        }

        return view;
    }

    @Override
    public View populateView(LayoutInflater inflater, ViewGroup container) {
        rootView = inflater.inflate(R.layout.application_settings_fragment_layout, container, false);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(Const.ACTIONBAR_ELEVATION);
        }

        setupViews(rootView);
        return rootView;
    }

    private void setupViews(View rootView) {
        notificationPreferences = rootView.findViewById(R.id.notificationPreferencesFrameLayout);
        legal = rootView.findViewById(R.id.legalFrameLayout);

        funModeCheckBox = (CheckBox) rootView.findViewById(R.id.enableFunModeCB);
        calendarStartDayCheckBox = (CheckBox) rootView.findViewById(R.id.calendarStartDayCB);
        showTutorialCB = (CheckBox) rootView.findViewById(R.id.showTutorialCB);
        landingPageSpinner = (Spinner)rootView.findViewById(R.id.spinner);
        landingPageSpinner.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    int landingPage = ApplicationManager.getPrefs(getContext()).load(LANDING_PAGE, 0);
                    if (landingPage >= getResources().getStringArray(R.array.navigationMenuArray).length) {
                        landingPage = 0;
                        ApplicationManager.getPrefs(getContext()).save(LANDING_PAGE, 0);
                    }
                    landingPageSpinner.setSelection(landingPage, false);
                    landingPageSpinner.post(new Runnable() {
                        @Override
                        public void run() {
                            landingPageSpinner.setOnItemSelectedListener(mLandPageListener);
                        }
                    });
                }
            }
        });

        landingPageSpinner.setAdapter(new PreferenceSpinnerAdapter(getActivity()));

        languageSpinner = (Spinner)rootView.findViewById(R.id.languageSpinner);
        languageSpinner.post(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    int language = ApplicationManager.getPrefs(getContext()).load(LANGUAGE, 0);
                    if (language >= getResources().getStringArray(R.array.supported_languages).length) {
                        language = 0;
                        ApplicationManager.getPrefs(getContext()).save(LANGUAGE, 0);
                    }
                    languageSpinner.setSelection(language, false);

                    languageSpinner.post(new Runnable() {
                        @Override
                        public void run() {
                            languageSpinner.setOnItemSelectedListener(mLanguageListener);
                        }
                    });
                }
            }
        });

        languageSpinner.setAdapter(new LanguageSpinnerAdapter(getActivity()));

        boolean startWeekMonday = ApplicationManager.getPrefs(getContext()).load(Const.CALENDAR_START_DAY_PREFS, false);

        funModeCheckBox.setChecked(ApplicationManager.getPrefs(getContext()).load(Const.FUN_MODE, false));
        calendarStartDayCheckBox.setChecked(startWeekMonday);

        btnMasquerade = (Button) rootView.findViewById(R.id.btn_masquerade);
        btnMasqueradeSwitch = (Button) rootView.findViewById(R.id.btn_masquerade_switch);
        masquerade = (LinearLayout) rootView.findViewById(R.id.masquerade);
        masqueradeId = (EditText) rootView.findViewById(R.id.masqueradeId);
        masqueradeDomain = (EditText) rootView.findViewById(R.id.masqueradeDomain);
        wearReminder = rootView.findViewById(R.id.wearReminder);
        wearNotifications = rootView.findViewById(R.id.wearNotifications);
        wearNotificationsCheckBox = (CheckBox) rootView.findViewById(R.id.wearNotificationsCB);

        if(Masquerading.isMasquerading(getActivity())) {
            masquerade.setVisibility(View.VISIBLE);
            masqueradeId.setText(String.valueOf(Masquerading.getMasqueradingId(getActivity())));
            masqueradeDomain.setText(APIHelpers.getDomain(getActivity()));
            btnMasquerade.setText(getResources().getString(R.string.stopMasquerading));
            btnMasqueradeSwitch.setVisibility(View.VISIBLE);
        } else {
            btnMasqueradeSwitch.setVisibility(View.GONE);
        }

        showTutorialCB.setChecked(!TutorialUtils.areAllTutorialsRead(ApplicationManager.getPrefs(getContext())));
        showTutorialCB.setOnCheckedChangeListener(tutorialCheckChangeListener);
        notificationPreferences.setOnClickListener(notificationPreferencesClickListener);
        wearReminder.setOnClickListener(wearReminderClickListener);
        checkIfWearableConnected();

        scrollView = (ScrollView) rootView.findViewById(R.id.scrollView);

        ImageView pulse = (ImageView)rootView.findViewById(R.id.pulse);
        new TutorialUtils(getActivity(), ApplicationManager.getPrefs(getContext()), pulse, TutorialUtils.TYPE.LANDING_PAGE)
                .setContent(getString(R.string.tutorial_tipLandingPageTitle), getString(R.string.tutorial_tipLandingPageMessage))
                .build();
    }

    private View.OnClickListener notificationPreferencesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Log to GA
            Analytics.trackAppFlow(getActivity(), NotificationPreferencesActivity.class);
            startActivity(new Intent(getActivity(), NotificationPreferencesActivity.class));
        }
    };

    private View.OnClickListener wearReminderClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //Log to GA
            Calendar calendar = Calendar.getInstance();
            timePickerDialog = TimePickerDialog.newInstance(ApplicationSettingsFragment.this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), false, false);

            timePickerDialog.show(getActivity().getSupportFragmentManager(), TIMEPICKER_TAG);
        }
    };

    private CompoundButton.OnCheckedChangeListener tutorialCheckChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //Log to GA
            if(!isChecked) {
                TutorialUtils.markAllTutorialsAsRead(ApplicationManager.getPrefs(getContext()));
            } else {
                TutorialUtils.resetAllTutorials(ApplicationManager.getPrefs(getContext()));
                Intent intent = new Intent(getActivity(), TutorialActivity.class);
                getActivity().startActivity(intent);
            }
        }
    };

    AdapterView.OnItemSelectedListener mLandPageListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            ApplicationManager.getPrefs(getContext()).save(LANDING_PAGE, position);
            Analytics.trackLandingPage(getActivity(), "Landing Page", new Long(position));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    AdapterView.OnItemSelectedListener mLanguageListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            restartAppForLocale(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpListeners();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Const.MASQUERADE_VISIBLE, masquerade.getVisibility() == View.VISIBLE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //we need to dismiss/show the dialogs so the get recreated in their proper orientation
        if(timePickerDialog.isAdded()) {
            timePickerDialog.dismiss();
            timePickerDialog.show(getActivity().getSupportFragmentManager(), TIMEPICKER_TAG);
        }
    }

    private void setUpListeners(){

        funModeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ApplicationManager.getPrefs(getContext()).save(Const.FUN_MODE, isChecked);
            }
        });

        calendarStartDayCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ApplicationManager manager = ((ApplicationManager) getActivity().getApplication());
                manager.setCalendarStartWithMonday(isChecked);
            }
        });

        wearNotificationsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                //Log to GA
                ApplicationManager.getPrefs(getContext()).save(Const.WEAR_NOTIFICATIONS, isChecked);

                if(isChecked) {
                    wearReminder.setVisibility(View.VISIBLE);
                } else {
                    wearReminder.setVisibility(View.GONE);
                }
            }
        });

        legal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LegalDialogStyled().show(getFragmentManager(), LegalDialogStyled.TAG);
            }
        });

        btnMasqueradeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMasquerading();
            }
        });
        btnMasquerade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnMasquerade.getText().toString().equals(getResources().getString(R.string.startMasquerading))) {
                    startMasquerading();
                }
                else {
                    masquerade.setVisibility(View.GONE);
                    masqueradeId.setText("");
                    Masquerading.stopMasquerading(getActivity());

                    //delete the cache for the masqueraded user
                    File cacheDir = new File(getActivity().getFilesDir(), "cache_masquerade");
                    //need to delete the contents of the internal cache folder so previous user's results don't show up on incorrect user
                    com.instructure.canvasapi.utilities.FileUtilities.deleteAllFilesInDirectory(cacheDir);

                    //clear any shared preferences for the masqueraded user
                    SharedPreferences masq_settings = getActivity().getSharedPreferences(ApplicationManager.MASQ_PREF_NAME, 0);
                    SharedPreferences.Editor masq_editor = masq_settings.edit();
                    masq_editor.clear();
                    masq_editor.apply();


                    //totally restart the app so the masquerading will apply
                    Intent mStartActivity = new Intent(getActivity(), NavigationActivity.getStartActivityClass());
                    PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), com.instructure.candroid.util.Const.MASQUERADING_PENDING_INTENT_ID, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager mgr = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                    System.exit(0);
                }
            }
        });

        //Set up gesture for the two finger double tap to show the masquerading option
        gesture = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener(){
            public boolean onDown(MotionEvent event) {
                return true;
            }
        });
        gestureListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEvent(event);
                return gesture.onTouchEvent(event);
            }
        };

        masqueradeId.setRawInputType(Configuration.KEYBOARD_12KEY);
        masqueradeId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    btnMasquerade.performClick();
                    return true;
                }
                return false;
            }
        });

        //set the gestureListener on the rootview so the two finger double tap will register
        rootView.setOnTouchListener(gestureListener);
        View rootScrollView = rootView.findViewById(R.id.rootScrollView);
        if(rootScrollView != null) {
            rootScrollView.setOnTouchListener(gestureListener);
        } else {
            rootView.findViewById(R.id.scrollView).setOnTouchListener(gestureListener);
        }

        if(scrollView != null) {
            scrollView.setOnTouchListener(gestureListener);
        }
    }

    private void startMasquerading() {
        if(masqueradeId.getText().toString().trim().length() == 0) {
            Toast.makeText(getActivity(), R.string.emptyId, Toast.LENGTH_LONG).show();
            return;
        }

        //Actually try to masquerade.
        long id = 0;
        if(masqueradeId.getText().toString().trim().length() > 0) {
            try {
                id = Long.parseLong(masqueradeId.getText().toString());
            }
            catch(NumberFormatException e) {
                id = 0;
            }
        }

        String domain = "";
        //Check to see if they're trying to switch domain as site admin
        if(masqueradeDomain.getText().toString().trim().length() != 0) {
            domain = masqueradeDomain.getText().toString().trim();
            //if there are no periods, append .instructure.com
            if (!domain.contains(".")) {
                domain += ".instructure.com";
            }
        }

        Masquerading.startMasquerading(id, getActivity(), MasqueradingCallbackUtil.getMasqueradingCallback(this), domain);
    }

    public boolean onTouchEvent(MotionEvent event) {
        try {
            int action = event.getAction() & MotionEvent.ACTION_MASK;
            //capture the event when the user lifts their fingers, not on the down press
            //to make sure they're not long pressing
            if (action == MotionEvent.ACTION_POINTER_UP) {
                //timer to get difference between clicks
                Calendar now = Calendar.getInstance();

                //detect number of fingers, change to 1 for a single-finger double-click, 3 for a triple-finger double-click!
                if (event.getPointerCount() == 2) {
                    firstFree = !firstFree;

                    if (firstFree) {
                        //if this is the first click, then there hasn't been a second
                        //click yet, also record the time
                        first = now.getTimeInMillis();
                    } else  {
                        //if this is the second click, record its time
                        second = now.getTimeInMillis();
                    }

                    //if the difference between the 2 clicks is less than 500 ms (1/2 second)
                    //Math.abs() is used because you need to be able to detect any sequence of clicks, rather than just in pairs of two
                    //(e.g. click1 could be registered as a second click if the difference between click1 and click2 > 500 but
                    //click2 and the next click1 is < 500)

                    if (Math.abs(second-first) < 500) {
                        if(masquerade.getVisibility() == View.GONE) {
                            masquerade.setVisibility(View.VISIBLE);
                            //set the domain as the current domain, they can change it if they want to
                            masqueradeDomain.setText(APIHelpers.getDomain(getActivity()));
                            //put the focus on the edit text for masquerading
                            masqueradeId.requestFocus();
                        }
                    }
                }
            }
        } catch (Exception e){
            Utils.e("Error: " + e);
        }

        return true;
    }

    /**
     * Created an adapter to show the custom drop down view that we want to show with the more material
     * arrow and no underline underneath
     */
    private class PreferenceSpinnerAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        private PreferenceSpinnerAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return getResources().getStringArray(R.array.navigationMenuArray).length;
        }

        @Override
        public Object getItem(int position) {
            return getResources().getStringArray(R.array.navigationMenuArray)[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.landing_page_spinner, parent, false);
            }

            final TextView mTitle = (TextView)convertView.findViewById(R.id.text1);
            final ImageView indicator = (ImageView) convertView.findViewById(R.id.indicator);
            indicator.setImageDrawable(ColorUtils.colorIt(
                    getResources().getColor(R.color.canvasTextDark),
                    getResources().getDrawable(R.drawable.ic_cv_expand_white)));

            final String title = (String)getItem(position);
            if(mTitle != null) {
                if (!TextUtils.isEmpty(title)) {
                    mTitle.setText(title);
                }
            }

            return convertView;
        }

        @Override
        public View getDropDownView(int position, View view, ViewGroup parent) {

            String item = (String)getItem(position);

            view = inflater.inflate(R.layout.landing_page_spinner_item, parent, false);

            TextView text1 = (TextView)view.findViewById(R.id.listTitle);
            text1.setText(item);

            return view;
        }
    }

    private class LanguageSpinnerAdapter extends PreferenceSpinnerAdapter {

        private LanguageSpinnerAdapter(Context context) {
            super(context);
        }

        @Override
        public int getCount() {
            return getResources().getStringArray(R.array.supported_languages).length;
        }

        @Override
        public Object getItem(int position) {
            return getResources().getStringArray(R.array.supported_languages)[position];
        }

    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
        Prefs prefs = new Prefs(getActivity(), Const.WEAR_REMINDER);
        prefs.save(Const.WEAR_REMINDER_HOUR, hourOfDay);
        prefs.save(Const.WEAR_REMINDER_MIN, minute);

        setupNotification();
    }

    private void setupNotification() {
        AlarmService.scheduleAlarm(getActivity());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Check if wearable is connected
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    public void checkIfWearableConnected() {

        retrieveDeviceNode(new Callback() {
            @Override
            public void success(String nodeId) {
                if(getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wearNotifications.setVisibility(View.VISIBLE);

                            //set the checked state here after the onCheckChanged listener has been set. This way when we set it the
                            //row to change the reminder time will also appear
                            wearNotificationsCheckBox.setChecked(ApplicationManager.getPrefs(getContext()).load(Const.WEAR_NOTIFICATIONS, false));
                        }
                    });
                }
            }

            @Override
            public void failed(String message) {
                if(getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wearNotifications.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        if(client != null) {
            client.disconnect();
            client = null;
        }
    }


    private GoogleApiClient getGoogleApiClient(Context context) {
        if (client == null)
            client = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .build();
        return client;
    }

    private interface Callback {
        public void success(final String nodeId);
        public void failed(final String message);
    }

    private void retrieveDeviceNode(final Callback callback) {
        final GoogleApiClient client = getGoogleApiClient(getActivity());
        new Thread(new Runnable() {

            @Override
            public void run() {
                client.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(client).await();
                List<Node> nodes = result.getNodes();
                if (nodes.size() > 0) {
                    String nodeId = nodes.get(0).getId();
                    callback.success(nodeId);
                } else {
                    callback.failed("no wearables found");
                }
                client.disconnect();
            }
        }).start();
    }

    public boolean allowBookmarking() {
        return false;
    }

    private void restartAppForLocale(final int position){
        String contentText;
        //Position 0 corresponds to the system default value
        if(position == 0){
            contentText = getResources().getString(R.string.defaultLanguageWarning);
        } else {
            contentText = getResources().getString(R.string.languageDialogText);
        }
        new MaterialDialog.Builder(getContext())
                .title(getResources().getString(R.string.restartingCanvas))
                .content(contentText)
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .canceledOnTouchOutside(false)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //If they select no, we want to reselect the language they had originally
                        int language = ApplicationManager.getPrefs(getContext()).load(LANGUAGE, 0);
                        //null out listener before re-selecting orignal
                        languageSpinner.setOnItemSelectedListener(null);
                        languageSpinner.setSelection(language, false);
                        languageSpinner.post(new Runnable() {
                            @Override
                            public void run() {
                                languageSpinner.setOnItemSelectedListener(mLanguageListener);
                            }
                        });

                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //set the language
                        ApplicationManager.setLanguage(getContext(), position);

                        //restart the App to apply language after a short delay to guarantee shared
                        //prefs is saved
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                restartApp();
                            }
                        }, 500);

                    }
                })
                .show();

    }

    private void restartApp(){
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.putExtra(com.instructure.candroid.util.Const.LANGUAGES_PENDING_INTENT_KEY, com.instructure.candroid.util.Const.LANGUAGES_PENDING_INTENT_ID);
        PendingIntent mPendingIntent = PendingIntent.getActivity(getContext(), com.instructure.candroid.util.Const.LANGUAGES_PENDING_INTENT_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)getContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

}

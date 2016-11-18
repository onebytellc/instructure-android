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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.instructure.candroid.R;
import com.instructure.candroid.interfaces.OnEventUpdatedCallback;
import com.instructure.candroid.util.StringUtilities;
import com.instructure.canvasapi.api.CalendarEventAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.ScheduleItem;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.Const;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.jsoup.helper.StringUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import hirondelle.date4j.DateTime;
import retrofit.client.Response;

public class CreateCalendarEventFragment extends ParentFragment implements DatePickerDialog.OnDateSetListener{

    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG_START = "timepicker_start";
    public static final String TIMEPICKER_TAG_END = "timepicker_end";

    private EditText mEventTitleEditText;
    private EditText mEventLocationEditText;
    private EditText mEventNoteEditText;

    private TextView mEventDateText;
    private TextView mEventStartTimeText;
    private TextView mEventEndTimeText;

    private DatePickerDialog mDatePickerDialog;
    private TimePickerDialog mTimeStartPickerDialog;
    private TimePickerDialog mTimeEndPickerDialog;

    private GregorianCalendar mStartCalendar;
    private GregorianCalendar mEndCalendar;
    private Calendar mDateCalendar;

    private CanvasCallback<ScheduleItem> mCanvasCallback;

    private OnEventUpdatedCallback mOnEventUpdatedCallback;

    @Override
    public String getFragmentTitle() {
        return getString(R.string.newEvent);
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        return getString(R.string.newEvent);
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.DIALOG;
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }

    //region LifeCycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Needed only to track time from the time pickers
        mStartCalendar = new GregorianCalendar();
        mStartCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mStartCalendar.set(Calendar.MINUTE, 0);
        mStartCalendar.set(Calendar.SECOND, 0);
        mStartCalendar.set(Calendar.MILLISECOND, 0);

        //Needed only to track time from the time pickers
        mEndCalendar = new GregorianCalendar();
        mEndCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mEndCalendar.set(Calendar.MINUTE, 0);
        mEndCalendar.set(Calendar.SECOND, 0);
        mEndCalendar.set(Calendar.MILLISECOND, 0);

        //mDateCalendar is used to instantiate the datepicker, as such, needs a locale
        if(mDateCalendar == null){
            mDateCalendar = GregorianCalendar.getInstance(Locale.getDefault());
            mDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
            mDateCalendar.set(Calendar.MINUTE, 0);
            mDateCalendar.set(Calendar.SECOND, 0);
            mDateCalendar.set(Calendar.MILLISECOND, 0);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(activity instanceof OnEventUpdatedCallback){
            mOnEventUpdatedCallback = (OnEventUpdatedCallback)activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = getLayoutInflater().inflate(R.layout.create_calendar_event_fragment_layout, container, false);
        setupDialogToolbar(rootView);
        initViews(rootView);
        setUpCanvasCallback();
        setUpListeners();
        return rootView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //we need to dismiss/show the dialogs so the get recreated in their proper orientation
        if(mDatePickerDialog != null && mDatePickerDialog.isAdded()) {
            mDatePickerDialog.dismiss();
            mDatePickerDialog.show(getActivity().getSupportFragmentManager(), DATEPICKER_TAG);
        }

        if(mTimeStartPickerDialog != null && mTimeStartPickerDialog.isAdded()) {
            mTimeStartPickerDialog.dismiss();
            mTimeStartPickerDialog.show(getActivity().getSupportFragmentManager(), TIMEPICKER_TAG_START);
        }

        if(mTimeEndPickerDialog != null && mTimeEndPickerDialog.isAdded()) {
            mTimeEndPickerDialog.dismiss();
            mTimeEndPickerDialog.show(getActivity().getSupportFragmentManager(), TIMEPICKER_TAG_END);
        }

    }

    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_save_generic, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_save:
                saveData();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if(dialog != null && !isTablet(getActivity())) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    //endregion lifeCycle

    //region View

    private void initViews(View rootView){
        mEventTitleEditText = (EditText)rootView.findViewById(R.id.titleEditText);
        mEventLocationEditText = (EditText)rootView.findViewById(R.id.locationEditText);
        mEventNoteEditText = (EditText)rootView.findViewById(R.id.eventNoteText);

        mEventDateText = (TextView)rootView.findViewById(R.id.eventDateText);
        //Set the date to the current day
        mEventDateText.setText(getFullDateString(mDateCalendar.getTime()));
        mEventStartTimeText = (TextView)rootView.findViewById(R.id.eventStartTimeText);
        mEventStartTimeText.setText(DateHelpers.getDayHourDateString(getContext(), mDateCalendar.getTime()));
        mEventEndTimeText = (TextView)rootView.findViewById(R.id.eventEndTimeText);
        mEventEndTimeText.setText(DateHelpers.getDayHourDateString(getContext(), mDateCalendar.getTime()));

        //Initialize the date picker, Time pickers are initialized upon first use
        mDatePickerDialog = DatePickerDialog.newInstance(this, mDateCalendar.get(Calendar.YEAR), mDateCalendar.get(Calendar.MONTH), mDateCalendar.get(Calendar.DAY_OF_MONTH), false);
    }

    private void setUpListeners(){
        mEventDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatePickerDialog.setYearRange(1985, 2028);
                mDatePickerDialog.setCloseOnSingleTapDay(true);
                mDatePickerDialog.show(getActivity().getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });

        mEventStartTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTimeStartPickerDialog = TimePickerDialog.newInstance(mOnStartTimeListener, mStartCalendar.get(Calendar.HOUR_OF_DAY), mStartCalendar.get(Calendar.MINUTE), false, false);
                mTimeStartPickerDialog.setCloseOnSingleTapMinute(false);
                mTimeStartPickerDialog.show(getActivity().getSupportFragmentManager(), TIMEPICKER_TAG_START);
            }
        });

        mEventEndTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTimeEndPickerDialog = TimePickerDialog.newInstance(mOnEndTimeListener, mEndCalendar.get(Calendar.HOUR_OF_DAY), mEndCalendar.get(Calendar.MINUTE), false, false);
                mTimeEndPickerDialog.setCloseOnSingleTapMinute(false);
                mTimeEndPickerDialog.show(getActivity().getSupportFragmentManager(), TIMEPICKER_TAG_END);
            }
        });
    }

    private void setUpCanvasCallback(){
        mCanvasCallback = new CanvasCallback<ScheduleItem>(this) {
            @Override
            public void firstPage(ScheduleItem scheduleItem, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()){
                    return;
                }

                showToast(R.string.eventSuccessfulCreation);
                //Refresh Calendar
                if(mOnEventUpdatedCallback != null && scheduleItem != null){
                    mOnEventUpdatedCallback.onEventSaved(scheduleItem, false);
                }
                getActivity().onBackPressed();
            }
        };
    }

    @Override
    public void onFragmentActionbarSetupComplete(FRAGMENT_PLACEMENT placement) {
        super.onFragmentActionbarSetupComplete(placement);
        setupTitle(getActionbarTitle());
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        mDateCalendar.set(year, month, day);
        mEventDateText.setText(getFullDateString(mDateCalendar.getTime()));
    }

    //endregion

    //region Helpers

    private void saveData(){
        String contextCode = "";
        String title = "";
        String note = "";
        String startTime = "";
        String endTime = "";
        String locationName = "";

        contextCode = APIHelpers.getCacheUser(getContext()).getContextId();

        if(!TextUtils.isEmpty(mEventTitleEditText.getText().toString())){
            title = mEventTitleEditText.getText().toString();
        }
        if(!TextUtils.isEmpty(mEventNoteEditText.getText().toString())){
            note = Html.fromHtml(mEventNoteEditText.getText().toString()).toString();
        }
        if(!TextUtils.isEmpty(mEventLocationEditText.getText().toString())){
            locationName = mEventLocationEditText.getText().toString();
        }
        startTime = APIHelpers.dateToString(getStartDate());
        endTime = APIHelpers.dateToString(getEndDate());

        CalendarEventAPI.createCalendarEvent(contextCode, title, note, startTime, endTime, locationName, mCanvasCallback);
    }

    private Date getStartDate(){
        mStartCalendar.set(Calendar.MONTH, mDateCalendar.get(Calendar.MONTH));
        mStartCalendar.set(Calendar.DAY_OF_MONTH, mDateCalendar.get(Calendar.DAY_OF_MONTH));
        mStartCalendar.set(Calendar.YEAR, mDateCalendar.get(Calendar.YEAR));
        return mStartCalendar.getTime();
    }

    private Date getEndDate(){
        mEndCalendar.set(Calendar.MONTH, mDateCalendar.get(Calendar.MONTH));
        mEndCalendar.set(Calendar.DAY_OF_MONTH, mDateCalendar.get(Calendar.DAY_OF_MONTH));
        mEndCalendar.set(Calendar.YEAR, mDateCalendar.get(Calendar.YEAR));
        return mEndCalendar.getTime();
    }

    public String getFullDateString(Date date) {
        if(date == null) {
            return "";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE,");
        String dayOfWeek = dateFormat.format(date);
        String dateString = DateHelpers.getFormattedDate(getContext(), date);

        return dayOfWeek + " " + dateString;
    }

    private TimePickerDialog.OnTimeSetListener mOnStartTimeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            mStartCalendar.setTimeInMillis(calendar.getTimeInMillis());
            mEventStartTimeText.setText(DateHelpers.getDayHourDateString(getContext(), mStartCalendar.getTime()));
            if(mStartCalendar.after(mEndCalendar)){
                //calendar is either equal or after, set end time = to start time.
                mEndCalendar.setTimeInMillis(mStartCalendar.getTimeInMillis());
                mEventEndTimeText.setText(DateHelpers.getDayHourDateString(getContext(), mEndCalendar.getTime()));
            }
        }
    };

    private TimePickerDialog.OnTimeSetListener mOnEndTimeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            mEndCalendar.setTimeInMillis(calendar.getTimeInMillis());
            mEventEndTimeText.setText(DateHelpers.getDayHourDateString(getContext(), mEndCalendar.getTime()));
            if(mEndCalendar.before(mStartCalendar)){
                //Calendar is either equal or before start time, set start time = to end time
                mStartCalendar.setTimeInMillis(mEndCalendar.getTimeInMillis());
                mEventStartTimeText.setText(DateHelpers.getDayHourDateString(getContext(), mStartCalendar.getTime()));
            }

        }
    };

    public static Bundle createBundle(CanvasContext canvasContext, long time){
        Bundle bundle = createBundle(canvasContext);
        bundle.putLong(Const.CALENDAR_EVENT_START_DATE, time);
        return bundle;
    }

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
        if(extras != null) {
            mDateCalendar = GregorianCalendar.getInstance(Locale.getDefault());
            mDateCalendar.setTimeInMillis(extras.getLong(Const.CALENDAR_EVENT_START_DATE));
            mDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
            mDateCalendar.set(Calendar.MINUTE, 0);
            mDateCalendar.set(Calendar.SECOND, 0);
            mDateCalendar.set(Calendar.MILLISECOND, 0);
        }
    }

    //endregion

}

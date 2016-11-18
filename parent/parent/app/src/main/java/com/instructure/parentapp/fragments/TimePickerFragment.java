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

package com.instructure.parentapp.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private TimePickerFragmentListener timePickerListener;

    private TimePickerCancelListener mCancelListener;


    public interface TimePickerFragmentListener {
        void onTimeSet(int hourOfDay, int minute);
    }
    public interface TimePickerCancelListener {
        void onCancel();
    }

    public TimePickerCancelListener getCancelListener() {
        return mCancelListener;
    }

    public void setCancelListener(TimePickerCancelListener cancelListener) {
        mCancelListener = cancelListener;
    }

    protected void notifyTimePickerCancelListener() {
        if(mCancelListener != null) {
            mCancelListener.onCancel();
        }
    }

    public TimePickerFragmentListener getTimePickerListener() {
        return this.timePickerListener;
    }

    public void setTimePickerListener(TimePickerFragmentListener listener) {
        this.timePickerListener = listener;
    }

    protected void notifyTimePickerListener(int hourOfDay, int minute) {
        if(this.timePickerListener != null) {
            this.timePickerListener.onTimeSet(hourOfDay, minute);
        }
    }

    public static TimePickerFragment newInstance(TimePickerFragmentListener listener, TimePickerCancelListener cancelListener) {
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setTimePickerListener(listener);
        fragment.setCancelListener(cancelListener);
        return fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        notifyTimePickerListener(hourOfDay, minute);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        notifyTimePickerCancelListener();
    }
}
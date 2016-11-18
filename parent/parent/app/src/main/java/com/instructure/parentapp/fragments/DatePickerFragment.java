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

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener, DatePickerDialog.OnCancelListener {

    private DatePickerFragmentListener mDatePickerListener;
    private DatePickerCancelListener mCancelListener;

    public interface DatePickerFragmentListener {
        void onDateSet(int year, int month, int day);

    }
    public interface DatePickerCancelListener {
        void onCancel();

    }

    public DatePickerCancelListener getCancelListener() {
        return mCancelListener;
    }

    public void setCancelListener(DatePickerCancelListener cancelListener) {
        mCancelListener = cancelListener;
    }

    protected void notifyDatePickerCancelListener() {
        if(mCancelListener != null) {
            mCancelListener.onCancel();
        }
    }

    public DatePickerFragmentListener getDatePickerListener() {
        return mDatePickerListener;
    }

    public void setDatePickerListener(DatePickerFragmentListener listener) {
        mDatePickerListener = listener;
    }

    protected void notifyDatePickerListener(int year, int month, int day) {
        if(mDatePickerListener != null) {
            mDatePickerListener.onDateSet(year, month, day);
        }
    }

    public static DatePickerFragment newInstance(DatePickerFragmentListener listener, DatePickerCancelListener cancelListener) {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setDatePickerListener(listener);
        fragment.setCancelListener(cancelListener);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //use the current day as the default
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        // Here we call the listener and pass the info back to it.
        notifyDatePickerListener(year, month, day);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        notifyDatePickerCancelListener();
    }
}
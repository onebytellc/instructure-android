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

package com.instructure.candroid.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import com.instructure.candroid.R;
import com.instructure.candroid.util.Analytics;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.pandautils.utils.Const;
import com.instructure.canvasapi.utilities.APIHelpers;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

public class CanvasPollDialog extends DialogFragment implements
        RadioGroup.OnCheckedChangeListener,
        TextWatcher,
        View.OnClickListener{

    private RadioGroup radioGroup;
    private RadioButton item1;
    private RadioButton item2;
    private RadioButton item3;
    private RadioButton item4;
    private RadioButton item5;

    private EditText otherEditText;
    private Button done;
    private Button cancel;

    private String finalDataToSend = "";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.canvas_poll_dialog, null);

        //Get questions and shuffle them
        String[] items = getResources().getStringArray(R.array.canvas_poll);
        List<String> shuffledItems = Arrays.asList(items);
        Collections.shuffle(shuffledItems);

        done = (Button)view.findViewById(R.id.done);
        cancel = (Button)view.findViewById(R.id.cancel);
        done.setOnClickListener(this);
        cancel.setOnClickListener(this);

        radioGroup = (RadioGroup)view.findViewById(R.id.radioGroup);
        item1 = (RadioButton)view.findViewById(R.id.item1);
        item2 = (RadioButton)view.findViewById(R.id.item2);
        item3 = (RadioButton)view.findViewById(R.id.item3);
        item4 = (RadioButton)view.findViewById(R.id.item4);
        item5 = (RadioButton)view.findViewById(R.id.item5);

        otherEditText = (EditText)view.findViewById(R.id.otherEditText);
        otherEditText.addTextChangedListener(this);
        otherEditText.setEnabled(false);

        setupRadioGroup(shuffledItems);
        radioGroup.setOnCheckedChangeListener(this);

        return new MaterialDialog.Builder(getActivity())
                                 .customView(view, false)
                                 .show();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
    }

    private void setupRadioGroup(List<String> items) {
        RadioButton[] buttons = new RadioButton[]{item1, item2, item3, item4, item5};
        for(int i = 0; i < buttons.length; i++) {
            buttons[i].setText(items.get(i));
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.done) {
            if(radioGroup.getCheckedRadioButtonId() != -1) {
                sendToAnalytics();
            } else {
                Toast.makeText(getActivity(), R.string.poll_warning, Toast.LENGTH_SHORT).show();
            }
        } else if(v.getId() == R.id.cancel) {
            finalDataToSend = "User declined do complete poll.";
            sendToAnalytics();
        } else {
            getDialog().dismiss();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        otherEditText.setEnabled(false);
        otherEditText.setVisibility(View.GONE);
        getRandomDateToShow(getContext());
        switch (checkedId) {
            case R.id.item1:
                finalDataToSend = getUsefulDataString(item1.getText().toString());
                break;
            case R.id.item2:
                finalDataToSend = getUsefulDataString(item2.getText().toString());
                break;
            case R.id.item3:
                finalDataToSend = getUsefulDataString(item3.getText().toString());
                break;
            case R.id.item4:
                finalDataToSend = getUsefulDataString(item4.getText().toString());
                break;
            case R.id.item5:
                finalDataToSend = getUsefulDataString(item5.getText().toString());
                break;
            case R.id.item6:
                finalDataToSend = "";
                otherEditText.setEnabled(true);
                otherEditText.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        finalDataToSend = s.toString();
    }

    private String getUsefulDataString(String selectedItemText) {
        //We do this for localization so we do not get non-english strings.
        String data = "";
        if(getResources().getString(R.string.poll_item_1).equals(selectedItemText)) {
            data = "Check my grades";
        } else if(getResources().getString(R.string.poll_item_2).equals(selectedItemText)) {
            data = "Send or view a message";
        } else if(getResources().getString(R.string.poll_item_3).equals(selectedItemText)) {
            data = "Turn in an assignment";
        } else if(getResources().getString(R.string.poll_item_4).equals(selectedItemText)) {
            data = "Look at course content";
        } else if(getResources().getString(R.string.poll_item_5).equals(selectedItemText)) {
            data = "Take a quiz";
        } else {
            data = otherEditText.getText().toString();
            if(TextUtils.isEmpty(data)) {
                data = "Other: no data entered by user";
            }
        }
        return data;
    }

    private void sendToAnalytics() {
        if(!TextUtils.isEmpty(finalDataToSend)) {
            Analytics.trackCanvasPollData(getActivity(), finalDataToSend, getResources().getBoolean(R.bool.isTablet));
            getDialog().dismiss();
        }
    }

    /**
     * Does all logic to determine what date to show and shows as needed. Will store all needed date to prevent showing again.
     * @param context
     */
    public void show(Context context, FragmentManager manager, String tag) {
        if(shouldShowPoll(context)) {
            ApplicationManager.getPrefs(context).save(Const.HAS_SHOWN_CANVAS_POLL, true);
        }
        super.show(manager, tag);
    }

    /**
     * Tells if the canvas poll should be shown
     * @param context
     * @return
     */
    public static boolean shouldShowPoll(Context context) {
        boolean hasBeenViewed = ApplicationManager.getPrefs(context).load(Const.HAS_SHOWN_CANVAS_POLL, false);
        if(hasBeenViewed) {
            return false;
        }

        //Only some institutions can be polled, this will check. If they cannot be polled we set the poll as viewed.
        if(!isDomainAllowedToBePolled(context)) {
            ApplicationManager.getPrefs(context).save(Const.HAS_SHOWN_CANVAS_POLL, true);
            return false;
        }

        Calendar today = Calendar.getInstance();
        long dateToShowInMilli = getRandomDateToShow(context);
        GregorianCalendar dateToShow = new GregorianCalendar();
        dateToShow.setTimeInMillis(dateToShowInMilli);

        if(today.after(dateToShow)) {
            return true;
        }
        return false;
    }

    //0 = no, 1 = yes, -1 = need to find out
    private static boolean isDomainAllowedToBePolled(Context context) {
        final int canBePolled = ApplicationManager.getPrefs(context).load(Const.CAN_BE_POLLED, -1);
        if(canBePolled == -1) {
            String[] pollDomainList = context.getResources().getStringArray(R.array.pollDomainList);
            String currentDomain = APIHelpers.getDomain(context);
            for (String domain : pollDomainList) {
                if (currentDomain.equalsIgnoreCase(domain)) {
                    ApplicationManager.getPrefs(context).save(Const.CAN_BE_POLLED, 1);
                    return true;
                }
            }
            ApplicationManager.getPrefs(context).save(Const.CAN_BE_POLLED, 0);
            return false;
        } else {
            return (canBePolled == 0 ? false : true);
        }
    }

    /**
     * Gets the date to show the canvas poll. If no date exists (is 0) then creates a sudo-random date.
     * @return the date in long format.
     */
    private static long getRandomDateToShow(Context context) {
        final long storedDate = ApplicationManager.getPrefs(context).load(Const.DATE_TO_SHOW_CANVAS_POLL, 0L);
        if(storedDate != 0) {
            return storedDate;
        } else {
            int[] daysOut = new int[]{15, 30, 45, 60};
            int randomInt = daysOut[new Random().nextInt(daysOut.length)];
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, randomInt);
            long date = calendar.getTimeInMillis();
            ApplicationManager.getPrefs(context).save(Const.DATE_TO_SHOW_CANVAS_POLL, date);
            return date;
        }
    }
}

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

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.instructure.candroid.R;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Enrollment;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;

import java.util.Calendar;
import java.util.Date;

import retrofit.client.Response;

public class SettingsFragment extends OrientationChangeFragment implements DatePickerDialog.OnDateSetListener {

    public static final String DATEPICKER_TAG = "datepicker";

    private enum DATEPICKER_TYPE {START, END};
    private View rootView;

    private Course course;

    private ViewFlipper viewFlipper;


    //View the Settings
    private TextView courseName;
    private TextView courseCode;

    private LinearLayout startAtLayout;
    private TextView startAt;

    private LinearLayout endAtLayout;
    private TextView endAt;

    private TextView license;
    private TextView visibility;


    //Edit the settings
    private TextView editCourseName;
    private TextView editCourseCode;

    private LinearLayout editStartAtLayout;
    private TextView editStartDate;
    private ImageView editStartAt;

    private LinearLayout editEndAtLayout;
    private TextView editEndDate;
    private ImageView editEndAt;

    private Spinner editLicense;
    private CheckBox editVisibility;

    private DatePickerDialog datePickerDialog;
    private Date editedEndDate;
    private Date editedStartDate;
    private DATEPICKER_TYPE datepickerType;

    private boolean addMenuItems = true;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.settings);
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public View populateView(LayoutInflater inflater, ViewGroup container) {
        rootView = inflater.inflate(R.layout.settings_fragment_layout, container, false);

        viewFlipper = (ViewFlipper) rootView.findViewById(R.id.view_flipper);

        //View the Settings
        courseName = (TextView) rootView.findViewById(R.id.course_name);
        courseCode = (TextView) rootView.findViewById(R.id.course_code);
        startAt = (TextView) rootView.findViewById(R.id.start_date);
        startAtLayout = (LinearLayout) rootView.findViewById(R.id.starts_layout);

        endAt = (TextView) rootView.findViewById(R.id.end_date);
        endAtLayout = (LinearLayout) rootView.findViewById(R.id.ends_layout);

        license = (TextView) rootView.findViewById(R.id.license_string);
        visibility = (TextView) rootView.findViewById(R.id.visibility_string);


        //Edit the Settings
        editCourseName = (TextView) rootView.findViewById(R.id.edit_course_name);
        editCourseCode = (TextView) rootView.findViewById(R.id.edit_course_code);
        editStartAt = (ImageView) rootView.findViewById(R.id.edit_start_date);
        editStartAt.setImageDrawable(CanvasContextColor.getColoredDrawable(getContext(), R.drawable.ic_cv_calendar_fill, course));

        editStartAt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                if(editedStartDate == null && course.getStartDate() != null) {
                    calendar.setTime(course.getStartDate());
                } else if(editedStartDate != null) {
                    calendar.setTime(editedStartDate);
                }
                datePickerDialog = DatePickerDialog.newInstance(SettingsFragment.this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),false);

                datePickerDialog.setYearRange(1985, 2028);
                datePickerDialog.setCloseOnSingleTapDay(true);

                //set the type so we know which fields to edit (start vs. end)
                datepickerType = DATEPICKER_TYPE.START;

                datePickerDialog.show(getActivity().getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });
        editStartDate = (TextView) rootView.findViewById(R.id.tvStartDate);

        if(course.getStartDate() != null) {
            editStartDate.setText(DateHelpers.getShortDate(getActivity(), course.getStartDate()));
        } else {
            editStartDate.setText(getString(R.string.noDate));
        }
        editEndAt = (ImageView) rootView.findViewById(R.id.edit_end_date);
        editEndAt.setImageDrawable(CanvasContextColor.getColoredDrawable(getContext(), R.drawable.ic_cv_calendar_fill, course));

        editEndAt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                if(editedEndDate == null && course.getEndDate() != null) {
                    calendar.setTime(course.getEndDate());
                } else if(editedEndDate != null) {
                    calendar.setTime(editedEndDate);
                }
                datePickerDialog = DatePickerDialog.newInstance(SettingsFragment.this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),false);

                datePickerDialog.setYearRange(1985, 2028);
                datePickerDialog.setCloseOnSingleTapDay(true);

                //set the type so we know which fields to edit (start vs. end)
                datepickerType = DATEPICKER_TYPE.END;

                datePickerDialog.show(getActivity().getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });

        editEndDate = (TextView) rootView.findViewById(R.id.tvEndDate);

        if(course.getEndDate() != null) {
            editEndDate.setText(DateHelpers.getShortDate(getActivity(), course.getEndDate()));
        } else {
            editEndDate.setText(getString(R.string.noDate));
        }
        editEndAtLayout = (LinearLayout) rootView.findViewById(R.id.edit_ends_layout);
        editStartAtLayout = (LinearLayout) rootView.findViewById(R.id.edit_starts_layout);

        editLicense = (Spinner) rootView.findViewById(R.id.edit_license_spinner);
        editVisibility = (CheckBox) rootView.findViewById(R.id.edit_visibility);


        viewFlipper.setInAnimation(getContext(), R.anim.fade_in_quick);
        viewFlipper.setOutAnimation(getContext(), R.anim.fade_out_quick);


        if(getArguments().getBoolean(Const.IN_EDIT_MODE)) {
            resetCourseEditData();
            viewFlipper.showNext();
            getActivity().supportInvalidateOptionsMenu();
        } else {
            resetCourseData();
        }
        return rootView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {


        if(getArguments() != null) {
            getArguments().putBoolean(Const.IN_EDIT_MODE, isEditMode());
        }
        //we need to dismiss/show the dialogs so the get recreated in their proper orientation
        if(datePickerDialog != null && datePickerDialog.isAdded()) {
            datePickerDialog.dismiss();
            datePickerDialog.show(getActivity().getSupportFragmentManager(), DATEPICKER_TAG);
        }
        super.onConfigurationChanged(newConfig);
    }


    ///////////////////////////////////////////////////////////////////////////
    // ActionBar
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);

        if(!addMenuItems) {
            return;
        }

        //Only teachers can edit a course.
        if (course.isTeacher()) {
            if (!isEditMode()) {
                menu.add(getString(R.string.edit))
                        .setIcon(R.drawable.ic_cv_edit_white_thin)
                        .setTitle(R.string.edit)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            } else {
                //We're in edit mode.
                menu.add(getString(R.string.cancel))
                        .setIcon(R.drawable.ic_cv_cancel_white_thin)
                        .setTitle(R.string.cancel)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

                menu.add(getString(R.string.confirm))
                        .setIcon(R.drawable.ic_cv_save_white_thin)
                        .setTitle(R.string.confirm)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getTitle() == null) {
            return super.onOptionsItemSelected(menuItem);
        } else if (getString(R.string.confirm).equals(menuItem.getTitle().toString())) {
            //confirm
            String newCourseName = editCourseName.getText().toString();
            String newCourseCode = editCourseCode.getText().toString();
            Date newStartDate = editedStartDate;
            Date newEndDate = editedEndDate;

            final Course.LICENSE license = Course.LICENSE.values()[editLicense.getSelectedItemPosition()];
            final boolean isPubliclyAvailable = editVisibility.isChecked();

            CourseAPI.updateCourse(newCourseName, newCourseCode, newStartDate, newEndDate, license, isPubliclyAvailable, course, new CanvasCallback<Course>(this) {
                @Override
                public void firstPage(Course course, LinkHeaders linkHeaders, Response response) {
                    if(!apiCheck()){
                        return;
                    }

                    //The PUT doesn't return optional parameters, even if you changed them. That's pretty stupid if you ask me.
                    course.setLicense(license);
                    course.setIsPublic(isPubliclyAvailable);

                    //The PUT also doesn't return enrollments, which we depend on here.
                    if (SettingsFragment.this.course.getEnrollments() != null) {
                        for (Enrollment enrollment : SettingsFragment.this.course.getEnrollments()) {
                            course.addEnrollment(enrollment);
                        }

                    }
                    //Let us get the changes.
                    SettingsFragment.this.course = course;
                    resetCourseData();

                    //reset the variables
                    editedEndDate = null;
                    editedStartDate = null;

                    //Leave edit mode.
                    viewFlipper.showNext();
                    getActivity().supportInvalidateOptionsMenu();
                }
            });
        } else {

            if (getString(R.string.edit).equals(menuItem.getTitle().toString())) {
                //Enter edit mode!
                if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                    Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                    return true;
                }

                resetCourseEditData();
            }

            viewFlipper.showNext();
            getActivity().supportInvalidateOptionsMenu();
        }
        return true;
    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {
        if(source.isAPI()) {
            hideProgressBar();
        }
    }

    public boolean isEditMode() {
        return viewFlipper.getDisplayedChild() != 0;
    }

    public void resetCourseData() {
        courseName.setText(course.getName());

        courseCode.setText(course.getCourseCode());

        if (course.getStartDate() != null) {
            startAt.setText(APIHelpers.dateToDayMonthYearString(getContext(), course.getStartDate()));
        } else {
            startAtLayout.setVisibility(View.GONE);
            editStartAtLayout.setVisibility(View.GONE);
        }

        if (course.getEndDate() != null) {
            endAt.setText(APIHelpers.dateToDayMonthYearString(getContext(), course.getEndDate()));

        } else {
            endAtLayout.setVisibility(View.GONE);
            editEndAtLayout.setVisibility(View.GONE);
        }

        license.setText(course.getLicensePrettyPrint());

        if (course.isPublic()) {
            visibility.setText(getString(R.string.publiclyAvailable));
        } else {
            visibility.setText(getString(R.string.privatelyAvailable));
        }

    }

    public void resetCourseEditData() {
        editCourseName.setText(course.getName());
        editCourseCode.setText(course.getCourseCode());
        editVisibility.setChecked(course.isPublic());

        //Add the license spinner adapter.
        int currentIndex = 0;
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item);
        for (int i = 0; i < Course.LICENSE.values().length; i++) {
            Course.LICENSE currentLicense = Course.LICENSE.values()[i];
            if (currentLicense == course.getLicense()) {
                currentIndex = i;
            }

            spinnerArrayAdapter.add(Course.licenseToPrettyPrint(currentLicense));
        }

        editLicense.setAdapter(spinnerArrayAdapter);
        editLicense.setSelection(currentIndex, true);


        //Handle course dates
        if (course.getStartDate() != null) {
            editStartDate.setText(DateHelpers.getShortDate(getActivity(), course.getStartDate()));
        }

        if (course.getEndDate() != null) {
            editEndDate.setText(DateHelpers.getShortDate(getActivity(), course.getEndDate()));
        }

        //if we've edited the dates, display those
        setEditedDates();
    }


    @Override
    public void onDateSet(DatePickerDialog datePickerDialog1, int year, int month, int day) {
        if(datepickerType == DATEPICKER_TYPE.START) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            editedStartDate = calendar.getTime();

            editStartDate.setText(DateHelpers.getShortDate(getActivity(),editedStartDate));

        } else if(datepickerType == DATEPICKER_TYPE.END) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            editedEndDate = calendar.getTime();

            editEndDate.setText(DateHelpers.getShortDate(getActivity(),editedEndDate));
        }

    }

    private void setEditedDates() {
        if(editedStartDate != null) {
            editStartDate.setText(DateHelpers.getShortDate(getActivity(),editedStartDate));
        }

        if(editedEndDate != null) {
            editEndDate.setText(DateHelpers.getShortDate(getActivity(),editedEndDate));
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle bundle) {
        super.handleIntentExtras(bundle);

        if(getCanvasContext() instanceof Course) {
            course = (Course) getCanvasContext();
        }
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}

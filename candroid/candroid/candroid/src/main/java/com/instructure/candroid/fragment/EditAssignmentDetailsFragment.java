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
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.instructure.candroid.R;
import com.instructure.canvasapi.api.AssignmentAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.AssignmentGroup;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit.client.Response;

public class EditAssignmentDetailsFragment extends OrientationChangeFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    public static final String DATEPICKER_TAG = "datepicker";
    public static final String TIMEPICKER_TAG = "timepicker";

    //VIEWS
    private EditText pointsPossibleET;
    private Spinner assignmentGroupSpinner;
    private Spinner gradingTypeSpinner;
    private Spinner turnInTypeSpinner;
    private LinearLayout onlineSubmissionTypeLayout;
    private LinearLayout allOnlineSubmissionTypes;
    private EditText assignmentTitleET;
    private TextView dateText;
    private ImageView calendarIcon;
    private CheckBox notifyUsers;

    private ArrayList<CheckBox> submissionTypesArrayList;
    private Assignment.TURN_IN_TYPE assignmentTurnInType;

    private Assignment assignment;
    private Course course;

    private AssignmentGroup[] assignmentGroups;

    private static final Assignment.TURN_IN_TYPE[] TURN_IN_TYPES_ALLOWED = { Assignment.TURN_IN_TYPE.NONE,Assignment.TURN_IN_TYPE.ONLINE, Assignment.TURN_IN_TYPE.ON_PAPER};

    private CanvasCallback<AssignmentGroup[]> assignmentGroupCanvasCallback;
    private CanvasCallback<Assignment> assignmentCanvasCallback;

    private String actionbarTitle = null;

    private OnAssignmentDetailsChanged onAssignmentDetailsChanged;

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;
    private Calendar calendar;
    private Calendar editedCalendar;
    private Date editedDate;

    public interface OnAssignmentDetailsChanged {
        void onAssignmentDetailsChanged(Assignment assignment);
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.DETAIL; }

    @Override
    public String getFragmentTitle() {
        return actionbarTitle;
    }

    @Nullable
    @Override
    public String getActionbarTitle() {
        return getFragmentTitle();
    }

    @Override
    public View populateView(LayoutInflater inflater, ViewGroup container) {
        View rootView = inflater.inflate(R.layout.edit_assignment_details_layout, container, false);

        initViews(rootView);
        setCanvasCallback();

        AssignmentAPI.getAssignmentGroupsList(course.getId(), assignmentGroupCanvasCallback);
        assignmentTurnInType = assignment.getTurnInType();

        if(assignment.getName() != null) {
            actionbarTitle = assignment.getName();
        } else {
            actionbarTitle = getString(R.string.assignments);
        }

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(activity instanceof OnAssignmentDetailsChanged){
            onAssignmentDetailsChanged= ((OnAssignmentDetailsChanged)activity);
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
    public void onConfigurationChanged(Configuration newConfig) {

        //we need to dismiss/show the dialogs so the get recreated in their proper orientation
        if(datePickerDialog.isAdded()) {
            datePickerDialog.dismiss();
            datePickerDialog.show(getActivity().getSupportFragmentManager(), DATEPICKER_TAG);
        }
        if(timePickerDialog.isAdded()) {
            timePickerDialog.dismiss();
            timePickerDialog.show(getActivity().getSupportFragmentManager(), TIMEPICKER_TAG);
        }

        super.onConfigurationChanged(newConfig);
    }

    //////////////////////////////////////////////////////////////
    // View
    //////////////////////////////////////////////////////////////
    private void initViews(View rootView) {
        pointsPossibleET = (EditText) rootView.findViewById(R.id.pointsPossibleET);
        assignmentGroupSpinner = (Spinner) rootView.findViewById(R.id.assignmentGroupSpinner);
        gradingTypeSpinner = (Spinner) rootView.findViewById(R.id.gradingTypeSpinner);
        turnInTypeSpinner = (Spinner) rootView.findViewById(R.id.submissionTypeSelectedSpinner);
        onlineSubmissionTypeLayout = (LinearLayout) rootView.findViewById(R.id.onlineSubmissionTypes);
        allOnlineSubmissionTypes = (LinearLayout) rootView.findViewById(R.id.allOnlineSubmissionTypes);
        submissionTypesArrayList = new ArrayList<>();
        assignmentTitleET = (EditText)rootView.findViewById(R.id.assignmenttitleET);
        dateText = (TextView)rootView.findViewById(R.id.dateText);

        calendarIcon = (ImageView)rootView.findViewById(R.id.dueDateDT);
        calendarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.setYearRange(1985, 2028);
                datePickerDialog.setCloseOnSingleTapDay(true);
                datePickerDialog.show(getActivity().getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });
        calendar = Calendar.getInstance();
        //set the calendar to the due date of the assignment
        if(assignment.getDueDate() != null) {
            calendar.setTime(assignment.getDueDate());
        } else {
            dateText.setText(getString(R.string.noDate));
        }

        datePickerDialog = DatePickerDialog.newInstance(this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),false);
        timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY) ,calendar.get(Calendar.MINUTE), false, false);
        calendarIcon.setImageDrawable(CanvasContextColor.getColoredDrawable(getContext(), R.drawable.ic_cv_calendar_fill, course));

        notifyUsers = (CheckBox)rootView.findViewById(R.id.notifyUsers);
    }

    private void populateViewData(){

        assignmentTitleET.setText(assignment.getName());
        if(assignment.getDueDate() != null) {
            dateText.setText(DateHelpers.getShortDate(getActivity(), assignment.getDueDate()));
        }

        pointsPossibleET.setText("" + assignment.getPointsPossible());

        //We are unable to set points possible on a quiz
        if(assignment.getTurnInType() == Assignment.TURN_IN_TYPE.QUIZ){
            pointsPossibleET.setEnabled(false);
        }

        int currentIndex = 0;

        //Set up assignmentGroupSpinner adapter
        ArrayAdapter<String> assignmentGroupSpinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item);
        for (int i = 0; i < assignmentGroups.length; i++) {
            AssignmentGroup currentAssignmentGroup = assignmentGroups[i];
            if (currentAssignmentGroup.getId() == assignment.getAssignmentGroupId()) {
                currentIndex = i;
            }
            assignmentGroupSpinnerArrayAdapter.add(currentAssignmentGroup.getName());
        }
        assignmentGroupSpinner.setAdapter(assignmentGroupSpinnerArrayAdapter);
        assignmentGroupSpinner.setSelection(currentIndex, true);

        //Set up gradingTypeSpinner adapter
        currentIndex = 0;
        ArrayAdapter<String> gradingTypeSpinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item);
        for (int i = 0; i < Assignment.GRADING_TYPE.values().length; i++) {
            Assignment.GRADING_TYPE currentGradingType = Assignment.GRADING_TYPE.values()[i];
            if (currentGradingType == assignment.getGradingType()) {
                currentIndex = i;
            }

            gradingTypeSpinnerArrayAdapter.add(Assignment.gradingTypeToPrettyPrintString(currentGradingType, getContext()));
        }
        gradingTypeSpinner.setAdapter(gradingTypeSpinnerArrayAdapter);
        gradingTypeSpinner.setSelection(currentIndex, true);

        //Set the Type spinner adapter
        if (assignmentTurnInType != Assignment.TURN_IN_TYPE.DISCUSSION && assignmentTurnInType != Assignment.TURN_IN_TYPE.QUIZ && assignmentTurnInType != Assignment.TURN_IN_TYPE.EXTERNAL_TOOL) {
            currentIndex = 0;
            ArrayAdapter<String> submissionTypeSelectedAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item);
            for (int i = 0; i < TURN_IN_TYPES_ALLOWED.length; i++) {

                Assignment.TURN_IN_TYPE turnInType = TURN_IN_TYPES_ALLOWED[i];
                if (turnInType == assignmentTurnInType) {
                    currentIndex = i;
                }

                submissionTypeSelectedAdapter.add(Assignment.turnInTypeToPrettyPrintString(turnInType, getContext()));

            }

            turnInTypeSpinner.setAdapter(submissionTypeSelectedAdapter);
            turnInTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Assignment.TURN_IN_TYPE turnInType = Assignment.stringToTurnInType((String) adapterView.getItemAtPosition(i), getContext());

                    if (turnInType == Assignment.TURN_IN_TYPE.ONLINE) {
                        allOnlineSubmissionTypes.setVisibility(View.VISIBLE);
                    } else {
                        allOnlineSubmissionTypes.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView){}
            });
            turnInTypeSpinner.setSelection(currentIndex, true);
        } else {
            ArrayAdapter<String> dummySpinnerAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item);
            dummySpinnerAdapter.add(Assignment.turnInTypeToPrettyPrintString(assignmentTurnInType, getContext()));
            turnInTypeSpinner.setAdapter(dummySpinnerAdapter);
            turnInTypeSpinner.setEnabled(false);
        }

        List<Assignment.SUBMISSION_TYPE> checkedSubmissionTypes = assignment.getSubmissionTypes();
        //Populate Online Submission options
        submissionTypesArrayList.clear();
        allOnlineSubmissionTypes.removeAllViews();

        for (int i = 0; i < Assignment.ONLINE_SUBMISSIONS.length; i++) {
            Assignment.SUBMISSION_TYPE currentSubmissionType = Assignment.ONLINE_SUBMISSIONS[i];
            CheckBox submissionTypeCheckBox = new CheckBox(getContext());
            submissionTypeCheckBox.setTag(currentSubmissionType);
            submissionTypeCheckBox.setText(Assignment.submissionTypeToPrettyPrintString(currentSubmissionType, getContext()));
            if (checkedSubmissionTypes.contains(currentSubmissionType)) {
                submissionTypeCheckBox.setChecked(true);
            }
            submissionTypeCheckBox.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);

            submissionTypesArrayList.add(submissionTypeCheckBox);
            allOnlineSubmissionTypes.addView(submissionTypeCheckBox);
        }

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(assignmentTitleET.getWindowToken(), 0);
    }
    //////////////////////////////////////////////////////////////
    // Helper methods
    //////////////////////////////////////////////////////////////

    private void saveData(){
        String editedAssignmentTitle = assignmentTitleET.getText().toString();
        //Date editedDueAt = calendarIcon.getDate();
        boolean shouldNotifyUsers = notifyUsers.isChecked();

        Double editedPointsPossible = Double.parseDouble(pointsPossibleET.getText().toString());
        String editedAssignmentGroupString = (String) assignmentGroupSpinner.getSelectedItem();
        Long editedAssignmentGroup = null;

        for (AssignmentGroup group : assignmentGroups) {
            if (editedAssignmentGroupString.equals(group.getName())) {
                editedAssignmentGroup = group.getId();
                break;
            }
        }


        Assignment.GRADING_TYPE editedGradingType = Assignment.getGradingTypeFromString((String) gradingTypeSpinner.getSelectedItem(), getContext());
        Assignment.SUBMISSION_TYPE[] editedSubmissionTypes;
        Assignment.TURN_IN_TYPE editedTurnInType = Assignment.stringToTurnInType((String) turnInTypeSpinner.getSelectedItem(), getContext());

        if (editedTurnInType == Assignment.TURN_IN_TYPE.ONLINE) {
            ArrayList<Assignment.SUBMISSION_TYPE> newSubmissionTypes = new ArrayList<Assignment.SUBMISSION_TYPE>();
            for (CheckBox checkbox : submissionTypesArrayList) {
                if (checkbox.isChecked()) {
                    newSubmissionTypes.add((Assignment.SUBMISSION_TYPE) checkbox.getTag());
                }
            }

            editedSubmissionTypes = newSubmissionTypes.toArray(new Assignment.SUBMISSION_TYPE[newSubmissionTypes.size()]);
        } else if (editedTurnInType == Assignment.TURN_IN_TYPE.ON_PAPER) {
            editedSubmissionTypes = new Assignment.SUBMISSION_TYPE[1];
            editedSubmissionTypes[0] = Assignment.SUBMISSION_TYPE.ON_PAPER;
        } else if (editedTurnInType == Assignment.TURN_IN_TYPE.NONE) {
            editedSubmissionTypes = new Assignment.SUBMISSION_TYPE[1];
            editedSubmissionTypes[0] = Assignment.SUBMISSION_TYPE.NONE;
        } else {
            editedSubmissionTypes = null;
        }

        assignment.setName(editedAssignmentTitle);
        assignment.setAssignmentGroupId(editedAssignmentGroup);
        assignment.setSubmissionTypes(editedSubmissionTypes);
        assignment.setPointsPossible(editedPointsPossible);
        assignment.setGradingType(editedGradingType);
        if(editedDate != null) {
            assignment.setDueDate(editedDate);
        }

        AssignmentAPI.editAssignment(assignment,shouldNotifyUsers, assignmentCanvasCallback);
    }

    private void assignmentEdited(Assignment assignment){
        hideProgressBar();

        if(onAssignmentDetailsChanged != null){
            onAssignmentDetailsChanged.onAssignmentDetailsChanged(assignment);
        }
        getActivity().onBackPressed();
    }


    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        editedCalendar = Calendar.getInstance();
        editedCalendar.set(year, month, day);

        timePickerDialog.setCloseOnSingleTapMinute(true);
        timePickerDialog.show(getActivity().getSupportFragmentManager(), TIMEPICKER_TAG);

    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
        editedCalendar.set(editedCalendar.get(Calendar.YEAR), editedCalendar.get(Calendar.MONTH), editedCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);
        editedDate = editedCalendar.getTime();
        dateText.setText(DateHelpers.getShortDate(getActivity(), editedDate));
    }

    //////////////////////////////////////////////////////////////
    // Callbacks
    //////////////////////////////////////////////////////////////
    public void setCanvasCallback() {
        assignmentGroupCanvasCallback = new CanvasCallback<AssignmentGroup[]>(this) {
            @Override
            public void firstPage(AssignmentGroup[] assignmentGroups, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()){
                    return;
                }

                //[] of fresh data.
                EditAssignmentDetailsFragment.this.assignmentGroups = assignmentGroups;
                   populateViewData();

            }
        };

        assignmentCanvasCallback = new CanvasCallback<Assignment>(this) {
            @Override
            public void cache(Assignment assignment) {}

            @Override
            public void firstPage(Assignment assignment, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()){
                    return;
                }

                assignmentEdited(assignment);
            }
        };
    }
    //////////////////////////////////////////////////////////////
    // Intent
    //////////////////////////////////////////////////////////////
    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
        course = (Course)getCanvasContext();
        assignment = extras.getParcelable(Const.ASSIGNMENT);

    }
    public static Bundle createBundle(Assignment assignment, Course course){
        Bundle bundle = createBundle(course);
        bundle.putParcelable(Const.ASSIGNMENT, assignment);

        return bundle;
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}

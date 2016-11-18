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

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi2.models.Assignment;
import com.instructure.canvasapi2.models.LockInfo;
import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.utils.DateHelper;
import com.instructure.pandautils.utils.AssignmentUtils2;
import com.instructure.pandautils.utils.Const;
import com.instructure.parentapp.R;
import com.instructure.parentapp.database.DatabaseHandler;
import com.instructure.parentapp.receivers.AlarmReceiver;
import com.instructure.parentapp.util.AnalyticUtils;
import com.instructure.parentapp.util.RouterUtils;
import com.instructure.parentapp.util.ViewUtils;
import com.instructure.parentapp.video.ActivityContentVideoViewClient;
import com.instructure.parentapp.view.CanvasWebView;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class AssignmentFragment extends ParentFragment implements
        TimePickerFragment.TimePickerFragmentListener,
        TimePickerFragment.TimePickerCancelListener,
        DatePickerFragment.DatePickerFragmentListener,
        DatePickerFragment.DatePickerCancelListener {

    private static final String DATE_PICKER_TAG = "datePicker";
    private static final String TIME_PICKER_TAG = "timePicker";

    private Assignment mAssignment;
    private String mName;
    private Student mStudent;
    private DatabaseHandler mDatabaseHandler;
    private int mAlarmId = -1;
    private SwitchCompat.OnCheckedChangeListener mCheckedChangeListener;
    //views
    private TextView mAssignmentTitle, mCourseName, mDueDate, mStatus, mGrade, mAlarmDetails;
    private CanvasWebView mAssignmentWebView;
    private TimePickerFragment mTimePicker;
    private DatePickerFragment mDatePickerDialog;
    private SwitchCompat mAlarmSwitch;
    private Calendar mSetDate;

    public static AssignmentFragment newInstance(Assignment assignment, String courseName, Student student) {
        Bundle args = new Bundle();
        args.putParcelable(Const.ASSIGNMENT, assignment);
        args.putString(Const.NAME, courseName);
        args.putParcelable(Const.STUDENT, student);
        AssignmentFragment fragment = new AssignmentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getRootLayout() {
        return R.layout.fragment_assignment;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAssignmentWebView != null) {
            mAssignmentWebView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAssignmentWebView != null) {
            mAssignmentWebView.onResume();
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAssignment = getArguments().getParcelable(Const.ASSIGNMENT);
        mName = getArguments().getString(Const.NAME, "");
        mStudent = getArguments().getParcelable(Const.STUDENT);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getRootLayout(), container, false);

        setupViews(view);
        setupListeners();
        setupDialogToolbar(view);

        setupAlarmInfo();

        setupGradeAndStatus();
        return view;
    }

    @Override
    protected void setupDialogToolbar(View rootView) {
        super.setupDialogToolbar(rootView);
    }

    private void setupAlarmInfo() {
        mDatabaseHandler = new DatabaseHandler(getActivity());
        try {
            mDatabaseHandler.open();
            Calendar alarm = mDatabaseHandler.getAlarmByAssignmentId(mAssignment.getId());
            if(alarm != null) {
                mAlarmId = mDatabaseHandler.getRowIdByAssignmentId(mAssignment.getId());

                mAlarmDetails.setVisibility(View.VISIBLE);
                mAlarmDetails.setText(DateHelper.getShortDateTimeStringUniversal(getContext(), alarm.getTime()));
                //set the listener to null so we don't trigger the onCheckChangedListener when we set the value
                mAlarmSwitch.setOnCheckedChangeListener(null);
                mAlarmSwitch.setChecked(true);
                mAlarmSwitch.setOnCheckedChangeListener(mCheckedChangeListener);
            } else {
                mAlarmSwitch.setChecked(false);
                mAlarmSwitch.setOnCheckedChangeListener(mCheckedChangeListener);
            }
            mDatabaseHandler.close();
        } catch (SQLException e) {
            //couldn't find the alarm in the database, so don't show that there is an alarm
            mAlarmSwitch.setChecked(false);
            mAlarmSwitch.setOnCheckedChangeListener(mCheckedChangeListener);
        }
    }

    private void cancelAlarm() {
        //cancel the alarm
        AlarmReceiver alarmReceiver = new AlarmReceiver();
        String subTitle = "";
        if(mAssignment.getDueAt() != null) {
            subTitle = getContext().getResources().getString(R.string.due) + " " + DateHelper.getDateTimeString(getContext(), mAssignment.getDueAt());
        }
        alarmReceiver.cancelAlarm(getContext(), mAssignment.getId(), mAssignment.getName(), subTitle);

        //remove it from the database
        if(mDatabaseHandler == null) {
            mDatabaseHandler = new DatabaseHandler(getActivity());
        }
        try {
            mDatabaseHandler.open();
            int id = mDatabaseHandler.getRowIdByAssignmentId(mAssignment.getId());
            int result = mDatabaseHandler.deleteAlarm(id);
            mDatabaseHandler.close();
        } catch (SQLException e) {
            //couldn't delete the alarm, so it will remain in the database. But the actual
            //alarm should have been canceled above.
        }
    }

    private void setupViews(View rootView) {
        mAssignmentTitle = (TextView) rootView.findViewById(R.id.assignmentName);
        mCourseName = (TextView) rootView.findViewById(R.id.courseName);
        mStatus = (TextView) rootView.findViewById(R.id.status);
        mGrade = (TextView) rootView.findViewById(R.id.grade);
        mDueDate = (TextView) rootView.findViewById(R.id.dueDate);
        mAlarmDetails = (TextView) rootView.findViewById(R.id.alarmDetails);
        mAssignmentWebView = (CanvasWebView) rootView.findViewById(R.id.assignmentWebView);

        mAssignmentTitle.setText(mAssignment.getName());
        if(mAssignment.getDueAt() != null) {
            mDueDate.setText(getString(R.string.due) + " " + DateHelper.getDateTimeString(getActivity(), mAssignment.getDueAt()));
        }

        mCourseName.setText(mName);

        mAlarmSwitch = (SwitchCompat) rootView.findViewById(R.id.alarmSwitch);
        mAlarmSwitch.setOnCheckedChangeListener(mCheckedChangeListener);

        mAssignmentWebView.setClient(new ActivityContentVideoViewClient(getActivity()));


        mAssignmentWebView.setCanvasEmbeddedWebViewCallback(new CanvasWebView.CanvasEmbeddedWebViewCallback() {
            @Override
            public void launchInternalWebViewFragment(String url) {
                //create and add the InternalWebviewFragment to deal with the link they clicked
                InternalWebviewFragment internalWebviewFragment = new InternalWebviewFragment();
                internalWebviewFragment.setArguments(InternalWebviewFragment.createBundle(url, "", null, mStudent));

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_from_bottom, android.R.anim.fade_out, R.anim.none, R.anim.slide_to_bottom);
                ft.add(R.id.fullscreen, internalWebviewFragment, internalWebviewFragment.getClass().getName());
                ft.addToBackStack(internalWebviewFragment.getClass().getName());
                ft.commitAllowingStateLoss();
            }

            @Override
            public boolean shouldLaunchInternalWebViewFragment(String url) {
                return true;
            }
        });

        mAssignmentWebView.setCanvasWebViewClientCallback(new CanvasWebView.CanvasWebViewClientCallback() {
            @Override
            public void openMediaFromWebView(String mime, String url, String filename) {

            }

            @Override
            public void onPageStartedCallback(WebView webView, String url) {

            }

            @Override
            public void onPageFinishedCallback(WebView webView, String url) {

            }

            @Override
            public boolean canRouteInternallyDelegate(String url) {
                Uri uri = Uri.parse(studentDomainReferrer());
                return RouterUtils.canRouteInternally(null, url, mStudent, uri.getHost(), false);
            }

            @Override
            public void routeInternallyCallback(String url) {
                Uri uri = Uri.parse(studentDomainReferrer());
                RouterUtils.canRouteInternally(getActivity(), url, mStudent, uri.getHost(), true);
            }

            @Override
            public String studentDomainReferrer() {
                return mStudent.getStudentDomain();
            }
        });

        //assignment description can be null
        String description;
        if (mAssignment.isLocked()) {
            description = getLockedInfoHTML(mAssignment.getLockInfo(), getActivity(), R.string.locked_assignment_desc, R.string.locked_assignment_desc_line2);
        } else if(mAssignment.getLockAt() != null && mAssignment.getLockAt().before(Calendar.getInstance(Locale.getDefault()).getTime())) {
            //if an assignment has an available from and until field and it has expired (the current date is after "until" it will have a lock explanation,
            //but no lock info because it isn't locked as part of a module
            description = mAssignment.getLockExplanation();
        } else {
            description = mAssignment.getDescription();
        }

        if (description == null || description.equals("null") || description.equals("")) {
            description = getString(R.string.no_description);
        }

        mAssignmentWebView.formatHTML(description, mAssignment.getName());

    }

    public String getLockedInfoHTML(LockInfo lockInfo, Context context, int explanationFirstLine, int explanationSecondLine) {
        /*
            Note: if the html that this is going in isn't based on html_wrapper.html (it will have something
            like -- String html = CanvasAPI.getAssetsFile(getSherlockActivity(), "html_wrapper.html");) this will
            not look as good. The blue button will just be a link.
         */
        //get the locked message and make the module name bold
        String lockedMessage = "";

        if(lockInfo.getLockedModuleName() != null) {
            lockedMessage = "<p>" + String.format(context.getString(explanationFirstLine), "<b>" + lockInfo.getLockedModuleName() + "</b>") + "</p>";
        }
        if(lockInfo.getModulePrerequisiteNames().size() > 0) {
            //we only want to add this text if there are module completion requirements
            lockedMessage += context.getString(R.string.mustComplete) + "<ul>";
            for(int i = 0; i < lockInfo.getModulePrerequisiteNames().size(); i++) {
                lockedMessage +=  "<li>" + lockInfo.getModulePrerequisiteNames().get(i) + "</li>";  //"&#8226; "
            }
            lockedMessage += "</ul>";
        }

        //check to see if there is an unlocked date
        if(lockInfo.getUnlockAt() != null && lockInfo.getUnlockAt().after(new Date())) {
            String unlocked = DateHelper.getDateTimeString(context, lockInfo.getUnlockAt());
            //If there is an unlock date but no module then the assignment is locked
            if(lockInfo.getContextModule() == null){
                lockedMessage = "<p>" + context.getString(R.string.locked_assignment_not_module) + "</p>";
            }
            lockedMessage += context.getString(R.string.unlockedAt) + "<ul><li>" + unlocked + "</li></ul>";
        }

        return lockedMessage;
    }

    private void setupListeners() {
        mCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked) {
                    mAlarmDetails.setVisibility(View.VISIBLE);
                    mSetDate = Calendar.getInstance();
                    Calendar cal = Calendar.getInstance();
                    if (mAssignment.getDueAt() != null) {
                        cal.setTime(mAssignment.getDueAt());
                    }

                    mDatePickerDialog = DatePickerFragment.newInstance(AssignmentFragment.this, AssignmentFragment.this);

                    mDatePickerDialog.show(getActivity().getFragmentManager(), DATE_PICKER_TAG);

                } else {
                    mAlarmDetails.setVisibility(View.INVISIBLE);
                    mAlarmDetails.setText("");
                    cancelAlarm();
                }
            }
        };
    }

    @Override
    public void onCancel() {
        mAlarmSwitch.setChecked(false);
    }


    @Override
    public void onDateSet(int year, int month, int day) {
        mSetDate.set(Calendar.YEAR, year);
        mSetDate.set(Calendar.MONTH, month);
        mSetDate.set(Calendar.DAY_OF_MONTH, day);

        mDatePickerDialog.dismiss();
        mTimePicker = TimePickerFragment.newInstance(this, this);

        mTimePicker.setCancelable(false);

        mTimePicker.show(getActivity().getSupportFragmentManager(), TIME_PICKER_TAG);
    }


    @Override
    public void onTimeSet(int hourOfDay, int minute) {

        mSetDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mSetDate.set(Calendar.MINUTE, minute);

        mAlarmDetails.setText(DateHelper.getShortDateTimeStringUniversal(getContext(), mSetDate.getTime()));
        //save/update the alarm information
        try {
            mDatabaseHandler.open();

            String subTitle;
            if(mAssignment.getDueAt() != null) {
                subTitle = getContext().getResources().getString(R.string.due) + " " + DateHelper.getShortDateTimeStringUniversal(getContext(), mAssignment.getDueAt());
            } else {
                subTitle = getContext().getResources().getString(R.string.no_due_date);
            }

            mDatabaseHandler.createAlarm(mSetDate.get(Calendar.YEAR), mSetDate.get(Calendar.MONTH), mSetDate.get(Calendar.DAY_OF_MONTH), mSetDate.get(Calendar.HOUR_OF_DAY), mSetDate.get(Calendar.MINUTE), mAssignment.getId(), mAssignment.getName(), subTitle);

            mDatabaseHandler.close();

        } catch (SQLException e) {
            //couldn't save the alarm in the database, so stop here and don't actually create one. If the database
            //doesn't have the alarm in it, the user will think that it didn't save
            Toast.makeText(getContext(), getContext().getString(R.string.alarmNotSet), Toast.LENGTH_SHORT).show();
            mTimePicker.dismiss();
            return;
        }
        AlarmReceiver alarmReceiver = new AlarmReceiver();
        String subTitle;

        if(mAssignment.getDueAt() != null) {
            subTitle = getContext().getResources().getString(R.string.due) + " " + DateHelper.getShortDateTimeStringUniversal(getContext(), mAssignment.getDueAt());
        } else {
            subTitle = getContext().getResources().getString(R.string.no_due_date);
        }

        alarmReceiver.setAlarm(getContext(), mSetDate, mAssignment.getId(), mAssignment.getName(), subTitle);

        AnalyticUtils.trackButtonPressed(AnalyticUtils.REMINDER_ASSIGNMENT);

        mTimePicker.dismiss();
    }

    private void setupGradeAndStatus() {
        int assignmentState = AssignmentUtils2.getAssignmentState(mAssignment, mAssignment.getSubmission());

        switch(assignmentState) {
            case(AssignmentUtils2.ASSIGNMENT_STATE_MISSING):
                mGrade.setVisibility(View.GONE);
                mStatus.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.submission_missing_background));
                mStatus.setText(getContext().getString(R.string.missing));
                mGrade.setVisibility(View.VISIBLE);
                mGrade.setText(getContext().getString(R.string.grade) + " "
                        + ViewUtils.getPointsPossibleMissing(mAssignment.getPointsPossible()));
                break;
            case(AssignmentUtils2.ASSIGNMENT_STATE_GRADED):
                mGrade.setVisibility(View.VISIBLE);
                mGrade.setText(getContext().getString(R.string.grade) + " "
                        + ViewUtils.getPercentGradeForm(mAssignment.getSubmission().getScore(),
                        mAssignment.getPointsPossible()) + " "
                        + ViewUtils.getPointsGradeForm(mAssignment.getSubmission().getScore(),
                        mAssignment.getPointsPossible()));
                mStatus.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.assignment_submitted_background));
                mStatus.setText(getContext().getString(R.string.submitted));
                break;
            case(AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED):
                mGrade.setVisibility(View.VISIBLE);
                mGrade.setText(getContext().getString(R.string.grade) + " " + getContext().getString(R.string.notGraded));
                mStatus.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.assignment_submitted_background));
                mStatus.setText(getContext().getString(R.string.submitted));
                break;
            case(AssignmentUtils2.ASSIGNMENT_STATE_GRADED_LATE):
                mGrade.setVisibility(View.VISIBLE);
                mGrade.setText(getContext().getString(R.string.grade) + " "
                        + ViewUtils.getPercentGradeForm(mAssignment.getSubmission().getScore(),
                        mAssignment.getPointsPossible()) + " "
                        + ViewUtils.getPointsGradeForm(mAssignment.getSubmission().getScore(),
                        mAssignment.getPointsPossible()));
                mStatus.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.late_assignment_background));
                mStatus.setText(getContext().getString(R.string.late));
                break;
            case(AssignmentUtils2.ASSIGNMENT_STATE_SUBMITTED_LATE):
                mGrade.setVisibility(View.VISIBLE);
                mGrade.setText(getContext().getString(R.string.grade) + " " + getContext().getString(R.string.notGraded));
                mStatus.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.late_assignment_background));
                mStatus.setText(getContext().getString(R.string.late));
                break;
            case(AssignmentUtils2.ASSIGNMENT_STATE_EXCUSED):
                mGrade.setVisibility(View.GONE);
                mStatus.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.assignment_submitted_background));
                mStatus.setText(getContext().getString(R.string.excused));
                break;
            case(AssignmentUtils2.ASSIGNMENT_STATE_DUE):
                mGrade.setVisibility(View.GONE);
                mStatus.setVisibility(View.GONE);
                break;
            case(AssignmentUtils2.ASSIGNMENT_STATE_IN_CLASS):
                mGrade.setVisibility(View.VISIBLE);
                mGrade.setText(getContext().getString(R.string.grade) + " " + getContext().getString(R.string.notGraded));
                mStatus.setBackgroundDrawable(getContext().getResources().getDrawable(R.drawable.assignment_in_class_background));
                mStatus.setText(getContext().getString(R.string.in_class));
                break;
        }
    }
}

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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.instructure.candroid.R;
import com.instructure.candroid.adapter.FileUploadAssignmentsAdapter;
import com.instructure.candroid.adapter.FileUploadCoursesAdapter;
import com.instructure.candroid.util.AnimationHelpers;
import com.instructure.candroid.util.UploadCheckboxManager;
import com.instructure.canvasapi.api.AssignmentAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.Const;

import java.util.ArrayList;
import java.util.Arrays;

import retrofit.client.Response;

public class ShareFileDestinationDialog extends DialogFragment implements UploadCheckboxManager.OnOptionCheckedListener{

    public static final String TAG = "uploadFileSourceFragment";

    // Dismiss interface
    public interface DialogCloseListener{
        public void onDismiss(DialogInterface dialog);
        public void onCancel(DialogInterface dialog);
        public void onNext(Bundle bundle);
    }

    private View rootView;

    // Custom dialog header
    private ImageView avatar;
    private TextView userName;
    private TextView title;

    private UploadCheckboxManager checkboxManager;
    private View contentView;

    private Spinner studentCoursesSpinner;
    private Spinner assignmentSpinner;
    private Spinner teacherCoursesSpinner;

    private Uri uri;
    private boolean isTeacher;
    private boolean isStudent;
    private ArrayList<Course> courses = new ArrayList<>();

    private User user;
    private CanvasCallback<Assignment[]> canvasCallbackAssignments;
    private Assignment selectedAssignment;

    private FileUploadCoursesAdapter teacherEnrollmentsAdapter;
    private FileUploadCoursesAdapter studentEnrollmentsAdapter;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////

    public static ShareFileDestinationDialog newInstance(Bundle bundle) {
        ShareFileDestinationDialog uploadFileSourceFragment = new ShareFileDestinationDialog();
        uploadFileSourceFragment.setArguments(bundle);
        return uploadFileSourceFragment;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////
    @Override public void onStart() {
        super.onStart();
        // Don't dim the background when the dialog is created.
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0f;
        windowParams.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(windowParams);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupCallbacks();

        if(getDialog() != null){
            getDialog().getWindow().getAttributes().windowAnimations = R.style.FileDestinationDialogAnimation;
            getDialog().getWindow().setWindowAnimations(R.style.FileDestinationDialogAnimation);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        loadBundleData();

        MaterialDialog.Builder builder= new MaterialDialog.Builder(getActivity());
        builder .positiveText(getString(R.string.next))
                .negativeText(getActivity().getString(R.string.cancel))
                .backgroundColorRes(R.color.white)
                .positiveColorRes(R.color.courseGreen)
                .negativeColorRes(R.color.canvasTextMedium)
                .cancelable(true)
                .autoDismiss(false)
                .customView(initViews(), false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        validateAndShowNext();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        onCancel(dialog);
                        dismiss();
                    }
                });

        return builder.build();
    }

    public void onDismiss(DialogInterface dialog){
        Activity activity = getActivity();
        if(activity instanceof DialogCloseListener){
            ((DialogCloseListener)activity).onCancel(dialog);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Activity activity = getActivity();
        if(activity instanceof DialogCloseListener){
            ((DialogCloseListener)activity).onCancel(dialog);
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance())
            getDialog().dismiss();
        super.onDestroyView();
    }

    private void validateAndShowNext(){
        // validate selections
        String errorString = validateForm();
        if(errorString.length() > 0){
            Toast.makeText(getActivity(), errorString, Toast.LENGTH_SHORT).show();
        }
        else if(getActivity() instanceof DialogCloseListener){
            ((DialogCloseListener) getActivity()).onNext(getUploadBundle());
            dismiss();
        }
    }

    /**
     * Checks if user has filled out form completely.
     * @return Returns an error string if the form is not valid.
     */
    private String validateForm(){
        //make sure the user has selected a course and an assignment
        FileUploadDialog.FileUploadType uploadType = checkboxManager.getSelectedType();

        // Make sure a course was selected
        if (uploadType == FileUploadDialog.FileUploadType.COURSE
                    && (teacherCoursesSpinner.getSelectedItem() == null || ((Course)teacherCoursesSpinner.getSelectedItem()).getId() == Long.MIN_VALUE)){

            return getString(R.string.noCourseSelected);
        }
        // Make sure an assignment & course was selected if FileUploadType.Assignment
        else if (uploadType == FileUploadDialog.FileUploadType.ASSIGNMENT){
            if(studentCoursesSpinner.getSelectedItem() == null){
                return  getString(R.string.noCourseSelected);
            }
            else if(assignmentSpinner.getSelectedItem() == null || ((Assignment)assignmentSpinner.getSelectedItem()).getId() == Long.MIN_VALUE){
                return getString(R.string.noAssignmentSelected);
            }
        }

        return "";
    }

    private Bundle getUploadBundle(){
        Bundle bundle;
        switch (checkboxManager.getSelectedCheckBox().getId()){
            case R.id.myFilesCheckBox:
                bundle = FileUploadDialog.createFilesBundle(uri, null);
                break;
            case R.id.courseFilesCheckBox:
                bundle = FileUploadDialog.createCourseBundle(uri, (Course)teacherCoursesSpinner.getSelectedItem(), null);
                break;
            case R.id.assignmentCheckBox:
                bundle = FileUploadDialog.createAssignmentBundle(uri, (Course)studentCoursesSpinner.getSelectedItem(), (Assignment)assignmentSpinner.getSelectedItem());
                break;
            default:
                bundle = FileUploadDialog.createFilesBundle(uri, null);
                break;
        }
        return bundle;
    }

    private View initViews(){
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.upload_file_destination, null);

        contentView = rootView.findViewById(R.id.dialogContents);

        studentCoursesSpinner = (Spinner) rootView.findViewById(R.id.studentCourseSpinner);
        teacherCoursesSpinner = (Spinner) rootView.findViewById(R.id.teacherCourseSpinner);
        assignmentSpinner = (Spinner) rootView.findViewById(R.id.assignmentSpinner);

        // animated header views
        title = (TextView) rootView.findViewById(R.id.dialogTitle);
        avatar = (ImageView) rootView.findViewById(R.id.avatar);
        userName = (TextView) rootView.findViewById(R.id.userName);
        userName.setText(user.getName());

        initCheckBoxes(rootView);
        setRevealContentsListener();

        if(isStudent){
            rootView.findViewById(R.id.assignmentContainer).setVisibility(View.VISIBLE);
        }
        if(isTeacher){
            rootView.findViewById(R.id.courseFilesContainer).setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    private void initCheckBoxes(View view){
        CheckedTextView assignmentCheckBox = (CheckedTextView) view.findViewById(R.id.assignmentCheckBox);
        CheckedTextView courseFilesCheckBox = (CheckedTextView) view.findViewById(R.id.courseFilesCheckBox);
        CheckedTextView myFilesCheckBox = (CheckedTextView) view.findViewById(R.id.myFilesCheckBox);
        CardView selectionIndicator = (CardView) view.findViewById(R.id.selectionIndicator);

        checkboxManager = new UploadCheckboxManager(this, selectionIndicator);
        checkboxManager.add(myFilesCheckBox);
        checkboxManager.add(assignmentCheckBox);
        checkboxManager.add(courseFilesCheckBox);
    }

    private void setAssignmentsSpinnerToLoading(){
        Assignment loading = new Assignment();
        ArrayList<Assignment> courseAssignments = new ArrayList<>();
        loading.setName(getString(R.string.loadingAssignments));
        loading.setId(Long.MIN_VALUE);
        courseAssignments.add(loading);
        assignmentSpinner.setAdapter(new FileUploadAssignmentsAdapter(getActivity(), courseAssignments));
    }

    private void setupCallbacks(){
        canvasCallbackAssignments = new CanvasCallback<Assignment[]>(APIHelpers.statusDelegateWithContext(getActivity())) {
            @Override
            public void firstPage(Assignment[] assignments, LinkHeaders linkHeaders, Response response) {
                if(!isAdded()){
                    return;
                }

                if(assignments.length > 0 && courseSelectionChanged(assignments[0].getCourseId())){
                    return;
                }

                ArrayList<Assignment> courseAssignments = FileUploadAssignmentsAdapter.getOnlineUploadAssignmentsList(getContext(),new ArrayList<>(Arrays.asList(assignments)));
                // init student spinner
                final FileUploadAssignmentsAdapter adapter = new FileUploadAssignmentsAdapter(getActivity(), courseAssignments);
                assignmentSpinner.setAdapter(adapter);
                if (selectedAssignment != null) {
                    assignmentSpinner.setOnItemSelectedListener(null); // prevent listener from firing the when selection is placed
                    int position = adapter.getPosition(selectedAssignment);
                    if (position >= 0) {
                        assignmentSpinner.setSelection(position, false); // prevents the network callback from replacing the what the user selected while cache was being displayed
                    }
                }
                assignmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position < 0) {
                            return;
                        }

                        if (position < adapter.getCount()) {
                            selectedAssignment = adapter.getItem(position);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        };
    }

    private void setupCourseSpinners() {
        // teacher spinner
        if(isTeacher){
            if(teacherEnrollmentsAdapter == null){
                teacherEnrollmentsAdapter = new FileUploadCoursesAdapter(getActivity(), getActivity().getLayoutInflater(), FileUploadCoursesAdapter.getFilteredCourseList(courses, FileUploadCoursesAdapter.Type.TEACHER));
                teacherCoursesSpinner.setAdapter(teacherEnrollmentsAdapter);
            }else{
                teacherEnrollmentsAdapter.setCourses(FileUploadCoursesAdapter.getFilteredCourseList(courses, FileUploadCoursesAdapter.Type.TEACHER));
            }
        }

        // student spinners
        if(isStudent){
            if(studentEnrollmentsAdapter == null){
                studentEnrollmentsAdapter = new FileUploadCoursesAdapter(getActivity(), getActivity().getLayoutInflater(), FileUploadCoursesAdapter.getFilteredCourseList(courses, FileUploadCoursesAdapter.Type.STUDENT));
                studentCoursesSpinner.setAdapter(studentEnrollmentsAdapter);
            }else{
                studentEnrollmentsAdapter.setCourses(FileUploadCoursesAdapter.getFilteredCourseList(courses, FileUploadCoursesAdapter.Type.STUDENT));
            }

            studentCoursesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //make the allowed extensions disappear
                    Course course = (Course) parent.getAdapter().getItem(position);
                    //if the user is a teacher, let them know and don't let them select an assignment
                    if(course.getId() > 0) {
                        setAssignmentsSpinnerToLoading();
                        AssignmentAPI.getAllAssignmentsExhaustive(course.getId(), canvasCallbackAssignments);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private boolean courseSelectionChanged(long newCourseId){
        if(checkboxManager.getSelectedCheckBox().getId() == R.id.assignmentCheckBox && studentCoursesSpinner != null && newCourseId != ((Course)studentCoursesSpinner.getSelectedItem()).getId()){
            // api call for assignments returning after user changes course; ignore results
            return true;
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // View Helpers
    ///////////////////////////////////////////////////////////////////////////
    private void setRevealContentsListener(){
        final Animation avatarAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.ease_in_shrink);
        final Animation titleAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.ease_in_bottom);

        avatar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                AnimationHelpers.removeGlobalLayoutListeners(avatar, this);
                ((MaterialDialog)getDialog()).getActionButton(DialogAction.POSITIVE).startAnimation(titleAnimation);
                ((MaterialDialog)getDialog()).getActionButton(DialogAction.NEGATIVE).startAnimation(titleAnimation);
                avatar.startAnimation(avatarAnimation);
                userName.startAnimation(titleAnimation);
                title.startAnimation(titleAnimation);
            }
        });

        contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                AnimationHelpers.removeGlobalLayoutListeners(contentView, this);

                final Animator revealAnimator = AnimationHelpers.createRevealAnimator(contentView);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        contentView.setVisibility(View.VISIBLE);
                        revealAnimator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                setupCourseSpinners();
                            }
                        });
                        revealAnimator.start();
                    }
                }, 600);
            }
        });
    }

    private void enableStudentSpinners(boolean isEnabled){
        assignmentSpinner.setEnabled(isEnabled);
        studentCoursesSpinner.setEnabled(isEnabled);
    }

    private void enableTeacherSpinners(boolean isEnabled){
        teacherCoursesSpinner.setEnabled(isEnabled);
    }
    ///////////////////////////////////////////////////////////////////////////
    // UploadCheckboxManager overrides
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onUserFilesSelected() {
        enableStudentSpinners(false);
        enableTeacherSpinners(false);
    }

    @Override
    public void onCourseFilesSelected() {
        enableStudentSpinners(false);
        enableTeacherSpinners(true);
    }

    @Override
    public void onAssignmentFilesSelected() {
        enableStudentSpinners(true);
        enableTeacherSpinners(false);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Data
    ///////////////////////////////////////////////////////////////////////////
    private void loadBundleData(){
        Bundle bundle = getArguments();
        courses   = bundle.getParcelableArrayList(Const.COURSES);
        isTeacher = bundle.getBoolean(Const.IS_TEACHER);
        isStudent = bundle.getBoolean(Const.IS_STUDENT);
        user      = APIHelpers.getCacheUser(getActivity());
        uri       = bundle.getParcelable(Const.URI);
    }

    public static Bundle createBundle(Uri uri, ArrayList<Course> courses, boolean isTeacher, boolean isStudent){
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.URI, uri);
        bundle.putBoolean(Const.IS_STUDENT, isStudent);
        bundle.putBoolean(Const.IS_TEACHER, isTeacher);
        bundle.putParcelableArrayList(Const.COURSES, courses);
        return bundle;
    }
}

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
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.instructure.candroid.R;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.util.LockInfoHTMLHelper;
import com.instructure.candroid.util.Param;
import com.instructure.candroid.util.RouterUtils;
import com.instructure.candroid.view.CanvasWebView;
import com.instructure.candroid.view.ViewUtils;
import com.instructure.canvasapi.api.AssignmentAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.AssignmentGroup;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.video.ActivityContentVideoViewClient;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import retrofit.client.Response;

public class AssignmentDetailsFragment extends ParentFragment {

    // views
    private CanvasWebView canvasWebView;
    private Course course;
    private View divider;
    private TextView notification;
    private View notificationContainer;
    private ImageButton notificationDismiss;

    private TextView assignmentTitleView;
    private TextView dueDateView;
    private TextView submissionDateView;

    private LinearLayout assignmentDetails; // Teacher view
    private TextView pointsPossible;
    private TextView assignmentGroup;
    private TextView gradingType;
    private TextView turnInTypeSelected;
    private LinearLayout onlineSubmissionTypeLayout;

    // model data
    private Assignment assignment; // keep assignment logic within populateAssignmentDetails method, otherwise assignment could be null

    private CanvasCallback<AssignmentGroup[]> canvasCallback;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.DETAIL; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.assignments);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Interface Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
    * For explanation of isWithinAnotherCallback and isCached refer to comment in {@link com.instructure.candroid.activity.CallbackActivity#getUserSelf}
    */
    public void setAssignment(Assignment assignment, boolean isWithinAnotherCallback, boolean isCached) {
        this.assignment = assignment;
        populateAssignmentDetails(assignment, isWithinAnotherCallback, isCached);
    }

    public void updateSubmissionDate(Date submissionDate) {
        String submitDate = getString(R.string.assignmentLastSubmission) + ": " + getString(R.string.assignmentNoSubmission);
        if (submissionDate != null) {
            submitDate = DateHelpers.createPrefixedDateTimeString(getContext(), R.string.assignmentLastSubmission, submissionDate);
        }
        submissionDateView.setText(submitDate);
    }

    @Override
    protected String getActionbarTitle() {
        return assignment != null ? assignment.getName() : null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (canvasWebView != null) {
            canvasWebView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (canvasWebView != null) {
            canvasWebView.onResume();
        }
    }

    @Override
    public boolean handleBackPressed() {
        if(canvasWebView != null) {
            return canvasWebView.handleGoBack();
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = getLayoutInflater().inflate(R.layout.assignment_details_fragment, container, false);
        initViews(rootView);
        setListeners();

        setCanvasCallback();
        return rootView;
    }

    private void initViews(View rootView) {
        assignmentDetails = (LinearLayout)rootView.findViewById(R.id.assignmentDetailsLayout);
        pointsPossible = (TextView) rootView.findViewById(R.id.pointsPossible);
        assignmentGroup = (TextView) rootView.findViewById(R.id.assignmentGroup);
        gradingType = (TextView) rootView.findViewById(R.id.gradingType);
        turnInTypeSelected = (TextView) rootView.findViewById(R.id.submissionTypeSelected);
        onlineSubmissionTypeLayout = (LinearLayout) rootView.findViewById(R.id.onlineSubmissionTypes);
        notification = (TextView)rootView.findViewById(R.id.notificationText);
        notificationContainer = rootView.findViewById(R.id.notificationTextContainer);
        notificationDismiss = (ImageButton)rootView.findViewById(R.id.notificationTextDismiss);
        divider = rootView.findViewById(R.id.divider);
        assignmentTitleView = (TextView) rootView.findViewById(R.id.textViewAssignmentTitle);
        dueDateView = (TextView) rootView.findViewById(R.id.textViewDueDate);
        submissionDateView = (TextView) rootView.findViewById(R.id.textViewSubmissionDate);
        canvasWebView = (CanvasWebView) rootView.findViewById(R.id.webview);
        canvasWebView.setClient(new ActivityContentVideoViewClient(getActivity()));
        // DO NOT use getSettings().setLayoutAlgorithm, it messes up the layout on API 16 ish devices
    }

    private void setListeners() {
        notificationDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
                fadeOut.setFillAfter(true);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        notificationContainer.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                notificationContainer.startAnimation(fadeOut);
            }
        });

        canvasWebView.setCanvasWebViewClientCallback(new CanvasWebView.CanvasWebViewClientCallback() {
            @Override
            public void openMediaFromWebView(String mime, String url, String filename) {
                openMedia(mime, url, filename);
            }

            @Override
            public void onPageFinishedCallback(WebView webView, String url) {

            }

            @Override
            public void onPageStartedCallback(WebView webView, String url) {


            }

            @Override
            public boolean canRouteInternallyDelegate(String url) {
                return RouterUtils.canRouteInternally(null, url, APIHelpers.getDomain(getActivity()), false);
            }

            @Override
            public void routeInternallyCallback(String url) {
                RouterUtils.canRouteInternally(getActivity(), url, APIHelpers.getDomain(getActivity()), true);
            }
        });

        canvasWebView.setCanvasEmbeddedWebViewCallback(new CanvasWebView.CanvasEmbeddedWebViewCallback() {
            @Override
            public void launchInternalWebViewFragment(String url) {
                InternalWebviewFragment.loadInternalWebView(getActivity(), (Navigation) getActivity(), InternalWebviewFragment.createBundle(getCanvasContext(), url, false));
            }

            @Override
            public boolean shouldLaunchInternalWebViewFragment(String url) {
                return true;
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    // View
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public HashMap<String, String> getParamForBookmark() {
        if(assignment == null) {
            return null;
        }
        HashMap<String, String> map = getCanvasContextParams();
        map.put(Param.ASSIGNMENT_ID, Long.toString(assignment.getId()));
        return map;
    }

    private void populateTeacherAssignmentDetails(AssignmentGroup[] assignmentGroups) {
        if(assignment == null) {
            return;
        }
        for (AssignmentGroup group : assignmentGroups) {
            if (group.getId() == assignment.getAssignmentGroupId()) {
                assignmentGroup.setText(group.getName());
            }

        }
    }

    /**
     * Updates each view with its corresponding assignment data.
     * @param assignment
     *
     * For explanation of isWithinAnotherCallback and isCached refer to comment in {@link com.instructure.candroid.activity.CallbackActivity#getUserSelf}
     */
    private void populateAssignmentDetails(Assignment assignment, boolean isWithinAnotherCallback, boolean isCached) {
        //Make sure we have all of the data.
        if(assignment == null) {
            return;
        }
        // anything that relies on intent data belongs here
        if(course != null && course.isTeacher()) {
            if (isWithinAnotherCallback) {
                AssignmentAPI.getAssignmentGroupsListChained(course.getId(), canvasCallback, isCached);
            } else {
                AssignmentAPI.getAssignmentGroupsList(course.getId(), canvasCallback);
            }
            assignmentDetails.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
        }

        assignmentTitleView.setText(assignment.getName());

        // Due Date
        if (assignment.getDueDate() != null) {
            String dueDate = DateHelpers.createPrefixedDateTimeString(getContext(), R.string.assignmentDue, assignment.getDueDate());
            dueDateView.setVisibility(View.VISIBLE);
            dueDateView.setTypeface(null, Typeface.ITALIC);
            dueDateView.setText(dueDate);

        } else {
            dueDateView.setVisibility(View.GONE);
        }

        // Submission Type
        if(assignment.getSubmissionTypes().contains(Assignment.SUBMISSION_TYPE.NONE)){
            submissionDateView.setVisibility(View.INVISIBLE);
        } else {
            submissionDateView.setVisibility(View.VISIBLE);
            if (assignment.getLastSubmission() != null) {
                updateSubmissionDate(assignment.getLastSubmission().getSubmitDate());
            }
        }
        pointsPossible.setText("" + assignment.getPointsPossible());

        populateWebView(assignment);

        //This check is to prevent the context from becoming null when assignment items are
        //clicked rapidly in the notification list.
        if(getContext() != null){
            if (assignment.getGradingType() != null) {
                gradingType.setText(Assignment.gradingTypeToPrettyPrintString(assignment.getGradingType(), getContext()));
            } else {
                gradingType.setVisibility(View.INVISIBLE);
            }

            Assignment.TURN_IN_TYPE assignmentTurnInType = assignment.getTurnInType();

            if(assignmentTurnInType != null){
                turnInTypeSelected.setText(Assignment.turnInTypeToPrettyPrintString(assignmentTurnInType, getContext()));
            }


            //Make sure there are no children views
            onlineSubmissionTypeLayout.removeAllViews();

            if (assignmentTurnInType == Assignment.TURN_IN_TYPE.ONLINE) {
                for (Assignment.SUBMISSION_TYPE submissionType : assignment.getSubmissionTypes()) {
                    TextView submissionTypeTextView = new TextView(getContext());
                    submissionTypeTextView.setPadding(0, (int)ViewUtils.convertDipsToPixels(5f, getContext()), 0, 0);

                    submissionTypeTextView.setText(Assignment.submissionTypeToPrettyPrintString(submissionType, getContext()));

                    onlineSubmissionTypeLayout.addView(submissionTypeTextView);
                }
            }
        }
    }

    private void populateWebView(Assignment assignment) {
        String description;
        if (assignment.isLocked()) {
            description = LockInfoHTMLHelper.getLockedInfoHTML(assignment.getLockInfo(), getActivity(), R.string.lockedAssignmentDesc, R.string.lockedAssignmentDescLine2);
        } else if(assignment.getlockAtDate() != null && assignment.getlockAtDate().before(Calendar.getInstance(Locale.getDefault()).getTime())) {
            //if an assignment has an available from and until field and it has expired (the current date is after "until" it will have a lock explanation,
            //but no lock info because it isn't locked as part of a module
            description = assignment.getLock_explanation();
        } else {
            description = assignment.getDescription();
        }

        if (description == null || description.equals("null") || description.equals("")) {
            description = getString(R.string.noDescription);
        }
        canvasWebView.formatHTML(description, assignment.getName());
    }


    public static int getTabTitle() {
        return R.string.assignmentTabDetails;
    }

    /**
     * For explanation of isWithinAnotherCallback and isCached refer to comment in {@link com.instructure.candroid.activity.CallbackActivity#getUserSelf}
     */
    public void setAssignmentWithNotification(Assignment assignment, String message, boolean isWithinAnotherCallback, boolean isCached) {
        if (assignment == null) {
            return;
        }

        populateAssignmentDetails(assignment, isWithinAnotherCallback, isCached);

        if(message != null) {
            message = message.trim();
        }

        if(!TextUtils.isEmpty(message)) {

            // get rid of "________________________________________ You received this email..." text
            int index = message.indexOf("________________________________________");
            if (index > 0) {
                message = message.substring(0, index);
            }

            if(notification != null) {
                notification.setText(message.trim());
                notification.setMovementMethod(LinkMovementMethod.getInstance());
                notification.setVisibility(View.VISIBLE);
                notificationContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Setup CanvasCallback
    ///////////////////////////////////////////////////////////////////////////

    public void setCanvasCallback() {
        canvasCallback = new CanvasCallback<AssignmentGroup[]>(this) {
            @Override
            public void firstPage(AssignmentGroup[] assignmentGroups, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()){
                    return;
                }

                //[] of fresh data.
                if (course.isTeacher()) {
                    populateTeacherAssignmentDetails(assignmentGroups);
                }
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
        course = (Course) getCanvasContext();
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}

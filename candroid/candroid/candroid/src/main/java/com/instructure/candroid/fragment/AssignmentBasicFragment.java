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
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.instructure.candroid.R;
import com.instructure.candroid.util.RouterUtils;
import com.instructure.candroid.view.CanvasWebView;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.LockInfo;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.pandautils.utils.Const;
import com.video.ActivityContentVideoViewClient;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AssignmentBasicFragment extends ParentFragment {

    private Assignment mAssignment;
    private TextView mDueDate;
    private RelativeLayout mDueDateWrapper;
    private CanvasWebView mAssignmentWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = getLayoutInflater().inflate(R.layout.fragment_assignment_basic, container, false);
        setupViews(rootView);

        return rootView;
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
    public void onFragmentActionbarSetupComplete(FRAGMENT_PLACEMENT placement) {
        super.onFragmentActionbarSetupComplete(placement);
        if(getSupportActionBar() != null && mAssignment != null && mAssignment.getName() != null) {
            getSupportActionBar().setTitle(mAssignment.getName());
        }
    }

    private void setupViews(View rootView) {

        mDueDate = (TextView) rootView.findViewById(R.id.dueDate);
        mAssignmentWebView = (CanvasWebView) rootView.findViewById(R.id.assignmentWebView);
        mDueDateWrapper = (RelativeLayout) rootView.findViewById(R.id.dueDateWrapper);

        if(mAssignment.getDueDate() != null) {
            mDueDate.setText(getString(R.string.dueAt) + " " + DateHelpers.getDateTimeString(getActivity(), mAssignment.getDueDate()));
        } else {
            mDueDateWrapper.setVisibility(View.GONE);
        }

        mAssignmentWebView.setClient(new ActivityContentVideoViewClient(getActivity()));


        mAssignmentWebView.setCanvasEmbeddedWebViewCallback(new CanvasWebView.CanvasEmbeddedWebViewCallback() {
            @Override
            public void launchInternalWebViewFragment(String url) {
                //create and add the InternalWebviewFragment to deal with the link they clicked
                InternalWebviewFragment internalWebviewFragment = new InternalWebviewFragment();
                internalWebviewFragment.setArguments(InternalWebviewFragment.createBundle(url, "", false, ""));

                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_from_bottom, android.R.anim.fade_out, R.anim.none, R.anim.slide_out_to_bottom);
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
                return RouterUtils.canRouteInternally(null, url, APIHelpers.getDomain(getActivity()), false);
            }

            @Override
            public void routeInternallyCallback(String url) {
                RouterUtils.canRouteInternally(getActivity(), url, APIHelpers.getDomain(getActivity()), true);
            }

        });

        //assignment description can be null
        String description;
        if (mAssignment.isLocked()) {
            description = getLockedInfoHTML(mAssignment.getLockInfo(), getActivity(), R.string.lockedAssignmentDesc, R.string.lockedAssignmentDescLine2);
        } else if(mAssignment.getlockAtDate() != null && mAssignment.getlockAtDate().before(Calendar.getInstance(Locale.getDefault()).getTime())) {
            //if an assignment has an available from and until field and it has expired (the current date is after "until" it will have a lock explanation,
            //but no lock info because it isn't locked as part of a module
            description = mAssignment.getLock_explanation();
        } else {
            description = mAssignment.getDescription();
        }

        if (description == null || description.equals("null") || description.equals("")) {
            description = getString(R.string.noDescription);
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
        if(lockInfo.getUnlockedAt() != null && lockInfo.getUnlockedAt().after(new Date())) {
            String unlocked = DateHelpers.getDateTimeString(context, lockInfo.getUnlockedAt());
            //If there is an unlock date but no module then the assignment is locked
            if(lockInfo.getContext_module() == null){
                lockedMessage = "<p>" + context.getString(R.string.lockedAssignmentNotModule) + "</p>";
            }
            lockedMessage += context.getString(R.string.unlockedAt) + "<ul><li>" + unlocked + "</li></ul>";
        }

        return lockedMessage;
    }

    @Override
    public String getFragmentTitle() {
        if(mAssignment != null) {
            return mAssignment.getName();
        }
        return null;
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.DETAIL;
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }

    // region Intent
    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
        if(extras == null){return;}
        mAssignment = extras.getParcelable(Const.ASSIGNMENT);
    }

    public static Bundle createBundle(CanvasContext canvasContext, Assignment assignment) {
        Bundle bundle = createBundle(canvasContext);
        bundle.putParcelable(Const.ASSIGNMENT, assignment);
        return bundle;
    }
    // endregion

}

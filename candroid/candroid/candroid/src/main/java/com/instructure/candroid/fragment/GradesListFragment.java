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
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import com.instructure.candroid.adapter.TermSpinnerAdapter;
import com.instructure.canvasapi.model.GradingPeriod;
import com.instructure.canvasapi.model.GradingPeriodResponse;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.instructure.candroid.R;
import com.instructure.candroid.adapter.GradesListRecyclerAdapter;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.dialog.WhatIfDialogStyled;
import com.instructure.candroid.interfaces.AdapterToFragmentCallback;
import com.instructure.candroid.util.Param;
import com.instructure.canvasapi.model.Tab;
import com.instructure.candroid.util.FragUtils;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.AssignmentGroup;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Submission;
import com.instructure.pandautils.utils.CanvasContextColor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import retrofit.client.Response;

public class GradesListFragment extends ParentFragment {

    // view
    private View mRootView;
    private TextView totalGradeView;
    private ImageView lockedGradeImage;
    private View mShadow;
    private AppCompatSpinner mTermSpinner;
    private TermSpinnerAdapter mTermAdapter;
    private ArrayList<GradingPeriod> mGradingPeriodsList = new ArrayList<>();

    private LinearLayout toggleGradeView;
    private CheckBox showTotalGradeCB;
    private LinearLayout toggleWhatIfScores;
    private CheckBox showWhatIfCheckbox;

    private Course mCourse;
    private GradingPeriod mAllTermsGradingPeriod;
    private GradesListRecyclerAdapter mRecyclerAdapter;

    // callbacks
    private WhatIfDialogStyled.WhatIfDialogCallback dialogStyled;
    private AdapterToFragmentCallback<Assignment> mAdapterToFragmentCallback;
    private GradesListRecyclerAdapter.AdapterToGradesCallback mAdapterToGradesCallback;
    private CanvasCallback<GradingPeriodResponse> mGradingPeriodsCallback;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.grades);
    }

    @Override
    protected String getSelectedParamName() {
        return Param.ASSIGNMENT_ID;
    }

    public String getTabId() {
        return Tab.GRADES_ID;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAllTermsGradingPeriod = new GradingPeriod();
        mAllTermsGradingPeriod.setTitle(getString(R.string.allGradingPeriods));
        setRetainInstance(this, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = getLayoutInflater().inflate(R.layout.course_grades, container, false);

        setUpCallbacks();
        configureViews(mRootView);

        mRecyclerAdapter = new GradesListRecyclerAdapter(getContext(), mCourse,
                mAdapterToFragmentCallback, mAdapterToGradesCallback, mGradingPeriodsCallback, dialogStyled);
        configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.gradesEmptyPandaView, R.id.listView);

        return mRootView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.gradesEmptyPandaView, R.id.listView);
    }

    public void configureViews(View rootView) {

        //Course will be null here in the case of memory pressure.
        //Not handled automatically as we cast from canvasContext;
        if(mCourse == null){
            return;
        }

        mTermSpinner = (AppCompatSpinner) rootView.findViewById(R.id.termSpinner);
        AppBarLayout appBarLayout = (AppBarLayout) rootView.findViewById(R.id.appbar);
        totalGradeView = (TextView)rootView.findViewById(R.id.txtOverallGrade);
        showTotalGradeCB = (CheckBox)rootView.findViewById(R.id.showTotalCheckBox);
        showWhatIfCheckbox = (CheckBox)rootView.findViewById(R.id.showWhatIfCheckBox);
        toggleGradeView = (LinearLayout)rootView.findViewById(R.id.grade_toggle_view);
        toggleWhatIfScores = (LinearLayout)rootView.findViewById(R.id.what_if_view);

        Drawable lockDrawable = CanvasContextColor.getColoredDrawable(getContext(),
                R.drawable.ic_cv_locked_fill, getResources().getColor(R.color.canvasTextDark));
        lockedGradeImage = (ImageView)rootView.findViewById(R.id.lockedGradeImage);
        lockedGradeImage.setImageDrawable(lockDrawable);

        View shadow = rootView.findViewById(R.id.shadow);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shadow.setVisibility(View.GONE);
        } else {
            shadow.setVisibility(View.VISIBLE);
        }

        setupListeners();
        lockGrade(mCourse.isFinalGradeHidden());

        dialogStyled = new WhatIfDialogStyled.WhatIfDialogCallback() {
            @Override
            public void onOkayClick(String whatIf, double total, Assignment assignment, int position) {

                //Create dummy submission for what if grade
                Submission s = new Submission();
                //check to see if grade is empty for reset
                if(TextUtils.isEmpty(whatIf)){
                    assignment.setLastSubmission(null);
                    mRecyclerAdapter.getAssignmentsHash().get(assignment.getId()).setLastSubmission(null);
                }else{
                    s.setScore(Double.parseDouble(whatIf));
                    s.setGrade(whatIf);
                    mRecyclerAdapter.getAssignmentsHash().get(assignment.getId()).setLastSubmission(s);
                }

                mRecyclerAdapter.notifyItemChanged(position);

                //Compute new overall grade
                new ComputeGradesTask(showTotalGradeCB.isChecked()).execute();
            }
        };

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                // workaround for Toolbar not showing with swipe to refresh
                if (i == 0) {
                    setRefreshingEnabled(true);
                } else {
                    setRefreshingEnabled(false);
                }
            }
        });
    }

    private void setupListeners() {
        toggleGradeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTotalGradeCB.toggle();
            }
        });

        showTotalGradeCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String grade = "";
                if (!isChecked) {
                    grade = formatGrade(mRecyclerAdapter.getFinalScore(), mRecyclerAdapter.getFinalGrade());
                    totalGradeView.setText(grade);
                } else if (showWhatIfCheckbox.isChecked()) {
                    new ComputeGradesTask(showTotalGradeCB.isChecked()).execute(mRecyclerAdapter.getAssignmentGroups());
                } else {
                    grade = formatGrade(mRecyclerAdapter.getCurrentScore(), mRecyclerAdapter.getCurrentGrade());
                    totalGradeView.setText(grade);
                }

                lockGrade(mCourse.isFinalGradeHidden());
            }
        });

        toggleWhatIfScores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWhatIfCheckbox.toggle();
            }
        });

        showWhatIfCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String currentGrade = mRecyclerAdapter.getCurrentScore() + "%";
                if (!showWhatIfCheckbox.isChecked()) {
                    totalGradeView.setText(currentGrade);
                } else if(mRecyclerAdapter.getWhatIfGrade() != null){
                    totalGradeView.setText(mRecyclerAdapter.getWhatIfGrade() + "%");
                }

                //If the user is turning off what if grades we need to do a full refresh, should be
                //cached data, so fast.
                if(!showWhatIfCheckbox.isChecked()) {
                    mRecyclerAdapter.refresh();
                } else {
                    mRecyclerAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void setUpCallbacks(){
        mAdapterToFragmentCallback = new AdapterToFragmentCallback<Assignment>() {
            @Override
            public void onRowClicked(Assignment assignment, int position, boolean isOpenDetail) {
                Bundle bundle = AssignmentFragment.createBundle(mCourse, assignment);
                Navigation nav = getNavigation();
                if(nav != null){
                    nav.addFragment(
                            FragUtils.getFrag(AssignmentFragment.class, bundle));
                }
            }

            @Override
            public void onRefreshFinished() {
                setRefreshing(false);
            }
        };
        mAdapterToGradesCallback = new GradesListRecyclerAdapter.AdapterToGradesCallback() {
            @Override
            public void setTermSpinnerState(boolean isEnabled) {
                if(!isAdded()){
                    return;
                }
                mTermSpinner.setEnabled(isEnabled);
                if(mTermAdapter != null){
                    if(isEnabled){
                        mTermAdapter.setIsLoading(false);
                    } else {
                        mTermAdapter.setIsLoading(true);
                    }
                    mTermAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void notifyGradeChanged(double score, String grade) {
                if(!isAdded()){
                    return;
                }
                if(grade != null && grade.equals(getString(R.string.noGradeText))) {
                    totalGradeView.setText(grade);
                } else {
                    totalGradeView.setText(formatGrade(score, grade));
                }
                lockGrade(mCourse.isFinalGradeHidden());
            }

            @Override
            public boolean getIsEdit() {
                return showWhatIfCheckbox.isChecked();
            }
        };


        /*
         *This code is similar to code in the AssignmentListFragment.
         *If you make changes here, make sure to check the same callback in the AssignmentListFrag.
         */
        mGradingPeriodsCallback = new CanvasCallback<GradingPeriodResponse>(this) {
            @Override
            public void firstPage(GradingPeriodResponse gradingPeriodResponse, LinkHeaders linkHeaders, Response response) {
                mGradingPeriodsList = new ArrayList<>();
                mGradingPeriodsList.addAll(gradingPeriodResponse.getGradingPeriodList());
                //add "select all" option
                mGradingPeriodsList.add(mAllTermsGradingPeriod);
                mTermAdapter = new TermSpinnerAdapter(getContext(), android.R.layout.simple_spinner_dropdown_item, mGradingPeriodsList);
                mTermSpinner.setAdapter(mTermAdapter);
                mTermSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        //The current item must always be set first
                        mRecyclerAdapter.setCurrentGradingPeriod(mTermAdapter.getItem(i));
                        if (mTermAdapter.getItem(i).getTitle().equals(getString(R.string.allGradingPeriods))) {
                            mRecyclerAdapter.loadAssignment();
                        } else {
                            mRecyclerAdapter.loadAssignmentsForGradingPeriod(mTermAdapter.getItem(i).getId(), true);
                            mTermSpinner.setEnabled(false);
                            mTermAdapter.setIsLoading(true);
                            mTermAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                    }
                });

                //If we have a "current" grading period select it
                if(mRecyclerAdapter.getCurrentGradingPeriod() != null) {
                    int position = mTermAdapter.getPositionForId(mRecyclerAdapter.getCurrentGradingPeriod().getId());
                    if (position != -1) {
                        mTermSpinner.setSelection(position);
                    } else {
                        Toast.makeText(getActivity(), com.instructure.loginapi.login.R.string.errorOccurred, Toast.LENGTH_SHORT).show();
                    }
                }
                mTermSpinner.setVisibility(View.VISIBLE);
            }

            @Override
            public void cache(GradingPeriodResponse gradingPeriodResponse, LinkHeaders linkHeaders, Response response) {
            }
        };
    }

    private String formatGrade(double score, String grade) {
        String formattedGrade = score + "%";

        if (grade != null && !grade.equals("null")) {
            formattedGrade += String.format(" (%s)", grade);
        }
        return formattedGrade;
    }


    private void lockGrade(boolean isLocked) {
        //If the final grade is hidden, we hide it and show the "lock"
        if (isLocked) {
            totalGradeView.setVisibility(View.INVISIBLE);
            lockedGradeImage.setVisibility(View.VISIBLE);
            toggleGradeView.setVisibility(View.GONE);
            toggleWhatIfScores.setVisibility(View.GONE);
        //If the final grade is not hidden, we must see if "all grading periods" is selected
        //If it is selected, we check to see if the overall grade view is locked
        } else if (mRecyclerAdapter != null && mRecyclerAdapter.isAllGradingPeriodsSelected()
                    && !mRecyclerAdapter.isAllPeriodsGradeShown()) {
        //The grade view is also hidden, so we lock it
            totalGradeView.setVisibility(View.INVISIBLE);
            lockedGradeImage.setVisibility(View.VISIBLE);
            toggleGradeView.setVisibility(View.GONE);
            toggleWhatIfScores.setVisibility(View.GONE);
        //Otherwise, we show the grade like normal
        } else {
            totalGradeView.setVisibility(View.VISIBLE);
            lockedGradeImage.setVisibility(View.INVISIBLE);
            toggleGradeView.setVisibility(View.VISIBLE);
            toggleWhatIfScores.setVisibility(View.VISIBLE);
        }

    }


    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
        mCourse = (Course)getCanvasContext();
    }

    ///////////////////////////////////////////////////////////////////////////
    // ASYNC
    ///////////////////////////////////////////////////////////////////////////


    /**
     * ComputeGradesTask calculates the current total grade based on what if scores and the state
     * of the showTotalGradeCheckbox.
     */
    private class ComputeGradesTask extends AsyncTask<ArrayList<AssignmentGroup>, Void, Double>{
        private boolean isShowTotalGrade;

        public ComputeGradesTask(boolean isShowTotalGrade) {
            this.isShowTotalGrade = isShowTotalGrade;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Double doInBackground(ArrayList<AssignmentGroup>... params) {
            //Calculates grade based on all assignments
            if(!isShowTotalGrade){
                if(mCourse.getApplyAssignmentGroupWeights()){
                    return calcGradesTotal(mRecyclerAdapter.getAssignmentGroups());
                }else{
                    return calcGradesTotalNoWeight(mRecyclerAdapter.getAssignmentGroups());
                }
            }else{ //Calculates grade based on only graded assignments
                if(mCourse.getApplyAssignmentGroupWeights()){
                    return calcGradesGraded(mRecyclerAdapter.getAssignmentGroups());
                }else{
                    return calcGradesGradedNoWeight(mRecyclerAdapter.getAssignmentGroups());
                }

            }
        }

        /**
         * This helper method is used to calculated a courses total grade
         * based on all assignments, this maps to the online check box in the UNCHECKED state:
         *
         * "Calculated based only on graded assignments"
         *
         * @param groups: A list of assignment groups for the course
         * @return: the grade as a rounded double, IE: 85.6
         */
        private double calcGradesTotal(ArrayList<AssignmentGroup> groups){
            double earnedScore = 0;

            for(AssignmentGroup g : groups){
                double earnedPoints = 0;
                double totalPoints = 0;
                double weight = g.getGroupWeight();
                for (Assignment a : g.getAssignments()){
                    Assignment tempAssignment = mRecyclerAdapter.getAssignmentsHash().get(a.getId());
                    Submission tempSub = tempAssignment.getLastSubmission();
                    if(tempSub != null && tempSub.getGrade() != null && !tempAssignment.getSubmissionTypes().contains(null)){
                        earnedPoints += tempSub.getScore();
                    }
                    totalPoints += tempAssignment.getPointsPossible();
                }

                if(totalPoints != 0 && earnedPoints != 0){
                    earnedScore += (earnedPoints / totalPoints) * (weight); //Cumulative
                }
            }

            return (round(earnedScore, 2));
        }

        /**
         * This helper method is used to calculated a courses total grade
         * based on all assignments, this maps to the online check box in the CHECKED state:
         *
         * "Calculated based only on graded assignments"
         *
         * @param groups: A list of assignment groups for the course
         * @return: the grade as a rounded double, IE: 85.6
         */
        private double calcGradesGraded(ArrayList<AssignmentGroup> groups){
            double totalWeight = 0;
            double earnedScore = 0;

            for(AssignmentGroup g : groups){
                double totalPoints = 0;
                double earnedPoints = 0;
                double weight = g.getGroupWeight();
                int assignCount = 0;
                boolean flag = true;
                for (Assignment a : g.getAssignments()){
                    Assignment tempAssignment = mRecyclerAdapter.getAssignmentsHash().get(a.getId());
                    Submission tempSub = tempAssignment.getLastSubmission();
                    if(tempSub != null && tempSub.getGrade() != null && !tempAssignment.getSubmissionTypes().contains(null)){
                        assignCount++; //determines if a group contains assignments
                        totalPoints += tempAssignment.getPointsPossible();
                        earnedPoints += tempSub.getScore();
                    }
                }

                if(totalPoints != 0){
                    earnedScore += (earnedPoints / totalPoints) * (weight);
                }

                    /*
                    In order to appropriately weight assignments when only some of the weight
                    categories contain graded assignments a totalWeight is created, based on the
                    weight of the missing categories.
                    */
                if(assignCount != 0 && flag){
                    totalWeight += weight;
                    flag = false;
                }
            }

            if (totalWeight < 100 && earnedScore != 0){ //Not sure if earnedScore !=0 needed
                earnedScore = (earnedScore/totalWeight) * 100;//Cumulative
            }

            return (round(earnedScore, 2));
        }

        /**
         * This helper method is used to calculated a courses total grade
         * based on all assignments, this maps to the online check box in the UNCHECKED state:
         *
         * "Calculated based only on graded assignments"
         *
         * AND
         *
         * When a course has the API object member "apply_assignment_group_weights" set to false.
         *
         * @param groups: A list of assignment groups for the mCourse
         * @return: the grade as a rounded double, IE: 85.6
         */
        private double calcGradesTotalNoWeight(ArrayList<AssignmentGroup> groups){
            double earnedScore = 0;
            double earnedPoints = 0;
            double totalPoints = 0;
            for(AssignmentGroup g : groups){
                for (Assignment a : g.getAssignments()){
                    Assignment tempAssignment = mRecyclerAdapter.getAssignmentsHash().get(a.getId());
                    Submission tempSub = tempAssignment.getLastSubmission();
                    if(tempSub != null && tempSub.getGrade() != null && !tempAssignment.getSubmissionTypes().contains(null)){
                        earnedPoints += tempSub.getScore();
                    }
                    totalPoints += tempAssignment.getPointsPossible();
                }
            }
            if(totalPoints != 0 && earnedPoints != 0){
                earnedScore += (earnedPoints / totalPoints) * 100; //Cumulative
            }

            return (round(earnedScore, 2));
        }

        /**
         * This helper method is used to calculated a courses total grade
         * based on all assignments, this maps to the online check box in the CHECKED state:
         *
         * "Calculated based only on graded assignments"
         *
         * AND
         *
         * When a course has the API object member "apply_assignment_group_weights" set to false.
         *
         * @param groups: A list of assignment groups for the mCourse
         * @return: the grade as a rounded double, IE: 85.6
         */
        private double calcGradesGradedNoWeight(ArrayList<AssignmentGroup> groups){
            double earnedScore = 0;
            double totalPoints = 0;
            double earnedPoints = 0;
            for(AssignmentGroup g : groups){
                for (Assignment a : g.getAssignments()){
                    Assignment tempAssignment = mRecyclerAdapter.getAssignmentsHash().get(a.getId());
                    Submission tempSub = tempAssignment.getLastSubmission();
                    if(tempSub != null && tempSub.getGrade() != null && !tempAssignment.getSubmissionTypes().contains(null)){
                        totalPoints += tempAssignment.getPointsPossible();
                        earnedPoints += tempSub.getScore();
                    }
                }
            }
            if(totalPoints != 0){
                earnedScore += (earnedPoints / totalPoints) * 100;
            }

            return (round(earnedScore, 2));
        }

        @Override
        protected void onPostExecute(Double aDouble) {
            super.onPostExecute(aDouble);
            mRecyclerAdapter.setWhatIfGrade(aDouble);
            totalGradeView.setText(aDouble + "%");
        }
    }

    @Override
    public boolean allowBookmarking() {
        return true;
    }
}

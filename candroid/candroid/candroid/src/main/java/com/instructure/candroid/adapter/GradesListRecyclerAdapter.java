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

package com.instructure.candroid.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.instructure.candroid.R;
import com.instructure.candroid.binders.EmptyBinder;
import com.instructure.candroid.binders.ExpandableHeaderBinder;
import com.instructure.candroid.binders.GradeBinder;
import com.instructure.candroid.dialog.WhatIfDialogStyled;
import com.instructure.candroid.holders.EmptyViewHolder;
import com.instructure.candroid.holders.ExpandableViewHolder;
import com.instructure.candroid.holders.GradeViewHolder;
import com.instructure.candroid.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi.api.AssignmentAPI;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.AssignmentGroup;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Enrollment;
import com.instructure.canvasapi.model.GradingPeriod;
import com.instructure.canvasapi.model.GradingPeriodResponse;
import com.instructure.canvasapi.model.Submission;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;
import com.instructure.pandautils.utils.CanvasContextColor;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.RetrofitError;
import retrofit.client.Response;

public class GradesListRecyclerAdapter extends ExpandableRecyclerAdapter<AssignmentGroup, Assignment, RecyclerView.ViewHolder> {

    private CanvasCallback<Course> mCourseCallback;
    private CanvasCallback<AssignmentGroup[]> mAssignmentGroupCallback;
    private CanvasCallback<GradingPeriodResponse> mGradingPeriodsCallback;
    private CanvasCallback<Enrollment[]> mEnrollmentCallback;
    private AdapterToFragmentCallback<Assignment> mAdapterToFragmentCallback;
    private AdapterToGradesCallback mAdapterToGradesCallback;
    private SetSelectedItemCallback mSelectedItemCallback;
    private WhatIfDialogStyled.WhatIfDialogCallback mDialogStyled;

    private CanvasContext mCanvasContext;
    private ArrayList<AssignmentGroup> mAssignmentGroups;
    private HashMap<Long, Assignment> mAssignmentsHash;
    private Double mWhatIfGrade;
    private GradingPeriod mCurrentGradingPeriod;

    //state for keeping track of grades for what/if and switching between periods
    private String mCurrentGrade;
    private double mCurrentScore;
    private String mFinalGrade;
    private double mFinalScore;

    private boolean mIsAllPeriodsGradeShown;

    public interface AdapterToGradesCallback{
        void notifyGradeChanged(double score, String grade);
        boolean getIsEdit();
        void setTermSpinnerState(boolean isEnabled);
    }

    public interface SetSelectedItemCallback{
        void setSelected(int position);
    }

    /* For Testing purposes only */
    protected GradesListRecyclerAdapter(Context context){
        super(context, AssignmentGroup.class, Assignment.class);
    }

    public GradesListRecyclerAdapter(Context context, CanvasContext canvasContext,
        AdapterToFragmentCallback adapterToFragmentCallback,
        AdapterToGradesCallback adapterToGradesCallback,
        CanvasCallback<GradingPeriodResponse> gradingPeriodsCallback, WhatIfDialogStyled.WhatIfDialogCallback dialogStyled) {
        super(context, AssignmentGroup.class, Assignment.class);

        mCanvasContext = canvasContext;
        mAdapterToFragmentCallback = adapterToFragmentCallback;
        mAdapterToGradesCallback = adapterToGradesCallback;
        mGradingPeriodsCallback = gradingPeriodsCallback;
        mDialogStyled = dialogStyled;

        mAssignmentGroups = new ArrayList<>();
        mAssignmentsHash = new HashMap<>();
        setExpandedByDefault(true);

        loadData();
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return new ExpandableViewHolder(v);
        } else if (viewType == Types.TYPE_EMPTY_CELL) {
            return new EmptyViewHolder(v);
        } else {
            return new GradeViewHolder(v);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return ExpandableViewHolder.holderResId();
        } else if (viewType == Types.TYPE_EMPTY_CELL) {
            return EmptyViewHolder.holderResId();
        } else {
            return GradeViewHolder.holderResId();
        }
    }
    @Override
    public void contextReady() {

    }

    @Override
    public void loadData() {
        CourseAPI.getCourseWithGrade(mCanvasContext.getId(), mCourseCallback);
    }

    public void loadAssignmentsForGradingPeriod (long gradingPeriodID, boolean refreshFirst) {
        /*Logic regarding MGP is similar here as it is in both assignment recycler adapters,
            if changes are made here, check if they are needed in the other recycler adapters.*/
        if(refreshFirst){
            resetData();
        }

        //Scope assignments if its for a student, both calls are still filtered by grading period
        if(((Course)mCanvasContext).isStudent()){
            AssignmentAPI.getAssignmentGroupsListScoped(mCanvasContext.getId(), gradingPeriodID, true, mAssignmentGroupCallback);
        } else {
            AssignmentAPI.getAssignmentGroupsListWithAssignmentsAndSubmissionsForGradingPeriod(mCanvasContext.getId(), gradingPeriodID, mAssignmentGroupCallback);
        }

        //Fetch the enrollments associated with the selected gradingPeriodID, these will contain the
        //correct grade for the period
        CourseAPI.getEnrollmentsForGradingPeriod(mCanvasContext.getId(), gradingPeriodID, mEnrollmentCallback);
    }


    public void loadAssignment () {
        //We need to update the course grades in all cases, so here are the additional MGP and
        //none mgp cases for grade updates
        if (isAllGradingPeriodsSelected()) {
            //Grade update for all grading periods selected
            mAdapterToGradesCallback.notifyGradeChanged(((Course) mCanvasContext).getFinalScore(), ((Course) mCanvasContext).getFinalGrade());
            mFinalScore = ((Course) mCanvasContext).getFinalScore();
            mFinalGrade = ((Course) mCanvasContext).getFinalGrade();
        } else {
            //Grade update for non - MGP
            if(((Course) mCanvasContext).getCurrentScore() == 0.0
                    && (((Course) mCanvasContext).getCurrentGrade() == null
                    || "null".equals(((Course) mCanvasContext).getCurrentGrade()))) {
                mAdapterToGradesCallback.notifyGradeChanged(0.0, mContext.getString(R.string.noGradeText));
            } else {
                mAdapterToGradesCallback.notifyGradeChanged(((Course) mCanvasContext).getCurrentScore(), ((Course) mCanvasContext).getCurrentGrade());
            }
            mFinalScore = ((Course) mCanvasContext).getFinalScore();
            mFinalGrade = ((Course) mCanvasContext).getFinalGrade();
            mCurrentScore = ((Course) mCanvasContext).getCurrentScore();
            mCurrentGrade = ((Course) mCanvasContext).getCurrentGrade();
        }
        //Standard load assignments, unfiltered
        AssignmentAPI.getAssignmentGroupsListWithAssignmentsAndSubmissions(mCanvasContext.getId(), mAssignmentGroupCallback);
    }

    @Override
    public void setupCallbacks(){
        /*Logic regarding MGP is similar here as it is in both assignment recycler adapters,
            if changes are made here, check if they are needed in the other recycler adapters.*/
        mCourseCallback = new CanvasCallback<Course>(this) {
            @Override
            public void firstPage(Course course, LinkHeaders linkHeaders, Response response) {
                mCanvasContext = course;

                if (isAllGradingPeriodsSelected()) {
                    loadAssignment();
                    return;
                }

                for (Enrollment enrollment : course.getEnrollments()) {
                    if (enrollment.isStudent() && enrollment.isMultipleGradingPeriodsEnabled()) {
                        mIsAllPeriodsGradeShown = enrollment.isTotalsForAllGradingPeriodsOption();
                        if(mCurrentGradingPeriod == null || mCurrentGradingPeriod.getTitle() == null) {
                            //we load current term
                            mCurrentGradingPeriod = new GradingPeriod();
                            mCurrentGradingPeriod.setId(enrollment.getCurrentGradingPeriodId());
                            mCurrentGradingPeriod.setTitle(enrollment.getCurrentGradingPeriodTitle());
                            //request the grading period objects and make the assignment calls
                            //This callback is fulfilled in the grade list fragment.
                            CourseAPI.getGradingPeriodsForCourse(course.getId(), mGradingPeriodsCallback);
                            return;
                        } else {
                            //Otherwise we load the info from the current grading period
                            loadAssignmentsForGradingPeriod(mCurrentGradingPeriod.getId(), true);
                            return;
                        }
                    }
                }

                //if we've made it this far, MGP is not enabled, so we do the standard behavior
                loadAssignment();
            }

            @Override
            public void cache(Course course, LinkHeaders linkHeaders, Response response) {
            }
        };

        mAssignmentGroupCallback = new CanvasCallback<AssignmentGroup[]>(this) {
            @Override
            public void firstPage(AssignmentGroup[] assignmentGroups, LinkHeaders linkHeaders, Response response) {
                //we still need to maintain local copies of the assignments/groups for what if grades
                //so we have the assignments Hash and assignments group list
                for (AssignmentGroup group : assignmentGroups) {
                    addOrUpdateAllItems(group, group.getAssignments());
                    for(Assignment assignment : group.getAssignments()){
                        mAssignmentsHash.put(assignment.getId(), assignment);
                    }
                    if(!mAssignmentGroups.contains(group)){
                        mAssignmentGroups.add(group);
                    }
                }
                setAllPagesLoaded(true);

                mAdapterToFragmentCallback.onRefreshFinished();
            }

        };

        mEnrollmentCallback = new CanvasCallback<Enrollment[]>(this) {
            @Override
            public void firstPage(Enrollment[] enrollments, LinkHeaders linkHeaders, Response response) {
                for (Enrollment enrollment : enrollments) {
                    if (enrollment.isStudent() && enrollment.getUserId() == APIHelpers.getCacheUser(getContext()).getId()) {
                        //If there are no assignments and the grade is a zero/null we want to match
                        //the web's behavior and set it to N/A
                        if(mAssignmentsHash.isEmpty() && enrollment.getCurrentScore() == 0.0
                                && (enrollment.getCurrentGrade() == null || "null".equals(enrollment.getCurrentGrade()))) {
                            mAdapterToGradesCallback.notifyGradeChanged(0.0, mContext.getString(R.string.noGradeText));
                        } else {
                            mAdapterToGradesCallback.notifyGradeChanged(enrollment.getCurrentScore(), enrollment.getCurrentGrade());
                        }
                        mCurrentGrade = enrollment.getCurrentGrade();
                        mCurrentScore = enrollment.getCurrentScore();
                        mFinalGrade = enrollment.getFinalGrade();
                        mFinalScore = enrollment.getFinalScore();
                        //Inform the spinner things are done
                        mAdapterToGradesCallback.setTermSpinnerState(true);
                        //we need to update the course that the fragment is using
                        ((Course)mCanvasContext).addEnrollment(enrollment);
                    }
                }
            }

            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                mAdapterToGradesCallback.setTermSpinnerState(true);
                return super.onFailure(retrofitError);
            }

            @Override
            public void cache(Enrollment[] enrollments, LinkHeaders linkHeaders, Response response) {
            }
        };

        mSelectedItemCallback = new SetSelectedItemCallback() {
            @Override
            public void setSelected(int position) {
                setSelectedPosition(position);
            }
        };
    }

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, AssignmentGroup assignmentGroup, Assignment assignment) {
        boolean isEdit = mAdapterToGradesCallback.getIsEdit();
        if(isEdit){
            GradeBinder.bind((GradeViewHolder) holder, getContext(), CanvasContextColor.getCachedColor(getContext(),
                    mCanvasContext), mAssignmentsHash.get(assignment.getId()), (Course) mCanvasContext, mAdapterToGradesCallback.getIsEdit(), mDialogStyled, mAdapterToFragmentCallback, mSelectedItemCallback);
        } else {
            GradeBinder.bind((GradeViewHolder) holder, getContext(), CanvasContextColor.getCachedColor(getContext(),
                    mCanvasContext), assignment, (Course) mCanvasContext, mAdapterToGradesCallback.getIsEdit(), mDialogStyled, mAdapterToFragmentCallback, mSelectedItemCallback);
        }
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, AssignmentGroup assignmentGroup, boolean isExpanded) {
        ExpandableHeaderBinder.bind(getContext(), mCanvasContext, (ExpandableViewHolder) holder, assignmentGroup, assignmentGroup.getName(), isExpanded, getViewHolderHeaderClicked());
    }

    @Override
    public void onBindEmptyHolder(RecyclerView.ViewHolder holder, AssignmentGroup assignmentGroup) {
        EmptyBinder.bind((EmptyViewHolder) holder, getContext().getResources().getString(R.string.noAssignmentsInGroup));
    }

    @Override
    public GroupSortedList.GroupComparatorCallback<AssignmentGroup> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<AssignmentGroup>() {
            @Override
            public int compare(AssignmentGroup o1, AssignmentGroup o2) {
                return o1.getPosition() - o2.getPosition();
            }

            @Override
            public boolean areContentsTheSame(AssignmentGroup oldGroup, AssignmentGroup newGroup) {
                return oldGroup.getName().equals(newGroup.getName());
            }

            @Override
            public boolean areItemsTheSame(AssignmentGroup group1, AssignmentGroup group2) {
                return group1.getId() == group2.getId();
            }

            @Override
            public int getGroupType(AssignmentGroup group) {
                return Types.TYPE_HEADER;
            }

            @Override
            public long getUniqueGroupId(AssignmentGroup group) {
                return group.getId();
            }
        };
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<AssignmentGroup, Assignment> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<AssignmentGroup, Assignment>() {
            @Override
            public int compare(AssignmentGroup group, Assignment o1, Assignment o2) {
                return o1.getPosition() - o2.getPosition();
            }

            @Override
            public boolean areContentsTheSame(Assignment oldItem, Assignment newItem) {
                return compareAssignments(oldItem, newItem);
            }

            @Override
            public boolean areItemsTheSame(Assignment item1, Assignment item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public long getUniqueItemId(Assignment item) {
                return item.getId();
            }

            @Override
            public int getChildType(AssignmentGroup group, Assignment item) {
                return Types.TYPE_ITEM;
            }
        };
    }

    @Override
    public void resetData() {
        mAssignmentsHash.clear();
        mAssignmentGroups.clear();
        super.resetData();
    }

    public ArrayList<AssignmentGroup> getAssignmentGroups(){
        return mAssignmentGroups;
    }

    public HashMap<Long, Assignment> getAssignmentsHash(){
        return mAssignmentsHash;
    }

    private boolean compareAssignments(Assignment oldItem, Assignment newItem) {
        boolean isSameName = oldItem.getName().equals(newItem.getName());
        boolean isSameScore = oldItem.getPointsPossible() == newItem.getPointsPossible();
        boolean isSameSubmission = true;
        boolean isSameGrade = true;
        Submission oldSubmission = oldItem.getLastSubmission();
        Submission newSubmission = newItem.getLastSubmission();
        if (oldSubmission != null && newSubmission != null) {
            if (oldSubmission.getGrade() != null && newSubmission.getGrade() != null) {
                isSameGrade = oldSubmission.getGrade().equals(newSubmission.getGrade());
            } else if (isNullableChanged(oldSubmission.getGrade(), newSubmission.getGrade())){
                isSameGrade = false;
            }
        }else if (isNullableChanged(oldSubmission, newSubmission)) {
            isSameSubmission = false;
        }
        return isSameName && isSameGrade && isSameScore && isSameSubmission;
    }

    private boolean isNullableChanged(Object o1, Object o2) {
        return (o1 == null && o2 != null) || (o1 !=null && o2 == null);
    }

    public void setWhatIfGrade(Double grade){
        mWhatIfGrade = grade;
    }

    public Double getWhatIfGrade(){
        return mWhatIfGrade;
    }

    public void setCurrentGradingPeriod(GradingPeriod gradingPeriod) {
        mCurrentGradingPeriod = gradingPeriod;
    }

    public GradingPeriod getCurrentGradingPeriod() {
        return mCurrentGradingPeriod;
    }

    public String getCurrentGrade(){
        return mCurrentGrade;
    }

    public double getCurrentScore(){
        return mCurrentScore;
    }

    public String getFinalGrade(){
        return mFinalGrade;
    }

    public double getFinalScore(){
        return mFinalScore;
    }

    public boolean isAllPeriodsGradeShown(){
        return mIsAllPeriodsGradeShown;
    }

    public boolean isAllGradingPeriodsSelected(){
        return mCurrentGradingPeriod != null
                && mCurrentGradingPeriod.getTitle() != null
                && mCurrentGradingPeriod.getTitle().equals(getContext().getString(R.string.allGradingPeriods));
    }

}

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
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.instructure.candroid.R;
import com.instructure.candroid.binders.ExpandableHeaderBinder;
import com.instructure.candroid.binders.RubricBinder;
import com.instructure.candroid.binders.RubricTopHeaderBinder;
import com.instructure.candroid.holders.ExpandableViewHolder;
import com.instructure.candroid.holders.RubricTopHeaderViewHolder;
import com.instructure.candroid.holders.RubricViewHolder;
import com.instructure.candroid.interfaces.AdapterToFragmentCallback;
import com.instructure.candroid.util.NoNetworkErrorDelegate;
import com.instructure.candroid.view.EmptyPandaView;
import com.instructure.canvasapi.api.SubmissionAPI;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.RubricCriterion;
import com.instructure.canvasapi.model.RubricCriterionRating;
import com.instructure.canvasapi.model.Submission;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;
import java.util.HashMap;
import java.util.List;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RubricRecyclerAdapter extends ExpandableRecyclerAdapter<RubricCriterion, RubricCriterionRating, RecyclerView.ViewHolder> {

    private CanvasContext mCanvasContext;
    private Assignment mAssignment;
    private AdapterToFragmentCallback mAdapterToFragment;
    private CanvasCallback<Submission> mSubmissionCallback;
    private RubricCriterion mTopViewHeader; // The top header is just a group with a different view layout

    // region Order Work-around
    // Since BaseListRecyclerAdapter uses a sorted list to store the list items, there has to be something to order them by.
    // Recipients have no clear way to order (Can't do by name, because the Last name isn't always in a consistent spot)
    // Since a hash is pretty easy, it made more sense than to create another BaseListRA that had a different representation.
    private HashMap<String, Integer> mInsertedOrderHash = new HashMap<>();

    /* For testing purposes only */
    protected RubricRecyclerAdapter(Context context){
        super(context, RubricCriterion.class, RubricCriterionRating.class);
    }

    public RubricRecyclerAdapter(Context context, CanvasContext canvasContext, EmptyPandaView emptyRubricView, AdapterToFragmentCallback adapterToFragmentCallback) {
        super(context, RubricCriterion.class, RubricCriterionRating.class);
        mTopViewHeader = new RubricCriterion(null);
        mTopViewHeader.setId("TopViewHeader"); // needs an id for expandableRecyclerAdapter to work
        mCanvasContext = canvasContext;
        mAdapterToFragment = adapterToFragmentCallback;
        setExpandedByDefault(true);
        // loadData is called from RubricFragment
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return new ExpandableViewHolder(v);
        } else if (viewType == Types.TYPE_TOP_HEADER) {
            return new RubricTopHeaderViewHolder(v);
        } else {
            return new RubricViewHolder(v, viewType);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if (viewType == Types.TYPE_HEADER) {
            return ExpandableViewHolder.holderResId();
        } else if (viewType == Types.TYPE_TOP_HEADER){
            return RubricTopHeaderViewHolder.holderResId();
        } else {
            return RubricViewHolder.holderResId(viewType);
        }
    }

    @Override
    public void contextReady() {}

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, RubricCriterion rubricCriterion, RubricCriterionRating rubricCriterionRating) {
        if(!mAssignment.isMuted()){
            RubricBinder.bind(getContext(), (RubricViewHolder) holder, rubricCriterionRating, mCanvasContext);
        }
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, RubricCriterion rubricCriterion, boolean isExpanded) {
        if (holder instanceof RubricTopHeaderViewHolder) {
            onBindTopHeaderHolder(holder);
        } else {
            if(!mAssignment.isMuted()) {
                ExpandableHeaderBinder.bind(getContext(), mCanvasContext, (ExpandableViewHolder) holder, rubricCriterion, rubricCriterion.getCriterionDescription(), isExpanded, getViewHolderHeaderClicked());
            }
        }
    }

    private void onBindTopHeaderHolder(RecyclerView.ViewHolder holder) {
        RubricTopHeaderBinder.bind(getContext(), (RubricTopHeaderViewHolder) holder, getCurrentPoints(), getCurrentGrade(), mAssignment.isMuted());
    }

    // region Data

    @Override
    public void loadData() {
        // use loadDataChained instead, since its a nested fragment and has chained callbacks
        loadDataChained(false, false); // Used when data is refreshed
    }

    /**
     * For explanation of isWithinAnotherCallback and isCached refer to comment in {@link com.instructure.candroid.activity.CallbackActivity#getUserSelf}
     */
    public void loadDataChained(boolean isWithinAnotherCallback, boolean isCached) {
        if (mAssignment == null) { return; }
        if (isWithinAnotherCallback) {
            SubmissionAPI.getSubmissionWithCommentsAndHistoryChained(mCanvasContext, mAssignment.getId(), APIHelpers.getCacheUser(getContext()).getId(), mSubmissionCallback, isCached);
        } else {
            SubmissionAPI.getSubmissionWithCommentsAndHistory(mCanvasContext, mAssignment.getId(), APIHelpers.getCacheUser(getContext()).getId(), mSubmissionCallback);
        }
    }

    @Override
    public void setupCallbacks() {
        mSubmissionCallback = new CanvasCallback<Submission>(this, new NoNetworkErrorDelegate()) {
            @Override
            public void firstPage(Submission submission, LinkHeaders linkHeaders, Response response) {
                // Don't mark freeFormComments since we only display the RubricCriterionRating in those cases.
                if (submission != null && mAssignment.hasRubric() && !mAssignment.isFreeFormCriterionComments()) {
                    mAssignment.getRubric().get(0).markGrades(submission.getRubricAssessment(), mAssignment.getRubric());
                }
                mAssignment.setLastSubmission(submission);
                mAdapterToFragment.onRefreshFinished();
                populateAssignmentDetails();
                setAllPagesLoaded(true);
            }

            //Submission API stuff sometimes hit 404 errors.
            //We should hide the progress dialog when that happens.
            @Override
            public boolean onFailure(RetrofitError retrofitError) {
                populateAssignmentDetails();
                //getting the submission fails for a teacher, so don't show the crouton
                if(!((Course)mCanvasContext).isTeacher()) {
                    return false;
                }
                return true;
            }
        };
    }

    private void populateAssignmentDetails() {
        addOrUpdateGroup(mTopViewHeader); // acts as a place holder for the top header

        final List<RubricCriterion> rubric = mAssignment.getRubric();

        if (mAssignment.hasRubric() && !mAssignment.isFreeFormCriterionComments()) {
            populateRatingItems(rubric);
        } else if(mAssignment.isFreeFormCriterionComments()){
            populateFreeFormRatingItems(rubric);
        } else {
            getAdapterToRecyclerViewCallback().setIsEmpty(true);
        }
    }
    // endregion

    // region Grade Helpers
    private void populateRatingItems(List<RubricCriterion> rubric){
        int insertCount = 0;
        for (RubricCriterion rubricCriterion : rubric) {
            final List<RubricCriterionRating> rubricCriterionRatings = rubricCriterion.getRatings();
            for(RubricCriterionRating rating : rubricCriterionRatings) {
                mInsertedOrderHash.put(rubricCriterion.getId(), ++insertCount);
                addOrUpdateItem(rubricCriterion, rating);
            }
        }
    }

    private void populateFreeFormRatingItems(List<RubricCriterion> rubric) {
        int insertCount = 0;
        for(RubricCriterion rubricCriterion : rubric){
            RubricCriterionRating gradedRating = getFreeFormRatingForCriterion(rubricCriterion);
            if( gradedRating != null){
                mInsertedOrderHash.put(rubricCriterion.getId(), ++insertCount);
                addOrUpdateItem(rubricCriterion, gradedRating);
            }
        }
    }

    private RubricCriterionRating getFreeFormRatingForCriterion(RubricCriterion criterion){
        Submission lastSubmission = mAssignment.getLastSubmission();
        if(lastSubmission != null){
          RubricCriterionRating rating =  lastSubmission.getRubricAssessmentHash().get(criterion.getId());
            if(rating != null){
                rating.setId(criterion.getId()); // We give the rating the criterion's id since the api doesn't return one
                rating.setMaxPoints(criterion.getPoints());
                rating.setIsFreeFormComment(true);
            }
            return rating;
        }
        return null;
    }

    private String getPointsPossible() {
        String pointsPossible = "";
        if (mAssignment != null) {
            if (Math.floor(mAssignment.getPointsPossible()) == mAssignment.getPointsPossible()) {
                pointsPossible += (int) mAssignment.getPointsPossible();
            } else {
                pointsPossible += mAssignment.getPointsPossible();
            }
        }
        return pointsPossible;
    }

    private boolean containsGrade() {
        return mAssignment != null && mAssignment.getLastSubmission() != null && mAssignment.getLastSubmission().getGrade() != null && !mAssignment.getLastSubmission().getGrade().equals("null");
    }

    private boolean isExcused() {
        return mAssignment != null && mAssignment.getLastSubmission() != null && mAssignment.getLastSubmission().isExcused();
    }

    private boolean isGradeLetterOrPercentage(String grade) {
        return grade.contains("%") || grade.matches("[a-zA-Z]+");
    }

    @Nullable
    private String getCurrentGrade() {
        String pointsPossible = getPointsPossible();
        if (isExcused()) {
            return getContext().getString(R.string.grade) + "\n" + getContext().getString(R.string.excused) + " / " + pointsPossible;
        }
        if (containsGrade()) {
            String grade = mAssignment.getLastSubmission().getGrade();
            if (isGradeLetterOrPercentage(grade)) {
                return getContext().getString(R.string.grade) + "\n" + grade;
            } else {
                return getContext().getString(R.string.grade) + "\n" + grade + " / " + pointsPossible;
            }
        }
        return null;
    }

    private String getCurrentPoints() {
        String pointsPossible = getPointsPossible();
        if (isExcused()) {
            return null;
        }
        if (containsGrade()) {
            String grade = mAssignment.getLastSubmission().getGrade();
            if (isGradeLetterOrPercentage(grade)) {
                return getContext().getString(R.string.points) + "\n" +  mAssignment.getLastSubmission().getScore() + " / " + pointsPossible;
            } else {
                return null;
            }
        } else {
            //the user doesn't have a grade, but we should display points possible if the mAssignment isn't null
            if(mAssignment != null) {
                //if the user is a teacher show them how many points are possible for the mAssignment
                if(((Course)mCanvasContext).isTeacher()) {
                    return getContext().getString(R.string.pointsPossibleNoPeriod) + "\n" + pointsPossible;
                } else {
                    return getContext().getString(R.string.points) + "\n" + "- / " + pointsPossible;
                }
            }
        }
        return getContext().getString(R.string.points) + "\n" + "- / -";
    }

    // endregion

    // region Expandable Callbacks
    @Override
    public GroupSortedList.GroupComparatorCallback<RubricCriterion> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<RubricCriterion>() {
            @Override
            public int compare(RubricCriterion o1, RubricCriterion o2) {
                // Always put the TopViewHeader at the top
                if (o1 == o2 && o1 == mTopViewHeader) {
                    return 0;
                } else if (o1 == mTopViewHeader) {
                    return -1;
                } else if (o2 == mTopViewHeader) {
                    return 1;
                }
                    return mInsertedOrderHash.get(o1.getId()) - mInsertedOrderHash.get(o2.getId());
                }

            @Override
            public boolean areContentsTheSame(RubricCriterion oldGroup, RubricCriterion newGroup) {
                return oldGroup.getCriterionDescription().equals(newGroup.getCriterionDescription());
            }

            @Override
            public boolean areItemsTheSame(RubricCriterion group1, RubricCriterion group2) {
                return group1.getId().equals(group2.getId());
            }

            @Override
            public long getUniqueGroupId(RubricCriterion group) {
                return group.getId().hashCode();
            }

            @Override
            public int getGroupType(RubricCriterion group) {
                if (group == mTopViewHeader) {
                    return Types.TYPE_TOP_HEADER;
                } else {
                    return Types.TYPE_HEADER;
                }
            }
        };
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<RubricCriterion, RubricCriterionRating> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<RubricCriterion, RubricCriterionRating>() {
            @Override
            public int compare(RubricCriterion group, RubricCriterionRating o1, RubricCriterionRating o2) {
                // put comments at the bottom
                if (o1.isComment() && o2.isComment()) {
                    return 0;
                } else if (o1.isComment()) {
                    return 1;
                } else if (o2.isComment()) {
                    return -1;
                }
                return Double.compare(o2.getPoints(), o1.getPoints());
            }

            @Override
            public boolean areContentsTheSame(RubricCriterionRating oldItem, RubricCriterionRating newItem) {
                if(oldItem.getRatingDescription() == null || newItem.getRatingDescription() == null){return false;}

                return oldItem.getRatingDescription().equals(newItem.getRatingDescription())
                        && !(oldItem.isComment() || newItem.isComment()) // if its a comment always refresh the layout
                        && oldItem.getPoints() == newItem.getPoints();
            }

            @Override
            public boolean areItemsTheSame(RubricCriterionRating item1, RubricCriterionRating item2) {
                return item1.getId().equals(item2.getId());
            }

            @Override
            public long getUniqueItemId(RubricCriterionRating item) {
                return item.getId().hashCode();
            }

            @Override
            public int getChildType(RubricCriterion group, RubricCriterionRating item) {
                if(item.isFreeFormComment() || item.isComment()){
                    return RubricViewHolder.TYPE_ITEM_COMMENT;
                }
                return RubricViewHolder.TYPE_ITEM_POINTS;
            }
        };
    }
    // endregion

    // region Getter & Setters

    public Assignment getAssignment() {
        return mAssignment;
    }

    public void setAssignment(Assignment assignment) {
        this.mAssignment = assignment;
    }


    // endregion

}

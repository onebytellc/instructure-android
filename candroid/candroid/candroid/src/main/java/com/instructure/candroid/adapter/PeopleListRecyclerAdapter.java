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
import com.instructure.candroid.binders.PeopleBinder;
import com.instructure.candroid.binders.PeopleHeaderBinder;
import com.instructure.candroid.holders.PeopleHeaderViewHolder;
import com.instructure.candroid.holders.PeopleViewHolder;
import com.instructure.candroid.interfaces.AdapterToFragmentCallback;
import com.instructure.canvasapi.api.UserAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Enrollment;
import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandarecycler.util.GroupSortedList;
import com.instructure.pandarecycler.util.Types;
import com.instructure.pandautils.utils.CanvasContextColor;

import java.util.List;

import retrofit.client.Response;

public class PeopleListRecyclerAdapter extends ExpandableRecyclerAdapter<Enrollment, User, RecyclerView.ViewHolder>{

    public enum TYPE {Teacher, Student, Ta, Observer}

    private AdapterToFragmentCallback<User> mAdapterToFragmentCallback;

    private CanvasCallback<User[]> mUserCanvasCallback;

    //These Callbacks are only used in a group context
    private CanvasCallback<User[]> mTeacherCallback;
    private CanvasCallback<User[]> mTaCallback;

    private boolean mIsTeacherCallback = false;
    private boolean mIsTaCallback = false;
    private boolean mIsUserCallback = false;

    private CanvasContext mCanvasContext;
    private int mCourseColor;

    public PeopleListRecyclerAdapter(Context context, CanvasContext canvasContext, AdapterToFragmentCallback<User> adapterToFragmentCallback) {
        super(context, Enrollment.class, User.class);

        mAdapterToFragmentCallback = adapterToFragmentCallback;
        mCanvasContext = canvasContext;
        mCourseColor = CanvasContextColor.getCachedColor(context, canvasContext);
        setExpandedByDefault(true);

        loadData();
    }

    @Override
    public void setupCallbacks() {
        mTaCallback = new CanvasCallback<User[]>(this) {

            @Override
            public void firstPage(User[] users, LinkHeaders linkHeaders, Response response) {

                mIsTaCallback = true;

                // update ui here with results
                setNextUrl(linkHeaders.nextURL);
                populateAdapter(users);

                if(linkHeaders.nextURL == null){
                    mIsTaCallback = false;
                    UserAPI.getFirstPagePeopleChained(mCanvasContext, APIHelpers.isCachedResponse(response), mUserCanvasCallback);
                }
            }
        };

        mTeacherCallback = new CanvasCallback<User[]>(this) {

            @Override
            public void firstPage(User[] users, LinkHeaders linkHeaders, Response response) {

                mIsTeacherCallback = true;

                // update ui here with results
                setNextUrl(linkHeaders.nextURL);
                populateAdapter(users);

                if(linkHeaders.nextURL == null){
                    mIsTeacherCallback = false;
                    if(CanvasContext.Type.isGroup(mCanvasContext) && ((Group)(mCanvasContext)).getCourseId() > 0) {
                        UserAPI.getFirstPagePeopleChained(CanvasContext.getGenericContext(CanvasContext.Type.COURSE, ((Group) (mCanvasContext)).getCourseId(), ""), UserAPI.ENROLLMENT_TYPE.TA, APIHelpers.isCachedResponse(response), mTaCallback);
                    } else {
                        UserAPI.getFirstPagePeopleChained(mCanvasContext, UserAPI.ENROLLMENT_TYPE.TA, APIHelpers.isCachedResponse(response), mTaCallback);
                    }
                }
            }
        };

        mUserCanvasCallback = new CanvasCallback<User[]>(this) {

            @Override
            public void firstPage(User[] users, LinkHeaders linkHeaders, Response response) {

                mIsUserCallback = true;

                // update ui here with results
                setNextUrl(linkHeaders.nextURL);

                populateAdapter(users);
            }
        };
    }

    private void populateAdapter(User[] result) {
        //check to see if we have teachers/tas/students
        Enrollment teacherEnrollment = null;
        Enrollment studentEnrollment = null;
        Enrollment groupMember = null;
        //we at least want teachers & TAs first, then students, then everything else
        for(User user : result) {
            List<Enrollment> enrollmentType = user.getEnrollments();
            if(enrollmentType != null){
                for(Enrollment enrollment: enrollmentType){
                    if(enrollment.getType().equals(TYPE.Teacher.toString()) || enrollment.getType().equals(TYPE.Ta.toString())){
                        teacherEnrollment = enrollment;
                    }else if(enrollment.getType().equals(TYPE.Student.toString())){
                        studentEnrollment = enrollment;
                    }
                }
            }
        }

        if(teacherEnrollment != null) {
            addOrUpdateGroup(teacherEnrollment);
        }
        if(studentEnrollment != null && !CanvasContext.Type.isGroup(mCanvasContext)) {
            addOrUpdateGroup(studentEnrollment);
        }
        if(CanvasContext.Type.isGroup(mCanvasContext)){
            groupMember = new Enrollment();
            groupMember.setType(getContext().getString(R.string.groupMembers));
            addOrUpdateGroup(groupMember);
        }

        //put each result in the correct category based on enrollmenttype
        for(User user : result) {
            List<Enrollment> enrollmentTypes = user.getEnrollments();
            if(enrollmentTypes != null && enrollmentTypes.size() > 0) {
                //each user could have multiple enrollments, we want to put them in the correct buckets
                //We don't want users being in multiple buckets
                Enrollment bestGuessEnrollment = enrollmentTypes.get(0);
                boolean isTeacherOrTA = false;
                boolean isStudent = false;
                for(Enrollment enrollment: enrollmentTypes) {
                    if(enrollment.getType().equals(TYPE.Teacher.toString()) ||  enrollment.getType().equals(TYPE.Ta.toString())){
                        isTeacherOrTA = true;
                        bestGuessEnrollment = enrollment;
                    }else if(enrollment.getType().equals(TYPE.Student.toString())){
                        isStudent = true;
                        if(!isTeacherOrTA) {
                            bestGuessEnrollment = enrollment;
                        }
                    }else if(!CanvasContext.Type.isGroup(mCanvasContext)){
                        if(!isStudent && !isTeacherOrTA) {
                            bestGuessEnrollment = enrollment;
                        }
                    }
                }

                addOrUpdateItem(bestGuessEnrollment, user);

            } else if(CanvasContext.Type.isGroup(mCanvasContext)){
                addOrUpdateItem(groupMember, user);
            }
        }

        notifyDataSetChanged();
        mAdapterToFragmentCallback.onRefreshFinished();
    }

    @Override
    public void loadFirstPage() {
        //If the canvasContext is a group, and has a course we want to add the Teachers and TAs from that course to the peoples list
        if(CanvasContext.Type.isGroup(mCanvasContext) && ((Group)(mCanvasContext)).getCourseId() > 0){
            //We build a generic CanvasContext with type set to COURSE and give it the CourseId from the group, so that it wil use the course API not the group API
            UserAPI.getFirstPagePeople(CanvasContext.getGenericContext(CanvasContext.Type.COURSE, ((Group) (mCanvasContext)).getCourseId(), ""), UserAPI.ENROLLMENT_TYPE.TEACHER, mTeacherCallback);
        } else {
            UserAPI.getFirstPagePeople(mCanvasContext, UserAPI.ENROLLMENT_TYPE.TEACHER, mTeacherCallback);
        }
    }

    @Override
    public void loadNextPage(String nextURL) {

        if(mIsTeacherCallback){
            UserAPI.getNextPagePeople(nextURL, mTeacherCallback);
        }
        if(mIsTaCallback) {
            UserAPI.getNextPagePeople(nextURL, mTaCallback);
        }
        if (mIsUserCallback){
            UserAPI.getNextPagePeople(nextURL, mUserCanvasCallback);
        }
    }

    @Override
    public boolean isPaginated() {
        return true;
    }


    @Override
    public void refresh() {
        super.refresh();
        mIsTaCallback = false;
        mIsTeacherCallback = false;
        mIsUserCallback = false;
    }

    @Override
    public RecyclerView.ViewHolder createViewHolder(View v, int viewType) {
        if(viewType == Types.TYPE_HEADER){
            return new PeopleHeaderViewHolder(v);
        } else {
            return new PeopleViewHolder(v);
        }
    }

    @Override
    public int itemLayoutResId(int viewType) {
        if(viewType == Types.TYPE_HEADER){
            return PeopleHeaderViewHolder.holderResId();
        } else {
            return PeopleViewHolder.holderResId();
        }
    }

    @Override
    public void contextReady() {

    }

    @Override
    public void onBindChildHolder(RecyclerView.ViewHolder holder, Enrollment enrollment, User user) {
        final int groupItemCount = getGroupItemCount(enrollment);
        final int itemPosition = storedIndexOfItem(enrollment, user);

        PeopleBinder.bind(user, getContext(), (PeopleViewHolder) holder, mAdapterToFragmentCallback, mCourseColor, itemPosition == 0, itemPosition == groupItemCount - 1);
    }

    @Override
    public void onBindHeaderHolder(RecyclerView.ViewHolder holder, Enrollment enrollment, boolean isExpanded) {
        PeopleHeaderBinder.bind(getContext(), mCanvasContext, (PeopleHeaderViewHolder) holder, enrollment, getHeaderTitle(enrollment), isExpanded, getViewHolderHeaderClicked());
    }

    @Override
    public GroupSortedList.GroupComparatorCallback<Enrollment> createGroupCallback() {
        return new GroupSortedList.GroupComparatorCallback<Enrollment>() {
            @Override
            public int compare(Enrollment o1, Enrollment o2) {
                return getHeaderTitle(o2).compareTo(getHeaderTitle(o1));
            }

            @Override
            public boolean areContentsTheSame(Enrollment oldGroup, Enrollment newGroup) {
                return getHeaderTitle(oldGroup).equals(getHeaderTitle(newGroup));
            }

            @Override
            public boolean areItemsTheSame(Enrollment group1, Enrollment group2) {
                return group1.getId() == group2.getId();
            }

            @Override
            public long getUniqueGroupId(Enrollment group) {
                return getHeaderTitle(group).hashCode();
            }

            @Override
            public int getGroupType(Enrollment group) {
                return Types.TYPE_HEADER;
            }
        };
    }

    @Override
    public GroupSortedList.ItemComparatorCallback<Enrollment, User> createItemCallback() {
        return new GroupSortedList.ItemComparatorCallback<Enrollment, User>() {
            @Override
            public int compare(Enrollment group, User o1, User o2) {
                return o1.getSortableName().toLowerCase().compareTo(o2.getSortableName().toLowerCase());
            }

            @Override
            public boolean areContentsTheSame(User oldItem, User newItem) {
                return oldItem.getSortableName().equals(newItem.getSortableName());
            }

            @Override
            public boolean areItemsTheSame(User item1, User item2) {
                return item1.getId() == item2.getId();
            }

            @Override
            public long getUniqueItemId(User item) {
                return item.getId();
            }

            @Override
            public int getChildType(Enrollment group, User item) {
                return Types.TYPE_ITEM;
            }
        };
    }

    private String getHeaderTitle(Enrollment enrollment){
        if(enrollment.getType().equals("")){
            return "";
        }
        else if(enrollment.getType().equals(TYPE.Teacher.toString()) ||  enrollment.getType().equals(TYPE.Ta.toString())){
            return getContext().getString(R.string.teachersTas);
        }else if (enrollment.getType().equals(TYPE.Student.toString())){
            return getContext().getString(R.string.students);
        } else if (enrollment.getType().equals(TYPE.Observer.toString())){
            return getContext().getString(R.string.observers);
        }else{
            return enrollment.getType();
        }
    }
}

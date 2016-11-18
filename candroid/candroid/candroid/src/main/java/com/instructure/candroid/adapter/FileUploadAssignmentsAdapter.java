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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.instructure.candroid.R;
import com.instructure.canvasapi.model.Assignment;
import com.instructure.canvasapi.model.Course;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class FileUploadAssignmentsAdapter extends ArrayAdapter<Assignment> {

    // Member Variables
    private List<Assignment> assignments = new ArrayList<>();
    private Context context;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////
    public FileUploadAssignmentsAdapter(Context context, ArrayList<Assignment> assignments) {
        super(context, R.layout.spinner_row_courses, assignments);
        this.assignments =  assignments;
        this.context = context;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Adapter Overrides
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public int getCount() {
        return this.assignments.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        AssignmentViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_row_courses, null);

            holder = new AssignmentViewHolder();
            holder.assignmentName = (TextView) convertView.findViewById(R.id.courseName);

            convertView.setTag(holder);
        } else {
            holder = (AssignmentViewHolder) convertView.getTag();
        }

        if (assignments.get(position) != null) {
            holder.assignmentName.setText(assignments.get(position).getName());
        }

        return convertView;
    }

    private static class AssignmentViewHolder {
        TextView assignmentName;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////
    public void setAssignments(ArrayList<Assignment> assignments){
        clearAssignments();
        this.assignments = getOnlineUploadAssignmentsList(context, assignments);
        notifyDataSetChanged();
    }

    public List<Assignment> getAssignments() {
        return this.assignments;
    }

    public void clearAssignments(){
        this.assignments.clear();
    }

    public static ArrayList<Assignment> getOnlineUploadAssignmentsList(Context context, List<Assignment> newAssignments){
        ArrayList<Assignment> onlineUploadAssignments = new ArrayList<>();
        Date currentDate = new Date();

        for(Assignment assignment : newAssignments){
            if (assignment.getSubmissionTypes().contains(Assignment.SUBMISSION_TYPE.ONLINE_UPLOAD)
                    && (assignment.getlockAtDate() == null || (assignment.getlockAtDate() != null && currentDate.before(assignment.getlockAtDate())))){
                onlineUploadAssignments.add(assignment);
            }
        }

        // if empty, add no assignments assignment. Else add a selection prompt.
        if(onlineUploadAssignments.size() == 0){
            Assignment noAssignments = new Assignment();
            noAssignments.setId(Long.MIN_VALUE);
            noAssignments.setName(context.getString(R.string.noAssignmentsWithFileUpload));
            onlineUploadAssignments.add(noAssignments);
        }

        return onlineUploadAssignments;
    }
}


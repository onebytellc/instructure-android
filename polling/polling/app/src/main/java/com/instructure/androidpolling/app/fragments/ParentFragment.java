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

package com.instructure.androidpolling.app.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.instructure.androidpolling.app.interfaces.UpdatePoll;
import com.instructure.canvasapi.model.Poll;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;

public class ParentFragment extends Fragment implements APIStatusDelegate, UpdatePoll {

    private OnUpdatePollListener callback;
    // Container Activity must implement this interface
    public interface OnUpdatePollListener {
        public void onUpdatePoll(Poll poll, String fragmentTag);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callback = (OnUpdatePollListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnUpdatePollListener");
        }
    }

    public OnUpdatePollListener getCallback() {
        return callback;
    }

    public void loadData() {}

    public void reloadData() {}
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //have to reset butterknife on fragments
        //ButterKnife.reset(this);
    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {
        
    }

    //override this if we want to update the poll information
    @Override
    public void updatePoll(Poll poll) {

    }

    @Override
    public void onNoNetwork() {

    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onCallbackStarted() {

    }
}

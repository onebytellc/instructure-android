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

package com.instructure.parentapp.presenters;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.parentapp.viewinterface.SettingsView;

import java.util.List;

import instructure.androidblueprint.SyncPresenter;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class SettingsPresenter extends SyncPresenter<Student, SettingsView> {

    public SettingsPresenter() {
        super(Student.class);
    }

    @Override
    public void loadData(boolean forceNetwork) {
        if(getViewCallback() != null) {
            UserManager.getStudentsForParentAirwolf(
                    getViewCallback().airwolfDomain(),
                    getViewCallback().parentId(),
                    mStudentsCallback
            );
        }
    }

    @Override
    public void refresh(boolean forceNetwork) {
        onRefreshStarted();
        mStudentsCallback.reset();
        clearData();
        loadData(forceNetwork);
    }

    private StatusCallback<List<Student>> mStudentsCallback = new StatusCallback<List<Student>>(mStatusDelegate){
        @Override
        public void onResponse(retrofit2.Response<List<Student>> response, LinkHeaders linkHeaders, ApiType type) {
            getData().addOrUpdate(response.body());
            if(getViewCallback() != null) {
                getViewCallback().hasStudent(!isEmpty());
            }
        }

        @Override
        public void onFinished(ApiType type) {
            if(getViewCallback() != null) {
                getViewCallback().onRefreshFinished();
                getViewCallback().checkIfEmpty();
            }
        }
    };
}

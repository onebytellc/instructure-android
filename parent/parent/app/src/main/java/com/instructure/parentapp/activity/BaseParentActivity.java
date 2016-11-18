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

package com.instructure.parentapp.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.RevokedTokenResponse;
import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.pandautils.activities.BaseActivity;
import com.instructure.parentapp.R;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Response;

public class BaseParentActivity extends BaseActivity {


    public void handleError(int code, ResponseBody error) {

        if(code == 418) {
            try {
                //parse the message from the response body
                Gson gson = new Gson();
                String json = error.string();
                JsonParser parser = new JsonParser();
                JsonElement mJson =  parser.parse(json);

                RevokedTokenResponse revokedTokenResponse = gson.fromJson(mJson, RevokedTokenResponse.class);
                showRevokedTokenDialog(revokedTokenResponse, this);
            }
            catch (IOException e) {}
        }
    }

    private void showRevokedTokenDialog(final RevokedTokenResponse response, final Context context) {
        new MaterialDialog.Builder(context)
                .title(R.string.revokedTokenErrorTitle)
                .content(R.string.revokedTokenErrorContent, response.shortName)
                .positiveText(R.string.removeStudent)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        removeStudent(APIHelpers.getAirwolfDomain(context), response.parentId, response.studentId, context);
                    }
                })
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .show();
    }

    private void removeStudent(final String airwolfDomain, final String parentId, String studentId, final Context context) {

        UserManager.removeStudentAirwolf(airwolfDomain, parentId, studentId, new StatusCallback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Response<ResponseBody> response, com.instructure.canvasapi2.utils.LinkHeaders linkHeaders, ApiType type) {
                super.onResponse(response, linkHeaders, type);
                //Inform the user that the student has been removed
                Toast.makeText(context, context.getResources().getString(R.string.studentRemoved), Toast.LENGTH_SHORT).show();

                //We want to refresh cache so the main activity can load quickly with accurate information
                UserManager.getStudentsForParentAirwolf(airwolfDomain, parentId, new StatusCallback<List<Student>>() {
                    @Override
                    public void onResponse(Response<List<Student>> response, LinkHeaders linkHeaders, ApiType type) {
                        if (response.body() != null && !response.body().isEmpty()) {
                            //they have students that they are observing, take them to that activity
                            startActivity(StudentViewActivity.createIntent(BaseParentActivity.this, response.body()));
                            overridePendingTransition(0, 0);
                            finish();

                        } else {
                            //Take the parent to the add user page.
                            startActivity(DomainPickerActivity.createIntent(BaseParentActivity.this, false, false, true));
                            overridePendingTransition(0, 0);
                            finish();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void unBundle(@NonNull Bundle extras) {

    }

    @Override
    public boolean applyThemeAutomagically() {
        return false;
    }

}

/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.instructure.androidfoosball.wear;

import android.os.Bundle;
import android.support.wearable.view.CardFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TableCardFragment extends CardFragment {

    private static final String TITLE = "TITLE";
    private static final String STATUS = "STATUS";

    @BindView(R.id.label) TextView mLabel;
    @BindView(R.id.status) TextView mStatus;

    @Override
    public View onCreateContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_table, container, false);
        ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        String label = getArguments().getString(TITLE);
        mLabel.setText(label);

        String count = getArguments().getString(STATUS);
        mStatus.setText(count);
    }

    public static TableCardFragment newInstance(String title, String status) {
        TableCardFragment fragment = new TableCardFragment();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }
}

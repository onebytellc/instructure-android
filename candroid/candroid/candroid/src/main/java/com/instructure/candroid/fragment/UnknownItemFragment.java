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
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.instructure.candroid.R;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.pandautils.utils.Const;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.StreamItem;
import com.instructure.canvasapi.utilities.DateHelpers;

import java.util.Date;

public class UnknownItemFragment extends ParentFragment {

    private TextView title;
    private TextView message;
    private TextView url;
    private TextView html_url;
    private TextView notification_category;
    private TextView updatedDateTime;

    private String fragmentTitle = "";
    private StreamItem streamItem;

    @Override
    public String getFragmentTitle() {
        return "";
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        return FRAGMENT_PLACEMENT.DETAIL;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = getLayoutInflater().inflate(R.layout.unknown_item, container, false);
        title = (TextView)rootView.findViewById(R.id.title);
        message = (TextView)rootView.findViewById(R.id.message);
        url = (TextView)rootView.findViewById(R.id.url);
        html_url = (TextView)rootView.findViewById(R.id.html_url);
        notification_category = (TextView)rootView.findViewById(R.id.notification_category);
        updatedDateTime = (TextView)rootView.findViewById(R.id.updated_date_time);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final String sTitle = streamItem.getTitle(getContext());
        if(!TextUtils.isEmpty(sTitle)) {
            fragmentTitle = streamItem.getTitle(getContext());
            //FIXME: set title now broken
//            setTitle(fragmentTitle);
            title.setText(sTitle);
        } else {
            title.setVisibility(View.GONE);
        }

        final String sMessage = streamItem.getMessage(getContext());
        if(!TextUtils.isEmpty(sMessage)) {
            message.setText(sMessage);
        } else {
            message.setVisibility(View.GONE);
        }

        final String sCategory = streamItem.getNotificationCategory();
        if(!TextUtils.isEmpty(sCategory)) {
            notification_category.setText(sCategory);
        } else {
            notification_category.setVisibility(View.GONE);
        }

        final Date date = streamItem.getUpdatedAtDate();
        if(date != null) {
            updatedDateTime.setText(DateHelpers.getDateTimeString(getContext(), date));
        } else {
            updatedDateTime.setVisibility(View.GONE);
        }
    }

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        if(extras.containsKey(Const.STREAM_ITEM)) {
            streamItem = extras.getParcelable(Const.STREAM_ITEM);
        }
    }

    public static Bundle createBundle(CanvasContext canvasContext, StreamItem item) {
        Bundle extras = createBundle(canvasContext);
        extras.putParcelable(Const.STREAM_ITEM, item);
        return extras;
    }

    @Override
    public boolean allowBookmarking() {
        Navigation navigation = getNavigation();
        //navigation is a course, but isn't in notification list.
        return (navigation != null && navigationContextIsCourse() && !(navigation.getCurrentFragment() instanceof NotificationListFragment));
    }
}

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

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.candroid.R;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.interfaces.OnEventUpdatedCallback;
import com.instructure.candroid.util.Param;
import com.instructure.candroid.util.RouterUtils;
import com.instructure.candroid.view.CanvasWebView;
import com.instructure.canvasapi.api.CalendarEventAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.ScheduleItem;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;
import com.video.ActivityContentVideoViewClient;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit.client.Response;

public class CalendarEventFragment extends ParentFragment {
    // view variables
    private CanvasWebView canvasWebView;
    private View calendarView;

    private TextView date1;
    private TextView date2;
    private TextView address1;
    private TextView address2;

    // model variables
    private ScheduleItem scheduleItem;
    private long scheduleItemId;

    private CanvasCallback<ScheduleItem> scheduleItemCallback;
    private CanvasCallback<ScheduleItem> mDeleteItemCallback;

    private OnEventUpdatedCallback mOnEventUpdatedCallback;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.DETAIL; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.Event);
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        return scheduleItem != null ? scheduleItem.getTitle() : null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.calendar_event_fragment_layout, container, false);
        setupDialogToolbar(rootView);
        initViews(rootView);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if(activity instanceof OnEventUpdatedCallback){
            mOnEventUpdatedCallback = (OnEventUpdatedCallback)activity;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpCallback();
        if (scheduleItem == null) {
            CalendarEventAPI.getCalendarEvent(scheduleItemId, scheduleItemCallback);
        } else {
            populateViews();
        }
    }

    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);
        //If this is an event on the user's personal calendar, give them the option to delete it
        if(scheduleItem != null && scheduleItem.getContextId() == APIHelpers.getCacheUser(getContext()).getId()){
            inflater.inflate(R.menu.calendar_event_menu, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_delete:
                if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                    Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                    return true;
                }
                deleteEvent();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onFragmentActionbarSetupComplete(FRAGMENT_PLACEMENT placement) {
        super.onFragmentActionbarSetupComplete(placement);
        setupTitle(getActionbarTitle());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if(dialog != null && !isTablet(getActivity())) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (canvasWebView != null) {
            canvasWebView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (canvasWebView != null) {
            canvasWebView.onResume();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // View
    ///////////////////////////////////////////////////////////////////////////

    void initViews(View rootView) {

        calendarView = rootView.findViewById(R.id.calendarView);

        date1 = (TextView) rootView.findViewById(R.id.date1);
        date2 = (TextView) rootView.findViewById(R.id.date2);
        address1 = (TextView) rootView.findViewById(R.id.address1);
        address2 = (TextView) rootView.findViewById(R.id.address2);

        canvasWebView = (CanvasWebView) rootView.findViewById(R.id.description);
        canvasWebView.setClient(new ActivityContentVideoViewClient(getActivity()));
        canvasWebView.setCanvasEmbeddedWebViewCallback(new CanvasWebView.CanvasEmbeddedWebViewCallback() {
            @Override
            public void launchInternalWebViewFragment(String url) {
                InternalWebviewFragment.loadInternalWebView(getActivity(), (Navigation) getActivity(), InternalWebviewFragment.createBundle(getCanvasContext(), url, false));
            }

            @Override
            public boolean shouldLaunchInternalWebViewFragment(String url) {
                return true;
            }
        });
        canvasWebView.setCanvasWebViewClientCallback(new CanvasWebView.CanvasWebViewClientCallback() {
            @Override
            public void openMediaFromWebView(String mime, String url, String filename) {
                openMedia(mime, url, filename);
            }

            @Override
            public void onPageStartedCallback(WebView webView, String url) {

            }

            @Override
            public void onPageFinishedCallback(WebView webView, String url) {

            }

            @Override
            public boolean canRouteInternallyDelegate(String url) {
                return RouterUtils.canRouteInternally(null, url, APIHelpers.getDomain(getActivity()), false);
            }

            @Override
            public void routeInternallyCallback(String url) {
                RouterUtils.canRouteInternally(getActivity(), url, APIHelpers.getDomain(getActivity()), true);
            }
        });
    }

    void populateViews() {
        setupTitle(getActionbarTitle());
        String content = scheduleItem.getDescription();

        calendarView.setVisibility(View.VISIBLE);
        canvasWebView.setVisibility(View.GONE);

        if(scheduleItem.isAllDay()) {
            date1.setText(getString(R.string.allDayEvent));
            date2.setText(getFullDateString(scheduleItem.getEndDate()));
        } else {
            //Setup the calendar event start/end times
            if(scheduleItem.getStartDate() != null && scheduleItem.getEndDate() != null && scheduleItem.getStartDate().getTime() != scheduleItem.getEndDate().getTime()) {
                //Our date times are different so we display two strings
                date1.setText(getFullDateString(scheduleItem.getEndDate()));
                String startTime = DateHelpers.getFormattedTime(getContext(), scheduleItem.getStartDate());
                String endTime = DateHelpers.getFormattedTime(getContext(), scheduleItem.getEndDate());
                date2.setText(startTime + " - " + endTime);
            } else {
                date1.setText(getFullDateString(scheduleItem.getStartDate()));
                date2.setVisibility(View.INVISIBLE);
            }
        }

        boolean noLocationTitle = TextUtils.isEmpty(scheduleItem.getLocationName());
        boolean noLocation = TextUtils.isEmpty(scheduleItem.getLocationAddress());

        if(noLocation && noLocationTitle) {
            address1.setText(getString(R.string.noLocation));
            address2.setVisibility(View.INVISIBLE);
        } else {
            if(noLocationTitle) {
                address1.setText(scheduleItem.getLocationAddress());
            } else {
                address1.setText(scheduleItem.getLocationName());
                address2.setText(scheduleItem.getLocationAddress());
            }
        }

        if(!TextUtils.isEmpty(content)){
            canvasWebView.setVisibility(View.VISIBLE);
            canvasWebView.setBackgroundColor(getResources().getColor(R.color.canvasBackgroundLight));
            canvasWebView.formatHTML(content, scheduleItem.getTitle());
        }

        int color;
        if(!(getCanvasContext() instanceof User)){
            color = CanvasContextColor.getCachedColor(getContext(), getCanvasContext());
        } else {
            color = getResources().getColor(R.color.defaultPrimary);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // CallBack
    ///////////////////////////////////////////////////////////////////////////

    public void setUpCallback() {
        scheduleItemCallback = new CanvasCallback<ScheduleItem>(this) {
            @Override
            public void firstPage(ScheduleItem scheduleItem, LinkHeaders linkHeaders, Response response) {
                if (scheduleItem != null) {
                    CalendarEventFragment.this.scheduleItem = scheduleItem;
                    populateViews();
                }
            }
        };

        mDeleteItemCallback = new CanvasCallback<ScheduleItem>(this) {
            @Override
            public void firstPage(ScheduleItem scheduleItem, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()){
                    return;
                }
                showToast(R.string.eventSuccessfulDeletion);
                //Refresh Calendar
                if(mOnEventUpdatedCallback != null && scheduleItem != null){
                    mOnEventUpdatedCallback.onEventSaved(scheduleItem, true);
                }
                getActivity().onBackPressed();
            }
        };
    }

    public String getFullDateString(Date date) {
        if(scheduleItem == null || date == null) {
            return "";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE,");
        String dayOfWeek = dateFormat.format(date);
        String dateString = DateHelpers.getFormattedDate(getContext(), date);

        return dayOfWeek + " " + dateString;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        if (extras.containsKey(Const.SCHEDULE_ITEM)) {
            scheduleItem =  extras.getParcelable(Const.SCHEDULE_ITEM);
            scheduleItemId = scheduleItem.getId();
        } else if (getUrlParams() != null) {
            scheduleItemId = parseLong(getUrlParams().get(Param.EVENT_ID), -1);
        } else {
            scheduleItemId = extras.getLong(Const.SCHEDULE_ITEM_ID, -1);
        }
    }

    public static Bundle createBundle(CanvasContext canvasContext, ScheduleItem scheduleItem) {
        Bundle extras = createBundle(canvasContext);
        extras.putParcelable(Const.SCHEDULE_ITEM, scheduleItem);
        return extras;
    }

    public static Bundle createBundle(CanvasContext canvasContext, long scheduleItemId) {
        Bundle extras = createBundle(canvasContext);
        extras.putLong(Const.SCHEDULE_ITEM_ID, scheduleItemId);
        return extras;
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }

    private void deleteEvent(){
        CalendarEventAPI.deleteCalendarEvent(scheduleItem.getId(), "", mDeleteItemCallback);
    }
}

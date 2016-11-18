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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.instructure.candroid.R;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.util.RouterUtils;
import com.instructure.candroid.view.CanvasWebView;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.pandautils.utils.Const;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.ScheduleItem;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.video.ActivityContentVideoViewClient;
import retrofit.client.Response;


public class SyllabusFragment extends ParentFragment {
    // view variables
    private CanvasWebView detailsWebView;

    // model variables
    private ScheduleItem syllabus;

    // callbacks
    CanvasCallback<Course> syllabusCallback;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.DETAIL; }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.syllabus);
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.syllabus_fragment, container, false);
        detailsWebView = (CanvasWebView) rootView.findViewById(R.id.description);
        detailsWebView.setClient(new ActivityContentVideoViewClient(getActivity()));
        detailsWebView.setCanvasWebViewClientCallback(new CanvasWebView.CanvasWebViewClientCallback() {
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
                return RouterUtils.canRouteInternally(getActivity(), url, APIHelpers.getDomain(getActivity()), false);
            }

            @Override
            public void routeInternallyCallback(String url) {
                RouterUtils.canRouteInternally(getActivity(), url, APIHelpers.getDomain(getActivity()), true);
            }
        });

        detailsWebView.setCanvasEmbeddedWebViewCallback(new CanvasWebView.CanvasEmbeddedWebViewCallback() {
            @Override
            public boolean shouldLaunchInternalWebViewFragment(String url) {
                return true;
            }

            @Override
            public void launchInternalWebViewFragment(String url) {
                InternalWebviewFragment.loadInternalWebView(getActivity(), (Navigation) getActivity(), InternalWebviewFragment.createBundle(getCanvasContext(), url, false));
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupCallbacks();

        if (syllabus == null || syllabus.getDescription() == null) {
            CourseAPI.getCourseWithSyllabus(getCanvasContext().getId(), syllabusCallback);
        } else {
            populateViews();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (detailsWebView != null) {
            detailsWebView.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (detailsWebView != null) {
            detailsWebView.onResume();
        }
    }

    @Override
    public boolean handleBackPressed() {
        return detailsWebView.handleGoBack();
    }

    @Override
    protected ScheduleItem getModelObject() {
        return syllabus;
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        return syllabus != null && !TextUtils.isEmpty(syllabus.getTitle()) ? syllabus.getTitle() : getString(R.string.syllabus);
    }

    ///////////////////////////////////////////////////////////////////////////
    // View
    ///////////////////////////////////////////////////////////////////////////

    void populateViews() {
        if (getActivity() == null || syllabus == null || syllabus.getType() != ScheduleItem.Type.TYPE_SYLLABUS) {
            return;
        }

        setupTitle(getActionbarTitle());
        detailsWebView.formatHTML(syllabus.getDescription(), syllabus.getTitle());
    }


    ///////////////////////////////////////////////////////////////////////////
    // Callbacks
    ///////////////////////////////////////////////////////////////////////////

    private void setupCallbacks() {
        syllabusCallback = new CanvasCallback<Course>(this) {
            @Override
            public void firstPage(Course course, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()){
                    return;
                }
                if (course.getSyllabusBody() != null) {
                    syllabus = new ScheduleItem();
                    syllabus.setType(ScheduleItem.Type.TYPE_SYLLABUS);
                    syllabus.setTitle(course.getName());
                    syllabus.setDescription(course.getSyllabusBody());
                    populateViews();
                }
            }
        };
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        syllabus =  extras.getParcelable(Const.SYLLABUS);
    }

    public static Bundle createBundle(Course course, ScheduleItem syllabus) {
        Bundle bundle = createBundle(course);
        bundle.putParcelable(Const.ADD_SYLLABUS, syllabus);
        bundle.putParcelable(Const.SYLLABUS, syllabus);
        return bundle;
    }

    @Override
    public boolean allowBookmarking() {
        return true;
    }
}

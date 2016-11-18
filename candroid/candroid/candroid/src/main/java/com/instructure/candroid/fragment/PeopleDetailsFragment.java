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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.instructure.candroid.R;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.util.FragUtils;
import com.instructure.candroid.util.Param;
import com.instructure.candroid.view.AutoResizeTextView;
import com.instructure.canvasapi.api.UserAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Enrollment;
import com.instructure.canvasapi.model.Recipient;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.loginapi.login.util.ProfileUtils;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit.client.Response;

public class PeopleDetailsFragment extends OrientationChangeFragment {

    private View rootView;
    private AutoResizeTextView name;
    private CircleImageView userAvatar;
    private TextView bioText;
    private TextView userRole;
    private FrameLayout userBackground;
    private FloatingActionButton composeButton;

    private CardView cardView;
    private RelativeLayout clickContainer;

    private User user;
    private long userId = -1; // used for routing from a url

    private CanvasCallback<User> getCourseUserByIdCallback;

    @Override
    public String getFragmentTitle() {
        return null;
    }

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        if (isTablet(context)) {
            return FRAGMENT_PLACEMENT.DIALOG;
        }
        return FRAGMENT_PLACEMENT.DETAIL;
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        return "";
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(this, true);
    }

    @Override
    public View populateView(LayoutInflater inflater, ViewGroup container) {
        rootView = getLayoutInflater().inflate(R.layout.people_details_fragment_layout, container, false);
        setupDialogToolbar(rootView);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            if(getDialogToolbar() != null && getFragmentPlacement(getContext()) == FRAGMENT_PLACEMENT.DIALOG) {
                getDialogToolbar().setElevation(0);
            } else if(getFragmentPlacement(getContext()) == FRAGMENT_PLACEMENT.DETAIL) {
                getSupportActionBar().setElevation(0);
            }
        }
        clickContainer = (RelativeLayout)rootView.findViewById(R.id.clickContainer);
        name = (AutoResizeTextView) rootView.findViewById(R.id.userName);
        userAvatar = (CircleImageView)rootView.findViewById(R.id.avatar);

        //bio
        bioText = (TextView) rootView.findViewById(R.id.bioText);
        userRole = (TextView) rootView.findViewById(R.id.userRole);
        userBackground = (FrameLayout) rootView.findViewById(R.id.userBackground);

        composeButton = (FloatingActionButton) rootView.findViewById(R.id.compose);

        int[] colors = CanvasContextColor.getCachedColors(getContext(), getCanvasContext());
        composeButton.setColorNormal(colors[0]);
        composeButton.setColorPressed(colors[1]);

        composeButton.setIconDrawable(getResources().getDrawable(R.drawable.ic_cv_send_thin_white_fill));
        composeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseMessageRecipientsFragment.allRecipients.clear();

                Recipient recipient = new Recipient(Long.toString(user.getId()), user.getShortName(), 0, 0, Recipient.recipientTypeToInt(Recipient.Type.person));
                recipient.setCommonCourses(user.getEnrollmentsHash());
                recipient.setAvatarURL(user.getAvatarURL());
                ChooseMessageRecipientsFragment.allRecipients.add(recipient);

                ChooseMessageRecipientsFragment.canvasContext = getCanvasContext();

                Navigation navigation = getNavigation();
                if (navigation != null) {
                    navigation.addFragment(FragUtils.getFrag(ComposeNewMessageFragment.class, ComposeNewMessageFragment.createBundle(getCanvasContext(), true)));
                }
            }
        });
        setupCallbacks();
        if (userId != -1) {
            UserAPI.getCourseUserById(getCanvasContext(), userId, getCourseUserByIdCallback);
        } else {
            setupUserViews();
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            getSupportActionBar().setElevation(Const.ACTIONBAR_ELEVATION);
        }
        super.onDestroyView();
    }

    private void setupUserViews() {
        if(user != null){
            name.setText(user.getName());

            ProfileUtils.configureAvatarView(getContext(), user, userAvatar);

            //show the bio if one exists
            if(!TextUtils.isEmpty(user.getBio()) && !user.getBio().equals("null")) {
                bioText.setVisibility(View.VISIBLE);
                bioText.setText(user.getBio());
            }

            String roles = "";
            for(Enrollment enrollment : user.getEnrollments()) {
                roles += enrollment.getType() + " ";
            }
            userRole.setText(roles);

            userBackground.setBackgroundColor(CanvasContextColor.getCachedColor(getActivity(), getCanvasContext()));
        }

    }

    private void setupCallbacks() {
        getCourseUserByIdCallback =  new CanvasCallback<User>(this) {

            @Override
            public void firstPage(User user, LinkHeaders linkHeaders, Response response) {
                PeopleDetailsFragment.this.user = user;
                setupTitle(getActionbarTitle());
                setupUserViews();
            }
        };
    }

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        if (extras.containsKey(Const.USER)) {
            user = (User) extras.getParcelable(Const.USER);
        } else if (getUrlParams() != null) {
            userId = parseLong(getUrlParams().get(Param.USER_ID), -1);
        }
    }

    public static Bundle createBundle(User user, CanvasContext canvasContext) {
        Bundle extras = createBundle(canvasContext);
        extras.putParcelable(Const.USER, user);
        return extras;
    }

    @Override
    public HashMap<String, String> getParamForBookmark() {
        HashMap<String, String> map = getCanvasContextParams();
        if(user != null) {
            map.put(Param.USER_ID, Long.toString(user.getId()));
        } else if(userId != -1) {
            map.put(Param.USER_ID, Long.toString(userId));
        }
        return map;
    }

    @Override
    public boolean allowBookmarking() {
        return (getCanvasContext() instanceof Course);
    }
}

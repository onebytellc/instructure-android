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
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.instructure.candroid.R;
import com.instructure.candroid.adapter.MultiSelectRecyclerAdapter;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.util.FragUtils;
import com.instructure.canvasapi.api.ConversationAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Conversation;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;

import java.lang.ref.WeakReference;

public class InboxFragment extends OrientationChangeFragment {

    public static final int TAB_INBOX    = 0;
    public static final int TAB_UNREAD   = 1;
    public static final int TAB_SENT     = 2;
    public static final int TAB_ARCHIVED = 3;
    public static final int NUMBER_OF_TABS = 4;

    private int currentTab = TAB_INBOX;

    private int defaultScope = TAB_INBOX;
    private TabLayout tabLayout;

    // views
    private ViewPager viewPager;

    // fragments
    private FragmentPagerDetailAdapter fragmentPagerAdapter;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    @Override
    public boolean allowBookmarking() {
        return false;
    }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.inbox);
    }

    @Override
    public boolean navigationContextIsCourse() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public View populateView(LayoutInflater inflater, ViewGroup container) {
        View rootView = getLayoutInflater().inflate(R.layout.inbox_fragment, container, false);
        setCanvasContext(CanvasContext.emptyUserContext());

        initActionButton(rootView);
        viewPager = (ViewPager) rootView.findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setSaveFromParentEnabled(false); // Prevents a crash with FragmentStatePagerAdapter, when the EditAssignmentFragment is dismissed
        fragmentPagerAdapter = new FragmentPagerDetailAdapter(getChildFragmentManager());
        viewPager.setAdapter(fragmentPagerAdapter);
        tabLayout = (TabLayout) rootView.findViewById(R.id.tabLayout);
        setupTabLayoutColors();
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setTabMode((!isTablet(getContext()) && !isLandscape(getContext()) ? TabLayout.MODE_SCROLLABLE : TabLayout.MODE_FIXED));
        tabLayout.setTabsFromPagerAdapter(fragmentPagerAdapter);
        tabLayout.setOnTabSelectedListener(tabSelectedListener);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(0);
            tabLayout.setElevation(Const.ACTIONBAR_ELEVATION);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Only show tabs as scrollable on phones in portrait
        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getInt(Const.TAB_ID, 0);
        }
        // currentTab can either be save on orientation change or handleIntentExtras (when someone opens a link from an email)
        viewPager.setCurrentItem(currentTab);
    }

    @Override
    public void onDestroyView () {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(Const.ACTIONBAR_ELEVATION);
        }
        super.onDestroyView();
    }

    private TabLayout.OnTabSelectedListener tabSelectedListener = new TabLayout.OnTabSelectedListener() {
        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            viewPager.setCurrentItem(tab.getPosition(), true);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {
            Fragment fragment = fragmentPagerAdapter.getRegisteredFragment(tab.getPosition());
            if (fragment != null) {
                ((MultiSelectRecyclerAdapter.MultiSelectCallback) fragment).endMultiSelectMode();
            }
        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    };

    @Override
    public void onSaveInstanceState (Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        if (viewPager != null) {
            savedInstanceState.putInt(Const.TAB_ID, viewPager.getCurrentItem());
            currentTab = viewPager.getCurrentItem();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // View
    ///////////////////////////////////////////////////////////////////////////
    private void initActionButton(View rootView){
        FloatingActionButton composeButton = (FloatingActionButton) rootView.findViewById(R.id.compose);

        composeButton.setSize(FloatingActionButton.SIZE_NORMAL);
        composeButton.setColorNormal(getResources().getColor(R.color.defaultPrimary));
        composeButton.setColorPressed(getResources().getColor(R.color.defaultPrimaryLight));
        composeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                    Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                    return;
                }

                ChooseMessageRecipientsFragment.allRecipients.clear();
                Bundle bundle = new Bundle();

                Navigation nav = getNavigation();
                if (nav != null) {
                    nav.addFragment(FragUtils.getFrag(ComposeNewMessageFragment.class, bundle));
                }
            }
        });
        composeButton.setIconDrawable(getResources().getDrawable(R.drawable.ic_cv_add_white_thin));
    }

    // Assumes selected item is the one to be updated, does not update a message based on conversationId param
    public void setConversationState(Conversation conversation, Conversation.WorkflowState state){
        if(getMessageListFragment() != null){
            getMessageListFragment().setConversationState(conversation, state);
        }
        //also update the unread state if the state is read
        if(state == Conversation.WorkflowState.READ && getUnreadListFragment() != null) {
            getUnreadListFragment().setConversationState(conversation, state);
        }
    }

    private void setupTabLayoutColors() {
        int color = CanvasContextColor.getCachedColor(getActivity(), getCanvasContext());
        tabLayout.setBackgroundColor(color);
        tabLayout.setTabTextColors(getResources().getColor(R.color.glassWhite), Color.WHITE);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source){
        if(source.isAPI()) {
            hideProgressBar();
        }
    }

    private MessageListFragment getMessageListFragment(){
        if (fragmentPagerAdapter == null) { return null; }
        return (MessageListFragment)fragmentPagerAdapter.getRegisteredFragment(TAB_INBOX);
    }

    private MessageListFragment getUnreadListFragment() {
        if (fragmentPagerAdapter == null) { return null; }
        return (MessageListFragment)fragmentPagerAdapter.getRegisteredFragment(TAB_UNREAD);
    }

    public void updateUnreadTab() {
        if (fragmentPagerAdapter == null) { return; }
        MessageListFragment fragment = (MessageListFragment)fragmentPagerAdapter.getRegisteredFragment(TAB_UNREAD);
        if (fragment != null) {
            fragment.reloadData();
        }
    }

    public void updateArchivedTab() {
        if (fragmentPagerAdapter == null) { return; }
        MessageListFragment fragment = (MessageListFragment)fragmentPagerAdapter.getRegisteredFragment(TAB_ARCHIVED);
        if (fragment != null) {
            fragment.reloadData();
        }
    }

    public void updateMessages(Conversation conversation) {
        if (fragmentPagerAdapter == null) { return; }

        for(int i = 0; i < fragmentPagerAdapter.getCount(); i++) {
            MessageListFragment fragment = (MessageListFragment)fragmentPagerAdapter.getRegisteredFragment(i);
            if (fragment != null) {
                fragment.removeConversation(conversation);
            }
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////
    // Adapter
    ///////////////////////////////////////////////////////////////////////////////////////////
    class FragmentPagerDetailAdapter extends FragmentStatePagerAdapter{
        // http://stackoverflow.com/questions/8785221/retrieve-a-fragment-from-a-viewpager
        SparseArray<WeakReference<Fragment>> registeredFragments;

        public FragmentPagerDetailAdapter(FragmentManager fm) {
            super(fm);
            registeredFragments = new SparseArray<>();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, new WeakReference<>(fragment));
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        @Nullable
        public Fragment getRegisteredFragment(int position) {
            WeakReference<Fragment> weakReference = registeredFragments.get(position);
            if (weakReference != null) {
                return weakReference.get();
            }
            return null;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            Bundle bundle;

            switch (position) {
                case TAB_INBOX:
                    bundle = MessageListFragment.createBundle(getCanvasContext(), true, ConversationAPI.ConversationScope.ALL );
                    break;
                case TAB_UNREAD:
                    bundle = MessageListFragment.createBundle(getCanvasContext(), true, ConversationAPI.ConversationScope.UNREAD );
                    break;
                case TAB_ARCHIVED:
                    bundle = MessageListFragment.createBundle(getCanvasContext(), true, ConversationAPI.ConversationScope.ARCHIVED );
                    break;
                case TAB_SENT:
                    bundle = MessageListFragment.createBundle(getCanvasContext(), true, ConversationAPI.ConversationScope.SENT );
                    break;
                default:
                    bundle = MessageListFragment.createBundle(getCanvasContext(), true, ConversationAPI.ConversationScope.ALL );
                    break;
            }
            fragment = ParentFragment.createFragment(MessageListFragment.class, bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return NUMBER_OF_TABS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            CharSequence title;
            switch (position) {
                case TAB_INBOX:
                    title = getContext().getResources().getString(R.string.inbox);
                    break;
                case TAB_UNREAD:
                    title = getContext().getResources().getString(R.string.unread);
                    break;
                case TAB_ARCHIVED:
                    title = getContext().getResources().getString(R.string.archived);
                    break;
                case TAB_SENT:
                    title = getContext().getResources().getString(R.string.sent);
                    break;
                default:
                    title = getContext().getResources().getString(R.string.inbox);
                    break;
            }
            return title;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);

        if(extras == null){return;}

        if (extras.containsKey(Const.SCOPE)) {
            currentTab =  extras.getParcelable(Const.SCOPE);
        }
    }

    public static Bundle createBundle(CanvasContext canvasContext, boolean isDashboard, ConversationAPI.ConversationScope defaultScope) {
        Bundle extras = createBundle(canvasContext);
        extras.putSerializable(Const.SCOPE, defaultScope);
        extras.putBoolean(Const.IS_DASHBOARD, isDashboard);
        return extras;
    }
}

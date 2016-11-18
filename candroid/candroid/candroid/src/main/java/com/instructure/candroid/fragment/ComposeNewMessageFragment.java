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
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.util.Rfc822Tokenizer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.RecipientEntry;
import com.instructure.candroid.R;
import com.instructure.candroid.adapter.CanvasContextSpinnerAdapter;
import com.instructure.candroid.adapter.NothingSelectedSpinnerAdapter;
import com.instructure.candroid.adapter.RecipientAdapter;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.util.FragUtils;
import com.instructure.candroid.view.CanvasRecipientManager;
import com.instructure.canvasapi.api.ConversationAPI;
import com.instructure.canvasapi.api.CourseAPI;
import com.instructure.canvasapi.api.GroupAPI;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.model.Recipient;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.canvasapi.utilities.LinkHeaders;
import com.instructure.loginapi.login.dialog.GenericDialogStyled;
import com.instructure.pandautils.utils.Const;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import retrofit.client.Response;

public class ComposeNewMessageFragment extends ParentFragment implements GenericDialogStyled.GenericDialogListener {

    // Callbacks
    private CanvasCallback<Response> mConversationCanvasCallback;
    private CanvasCallback<Group[]> mGroupsCallback;
    private CanvasCallback<Course[]> mCoursesCallback;
	private EditText mMessage;
    private EditText mSubject;

    // Course Spinner
    private Spinner mCourseSpinner;
    private Course[] mCourses;
    private Group[] mGroups;
    private CanvasContext mSelectedCourse;

    // Recipient Chips
    private ArrayList<String> mIds;
    private RecipientAdapter mChipsAdapter;
    private RecipientEditTextView mChipsTextView;
    private boolean mIsChooseRecipientsVisable = false;
    private boolean mIsSendEnabled = true;
    private CardView mChipsTextViewWrapper;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {
        if (isTablet(context)) {
            return FRAGMENT_PLACEMENT.DIALOG;
        }
        return FRAGMENT_PLACEMENT.DETAIL;
    }

    @Override
    public String getFragmentTitle() {
        return ChooseMessageRecipientsFragment.getRecipientsTitle(getResources().getString(R.string.noRecipients), getResources().getString(R.string.users));
    }

    @Nullable
    @Override
    protected String getActionbarTitle() {
        return getString(R.string.composeMessage);
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = getLayoutInflater().inflate(R.layout.compose_message, container, false);
        setupDialogToolbar(rootView);
        mMessage = (EditText) rootView.findViewById(R.id.message);
        mSubject = (EditText) rootView.findViewById(R.id.subject);
        mCourseSpinner   = (Spinner)  rootView.findViewById(R.id.course_spinner);
        mChipsTextViewWrapper = (CardView) rootView.findViewById(R.id.recipientWrapper);
        mChipsTextView = (RecipientEditTextView) rootView.findViewById(R.id.recipient);
        mChipsTextView.setTokenizer(new Rfc822Tokenizer());

        mIds = new ArrayList<>();
        mChipsAdapter = new RecipientAdapter(getActivity().getApplicationContext());
        mChipsTextView.setAdapter(mChipsAdapter);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpCallback();
        CourseAPI.getAllFavoriteCourses(mCoursesCallback);
        GroupAPI.getAllFavoriteGroups(mGroupsCallback);

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSubject.getWindowToken(), 0);
    }

    @Override
    public void onFragmentActionbarSetupComplete(FRAGMENT_PLACEMENT placement) {
        super.onFragmentActionbarSetupComplete(placement);
        if(getDialogToolbar() != null && placement == FRAGMENT_PLACEMENT.DIALOG) {
            getDialogToolbar().setBackgroundColor(getResources().getColor(R.color.defaultPrimary));
        } else {
            Navigation navigation = getNavigation();
            if(navigation != null) {
                final int color = getResources().getColor(R.color.defaultPrimary);
                navigation.setActionBarStatusBarColors(color, color);
            }
        }
    }

    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
		//Check to see if we need to add a new title for the action bar after selecting recipients

		//See if we now need a compose button or if we need to remove it.
		getActivity().supportInvalidateOptionsMenu();
	}

    public void setUpCallback(){
        mConversationCanvasCallback = new CanvasCallback<Response>(this) {
            @Override
            public void firstPage(Response response, LinkHeaders linkHeaders, Response redundantResponse) {
                if(!apiCheck()){
                    return;
                }

                //Let the conversation list know to update itself
                Intent intent = new Intent();
                intent.putExtra(Const.CHANGED, true);
                getActivity().setResult(Activity.RESULT_OK, intent);
                showToast(R.string.successSendingMessage);
                ChooseMessageRecipientsFragment.allRecipients.clear();
                //Allow time for the success message to pop up.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Clear out everything so back pressed does not think data is being lost.
                        mMessage.setText("");
                        mChipsTextView.setText("");
                        mSubject.setText("");
                        getActivity().onBackPressed();
                    }
                }, TimeUnit.SECONDS.toMillis(1));

                //close keyboard if it is showing
                View view = getActivity().getCurrentFocus();
                if(view != null) {
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        };

        mCoursesCallback = new CanvasCallback<Course[]>(this) {
            @Override
            public void firstPage(Course[] courses, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()){
                    return;
                }
                mCourses = courses;
                populateCourseSpinnerAdapter();
            }
        };

        mGroupsCallback = new CanvasCallback<Group[]>(this) {
            @Override
            public void firstPage(Group[] groups, LinkHeaders linkHeaders, Response response) {
                if(!apiCheck()){
                    return;
                }
                mGroups = groups;
                populateCourseSpinnerAdapter();
            }
        };
    }

    private void populateCourseSpinnerAdapter() {
        if (mGroups == null || mCourses == null) {
            return;
        }
        final CanvasContextSpinnerAdapter adapter = CanvasContextSpinnerAdapter.newAdapterInstance(getContext(), mCourses, mGroups);
        mCourseSpinner.setAdapter(new NothingSelectedSpinnerAdapter(adapter, R.layout.spinner_item_nothing_selected, getContext()));
        if (mSelectedCourse != null) {
            mCourseSpinner.setOnItemSelectedListener(null); // prevent listener from firing the when selection is placed
            mCourseSpinner.setSelection(adapter.getPosition(mSelectedCourse) + 1, false); //  + 1 is for the nothingSelected position
            courseWasSelected();
        }
        mCourseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) { // position zero is nothingSelected prompt
                    mChipsTextView.removeAllRecipientEntry();
                    CanvasContext canvasContext = adapter.getItem(position - 1); // -1 to account for nothingSelected item
                    mSelectedCourse = canvasContext;
                    courseWasSelected();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void courseWasSelected() {
        mChipsTextViewWrapper.setVisibility(View.VISIBLE);
        getActivity().invalidateOptionsMenu();
        mIsChooseRecipientsVisable = true;
        mChipsAdapter.getCanvasRecipientManager().setCanvasContext(mSelectedCourse);

        ViewTreeObserver vto = mChipsTextView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                populateRecipients();
                ViewTreeObserver obs = mChipsTextView.getViewTreeObserver();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }

        });
    }

    public void populateRecipients() {
        for(Recipient recipient : ChooseMessageRecipientsFragment.allRecipients){
            RecipientEntry recipientEntry = new RecipientEntry(recipient.getIdAsLong(), recipient.getName(), recipient.getStringId(), "", recipient.getAvatarURL(), recipient.getUser_count(), recipient.getItemCount(), true,
                    recipient.getCommonCourses() != null ? recipient.getCommonCourses().keySet() : null,
                    recipient.getCommonGroups() != null ?  recipient.getCommonGroups().keySet() : null);

            mChipsTextView.appendRecipientEntry(recipientEntry);
        }


        ChooseMessageRecipientsFragment.allRecipients.clear();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public boolean handleBackPressed() {
        mMessage.setText("");
        mChipsTextView.setText("");
        mSubject.setText("");
        return super.handleBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSelectedCourse == null) {
            mSelectedCourse = ChooseMessageRecipientsFragment.canvasContext;
        }

        ChooseMessageRecipientsFragment.canvasContext = null;
        setUpCallback();


        dataLossResume(mMessage, Const.DATA_LOSS_COMPOSE_NEW_MESSAGE);
        dataLossAddTextWatcher(mMessage, Const.DATA_LOSS_COMPOSE_NEW_MESSAGE);
    }

    @Override
    public void onPause() {
        dataLossPause(mMessage, Const.DATA_LOSS_COMPOSE_NEW_MESSAGE);
        CanvasRecipientManager.getInstance(getContext()).saveCache();
        CanvasRecipientManager.releaseInstance();
        super.onPause();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Dialog Overrides
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void onPositivePressed() {
        sendMessage(true);
    }

    @Override
    public void onNegativePressed() {
        sendMessage(false);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Data Submission.
    ///////////////////////////////////////////////////////////////////////////

    private boolean isValidNewMessage() {
        if (mSelectedCourse == null) {
            showToast(R.string.noCourseSelected);
            return false;
        } else if (mChipsTextView.getSelectedRecipients().size() == 0) {
            showToast(R.string.messageHasNoRecipients);
            return false;
        } else if ("".equals(mMessage.getText().toString().trim())) {
            showToast(R.string.emptyMessage);
            return false;
        }
        return true;
    }

    void sendMessage(boolean group){
        for(Recipient R: ChooseMessageRecipientsFragment.allRecipients) {
            if(!mIds.contains(R.getStringId())){
                mIds.add(R.getStringId());
            }
        }

        for(RecipientEntry entry : mChipsTextView.getSelectedRecipients()){
            if(!mIds.contains(entry.getDestination())){
                mIds.add(entry.getDestination());
            }
        }

        ConversationAPI.createConversation(mConversationCanvasCallback, mIds, mMessage.getText().toString(), mSubject.getText().toString(), mSelectedCourse.getContextId(), group);
        dataLossDeleteStoredData(Const.DATA_LOSS_COMPOSE_NEW_MESSAGE);
    }

    ///////////////////////////////////////////////////////////////////////////
    // ActionBar
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_send) {
            if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                return true;
            }

            if (isValidNewMessage()) {
                //	Determine whether or not the user intended this as a group message
                if (ChooseMessageRecipientsFragment.isPossibleGroupMessage() || (ChooseMessageRecipientsFragment.allRecipients.size() +  mChipsTextView.getSelectedRecipients().size()) > 1) {
                    GenericDialogStyled newFragment = GenericDialogStyled.newInstance(true, R.string.groupDialogTitle, R.string.groupDialogMessage, R.string.group, R.string.individually, R.drawable.action_about, this);
                    newFragment.setCancelable(true);
                    newFragment.show(getFragmentManager(), "tag");
                } else {
                    //Send the message
                    mIsSendEnabled = false;
                    getActivity().invalidateOptionsMenu();
                    sendMessage(false);
                }
            }
            return true;
        }  else if (item.getItemId() == R.id.menu_choose_recipients) {
            //We want to assume that nothing has been selected yet.
            if (mSelectedCourse == null) {
                showToast(R.string.noCourseSelected);
                return true;
            }
            ChooseMessageRecipientsFragment.allRecipients.clear();
            Navigation navigation = getNavigation();
            if(navigation != null){
                navigation.addFragment(FragUtils.getFrag(ChooseMessageRecipientsFragment.class, createBundle(mSelectedCourse)));
            }

            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        super.createOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_compose_message, menu);
        menu.findItem(R.id.menu_choose_recipients).setVisible(mIsChooseRecipientsVisable);
        menu.findItem(R.id.menu_send).setEnabled(mIsSendEnabled);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
    }

    public static Bundle createBundle(CanvasContext canvasContext, Boolean fromPeople) {
        Bundle bundle = createBundle(canvasContext);
        bundle.putBoolean(Const.FROM_PEOPLE, fromPeople);
        return bundle;
    }

    @Override
    public boolean allowBookmarking() {
        return false;
    }
}

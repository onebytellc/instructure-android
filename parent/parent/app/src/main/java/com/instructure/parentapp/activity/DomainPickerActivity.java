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
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.managers.AccountDomainManager;
import com.instructure.canvasapi2.managers.UserManager;
import com.instructure.canvasapi2.models.AccountDomain;
import com.instructure.canvasapi2.models.ParentResponse;
import com.instructure.canvasapi2.models.Student;
import com.instructure.canvasapi2.models.User;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.canvasapi2.utils.ApiType;
import com.instructure.canvasapi2.utils.LinkHeaders;
import com.instructure.loginapi.login.adapter.AccountAdapter;
import com.instructure.loginapi.login.materialdialogs.CustomDialog;
import com.instructure.loginapi.login.model.Account;
import com.instructure.loginapi.login.util.SoftKeyboardUtil;
import com.instructure.pandautils.utils.Const;
import com.instructure.parentapp.R;
import com.instructure.parentapp.asynctask.LogoutAsyncTask;
import com.instructure.parentapp.util.ApplicationManager;
import com.instructure.parentapp.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Copyright (c) 2016 Instructure. All rights reserved.
 */
public class DomainPickerActivity extends BaseParentActivity implements
        SoftKeyboardUtil.OnSoftKeyBoardHideListener {

    private static final int STUDENT_LOGIN_REQUEST_CODE = 1243;
    private static final String RESULT_REQUESTED = "resultRequested";
    private AutoCompleteTextView mSchool;

    private Button mFinish;
    private TextView mLogout;
    private ImageView mIcon;
    private ListView mListView;

    private AccountAdapter mAccountAdapter;
    private List<AccountDomain> mAccounts = new ArrayList<>();
    private StatusCallback<List<AccountDomain>> mAccountDomainCanvasCallback;
    private StatusCallback<User> mUserCallback;
    private View mFooterView;

    public static Intent createIntent(Context context, boolean hasStudents, boolean resultRequested, boolean isStudent) {
        Intent intent = new Intent(context, DomainPickerActivity.class);
        intent.putExtra(Const.HAS_STUDENTS, hasStudents);
        intent.putExtra(RESULT_REQUESTED, resultRequested);
        intent.putExtra(Const.IS_STUDENT, isStudent);
        return intent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.parentStatusBarColor));
        setContentView(R.layout.activity_student_domain_picker);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupViews();
        setupListeners();
        setupCallbacks();

        AccountDomainManager.getAllAccountDomains(mAccountDomainCanvasCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == STUDENT_LOGIN_REQUEST_CODE && data != null) {
            ArrayList<Student> students = data.getParcelableArrayListExtra(Const.STUDENT);
            if(getIntent().getExtras().getBoolean(RESULT_REQUESTED, false)) {
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra(Const.STUDENT, new ArrayList<Parcelable>(students));
                setResult(RESULT_OK, intent);
                finish();
            } else {
                startActivity(StudentViewActivity.createIntent(DomainPickerActivity.this, students));
                finish();
            }
        }
    }

    private void setupViews() {
        mSchool = (AutoCompleteTextView) findViewById(R.id.school);
        mIcon = (ImageView) findViewById(R.id.addStudentIcon);
        mFinish = (Button) findViewById(R.id.finish);
        mLogout = (TextView) findViewById(R.id.log_out);
        TextView noStudentText = (TextView) findViewById(R.id.noStudentText);
        //We only want to display the logout button and no student text if they don't have any
        //students or if we are in the Main Activity
        if(!hasStudents()) {
            mLogout.setVisibility(View.VISIBLE);
            noStudentText.setVisibility(View.VISIBLE);
        }

        mSchool.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mFinish.performClick();
                return true;
            }
        });

        if(!isStudent()) {
            mLogout.setVisibility(View.GONE);
            noStudentText.setText(getString(R.string.enterURL));
            mSchool.setHint(getString(R.string.exampleURLSimple));
            mIcon.setImageResource(R.drawable.canvas_logo_white);
        }
        // we don't want them to try to go to the next page without actually typing something
        mFinish.setEnabled(false);
        mListView = (ListView) findViewById(R.id.listView);


        if(!getResources().getBoolean(R.bool.isTablet) && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mIcon.setVisibility(View.GONE);
        } else {
            mIcon.setVisibility(View.VISIBLE);
        }
    }

    private boolean hasStudents() {
        return getIntent().getExtras().getBoolean(Const.HAS_STUDENTS);
    }

    private boolean isStudent() {
        return getIntent().getExtras().getBoolean(Const.IS_STUDENT);
    }

    private void setupListeners() {
        //pull up results after 1 letter
        mSchool.setThreshold(2);
        mSchool.addTextChangedListener(mDomainTextWatcher);
        mSchool.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_GO){
                    //select domain
                    mFinish.performClick();
                }
                return false;
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutWarning();
            }
        });

        mFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(mSchool.getText())) {
                    return;
                }

                mListView.setVisibility(View.GONE);

                if (mAccountDomainCanvasCallback != null) {
                    mAccountDomainCanvasCallback.cancel();
                }

                String domain = mSchool.getText().toString().toLowerCase().trim();

                //if there are no periods, append .instructure.com
                if (!domain.contains(".")) {
                    domain += ".instructure.com";
                }

                //URIs need to to start with a scheme.
                if (!domain.startsWith("http://") && !domain.startsWith("https://")) {
                    domain = "https://" + domain;
                }

                if(isStudent()) {
                    UserManager.addStudentToParentAirwolf(
                            APIHelper.getAirwolfDomain(DomainPickerActivity.this),
                            ApplicationManager.getParentId(DomainPickerActivity.this),
                            domain,
                            new StatusCallback<ResponseBody>(mStatusDelegate) {
                                @Override
                                public void onResponse(retrofit2.Response<ResponseBody> response, LinkHeaders linkHeaders, ApiType type) {

                                }

                                @Override
                                public void onFail(Call<ResponseBody> callResponse, Throwable error, retrofit2.Response response) {
                                    if (response.code() == 302) {
                                        Headers headers = response.headers();

                                        if (headers.values("Location") != null) {

                                            mListView.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mListView.setVisibility(View.GONE);
                                                }
                                            });
                                            domainChoiceComplete(headers.values("Location").get(0), mSchool.getText().toString());
                                            return;
                                        }

                                    } else if (response.code() == 401) {
                                        Toast.makeText(DomainPickerActivity.this, getString(R.string.badDomainError), Toast.LENGTH_SHORT).show();
                                    } else if (response.code() == 403) {
                                        //the institution doesn't allow the parent app, let them know
                                        CustomDialog.Builder builder = new CustomDialog.Builder(
                                                DomainPickerActivity.this,
                                                getString(R.string.access_not_enabled),
                                                getString(R.string.dismiss));

                                        final CustomDialog noAccessDialog = builder.build();

                                        noAccessDialog.show();

                                        noAccessDialog.setClickListener(new CustomDialog.ClickListener() {
                                            @Override
                                            public void onConfirmClick() {
                                                noAccessDialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelClick() {
                                                noAccessDialog.dismiss();
                                            }
                                        });
                                    }
                                }
                            }
                    );
                } else {
                    UserManager.authenticateCanvasParentAirwolf(
                            APIHelper.getAirwolfDomain(DomainPickerActivity.this),
                            domain,
                            new StatusCallback<ParentResponse>() {
                                @Override
                                public void onResponse(Response<ParentResponse> response, LinkHeaders linkHeaders, ApiType type) {
                                    super.onResponse(response, linkHeaders, type);
                                }

                                @Override
                                public void onFail(Call<ParentResponse> callResponse, Throwable error, retrofit2.Response response) {
                                    if (response.code() == 302) {
                                        Headers headers = response.headers();

                                        if (headers.values("Location") != null) {

                                            mListView.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mListView.setVisibility(View.GONE);
                                                }
                                            });
                                            domainChoiceComplete(headers.values("Location").get(0), mSchool.getText().toString());
                                            return;
                                        }

                                    } else if (response.code() == 401) {
                                        Toast.makeText(DomainPickerActivity.this, getString(R.string.badDomainError), Toast.LENGTH_SHORT).show();
                                    } else if (response.code() == 403) {
                                        //the institution doesn't allow the parent app, let them know
                                        CustomDialog.Builder builder = new CustomDialog.Builder(
                                                DomainPickerActivity.this,
                                                getString(R.string.access_not_enabled),
                                                getString(R.string.dismiss));

                                        final CustomDialog noAccessDialog = builder.build();

                                        noAccessDialog.show();

                                        noAccessDialog.setClickListener(new CustomDialog.ClickListener() {
                                            @Override
                                            public void onConfirmClick() {
                                                noAccessDialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelClick() {
                                                noAccessDialog.dismiss();
                                            }
                                        });
                                    }
                                }
                            }
                    );
                }
            }
        });
    }

    private TextWatcher mDomainTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (mAccountAdapter != null) {
                mAccountAdapter.getFilter().filter(s);
            }

            if (TextUtils.isEmpty(s)) {
                mListView.setVisibility(View.INVISIBLE);
                //disable button and show logout
                onAccountsRetrieved(mAccounts);
                mFinish.setEnabled(false);
                if(!hasStudents()) {
                    mLogout.setVisibility(View.VISIBLE);
                }
                mFinish.setTextColor(getResources().getColor(R.color.parentButtonTextDisabled));
            } else {
                mListView.setVisibility(View.VISIBLE);
                //Enable button and hide logout
                mFinish.setEnabled(true);
                if(!hasStudents()) {
                    mLogout.setVisibility(View.GONE);
                }
                mFinish.setTextColor(getResources().getColor(R.color.white));;
            }
            removeFooterViews();
        }

        @Override
        public void afterTextChanged(Editable s) {
            setVisibleListItem();
        }
    };

    private void domainChoiceComplete(String domain, String domainForDisplay) {
        if(domain == null) {
            domain = "";
        }

        if(domainForDisplay == null) {
            domainForDisplay = "";
        }

        Intent result = CanvasLoginActivity.createIntent(DomainPickerActivity.this, domain, domainForDisplay, isStudent());
        result.putExtra(Const.DOMAIN, domain);
        result.putExtra(Const.DOMAIN_FOR_DISPLAY, domainForDisplay);
        startActivityForResult(result, STUDENT_LOGIN_REQUEST_CODE);
    }

    @Override
    public void onSoftKeyBoardVisibilityChanged(boolean isVisible) {
        //We want to hide the icon to add space for the listview, always hide icon on landscape
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mIcon.setVisibility(View.GONE);
            return;
        }
        if (isVisible) {
            mIcon.setVisibility(View.GONE);
        } else {
            mIcon.setVisibility(View.VISIBLE);
        }
    }

    private void setupCallbacks() {
        mAccountDomainCanvasCallback = new StatusCallback<List<AccountDomain>>(mStatusDelegate){
            @Override
            public void onResponse(retrofit2.Response<List<AccountDomain>> response, LinkHeaders linkHeaders, ApiType type) {
                boolean isDebuggable = 0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE);

                if(isDebuggable && StatusCallback.isFirstPage(linkHeaders)) {
                    //put these domains first
                    response.body().add(0, createAccountForDebugging("mobiledev.instructure.com"));
                    response.body().add(1, createAccountForDebugging("mobiledev.beta.instructure.com"));
                    response.body().add(2, createAccountForDebugging("mobileqa.instructure.com"));
                    response.body().add(3, createAccountForDebugging("mobileqat.instructure.com"));
                    response.body().add(4, createAccountForDebugging("ben-k.instructure.com"));
                    response.body().add(5, createAccountForDebugging("clare.instructure.com"));
                    response.body().add(6, createAccountForDebugging("mobile-1-canvas.portal2.canvaslms.com"));
                    response.body().add(7, createAccountForDebugging("mobile-2-canvas.portal2.canvaslms.com"));
                    response.body().add(8, createAccountForDebugging("twilson.instructure.com"));
                }

                mAccounts.addAll(response.body());

                if(StatusCallback.moreCallsExist(linkHeaders)) {
                    AccountDomainManager.getAllAccountDomains(mAccountDomainCanvasCallback);
                } else {
                    onAccountsRetrieved(mAccounts);
                }
            }
        };

        mUserCallback = new StatusCallback<User>(mStatusDelegate){
            @Override
            public void onResponse(retrofit2.Response<User> response, LinkHeaders linkHeaders, ApiType type) {
                Toast.makeText(DomainPickerActivity.this, getString(R.string.studentAdded), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private AccountDomain createAccountForDebugging(String domain) {
        AccountDomain account = new AccountDomain();
        account.setDomain(domain);
        account.setName("-- " + domain);
        account.setDistance(null);
        return account;
    }

    private void setupAdapterWithHeaders(BaseAdapter adapter) {
        //Note: headers and footers need to be added before calling setAdapter.
        removeFooterViews();
        mListView.addFooterView(getCanvasHelpView());
        mListView.setAdapter(adapter);

        removeFooterViews();
    }

    public View getCanvasHelpView() {
        if (mFooterView == null) {
            View header = getLayoutInflater().inflate(R.layout.accounts_adapter_item_help, null);
            header.setId(R.id.canvasHelpFooter);
            mFooterView = header;
        }
        return mFooterView;
    }


    private void removeFooterViews() {
        mListView.removeFooterView(getCanvasHelpView());
        for (int i = 0; i < mListView.getFooterViewsCount(); i++) {
            mListView.removeFooterView(getCanvasHelpView());
        }
    }

    public void onAccountsRetrieved(List<AccountDomain> accounts) {

        this.mAccounts = accounts;
        mAccountAdapter = new AccountAdapter(DomainPickerActivity.this, accounts, Account.scrubList(accounts));
        setupAdapterWithHeaders(mAccountAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (view.getId() == R.id.canvasHelpFooter) {
                    //do nothing
                } else {

                    //Make sure the headers are not counting as items for our item clicks
                    final AccountDomain account = (AccountDomain) mAccountAdapter.getItem(Math.abs(position - mListView.getHeaderViewsCount()));
                    if (account != null) {
                        if (mAccountDomainCanvasCallback != null) {
                            mAccountDomainCanvasCallback.cancel();
                        }

                        //get the url to show, need to make an api call
                        String domain = account.getDomain();
                        if(!domain.startsWith("https://")) {
                            domain = "https://" + domain;
                        }

                        if(isStudent()) {
                            UserManager.addStudentToParentAirwolf(
                                    APIHelper.getAirwolfDomain(DomainPickerActivity.this),
                                    ApplicationManager.getParentId(DomainPickerActivity.this),
                                    domain,
                                    new StatusCallback<ResponseBody>(mStatusDelegate) {
                                        @Override
                                        public void onResponse(retrofit2.Response<ResponseBody> response, LinkHeaders linkHeaders, ApiType type, int code) {

                                        }

                                        @Override
                                        public void onFail(Call<ResponseBody> callResponse, Throwable error, retrofit2.Response response) {


                                            if (response != null) {
                                                if (response.code() == 302) {
                                                    Headers headers = response.headers();

                                                    if (headers.values("Location") != null) {
                                                        mListView.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                mListView.setVisibility(View.GONE);
                                                            }
                                                        });
                                                        domainChoiceComplete(headers.values("Location").get(0), account.getDomain());
                                                        return;
                                                    }
                                                } else if (response.code() == 401) {
                                                    Toast.makeText(DomainPickerActivity.this, getString(R.string.badDomainError), Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                            }
                                        }
                                    }
                            );
                        } else {
                            UserManager.authenticateCanvasParentAirwolf(
                                    APIHelper.getAirwolfDomain(DomainPickerActivity.this),
                                    domain,
                                    new StatusCallback<ParentResponse>() {
                                        @Override
                                        public void onResponse(Response<ParentResponse> response, LinkHeaders linkHeaders, ApiType type) {
                                            super.onResponse(response, linkHeaders, type);
                                        }

                                        @Override
                                        public void onFail(Call<ParentResponse> callResponse, Throwable error, retrofit2.Response response) {
                                            if (response.code() == 302) {
                                                Headers headers = response.headers();

                                                if (headers.values("Location") != null) {

                                                    mListView.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mListView.setVisibility(View.GONE);
                                                        }
                                                    });
                                                    domainChoiceComplete(headers.values("Location").get(0), account.getDomain());
                                                    return;
                                                }

                                            } else if (response.code() == 401) {
                                                Toast.makeText(DomainPickerActivity.this, getString(R.string.badDomainError), Toast.LENGTH_SHORT).show();
                                            } else if (response.code() == 403) {
                                                //the institution doesn't allow the parent app, let them know
                                                CustomDialog.Builder builder = new CustomDialog.Builder(
                                                        DomainPickerActivity.this,
                                                        getString(R.string.access_not_enabled),
                                                        getString(R.string.dismiss));

                                                final CustomDialog noAccessDialog = builder.build();

                                                noAccessDialog.show();

                                                noAccessDialog.setClickListener(new CustomDialog.ClickListener() {
                                                    @Override
                                                    public void onConfirmClick() {
                                                        noAccessDialog.dismiss();
                                                    }

                                                    @Override
                                                    public void onCancelClick() {
                                                        noAccessDialog.dismiss();
                                                    }
                                                });
                                            }
                                        }
                                    }
                            );
                        }
                    }
                }
            }
        });

        final int textLength = mSchool.getText().toString().length();
        //Fixes a requirement to have the header hidden and shown at funky times
        if (textLength > 0) {
            //Fixes a rotation issue with no filtered results
            mSchool.setText(mSchool.getText());
            mSchool.setSelection(textLength);
        }

        mListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibleListItem();
            }
        }, 300);

        if (mAccountAdapter != null) {
            mAccountAdapter.notifyDataSetChanged();
        }
    }

    private void logoutWarning(){
        CustomDialog.Builder builder = new CustomDialog.Builder(DomainPickerActivity.this,
                getString(R.string.logout_warning), getString(R.string.logout_yes));
        builder.negativeText(getString(R.string.logout_no));

        final CustomDialog logoutWarningDialog = builder.build();

        logoutWarningDialog.show();

        logoutWarningDialog.setClickListener(new CustomDialog.ClickListener() {
            @Override
            public void onConfirmClick() {
                new LogoutAsyncTask(DomainPickerActivity.this, "").execute();
            }

            @Override
            public void onCancelClick() {
                logoutWarningDialog.dismiss();
            }
        });
    }

    private void setVisibleListItem() {
        mListView.clearFocus();
        mListView.post(new Runnable() {
            @Override
            public void run() {
                mListView.setSelectionAfterHeaderView();
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

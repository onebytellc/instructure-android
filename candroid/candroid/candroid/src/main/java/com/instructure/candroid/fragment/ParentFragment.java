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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.instructure.candroid.R;
import com.instructure.candroid.activity.ParentActivity;
import com.instructure.candroid.adapter.ExpandableRecyclerAdapter;
import com.instructure.candroid.decorations.DividerItemDecoration;
import com.instructure.candroid.decorations.ExpandableGridSpacingDecorator;
import com.instructure.candroid.decorations.GridSpacingDecorator;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.interfaces.ConfigureRecyclerView;
import com.instructure.candroid.util.ApplicationManager;
import com.instructure.candroid.util.FileUtils;
import com.instructure.candroid.util.LoggingUtility;
import com.instructure.candroid.util.Param;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Group;
import com.instructure.canvasapi.model.Tab;
import com.instructure.canvasapi.model.User;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.canvasapi.utilities.CanvasRestAdapter;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandarecycler.BaseRecyclerAdapter;
import com.instructure.pandarecycler.PaginatedRecyclerAdapter;
import com.instructure.pandarecycler.PandaRecyclerView;
import com.instructure.pandarecycler.interfaces.EmptyViewInterface;
import com.instructure.pandarecycler.util.Types;
import com.instructure.pandautils.loaders.OpenMediaAsyncTaskLoader;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.LoaderUtils;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;

abstract public class ParentFragment extends DialogFragment implements
        APIStatusDelegate,
        ConfigureRecyclerView {

    private CanvasContext canvasContext;

    //DIALOG placement should only be used for fragments that will never link to other fragments of a different placement type.
    public enum FRAGMENT_PLACEMENT {MASTER, DETAIL, DIALOG}

    private HashMap<String, String> mUrlParams;

    // region OpenMediaAsyncTaskLoader
    private Bundle openMediaBundle;
    private LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> openMediaCallbacks;
    private ProgressDialog progressDialog;
    // endregion

    // region RecyclerView
    protected long mDefaultSelectedId = -1;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView.ItemDecoration mSpacingDecoration;
    // endregion

    private Toolbar mDialogToolbar;
    private Tab taggedTab;
    private boolean mShouldUpdateTitle = true;
    private OpenMediaAsyncTaskLoader.LoadedMedia mLoadedMedia;

    ///////////////////////////////////////////////////////////////////////////
    // Getters and Setters
    ///////////////////////////////////////////////////////////////////////////

    public abstract String getFragmentTitle();
    public abstract FRAGMENT_PLACEMENT getFragmentPlacement(Context context);
    public abstract boolean allowBookmarking();//Controls overflow menu bookmark item

    public String getTabId() {
        return null;
    }

    public boolean navigationContextIsCourse() {
        if(getCanvasContext() != null && getCanvasContext().getType() == CanvasContext.Type.USER) {
            return false;
        } else {
            return true;
        }
    }

    public LayoutInflater getLayoutInflater() {
        return getActivity().getLayoutInflater();
    }

    public Tab getTaggedTab() {
        return taggedTab;
    }

    // used if we don't want to let the fragment update the title. Currently only used in the CourseModuleProgressionFragment
    public void setShouldUpdateTitle(boolean shouldUpdateTitle) {
        mShouldUpdateTitle = shouldUpdateTitle;
    }
    public boolean shouldUpdateTitle() {
        return mShouldUpdateTitle;
    }
    protected void setupTitle(String title) {
        if(!shouldUpdateTitle()) {
            return;
        }
        if (title == null) { // "" is valid, but prevent null to avoid unnecessary title change flashes.
            return;
        }
        if(mDialogToolbar != null && getFragmentPlacement(getContext())== FRAGMENT_PLACEMENT.DIALOG) {
            mDialogToolbar.setTitle(title);
        } else {
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null) {
                actionBar.setTitle(title);
            }
        }
    }

    protected void setupDialogToolbar(View rootView) {
        mDialogToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        if (mDialogToolbar != null) {
            if (getFragmentPlacement(getActivity()) == FRAGMENT_PLACEMENT.DETAIL) {
                mDialogToolbar.setVisibility(View.GONE);
            } else {
                mDialogToolbar.setVisibility(View.VISIBLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mDialogToolbar.setElevation(Const.ACTIONBAR_ELEVATION);
                }

                int color;
                CanvasContext canvasContext = getCanvasContext();
                if (canvasContext == null || canvasContext.getType() == CanvasContext.Type.USER) {
                    color = getResources().getColor(R.color.defaultPrimary);
                } else {
                    color = CanvasContextColor.getCachedColor(getActivity(), canvasContext);
                }

                mDialogToolbar.setBackgroundColor(color);
                setStatusBarColor(color);

                mDialogToolbar.setNavigationIcon(R.drawable.ic_content_close);
                mDialogToolbar.setNavigationContentDescription(R.string.toolbar_close);
                mDialogToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getActivity().onBackPressed();
                    }
                });

            }
        }
    }

    public Toolbar getDialogToolbar() {
        return mDialogToolbar;
    }


    public void onFragmentActionbarSetupComplete(FRAGMENT_PLACEMENT placement) {
        setupTitle(getActionbarTitle());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(int statusBarColor) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && statusBarColor != Integer.MAX_VALUE) {
            Dialog dialog = getDialog();
            if(dialog != null) {
                dialog.getWindow().setStatusBarColor(statusBarColor);
            }
        }
    }

    public HashMap<String, String> getParamForBookmark() {
        return getCanvasContextParams();
    }

    public HashMap<String, String> getQueryParamForBookmark() {
        return new HashMap<>();
    }

    protected HashMap<String, String> getCanvasContextParams() {
        HashMap<String, String> map = new HashMap<>();
        if(canvasContext instanceof Course || canvasContext instanceof Group) {
            map.put(Param.COURSE_ID, Long.toString(canvasContext.getId()));
        } else if(canvasContext instanceof User) {
            map.put(Param.USER_ID, Long.toString(canvasContext.getId()));
        }
        return map;
    }

    public CanvasContext getCanvasContext() {
        return canvasContext;
    }

    public void setCanvasContext(CanvasContext canvasContext) {
        this.canvasContext = canvasContext;
    }

    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

    public void addBookmarkToMenu(Menu menu, MenuInflater inflater) {
        menu.removeItem(R.id.bookmark);
        if(allowBookmarking()) {
            inflater.inflate(R.menu.bookmark_menu, menu);
        }
    }

    public ActionBar getSupportActionBar() {
        if(getNavigation() != null) {
            return getNavigation().getSupportActionBar();
        } else {
            if(getActivity() instanceof AppCompatActivity) {
                return ((AppCompatActivity) getActivity()).getSupportActionBar();
            }
        }
        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //first saving my state, so the bundle wont be empty.
        dismissProgressDialog(); // Always close.
        LoaderUtils.saveLoaderBundle(outState, openMediaBundle, Const.OPEN_MEDIA_LOADER_BUNDLE);

        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if(args != null) {
            handleIntentExtras(args);
        }
        LoggingUtility.Log(getActivity(), Log.DEBUG, Utils.getFragmentName(this) + " --> On Create");
    }

    public void setRetainInstance(ParentFragment fragment, boolean retain) {
        if(fragment != null) {
            try{
                fragment.setRetainInstance(retain);
            }catch(IllegalStateException e){
                Utils.d("failed to setRetainInstance on fragment: " + e);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LoggingUtility.Log(getActivity(), Log.DEBUG, Utils.getFragmentName(this) + " --> On Create View");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LoggingUtility.Log(getActivity(), Log.DEBUG, Utils.getFragmentName(this) + " --> On Activity Created");

        LoaderUtils.restoreLoaderFromBundle(getActivity().getSupportLoaderManager(), savedInstanceState, getLoaderCallbacks(), R.id.openMediaLoaderID, Const.OPEN_MEDIA_LOADER_BUNDLE);
        if (savedInstanceState != null && savedInstanceState.getBundle(Const.OPEN_MEDIA_LOADER_BUNDLE) != null) {
            showProgressDialog();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDetach() {

        //This could go wrong, but we don't want to crash the app since we are just
        //dismissing the soft keyboard
        try{
            closeKeyboard();
        } catch (Exception e){
            LoggingUtility.Log(getActivity(), Log.DEBUG,
                    "An exception was thrown while trying to dismiss the keyboard: "
                            + e.getMessage());
        }

        //Very important fix for the support library and child fragments.
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        LoggingUtility.Log(getActivity(), Log.DEBUG, Utils.getFragmentName(this) + " --> On Start");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        setupTitle("");
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        LoggingUtility.Log(getActivity(), Log.DEBUG, Utils.getFragmentName(this) + " --> On Resume");
    }

    @Override
    public void onPause() {
        super.onPause();
        LoggingUtility.Log(getActivity(), Log.DEBUG, Utils.getFragmentName(this) + " --> On Pause.");
    }

    public boolean handleBackPressed() {
        return false;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(getActivity() != null) {
            //do not override to add menu items, use createOptionsMenu
            if (mDialogToolbar != null && getFragmentPlacement(getActivity()) == FRAGMENT_PLACEMENT.DIALOG) {
                menu = mDialogToolbar.getMenu();
                mDialogToolbar.setOnMenuItemClickListener(onToolbarOptionsItemSelected);
            }

            menu.clear();

            if (allowBookmarking()) {
                addBookmarkToMenu(menu, getActivity().getMenuInflater());
            }
            createOptionsMenu(menu, getActivity().getMenuInflater());
        }
        super.onPrepareOptionsMenu(menu);
    }

    public void createOptionsMenu(Menu menu, MenuInflater inflater) {
        //Override to add menu items
    }

    public Toolbar.OnMenuItemClickListener onToolbarOptionsItemSelected = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return onOptionsItemSelected(item);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.bookmark) {
            if(!CanvasRestAdapter.isNetworkAvaliable(getContext())) {
                Toast.makeText(getContext(), getContext().getString(R.string.notAvailableOffline), Toast.LENGTH_SHORT).show();
                return true;
            }
            if (getActivity() instanceof Navigation) {
                ((Navigation) getActivity()).addBookmark();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    ///////////////////////////////////////////////////////////////////////////
    // ActionBar
    ///////////////////////////////////////////////////////////////////////////

    /*
        Hide or show the indeterminate progress bar
     */
    public void showProgressBar() {
        // check to see if user exited activity
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        if(activity instanceof ParentActivity){
            ((ParentActivity)activity).showProgress();
        } else {
            getActivity().setProgressBarIndeterminateVisibility(true);
        }
    }


    public void hideProgressBar() {
        // check to see if user exited activity
        final Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        //if we're removing the fragment or have already started the fragment
        //we don't want to worry about changing the margins or removing the indeterminate progress bar.
        //There was a problem sometimes when an fragment that you could no longer see finished an
        //async task and would hide the progress bar and throw off the margins
        if(this.isRemoving() && !this.isResumed()) {
            return;
        }

        if(activity instanceof ParentActivity){
            ((ParentActivity)activity).hideProgress();
        } else {
            getActivity().setProgressBarIndeterminateVisibility(false);
        }
    }

    @Nullable
    protected String getActionbarTitle() {
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides for ContextDelegate
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Context getContext() {
        return getActivity();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides for APIStatusDelegate
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onCallbackStarted() {
        if (ParentActivity.isUIThread()) {
            showProgressBar();
        }
    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {
        if(source.isAPI() && ParentActivity.isUIThread()) {
            hideProgressBar();
        }
    }

    @Override public void onNoNetwork() { }
    
    ///////////////////////////////////////////////////////////////////////////
    // Fragment
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Factory method for constructing a fragment of the specified type.
     *
     * Make sure to use the generic parameters of this method
     * in order to avoid casting.
     *
     *
     * @param fragmentClass The class of fragment to be created.
     * @param params The bundle of extras to be passed to the
     *               fragment's handleIntentExtras() method. This
     *               method is called immediately after the fragment is constructed.
     * @param <Type> The type of fragment that this method will return, in order to
     *               avoid casting.
     * @return The fragment that was constructed.
     */
    public static <Type extends ParentFragment> Type createFragment(Class<Type> fragmentClass, Bundle params) {
        ParentFragment fragment = null;
        try {
            fragment = fragmentClass.newInstance();
        } catch (java.lang.InstantiationException e) {
            LoggingUtility.LogException(null, e);
        } catch (IllegalAccessException e) {
            LoggingUtility.LogException(null, e);
        }
        fragment.setArguments(params);
        fragment.handleIntentExtras(params);
        return (Type)fragment;
    }

    public void loadData() {}

    public void reloadData() {}

    //Fragment-ception fix:
    //Some fragments (currently our AssigmentFragment) have children fragments.
    //In the module progression view pager these child fragments don't get
    //destroyed when the root fragment gets destroyed. Override this function
    //in the appropriate activity to remove child fragments.  For example, in
    //the module progression class we call this function when onDestroyItem
    //is called and it is implemented in the AssigmentFragment class.
    public void removeChildFragments() {}

    protected <I> I getModelObject() { return null; }

    @Override
    public void startActivity(Intent intent) {
        if(getContext() == null){return;}
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if(getContext() == null) {return;}
        super.startActivityForResult(intent, requestCode);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Url Params
    ///////////////////////////////////////////////////////////////////////////

    public HashMap<String, String> getUrlParams() {
        return mUrlParams;
    }

    public long parseLong(String l, long defaultValue) {
        long value;
        try {
            value = Long.parseLong(l);
        } catch (NumberFormatException e) {
            value = defaultValue;
        }
        return value;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    public void handleIntentExtras(Bundle extras) {

        if(extras == null) {
            Utils.d("handleIntentExtras extras was null");
            return;
        }
        Serializable serializable =  extras.getSerializable(Const.URL_PARAMS);
        if (serializable instanceof HashMap) {
            mUrlParams = (HashMap<String, String>) extras.getSerializable(Const.URL_PARAMS);
        }

        if (getUrlParams() != null) {
            mDefaultSelectedId = parseLong(getUrlParams().get(getSelectedParamName()), -1);
        } else if (extras.containsKey(Const.ITEM_ID)) {
            mDefaultSelectedId = extras.getLong(Const.ITEM_ID, -1);
        }

        if(extras.containsKey(Const.TAB)) {
            taggedTab = extras.getParcelable(Const.TAB);
        }

        LoggingUtility.LogBundle(getActivity(), extras);
        setCanvasContext((CanvasContext) extras.getParcelable(Const.CANVAS_CONTEXT));
    }

    public static Bundle createBundle(CanvasContext canvasContext) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext);

        return bundle;
    }

    public static Bundle createBundle(CanvasContext canvasContext, Tab tab) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext);
        bundle.putParcelable(Const.TAB, tab);
        return bundle;
    }

    public static Bundle createBundle(CanvasContext canvasContext, HashMap<String, String> params, HashMap<String, String> queryParams, String url, Tab tab) {
        Bundle bundle = createBundle(canvasContext, tab);
        bundle.putSerializable(Const.URL_PARAMS, params);
        bundle.putSerializable(Const.URL_QUERY_PARAMS, queryParams);
        bundle.putSerializable(Const.TAB_ID, (tab == null ? null : tab.getTabId()));
        bundle.putSerializable(Const.URL, url);
        return bundle;
    }

    public static Bundle createBundle(CanvasContext canvasContext, long itemId) {
        Bundle bundle = createBundle(canvasContext);
        bundle.putLong(Const.ITEM_ID, itemId);
        return bundle;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helpers
    ///////////////////////////////////////////////////////////////////////////

    public Navigation getNavigation() {
        if(getActivity() instanceof Navigation) {
            return (Navigation) getActivity();
        }
        return null;
    }

    public boolean isTablet(Context context) {
        if(context == null) {
            return false;
        }
        return context.getResources().getBoolean(R.bool.isTablet);
    }

    public boolean isLandscape(Context context) {
        if(context == null) {
            return false;
        }
        return context.getResources().getBoolean(R.bool.isLandscape);
    }

    public AppCompatActivity getFragmentActivity() {
        if(getActivity() instanceof AppCompatActivity) {
            return ((AppCompatActivity)getActivity());
        } else {
            return null;
        }
    }

    public boolean apiCheck(){
        return isAdded();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Data Loss Helpers
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Will try to save data if some exits
     * Intended to be used with @dataLossResume()
     * @param editText
     * @param preferenceConstant the constant the fragment is using to store data
     */
    final public void dataLossPause(final EditText editText, final String preferenceConstant) {
        if(editText != null && !TextUtils.isEmpty(editText.getText())) {
            //Data exists in message editText so we want to save it.
            ApplicationManager.getPrefs(getContext()).save(preferenceConstant, editText.getText().toString());
        }
    }

    /**
     * Restores data that may have been lost by navigating
     * @param editText
     * @param preferenceConstant the constant the fragment is using to store data
     * @return if the data was restored
     */
    final public boolean dataLossResume(final EditText editText, final String preferenceConstant) {
        //If we have no text in our editText
        if(editText != null && TextUtils.isEmpty(editText.getText())) {
            //and we have text stored, we can restore that text
            String messageText = ApplicationManager.getPrefs(getContext()).load(preferenceConstant, "");
            if (!TextUtils.isEmpty(messageText)) {
                editText.setText(messageText);
                return true;
            }
        }
        return false;
    }

    /**
     * Will remove any data for a given constant
     * @param preferenceConstant
     */
    final public void dataLossDeleteStoredData(final String preferenceConstant) {
        ApplicationManager.getPrefs(getContext()).remove(preferenceConstant);
    }

    /**
     * A text watcher that will remove any data stored when the user has removed all text
     * @param editText
     * @param preferenceConstant the constant the fragment is using to store data
     */
    final public void dataLossAddTextWatcher(final EditText editText, final String preferenceConstant) {
        if (editText != null) {
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (TextUtils.isEmpty(s.toString())) {
                        dataLossDeleteStoredData(preferenceConstant);
                    }
                }
            });
        }
    }

    public void closeKeyboard() {
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    // region RecyclerView Methods

    // The paramName is used to specify which param should be selected when the list loads for the first time
    protected String getSelectedParamName() {
        return "";
    }

    protected long getDefaultSelectedId() {
        return mDefaultSelectedId;
    }

    protected void setDefaultSelectedId(long id) {
        mDefaultSelectedId = id;
    }

    public void setRefreshing(boolean isRefreshing) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(isRefreshing);
        }
    }

    public void setRefreshingEnabled(boolean isEnabled) {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setEnabled(isEnabled);
        }
    }

    @Override
    public PandaRecyclerView configureRecyclerView(
            View rootView,
            Context context,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId) {
        return configureRecyclerView(rootView, context, baseRecyclerAdapter, swipeRefreshLayoutResId, emptyViewResId, recyclerViewResId, getResources().getString(R.string.noItemsToDisplayShort), false);
    }

    @Override
    public PandaRecyclerView configureRecyclerView(
            View rootView,
            Context context,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            int emptyViewStringResId) {
        return configureRecyclerView(rootView, context, baseRecyclerAdapter, swipeRefreshLayoutResId, emptyViewResId, recyclerViewResId, getResources().getString(emptyViewStringResId), false);
    }

    @Override
    public PandaRecyclerView configureRecyclerView(
            View rootView,
            Context context,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            String emptyViewString) {
        return configureRecyclerView(rootView, context, baseRecyclerAdapter, swipeRefreshLayoutResId, emptyViewResId, recyclerViewResId, emptyViewString, false);
    }

    @Override
    public PandaRecyclerView configureRecyclerView(
            View rootView,
            Context context,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            boolean withDividers) {
        return configureRecyclerView(rootView, context, baseRecyclerAdapter, swipeRefreshLayoutResId, emptyViewResId, recyclerViewResId, getResources().getString(R.string.noItemsToDisplayShort), withDividers);
    }

    @Override
    public PandaRecyclerView configureRecyclerView(
            View rootView,
            Context context,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            String emptyViewString,
            boolean withDivider) {
        EmptyViewInterface emptyViewInterface = (EmptyViewInterface)rootView.findViewById(emptyViewResId);
        PandaRecyclerView recyclerView = (PandaRecyclerView)rootView.findViewById(recyclerViewResId);

        baseRecyclerAdapter.setSelectedItemId(getDefaultSelectedId());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setEmptyView(emptyViewInterface);
        emptyViewInterface.emptyViewText(emptyViewString);
        emptyViewInterface.setNoConnectionText(getString(R.string.noConnection));
        recyclerView.setSelectionEnabled(true);
        recyclerView.setAdapter(baseRecyclerAdapter);
        if(withDivider) {
            recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST));
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(swipeRefreshLayoutResId);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!com.instructure.pandautils.utils.Utils.isNetworkAvailable(getContext())) {
                    mSwipeRefreshLayout.setRefreshing(false);
                } else {
                    baseRecyclerAdapter.refresh();
                }
            }
        });

        return recyclerView;
    }

    @Override
    public void configureRecyclerViewAsGrid(
            View rootView,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId) {
        configureRecyclerViewAsGrid(rootView, baseRecyclerAdapter, swipeRefreshLayoutResId, emptyViewResId, recyclerViewResId, R.string.noItemsToDisplayShort);
    }

    @Override
    public void configureRecyclerViewAsGrid(View rootView, BaseRecyclerAdapter baseRecyclerAdapter, int swipeRefreshLayoutResId, int emptyViewResId, int recyclerViewResId, int emptyViewStringResId, int span) {
        configureRecyclerViewAsGrid(rootView, baseRecyclerAdapter, swipeRefreshLayoutResId, emptyViewResId, recyclerViewResId, emptyViewStringResId, span, null);
    }

    @Override
    public void configureRecyclerViewAsGrid(
            View rootView,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            int emptyViewStringResId,
            Drawable...emptyImage) {
        configureRecyclerViewAsGrid(rootView, baseRecyclerAdapter, swipeRefreshLayoutResId, emptyViewResId, recyclerViewResId, emptyViewStringResId, null, emptyImage);
    }

    @Override
    public void configureRecyclerViewAsGrid(View rootView, BaseRecyclerAdapter baseRecyclerAdapter, int swipeRefreshLayoutResId, int emptyViewResId, int recyclerViewResId, int emptyViewStringResId, View.OnClickListener emptyImageClickListener, Drawable... emptyImage) {
        final int minCardWidth = getResources().getDimensionPixelOffset(R.dimen.course_card_min_width);
        final Display display = getActivity().getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        final int width = size.x;
        final int cardPadding = getResources().getDimensionPixelOffset(R.dimen.card_outer_margin);

        //Sets a dynamic span size based on the min card width we need to display the color chooser.
        final int span;
        if(width != 0) {
            span = width / (minCardWidth + cardPadding);
        } else {
            span = 1;
        }
        configureRecyclerViewAsGrid(rootView, baseRecyclerAdapter, swipeRefreshLayoutResId, emptyViewResId, recyclerViewResId, emptyViewStringResId, span, emptyImageClickListener, emptyImage);
    }

    @Override
    public void configureRecyclerViewAsGrid(
            View rootView,
            final BaseRecyclerAdapter baseRecyclerAdapter,
            int swipeRefreshLayoutResId,
            int emptyViewResId,
            int recyclerViewResId,
            int emptyViewStringResId,
            final int span,
            View.OnClickListener emptyImageListener,
            Drawable...emptyImage) {

        final int cardPadding = getResources().getDimensionPixelOffset(R.dimen.card_outer_margin);
        EmptyViewInterface emptyViewInterface = (EmptyViewInterface)rootView.findViewById(emptyViewResId);
        final PandaRecyclerView recyclerView = (PandaRecyclerView)rootView.findViewById(recyclerViewResId);
        baseRecyclerAdapter.setSelectedItemId(getDefaultSelectedId());
        emptyViewInterface.emptyViewText(emptyViewStringResId);
        emptyViewInterface.setNoConnectionText(getString(R.string.noConnection));

        if(emptyImage != null && emptyImage.length > 0) {
            emptyViewInterface.emptyViewImage(emptyImage[0]);
            if(emptyImageListener != null && emptyViewInterface.getEmptyViewImage() != null) {
                emptyViewInterface.getEmptyViewImage().setOnClickListener(emptyImageListener);
            }
        }

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), span, GridLayoutManager.VERTICAL, false);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(position < recyclerView.getAdapter().getItemCount()) {
                    int viewType = recyclerView.getAdapter().getItemViewType(position);
                    if (Types.TYPE_HEADER == viewType || PaginatedRecyclerAdapter.LOADING_FOOTER_TYPE == viewType) {
                        return span;
                    }
                } else {
                    //if something goes wrong it will take up the entire space, but at least it won't crash
                    return span;
                }
                return 1;
            }
        });

        if(mSpacingDecoration != null) {
            recyclerView.removeItemDecoration(mSpacingDecoration);
        }
        if(baseRecyclerAdapter instanceof ExpandableRecyclerAdapter) {
            mSpacingDecoration = new ExpandableGridSpacingDecorator(cardPadding);
        } else {
            mSpacingDecoration = new GridSpacingDecorator(cardPadding);
        }
        recyclerView.addItemDecoration(mSpacingDecoration);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setEmptyView(emptyViewInterface);
        recyclerView.setAdapter(baseRecyclerAdapter);
        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(swipeRefreshLayoutResId);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!com.instructure.pandautils.utils.Utils.isNetworkAvailable(getContext())) {
                    mSwipeRefreshLayout.setRefreshing(false);
                } else {
                    baseRecyclerAdapter.refresh();
                }
            }
        });

    }
    // endregion

    // region OpenMediaAsyncTaskLoader

    private LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia> getLoaderCallbacks() {
        if (openMediaCallbacks == null) {
            openMediaCallbacks = new LoaderManager.LoaderCallbacks<OpenMediaAsyncTaskLoader.LoadedMedia>() {
                @Override
                public Loader<OpenMediaAsyncTaskLoader.LoadedMedia> onCreateLoader(int id, Bundle args) {
                    showProgressDialog();
                    return new OpenMediaAsyncTaskLoader(getContext(), args);
                }

                @Override
                public void onLoadFinished(Loader<OpenMediaAsyncTaskLoader.LoadedMedia> loader, OpenMediaAsyncTaskLoader.LoadedMedia loadedMedia) {
                    dismissProgressDialog();

                    try {
                        if (loadedMedia.isError()) {
                            if(loadedMedia.getErrorType() == OpenMediaAsyncTaskLoader.ERROR_TYPE.NO_APPS) {
                                mLoadedMedia = loadedMedia;
                                Snackbar.make(getView(), getString(R.string.noAppsShort), Snackbar.LENGTH_LONG)
                                        .setAction(getString(R.string.download), snackbarClickListener)
                                        .setActionTextColor(Color.WHITE)
                                        .show();
                            } else {
                                Toast.makeText(getActivity(), getActivity().getResources().getString(loadedMedia.getErrorMessage()), Toast.LENGTH_LONG).show();
                            }
                        } else if (loadedMedia.isHtmlFile()) {
                            InternalWebviewFragment.loadInternalWebView(getActivity(), (Navigation) getActivity(), loadedMedia.getBundle());
                        } else if (loadedMedia.getIntent() != null) {
                            if(loadedMedia.getIntent().getType().contains("pdf") && !loadedMedia.isUseOutsideApps()){
                                //show pdf with PSPDFkit
                                Uri uri = loadedMedia.getIntent().getData();
                                FileUtils.showPdfDocument(uri, loadedMedia, getContext());
                            } else {
                                getActivity().startActivity(loadedMedia.getIntent());
                            }
                        }
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getActivity(), R.string.noApps, Toast.LENGTH_LONG).show();
                    }
                    openMediaBundle = null; // set to null, otherwise the progressDialog will appear again
                }

                @Override
                public void onLoaderReset(Loader<OpenMediaAsyncTaskLoader.LoadedMedia> loader) {
                }
            };
        }
        return openMediaCallbacks;
    }

    public View.OnClickListener snackbarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                downloadFileToDownloadDir(getContext(), mLoadedMedia.getIntent().getData().getPath(), mLoadedMedia.getIntent().getData().getLastPathSegment());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), R.string.errorOccurred, Toast.LENGTH_LONG).show();
            }
        }
    };

    public void openMedia(String mime, String url, String filename) {
        openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(getCanvasContext(), mime, url, filename);
        LoaderUtils.restartLoaderWithBundle(getActivity().getSupportLoaderManager(), openMediaBundle, getLoaderCallbacks(), R.id.openMediaLoaderID);
    }

    public void openMedia(CanvasContext canvasContext, String url) {
        openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(canvasContext, url);
        LoaderUtils.restartLoaderWithBundle(getActivity().getSupportLoaderManager(), openMediaBundle, getLoaderCallbacks(), R.id.openMediaLoaderID);
    }

    public void openMedia(String mime, String url, String filename, boolean useOutsideApps) {
        openMediaBundle = OpenMediaAsyncTaskLoader.createBundle(getCanvasContext(), mime, url, filename, useOutsideApps);
        LoaderUtils.restartLoaderWithBundle(getActivity().getSupportLoaderManager(), openMediaBundle, getLoaderCallbacks(), R.id.openMediaLoaderID);
    }

    private File downloadFileToDownloadDir(Context context, String url, String filename) throws Exception {
        //They have to download the content first... gross.
        //Download it if the file doesn't exist in the external cache....
        Log.d(Const.OPEN_MEDIA_ASYNC_TASK_LOADER_LOG, "downloadFile URL: " + url);
        File cachedFile = new File(mLoadedMedia.getIntent().getData().getPath());

        File attachmentFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mLoadedMedia.getIntent().getData().getLastPathSegment());

        if (!attachmentFile.exists()) {
            //the cached file is already downloaded, so we'll rename it to be in the download directory
            cachedFile.renameTo(attachmentFile);
        }
        return attachmentFile;
    }

    // ProgressDialog
    private void initProgressDialog() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(true);
        progressDialog.setMessage(getString(R.string.opening));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dismissProgressDialog();
                openMediaBundle = null; // set to null, otherwise the progressDialog will appear again
                if (getActivity() == null) { return; }
                getActivity().getSupportLoaderManager().destroyLoader(R.id.openMediaLoaderID);
            }
        });
        progressDialog.setCanceledOnTouchOutside(true);
    }

    public void showProgressDialog() {
        if (progressDialog == null) {
            initProgressDialog();
        }
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    // endregion


    public void showToast(int stringResId) {
        if(isAdded()) {
            Toast.makeText(getActivity(), stringResId, Toast.LENGTH_SHORT).show();
        }
    }

    public void showToast(String message) {
        if(TextUtils.isEmpty(message)) {
            return;
        }
        if(isAdded()) {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public void showToast(int stringResId, int length) {
        if(isAdded()) {
            Toast.makeText(getActivity(), stringResId, length).show();
        }
    }

    public void showToast(String message, int length) {
        if(TextUtils.isEmpty(message)) {
            return;
        }
        if(isAdded()) {
            Toast.makeText(getActivity(), message, length).show();
        }
    }
}

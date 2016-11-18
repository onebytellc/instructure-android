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

package com.instructure.candroid.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.Toast;


import com.instructure.candroid.R;
import com.instructure.candroid.util.LoggingUtility;
import com.instructure.canvasapi.utilities.APIStatusDelegate;
import com.instructure.canvasapi.utilities.CanvasCallback;
import com.instructure.loginapi.login.util.Utils;
import com.instructure.pandautils.activities.BaseActionBarActivity;
import com.instructure.pandautils.utils.Const;

import java.lang.reflect.Field;


public abstract class ParentActivity extends BaseActionBarActivity implements OnTouchListener, APIStatusDelegate {
	public final static String DEBOUNCE_TAG = "debounce_tag";

	private boolean hasDebounced;

    private BroadcastReceiver uploadStartedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showMessage(getString(R.string.kalturaSubmissionInProgress));
        }
    };

    private BroadcastReceiver uploadFinishedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showMessage(getString(R.string.kalturaSubmissionSuccessful));
        }
    };
    ///////////////////////////////////////////////////////////////////////////
    // LifeCycle
    ///////////////////////////////////////////////////////////////////////////

	@Override
	public void onCreate(Bundle savedInstanceState) {
        // Add progress indicator to action bar
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		LoggingUtility.Log(this, Log.DEBUG, this.getClass().getSimpleName() +" --> On Create");

        try {
            //Fixes an error with devices with a menu key when clicking the overflow menu item.
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            // Ignore
            Utils.e("CAN IGNORE: " + e);
        }
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //first saving my state, so the bundle wont be empty.
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY",  "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onResume() {
        hasDebounced = false;
        super.onResume();

        LoggingUtility.Log(this, Log.DEBUG, this.getClass().getSimpleName() +" --> On Resume");
        //register some broadcast receivers so that we can receive some communication from the upload service and display messages to the user
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(uploadStartedReceiver,
                new IntentFilter(Const.UPLOAD_STARTED));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(uploadFinishedReceiver,
                new IntentFilter(Const.UPLOAD_SUCCESS));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(uploadStartedReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(uploadFinishedReceiver);
    }

    ///////////////////////////////////////////////////////////////////////////
    // ActionBar
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //go back one level
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showProgress() {
        setProgressBarIndeterminateVisibility(true);
    }

    public void hideProgress() {
        setProgressBarIndeterminateVisibility(false);
    }

    public static boolean isUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Required Overrides
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onCallbackStarted() {
        if (isUIThread()) {
            showProgress();
        }
    }

    @Override
    public void onCallbackFinished(CanvasCallback.SOURCE source) {
        if(source.isAPI() && isUIThread()) {
            hideProgress();
        }
    }

    @Override public void onNoNetwork() { }

    @Override
    public Context getContext() {
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Touch Events and Debouncing
    ///////////////////////////////////////////////////////////////////////////
    
    // http://stackoverflow.com/questions/26833242/nullpointerexception-phonewindowonkeyuppanel1002-main
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && "LGE".equalsIgnoreCase(Build.BRAND)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && "LGE".equalsIgnoreCase(Build.BRAND)) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
	
	/**
	 * Starts the activity, but will block repeat starts until
	 * the activity has closed and this activity has been resumed.
	 * Any other debounced activities cannot be started until the
	 * first one completes.
	 * @param intent
	 */
	public final void startDebouncedActivity(Intent intent) {
		if (hasDebounced) {
			return;
		}
		hasDebounced = true;
		startActivity(intent);
	}
	
	/**
	 * Starts the activity, but will block repeat starts until
	 * the activity has closed and this activity has been resumed.
	 * Any other debounced activities cannot be started until the
	 * first one completes.
	 * @param intent
	 */
	public final void startDeboucedActivityForResult(Intent intent, int resultCode) {
		if (hasDebounced) {
			return;
		}
		hasDebounced = true;
		startActivityForResult(intent, resultCode);
	}

    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    /**
     *
     * @param context
     * @param type The class of Activity that will be started.
     * @param layoutId The R layout integer, such as R.layout.example_layout.
     * @return
     */
    protected static Intent createIntent(Context context, Class<? extends ParentActivity> type, int layoutId) {
        Intent intent;

        if(context != null) {
            intent = new Intent(context, type);
        } else { //used for unit tests
            intent = new Intent();
        }

        intent.putExtra(Const.LAYOUT_ID, layoutId);

        //Done to know where we just came from
        if(context != null)
            intent.putExtra(Const.__PREVIOUS, context.getClass().getName());

        if(type != null)
            intent.putExtra(Const.__CURRENT, type.getName());
        return intent;
    }

    protected static Intent createIntent(Context context, Class<? extends ParentActivity> type) {
        Intent intent;

        if(context != null) {
            intent = new Intent(context, type);
        } else { //used for unit tests
            intent = new Intent();
        }

        //Done to know where we just came from
        if(context != null)
            intent.putExtra(Const.__PREVIOUS, context.getClass().getName());

        if(type != null)
            intent.putExtra(Const.__CURRENT, type.getName());
        return intent;
    }

    public void showMessage(String message) {
        if(!TextUtils.isEmpty(message)) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    public void showMessage(Bundle extras) {
        if(extras == null){return;}
        if(extras.containsKey(Const.MESSAGE_TYPE) && extras.containsKey(Const.MESSAGE)) {
            showMessage(extras.getString(Const.MESSAGE));
        }
    }

    /**
     * Reads information out of the intent extras.
     */
    protected void handleIntent() {
        LoggingUtility.LogIntent(this, getIntent());
    }

    public void loadData(){}

    final public boolean isTablet() {
        return getResources().getBoolean(R.bool.isTablet);
    }
    final public boolean isLandscape() {
        return getResources().getBoolean(R.bool.isLandscape);
    }
}


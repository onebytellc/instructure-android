/*
 * Copyright (C) 2016 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.instructure.pandautils.activities;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.instructure.canvasapi2.AppManager;
import com.instructure.canvasapi2.StatusCallback;
import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.CanvasErrorCode;
import com.instructure.pandautils.R;
import com.instructure.pandautils.interfaces.NavigationCallbacks;
import com.instructure.pandautils.utils.Const;
import com.instructure.pandautils.utils.ThemeUtils;
import com.instructure.pandautils.utils.ToolbarColorizeHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import okhttp3.ResponseBody;


public abstract class BaseActivity extends AppCompatActivity {

    private CanvasContext mCanvasContext;

    //Only gets called if not null
    public abstract void unBundle(@NonNull Bundle extras);
    public abstract boolean applyThemeAutomagically();
    public boolean tryUnBundle(){
        return true;
    }

    public void handleError(int code, ResponseBody error) {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(applyThemeAutomagically()) {
            themeStatusBar();
        }

        if(tryUnBundle() && getIntent().getExtras() != null) {
            unBundle(getIntent().getExtras());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(CanvasErrorCode errorCode) {
        if(errorCode != null) {
            handleError(errorCode.getCode(), errorCode.getError());
        }
    }

    public static Bundle createBundle(CanvasContext canvasContext) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.CANVAS_CONTEXT, canvasContext);
        return bundle;
    }

    @Nullable
    public final CanvasContext getCanvasContext() {
        if(mCanvasContext == null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(Const.CANVAS_CONTEXT)) {
            mCanvasContext = getIntent().getExtras().getParcelable(Const.CANVAS_CONTEXT);
        }
        return mCanvasContext;
    }

    public final void setCanvasContext(CanvasContext canvasContext) {
        mCanvasContext = canvasContext;
    }

    public void themeToolbar(@NonNull final Toolbar toolbar) {
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                final int textColor = ThemeUtils.getPrimaryText(BaseActivity.this);
                toolbar.setBackgroundColor(ThemeUtils.getPrimary(BaseActivity.this));
                ToolbarColorizeHelper.colorizeToolbar(toolbar, textColor, BaseActivity.this);
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void themeStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ThemeUtils.darker(ThemeUtils.getPrimary(BaseActivity.this), 0.85F));
        }
    }

    protected void addUpIndicatorToExit(Toolbar toolbar) {
        if(toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.vd_close_white);
            toolbar.setNavigationContentDescription(R.string.close);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Fragment topFragment = getTopFragment();
                    if(topFragment instanceof NavigationCallbacks) {
                        if(((NavigationCallbacks) topFragment).onHandleBackPressed()) {
                            return;
                        }
                    }
                    finish();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Fragment topFragment = getTopFragment();
        if(topFragment instanceof NavigationCallbacks) {
            if(((NavigationCallbacks) topFragment).onHandleBackPressed()) {
                return;
            }
        }

        super.onBackPressed();
    }

    /**
     * Only returns a fragment if one exists and if it was added to the backStack
     * FragmentManager.addToBackStack()
     * @return The top fragment in the backStack or null
     */
    @Nullable
    public Fragment getTopFragment() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            final List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if(!fragments.isEmpty()) {
                return fragments.get(getSupportFragmentManager().getBackStackEntryCount() - 1);
            }
        }
        return null;
    }

    public StatusCallback.StatusDelegate mStatusDelegate = new StatusCallback.StatusDelegate() {
        @Override
        public boolean hasNetworkConnection() {
            return AppManager.hasNetworkConnection(getApplicationContext());
        }
    };

}

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

package com.instructure.pandautils.video;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.instructure.pandautils.R;

/**
 * Uses an existing Activity to handle displaying video in full screen.
 */
public class ActivityContentVideoViewClient implements ContentVideoViewClient {
    private Activity mActivity;
    private View mView;
    private boolean mIsFullScreen;

    public ActivityContentVideoViewClient(Activity activity)  {
        this.mActivity = activity;
    }

    @Override
    public void onShowCustomView(View view) {
        view.setBackgroundColor(Color.BLACK);
        view.setClickable(true);
        Window window = getWindow();
        window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        view.setId(R.id.videoFullScreenView);

        if (isSuperDumb()) {
            performWorkAroundForDumbDevices(false);
        }

        window.addContentView(view,
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER));
        mIsFullScreen = true;
        mView = view;
    }

    @Override
    public void onDestroyContentVideoView() {
        mIsFullScreen = false;
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        if (isSuperDumb()) {
            performWorkAroundForDumbDevices(true);
        }

        ViewGroup rootView = (ViewGroup) window.getDecorView();
        if (rootView != null && rootView.findViewById(R.id.videoFullScreenView) != null) {
            ViewGroup container = (ViewGroup) rootView.findViewById(R.id.videoFullScreenView).getParent();
            container.removeView(mView);
            mView = null;
        }
    }

    private void performWorkAroundForDumbDevices(boolean isShow) {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setWindowAnimations(R.style.DetailsDialogAnimationVideo);
            if (isShow) {
                dialog.show();
            } else {
                dialog.hide();
            }
        }
    }

    private boolean isSuperDumb() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    @Nullable
    private Dialog getDialog() {
        return null;
    }

    private Window getWindow() {
        Window window = mActivity.getWindow();
        Dialog dialog = getDialog();
        if (dialog != null) {
            if (!isSuperDumb()) {
                // Add the video view to the dialog's window, provides a smoother transition to fullscreen and back
                window = dialog.getWindow();
            }
        }
        return window;
    }

    public boolean isFullscreen() {
        return mIsFullScreen;
    }

    @Override
    public View getVideoLoadingProgressView() {

        return null;
    }

    public void toggleHideyBar() {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = mActivity.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        mActivity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }
}

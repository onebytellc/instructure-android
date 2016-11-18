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

import android.view.View;

// Copyright 2013 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

/**
 *  Main callback class used by ContentVideoView.
 *
 *  This contains the superset of callbacks that must be implemented by the embedder.
 *
 *  onShowCustomView and onDestoryContentVideoView must be implemented,
 *  getVideoLoadingProgressView() is optional, and may return null if not required.
 *
 *  The implementer is responsible for displaying the Android view when
 *  {@link #onShowCustomView(View)} is called.
 */
public interface ContentVideoViewClient {
    /**
     * Called when the video view is ready to be shown. Must be implemented.
     * @param view The view to show.
     */
    public void onShowCustomView(View view);

    /**
     * Called when it's time to destroy the video view. Must be implemented.
     */
    public void onDestroyContentVideoView();

    public boolean isFullscreen();

    /**
     * Allows the embedder to replace the view indicating that the video is loading.
     * If null is returned, the default video loading view is used.
     */
    public View getVideoLoadingProgressView();

    /**
     * Allows the embedder to replace the default playback controls by returning a custom
     * implementation. If null is returned, the default controls are used.
     */
    //public ContentVideoViewControls createControls();
}

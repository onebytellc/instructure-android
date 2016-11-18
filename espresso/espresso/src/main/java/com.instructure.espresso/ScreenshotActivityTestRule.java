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
 */

package com.instructure.espresso;

import android.app.Activity;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.FailureHandler;
import android.support.test.espresso.base.DefaultFailureHandler;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Take screenshots in a test with: ScreenShotter.takeScreenshot("hello!", mActivityRule.getActivity());
 **/
public class ScreenshotActivityTestRule<T extends Activity> extends ActivityTestRule<T> {
    /**
     * {@inheritDoc}
     **/
    public ScreenshotActivityTestRule(Class<T> activityClass) {
        super(activityClass, false);
    }

    // http://stackoverflow.com/a/30835689
    @Override
    public Statement apply(final Statement base, final Description description) {
        Espresso.setFailureHandler(new FailureHandler() {
            @Override
            public void handle(Throwable throwable, Matcher<View> matcher) {
                try {
                    // Google's Firebase Test Lab screenshot lib will randomly NPE. yay.
                    // TODO: replace this with the new screenshot library in android support test
                    // once that's released.
                } catch (Exception e) {
                    Log.e(Tag.espresso, "failed to capture screenshot", e);
                }
                new DefaultFailureHandler(InstrumentationRegistry.getTargetContext()).handle(throwable, matcher);
            }
        });
        return super.apply(base, description);
    }
}

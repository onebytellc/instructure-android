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

package com.instructure.candroid.test.util;

import android.os.Bundle;
import android.test.InstrumentationTestCase;
import com.crashlytics.android.Crashlytics;
import com.instructure.candroid.fragment.AssignmentFragment;
import com.instructure.candroid.fragment.DetailedDiscussionFragment;
import com.instructure.candroid.fragment.FileDetailsFragment;
import com.instructure.candroid.fragment.InternalWebviewFragment;
import com.instructure.candroid.fragment.ModuleQuizDecider;
import com.instructure.candroid.fragment.PageDetailsFragment;
import com.instructure.candroid.fragment.ParentFragment;
import com.instructure.candroid.util.Const;
import com.instructure.candroid.util.ModuleUtility;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.ModuleItem;
import com.instructure.canvasapi.model.ModuleObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import io.fabric.sdk.android.Fabric;

@Config(sdk = 17)
@RunWith(RobolectricTestRunner.class)
public class ModuleUtilityTest extends InstrumentationTestCase {

    @Test
    public void testGetFragment_file() {
        String url = "https://mobile.canvas.net/api/v1/courses/222/assignments/123456789";
        ModuleItem moduleItem = new ModuleItem();
        moduleItem.setType("File");
        moduleItem.setId(4567);
        moduleItem.setUrl(url);


        ModuleObject moduleObject = new ModuleObject();
        moduleObject.setId(1234);
        Course course = new Course();

        String expectedUrl = "courses/222/assignments/123456789";

        Bundle expectedBundle = new Bundle();
        expectedBundle.putSerializable(Const.CANVAS_CONTEXT, course);
        expectedBundle.putString(Const.FILE_URL, expectedUrl);
        expectedBundle.putLong(Const.MODULE_ID, moduleObject.getId());
        expectedBundle.putLong(Const.ITEM_ID, moduleItem.getId());


        ParentFragment parentFragment = callGetFragment(moduleItem, course, moduleObject);
        assertNotNull(parentFragment);
        assertEquals(FileDetailsFragment.class, parentFragment.getClass());
        assertEquals(expectedBundle.toString(), parentFragment.getArguments().toString());

        // test module object is null
        moduleObject = null;
        expectedBundle = new Bundle();
        expectedBundle.putSerializable(Const.CANVAS_CONTEXT, course);
        expectedBundle.putString(Const.FILE_URL, expectedUrl);
        parentFragment = callGetFragment(moduleItem, course, moduleObject);
        assertNotNull(parentFragment);
        assertEquals(FileDetailsFragment.class, parentFragment.getClass());
        assertEquals(expectedBundle.toString(), parentFragment.getArguments().toString());

    }

    @Test
    public void testGetFragment_page() {
        String url = "https://mobile.canvas.net/api/v1/courses/222/pages/hello-world";
        ModuleItem moduleItem = new ModuleItem();
        moduleItem.setType("Page");
        moduleItem.setId(4567);
        moduleItem.setUrl(url);

        Course course = new Course();

        Bundle expectedBundle = new Bundle();
        expectedBundle.putSerializable(Const.CANVAS_CONTEXT, course);
        expectedBundle.putString(Const.PAGE_NAME, "hello-world");

        ParentFragment parentFragment = callGetFragment(moduleItem, course, null);
        assertNotNull(parentFragment);
        assertEquals(PageDetailsFragment.class, parentFragment.getClass());
        assertEquals(expectedBundle.toString(), parentFragment.getArguments().toString());
    }

    @Test
    public void testGetFragment_assignment() {
        String url = "https://mobile.canvas.net/api/v1/courses/222/assignments/123456789";
        ModuleItem moduleItem = new ModuleItem();
        moduleItem.setType("Assignment");
        moduleItem.setId(4567);
        moduleItem.setUrl(url);

        Course course = new Course();

        Bundle expectedBundle = new Bundle();
        expectedBundle.putSerializable(Const.CANVAS_CONTEXT, course);
        expectedBundle.putLong(Const.ASSIGNMENT_ID, 123456789);

        ParentFragment parentFragment = callGetFragment(moduleItem, course, null);
        assertNotNull(parentFragment);
        assertEquals(AssignmentFragment.class, parentFragment.getClass());
        assertEquals(expectedBundle.toString(), parentFragment.getArguments().toString());
    }

    @Test
    public void testGetFragment_externalurl_externaltool() {
        String url = "https://instructure.com";
        ModuleItem moduleItem = new ModuleItem();
        moduleItem.setType("ExternalUrl");
        moduleItem.setId(4567);
        moduleItem.setTitle("Hello");
        moduleItem.setHtml_url(url);

        Course course = new Course();

        Bundle expectedBundle = new Bundle();
        expectedBundle.putSerializable(Const.CANVAS_CONTEXT, course);
        expectedBundle.putString(Const.INTERNAL_URL, "https://instructure.com?display=borderless");
        expectedBundle.putString(Const.ACTION_BAR_TITLE, "Hello");
        expectedBundle.putBoolean(Const.AUTHENTICATE, true);
        expectedBundle.putBoolean(com.instructure.pandautils.utils.Const.IS_UNSUPPORTED_FEATURE, true);

        ParentFragment parentFragment = callGetFragment(moduleItem, course, null);
        assertNotNull(parentFragment);
        assertEquals(InternalWebviewFragment.class, parentFragment.getClass());
        assertEquals(expectedBundle.toString(), parentFragment.getArguments().toString());
        // test external tool type

        moduleItem.setType("ExternalTool");
        parentFragment = callGetFragment(moduleItem, course, null);
        assertNotNull(parentFragment);
        assertEquals(InternalWebviewFragment.class, parentFragment.getClass());
        assertEquals(expectedBundle.toString(), parentFragment.getArguments().toString());
    }

    @Test
    public void testGetFragment_subheader() {
        ModuleItem moduleItem = new ModuleItem();
        moduleItem.setType("SubHeader");
        Course course = new Course();

        ParentFragment parentFragment = callGetFragment(moduleItem, course, null);
        assertNull(parentFragment);
    }

    @Test
    public void testGetFragment_quiz() {
        String url = "https://mobile.canvas.net/api/v1/courses/222/quizzes/123456789";
        ModuleItem moduleItem = new ModuleItem();
        moduleItem.setType("Quiz");
        moduleItem.setId(4567);
        moduleItem.setUrl(url);

        Course course = new Course();

        String htmlUrl = "https://mobile.canvas.net/courses/222/quizzes/123456789";
        String apiUrl = "courses/222/quizzes/123456789";
        moduleItem.setHtml_url(htmlUrl);
        Bundle expectedBundle = new Bundle();
        expectedBundle.putSerializable(Const.CANVAS_CONTEXT, course);
        expectedBundle.putString(Const.URL, htmlUrl);
        expectedBundle.putString(Const.API_URL, apiUrl);

        ParentFragment parentFragment = callGetFragment(moduleItem, course, null);
        assertNotNull(parentFragment);
        assertEquals(ModuleQuizDecider.class, parentFragment.getClass());
        assertEquals(expectedBundle.toString(), parentFragment.getArguments().toString());
    }

    @Test
    public void testGetFragment_discussion() {
        String url = "https://mobile.canvas.net/api/v1/courses/222/discussion_topics/123456789";
        ModuleItem moduleItem = new ModuleItem();
        moduleItem.setType("Discussion");
        moduleItem.setId(4567);
        moduleItem.setUrl(url);

        Course course = new Course();

        Bundle expectedBundle = new Bundle();
        expectedBundle.putSerializable(Const.CANVAS_CONTEXT, course);
        expectedBundle.putLong(Const.TOPIC_ID, 123456789);
        expectedBundle.putBoolean(Const.ANNOUNCEMENT, false);
        ParentFragment parentFragment = callGetFragment(moduleItem, course, null);
        assertNotNull(parentFragment);
        assertEquals(DetailedDiscussionFragment.class, parentFragment.getClass());
        assertEquals(expectedBundle.toString(), parentFragment.getArguments().toString());
    }


    private ParentFragment callGetFragment(ModuleItem moduleItem, Course course, ModuleObject moduleObject) {
        Fabric.with(RuntimeEnvironment.application, new Crashlytics());
        return ModuleUtility.getFragment(moduleItem, course, moduleObject);
    }

}

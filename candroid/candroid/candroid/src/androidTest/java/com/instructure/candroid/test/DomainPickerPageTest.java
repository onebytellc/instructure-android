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

package com.instructure.candroid.test;

import android.support.test.runner.AndroidJUnit4;

import com.instructure.candroid.activity.LoginActivity;
import com.instructure.candroid.test.page.PageObjects;
import com.instructure.espresso.ScreenshotActivityTestRule;

import static com.instructure.candroid.test.utils.UserProfile.INVALID_DOMAIN;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DomainPickerPageTest extends PageObjects {
    @Rule
    public ScreenshotActivityTestRule<LoginActivity> mActivityRule =
            new ScreenshotActivityTestRule<>(LoginActivity.class);

    @Test
    /**
     *  TODO: Add test rails reporting
     *  TESTRAILS -- priority: 1, test-id: 221316
     */
    public void displaysDomainPickerPage() {
        domainPickerPage.assertPageObjects();
    }

    @Test
    /**
     *  TODO: Add test rails reporting
     *  TESTRAILS -- priority: 1, test-id: 221317
     */
    public void routesToCanvasGuides() {
        domainPickerPage.openHelpMenu();
        domainPickerPage.clickCanvasGuides();
        domainPickerPage.assertCanvasGuides();
        domainPickerPage.tapNavigateUp();
        domainPickerPage.assertPageObjects();
    }

    @Test
    public void routesToCanvasGuidesFromDomainList() {
        domainPickerPage.enterSchool(INVALID_DOMAIN);
        domainPickerPage.openHelpMenuFromDomainList();
        domainPickerPage.clickCanvasGuides();
        domainPickerPage.assertCanvasGuides();
    }

    @Test

    /**
     *  TODO: Add test rails reporting
     *  TESTRAILS -- priority: 1, test-id: 221318
     */
    public void opensReportProblem() {
        domainPickerPage.openHelpMenu();
        domainPickerPage.clickReportProblem();
        domainPickerPage.assertReportProblem();
        domainPickerPage.assertReportSeverities();
        domainPickerPage.tapReportCancel();
        domainPickerPage.assertPageObjects();
    }

    @Test

    public void opensReportProblemFromDomainList() {
        domainPickerPage.enterSchool(INVALID_DOMAIN);
        domainPickerPage.openHelpMenuFromDomainList();
        domainPickerPage.clickReportProblem();
        domainPickerPage.assertReportProblem();
        domainPickerPage.assertReportSeverities();
        domainPickerPage.tapReportCancel();
    }

    @Test
    public void listsPossibleDomains() {
        domainPickerPage.enterSchool("Utah");
        domainPickerPage.assertDomainListView();
    }
}

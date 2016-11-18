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

package com.instructure.candroid.test.page;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.web.webdriver.Locator;

import com.instructure.candroid.test.utils.User;

import com.instructure.candroid.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


import static android.support.test.espresso.web.assertion.WebViewAssertions.webMatches;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.getText;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webKeys;
import static org.hamcrest.Matchers.containsString;
import static com.instructure.candroid.test.utils.UserProfile.DOMAIN;

public class LoginPage {

    private static String DIV_ERROR = "div.error";
    private static String EMAIL_FIELD_CSS = "input[type=\"email\"]";
    private static String PASSWORD_FIELD_CSS = "input[type=\"password\"]";
    private static String SUBMIT_BUTTON_CSS = "button[type=\"submit\"]";
    private static String PASSWORD_RESET_CSS = "a[class=\"btn btn-large btn-inverse flip-to-back\"]";
    private static String NO_PASSWORD_ERROR = "No password was given";
    private static String INCORRECT_PASSWORD_ERROR = "Incorrect username and/or password";
    private static String AUTHORIZE_BUTTON_CSS = "button[type=\"submit\"]";

    private ViewInteraction toolBarDomain() { return onView(withId(R.id.toolbar)); }
    private ViewInteraction domainTitle() {return onView(withText(DOMAIN)); }

    /************************ Assertion Helpers *******************************/

    public void assertIncorrectUserOrPasswordError() {
        onWebView()
                .withElement(findElement(Locator.CSS_SELECTOR, DIV_ERROR))
                .check(webMatches(getText(), containsString(INCORRECT_PASSWORD_ERROR)));
    }

    public void assertNoPasswordError() {
        onWebView()
                .withElement(findElement(Locator.CSS_SELECTOR, "div.error"))
                .check(webMatches(getText(), containsString(NO_PASSWORD_ERROR)));
    }

    public void assertPageObjects() {
        toolBarDomain().check(matches(isDisplayed()));
        domainTitle().check(matches(isDisplayed()));
    }

    /*********************** UI Action Helpers ********************************/

    public void login(User user) {
        onWebView()
                .withElement(findElement(Locator.CSS_SELECTOR, EMAIL_FIELD_CSS))
                .perform(webKeys(user.email))
                .withElement(findElement(Locator.CSS_SELECTOR, PASSWORD_FIELD_CSS))
                .perform(webKeys(user.password))
                .withElement(findElement(Locator.CSS_SELECTOR, SUBMIT_BUTTON_CSS))
                .perform(webClick());
    }

    public void clickSubmitButton() {
        onWebView()
                .withElement(findElement(Locator.CSS_SELECTOR, SUBMIT_BUTTON_CSS))
                .perform(webClick());
    }

    public void togglePasswordResetForm() {
        onWebView()
                .withElement(findElement(Locator.CSS_SELECTOR, PASSWORD_RESET_CSS))
                .perform(webClick());

    }

    // https://github.com/instructure/canvas-lms/blob/61ac318701fdc21cdad5d3b277e48839c5a009de/app/views/oauth2_provider/confirm_mobile.html.erb#L9
    public void authorizeApp() {
        onWebView()
                .withElement(findElement(Locator.CSS_SELECTOR, AUTHORIZE_BUTTON_CSS))
                .perform(webClick());
    }
}

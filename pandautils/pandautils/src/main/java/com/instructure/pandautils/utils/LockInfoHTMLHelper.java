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

package com.instructure.pandautils.utils;

import android.content.Context;

import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.canvasapi2.models.LockInfo;
import com.instructure.pandautils.R;

import java.util.Date;


public class LockInfoHTMLHelper {

    public static String getLockedInfoHTML(LockInfo lockInfo, Context context, int explanationFirstLine) {
        /*
            Note: if the html that this is going in isn't based on html_wrapper.html (it will have something
            like -- String html = ApiHelper.getAssetsFile(context, "html_wrapper.html");) this will
            not look as good. The blue button will just be a link.
         */
        String buttonTemplate = "<a href = \"%s\" class=\"button blue\"> %s </a>";

        //get the locked message and make the module name bold
        String lockedMessage = "";

        if(lockInfo.getLockedModuleName() != null) {
            lockedMessage = "<p>" + String.format(context.getString(explanationFirstLine), "<b>" + lockInfo.getLockedModuleName() + "</b>") + "</p>";
        }
        if(lockInfo.getModulePrerequisiteNames().size() > 0) {
            //we only want to add this text if there are module completion requirements
            lockedMessage += context.getString(R.string.mustComplete) + "<ul>";
            for(int i = 0; i < lockInfo.getModulePrerequisiteNames().size(); i++) {
                lockedMessage +=  "<li>" + lockInfo.getModulePrerequisiteNames().get(i) + "</li>";  //"&#8226; "
            }
            lockedMessage += "</ul>";
        }

        //check to see if there is an unlocked date
        if(lockInfo.getUnlockAt() != null && lockInfo.getUnlockAt().after(new Date())) {
            String unlocked = DateHelpers.getDateTimeString(context, lockInfo.getUnlockAt());
            //If there is an unlock date but no module then the assignment is locked
            if(lockInfo.getContextModule() == null){
                lockedMessage = "<p>" + context.getString(R.string.lockedAssignmentNotModule) + "</p>";
            }
            lockedMessage += context.getString(R.string.unlockedAt) + "<ul><li>" + unlocked + "</li></ul>";
        }
        //add the second line telling user how to check requirements
        //lockedMessage += "<p>" + context.getResources().getString(explanationSecondLine) + "</p>";
        //make sure we know what the protocol is (http or https)

        if (lockInfo.getContextModule() != null) {
            //create the url to modules for this course
            String url = APIHelpers.loadProtocol(context) + "://" + APIHelpers.getDomain(context) + "/courses/" + lockInfo.getContextModule().getContextId() + "/modules";
            //create the button and link it to modules
            String linkToModules = "<center>" + String.format(buttonTemplate, url, context.getResources().getString(R.string.goToModules)) + "</center>";

            lockedMessage += linkToModules;
        }
        return lockedMessage;
    }
}

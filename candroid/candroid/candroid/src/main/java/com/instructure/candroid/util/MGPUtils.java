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

package com.instructure.candroid.util;

import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Enrollment;


public class MGPUtils {

    public static boolean isAllGradingPeriodsShown(Course course) {
        for (Enrollment enrollment : course.getEnrollments()) {
            if (enrollment.isStudent() && enrollment.isMultipleGradingPeriodsEnabled()) {
                //First check to see if a current Grading period actually exists
                if(enrollment.getCurrentGradingPeriodTitle() != null && !enrollment.getCurrentGradingPeriodTitle().equals("")) {
                    //if it does exist, we want to just show the grade for the current period
                    return true;
                } else {
                    //Otherwise we want to assume that since MGP = true, but there are no grading periods,
                    //so we must treat this as the "all grading periods" period.
                    return enrollment.isTotalsForAllGradingPeriodsOption();
                }
            }
        }

        //if the if never gets hit we always return true so that we show the grade based
        //only on other boolean values
        return true;
    }

}

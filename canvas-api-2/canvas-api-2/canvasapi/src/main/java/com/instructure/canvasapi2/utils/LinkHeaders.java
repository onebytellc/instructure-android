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

package com.instructure.canvasapi2.utils;


public class LinkHeaders {
    public String prevUrl;
    public String nextUrl;
    public String lastUrl;
    public String firstUrl;

    @Override
    public String toString() {
        return new StringBuilder()
                .append("PREV:  " + prevUrl)
                .append("\n")
                .append("NEXT:  " + nextUrl)
                .append("\n")
                .append("LAST:  " + lastUrl)
                .append("\n")
                .append("FIRST: " + firstUrl)
                .toString();

    }
}

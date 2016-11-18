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

package com.instructure.pandautils.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.instructure.canvasapi2.models.CanvasContext;
import com.instructure.canvasapi2.models.Course;
import com.instructure.canvasapi2.models.DiscussionEntry;
import com.instructure.canvasapi2.models.DiscussionTopic;
import com.instructure.canvasapi2.models.DiscussionTopicHeader;
import com.instructure.canvasapi2.utils.APIHelper;
import com.instructure.pandautils.R;
import com.instructure.pandautils.models.FormatHtmlObject;
import com.instructure.pandautils.utils.CanvasContextColor;
import com.instructure.pandautils.utils.DiscussionEntryHTMLHelper;
import com.instructure.pandautils.utils.ThemeUtils;
import com.instructure.pandautils.views.CanvasWebView;

import java.util.HashMap;


public class FormatHtmlLoader extends AsyncTaskLoader<FormatHtmlObject> {

    final DiscussionEntry discussionEntry;
    final DiscussionTopic discussionTopic;
    final CanvasContext canvasContext;
    final DiscussionTopicHeader discussionTopicHeader;
    final boolean isAnnouncement;

    public FormatHtmlLoader(Context context,
                            CanvasContext canvasContext,
                            DiscussionEntry discussionEntry,
                            DiscussionTopic discussionTopic,
                            DiscussionTopicHeader discussionTopicHeader,
                            boolean isAnnouncement) {
        super(context);
        this.discussionEntry = discussionEntry;
        this.discussionTopic = discussionTopic;
        this.canvasContext = canvasContext;
        this.discussionTopicHeader = discussionTopicHeader;
        this.isAnnouncement = isAnnouncement;
    }

    @Override
    public FormatHtmlObject loadInBackground() {

        String html = APIHelper.getAssetsFile(getContext(), "discussion_html_header.html");

        final String colorString = CanvasContextColor.getColorStringFromInt(ThemeUtils.getAccent(getContext()), true);

        HashMap<Long, Integer> ratingsMap = discussionTopic.getEntryRatings();
        int rating = 0;
        if (ratingsMap != null) {
            if (ratingsMap.containsKey(discussionEntry.getId())) {
                rating = ratingsMap.get(discussionEntry.getId());
            }
        }

        html = html.replaceAll("#0076A3", colorString);//replaces default canvas colors

        boolean shouldAllowLiking = shouldAllowLiking(canvasContext, discussionTopicHeader);

        html += DiscussionEntryHTMLHelper.getHTML(
                discussionEntry,
                getContext(),
                -1,
                getContext().getString(R.string.deleted),
                colorString,
                discussionTopic.isForbidden(),
                canReplyHack(discussionTopicHeader, canvasContext),
                rating,
                getLikeString(getContext(), shouldAllowLiking, discussionEntry),
                discussionTopicHeader.getDiscussionSubentryCount());

        //Now get children.
        for (int i = 0; i < discussionEntry.getReplies().size(); i++) {
            rating = 0;
            if (ratingsMap != null) {
                if (ratingsMap.containsKey(discussionEntry.getReplies().get(i).getId())) {
                    rating = ratingsMap.get(discussionEntry.getReplies().get(i).getId());
                }
            }
            boolean shouldAllowRatingForItem = shouldAllowLiking(canvasContext, discussionTopicHeader);

            html += DiscussionEntryHTMLHelper.getHTML(
                    discussionEntry.getReplies().get(i),
                    getContext(),
                    i,
                    getContext().getString(R.string.deleted),
                    colorString,
                    !isAnnouncement && shouldAllowLiking(canvasContext, discussionTopicHeader),
                    canReplyHack(discussionTopicHeader, canvasContext),
                    rating,
                    getLikeString(getContext(), shouldAllowRatingForItem, discussionEntry.getReplies().get(i)),
                    discussionEntry.getReplies().get(i).getReplies().size());
        }

        html = CanvasWebView.applyWorkAroundForDoubleSlashesAsUrlSource(html);
        html += APIHelper.getAssetsFile(getContext(), "discussion_html_footer.html");

        return new FormatHtmlObject(html, null);
    }

    public static boolean shouldAllowLiking(CanvasContext canvasContext, DiscussionTopicHeader header) {
        if (header.isAllowRating()) {
            if (header.isOnlyGradersCanRate()) {
                if (canvasContext.getType() == CanvasContext.Type.COURSE) {
                    return ((Course) canvasContext).isTeacher() || ((Course) canvasContext).isTA();
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public static String getLikeString(Context context, boolean shouldAllowLiking, DiscussionEntry discussionEntry) {
        if(!shouldAllowLiking || discussionEntry.getId() == 0) return "";

        if(discussionEntry.getRatingSum() == 0) {
            return String.format("<div class=\"likes\">%s</div>", context.getString(R.string.like));
        }

        String like = context.getString(discussionEntry.getRatingSum() == 1  ? R.string.like : R.string.likes);
        return String.format("<div class=\"likes\">%s</div>", like + " (" + discussionEntry.getRatingSum() + ")");
    }

    public static boolean canReplyHack(DiscussionTopicHeader header, CanvasContext canvasContext) {
        //TODO: replace with discussionTopicHeader.getPermissions().canReply() when api is complete
            /*
                There are three related scenarios in which we don't want users to be able to reply.
                   so we check that none of these conditions exist
                1.) The discussion is locked for an unknown reason.
                2.) It's locked due to a module/etc.
                3.) User is an Observer in a course.
            */

        return !header.isLocked() && (header.getLockInfo() == null || header.getLockInfo().isEmpty()) && (canvasContext.getType() != CanvasContext.Type.COURSE || !((Course) canvasContext).isObserver());
    }
}

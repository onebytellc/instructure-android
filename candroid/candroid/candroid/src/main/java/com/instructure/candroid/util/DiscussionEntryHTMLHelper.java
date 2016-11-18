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

import android.content.Context;

import com.instructure.candroid.R;
import com.instructure.candroid.fragment.ProfileFragment;
import com.instructure.canvasapi.model.DiscussionAttachment;
import com.instructure.canvasapi.model.DiscussionEntry;
import com.instructure.canvasapi.utilities.APIHelpers;
import com.instructure.canvasapi.utilities.DateHelpers;
import com.instructure.loginapi.login.util.ProfileUtils;
import com.instructure.pandautils.utils.Const;

public class DiscussionEntryHTMLHelper {
    private static final String assetUrl = "file:///android_asset";

    private static String getContentHTML(DiscussionEntry discussionEntry, String message) {
        if (message == null || message.equals("null")) {
            return "";
        } else {
            StringBuilder html = new StringBuilder();
            html.append("<div class=\"width\">");
            html.append(message);

            for(DiscussionAttachment attachment : discussionEntry.getAttachments()){
                if (attachment.shouldShowToUser() && !discussionEntry.isDeleted()) {
                    html.append("<div class=\"nowrap\">");
                    html.append(String.format("<img class=\"attachmentimg\" src=\"%s/conversation_attachment.png\" /> <a class=\"attachmentlink\" href=\"%s\">%s</a>", assetUrl, attachment.getUrl(), attachment.getDisplayName()));
                    html.append("</div>");
                }
            }
            html.append("</div>");
            return html.toString();
        }
    }

    private static String getClickListener(int index) {
        if (index == -1) {
            return "";
        } else {
            return " onClick=\"onPressed('" + index + "')\"";
        }
    }

    public static String getUnreadAndTotalLabelHtml(DiscussionEntry discussionEntry, boolean isContainer) {
        StringBuilder toReturn = new StringBuilder();
        if (discussionEntry.getUnreadChildren() > 0) {
            toReturn.append(String.format("<li class=\"unread\" id=\"unread_%d\">%d</li>", discussionEntry.getId(), discussionEntry.getUnreadChildren()));
        }
        if (discussionEntry.getTotalChildren() > 0) {
            toReturn.append(String.format("<li class=\"total\" id =\"total_%d\">%d</li>",discussionEntry.getId(), discussionEntry.getTotalChildren()));
        }
        if(discussionEntry.isUnread()){
            toReturn.append(String.format("<li class=\"isUnread\" id =\"isUnread_%d\">unread</li>", discussionEntry.getId()));
        }
        if(toReturn.length() == 0){
            return "";
        }else{
            if (isContainer) {
                return String.format("<ul class=\"container\" id=\"count_%d\">%s</ul>", discussionEntry.getId(), toReturn.toString());
            } else {
                return toReturn.toString();
            }
        }
    }


    public static String getHTML(DiscussionEntry discussionEntry, Context context, int index, String deleted, String colorString, boolean shouldAllowRating, int currentRatingForUser, String likeString) {
        return getHTML(discussionEntry, context, index, deleted, colorString, false, shouldAllowRating, currentRatingForUser, likeString);
    }
    /*
     ********************************
     ***** Constants To Replace *****
     ********************************
     *
     * __LISTENER_HTML__
     * __AVATAR_URL__
     * __TITLE__
     * __DATE__
     * __UNREAD_TOTAL_LABELS_HTML__
     * __CONTENT_HTML__
     * __CLASS__
     * __STATUS__
     * __HEADER_ID__
     */
    public static String getHTML(DiscussionEntry discussionEntry, Context context, int index, String deleted, String colorString, boolean isForbidden, boolean shouldAllowRating, int currentRatingForUser, String likeString) {
        String unread = getUnreadAndTotalLabelHtml(discussionEntry, true);
        String listener = getClickListener(index);
        String avatarurl = "";
        String title = "";
        String date = "";
        String message = discussionEntry.getMessage(deleted);
        String content = getContentHTML(discussionEntry,message);
        String status = "read_message";
        String classString = "parent";
        String displayInitials = "";
        String borderString = "";
        String ratePostImage = "";
        String likesId = "likes_" + discussionEntry.getId();

        String forbiddenHtml = isForbidden ? getForbiddenHtml(context.getString(R.string.forbidden)) : "";

        if (index >= 0) {
            classString = "child";
        }
        if (discussionEntry.isUnread()) {
            status = "unread_message";
        }
        if (shouldAllowRating && discussionEntry.getId() > 0) {
            if(currentRatingForUser == 1) {
                ratePostImage = "<img onclick=\"javascript:onRatePressed('" + discussionEntry.getId()  + "')\"  src=\"file:///android_res/drawable/ic_thumbs_up_selected.png\" id=\"imgRate_" + discussionEntry.getId() + "\"/>";
            } else {
                ratePostImage = "<img onclick=\"javascript:onRatePressed('" + discussionEntry.getId() + "')\"  src=\"file:///android_res/drawable/ic_thumbs_up_unselected.png\" id=\"imgRate_" + discussionEntry.getId() + "\"/>";
            }
        }

        if (!shouldAllowRating) {
            //if we're not showing the rating button we don't want to show the like string either
            likeString = "";
        }
        if (discussionEntry.isDeleted()) {
            avatarurl = "background-image:url(ic_cv_student_fill.png);";
            title = deleted;
        } else {
            if (discussionEntry.getAuthor() != null && discussionEntry.getAuthor().getAvatarUrl() != null) {
                //large discussions slow down and sometimes get an ANR due to getting so many avatar images. This
                //limits to getting 30 profile avatars and then it will just use the initials
                if(!isEmptyImage(discussionEntry.getAuthor().getAvatarUrl()) && index < 30){
                    avatarurl = "background-image:url(" +discussionEntry.getAuthor().getAvatarUrl() +");";
                    borderString = "border: 0px solid " + colorString + ";";
                }else {
                    avatarurl = "background:" + ProfileUtils.getUserHexColorString(discussionEntry.getAuthor().getDisplayName()) +";";
                    displayInitials = ProfileUtils.getUserInitials(discussionEntry.getAuthor().getDisplayName());
                }
            }else {
                avatarurl = "background-image:url(ic_cv_student_fill.png);";
                borderString = "border: 0px solid " + colorString + ";";
            }

            if (discussionEntry.getAuthor() != null && discussionEntry.getAuthor().getDisplayName() != null) {
                title = discussionEntry.getAuthor().getDisplayName();
            } else if (discussionEntry.getDescription() != null) {    //Graded discussion.
                title = discussionEntry.getDescription();
            }
            //if title is still null set it to an empty string
            if(title == null) {
                title = "";
            }
        }

        if (discussionEntry.getLastUpdated() != null) {
            //don't display seconds
            String updated = DateHelpers.getDateTimeString(context, discussionEntry.getLastUpdated());
            date = updated;
        }

        return (APIHelpers.getAssetsFile(context, "discussion_html_template.html")
                .replace("__BORDER_STRING__", borderString)
                .replace("__LISTENER_HTML__", listener)
                .replace("__AVATAR_URL__", avatarurl)
                .replace("__TITLE__", title)
                .replace("__DATE__", date)
                .replace("__RATE__", ratePostImage)
                .replace("__NUM_LIKES_ID__", likesId)
                .replace("__LIKE_STRING__", likeString)
                .replace("__ENTRY_ID__", Long.toString(discussionEntry.getId()))
                .replace("__UNREAD_TOTAL_LABELS_HTML__", unread)
                .replace("__CONTENT_HTML__", content)
                .replace("__CLASS__", classString)
                .replace("__STATUS__", status)
                .replace("__FORBIDDEN__", forbiddenHtml)
                .replace("__HEADER_ID__", Long.toString(discussionEntry.getId()))
                .replace("__USER_ID__", Long.toString(discussionEntry.getUserId())))
                .replace("__USER_INITIALS__", displayInitials);
    }

    /*
     * Displays text to the user when they have to post before viewing replies
     */
    private static String getForbiddenHtml(String forbiddenText) {
        return String.format("<div class=\"forbidden\"><p>%s</p></div>", forbiddenText);
    }

    public static boolean isEmptyImage(String avatarURL){
        if(avatarURL.contains(ProfileFragment.noPictureURL) || avatarURL.contains(Const.PROFILE_URL)){
            return true;
        }
        return false;
    }


}

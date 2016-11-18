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

package com.instructure.candroid.binders;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.View;

import com.instructure.candroid.R;
import com.instructure.candroid.holders.QuizNumericalViewHolder;
import com.instructure.candroid.interfaces.QuizPostNumerical;
import com.instructure.candroid.interfaces.QuizToggleFlagState;
import com.instructure.candroid.view.CanvasWebView;
import com.instructure.canvasapi.model.QuizSubmissionQuestion;
import com.instructure.pandautils.utils.CanvasContextColor;


public class QuizNumericalBinder {

    public static void bind(final QuizNumericalViewHolder holder, final QuizSubmissionQuestion quizSubmissionQuestion, int courseColor, int position, boolean shouldLetAnswer, final Context context, final QuizToggleFlagState flagStateCallback, CanvasWebView.CanvasEmbeddedWebViewCallback callback, CanvasWebView.CanvasWebViewClientCallback webViewClientCallback, final QuizPostNumerical numericalCallback) {

        if(holder == null) {
            return;
        }
        holder.question.loadUrl("about:blank");
        holder.question.setCanvasWebViewClientCallback(webViewClientCallback);
        if(context instanceof Activity) {
            holder.question.addJavascriptInterface(new QuizMultiChoiceBinder.WebAppInterface(((Activity) context), holder.question), "MyApp");
        } else if(context instanceof ContextThemeWrapper) {
            holder.question.addJavascriptInterface(new QuizMultiChoiceBinder.WebAppInterface((Activity)(((ContextThemeWrapper) context).getBaseContext()), holder.question), "MyApp");
        }
        holder.question.formatHTML(quizSubmissionQuestion.getQuestionText(), "");
        holder.question.setBackgroundColor(Color.TRANSPARENT);
        holder.question.setCanvasEmbeddedWebViewCallback(callback);
        holder.questionNumber.setText(context.getString(R.string.question) + " " + (position + 1));

        holder.questionId = quizSubmissionQuestion.getId();
        if(!shouldLetAnswer) {
            holder.answer.setVisibility(View.GONE);
            holder.divider.setVisibility(View.GONE);
        }

        //clear out the answer in case it was recycled
        holder.answer.setText("");
        if(quizSubmissionQuestion.getAnswer() != null) {
            holder.answer.setText(Html.fromHtml((String)quizSubmissionQuestion.getAnswer()));
        }


        holder.answer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //save data after they're done typing
                if(holder.answer.getText() != null && holder.answer.getText().toString() != null) {
                    numericalCallback.postNumerical(holder.questionId, editable.toString());
                }
            }
        });

        final Drawable courseColorFlag = CanvasContextColor.getColoredDrawable(context, R.drawable.ic_bookmark_fill_grey, courseColor);

        if(quizSubmissionQuestion.isFlagged()) {
            holder.flag.setImageDrawable(courseColorFlag);
        } else {
            holder.flag.setImageResource(R.drawable.ic_bookmark_outline_grey);
        }
        if(shouldLetAnswer) {
            holder.flag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (quizSubmissionQuestion.isFlagged()) {
                        //unflag it
                        holder.flag.setImageResource(R.drawable.ic_bookmark_outline_grey);
                        flagStateCallback.toggleFlagged(false, quizSubmissionQuestion.getId());
                        quizSubmissionQuestion.setFlagged(false);
                    } else {
                        //flag it
                        holder.flag.setImageDrawable(courseColorFlag);
                        flagStateCallback.toggleFlagged(true, quizSubmissionQuestion.getId());
                        quizSubmissionQuestion.setFlagged(true);
                    }
                }
            });
        } else {
            holder.flag.setEnabled(false);
        }
    }
}

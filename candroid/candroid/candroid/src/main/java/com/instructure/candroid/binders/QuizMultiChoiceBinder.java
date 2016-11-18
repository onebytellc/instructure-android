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
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.instructure.candroid.R;
import com.instructure.candroid.dialog.AccountNotificationDialog;
import com.instructure.candroid.holders.QuizMultiChoiceViewHolder;
import com.instructure.candroid.interfaces.QuizPostMultiChoice;
import com.instructure.candroid.interfaces.QuizToggleFlagState;
import com.instructure.candroid.view.CanvasWebView;
import com.instructure.canvasapi.model.QuizSubmissionAnswer;
import com.instructure.canvasapi.model.QuizSubmissionQuestion;
import com.instructure.pandautils.utils.CanvasContextColor;

import java.lang.ref.WeakReference;

public class QuizMultiChoiceBinder {


    public static void bind(final QuizMultiChoiceViewHolder holder,
                            final QuizSubmissionQuestion quizSubmissionQuestion,
                            final int courseColor,
                            final int position,
                            final boolean shouldLetAnswer,
                            final Context context,
                            final CanvasWebView.CanvasEmbeddedWebViewCallback embeddedWebViewCallback,
                            final CanvasWebView.CanvasWebViewClientCallback webViewClientCallback,
                            final QuizPostMultiChoice callback,
                            final QuizToggleFlagState flagStateCallback) {

        if(holder == null) {
            return;
        }

        holder.question.loadUrl("about:blank");

        holder.question.setBackgroundColor(Color.TRANSPARENT);

        holder.question.setCanvasWebViewClientCallback(webViewClientCallback);

        if(context instanceof Activity) {
            holder.question.addJavascriptInterface(new WebAppInterface(((Activity) context), holder.question), "MyApp");
        } else if(context instanceof ContextThemeWrapper) {
            holder.question.addJavascriptInterface(new WebAppInterface((Activity)(((ContextThemeWrapper) context).getBaseContext()), holder.question), "MyApp");
        }
        holder.question.formatHTML(quizSubmissionQuestion.getQuestionText(), "");
        holder.question.setCanvasEmbeddedWebViewCallback(embeddedWebViewCallback);

        holder.questionNumber.setText(context.getString(R.string.question) + " " + (position + 1));

        holder.questionId = quizSubmissionQuestion.getId();

        LayoutInflater inflater = LayoutInflater.from(context);

        //sometimes when we recycle views it keeps the old views in there, so clear them out if there
        //are any in there
        if(holder.answerContainer.getChildCount() > 0) {
            holder.answerContainer.removeAllViews();
        }
        //add answers to the answer container
        int index = 0;
        for(final QuizSubmissionAnswer answer : quizSubmissionQuestion.getAnswers()) {

            final LinearLayout answerWrapper = (LinearLayout)inflater.inflate(R.layout.quiz_multi_choice_answer, null, false);
            final CanvasWebView webView = (CanvasWebView) answerWrapper.findViewById(R.id.html_answer);
            webView.setClickable(false);
            webView.setFocusableInTouchMode(false);
            final TextView textView = (TextView) answerWrapper.findViewById(R.id.text_answer);
            final CheckBox checkBox = (CheckBox) answerWrapper.findViewById(R.id.answer_checkbox);

            if(!TextUtils.isEmpty(answer.getHtml())) {
                textView.setVisibility(View.GONE);

                final String html = AccountNotificationDialog.trimTrailingWhitespace(answer.getHtml()).toString();

                webView.formatHTML(html, "");

                webView.setBackgroundColor(Color.TRANSPARENT);
                //we only care about marking the answers if they can actually answer
                if(shouldLetAnswer) {
                    webView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            //this may or may not be how we do the selection of things when we get the final UI
                            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                                answerWrapper.performClick();

                                return true;
                            }
                            return true;
                       }
                    });
                }

                if(quizSubmissionQuestion.getAnswer() != null) {
                    if(Long.parseLong((String)quizSubmissionQuestion.getAnswer()) == answer.getId()) {
                        //webView.setBackgroundColor(context.getResources().getColor(R.color.canvasBackgroundMedium));
                        answerWrapper.setBackgroundColor(context.getResources().getColor(R.color.canvasBackgroundMedium));

                        //mark this one as selected
                        checkBox.setChecked(true);
                    }
                }

            } else if(!TextUtils.isEmpty(answer.getText())) {
                webView.setVisibility(View.GONE);
                textView.setText(answer.getText());


                if(quizSubmissionQuestion.getAnswer() != null) {
                    if(!TextUtils.isEmpty((String)quizSubmissionQuestion.getAnswer()) && Long.parseLong((String)quizSubmissionQuestion.getAnswer()) == answer.getId()) {
                        //mark this one as selected
                        checkBox.setChecked(true);
                        answerWrapper.setBackgroundColor(context.getResources().getColor(R.color.canvasBackgroundMedium));

                    }
                }
            }

            if(shouldLetAnswer) {
                answerWrapper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        for (int i = 0; i < holder.answerContainer.getChildCount(); i++) {
                            if(holder.answerContainer.getId() != view.getId()) {
                                resetViews(i, holder, context);
                            }
                        }
                        checkBox.setChecked(true);

                        answerWrapper.setBackgroundColor(context.getResources().getColor(R.color.canvasBackgroundMedium));

                        //post the answer to the api
                        callback.postAnswer(holder.questionId, answer.getId());

                        //set the answer on the quizSubmissionQuestion so we'll remember which question was answered during row recycling
                        quizSubmissionQuestion.setAnswer(Long.toString(answer.getId()));

                    }
                });
            }

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

            holder.answerContainer.addView(answerWrapper);

            if(index == quizSubmissionQuestion.getAnswers().length - 1) {
                //if we're on the last answer remove the bottom divider
                answerWrapper.findViewById(R.id.divider).setVisibility(View.GONE);
            } else {
                answerWrapper.findViewById(R.id.divider).setVisibility(View.VISIBLE);
                index++;
            }
        }
    }

    private static void resetViews(int i, QuizMultiChoiceViewHolder holder, Context context) {
        //make all the layouts the normal color
        LinearLayout layout = (LinearLayout) holder.answerContainer.getChildAt(i);

        layout.setBackgroundColor(context.getResources().getColor(R.color.canvasBackgroundLight));

        ((CheckBox)layout.findViewById(R.id.answer_checkbox)).setChecked(false);
    }

    public static class WebAppInterface {
        private WeakReference<Activity> activity;
        private WebView webView;

        /**
         * Instantiate the interface and set the context
         */
        WebAppInterface(Activity activity, WebView webView) {
            this.activity = new WeakReference<>(activity);
            this.webView = webView;
        }


        @JavascriptInterface
        public void resize(final float height) {
            activity.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ViewGroup.LayoutParams params = webView.getLayoutParams();
                    params.width = webView.getWidth();
                    params.height = (int) (height * activity.get().getResources().getDisplayMetrics().density);
                    webView.setLayoutParams(params);
                }
            });
        }
    }
}

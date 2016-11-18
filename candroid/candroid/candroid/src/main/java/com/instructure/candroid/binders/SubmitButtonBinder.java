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


import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.instructure.candroid.R;
import com.instructure.candroid.adapter.QuizSubmissionQuestionListRecyclerAdapter;
import com.instructure.candroid.holders.SubmitButtonViewHolder;
import com.instructure.candroid.interfaces.QuizSubmit;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.loginapi.login.dialog.GenericDialogStyled;
import com.instructure.pandautils.utils.CanvasContextColor;

import java.util.Locale;

public class SubmitButtonBinder {

    private static GenericDialogStyled dialogStyled;

    public static void bind(SubmitButtonViewHolder holder, final Context context, CanvasContext canvasContext, final QuizSubmissionQuestionListRecyclerAdapter adapter, final QuizSubmit callback) {

        if(holder == null) {
            return;
        }

        final GenericDialogStyled.GenericDialogListener listener = new GenericDialogStyled.GenericDialogListener() {
            @Override
            public void onPositivePressed() {
                //submit the quiz
                callback.submitQuiz();
            }

            @Override
            public void onNegativePressed() {

            }
        };

        int[] colors = CanvasContextColor.getCachedColors(context, canvasContext);
        StateListDrawable stateListDrawable=new StateListDrawable();
        if(colors.length >= 2) {
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(colors[1]));
            stateListDrawable.addState(new int[]{android.R.attr.state_enabled}, new ColorDrawable(colors[0]));
            holder.submitButton.setBackgroundDrawable(stateListDrawable);
        } else {
            //it shouldn't get here, but this will just set the color as the course color, so no pressed state
            holder.submitButton.setBackgroundColor(CanvasContextColor.getCachedColor(context, canvasContext));
        }

        holder.submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //check to see if there are unanswered quiz questions
                boolean hasUnanswered = false;
                int numUnanswered = 0;
                if(adapter.getAnsweredQuestions().size() != adapter.getItemCount() - 1) {
                    hasUnanswered = true;
                    numUnanswered = (adapter.getItemCount() - 1) - adapter.getAnsweredQuestions().size();
                }

                if(hasUnanswered) {
                    String unanswered;
                    if(numUnanswered == 1) {
                        unanswered = String.format(Locale.getDefault(), context.getString(R.string.unansweredQuizQuestions), numUnanswered, context.getString(R.string.question).toLowerCase(Locale.getDefault()));
                    } else {
                        unanswered = String.format(Locale.getDefault(), context.getString(R.string.unansweredQuizQuestions), numUnanswered, context.getString(R.string.questions).toLowerCase(Locale.getDefault()));
                    }

                    dialogStyled = GenericDialogStyled.newInstance(true, R.string.submitQuiz, R.string.unansweredQuizQuestions, R.string.logout_yes, R.string.logout_no, R.drawable.ic_cv_alert_fill, unanswered, listener);
                    dialogStyled.setCancelable(true);
                    dialogStyled.show(((FragmentActivity)context).getSupportFragmentManager(), "tag");
                } else {
                    dialogStyled = GenericDialogStyled.newInstance(true, R.string.submitQuiz, R.string.areYouSure, R.string.logout_yes, R.string.logout_no, R.drawable.ic_cv_alert_fill, listener);
                    dialogStyled.setCancelable(true);
                    dialogStyled.show(((FragmentActivity)context).getSupportFragmentManager(), "tag");
                }
            }
        });

    }

}

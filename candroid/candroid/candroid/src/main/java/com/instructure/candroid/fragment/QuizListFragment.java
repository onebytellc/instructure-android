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

package com.instructure.candroid.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.instructure.candroid.R;
import com.instructure.candroid.adapter.QuizListRecyclerAdapter;
import com.instructure.candroid.delegate.Navigation;
import com.instructure.candroid.interfaces.AdapterToFragmentCallback;
import com.instructure.candroid.util.FragUtils;
import com.instructure.candroid.util.Param;
import com.instructure.canvasapi.model.CanvasContext;
import com.instructure.canvasapi.model.Course;
import com.instructure.canvasapi.model.Quiz;
import com.instructure.canvasapi.model.QuizQuestion;
import com.instructure.canvasapi.model.Tab;

import java.util.ArrayList;

public class QuizListFragment extends ParentFragment {

    private View mRootView;
    private QuizListRecyclerAdapter mRecyclerAdapter;
    private AdapterToFragmentCallback<Quiz> mAdapterToFragmentCallback;

    @Override
    public FRAGMENT_PLACEMENT getFragmentPlacement(Context context) {return FRAGMENT_PLACEMENT.MASTER; }

    public String getTabId() {
        return Tab.QUIZZES_ID;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapterToFragmentCallback = new AdapterToFragmentCallback<Quiz>() {
            @Override
            public void onRowClicked(Quiz quiz, int position, boolean isOpenDetail) {
                rowClick(quiz, isOpenDetail);
            }

            @Override
            public void onRefreshFinished() {
                setRefreshing(false);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mRootView = getLayoutInflater().inflate(R.layout.quiz_list_layout, container, false);

        mRecyclerAdapter = new QuizListRecyclerAdapter(getContext(), getCanvasContext(), mAdapterToFragmentCallback);
        configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView);

        return mRootView;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        configureRecyclerViewAsGrid(mRootView, mRecyclerAdapter, R.id.swipeRefreshLayout, R.id.emptyPandaView, R.id.listView);
    }

    @Override
    public String getFragmentTitle() {
        return getString(R.string.quizzes);
    }

    @Override
    protected String getSelectedParamName() {
        return Param.QUIZ_ID;
    }

    private void rowClick(Quiz quiz, boolean closeSlidingPane){
        Navigation navigation = getNavigation();
        if(navigation != null){
            //determine if we support the quiz question types. If not, just show the questions in a webview.
            //if the quiz has an access code we don't currently support that natively on the app, so send them
            //to a webview. Also, we currently don't support one quiz question at a time quizzes.
            if(!isNativeQuiz(getCanvasContext(), quiz)) {
                //Log to GA, track if they're a teacher (because teachers currently always get the non native quiz)
                Bundle bundle = BasicQuizViewFragment.createBundle(getCanvasContext(), quiz.getUrl(), quiz);
                navigation.addFragment(
                        FragUtils.getFrag(BasicQuizViewFragment.class, bundle));
            } else {

                Bundle bundle = QuizStartFragment.createBundle(getCanvasContext(), quiz);
                navigation.addFragment(
                        FragUtils.getFrag(QuizStartFragment.class, bundle));
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Helper Methods
    ///////////////////////////////////////////////////////////////////////////

    public static boolean isNativeQuiz(CanvasContext canvasContext, Quiz quiz) {
        return !(containsUnsupportedQuestionType(quiz) || quiz.hasAccessCode() || quiz.isOneQuestionAtATime() || (canvasContext instanceof Course && ((Course) canvasContext).isTeacher()));
    }

    //currently support TRUE_FALSE, ESSAY, SHORT_ANSWER, MULTI_CHOICE
    private static boolean containsUnsupportedQuestionType(Quiz quiz) {
        ArrayList<QuizQuestion.QUESTION_TYPE> questionTypes = quiz.getQuestionTypes();
        if(questionTypes == null || questionTypes.size() == 0) {
            return true;
        }

        //loop through all the quiz question types. If there is one we don't support, return false
        for(QuizQuestion.QUESTION_TYPE questionType : questionTypes) {
            switch (questionType) {
                case CALCULATED:
                case FILL_IN_MULTIPLE_BLANKS:
                case MULTIPLE_DROPDOWNS:
                case UNKNOWN:
                    return true;
            }
        }

        return false;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Intent
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void handleIntentExtras(Bundle extras) {
        super.handleIntentExtras(extras);
    }

    @Override
    public boolean allowBookmarking() {
        return true;
    }
}

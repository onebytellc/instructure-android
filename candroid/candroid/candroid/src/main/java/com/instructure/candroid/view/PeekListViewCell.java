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

package com.instructure.candroid.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.*;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;
import com.instructure.candroid.R;
import com.instructure.candroid.util.ApplicationManager;

import java.util.ArrayList;

/**
 * Cell for users to "peek" at their grades, should work all the
 * way back to 2.3 devices. Must be used in a PeekPullToRefreshListView
 * in order to work. In particular, the onInterceptTouchEvent must
 * be overridden for this cell to work in a list view correctly,
 * in the containing activity. You can find the code for this
 * commented about below. Unfortunately, there isn't an slicker
 * way to implement this that I am currently aware of.
 *
 *  @Override
 *   public boolean dispatchTouchEvent(MotionEvent ev) {
 *          PeekListViewCell cell = PeekListViewCell.getCancelledCell();
 *          if (cell != null) {
 *              cell.onTouchEvent(ev);
 *              return true;
 *          }
 *          return super.dispatchTouchEvent(ev);
 *   }
 *
 * Furthermore, the text view that shows the grade (or whatever is
 * hidden) MUST have setClickable(false) on it by default, as it
 * will not work otherwise.
 */
public class PeekListViewCell extends RelativeLayout {

    private static PeekListViewCell cancelledCell;
    private static ArrayList<PeekListViewCell> cells = new ArrayList<PeekListViewCell>();

    private static MotionEvent lastMove;
    private float touchX;
    private float slidingX;
    private boolean isOpen;
    private boolean hasTouchedHandle;

    private RelativeLayout slidingLayout;
    private ValueAnimator animator;

    private boolean shouldShowGrades;
    private View separator1;
    private View separator2;

    ///////////////////////////////////////////////////////////////////////////
    // Factory Method and Constructors.
    ///////////////////////////////////////////////////////////////////////////

    public PeekListViewCell(Context context) {
        super(context);
        init(context);
    }

    public PeekListViewCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PeekListViewCell(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(final Context context) {
        LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.listview_item_row_peek_shared, this, true);
        slidingLayout = (RelativeLayout) findViewById(R.id.top_layer);
        animator = ValueAnimator.ofFloat(slidingX, 0);
//        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                //set the width of the view to be the normal width, plus 8dp so that the drop
//                //shadow hangs off the screen when the view is closed
//                updateContentWidth((int) ViewUtils.convertDpToPixel(8, context));
//                getViewTreeObserver().removeGlobalOnLayoutListener(this);
//            }
//        });
        shouldShowGrades = ((ApplicationManager)context.getApplicationContext()).shouldShowGrades();

        separator1 = findViewById(R.id.separator1);
        separator2 = findViewById(R.id.separator2);

        setPeekingEnabled(shouldShowGrades);

        findViewById(R.id.handle).setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    hasTouchedHandle = true;
                }
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    hasTouchedHandle = false;
                }
                return false;
            }
        });

        cells.add(this);
    }

    public static PeekListViewCell getCancelledCell() {
        return cancelledCell;
    }

    public static void clearCells() {
        cells.clear();
    }

    public static ArrayList<PeekListViewCell> getCells() {
        return cells;
    }

    public static void setPeekingEnabledForCells(boolean enabled) {
        for (PeekListViewCell cell: cells) {
            cell.setPeekingEnabled(enabled);
            cell.invalidate();
        }
    }

    public void setPeekingEnabled(boolean enabled) {
        shouldShowGrades = enabled;
        if (enabled) {
            separator1.setVisibility(View.VISIBLE);
            separator2.setVisibility(View.VISIBLE);
        } else {
            separator1.setVisibility(View.INVISIBLE);
            separator2.setVisibility(View.INVISIBLE);
        }
        invalidate();
    }


    public void updateContentWidth(int padding) {
        setAdjustedWidth(getWidth() + padding);
    }

    /**
     * Manually override the width of the view.
     * @param adjustedWidth
     */
    public void setAdjustedWidth(int adjustedWidth) {
        ViewGroup.LayoutParams params = this.getLayoutParams();
        params.width = adjustedWidth;
        setLayoutParams(params);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!shouldShowGrades || !hasTouchedHandle) {
            return super.onTouchEvent(event);
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            touchX = event.getX();
            animator.cancel();
        }
        if (action == MotionEvent.ACTION_MOVE) {
            animator.cancel();

            //calculate distance of drag
            float delta = touchX - event.getX();
            if (isOpen) {
                delta +=  (getWidth() / 2);
            }

            //prevent going too far left or right

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)slidingLayout.getLayoutParams();

            slidingX = params.leftMargin - delta;
            if (slidingX > 0) {
                slidingX = 0;
            }
            if (slidingX < -(getWidth()/2)) {
                slidingX = -(getWidth()/2);
            }

            //apply shift to the top relative layout
            slidingLayout.layout((int)slidingX, 0, getWidth() + (int)slidingX, getHeight());

            lastMove = event;

            return true;
        }
        if (action == MotionEvent.ACTION_UP) {
            cancelledCell = null;
            animateClosed();
        }
        if (action == MotionEvent.ACTION_CANCEL) {
            cancelledCell = this;
        }
        return true;
    }

    private void animateClosed() {

        float endPos;

        if (slidingX <= -(getWidth() / 4)) {
            endPos  = -(getWidth() / 2);
            isOpen = true;
        } else {
            endPos = 0;
            isOpen = false;
        }

        findViewById(R.id.grade).setClickable(isOpen);
        findViewById(R.id.lockedGrade).setClickable(isOpen);

        animator = ValueAnimator.ofFloat(slidingX, endPos);
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                slidingX = (Float) animation.getAnimatedValue();
                slidingLayout.layout((int)slidingX, 0, getWidth() + (int)slidingX, getHeight());
            }
        });
        animator.start();
    }

    public void close() {
        slidingX = 0;
        isOpen = false;
    }



}

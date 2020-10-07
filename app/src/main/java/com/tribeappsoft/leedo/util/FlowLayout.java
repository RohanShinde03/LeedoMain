package com.tribeappsoft.leedo.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/*
 * Created by ${ROHAN} on 25/8/18.
 */


public class FlowLayout extends ViewGroup {
    // Custom layout that wraps child views to a new line.

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop();
        int lowestBottom = 0;
        int lineHeight = 0;
        int myWidth = resolveSize(100, widthMeasureSpec);
        int wantedHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            child.measure(getChildMeasureSpec(widthMeasureSpec, 0, child.getLayoutParams().width),
                    getChildMeasureSpec(heightMeasureSpec, 0, child.getLayoutParams().height));
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            lineHeight = Math.max(childHeight, lineHeight);

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            childLeft += lp.leftMargin;
            childTop += lp.topMargin;
            if (childLeft + childWidth + lp.rightMargin + getPaddingRight() > myWidth) { // Wrap this line
                childLeft = getPaddingLeft() + lp.leftMargin;
                childTop = lowestBottom + lp.topMargin; // Spaced below the previous lowest point
                lineHeight = childHeight;
            }
            childLeft += childWidth + lp.rightMargin;

            if (childTop + childHeight + lp.bottomMargin > lowestBottom) { // New lowest point
                lowestBottom = childTop + childHeight + lp.bottomMargin;
            }
        }
        wantedHeight += lowestBottom + getPaddingBottom(); // childTop + lineHeight + getPaddingBottom();
        setMeasuredDimension(myWidth, resolveSize(wantedHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop();
        int lowestBottom = 0;
        int myWidth = right - left;
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            childLeft += lp.leftMargin;
            childTop += lp.topMargin;
            if (childLeft + childWidth + lp.rightMargin + getPaddingRight() > myWidth) { // Wrap this line
                childLeft = getPaddingLeft() + lp.leftMargin;
                childTop = lowestBottom + lp.topMargin; // Spaced below the previous lowest point
            }
            child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            childLeft += childWidth + lp.rightMargin;

            if (childTop + childHeight + lp.bottomMargin > lowestBottom) { // New lowest point
                lowestBottom = childTop + childHeight + lp.bottomMargin;
            }
        }
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new FlowLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) lp);
        }
        else if (lp instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) lp);
        }
        else
            return super.generateLayoutParams(lp);
    }

    /**
     * Per-child layout information for layouts that support margins.
     */
    public static class LayoutParams extends MarginLayoutParams {
        public LayoutParams(@NonNull Context c, @Nullable AttributeSet attrs) {
            super(c, attrs);
        }
        public LayoutParams(int width, int height) {
            super(width, height);
        }
        public LayoutParams(@NonNull ViewGroup.LayoutParams source) {
            super(source);
        }
        public LayoutParams(@NonNull MarginLayoutParams source) {
            super(source);
        }
        public LayoutParams(@NonNull LayoutParams source) {
            super(source);
        }
    }
}


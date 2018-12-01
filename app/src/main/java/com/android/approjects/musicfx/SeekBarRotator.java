package com.android.approjects.musicfx;


import android.content.Context;
import android.opengl.Visibility;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/*
 *  This ViewGroup contains a single view, which will be rotated by 90 degrees counterclockwise.
 */
// 把SeekBar逆时针旋转90度。
// 对于ViewGroup原理还是比较混，是不是应该看看源码？
public class SeekBarRotator extends ViewGroup {

    public SeekBarRotator(Context context) {
        super(context);
    }

    public SeekBarRotator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeekBarRotator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // 获取SeekBar
        final View child = getChildAt(0);

        if (child.getVisibility() != GONE) {
            // swap width and height for child.
            // 要求子界面测试自己，加入传进来的需求。
            measureChild(child, heightMeasureSpec, widthMeasureSpec);
            setMeasuredDimension(
                    child.getMeasuredHeightAndState(),
                    child.getMeasuredWidthAndState());
        } else {
            setMeasuredDimension(
                    resolveSizeAndState(0, widthMeasureSpec, 0),
                    resolveSizeAndState(0, heightMeasureSpec, 0));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final View child = getChildAt(0);

        if (child.getVisibility() != GONE) {
            // rotate the child 90 degrees counterclockwise around its upper-left.
            // 设置旋转基准点。
            child.setPivotX(0);
            child.setPivotY(0);
            child.setRotation(-90);
        }

        // place the child below this view, so it rotates into view.
        int myWidth = r - l;
        int myHeight = b - t;

        int childWidth = myHeight;
        int childHeight = myWidth;

        /**
         * @param l Left position, relative to parent
         * @param t Top position, relative to parent
         * @param r Right position, relative to parent
         * @param b Bottom position, relative to parent
         */
        // 可以理解为左边对齐，上面小方框，下面大方框。
        child.layout(0, myHeight, childWidth, myHeight + childHeight);
    }
}

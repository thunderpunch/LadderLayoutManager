package com.thunderpunch.ladderlayoutmanager.sample.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.thunderpunch.ladderlayoutmanager.R;


/**
 * Created by thunderpunch on 2016/7/21
 * Description:锁定纵横比
 */
public class HWRatioContainer extends LinearLayout {
    private float hwRatio;
    private static final int LOCK_WIDTH = 0;
    private static final int LOCK_HEIGHT = 1;
    private boolean isWidthLocked;
    private boolean includePadding;//计算宽高比时是否将padding算进去

    public HWRatioContainer(Context context) {
        this(context, null);
    }

    public HWRatioContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HWRatioContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.HWRatioContainer);
        if (typedArray != null) {
            hwRatio = typedArray.getFloat(R.styleable.HWRatioContainer_heightDivideWidthRatio, 1);
            isWidthLocked = typedArray.getInt(R.styleable.HWRatioContainer_lockSide, LOCK_WIDTH) == LOCK_WIDTH;
            includePadding = typedArray.getBoolean(R.styleable.HWRatioContainer_calculateRatioIncludePaddiang, false);
        }
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isWidthLocked && MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            int width = MeasureSpec.getSize(widthMeasureSpec) - (includePadding ? 0 : (getPaddingLeft() + getPaddingRight()));
            int height = (int) (width * hwRatio) + (includePadding ? 0 : (getPaddingTop() + getPaddingBottom()));
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        } else if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED) {
            int height = MeasureSpec.getSize(heightMeasureSpec) - (includePadding ? 0 : (getPaddingBottom() + getPaddingTop()));
            int width = (int) (height / hwRatio + (includePadding ? 0 : getPaddingLeft() + getPaddingRight()));
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}

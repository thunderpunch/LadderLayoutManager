package com.thunderpunch.lib.layoutmanager;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import java.util.ArrayList;

/**
 * Created by ThunderPunch on 2017/2/15
 * Description:
 */

public class LadderLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {
    private static final int INVALIDATE_SCROLL_OFFSET = Integer.MAX_VALUE;
    private static final float DEFAULT_CHILD_LAYOUT_OFFSET = 0.2f;
    public static final int UNLIMITED = 0;
    public static final int VERTICAL = 1;
    public static final int HORIZONTAL = 0;
    private boolean mCheckedChildSize;
    private int[] mChildSize;
    private int mChildPeekSize;
    private int mChildPeekSizeInput;
    private boolean mReverse;
    private int mScrollOffset = INVALIDATE_SCROLL_OFFSET;
    private float mItemHeightWidthRatio;
    private float mScale;
    private int mChildCount;
    private float mVanishOffset = 0;
    private Interpolator mInterpolator;
    private int mOrientation;
    private ChildDecorateHelper mDecorateHelper;
    private int mMaxItemLayoutCount;

    public LadderLayoutManager(float itemHeightWidthRatio) {
        this(itemHeightWidthRatio, 0.9f, VERTICAL);
    }

    /**
     * @param itemHeightWidthRatio childview的纵横比。所有childview都会按该纵横比展示
     * @param scale                chidview每一层级相对于上一层级的缩放量
     * @param orientation          布局方向
     */
    public LadderLayoutManager(float itemHeightWidthRatio, float scale, int orientation) {
        this.mItemHeightWidthRatio = itemHeightWidthRatio;
        this.mOrientation = orientation;
        this.mScale = scale;
        this.mChildSize = new int[2];
        this.mInterpolator = new DecelerateInterpolator();
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(mChildSize[0], mChildSize[1]);
    }

    public LadderLayoutManager setChildDecorateHelper(ChildDecorateHelper layoutHelper) {
        mDecorateHelper = layoutHelper;
        return this;
    }

    public void setMaxItemLayoutCount(int count) {
        mMaxItemLayoutCount = Math.max(2, count);
        if (getChildCount() > 0) {
            requestLayout();
        }
    }

    /**
     * @param offset 消失点的偏移量 范围[-1,1]，默认0（居中）
     */
    public void setVanishOffset(float offset) {
        mVanishOffset = offset;
        if (getChildCount() > 0) {
            requestLayout();
        }
    }


    public void setChildPeekSize(int childPeekSize) {
        mChildPeekSizeInput = childPeekSize;
        mCheckedChildSize = false;
        if (getChildCount() > 0) {
            requestLayout();
        }
    }

    /**
     * @param itemHeightWidthRatio child的纵横比。所有childview都会按该纵横比展示
     */
    public void setItemHeightWidthRatio(float itemHeightWidthRatio) {
        mItemHeightWidthRatio = itemHeightWidthRatio;
        mCheckedChildSize = false;
        if (getChildCount() > 0) {
            requestLayout();
        }
    }

    /**
     * @param reverse 是否反向显示数据
     */
    public void setReverse(boolean reverse) {
        if (mReverse != reverse) {
            mReverse = reverse;
            if (getChildCount() > 0) {
                requestLayout();
            }
        }
    }

    public boolean isReverse() {
        return mReverse;
    }


    /**
     * @param direction 滑动方向，正负同{@link #mScrollOffset}的增减
     * @param fixValue  同向修正值
     * @return
     */
    public int getFixedScrollPosition(int direction, float fixValue) {
        if (mCheckedChildSize) {
            if (mScrollOffset % mChildSize[mOrientation] == 0) {
                return RecyclerView.NO_POSITION;
            }
            float position = mScrollOffset * 1.0f / mChildSize[mOrientation];
            return convert2AdapterPosition((int) (direction > 0 ? position + fixValue : position + (1 - fixValue)) - 1);
        }
        return RecyclerView.NO_POSITION;
    }


    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        super.onMeasure(recycler, state, widthSpec, heightSpec);
        mCheckedChildSize = false;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (state.getItemCount() == 0) {
            removeAndRecycleAllViews(recycler);
            return;
        }
        if (!mCheckedChildSize) {
            if (mOrientation == VERTICAL) {
                mChildSize[0] = getHorizontalSpace();
                mChildSize[1] = (int) (mItemHeightWidthRatio * mChildSize[0]);
            } else {
                mChildSize[1] = getVerticalSpace();
                mChildSize[0] = (int) (mChildSize[1] / mItemHeightWidthRatio);
            }
            mChildPeekSize = mChildPeekSizeInput == 0 ?
                    (int) (mChildSize[mOrientation] * DEFAULT_CHILD_LAYOUT_OFFSET) : mChildPeekSizeInput;
            mCheckedChildSize = true;
        }
        int itemCount = getItemCount();
        if (mReverse) {
            mScrollOffset += (itemCount - mChildCount) * mChildSize[mOrientation];
        }
        mChildCount = itemCount;
        mScrollOffset = makeScrollOffsetWithinRange(mScrollOffset);
        fill(recycler);
    }

    public void fill(RecyclerView.Recycler recycler) {
        int bottomItemPosition = (int) Math.floor(mScrollOffset / mChildSize[mOrientation]);//>=1
        int bottomItemVisibleSize = mScrollOffset % mChildSize[mOrientation];
        final float offsetPercent = mInterpolator.getInterpolation(
                bottomItemVisibleSize * 1.0f / mChildSize[mOrientation]);//[0,1)
        final int space = mOrientation == VERTICAL ? getVerticalSpace() : getHorizontalSpace();

        ArrayList<ItemLayoutInfo> layoutInfos = new ArrayList<>();
        for (int i = bottomItemPosition - 1, j = 1, remainSpace = space - mChildSize[mOrientation];
             i >= 0; i--, j++) {
            double maxOffset = mChildPeekSize * Math.pow(mScale, j);
            int start = (int) (remainSpace - offsetPercent * maxOffset);
            ItemLayoutInfo info = new ItemLayoutInfo(start,
                    (float) (Math.pow(mScale, j - 1) * (1 - offsetPercent * (1 - mScale))),
                    offsetPercent, start * 1.0f / space
            );
            layoutInfos.add(0, info);

            if (mMaxItemLayoutCount != UNLIMITED && j == mMaxItemLayoutCount - 1) {
                if (offsetPercent != 0) {
                    info.start = remainSpace;
                    info.positionOffsetPercent = 0;
                    info.layoutPercent = remainSpace / space;
                    info.scaleXY = (float) Math.pow(mScale, j - 1);
                }
                break;
            }
            remainSpace -= maxOffset;
        }

        if (bottomItemPosition < mChildCount) {
            final int start = space - bottomItemVisibleSize;
            layoutInfos.add(new ItemLayoutInfo(start, 1.0f,
                    bottomItemVisibleSize * 1.0f / mChildSize[mOrientation], start * 1.0f / space).
                    setIsBottom());
        } else {
            bottomItemPosition -= 1;
        }

        int layoutCount = layoutInfos.size();

        final int startPos = bottomItemPosition - (layoutCount - 1);
        final int endPos = bottomItemPosition;
        final int childCount = getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            View childView = getChildAt(i);
            int pos = convert2LayoutPosition(getPosition(childView));
            if (pos > endPos || pos < startPos) {
                removeAndRecycleView(childView, recycler);
            }
        }
        detachAndScrapAttachedViews(recycler);

        for (int i = 0; i < layoutCount; i++) {
            fillChild(recycler.getViewForPosition(convert2AdapterPosition(startPos + i)), layoutInfos.get(i));
        }
    }

    private void fillChild(View view, ItemLayoutInfo layoutInfo) {
        addView(view);
        measureChildWithExactlySize(view);
        final int scaleFix = (int) (mChildSize[mOrientation] * (1 - layoutInfo.scaleXY) / 2);
        final float gap = (mOrientation == VERTICAL ? getHorizontalSpace() : getVerticalSpace())
                - mChildSize[(mOrientation + 1) % 2] * layoutInfo.scaleXY;

        if (mOrientation == VERTICAL) {
            int left = (int) (getPaddingLeft() + (gap * 0.5 * mVanishOffset));
            layoutDecoratedWithMargins(view, left, layoutInfo.start - scaleFix
                    , left + mChildSize[0], layoutInfo.start + mChildSize[1] - scaleFix);
        } else {
            int top = (int) (getPaddingTop() + (gap * 0.5 * mVanishOffset));
            layoutDecoratedWithMargins(view, layoutInfo.start - scaleFix, top
                    , layoutInfo.start + mChildSize[0] - scaleFix, top + mChildSize[1]);
        }
        ViewCompat.setScaleX(view, layoutInfo.scaleXY);
        ViewCompat.setScaleY(view, layoutInfo.scaleXY);
        if (mDecorateHelper != null) {
            mDecorateHelper.decorateChild(view, layoutInfo.positionOffsetPercent, layoutInfo.layoutPercent, layoutInfo.isBottom);
        }
    }

    private void measureChildWithExactlySize(View child) {
        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(
                mChildSize[0] - lp.leftMargin - lp.rightMargin, View.MeasureSpec.EXACTLY);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(
                mChildSize[1] - lp.topMargin - lp.bottomMargin, View.MeasureSpec.EXACTLY);
        child.measure(widthSpec, heightSpec);
    }

    private int makeScrollOffsetWithinRange(int scrollOffset) {
        return Math.min(Math.max(mChildSize[mOrientation], scrollOffset), mChildCount * mChildSize[mOrientation]);
    }


    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int pendingScrollOffset = mScrollOffset + dy;
        mScrollOffset = makeScrollOffsetWithinRange(pendingScrollOffset);
        fill(recycler);
        return mScrollOffset - pendingScrollOffset + dy;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int pendingScrollOffset = mScrollOffset + dx;
        mScrollOffset = makeScrollOffsetWithinRange(pendingScrollOffset);
        fill(recycler);
        return mScrollOffset - pendingScrollOffset + dx;
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        final LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            public int calculateDyToMakeVisible(final View view, final int snapPreference) {
                if (mOrientation == VERTICAL) {
                    return -calculateDistanceToPosition(getPosition(view));
                }
                return 0;
            }

            @Override
            public int calculateDxToMakeVisible(final View view, final int snapPreference) {
                if (mOrientation == HORIZONTAL) {
                    return -calculateDistanceToPosition(getPosition(view));
                }
                return 0;
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }

    /**
     * @return 当前位置与目标下标位视图的距离
     */
    public int calculateDistanceToPosition(int targetPos) {
        int pendingScrollOffset = mChildSize[mOrientation] * (convert2LayoutPosition(targetPos) + 1);
        return pendingScrollOffset - mScrollOffset;
    }


    @Override
    public void scrollToPosition(int position) {
        if (position > 0 && position < mChildCount) {
            mScrollOffset = mChildSize[mOrientation] * (convert2LayoutPosition(position) + 1);
            requestLayout();
        }
    }


    @Override
    public boolean canScrollVertically() {
        return mOrientation == VERTICAL;
    }

    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == HORIZONTAL;
    }

    public int convert2AdapterPosition(int layoutPosition) {
        return mReverse ? mChildCount - 1 - layoutPosition : layoutPosition;
    }

    public int convert2LayoutPosition(int adapterPostion) {
        return mReverse ? mChildCount - 1 - adapterPostion : adapterPostion;
    }

    public int getVerticalSpace() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    public int getHorizontalSpace() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        int pos = convert2LayoutPosition(targetPosition);
        int scrollOffset = (pos + 1) * mChildSize[mOrientation];
        return mOrientation == VERTICAL ? new PointF(0, Math.signum(scrollOffset - mScrollOffset))
                : new PointF(Math.signum(scrollOffset - mScrollOffset), 0);
    }

    private static class ItemLayoutInfo {
        float scaleXY;
        float layoutPercent;
        float positionOffsetPercent;
        int start;
        boolean isBottom;

        ItemLayoutInfo(int top, float scale, float positonOffset, float percent) {
            this.start = top;
            this.scaleXY = scale;
            this.positionOffsetPercent = positonOffset;
            this.layoutPercent = percent;
        }

        ItemLayoutInfo setIsBottom() {
            isBottom = true;
            return this;
        }

    }


    @Override
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState();
        savedState.scrollOffset = mScrollOffset;
        savedState.reverse = mReverse;
        savedState.vanishOffset = mVanishOffset;
        savedState.scale = mScale;
        savedState.childLayoutOffsetInput = mChildPeekSizeInput;
        savedState.itemHeightWidthRatio = mItemHeightWidthRatio;
        savedState.orientation = mOrientation;
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof SavedState) {
            SavedState s = (SavedState) state;
            mScrollOffset = s.scrollOffset;
            mReverse = s.reverse;
            mVanishOffset = s.vanishOffset;
            mScale = s.scale;
            mChildPeekSizeInput = s.childLayoutOffsetInput;
            mItemHeightWidthRatio = s.itemHeightWidthRatio;
            mOrientation = s.orientation;
            requestLayout();
        }
    }

    public static class SavedState implements Parcelable {

        int scrollOffset, childLayoutOffsetInput, orientation;
        float itemHeightWidthRatio, scale, elevation, vanishOffset;
        boolean reverse;

        public SavedState() {
        }

        SavedState(LadderLayoutManager.SavedState other) {
            scrollOffset = other.scrollOffset;
            childLayoutOffsetInput = other.childLayoutOffsetInput;
            orientation = other.orientation;
            itemHeightWidthRatio = other.itemHeightWidthRatio;
            scale = other.scale;
            elevation = other.elevation;
            vanishOffset = other.vanishOffset;
            reverse = other.reverse;
        }

        SavedState(Parcel in) {
            scrollOffset = in.readInt();
            childLayoutOffsetInput = in.readInt();
            orientation = in.readInt();
            itemHeightWidthRatio = in.readFloat();
            scale = in.readFloat();
            elevation = in.readFloat();
            vanishOffset = in.readFloat();
            reverse = in.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(scrollOffset);
            dest.writeInt(childLayoutOffsetInput);
            dest.writeInt(orientation);
            dest.writeFloat(itemHeightWidthRatio);
            dest.writeFloat(scale);
            dest.writeFloat(elevation);
            dest.writeFloat(vanishOffset);
            dest.writeInt(reverse ? 1 : 0);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<SavedState> CREATOR
                = new Creator<SavedState>() {
            @Override
            public LadderLayoutManager.SavedState createFromParcel(Parcel in) {
                return new LadderLayoutManager.SavedState(in);
            }

            @Override
            public LadderLayoutManager.SavedState[] newArray(int size) {
                return new LadderLayoutManager.SavedState[size];
            }
        };
    }


    public interface ChildDecorateHelper {
        /**
         * @param child
         * @param posOffsetPercent childview相对于自身起始位置的偏移量百分比范围[0，1)
         * @param layoutPercent    childview 在整个布局中的位置百分比
         * @param isBottom         childview 是否处于底部
         */
        void decorateChild(View child, float posOffsetPercent, float layoutPercent, boolean isBottom);
    }

    public static class DefaultChildDecorateHelper implements ChildDecorateHelper {
        private float mElevation;

        public DefaultChildDecorateHelper(float maxElevation) {
            mElevation = maxElevation;
        }

        @Override
        public void decorateChild(View child, float posOffsetPercent, float layoutPercent, boolean isBottom) {
            ViewCompat.setElevation(child, (float) (layoutPercent * mElevation * 0.7 + mElevation * 0.3));
        }
    }
}

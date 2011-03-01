/*******************************************************************************
 * Copyright (c) 2011 Adam Shanks (ChainsDD)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.noshufou.android.su.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class PinnedHeaderExpandableListView extends ExpandableListView {

    public interface PinnedHeaderExpandableAdapter {

        /**
         * Pinned header state: don't show the header
         */
        public static final int PINNED_HEADER_GONE = 0;

        /**
         * Pinned header state: show the header at the top of the list
         */
        public static final int PINNED_HEADER_VISIBLE = 1;

        /**
         * Pinned header state: show the header. If the header extends beyond
         * the bottom of the first shown element, push it up and clip
         */
        public static final int PINNED_HEADER_PUSHED_UP = 2;

        /**
         * Computes the desired state of the pinned header for the given
         * position of the first visible list item. Allowed return values are
         * {@link #PINNED_HEADER_GONE}, {@link #PINNED_HEADER_VISIBLE} or
         * {@link #PINNED_HEADER_PUSHED_UP}.
         */
        int getPinnedHeaderState(int position);

        /**
         * Configures the pinned header view to match the first visible list item.
         *
         * @param header pinned header view.
         * @param position position of the first visible list item.
         * @param alpha fading of the header view, between 0 and 255.
         */
        void configurePinnedHeader(View header, int position, int alpha);
    }

    public static final int MAX_ALPHA = 255;

    private PinnedHeaderExpandableAdapter mAdapter;
    private View mHeaderView;
    private boolean mHeaderViewVisible;

    private int mHeaderViewWidth;
    private int mHeaderViewHeight;

    public PinnedHeaderExpandableListView(Context context) {
        super(context);
    }

    public PinnedHeaderExpandableListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PinnedHeaderExpandableListView(Context context, AttributeSet attrs,
            int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setPinnedHeaderView(View view) {
        mHeaderView = view;

        if (mHeaderView != null) {
            setFadingEdgeLength(0);
        }
        requestLayout();
    }

    @Override
    public void setAdapter(ExpandableListAdapter adapter) {
        super.setAdapter(adapter);
        mAdapter = (PinnedHeaderExpandableAdapter) adapter;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mHeaderViewWidth = mHeaderView.getMeasuredWidth();
            mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mHeaderView != null) {
            mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
            configureHeaderView(getFirstVisibleGroupPosition());
        }
    }

    /**
     * Return the first visible group position. This function returns the group
     * a child belongs to if a child happens to be the first visible position.
     */
    public int getFirstVisibleGroupPosition() {
        return getPackedPositionGroup(getExpandableListPosition(
                super.getFirstVisiblePosition()));
    }

    public void configureHeaderView(int position) {
        if (mHeaderView == null || getCount() == 0) {
            return;
        }

        int state = mAdapter.getPinnedHeaderState(position);
        switch (state) {
        case PinnedHeaderExpandableAdapter.PINNED_HEADER_GONE:
            mHeaderViewVisible = false;
            break;
        
        case PinnedHeaderExpandableAdapter.PINNED_HEADER_VISIBLE:
            mAdapter.configurePinnedHeader(mHeaderView, position, MAX_ALPHA);
            if (mHeaderView.getTop() != 0) {
                mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
            }
            mHeaderViewVisible = true;
            break;
            
        case PinnedHeaderExpandableAdapter.PINNED_HEADER_PUSHED_UP:
            View firstView = getChildAt(0);
            int bottom = firstView.getBottom();
            int headerHeight = mHeaderView.getHeight();
            int y;
            int alpha;
            if (bottom < headerHeight) {
                y = bottom - headerHeight;
                alpha = MAX_ALPHA * (headerHeight + y) / headerHeight;
            } else {
                y = 0;
                alpha = MAX_ALPHA;
            }
            mAdapter.configurePinnedHeader(mHeaderView, position, alpha);
            if (mHeaderView.getTop() != y) {
                mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight + y);
            }
            mHeaderViewVisible = true;
            break;
        }
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHeaderViewVisible) {
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }
    
    public final static class PinnedHeaderExpandableCache {
        public TextView titleView;
        public ColorStateList textColor;
        public Drawable background;
    }
}

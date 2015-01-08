package com.staples.mobile.cfa.widget;

/**
 * Author: Yongnan Zhou
 */

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class AnimatedBarScrollView extends ScrollView
{
    private final String TAG = "AnimatedBarScrollView";
    public static final int MAX_ALPHA = 255;
    public static int currentAlpha;
    public static boolean isFirstLoad = true;

    private OnAnimatedScrollListener onAnimatedScrollCallback;

    public interface OnAnimatedScrollListener {
        public void initAnimatedActionBar();

        public void setAnimatedActionBarOnScroll(float scrollY);
    }

    public void setAnimatedScrollCallback(OnAnimatedScrollListener listener) {
        this.onAnimatedScrollCallback = listener;
    }

    public AnimatedBarScrollView(Context context){
        super(context);
    }

    public AnimatedBarScrollView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // call implemented callback methods to initialize the scroll view
        onAnimatedScrollCallback.initAnimatedActionBar();
    };

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        // call implemented callback methods to update the scroll view ui
        onAnimatedScrollCallback.setAnimatedActionBarOnScroll(getScrollY());

        super.onScrollChanged(l, t, oldl, oldt);
    }
}

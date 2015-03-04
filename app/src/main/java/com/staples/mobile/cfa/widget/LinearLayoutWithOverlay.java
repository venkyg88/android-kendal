/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by sutdi001 on 10/17/14.
 *
 * Class to swallow touch events of container and its children while overlay is visible
 */
public class LinearLayoutWithOverlay extends LinearLayout {

    boolean swallowTouchEvents = false;
    View overlayView;
    OnClickListener onSwallowedClickListener;

    public LinearLayoutWithOverlay(Context context) {
        super(context);
    }

    public LinearLayoutWithOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearLayoutWithOverlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void showOverlay(boolean showIndicator) {
        if (overlayView != null) {
            overlayView.setVisibility(showIndicator ? View.VISIBLE : View.GONE);
        }
        this.swallowTouchEvents = showIndicator;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (swallowTouchEvents && onSwallowedClickListener != null) {
            onSwallowedClickListener.onClick(this);
        }

        // returning true disables touch events on view and its children
        return swallowTouchEvents;
    }


    /** sets overlay view to use when showOverlay is called. this view must be contained
     * in the same FrameLayout as the layout defined by this class. */
    public void setOverlayView(View overlayView) {
        this.overlayView = overlayView;
    }

    /** optional: set onClick listener so app can respond (e.g. to dismiss bottom sheet)  */
    public void setOnSwallowedClickListener(OnClickListener onSwallowedClickListener) {
        this.onSwallowedClickListener = onSwallowedClickListener;
    }
}

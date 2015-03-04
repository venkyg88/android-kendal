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
 * Class to swallow touch events of cart container and its children while progress indicator is visible
 */
public class LinearLayoutWithProgressOverlay extends LinearLayout {

    boolean swallowTouchEvents = false;
    View progressOverlay;
    OnClickListener onSwallowedClickListener;

    public LinearLayoutWithProgressOverlay(Context context) {
        super(context);
    }

    public LinearLayoutWithProgressOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearLayoutWithProgressOverlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void showProgressIndicator(boolean showIndicator) {
        if (progressOverlay != null) {
            progressOverlay.setVisibility(showIndicator ? View.VISIBLE : View.GONE);
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


    /** sets overlay view to display when operation in progress */
    public void setProgressOverlay(View progressOverlay) {
        this.progressOverlay = progressOverlay;
    }

    /** optional: set onClick listener so app can respond (e.g. to dismiss bottom sheet)  */
    public void setOnSwallowedClickListener(OnClickListener onSwallowedClickListener) {
        this.onSwallowedClickListener = onSwallowedClickListener;
    }
}

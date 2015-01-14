/*
 * Copyright (c) 2015 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by sutdi001 on 10/17/14.
 *
 * Class to swallow touch events of cart container and its children while progress indicator is visible
 */
public class RelativeLayoutWithProgressOverlay extends RelativeLayout {

    boolean swallowTouchEvents = false;
    View progressOverlay;

    public RelativeLayoutWithProgressOverlay(Context context) {
        super(context);
    }

    public RelativeLayoutWithProgressOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeLayoutWithProgressOverlay(Context context, AttributeSet attrs, int defStyle) {
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
        // returning true disables touch events on view and its children
        return swallowTouchEvents;
    }


    /** sets overlay view to display when operation in progress */
    public void setProgressOverlay(View progressOverlay) {
        this.progressOverlay = progressOverlay;
    }
}

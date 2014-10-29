/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

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
    CartAdapter.ProgressIndicator progressIndicator;
    View cartProgressOverlay;

    public LinearLayoutWithProgressOverlay(Context context) {
        super(context);
    }

    public LinearLayoutWithProgressOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LinearLayoutWithProgressOverlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CartAdapter.ProgressIndicator getProgressIndicator () {
        if (progressIndicator == null) {
            progressIndicator = new CartAdapter.ProgressIndicator() {
                public void showProgressIndicator() {
                    if (LinearLayoutWithProgressOverlay.this.cartProgressOverlay != null) {
                        LinearLayoutWithProgressOverlay.this.cartProgressOverlay.setVisibility(View.VISIBLE);
                    }
                    setSwallowTouchEvents(true);
                }

                public void hideProgressIndicator() {
                    if (LinearLayoutWithProgressOverlay.this.cartProgressOverlay != null) {
                        LinearLayoutWithProgressOverlay.this.cartProgressOverlay.setVisibility(View.GONE);
                    }
                    setSwallowTouchEvents(false);
                }
            };
        }
        return progressIndicator;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // returning true disables touch events on view and its children
        return swallowTouchEvents;
    }


    public boolean isSwallowTouchEvents() {
        return swallowTouchEvents;
    }

    /** disables touch events on view and its children */
    public void setSwallowTouchEvents(boolean swallowTouchEvents) {
        this.swallowTouchEvents = swallowTouchEvents;
    }


    public View getCartProgressOverlay() {
        return cartProgressOverlay;
    }

    /** sets overlay view to display when operation in progress */
    public void setCartProgressOverlay(View cartProgressOverlay) {
        this.cartProgressOverlay = cartProgressOverlay;
    }
}

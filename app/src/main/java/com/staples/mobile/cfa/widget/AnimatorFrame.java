package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * This class makes possible relative animation control with objectAnimator
 */
public class AnimatorFrame extends FrameLayout {
    public AnimatorFrame(Context context) {
        super(context, null, 0);
    }

    public AnimatorFrame(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public AnimatorFrame(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // Property modifiers

    public void setXFraction(float fraction) {
        int width = getWidth();
        if (width>0) setX(fraction * width);
    }

    public void setYFraction(float fraction) {
        int height = getHeight();
        if (height>0) setY(fraction * height);
    }
}

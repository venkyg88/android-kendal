package app.staples.mobile.cfa.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * This class makes possible relative animation control with objectAnimator
 */
public class AnimatorLayout extends LinearLayout {
    public AnimatorLayout(Context context) {
        super(context, null, 0);
    }

    public AnimatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public AnimatorLayout(Context context, AttributeSet attrs, int defStyle) {
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

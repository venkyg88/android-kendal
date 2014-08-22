package com.staples.drawertest;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by pyhre001 on 8/22/14.
 */
public class SquareButton extends ImageView {
    public SquareButton(Context context) {
        super(context, null, 0);
    }

    public SquareButton(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public SquareButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(height, height);
    }
}

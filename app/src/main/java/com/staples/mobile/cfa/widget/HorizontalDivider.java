package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.staples.mobile.cfa.R;

public class HorizontalDivider extends RecyclerView.ItemDecoration {
    private static final String TAG = "HorizontalDivider";

    private int thick;
    private int gap;
    private Paint paint;

    public HorizontalDivider(Context context) {
        super();

        // Get attributes
        Resources res = context.getResources();
        thick = res.getDimensionPixelSize(R.dimen.horizontal_divider_thickness);
        gap = res.getDimensionPixelOffset(R.dimen.horizontal_divider_gap);
        int color = res.getColor(R.color.staples_off_white);

        // Divider paint
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
    }
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = 0;
        outRect.right = 0;
        outRect.top = 0;
        outRect.bottom = 0;
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int y = parent.getTop()+parent.getPaddingTop();
        int n = parent.getChildCount();
        for(int i=0;i<n;i++) {
            View child = parent.getChildAt(i);
            int top = child.getTop();
            if (top>y) {
                int bottom = top + thick;
                int left = child.getLeft() + gap;
                int right = child.getRight() - gap;
                canvas.drawRect(left, top, right, bottom, paint);
            }
        }
    }
}

package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class FixedSizeLayoutManager extends LinearLayoutManager {
    private static final String TAG = "FixedSizeLayoutManager";
    private int unitHeight;

    public FixedSizeLayoutManager(Context context) {
        super(context);
    }

    public void setUnitHeight(int unitHeight) {
        this.unitHeight = unitHeight;
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        int width = View.MeasureSpec.getSize(widthSpec);
        int height = View.MeasureSpec.getSize(heightSpec);

        int max = state.getItemCount()*unitHeight+getPaddingTop()+getPaddingBottom();

        switch(View.MeasureSpec.getMode(heightSpec)) {
            case View.MeasureSpec.AT_MOST:
                if (max<height) height = max;
                break;
            case View.MeasureSpec.UNSPECIFIED:
                height = max;
                break;
        }
        setMeasuredDimension(width, height);
    }
}

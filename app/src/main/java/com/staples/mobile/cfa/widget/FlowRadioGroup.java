package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Collections;

public class FlowRadioGroup extends RadioGroup {
    private static final String TAG = "FlowRadioGroup";

    private int[] childWidths;
    private ArrayList<Integer> lines;
    private int lineHeight;

    public FlowRadioGroup(Context context) {
        this(context, null);
    }

    public FlowRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private ArrayList<Integer> fitChildren(int width) {
        ArrayList<Integer> fit = new ArrayList<Integer>();
        int i = 0;
        fit.add(0);
        for(int j=0;j<childWidths.length;j++) {
            if (fit.get(i)+childWidths[j]>width) {
                i++;
                fit.add(0);
            }
            fit.set(i, fit.get(i)+childWidths[j]);
        }
        return(fit);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = View.MeasureSpec.getSize(widthSpec)-getPaddingLeft()-getPaddingRight();

        int n = getChildCount();
        if (childWidths==null) childWidths = new int[n];
        lineHeight = 0;

        // Measure children
        for(int i=0;i<n;i++) {
            View child = getChildAt(i);
            child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                          MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            childWidths[i] = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            if (childHeight > lineHeight) lineHeight = childHeight;
        }

        // Fit narrowest possible in minimal lines
        lines = fitChildren(width);
        for(;;) {
            int grant = Collections.max(lines)-1;
            ArrayList<Integer> fit = fitChildren(grant);
            if (fit.size()>lines.size()) break;
            lines = fit;
            width = grant;
        }

        width += getPaddingTop()+getPaddingBottom();
        int height = lines.size()*lineHeight+getPaddingTop()+getPaddingBottom();
        setMeasuredDimension(resolveSize(width, widthSpec), resolveSize(height, heightSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int i = 0;
        int y = top+getPaddingTop();
        int x = (left+right-lines.get(0))/2+getPaddingLeft();
        int used = 0;

        int n = getChildCount();
        for(int j=0;j<n;j++) {
            // Measure child
            View child = getChildAt(j);
            int width = child.getMeasuredWidth();
            int height = child.getMeasuredHeight();

            // Check fit
            if (used+width>lines.get(i)) {
                i++;
                y += lineHeight;
                x = (left+right-lines.get(i))/2;
                used = 0;
            }

            // Consume width
            child.layout(x, y, x+width,  y+height);
            x += width;
            used += width;
        }
    }
}
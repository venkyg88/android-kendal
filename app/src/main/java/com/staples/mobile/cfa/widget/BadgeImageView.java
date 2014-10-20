/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.staples.mobile.R;

/**
 * Created by sutdi001 on 10/10/14.
 *
 * Custom image view that draws text on top of image
 * (code borrowed from SDK's LabelView example)
 */
public class BadgeImageView extends ImageView {
    private Paint textPaint;
    private String text;
//    private int textLeftOffset;
//    private int textBottomOffset;

    /**
     * Constructor.  This version is only needed if you will be instantiating
     * the object manually (not from a layout XML file).
     * @param context
     */
    public BadgeImageView(Context context) {
        super(context);
        initView(context, null);
    }

    /**
     * Construct object, initializing with any attributes we understand from a
     * layout file. These attributes are defined in
     * SDK/assets/res/any/classes.xml.
     *
     * @see android.view.View#View(android.content.Context, android.util.AttributeSet)
     */
    public BadgeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public BadgeImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs);
    }


    private void initView(Context context, AttributeSet attrs) {
        text = "";
//        textLeftOffset = 2;
//        textBottomOffset = 2;

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(16);
        textPaint.setColor(Color.YELLOW);

        if (attrs != null) {

            // get params specified in layout
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BadgeImageView);

            // retrieve initial text and apply it
            CharSequence s = a.getString(R.styleable.BadgeImageView_android_text);
            if (s != null) {
                text = s.toString();
            }

            // retrieve text color and apply it
            textPaint.setColor(a.getColor(R.styleable.BadgeImageView_android_textColor,
                    textPaint.getColor()));

            // retrieve text size and apply it
            int textSize = a.getDimensionPixelOffset(R.styleable.BadgeImageView_android_textSize, 0);
            if (textSize > 0) {
                textPaint.setTextSize(textSize);
            }

            // retrieve text offsets and apply them
//            textLeftOffset = a.getDimensionPixelOffset(R.styleable.BadgeImageView_textLeftOffset, textLeftOffset);
//            textBottomOffset = a.getDimensionPixelOffset(R.styleable.BadgeImageView_textBottomOffset, textBottomOffset);

            a.recycle();
        }
    }


    /**
     * Renders the image with badge text
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
//        canvas.drawText(text, getPaddingLeft() + textLeftOffset,
//                getHeight() - getPaddingBottom() - textBottomOffset, textPaint);
        canvas.drawText(text, (getWidth() - getPaddingRight()) / 2 + getPaddingLeft()/2,
                (getHeight() - textPaint.ascent() - getPaddingBottom()) / 2 + getPaddingTop()/2, textPaint);
    }


    /**
     * Sets the text to display as the badge
     * @param text The text to display. This will be drawn as one line.
     */
    public void setText(String text) {
        this.text = text;
        requestLayout();
        invalidate();
    }

    /**
     * Sets the text size for the badge
     * @param size Font size
     */
    public void setTextSize(int size) {
        textPaint.setTextSize(size);
        requestLayout();
        invalidate();
    }

//    /**
//     * Sets the left and bottom offset for positioning text over the image
//     * @param leftOffset offset from the left
//     * @param bottomOffset offset from the bottom
//     */
//    public void setTextOffsets(int leftOffset, int bottomOffset) {
//        textLeftOffset = leftOffset;
//        textBottomOffset = bottomOffset;
//        requestLayout();
//        invalidate();
//    }

    /**
     * Sets the text color for the badge
     * @param color ARGB value for the text
     */
    public void setTextColor(int color) {
        textPaint.setColor(color);
        invalidate();
    }

}

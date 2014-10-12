package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.staples.mobile.R;

import java.text.DecimalFormat;

public class PriceSticker extends View {
    private static final String TAG = "StickerPrice";

    private static final DecimalFormat format = new DecimalFormat("$0.00");
    private Paint pricePaint;
    private Paint unitPaint;
    private Rect bounds = new Rect();

    private int baseline;
    private int height;

    private float price;
    private String unit;

    public PriceSticker(Context context) {
        this(context, null, 0);
    }

    public PriceSticker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PriceSticker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Preset default attributes
        int textSize = 10;

        // Get styled attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PriceSticker);
        int n = a.getIndexCount();
        for(int i=0;i<n;i++) {
            int index = a.getIndex(i);
            switch(index) {
                case R.styleable.PriceSticker_android_textSize:
                    textSize = a.getDimensionPixelSize(index, textSize);
            }
        }
        a.recycle();

        // Initialize paint
        pricePaint = new Paint();
        pricePaint.setAntiAlias(true);
        pricePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        pricePaint.setTextSize(textSize);
        pricePaint.setColor(0xff000000);

        unitPaint = new Paint();
        unitPaint.setAntiAlias(true);
        unitPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        unitPaint.setTextSize(0.65f*textSize);
        unitPaint.setColor(0xff666666);

        // Get metrics
        baseline = (int) -pricePaint.ascent();
        height = baseline + (int) pricePaint.descent();
    }

    public void setPrice(float price, String unit) {
        this.price = price;
        this.unit = unit;
        invalidate();
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        heightSpec = getPaddingTop() + height + getPaddingBottom();
        setMeasuredDimension(widthSpec, heightSpec);
    }

    @Override
    public void onDraw(Canvas canvas) {
        float x = getPaddingLeft();
        float y = getPaddingTop()+baseline;

        String text = format.format(price);
        pricePaint.getTextBounds(text, 0, text.length(), bounds);
        canvas.drawText(text, x, y, pricePaint);

        if (unit!=null) {
            x += bounds.right+height/4.0f;
            canvas.drawText(unit, x, y, unitPaint);
        }
    }
}

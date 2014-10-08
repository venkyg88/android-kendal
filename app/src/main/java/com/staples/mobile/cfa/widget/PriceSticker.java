package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.Resources;
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

    private static final DecimalFormat format = new DecimalFormat("$#.00");
    private static Paint pricePaint;
    private static Paint unitPaint;

    private static int baseline;
    private static int height;

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

        if (pricePaint == null) {
            Resources res = context.getResources();

            // Initialize paint
            pricePaint = new Paint();
            pricePaint.setAntiAlias(true);
            pricePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            pricePaint.setTextSize(res.getDimension(R.dimen.price_font_size));
            pricePaint.setColor(0xff000000);

            unitPaint = new Paint();
            unitPaint.setAntiAlias(true);
            unitPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            unitPaint.setTextSize(0.65f*res.getDimension(R.dimen.price_font_size));
            unitPaint.setColor(0xff666666);

            // Get metrics
            baseline = (int) -pricePaint.ascent();
            height = baseline + (int) pricePaint.descent();
        }
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
        Rect bounds = new Rect();
        pricePaint.getTextBounds(text, 0, text.length(), bounds);
        canvas.drawText(text, x, y, pricePaint);

        if (unit!=null) {
            x += bounds.right+height/4.0f;
            canvas.drawText(unit, x, y, unitPaint);
        }
    }
}

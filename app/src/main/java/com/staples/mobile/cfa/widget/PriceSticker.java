package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.model.browse.Pricing;

import java.text.NumberFormat;
import java.util.List;

/**
 * <b>XML attributes</b>
 * majorTextSize,
 * majorTextColor,
 * minorTextSize,
 * minorTextColor,
 * android:gravity
 */
public class PriceSticker extends View {
    private static final String TAG = "StickerPrice";

    private static final NumberFormat format = NumberFormat.getCurrencyInstance();
    private Paint majorPaint;
    private Paint minorPaint;

    private int gravity;
    private int baseline;
    private int majorHeight;

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
        int majorTextSize = 20;
        int minorTextSize = 16;
        int majorTextColor = 0xff000000;
        int minorTextColor = 0xff000000;
        gravity = Gravity.LEFT;

        // Get styled attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PriceSticker);
        int n = a.getIndexCount();
        for(int i=0;i<n;i++) {
            int index = a.getIndex(i);
            switch(index) {
                case R.styleable.PriceSticker_majorTextSize:
                    majorTextSize = a.getDimensionPixelSize(index, majorTextSize);
                    break;
                case R.styleable.PriceSticker_minorTextSize:
                    minorTextSize = a.getDimensionPixelSize(index, minorTextSize);
                    break;
                case R.styleable.PriceSticker_majorTextColor:
                    majorTextColor = a.getColor(index, majorTextColor);
                    break;
                case R.styleable.PriceSticker_minorTextColor:
                    minorTextColor = a.getColor(index, minorTextColor);
                    break;
                case R.styleable.PriceSticker_android_gravity:
                    gravity = a.getInt(index, gravity)&Gravity.HORIZONTAL_GRAVITY_MASK;
                    break;
            }
        }
        a.recycle();

        // Initialize paints
        majorPaint = new Paint();
        majorPaint.setAntiAlias(true);
        majorPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        majorPaint.setTextSize(majorTextSize);
        majorPaint.setColor(majorTextColor);

        minorPaint = new Paint();
        minorPaint.setAntiAlias(true);
        minorPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        minorPaint.setTextSize(minorTextSize);
        minorPaint.setColor(minorTextColor);

        // Get metrics
        baseline = (int) -majorPaint.ascent();
        majorHeight = baseline + (int) majorPaint.descent();
    }

    public void setPricing(float price, String unit) { // TODO old code
        this.price = price;
        this.unit = unit;
        if (this.unit!=null && this.unit.isEmpty()) this.unit = null;
        invalidate();
    }

    public boolean setPricing(List<Pricing> pricings) {
        if (pricings==null) return(false);
        for(Pricing pricing : pricings) {
            if (setPricing(pricing)) return (true);
        }
        return (false);
    }

    public boolean setPricing(Pricing pricing) {
        if (pricing==null) return(false);
        price = pricing.getFinalPrice();
        unit = pricing.getUnitOfMeasure();
        if (unit!=null && unit.isEmpty()) unit = null;
        invalidate();
        return(true);
    }

    public float getPrice() {
        return price;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        float x = getPaddingLeft() + getPaddingRight();
        String text = format.format(price);
        x += majorPaint.measureText(text, 0, text.length());
        if (unit!=null) {
            text = " " + unit;
            x += minorPaint.measureText(text, 0, text.length());
        }
        int height = getPaddingTop() + majorHeight + getPaddingBottom();
        int width = resolveSize((int) Math.ceil(x), widthSpec);
        height = resolveSize(height, heightSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        String unitText = null;

        float slack = getWidth()-getPaddingLeft()-getPaddingRight();

        String priceText = format.format(price);
        float priceWidth = majorPaint.measureText(priceText, 0, priceText.length());
        slack -= priceWidth;

        if (unit!=null) {
            unitText = " " + unit;
            slack -= minorPaint.measureText(unitText, 0, unitText.length());
        }

        // Apply gravity
        float x = getPaddingLeft();
        if (gravity==Gravity.CENTER_HORIZONTAL) x += slack/2.0f;
        else if (gravity==Gravity.RIGHT) x += slack;
        float y = getPaddingTop()+baseline;

        // Draw texts
        canvas.drawText(priceText, x, y, majorPaint);
        if (unit!=null) {
            x += priceWidth;
            canvas.drawText(unitText, x, y, minorPaint);
        }
    }
}

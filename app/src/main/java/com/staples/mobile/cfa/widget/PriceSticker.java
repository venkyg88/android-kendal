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
    private static final String TAG = PriceSticker.class.getSimpleName();

    private static final NumberFormat format = NumberFormat.getCurrencyInstance();

    private Paint majorPaint;
    private Paint wasPaint;
    private Paint unitPaint;

    private int gravity;
    private int baseline;
    private int majorHeight;
    private int wasGap;
    private int unitGap;

    private int[] widths;

    private float finalPrice;
    private float wasPrice;
    private String unit;
    private String rebateIndicator;

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
        int majorTextColor = context.getResources().getColor(R.color.staples_black);
        int minorTextColor = majorTextColor;
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

        wasPaint = new Paint();
        wasPaint.setAntiAlias(true);
        wasPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        wasPaint.setTextSize(minorTextSize);
        wasPaint.setColor(minorTextColor);
        wasPaint.setFlags(wasPaint.getFlags()|Paint.STRIKE_THRU_TEXT_FLAG);

        unitPaint = new Paint();
        unitPaint.setAntiAlias(true);
        unitPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        unitPaint.setTextSize(minorTextSize);
        unitPaint.setColor(minorTextColor);

        // Get metrics
        baseline = (int) -majorPaint.ascent();
        majorHeight = baseline + (int) majorPaint.descent();
        wasGap = (int) wasPaint.measureText(" ");
        unitGap = (int) unitPaint.measureText(" ");

        widths = new int[3];
    }

    public void setPricing(float finalPrice, float wasPrice, String unit, String rebateIndicator) { // TODO old code
        this.finalPrice = finalPrice;
        this.wasPrice = wasPrice;
        this.unit = unit;
        if(rebateIndicator!=null) this.rebateIndicator = rebateIndicator;
        if (this.unit!=null && this.unit.isEmpty()) this.unit = null;
        invalidate();
    }

    // Browse pricing

    public boolean setBrowsePricing(List<com.staples.mobile.common.access.easyopen.model.browse.Pricing> pricings) {
        if (pricings==null) return(false);
        for(com.staples.mobile.common.access.easyopen.model.browse.Pricing pricing : pricings) {
            if (setPricing(pricing)) return (true);
        }
        return (false);
    }

    public boolean setPricing(com.staples.mobile.common.access.easyopen.model.browse.Pricing pricing) {
        if (pricing==null) return(false);
        finalPrice = pricing.getFinalPrice();
        wasPrice = pricing.getListPrice();
        unit = pricing.getUnitOfMeasure();
        if (unit!=null && unit.isEmpty()) unit = null;
        invalidate();
        return(true);
    }

    // Cart pricing

    public boolean setCartPricing(List<com.staples.mobile.common.access.easyopen.model.cart.Pricing> pricings) {
        if (pricings==null) return(false);
        for(com.staples.mobile.common.access.easyopen.model.cart.Pricing pricing : pricings) {
            if (setPricing(pricing)) return (true);
        }
        return(false);
    }

    public boolean setPricing(com.staples.mobile.common.access.easyopen.model.cart.Pricing pricing) {
        if (pricing==null) return(false);
        finalPrice = pricing.getFinalPrice();
        wasPrice = pricing.getListPrice();
        unit = pricing.getUnitOfMeasure();
        if (unit!=null && unit.isEmpty()) unit = null;
        invalidate();
        return(true);
    }

    public float getFinalPrice() {
        return(finalPrice);
    }

    private void measureWidths() {
        int width = 0;
        if (finalPrice>0.0f) {
            String text = format.format(finalPrice);
            if (rebateIndicator!=null) {
                text += rebateIndicator;
            }
            width += majorPaint.measureText(text);
        }
        if (wasPrice>0.0f && wasPrice!=finalPrice) {
            width += wasGap;
            widths[0] = width;
            String text = format.format(wasPrice);
            width += wasPaint.measureText(text);
        }
        if (unit!=null) {
            width += unitGap;
            widths[1] = width;
            width += unitPaint.measureText(unit);
        }
        widths[2] = width;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        measureWidths();
        int height = getPaddingTop()+majorHeight+getPaddingBottom();
        int width = getPaddingLeft()+widths[2]+getPaddingRight();
        setMeasuredDimension(resolveSize(width, widthSpec), resolveSize(height, heightSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int slack = getWidth()-getPaddingLeft()-getPaddingRight()-widths[2];

        // Apply gravity
        float x = getPaddingLeft();
        if (gravity==Gravity.CENTER_HORIZONTAL) x += slack/2.0f;
        else if (gravity==Gravity.RIGHT) x += slack;
        float y = getPaddingTop()+baseline;

        // Draw texts
        if (finalPrice>0.0f) {
            String text = format.format(finalPrice);
            if (rebateIndicator!=null) {
                text += rebateIndicator;
            }
            canvas.drawText(text, x, y, majorPaint);
        }
        if (wasPrice>0.0f && wasPrice!=finalPrice) {
            String text = format.format(wasPrice);
            canvas.drawText(text, x+widths[0], y, wasPaint);
        }
        if (unit!=null) {
            canvas.drawText(unit, x+widths[1], y, unitPaint);
        }
    }
}

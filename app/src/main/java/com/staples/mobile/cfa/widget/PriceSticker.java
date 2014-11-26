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

public class PriceSticker extends View {
    private static final String TAG = "StickerPrice";

    private static final NumberFormat format = NumberFormat.getCurrencyInstance();
    private Paint pricePaint;
    private Paint unitPaint;

    private int gravity;
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
        gravity = Gravity.LEFT;

        // Get styled attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PriceSticker);
        int n = a.getIndexCount();
        for(int i=0;i<n;i++) {
            int index = a.getIndex(i);
            switch(index) {
                case R.styleable.PriceSticker_android_textSize:
                    textSize = a.getDimensionPixelSize(index, textSize);
                    break;
                case R.styleable.RatingStars_android_gravity:
                    gravity = a.getInt(index, gravity)&Gravity.HORIZONTAL_GRAVITY_MASK;
                    break;
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

    public void setPricing(float price, String unit) { // TODO old code
        this.price = price;
        this.unit = unit;
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
        invalidate();
        return(true);
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        heightSpec = getPaddingTop() + height + getPaddingBottom();
        setMeasuredDimension(widthSpec, heightSpec);
    }

    @Override
    public void onDraw(Canvas canvas) {
        String unitText = null;

        float slack = getWidth()-getPaddingLeft()-getPaddingRight();

        String priceText = format.format(price);
        float priceWidth = pricePaint.measureText(priceText, 0, priceText.length());
        slack -= priceWidth;

        if (unit!=null) {
            unitText = " " + unit;
            slack -= unitPaint.measureText(unitText, 0, unitText.length());
        }

        // Apply gravity
        float x = getPaddingLeft();
        if (gravity==Gravity.CENTER_HORIZONTAL) x += slack/2.0f;
        else if (gravity==Gravity.RIGHT) x += slack;
        float y = getPaddingTop()+baseline;

        // Draw texts
        canvas.drawText(priceText, x, y, pricePaint);
        if (unit!=null) {
            x += priceWidth;
            canvas.drawText(unitText, x, y, unitPaint);
        }
    }
}

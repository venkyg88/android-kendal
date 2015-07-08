package app.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.text.DecimalFormat;
import java.util.ArrayList;

import app.staples.R;
import app.staples.mobile.cfa.util.MiscUtils;

public class IndicatorBlock extends View {
    private static final String TAG = IndicatorBlock.class.getSimpleName();

    private class Indicator {
        private String text;
        private int color;
    }

    private ArrayList<Indicator> array;
    private RectF rect;
    private Paint cellPaint;
    private Paint textPaint;
    private float cornerRadius;
    private int spacingGap;

    private int flags = -1; // TODO Hacked

    public IndicatorBlock(Context context) {
        this(context, null, 0);
    }

    public IndicatorBlock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorBlock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        array = new ArrayList<Indicator>();
        rect = new RectF();

        // Set default values
        int textSize = 10;
        int textColor = 0xff000000;
        cornerRadius = 0f;
        spacingGap = 0;

        // Get styled attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IndicatorBlock);
        int n = a.getIndexCount();
        for(int i=0;i<n;i++) {
            int index = a.getIndex(i);
            switch(index) {
                case R.styleable.IndicatorBlock_android_textSize:
                    textSize = a.getDimensionPixelSize(index, textSize);
                    break;
                case R.styleable.IndicatorBlock_android_textColor:
                    textColor = a.getInt(index, textColor);
                    break;
                case R.styleable.IndicatorBlock_cornerRadius:
                    cornerRadius = a.getDimension(index, cornerRadius);
                    break;
                case R.styleable.IndicatorBlock_spacingGap:
                    spacingGap = a.getDimensionPixelSize(index, spacingGap);
                    break;
            }
        }
        a.recycle();

        // Make Paints
        cellPaint = new Paint();
        cellPaint.setAntiAlias(true);
        cellPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        textPaint.setTextSize(textSize);
        textPaint.setColor(textColor);
    }

    public void reset() {
        array.clear();
        requestLayout();
    }

    public void addIndicator(int textId, int colorId) {
        addPricedIndicator(0f, textId, colorId);
    }

    public void addPricedIndicator(float price, int textId, int colorId) {
        Resources res = getResources();
        Indicator item = new Indicator();
        if (price>0f) {
            StringBuilder sb = new StringBuilder();
            DecimalFormat format = MiscUtils.getIntegerCurrencyFormat();
            sb.append(format.format(price));
            sb.append(" ");
            sb.append(res.getString(textId));
            item.text = sb.toString();
        } else {
            item.text = res.getString(textId);
        }
        item.color = res.getInteger(colorId);
        array.add(item);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = 0;
        for(Indicator item : array) {
            if (width>0) width += spacingGap;
            width += textPaint.measureText(item.text)+getPaddingLeft()+getPaddingRight();
        }
        int height = (int) textPaint.getTextSize()+getPaddingTop()+getPaddingBottom();
        setMeasuredDimension(resolveSize(width, widthSpec), resolveSize(height, heightSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (flags==0) return;
        rect.top = 0;
        rect.bottom = textPaint.getTextSize()+getPaddingTop()+getPaddingBottom();
        rect.left = 0;
        float y = -0.9f*textPaint.ascent()+getPaddingTop();
        for(Indicator item : array) {
            // Draw background
            rect.right = rect.left+(int) textPaint.measureText(item.text)+getPaddingLeft()+getPaddingRight();
            cellPaint.setColor(item.color);
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, cellPaint);

            // Draw text
            canvas.drawText(item.text, rect.left+getPaddingLeft(), y, textPaint);
            rect.left = rect.right+spacingGap;
        }
    }
}

package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.EditText;

import com.staples.mobile.cfa.R;

public class DualHintEdit extends EditText {
    private static final String TAG = "FloatHintEdit";

    private TextPaint dualPaint;
    private int dualSize;
    private int dualGap;
    private int dualX;
    private int dualY;

    // Constructors

    public DualHintEdit(Context context) {
        super(context);
        init(context, null);
    }

    public DualHintEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public DualHintEdit(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Preset default attributes
        Typeface dualTypeface = getTypeface();
        dualSize = (int) (0.75f*getTextSize());
        dualGap = 0;
        int dualColor = 0xff0000ff;

        // Get styled attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DualHintEdit);
        int n = a.getIndexCount();
        for(int i=0;i<n;i++) {
            int index = a.getIndex(i);
            switch(index) {
                case R.styleable.DualHintEdit_textDualSize:
                    dualSize = a.getDimensionPixelSize(index, dualSize);
                    break;
                case R.styleable.DualHintEdit_textDualGap:
                    dualGap = a.getDimensionPixelSize(index, dualGap);
                    break;
                case R.styleable.DualHintEdit_textDualColor:
                    dualColor = a.getInt(index, dualColor);
                    break;
            }
        }
        a.recycle();

        // Set paint
        dualPaint = new TextPaint();
        dualPaint.setAntiAlias(true);
        dualPaint.setTypeface(dualTypeface);
        dualPaint.setTextSize(dualSize);
        dualPaint.setColor(dualColor);

        // Set metrics
        dualX = getPaddingLeft();
        dualY = getPaddingTop()-(int) dualPaint.ascent();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        height += dualSize+dualGap;
        setMeasuredDimension(width, resolveSize(height, heightSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        CharSequence text = getText();
        if (text!=null && text.length()>0) {
            CharSequence hint = getHint();
            if (hint!=null) {
                canvas.drawText(hint, 0, hint.length(), dualX, dualY, dualPaint);
            }
        }
    }
}

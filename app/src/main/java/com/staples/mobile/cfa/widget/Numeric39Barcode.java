/*
 * Copyright (c) 2015 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.staples.mobile.cfa.R;

import java.util.HashMap;

/**
 * Created by sutdi001 on 2/13/15.
 */
public class Numeric39Barcode extends View {

    private static final String TAG = TextView.class.getSimpleName();

    private static final int LINE_WIDTH = 2;
    private static final int WIDELINE_WIDTH = LINE_WIDTH*3; // 3-to-1 ratio between wide and narrow lines
    private static final int SYMBOL_WIDTH = LINE_WIDTH*3 +      // 3 narrow lines
                                            WIDELINE_WIDTH*3 +  // 2 wide lines and 1 wide space
                                            LINE_WIDTH*4;       // 4 narrow spaces

    private static HashMap<Character, String> mapChars;
    static {
        /* In the following notation, 0 represents a narrow black bar, 1 represents a wide black
         * bar, and the dash represents a narrow blank bar (plus dividers),
         * as described in http://howto.wired.com/wiki/Read_a_Barcode */
        mapChars = new HashMap<Character, String>();
        mapChars.put('*', "0-0110"); // asterisk is used as the start and end character
        mapChars.put('0', "00-110");
        mapChars.put('1', "10-001");
        mapChars.put('2', "01-001");
        mapChars.put('3', "11-000");
        mapChars.put('4', "00-101");
        mapChars.put('5', "10-100");
        mapChars.put('6', "01-100");
        mapChars.put('7', "00-011");
        mapChars.put('8', "10-010");
        mapChars.put('9', "01-010");
    }

//    private Paint narrowLinePaint;
//    private Paint wideLinePaint;
    private Paint linePaint;
    private int barcodeHeight;
    private int barcodeColor;
    private String text;
    private int startingXPosition;
    private int startingYPosition;
    private int desiredWidth;
    private int desiredHeight;

    // initialize widths based on defaults, adjust later if small screen
    private int lineWidth = LINE_WIDTH;
    private int wideLineWidth = WIDELINE_WIDTH;
    private int symbolWidth = SYMBOL_WIDTH;

    // Constructors

    public Numeric39Barcode(Context context) {
        super(context);
        init(context, null);
    }

    public Numeric39Barcode(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Numeric39Barcode(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void setText(String text) throws NumberFormatException {
        // make sure text is numeric
        long numericValue = Long.parseLong(text);
        this.text = text;
    }

    private void init(Context context, AttributeSet attrs) {
        // Preset default attributes
        barcodeHeight = 24;
        barcodeColor = R.color.text_black;

        // Get styled attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Numeric39Barcode);
        try {
            barcodeHeight = a.getDimensionPixelSize(R.styleable.Numeric39Barcode_barcodeHeight, barcodeHeight);
            barcodeColor = a.getInt(R.styleable.Numeric39Barcode_barcodeColor, barcodeColor);
        } finally {
            a.recycle();
        }

        // create paint object
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(barcodeColor);
        linePaint.setStyle(Paint.Style.FILL);

        // Set metrics
        startingXPosition = getPaddingLeft();
        startingYPosition = getPaddingTop();
    }

    // these exist just for clarity, even though divider widths are the same as line widths
    private int getDividerWidth() { return lineWidth; }
    private int getWideDividerWidth() { return wideLineWidth; }

    private void drawCharacter(Canvas canvas, int x, int y, char c) {
        String charCode = mapChars.get(c);
        if (charCode != null) {
            for (int i = 0; i < charCode.length(); i++) {
                char codeElement = charCode.charAt(i);
                switch (codeElement) {
                    case '0': // narrow line
                        canvas.drawRect(x, y, x + lineWidth, y + barcodeHeight, linePaint);
                        x += (lineWidth + getDividerWidth());
                        break;
                    case '1': // wide line
                        canvas.drawRect(x, y, x + wideLineWidth, y + barcodeHeight, linePaint);
                        x += (wideLineWidth + getDividerWidth());
                        break;
                    case '-': // wide space
                        x += (getWideDividerWidth() - getDividerWidth());  // subtracting off already added diverWidth from last character
                        break;
                }
            }
        }
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        int barcodeSymbolCount = 2; // we'll at least have begin and end chars
        if (!TextUtils.isEmpty(text)) {
            barcodeSymbolCount += text.length();
        }
        desiredWidth = getPaddingLeft() + getPaddingRight() + (barcodeSymbolCount * symbolWidth);
        desiredHeight = getPaddingTop() + getPaddingBottom() + barcodeHeight;
        setMeasuredDimension(resolveSize(desiredWidth, widthSpec), resolveSize(desiredHeight, heightSpec));
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = startingXPosition;
        int y = startingYPosition;

        // make adjustments based on actual dimensions
        if (getWidth() < desiredWidth) {
            lineWidth = LINE_WIDTH/2;
            wideLineWidth = WIDELINE_WIDTH/2;
            symbolWidth = SYMBOL_WIDTH/2;
        }
        if (getHeight() < desiredHeight) {
            barcodeHeight -= (desiredHeight - getHeight());
        }

        drawCharacter(canvas, x, y, '*');
        x += symbolWidth;
        if (text != null) {
            for (int i = 0; i < text.length(); i++) {
                drawCharacter(canvas, x, y, text.charAt(i));
                x += symbolWidth;
            }
        }
        drawCharacter(canvas, x, y, '*');
    }
}

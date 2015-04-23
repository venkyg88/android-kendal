package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.staples.mobile.cfa.R;

/**
 * Custom image view that draws text on top of image
 * (code borrowed from SDK's LabelView example)
 */
public class BadgeImageView extends ImageView {
    private Paint textPaint;
    private Paint circlePaint;
    private String text;

    public BadgeImageView(Context context) {
        this(context, null, 0);
    }

    public BadgeImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BadgeImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Resources r = context.getResources();

        text = "";
        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(16);
        textPaint.setColor(r.getColor(R.color.staples_red));

        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(r.getColor(R.color.staples_white));

        // get params specified in layout
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BadgeImageView);

        // retrieve initial text and apply it
        String s = a.getString(R.styleable.BadgeImageView_android_text);
        if (s != null) {
            text = s;
        }

        // retrieve text color and apply it
        textPaint.setColor(a.getColor(R.styleable.BadgeImageView_android_textColor,
                textPaint.getColor()));

        // retrieve circle color and apply it
        circlePaint.setColor(a.getColor(R.styleable.BadgeImageView_circleColor, circlePaint.getColor()));

        // retrieve text size and apply it
        int textSize = a.getDimensionPixelOffset(R.styleable.BadgeImageView_android_textSize, 0);
        if (textSize > 0) {
            textPaint.setTextSize(textSize);
        }

        a.recycle();
    }

    /**
     * Renders the image with badge text
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!TextUtils.isEmpty(text)) {

            // using circle
//            float textHeight = textPaint.getFontSpacing();  // textPaint.getFontSpacing(), -textPaint.ascent()
//            float textWidth = textPaint.measureText(text);
//            float radius = Math.max(textWidth, textHeight) * 0.7f; // set radius to 70% of text size
//            float x = getWidth() - radius;
//            float y = radius;
//            canvas.drawCircle(x, y, radius, circlePaint);
//            canvas.drawText(text, x, y - textPaint.ascent()/2 - 2, textPaint);

            // using oval
            float ovalHeight = textPaint.getFontSpacing() * 1.2f; // textPaint.getFontSpacing() , textPaint.ascent() * -1.2f
            float ovalWidth = textPaint.measureText(text) * 1.4f;
            ovalWidth = Math.max(ovalWidth, ovalHeight); // make width at least as much as height
            float x = getWidth() - ovalWidth / 2;
            float y = ovalHeight / 2;
            RectF rectF = new RectF(getWidth() - ovalWidth, 0f, (float) getWidth(), ovalHeight); //RectF(float left, float top, float right, float bottom)
            canvas.drawOval(rectF, circlePaint);
            canvas.drawText(text, x, y - textPaint.ascent()/2 - 2, textPaint); // using arbitrary offset of 2 to make text appear more vertically centered
        }
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

    /**
     * Sets the text color for the badge
     * @param color ARGB value for the text
     */
    public void setTextColor(int color) {
        textPaint.setColor(color);
        invalidate();
    }

}

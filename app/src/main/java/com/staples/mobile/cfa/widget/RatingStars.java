package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.staples.mobile.R;

public class RatingStars extends View {
    public static Bitmap fullStar;
    public static Bitmap halfStar;
    public static Bitmap emptyStar;
    public static int width;
    public static int height;
    public static Paint paint;

    private float rating;
    private int count;

    public RatingStars(Context context) {
        this(context, null, 0);
    }

    public RatingStars(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatingStars(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        if (paint == null) {
            // Load drawables
            Resources res = context.getResources();
            fullStar = BitmapFactory.decodeResource(res, R.drawable.full_star);
            halfStar = BitmapFactory.decodeResource(res, R.drawable.half_star);
            emptyStar = BitmapFactory.decodeResource(res, R.drawable.empty_star);

            // Get dimensions
            width = fullStar.getWidth();
            height = fullStar.getHeight();

            // Initialize paint
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            paint.setTextSize(3 * height / 4);
            paint.setColor(0xff000000);
        }
    }

    public void setRating(float rating, int count) {
        this.rating = rating;
        this.count = count;
        invalidate();
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {

        heightSpec = getPaddingTop() + height + getPaddingBottom();
        setMeasuredDimension(widthSpec, heightSpec);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Bitmap b;

        float x = getPaddingLeft();
        float y = getPaddingTop();

        float f = rating;
        for(int i=0;i<5;i++) {
            if (f>=0.75f) b = fullStar;
            else if (f>=0.25f) b = halfStar;
            else b = emptyStar;
            canvas.drawBitmap(b, x, y, paint);

            x += width;
            f -= 1.0f;
        }

        String text = " (" + count + ")";
        canvas.drawText(text, x, y+3*height/4, paint);
    }
}

package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.staples.mobile.R;

public class RatingStars extends View {
    public static final String TAG = "RatingStars";

    private static final double DELTA = Math.PI/5.0; // I love geometry
    private static final double INDENTFACTOR = 2.0/(Math.sqrt(5.0)+3.0); // 0.381966
    private static final double WIDTHFACTOR = Math.sqrt(2.0*Math.sqrt(5)+10.0)/4.0; // 0.951056

    private static final int RED = 0xffff0000;
    private static final int GRAY = 0xffcccccc;
    private static final int TEXTCOLOR = 0xff666666;

    private static Bitmap fullStar;
    private static Bitmap halfStar;
    private static Bitmap emptyStar;

    private static Paint textPaint;

    private static int width;
    private static int height;

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

        if (fullStar == null) {
            Resources res = context.getResources();
            int textSize = res.getDimensionPixelSize(R.dimen.rating_star_size);

            // Initialize paint
            textPaint = new Paint();
            textPaint.setAntiAlias(true);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            textPaint.setTextSize(textSize);
            textPaint.setColor(TEXTCOLOR);

            // Metrics
            height = (int) (1.25*textSize);
            width = (int) Math.ceil(WIDTHFACTOR*height);
            double radius = 0.49*height;
            drawStars(radius);
        }
    }

    public void setRating(float rating, int count) {
        this.rating = rating;
        this.count = count;
        invalidate();
    }

    private void getVertex(double radius, int index, PointF p) {
        double theta = index*DELTA;
        if ((index&1)>0) radius *= INDENTFACTOR;
        p.x = (float) (radius*Math.sin(theta)+width/2.0);
        p.y = (float) (-radius*Math.cos(theta)+height/2.0);
    }

    private Path getPath(double radius, int start, int end) {
        Path path = new Path();
        PointF p = new PointF();

        int index = start;
        getVertex(radius, index, p);
        path.moveTo(p.x, p.y);

        for(index++;index<=end;index++) {
            getVertex(radius, index, p);
            path.lineTo(p.x, p.y);
        }

        path.close();
        return(path);
    }

    private void drawStars(double radius) {
        Path path;

        Canvas canvas = new Canvas();

        Paint paint = new Paint();
        textPaint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        fullStar = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(fullStar);
        path = getPath(radius, 0, 9);
        paint.setColor(RED);
        canvas.drawPath(path, paint);

        emptyStar = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(emptyStar);
        paint.setColor(GRAY);
        canvas.drawPath(path, paint);

        halfStar = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(halfStar);
        path = getPath(radius, 5, 10);
        paint.setColor(RED);
        canvas.drawPath(path, paint);

        path = getPath(radius, 0, 5);
        paint.setColor(GRAY);
        canvas.drawPath(path, paint);
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

        // Draw 5 assorted stars
        float f = rating;
        for(int i=0;i<5;i++) {
            if (f>=0.75f) b = fullStar;
            else if (f>=0.25f) b = halfStar;
            else b = emptyStar;
            canvas.drawBitmap(b, x, y, null);

            x += width;
            f -= 1.0f;
        }

        // Draw customer count
        y += (height-0.7f*textPaint.ascent())/2.0f; // Fudge for visual centering
        String text = " (" + count + ")";
        canvas.drawText(text, x, y, textPaint);
    }
}

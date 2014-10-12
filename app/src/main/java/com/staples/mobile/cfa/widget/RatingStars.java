package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.staples.mobile.R;

/**
 * This class does all the drawing required to display a star rating.
 * It supports multiple size configurations (up to MAXCONFIGS).
 * Star and text size is scaled to the android:textSize specified.
 * The main method is setRating(float rating, int count);
 * rating is 0.0 to 5.0 but handles clipping.
 */
public class RatingStars extends View {
    public static final String TAG = "RatingStars";

    private static final double DELTA = Math.PI/5.0; // I love geometry
    private static final double INDENTFACTOR = 2.0/(Math.sqrt(5.0)+3.0); // 0.381966
    private static final double WIDTHFACTOR = Math.sqrt(2.0*Math.sqrt(5)+10.0)/4.0; // 0.951056

    private static final int REDSTAR = 0xffff0000;
    private static final int GRAYSTAR = 0xffcccccc;
    private static final int TEXTCOLOR = 0xff666666;

    private static final int MAXCONFIGS = 4;
    private static final Config[] configs = new Config[MAXCONFIGS];

    private Config config;
    private float rating;
    private int count;

    private Rect src = new Rect();
    private Rect dst = new Rect();

    public RatingStars(Context context) {
        this(context, null, 0);
    }

    public RatingStars(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatingStars(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Preset default attributes
        int textSize = 10;

        // Get styled attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatingStars);
        int n = a.getIndexCount();
        for(int i=0;i<n;i++) {
            int index = a.getIndex(i);
            switch(index) {
                case R.styleable.RatingStars_android_textSize:
                    textSize = a.getDimensionPixelSize(index, textSize);
            }
        }
        a.recycle();

        // Find or create compatible configuration
        for(int i=0;i< MAXCONFIGS;i++) {
            if (configs[i]==null) {
                config = new Config(textSize);
                configs[i] = config;
                break;
            }
            if (configs[i].textSize==textSize) {
                config = configs[i];
                break;
            }
        }
        if (config==null) throw(new RuntimeException("Could not create new RatingStars.Config"));
    }

    private static class Config {
        private int textSize;
        private int width;
        private int height;
        private Bitmap bitmap;
        private Paint textPaint;

        private Config(int textSize) {
            // Metrics
            this.textSize = textSize;
            height = (int) (1.25 * textSize);
            width = (int) Math.ceil(WIDTHFACTOR * height);

            // Draw stars
            double radius = height/2.0;
            drawStars(radius);

            // Initialize paint
            textPaint = new Paint();
            textPaint.setAntiAlias(true);
            textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            textPaint.setTextSize(textSize);
            textPaint.setColor(TEXTCOLOR);
        }

        private void getVertex(PointF p, double radius, int index) {
            double theta = index*DELTA;
            if ((index&1)>0) radius *= INDENTFACTOR;
            p.x = (float) (radius*Math.sin(theta)+width/2.0);
            p.y = (float) (-radius*Math.cos(theta)+height/2.0);
        }

        private void fillPath(Path path, double radius, int offset, int start, int end) {
            PointF p = new PointF();

            path.rewind();

            int index = start;
            getVertex(p, radius, index);
            path.moveTo(p.x+offset, p.y);

            for(index++;index<=end;index++) {
                getVertex(p, radius, index);
                path.lineTo(p.x+offset, p.y);
            }

            path.close();
        }

        private void drawStars(double radius) {
            Canvas canvas = new Canvas();
            Path path = new Path();

            bitmap = Bitmap.createBitmap(3*width, height, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(bitmap);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);

            paint.setColor(REDSTAR);
            fillPath(path, radius, 0, 0, 9);
            canvas.drawPath(path, paint);

            fillPath(path, radius, width, 5, 10);
            canvas.drawPath(path, paint);

            paint.setColor(GRAYSTAR);
            fillPath(path, radius, width, 0, 5);
            canvas.drawPath(path, paint);

            fillPath(path, radius, 2*width, 0, 9);
            canvas.drawPath(path, paint);
        }
    }

    public void setRating(float rating, int count) {
        this.rating = rating;
        this.count = count;
        invalidate();
    }

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        heightSpec = getPaddingTop()+config.height+getPaddingBottom();
        setMeasuredDimension(widthSpec, heightSpec);
    }

    @Override
    public void onDraw(Canvas canvas) {
        src.top = 0;
        src.bottom = config.height;

        dst.left = getPaddingLeft();
        dst.top = getPaddingTop();
        dst.bottom = dst.top+config.height;

        // Draw 5 assorted stars
        float f = rating;
        for(int i=0;i<5;i++) {
            // Get source rectangle
            if (f>=0.75f) src.left = 0;
            else if (f>=0.25f) src.left = config.width;
            else src.left = 2*config.width;
            src.right = src.left+config.width;

            // Get destination rectangle
            dst.right = dst.left+config.width;

            // Draw and increment
            canvas.drawBitmap(config.bitmap, src, dst, null);
            dst.left += config.width;
            f -= 1.0f;
        }

        // Draw customer count
        dst.top += (config.height-0.7f*config.textPaint.ascent())/2.0f; // Fudge for visual centering
        String text = " (" + count + ")";
        canvas.drawText(text, dst.left, dst.top, config.textPaint);
    }
}

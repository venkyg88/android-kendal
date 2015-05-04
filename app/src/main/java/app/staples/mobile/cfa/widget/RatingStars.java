package app.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import app.staples.mobile.cfa.R;

/**
 * This class does all the drawing required to display a star rating and review count.
 * It supports multiple size configurations (up to MAXCONFIGS).
 * Star and text size is scaled to the android:textSize specified.
 * The main method is setRating(float rating, int reviews);
 * rating is 0.0 to 5.0 but handles clipping.
 * reviews is the integer count or null for nothing to display
 */
public class RatingStars extends View {
    private static final String TAG = RatingStars.class.getSimpleName();

    private static final int NSTARS = 5;
    private static final double DELTA = Math.PI/5.0; // I love geometry
    private static final double INDENTFACTOR = 2.0/(Math.sqrt(5.0)+3.0); // 0.381966
    private static final double WIDTHFACTOR = Math.sqrt(2.0*Math.sqrt(5)+10.0)/4.0; // 0.951056

    private static int REDSTAR;
    private static int GRAYSTAR;
    private static int TEXTCOLOR;

    private static final int MAXCONFIGS = 4;
    private static final Config[] configs = new Config[MAXCONFIGS];

    private Config config;
    private int gravity;
    private float rating;
    private Integer reviews;
    private String noReviews;

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

        Resources r = context.getResources();
        REDSTAR = r.getColor(R.color.staples_red);
        GRAYSTAR = r.getColor(R.color.staples_middle_gray);
        TEXTCOLOR = r.getColor(R.color.staples_dark_gray);

        // Preset default attributes
        int textSize = 10;
        gravity = Gravity.LEFT;

        // Get styled attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatingStars);
        int n = a.getIndexCount();
        for(int i=0;i<n;i++) {
            int index = a.getIndex(i);
            switch(index) {
                case R.styleable.RatingStars_android_textSize:
                    textSize = a.getDimensionPixelSize(index, textSize);
                    break;
                case R.styleable.RatingStars_android_gravity:
                    gravity = a.getInt(index, gravity)&Gravity.HORIZONTAL_GRAVITY_MASK;
                    break;
                case R.styleable.RatingStars_noReviews:
                    noReviews = a.getString(index);
                    break;
            }
        }
        a.recycle();

        // Find or create compatible configuration
        int i;
        for(i=0;i<MAXCONFIGS;i++) {
            if (configs[i]==null) break;
            if (configs[i].textSize==textSize) {
                config = configs[i];
                break;
            }
        }
        if (config==null) {
            config = new Config(textSize);
            if (i<MAXCONFIGS) {
                configs[i] = config;
            }
        }
    }

    private static class Config {
        private int textSize;
        private Paint textPaint;

        private int starWidth;
        private int starHeight;
        private Bitmap starBitmap;

        private Config(int textSize) {
            // Metrics
            this.textSize = textSize;
            starHeight = (int) (1.25*textSize);
            starWidth = (int) Math.ceil(WIDTHFACTOR*starHeight);

            // Draw stars
            double radius = starHeight/2.0;
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
            p.x = (float) (radius*Math.sin(theta)+starWidth/2.0);
            p.y = (float) (-radius*Math.cos(theta)+starHeight/2.0);
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

            starBitmap = Bitmap.createBitmap(3* starWidth, starHeight, Bitmap.Config.ARGB_8888);
            canvas.setBitmap(starBitmap);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);

            paint.setColor(REDSTAR);
            fillPath(path, radius, 0, 0, 9);
            canvas.drawPath(path, paint);

            fillPath(path, radius, starWidth, 5, 10);
            canvas.drawPath(path, paint);

            paint.setColor(GRAYSTAR);
            fillPath(path, radius, starWidth, 0, 5);
            canvas.drawPath(path, paint);

            fillPath(path, radius, 2*starWidth, 0, 9);
            canvas.drawPath(path, paint);
        }
    }

    public void setRating(float rating, Integer reviews) {
        this.rating = rating;
        this.reviews = reviews;
        requestLayout();
    }

    private String getText() {
       if (reviews==null) return(null);
        if (reviews<=0 && noReviews!=null) return(" (" + noReviews + ")");
        return("(" + Integer.toString(reviews) + ")");
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = NSTARS*config.starWidth+getPaddingLeft()+getPaddingRight();
        String text = getText();
        if (text!=null) width += config.textPaint.measureText(text, 0, text.length());
        int height = getPaddingTop()+config.starHeight+getPaddingBottom();
        width = resolveSize(width, widthSpec);
        height = resolveSize(height, heightSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Get text
        String text =  getText();

        // Use gravity to determine left position
        if (gravity==Gravity.LEFT) {
            dst.left = getPaddingLeft();
        } else {
            float slack = getWidth()-getPaddingLeft()-getPaddingRight()-NSTARS*config.starWidth;
            if (text!=null) slack -= config.textPaint.measureText(text, 0, text.length());
            dst.left = getPaddingLeft();
            if (gravity==Gravity.CENTER_HORIZONTAL) dst.left += slack/2.0f;
            else if (gravity==Gravity.RIGHT) dst.left += slack;
        }
        dst.top = getPaddingTop();
        dst.bottom = dst.top+config.starHeight;

        // Draw 5 assorted stars
        src.top = 0;
        src.bottom = config.starHeight;
        float f = rating;
        for(int i=0;i<NSTARS;i++) {
            // Get source rectangle
            if (f>=0.75f) src.left = 0;
            else if (f>=0.25f) src.left = config.starWidth;
            else src.left = 2*config.starWidth;
            src.right = src.left+config.starWidth;

            // Get destination rectangle
            dst.right = dst.left+config.starWidth;

            // Draw and increment
            canvas.drawBitmap(config.starBitmap, src, dst, null);
            dst.left += config.starWidth;
            f -= 1.0f;
        }

        // Draw review count
        if (text!=null) {
            dst.top += (config.starHeight - 0.7f * config.textPaint.ascent()) / 2.0f; // Fudge for visual centering
            canvas.drawText(text, dst.left, dst.top, config.textPaint);
        }
    }
}

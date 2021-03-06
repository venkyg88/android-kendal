package app.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import app.staples.R;

public class PagerStripe extends View implements ViewPager.OnPageChangeListener {
    private static final String TAG = PagerStripe.class.getSimpleName();

    private Paint stripePaint;
    private Paint trackPaint;
    private Paint textPaint;
    private int stripeThickness;
    private int textSize;

    private int position;
    private int count;

    public PagerStripe(Context context) {
        this(context, null, 0);
    }

    public PagerStripe(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerStripe(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Preset default attributes
        stripeThickness = 2;
        textSize = 10;

        // Get styled attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PagerStripe);
        int n = a.getIndexCount();
        for(int i=0;i<n;i++) {
            int index = a.getIndex(i);
            switch(index) {
                case R.styleable.PagerStripe_stripeThickness:
                    stripeThickness = a.getDimensionPixelSize(index, stripeThickness);
                    break;
                case R.styleable.PagerStripe_android_textSize:
                    textSize = a.getDimensionPixelSize(index, textSize);
                    break;
            }
        }
        a.recycle();

        Resources r = context.getResources();
        int blackColor = r.getColor(R.color.staples_black);

        stripePaint = new Paint();
        stripePaint.setColor(blackColor);

        trackPaint = new Paint();
        trackPaint.setColor(r.getColor(R.color.staples_light_gray));

        textPaint = new Paint();
        textPaint.setColor(blackColor);
        textPaint.setTextSize(textSize);
    }

    public void setCount(int count) {
        this.count=count;
        invalidate();
    }

    public void onPageScrollStateChanged(int state) {
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    public void onPageSelected(int position) {
        this.position = position;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        heightSpec = getPaddingTop()+stripeThickness+textSize+getPaddingBottom();
        setMeasuredDimension(widthSpec, heightSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Safety check
        if (count<=0) return;

        // Get horizontal coordinates
        int a = getPaddingLeft();
        int d = getWidth()-getPaddingRight();
        int b = (position*(d-a)+(count/2))/count+a;
        int c = ((position+1)*(d-a)+(count/2))/count+a;

        // Get vertical coordinates
        int top = getPaddingTop();
        int bottom = top+stripeThickness;

        // Draw stripes
        if (b>a) canvas.drawRect(a, top, b, bottom, trackPaint);
        if (c>b) canvas.drawRect(b, top, c, bottom, stripePaint);
        if (d>c) canvas.drawRect(c, top, d, bottom, trackPaint);

        // Draw text
        String text = (position+1) +" of " + count;
        int x = d-(int) textPaint.measureText(text);
        canvas.drawText(text, 0, text.length(), x, bottom+textSize, textPaint);
    }
}

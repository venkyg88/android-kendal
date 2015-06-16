package app.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import app.staples.R;

/**
 * <b>XML attributes</b>
 * minHUnits
 * maxHUnits
 * hModulo
 * minHUnitSize
 */
public class TileLayout extends ViewGroup {
    private static final String TAG = TileLayout.class.getSimpleName();

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        private int hUnits;
        private int vUnits;

        public LayoutParams(int hUnits, int vUnits) {
            super(0, 0);
            this.hUnits = hUnits;
            this.vUnits = vUnits;
        }
    }

    private int minHUnits;
    private int maxHUnits;
    private int hModulo;
    private int minHUnitSize;

    public TileLayout(Context context) {
        this(context, null, 0);
    }

    public TileLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TileLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Set defaults
        minHUnits = 1;
        maxHUnits = Integer.MAX_VALUE;
        hModulo = 1;
        minHUnitSize = 100;

        // Get styled attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TileLayout);
        int n = a.getIndexCount();
        for(int i=0;i<n;i++) {
            int index = a.getIndex(i);
            switch(index) {
                case R.styleable.TileLayout_minHUnits:
                    minHUnits = a.getInt(R.styleable.TileLayout_minHUnits, minHUnits);
                    break;
                case R.styleable.TileLayout_maxHUnits:
                    maxHUnits = a.getInt(R.styleable.TileLayout_maxHUnits, maxHUnits);
                    break;
                case R.styleable.TileLayout_hModulo:
                    hModulo = a.getInt(R.styleable.TileLayout_hModulo, hModulo);
                    break;
                case R.styleable.TileLayout_minHUnitSize:
                    minHUnitSize = a.getDimensionPixelSize(R.styleable.TileLayout_minHUnitSize, minHUnitSize);
                    break;
            }
        }
        a.recycle();
    }

    private int tile(int width, boolean layout) {
        // Determine horizontal units in frame
        int hUnits = hModulo*(width/(hModulo*minHUnitSize));
        if (hUnits>maxHUnits) hUnits = maxHUnits;
        if (hUnits<minHUnits) hUnits = minHUnits;

        // Layout children
        int[] fill = new int[hUnits];
        int n = getChildCount();
        for(int i=0;i<n;i++) {
            View child = getChildAt(i);
            Object obj = child.getLayoutParams();
            if (obj instanceof LayoutParams) {
                LayoutParams params = (LayoutParams) obj;

                // Find low of high points
                int lowPoint = Integer.MAX_VALUE;
                int lowIndex = 0;
                for(int j=0;j+params.hUnits<=hUnits;j++) {
                    // Find high point
                    int highPoint = Integer.MIN_VALUE;
                    for(int k=j;k<j+params.hUnits;k++) {
                        if (fill[k]>highPoint) {
                            highPoint = fill[k];
                        }
                    }
                    // Accumulate low point
                    if (highPoint<lowPoint) {
                        lowPoint = highPoint;
                        lowIndex = j;
                    }
                }

                // Set fill level
                int bottom = lowPoint+(width+hUnits/2)/hUnits*params.vUnits;
                for(int j=lowIndex;j<lowIndex+params.hUnits;j++) {
                    fill[j] = bottom;
                }

                // Layout child
                if (layout) {
                    int x1 = (lowIndex*width+hUnits/2)/hUnits+params.leftMargin;
                    int x2 = ((lowIndex+params.hUnits)*width+hUnits/2)/hUnits-params.rightMargin;
                    int y1 = lowPoint+params.topMargin;
                    int y2 = bottom-params.bottomMargin;
                    child.layout(x1, y1, x2, y2);
                }
            }
        }
        return(fill[0]); // TODO Not a rigorous solution
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        int width = View.MeasureSpec.getSize(widthSpec);
        int height = tile(width, false);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right-left;
        tile(width, true);
    }
}

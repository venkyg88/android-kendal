package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.staples.mobile.cfa.R;

/**
 * This class is a ViewGroup wrapper for three child Views:
 * <ol><li>A View for when the list has items (probably a ListView or GridView)</li>
 * <li>A View for when the list is populating (probably a ProgressBar)</li>
 * <li>A View for when the list is empty (possibly a simple TextView)</li></ol>
 * The child Views are identified by their order in the child View tree.
 * Usage: setState(DataWrapper.State state)
 * <b>XML attributes</b>
 * minGridWidth
 */
public class DataWrapper extends LinearLayout {
    private static final String TAG = DataWrapper.class.getSimpleName();

    public enum State {
        // Used for initial loading of adapters
        LOADING (View.GONE,    View.VISIBLE, View.GONE),
        EMPTY   (View.GONE,    View.GONE,    View.VISIBLE),
        // Used for additional loading of adapters
        ADDING  (View.VISIBLE, View.VISIBLE, View.GONE),
        NOMORE  (View.VISIBLE, View.GONE,    View.VISIBLE),
        // Used for success
        DONE    (View.VISIBLE, View.GONE,    View.GONE),
        // Used for making extra elements visible
        GONE    (View.GONE,    View.GONE,    View.GONE);

        public final int list;
        public final int progress;
        public final int empty;

        State(int list, int progress, int empty) {
            this.list = list;
            this.progress = progress;
            this.empty = empty;
        }
    }

    public enum LayoutMode {WIDE, TALL}

    public interface Layoutable {
        void setLayout(LayoutMode mode);
    }

    private int minGridWidth;

    public DataWrapper(Context context) {
        this(context, null, 0);
    }

    public DataWrapper(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DataWrapper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // Get styled attributes
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DataWrapper);
        minGridWidth = a.getDimensionPixelSize(R.styleable.DataWrapper_minGridWidth, 0);
        a.recycle();
    }

    // Property modifiers for animation

    public void setXFraction(float fraction) {
        int width = getWidth();
        if (width>0) setX(fraction * width);
    }

    public void setYFraction(float fraction) {
        int height = getHeight();
        if (height>0) setY(fraction * height);
    }

    // Display state handling

    public void setState(State state) {
        View list = getChildAt(0);
        if (list!=null) list.setVisibility(state.list);
        View progress = getChildAt(1);
        if (progress!=null) progress.setVisibility(state.progress);
        View empty = getChildAt(2);
        if (empty!=null) empty.setVisibility(state.empty);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);

        // Special handling for RecyclerView...
        View list = getChildAt(0);
        if (list instanceof RecyclerView) {
            RecyclerView recycle = (RecyclerView) list;

            // When it has a GridLayout
            RecyclerView.LayoutManager manager = recycle.getLayoutManager();
            if (manager instanceof GridLayoutManager) {
                int n = 1;
                if (minGridWidth>0) {
                    int viewWidth = View.MeasureSpec.getSize(widthSpec);
                    n = viewWidth / minGridWidth;
                    if (n<1) n = 1;
                }
                ((GridLayoutManager) manager).setSpanCount(n);

                // Special handling for Layoutable
                RecyclerView.Adapter adapter = recycle.getAdapter();
                if (adapter instanceof Layoutable) {
                    if (n>1) ((Layoutable) adapter).setLayout(LayoutMode.TALL);
                    else ((Layoutable) adapter).setLayout(LayoutMode.WIDE);
                }
            }
        }
    }
}

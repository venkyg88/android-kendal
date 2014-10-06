package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.staples.mobile.R;

/**
 * This class is a ViewGroup wrapper for three child Views:
 * <ol><li>A View for when the list has items (probably a ListView)</li>
 * <li>A View for when the list is populating (probably a ProgressBar)</li>
 * <li>A View for when the list is empty (possibly a simple TextView)</li></ol>
 * The child Views are identified by their order in the child View tree.
 */
public class ListViewWrapper extends LinearLayout {
    public static final String TAG ="ListViewWrapper";

    public enum State {
        // Used for initial loading of ListView
        LOADING (View.GONE,    View.VISIBLE, View.GONE),
        EMPTY   (View.GONE,    View.GONE,    View.VISIBLE),
        // Used for additional loading of ListView
        ADDING  (View.VISIBLE, View.VISIBLE, View.GONE),
        NOMORE  (View.VISIBLE, View.GONE,    View.VISIBLE),
        // Used for success
        DONE    (View.VISIBLE, View.GONE,    View.GONE);

        int list;
        int progress;
        int empty;

        State(int list, int progress, int empty) {
            this.list = list;
            this.progress = progress;
            this.empty = empty;
        }
    }

    public ListViewWrapper(Context context) {
        super(context, null, 0);
    }

    public ListViewWrapper(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public ListViewWrapper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // General state handling

    public void setState(State state) {
        View list = getChildAt(0);
        View progress = getChildAt(1);
        View empty = getChildAt(2);

        list.setVisibility(state.list);
        progress.setVisibility(state.progress);
        empty.setVisibility(state.empty);
    }

    // Special handling for GridView

    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        View list = getChildAt(0);
        if (list instanceof GridView) {
            int width = View.MeasureSpec.getSize(widthSpec);
            int column = getResources().getDimensionPixelSize(R.dimen.min_column_width);
            ((GridView) list).setNumColumns(width/column);
        }
    }
}

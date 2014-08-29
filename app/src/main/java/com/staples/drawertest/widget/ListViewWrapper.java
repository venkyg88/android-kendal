package com.staples.drawertest.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

/**
 * This class is a ViewGroup wrapper for three child Views:
 * <ol><li>A View for when the list is populating (probably a ProgressBar)</li>
 * <li>A View for when the list is empty (possibly a simple TextView)</li>
 * <li>A View for when the list has items (probably a ListView)</li></ol>
 * The child Views are identified by their order in the child View tree.
 */
public class ListViewWrapper extends FrameLayout {
    public static final String TAG ="ListViewWrapper";

    private ListAdapter adapter;
    private Observer observer;

    public ListViewWrapper(Context context) {
        super(context, null, 0);
    }

    public ListViewWrapper(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public ListViewWrapper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public class Observer extends DataSetObserver {
        @Override
        public void onChanged() {
            if (adapter!=null) {
                View progress = getChildAt(0);
                View empty = getChildAt(1);
                View list = getChildAt(2);

                if (progress!=null) progress.setVisibility(View.GONE);
                if (adapter.isEmpty()) {
                    if (empty!=null) empty.setVisibility(View.VISIBLE);
                    if (list!=null) list.setVisibility(View.GONE);
                } else {
                    if (empty!=null) empty.setVisibility(View.GONE);
                    if (list!=null) list.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public void onInvalidated() {
            View progress = getChildAt(0);
            View empty = getChildAt(1);
            View list = getChildAt(2);

            if (progress!=null) progress.setVisibility(View.VISIBLE);
            if (empty!=null) empty.setVisibility(View.GONE);
            if (list!=null) list.setVisibility(View.GONE);
        }
    }

    /**
     * Sets the adapter to observe for data changes.
     */
    public void setAdapter(ListAdapter adapter) {
        // Safety check
        if (adapter==this.adapter) return;

        // Unregister old adapter
        if (this.adapter!=null & observer!=null)
            this.adapter.unregisterDataSetObserver(observer);

        // Use new adapter
        this.adapter = adapter;

        // Register new adapter
        if (this.adapter!=null) {
            if (observer==null) observer = new Observer();
            this.adapter.registerDataSetObserver(observer);
        }
    }
}

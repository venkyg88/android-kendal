package com.staples.drawertest;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

/**
 * Created by pyhre001 on 8/27/14.
 */
public class StatusLayout extends FrameLayout {
    public static final String TAG ="StatusLayout";

    private ListAdapter adapter;
    private Observer observer;

    public StatusLayout(Context context) {
        super(context, null, 0);
    }

    public StatusLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public StatusLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public class Observer extends DataSetObserver {
        @Override
        public void onChanged() {
            Log.d(TAG, "onChanged");
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
            Log.d(TAG, "onInvalidated");
            View progress = getChildAt(0);
            View empty = getChildAt(1);
            View list = getChildAt(2);

            if (progress!=null) progress.setVisibility(View.VISIBLE);
            if (empty!=null) empty.setVisibility(View.GONE);
            if (list!=null) list.setVisibility(View.GONE);
        }
    }

    public void setAdapter(ListAdapter adapter) {
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

package com.staples.drawertest;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * Created by pyhre001 on 8/27/14.
 */
public class NothingFound extends TextView {
    public static final String TAG ="NothingFound";

    private ListAdapter adapter;
    private Observer observer;

    public NothingFound(Context context) {
        super(context, null, 0);
    }

    public NothingFound(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public NothingFound(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public class Observer extends DataSetObserver {
        @Override
        public void onChanged() {
            Log.d(TAG, "onChanged");
            if (adapter!=null && adapter.isEmpty())
                setVisibility(View.VISIBLE);
            else setVisibility(View.GONE);
        }

        @Override
        public void onInvalidated() {
            Log.d(TAG, "onInvalidated");
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

package com.staples.drawertest.browse;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.staples.drawertest.R;

import java.util.ArrayList;

/**
 * Created by pyhre001 on 8/20/14.
 */
public class CategoryAdapter extends BaseAdapter {
    private static final String TAG = "CategoryAdapter";

    private Activity activity;
    private LayoutInflater inflater;
    private ArrayList<CategoryItem> array;

    public CategoryAdapter(Activity activity) {
        super();
        this.activity = activity;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<CategoryItem>();
    }

    /* Array items */

    @Override
    public int getCount() {
        return(array.size());
    }

    @Override
    public CategoryItem getItem(int position) {
        return(array.get(position));
    }

    @Override
    public long getItemId(int position) {
        return(0);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        CategoryItem item = array.get(position);

        if (view==null)
            view = inflater.inflate(R.layout.category_item, null);

        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(item.title);

        return(view);
    }

    public void addCategory(String title, int childCount, String path) {
        CategoryItem item = new CategoryItem(title, childCount, path);
        array.add(item);
    }

    public void update() {
        Runnable runs = new Runnable() {public void run() {CategoryAdapter.this.notifyDataSetChanged();}};
        activity.runOnUiThread(runs);
    }
}

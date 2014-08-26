package com.staples.drawertest;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.staples.drawertest.browse.CategoryFragment;

import java.util.ArrayList;

/**
 * Created by pyhre001 on 8/14/14.
 */
public class DrawerAdapter extends BaseAdapter {
    private static final String TAG = "DrawerAdapter";

    private Activity activity;
    private LayoutInflater inflater;
    private ArrayList<DrawerItem> array;

    public DrawerAdapter(Context context) {
        super();
        activity = (Activity) context;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<DrawerItem>(16);
    }

    /* Array items */

    @Override
    public int getCount() {
        return(array.size());
    }

    @Override
    public DrawerItem getItem(int position) {
        return(array.get(position));
    }

    @Override
    public long getItemId(int position) {
        return(0);
    }

    /* Views */

    @Override
    public int getViewTypeCount() {
        return (DrawerItem.NTYPES);
    }

    @Override
    public int getItemViewType(int position) {
        DrawerItem item = array.get(position);
        return(item.type.viewType);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        DrawerItem item = array.get(position);

        if (view==null)
            view = inflater.inflate(item.type.layoutId, null);

        TextView title = (TextView) view.findViewById(R.id.title);
        if (title!=null) {
            if (item.childCount > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(item.title);
                sb.append(" (");
                sb.append(item.childCount);
                sb.append(")");
                title.setText(sb.toString());
            } else title.setText(item.title);
        }
        return(view);
    }

    public void fill() {
        // Fill adapter with fragment titles
        add(new DrawerItem(DrawerItem.Type.HEADER, activity, 0, R.string.account_title));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.alfa_title, AlfaFragment.class));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.bravo_title, BravoFragment.class));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.charlie_title, CharlieFragment.class));
        add(new DrawerItem(DrawerItem.Type.HEADER, activity, 0, R.string.products_title));

        new TopCategoryFiller().execute(this);
    }

    // add and update must be run on the UI thread

    public void add(final DrawerItem item) {
        Runnable runs = new Runnable() {public void run() {DrawerAdapter.this.array.add(item);}};
        activity.runOnUiThread(runs);
    }

    public void update() {
        Runnable runs = new Runnable() {public void run() {DrawerAdapter.this.notifyDataSetChanged();}};
        activity.runOnUiThread(runs);
    }
}

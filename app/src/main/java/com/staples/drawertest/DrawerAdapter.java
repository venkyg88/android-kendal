package com.staples.drawertest;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by pyhre001 on 8/14/14.
 */
public class DrawerAdapter extends BaseAdapter {
    private static final String TAG = "DrawerAdapter";

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<DrawerItem> array;

    private Fragment topCategory;

    public DrawerAdapter(Context context) {
        super();
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        if (title!=null)
            title.setText(item.title);

        return(view);
    }

    public void fill() {
        // Fill adapter with fragment titles
        array.add(new DrawerItem(DrawerItem.Type.SEARCH));
        array.add(new DrawerItem(DrawerItem.Type.HEADER, context, 0, R.string.account_title));
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, context, 0, R.string.alfa_title, AlfaFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, context, 0, R.string.bravo_title, BravoFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, context, 0, R.string.charlie_title, CharlieFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.HEADER, context, 0, R.string.products_title));

        new TopCategoryFiller().execute(this);
    }

    public void addCategory(String name) {
        if (topCategory==null)
            topCategory = Fragment.instantiate(context, TopCategoryFragment.class.getName());
        DrawerItem item = new DrawerItem(DrawerItem.Type.FRAGMENT, name, topCategory);
        array.add(item);
    }

    public void update() {
        final DrawerAdapter adapter = this;
        Runnable runs = new Runnable() {public void run() {adapter.notifyDataSetChanged();}};
        ((Activity) context).runOnUiThread(runs);
    }
}

package com.staples.mobile.cfa.browse;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.staples.mobile.cfa.R;

import java.util.ArrayList;

public class BrowseAdapter extends BaseAdapter {
    private static final String TAG = "BrowseAdapter";


    private Activity activity;
    private LayoutInflater inflater;

    private ArrayList<BrowseItem> stackList;
    private ArrayList<BrowseItem> browseList;

    public BrowseAdapter(Activity activity) {
        super();
        this.activity = activity;

        inflater = activity.getLayoutInflater();
        stackList = new ArrayList<BrowseItem>();
        browseList = new ArrayList<BrowseItem>();
    }

    // Items

    @Override
    public int getCount() {
        return(stackList.size()+browseList.size());
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }

    @Override
    public BrowseItem getItem(int position) {
        int size = stackList.size();
        if (position < size) return (stackList.get(position));
        position -= size;
        size = browseList.size();
        if (position < size) return (browseList.get(position));
        return(null);
    }

    /* Views */

    @Override
    public int getViewTypeCount() {
        return (BrowseItem.NTYPES);
    }

    @Override
    public int getItemViewType(int position) {
        BrowseItem item = getItem(position);
        return(item.type.viewType);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        BrowseItem item = getItem(position);

        // Get a new or recycled view of the right type
        if (view==null)
            view = inflater.inflate(item.type.layoutId, parent, false);

        // Set title
        TextView title = (TextView) view.findViewById(R.id.title);
        if (title!=null) title.setText(item.title);

        return(view);
    }

    public void pushStack(BrowseItem item) {
        // Push new stack item
        item.type = BrowseItem.Type.STACK;
        stackList.add(item);

        // Clear browse list
        browseList.clear();
    }

    public String popStack(BrowseItem item) {
        // Safety check
        int index = stackList.indexOf(item);
        if (index < 0) return (null);

        // Get identifier
        String identifier = null;
        if (index > 0) identifier = stackList.get(index - 1).identifier;

        // Pop stack
        int size = stackList.size();
        for(; index < size; ) {
            size--;
            BrowseItem dead = (BrowseItem) stackList.remove(size);
        }

        // Clear browse list
        browseList.clear();

        return (identifier);
    }

    public void addItem(BrowseItem item) {
        item.type = BrowseItem.Type.ITEM;
        browseList.add(item);
    }
}

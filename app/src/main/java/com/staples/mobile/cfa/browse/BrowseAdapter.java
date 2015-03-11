package com.staples.mobile.cfa.browse;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.staples.mobile.cfa.R;

import java.util.ArrayList;

public class BrowseAdapter extends BaseAdapter {
    private static final String TAG = "BrowseAdapter";

    private static final String TITLES = "titles";
    private static final String IDENTIFIERS = "identifiers";

    private LayoutInflater inflater;

    private ArrayList<BrowseItem> stackList;
    private ArrayList<BrowseItem> browseList;

    public BrowseAdapter(Activity activity) {
        super();

        inflater = activity.getLayoutInflater();
        stackList = new ArrayList<BrowseItem>();
        browseList = new ArrayList<BrowseItem>();

        stackList.add(new BrowseItem(BrowseItem.Type.ACTIVE, activity.getResources().getString(R.string.browse_title), null));
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
        // Change old active to regular stack
        int size = stackList.size();
        if (size>0)
            stackList.get(size-1).type = BrowseItem.Type.STACK;

        // Push new stack item
        item.type = BrowseItem.Type.ACTIVE;
        stackList.add(item);

        // Clear browse list
        browseList.clear();
    }

    public String popStack(BrowseItem item) {
        // Safety check
        int index = stackList.indexOf(item);
        if (index < 0) return (null);

        // Change new active
        item.type = BrowseItem.Type.ACTIVE;

        // Pop stack
        for(int i = stackList.size()-1;i>index;i--)
            stackList.remove(i);

        // Clear browse list
        browseList.clear();

        return (item.identifier);
    }

    /** needed for analytics */
    public String getCategoryHierarchy() {
        StringBuilder buf = new StringBuilder();
        for (BrowseItem item : stackList) {
            if (!TextUtils.isEmpty(item.identifier) && Character.isLetter(item.identifier.charAt(0))) {
                if (buf.length() > 0) {
                    buf.append(":");
                }
                buf.append(item.title);
            }
        }
        return buf.toString();
    }

    public void addItem(BrowseItem item) {
        item.type = BrowseItem.Type.ITEM;
        browseList.add(item);
    }

    public String getActiveIdentifier() {
        int size = stackList.size();
        if (size<1) return(null);
        return(stackList.get(size-1).identifier);
    }

    // State save and restore

    public Bundle saveState(Bundle bundle) {
        if (bundle==null) bundle = new Bundle();

        int size = stackList.size();
        if (size<2) return(bundle);

        // Build split lists
        ArrayList<String> titles = new ArrayList<String>(size-1);
        ArrayList<String> identifiers = new ArrayList<String>(size-1);
        for(int i=1;i<size;i++) {
            BrowseItem item = stackList.get(i);
            titles.add(item.title);
            identifiers.add(item.identifier);
        }

        // Put lists in bundle
        bundle.putStringArrayList(TITLES, titles);
        bundle.putStringArrayList(IDENTIFIERS, identifiers);
        return(bundle);
    }

    public boolean restoreState(Bundle bundle) {
        // Safety check
        if (bundle == null) return (false);

        // Get valid arrays from the bundle
        ArrayList<String> titles = bundle.getStringArrayList(TITLES);
        ArrayList<String> identifiers = bundle.getStringArrayList(IDENTIFIERS);
        if (titles == null || identifiers == null) return (false);
        int ntitles = titles.size();
        int nidentifiers = identifiers.size();
        if (ntitles == 0 || nidentifiers == 0 || ntitles != nidentifiers) return (false);

        // Set root item as not active
        stackList.get(0).type = BrowseItem.Type.STACK;

        // Remove old entries
        int size = stackList.size();
        for(int i = size - 1; i > 0; i--)
            stackList.remove(i);

        // Insert new entries
        BrowseItem item = null;
        for(int i = 0; i < ntitles; i++) {
            item = new BrowseItem(BrowseItem.Type.STACK, titles.get(i), identifiers.get(i));
            stackList.add(item);
        }

        // Set last item as active
        item.type = BrowseItem.Type.ACTIVE;

        // Clear items
        browseList.clear();
        return (true);
    }
}

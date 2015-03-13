package com.staples.mobile.cfa.browse;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.staples.mobile.cfa.R;

import java.util.ArrayList;

public class BrowseAdapter extends RecyclerView.Adapter<BrowseAdapter.ViewHolder> {
    private static final String TAG = "BrowseAdapter";

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;

        private ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
        }
    }

    private LayoutInflater inflater;
    private ArrayList<BrowseItem> stackList;
    private ArrayList<BrowseItem> browseList;
    private View.OnClickListener listener;

    public BrowseAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        stackList = new ArrayList<BrowseItem>();
        browseList = new ArrayList<BrowseItem>();
        stackList.add(new BrowseItem(BrowseItem.Type.ACTIVE, context.getResources().getString(R.string.browse_title), null));
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    // Items

    @Override
    public int getItemCount() {
        return(stackList.size()+browseList.size());
    }

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
    public int getItemViewType(int position) {
        BrowseItem item = getItem(position);
        return(item.type.viewType);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        int layout = BrowseItem.Type.values()[type].layoutId;
        View view = inflater.inflate(layout, parent, false);
        ViewHolder vh = new ViewHolder(view);
        vh.itemView.setOnClickListener(listener);
        return(vh);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        BrowseItem item = getItem(position);
        vh.itemView.setTag(item);
        vh.title.setText(item.title);
    }

    // Stack operations

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
        notifyDataSetChanged();
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

        notifyDataSetChanged();
        return (item.identifier);
    }

    public void selectItem(BrowseItem selected) {
        for(BrowseItem item : browseList) {
            if (item==selected) item.type = BrowseItem.Type.SELECTED;
            else item.type = BrowseItem.Type.ITEM;
        }
        notifyDataSetChanged();
    }

    public void addItem(BrowseItem item) {
        item.type = BrowseItem.Type.ITEM;
        browseList.add(item);
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
}

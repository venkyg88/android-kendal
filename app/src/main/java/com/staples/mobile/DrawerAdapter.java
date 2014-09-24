package com.staples.mobile;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.staples.mobile.browse.CategoryFragment;
import com.staples.mobile.feed.FeedFragment;
import com.staples.mobile.home.LmsFragment;

public class DrawerAdapter extends ArrayAdapter<DrawerItem>{
    private static final String TAG = "DrawerAdapter";

    private Activity activity;
    private LayoutInflater inflater;

    public DrawerAdapter(Context context) {
        super(context, 0);
        activity = (Activity) context;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /* Views */

    @Override
    public int getViewTypeCount() {
        return (DrawerItem.NTYPES);
    }

    @Override
    public int getItemViewType(int position) {
        DrawerItem item = getItem(position);
        return(item.type.viewType);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        DrawerItem item = getItem(position);

        // Get a new or recycled view of the right type
        if (view==null)
            view = inflater.inflate(item.type.layoutId, parent, false);

        // Set icon
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        if (icon!=null) icon.setImageDrawable(item.icon);

        // Set title
        TextView title = (TextView) view.findViewById(R.id.title);
        if (title!=null) title.setText(item.title);

        return(view);
    }

    public void fill() {
        // Fill adapter with fragment titles
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.logo, R.string.home_title, LmsFragment.class));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.logo, R.string.personal_feed_title, FeedFragment.class));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.logo, R.string.shop_categories_title, CategoryFragment.class));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.logo, R.string.store_locator_title, ToBeDoneFragment.class));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.logo, R.string.weekly_ad_title, ToBeDoneFragment.class));
        add(new DrawerItem(DrawerItem.Type.ACCOUNT, activity, R.drawable.logo, R.string.account_title));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.logo, R.string.order_title, ToBeDoneFragment.class));
        add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.logo, R.string.profile_title, ToBeDoneFragment.class));
    }
}

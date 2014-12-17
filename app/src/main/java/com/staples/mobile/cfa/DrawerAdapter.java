package com.staples.mobile.cfa;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.staples.mobile.cfa.browse.BrowseFragment;
import com.staples.mobile.cfa.feed.PersonalFeedFragment;
import com.staples.mobile.cfa.home.ConfiguratorFragment;
import com.staples.mobile.cfa.login.LoginFragment;
import com.staples.mobile.cfa.profile.ProfileFragment;
import com.staples.mobile.cfa.rewards.RewardsFragment;
import com.staples.mobile.cfa.store.StoreFragment;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.common.access.Access;

import java.util.ArrayList;

public class DrawerAdapter extends BaseAdapter {
    private static final String TAG = "DrawerAdapter";

    private MainActivity activity;
    private LayoutInflater inflater;
    private ArrayList<DrawerItem> array;

    public DrawerAdapter(MainActivity activity) {
        super();
        this.activity = activity;
        inflater = activity.getLayoutInflater();
        array = new ArrayList<DrawerItem>();
    }

    // Items

    @Override
    public int getCount() {
        return(array.size());
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }

    @Override
    public DrawerItem getItem(int position) {
        return(array.get(position));
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
        if (title!=null) {
            title.setText(item.title);
            title.setTextColor(activity.getResources().getColor(item.enabled? R.color.text_black : R.color.text_gray));
        }

        // Set callback
        View button = view.findViewById(R.id.account_button);
        if (button!=null) button.setOnClickListener(activity);

        return(view);
    }

    public void fill() {
        // Fill menu list TODO icons nulled out
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.home_title, ConfiguratorFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.personal_feed_title, PersonalFeedFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.browse_title, BrowseFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.store_locator_title, StoreFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.weekly_ad_title, ToBeDoneFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.ACCOUNT, activity, 0, R.string.account_title, LoginFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.rewards_title, RewardsFragment.class, false)); // set initially disabled
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.order_title, ToBeDoneFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.PROFILE, activity, 0, R.string.profile_title, ProfileFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, 0, R.string.about_title, AboutFragment.class));
    }
}

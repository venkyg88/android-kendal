package com.staples.mobile.cfa;

import android.text.TextUtils;
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
import com.staples.mobile.cfa.order.OrderFragment;
import com.staples.mobile.cfa.notify.NotifyPrefsFragment;
import com.staples.mobile.cfa.profile.ProfileFragment;
import com.staples.mobile.cfa.rewards.RewardsFragment;
import com.staples.mobile.cfa.store.StoreFragment;
import com.staples.mobile.cfa.weeklyad.WeeklyAdByCategoryFragment;

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

    public DrawerItem getItem(Class fragmentClass) {
        for (DrawerItem item : array) {
            if (item.fragmentClass.equals(fragmentClass)) {
                return item;
            }
        }
        return null;
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

        // Set additional text
        TextView additionalTextVw = (TextView) view.findViewById(R.id.additional_text);
        if (additionalTextVw!=null) {
            if (!TextUtils.isEmpty(item.additionalText) && item.enabled) {
                additionalTextVw.setText(item.additionalText);
                additionalTextVw.setTextColor(activity.getResources().getColor(R.color.text_red));
                additionalTextVw.setVisibility(View.VISIBLE);
            } else {
                additionalTextVw.setVisibility(View.GONE);
            }
        }

        // Set callback
        View button = view.findViewById(R.id.account_option);
        if (button!=null) button.setOnClickListener(activity);

        return(view);
    }

    public void fill() {
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.ic_home_black, R.string.home_title, ConfiguratorFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.ic_android, R.string.personal_feed_title, PersonalFeedFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.ic_browse_black, R.string.browse_title, BrowseFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.ic_store_locator_black, R.string.store_locator_title, StoreFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.ic_weekly_ad_black, R.string.weekly_ad_title, WeeklyAdByCategoryFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.ACCOUNT, activity, R.drawable.ic_account_black, R.string.account_title, LoginFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.ic_rewards_black, R.string.rewards_title, RewardsFragment.class, false)); // set initially disabled
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.ic_orders_black, R.string.order_title, OrderFragment.class, false)); // set initially disabled
        array.add(new DrawerItem(DrawerItem.Type.PROFILE, activity, R.drawable.ic_profile_black, R.string.profile_title, ProfileFragment.class, false)); // set initially disabled
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.ic_android, R.string.notify_prefs_title, NotifyPrefsFragment.class));
        array.add(new DrawerItem(DrawerItem.Type.FRAGMENT, activity, R.drawable.ic_android, R.string.about_title, AboutFragment.class));
    }
}

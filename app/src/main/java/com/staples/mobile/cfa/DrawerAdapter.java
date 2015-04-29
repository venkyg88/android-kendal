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
import com.staples.mobile.cfa.home.HomeFragment;
import com.staples.mobile.cfa.notify.NotifyPrefsFragment;
import com.staples.mobile.cfa.order.OrderFragment;
import com.staples.mobile.cfa.profile.ProfileFragment;
import com.staples.mobile.cfa.rewards.RewardsFragment;
import com.staples.mobile.cfa.store.StoreFragment;
import com.staples.mobile.cfa.weeklyad.WeeklyAdByCategoryFragment;

import java.util.ArrayList;

public class DrawerAdapter extends BaseAdapter {
    private static final String TAG = DrawerAdapter.class.getSimpleName();

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

    public DrawerItem findItemByTag(String tag) {
        for (DrawerItem item : array) {
            if (item.tag.equals(tag)) {
                return item;
            }
        }
        return null;
    }

    /* Views */

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        DrawerItem item = getItem(position);

        // Get a new or recycled view of the right type
        if (view==null)
            view = inflater.inflate(R.layout.drawer_item, parent, false);

        // Set icon
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        if (icon!=null) icon.setImageDrawable(item.icon);

        // Set title
        TextView title = (TextView) view.findViewById(R.id.title);
        if (title!=null) {
            title.setText(item.title);
            title.setTextColor(activity.getResources().getColor(item.enabled? R.color.staples_black : R.color.staples_middle_gray));
        }

        // Set extra
        TextView extra = (TextView) view.findViewById(R.id.extra);
        switch(item.tag) {
            case DrawerItem.ACCOUNT:
                if (!TextUtils.isEmpty(item.extra)) {
                    extra.setVisibility(View.VISIBLE);
                    extra.setTextColor(activity.getResources().getColor(R.color.staples_black));
                    extra.setText(item.extra);
                    extra.setOnClickListener(activity);
                } else {
                    extra.setVisibility(View.GONE);
                }
                break;
            case DrawerItem.REWARDS:
                if (!TextUtils.isEmpty(item.extra) && item.enabled) {
                    extra.setVisibility(View.VISIBLE);
                    extra.setText(item.extra);
                    extra.setTextColor(activity.getResources().getColor(R.color.staples_red));
                    extra.setOnClickListener(null);
                } else {
                    extra.setVisibility(View.GONE);
                }
                break;
            default:
                extra.setVisibility(View.GONE);
                extra.setOnClickListener(null);
                break;
        }

        // Set login option

        return(view);
    }

    public void fill() {
        array.add(new DrawerItem(activity, DrawerItem.HOME,    R.drawable.ic_home_black, R.string.home_title, HomeFragment.class));
        array.add(new DrawerItem(activity, DrawerItem.FEED,    R.drawable.ic_chat_black, R.string.personal_feed_title, PersonalFeedFragment.class));
        array.add(new DrawerItem(activity, DrawerItem.BROWSE,  R.drawable.ic_browse_black, R.string.browse_title, BrowseFragment.class));
        array.add(new DrawerItem(activity, DrawerItem.STORE,   R.drawable.ic_store_locator_black, R.string.store_locator_title, StoreFragment.class));
        array.add(new DrawerItem(activity, DrawerItem.WEEKLY,  R.drawable.ic_weekly_ad_black, R.string.weekly_ad_title, WeeklyAdByCategoryFragment.class));
        array.add(new DrawerItem(activity, DrawerItem.ACCOUNT, R.drawable.ic_account_black, R.string.account_title, ProfileFragment.class, false));
        array.add(new DrawerItem(activity, DrawerItem.REWARDS, R.drawable.ic_rewards_black, R.string.rewards_title, RewardsFragment.class, false)); // set initially disabled
        array.add(new DrawerItem(activity, DrawerItem.ORDERS,  R.drawable.ic_orders_black, R.string.order_title, OrderFragment.class, false)); // set initially disabled
        array.add(new DrawerItem(activity, DrawerItem.PROFILE, R.drawable.ic_profile_black, R.string.profile_title, ProfileFragment.class, false)); // set initially disabled
        array.add(new DrawerItem(activity, DrawerItem.NOTIFY,  R.drawable.ic_notifications_black, R.string.notify_prefs_title, NotifyPrefsFragment.class));
        array.add(new DrawerItem(activity, DrawerItem.TERMS,   R.drawable.ic_info_black, R.string.terms_title, TermsFragment.class));
        array.add(new DrawerItem(activity, DrawerItem.ABOUT,   R.drawable.ic_info_black, R.string.about_title, AboutFragment.class));
    }
}

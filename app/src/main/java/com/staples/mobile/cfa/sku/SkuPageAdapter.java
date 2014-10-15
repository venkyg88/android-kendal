package com.staples.mobile.cfa.sku;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.R;

import java.util.ArrayList;

public class SkuPageAdapter extends PagerAdapter {
    private static final String TAG = "SkuPageAdapter";

    private Activity activity;
    private ArrayList<SkuPageItem> array;

    private static class SkuPageItem {
        TextView view;
        String title;
    }

    public SkuPageAdapter(Activity activity) {
        super();
        this.activity = activity;
        array = new ArrayList<SkuPageItem>();
    }

    @Override
    public int getCount() {
        return (array.size());
    }

    public void add(String title) {
        SkuPageItem item = new SkuPageItem();
        item.title = title;
        array.add(item);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        SkuPageItem item = array.get(position);

        item.view = new TextView(activity);
        item.view.setText(item.title);

        container.addView(item.view);
        return (item);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        SkuPageItem item = array.get(position);
        container.removeView(item.view);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==((SkuPageItem) object).view);
    }

    @Override
    public String getPageTitle (int position) {
        SkuPageItem item = array.get(position);
        return(item.title);
    }
}

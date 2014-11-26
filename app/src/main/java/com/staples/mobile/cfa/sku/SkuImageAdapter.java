package com.staples.mobile.cfa.sku;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.R;

import java.util.ArrayList;

public class SkuImageAdapter extends PagerAdapter {
    private static final String TAG = "SkuImageAdapter";

    private Activity activity;
    private ArrayList<SkuImageItem> array;

    private static class SkuImageItem {
        ImageView view;
        String url;
    }

    public SkuImageAdapter(Activity activity) {
        super();
        this.activity = activity;
        array = new ArrayList<SkuImageItem>();
    }

    @Override
    public int getCount() {
        return (array.size());
    }

    public void add(String url) {
        SkuImageItem item = new SkuImageItem();
        item.url = url;
        array.add(item);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        SkuImageItem item = array.get(position);

        item.view = new ImageView(activity);
        Picasso.with(activity).load(item.url).error(R.drawable.no_photo).into(item.view);

        container.addView(item.view);
        return (item);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        SkuImageItem item = array.get(position);
        container.removeView(item.view);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==((SkuImageItem) object).view);
    }
}

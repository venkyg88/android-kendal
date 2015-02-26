package com.staples.mobile.cfa.sku;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.common.access.easyopen.model.browse.Product;
import com.staples.mobile.common.access.easyopen.model.reviews.Data;
import com.staples.mobile.common.access.easyopen2.model.review.Review;

import java.util.ArrayList;
import java.util.List;

public class SkuTabAdapter extends PagerAdapter {
    private static final String TAG = "SkuPageAdapter";

    private Activity activity;
    private ArrayList<SkuPageItem> array;
    private LayoutInflater inflater;
    private Product product;
    private List<Data> reviews;
    private List<Review> yotpoReviews;

    private static class SkuPageItem {
        View view;
        String title;
    }

    public SkuTabAdapter(Activity activity) {
        super();
        this.activity = activity;
        array = new ArrayList<SkuPageItem>();
        inflater = activity.getLayoutInflater();
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setReviews(List<Data> reviews) {
        this.reviews = reviews;
    }

    public void setYotpoReviews(List<Review> yotpoReviews) {
        this.yotpoReviews = yotpoReviews;
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
        ViewGroup parent;

        SkuPageItem item = array.get(position);

        switch(position) {
            case 0:
                item.view = inflater.inflate(R.layout.sku_detail_scroll, container, false);
                parent = (ViewGroup) item.view.findViewById(R.id.details);
                SkuFragment.buildDescription(inflater, parent, product, 50);
                break;
            case 1:
                item.view = inflater.inflate(R.layout.sku_detail_scroll, container, false);
                parent = (ViewGroup) item.view.findViewById(R.id.details);
                SkuFragment.addSpecifications(inflater, parent, product, 50);
                break;
            case 2:
                item.view = inflater.inflate(R.layout.sku_detail_list, container, false);
                //ReviewAdapter adapter = new ReviewAdapter(activity);
                //((ListView) item.view).setAdapter(adapter);
                //adapter.setReviews(reviews);

                YotpoReviewAdapter adapter = new YotpoReviewAdapter(activity);
                ((ListView) item.view).setAdapter(adapter);
                adapter.setYotpoReviews(yotpoReviews);
                break;
        }

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

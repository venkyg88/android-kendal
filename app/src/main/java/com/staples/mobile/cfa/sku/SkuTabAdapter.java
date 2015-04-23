package com.staples.mobile.cfa.sku;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.HorizontalDivider;
import com.staples.mobile.common.access.easyopen.model.browse.Product;

import java.util.ArrayList;

public class SkuTabAdapter extends PagerAdapter {
    private static final String TAG = SkuTabAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<SkuPageItem> array;
    private LayoutInflater inflater;
    private Product product;
    private SkuReviewAdapter reviewAdapter;

    private static class SkuPageItem {
        View view;
        String title;
    }

    public SkuTabAdapter(Context context) {
        super();
        this.context = context;
        array = new ArrayList<SkuPageItem>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public Product getProduct() {
        return this.product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }

    public void setReviewAdapter(SkuReviewAdapter adapter) {
        reviewAdapter = adapter;
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
                RecyclerView list = (RecyclerView) item.view.findViewById(R.id.list);
                list.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));
                list.addItemDecoration(new HorizontalDivider(context));
                list.setAdapter(reviewAdapter);
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

package com.staples.mobile.cfa.sku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.staples.mobile.R;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.easyopen.model.reviews.Data;

import java.util.List;

public class ReviewAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private List<Data> reviews;

    public ReviewAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setReviews(List<Data> reviews) {
        this.reviews = reviews;
    }

    @Override
    public int getCount() {
        if (reviews==null) return(0);
        return(reviews.size());
    }

    @Override
    public Data getItem(int position) {
        return(reviews.get(position));
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view==null)
            view = inflater.inflate(R.layout.sku_review_item, parent, false);

        Data item = reviews.get(position);

        // Set items
        String created = SkuFragment.formatTimestamp(item.getCreated_datetime());
        if (created!=null)
            ((TextView) view.findViewById(R.id.sku_review_date)).setText(created);
        else ((TextView) view.findViewById(R.id.sku_review_date)).setVisibility(View.GONE);

        ((RatingStars) view.findViewById(R.id.sku_review_rating)).setRating(item.getRating(), null);

        String comments = item.getComments();
        if (comments!=null)
            ((TextView) view.findViewById(R.id.sku_review_comments)).setText(comments);
        else ((TextView) view.findViewById(R.id.sku_review_comments)).setVisibility(View.GONE);

        return(view);
    }
}

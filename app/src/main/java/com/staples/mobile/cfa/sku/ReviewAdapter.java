package com.staples.mobile.cfa.sku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.easyopen.model.reviews.Data;

import java.util.List;

public class ReviewAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List<Data> reviews;

    public ReviewAdapter(Context context) {
        this.context = context;
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

    private void setTextView(TextView view, String text) {
        if (text!=null) {
            view.setText(text);
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        TextView text;

        if (view==null)
            view = inflater.inflate(R.layout.sku_review_full_item, parent, false);

        Data item = reviews.get(position);

        // Set items
        String created = SkuFragment.formatTimestamp(item.getCreated_datetime());
        setTextView((TextView) view.findViewById(R.id.sku_review_date), created);

        ((RatingStars) view.findViewById(R.id.sku_review_rating)).setRating(item.getRating(), null);

        setTextView((TextView) view.findViewById(R.id.sku_review_title), item.getHeadline());
        setTextView((TextView) view.findViewById(R.id.sku_review_comments), item.getComments());

        String verdict = item.getBottomline();
        if (verdict!=null) verdict = context.getResources().getString(R.string.would_you)+" "+verdict;
        setTextView((TextView) view.findViewById(R.id.sku_review_verdict), verdict);

        return(view);
    }
}

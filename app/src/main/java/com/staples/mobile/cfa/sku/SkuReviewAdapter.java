package com.staples.mobile.cfa.sku;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.channel.model.review.Review;

import java.util.ArrayList;
import java.util.List;

public class SkuReviewAdapter extends RecyclerView.Adapter<SkuReviewAdapter.ViewHolder> {
    private static final String TAG = SkuReviewAdapter.class.getSimpleName();

    public interface OnFetchMoreData {
        void onFetchMoreData();
    }

    private static class Item {
        float rating;
        String title;
        String author;
        String date;
        String comments;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private RatingStars rating;
        private TextView title;
        private TextView author;
        private TextView date;
        private TextView comments;

        private ViewHolder(View view) {
            super(view);
            rating = (RatingStars) view.findViewById(R.id.sku_review_rating);
            title = (TextView) view.findViewById(R.id.sku_review_title);
            author = (TextView) view.findViewById(R.id.sku_review_author);
            date = (TextView) view.findViewById(R.id.sku_review_date);
            comments = (TextView) view.findViewById(R.id.sku_review_comments);
        }
    }

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<Item> array;
    private OnFetchMoreData fetcher;
    private int threshold;

    public SkuReviewAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<Item>();
    }

    public void setOnFetchMoreData(OnFetchMoreData fetcher, int threshold) {
        this.fetcher = fetcher;
        this.threshold = threshold;
    }

    @Override
    public int getItemCount() {
        return(array.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = inflater.inflate(R.layout.sku_review_full_item, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return(vh);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        Item item = array.get(position);

        vh.rating.setRating(item.rating, null);

        if (item.title!=null) {
            vh.title.setVisibility(View.VISIBLE);
            vh.title.setText(item.title);
        } else {
            vh.title.setVisibility(View.GONE);
        }

        if (item.author!=null) {
            vh.author.setVisibility(View.VISIBLE);
            vh.author.setText(item.author);
        } else {
            vh.author.setVisibility(View.GONE);
        }

        if (item.date!=null) {
            vh.date.setVisibility(View.VISIBLE);
            vh.date.setText(item.date);
        } else {
            vh.date.setVisibility(View.GONE);
        }

        if (item.comments != null) {
            vh.comments.setVisibility(View.VISIBLE);
            vh.comments.setText(item.comments);
        } else {
            vh.comments.setVisibility(View.GONE);
        }

        // Need to get more data?
        if (fetcher!=null && position>threshold) {
            fetcher.onFetchMoreData();
            fetcher = null;
            threshold = 0;
        }
    }

    public void fill(List<Review> reviews) {
        if (reviews==null) return;
        for(Review review : reviews) {
            Item item = new Item();
            item.rating = review.getScore();
            item.title = review.getTitle();
            item.date = SkuFragment.formatTimestamp(review.getCreatedAt());
            item.comments = review.getContent();
            array.add(item);
        }
    }
}


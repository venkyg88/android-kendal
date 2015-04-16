package com.staples.mobile.cfa.sku;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.easyopen2.model.review.Review;

import java.util.List;

public class YotpoReviewAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List<Review> yotpoReviews;

    public YotpoReviewAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setYotpoReviews(List<Review> yotpoReviews) {
        this.yotpoReviews = yotpoReviews;
    }

    @Override
    public int getCount() {
        if (yotpoReviews == null) return(0);
        return(yotpoReviews.size());
    }

    @Override
    public Review getItem(int position) {
        return(yotpoReviews.get(position));
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view==null)
            view = inflater.inflate(R.layout.sku_review_full_item, parent, false);

        Review review = yotpoReviews.get(position);

        // Rating
        ((RatingStars) view.findViewById(R.id.sku_review_rating)).setRating(review.getScore(), null);

        // Title
        String title = review.getTitle();
        if (title != null) {
            ((TextView) view.findViewById(R.id.sku_review_title)).setText(title);
        }
        else {
            view.findViewById(R.id.sku_review_title).setVisibility(View.GONE);
        }

        // Author
        String author = review.getUser().getDisplayName();
        if (author != null) {
            ((TextView) view.findViewById(R.id.sku_review_author)).setText("By " + author + " - ");
        }
        else {
            view.findViewById(R.id.sku_review_author).setVisibility(View.GONE);
        }

        // Created date
        String[] createdDateTime = review.getCreatedAt().split("T");
        String createdDate = createdDateTime[0];

        if (createdDate != null) {
            ((TextView) view.findViewById(R.id.sku_review_date)).setText(createdDate);
        }
        else {
            view.findViewById(R.id.sku_review_date).setVisibility(View.GONE);
        }

        // Comment
        String comments = review.getContent();
        if (comments != null) {
            ((TextView) view.findViewById(R.id.sku_review_comments)).setText(comments);
        }
        else {
            view.findViewById(R.id.sku_review_comments).setVisibility(View.GONE);
        }

        return(view);
    }
}

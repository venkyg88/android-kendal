package com.staples.mobile.cfa.sku;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.channel.model.review.Review;
import com.staples.mobile.common.access.channel.model.review.User;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class SkuReviewAdapter extends RecyclerView.Adapter<SkuReviewAdapter.ViewHolder> {
    private static final String TAG = SkuReviewAdapter.class.getSimpleName();

    private static SimpleDateFormat iso8601;

    public interface OnFetchMoreData {
        void onFetchMoreData();
    }

    private static class Item {
        private float rating;
        private String title;
        private String author;
        private String date;
        private String comments;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private RatingStars rating;
        private TextView title;
        private TextView attribution;
        private TextView comments;

        private ViewHolder(View view) {
            super(view);
            rating = (RatingStars) view.findViewById(R.id.sku_review_rating);
            title = (TextView) view.findViewById(R.id.sku_review_title);
            attribution = (TextView) view.findViewById(R.id.sku_review_attribution);
            comments = (TextView) view.findViewById(R.id.sku_review_comments);
        }

        public void limitComments(int n) {
            comments.setMaxLines(n);
        }
    }

    private LayoutInflater inflater;
    private ArrayList<Item> array;
    private OnFetchMoreData fetcher;
    private int threshold;
    private String authorPrefix;

    public SkuReviewAdapter(Context context) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<Item>();
        authorPrefix = context.getResources().getString(R.string.author_prefix);
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
        View view = inflater.inflate(R.layout.sku_review_item, parent, false);
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

        if (item.author!=null || item.date!=null) {
            vh.attribution.setVisibility(View.VISIBLE);
            StringBuilder sb = new StringBuilder();
            if (item.author!=null) {
                sb.append(authorPrefix);
                sb.append(' ');
                sb.append(item.author);
            }
            if (item.date!=null) {
                if (sb.length()>0) sb.append(" - ");
                sb.append(item.date);
            }
            vh.attribution.setText(sb);
        } else {
            vh.attribution.setVisibility(View.GONE);
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

    public static String formatTimestamp(String raw) {
        if (raw == null) return (null);

        if (iso8601 == null)
            iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        iso8601.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date date = iso8601.parse(raw);
            String text = DateFormat.getDateInstance().format(date);
            return (text);
        } catch (ParseException e) {
            Crittercism.logHandledException(e);
            return (null);
        }
    }

    public void fill(List<Review> reviews) {
        if (reviews==null) return;
        for(Review review : reviews) {
            Item item = new Item();
            item.rating = review.getScore();
            item.title = Html.fromHtml(review.getTitle()).toString();
            User user = review.getUser();
            if (user!=null) {
                item.author = user.getDisplayName();
            }
            item.date = formatTimestamp(review.getCreatedAt());
            item.comments = Html.fromHtml(review.getContent()).toString();
            array.add(item);
        }
    }
}


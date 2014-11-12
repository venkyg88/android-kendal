package com.staples.mobile.cfa.feed;

/**
 * Author: Yongnan Zhou
 */

import com.squareup.picasso.Picasso;
import com.staples.mobile.R;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.RatingStars;

public class SeenProductsAdapter extends ArrayAdapter<SeenProductsRowItem> {
    private Activity activity;
    private Drawable noPhoto;
    private LayoutInflater inflater;

    public SeenProductsAdapter(Activity activity) {
        super(activity, 0);
        this.activity = activity;
        inflater = activity.getLayoutInflater();
        noPhoto = activity.getResources().getDrawable(R.drawable.no_photo);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SeenProductsRowItem rowItem = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.personal_feed_product_item, parent, false);
        }

        TextView title = (TextView) convertView.findViewById(R.id.title);
        if (title!=null) title.setText(rowItem.getProduceName());

        RatingStars ratingStars = (RatingStars) convertView.findViewById(R.id.rating);
        ratingStars.setRating(Float.parseFloat(rowItem.getRating()), Integer.parseInt(rowItem.getReviewAmount()));

        PriceSticker priceSticker = (PriceSticker) convertView.findViewById(R.id.pricing);
        priceSticker.setPricing(Float.parseFloat(rowItem.getCurrentPrice()), rowItem.getUnitOfMeasure());

        ImageView image = (ImageView) convertView.findViewById(R.id.image);
        if (rowItem.getImageUrl() == null) {
            image.setImageDrawable(noPhoto);
        }
        else {
            Picasso.with(activity).load(rowItem.getImageUrl()).error(noPhoto).into(image);
        }

        return convertView;
    }
}


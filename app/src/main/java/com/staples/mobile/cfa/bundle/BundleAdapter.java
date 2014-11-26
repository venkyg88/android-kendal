package com.staples.mobile.cfa.bundle;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.easyopen.model.browse.Product;

import java.util.List;

public class BundleAdapter extends ArrayAdapter<BundleItem> implements DataWrapper.Layoutable {
    private static final String TAG = "BundleAdapter";

    private Activity activity;
    private LayoutInflater inflater;
    private int layout;
    private Drawable noPhoto;

    public BundleAdapter(Activity activity) {
        super(activity, 0);
        this.activity = activity;
        inflater = activity.getLayoutInflater();
        noPhoto = activity.getResources().getDrawable(R.drawable.no_photo);
    }

    public void setLayout(DataWrapper.Layout layout) {
        if (layout==DataWrapper.Layout.TALL) this.layout = R.layout.bundle_item_tall;
        else  this.layout = R.layout.bundle_item_wide;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        BundleItem item = getItem(position);

        if (view == null)
            view = inflater.inflate(layout, parent, false);

        TextView title = (TextView) view.findViewById(R.id.title);
        if (title != null) title.setText(item.title);

        RatingStars ratingStars = (RatingStars) view.findViewById(R.id.rating);
        ratingStars.setRating(item.customerRating, item.customerCount);

        PriceSticker priceSticker = (PriceSticker) view.findViewById(R.id.pricing);
        priceSticker.setPricing(item.price, item.unit);

        ImageView image = (ImageView) view.findViewById(R.id.image);
        if (item.imageUrl == null) image.setImageDrawable(noPhoto);
        else Picasso.with(activity).load(item.imageUrl).error(noPhoto).into(image);

        return (view);
    }

    public int fill(List<Product> products) {
        if (products==null) return(0);
        int count = 0;
        for (Product product : products) {
            String name = Html.fromHtml(product.getProductName()).toString();
            BundleItem item = new BundleItem(name, product.getSku());
            item.setImageUrl(product.getImage());
            item.setPrice(product.getPricing());
            item.customerRating = product.getCustomerReviewRating();
            item.customerCount = product.getCustomerReviewCount();
            add(item);
            count++;
        }
        notifyDataSetChanged();
        return(count);
    }
}

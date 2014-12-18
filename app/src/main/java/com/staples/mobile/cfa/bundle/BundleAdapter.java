package com.staples.mobile.cfa.bundle;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.easyopen.model.browse.Product;

import java.util.ArrayList;
import java.util.List;

public class BundleAdapter extends RecyclerView.Adapter implements DataWrapper.Layoutable {
    private static final String TAG = "BundleAdapter";

    private static class BundleVH extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView title;
        private RatingStars ratingStars;
        private PriceSticker priceSticker;
        private ImageView action;

        private BundleVH(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            title =  (TextView) view.findViewById(R.id.title);
            ratingStars = (RatingStars) view.findViewById(R.id.rating);
            priceSticker = (PriceSticker) view.findViewById(R.id.pricing);
            action = (ImageView) view.findViewById(R.id.action);
        }
    }

    private Activity activity;
    private LayoutInflater inflater;
    private ArrayList<BundleItem> array;
    private View.OnClickListener listener;
    private int layout;
    private Drawable noPhoto;

    public BundleAdapter(Activity activity) {
        this.activity = activity;
        inflater = activity.getLayoutInflater();
        array = new ArrayList<BundleItem>();
        noPhoto = activity.getResources().getDrawable(R.drawable.no_photo);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public void setLayout(DataWrapper.Layout layout) {
        if (layout==DataWrapper.Layout.TALL) this.layout = R.layout.bundle_item_tall;
        else  this.layout = R.layout.bundle_item_wide;
    }

    @Override
    public int getItemCount() {
        return(array.size());
    }

    @Override
    public BundleVH onCreateViewHolder(ViewGroup parent, int type) {
        View view = inflater.inflate(layout, parent, false);
        BundleVH bvh = new BundleVH(view);
        bvh.itemView.setOnClickListener(listener);
        bvh.action.setOnClickListener(listener);
        return(bvh);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vh, int position) {
        BundleVH bvh = (BundleVH) vh;
        BundleItem item = array.get(position);

        // Set tag for onClickListeners
        bvh.itemView.setTag(item);
        bvh.action.setTag(item);

        // Set content
        if (item.imageUrl == null) bvh.image.setImageDrawable(noPhoto);
        else Picasso.with(activity).load(item.imageUrl).error(noPhoto).into(bvh.image);
        bvh.title.setText(item.title);
        bvh.ratingStars.setRating(item.customerRating, item.customerCount);
        bvh.priceSticker.setPricing(item.price, item.unit);
        bvh.action.setImageResource(R.drawable.ic_launcher);
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
            array.add(item);
            count++;
        }
        notifyDataSetChanged();
        return(count);
    }
}

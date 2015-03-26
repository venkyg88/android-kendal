package com.staples.mobile.cfa.bundle;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.IdentifierType;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.easyopen.model.browse.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BundleAdapter extends RecyclerView.Adapter<BundleAdapter.ViewHolder> implements DataWrapper.Layoutable {
    private static final String TAG = "BundleAdapter";

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView title;
        private RatingStars ratingStars;
        private PriceSticker priceSticker;
        private ImageView action;
        private LinearLayout overweightLayout;
        private LinearLayout addonLayout;

        private ViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            title = (TextView) view.findViewById(R.id.title);
            ratingStars = (RatingStars) view.findViewById(R.id.rating);
            priceSticker = (PriceSticker) view.findViewById(R.id.pricing);
            action = (ImageView) view.findViewById(R.id.bundle_action);
            overweightLayout = (LinearLayout) view.findViewById(R.id.overweight_layout);
            addonLayout = (LinearLayout) view.findViewById(R.id.add_on_layout);
        }
    }

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<BundleItem> array;
    private View.OnClickListener listener;
    private int layout;
    private Drawable noPhoto;

    public BundleAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        array = new ArrayList<BundleItem>();
        noPhoto = context.getResources().getDrawable(R.drawable.no_photo);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public void setLayout(DataWrapper.Layout layout) {
        if (layout==DataWrapper.Layout.TALL) this.layout = R.layout.bundle_item_tall;
        else  this.layout = R.layout.bundle_item_wide;
    }

    /** needed for analytics */
    public int getItemPosition(BundleItem item) {
        return array.indexOf(item);
    }

    @Override
    public int getItemCount() {
        return(array.size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = inflater.inflate(layout, parent, false);
        ViewHolder vh = new ViewHolder(view);

        // Set onClickListeners
        vh.itemView.setOnClickListener(listener);
        vh.action.setOnClickListener(listener);
        return(vh);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        BundleItem item = array.get(position);

        // Set tag for onClickListeners
        vh.itemView.setTag(item);
        vh.action.setTag(item);

        // Set content
        if (item.imageUrl == null) vh.image.setImageDrawable(noPhoto);
        else Picasso.with(context).load(item.imageUrl).error(noPhoto).into(vh.image);
        vh.title.setText(item.title);
        vh.ratingStars.setRating(item.customerRating, item.customerCount);
        vh.priceSticker.setPricing(item.price, item.unit);
        if (item.type==IdentifierType.SKUSET) vh.action.setImageResource(R.drawable.sku_set);
        else vh.action.setImageResource(R.drawable.ic_add_shopping_cart_black);

        // check if the product is an add-on product
        vh.addonLayout.setVisibility((item.isAddOnItem)? View.VISIBLE : View.GONE);

        // check if the product is an overweight product, example sku:650465
        vh.overweightLayout.setVisibility((item.isOverSized)? View.VISIBLE  : View.GONE);
    }

    public void fill(List<Product> products) {
        if (products==null) return;
        for (Product product : products) {
            String name = Html.fromHtml(product.getProductName()).toString();
            BundleItem item = new BundleItem(array.size(), name, product.getSku());
            item.setImageUrl(product.getImage());
            item.setPrice(product.getPricing());
            item.customerRating = product.getCustomerReviewRating();
            item.customerCount = product.getCustomerReviewCount();

            // check if the product is an add-on product
            item.isAddOnItem = !TextUtils.isEmpty(product.getPricing().get(0).getAddOnItem());

            // check if the product is an overweight product, example sku:650465
            item.isOverSized = !TextUtils.isEmpty(product.getPricing().get(0).getOverSizeItem());

            array.add(item);
        }
    }

    public void sort(Comparator<BundleItem> comparator) {
        Collections.sort(array, comparator);
    }
}

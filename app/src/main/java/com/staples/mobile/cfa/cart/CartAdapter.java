/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.R;
import com.staples.mobile.cfa.widget.QuantityEditor;
import com.staples.mobile.cfa.widget.PriceSticker;


public class CartAdapter extends ArrayAdapter<CartItem> {

    private static final String TAG = CartAdapter.class.getSimpleName();

    private Activity activity;
    private LayoutInflater inflater;
    private int cartItemLayoutResId;

    private Drawable noPhoto;

    // widget listeners
    private View.OnClickListener qtyDeleteButtonListener;
    private QuantityEditor.OnQtyChangeListener qtyChangeListener;



    public CartAdapter(Activity activity, int cartItemLayoutResId, QuantityEditor.OnQtyChangeListener qtyChangeListener,
                       View.OnClickListener qtyDeleteButtonListener) {
        super(activity, cartItemLayoutResId);
        this.activity = activity;
        this.cartItemLayoutResId = cartItemLayoutResId;
        this.qtyChangeListener = qtyChangeListener;
        this.qtyDeleteButtonListener = qtyDeleteButtonListener;
        noPhoto = activity.getResources().getDrawable(R.drawable.no_photo);
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }



/* Views */


    @Override
    public View getView(int position, View view, ViewGroup parent) {

        // use view holder pattern to improve listview performance
        ViewHolder vh = null;

        // Get a new or recycled view of the right type
        if (view == null) {
            view = inflater.inflate(cartItemLayoutResId, parent, false);
            vh = new ViewHolder(view); // get various widgets and place in view holder
            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }

        CartItem cartItem = getItem(position);

        // set or hide shipping estimate
        if (cartItem.getExpectedDelivery() != null) {
            String shippingEstimateLabel = activity.getResources().getString(R.string.expected_delivery);
            vh.shipEstimateTextView.setText(shippingEstimateLabel + " " + cartItem.getExpectedDelivery());
            vh.shipEstimateTextView.setVisibility(View.VISIBLE);
        } else {
            vh.shipEstimateTextView.setVisibility(View.GONE);
        }

        // Set image
        String imageUrl = cartItem.getImageUrl();
        if (imageUrl == null) {
            vh.imageView.setImageDrawable(noPhoto);
        } else {
            Picasso.with(activity).load(imageUrl).error(noPhoto).into(vh.imageView);
        }

        // Set title
        vh.titleTextView.setText(cartItem.getDescription());

        // TODO: include original price
        // set price
        vh.priceSticker.setPricing(cartItem.getFinalPrice(), cartItem.getPriceUnitOfMeasure());

        // associate cart position with each widget
        vh.qtyWidget.setTag(position);
        vh.deleteButton.setTag(position);
//        vh.updateButton.setTag(position);

        // associate qty widget with cart item
        cartItem.setQtyWidget(vh.qtyWidget);

        // set widget listeners
        vh.qtyWidget.setOnQtyChangeListener(qtyChangeListener);
        vh.deleteButton.setOnClickListener(qtyDeleteButtonListener);
//        vh.updateButton.setOnClickListener(qtyUpdateButtonListener);

        // set quantity (AFTER listeners set up above)
        vh.qtyWidget.setQtyValue(cartItem.getProposedQty());

        // set visibility of update button
//        vh.updateButton.setVisibility(cartItem.isProposedQtyDifferent()? View.VISIBLE : View.GONE);
        vh.qtyWidget.setErrorIndicator(cartItem.isProposedQtyDifferent() ? "Update failed" : null);

        return(view);
    }

    //---------------------------------------//
    //------------ inner classes ------------//
    //---------------------------------------//

    /************* view holder ************/

    static class ViewHolder {
        TextView shipEstimateTextView;
        ImageView imageView;
        TextView titleTextView;
        PriceSticker priceSticker;
        QuantityEditor qtyWidget;
        Button deleteButton;
//        Button updateButton;

        ViewHolder(View convertView) {
            shipEstimateTextView = (TextView) convertView.findViewById(R.id.cartitem_shipping_estimate);
            imageView = (ImageView) convertView.findViewById(R.id.cartitem_image);
            titleTextView = (TextView) convertView.findViewById(R.id.cartitem_title);
            priceSticker = (PriceSticker) convertView.findViewById(R.id.cartitem_price);
            qtyWidget = (QuantityEditor) convertView.findViewById(R.id.cartitem_qty);
            deleteButton = (Button) convertView.findViewById(R.id.cartitem_delete);
//            updateButton = (Button) convertView.findViewById(R.id.cartitem_update);
        }
    }
}

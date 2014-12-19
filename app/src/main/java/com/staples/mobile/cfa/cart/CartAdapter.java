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
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.QuantityEditor;
import com.staples.mobile.cfa.widget.PriceSticker;


public class CartAdapter extends ArrayAdapter<CartItemGroup> {

    private static final String TAG = CartAdapter.class.getSimpleName();

    private Activity activity;
    private LayoutInflater inflater;
    private int cartItemGroupLayoutResId;

    private Drawable noPhoto;

    // widget listeners
    private View.OnClickListener qtyDeleteButtonListener;
    private QuantityEditor.OnQtyChangeListener qtyChangeListener;


    public CartAdapter(Activity activity, int cartItemGroupLayoutResId,
                       QuantityEditor.OnQtyChangeListener qtyChangeListener,
                       View.OnClickListener qtyDeleteButtonListener) {
        super(activity, cartItemGroupLayoutResId);
        this.activity = activity;
        this.cartItemGroupLayoutResId = cartItemGroupLayoutResId;
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
            view = inflater.inflate(cartItemGroupLayoutResId, parent, false);
            vh = new ViewHolder(view); // get various widgets and place in view holder
            view.setTag(vh);
        } else {
            vh = (ViewHolder) view.getTag();
        }

        CartItemGroup cartItemGroup = getItem(position);

        // set shipping estimate
        String shippingEstimateLabel = activity.getResources().getString(R.string.expected_delivery);
        vh.shipEstimateTextView.setText(shippingEstimateLabel + " " + cartItemGroup.getExpectedDelivery());
        vh.shipEstimateItemQtyTextView.setText(activity.getResources().getQuantityString(R.plurals.cart_qty,
                cartItemGroup.getExpectedDeliveryItemQty(), cartItemGroup.getExpectedDeliveryItemQty()));

        // adjust the number of child views in cart item list
        int listLayoutChildCount = vh.cartItemListLayout.getChildCount();
        int groupSize = cartItemGroup.getCartItems().size();
        if (listLayoutChildCount > groupSize) {
            vh.cartItemListLayout.removeViews(groupSize, listLayoutChildCount - groupSize);
        } else {
            for (int i = listLayoutChildCount; i < groupSize; i++) {
                View v = inflater.inflate(R.layout.cart_item, parent, false);
                v.setTag(new CartItemViewHolder(v));
                vh.cartItemListLayout.addView(v);
            }
        }

        // for each cart item
        for (int i = 0; i < groupSize; i++) {

            CartItem cartItem = cartItemGroup.getCartItems().get(i);
            CartItemViewHolder ciVh = (CartItemViewHolder) vh.cartItemListLayout.getChildAt(i).getTag();

            // Set image
            String imageUrl = cartItem.getImageUrl();
            if (imageUrl == null) {
                ciVh.imageView.setImageDrawable(noPhoto);
            } else {
                Picasso.with(activity).load(imageUrl).error(noPhoto).into(ciVh.imageView);
            }

            // Set title
            ciVh.titleTextView.setText(cartItem.getDescription());

            // TODO: include original price
            // set price
            ciVh.priceSticker.setPricing(cartItem.getFinalPrice(), cartItem.getPriceUnitOfMeasure());

            // associate position with each widget (position of card, and position within group)
            CartItemPosition pos = new CartItemPosition(position, i);
            ciVh.qtyWidget.setTag(pos);
            ciVh.deleteButton.setTag(pos);
//        ciVh.updateButton.setTag(pos);

            // associate qty widget with cart item
            cartItem.setQtyWidget(ciVh.qtyWidget);

            // set widget listeners
            ciVh.qtyWidget.setOnQtyChangeListener(qtyChangeListener);
            ciVh.deleteButton.setOnClickListener(qtyDeleteButtonListener);
//        ciVh.updateButton.setOnClickListener(qtyUpdateButtonListener);

            // set quantity (AFTER listeners set up above)
            ciVh.qtyWidget.setQuantity(cartItem.getProposedQty());

            // set visibility of update button
//        ciVh.updateButton.setVisibility(cartItem.isProposedQtyDifferent()? View.VISIBLE : View.GONE);
            ciVh.qtyWidget.setError(cartItem.isProposedQtyDifferent() ? "Update failed" : null);

            // set visibility of horizontal rule
            ciVh.horizontalRule.setVisibility((i < groupSize-1)? View.VISIBLE : View.GONE);
        }

        return(view);
    }

    //---------------------------------------//
    //------------ inner classes ------------//
    //---------------------------------------//

    /************* view holder ************/

    public static class CartItemPosition {
        public int groupPosition;
        public int itemPositionWithinGroup;

        public CartItemPosition(int groupPosition, int itemPositionWithinGroup) {
            this.groupPosition = groupPosition;
            this.itemPositionWithinGroup = itemPositionWithinGroup;
        }
    }

    static class ViewHolder {
        ViewGroup shipEstimateLayout;
        TextView shipEstimateTextView;
        TextView shipEstimateItemQtyTextView;
        ViewGroup cartItemListLayout;

        ViewHolder(View convertView) {
            shipEstimateLayout = (ViewGroup) convertView.findViewById(R.id.cartitem_shipping_estimate_layout);
            shipEstimateTextView = (TextView) convertView.findViewById(R.id.cartitem_shipping_estimate);
            shipEstimateItemQtyTextView = (TextView) convertView.findViewById(R.id.cartitem_shipping_estimate_itemqty);
            cartItemListLayout = (ViewGroup) convertView.findViewById(R.id.cart_item_list);
        }
    }

    static class CartItemViewHolder {
        ImageView imageView;
        TextView titleTextView;
        PriceSticker priceSticker;
        QuantityEditor qtyWidget;
        Button deleteButton;
//        Button updateButton;
        View horizontalRule;


        CartItemViewHolder(View convertView) {
            imageView = (ImageView) convertView.findViewById(R.id.cartitem_image);
            titleTextView = (TextView) convertView.findViewById(R.id.cartitem_title);
            priceSticker = (PriceSticker) convertView.findViewById(R.id.cartitem_price);
            qtyWidget = (QuantityEditor) convertView.findViewById(R.id.cartitem_qty);
            deleteButton = (Button) convertView.findViewById(R.id.cartitem_delete);
//            updateButton = (Button) convertView.findViewById(R.id.cartitem_update);
            horizontalRule = convertView.findViewById(R.id.cart_item_horizontal_rule);
        }
    }
}

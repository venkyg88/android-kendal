/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.widget.QuantityEditor;
import com.staples.mobile.cfa.widget.PriceSticker;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private static final String TAG = CartAdapter.class.getSimpleName();

    private List<CartItemGroup> cartItemGroups = new ArrayList<CartItemGroup>();

    private int cartItemGroupLayoutResId;

    private Context context;
    private LayoutInflater inflater;
    ViewGroup parentViewGroup;

    private Drawable noPhoto;
    String shippingEstimateLabel;

    // widget listeners
    private View.OnClickListener qtyDeleteButtonListener;
    private View.OnClickListener productImageListener;
    private QuantityEditor.OnQtyChangeListener qtyChangeListener;

    /** constructor */
    public CartAdapter(Context context,
                       int cartItemGroupLayoutResId,
                       QuantityEditor.OnQtyChangeListener qtyChangeListener,
                       View.OnClickListener qtyDeleteButtonListener,
                       View.OnClickListener productImageListener) {
        this.cartItemGroupLayoutResId = cartItemGroupLayoutResId;
        this.qtyChangeListener = qtyChangeListener;
        this.qtyDeleteButtonListener = qtyDeleteButtonListener;
        this.productImageListener = productImageListener;
        this.noPhoto = context.getResources().getDrawable(R.drawable.no_photo);
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.shippingEstimateLabel = context.getResources().getString(R.string.expected_delivery);
    }

    public void setItems(List<CartItemGroup> items) {
        cartItemGroups = items;
        notifyDataSetChanged();
    }

/* Views */

    // Create new views (invoked by the layout manager)
    @Override
    public CartAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        parentViewGroup = parent;
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(cartItemGroupLayoutResId, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        CartItemGroup cartItemGroup = getGroupItem(position);

        // set shipping estimate
        vh.shipEstimateTextView.setText(shippingEstimateLabel + " " + cartItemGroup.getExpectedDelivery());
        vh.shipEstimateItemQtyTextView.setText(context.getResources().getQuantityString(R.plurals.cart_qty,
                cartItemGroup.getExpectedDeliveryItemQty(), cartItemGroup.getExpectedDeliveryItemQty()));

        // adjust the number of child views in cart item list
        int listLayoutChildCount = vh.cartItemListLayout.getChildCount();
        int groupSize = cartItemGroup.getCartItems().size();
        if (listLayoutChildCount > groupSize) {
            vh.cartItemListLayout.removeViews(groupSize, listLayoutChildCount - groupSize);
        } else {
            for (int i = listLayoutChildCount; i < groupSize; i++) {
                View v = inflater.inflate(R.layout.cart_item, parentViewGroup, false);
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
                Picasso.with(context).load(imageUrl).error(noPhoto).into(ciVh.imageView);
            }

            // Set title
            ciVh.titleTextView.setText(cartItem.getDescription());

            // TODO: include original price
            // set price
            ciVh.priceSticker.setPricing(cartItem.getFinalPrice(), cartItem.getPriceUnitOfMeasure());
            ciVh.shippingLogicLayout.setVisibility(View.GONE);

            // associate position with each widget (position of card, and position within group)
            CartItemPosition pos = new CartItemPosition(position, i);
            ciVh.qtyWidget.setTag(pos);
            ciVh.deleteButton.setTag(pos);
            ciVh.imageView.setTag(pos);
//        ciVh.updateButton.setTag(pos);

            // associate qty widget with cart item
            cartItem.setQtyWidget(ciVh.qtyWidget);

            // set widget listeners
            ciVh.qtyWidget.setOnQtyChangeListener(qtyChangeListener);
            ciVh.deleteButton.setOnClickListener(qtyDeleteButtonListener);
            ciVh.imageView.setOnClickListener(productImageListener);
//        ciVh.updateButton.setOnClickListener(qtyUpdateButtonListener);

            // set quantity (AFTER listeners set up above)
            ciVh.qtyWidget.setQuantity(cartItem.getProposedQty());

            // set visibility of update button
//        ciVh.updateButton.setVisibility(cartItem.isProposedQtyDifferent()? View.VISIBLE : View.GONE);
            ciVh.qtyWidget.setError(cartItem.isProposedQtyDifferent() ? "Update failed" : null);

            // set visibility of horizontal rule
            ciVh.horizontalRule.setVisibility((i < groupSize-1)? View.VISIBLE : View.GONE);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cartItemGroups.size();
    }

    public CartItemGroup getGroupItem(int position) {
        return cartItemGroups.get(position);
    }

    public CartItem getCartItem(CartAdapter.CartItemPosition position) {
        CartItemGroup cartItemGroup = getGroupItem(position.groupPosition);
        CartItem cartItem = cartItemGroup.getCartItems().get(position.itemPositionWithinGroup);
        return cartItem;
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewGroup shipEstimateLayout;
        TextView shipEstimateTextView;
        TextView shipEstimateItemQtyTextView;
        ViewGroup cartItemListLayout;

        ViewHolder(View itemView) {
            super(itemView);
            shipEstimateLayout = (ViewGroup) itemView.findViewById(R.id.cartitem_shipping_estimate_layout);
            shipEstimateTextView = (TextView) itemView.findViewById(R.id.cartitem_shipping_estimate);
            shipEstimateItemQtyTextView = (TextView) itemView.findViewById(R.id.cartitem_shipping_estimate_itemqty);
            cartItemListLayout = (ViewGroup) itemView.findViewById(R.id.cart_item_list);
        }
    }

    static class CartItemViewHolder {
        ImageView imageView;
        TextView titleTextView;
        PriceSticker priceSticker;
        QuantityEditor qtyWidget;
        View deleteButton;
//        Button updateButton;
        View horizontalRule;
        LinearLayout shippingLogicLayout;

        CartItemViewHolder(View convertView) {
            imageView = (ImageView) convertView.findViewById(R.id.cartitem_image);
            titleTextView = (TextView) convertView.findViewById(R.id.cartitem_title);
            shippingLogicLayout = (LinearLayout) convertView.findViewById(R.id.shipping_logic_layout);
            priceSticker = (PriceSticker) convertView.findViewById(R.id.cartitem_price);
            qtyWidget = (QuantityEditor) convertView.findViewById(R.id.cartitem_qty);
            deleteButton = convertView.findViewById(R.id.cartitem_delete);
//            updateButton = (Button) convertView.findViewById(R.id.cartitem_update);
            horizontalRule = convertView.findViewById(R.id.cart_item_horizontal_rule);
        }
    }
}

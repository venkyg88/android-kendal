/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

import com.staples.mobile.cfa.widget.CartItemQtyEditor;
import com.staples.mobile.common.access.easyopen.model.cart.Image;
import com.staples.mobile.common.access.easyopen.model.cart.Pricing;
import com.staples.mobile.common.access.easyopen.model.cart.Product;

import java.util.List;

public class CartItem {
    public static final String TAG = "CartItem";

    private Product product;
    private int proposedQty;

    private CartAdapter cartAdapter;
    private QtyDeleteButtonListener qtyDeleteButtonListener;
    private QtyUpdateButtonListener qtyUpdateButtonListener;
    private QtyTextChangeListener qtyTextChangeListener;

    // Constructor
    public CartItem(Product product, CartAdapter cartAdapter) {
        this.product = product;
        this.proposedQty = product.getQuantity();
        this.cartAdapter = cartAdapter;

        // create widget listeners
        qtyDeleteButtonListener = new QtyDeleteButtonListener(this);
        qtyUpdateButtonListener = new QtyUpdateButtonListener(this);
        qtyTextChangeListener = new QtyTextChangeListener(this);
    }


    public String getDescription() {
        return product.getProductName();
    }

    public String getOrderItemId() {
        return product.getOrderItemId();
    }

    public String getSku() {
        return product.getSku();
    }

    public String getPartNumber() {
        return product.getManufacturerPartNumber();
    }

    public Pricing getPricing() {
        List<Pricing> pricings = product.getPricing();
        if (pricings != null) {
            for (Pricing pricing : pricings) {
                if (pricing.getFinalPrice() > 0.0f) {
                    return pricing;
                }
            }
        }
        return null;
    }

    public float getFinalPrice() {
        Pricing pricing = getPricing();
        if (pricing != null) {
            return pricing.getFinalPrice();
        }
        return 0.0f;
    }

    public String getPriceUnitOfMeasure() {
        Pricing pricing = getPricing();
        if (pricing != null) {
            return pricing.getUnitOfMeasure();
        }
        return null;
    }

    public String getImageUrl() {
        List<Image> images = product.getImage();
        if (images != null) {
            for(Image image : images) {
                if (image.getUrl() != null) {
                    return image.getUrl();
                }
            }
        }
        return null;
    }

    public String getThumbnailImageUrl() {
        List<Image> images = product.getThumbnailImage();
        if (images != null) {
            for(Image image : images) {
                if (image.getUrl() != null) {
                    return image.getUrl();
                }
            }
        }
        return null;
    }


    public int getQuantity() {
        return product.getQuantity();
    }

    public void setQuantity(int quantity) {
        product.setQuantity(quantity);
    }

    public int getProposedQty() {
        return proposedQty;
    }

    public void setProposedQty(int proposedQty) {
        this.proposedQty = proposedQty;
    }

    public boolean isProposedQtyDifferent() {
        return getQuantity() != getProposedQty();
    }

    public QtyTextChangeListener getQtyTextChangeListener() {
        return qtyTextChangeListener;
    }

    public QtyDeleteButtonListener getQtyDeleteButtonListener() {
        return qtyDeleteButtonListener;
    }

    public QtyUpdateButtonListener getQtyUpdateButtonListener() {
        return qtyUpdateButtonListener;
    }

    public void setQtyWidgets(CartItemQtyEditor qtyWidget, Button updateButton) {
        qtyTextChangeListener.setUpdateButton(updateButton);
        qtyDeleteButtonListener.setQtyWidget(qtyWidget);
        qtyUpdateButtonListener.setQtyWidget(qtyWidget);
    }

    // listener class for quantity deletion button
    class QtyTextChangeListener implements TextWatcher {
        CartItem cartItem;
        Button updateButton;

        QtyTextChangeListener(CartItem cartItem) {
            this.cartItem = cartItem;
        }

        public void setUpdateButton(Button updateButton) {
            this.updateButton = updateButton;
        }

        @Override public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) { }
        @Override public void afterTextChanged(Editable editable) {}

        @Override public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            // if text is truly different from before, make update button visible, set proposed qty
            if (!charSequence.toString().equals(""+cartItem.getQuantity())) {
                if (charSequence.length() > 0) {
                    try {
                        cartItem.setProposedQty(Integer.parseInt(charSequence.toString()));
                    } catch (NumberFormatException e) {
                        cartItem.setProposedQty(cartItem.getQuantity());
                    }
                } else {
                    cartItem.setProposedQty(cartItem.getQuantity());
                }
            }
            updateButton.setVisibility(cartItem.isProposedQtyDifferent()? View.GONE : View.VISIBLE);
            cartAdapter.notifyDataSetChanged();
        }
    }

    // listener class for quantity deletion button
    class QtyDeleteButtonListener implements View.OnClickListener {

        CartItem cartItem;
        CartItemQtyEditor qtyWidget;

        QtyDeleteButtonListener(CartItem cartItem) {
            this.cartItem = cartItem;
        }

        public void setQtyWidget(CartItemQtyEditor qtyWidget) {
            this.qtyWidget = qtyWidget;
        }

        @Override
        public void onClick(View view) {
            cartAdapter.hideSoftKeyboard(qtyWidget);

//            qtyWidget.setSelection(0); // assumes position zero holds "0" value
            qtyWidget.setText("0");
            setProposedQty(0);

            // update cart via API
//            updateItemQty(cartItemPosition, 0);

        }
    }

    // listener class for quantity deletion button
    class QtyUpdateButtonListener implements View.OnClickListener {

        CartItem cartItem;
        CartItemQtyEditor qtyWidget;

        QtyUpdateButtonListener(CartItem cartItem) {
            this.cartItem = cartItem;
        }

        public void setQtyWidget(CartItemQtyEditor qtyWidget) {
            this.qtyWidget = qtyWidget;
        }

        @Override
        public void onClick(View view) {
            cartAdapter.hideSoftKeyboard(qtyWidget);

//            CartItem cartItem = getItem(cartItemPosition);
            int origQty = cartItem.getQuantity();
            int newQty = origQty;

            String value = qtyWidget.getText().toString();
            if (value != null && value.length() > 0) {
                try { newQty = Integer.parseInt(value); } catch (NumberFormatException e) {}
            } else {
                qtyWidget.setText("" + origQty); // if empty, assume no change
            }
            if (newQty != origQty) {
                // update cart via API
                cartAdapter.updateItemQty(CartItem.this, newQty);
            }

            // hide button after clicking
            view.setVisibility(View.GONE);

//            qtyWidget.setText("" + qty);
        }
    }

}

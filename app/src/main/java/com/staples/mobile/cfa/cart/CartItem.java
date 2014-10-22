/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.staples.mobile.cfa.widget.CartItemQtyEditor;
import com.staples.mobile.common.access.easyopen.model.cart.Image;
import com.staples.mobile.common.access.easyopen.model.cart.Pricing;
import com.staples.mobile.common.access.easyopen.model.cart.Product;

import java.util.List;

public class CartItem {
    private static final String TAG = "CartItem";

    private Product product;
    private int proposedQty;

    private CartAdapter cartAdapter;
    private QtyDeleteButtonListener qtyDeleteButtonListener;
    private QtyUpdateButtonListener qtyUpdateButtonListener;
    private QtyTextChangeListener qtyTextChangeListener;
    private SpinnerChangeListener spinnerChangeListener;

    // Constructor
    public CartItem(Product product, CartAdapter cartAdapter) {
        this.product = product;
        this.proposedQty = product.getQuantity();
        this.cartAdapter = cartAdapter;

        // create widget listeners
        qtyDeleteButtonListener = new QtyDeleteButtonListener(this);
        qtyUpdateButtonListener = new QtyUpdateButtonListener(this);
        qtyTextChangeListener = new QtyTextChangeListener(this);
        spinnerChangeListener = new SpinnerChangeListener(this);
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

    public void resetProposedQty() {
        setProposedQty(getQuantity());
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

    public SpinnerChangeListener getSpinnerChangeListener() {
        return spinnerChangeListener;
    }

    public void setQtyWidgets(CartItemQtyEditor qtyWidget, Button updateButton) {
        qtyTextChangeListener.setUpdateButton(updateButton);
        qtyTextChangeListener.setQtyWidget(qtyWidget);
        qtyDeleteButtonListener.setQtyWidget(qtyWidget);
        qtyUpdateButtonListener.setQtyWidget(qtyWidget);
        spinnerChangeListener.setQtyWidget(qtyWidget);
    }


    /** listener class for text change */
    class QtyTextChangeListener implements TextWatcher, TextView.OnEditorActionListener {
        CartItem cartItem;
        Button updateButton;
        CartItemQtyEditor qtyWidget;

        /** constructor */
        QtyTextChangeListener(CartItem cartItem) {
            this.cartItem = cartItem;
        }

        public void setUpdateButton(Button updateButton) {
            this.updateButton = updateButton;
        }

        public void setQtyWidget(CartItemQtyEditor qtyWidget) {
            this.qtyWidget = qtyWidget;
        }

        @Override
        public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
            // notifying data set changed while keyboard is up causes it to change to alphabetic,
            // so doing it here in onEditorAction instead
            int origQty = cartItem.getQuantity();
            int newQty = qtyWidget.getQtyValue(origQty); // default value to orig in case new value not parseable;
            cartItem.setProposedQty(newQty);
            cartAdapter.notifyDataSetChanged();
            return false;
        }

        @Override public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) { }
        @Override public void afterTextChanged(Editable editable) {
            int origQty = cartItem.getQuantity();
            int newQty = qtyWidget.getQtyValue(origQty); // default value to orig in case new value not parseable;
            cartItem.setProposedQty(newQty);
            // notifying data set changed while keyboard is up causes it to change to alphabetic,
            // so doing it in onEditorAction instead
        }

        @Override public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            // if text is truly different from before, make update button visible, set proposed qty
//            if (!charSequence.toString().equals(""+cartItem.getQuantity())) {
//                if (charSequence.length() > 0) {
//                    try {
//                        cartItem.setProposedQty(Integer.parseInt(charSequence.toString()));
//                    } catch (NumberFormatException e) {
//                        cartItem.resetProposedQty();
//                    }
//                } else {
//                    cartItem.resetProposedQty();
//                }
//            }
            // notifying data set changed while keyboard is up causes it to change to alphabetic,
            // so do it in onEditorAction instead
        }
    }



    /** listener class for quantity widget selection */
    class SpinnerChangeListener implements AdapterView.OnItemSelectedListener {
        CartItem cartItem;
        CartItemQtyEditor qtyWidget;

        /** constructor */
        SpinnerChangeListener(CartItem cartItem) {
            this.cartItem = cartItem;
        }

        public void setQtyWidget(CartItemQtyEditor qtyWidget) {
            this.qtyWidget = qtyWidget;
        }


        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            int origQty = cartItem.getQuantity();
            int newQty = qtyWidget.getQtyValue(origQty); // default value to orig in case new value not parseable;
            cartItem.setProposedQty(newQty);
            cartAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) { }
    }


    /** listener class for item deletion button */
    class QtyDeleteButtonListener implements View.OnClickListener {

        CartItem cartItem;
        CartItemQtyEditor qtyWidget;

        /** constructor */
        QtyDeleteButtonListener(CartItem cartItem) {
            this.cartItem = cartItem;
        }

        public void setQtyWidget(CartItemQtyEditor qtyWidget) {
            this.qtyWidget = qtyWidget;
        }

        @Override
        public void onClick(View view) {
            qtyWidget.hideSoftKeyboard();

            qtyWidget.setQtyValue(0);  // this will trigger selection change which will handle the rest
            setProposedQty(0);

            // update cart via API
//            cartAdapter.updateItemQty(CartItem.this, 0);

//            cartAdapter.notifyDataSetChanged();
        }
    }

    /** listener class for quantity update button */
    class QtyUpdateButtonListener implements View.OnClickListener {

        CartItem cartItem;
        CartItemQtyEditor qtyWidget;

        /** constructor */
        QtyUpdateButtonListener(CartItem cartItem) {
            this.cartItem = cartItem;
        }

        public void setQtyWidget(CartItemQtyEditor qtyWidget) {
            this.qtyWidget = qtyWidget;
        }

        @Override
        public void onClick(View view) {
            qtyWidget.hideSoftKeyboard();

//            CartItem cartItem = getItem(cartItemPosition);
            int origQty = cartItem.getQuantity();
            int newQty = qtyWidget.getQtyValue(origQty); // default value to orig in case new value not parseable


//                qtyWidget.setQtyValue(newQty); // if empty, assume no change

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

/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.staples.mobile.R;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.cart.AddUpdateCart;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.CartUpdateBody;
import com.staples.mobile.common.access.easyopen.model.cart.ItemsAdded;
import com.staples.mobile.common.access.easyopen.model.cart.OrderItemId;
import com.staples.mobile.common.access.easyopen.model.cart.Product;
import com.staples.mobile.common.access.easyopen.model.cart.UpdateOrderItem;
import com.staples.mobile.common.access.easyopen.model.cart.ViewCart;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CartAdapter extends ArrayAdapter<CartItem> {

    private static final String TAG = "CartAdapter";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";

    private static final int MAXFETCH = 50;

    private Activity activity;
    private LayoutInflater inflater;
    private int cartItemLayoutResId;

    private Drawable noPhoto;

    private ViewCartListener viewCartListener = new ViewCartListener();
    private AddUpdateCartListener addUpdateCartListener = new AddUpdateCartListener();


    public CartAdapter(Activity activity, int cartItemLayoutResId) {
        super(activity, cartItemLayoutResId);
        this.activity = activity;
        this.cartItemLayoutResId = cartItemLayoutResId;
        noPhoto = activity.getResources().getDrawable(R.drawable.no_photo);
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    // Items

    public int getTotalCount() {
        int totalCount = 0;
        for (int position = 0;  position < getCount();  position++) {
            CartItem item = getItem(position);
            totalCount += item.getQuantity();
        }
        return totalCount;
    }


    /* Views */


    @Override
    public View getView(int position, View view, ViewGroup parent) {

        // Get a new or recycled view of the right type
        if (view == null) {
            view = inflater.inflate(cartItemLayoutResId, parent, false);
        }

        CartItem item = getItem(position);

        // Set image
        ImageView imageView = (ImageView) view.findViewById(R.id.cartitem_image);
        String imageUrl = item.getImageUrl();
        if (imageUrl == null) {
            imageView.setImageDrawable(noPhoto);
        } else {
            Picasso.with(activity).load(imageUrl).error(noPhoto).into(imageView);
        }

        // Set title
        TextView titleTextView = (TextView) view.findViewById(R.id.cartitem_title);
        titleTextView.setText(item.getDescription());

        // TODO: include original price
        // set price
        PriceSticker priceSticker = (PriceSticker) view.findViewById(R.id.cartitem_price);
        priceSticker.setPricing(item.getFinalPrice(), item.getPriceUnitOfMeasure());

        // set quantity
        EditText qtyWidget = (EditText) view.findViewById(R.id.cartitem_qty);
        qtyWidget.setText("" + item.getQuantity());
//        Spinner qtySpinner = (Spinner) view.findViewById(R.id.cartitem_qty);
//        QtySpinnerAdapter qtySpinnerAdapter = new QtySpinnerAdapter(activity);
//        qtySpinner.setAdapter(qtySpinnerAdapter);
//        qtySpinner.setSelection(qtySpinnerAdapter.getPosition("" + item.getQuantity()));
//        qtySpinner.setOnItemSelectedListener(new QtySpinnerAdapterItemSelectedListener(position));
//        EditText qtyView = (EditText) view.findViewById(R.id.cartitem_qty);
//        qtyView.setText(String.valueOf(item.getQuantity()));

        // add listener to deletion button
        Button deleteButton = (Button)view.findViewById(R.id.cartitem_delete);
        deleteButton.setOnClickListener(new QtyDeleteButtonListener(position, qtyWidget));

        // add listener to update button
        Button updateButton = (Button)view.findViewById(R.id.cartitem_update);
        updateButton.setOnClickListener(new QtyUpdateButtonListener(position, qtyWidget));

        return(view);
    }


    public void fill() {
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);

        // query for items in cart
        easyOpenApi.viewCart(RECOMMENDATION, STORE_ID, LOCALE, ZIPCODE, CATALOG_ID, CLIENT_ID, viewCartListener);

        notifyDataSetChanged();
    }

    public void updateItemQty(int position, int newQty) {
        CartItem cartItem = getItem(position);
        cartItem.setProposedQty(newQty); // record the value we're trying to set, update the model upon success

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);

        //TODO: waiting on api
        // update quantity of item in cart
        easyOpenApi.updateCart(createCartUpdateBody(cartItem, newQty), RECOMMENDATION, STORE_ID,
                LOCALE, ZIPCODE, CATALOG_ID, CLIENT_ID, addUpdateCartListener);
    }

    private CartUpdateBody createCartUpdateBody(CartItem cartItem, int newQty) {
        UpdateOrderItem updateOrderItem = new UpdateOrderItem();
        updateOrderItem.setOrderItemId(cartItem.getOrderItemId());
        updateOrderItem.setPartNumber_0(cartItem.getPartNumber());
        updateOrderItem.setQuantity_0(newQty);
        List<UpdateOrderItem> updateOrderItems = new ArrayList<UpdateOrderItem>();
        updateOrderItems.add(updateOrderItem);
        CartUpdateBody body = new CartUpdateBody();
        body.setUpdateOrderItem(updateOrderItems);
        return body;
    }


    private void hideSoftKeyboard(EditText editText) {
        InputMethodManager keyboard = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }


    //---------------------------------------//
    //------------ inner classes ------------//
    //---------------------------------------//


    // listens for completion of view request
    class ViewCartListener implements Callback<ViewCart> {

        @Override
        public void success(ViewCart viewCart, Response response) {

            // TODO: get items returned from cart API

            // getting data from viewCart request
            List<Cart> cartCollection = viewCart.getCart();
            if (cartCollection == null || cartCollection.size() == 0) {
                notifyDataSetChanged();
                return;
            }
            Cart cart = cartCollection.get(0);
            List<Product> products = cart.getProduct();
            if (products != null) {
                if (getCount() > 0) {
                    clear();
                }
                for (Product product : products) {
                    add(new CartItem(product));
                }
                notifyDataSetChanged();
                return;
            }


            notifyDataSetChanged();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            String msg = "Unable to obtain cart information: " + retrofitError.getMessage();
            Log.d(TAG, msg);
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            notifyDataSetChanged();
        }
    }


    // listens for completion of additions and updates to cart
    class AddUpdateCartListener implements Callback<AddUpdateCart> {

        @Override
        public void success(AddUpdateCart addUpdateCart, Response response) {

            // determine which items were updated
            List<ItemsAdded> itemsAdded = addUpdateCart.getItemsAdded();
            for (int i = 0; i < getCount(); i++) {
                CartItem cartItem = getItem(i);
                if (isCartItemChanged(cartItem.getOrderItemId(), itemsAdded)) {
                    cartItem.setQuantity(cartItem.getProposedQty());
                }
            }

            notifyDataSetChanged();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            String msg = "Failed Cart Update: " + retrofitError.getMessage();
            Log.d(TAG, msg);
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            notifyDataSetChanged();
        }

        private boolean isCartItemChanged(String orderItemId, List<ItemsAdded> itemsAdded) {
            for (ItemsAdded itemAdded : itemsAdded) {
                for (OrderItemId oid : itemAdded.getOrderItemIds()) {
                    if (oid.getOrderItemId().equals(orderItemId)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }



//    // listener class for quantity widget selection
//    class QtySpinnerAdapterItemSelectedListener implements AdapterView.OnItemSelectedListener {
//
//        int cartItemPosition;
//
//        QtySpinnerAdapterItemSelectedListener(int cartItemPosition) {
//            this.cartItemPosition = cartItemPosition;
//        }
//
//        @Override
//        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//            CartItem item = getItem(cartItemPosition);
//            int origQty = item.getQuantity();
//            int newQty = origQty;
//
//            String value = ((TextView)view).getText().toString();
//            if (value != null) {
//                if (!value.endsWith("+")) {
//                    try { newQty = Integer.parseInt(value); } catch (NumberFormatException e) {}
//                }
//            }
//            if (newQty != origQty) {
//                updateItemQty(cartItemPosition, newQty);
//            }
//        }
//
//        @Override
//        public void onNothingSelected(AdapterView<?> adapterView) {
//            Toast.makeText(activity, "nothing selected", Toast.LENGTH_SHORT);
//        }
//    }


    // listener class for quantity deletion button
    class QtyDeleteButtonListener implements View.OnClickListener {

        int cartItemPosition;
        EditText qtyWidget;

        QtyDeleteButtonListener(int cartItemPosition, EditText qtyWidget) {
            this.cartItemPosition = cartItemPosition;
            this.qtyWidget = qtyWidget;
        }

        @Override
        public void onClick(View view) {
            hideSoftKeyboard(qtyWidget);

//            qtyWidget.setSelection(0); // assumes position zero holds "0" value
            qtyWidget.setText("0");


            // update cart via API
//            updateItemQty(cartItemPosition, 0);

        }
    }

    // listener class for quantity deletion button
    class QtyUpdateButtonListener implements View.OnClickListener {

        int cartItemPosition;
        EditText qtyWidget;

        QtyUpdateButtonListener(int cartItemPosition, EditText qtyWidget) {
            this.cartItemPosition = cartItemPosition;
            this.qtyWidget = qtyWidget;
        }

        @Override
        public void onClick(View view) {
            hideSoftKeyboard(qtyWidget);

            CartItem cartItem = getItem(cartItemPosition);
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
                updateItemQty(cartItemPosition, newQty);
            }

//            qtyWidget.setText("" + qty);
        }
    }
}

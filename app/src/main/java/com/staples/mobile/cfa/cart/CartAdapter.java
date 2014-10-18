/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
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
import com.staples.mobile.common.access.easyopen.model.cart.CartContents;
import com.staples.mobile.common.access.easyopen.model.cart.CartUpdate;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.CartRequestBody;
import com.staples.mobile.common.access.easyopen.model.cart.ItemsAdded;
import com.staples.mobile.common.access.easyopen.model.cart.OrderItem;
import com.staples.mobile.common.access.easyopen.model.cart.OrderItemId;
import com.staples.mobile.common.access.easyopen.model.cart.Product;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CartAdapter extends ArrayAdapter<CartItem> {

    public interface ProgressIndicator {
        public void showProgressIndicator();
        public void hideProgressIndicator();
    }

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
    ProgressIndicator progressIndicator;

    private Drawable noPhoto;

    // api listeners
    private ViewCartListener viewCartListener;
    private AddUpdateCartListener addUpdateCartListener;


    public CartAdapter(Activity activity, int cartItemLayoutResId, ProgressIndicator progressIndicator) {
        super(activity, cartItemLayoutResId);
        this.activity = activity;
        this.cartItemLayoutResId = cartItemLayoutResId;
        this.progressIndicator = progressIndicator;
        noPhoto = activity.getResources().getDrawable(R.drawable.no_photo);
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // create api listeners
        viewCartListener = new ViewCartListener();
        addUpdateCartListener = new AddUpdateCartListener();
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
        progressIndicator.showProgressIndicator();

        // query for items in cart
        easyOpenApi.viewCart(RECOMMENDATION, STORE_ID, LOCALE, ZIPCODE, CATALOG_ID, CLIENT_ID, viewCartListener);

        notifyDataSetChanged();
    }

    public void addToCart(String sku) {

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        progressIndicator.showProgressIndicator();

        // update quantity of item in cart
        easyOpenApi.addToCart(createCartRequestBody(sku, 1), RECOMMENDATION, STORE_ID,
                LOCALE, ZIPCODE, CATALOG_ID, CLIENT_ID, addUpdateCartListener);
    }

    public void updateItemQty(int position, int newQty) {
        CartItem cartItem = getItem(position);
        cartItem.setProposedQty(newQty); // record the value we're trying to set, update the model upon success

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        progressIndicator.showProgressIndicator();

        // update quantity of item in cart
        easyOpenApi.updateCart(createCartRequestBody(cartItem, newQty), RECOMMENDATION, STORE_ID,
                LOCALE, ZIPCODE, CATALOG_ID, CLIENT_ID, addUpdateCartListener);
    }

    private CartRequestBody createCartRequestBody(CartItem cartItem, int newQty) {
        OrderItem orderItem = new OrderItem(cartItem.getOrderItemId(), cartItem.getSku(), newQty);
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        orderItems.add(orderItem);
        CartRequestBody body = new CartRequestBody();
        body.setOrderItem(orderItems);
        return body;
    }

    private CartRequestBody createCartRequestBody(String sku, int qty) {
        OrderItem addOrderItem = new OrderItem(null, sku, qty);
        List<OrderItem> addOrderItems = new ArrayList<OrderItem>();
        addOrderItems.add(addOrderItem);
        CartRequestBody body = new CartRequestBody();
        body.setOrderItem(addOrderItems);
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
    class ViewCartListener implements Callback<CartContents> {

        @Override
        public void success(CartContents cartContents, Response response) {
            progressIndicator.hideProgressIndicator();

            // getting data from cartContent request
            List<Cart> cartCollection = cartContents.getCart();
            if (cartCollection != null && cartCollection.size() > 0) {
                Cart cart = cartCollection.get(0);
                List<Product> products = cart.getProduct();
                if (products != null) {
                    if (getCount() > 0) {
                        clear();
                    }
                    for (Product product : products) {
                        add(new CartItem(product));
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            progressIndicator.hideProgressIndicator();
            String msg = "Unable to obtain cart information: " + retrofitError.getMessage();
            Log.d(TAG, msg);
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            notifyDataSetChanged();

            // note: workaround to unknown field errors is to annotate model with @JsonIgnoreProperties(ignoreUnknown = true)
        }
    }


    // listens for completion of additions and updates to cart
    class AddUpdateCartListener implements Callback<CartUpdate> {

        @Override
        public void success(CartUpdate cartUpdate, Response response) {
            progressIndicator.hideProgressIndicator();

            // if message, display to user (e.g. out-of-stock message)
            if (!TextUtils.isEmpty(cartUpdate.getMessage())) {
                Toast.makeText(activity, cartUpdate.getMessage(), Toast.LENGTH_LONG).show();
            }

            // if no items added, no need to update display
            if (cartUpdate.getItemsAdded().size() == 0) {
                return;
            }

            // Note: there's no way i can see to distinguish between responses to add and update,
            // and there could be a race condition. I'm using the proposedQty variable to flag that
            // an update was requested. If an add response returns first and matches an existing item
            // identifier, then it could resemble an update. However, I am treating it as an update
            // regardless, because when the real update response comes, it will then look like an
            // add which will result in a full refresh of the cart.

            // determine which if any items were updated and fix their qty without refreshing the cart
            int updatedItems = 0;
            List<ItemsAdded> itemsAdded = cartUpdate.getItemsAdded();
            for (int i = 0; i < getCount(); i++) {
                CartItem cartItem = getItem(i);
                if (cartItem.getProposedQty() != -1 &&
                        isCartItemChanged(cartItem.getOrderItemId(), itemsAdded)) {
                    cartItem.setQuantity(cartItem.getProposedQty());
                    cartItem.setProposedQty(-1);
                    updatedItems++;
                }
            }

            // if no items updated or a mismatch in the number, then it was probably an add action,
            // so do a full refresh of the cart (see note above)
            if (itemsAdded.size() > updatedItems) {
                fill();
            }

            notifyDataSetChanged();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            progressIndicator.hideProgressIndicator();
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

//        private boolean isAtLeastOneItem(List<ItemsAdded> itemsAdded) {
//            for (ItemsAdded itemAdded : itemsAdded) {
//                for (OrderItemId oid : itemAdded.getOrderItemIds()) {
//                    return true; // if any, return true
//                }
//            }
//            return false;
//        }
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

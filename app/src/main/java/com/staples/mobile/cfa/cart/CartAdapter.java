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
import com.staples.mobile.cfa.widget.CartItemQtyEditor;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.cart.*;

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

    private Activity activity;
    private LayoutInflater inflater;
    private int cartItemLayoutResId;
    ProgressIndicator progressIndicator;

    private Drawable noPhoto;

    // api listeners
    private ViewCartListener viewCartListener;
    private AddUpdateCartListener addtoCartListener;
    private AddUpdateCartListener updateCartListener;
    private DeleteFromCartListener deleteFromCartListener;


    public CartAdapter(Activity activity, int cartItemLayoutResId, ProgressIndicator progressIndicator) {
        super(activity, cartItemLayoutResId);
        this.activity = activity;
        this.cartItemLayoutResId = cartItemLayoutResId;
        this.progressIndicator = progressIndicator;
        noPhoto = activity.getResources().getDrawable(R.drawable.no_photo);
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // create api listeners
        viewCartListener = new ViewCartListener();
        addtoCartListener = new AddUpdateCartListener(false);
        updateCartListener = new AddUpdateCartListener(true);
        deleteFromCartListener = new DeleteFromCartListener();
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

        CartItem cartItem = getItem(position);

        // Set image
        ImageView imageView = (ImageView) view.findViewById(R.id.cartitem_image);
        String imageUrl = cartItem.getImageUrl();
        if (imageUrl == null) {
            imageView.setImageDrawable(noPhoto);
        } else {
            Picasso.with(activity).load(imageUrl).error(noPhoto).into(imageView);
        }

        // Set title
        TextView titleTextView = (TextView) view.findViewById(R.id.cartitem_title);
        titleTextView.setText(cartItem.getDescription());

        // TODO: include original price
        // set price
        PriceSticker priceSticker = (PriceSticker) view.findViewById(R.id.cartitem_price);
        priceSticker.setPricing(cartItem.getFinalPrice(), cartItem.getPriceUnitOfMeasure());


        // get qty related widgets
        CartItemQtyEditor qtyWidget = (CartItemQtyEditor)view.findViewById(R.id.cartitem_qty);
        Button deleteButton = (Button)view.findViewById(R.id.cartitem_delete);
        Button updateButton = (Button)view.findViewById(R.id.cartitem_update);

        // set up widget listeners
        cartItem.setQtyWidgets(qtyWidget, updateButton);
        qtyWidget.setTextChangedListener(cartItem.getQtyTextChangeListener());
        deleteButton.setOnClickListener(cartItem.getQtyDeleteButtonListener());
        updateButton.setOnClickListener(cartItem.getQtyUpdateButtonListener());

        // set quantity (AFTER listeners set up above)
        qtyWidget.setText("" + cartItem.getProposedQty());

//        Spinner qtySpinner = (Spinner) view.findViewById(R.id.cartitem_qty);
//        QtySpinnerAdapter qtySpinnerAdapter = new QtySpinnerAdapter(activity);
//        qtySpinner.setAdapter(qtySpinnerAdapter);
//        qtySpinner.setSelection(qtySpinnerAdapter.getPosition("" + item.getQuantity()));
//        qtySpinner.setOnItemSelectedListener(new QtySpinnerAdapterItemSelectedListener(position));
//        EditText qtyView = (EditText) view.findViewById(R.id.cartitem_qty);
//        qtyView.setText(String.valueOf(item.getQuantity()));

        // set visibility of update button
        updateButton.setVisibility(cartItem.isProposedQtyDifferent()? View.VISIBLE : View.GONE);

        return(view);
    }

    /** refreshes cart (fills data set with contents of cart) */
    public void fill() {
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        progressIndicator.showProgressIndicator();

        // query for items in cart
        easyOpenApi.viewCart(RECOMMENDATION, STORE_ID, LOCALE, ZIPCODE, CATALOG_ID, CLIENT_ID,
                1, 1000, viewCartListener); // 0 offset results in max of 5 items, so using 1

        notifyDataSetChanged();
    }

    /** adds item to cart */
    public void addToCart(String sku, int qty) {

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        progressIndicator.showProgressIndicator();


        // update quantity of item in cart
        easyOpenApi.addToCart(createCartRequestBody(sku, qty), RECOMMENDATION, STORE_ID,
                LOCALE, ZIPCODE, CATALOG_ID, CLIENT_ID, addtoCartListener);
    }

    /** updates item quantity */
    public void updateItemQty(CartItem cartItem, int newQty) {
        if (newQty == 0) {
            deleteItem(cartItem);
        } else {
//            CartItem cartItem = getItem(position);
            cartItem.setProposedQty(newQty); // record the value we're trying to set, update the model upon success

            EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
            progressIndicator.showProgressIndicator();

            // update quantity of item in cart
            easyOpenApi.updateCart(createCartRequestBody(cartItem, newQty), RECOMMENDATION, STORE_ID,
                    LOCALE, ZIPCODE, CATALOG_ID, CLIENT_ID, updateCartListener);
        }
    }

    /** deletes an item from the cart */
    public void deleteItem(CartItem cartItem) {
//        CartItem cartItem = getItem(position);
        cartItem.setProposedQty(0); // record the value we're trying to set, update the model upon success

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        progressIndicator.showProgressIndicator();

        // delete item from cart
        easyOpenApi.deleteFromCart(RECOMMENDATION, STORE_ID, cartItem.getOrderItemId(),
                LOCALE, CLIENT_ID, deleteFromCartListener);
    }


    //for updating
    private TypedJsonString createCartRequestBody(CartItem cartItem, int newQty) {
        OrderItem orderItem = new OrderItem(cartItem.getOrderItemId(), cartItem.getSku(), newQty);
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        orderItems.add(orderItem);
        //TODO add more cart items as required
        String json = CartBodyGenerator.generateUpdateBody(orderItems);
        return new TypedJsonString(json);
    }

    //for adding
    private TypedJsonString createCartRequestBody(String sku, int qty) {
        OrderItem addOrderItem = new OrderItem(null, sku, qty);
        List<OrderItem> addOrderItems = new ArrayList<OrderItem>();
        addOrderItems.add(addOrderItem);
        //TODO add more cart items as required
        String json = CartBodyGenerator.generateAddBody(addOrderItems);
        return new TypedJsonString(json);
    }

    public void hideSoftKeyboard(EditText editText) {
        InputMethodManager keyboard = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }


    // called by cart listeners below
    private void respondToFailure(String msg) {
        progressIndicator.hideProgressIndicator();
        Log.d(TAG, msg);
        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
        notifyDataSetChanged();
    }

    //---------------------------------------//
    //------------ inner classes ------------//
    //---------------------------------------//


    /** listens for completion of view request */
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
                        add(new CartItem(product, CartAdapter.this));
                    }
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            respondToFailure("Unable to obtain cart information: " + retrofitError.getMessage());
            // note: workaround to unknown field errors is to annotate model with @JsonIgnoreProperties(ignoreUnknown = true)
        }
    }


    /** listens for completion of additions and updates to cart */
    class AddUpdateCartListener implements Callback<CartUpdate> {

        boolean update;

        AddUpdateCartListener(boolean update) {
            this.update = update;
        }

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

            // if an update
            if (update) {
                // determine which items were updated and fix their qty without refreshing the cart
                List<String> itemIds = convertItemIdsToStringList(cartUpdate.getItemsAdded());
                for (int i = 0; i < getCount(); i++) {
                    CartItem cartItem = getItem(i);
                    if (cartItem.isProposedQtyDifferent() && itemIds.contains(cartItem.getOrderItemId())) {
                        cartItem.setQuantity(cartItem.getProposedQty());
                    }
                }
            } else {
                //otherwise an insert, so refresh cart
                fill();
            }

            notifyDataSetChanged();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            respondToFailure("Failed Cart Update: " + retrofitError.getMessage());
        }

        /** converts list of items into list of ids */
        private List<String> convertItemIdsToStringList(List<ItemsAdded> itemsAdded) {
            List<String> ids = new ArrayList<String>();
            for (ItemsAdded itemAdded : itemsAdded) {
                for (OrderItemId oid : itemAdded.getOrderItemIds()) {
                    ids.add(oid.getOrderItemId());
                }
            }
            return ids;
        }
    }



    /** listens for completion of deletion request */
    class DeleteFromCartListener implements Callback<DeleteFromCart> {

        @Override
        public void success(DeleteFromCart cartContents, Response response) {
            progressIndicator.hideProgressIndicator();
            // determine which items were deleted and update their qty
            for (int i = 0; i < getCount(); i++) {
                CartItem cartItem = getItem(i);
                if (cartItem.getProposedQty() == 0) {
                    cartItem.setQuantity(0);
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            respondToFailure("Failed Cart Update: " + retrofitError.getMessage());
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

}

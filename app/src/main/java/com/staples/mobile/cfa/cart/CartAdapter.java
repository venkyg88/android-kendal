/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.staples.mobile.R;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.widget.QuantityEditor;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.cart.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CartAdapter extends ArrayAdapter<CartItem> {

    public interface ProgressIndicator {
        public void showProgressIndicator();
        public void hideProgressIndicator();
    }

    private static final String TAG = CartAdapter.class.getSimpleName();

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
//    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;

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

    // widget listeners
    private QtyDeleteButtonListener qtyDeleteButtonListener;
//    private QtyUpdateButtonListener qtyUpdateButtonListener;
    private QtyChangeListener qtyChangeListener;

    // cart object
    private Cart cart;

    int minExpectedBusinessDays;
    int maxExpectedBusinessDays;


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

        // create widget listeners
        qtyDeleteButtonListener = new QtyDeleteButtonListener();
//        qtyUpdateButtonListener = new QtyUpdateButtonListener();
        qtyChangeListener = new QtyChangeListener();

    }



    public Cart getCart() {
        return cart;
    }

    public int getMinExpectedBusinessDays() {
        return minExpectedBusinessDays;
    }

    public int getMaxExpectedBusinessDays() {
        return maxExpectedBusinessDays;
    }

    public String getExpectedDeliveryRange() {
        if (maxExpectedBusinessDays > minExpectedBusinessDays) {
            return minExpectedBusinessDays + " - " + maxExpectedBusinessDays;
        } else {
            return ""+minExpectedBusinessDays;
        }
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
            vh.shipEstimateTextView.setText(cartItem.getExpectedDelivery());
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

    /** refreshes cart (fills data set with contents of cart) */
    public void fill() {
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        progressIndicator.showProgressIndicator();

        // query for items in cart
        easyOpenApi.viewCart(RECOMMENDATION, STORE_ID, LOCALE, ZIPCODE, CATALOG_ID, CLIENT_ID,
                1, 1000, viewCartListener); // 0 offset results in max of 5 items, so using 1
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
    public void updateItemQty(CartItem cartItem) {
        if (cartItem.isProposedQtyDifferent()) {
            if (cartItem.getProposedQty() == 0) {
                deleteItem(cartItem);
            } else {
                EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
                progressIndicator.showProgressIndicator();

                // update quantity of item in cart
                easyOpenApi.updateCart(createCartRequestBody(cartItem, cartItem.getProposedQty()), RECOMMENDATION, STORE_ID,
                        LOCALE, ZIPCODE, CATALOG_ID, CLIENT_ID, updateCartListener);
            }
        }
    }

    /** deletes an item from the cart */
    public void deleteItem(CartItem cartItem) {
        cartItem.setProposedQty(0); // record the value we're trying to set, update the model upon success

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        progressIndicator.showProgressIndicator();

        // delete item from cart
        easyOpenApi.deleteFromCart(RECOMMENDATION, STORE_ID, cartItem.getOrderItemId(),
                LOCALE, CLIENT_ID, deleteFromCartListener);
    }

    /** gets the billing address for the user account */
    public void getBillingAddress() {
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(true);
        progressIndicator.showProgressIndicator();

        easyOpenApi.getBillingAddress(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, new Callback<AddressDetail>(){

            @Override
            public void success(AddressDetail billingDetail, Response response) {
                int code = response.getStatus();

                Log.i("Status Code", " " + code);
                Log.i("Billing  Address 1", billingDetail.getAddress().get(0).getAddress1());
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.i("Fail Message for getting the billing address", " " + ApiError.getErrorMessage(retrofitError));
                Log.i("Post URL address for getting the billing address", " " + retrofitError.getUrl());
            }
        }
        );
    }

    /** gets the shipping address for the user account */
    public void getShippingAddress() {
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(true);
        progressIndicator.showProgressIndicator();

        easyOpenApi.getShippingAddress(RECOMMENDATION, STORE_ID, LOCALE, CLIENT_ID, new Callback<AddressDetail>() {

                    @Override
                    public void success(AddressDetail billingDetail, Response response) {
                        int code = response.getStatus();

                        Log.i("Status Code", " " + code);
                        Log.i("Shipping  Address 1", billingDetail.getAddress().get(0).getAddress1());
                    }

                    @Override
                    public void failure(RetrofitError retrofitError) {
                        Log.i("Fail Message for getting the shipping address", " " + ApiError.getErrorMessage(retrofitError));
                        Log.i("Post URL address for getting the shipping address", " " + retrofitError.getUrl());
                    }
                }
        );
    }

    //for updating
    private TypedJsonString createCartRequestBody(CartItem cartItem, int newQty) {
        OrderItem orderItem = new OrderItem(cartItem.getOrderItemId(), cartItem.getSku(), newQty);
        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        orderItems.add(orderItem);
        //TODO add more cart items as required
        //generates json string for corresponding updates
        String json = CartBodyGenerator.generateUpdateBody(orderItems);
        return new TypedJsonString(json);
    }

    //for adding
    private TypedJsonString createCartRequestBody(String sku, int qty) {
        OrderItem addOrderItem = new OrderItem(null, sku, qty);
        List<OrderItem> addOrderItems = new ArrayList<OrderItem>();
        addOrderItems.add(addOrderItem);
        //TODO add more cart items as required
        //generates json string for corresponding updates
        String json = CartBodyGenerator.generateAddBody(addOrderItems);
        return new TypedJsonString(json);
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

    /************* view holder ************/

    static class ViewHolder {
        TextView shipEstimateTextView;
        ImageView imageView;
        TextView titleTextView;
        PriceSticker priceSticker;
        QuantityEditor qtyWidget;
        Button deleteButton;
        Button updateButton;

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

    /************* api listeners ************/


    /** listens for completion of view request */
    class ViewCartListener implements Callback<CartContents> {

        @Override
        public void success(CartContents cartContents, Response response) {
            progressIndicator.hideProgressIndicator();

            // clear the cart before refilling
            cart = null;
            clear();


            // get data from cartContent request
            List<Cart> cartCollection = cartContents.getCart();
            if (cartCollection != null && cartCollection.size() > 0) {
                cart = cartCollection.get(0);
                List<Product> products = cart.getProduct();
                if (products != null) {
                    List<CartItem> listItems = new ArrayList<CartItem>();
                    // iterate thru products in reverse order so newest item appears first
                    for (int i = products.size() - 1;  i >= 0;  i--) {
                        Product product = products.get(i);
                        CartItem cartItem = new CartItem(product);
                        listItems.add(cartItem);
                    }

                    // sort by expected delivery date
                    Collections.sort(listItems, new Comparator<CartItem>() {
                        @Override
                        public int compare(CartItem cartItem1, CartItem cartItem2) {
                            if (cartItem1.getMinExpectedBusinessDays() != cartItem2.getMinExpectedBusinessDays()) {
                                return cartItem1.getMinExpectedBusinessDays() - cartItem2.getMinExpectedBusinessDays();
                            } else {
                                return cartItem1.getMaxExpectedBusinessDays() - cartItem2.getMaxExpectedBusinessDays();
                            }
                        }
                    });

                    // calculate expected delivery times
                    String shippingEstimateLabel = activity.getResources().getString(R.string.expected_delivery);
                    String leadTimeDescription = null;
                    minExpectedBusinessDays = -1;
                    maxExpectedBusinessDays = -1;
                    for (int i = 0; i < listItems.size(); i++) {
                        CartItem cartItem = listItems.get(i);
                        if (cartItem.getLeadTimeDescription() != null  &&
                                !cartItem.getLeadTimeDescription().equals(leadTimeDescription)) {
                            leadTimeDescription = cartItem.getLeadTimeDescription();
                            cartItem.setExpectedDelivery(shippingEstimateLabel + " " + leadTimeDescription);
                        }
                        if (minExpectedBusinessDays == -1 ||
                                cartItem.getMinExpectedBusinessDays() < minExpectedBusinessDays) {
                            minExpectedBusinessDays = cartItem.getMinExpectedBusinessDays();
                        }
                        if (maxExpectedBusinessDays == -1 ||
                                cartItem.getMaxExpectedBusinessDays() > maxExpectedBusinessDays) {
                            maxExpectedBusinessDays = cartItem.getMaxExpectedBusinessDays();
                        }
                    }

                    // add items to the adapter list
                    addAll(listItems);
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            respondToFailure("Unable to obtain cart information: " + ApiError.getErrorMessage(retrofitError));
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
            } else {
                // sometimes out-of-stock message is here instead
                String errMsg = ApiError.getApiSuccessError(cartUpdate);
                if (!TextUtils.isEmpty(errMsg)) {
                    Toast.makeText(activity, errMsg, Toast.LENGTH_LONG).show();
                }
            }

            // if a successful insert, refill cart
            if (cartUpdate.getItemsAdded().size() > 0) {
                fill();  // need updated info about the cart such as shipping and subtotals in addition to new quantities
            } else {
                // notify data set changed because qty text may have changed, but actual qty not
                // and we need update button to be visible
                notifyDataSetChanged();
            }
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            respondToFailure("Failed Cart Update: " + ApiError.getErrorMessage(retrofitError));
        }
    }



    /** listens for completion of deletion request */
    class DeleteFromCartListener implements Callback<DeleteFromCart> {

        @Override
        public void success(DeleteFromCart cartContents, Response response) {
            progressIndicator.hideProgressIndicator();
            fill();
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            respondToFailure("Failed Cart Item Deletion: " + ApiError.getErrorMessage(retrofitError));
        }
    }



    /************* widget listeners ************/


    /** listener class for text change */
    class QtyChangeListener implements QuantityEditor.OnQtyChangeListener {
        @Override
        public void onQtyChange(View view, boolean validSpinnerValue) {
            CartItem cartItem = getItem((Integer)view.getTag());

            // default proposed qty to orig in case new value not parseable;
            cartItem.setProposedQty(cartItem.getQtyWidget().getQtyValue(cartItem.getQuantity()));

            // if valid spinner value, then automatically update the cart
//            if (validSpinnerValue) {
//                updateItemQty(cartItem);
//            } else { // otherwise notify data set changed to make update button appear or disappear
//                notifyDataSetChanged();
//            }
              updateItemQty(cartItem);
        }
    }


    /** listener class for item deletion button */
    class QtyDeleteButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            CartItem cartItem = getItem((Integer)view.getTag());

            cartItem.getQtyWidget().hideSoftKeyboard();

            // delete from cart via API
            cartItem.setProposedQty(0);
            updateItemQty(cartItem);

//            cartItem.getQtyWidget().setQtyValue(0);  // this will trigger selection change which will handle the rest
        }
    }

    /** listener class for quantity update button */
    class QtyUpdateButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            CartItem cartItem = getItem((Integer)view.getTag());

            cartItem.getQtyWidget().hideSoftKeyboard();

            // default proposed value to orig in case new value not parseable
            cartItem.setProposedQty(cartItem.getQtyWidget().getQtyValue(cartItem.getQuantity()));

            // update cart via API
            updateItemQty(cartItem);

            // hide button after clicking
            view.setVisibility(View.GONE);
        }
    }
}

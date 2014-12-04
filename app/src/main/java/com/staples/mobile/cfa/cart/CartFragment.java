/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.animation.LayoutTransition;
import android.app.Fragment;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.BaseFragment;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.MainApplication;
import com.staples.mobile.cfa.checkout.CheckoutFragment;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.cfa.widget.QuantityEditor;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.configurator.model.Configurator;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.CartContents;
import com.staples.mobile.common.access.easyopen.model.cart.CartUpdate;
import com.staples.mobile.common.access.easyopen.model.cart.DeleteFromCart;
import com.staples.mobile.common.access.easyopen.model.cart.OrderItem;
import com.staples.mobile.common.access.easyopen.model.cart.Product;
import com.staples.mobile.common.access.easyopen.model.cart.TypedJsonString;
import com.staples.mobile.common.access.lms.LmsManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/** fragment to manage display and update of shopping cart */
public class CartFragment extends BaseFragment implements View.OnClickListener {

    public interface AddToCartCallback {
        public void onAddToCartComplete();
    }

    private static final String TAG = CartFragment.class.getSimpleName();

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";

    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";

    private static final String ZIPCODE = "01010";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;

    private static final int MAXFETCH = 50;


    // saving around Activity object since getActivity() returns null after user navigates away from
    // fragment, but api call may still be returning
    private MainActivity activity;

    private TextView cartSubtotal;
    private TextView cartFreeShippingMsg;
    private TextView cartShipping;
    private View emptyCartMsg;
    private View cartProceedToCheckout;
    private View cartShippingLayout;
    private View cartSubtotalLayout;
    private CartAdapter cartAdapter;
    private ListView cartListVw;

    // cart object - make these static so they're not lost on device rotation
    private static Cart cart;
    private static List<CartItem> cartListItems;


    int minExpectedBusinessDays;
    int maxExpectedBusinessDays;


    // widget listeners
    private QtyDeleteButtonListener qtyDeleteButtonListener;
    private QtyChangeListener qtyChangeListener;
    //private QtyUpdateButtonListener qtyUpdateButtonListener;


    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();


    // api listeners
    private ViewCartListener viewCartListener;
    private AddUpdateCartListener addToCartListener;
    private AddUpdateCartListener updateCartListener;
    private DeleteFromCartListener deleteFromCartListener;

    /** default constructor - note that fragment instance will be retained whereas view will come and go as attached to activity */
    public CartFragment() {
        // create api listeners
        viewCartListener = new ViewCartListener();
        addToCartListener = new AddUpdateCartListener(false);
        updateCartListener = new AddUpdateCartListener(true);
        deleteFromCartListener = new DeleteFromCartListener();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        activity = (MainActivity)getActivity();

        // inflate and get child views
        View view = inflater.inflate(R.layout.cart_fragment, container, false);

        emptyCartMsg = view.findViewById(R.id.empty_cart_msg);
        cartFreeShippingMsg = (TextView) view.findViewById(R.id.free_shipping_msg);
        cartShipping = (TextView) view.findViewById(R.id.cart_shipping);
        cartSubtotal = (TextView) view.findViewById(R.id.cart_subtotal);
        cartShippingLayout = view.findViewById(R.id.cart_shipping_layout);
        cartSubtotalLayout = view.findViewById(R.id.cart_subtotal_layout);
        cartProceedToCheckout = view.findViewById(R.id.action_checkout);


        // create widget listeners
        qtyChangeListener = new QtyChangeListener();
        qtyDeleteButtonListener = new QtyDeleteButtonListener();
//        qtyUpdateButtonListener = new QtyUpdateButtonListener();

        // Initialize cart listview
        cartAdapter = new CartAdapter(activity, R.layout.cart_item, qtyChangeListener, qtyDeleteButtonListener);
        cartAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateCartFields();
            }
        });
        cartListVw = (ListView) view.findViewById(R.id.cart_list);
        cartListVw.setAdapter(cartAdapter);
        cartListVw.setOnScrollListener(new AbsListView.OnScrollListener() {
            int oldFirstVisibleItem = 0;
            int oldTop = 0;
            @Override public void onScrollStateChanged(AbsListView absListView, int scrollState) { }
            @Override public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int top = getTopOfFirstVisibleView(absListView);
                if (firstVisibleItem == 0 && top == 0) {
                    onScrollUp();
                } else if (firstVisibleItem == oldFirstVisibleItem) {
                    if (oldTop - top > 5) {
                        onScrollDown();
                    } else if (top - oldTop > 5 && firstVisibleItem + visibleItemCount < totalItemCount) { // accounting for scroll bounce at the bottom
                        onScrollUp();
                    }
                } else if (firstVisibleItem > oldFirstVisibleItem) {
                    onScrollDown();
                } else if (firstVisibleItem < oldFirstVisibleItem) {
                    onScrollUp();
                }
                oldFirstVisibleItem = firstVisibleItem;
                oldTop = top;
            }

            private void onScrollUp() {
                if (cartShippingLayout.getVisibility() != View.VISIBLE) {
                    cartShippingLayout.setVisibility(View.VISIBLE); // show math story
                }
            }
            private void onScrollDown() {
                if (cartShippingLayout.getVisibility() != View.GONE) {
                    cartShippingLayout.setVisibility(View.GONE); // hide math story
                }
            }
        });

        // Set click listeners
        cartProceedToCheckout.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // update action bar
        activity.showCartActionBarEntities();
        activity.setActionBarTitle(getResources().getString(R.string.cart_title));

        //initialize cart based on what's been returned from api so far
        setAdapterListItems();
    }

    @Override
    public void onPause() {
        super.onPause();
        cartAdapter = null;
    }

    /** Sets item count indicator on cart icon and cart drawer title */
    private void updateCartFields() {
        Resources r = getResources();

        int totalItemCount = 0;
        String shipping = "";
        float subtotal = 0;
        float preTaxSubtotal = 0;
        float freeShippingThreshold = 0;
        if (cart != null) {
            totalItemCount = cart.getTotalItems();
            shipping = cart.getDelivery();
            subtotal = cart.getSubTotal();
            preTaxSubtotal = cart.getPreTaxTotal();
            LmsManager lmsManager = new LmsManager(MainApplication.application);
            Configurator configurator = lmsManager.getConfigurator();
            if (configurator != null) {
                freeShippingThreshold = configurator.getPromotions().getFreeShippingThreshold().floatValue();
            }
        }

        // set text of cart icon badge
        activity.updateCartIcon(totalItemCount);

        // if fragment is attached to activity, then update the fragment's views
        if (cartAdapter != null) {

            // Set text of cart item qty
            if (totalItemCount == 0) {
                activity.setActionBarCartQty("");
                emptyCartMsg.setVisibility(View.VISIBLE);
            } else {
                activity.setActionBarCartQty(r.getQuantityString(R.plurals.cart_qty, totalItemCount, totalItemCount));
                emptyCartMsg.setVisibility(View.GONE);
            }

            // set text of free shipping msg
            if (totalItemCount > 0) {
                if (freeShippingThreshold > subtotal && !"Free".equals(shipping) && !ProfileDetails.isRewardsMember()) {
                    // need to spend more to qualify for free shipping
                    cartFreeShippingMsg.setVisibility(View.VISIBLE);
                    cartFreeShippingMsg.setText(String.format(r.getString(R.string.free_shipping_msg1),
                            currencyFormat.format(freeShippingThreshold), currencyFormat.format(freeShippingThreshold - subtotal)));
                    cartFreeShippingMsg.setBackgroundColor(0xff3f6fff); // blue
                } else {
                    // qualifies for free shipping
                    String freeShippingMsg = r.getString(R.string.free_shipping_msg2);
                    if (!freeShippingMsg.equals(cartFreeShippingMsg.getText().toString())) {
                        cartFreeShippingMsg.setVisibility(View.VISIBLE);
                        cartFreeShippingMsg.setText(freeShippingMsg);
                        cartFreeShippingMsg.setBackgroundColor(0xff00ff00); // green
                        // hide after a delay
                        cartFreeShippingMsg.postDelayed(new Runnable() {
                            @Override public void run() {
                                cartFreeShippingMsg.setVisibility(View.GONE);
                            }
                        }, 3000);
                    }
                }
            } else {
                cartFreeShippingMsg.setVisibility(View.GONE);
            }

            // set text of shipping and subtotal
            cartShipping.setText(CheckoutFragment.formatShippingCharge(shipping, currencyFormat));
            cartSubtotal.setText(currencyFormat.format(preTaxSubtotal));

            // only show shipping, subtotal, and proceed-to-checkout when at least one item
            if (totalItemCount == 0) {
                cartShippingLayout.setVisibility(View.GONE);
                cartSubtotalLayout.setVisibility(View.GONE);
                cartProceedToCheckout.setVisibility(View.GONE);
            } else {
                if (cartListVw.getFirstVisiblePosition() == 0 && getTopOfFirstVisibleView(cartListVw) == 0) {
                    cartShippingLayout.setVisibility(View.VISIBLE);
                }
                cartSubtotalLayout.setVisibility(View.VISIBLE);
                cartProceedToCheckout.setVisibility(View.VISIBLE);
            }
        }
    }

    /** returns true if list view is scrolled to the very top */
    private int getTopOfFirstVisibleView(AbsListView listView) {
        View view = listView.getChildAt(0);
        return (view == null) ? 0 : view.getTop();
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.action_checkout:
                activity.selectOrderCheckout();
                break;
        }
    }

    private void showProgressIndicator() {
        if (activity != null) {
            activity.showProgressIndicator();
        }
    }

    private void hideProgressIndicator() {
        if (activity != null) {
            activity.hideProgressIndicator();
        }
    }




    /** returns current cart object */
    public static Cart getCart() {
        return cart;
    }

    /** returns current list of cart items */
    public static List<CartItem> getListItems() {
        return cartListItems;
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

    /** refreshes cart (fills data set with contents of cart) */
    public void refreshCart(MainActivity activity) {
        this.activity = activity;

        // only show progress indicator when refreshing the cart while cart is in view, not when refreshed from other fragments
        if (cartAdapter != null) {
            showProgressIndicator();
        }

        // query for items in cart
        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        easyOpenApi.viewCart(RECOMMENDATION, STORE_ID, LOCALE, ZIPCODE, CATALOG_ID, CLIENT_ID,
                1, 1000, viewCartListener); // 0 offset results in max of 5 items, so using 1
    }

    /** adds item to cart */
    public void addToCart(String sku, int qty, MainActivity activity) {

        this.activity = activity;

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        showProgressIndicator();

        // update quantity of item in cart
        easyOpenApi.addToCart(createCartRequestBody(sku, qty), RECOMMENDATION, STORE_ID,
                LOCALE, ZIPCODE, CATALOG_ID, CLIENT_ID, addToCartListener);
    }

    /** updates item quantity */
    private void updateItemQty(CartItem cartItem) {
        if (cartItem.isProposedQtyDifferent()) {
            if (cartItem.getProposedQty() == 0) {
                deleteItem(cartItem);
            } else {
                EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
                showProgressIndicator();

                // update quantity of item in cart
                easyOpenApi.updateCart(createCartRequestBody(cartItem, cartItem.getProposedQty()), RECOMMENDATION, STORE_ID,
                        LOCALE, ZIPCODE, CATALOG_ID, CLIENT_ID, updateCartListener);
            }
        }
    }

    /** deletes an item from the cart */
    private void deleteItem(CartItem cartItem) {
        cartItem.setProposedQty(0); // record the value we're trying to set, update the model upon success

        EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
        showProgressIndicator();

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


    private void notifyDataSetChanged() {
        // if fragment is attached to activity, then update the fragment's views
        if (cartAdapter != null) {
            cartAdapter.notifyDataSetChanged();
        }
    }

    // synchronizing this method in case cartListItems updated simultaneously (not sure this would
    // happen since this should all be on the main UI thread)
    private synchronized void setAdapterListItems() {
        // if fragment is attached to activity, then update the fragment's views
        if (cartAdapter != null) {
            cartAdapter.clear();
            if (cartListItems != null && cartListItems.size() > 0) {
                cartAdapter.addAll(cartListItems);
            }
            cartAdapter.notifyDataSetChanged();
        } else {
            if (activity != null) {
                activity.updateCartIcon(cart == null ? 0 : cart.getTotalItems());
            }
        }
    }

    private void makeToast(String msg) {
        if (activity != null) {
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
        }
    }
    private void makeToast(int msgId) {
        if (activity != null) {
            Toast.makeText(activity, msgId, Toast.LENGTH_LONG).show();
        }
    }

    // called by cart listeners below
    private void respondToFailure(String msg) {
        hideProgressIndicator();
        Log.d(TAG, msg);
        makeToast(msg);
        notifyDataSetChanged();
    }


    // Retrofit callbacks

    /************* api listeners ************/


    /** listens for completion of view request */
    class ViewCartListener implements Callback<CartContents> {

        @Override
        public void success(CartContents cartContents, Response response) {
            hideProgressIndicator();

            // clear the cart before refilling
            cart = null;
            ArrayList<CartItem> listItems = new ArrayList<CartItem>();

            // get data from cartContent request
            List<Cart> cartCollection = cartContents.getCart();
            if (cartCollection != null && cartCollection.size() > 0) {
                cart = cartCollection.get(0);
                List<Product> products = cart.getProduct();
                if (products != null) {
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
                    String leadTimeDescription = null;
                    CartItem firstInGroupCartItem = null;
                    minExpectedBusinessDays = -1;
                    maxExpectedBusinessDays = -1;
                    for (int i = 0; i < listItems.size(); i++) {
                        CartItem cartItem = listItems.get(i);
                        // if lead time different from previous item's lead time, set expected delivery info
                        if (cartItem.getLeadTimeDescription() != null  &&
                                !cartItem.getLeadTimeDescription().equals(leadTimeDescription)) {
                            leadTimeDescription = cartItem.getLeadTimeDescription();
                            cartItem.setExpectedDelivery(leadTimeDescription);
                            cartItem.setExpectedDeliveryItemQty(cartItem.getQuantity());
                            firstInGroupCartItem = cartItem;
                        } else {
                            // since lead time same as previous, add item quantity to first cart item in group
                            if (firstInGroupCartItem != null) {
                                firstInGroupCartItem.setExpectedDeliveryItemQty(
                                        firstInGroupCartItem.getExpectedDeliveryItemQty() + cartItem.getQuantity());
                            }
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
                }
            }
            cartListItems = listItems;
            setAdapterListItems();
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
            hideProgressIndicator();

            // if message, display to user (e.g. out-of-stock message)
            if (!TextUtils.isEmpty(cartUpdate.getMessage())) {
                makeToast(cartUpdate.getMessage());
            } else {
                // sometimes out-of-stock message is here instead
                String errMsg = ApiError.getApiSuccessError(cartUpdate);
                if (!TextUtils.isEmpty(errMsg)) {
                    makeToast(errMsg);
                }
            }

            // if a successful insert, refill cart
            if (cartUpdate.getItemsAdded().size() > 0) {
                refreshCart(activity);  // need updated info about the cart such as shipping and subtotals in addition to new quantities
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
            hideProgressIndicator();
            refreshCart(activity);
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
            CartItem cartItem = cartAdapter.getItem((Integer) view.getTag());

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
            CartItem cartItem = cartAdapter.getItem((Integer) view.getTag());

            cartItem.getQtyWidget().hideSoftKeyboard();

            // delete from cart via API
            cartItem.setProposedQty(0);
            updateItemQty(cartItem);

//            cartItem.getQtyWidget().setQtyValue(0);  // this will trigger selection change which will handle the rest
        }
    }

//    /** listener class for quantity update button */
//    class QtyUpdateButtonListener implements View.OnClickListener {
//
//        @Override
//        public void onClick(View view) {
//            CartItem cartItem = cartAdapter.getItem((Integer)view.getTag());
//
//            cartItem.getQtyWidget().hideSoftKeyboard();
//
//            // default proposed value to orig in case new value not parseable
//            cartItem.setProposedQty(cartItem.getQtyWidget().getQtyValue(cartItem.getQuantity()));
//
//            // update cart via API
//            updateItemQty(cartItem);
//
//            // hide button after clicking
//            view.setVisibility(View.GONE);
//        }
//    }

}

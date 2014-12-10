/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.MainApplication;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.checkout.CheckoutFragment;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.cfa.widget.QuantityEditor;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.configurator.model.Configurator;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.EmptyResponse;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.CartContents;
import com.staples.mobile.common.access.easyopen.model.cart.CartUpdate;
import com.staples.mobile.common.access.easyopen.model.cart.Coupon;
import com.staples.mobile.common.access.easyopen.model.cart.DeleteFromCart;
import com.staples.mobile.common.access.easyopen.model.cart.OrderItem;
import com.staples.mobile.common.access.easyopen.model.cart.Product;
import com.staples.mobile.common.access.easyopen.model.cart.TypedJsonString;
import com.staples.mobile.common.access.easyopen.model.member.InkRecyclingDetail;
import com.staples.mobile.common.access.easyopen.model.member.Reward;
import com.staples.mobile.common.access.easyopen.model.member.RewardDetail;
import com.staples.mobile.common.access.config.AppConfigurator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

//import com.staples.mobile.cfa.widget.QuantityEditor;

/** fragment to manage display and update of shopping cart */
public class CartFragment extends Fragment implements View.OnClickListener {

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
    private TextView couponsRewardsValue;
    private ListView couponListVw;
    private CouponAdapter couponAdapter;
    private View couponList;
    private View emptyCartMsg;
    private View cartProceedToCheckout;
    private View cartShippingLayout;
    private View cartSubtotalLayout;
    private CartAdapter cartAdapter;
    private ListView cartListVw;
    private View couponsRewardsLayout;
    private int greenBackground;
    private int blueBackground;
    private int redText;
    private int blackText;


    // cart object - make these static so they're not lost on device rotation
    private static Cart cart;
    private static List<CartItem> cartListItems;


    int minExpectedBusinessDays;
    int maxExpectedBusinessDays;


    // widget listeners
    private QtyDeleteButtonListener qtyDeleteButtonListener;
    private QtyChangeListener qtyChangeListener;
    //private QtyUpdateButtonListener qtyUpdateButtonListener;


    private DecimalFormat currencyFormat;


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

        // set up currency format to use minus sign for negative amounts (needed for coupons)
        currencyFormat = (DecimalFormat)NumberFormat.getCurrencyInstance();
        String symbol = currencyFormat.getCurrency().getSymbol();
        currencyFormat.setNegativePrefix("-"+symbol);
        currencyFormat.setNegativeSuffix("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Log.d(TAG, "onCreateView()");

        activity = (MainActivity)getActivity();

        // inflate and get child views
        View view = inflater.inflate(R.layout.cart_fragment, container, false);

        // temporary try-catch
        try {

        emptyCartMsg = view.findViewById(R.id.empty_cart_msg);
        cartFreeShippingMsg = (TextView) view.findViewById(R.id.free_shipping_msg);
        couponsRewardsLayout = view.findViewById(R.id.coupons_rewards_layout);
        couponsRewardsValue = (TextView) view.findViewById(R.id.coupons_rewards_value);
        couponList = view.findViewById(R.id.coupon_list);
        cartShipping = (TextView) view.findViewById(R.id.cart_shipping);
        cartSubtotal = (TextView) view.findViewById(R.id.cart_subtotal);
        cartShippingLayout = view.findViewById(R.id.cart_shipping_layout);
        cartSubtotalLayout = view.findViewById(R.id.cart_subtotal_layout);
        cartProceedToCheckout = view.findViewById(R.id.action_checkout);

        Resources r = getResources();
        greenBackground = r.getColor(R.color.background_green);
        blueBackground = r.getColor(R.color.background_blue);
        redText = r.getColor(R.color.text_red);
        blackText = r.getColor(R.color.text_black);

        // create widget listeners
        qtyChangeListener = new QtyChangeListener();
        qtyDeleteButtonListener = new QtyDeleteButtonListener();
//        qtyUpdateButtonListener = new QtyUpdateButtonListener();

        // Initialize coupon listview
        couponListVw = (ListView) view.findViewById(R.id.coupon_list);
        couponAdapter = new CouponAdapter(activity, this, this);
        couponListVw.setAdapter(couponAdapter);


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
                    couponsRewardsLayout.setVisibility(View.VISIBLE);
                }
            }
            private void onScrollDown() {
                if (cartShippingLayout.getVisibility() != View.GONE) {
                    cartShippingLayout.setVisibility(View.GONE); // hide math story
                    couponsRewardsLayout.setVisibility(View.GONE);
                }
            }
        });

        // Set click listeners
        cartProceedToCheckout.setOnClickListener(this);
        couponsRewardsLayout.setOnClickListener(this);

        // temporary
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }


    @Override
    public void onResume() {
        // temporary try-catch
        try {

        super.onResume();

        // update action bar
        activity.showCartActionBarEntities();
        activity.showActionBar(R.string.cart_title, 0, null);

        //initialize cart based on what's been returned from api so far
        setAdapterListItems();

            // temporary
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        cartAdapter = null;
    }


    /** returns sum of adjusted amounts for rewards and coupons applied to cart */
    public float getCouponsRewardsAdjustedAmount() {
        float totalAdjustedAmount = 0;
        // coupons & rewards
        if (cart != null && cart.getCoupon() != null) {
            for (Coupon c : cart.getCoupon()) {
                totalAdjustedAmount += c.getAdjustedAmount();
            }
        }
        return totalAdjustedAmount;
    }

    /** extracts list of rewards from profile */
    private List<Reward> createProfileRewardList() {
        List<Reward> profileRewards = new ArrayList<Reward>();
        if (ProfileDetails.getMember() != null) {
            if (ProfileDetails.getMember().getInkRecyclingDetails() != null) {
                for (InkRecyclingDetail detail : ProfileDetails.getMember().getInkRecyclingDetails()) {
                    if (detail.getReward() != null) {
                        for (Reward reward : detail.getReward()) {
                            profileRewards.add(reward);
                        }
                    }
                }
            }
            if (ProfileDetails.getMember().getRewardDetails() != null) {
                for (RewardDetail detail : ProfileDetails.getMember().getRewardDetails()) {
                    if (detail.getReward() != null) {
                        for (Reward reward : detail.getReward()) {
                            profileRewards.add(reward);
                        }
                    }
                }
            }
        }
        return profileRewards;
    }

    /** returns reward within list that matches specified code, if any */
    private Reward findMatchingReward(List<Reward> rewards, String code) {
        if (rewards != null) {
            for (Reward reward : rewards) {
                if (reward.getCode().equals(code)) {
                    return reward;
                }
            }
        }
        return null;
    }

    /** Sets item count indicator on cart icon and cart drawer title */
    private void updateCartFields() {
        // temporary try-catch
        try {

        Resources r = getResources();

        int totalItemCount = 0;
        String shipping = "";
        float couponsRewardsAmount = 0;
        float subtotal = 0;
        float preTaxSubtotal = 0;
        float freeShippingThreshold = 0;
        if (cart != null) {
            totalItemCount = cart.getTotalItems();
            couponsRewardsAmount = getCouponsRewardsAdjustedAmount();
            shipping = cart.getDelivery();
            subtotal = cart.getSubTotal();
            preTaxSubtotal = cart.getPreTaxTotal();
            AppConfigurator appConfigurator = new AppConfigurator(MainApplication.application);
            Configurator configurator = appConfigurator.getConfigurator();
            if (configurator != null) {
                freeShippingThreshold = configurator.getAppContext().getPromotions().getFreeShippingThreshold().floatValue();
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
                    cartFreeShippingMsg.setBackgroundColor(blueBackground);
                } else {
                    // qualifies for free shipping
                    String freeShippingMsg = r.getString(R.string.free_shipping_msg2);
                    if (!freeShippingMsg.equals(cartFreeShippingMsg.getText().toString())) {
                        cartFreeShippingMsg.setVisibility(View.VISIBLE);
                        cartFreeShippingMsg.setText(freeShippingMsg);
                        cartFreeShippingMsg.setBackgroundColor(greenBackground);
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

            // set text of coupons, shipping, and subtotal
            couponsRewardsValue.setText(currencyFormat.format(couponsRewardsAmount));
            cartShipping.setText(CheckoutFragment.formatShippingCharge(shipping, currencyFormat));
            cartShipping.setTextColor("Free".equals(shipping) ? redText : blackText);
            cartSubtotal.setText(currencyFormat.format(preTaxSubtotal));

            // update coupon list
            List<CouponItem> couponItems = new ArrayList<CouponItem>();
            List<Reward> profileRewards = createProfileRewardList();
            // add line to add a coupon
            couponItems.add(new CouponItem(null, null, CouponItem.TYPE_COUPON_TO_ADD));
            // add list of applied coupons
            if (cart.getCoupon() != null && cart.getCoupon().size() > 0) {
                for (Coupon coupon : cart.getCoupon()) {
                    // coupon may or may not have a matching reward
                    Reward reward = findMatchingReward(profileRewards, coupon.getCode());
                    if (reward != null) {
                        profileRewards.remove(reward); // remove the applied rewards from the list
                    }
                    couponItems.add(new CouponItem(coupon, reward, CouponItem.TYPE_APPLIED_COUPON));
                }
            }
            // if any unapplied redeemable rewards
            if (profileRewards.size() > 0) {
                // add redeemable rewards heading
                couponItems.add(new CouponItem(null, null, CouponItem.TYPE_REDEEMABLE_REWARD_HEADING));
                // add redeemable rewards
                for (Reward reward : profileRewards) {
                    couponItems.add(new CouponItem(null, reward, CouponItem.TYPE_REDEEMABLE_REWARD));
                }
            }
            couponAdapter.setItems(couponItems);

            // only show shipping, subtotal, and proceed-to-checkout when at least one item
            if (totalItemCount == 0) {
                couponsRewardsLayout.setVisibility(View.GONE);
                cartShippingLayout.setVisibility(View.GONE);
                cartSubtotalLayout.setVisibility(View.GONE);
                cartProceedToCheckout.setVisibility(View.GONE);
            } else {
                if (cartListVw.getFirstVisiblePosition() == 0 && getTopOfFirstVisibleView(cartListVw) == 0) {
                    couponsRewardsLayout.setVisibility(View.VISIBLE);
                    cartShippingLayout.setVisibility(View.VISIBLE);
                }
                cartSubtotalLayout.setVisibility(View.VISIBLE);
                cartProceedToCheckout.setVisibility(View.VISIBLE);
            }
        }


        // temporary
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** returns true if list view is scrolled to the very top */
    private int getTopOfFirstVisibleView(AbsListView listView) {
        View view = listView.getChildAt(0);
        return (view == null) ? 0 : view.getTop();
    }


    @Override
    public void onClick(View view) {
        hideSoftKeyboard(view);
        switch(view.getId()) {
            case R.id.coupons_rewards_layout:
                if (couponList.getVisibility() != View.VISIBLE) {
                    cartListVw.setVisibility(View.GONE);
                    couponList.setVisibility(View.VISIBLE);
                } else {
                    cartListVw.setVisibility(View.VISIBLE);
                    couponList.setVisibility(View.GONE);
                }
                break;
            case R.id.coupon_add_button:
                addCoupon(couponAdapter.getItem((Integer) view.getTag()).getCouponCodeToAdd());
                break;
            case R.id.reward_add_button:
                addCoupon(couponAdapter.getItem((Integer) view.getTag()).getReward().getCode());
                break;
            case R.id.coupon_delete_button:
                deleteCoupon(couponAdapter.getItem((Integer) view.getTag()).getCoupon().getCode());
                break;
            case R.id.action_checkout:
                activity.selectOrderCheckout();
                break;
        }
    }

    private void addCoupon(String couponCode) {
        if (!TextUtils.isEmpty(couponCode)) {
            EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
            showProgressIndicator();
            Coupon coupon = new Coupon();
            coupon.setPromoName(couponCode);
            easyOpenApi.addCoupon(coupon, RECOMMENDATION, STORE_ID, LOCALE, ZIPCODE, CLIENT_ID,
                    new Callback<EmptyResponse>() {
                        @Override
                        public void success(EmptyResponse emptyResponse, Response response) {
                            refreshCart(activity);  // need updated info about the cart such as shipping and subtotals in addition to new quantities
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            hideProgressIndicator();
                            makeToast(ApiError.getErrorMessage(error));
                        }
                    });
        }
    }

    private void deleteCoupon(String couponCode) {
        if (!TextUtils.isEmpty(couponCode)) {
            EasyOpenApi easyOpenApi = Access.getInstance().getEasyOpenApi(false);
            showProgressIndicator();
            Coupon coupon = new Coupon();
            coupon.setPromoName(couponCode);
            easyOpenApi.deleteCoupon(RECOMMENDATION, STORE_ID, couponCode, LOCALE, CLIENT_ID,
                    new Callback<EmptyResponse>() {
                        @Override
                        public void success(EmptyResponse emptyResponse, Response response) {
                            refreshCart(activity);  // need updated info about the cart such as shipping and subtotals in addition to new quantities
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            hideProgressIndicator();
                            makeToast(ApiError.getErrorMessage(error));
                        }
                    });
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


//    /** listener class for qty change */
//    class QtyChangeListener implements QuantityEditor.OnQtyChangeListener {
//        @Override
//        public void onQtyChange(View view, boolean validSpinnerValue) {
//            CartItem cartItem = cartAdapter.getItem((Integer) view.getTag());
//
//            // default proposed qty to orig in case new value not parseable;
//            cartItem.setProposedQty(cartItem.getQtyWidget().getQtyValue(cartItem.getQuantity()));
//
//            // if valid spinner value, then automatically update the cart
////            if (validSpinnerValue) {
////                updateItemQty(cartItem);
////            } else { // otherwise notify data set changed to make update button appear or disappear
////                notifyDataSetChanged();
////            }
//            updateItemQty(cartItem);
//        }
//    }

    /** listener class for qty change */
    class QtyChangeListener implements QuantityEditor.OnQtyChangeListener {
        @Override
        public void onQtyChange(View view, int value) {
            CartItem cartItem = cartAdapter.getItem((Integer) view.getTag());

            // default proposed qty to orig in case new value not parseable;
            cartItem.setProposedQty(value);

            updateItemQty(cartItem);
        }
    }

    /** listener class for item deletion button */
    class QtyDeleteButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            CartItem cartItem = cartAdapter.getItem((Integer) view.getTag());

//            cartItem.getQtyWidget().hideSoftKeyboard();
            hideSoftKeyboard(view);


            // delete from cart via API
            cartItem.setProposedQty(0);
            updateItemQty(cartItem);

//            cartItem.getQtyWidget().setQtyValue(0);  // this will trigger selection change which will handle the rest
        }
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager keyboard = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

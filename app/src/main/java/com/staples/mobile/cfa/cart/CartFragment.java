/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.checkout.CheckoutFragment;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.cfa.rewards.RewardsLinkingFragment;
import com.staples.mobile.cfa.widget.QuantityEditor;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.configurator.model.Configurator;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.Coupon;
import com.staples.mobile.common.access.easyopen.model.cart.Product;
import com.staples.mobile.common.access.easyopen.model.member.Reward;
import com.staples.mobile.common.access.config.AppConfigurator;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/** fragment to manage display and update of shopping cart */
public class CartFragment extends Fragment implements View.OnClickListener, CartApiManager.CartRefreshCallback {

    private static final String TAG = CartFragment.class.getSimpleName();


    // saving around Activity object since getActivity() returns null after user navigates away from
    // fragment, but api call may still be returning
    private MainActivity activity;


    private TextView cartSubtotal;
    private TextView cartFreeShippingMsg;
    private TextView cartShipping;
    private TextView couponsRewardsValue;
    private RecyclerView couponListVw;
    private CouponAdapter couponAdapter;
    private View couponList;
    private View emptyCartMsg;
    private View cartProceedToCheckout;
    private View cartShippingLayout;
    private View cartSubtotalLayout;
    private CartAdapter cartAdapter;
    private RecyclerView cartListVw;
    private LinearLayoutManager cartListLayoutMgr;
    private View couponsRewardsLayout;
    private View linkRewardsAcctLayout;
    private Button rewardsLinkAcctButton;
    private int greenBackground;
    private int blueBackground;
    private int redText;
    private int blackText;


    // cart object - make these static so they're not lost on device rotation
    private static List<CartItem> cartListItems;
    private static List<CartItemGroup> cartItemGroups;


//    int minExpectedBusinessDays;
//    int maxExpectedBusinessDays;


    // widget listeners
    private QtyDeleteButtonListener qtyDeleteButtonListener;
    private QtyChangeListener qtyChangeListener;
    //private QtyUpdateButtonListener qtyUpdateButtonListener;


    private DecimalFormat currencyFormat;


    /** default constructor - note that fragment instance will be retained whereas view will come and go as attached to activity */
    public CartFragment() {
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
        linkRewardsAcctLayout = view.findViewById(R.id.link_rewards_acct_layout);
        cartShipping = (TextView) view.findViewById(R.id.cart_shipping);
        cartSubtotal = (TextView) view.findViewById(R.id.cart_subtotal);
        cartShippingLayout = view.findViewById(R.id.cart_shipping_layout);
        cartSubtotalLayout = view.findViewById(R.id.cart_subtotal_layout);
        cartProceedToCheckout = view.findViewById(R.id.action_checkout);
        rewardsLinkAcctButton = (Button)view.findViewById(R.id.rewards_link_acct_button);


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
        couponListVw = (RecyclerView) view.findViewById(R.id.coupon_list);
        couponAdapter = new CouponAdapter(this, this);
        couponListVw.setAdapter(couponAdapter);
        couponListVw.setLayoutManager(new LinearLayoutManager(activity));


        // Initialize cart listview
        cartAdapter = new CartAdapter(activity, R.layout.cart_item_group, qtyChangeListener, qtyDeleteButtonListener);
        cartAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateCartFields();
            }
        });
        cartListVw = (RecyclerView) view.findViewById(R.id.cart_list);
        cartListLayoutMgr = new LinearLayoutManager(activity);
        cartListVw.setLayoutManager(cartListLayoutMgr);
        cartListVw.setAdapter(cartAdapter);
        cartListVw.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 5) {
                    onScrollDown();
                } else if (dy < -5 || (dy < 0 && isTopOfFirstItemVisible(recyclerView))) {
                    onScrollUp();
                }
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
        rewardsLinkAcctButton.setOnClickListener(this);

        // temporary
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }

        return view;
    }


    @Override
    public void onResume() {
        // temporary try-catch
        try {

        super.onResume();

        // update action bar
        activity.showActionBar(R.string.cart_title, 0, null);

        //initialize cart based on what's been returned from api so far
        convertCart(CartApiManager.getCart());

            // temporary
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
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
        Cart cart = CartApiManager.getCart();
        // coupons & rewards
        if (cart != null && cart.getCoupon() != null) {
            for (Coupon c : cart.getCoupon()) {
                totalAdjustedAmount += c.getAdjustedAmount();
            }
        }
        return totalAdjustedAmount;
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
        Cart cart = CartApiManager.getCart();
        if (cart != null) {
            totalItemCount = cart.getTotalItems();
            couponsRewardsAmount = getCouponsRewardsAdjustedAmount();
            shipping = cart.getDelivery();
            subtotal = cart.getSubTotal();
            preTaxSubtotal = cart.getPreTaxTotal();
            AppConfigurator appConfigurator = AppConfigurator.getInstance();
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
            List<Reward> profileRewards = ProfileDetails.getAllProfileRewards();
            // add line to add a coupon
            couponItems.add(new CouponItem(null, null, CouponItem.TYPE_COUPON_TO_ADD));
            // add list of applied coupons
            if (cart != null && cart.getCoupon() != null && cart.getCoupon().size() > 0) {
                for (Coupon coupon : cart.getCoupon()) {
                    // coupon may or may not have a matching reward
                    Reward reward = ProfileDetails.findMatchingReward(profileRewards, coupon.getCode());
                    if (reward != null) {
                        profileRewards.remove(reward); // remove the applied rewards from the list
                    }
                    couponItems.add(new CouponItem(coupon, reward, CouponItem.TYPE_APPLIED_COUPON));
                }
            }

            // if profile exists (registered user logged in and no errors getting profile)
            if (ProfileDetails.getMember() != null) {

                // if rewards member
                if (ProfileDetails.isRewardsMember()) {

                    // add redeemable rewards heading
                    couponItems.add(new CouponItem(null, null, CouponItem.TYPE_REDEEMABLE_REWARD_HEADING));

                    // if any unapplied redeemable rewards
                    if (profileRewards.size() > 0) {
                        // add redeemable rewards
                        for (Reward reward : profileRewards) {
                            couponItems.add(new CouponItem(null, reward, CouponItem.TYPE_REDEEMABLE_REWARD));
                        }
                    } else {
                        couponItems.add(new CouponItem(null, null, CouponItem.TYPE_NO_REDEEMABLE_REWARDS_MSG));
                    }
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

                if (isTopOfFirstItemVisible(cartListVw)) {
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
            Log.d(TAG, e.getMessage());
        }
    }

    /** returns true if list view is scrolled to the very top */
    private boolean isTopOfFirstItemVisible(RecyclerView listView) {
        if (cartListLayoutMgr.findFirstVisibleItemPosition() == 0) {
//            View view = listView.getChildAt(0); // might not get first child
            View view = cartListLayoutMgr.findViewByPosition(0); // lesser performance
            return view != null && view.getTop() >= 0;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        hideSoftKeyboard(view);
        switch(view.getId()) {
            case R.id.coupons_rewards_layout:
                if (couponList.getVisibility() != View.VISIBLE) {
                    cartListVw.setVisibility(View.GONE);
                    couponList.setVisibility(View.VISIBLE);
                    // if logged in and not a rewards member, display link rewards layout
                    if (Access.getInstance().isLoggedIn() && !Access.getInstance().isGuestLogin() &&
                            ProfileDetails.getMember() != null && !ProfileDetails.isRewardsMember()) {
                        linkRewardsAcctLayout.setVisibility(View.VISIBLE);
                    }
                } else {
                    cartListVw.setVisibility(View.VISIBLE);
                    couponList.setVisibility(View.GONE);
                    linkRewardsAcctLayout.setVisibility(View.GONE);
                }
                break;
            case R.id.coupon_add_button:
                showProgressIndicator();
                CartApiManager.addCoupon(couponAdapter.getItem((Integer) view.getTag()).getCouponCodeToAdd(), this);
                break;
            case R.id.reward_add_button:
                showProgressIndicator();
                CartApiManager.addCoupon(couponAdapter.getItem((Integer) view.getTag()).getReward().getCode(), this);
                break;
            case R.id.coupon_delete_button:
                showProgressIndicator();
                CartApiManager.deleteCoupon(couponAdapter.getItem((Integer) view.getTag()).getCoupon().getCode(), this);
                break;
            case R.id.action_checkout:
                activity.selectOrderCheckout();
                break;
            case R.id.rewards_link_acct_button:
                String rewardsNumber = ((EditText)getView().findViewById(R.id.rewards_card_number)).getText().toString();
                String phoneNumber = ((EditText)getView().findViewById(R.id.rewards_phone_number)).getText().toString();
                showProgressIndicator();
                RewardsLinkingFragment.linkRewardsAccount(rewardsNumber, phoneNumber, new RewardsLinkingFragment.LinkRewardsCallback() {
                    @Override
                    public void onLinkRewardsComplete(String errMsg) {
                        hideProgressIndicator();
                        if (errMsg != null) {
                            makeToast(errMsg);
                        } else {
                            linkRewardsAcctLayout.setVisibility(View.GONE);
                            updateCartFields();
                        }
                    }
                });
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

    /** returns current list of cart items */
    public static List<CartItem> getListItems() {
        return cartListItems;
    }



//    public int getMinExpectedBusinessDays() {
//        return minExpectedBusinessDays;
//    }
//
//    public int getMaxExpectedBusinessDays() {
//        return maxExpectedBusinessDays;
//    }
//
//    public String getExpectedDeliveryRange() {
//        if (maxExpectedBusinessDays > minExpectedBusinessDays) {
//            return minExpectedBusinessDays + " - " + maxExpectedBusinessDays;
//        } else {
//            return ""+minExpectedBusinessDays;
//        }
//    }



    /** updates item quantity */
    private void updateItemQty(CartItem cartItem) {
        if (cartItem.isProposedQtyDifferent()) {
            showProgressIndicator();
            if (cartItem.getProposedQty() == 0) {
                CartApiManager.deleteItem(cartItem.getOrderItemId(), this);
            } else {
                CartApiManager.updateItemQty(cartItem.getOrderItemId(), cartItem.getSku(), cartItem.getProposedQty(), this);
            }
        }
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
            cartAdapter.setItems(cartItemGroups);
        } else {
            if (activity != null) {
                activity.updateCartIcon(CartApiManager.getCartTotalItems());
            }
        }
    }

    private void makeToast(String msg) {
        if (activity != null && msg != null) {
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
        }
    }



    public void onCartRefreshComplete(String errMsg) {
        hideProgressIndicator();
        if (errMsg != null) {
            makeToast(errMsg);
        }
        convertCart(CartApiManager.getCart());
    }



    private void convertCart(Cart cart) {

        // clear the cart before refilling
        ArrayList<CartItem> cartItems = new ArrayList<CartItem>();
        ArrayList<CartItemGroup> itemGroups = new ArrayList<CartItemGroup>();

        if (cart != null) {

            // rather than call the api to refresh the profile, use the info from the cart to update coupon info in the profile
            ProfileDetails.updateRewardsFromCart(cart);


            List<Product> products = cart.getProduct();
            if (products != null) {

                // iterate thru products to create list of cart items
                for (Product product : products) {
                    cartItems.add(new CartItem(product));
                }

                // sort by expected delivery date
                Collections.sort(cartItems, new Comparator<CartItem>() {
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
                CartItemGroup itemGroup = null;
//                    minExpectedBusinessDays = -1;
//                    maxExpectedBusinessDays = -1;
                for (int i = 0; i < cartItems.size(); i++) {
                    CartItem cartItem = cartItems.get(i);
                    // if lead time different from previous item's lead time, set expected delivery info
                    if (!cartItem.getLeadTimeDescription().equals(leadTimeDescription)) {
                        itemGroup = new CartItemGroup();
                        itemGroups.add(itemGroup);
                        itemGroup.setExpectedDelivery(cartItem.getLeadTimeDescription());
                        itemGroup.setExpectedDeliveryItemQty(cartItem.getQuantity());
                        leadTimeDescription = cartItem.getLeadTimeDescription();
                    } else {
                        // since lead time same as previous, add item quantity to group
                        itemGroup.setExpectedDeliveryItemQty(itemGroup.getExpectedDeliveryItemQty() + cartItem.getQuantity());
                    }
                    itemGroup.addItem(cartItem);

//                        if (minExpectedBusinessDays == -1 ||
//                                cartItem.getMinExpectedBusinessDays() < minExpectedBusinessDays) {
//                            minExpectedBusinessDays = cartItem.getMinExpectedBusinessDays();
//                        }
//                        if (maxExpectedBusinessDays == -1 ||
//                                cartItem.getMaxExpectedBusinessDays() > maxExpectedBusinessDays) {
//                            maxExpectedBusinessDays = cartItem.getMaxExpectedBusinessDays();
//                        }
                }
            }
        }
        cartListItems = cartItems;
        cartItemGroups = itemGroups;
        setAdapterListItems();
    }

    /************* widget listeners ************/


    /** listener class for qty change */
    class QtyChangeListener implements QuantityEditor.OnQtyChangeListener {
        @Override
        public void onQtyChange(View view, int value) {
            CartAdapter.CartItemPosition position = (CartAdapter.CartItemPosition)view.getTag();
            CartItemGroup cartItemGroup = cartAdapter.getItem(position.groupPosition);
            CartItem cartItem = cartItemGroup.getCartItems().get(position.itemPositionWithinGroup);

            // default proposed qty to orig in case new value not parseable;
            cartItem.setProposedQty(value);

            updateItemQty(cartItem);
        }
    }

    /** listener class for item deletion button */
    class QtyDeleteButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            CartAdapter.CartItemPosition position = (CartAdapter.CartItemPosition)view.getTag();
            CartItemGroup cartItemGroup = cartAdapter.getItem(position.groupPosition);
            CartItem cartItem = cartItemGroup.getCartItems().get(position.itemPositionWithinGroup);

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

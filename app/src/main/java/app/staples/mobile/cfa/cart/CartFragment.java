package app.staples.mobile.cfa.cart;

import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;
import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.Coupon;
import com.staples.mobile.common.access.easyopen.model.cart.Product;
import com.staples.mobile.common.access.easyopen.model.member.Reward;
import com.staples.mobile.common.analytics.Tracker;
import com.staples.mobile.configurator.AppConfigurator;
import com.staples.mobile.configurator.model.AppContext;
import com.staples.mobile.configurator.model.Configurator;
import com.staples.mobile.configurator.model.Promotions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.apptentive.ApptentiveSdk;
import app.staples.mobile.cfa.checkout.CheckoutFragment;
import app.staples.mobile.cfa.profile.ProfileDetails;
import app.staples.mobile.cfa.rewards.RewardsLinkingFragment;
import app.staples.mobile.cfa.util.MiscUtils;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.QuantityEditor;

/** fragment to manage display and update of shopping cart */
public class CartFragment extends Fragment implements View.OnClickListener, QuantityEditor.OnQtyChangeListener, CartApiManager.CartRefreshCallback {
    private static final String TAG = CartFragment.class.getSimpleName();

    // saving around Activity object since getActivity() returns null after user navigates away from
    // fragment, but api call may still be returning
    private MainActivity activity;

    private TextView cartSubtotal;
//    private View freeShippingLayout;
    private TextView freeShippingMsg;
    private TextView heavyweightShipping;
    private TextView heavyweightShippingLabel;
    private TextView cartShipping;
    private TextView couponsRewardsLabel;
    private TextView couponsRewardsValue;
    private RecyclerView couponListVw;
    private CouponAdapter couponAdapter;
    private View emptyCartLayout;
    private View cartShippingLayout;
    private View cartActionLayout;
    private CartAdapter cartAdapter;
    private RecyclerView cartListVw;
    private View couponsRewardsLayout;
    private int greenText;
    private int blackText;
    private int blueText;
    private View actionCheckout;
    private boolean couponsExpanded;

    // cart object - make these static so they're not lost on device rotation
    private static List<CartItem> cartListItems;
    private static List<CartItemGroup> cartItemGroups;

    private float couponsRewardsAmount;

    CouponAnimator couponExpandAnimator;
    CouponAnimator couponCollapseAnimator;
//    Animation mathStoryFadeInAnimation;
//    Animation mathStoryFadeOutAnimation;
//    FadeInOutListener mathStoryFadeInAnimationListener;
//    FadeInOutListener mathStoryFadeOutAnimationListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        Crittercism.leaveBreadcrumb("CartFragment:onCreateView(): Displaying the cart.");
        activity = (MainActivity) getActivity();

        // inflate and get child views
        View view = inflater.inflate(R.layout.cart_fragment, container, false);

        emptyCartLayout = view.findViewById(R.id.empty_cart_layout);
//        freeShippingLayout = view.findViewById(R.id.free_shipping_layout);
        freeShippingMsg = (TextView) view.findViewById(R.id.free_shipping_msg);
        couponsRewardsLayout = view.findViewById(R.id.coupons_rewards_layout);
        couponsRewardsLabel = (TextView) view.findViewById(R.id.coupons_rewards_label);
        couponsRewardsValue = (TextView) view.findViewById(R.id.coupons_rewards_value);
        heavyweightShipping = (TextView) view.findViewById(R.id.heavyweight_shipping);
        heavyweightShippingLabel = (TextView) view.findViewById(R.id.heavyweight_shipping_label);
        cartShipping = (TextView) view.findViewById(R.id.cart_shipping);
        cartSubtotal = (TextView) view.findViewById(R.id.cart_subtotal);
        cartShippingLayout = view.findViewById(R.id.cart_shipping_layout);
        cartActionLayout = view.findViewById(R.id.cart_action_layout);
        actionCheckout = view.findViewById(R.id.action_checkout);

        Resources r = getResources();
        greenText = r.getColor(R.color.staples_green);
        blackText = r.getColor(R.color.staples_black);
        blueText = r.getColor(R.color.staples_blue);

        // Initialize coupon listview
        couponListVw = (RecyclerView) view.findViewById(R.id.coupon_list);
        couponAdapter = new CouponAdapter(this);
        couponListVw.setAdapter(couponAdapter);
        couponListVw.setLayoutManager(new LinearLayoutManager(activity));
        couponsExpanded = false;

        // set up coupons & rewards panel animation
        couponExpandAnimator = new CouponAnimator(true);
        couponCollapseAnimator = new CouponAnimator(false);

//        // set up fade in/out animations
//        mathStoryFadeInAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
//        mathStoryFadeOutAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_out);
//        mathStoryFadeInAnimationListener = new FadeInOutListener(true);
//        mathStoryFadeOutAnimationListener = new FadeInOutListener(false);
//        mathStoryFadeInAnimation.setAnimationListener(mathStoryFadeInAnimationListener);
//        mathStoryFadeOutAnimation.setAnimationListener(mathStoryFadeOutAnimationListener);

        // Initialize cart listview
        cartAdapter = new CartAdapter(activity, this, this);
        cartAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateCartFields();
            }
        });
        cartListVw = (RecyclerView) view.findViewById(R.id.cart_list);
        cartListVw.setLayoutManager(new LinearLayoutManager(activity));
        cartListVw.setAdapter(cartAdapter);
//        cartListVw.setOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (dy > 5 && !isTopOfFirstItemVisible(recyclerView)) {
//                    onScrollDown();
//                } else if (dy < -5 || (dy < 0 && isTopOfFirstItemVisible(recyclerView))) {
//                    onScrollUp();
//                }
//            }
//
//            private void onScrollUp() {
//                if (couponsRewardsLayout.getVisibility() != View.VISIBLE && !mathStoryFadeInAnimationListener.isInProcess()) {
//                    couponsRewardsLayout.startAnimation(mathStoryFadeInAnimation); // fade in
//                }
//            }
//            private void onScrollDown() {
//                if (couponsRewardsLayout.getVisibility() == View.VISIBLE && !mathStoryFadeOutAnimationListener.isInProcess()) {
//                    couponsRewardsLayout.startAnimation(mathStoryFadeOutAnimation); // fade out
//                }
//            }
//        });

        // Set click listeners
        view.findViewById(R.id.action_checkout).setOnClickListener(this);
        view.findViewById(R.id.action_android_pay).setOnClickListener(this);
        couponsRewardsLayout.setOnClickListener(this);
//        rewardsLinkAcctButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // update action bar
        ActionBar.getInstance().setConfig(ActionBar.Config.CART);
        //initialize cart based on what's been returned from api so far
        convertCart(CartApiManager.getCart());
        Tracker.getInstance().trackStateForCart(CartApiManager.getCart()); // analytics
        Apptentive.engage(activity, ApptentiveSdk.CART_SHOWN_EVENT);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /** Sets item count indicator on cart icon and cart drawer title */
    private void updateCartFields() {
        Resources r = getResources();

        int totalItemCount = 0;
        String shipping = "";
        float subtotal = 0;
        float preTaxSubtotal = 0;
        float freeShippingThreshold = 0;
        float totalHandlingCost = 0;
        Cart cart = CartApiManager.getCart();
        if (cart != null) {
            totalItemCount = cart.getTotalItems();
            totalHandlingCost = cart.getTotalHandlingCost();
            shipping = cart.getDelivery();
            subtotal = cart.getSubTotal();
            preTaxSubtotal = cart.getPreTaxTotal();
            
            AppConfigurator appConfigurator = AppConfigurator.getInstance();
            Configurator configurator = appConfigurator.getConfigurator();
            if (configurator != null) {
                AppContext appContext = configurator.getAppContext();
                if (appContext!=null) {
                    Promotions promos = appContext.getPromotions();
                    if (promos!=null) {
                        freeShippingThreshold = promos.getFreeShippingThreshold();
                    }
                }
            }
        }

        // set text of cart icon badge
        ActionBar.getInstance().setCartCount(totalItemCount);

        // if fragment is attached to activity, then update the fragment's views
        if (getActivity() != null) {

            emptyCartLayout.setVisibility(totalItemCount == 0? View.VISIBLE : View.GONE);

            DecimalFormat currencyFormat = MiscUtils.getCurrencyFormat();

            // set text of free shipping msg
            if (totalItemCount > 0) {
                if(cart.getAmountToReachToCheckoutAddOnItems() > 0.0) {
                    // minimum $25 total
                    String freeShippingMsg = String.format(r.getString(R.string.minimum_shipping_msg),
                            currencyFormat.format(cart.getAmountToReachToCheckoutAddOnItems()));
                    this.freeShippingMsg.setVisibility(View.VISIBLE);
                    this.freeShippingMsg.setText(freeShippingMsg);
                    this.freeShippingMsg.setTextColor(blueText);
                    actionCheckout.setEnabled(false);
                }
                else if (freeShippingThreshold > subtotal && !"Free".equals(shipping) && !ProfileDetails.isRewardsMember()) {
                    // need to spend more to qualify for free shipping
                    freeShippingMsg.setVisibility(View.VISIBLE);
                    freeShippingMsg.setText(String.format(r.getString(R.string.free_shipping_msg1),
                            currencyFormat.format(freeShippingThreshold), currencyFormat.format(freeShippingThreshold - subtotal)));
                    this.freeShippingMsg.setTextColor(greenText);
                    actionCheckout.setEnabled(true);
                } else {
                    // qualifies for free shipping
                    String freeShippingMsg = r.getString(R.string.free_shipping_msg2);
                    if (!freeShippingMsg.equals(this.freeShippingMsg.getText().toString())) {
                        this.freeShippingMsg.setVisibility(View.VISIBLE);
                        this.freeShippingMsg.setText(freeShippingMsg);
                        this.freeShippingMsg.setTextColor(greenText);
                        actionCheckout.setEnabled(true);
                    }
                }
            } else {
                freeShippingMsg.setVisibility(View.GONE);
            }

            // set text of shipping, and subtotal
            if (totalHandlingCost > 0) {
                String totalHandlingCostStr = Float.toString(totalHandlingCost);
                heavyweightShipping.setText(CheckoutFragment.formatShippingCharge(totalHandlingCostStr, currencyFormat));
                heavyweightShipping.setTextColor(blackText);
                heavyweightShippingLabel.setVisibility(View.VISIBLE);
                heavyweightShipping.setVisibility(View.VISIBLE);

                // additional shipping fee
                String freeShippingMsg = String.format(r.getString(R.string.oversized_shipping_msg),
                        currencyFormat.format(totalHandlingCost));
                this.freeShippingMsg.setVisibility(View.VISIBLE);
                this.freeShippingMsg.setText(freeShippingMsg);
                this.freeShippingMsg.setTextColor(blueText);
            } else {
                heavyweightShippingLabel.setVisibility(View.GONE);
                heavyweightShipping.setVisibility(View.GONE);
            }

            cartShipping.setText(CheckoutFragment.formatShippingCharge(shipping, currencyFormat));
            cartShipping.setTextColor("Free".equals(shipping) ? greenText : blackText);
            cartSubtotal.setText(currencyFormat.format(preTaxSubtotal));

            // only show shipping, subtotal, and proceed-to-checkout when at least one item
            if (totalItemCount == 0) {
                couponsRewardsLayout.setVisibility(View.GONE);
                cartShippingLayout.setVisibility(View.GONE);
                cartActionLayout.setVisibility(View.GONE);
            } else {
//                if (isTopOfFirstItemVisible(cartListVw)) {
                couponsRewardsLayout.setVisibility(View.VISIBLE);
                cartShippingLayout.setVisibility(View.VISIBLE);
//                }
                cartActionLayout.setVisibility(View.VISIBLE);
            }
        }
    }

//    /** returns true if list view is scrolled to the very top */
//    private boolean isTopOfFirstItemVisible(RecyclerView listView) {
//        if (cartListLayoutMgr.findFirstVisibleItemPosition() == 0) {
////            View view = listView.getChildAt(0); // might not get first child
//            View view = cartListLayoutMgr.findViewByPosition(0); // lesser performance
//            return view != null && view.getTop() >= -200; // giving it some margin since i've seen as low as -110 when top still visible after a scroll (e.g. when just enough content to allow scrolling)
//        }
//        return false;
//    }

    private boolean validateFields(String rewardsNumber, String phoneNumber) {
        if(TextUtils.isEmpty(rewardsNumber) || TextUtils.isEmpty(phoneNumber)) {
            return false;
        }
        return true;
    }

    private String stripPhoneNumber(String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {
            return phoneNumber.replaceAll("[^0-9]", "");
        }
        return phoneNumber;
    }

    /** returns current list of cart items */
    public static List<CartItem> getListItems() {
        return cartListItems;
    }

    /** updates item quantity */
    private void updateItemQty(final CartItem cartItem) {
        if (cartItem.isProposedQtyDifferent()) {
            activity.showProgressIndicator();
            if (cartItem.getProposedQty() == 0) {
                CartApiManager.deleteItem(cartItem.getOrderItemId(), new CartApiManager.CartRefreshCallback() {
                    @Override public void onCartRefreshComplete(String errMsg) {
                        Tracker.getInstance().trackActionForRemoveFromCart(cartItem.getSku());
                        CartFragment.this.onCartRefreshComplete(errMsg);
//                        updateCartFields();
                    }
                });
            } else {
                Tracker.getInstance().trackActionForUpdateQtyFromCart(cartItem.getSku());
                CartApiManager.updateItemQty(cartItem.getOrderItemId(), cartItem.getSku(), cartItem.getProposedQty(), this);
//                updateCartFields();
            }
        }
    }

    // synchronizing this method in case cartListItems updated simultaneously (not sure this would
    // happen since this should all be on the main UI thread)
    private synchronized void setAdapterListItems() {
        // if fragment is attached to activity, then update the fragment's views
        if (getActivity() != null) {
            cartAdapter.setItems(cartItemGroups);
        } else {
            ActionBar.getInstance().setCartCount(CartApiManager.getCartTotalItems());
        }
    }

    public void onCartRefreshComplete(String errMsg) {
        // if fragment is attached to activity
        if (getActivity() != null) {
            activity.hideProgressIndicator();
            if (errMsg != null) {
                // if non-grammatical out-of-stock message from api, provide a nicer message
                if (errMsg.contains("items is out of stock")) {
                    errMsg = activity.getResources().getString(R.string.avail_outofstock);
                }
                activity.showErrorDialog(errMsg);
            } else {
//                activity.showNotificationBanner(R.string.cart_updated_msg);
            }
            convertCart(CartApiManager.getCart());
        } else {
            ActionBar.getInstance().setCartCount(CartApiManager.getCartTotalItems());
        }
    }

    private void convertCart(Cart cart) {

        // clear the cart before refilling
        ArrayList<CartItem> cartItems = new ArrayList<CartItem>();
        ArrayList<CartItemGroup> itemGroups = new ArrayList<CartItemGroup>();
        List<CouponItem> couponItems = new ArrayList<CouponItem>();
        couponsRewardsAmount = 0;

        if (cart != null) {

            // rather than call the api to refresh the profile, use the info from the cart to update coupon info in the profile
            ProfileDetails.updateRewardsFromCart(cart);

            couponsRewardsAmount = CartApiManager.getCouponsRewardsAdjustedAmount();

            List<Product> products = cart.getProduct();
            if (products != null) {

                // iterate thru products to create list of cart items
                for (Product product : products) {
                    if (product.getQuantity() > 0) { // I actually saw a zero quantity once returned from sapi
                        cartItems.add(new CartItem(product));
                    }
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
                }
            }

            DecimalFormat currencyFormat = MiscUtils.getCurrencyFormat();

            // set text of coupons
            couponsRewardsValue.setText(currencyFormat.format(couponsRewardsAmount));
            boolean showCouponsAmount = (couponsRewardsAmount != 0 || couponsExpanded);
            couponsRewardsValue.setVisibility(showCouponsAmount ? View.VISIBLE : View.GONE);
            couponsRewardsLabel.setCompoundDrawablesWithIntrinsicBounds(couponsExpanded ? R.drawable.ic_remove_green : R.drawable.ic_add_green,0,0,0);

            // update coupon list
            List<Reward> profileRewards = ProfileDetails.getAllProfileRewards();
            // potentially add lines for associate reward coupons
            Coupon assocRewardCoupon = CartApiManager.getAssocRewardCoupon();
            Coupon assocRewardStaplesCoupon = CartApiManager.getAssocRewardStaplesCoupon();
            if (assocRewardCoupon != null) {
                couponItems.add(new CouponItem(CouponItem.TYPE_ASSOC_REWARD_COUPON, assocRewardCoupon, null));
            }
            if (assocRewardStaplesCoupon != null) {
                couponItems.add(new CouponItem(CouponItem.TYPE_ASSOC_REWARD_COUPON, assocRewardStaplesCoupon, null));
            }
            // add line to add a coupon
            couponItems.add(new CouponItem(CouponItem.TYPE_COUPON_TO_ADD, null, null));
            // add list of applied cart-level coupons
            if (cart!=null) {
                List<Coupon> coupons = cart.getCoupon();
                if (coupons!=null) {
                    for(Coupon coupon : coupons) {
                        if (!CartApiManager.isAssocCoupon(coupon)) {
                            // coupon may or may not have a matching reward
                            Reward reward = ProfileDetails.findMatchingReward(profileRewards, coupon.getCode());
                            if (reward != null) {
                                profileRewards.remove(reward); // remove the applied rewards from the list
                            }
                            couponItems.add(new CouponItem(CouponItem.TYPE_APPLIED_COUPON, coupon, reward));
                        }
                    }
                }
            }
            // if any sku-level coupons should be displayed, add them
            List<Coupon> skuLevelCoupons = CartApiManager.getManualSkuLevelCoupons();
            for (Coupon coupon : skuLevelCoupons) {
                couponItems.add(new CouponItem(CouponItem.TYPE_APPLIED_COUPON, coupon, null));
            }

            // if profile exists (registered user logged in and no errors getting profile)
            if (ProfileDetails.getMember() != null) {

                // if rewards member
                if (ProfileDetails.isRewardsMember()) {

                    // add redeemable rewards heading
                    couponItems.add(new CouponItem(CouponItem.TYPE_REDEEMABLE_REWARD_HEADING, null, null));

                    // if any unapplied redeemable rewards
                    if (profileRewards.size() > 0) {
                        // add redeemable rewards
                        for (Reward reward : profileRewards) {
                            couponItems.add(new CouponItem(CouponItem.TYPE_REDEEMABLE_REWARD, null, reward));
                        }
                    } else {
                        couponItems.add(new CouponItem(CouponItem.TYPE_NO_REDEEMABLE_REWARDS_MSG, null, null));
                    }
                } else {
                    // if registered but not a rewards member
                    couponItems.add(new CouponItem(CouponItem.TYPE_LINK_REWARD_ACCOUNT, null, null));
                }
            }
        }
        couponAdapter.setItems(couponItems);
        cartListItems = cartItems;
        cartItemGroups = itemGroups;
        setAdapterListItems();
    }

//    class FadeInOutListener implements Animation.AnimationListener {
//
//        private boolean fadeIn;
//        private boolean inProcess = false;
//
//        FadeInOutListener(boolean fadeIn) {
//            this.fadeIn = fadeIn;
//        }
//
//        public boolean isInProcess() {
//            return inProcess;
//        }
//
//        @Override public void onAnimationStart(Animation animation) {
//            inProcess = true;
//        }
//        @Override public void onAnimationEnd(Animation animation) {
//            couponsRewardsLayout.setVisibility(fadeIn? View.VISIBLE : View.GONE); // show/hide after animation finished
//            inProcess = false;
//        }
//        @Override public void onAnimationRepeat(Animation animation) { }
//    }

    private class CouponAnimator {
        private ValueAnimator valueAnimator;

        private CouponAnimator(final boolean expand) {
            valueAnimator = ValueAnimator.ofFloat(0f, 1f);
            valueAnimator.setDuration(500);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    LinearLayout.LayoutParams params;
                    float ratio = animation.getAnimatedFraction();
                    params = (LinearLayout.LayoutParams) cartListVw.getLayoutParams();
                    params.weight = !expand ? ratio : (1f-ratio);
                    params = (LinearLayout.LayoutParams) couponListVw.getLayoutParams();
                    params.weight = expand ? ratio : (1f-ratio);
                    cartListVw.requestLayout();
                    couponListVw.requestLayout();
                }
            });
        }

        private void start() {
            valueAnimator.start();
        }
    }

    private void toggleCouponLayout() {
        if (couponsExpanded) {
            couponCollapseAnimator.start();
            couponsExpanded = false;
        } else {
            couponExpandAnimator.start();
            couponsExpanded = true;
            Tracker.getInstance().trackStateForCartCoupons(); // analytics
        }

        couponsRewardsLabel.setCompoundDrawablesWithIntrinsicBounds(couponsExpanded ? R.drawable.ic_remove_green : R.drawable.ic_add_green,0,0,0);

        boolean showCouponsAmount = (couponsRewardsAmount != 0 || couponsExpanded);
        couponsRewardsValue.setVisibility(showCouponsAmount ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View view) {
        Object tag;
        activity.hideSoftKeyboard();
        switch(view.getId()) {
            case R.id.cartitem_image:
            case R.id.cartitem_title:
                tag = view.getTag();
                if (tag instanceof CartItem) {
                    CartItem cartItem = (CartItem) tag;
                    activity.selectSkuItem(cartItem.getDescription(), cartItem.getSku(), false);
                }
                break;
            case R.id.cartitem_delete:
                tag = view.getTag();
                if (tag instanceof CartItem) {
                    CartItem cartItem = (CartItem) tag;

                    activity.hideSoftKeyboard();

                    // delete from cart via API
                    cartItem.setProposedQty(0);
                    updateItemQty(cartItem);
                }
                break;
            case R.id.coupons_rewards_layout:
                toggleCouponLayout();
                break;
            case R.id.coupon_add_button:
                tag = view.getTag();
                if (tag instanceof CouponItem) {
                    String code = ((CouponItem) tag).getCode();
                    if (TextUtils.isEmpty(code)) {
                        activity.showErrorDialog(R.string.missing_coupon_code);
                    } else {
                        activity.showProgressIndicator();
                        CartApiManager.addCoupon(code, this);
                        Tracker.getInstance().trackActionForRewardsAddToCart();
                    }
                }
                break;
            case R.id.coupon_delete_button:
                tag = view.getTag();
                if (tag instanceof CouponItem) {
                    String code = ((CouponItem) tag).getCode();
                    if (!TextUtils.isEmpty(code)) {
                        activity.showProgressIndicator();
                        CartApiManager.deleteCoupon(code, this);
                    }
                }
                break;
//            case R.id.coupon_view_button:
//                tag = view.getTag();
//                if (tag instanceof CouponItem) {
//                    Reward reward = ((CouponItem) tag).getReward();
//                    if (reward!=null) {
//                        BarcodeFragment fragment = new BarcodeFragment();
//                        fragment.setArguments("Coupon", reward.getCode(), reward.getAmount(), reward.getExpiryDate());
//                        activity.selectFragment(DrawerItem.BARCODE, fragment, MainActivity.Transition.RIGHT);
//                    }
//                }
//                break;
            case R.id.rewards_link_acct_button:
                tag = view.getTag();
                if (tag instanceof CouponItem) {
                    CouponItem item = (CouponItem) tag;
                    final String rewardsNumber = item.getRewardsNumber();
                    String phoneNumber = stripPhoneNumber(item.getPhoneNumber());

                    if(validateFields(rewardsNumber, phoneNumber)) {
                        if(phoneNumber.length() < 10) {
                            activity.showErrorDialog(R.string.invalid_phone_number);
                            return;
                        }
                        activity.showProgressIndicator();
                        RewardsLinkingFragment.linkRewardsAccount(rewardsNumber, phoneNumber, new RewardsLinkingFragment.LinkRewardsCallback() {
                            @Override
                            public void onLinkRewardsComplete(String errMsg) {
                                if (getActivity() == null) return; // check for fragment detachment

                                activity.hideProgressIndicator();
                                if (errMsg != null) {
                                    activity.showErrorDialog(errMsg);
                                } else {
                                    // temporarily update profile object
                                    ProfileDetails.getMember().setRewardsNumber(rewardsNumber);
                                    ProfileDetails.getMember().setRewardsNumberVerified(true);
                                    convertCart(CartApiManager.getCart());
                                }
                            }
                        });
                    } else{
                        activity.showErrorDialog(R.string.empty_rewards_linking_msg);
                    }
                }
                break;
            case R.id.action_checkout:
                activity.selectOrderCheckout();
                break;
            case R.id.action_android_pay:
                // TODO Need to implement Android Pay
                break;
        }
    }

    @Override
    public void onQtyChange(View view, int value) {
        Object tag = view.getTag();
        if (tag instanceof CartItem) {
            CartItem cartItem = (CartItem) tag;

            // default proposed qty to orig in case new value not parseable;
            cartItem.setProposedQty(value);
            updateItemQty(cartItem);
        }
    }
}

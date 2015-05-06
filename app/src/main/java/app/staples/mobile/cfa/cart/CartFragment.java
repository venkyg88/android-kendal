package app.staples.mobile.cfa.cart;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;
import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.config.AppConfigurator;
import com.staples.mobile.common.access.configurator.model.Configurator;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.easyopen.model.cart.Coupon;
import com.staples.mobile.common.access.easyopen.model.cart.Product;
import com.staples.mobile.common.access.easyopen.model.member.Reward;
import com.staples.mobile.common.analytics.Tracker;

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
import app.staples.mobile.cfa.util.CurrencyFormat;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.QuantityEditor;

/** fragment to manage display and update of shopping cart */
public class CartFragment extends Fragment implements View.OnClickListener, CartApiManager.CartRefreshCallback {

    private static final String TAG = CartFragment.class.getSimpleName();

    // saving around Activity object since getActivity() returns null after user navigates away from
    // fragment, but api call may still be returning
    private MainActivity activity;

    private TextView cartSubtotal;
    private TextView cartFreeShippingMsg;
    private TextView oversizedShipping;
    private TextView oversizedShippingLabel;
    private TextView cartShipping;
    private TextView couponsRewardsLabel;
    private TextView couponsRewardsValue;
    EditText phoneNumberVw;
    private RecyclerView couponListVw;
    private CouponAdapter couponAdapter;
    private View emptyCartLayout;
    private View cartProceedToCheckout;
    private View cartShippingLayout;
    private View cartSubtotalLayout;
    private CartAdapter cartAdapter;
    private RecyclerView cartListVw;
    private LinearLayoutManager cartListLayoutMgr;
    private View couponsRewardsLayout;
    private View linkRewardsAcctLayout;
    private Button rewardsLinkAcctButton;
    private int greenText;
    private int blackText;

    // cart object - make these static so they're not lost on device rotation
    private static List<CartItem> cartListItems;
    private static List<CartItemGroup> cartItemGroups;

    private float couponsRewardsAmount;

    // widget listeners
    private QtyDeleteButtonListener qtyDeleteButtonListener;
    private QtyChangeListener qtyChangeListener;
    private ProductClickListener productClickListener;
    PhoneNumberFormattingTextWatcher phoneNumberFormattingTextWatcher;

    CouponWeightAnimator couponUpAnimator;
    CouponWeightAnimator couponDownAnimator;
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
        cartFreeShippingMsg = (TextView) view.findViewById(R.id.free_shipping_msg);
        couponsRewardsLayout = view.findViewById(R.id.coupons_rewards_layout);
        couponsRewardsLabel = (TextView) view.findViewById(R.id.coupons_rewards_label);
        couponsRewardsValue = (TextView) view.findViewById(R.id.coupons_rewards_value);
        linkRewardsAcctLayout = view.findViewById(R.id.link_rewards_acct_layout);
        oversizedShipping = (TextView) view.findViewById(R.id.oversized_shipping);
        oversizedShippingLabel = (TextView) view.findViewById(R.id.oversized_shipping_label);
        cartShipping = (TextView) view.findViewById(R.id.cart_shipping);
        cartSubtotal = (TextView) view.findViewById(R.id.cart_subtotal);
        cartShippingLayout = view.findViewById(R.id.cart_shipping_layout);
        cartSubtotalLayout = view.findViewById(R.id.cart_subtotal_layout);
        cartProceedToCheckout = view.findViewById(R.id.action_checkout);
        rewardsLinkAcctButton = (Button)view.findViewById(R.id.rewards_link_acct_button);

        Resources r = getResources();
        greenText = r.getColor(R.color.staples_green);
        blackText = r.getColor(R.color.staples_black);

        // create widget listeners
        qtyChangeListener = new QtyChangeListener();
        qtyDeleteButtonListener = new QtyDeleteButtonListener();
        productClickListener = new ProductClickListener();

        // Initialize coupon listview
        couponListVw = (RecyclerView) view.findViewById(R.id.coupon_list);
        couponAdapter = new CouponAdapter(this, this);
        couponListVw.setAdapter(couponAdapter);
        couponListVw.setLayoutManager(new LinearLayoutManager(activity));

        // set up coupons & rewards panel animation
        couponUpAnimator = new CouponWeightAnimator(true);
        couponDownAnimator = new CouponWeightAnimator(false);

        phoneNumberFormattingTextWatcher = new PhoneNumberFormattingTextWatcher();
        phoneNumberVw = (EditText)view.findViewById(R.id.rewards_phone_number);
        phoneNumberVw.addTextChangedListener(phoneNumberFormattingTextWatcher);

//        // set up fade in/out animations
//        mathStoryFadeInAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
//        mathStoryFadeOutAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_out);
//        mathStoryFadeInAnimationListener = new FadeInOutListener(true);
//        mathStoryFadeOutAnimationListener = new FadeInOutListener(false);
//        mathStoryFadeInAnimation.setAnimationListener(mathStoryFadeInAnimationListener);
//        mathStoryFadeOutAnimation.setAnimationListener(mathStoryFadeOutAnimationListener);

        // Initialize cart listview
        cartAdapter = new CartAdapter(activity, R.layout.cart_item_group, qtyChangeListener,
                qtyDeleteButtonListener, productClickListener);
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
        cartProceedToCheckout.setOnClickListener(this);
        couponsRewardsLayout.setOnClickListener(this);
        rewardsLinkAcctButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // force cart fragment to portrait mode because it doesn't fit well in landscape
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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

        // allow rotation for all fragments except cart
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
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
                freeShippingThreshold = configurator.getAppContext().getPromotions().getFreeShippingThreshold().floatValue();
            }
        }

        // set text of cart icon badge
        ActionBar.getInstance().setCartCount(totalItemCount);

        // if fragment is attached to activity, then update the fragment's views
        if (getActivity() != null) {

            emptyCartLayout.setVisibility(totalItemCount == 0? View.VISIBLE : View.GONE);

            DecimalFormat currencyFormat = CurrencyFormat.getFormatter();

            // set text of free shipping msg
            if (totalItemCount > 0) {
                if (freeShippingThreshold > subtotal && !"Free".equals(shipping) && !ProfileDetails.isRewardsMember()) {
                    // need to spend more to qualify for free shipping
                    cartFreeShippingMsg.setVisibility(View.VISIBLE);
                    cartFreeShippingMsg.setText(String.format(r.getString(R.string.free_shipping_msg1),
                            currencyFormat.format(freeShippingThreshold), currencyFormat.format(freeShippingThreshold - subtotal)));
                } else {
                    // qualifies for free shipping
                    String freeShippingMsg = r.getString(R.string.free_shipping_msg2);
                    if (!freeShippingMsg.equals(cartFreeShippingMsg.getText().toString())) {
                        cartFreeShippingMsg.setVisibility(View.VISIBLE);
                        cartFreeShippingMsg.setText(freeShippingMsg);
//                        // hide after a delay
//                        cartFreeShippingMsg.postDelayed(new Runnable() {
//                            @Override public void run() {
//                                cartFreeShippingMsg.setVisibility(View.GONE);
//                            }
//                        }, 3000);
                    }
                }
            } else {
                cartFreeShippingMsg.setVisibility(View.GONE);
            }

            // set text of shipping, and subtotal
            if (totalHandlingCost > 0) {
                String totalHandlingCostStr = Float.toString(totalHandlingCost);
                oversizedShipping.setText(CheckoutFragment.formatShippingCharge(totalHandlingCostStr, currencyFormat));
                oversizedShipping.setTextColor(blackText);
                oversizedShippingLabel.setVisibility(View.VISIBLE);
                oversizedShipping.setVisibility(View.VISIBLE);
            } else {
                oversizedShippingLabel.setVisibility(View.GONE);
                oversizedShipping.setVisibility(View.GONE);
            }

            cartShipping.setText(CheckoutFragment.formatShippingCharge(shipping, currencyFormat));
            cartShipping.setTextColor("Free".equals(shipping) ? greenText : blackText);
            cartSubtotal.setText(currencyFormat.format(preTaxSubtotal));

            // only show shipping, subtotal, and proceed-to-checkout when at least one item
            if (totalItemCount == 0) {
                couponsRewardsLayout.setVisibility(View.GONE);
                cartShippingLayout.setVisibility(View.GONE);
                cartSubtotalLayout.setVisibility(View.GONE);
                cartProceedToCheckout.setVisibility(View.GONE);
            } else {
//                if (isTopOfFirstItemVisible(cartListVw)) {
                couponsRewardsLayout.setVisibility(View.VISIBLE);
                cartShippingLayout.setVisibility(View.VISIBLE);
//                }
                cartSubtotalLayout.setVisibility(View.VISIBLE);
                cartProceedToCheckout.setVisibility(View.VISIBLE);
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

    private boolean couponsExpanded() {
        return (((LinearLayout.LayoutParams)couponListVw.getLayoutParams()).weight > 0);
    }

    @Override
    public void onClick(View view) {
        activity.hideSoftKeyboard();
        switch(view.getId()) {
            case R.id.coupons_rewards_layout:
                if (!couponsExpanded()) {
                    couponUpAnimator.start();
                    Tracker.getInstance().trackStateForCartCoupons(); // analytics
                } else {
                    couponDownAnimator.start();
                }
                break;
            case R.id.coupon_add_button:
                String couponCode = couponAdapter.getItem((Integer)view.getTag()).getCouponCodeToAdd();
                if (TextUtils.isEmpty(couponCode)) {
                    activity.showErrorDialog(R.string.missing_coupon_code);
                } else {
                    activity.showProgressIndicator();
                    CartApiManager.addCoupon(couponCode, this);
                }
                break;
            case R.id.reward_add_button:
                activity.showProgressIndicator();
                CartApiManager.addCoupon(couponAdapter.getItem((Integer) view.getTag()).getReward().getCode(), this);
                break;
            case R.id.coupon_delete_button:
                activity.showProgressIndicator();
                CartApiManager.deleteCoupon(couponAdapter.getItem((Integer) view.getTag()).getCoupon().getCode(), this);
                break;
            case R.id.action_checkout:
                activity.selectOrderCheckout();
                break;
            case R.id.rewards_link_acct_button:
                activity.hideSoftKeyboard();


                String rewardsNumber = ((EditText)getView().findViewById(R.id.rewards_card_number)).getText().toString();
                String phoneNumber = stripPhoneNumber(phoneNumberVw.getText().toString());

                if(validateFields(rewardsNumber, phoneNumber)) {
                    if(phoneNumber.length() < 10) {
                        activity.showErrorDialog(R.string.invalid_phone_number);
                        return;
                    }
                    activity.showProgressIndicator();
                    RewardsLinkingFragment.linkRewardsAccount(rewardsNumber, phoneNumber, new RewardsLinkingFragment.LinkRewardsCallback() {
                        @Override
                        public void onLinkRewardsComplete(String errMsg) {
                            activity.hideProgressIndicator();
                            if (errMsg != null) {
                                activity.showErrorDialog(errMsg);
                            } else {
                                linkRewardsAcctLayout.setVisibility(View.GONE);
                                updateCartFields();
                            }
                        }
                    });
                } else{
                    activity.showErrorDialog(R.string.empty_rewards_linking_msg);
                }
                break;
        }
    }

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
                    }
                });
            } else {
                Tracker.getInstance().trackActionForUpdateQtyFromCart(cartItem.getSku());
                CartApiManager.updateItemQty(cartItem.getOrderItemId(), cartItem.getSku(), cartItem.getProposedQty(), this);
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
                activity.showNotificationBanner(R.string.cart_updated_msg);
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

            DecimalFormat currencyFormat = CurrencyFormat.getFormatter();

            // set text of coupons
            boolean couponsShowing = couponsExpanded();
            couponsRewardsValue.setText(currencyFormat.format(couponsRewardsAmount));
            boolean showCouponsAmount = (couponsRewardsAmount != 0 || couponsShowing);
            couponsRewardsValue.setVisibility(showCouponsAmount ? View.VISIBLE : View.GONE);
            couponsRewardsLabel.setCompoundDrawablesWithIntrinsicBounds(couponsShowing? R.drawable.ic_remove_green : R.drawable.ic_add_green,0,0,0);

            // update coupon list
            List<Reward> profileRewards = ProfileDetails.getAllProfileRewards();
            // potentially add lines for associate reward coupons
            Coupon assocRewardCoupon = CartApiManager.getAssocRewardCoupon();
            Coupon assocRewardStaplesCoupon = CartApiManager.getAssocRewardStaplesCoupon();
            if (assocRewardCoupon != null) {
                couponItems.add(new CouponItem(assocRewardCoupon, null, CouponItem.TYPE_ASSOC_REWARD_COUPON));
            }
            if (assocRewardStaplesCoupon != null) {
                couponItems.add(new CouponItem(assocRewardStaplesCoupon, null, CouponItem.TYPE_ASSOC_REWARD_COUPON));
            }
            // add line to add a coupon
            couponItems.add(new CouponItem(null, null, CouponItem.TYPE_COUPON_TO_ADD));
            // add list of applied cart-level coupons
            if (cart != null && cart.getCoupon() != null && cart.getCoupon().size() > 0) {
                for (Coupon coupon : cart.getCoupon()) {
                    if (!CartApiManager.isAssocCoupon(coupon)) {
                        // coupon may or may not have a matching reward
                        Reward reward = ProfileDetails.findMatchingReward(profileRewards, coupon.getCode());
                        if (reward != null) {
                            profileRewards.remove(reward); // remove the applied rewards from the list
                        }
                        couponItems.add(new CouponItem(coupon, reward, CouponItem.TYPE_APPLIED_COUPON));
                    }
                }
            }
            // if any sku-level coupons should be displayed, add them
            List<Coupon> skuLevelCoupons = CartApiManager.getManualSkuLevelCoupons();
            for (Coupon coupon : skuLevelCoupons) {
                couponItems.add(new CouponItem(coupon, null, CouponItem.TYPE_APPLIED_COUPON));
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

    class CouponWeightAnimator {
        ValueAnimator valueAnimator;
        float maxWeight = 999;

        public CouponWeightAnimator(final boolean up) {
            valueAnimator = ValueAnimator.ofFloat(0, maxWeight);
            valueAnimator.setDuration(500);
            valueAnimator.setInterpolator(new DecelerateInterpolator() {
                @Override
                public float getInterpolation(float input) {
                    input = super.getInterpolation(input);
                    // need a weight ratio that corresponds to the animation fraction
                    // The cart item list has weight=1 so, input = w/(w+1), so w = (input/(1-input))
                    if (!up) { input = 1 - input; } // if going down reverse it
                    float weightRatio = 1;
                    if (input < 1) {
                        weightRatio = (input / (1 - input)) / maxWeight;
                    }
                    return weightRatio;
                }
            });
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) couponListVw.getLayoutParams();
                    layoutParams.weight = (float) animation.getAnimatedValue();
                    couponListVw.setLayoutParams(layoutParams);
                }
            });
            valueAnimator.addListener(new Animator.AnimatorListener() {
                @Override public void onAnimationStart(Animator animation) {
                    // if starting to animate down, set cart items to visible and hide rewards account
                    if (!up) {
                        linkRewardsAcctLayout.setVisibility(View.GONE);
                    }
                    // update these at beginning of animation while it's moving fast rather than when it's coasting to a finish at the end
                    boolean showCouponsAmount = (couponsRewardsAmount != 0 || up);
                    couponsRewardsValue.setVisibility(showCouponsAmount? View.VISIBLE : View.GONE);
                    couponsRewardsLabel.setCompoundDrawablesWithIntrinsicBounds(up? R.drawable.ic_remove_green : R.drawable.ic_add_green,0,0,0);
                }
                @Override public void onAnimationEnd(Animator animation) {
                    // if finishing animation up, set cart items to gone and show rewards account if applicable
                    if (up) {
                        // if logged in and not a rewards member, display link rewards layout
                        if (Access.getInstance().isLoggedIn() && !Access.getInstance().isGuestLogin() &&
                                ProfileDetails.getMember() != null && !ProfileDetails.isRewardsMember()) {
                            linkRewardsAcctLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
                @Override public void onAnimationCancel(Animator animation) { }
                @Override public void onAnimationRepeat(Animator animation) { }
            });

        }

        public void start() {
            valueAnimator.start();
        }
    }

    /************* widget listeners ************/

    /** listener class for qty change */
    class QtyChangeListener implements QuantityEditor.OnQtyChangeListener {
        @Override
        public void onQtyChange(View view, int value) {
            Object tag = view.getTag();
            if (tag != null && tag instanceof CartAdapter.CartItemPosition) {
                CartItem cartItem = cartAdapter.getCartItem((CartAdapter.CartItemPosition) tag);

                // default proposed qty to orig in case new value not parseable;
                cartItem.setProposedQty(value);

                updateItemQty(cartItem);
            }
        }
    }

    /** listener class for item deletion button */
    class QtyDeleteButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Object tag = view.getTag();
            if (tag != null && tag instanceof CartAdapter.CartItemPosition) {
                CartItem cartItem = cartAdapter.getCartItem((CartAdapter.CartItemPosition)tag);

                activity.hideSoftKeyboard();

                // delete from cart via API
                cartItem.setProposedQty(0);
                updateItemQty(cartItem);
            }
        }
    }

    /** listener class for item deletion button */
    class ProductClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (view.getTag() != null && view.getTag() instanceof CartAdapter.CartItemPosition) {
                CartItem cartItem = cartAdapter.getCartItem((CartAdapter.CartItemPosition) view.getTag());
                activity.selectSkuItem(cartItem.getDescription(), cartItem.getSku(), false);
            }
        }
    }

}

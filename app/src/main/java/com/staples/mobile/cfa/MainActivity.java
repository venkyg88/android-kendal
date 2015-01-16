package com.staples.mobile.cfa;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import com.staples.mobile.cfa.bundle.BundleFragment;
import com.staples.mobile.cfa.cart.CartApiManager;
import com.staples.mobile.cfa.cart.CartFragment;
import com.staples.mobile.cfa.checkout.CheckoutFragment;
import com.staples.mobile.cfa.checkout.ConfirmationFragment;
import com.staples.mobile.cfa.checkout.GuestCheckoutFragment;
import com.staples.mobile.cfa.checkout.RegisteredCheckoutFragment;
import com.staples.mobile.cfa.location.LocationFinder;
import com.staples.mobile.cfa.login.LoginFragment;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.profile.AddressFragment;
import com.staples.mobile.cfa.profile.AddressListFragment;
import com.staples.mobile.cfa.profile.CreditCardFragment;
import com.staples.mobile.cfa.profile.CreditCardListFragment;
import com.staples.mobile.cfa.profile.OrderFragment;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.cfa.profile.ProfileFragment;
import com.staples.mobile.cfa.rewards.RewardsFragment;
import com.staples.mobile.cfa.rewards.RewardsLinkingFragment;
import com.staples.mobile.cfa.search.SearchFragment;
import com.staples.mobile.cfa.sku.SkuFragment;
import com.staples.mobile.cfa.skuset.SkuSetFragment;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.LinearLayoutWithProgressOverlay;
import com.staples.mobile.common.access.config.AppConfigurator;
import com.staples.mobile.common.access.configurator.model.Configurator;

public class MainActivity extends Activity
                          implements View.OnClickListener, AdapterView.OnItemClickListener, LoginHelper.OnLoginCompleteListener, AppConfigurator.AppConfiguratorCallback {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int SURRENDER_TIMEOUT = 5000;

    private DrawerLayout drawerLayout;
    private ListView leftDrawer;
    private DrawerAdapter leftDrawerAdapter;
    private LinearLayoutWithProgressOverlay mainLayout;
    private CartFragment cartFragment;
    private DrawerItem homeDrawerItem;
    private int screenHeight;

    private LoginHelper loginHelper;

    private AppConfigurator appConfigurator;

    public enum Transition {
        NONE (0, 0, 0, 0, 0),
        RIGHT(0, R.animator.right_push_enter, R.animator.right_push_exit, R.animator.right_pop_enter, R.animator.right_pop_exit),
        UP   (0, R.animator.up_push_enter, R.animator.up_push_exit, R.animator.up_pop_enter, R.animator.up_pop_exit),
        FADE (0, R.animator.fade_in, R.animator.fade_out, 0, 0);

        private int standard;
        private int push_enter;
        private int push_exit;
        private int pop_enter;
        private int pop_exit;

        Transition(int standard, int push_enter, int push_exit, int pop_enter, int pop_exit) {
            this.standard = standard;
            this.push_enter = push_enter;
            this.push_exit = push_exit;
            this.pop_enter = pop_enter;
            this.pop_exit = pop_exit;
        }

        public void setAnimation(FragmentTransaction transaction) {
            if (standard!=0) {
                transaction.setTransition(standard);
            }
            else if (pop_enter!=0 && pop_exit!=0) {
                transaction.setCustomAnimations(push_enter, push_exit, pop_enter, pop_exit);
            }
            else if (push_enter!=0 && push_exit!=0) {
                transaction.setCustomAnimations(push_enter, push_exit);
            }
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        boolean freshStart = (bundle == null);
        prepareMainScreen(freshStart);

        LocationFinder.getInstance(this);

        appConfigurator = AppConfigurator.getInstance();
        appConfigurator.getConfigurator(this); // AppConfiguratorCallback
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationFinder.getInstance(this).saveRecentLocation();
//        searchView.saveSearchHistory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new LoginHelper(this).unregisterLoginCompleteListener(this);
    }

    public void showMainScreen() {
        findViewById(R.id.splash).setVisibility(View.GONE);
        findViewById(R.id.main).setVisibility(View.VISIBLE);
    }

    public void prepareMainScreen(boolean freshStart) {
        // Inflate
        setContentView(R.layout.main);
        ActionBar.getInstance().init(this);

        // Find top-level entities
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        leftDrawer = (ListView) findViewById(R.id.left_drawer);
        mainLayout = (LinearLayoutWithProgressOverlay)findViewById(R.id.main);
        mainLayout.setProgressOverlay(findViewById(R.id.progress_overlay));

        // Initialize left drawer listview
        leftDrawerAdapter = new DrawerAdapter(this);
        leftDrawer.setAdapter(leftDrawerAdapter);
        leftDrawerAdapter.fill();
        leftDrawer.setOnItemClickListener(this);

        // Create non-drawer DrawerItems
        homeDrawerItem = leftDrawerAdapter.getItem(0); // TODO Hard-coded alias

        // Cart
        cartFragment = new CartFragment();
        ActionBar.getInstance().setCartCount(0);

        // Fresh start?
        if (freshStart) {
            selectDrawerItem(homeDrawerItem, Transition.NONE, false);
            Runnable runs = new Runnable() {public void run() {
                showMainScreen();}};
            new Handler().postDelayed(runs, SURRENDER_TIMEOUT);
        } else {
            showMainScreen();
        }
    }

    public void onGetConfiguratorResult(Configurator configurator, boolean success) {

        if (success) {

            loginHelper = new LoginHelper(this);
            loginHelper.registerLoginCompleteListener(this);
            // if already logged in (e.g. when device is rotated), don't login again, but do notify
            // that login is complete so that cart can be refilled
            if (loginHelper.isLoggedIn()) {
                onLoginComplete(loginHelper.isGuestLogin());
            } else {
                // otherwise, do login as guest
                loginHelper.getGuestTokens();
            }
        }
    }

    @Override
    public void onLoginComplete(boolean guestLevel) {
        // if registered user, update menu state and load cart info
        if (!guestLevel) {
            CartApiManager.loadCart(new CartApiManager.CartRefreshCallback() {
                @Override
                public void onCartRefreshComplete(String errMsg) {
                    ActionBar.getInstance().setCartCount(CartApiManager.getCartTotalItems());
                }
            });
        }
        // enable/disable left drawer menu items that depend upon login
        refreshMenuItemState(!guestLevel);
    }

    @Override
    public void onLogoutComplete() {
        // if cart not empty, reset it
        if (CartApiManager.getCart() != null && CartApiManager.getCartTotalItems() > 0) {
            CartApiManager.resetCart();
            ActionBar.getInstance().setCartCount(0);
        }

        // disable menu items as appropriate
        refreshMenuItemState(false);
    }

    private void refreshMenuItemState(boolean registeredUser) {
        // enable/disable left drawer menu items that depend upon login
        int itemCount = leftDrawerAdapter.getCount();
        for (int position = 0; position < itemCount; position++) {
            DrawerItem item = leftDrawerAdapter.getItem(position);
            if (item.fragmentClass == RewardsFragment.class || item.fragmentClass == OrderFragment.class) {
                item.enabled = registeredUser;
                leftDrawerAdapter.notifyDataSetChanged();
            }
        }

        // update sign-in button text
        Button signInButton = (Button)findViewById(R.id.account_button);
        if (signInButton != null) { // is null in roboelectric tests
            signInButton.setText(registeredUser ? R.string.signout_title : R.string.login_title);
        }
    }

    public void showProgressIndicator() {
        mainLayout.showProgressIndicator(true);
    }

    public void hideProgressIndicator() {
        mainLayout.showProgressIndicator(false);
    }

    // Navigation
    public boolean selectFragment(Fragment fragment, Transition transition, boolean push) {
        return selectFragment(fragment, transition, push, null);
    }

    public boolean selectFragment(Fragment fragment, Transition transition, boolean push, String tag) {
        // Make sure all drawers are closed
        drawerLayout.closeDrawers();

        // Swap Fragments
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (transition!=null) transition.setAnimation(transaction);
        transaction.replace(R.id.content, fragment, tag);
        if (push)
            transaction.addToBackStack(fragment.getClass().getName());
        transaction.commit();
        return(true);
    }

    private boolean selectDrawerItem(DrawerItem item, Transition transition, boolean push) {
        // Safety check
        if (item == null || item.fragmentClass == null) return (false);

        // Create Fragment if necessary
        if (item.fragment == null)
            item.instantiate(this);
        return(selectFragment(item.fragment, transition, push));
    }

    public boolean selectShoppingCart() {
        return selectFragment(cartFragment, Transition.NONE, true);
    }

    public boolean selectOrderCheckout(String deliveryRange) {
        LoginHelper loginHelper = new LoginHelper(this);
        if (loginHelper.isLoggedIn()) {
            CheckoutFragment fragment;
            float couponsRewardsAmount = cartFragment.getCouponsRewardsAdjustedAmount();
            // if logged in and have at least an address or a payment method, then use registered flow, otherwise use guest flow
            if (!loginHelper.isGuestLogin() && (ProfileDetails.hasAddress() || ProfileDetails.hasPaymentMethod())) {
                fragment = RegisteredCheckoutFragment.newInstance(couponsRewardsAmount,
                        CartApiManager.getSubTotal(), CartApiManager.getPreTaxTotal(), deliveryRange);
            } else {
                fragment = GuestCheckoutFragment.newInstance(couponsRewardsAmount,
                        CartApiManager.getSubTotal(), CartApiManager.getPreTaxTotal(), deliveryRange);
            }
            return selectFragment(fragment, Transition.NONE, true);
        }
        return false;
    }

    public boolean selectOrderConfirmation(String orderNumber, String emailAddress,
                                           String deliveryRange, String total) {
        // open order confirmation fragment
        Fragment fragment = ConfirmationFragment.newInstance(orderNumber, emailAddress, deliveryRange, total);
        return selectFragment(fragment, Transition.NONE, true, ConfirmationFragment.TAG);
    }

    public boolean selectRewardsFragment() {
        return selectFragment(new RewardsFragment(), Transition.RIGHT, true);
    }

    public boolean selectRewardsLinkingFragment() {
        return selectFragment(new RewardsLinkingFragment(), Transition.RIGHT, true);
    }

    public boolean selectBundle(String title, String identifier) {
        BundleFragment fragment = new BundleFragment();
        fragment.setArguments(title, identifier);
        return(selectFragment(fragment, Transition.RIGHT, true));
    }

    public boolean selectSearch(String keyword) {
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(keyword);
        return(selectFragment(fragment, Transition.RIGHT, true));
    }

    public boolean selectSkuSet(String title, String identifier, String imageUrl) {
        SkuSetFragment fragment = new SkuSetFragment();
        fragment.setArguments(title, identifier, imageUrl);
        return(selectFragment(fragment, Transition.UP, true));
    }

    public boolean selectSkuItem(String title, String identifier) {
        SkuFragment fragment = new SkuFragment();
        fragment.setArguments(title, identifier);

        // set animated bar in sku page
//        initAnimatedBar();

        return(selectFragment(fragment, Transition.RIGHT, true));
    }

    public boolean selectProfileFragment() {
        Fragment fragment = new ProfileFragment();
        return(selectFragment(fragment, Transition.NONE, true));
    }

    public boolean selectLoginFragment() {
        Fragment fragment = new LoginFragment();
        return(selectFragment(fragment, Transition.UP, true));
    }

    /** opens the profile addresses fragment */
    public boolean selectProfileAddressesFragment() {
        return(selectProfileAddressesFragment(null, null));
    }

    /** opens the profile addresses fragment, with optional selection listener */
    public boolean selectProfileAddressesFragment(ProfileDetails.AddressSelectionListener addressSelectionListener, String currentAddressId) {
        ProfileDetails.addressSelectionListener = addressSelectionListener;
        ProfileDetails.currentAddressId = currentAddressId;
        Fragment fragment;
        if (ProfileDetails.hasAddress()) {
            fragment = Fragment.instantiate(this, AddressListFragment.class.getName());
        } else {
            fragment = Fragment.instantiate(this, AddressFragment.class.getName());
        }
        return(selectFragment(fragment, Transition.NONE, true));
    }

    /** opens the profile credit cards fragment */
    public boolean selectProfileCreditCardsFragment() {
        return selectProfileCreditCardsFragment(null, null);
    }

    /** opens the profile credit cards fragment, with optional selection listener */
    public boolean selectProfileCreditCardsFragment(ProfileDetails.PaymentMethodSelectionListener paymentMethodSelectionListener, String currentPaymentMethodId) {
        ProfileDetails.paymentMethodSelectionListener = paymentMethodSelectionListener;
        ProfileDetails.currentPaymentMethodId = currentPaymentMethodId;
        Fragment fragment;
        if(ProfileDetails.hasPaymentMethod()) {
            fragment = Fragment.instantiate(this, CreditCardListFragment.class.getName());
        } else {
            fragment = Fragment.instantiate(this, CreditCardFragment.class.getName());
        }
        return(selectFragment(fragment, Transition.NONE, true));
    }

    public boolean navigateToFragment(Fragment fragment) {
        return (selectFragment(fragment, Transition.NONE, true));
    }

    ////////////////////////////////////////////////////////////
    // Methods for animated action bar
//    private void initAnimatedBar() {
//        // hide action bar at first (it will be shown while being scrolled down)
//        AnimatedBarScrollView.isFirstLoad = true;
//        AnimatedBarScrollView.currentAlpha = 0;
//        setContainFrameOffset();
//    }
//
//    public int getScreenHeight(){
//        // get height for sku scrollview animation effect
//        calculateScreenHeight();
//
//        return screenHeight;
//    }
//
//    private void calculateScreenHeight(){
//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        screenHeight = size.y;
//    }
//
//    public void setContainFrameOffset(){
//        containFrame.setPadding(0, 0, 0, 0);
//    }
//
//    public void restoreContainFrame(){
//        containFrame.setPadding(0, Math.round(convertDpToPixel(56f, this)), 0, 0);
//    }
//
//    public void setActionBarAlpha(int alpha){
//        actionBar.getBackground().setAlpha(alpha);
//    }
//
//    public void setActionBarColor(int id){
//        actionBar.setBackgroundColor(getResources().getColor(id));
//    }
//
//    public void setActionBarTitleAlpha(int alpha){
//        titleView.setTextColor(titleView.getTextColors().withAlpha(alpha));
//    }
//
//    public void setActionBarTitle(String title){
//        titleView.setText(title);
//    }
//
//    public void restoreDefaultActionBar(){
//        // restore action bar offset and title offset
//        setActionBarTitleAlpha(255);
//        setActionBarAlpha(255);
//
//        // restore contain frame offset
//        containFrame.setPadding(0, Math.round(convertDpToPixel(56f, this)), 0, 0);
//    }
//
//    public void setLeftDrawerOffset(){
//        leftDrawer.setPadding(0, (int) convertDpToPixel((float) 56, this), 0, 0);
//    }
//
//    public void restoreDefaultLeftDrawer(){
//        leftDrawer.setPadding(0, 0, 0, 0);
//    }
//
//    private float convertDpToPixel(float dp, Context context){
//        Resources resources = context.getResources();
//        DisplayMetrics metrics = resources.getDisplayMetrics();
//        float px = dp * (metrics.densityDpi / 160f);
//        return px;
//    }

    @Override
    public void onBackPressed () {
        // if on order confirmation fragment, don't go back to any of the checkout related pages, go to Home page
        FragmentManager manager = getFragmentManager();
        Fragment confirmationFragment = manager.findFragmentByTag(ConfirmationFragment.TAG);

        if (confirmationFragment != null && confirmationFragment.isVisible()) {
            selectDrawerItem(homeDrawerItem, Transition.NONE, true);
        } else {
            super.onBackPressed();
        }

    }

    // Action bar & button clicks

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.action_left_drawer:
                if (!drawerLayout.isDrawerOpen(leftDrawer)) {
                    drawerLayout.openDrawer(leftDrawer);
                } else drawerLayout.closeDrawers();
                break;

            case R.id.continue_shopping_btn:
                selectDrawerItem(homeDrawerItem, Transition.NONE, true);
                break;

            case R.id.action_show_cart:
                selectShoppingCart();
                break;

            case R.id.co_signin_button:
                selectLoginFragment();
                break;

            case R.id.close_button:
                selectShoppingCart();
                break;

            case R.id.account_button:
                if (loginHelper.isLoggedIn() && !loginHelper.isGuestLogin()) {
                    loginHelper.userSignOut();
                    selectDrawerItem(homeDrawerItem, Transition.RIGHT, true);
                } else {
                    selectLoginFragment();
                }
                break;

            case R.id.search_view:
                String query = ((SearchView) view).getQuery().toString().trim();
                if (!query.isEmpty()) {
                    selectSearch(query);
                }
                break;
        }
    }

    // Left drawer listview clicks

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        DrawerAdapter adapter;

        DrawerItem item = (DrawerItem) parent.getItemAtPosition(position);
        if (item.enabled) {
            switch (item.type) {
                case FRAGMENT:
                    // special case of RewardsFragment but not registered user not a rewards member
                    if (item.fragmentClass == RewardsFragment.class) {
                        if (loginHelper.isLoggedIn() && !loginHelper.isGuestLogin() && !ProfileDetails.isRewardsMember()) {
                            selectRewardsLinkingFragment();
                        } else {
                            selectRewardsFragment();
                        }
                    } else {
                        selectDrawerItem(item, Transition.RIGHT, true);
                    }
                    break;
                case ACCOUNT:
                    selectDrawerItem(item, Transition.RIGHT, true);
                    break;
                case PROFILE:
                    if (loginHelper.isLoggedIn() && !loginHelper.isGuestLogin()) {
                        selectProfileFragment();
                    } else {
                        selectLoginFragment();
                    }
            }
        }
    }
}

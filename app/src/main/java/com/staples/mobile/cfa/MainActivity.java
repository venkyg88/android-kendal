package com.staples.mobile.cfa;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;

import com.staples.mobile.cfa.analytics.Tracker;
import com.staples.mobile.cfa.bundle.BundleFragment;
import com.staples.mobile.cfa.cart.CartApiManager;
import com.staples.mobile.cfa.cart.CartFragment;
import com.staples.mobile.cfa.checkout.CheckoutFragment;
import com.staples.mobile.cfa.checkout.ConfirmationFragment;
import com.staples.mobile.cfa.checkout.GuestCheckoutFragment;
import com.staples.mobile.cfa.checkout.RegisteredCheckoutFragment;
import com.staples.mobile.cfa.home.ConfiguratorFragment;
import com.staples.mobile.cfa.location.LocationFinder;
import com.staples.mobile.cfa.login.LoginFragment;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.notify.IntentReceiver;
import com.staples.mobile.cfa.profile.AddressFragment;
import com.staples.mobile.cfa.profile.AddressListFragment;
import com.staples.mobile.cfa.profile.CreditCardFragment;
import com.staples.mobile.cfa.profile.CreditCardListFragment;
import com.staples.mobile.cfa.order.OrderFragment;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.cfa.profile.ProfileFragment;
import com.staples.mobile.cfa.rewards.RewardsFragment;
import com.staples.mobile.cfa.rewards.RewardsLinkingFragment;
import com.staples.mobile.cfa.search.SearchFragment;
import com.staples.mobile.cfa.sku.SkuFragment;
import com.staples.mobile.cfa.skuset.SkuSetFragment;
import com.staples.mobile.cfa.util.CurrencyFormat;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.LinearLayoutWithProgressOverlay;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.config.AppConfigurator;
import com.staples.mobile.common.access.config.StaplesAppContext;
import com.staples.mobile.common.access.configurator.model.Configurator;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.MemberDetail;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

import java.util.Date;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends Activity
                          implements View.OnClickListener, AdapterView.OnItemClickListener,
        LoginHelper.OnLoginCompleteListener, AppConfigurator.AppConfiguratorCallback{
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean LOGGING = false;

    private static final int SURRENDER_TIMEOUT = 5000;
    private static final int CONNECTIVITY_CHECK_INTERVAL = 300000; // in milliseconds (e.g. 300000=5min)

    private DrawerLayout drawerLayout;
    private ViewGroup leftDrawer;
    private ListView leftMenu;
    private DrawerAdapter leftMenuAdapter;
    private LinearLayoutWithProgressOverlay mainLayout;
    private CartFragment cartFragment;
    private DrawerItem homeDrawerItem;
    private long timeOfLastSessionCheck;
    private TextView notificationBanner;
    Animation notificationBannerAnimation;

    private LoginHelper loginHelper;
    boolean initialLoginComplete;
    boolean activityInForeground;

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
        if (LOGGING) {
            Log.v(TAG, "MainActivity:onCreate():"
                    + " bundle[" + bundle + "]");
        }
        activityInForeground = true; // need to set here in addition to in onResume since configurator callback can occur before onResume

        // Note: error handling for no network availability will happen in ensureActiveSession() called from onResume()
        if (isNetworkAvailable()) {

            boolean freshStart = (bundle == null);
            prepareMainScreen(freshStart);

            LocationFinder.getInstance(this);

            initialLoginComplete = false;
            appConfigurator = AppConfigurator.getInstance();
            appConfigurator.getConfigurator(this); // AppConfiguratorCallback
        }

        // Support for Urban Airship
        AirshipConfigOptions options = AirshipConfigOptions.loadDefaultOptions(this);
        UAirship.takeOff(getApplication(), options);
        PushManager manager = UAirship.shared().getPushManager();
        manager.setNotificationFactory(new IntentReceiver.CustomNotificationFactory(this));
        manager.setUserNotificationsEnabled(true);
    }

    @Override
    public void onNewIntent(Intent intent) {
        String action = intent.getAction();

        if (IntentReceiver.ACTION_OPEN_SKU.equals(action)) {
            String sku = intent.getStringExtra(IntentReceiver.EXTRA_SKU);
            if (sku!=null) {
                String title = intent.getStringExtra(IntentReceiver.EXTRA_TITLE);
                if (title==null) title = getResources().getString(R.string.sku_notification_title);
                selectSkuItem(title, sku, false);
            }
            return;
        }

        if (IntentReceiver.ACTION_OPEN_CATEGORY.equals(action)) {
            String identifier = intent.getStringExtra(IntentReceiver.EXTRA_IDENTIFIER);
            if (identifier!=null) {
                String title = intent.getStringExtra(IntentReceiver.EXTRA_TITLE);
                if (title==null) title = getResources().getString(R.string.sku_notification_title);
                selectBundle(title, identifier);
            }
            return;
        }

        if (IntentReceiver.ACTION_OPEN_SEARCH.equals(action)) {
            String keyword = intent.getStringExtra(IntentReceiver.EXTRA_KEYWORD);
            if (keyword!=null) {
                String title = intent.getStringExtra(IntentReceiver.EXTRA_TITLE);
                if (title==null) title = getResources().getString(R.string.sku_notification_title);
                selectSearch(keyword);
            }
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Apptentive.onStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityInForeground = true;
        Apptentive.onStart(this);
        ensureActiveSession();
        //@TODO So what happens ensure errors out! REach next line?
        //Analytics
        Tracker.getInstance().enableTracking(true); // this will be ignored if tracking not yet initialized (initialization happens after configurator completes)
    }

    @Override
    protected void onPause() {
        super.onPause();
        activityInForeground = false;
        LocationFinder locationFinder = LocationFinder.getInstance(this);
        if (locationFinder != null) {
            locationFinder.saveRecentLocation();
        }
        ActionBar actionBar = ActionBar.getInstance();
        if (actionBar != null) {
            actionBar.saveSearchHistory();
        }
        //Analytics
        Tracker.getInstance().enableTracking(false); // this will be ignored if tracking not yet initialized (initialization happens after configurator completes)
    }

    @Override
    protected void onStop() {
        super.onStop();
        Apptentive.onStop(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // if we got past configurator initialization (otherwise LoginHelper constructor throws NPE)
        if (AppConfigurator.getInstance().getConfigurator() != null) {
            // unregister loginCompleteListener
            new LoginHelper(this).unregisterLoginCompleteListener(this);
            StaplesAppContext.getInstance().resetConfigurator(); // need to reset so a fresh network attempt is made, to enable correct handling of redirect error
        }
    }

    private boolean isNetworkAvailable() {
        // first check for network connectivity
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private void showNetworkSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.error_network_connectivity);
        builder.setPositiveButton(R.string.network_settings, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                MainActivity.this.finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void ensureActiveSession() {
        // if interval has passed since last check
        final long currentTime = new Date().getTime();
        if (currentTime > timeOfLastSessionCheck + CONNECTIVITY_CHECK_INTERVAL) {
            // update time of last check even though the check may not succeed. in reality, onResume
            // can get called repeatedly in succession and we don't want to ensure active session repeatedly.
            timeOfLastSessionCheck = currentTime;

            // first check for network connectivity
            if (isNetworkAvailable()) {

                // if initial login complete, then attempt a small easyopen api call to ensure session is still active
                if (initialLoginComplete) {
                    // make call requiring secure api which will fail if expired tokens.
                    // If the session is expired, this test will give us the desired
                    // "_ERR_INVALID_COOKIE" failure regardless of guest or user session.
                    // If the session is okay, it will fail for guest session (since no profile for guest)
                    // but with a different error code, so the following logic is okay.
                    EasyOpenApi api = Access.getInstance().getEasyOpenApi(true);
                    api.getMemberProfile(new Callback<MemberDetail>() {
                        @Override public void success(MemberDetail memberDetail, Response response) {}
                        @Override public void failure(RetrofitError error) {
                            ApiError apiError = ApiError.getApiError(error);
                            if (apiError.isAuthenticationError()) {
                                // reestablish session
                                new LoginHelper(MainActivity.this).refreshSession();
                            } else if (apiError.isRedirectionError()) {
                                showErrorDialog(R.string.error_redirect, true); // setting fatal=true which will close the app
                            }
                        }
                    });
                }
            } else {
                showNetworkSettingsDialog();
            }
        }
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showErrorDialog(int msgId) {
        showErrorDialog(msgId, false);
    }
    public void showErrorDialog(String msg) {
        showErrorDialog(msg, false);
    }
    public void showErrorDialog(int msgId, boolean fatal) {
        showErrorDialog(getResources().getString(msgId), fatal);
    }
    public void showErrorDialog(String msg, final boolean fatal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);
        builder.setPositiveButton(R.string.okay_btn, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                if (fatal) {
                    MainActivity.this.finish();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showNotificationBanner(int msgId) {
        showNotificationBanner(getResources().getString(msgId));

    }
    public void showNotificationBanner(String msg) {
        notificationBanner.setText(msg);
        notificationBanner.startAnimation(notificationBannerAnimation);
    }

    private void showMainScreen() {
        findViewById(R.id.splash).setVisibility(View.GONE);
        findViewById(R.id.main).setVisibility(View.VISIBLE);
        selectDrawerItem(homeDrawerItem, Transition.NONE, false);
    }

    public void prepareMainScreen(boolean freshStart) {
        // Inflate, init ActionBar
        setContentView(R.layout.main);
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.init(this);

        // Find top-level entities
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        leftDrawer = (ViewGroup) findViewById(R.id.left_drawer);
        leftMenu = (ListView) findViewById(R.id.left_menu);
        mainLayout = (LinearLayoutWithProgressOverlay)findViewById(R.id.main);
        mainLayout.setProgressOverlay(findViewById(R.id.progress_overlay));

        // Initialize left drawer listview
        leftMenuAdapter = new DrawerAdapter(this);
        leftMenu.setAdapter(leftMenuAdapter);
        leftMenuAdapter.fill();
        leftMenu.setOnItemClickListener(this);

        // Create non-drawer DrawerItems
        homeDrawerItem = leftMenuAdapter.getItem(0); // TODO Hard-coded alias

        // Cart
        cartFragment = new CartFragment();

        // get notification banner and set up animation
        notificationBanner = (TextView) findViewById(R.id.notification_banner);
        notificationBannerAnimation = AnimationUtils.loadAnimation(this, R.anim.notification_slide_from_bottom);
        notificationBannerAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {
                notificationBanner.setVisibility(View.VISIBLE);
            }
            @Override public void onAnimationEnd(Animation animation) {
                notificationBanner.setVisibility(View.INVISIBLE);
            }
            @Override public void onAnimationRepeat(Animation animation) { }
        });

        // DLS: wait to show main screen until configurator available. Configurator fragment needs
        // profile info to be loaded before it can be displayed.
    }

    public void onGetConfiguratorResult(Configurator configurator, boolean success, RetrofitError retrofitError) {
        if (LOGGING) {
            Log.v(TAG, "MainActivity:AppConfigurator.onGetConfiguratorResult():"
                    + " success[" + success + "]"
                    + " retrofitError[" + retrofitError + "]"
                    + " configurator[" + configurator + "]"
                    + " this[" + this + "]");
        }
        // note that retrofitError may be non-null even if success==true, since config may have been
        // successfully drawn from a persisted location following a failed network attempt.
        // Regardless of success, if retrofitError not null, check for the redirect error condition
        if (retrofitError != null) {
            ApiError apiError = ApiError.getApiError(retrofitError);
            if (apiError.isRedirectionError()) {
                showErrorDialog(R.string.error_redirect, true); // setting fatal=true which will close the app
                return;
            }
        }

        // if configurator successfully retrieved (from network OR persisted file)
        if (success) {

            loginHelper = new LoginHelper(this);
            loginHelper.registerLoginCompleteListener(this);

            // do login based on persisted cache if available
            loginHelper.doCachedLogin(new ProfileDetails.ProfileRefreshCallback() {
                @Override public void onProfileRefresh(Member member, String errMsg) {
                    initialLoginComplete = true;

                    // if activity no longer showing at this point (e.g. user hit Back button during loading), then
                    // we need to kill the activity so that loading can be re-initiated next time.
                    // Otherwise, the app can get into a state where the activity is loaded, but no
                    // fragment is loaded (loading fragment when activity is paused doesn't do anything.)
                    if (!activityInForeground) {
                        MainActivity.this.finish();
                        return;
                    }

                    // open home page after profile loaded (if available) since home page now needs it
                    showMainScreen();
                }
            });

            // initialize analytics
            Tracker.getInstance().initialize(Tracker.AppType.CFA, this.getApplicationContext(),
                    configurator.getAppContext().getDev()); // allow logging only for dev environment
            // The call in onResume to enable tracking will be ignored during application creation
            // because the configurator object is not yet available. Therefore, enable here.
            Tracker.getInstance().enableTracking(true);

        } else { // can't get configurator from network or from persisted file
            showErrorDialog(R.string.error_server_connection, true); // setting fatal=true which will close the app
        }
    }

    @Override
    public void onLoginComplete(boolean guestLevel) {
        // reload cart info (even if only a guest login because needed on device rotation)
        CartApiManager.loadCart(new CartApiManager.CartRefreshCallback() {
            @Override
            public void onCartRefreshComplete(String errMsg) {
                ActionBar.getInstance().setCartCount(CartApiManager.getCartTotalItems());
            }
        });

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

        selectFragment(new ConfiguratorFragment(), Transition.NONE, true);
    }

    private void refreshMenuItemState(boolean registeredUser) {
        Resources r = getResources();
        // enable/disable left drawer menu items that depend upon login
        int itemCount = leftMenuAdapter.getCount();
        for (int position = 0; position < itemCount; position++) {
            DrawerItem item = leftMenuAdapter.getItem(position);
            if (item.fragmentClass == RewardsFragment.class || item.fragmentClass == OrderFragment.class ||
                item.fragmentClass == ProfileFragment.class) {
                item.enabled = registeredUser;
                if (item.fragmentClass == RewardsFragment.class) {
                    float rewardsTotal = 0;
                    if (registeredUser) {
                        rewardsTotal = ProfileDetails.getRewardsTotal();
                    }
                    item.additionalText = (rewardsTotal > 0)? CurrencyFormat.getFormatter().format(rewardsTotal) : null;
                }
            }
        }
        leftMenuAdapter.notifyDataSetChanged();

        // update sign-in button text
        TextView signInButton = (TextView) findViewById(R.id.account_option);
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
        ActionBar.getInstance().closeSearch();

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
            return selectFragment(fragment, Transition.NONE, true, CheckoutFragment.TAG);
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

    public boolean selectSkuItem(String title, String identifier, boolean isSkuSetOriginated) {
        SkuFragment fragment = new SkuFragment();
        fragment.setArguments(title, identifier, isSkuSetOriginated);

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

    @Override
    public void onBackPressed () {
        hideProgressIndicator();

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
                FragmentManager manager = getFragmentManager();
                Fragment checkOutFragment = manager.findFragmentByTag(CheckoutFragment.TAG);

                if (checkOutFragment != null && checkOutFragment.isVisible()) {
                    selectShoppingCart();
                } else {
                    if (manager != null) {
                        manager.popBackStack(); // this will take us back to one of the many places that could have opened this page
                    }
                }
                break;

            case R.id.back_button:
                ActionBar.getInstance().closeSearch();
                break;

            case R.id.account_option:
                if (loginHelper.isLoggedIn() && !loginHelper.isGuestLogin()) {
                    loginHelper.userSignOut();
                    selectDrawerItem(homeDrawerItem, Transition.RIGHT, true);
                } else {
                    selectLoginFragment();
                }
                break;
        }
    }

    // Left drawer listview clicks
    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        DrawerItem item = (DrawerItem) parent.getItemAtPosition(position);
        if (item.enabled) {
            Tracker.getInstance().trackActionForNavigationDrawer(item.title, ActionBar.getInstance().getPageName()); // analytics
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

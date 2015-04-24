package com.staples.mobile.cfa;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.apptentive.android.sdk.Apptentive;
import com.crittercism.app.Crittercism;
import com.staples.mobile.cfa.analytics.AdobeTracker;
import com.staples.mobile.cfa.apptentive.ApptentiveSdk;
import com.staples.mobile.cfa.bundle.BundleFragment;
import com.staples.mobile.cfa.cart.CartApiManager;
import com.staples.mobile.cfa.cart.CartFragment;
import com.staples.mobile.cfa.checkout.CheckoutFragment;
import com.staples.mobile.cfa.checkout.ConfirmationFragment;
import com.staples.mobile.cfa.checkout.GuestCheckoutFragment;
import com.staples.mobile.cfa.checkout.RegisteredCheckoutFragment;
import com.staples.mobile.cfa.feed.PersonalFeedFragment;
import com.staples.mobile.cfa.home.ConfiguratorFragment;
import com.staples.mobile.cfa.kount.KountManager;
import com.staples.mobile.cfa.location.LocationFinder;
import com.staples.mobile.cfa.login.LoginFragment;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.notify.NotifyReceiver;
import com.staples.mobile.cfa.order.OrderFragment;
import com.staples.mobile.cfa.profile.AddressFragment;
import com.staples.mobile.cfa.profile.AddressListFragment;
import com.staples.mobile.cfa.profile.CreditCardFragment;
import com.staples.mobile.cfa.profile.CreditCardListFragment;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.cfa.profile.ProfileFragment;
import com.staples.mobile.cfa.rewards.RewardsFragment;
import com.staples.mobile.cfa.rewards.RewardsLinkingFragment;
import com.staples.mobile.cfa.search.SearchFragment;
import com.staples.mobile.cfa.sku.SkuFragment;
import com.staples.mobile.cfa.skuset.SkuSetFragment;
import com.staples.mobile.cfa.store.StoreFragment;
import com.staples.mobile.cfa.UpgradeManager.UPGRADE_STATUS;
import com.staples.mobile.cfa.util.CurrencyFormat;
import com.staples.mobile.cfa.weeklyad.WeeklyAdByCategoryFragment;
import com.staples.mobile.cfa.weeklyad.WeeklyAdInStoreFragment;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.LinearLayoutWithOverlay;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.config.AppConfigurator;
import com.staples.mobile.common.access.config.StaplesAppContext;
import com.staples.mobile.common.access.configurator.model.Configurator;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.MemberDetail;
import com.staples.mobile.common.analytics.Tracker;
import com.urbanairship.AirshipConfigOptions;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends Activity
                          implements View.OnClickListener, AdapterView.OnItemClickListener,
        LoginHelper.OnLoginCompleteListener, AppConfigurator.AppConfiguratorCallback, UAirship.OnReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final boolean LOGGING = false;

    public static final String PREFS_FILENAME = "com.staples.mobile.cfa";

    private static final int CONNECTIVITY_CHECK_INTERVAL = 300000; // in milliseconds (e.g. 300000=5min)

    private DrawerLayout drawerLayout;
    private ListView leftMenu;
    private DrawerAdapter leftMenuAdapter;
    private LinearLayoutWithOverlay mainLayout;
    private CartFragment cartFragment;
    private DrawerItem homeDrawerItem;
    private long timeOfLastSessionCheck;
    private TextView notificationBanner;
    private Animation notificationBannerAnimation;

    private LoginHelper loginHelper;
    private boolean initialLoginComplete;

    private AppConfigurator appConfigurator;
    private AlertDialog upgradeDialog;

    private NetworkConnectivityBroadCastReceiver networkConnectivityBroadCastReceiver;

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
        // DLS: do NOT pass in bundle to super.onCreate!!! if super tries to restore from previous
        // state, it will try to attach fragments before our app configurator initialization completes
        // and all kinds of errors will get thrown. I'm able to cause this to happen on my API 15 HTC
        // Evo phone by turning the phone completely off while the Staples app is open, then turning
        // it back on and re-opening the Staples app.
        super.onCreate(null);
        Crittercism.leaveBreadcrumb("MainActivity:onCreate(): Entry.");
        if (LOGGING) {
            Log.v(TAG, "MainActivity:onCreate():"
                    + " bundle[" + bundle + "]");
        }

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
        UAirship.takeOff(getApplication(), options, this);

        networkConnectivityBroadCastReceiver = new NetworkConnectivityBroadCastReceiver();
    }

    public void onAirshipReady(UAirship airship) {
        PushManager manager = airship.getPushManager();
        manager.setNotificationFactory(new NotifyReceiver.CustomNotificationFactory(this));
        manager.setUserNotificationsEnabled(true);
    }

    @Override
    public void onNewIntent(Intent intent) {
        String action = intent.getAction();

        // analytics
        String userMessage = intent.getStringExtra(NotifyReceiver.EXTRA_MESSAGE);
        if (!TextUtils.isEmpty(userMessage)) {
            Tracker tracker = Tracker.getInstance();
            if (tracker.isInitialized()) {
                Tracker.getInstance().trackActionForPushMessaging(userMessage);
            }
        }

        if (NotifyReceiver.ACTION_OPEN_SKU.equals(action)) {
            String sku = intent.getStringExtra(NotifyReceiver.EXTRA_SKU);
            if (sku!=null) {
                String title = intent.getStringExtra(NotifyReceiver.EXTRA_TITLE);
                if (title==null) title = getResources().getString(R.string.sku_notification_title);
                selectSkuItem(title, sku, false);
            }
            return;
        }

        if (NotifyReceiver.ACTION_OPEN_CATEGORY.equals(action)) {
            String identifier = intent.getStringExtra(NotifyReceiver.EXTRA_IDENTIFIER);
            if (identifier!=null) {
                String title = intent.getStringExtra(NotifyReceiver.EXTRA_TITLE);
                if (title==null) title = getResources().getString(R.string.sku_notification_title);
                selectBundle(title, identifier);
            }
            return;
        }

        if (NotifyReceiver.ACTION_OPEN_SEARCH.equals(action)) {
            String keyword = intent.getStringExtra(NotifyReceiver.EXTRA_KEYWORD);
            if (keyword!=null) {
                String title = intent.getStringExtra(NotifyReceiver.EXTRA_TITLE);
                if (title==null) title = getResources().getString(R.string.sku_notification_title);
                selectSearch(title, keyword);
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
        registerReceiver(networkConnectivityBroadCastReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        //@TODO So what happens ensure errors out! REach next line?
        //Analytics
        AdobeTracker.enableTracking(true); // this will be ignored if tracking not yet initialized (initialization happens after configurator completes)
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkConnectivityBroadCastReceiver);
        LocationFinder locationFinder = LocationFinder.getInstance(this);
        if (locationFinder != null) {
            locationFinder.saveRecentLocation();
        }
        ActionBar actionBar = ActionBar.getInstance();
        if (actionBar != null) {
            actionBar.saveSearchHistory();
        }
        //Analytics
        AdobeTracker.enableTracking(false); // this will be ignored if tracking not yet initialized (initialization happens after configurator completes)
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
            loginHelper.unregisterLoginCompleteListener(this);
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
                // if wifi-only device, go to wifi settings, otherwise go to more general wireless settings
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfoMobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (networkInfoMobile == null) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                } else {
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                }
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

    @Override
    public void onAttachFragment(Fragment fragment) {
        ensureActiveSession(); // this ensures active tokens (ignored unless 5 minutes has passed)
        super.onAttachFragment(fragment);
    }

    // this receives notifications when network connectivity changes
    public class NetworkConnectivityBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true)) {
                if (!isNetworkAvailable()) {
                    showNetworkSettingsDialog();
                }
            }
        }
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
                                loginHelper.refreshSession();
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

    public void showSoftKeyboard(View view) {
        InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
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
        Apptentive.engage(this, ApptentiveSdk.HOME_CONTAINER_SHOWN_EVENT);
    }

    public void prepareMainScreen(boolean freshStart) {
        // Inflate, init ActionBar
        setContentView(R.layout.main);
        ActionBar actionBar = (ActionBar) findViewById(R.id.action_bar);
        actionBar.init(this);

        // Find top-level entities
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        leftMenu = (ListView) findViewById(R.id.left_menu);
        mainLayout = (LinearLayoutWithOverlay)findViewById(R.id.main);
        mainLayout.setOverlayView(findViewById(R.id.progress_overlay));

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

            UpgradeManager upgradeManager = new UpgradeManager(this);
            UpgradeManager.UPGRADE_STATUS upgradeStatus = upgradeManager.getUpgradeStatus();
            String upgradeMsg = upgradeManager.getUpgradeMsg();
            String upgradeUrl = upgradeManager.getUpgradeUrl();

            if (upgradeStatus == UPGRADE_STATUS.FORCE_UPGRADE) {

                doForcedUpgrade(upgradeMsg, upgradeUrl);

            } else {

                loginHelper = new LoginHelper(this);
                loginHelper.registerLoginCompleteListener(this);

                // do login based on persisted cache if available
                loginHelper.doCachedLogin(new ProfileDetails.ProfileRefreshCallback() {
                    @Override public void onProfileRefresh(Member member, String errMsg) {
                        initialLoginComplete = true;
                        showMainScreen();
                    }
                });

                // initialize analytics
                new AdobeTracker(this.getApplicationContext(), configurator.getAppContext().getDev()); // allow logging only for dev environment
                // The call in onResume to enable tracking will be ignored during application creation
                // because the configurator object is not yet available. Therefore, enable here.
                AdobeTracker.enableTracking(true);

                // set default zip code for now, update it as it changes
                Tracker.getInstance().setZipCode("02139");

                // initialize Kount fraud detection
                KountManager.getInstance(this);

                if (upgradeStatus == UPGRADE_STATUS.SUGGEST_UPGRADE) {
                    doOptionalUpgrade(upgradeMsg, upgradeUrl);
                }
            }
        } else { // can't get configurator from network or from persisted file

            showErrorDialog(R.string.error_server_connection, true); // setting fatal=true which will close the app
        }
    }

    private void doOptionalUpgrade(String upgradeMsg, final String upgradeUrl) {

        if (LOGGING) {
            Log.v(TAG, "MainActivity:doOptionalUpgrade(): Entry.");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(upgradeMsg);

        // Upgrade Button

        builder.setPositiveButton(R.string.upgrade_btn,

                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Initiate upgrade and DO NOT finish the activity.
                        launchUpgrade(upgradeUrl, false);
                    }
                });

        // Ignore Button

        builder.setNegativeButton(R.string.ignore_btn,

                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Ignore and allow user to continue.
                    }
                });

        upgradeDialog = builder.create();
        upgradeDialog.show();
    }

    private void doForcedUpgrade(String upgradeMsg, final String upgradeUrl) {

        if (LOGGING) {
            Log.v(TAG, "MainActivity:doForcedUpgrade(): Entry.");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(upgradeMsg);

        // Upgrade Button

        builder.setPositiveButton(R.string.upgrade_btn,

                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Initiate upgrade and finish the activity.
                        launchUpgrade(upgradeUrl, true);
                    }
                });

        upgradeDialog = builder.create();
        upgradeDialog.show();
    }

    private void launchUpgrade(String upgradeUrl, boolean finishActivity) {

        if (LOGGING) {
            Log.v(TAG, "MainActivity:launchUpgrade(): Entry.");
        }

        upgradeDialog.dismiss();

        Uri uriUrl = Uri.parse(upgradeUrl);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);

        if (finishActivity) {
            MainActivity.this.finish();
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
            signInButton.setText(registeredUser ? R.string.logout_title : R.string.login_title);
        }
    }

    public void showProgressIndicator() {
        mainLayout.showOverlay(true);
    }

    public void hideProgressIndicator() {
        mainLayout.showOverlay(false);
    }

    public void swallowTouchEvents(boolean swallow) {
        mainLayout.swallowTouchEvents(swallow);
    }

    // Navigation
    public boolean selectFragment(Fragment fragment, Transition transition, boolean push) {
        // Make sure all drawers are closed
        drawerLayout.closeDrawers();
        ActionBar.getInstance().closeSearch();

        String fragmentName = fragment.getClass().getSimpleName();

        // Swap Fragments
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (transition != null) transition.setAnimation(transaction);
        transaction.replace(R.id.content, fragment, fragmentName);
        if (push)
            transaction.addToBackStack(fragmentName);
        transaction.commitAllowingStateLoss();
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
        return selectFragment(cartFragment, Transition.RIGHT, true);
    }

    public boolean selectOrderCheckout(/*String deliveryRange, float couponsRewardsAmount*/) {
        if (loginHelper.isLoggedIn()) {
            CheckoutFragment fragment;
            // if logged in and have at least an address or a payment method, then use registered flow, otherwise use guest flow
            if (!loginHelper.isGuestLogin()) {
                fragment = RegisteredCheckoutFragment.newInstance();
            } else {
                fragment = GuestCheckoutFragment.newInstance();
            }
            return selectFragment(fragment, Transition.RIGHT, true);
        }
        return false;
    }

    public boolean selectOrderConfirmation(String orderNumber, String emailAddress,
                                           String deliveryRange, String total) {
        // clear the back stack immediately (not asynchronously)
        getFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // open order confirmation fragment
        Fragment fragment = ConfirmationFragment.newInstance(orderNumber, emailAddress, deliveryRange, total);
        return selectFragment(fragment, Transition.RIGHT, true);
    }

    public boolean selectStoreFinder() {
        DrawerItem drawerItem = leftMenuAdapter.findItemByClassName(StoreFragment.class.getSimpleName());
        return selectDrawerItem(drawerItem, Transition.RIGHT, true);
    }

    public boolean selectRewardsFragment() {
        return selectFragment(new RewardsFragment(), Transition.RIGHT, true);
    }

    public boolean selectRewardsLinkingFragment() {
        return selectFragment(new RewardsLinkingFragment(), Transition.RIGHT, true);
    }

    public boolean selectBundle(String title, String identifier) {
        Crittercism.leaveBreadcrumb("MainActivity:selectBundle(): Selecting a bundle by id."
            + " identifier[" + identifier + "]"
            + " title[" + title + "]"
        );
        BundleFragment fragment = new BundleFragment();
        fragment.setArguments(title, identifier);
        return(selectFragment(fragment, Transition.RIGHT, true));
    }

    public boolean selectSearch(String title, String keyword) {
        Crittercism.leaveBreadcrumb("MainActivity:selectSearch(): Searching by keyword."
            + " keyword[" + keyword + "]"
            + " title[" + title + "]"
        );
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(title, keyword);
        return(selectFragment(fragment, Transition.RIGHT, true));
    }

    public boolean selectSkuSet(String title, String identifier, String imageUrl) {
        Crittercism.leaveBreadcrumb("MainActivity:selectSkuSet(): Selecting a SKU set by identifier."
            + " identifier[" + identifier + "]"
            + " title[" + title + "]"
        );
        SkuSetFragment fragment = new SkuSetFragment();
        fragment.setArguments(title, identifier, imageUrl);
        return(selectFragment(fragment, Transition.RIGHT, true));
    }

    public boolean selectSkuItem(String title, String identifier, boolean isSkuSetOriginated) {
        Crittercism.leaveBreadcrumb("MainActivity:selectSkuItem(): Selecting a SKU item by identifier."
            + " identifier[" + identifier + "]"
            + " title[" + title + "]"
        );
        SkuFragment fragment = new SkuFragment();
        fragment.setArguments(title, identifier, isSkuSetOriginated);
        return(selectFragment(fragment, Transition.RIGHT, true));
    }

    public boolean selectWeeklyAd(String storeNo) {
        Crittercism.leaveBreadcrumb("MainActivity:selectWeeklyAd(): Selecting a weekly ad with store number."
            + " storeNo[" + storeNo + "]"
        );
        WeeklyAdByCategoryFragment fragment = new WeeklyAdByCategoryFragment();
        fragment.setArguments(storeNo);
        return(selectFragment(fragment, Transition.RIGHT, true));
    }

    public boolean selectInStoreWeeklyAd(String title, float price, String unit, String literal, String imageUrl, boolean inStoreOnly) {
        Crittercism.leaveBreadcrumb("MainActivity:selectInStoreWeeklyAd(): Selecting an in-store weekly ad."
            + " unit[" + unit + "]"
            + " title[" + title + "]"
        );
        WeeklyAdInStoreFragment fragment = new WeeklyAdInStoreFragment();
        fragment.setArguments(title, price, unit, literal, imageUrl, inStoreOnly);
        return(selectFragment(fragment, Transition.RIGHT, true));
    }

    public boolean selectProfileFragment() {
        Fragment fragment = new ProfileFragment();
        return(selectFragment(fragment, Transition.RIGHT, true));
    }

    public boolean selectLoginFragment() {
        return selectLoginFragment(false);
    }

    /**
     * @param returnToCheckout set to true when login page is called from guest checkout page
     * @return
     */
    public boolean selectLoginFragment(boolean returnToCheckout) {
        Fragment fragment = LoginFragment.newInstance(returnToCheckout);
        return(selectFragment(fragment, Transition.RIGHT, true));
    }

    public boolean selectFeedFragment() {
        DrawerItem item = leftMenuAdapter.findItemByClassName(PersonalFeedFragment.class.getSimpleName());
        return(selectDrawerItem(item, Transition.RIGHT, true));
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
        return(selectFragment(fragment, Transition.RIGHT, true));
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
        return(selectFragment(fragment, Transition.RIGHT, true));
    }

    public boolean navigateToFragment(Fragment fragment) {
        return (selectFragment(fragment, Transition.RIGHT, true));
    }

    @Override
    public void onBackPressed () {
        hideProgressIndicator();
        super.onBackPressed();
    }

    // Action bar & button clicks
    @Override
    public void onClick(View view) {

        // get current fragment name
        FragmentManager fragmentManager = getFragmentManager();
        String currentFragmentName = null;
        int currentBackStackIndex = fragmentManager.getBackStackEntryCount()-1;
        if (currentBackStackIndex >= 0) {
            currentFragmentName = fragmentManager.getBackStackEntryAt(currentBackStackIndex).getName();
        }

        switch(view.getId()) {
            case R.id.action_left_drawer:
                if (!drawerLayout.isDrawerOpen(leftMenu)) {
                    drawerLayout.openDrawer(leftMenu);
                } else drawerLayout.closeDrawers();
                break;

            case R.id.action_feed:
                selectFeedFragment();
                break;

            case R.id.continue_shopping_btn:
                selectDrawerItem(homeDrawerItem, Transition.RIGHT, true);
                break;

            case R.id.action_show_cart:
                selectShoppingCart();
                break;

            case R.id.checkout_login_button:
                selectLoginFragment(true);
                break;

            case R.id.close_button:
                hideSoftKeyboard(view);
                if (currentFragmentName != null && (currentFragmentName.equals(GuestCheckoutFragment.class.getSimpleName()) ||
                        currentFragmentName.equals(RegisteredCheckoutFragment.class.getSimpleName()))) {
                    fragmentManager.popBackStack(CartFragment.class.getSimpleName(), 0);
                } else {
                    // at the moment order confirmation is the only other fragment that uses the Close button
                    // and it has the backstack cleared upon opening, so going back one is appropriate.
                    fragmentManager.popBackStack();
                }
                break;

            case R.id.up_button:
                hideSoftKeyboard(view);
                if (!ActionBar.getInstance().closeSearch()) {

                    // Note that checkout page is handled by the close button above. If checkout had an Up
                    // button, we'd need to check for that case and have it go to Cart.

                    // see if current fragment is from the drawer menu
                    DrawerItem drawerItem = null;
                    if (currentFragmentName != null) {
                        drawerItem = leftMenuAdapter.findItemByClassName(currentFragmentName);
                    }

                    // if on page reached via drawer-menu then go to first Home fragment found in backstack
                    if (drawerItem != null) {
                        int backstackIndex = currentBackStackIndex - 1;
                        while (backstackIndex >= 0) {
                            if (currentBackStackIndex >= 0) {
                                if (ConfiguratorFragment.class.getSimpleName().equals(fragmentManager.getBackStackEntryAt(backstackIndex).getName())) {
                                    fragmentManager.popBackStack(ConfiguratorFragment.class.getSimpleName(), 0);
                                    break;
                                }
                            }
                            backstackIndex--;
                        }
                        // if no Home found in backstack, clear the backstack all the way up to landing page
                        if (backstackIndex < 0) {
                            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        }
                    } else { // otherwise Up = Back
                        fragmentManager.popBackStack();
                    }
                }
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
                    selectProfileFragment();
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

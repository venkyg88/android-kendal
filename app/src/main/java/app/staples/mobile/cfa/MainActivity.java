package app.staples.mobile.cfa;

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
import android.content.UriMatcher;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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

import com.adobe.mobile.Config;
import com.apptentive.android.sdk.Apptentive;
import com.crittercism.app.Crittercism;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.config.AppConfigurator;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.member.Member;
import com.staples.mobile.common.access.easyopen.model.member.MemberDetail;
import com.staples.mobile.common.analytics.Tracker;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

import java.util.ArrayList;
import java.util.Date;

import app.staples.R;
import app.staples.mobile.cfa.UpgradeManager.UPGRADE_STATUS;
import app.staples.mobile.cfa.apptentive.ApptentiveSdk;
import app.staples.mobile.cfa.bundle.BundleFragment;
import app.staples.mobile.cfa.cart.CartApiManager;
import app.staples.mobile.cfa.cart.CartFragment;
import app.staples.mobile.cfa.checkout.CheckoutFragment;
import app.staples.mobile.cfa.checkout.ConfirmationFragment;
import app.staples.mobile.cfa.checkout.GuestCheckoutFragment;
import app.staples.mobile.cfa.checkout.RegisteredCheckoutFragment;
import app.staples.mobile.cfa.home.HomeFragment;
import app.staples.mobile.cfa.kount.KountManager;
import app.staples.mobile.cfa.location.LocationFinder;
import app.staples.mobile.cfa.login.LoginFragment;
import app.staples.mobile.cfa.login.LoginHelper;
import app.staples.mobile.cfa.notify.NotifyReceiver;
import app.staples.mobile.cfa.profile.AddressFragment;
import app.staples.mobile.cfa.profile.AddressListFragment;
import app.staples.mobile.cfa.profile.CreditCardFragment;
import app.staples.mobile.cfa.profile.CreditCardListFragment;
import app.staples.mobile.cfa.profile.ProfileDetails;
import app.staples.mobile.cfa.profile.ProfileFragment;
import app.staples.mobile.cfa.rewards.RewardsFragment;
import app.staples.mobile.cfa.rewards.RewardsLinkingFragment;
import app.staples.mobile.cfa.search.SearchFragment;
import app.staples.mobile.cfa.sku.SkuFragment;
import app.staples.mobile.cfa.skuset.SkuSetFragment;
import app.staples.mobile.cfa.util.MiscUtils;
import app.staples.mobile.cfa.weeklyad.WeeklyAdByCategoryFragment;
import app.staples.mobile.cfa.weeklyad.WeeklyAdInStoreFragment;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.LinearLayoutWithOverlay;
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

    private static class QueuedTransaction { // TODO This is only public for test!
        enum Type {POPONE, POPNAME, POPALL, PUSH};

        private Type type;
        private String name;
        private Fragment fragment;
        private Transition transition;
    }

    public enum Transition {
        NONE (0, 0, 0, 0, 0),
        RIGHT(0, R.animator.right_push_enter, R.animator.right_push_exit, R.animator.right_pop_enter, R.animator.right_pop_exit),
        UP   (0, R.animator.up_push_enter, R.animator.up_push_exit, R.animator.up_pop_enter, R.animator.up_pop_exit);

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
            else if (push_enter!=0 && push_exit!=0 && pop_enter!=0 && pop_exit!=0) {
                transaction.setCustomAnimations(push_enter, push_exit, pop_enter, pop_exit);
            }
        }
    }

    public static final String SCHEME    = "http";
    public static final String AUTHORITY = "staples.com";

    private static final UriMatcher uriMatcher;
    private static final int MATCH_HOME     = 1;
    private static final int MATCH_SKU      = 2;
    private static final int MATCH_CATEGORY = 3;
    private static final int MATCH_SEARCH   = 4;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "cfa/home",       MATCH_HOME);
        uriMatcher.addURI(AUTHORITY, "cfa/sku/*",      MATCH_SKU);
        uriMatcher.addURI(AUTHORITY, "cfa/category/*", MATCH_CATEGORY);
        uriMatcher.addURI(AUTHORITY, "cfa/search/*",   MATCH_SEARCH);
    }

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

    private AlertDialog upgradeDialog;

    private ArrayList<QueuedTransaction> queuedTransactions;

    private NetworkConnectivityBroadCastReceiver networkConnectivityBroadCastReceiver;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        try {
            Crittercism.initialize(getApplicationContext(), FlavorSpecific.CRITTERCISM_ID);
            Crittercism.leaveBreadcrumb("MainActivty:onCreate(): Crittercism initialized.");
        } catch (Exception exception) {}

        // DebugUtil.setStrictMode();

        //noinspection ResourceType
        setRequestedOrientation(getResources().getInteger(R.integer.screenOrientation));

        prepareMainScreen();
        queuedTransactions = new ArrayList<QueuedTransaction>();

        // Analytics
        Config.setContext(this);
        Tracker.getInstance().initialize(Tracker.AppType.CFA);


        // Support for Urban Airship
//        AirshipConfigOptions options = AirshipConfigOptions.loadDefaultOptions(this); TODO Enable Urban Airship when we're ready
//        UAirship.takeOff(getApplication(), options, this);

        networkConnectivityBroadCastReceiver = new NetworkConnectivityBroadCastReceiver();
    }

    public void onAirshipReady(UAirship airship) {
        PushManager manager = airship.getPushManager();
        manager.setNotificationFactory(new NotifyReceiver.CustomNotificationFactory(this));
        manager.setUserNotificationsEnabled(true);
    }

    // Intent handling for notifications & deep links

    @Override
    public void onNewIntent(Intent intent) {
        if (intent==null || !initialLoginComplete) {
            // Do nothing
        } else if (handleCustomIntent(intent)) {
            setIntent(null);
        } else {
            ViewGroup content = (ViewGroup) findViewById(R.id.content);
            if (content!=null && content.getChildCount()==0) {
                selectDrawerItem(homeDrawerItem, Transition.NONE);
            }
            setIntent(null);
        }
    }

    private boolean handleCustomIntent(Intent intent) {
        if (intent==null) return(false);
        Uri uri = intent.getData();
        if (uri==null) return(false);
        if (!SCHEME.equals(uri.getScheme())) return(false);

        int match = uriMatcher.match(uri);
        switch(match) {
            case MATCH_HOME:
                selectDrawerItem(homeDrawerItem, Transition.NONE);
                return(true);
            case MATCH_SKU:
                String sku = uri.getPathSegments().get(2);
                selectSkuItem("Product item", sku, false);
                return(true);
            case MATCH_CATEGORY:
                String identifier = uri.getPathSegments().get(2);
                selectBundle("Category", identifier);
                return(true);
            case MATCH_SEARCH:
                String keyword = uri.getPathSegments().get(2);
                selectSearch("Search", keyword);
                return(true);
        }
        return(false);
    }

    // Activity life cycle methods

    @Override
    protected void onStart() {
        super.onStart();
        Apptentive.onStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // load Configurator
        AppConfigurator appConfigurator = AppConfigurator.getInstance();
        appConfigurator.loadConfigurator(this, FlavorSpecific.MCS_SERVER, FlavorSpecific.MCS_TAG, this);

        registerReceiver(networkConnectivityBroadCastReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        executeQueuedTransactions();

        //Analytics
        Config.collectLifecycleData();
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
        Config.pauseCollectingLifecycleData();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Apptentive.onStop(this);
    }

    @Override
    protected void onDestroy() {
        // if we got past configurator initialization (otherwise LoginHelper constructor throws NPE)
        if (AppConfigurator.getInstance().getConfigurator() != null) {
            // unregister loginCompleteListener
            if (loginHelper != null) {
                loginHelper.unregisterLoginCompleteListener(this);
            }
        }
        super.onDestroy();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] infos = cm.getAllNetworkInfo();
        if (infos==null) return(false);
        for(NetworkInfo info : infos) {
            if (info.isConnected()) return(true);
        }
        return(false);
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
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // note that user may go fix the network settings and return before hitting this
                // button, so test again for availability before exiting
                if (!isNetworkAvailable()) {
                    MainActivity.this.finish();
                }
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

    public void hideSoftKeyboard() {
        View view = getCurrentFocus();
        if (view!=null) {
            InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
        onNewIntent(getIntent());
        Apptentive.engage(this, ApptentiveSdk.HOME_CONTAINER_SHOWN_EVENT);
    }

    public void prepareMainScreen() {
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

        // Get home drawer item
        homeDrawerItem = leftMenuAdapter.findItemByTag(DrawerItem.HOME);

        // Cart
        cartFragment = new CartFragment();

        // get notification banner and set up animation
        notificationBanner = (TextView) findViewById(R.id.notification_banner);
        notificationBannerAnimation = AnimationUtils.loadAnimation(this, R.anim.notification_slide_from_bottom);
        notificationBannerAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                notificationBanner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                notificationBanner.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        // DLS: wait to show main screen until configurator available. Configurator fragment needs
        // profile info to be loaded before it can be displayed.
    }

    private Fragment getTopFragment() {
        ViewGroup content = (ViewGroup) findViewById(R.id.content);
        if (content==null || content.getChildCount()==0) return(null);
        View page = content.getChildAt(0);
        Object tag = page.getTag();
        if (tag instanceof Fragment) return((Fragment) tag);
        return(null);
    }

    @Override
    public void onGetConfiguratorResult(AppConfigurator.Status status, RetrofitError retrofitError) {
        Log.d(TAG, "AppConfigurator.load "+status);
        if (status!= AppConfigurator.Status.NOQUERY) {
            Crittercism.leaveBreadcrumb("AppConfigurator.load returned "+status);
        }

        // Show errors
        if (retrofitError != null) {
            ApiError apiError = ApiError.getApiError(retrofitError);
            if (apiError.isRedirectionError()) {
                showErrorDialog(R.string.error_redirect, true); // setting fatal=true which will close the app
                return;
            }
        }

        if (status==AppConfigurator.Status.FAILURE) {
            showErrorDialog(R.string.error_server_connection, true); // setting fatal=true which will close the app
            return;
        }

        UpgradeManager upgradeManager = new UpgradeManager(this);
        UpgradeManager.UPGRADE_STATUS upgradeStatus = upgradeManager.getUpgradeStatus();
        String upgradeMsg = upgradeManager.getUpgradeMsg();
        String upgradeUrl = upgradeManager.getUpgradeUrl();

        if (upgradeStatus == UPGRADE_STATUS.FORCE_UPGRADE) {
            doForcedUpgrade(upgradeMsg, upgradeUrl);
            return;
        }

        if (!initialLoginComplete ||
            status==AppConfigurator.Status.STARTUP ||
            status==AppConfigurator.Status.CHANGED ||
            status==AppConfigurator.Status.FALLBACK) {
            loginHelper = new LoginHelper(this);
            loginHelper.registerLoginCompleteListener(this);

            // do login based on persisted cache if available
            loginHelper.doCachedLogin(new ProfileDetails.ProfileRefreshCallback() {
                @Override
                public void onProfileRefresh(Member member, String errMsg) {
                    initialLoginComplete = true;
                    showMainScreen();
                }
            });

            // Enable analytics debugging in dev
            if (AppConfigurator.getInstance().getConfigurator().getAppContext().isDev()) {
                Config.setDebugLogging(true);
            }

            // set default zip code for now, update it as it changes (via LocationFinder below)
            Tracker.getInstance().setZipCode("02139");

            // initialize location finder (this will update zip code if possible)
            LocationFinder.getInstance(this);

            // initialize Kount fraud detection
            KountManager.getInstance(this);

            if (upgradeStatus == UPGRADE_STATUS.SUGGEST_UPGRADE) {
                doOptionalUpgrade(upgradeMsg, upgradeUrl);
            }
        } else showMainScreen();

        // Refresh HomeFragment if necessary
        if (status==AppConfigurator.Status.CHANGED) {
            Fragment top = getTopFragment();
            if (top instanceof HomeFragment) {
                ((HomeFragment) top).refreshPage();
            }
        }
    }

    public void onNearbyStore() {
        // Refresh HomeFragment header if necessary
        Fragment top = getTopFragment();
        if (top instanceof HomeFragment) {
            ((HomeFragment) top).refreshNearbyStore();
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
        updateMenuItemState();
    }

    @Override
    public void onLogoutComplete() {
        // if cart not empty, reset it
        if (CartApiManager.getCart() != null && CartApiManager.getCartTotalItems() > 0) {
            CartApiManager.resetCart();
            ActionBar.getInstance().setCartCount(0);
        }

        // disable menu items as appropriate
        updateMenuItemState();

        // Update header or switch to home page
        Fragment top = getTopFragment();
        if (top instanceof HomeFragment) {
            ((HomeFragment) top).updateMessageBar();
        } else {
            selectFragment(DrawerItem.HOME, new HomeFragment(), Transition.RIGHT);
        }
    }

    private void updateMenuItemState() {
        Access access = Access.getInstance();
        boolean registeredUser = access.isLoggedIn() && !access.isGuestLogin();
        Resources res = getResources();
        // enable/disable left drawer menu items that depend upon login
        int itemCount = leftMenuAdapter.getCount();
        for (int position = 0; position < itemCount; position++) {
            DrawerItem item = leftMenuAdapter.getItem(position);
            switch(item.tag) {
                case DrawerItem.ACCOUNT:
                    item.enabled = registeredUser;
                    item.extra = res.getString(registeredUser ? R.string.logout_title : R.string.login_title);
                    break;
                case DrawerItem.REWARDS:
                    item.enabled = registeredUser;
                    if (registeredUser) {
                        float rewards = ProfileDetails.getRewardsTotal();
                        if (rewards > 0f)
                            item.extra = MiscUtils.getCurrencyFormat().format(rewards);
                        else item.extra = null;
                    } else item.extra = null;
                    break;
                case DrawerItem.ORDERS:
                case DrawerItem.PROFILE:
                    item.enabled = registeredUser;
                    break;
            }
        }
        leftMenuAdapter.notifyDataSetChanged();
    }

    public void showProgressIndicator() {
        if (mainLayout != null) {
            mainLayout.showOverlay(true);
        }
    }

    public void hideProgressIndicator() {
        if (mainLayout != null) {
            mainLayout.showOverlay(false);
        }
    }

    public void swallowTouchEvents(boolean swallow) {
        if (mainLayout != null) {
            mainLayout.swallowTouchEvents(swallow);
        }
    }

    /** FragmentManager primitives
     * All fragment navigation should be funneled through here
     */

    public boolean popBackStack() {
        QueuedTransaction qt = new QueuedTransaction();
        qt.type = QueuedTransaction.Type.POPONE;
        queuedTransactions.add(qt);
        return(executeQueuedTransactions());
    }

    public boolean popBackStack(String name) {
        QueuedTransaction qt = new QueuedTransaction();
        qt.type = QueuedTransaction.Type.POPNAME;
        qt.name = name;
        queuedTransactions.add(qt);
        return(executeQueuedTransactions());
    }

    public boolean clearBackStack() {
        QueuedTransaction qt = new QueuedTransaction();
        qt.type = QueuedTransaction.Type.POPALL;
        queuedTransactions.add(qt);
        return(executeQueuedTransactions());
    }

    public boolean selectFragment(String name, Fragment fragment, Transition transition) {
        // Close everything
        drawerLayout.closeDrawers();
        hideSoftKeyboard();
        ActionBar.getInstance().closeSearch();

        QueuedTransaction qt = new QueuedTransaction();
        qt.type = QueuedTransaction.Type.PUSH;
        qt.name = name;
        qt.fragment = fragment;
        qt.transition = transition;
        queuedTransactions.add(qt);
        return(executeQueuedTransactions());
    }

    private boolean executeQueuedTransactions() {
        FragmentManager fm = getFragmentManager();
        for(;;) {
            if (queuedTransactions.size()==0) return(true);
            QueuedTransaction qt=queuedTransactions.get(0);

            try {
                switch(qt.type) {
                    case POPONE:
                        fm.popBackStack();
                        break;
                    case POPNAME:
                        fm.popBackStack(qt.name, 0);
                        break;
                    case POPALL:
                        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        break;
                    case PUSH:
                        ViewGroup content = (ViewGroup) findViewById(R.id.content);
                        boolean empty = (content==null || content.getChildCount()==0);
                        // Check for duplicates
                        if (!empty) {
                            View page = content.getChildAt(0);
                            Object tag = page.getTag();
                            if (tag!=null && tag.getClass()==qt.fragment.getClass()) {
                                break; // skip transaction
                            }
                        }
                        // Perform transaction
                        FragmentTransaction transaction = fm.beginTransaction();
                        if (!empty && qt.transition!=null) {
                            qt.transition.setAnimation(transaction);
                        }
                        transaction.replace(R.id.content, qt.fragment);
                        if (!empty) {
                            transaction.addToBackStack(qt.name);
                        }
                        transaction.commit();
                        break;
                }
            } catch(IllegalStateException e) {
                return(false);
            }

            queuedTransactions.remove(0);
        }
    }

    // Higher level navigation methods

    private boolean selectDrawerItem(DrawerItem item, Transition transition) {
        // Safety check
        if (item == null || item.fragmentClass == null) return (false);

        // Create Fragment if necessary
        if (item.fragment == null)
            item.instantiate(this);
        return(selectFragment(item.tag, item.fragment, transition));
    }

    public boolean selectShoppingCart() {
        return selectFragment(DrawerItem.CART, cartFragment, Transition.RIGHT);
    }

    public boolean selectOrderCheckout(/*String deliveryRange, float couponsRewardsAmount*/) {
        if (loginHelper.isLoggedIn()) {
            CheckoutFragment fragment;
            // if logged in and have at least an address or a payment method, then use registered flow, otherwise use guest flow
            if (!loginHelper.isGuestLogin()) {
                fragment = RegisteredCheckoutFragment.newInstance();
                return selectFragment(DrawerItem.REG_CHECKOUT, fragment, Transition.RIGHT);
            } else {
                fragment = GuestCheckoutFragment.newInstance();
                return selectFragment(DrawerItem.GUEST_CHECKOUT, fragment, Transition.RIGHT);
            }
        }
        return false;
    }

    public boolean selectOrderConfirmation(String orderNumber, String emailAddress,
                                           String deliveryRange, String total) {
        // clear the back stack
        clearBackStack();

        // open order confirmation fragment
        Fragment fragment = ConfirmationFragment.newInstance(orderNumber, emailAddress, deliveryRange, total);
        return selectFragment(DrawerItem.CONFIRM, fragment, Transition.RIGHT);
    }

    public boolean selectStoreFinder() {
        DrawerItem drawerItem = leftMenuAdapter.findItemByTag(DrawerItem.STORE);
        return selectDrawerItem(drawerItem, Transition.RIGHT);
    }

    public boolean selectRewardsFragment() {
        return selectFragment(DrawerItem.REWARDS, new RewardsFragment(), Transition.RIGHT);
    }

    public boolean selectRewardsLinkingFragment() {
        return selectFragment(DrawerItem.LINK, new RewardsLinkingFragment(), Transition.RIGHT);
    }

    public boolean selectBundle(String title, String identifier) {
        Crittercism.leaveBreadcrumb("MainActivity:selectBundle(): Selecting a bundle by id."
            + " identifier[" + identifier + "]"
            + " title[" + title + "]"
        );
        BundleFragment fragment = new BundleFragment();
        fragment.setArguments(title, identifier);
        return(selectFragment(DrawerItem.BUNDLE, fragment, Transition.RIGHT));
    }

    public boolean selectSearch(String title, String keyword) {
        Crittercism.leaveBreadcrumb("MainActivity:selectSearch(): Searching by keyword."
            + " keyword[" + keyword + "]"
            + " title[" + title + "]"
        );
        SearchFragment fragment = new SearchFragment();
        fragment.setArguments(title, keyword);
        return(selectFragment(DrawerItem.SEARCH, fragment, Transition.RIGHT));
    }

    public boolean selectSkuSet(String title, String identifier, String imageUrl) {
        Crittercism.leaveBreadcrumb("MainActivity:selectSkuSet(): Selecting a SKU set by identifier."
            + " identifier[" + identifier + "]"
            + " title[" + title + "]"
        );
        SkuSetFragment fragment = new SkuSetFragment();
        fragment.setArguments(title, identifier, imageUrl);
        return(selectFragment(DrawerItem.SKUSET, fragment, Transition.RIGHT));
    }

    public boolean selectSkuItem(String title, String identifier, boolean isSkuSetOriginated) {
        Crittercism.leaveBreadcrumb("MainActivity:selectSkuItem(): Selecting a SKU item by identifier."
            + " identifier[" + identifier + "]"
            + " title[" + title + "]"
        );
        SkuFragment fragment = new SkuFragment();
        fragment.setArguments(title, identifier, isSkuSetOriginated);
        return(selectFragment(DrawerItem.SKU, fragment, Transition.RIGHT));
    }

    public boolean selectWeeklyAd(String storeNo) {
        Crittercism.leaveBreadcrumb("MainActivity:selectWeeklyAd(): Selecting a weekly ad with store number."
            + " storeNo[" + storeNo + "]"
        );
        WeeklyAdByCategoryFragment fragment = new WeeklyAdByCategoryFragment();
        fragment.setArguments(storeNo);
        return(selectFragment(DrawerItem.WEEKLY, fragment, Transition.RIGHT));
    }

    public boolean selectInStoreWeeklyAd(String title, float price, String unit, String literal, String imageUrl, boolean inStoreOnly) {
        Crittercism.leaveBreadcrumb("MainActivity:selectInStoreWeeklyAd(): Selecting an in-store weekly ad."
            + " unit[" + unit + "]"
            + " title[" + title + "]"
        );
        WeeklyAdInStoreFragment fragment = new WeeklyAdInStoreFragment();
        fragment.setArguments(title, price, unit, literal, imageUrl, inStoreOnly);
        return(selectFragment(DrawerItem.SALES, fragment, Transition.RIGHT));
    }

    public boolean selectProfileFragment() {
        Fragment fragment = new ProfileFragment();
        return(selectFragment(DrawerItem.PROFILE, fragment, Transition.RIGHT));
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
        return(selectFragment(DrawerItem.LOGIN, fragment, Transition.RIGHT));
    }

    public boolean selectFeedFragment() {
        DrawerItem item = leftMenuAdapter.findItemByTag(DrawerItem.FEED);
        return(selectDrawerItem(item, Transition.RIGHT));
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
        return(selectFragment(DrawerItem.ADDRESS, fragment, Transition.RIGHT));
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
        return(selectFragment(DrawerItem.CARD, fragment, Transition.RIGHT));
    }

    @Override
    public void onBackPressed () {
        hideProgressIndicator();

        // HACK: if going BACK to guest checkout pop an additional time
        FragmentManager fragmentManager = getFragmentManager();
        int currentBackStackIndex = fragmentManager.getBackStackEntryCount()-1;
        if (currentBackStackIndex >= 2  &&
                DrawerItem.GUEST_CHECKOUT.equals(fragmentManager.getBackStackEntryAt(currentBackStackIndex - 1).getName())) {
            popBackStack(fragmentManager.getBackStackEntryAt(currentBackStackIndex - 2).getName());
        } else {
            super.onBackPressed();
        }
    }

    // Action bar & button clicks
    @Override
    public void onClick(View view) {

        // get current fragment name
        FragmentManager fragmentManager = getFragmentManager();
        String currentTag = null;
        int currentBackStackIndex = fragmentManager.getBackStackEntryCount()-1;
        if (currentBackStackIndex >= 0) {
            currentTag = fragmentManager.getBackStackEntryAt(currentBackStackIndex).getName();
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
                selectDrawerItem(homeDrawerItem, Transition.RIGHT);
                break;

            case R.id.action_show_cart:
                selectShoppingCart();
                break;

            case R.id.checkout_login_button:
                selectLoginFragment(true);
                break;

            case R.id.close_button:
                hideSoftKeyboard();
                if (currentTag != null && (currentTag.equals(DrawerItem.REG_CHECKOUT) ||
                        currentTag.equals(DrawerItem.GUEST_CHECKOUT))) {
                    popBackStack(DrawerItem.CART);
                } else {
                    // at the moment order confirmation is the only other fragment that uses the Close button
                    // and it has the backstack cleared upon opening, so going back one is appropriate.
                    popBackStack();
                }
                break;

            case R.id.up_button:
                hideSoftKeyboard();
                if (!ActionBar.getInstance().closeSearch()) {

                    // Note that checkout page is handled by the close button above. If checkout had an Up
                    // button, we'd need to check for that case and have it go to Cart.

                    // see if current fragment is from the drawer menu
                    DrawerItem drawerItem = null;
                    if (currentTag != null) {
                        drawerItem = leftMenuAdapter.findItemByTag(currentTag);
                    }

                    // if on search results or page reached via drawer-menu then go to first Home fragment found in backstack
                    if (drawerItem != null || DrawerItem.SEARCH.equals(currentTag) || DrawerItem.LOGIN.equals(currentTag)) {
                        int backstackIndex = currentBackStackIndex - 1;
                        while (backstackIndex >= 0) {
                            if (DrawerItem.HOME.equals(fragmentManager.getBackStackEntryAt(backstackIndex).getName())) {
                                fragmentManager.popBackStack(DrawerItem.HOME, 0);
                                break;
                            }
                            backstackIndex--;
                        }
                        // if no Home found in backstack, clear the backstack all the way up to landing page
                        if (backstackIndex < 0) {
                            clearBackStack();
                        }
                    } else { // otherwise Up = Back
                        popBackStack();
                    }
                }
                break;

            case R.id.extra:
                if (loginHelper.isLoggedIn() && !loginHelper.isGuestLogin()) {
                    loginHelper.userSignOut();
                    selectDrawerItem(homeDrawerItem, Transition.RIGHT);
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
            switch(item.tag) {
                case DrawerItem.ACCOUNT:
                    selectProfileFragment();
                    break;
                case DrawerItem.REWARDS:
                    if (loginHelper.isLoggedIn() && !loginHelper.isGuestLogin() && !ProfileDetails.isRewardsMember()) {
                        selectRewardsLinkingFragment();
                    } else {
                        selectRewardsFragment();
                    }
                    break;
                case DrawerItem.PROFILE:
                    if (loginHelper.isLoggedIn() && !loginHelper.isGuestLogin()) {
                        selectProfileFragment();
                    } else {
                        selectLoginFragment();
                    }
                    break;
                default:
                    selectDrawerItem(item, Transition.RIGHT);
                    break;
            }
        }
    }
}

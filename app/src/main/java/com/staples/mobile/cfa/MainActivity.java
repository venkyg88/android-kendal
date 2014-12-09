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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.staples.mobile.cfa.bundle.BundleFragment;
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
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.cfa.profile.ProfileFragment;
import com.staples.mobile.cfa.search.SearchBarView;
import com.staples.mobile.cfa.search.SearchFragment;
import com.staples.mobile.cfa.sku.SkuFragment;
import com.staples.mobile.cfa.widget.BadgeImageView;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.LinearLayoutWithProgressOverlay;

public class MainActivity extends Activity
                          implements View.OnClickListener, AdapterView.OnItemClickListener, LoginHelper.OnLoginCompleteListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int SURRENDER_TIMEOUT = 5000;

    private DrawerLayout drawerLayout;
    private View leftDrawerAction;
    private View leftDrawer;
    private TextView titleView;
    private SearchBarView searchBar;
    private ImageView optionIcon;
    private View.OnClickListener optionListener;
    private BadgeImageView cartIconAction;
    private LinearLayoutWithProgressOverlay mainLayout;
    private CartFragment cartFragment;
    private TextView cartQtyView;
    private Button checkoutSigninButton;
    private View closeButton;
    private DrawerItem homeDrawerItem;

    private LoginHelper loginHelper;

    public enum Transition {
        NONE  (0, 0, 0, 0, 0),
        SLIDE (0, R.animator.push_enter, R.animator.push_exit, R.animator.pop_enter, R.animator.pop_exit),
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

//        String zipCode = LocationService.getCachedZipCode(this.getApplicationContext());
//        if (zipCode == null) {
//            LocationService userLocationService = new LocationService(this.getApplicationContext(), this);
//            userLocationService.getUserLocation();
//        }

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

    @Override
    protected void onPause() {
        super.onPause();
        LocationFinder.getInstance(this).saveRecentLocation();
        searchBar.saveSearchHistory();
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

    public void showActionBar(int titleId, int iconId, View.OnClickListener listener) {
        if (titleId==0) titleView.setVisibility(View.GONE);
        else
        {
            titleView.setVisibility(View.VISIBLE);
            titleView.setText(titleId);
        }

        if (iconId==0) {
            optionIcon.setVisibility(View.GONE);
            optionListener = null;
        } else {
            optionIcon.setVisibility(View.VISIBLE);
            optionIcon.setImageResource(iconId);
            optionListener = listener;
        }
    }

    public void showActionBar() {
        showActionBar(R.string.staples, R.drawable.ic_search_white, searchBar);
    }

    /** sets standard action bar, fragments need to override their onResume methods to set up action bar */
    public void showStandardActionBar() {
        // set standard title bar
        showActionBar();

        // show standard entities
        leftDrawerAction.setVisibility(View.VISIBLE);
        searchBar.setVisibility(View.GONE);
        optionIcon.setVisibility(View.VISIBLE);
        cartIconAction.setVisibility(View.VISIBLE);

        // hide non-standard entities
        cartQtyView.setVisibility(View.GONE);
        checkoutSigninButton.setVisibility(View.GONE);
        closeButton.setVisibility(View.GONE);
    }

    public void showCartActionBarEntities() {
        // show cart-specific entities
        leftDrawerAction.setVisibility(View.VISIBLE);
        cartQtyView.setVisibility(View.VISIBLE);
        // hide unwanted entities
        searchBar.setVisibility(View.GONE);
        optionIcon.setVisibility(View.GONE);
        cartIconAction.setVisibility(View.GONE);
        checkoutSigninButton.setVisibility(View.GONE);
        closeButton.setVisibility(View.GONE);
    }

    public void showCheckoutActionBarEntities() {
        // show checkout-specific entities
        closeButton.setVisibility(View.VISIBLE);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectShoppingCart();
            }
        });
        LoginHelper loginHelper = new LoginHelper(this);
        if (loginHelper.isLoggedIn() && loginHelper.isGuestLogin()) {
            checkoutSigninButton.setVisibility(View.VISIBLE);
        } else {
            checkoutSigninButton.setVisibility(View.GONE);
        }
        // hide unwanted entities
        leftDrawerAction.setVisibility(View.GONE);
        searchBar.setVisibility(View.GONE);
        optionIcon.setVisibility(View.GONE);
        cartIconAction.setVisibility(View.GONE);
        cartQtyView.setVisibility(View.GONE);
    }

    public void showOrderConfirmationActionBarEntities() {
        // show cart-specific entities
        leftDrawerAction.setVisibility(View.VISIBLE);
        cartQtyView.setVisibility(View.VISIBLE);
        // hide unwanted entities
        searchBar.setVisibility(View.GONE);
        optionIcon.setVisibility(View.GONE);
        cartIconAction.setVisibility(View.GONE);
        cartQtyView.setVisibility(View.GONE);
    }

    /** sets action bar cart quantity */
    public void setActionBarCartQty(String qtyText) {
        cartQtyView.setText(qtyText);
    }

    public void prepareMainScreen(boolean freshStart) {
        // Inflate
        setContentView(R.layout.main);

        // Find top-level entities
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        leftDrawer = findViewById(R.id.left_drawer);
        titleView = (TextView) findViewById(R.id.title);
        searchBar = (SearchBarView) findViewById(R.id.search_text);
        optionIcon = (ImageView) findViewById(R.id.option_icon);
        cartIconAction = (BadgeImageView)findViewById(R.id.action_show_cart);
        leftDrawerAction = findViewById(R.id.action_left_drawer);
        cartQtyView = (TextView)findViewById(R.id.cart_item_qty);
        checkoutSigninButton = (Button)findViewById(R.id.co_signin_button);
        closeButton = findViewById(R.id.close_button);

        mainLayout = (LinearLayoutWithProgressOverlay)findViewById(R.id.main);
        mainLayout.setCartProgressOverlay(findViewById(R.id.progress_overlay));


        // Set action bar listeners
        leftDrawerAction.setOnClickListener(this);
        optionIcon.setOnClickListener(this);
        cartIconAction.setOnClickListener(this);
        checkoutSigninButton.setOnClickListener(this);


        // initialize action bar
        showStandardActionBar();

        // Init search bar
        searchBar.initSearchBar();

        // Initialize left drawer listview
        DataWrapper wrapper = (DataWrapper) findViewById(R.id.left_drawer);
        ListView menu = (ListView) wrapper.findViewById(R.id.menu);
        DrawerAdapter adapter = new DrawerAdapter(this, wrapper);
        menu.setAdapter(adapter);
        adapter.fill();
        menu.setOnItemClickListener(this);

        // Create non-drawer DrawerItems
        homeDrawerItem = adapter.getItem(0); // TODO Hard-coded alias

        // Cart
        cartFragment = new CartFragment();
        updateCartIcon(0); // initialize cart item count to 0 until we're able to fill the cart

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

    @Override
    public void onLoginComplete(boolean guestLevel) {
        // load cart info after successful login (if registered login or if guest login following a signout where cart was non-empty)
        if (!guestLevel || (guestLevel && cartFragment.getCart() != null && cartFragment.getCart().getTotalItems() > 0)) {
            cartFragment.refreshCart(MainActivity.this);
        }

        // for faster debugging with registered user (automatic login), uncomment this and use your
        // own credentials, but re-comment out before checking code in
//        if (guestLevel) {
//            new LoginHelper(this).getUserTokens("testuser2", "password");
//        }
    }

    public void showProgressIndicator() {
        mainLayout.getProgressIndicator().showProgressIndicator();
    }

    public void hideProgressIndicator() {
        mainLayout.getProgressIndicator().hideProgressIndicator();
    }


    // Navigation

    public boolean selectFragment(Fragment fragment, Transition transition, boolean push) {
        // Make sure all drawers are closed
        drawerLayout.closeDrawers();

        // Swap Fragments
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (transition!=null) transition.setAnimation(transaction);
        transaction.replace(R.id.content, fragment);
        if (push)
            transaction.addToBackStack(null);
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

    public boolean selectOrderCheckout() {
        LoginHelper loginHelper = new LoginHelper(this);
        if (loginHelper.isLoggedIn()) {
            CheckoutFragment fragment;
            float couponsRewardsAmount = cartFragment.getCouponsRewardsAdjustedAmount();
            // if logged in and have at least an address or a payment method, then use registered flow, otherwise use guest flow
            if (!loginHelper.isGuestLogin() && (ProfileDetails.hasAddress() || ProfileDetails.hasPaymentMethod())) {
                fragment = RegisteredCheckoutFragment.newInstance(couponsRewardsAmount,
                        cartFragment.getCart().getSubTotal(), cartFragment.getCart().getPreTaxTotal());
            } else {
                fragment = GuestCheckoutFragment.newInstance(couponsRewardsAmount,
                        cartFragment.getCart().getSubTotal(), cartFragment.getCart().getPreTaxTotal());
            }
            return selectFragment(fragment, Transition.NONE, true);
        }
        return false;
    }

    public boolean selectOrderConfirmation(String orderId, String orderNumber) {
        // refresh cart since should now be empty
        cartFragment.refreshCart(this);
        // open order confirmation fragment
        Fragment fragment = ConfirmationFragment.newInstance(orderId, orderNumber);
        return selectFragment(fragment, Transition.SLIDE, true);
    }

    public boolean selectBundle(String title, String path) {
        Fragment fragment = Fragment.instantiate(this, BundleFragment.class.getName());
        Bundle args = new Bundle();
        if (path != null) args.putString("path", path);
        fragment.setArguments(args);
        return (selectFragment(fragment, Transition.SLIDE, true));
    }

    public boolean selectSearch(String keyword) {
        Fragment fragment = Fragment.instantiate(this, SearchFragment.class.getName());
        Bundle args = new Bundle();
        if (keyword!=null) args.putString("identifier", keyword);
        fragment.setArguments(args);
        return (selectFragment(fragment, Transition.SLIDE, true));
    }

    public boolean selectSkuItem(String identifier) {
        Fragment fragment = Fragment.instantiate(this, SkuFragment.class.getName());
        Bundle args = new Bundle();
        if (identifier!=null) args.putString("identifier", identifier);
        fragment.setArguments(args);
        return (selectFragment(fragment, Transition.SLIDE, true));
    }

    public boolean selectProfileFragment() {
        Fragment fragment = Fragment.instantiate(this, ProfileFragment.class.getName());
        return (selectFragment(fragment, Transition.NONE, true));
    }

    public boolean selectLoginFragment() {
        Fragment fragment = Fragment.instantiate(this, LoginFragment.class.getName());
        return (selectFragment(fragment, Transition.NONE, true));
    }

    /** opens the profile addresses fragment */
    public boolean selectProfileAddressesFragment() {
        return selectProfileAddressesFragment(null, null);
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
        return navigateToFragment(fragment);
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
        return navigateToFragment(fragment);
    }

    public boolean navigateToFragment(Fragment fragment) {
        return (selectFragment(fragment, Transition.NONE, true));
    }

    /** Sets item count indicator on cart icon */
    public void updateCartIcon(int totalItemCount) {
        cartIconAction.setText(totalItemCount == 0 ? null : Integer.toString(totalItemCount));
    }

    /** Adds an item to the cart */
    public void addItemToCart(String partNumber, int qty) {
        cartFragment.addToCart(partNumber, qty, this);
    }

    // Action bar & topper clicks

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

            case R.id.option_icon:
                if (optionListener!=null)
                    optionListener.onClick(view);
                break;

            case R.id.action_show_cart:
                selectShoppingCart();
                break;

            case R.id.co_signin_button:
                selectLoginFragment();
                break;
        }
    }

    public void signInBtnClick(View view) {
        Button accountBtn = (Button)view;
        String buttonText = accountBtn.getText().toString();

        if(buttonText.equals("Sign In")){
            selectLoginFragment();
        }
        if(buttonText.equals("Sign Out")){
            loginHelper.userSignOut();
            selectDrawerItem(homeDrawerItem, Transition.NONE, true);
            accountBtn.setText("Sign In");
        }
    }

    // Left drawer listview clicks

    @Override
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        DrawerAdapter adapter;

        DrawerItem item = (DrawerItem) parent.getItemAtPosition(position);
        switch(item.type) {
            case FRAGMENT:
            case ACCOUNT:
                drawerLayout.closeDrawers();
                selectDrawerItem(item, Transition.SLIDE, true);
                break;

            case BROWSE:
                adapter = (DrawerAdapter) parent.getAdapter();
                adapter.setBrowseMode(true);
                break;

            case BACKTOTOP:
                adapter = (DrawerAdapter) parent.getAdapter();
                adapter.setBrowseMode(false);
                break;

            case STACK:
                adapter = (DrawerAdapter) parent.getAdapter();
                adapter.popStack(item);
                break;

            case CATEGORY:
                String identifier = item.getBundleIdentifier();
                if (identifier!=null) {
                    drawerLayout.closeDrawers();
                    selectBundle(item.title, item.path);
                } else {
                    adapter = (DrawerAdapter) parent.getAdapter();
                    adapter.pushStack(item);
                }
                break;

            case PROFILE:
                if(loginHelper.isLoggedIn() && !loginHelper.isGuestLogin()) {
                    selectProfileFragment();
                } else {
                    selectLoginFragment();
                }
        }
    }
}

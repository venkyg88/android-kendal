package com.staples.mobile.cfa;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.staples.mobile.R;
import com.staples.mobile.cfa.bundle.BundleFragment;
import com.staples.mobile.cfa.cart.CartAdapter;
import com.staples.mobile.cfa.checkout.CheckoutFragment;
import com.staples.mobile.cfa.checkout.ConfirmationFragment;
import com.staples.mobile.cfa.login.LoginFragment;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.profile.ProfileDetails;
import com.staples.mobile.cfa.profile.ProfileFragment;
import com.staples.mobile.cfa.widget.LinearLayoutWithProgressOverlay;
import com.staples.mobile.cfa.search.SearchBarView;
import com.staples.mobile.cfa.search.SearchFragment;
import com.staples.mobile.cfa.sku.SkuFragment;
import com.staples.mobile.cfa.widget.BadgeImageView;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.common.access.configurator.model.Configurator;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.access.lms.LmsManager;

import java.text.NumberFormat;

public class MainActivity extends Activity
                          implements View.OnClickListener, AdapterView.OnItemClickListener, LoginHelper.OnLoginCompleteListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int SURRENDER_TIMEOUT = 5000;

    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    private DrawerLayout drawerLayout;
    private View leftDrawer;
    private View rightDrawer;
    private SearchBarView searchBar;
    private BadgeImageView rightDrawerAction;
    private TextView cartTitle;
    private TextView cartSubtotal;
    private TextView cartFreeShippingMsg;
    private TextView cartShipping;
    private View cartProceedToCheckout;
    private View cartSubtotalLayout;
    private CartAdapter cartAdapter;

    private DrawerItem homeDrawerItem;
    private DrawerItem storeDrawerItem;
    private DrawerItem rewardsDrawerItem;
    
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

    public void prepareMainScreen(boolean freshStart) {
        // Inflate
        setContentView(R.layout.main);

        // Find top-level entities
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        leftDrawer = findViewById(R.id.left_drawer);
        rightDrawer = findViewById(R.id.right_drawer);
        searchBar = (SearchBarView) findViewById(R.id.search_text);
        rightDrawerAction = (BadgeImageView)findViewById(R.id.action_right_drawer);

        // Set action bar listeners
        findViewById(R.id.action_left_drawer).setOnClickListener(this);
        findViewById(R.id.action_home).setOnClickListener(this);
        findViewById(R.id.action_right_drawer).setOnClickListener(this);

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
        storeDrawerItem = new DrawerItem(DrawerItem.Type.FRAGMENT, this, R.drawable.logo, R.string.store_info_title, ToBeDoneFragment.class);
        rewardsDrawerItem = adapter.getItem(6); // TODO Hard-coded alias

        // Cart
        cartTitle = (TextView) rightDrawer.findViewById(R.id.cart_title);
        cartFreeShippingMsg = (TextView) rightDrawer.findViewById(R.id.free_shipping_msg);
        cartShipping = (TextView) rightDrawer.findViewById(R.id.cart_shipping);
        cartSubtotal = (TextView) rightDrawer.findViewById(R.id.cart_subtotal);
        cartSubtotalLayout = rightDrawer.findViewById(R.id.cart_subtotal_layout);
        cartProceedToCheckout = rightDrawer.findViewById(R.id.action_checkout);
        cartProceedToCheckout.setOnClickListener(this);

        // Initialize right drawer cart listview
        LinearLayoutWithProgressOverlay cartContainer = (LinearLayoutWithProgressOverlay) rightDrawer.findViewById(R.id.right_drawer_content);
        cartContainer.setCartProgressOverlay(rightDrawer.findViewById(R.id.cart_progress_overlay));
        cartAdapter = new CartAdapter(this, R.layout.cart_item, cartContainer.getProgressIndicator());
        cartAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateCartIndicators(cartAdapter.getCart());
            }
        });
        updateCartIndicators(null); // initialize cart display until we're able to fill the cart (e.g. item count to zero)
        ((ListView) rightDrawer.findViewById(R.id.cart_list)).setAdapter(cartAdapter);

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
        // load cart drawer (requires successful login)
        cartAdapter.fill();

        // for faster debugging with registered user (automatic login), uncomment this and use your
        // own credentials, but re-comment out before checking code in
//        if (guestLevel) {
//            new LoginHelper(this).getUserTokens("<user>", "<password>");
//        }
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

//    // dls: considering whether to support multiple fragments
//    public boolean selectFragments(List<Fragment> fragments, Transition transition, boolean push) {
//        // Make sure all drawers are closed
//        drawerLayout.closeDrawers();
//
//        // Swap fragments
//        FragmentManager manager = getFragmentManager();
//        FragmentTransaction transaction = manager.beginTransaction();
//        for (int i=0;  i < fragments.size();  i++) {
//            Fragment fragment = fragments.get(i);
//            if (i == 0) {
//                transaction.replace(R.id.content, fragment);
//            } else {
//                transaction.add(R.id.content, fragment);
//            }
//        }
//        if (transition!=null) {
//            transition.setAnimation(transaction);
//        }
//        if (push) {
//            transaction.addToBackStack(null);
//        }
//        transaction.commit();
//        return(true);
//    }

    private boolean selectDrawerItem(DrawerItem item, Transition transition, boolean push) {
        // Safety check
        if (item == null || item.fragmentClass == null) return (false);

        // Create Fragment if necessary
        if (item.fragment == null)
            item.instantiate(this);
        return(selectFragment(item.fragment, transition, push));
    }


    public boolean selectOrderCheckout() {
        LoginHelper loginHelper = new LoginHelper(this);
        if (loginHelper.isLoggedIn()) {
            // if guest or if no profile info, then go to single-page checkout, otherwise open checkout with profile editing options
            CheckoutFragment fragment = CheckoutFragment.newInstance(cartAdapter.getExpectedDeliveryRange(),
                        cartAdapter.getCart().getSubTotal(), cartAdapter.getCart().getPreTaxTotal(),
                        !loginHelper.isGuestLogin() && ProfileDetails.hasAddressOrPaymentMethod());
            return selectFragment(fragment, Transition.NONE, true);
        }
        return false;
    }

    public boolean selectOrderConfirmation(String orderId, String orderNumber) {
        // refresh cart since should now be empty
        cartAdapter.fill();
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
        return (selectFragment(fragment, Transition.SLIDE, true));
    }

    public boolean selectLoginFragment() {
        Fragment fragment = Fragment.instantiate(this, LoginFragment.class.getName());
        return (selectFragment(fragment, Transition.SLIDE, true));
    }

    public boolean navigateToFragment(Fragment fragment) {
        return (selectFragment(fragment, Transition.SLIDE, true));
    }

    /** Sets item count indicator on cart icon and cart drawer title */
    public void updateCartIndicators(Cart cart) {
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
            LmsManager lmsManager = new LmsManager();
            Configurator configurator = lmsManager.getConfigurator();
            if (configurator != null) {
                freeShippingThreshold = configurator.getPromotions().getFreeShippingThreshold().floatValue();
            }
        }

        // set text of cart icon badge
        rightDrawerAction.setText(totalItemCount == 0 ? null : Integer.toString(totalItemCount));

        // Set text of cart drawer title
        if (totalItemCount==0) {
            cartTitle.setText(r.getString(R.string.your_cart));
        } else {
            cartTitle.setText(r.getQuantityString(R.plurals.your_cart, totalItemCount, totalItemCount));
        }

        // set text of free shipping msg
        if (freeShippingThreshold > subtotal && !"Free".equals(shipping) && !ProfileDetails.isRewardsMember()) {
            // need to spend more to qualify for free shipping
            cartFreeShippingMsg.setText(String.format(r.getString(R.string.free_shipping_msg1),
                    currencyFormat.format(freeShippingThreshold), currencyFormat.format(freeShippingThreshold - subtotal)));
            cartFreeShippingMsg.setBackgroundColor(0xff3f6fff); // blue
        } else {
            // qualifies for free shipping
            cartFreeShippingMsg.setText(R.string.free_shipping_msg2);
            cartFreeShippingMsg.setBackgroundColor(0xff00ff00); // green
        }

        // set text of shipping and subtotal
        cartShipping.setText(CheckoutFragment.formatShippingCharge(shipping, currencyFormat));
        cartSubtotal.setText(currencyFormat.format(preTaxSubtotal));

        // only show shipping, subtotal, and proceed-to-checkout when at least one item
        cartFreeShippingMsg.setVisibility(totalItemCount == 0? View.GONE : View.VISIBLE);
        cartSubtotalLayout.setVisibility(totalItemCount == 0? View.GONE : View.VISIBLE);
        cartProceedToCheckout.setVisibility(totalItemCount == 0? View.GONE : View.VISIBLE);
    }

    /** Adds an item to the cart */
    public void addItemToCart(String partNumber, int qty) {
        cartAdapter.addToCart(partNumber, qty);
        drawerLayout.openDrawer(rightDrawer);
    }

    // Action bar & topper clicks

    @Override
    public void onClick(View view) {

        switch(view.getId()) {
            case R.id.action_left_drawer:
                if (!drawerLayout.isDrawerOpen(leftDrawer)) {
                    drawerLayout.closeDrawer(rightDrawer);
                    drawerLayout.openDrawer(leftDrawer);
                } else drawerLayout.closeDrawers();
                break;

            case R.id.continue_shopping_btn:
            case R.id.action_home:
                selectDrawerItem(homeDrawerItem, Transition.NONE, true);
                break;

            case R.id.action_right_drawer:
                if (!drawerLayout.isDrawerOpen(rightDrawer)) {
                    drawerLayout.closeDrawer(leftDrawer);
                    drawerLayout.openDrawer(rightDrawer);
                } else drawerLayout.closeDrawers();
                break;

            case R.id.action_checkout:
                selectOrderCheckout();
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

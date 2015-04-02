package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.common.analytics.Tracker;
import com.staples.mobile.cfa.search.SearchBarView;

public class ActionBar extends LinearLayout {
    private static final String TAG = "ActionBar";

    public enum Config {

        //        back   drawer logo   feed,  search qty    cart   signin close  title
        ABOUT    (false, true,  true,  false, false, false, true,  false, false, R.string.about_title),
        ADDCARD  (true,  false, false, false, true,  false, true,  false, false, R.string.add_card_title),
        ADDRESS  (true,  false, false, false, true,  false, true,  false, false, R.string.address_title),
        BROWSE   (false, true,  true,  false, true,  false, true,  false, false, 0),
        BUNDLE   (true,  false, false, true,  true,  false, true,  false, false, 0),
        CART     (true,  false, false, false, false, true,  false, false, false, R.string.cart_title),
        COGUEST  (true,  false, false, false, false, false, false, true,  false, R.string.guest_checkout_title),
        CONFIRM  (true,  false, false, false, false, false, true,  false, false, R.string.order_confirm_title),
        COREG    (true,  false, false, false, false, false, false, false, false, R.string.checkout_title),
        DEFAULT  (false, true,  true,  true,  true,  false, true,  false, false, 0),
        FEED     (false, true,  false, false, true,  false, true,  false, false, R.string.personal_feed_title),
        LINK     (false, true,  false, false, true,  false, true,  false, false, R.string.link_rewards_title),
        LOGIN    (false, true,  false, false, true,  false, true,  false, false, R.string.login_title),
        NOTIFY   (true,  false, true,  true,  true, false,  true,  false, false, 0),
        ORDER    (false, true,  false, false, true,  false, true,  false, false, R.string.order_title),
        PASSWORD (false, true,  false, false, true,  false, true,  false, false, R.string.password_reset),
        PROFILE  (false, true,  false, false, true,  false, true,  false, false, R.string.profile_title),
        QUERY    (true,  false, false, false, true,  false, true,  false, false, 0),
        REWARDS  (false, true,  false, false, true,  false, true,  false, false, R.string.rewards_title),
        SEARCH   (true,  false, false, false, true,  false, true,  false, false, 0),
        SKU      (false, false, false, false, true,  false, true,  false, false, 0),
        SKUSET   (false, false, false, false, false, false, false, false, false, R.string.sku_title),
        STORE    (true,  false, false, false, false, false, true,  false, false, R.string.store_locator_title),
        VIEWCARD (true,  false, false, false, true,  false, true,  false, false, R.string.credit_card_title),
        WEEKLYAD (true,  false, false, false, true,  false, true,  false, false, R.string.weekly_ad_title);

        private boolean back;
        private boolean drawer;
        private boolean logo;
        private boolean feed;
        private boolean search;
        private boolean quantity;
        private boolean cart;
        private boolean signin;
        private boolean close;
        private int title;

        Config(boolean back, boolean drawer, boolean logo, boolean feed, boolean search,
               boolean quantity, boolean cart, boolean signin, boolean close, int title) {
            this.back = back;
            this.drawer = drawer;
            this.logo = logo;
            this.feed = feed;
            this.search = search;
            this.quantity = quantity;
            this.cart = cart;
            this.signin = signin;
            this.close = close;
            this.title = title;
        }
    }

    private class State {
        private Config config;
        private String title;
        private int icon;
        private OnClickListener listener;

        private void copy(State other) {
            if (other==null) return;
            config = other.config;
            title = other.title;
            icon = other.icon;
            listener = other.listener;
        }
    }

    private static ActionBar instance;

    private State state;
    private State pushed;

    private ImageView backButton;
    private ImageView leftDrawerAction;
    private ImageView logoView;
    private ImageView feedIcon;
    private SearchBarView searchBar;
    private TextView cartQtyView;
    private BadgeImageView cartIconAction;
    private Button checkoutSigninButton;
    private ImageView optionIcon;
    private ImageView closeButton;
    private TextView titleView;

    public static ActionBar getInstance() {
        return(instance);
    }

    public ActionBar(Context context) {
        this(context, null, 0);
    }

    public ActionBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        instance = this;
        state = new State();
    }

    public void init(OnClickListener listener) {
        // Find elements
        backButton = (ImageView) findViewById(R.id.back_button);
        leftDrawerAction = (ImageView) findViewById(R.id.action_left_drawer);
        logoView = (ImageView) findViewById(R.id.action_logo);
        feedIcon = (ImageView) findViewById(R.id.action_feed);
        searchBar = (SearchBarView) findViewById(R.id.search_view);
        cartQtyView = (TextView) findViewById(R.id.cart_item_qty);
        cartIconAction = (BadgeImageView) findViewById(R.id.action_show_cart);
        checkoutSigninButton = (Button) findViewById(R.id.checkout_login_button);
        optionIcon = (ImageView) findViewById(R.id.option_icon);
        closeButton = (ImageView) findViewById(R.id.close_button);
        titleView = (TextView) findViewById(R.id.action_title);

        // Set icon listeners
        backButton.setOnClickListener(listener);
        leftDrawerAction.setOnClickListener(listener);
        feedIcon.setOnClickListener(listener);
        cartIconAction.setOnClickListener(listener);
        checkoutSigninButton.setOnClickListener(listener);
        closeButton.setOnClickListener(listener);
    }

    // Configuration setters

    public void setConfig(Config config) {
        searchBar.close();
        state.config = config;
        state.title = null;
        state.icon = 0;
        state.listener = null;
        update();
    }

    public void setConfig(Config config, String title) {
        searchBar.close();
        state.config = config;
        state.title = title;
        state.icon = 0;
        state.listener = null;
        update();
    }

    public void setConfig(Config config, int icon, OnClickListener listener) {
        searchBar.close();
        state.config = config;
        state.title = null;
        state.icon = icon;
        state.listener = listener;
        update();
    }

    /** needed for analytics */
    public String getPageName() {
        String pageName = null;
        if (state.config != null) {
            if (state.config.title > 0) {
                pageName = getResources().getString(state.config.title);
            } else {
                switch (state.config) {
                    case BROWSE:  pageName = "Browse"; break;
                    case BUNDLE:  pageName = "Bundle"; break;
                    case SKU:     pageName = "SKU"; break;
                    case DEFAULT: pageName = "Home"; break;
                    case QUERY:   pageName = "Search"; break;
                    case SEARCH:  pageName = "Search Results"; break;
                }
            }
        }
        return pageName;
    }

    private void update() {
        if (state.config==null) return;

        closeButton.setVisibility(state.config.close ? VISIBLE : GONE);
        backButton.setVisibility(state.config.back ? VISIBLE : GONE);
        leftDrawerAction.setVisibility(state.config.drawer ? VISIBLE : GONE);
        cartIconAction.setVisibility(state.config.cart ? VISIBLE : GONE);
        checkoutSigninButton.setVisibility(state.config.signin ? VISIBLE : GONE);
        cartQtyView.setVisibility(state.config.quantity ? VISIBLE : GONE);

        // Set option icon
        if (state.icon!=0 && state.listener!=null) {
            optionIcon.setVisibility(VISIBLE);
            optionIcon.setImageResource(state.icon);
            optionIcon.setOnClickListener(state.listener);
        } else {
            optionIcon.setVisibility(GONE);
            optionIcon.setOnClickListener(null);
        }

        searchBar.setVisibility(state.config.search ? VISIBLE : GONE);
        feedIcon.setVisibility(state.config.feed ? VISIBLE : GONE);
        logoView.setVisibility(state.config.logo ? VISIBLE : GONE);

        // Set title
        if (state.title!=null) {
            titleView.setVisibility(VISIBLE);
            titleView.setText(state.title);
        } else if (state.config.title!=0) {
            titleView.setVisibility(VISIBLE);
            titleView.setText(state.config.title);
        } else {
            titleView.setVisibility(GONE);
        }
    }

    public void openSearch() {
        if (pushed==null) pushed = new State();
        pushed.copy(state);

        state.config = Config.QUERY;
        state.title = null;
        state.icon = 0;
        state.listener = null;
        update();

        searchBar.open();

        Tracker.getInstance().trackStateForSearchBar(); // analytics
    }

    public boolean closeSearch() {
        if (!searchBar.close()) return(false);

        if (pushed!=null) {
            state.copy(pushed);
            update();
        }
        return(true);
    }

    public void setCartCount(int count) {
        cartIconAction.setText(count==0 ? null : Integer.toString(count));
        if (count==0) cartQtyView.setText(null);
        else cartQtyView.setText(getResources().getQuantityString(R.plurals.cart_qty, count, count));
    }

    public void saveSearchHistory() {
        searchBar.saveSearchHistory();
    }

    // Measurement & layout

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        // Get dimensions
        int layoutWidth = MeasureSpec.getSize(widthSpec);
        int layoutHeight = MeasureSpec.getSize(heightSpec);
        int slackWidth = layoutWidth-getPaddingLeft()-getPaddingRight();
        int usedHeight = 0;

        // Iterate over children
        int n = getChildCount();
        for(int i=0;i<n;i++) {
            View child = getChildAt(i);
            if (child.getVisibility()!=View.GONE) {
                // Measure child
                LayoutParams params = (LayoutParams) child.getLayoutParams();
                slackWidth -= params.leftMargin+params.rightMargin;
                child.measure(MeasureSpec.makeMeasureSpec(slackWidth, MeasureSpec.AT_MOST),
                              MeasureSpec.makeMeasureSpec(layoutHeight, MeasureSpec.AT_MOST));
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight()+params.topMargin+params.bottomMargin;

                // Calculate usage
                slackWidth -= childWidth;
                usedHeight = Math.max(usedHeight, childHeight);
            }
        }

        // Adjust for padding and return
        usedHeight += getPaddingTop()+getPaddingBottom();
        setMeasuredDimension(layoutWidth, resolveSize(usedHeight, heightSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // Get relative dimensions
        right -= left;
        bottom -= top;

        // Adjust for padding
        left = getPaddingLeft();
        top = getPaddingTop();
        right -= getPaddingRight();
        bottom -= getPaddingBottom();

        // Iterate over children
        int n = getChildCount();
        for(int i=0;i<n;i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                int x, y;
                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();
                LayoutParams params = (LayoutParams) child.getLayoutParams();

                // Horizontal positioning
                switch(params.gravity&Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.RIGHT:
                        x = right-width-params.rightMargin;
                        right = x-params.leftMargin;
                        break;
                    case Gravity.CENTER_HORIZONTAL: // Only a last child can be centered
                        left += params.leftMargin;
                        right -= params.rightMargin;
                        x = (left+right-width)/2;
                        break;
                    default:
                        x = left+params.leftMargin;
                        left = x+width+params.rightMargin;
                        break;
                }

                // Vertical centering
                switch(params.gravity&Gravity.VERTICAL_GRAVITY_MASK) {
                    case Gravity.BOTTOM:
                        y = bottom;
                        break;
                    case Gravity.CENTER_VERTICAL:
                        y = (bottom-top-height)/2;
                        break;
                    default:
                        y = top;
                }

                child.layout(x, y, x+width, y+height);
            }
        }
    }
}

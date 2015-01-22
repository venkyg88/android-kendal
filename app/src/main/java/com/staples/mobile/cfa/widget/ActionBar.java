package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.search.SearchBarView;

public class ActionBar extends LinearLayout {
    private static final String TAG = "ActionBar";

    public enum Config {
<<<<<<< HEAD
        //        close, back,  draw,  logo,  srch,  qty,   cart,  login, title,                         option
        ABOUT    (false, false, true,  true,  false, false, true,  false, R.string.about_title,          0),
        ADDCARD  (false, false, true,  false, true,  false, true,  false, R.string.add_card_title,       0),
        ADDRESS  (false, false, true,  false, true,  false, true,  false, R.string.address_title,        0),
        BROWSE   (false, false, true,  true,  true,  false, true,  false, 0,                             0),
        BUNDLE   (false, false, true,  false, true,  false, true,  false, 0,                             0),
        CART     (false, false, true,  false, false, true,  false, false, R.string.cart_title,           0),
        COGUEST  (true,  false, false, false, false, false, false, true,  R.string.guest_checkout_title, 0),
        CONFIRM  (false, false, true,  false, false, false, true,  false, R.string.order_confirm_title,  0),
        COREG    (true,  false, false, false, false, false, false, false, R.string.checkout_title,       0),
        DEFAULT  (false, false, true,  true,  true,  false, true,  false, 0,                             0),
        FEED     (false, false, true,  false, true,  false, true,  false, R.string.personal_feed_title,  0),
        LINK     (false, false, true,  false, true,  false, true,  false, R.string.link_rewards_title,   0),
        LOGIN    (false, false, true,  false, true,  false, true,  false, R.string.login_title,          0),
        MAPVIEW  (false, false, true,  false, false, false, true,  false, R.string.store_locator_title,  R.drawable.ic_map_white),
        MAPLIST  (false, false, true,  false, false, false, true,  false, R.string.store_locator_title,  R.drawable.ic_view_list_white),
        ORDER    (false, false, true,  false, true,  false, true,  false, R.string.order_title,          0),
        PASSWORD (false, false, true,  false, true,  false, true,  false, R.string.password_reset,       0),
        PROFILE  (false, false, true,  false, true,  false, true,  false, R.string.profile_title,        0),
        QUERY    (false, true,  false, false, true,  false, false, false, 0,                             0),
        REWARDS  (false, false, true,  false, true,  false, true,  false, R.string.rewards_title,        0),
        SEARCH   (false, false, true,  true,  true,  false, true,  false, 0,                             0),
        SKU      (false, false, true,  false, true,  false, true,  false, 0,                             0),
        SKUSET   (false, false, true,  false, true,  false, true,  false, 0,                             0),
        VIEWCARD (false, false, true,  false, true,  false, true,  false, R.string.credit_card_title,    0);
=======
        //        close, drawer, logo,  search, quantity, cart,  signin, title,                         option
        ABOUT    (false, true,   true,  false,  false,    true,  false,  R.string.about_title,          0),
        ADDCARD  (false, true,   false, true,   false,    true,  false,  R.string.add_card_title,       0),
        ADDRESS  (false, true,   false, true,   false,    true,  false,  R.string.address_title,        0),
        BROWSE   (false, true,   true,  true,   false,    true,  false,  0,                             0),
        BUNDLE   (false, true,   false, true,   false,    true,  false,  0,                             0),
        CART     (false, true,   false, false,  true,     false, false,  R.string.cart_title,           0),
        COGUEST  (true,  false,  false, false,  false,    false, true,   R.string.guest_checkout_title, 0),
        CONFIRM  (false, true,   false, false,  false,    true,  false,  R.string.order_confirm_title,  0),
        COREG    (true,  false,  false, false,  false,    false, false,  R.string.checkout_title,       0),
        DEFAULT  (false, true,   true,  true,   false,    true,  false,  0,                             0),
        FEED     (false, true,   false, true,   false,    true,  false,  R.string.personal_feed_title,  0),
        LINK     (false, true,   false, true,   false,    true,  false,  R.string.link_rewards_title,   0),
        LOGIN    (false, true,   false, true,   false,    true,  false,  R.string.login_title,          0),
        MAPVIEW  (false, true,   false, false,  false,    true,  false,  R.string.store_locator_title,  R.drawable.ic_map_white),
        MAPLIST  (false, true,   false, false,  false,    true,  false,  R.string.store_locator_title,  R.drawable.ic_view_list_white),
        ORDER    (false, true,   false, true,   false,    true,  false,  R.string.order_title,          0),
        PASSWORD (false, true,   false, true,   false,    true,  false,  R.string.password_reset,       0),
        PROFILE  (false, true,   false, true,   false,    true,  false,  R.string.profile_title,        0),
        REWARDS  (false, true,   false, true,   false,    true,  false,  R.string.rewards_title,        0),
        SEARCH   (false, true,   true,  true,   false,    true,  false,  0,                             0),
        SKU      (false, true,   false, true,   false,    true,  false,  0,                             0),
        SKUSET   (false, true,   false, true,   false,    true,  false,  0,                             0),
        VIEWCARD (false, true,   false, true,   false,    true,  false,  R.string.credit_card_title,    0),
        UPDATECARD  (false, true,   false, true,   false,    true,  false,  R.string.edit_credit_card_title,       0);
>>>>>>> credit card ui simplification

        private boolean close;
        private boolean back;
        private boolean drawer;
        private boolean logo;
        private boolean search;
        private boolean quantity;
        private boolean cart;
        private boolean signin;
        private int title;
        private int option;

        private Config(boolean close, boolean back, boolean drawer, boolean logo, boolean search,
                       boolean quantity, boolean cart, boolean signin, int title, int option) {
            this.close = close;
            this.back = back;
            this.drawer = drawer;
            this.logo = logo;
            this.search = search;
            this.quantity = quantity;
            this.cart = cart;
            this.signin = signin;
            this.title = title;
            this.option = option;
        }
    }

    private static ActionBar instance;

    private ImageView closeButton;
    private ImageView backButton;
    private ImageView leftDrawerAction;
    private BadgeImageView cartIconAction;
    private Button checkoutSigninButton;
    private TextView cartQtyView;
    private ImageView optionIcon;
    private SearchBarView searchBar;
    private ImageView logoView;
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
    }

    public void init(OnClickListener listener) {
        // Find elements
        closeButton = (ImageView) findViewById(R.id.close_button);
        backButton = (ImageView) findViewById(R.id.back_button);
        leftDrawerAction = (ImageView) findViewById(R.id.action_left_drawer);
        cartIconAction = (BadgeImageView) findViewById(R.id.action_show_cart);
        checkoutSigninButton = (Button) findViewById(R.id.co_signin_button);
        cartQtyView = (TextView) findViewById(R.id.cart_item_qty);
        optionIcon = (ImageView) findViewById(R.id.option_icon);
        searchBar = (SearchBarView) findViewById(R.id.search_view);
        logoView = (ImageView) findViewById(R.id.action_logo);
        titleView = (TextView) findViewById(R.id.action_title);

        // Set icon listeners
        closeButton.setOnClickListener(listener);
        backButton.setOnClickListener(listener);
        leftDrawerAction.setOnClickListener(listener);
        cartIconAction.setOnClickListener(listener);
        checkoutSigninButton.setOnClickListener(listener);
    }

    // Configuration setters

    public void setConfig(Config config) {
        setConfig(config, null, null);
    }

    public void setConfig(Config config, String title) {
        setConfig(config, null, title);
    }

    public void setConfig(Config config, OnClickListener listener) {
        setConfig(config, listener, null);
    }

    public void setConfig(Config config, OnClickListener listener, String title) {
        if (config==null) return;

        closeButton.setVisibility(config.close ? VISIBLE : GONE);
        backButton.setVisibility(config.back ? VISIBLE : GONE);
        leftDrawerAction.setVisibility(config.drawer ? VISIBLE : GONE);
        cartIconAction.setVisibility(config.cart ? VISIBLE : GONE);
        checkoutSigninButton.setVisibility(config.signin ? VISIBLE : GONE);
        cartQtyView.setVisibility(config.quantity ? VISIBLE : GONE);
        if (config.option==0) {
            optionIcon.setVisibility(GONE);
            optionIcon.setOnClickListener(null);
        }
        else {
            optionIcon.setVisibility(VISIBLE);
            optionIcon.setImageResource(config.option);
            optionIcon.setOnClickListener(listener);
        }
        searchBar.setVisibility(config.search ? VISIBLE : GONE);
        logoView.setVisibility(config.logo ? VISIBLE : GONE);
        if (title!=null) {
            titleView.setVisibility(VISIBLE);
            titleView.setText(title);
        }
        else if (config.title==0) {
            titleView.setVisibility(GONE);
        }
        else {
            titleView.setVisibility(VISIBLE);
            titleView.setText(config.title);
        }
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

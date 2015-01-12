package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.staples.mobile.cfa.R;

public class ActionBar extends LinearLayout {
    private static final String TAG = "ActionBar";

    public enum Config {
        //       close, drawer, logo,  title,                        search, quantity, option,                        cart,  signin
        DEFAULT (false, true,   true,  0,                            true,   false,    0,                             true,  false),
        MAPVIEW (false, true,   true,  R.string.store_locator_title, false,  false,    R.drawable.ic_map_white,       true,  false),
        MAPLIST (false, true,   true,  R.string.store_locator_title, false,  false,    R.drawable.ic_view_list_white, true,  false);

        private boolean close;
        private boolean drawer;
        private boolean logo;
        private int title;
        private boolean search;
        private boolean quantity;
        private int option;
        private boolean cart;
        private boolean signin;

        private Config(boolean close, boolean drawer, boolean logo,  int title, boolean search,
                       boolean quantity, int option, boolean cart, boolean signin) {
            this.close = close;
            this.drawer = drawer;
            this.logo = logo;
            this.title = title;
            this.search = search;
            this.quantity = quantity;
            this.option = option;
            this.cart = cart;
            this.signin = signin;
        }
    }

    private static ActionBar instance;

    private ImageView closeButton;
    private ImageView leftDrawerAction;
    private BadgeImageView cartIconAction;
    private Button checkoutSigninButton;
    private TextView cartQtyView;
    private ImageView optionIcon;
    private SearchView searchView;
    private ImageView logoView;
    private TextView titleView;

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

    public static ActionBar getInstance() {
        return(instance);
    }

    public void findElements() {
        closeButton = (ImageView) findViewById(R.id.close_button);
        leftDrawerAction = (ImageView) findViewById(R.id.action_left_drawer);
        cartIconAction = (BadgeImageView) findViewById(R.id.action_show_cart);
        checkoutSigninButton = (Button) findViewById(R.id.co_signin_button);
        cartQtyView = (TextView) findViewById(R.id.cart_item_qty);
        optionIcon = (ImageView) findViewById(R.id.option_icon);
        searchView = (SearchView) findViewById(R.id.search_text);
        logoView = (ImageView) findViewById(R.id.action_logo);
        titleView = (TextView) findViewById(R.id.action_title);
    }

    public void setConfig(Config config) {
        setConfig(config, null, null);
    }

    public void setConfig(Config config, OnClickListener listener) {
        setConfig(config, listener, null);
    }

    public void setConfig(Config config, OnClickListener listener, String title) {
        if (config==null) return;
        closeButton.setVisibility(config.close ? VISIBLE : GONE);
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
        searchView.setVisibility(config.search ? VISIBLE : GONE);
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

    // SearchView styling

    private static View findSearchViewElement(Resources res, SearchView searchView, String name) {
        int id = searchView.getResources().getIdentifier(name, null, null);
        if (id==0) return(null);
        View view = searchView.findViewById(id);
        return(view);
    }

    public static void styleSearchView(SearchView searchView) {
        View view;
        Resources res = searchView.getResources();

        view = findSearchViewElement(res, searchView, "android:id/search_button");
        if (view instanceof ImageView) {
            ((ImageView) view).setImageResource(R.drawable.ic_search_white);
        }

        view = findSearchViewElement(res, searchView, "android:id/search_close_btn");
        if (view instanceof ImageView) {
            ((ImageView) view).setImageResource(R.drawable.ic_close_white_18dp);
        }

//        view = findSearchViewElement(res, searchView, "android:id/search_mag_icon");
//        if (view instanceof ImageView) {
//            ((ImageView) view).setImageResource(R.drawable.ic_android);
//        }

//        view = findSearchViewElement(res, searchView, "android:id/search_plate");
//        if (view instanceof LinearLayout) {
//        }

        view = findSearchViewElement(res, searchView, "android:id/search_src_text");
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(0xffffffff);
        }
    }

    // Measurement & layout

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        // Get dimensions
        int layoutWidth = MeasureSpec.getSize(widthSpec);
        int slackWidth = layoutWidth-getPaddingLeft()-getPaddingRight();
        int usedHeight = 0;

        // Iterate over children
        int n = getChildCount();
        for(int i=0;i<n;i++) {
            View child = getChildAt(i);
            if (child.getVisibility()!=View.GONE) {
                // Measure child
                child.measure(MeasureSpec.makeMeasureSpec(slackWidth, MeasureSpec.AT_MOST),
                              MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int childWidth = child.getMeasuredWidth();
                int childHeight = child.getMeasuredHeight();

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
        // Get dimensions
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
                        x = right-width;
                        right = x;
                        break;
                    case Gravity.CENTER_HORIZONTAL: // Only a last child can be centered
                        x = (left+right-width)/2;
                        break;
                    default:
                        x = left;
                        left += width;
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

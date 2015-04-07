package com.staples.mobile.cfa.search;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.bundle.BundleItem;
import com.staples.mobile.cfa.cart.CartApiManager;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.common.analytics.Tracker;

/**
 * Created by Avinash Dodda.
 */
public class AddToCart implements CartApiManager.CartRefreshCallback {
    private final int IMAGE_DISPLAY_LENGTH = 1500;
    private BundleItem item;
    private ImageView button;
    private View whirlie;
    private Activity activity;

    public AddToCart(BundleItem item, View button, Activity activity) {
        this.item = item;
        this.button = (ImageView)button;
        this.activity = activity;

        View parent = (View) button.getParent();
        whirlie = parent.findViewById(R.id.bundle_whirlie);


        button.setVisibility(View.GONE);
        whirlie.setVisibility(View.VISIBLE);

        CartApiManager.addItemToCart(item.identifier, 1, this);
    }

    @Override
    public void onCartRefreshComplete(String errMsg) {
        MainActivity activity = (MainActivity) this.activity;
        if (activity == null) return;

        button.setVisibility(View.VISIBLE);
        whirlie.setVisibility(View.GONE);

        // if success
        if (errMsg == null) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ObjectAnimator.ofFloat(button, View.ALPHA, 0.2f, 1.0f).setDuration(1000).start();
                    button.setImageDrawable(button.getResources().getDrawable(R.drawable.ic_add_shopping_cart_black));
                }
            }, IMAGE_DISPLAY_LENGTH);
            button.setImageDrawable(button.getResources().getDrawable(R.drawable.ic_check_green));
            ActionBar.getInstance().setCartCount(CartApiManager.getCartTotalItems());
            Tracker.getInstance().trackActionForAddToCartFromClass(item.identifier, item.finalPrice, 1);
        } else {
            // if non-grammatical out-of-stock message from api, provide a nicer message
            if (errMsg.contains("items is out of stock")) {
                errMsg = activity.getResources().getString(R.string.avail_outofstock);
            }
            activity.showErrorDialog(errMsg);
        }
    }
}

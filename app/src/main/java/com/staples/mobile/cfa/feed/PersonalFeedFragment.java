package com.staples.mobile.cfa.feed;

/**
 * Author: Yongnan Zhou
 */

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.cart.CartApiManager;
import com.staples.mobile.cfa.widget.ActionBar;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.Product;
import com.staples.mobile.common.access.easyopen.model.browse.SkuDetails;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.analytics.Tracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class PersonalFeedFragment extends Fragment {
    private static final String TAG = PersonalFeedFragment.class.getSimpleName();

    public static final String SEEN_PRODUCT_SKU_LIST = "seenProductSkuList";
    public static final String SEEN_PRODUCT_LIST = "seenProductList";

    public static final String DAILY_DEAL_IDENTIFIER = "BI739472"; // TODO Needs to be configurable
    public static final String CLEARANCE_IDENTIFIER = "BI642994"; // TODO Needs to be configurable

    private static final int MAXFETCH = 50;

    private LinearLayout dailyDealLayout;
    private LinearLayout clearanceLayout;
    private LinearLayout seenProductsLayout;
    private LinearLayout emptyFeedLayout;

    private DataWrapper dailyDealWrapper;
    private DataWrapper clearanceWrapper;
    private DataWrapper seenProductsWrapper;

    private LinearLayout dailyDealContainer;
    private LinearLayout clearanceContainer;
    private LinearLayout seenProductsContainer;

    private TextView seenProductClearTV;
    private RelativeLayout seenProductsLoading;

    private String dailyDealTitle;
    private String clearanceTitle;
    private String seenProductsTitle;

    private List<com.staples.mobile.common.access.easyopen.model.cart.Product> cartItems;

    private class SkuDetailsCallback implements Callback<SkuDetails> {
        @Override
        public void success(final SkuDetails sku, Response response) {
            seenProductClearTV.setVisibility(View.VISIBLE);
            seenProductsWrapper.setState(DataWrapper.State.DONE);

            Activity activity = getActivity();
            if (activity == null) {
                return;
            }

            LayoutInflater inflater = getActivity().getLayoutInflater();

            Product seenProduct = sku.getProduct().get(0);

            View seenSavedProductRow = inflater.inflate(R.layout.personal_feed_product_item, null);

            TextView title = (TextView) seenSavedProductRow.findViewById(R.id.title);
            final String productName = Html.fromHtml(seenProduct.getProductName()).toString();
            title.setText(productName);

            RatingStars ratingStars = (RatingStars) seenSavedProductRow.findViewById(R.id.rating);
            ratingStars.setRating(seenProduct.getCustomerReviewRating(),
                    seenProduct.getCustomerReviewCount());

            PriceSticker priceSticker = (PriceSticker) seenSavedProductRow.findViewById(R.id.pricing);
            priceSticker.setBrowsePricing(seenProduct.getPricing());

            ImageView imageView = (ImageView) seenSavedProductRow.findViewById(R.id.image);

            // API safety check
            if(seenProduct.getImage() != null && seenProduct.getImage().size() > 0){
                Picasso.with(getActivity()).load(seenProduct.getImage().get(0).getUrl()).error(R.drawable.no_photo).into(imageView);
            }
            else{
                Log.d(TAG, "API returned empty image url!");
                imageView.setImageResource(R.drawable.no_photo);
            }

            seenProductsContainer.addView(seenSavedProductRow);

            // Set listener for all the items
            final String skuId = seenProduct.getSku();
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tracker.getInstance().trackActionForPersonalFeed(seenProductsTitle);
                    ((MainActivity) getActivity()).selectSkuItem(productName, skuId, false);
                }
            });

            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tracker.getInstance().trackActionForPersonalFeed(seenProductsTitle);
                    ((MainActivity) getActivity()).selectSkuItem(productName, skuId, false);
                }
            });

            PersonalFeedSingleton feedSingleton = PersonalFeedSingleton.getInstance(getActivity());
            HashSet<String> savedSkuSet = feedSingleton.getSavedSkus(getActivity());
            if(seenProductsContainer.getChildCount() < savedSkuSet.size()) {
                // still loading other items' data
                seenProductsLoading.setVisibility(View.VISIBLE);
            }
            else{
                seenProductsLoading.setVisibility(View.GONE);
            }

            Log.d(TAG, "Saved seen products: " + seenProduct.getProductName());
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            seenProductsWrapper.setState(DataWrapper.State.EMPTY);

            Activity activity = getActivity();
            if (activity == null) {
                return;
            }

            String message = ApiError.getErrorMessage(retrofitError);
            ((MainActivity)activity).showErrorDialog(message);
            Log.d(TAG, message);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        LinearLayout personalFeedLayout = (LinearLayout) inflater.inflate(R.layout.personal_feed, container, false);

        // get section titles for the sake of analytics
        Resources r = getResources();
        dailyDealTitle = r.getString(R.string.feed_daily_deal_title);
        clearanceTitle = r.getString(R.string.feed_clearance_title);
        seenProductsTitle = r.getString(R.string.feed_seen_products_title);


        dailyDealLayout = (LinearLayout) personalFeedLayout.findViewById(R.id.daily_deal_layout);
        clearanceLayout = (LinearLayout) personalFeedLayout.findViewById(R.id.clearance_layout);
        seenProductsLayout = (LinearLayout) personalFeedLayout.findViewById(R.id.seen_products_layout);
        emptyFeedLayout = (LinearLayout) personalFeedLayout.findViewById(R.id.empty_feed_layout);

        dailyDealWrapper = (DataWrapper) personalFeedLayout.findViewById(R.id.daily_deal_wrapper);
        clearanceWrapper = (DataWrapper) personalFeedLayout.findViewById(R.id.clearance_wrapper);
        seenProductsWrapper = (DataWrapper) personalFeedLayout.findViewById(R.id.seen_products_wrapper);

        dailyDealContainer = (LinearLayout) personalFeedLayout.findViewById(R.id.daily_deal_container);
        clearanceContainer = (LinearLayout) personalFeedLayout.findViewById(R.id.clearance_container);
        seenProductsContainer = (LinearLayout) personalFeedLayout.findViewById(R.id.seen_products_container);

        seenProductClearTV = (TextView) personalFeedLayout.findViewById(R.id.seen_products_clear);
        seenProductClearTV.setVisibility(View.GONE);
        seenProductClearTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seenProductsWrapper.setState(DataWrapper.State.LOADING);
                seenProductsContainer.removeAllViews();
                seenProductClearTV.setVisibility(View.GONE);
                seenProductsLoading.setVisibility(View.GONE);

                removeSavedSeenProducts();

                PersonalFeedSingleton feedSingleton = PersonalFeedSingleton.getInstance(getActivity());
                feedSingleton.setSavedSkus(new HashSet<String>());
                feedSingleton.setSavedSeenProducts(
                        new PersistentSizedArrayList<String>(PersonalFeedSingleton.SEEN_PRODUCTS_AMOUNT));
                seenProductsLayout.setVisibility(View.GONE);

                if(dailyDealContainer.getChildCount() == 0 && clearanceContainer.getChildCount() == 0) {
                    emptyFeedLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        setCartItems();

        seenProductsLoading = (RelativeLayout) personalFeedLayout.findViewById(R.id.seen_products_loading_footer);

        seenProductsWrapper.setState(DataWrapper.State.LOADING);

        setSeenProductsAdapter();
        setDailyDealAdapter();
        setClearenceAdapter();

        return (personalFeedLayout);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar.getInstance().setConfig(ActionBar.Config.FEED);
        Tracker.getInstance().trackStateForPersonalFeed(); // Analytics
    }

    private void setCartItems() {
        this.cartItems = getCartItems(CartApiManager.getCart());
    }

    private void setSeenProductsAdapter(){
        // set seen products list
        HashSet<String> saveSeenSkus =
                PersonalFeedSingleton.getInstance(getActivity()).getSavedSkus(getActivity());

        // display "nothing found" if no saved seen products
        if(saveSeenSkus.isEmpty()){
            seenProductsLayout.setVisibility(View.GONE);
        }
        else{
            emptyFeedLayout.setVisibility(View.GONE);
            seenProductsWrapper.setState(DataWrapper.State.LOADING);
            for(String sku : saveSeenSkus){
                // Initiate SKU API call
                EasyOpenApi api = Access.getInstance().getEasyOpenApi(false);
                api.getSkuDetails(sku, 1, MAXFETCH, new SkuDetailsCallback());
            }
        }
    }

    private void setDailyDealAdapter() {
        if(cartItems != null) {
            dailyDealWrapper.setState(DataWrapper.State.LOADING);

            String bundleUrl = "category/identifier/";
            Map collectionMap = new HashMap<String, String>();
            ProductCollection.getProducts(bundleUrl + DAILY_DEAL_IDENTIFIER,
                    "50", // limit (may be null)
                    "1", // offset (may be null)
                    collectionMap, // currently not used
                    new ProductCollection.ProductCollectionCallBack() {
                        @Override
                        public void onProductCollectionResult(ProductCollection.ProductContainer productContainer,
                                                              List<ProductCollection.ProductContainer.ERROR_CODES> errorCodes) {
                            HashSet<String> dailyDealSkuSet = new HashSet<String>();
                            if (productContainer.getProducts() != null) {
                                for (Product p : productContainer.getProducts()) {
                                    dailyDealSkuSet.add(p.getSku());
                                    //fillContainer(p, dailyDealContainer, dailyDealTitle);
                                    //Log.d(TAG, "Daily Deal Products: " + p.getProductName() + "-" + p.getSku());
                                }
                            }

                            if (cartItems != null) {
                                for (com.staples.mobile.common.access.easyopen.model.cart.Product cartItem : cartItems) {
                                    String cartItemSku = cartItem.getSku();
                                    if (dailyDealSkuSet.contains(cartItemSku)) {
                                        fillContainer(cartItem, dailyDealContainer, dailyDealTitle);
                                        Log.d(TAG, "Daily deal products in cart: " + cartItem.getProductName());
                                    }
                                }
                            }

                            // display "nothing found" if no daily deal products
                            if (dailyDealContainer.getChildCount() == 0) {
                                if(seenProductsContainer.getChildCount() == 0 && clearanceContainer.getChildCount() == 0) {
                                    emptyFeedLayout.setVisibility(View.VISIBLE);
                                }
                            } else {
                                emptyFeedLayout.setVisibility(View.GONE);
                                dailyDealWrapper.setState(DataWrapper.State.DONE);
                                dailyDealLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    }); // ProductCollection CallBack
        }
        else {
            dailyDealWrapper.setState(DataWrapper.State.EMPTY);
        }
    }

    private void setClearenceAdapter() {
        String bundleUrl = "category/identifier/";
        Map collectionMap = new HashMap<String, String>();

        ProductCollection.getProducts(bundleUrl + CLEARANCE_IDENTIFIER,
                "50", // limit (may be null)
                "1", // offset (may be null)
                collectionMap, // currently not used
                new ProductCollection.ProductCollectionCallBack() {
                    @Override
                    public void onProductCollectionResult(ProductCollection.ProductContainer productContainer,
                                                          List<ProductCollection.ProductContainer.ERROR_CODES> errorCodes) {
                        HashSet<String> clearanceSkuSet = new HashSet<String>();
                        if (productContainer.getProducts() != null) {
                            for (Product p : productContainer.getProducts()) {
                                clearanceSkuSet.add(p.getSku());
                                //fillContainer(p, clearanceContainer, clearanceTitle);
                                //Log.d(TAG, "Clearance Products: " + p.getProductName() + "-" + p.getSku());
                            }

                        }

                        if (cartItems != null) {
                            clearanceWrapper.setState(DataWrapper.State.LOADING);
                            for (com.staples.mobile.common.access.easyopen.model.cart.Product cartItem : cartItems) {
                                String cartItemSku = cartItem.getSku();
                                if (clearanceSkuSet.contains(cartItemSku)) {
                                    fillContainer(cartItem, clearanceContainer, clearanceTitle);
                                    Log.d(TAG, "Clearance products in cart: " + cartItem.getProductName());
                                }
                            }
                        } else{
                            clearanceWrapper.setState(DataWrapper.State.EMPTY);
                        }

                        // display "nothing found" if no clearance products
                        if (clearanceContainer.getChildCount() == 0) {
                        } else {
                            emptyFeedLayout.setVisibility(View.GONE);
                            clearanceWrapper.setState(DataWrapper.State.DONE);
                            clearanceLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }); // ProductCollection CallBack
    }

    private void fillContainer(com.staples.mobile.common.access.easyopen.model.cart.Product cartItem,
                               LinearLayout container, final String containerTitle){
        final String productName = Html.fromHtml(cartItem.getProductName()).toString();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View row = inflater.inflate(R.layout.personal_feed_product_item, null);

        TextView title = (TextView) row.findViewById(R.id.title);
        title.setText(productName);

        RatingStars ratingStars = (RatingStars) row.findViewById(R.id.rating);
        ratingStars.setRating(cartItem.getCustomerReviewRating(), cartItem.getCustomerReviewCount());

        PriceSticker priceSticker = (PriceSticker) row.findViewById(R.id.pricing);
        priceSticker.setCartPricing(cartItem.getPricing());

        ImageView imageView = (ImageView) row.findViewById(R.id.image);
        String imageUrl= "";
        if(cartItem.getImage() != null && cartItem.getImage().size() > 0){
            imageUrl = cartItem.getImage().get(0).getUrl();
        }
        Picasso.with(getActivity()).load(imageUrl).error(R.drawable.no_photo).into(imageView);

        container.addView(row);

        // Set listener for all the items
        final String sku = cartItem.getSku();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker.getInstance().trackActionForPersonalFeed(containerTitle);
                ((MainActivity) getActivity()).selectSkuItem(productName, sku, false);
            }
        });

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tracker.getInstance().trackActionForPersonalFeed(containerTitle);
                ((MainActivity) getActivity()).selectSkuItem(productName, sku, false);
            }
        });
    }

    private void removeSavedSeenProducts() {
        SharedPreferences sp = getActivity().getSharedPreferences(MainActivity.PREFS_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SEEN_PRODUCT_SKU_LIST, "");
        editor.putString(SEEN_PRODUCT_LIST, "");
        editor.apply();
    }

    private ArrayList<com.staples.mobile.common.access.easyopen.model.cart.Product> getCartItems(Cart cart) {
        ArrayList<com.staples.mobile.common.access.easyopen.model.cart.Product> cartItems
                = new ArrayList<com.staples.mobile.common.access.easyopen.model.cart.Product>();

        if (cart != null) {
            List<com.staples.mobile.common.access.easyopen.model.cart.Product> products = cart.getProduct();
            if (products != null) {
                // iterate thru products to create list of cart items
                for (com.staples.mobile.common.access.easyopen.model.cart.Product product : products) {
                    if (product.getQuantity() > 0) { // I actually saw a zero quantity once returned from sapi
                        cartItems.add(product);
                    }
                }
            }
        }

        return cartItems;
    }
}

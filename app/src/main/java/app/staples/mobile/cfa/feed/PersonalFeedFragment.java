package app.staples.mobile.cfa.feed;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crittercism.app.Crittercism;
import com.squareup.picasso.Picasso;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.Discount;
import com.staples.mobile.common.access.easyopen.model.browse.Pricing;
import com.staples.mobile.common.access.easyopen.model.browse.Product;
import com.staples.mobile.common.access.easyopen.model.browse.SkuDetails;
import com.staples.mobile.common.access.easyopen.model.cart.Cart;
import com.staples.mobile.common.analytics.Tracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import app.staples.R;
import app.staples.mobile.cfa.MainActivity;
import app.staples.mobile.cfa.cart.CartApiManager;
import app.staples.mobile.cfa.util.MiscUtils;
import app.staples.mobile.cfa.widget.ActionBar;
import app.staples.mobile.cfa.widget.DataWrapper;
import app.staples.mobile.cfa.widget.PriceSticker;
import app.staples.mobile.cfa.widget.RatingStars;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PersonalFeedFragment extends Fragment {
    private static final String TAG = PersonalFeedFragment.class.getSimpleName();

    public static final String SEEN_PRODUCT_SKU_LIST = "seenProductSkuList";
    public static final String SEEN_PRODUCT_LIST = "seenProductList";

    public static final String DAILY_DEAL_IDENTIFIER = "BI739472"; // TODO Needs to be configurable
    public static final String CLEARANCE_IDENTIFIER = "BI642994"; // TODO Needs to be configurable

    private LinearLayout dailyDealLayout;
    private LinearLayout clearanceLayout;
    private LinearLayout seenProductsLayout;
    private LinearLayout emptyFeedLayout;

    private DataWrapper dailyDealWrapper;
    private DataWrapper clearanceWrapper;
    private DataWrapper seenProductsWrapper;

    private static LinearLayout dailyDealContainer;
    private static LinearLayout clearanceContainer;
    private static LinearLayout seenProductsContainer;

    private TextView seenProductClearTV;

    private RelativeLayout seenProductsLoading;
    private LinearLayout feedLoading;

    private View seenProductsSeparator;
    private View clearanceSeparator;

    private String dailyDealTitle;
    private String clearanceTitle;
    private String seenProductsTitle;

    private LayoutInflater myInflater;

    private List<com.staples.mobile.common.access.easyopen.model.cart.Product> cartItems;

    private class SkuDetailsCallback implements Callback<SkuDetails> {
        @Override
        public void success(final SkuDetails sku, Response response) {
            MainActivity activity = (MainActivity) getActivity();
            if (activity==null) return;

            seenProductClearTV.setVisibility(View.VISIBLE);
            seenProductsWrapper.setState(DataWrapper.State.DONE);

            Product seenProduct = sku.getProduct().get(0);

            View seenSavedProductRow = myInflater.inflate(R.layout.personal_feed_product_item, null);

            TextView title = (TextView) seenSavedProductRow.findViewById(R.id.title);
            final String productName = MiscUtils.cleanupHtml(seenProduct.getProductName());
            title.setText(productName);

            RatingStars ratingStars = (RatingStars) seenSavedProductRow.findViewById(R.id.rating);
            ratingStars.setRating(seenProduct.getCustomerReviewRating(),
                    seenProduct.getCustomerReviewCount());

            // check if the product has discount
            List<Pricing> pricings = seenProduct.getPricing();
            if (pricings!=null && pricings.size()>0) {
                Pricing pricing = pricings.get(0);
                PriceSticker priceSticker = (PriceSticker) seenSavedProductRow.findViewById(R.id.pricing);

                float rebate = findRebate(pricing);
                if (rebate>0.0f) {
                    seenSavedProductRow.findViewById(R.id.rebate_layout).setVisibility(View.VISIBLE);
                    TextView rebateText = (TextView) seenSavedProductRow.findViewById(R.id.rebate_text);
                    String text = String.format("$%.2f %s", rebate, getResources().getString(R.string.rebate));
                    rebateText.setText(text);

                    float finalPrice = pricing.getFinalPrice();
                    float wasPrice = pricing.getListPrice();
                    String unit = pricing.getUnitOfMeasure();
                    priceSticker.setPricing(finalPrice + rebate, wasPrice, unit, "*");
                } else {
                    seenSavedProductRow.findViewById(R.id.rebate_layout).setVisibility(View.GONE);
                    priceSticker.setPricing(pricing);
                }
            }

            ImageView imageView = (ImageView) seenSavedProductRow.findViewById(R.id.image);

            // API safety check
            if(seenProduct.getImage() != null && seenProduct.getImage().size() > 0){
                Picasso.with(activity).load(seenProduct.getImage().get(0).getUrl()).error(R.drawable.no_photo).into(imageView);
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
                    MainActivity activity = (MainActivity) getActivity();
                    Tracker.getInstance().trackActionForPersonalFeed(seenProductsTitle);
                    activity.selectSkuItem(productName, skuId, false);
                }
            });

            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity activity = (MainActivity) getActivity();
                    Tracker.getInstance().trackActionForPersonalFeed(seenProductsTitle);
                    activity.selectSkuItem(productName, skuId, false);
                }
            });

            PersonalFeedSingleton feedSingleton = PersonalFeedSingleton.getInstance(activity);
            HashSet<String> savedSkuSet = feedSingleton.getSavedSkus(activity);
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
            MainActivity activity = (MainActivity) getActivity();
            if (activity==null) return;

            seenProductsWrapper.setState(DataWrapper.State.EMPTY);

            String message = ApiError.getErrorMessage(retrofitError);
            activity.showErrorDialog(message);
            Log.d(TAG, message);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        MainActivity activity = (MainActivity) getActivity();
        this.myInflater = inflater;

        Crittercism.leaveBreadcrumb("PersonalFeedFragment:onCreateView(): Displaying the Personal Feed screen.");
        LinearLayout personalFeedLayout = (LinearLayout) myInflater.inflate(R.layout.personal_feed, container, false);
        personalFeedLayout.setTag(this);

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

        seenProductsSeparator = (View) personalFeedLayout.findViewById(R.id.seen_products_separator);
        clearanceSeparator = (View) personalFeedLayout.findViewById(R.id.clearance_separator);

        seenProductsLoading = (RelativeLayout) personalFeedLayout.findViewById(R.id.seen_products_loading_footer);
        feedLoading = (LinearLayout) personalFeedLayout.findViewById(R.id.feed_loading_spinner);

        seenProductClearTV = (TextView) personalFeedLayout.findViewById(R.id.seen_products_clear);
        seenProductClearTV.setVisibility(View.GONE);
        seenProductClearTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                seenProductsContainer.removeAllViews();
                seenProductClearTV.setVisibility(View.GONE);
                seenProductsLoading.setVisibility(View.GONE);

                removeSavedSeenProducts();

                PersonalFeedSingleton feedSingleton = PersonalFeedSingleton.getInstance(activity);
                feedSingleton.setSavedSkus(new HashSet<String>());
                feedSingleton.setSavedSeenProducts(
                        new PersistentSizedArrayList<String>(PersonalFeedSingleton.SEEN_PRODUCTS_AMOUNT));
                seenProductsLayout.setVisibility(View.GONE);
                seenProductsSeparator.setVisibility(View.GONE);

                if(dailyDealContainer.getChildCount() == 0 && clearanceContainer.getChildCount() == 0) {
                    emptyFeedLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        setCartItems();

        getDailyDeal();

        return personalFeedLayout;
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

    private void getDailyDeal() {
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
                            Activity activity = getActivity();
                            if (activity==null) return;

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

                            // if no daily deal products
                            if (dailyDealContainer.getChildCount() == 0) {
                                dailyDealWrapper.setState(DataWrapper.State.EMPTY);
//                                if(seenProductsContainer.getChildCount() == 0 && clearanceContainer.getChildCount() == 0) {
//                                    emptyFeedLayout.setVisibility(View.VISIBLE);
//                                }
                            } else {
                                emptyFeedLayout.setVisibility(View.GONE);
                                dailyDealWrapper.setState(DataWrapper.State.DONE);
                                dailyDealLayout.setVisibility(View.VISIBLE);
                                feedLoading.setVisibility(View.GONE);
                            }

                            getClearance();
                        }
                    }); // ProductCollection CallBack
        }
        else {
            getSeenProducts();

            dailyDealWrapper.setState(DataWrapper.State.EMPTY);
        }

        MainActivity activity = (MainActivity) getActivity();
        HashSet<String> saveSeenSkus =
                PersonalFeedSingleton.getInstance(activity).getSavedSkus(activity);

        if(saveSeenSkus.isEmpty()){
            seenProductsLayout.setVisibility(View.GONE);
        }
        else{
            feedLoading.setVisibility(View.GONE);
            seenProductsLayout.setVisibility(View.VISIBLE);
        }
    }

    private void getClearance() {
        if(cartItems != null) {
            clearanceWrapper.setState(DataWrapper.State.LOADING);

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
                            Activity activity = getActivity();
                            if (activity==null) return;

                            HashSet<String> clearanceSkuSet = new HashSet<String>();
                            if (productContainer.getProducts() != null) {
                                for (Product p : productContainer.getProducts()) {
                                    clearanceSkuSet.add(p.getSku());
                                    //fillContainer(p, clearanceContainer, clearanceTitle);
                                    //Log.d(TAG, "Clearance Products: " + p.getProductName() + "-" + p.getSku());
                                }
                            }

                            if (cartItems != null) {
                                for (com.staples.mobile.common.access.easyopen.model.cart.Product cartItem : cartItems) {
                                    String cartItemSku = cartItem.getSku();
                                    if (clearanceSkuSet.contains(cartItemSku)) {
                                        fillContainer(cartItem, clearanceContainer, clearanceTitle);
                                        Log.d(TAG, "Clearance products in cart: " + cartItem.getProductName());
                                    }
                                }
                            }

                            // if no clearance products
                            if (clearanceContainer.getChildCount() == 0) {
                                clearanceWrapper.setState(DataWrapper.State.EMPTY);
                                clearanceSeparator.setVisibility(View.GONE);

                                if (seenProductsContainer.getChildCount() == 0 && dailyDealContainer.getChildCount() == 0) {
                                    emptyFeedLayout.setVisibility(View.VISIBLE);
                                    feedLoading.setVisibility(View.GONE);
                                } else {
                                    emptyFeedLayout.setVisibility(View.GONE);
                                    feedLoading.setVisibility(View.GONE);
                                }
                            } else {
                                emptyFeedLayout.setVisibility(View.GONE);
                                clearanceSeparator.setVisibility(View.VISIBLE);
                                clearanceWrapper.setState(DataWrapper.State.DONE);
                                clearanceLayout.setVisibility(View.VISIBLE);
                                feedLoading.setVisibility(View.GONE);
                            }

                            getSeenProducts();
                        }
                    }); // ProductCollection CallBack
        }
        else{
            clearanceWrapper.setState(DataWrapper.State.EMPTY);
        }
    }

    private void getSeenProducts(){
        seenProductsWrapper.setState(DataWrapper.State.LOADING);

        // set seen products list
        MainActivity activity = (MainActivity) getActivity();
        HashSet<String> saveSeenSkus =
                PersonalFeedSingleton.getInstance(activity).getSavedSkus(activity);

        // if no saved seen products
        if(saveSeenSkus.isEmpty()){
            seenProductsLayout.setVisibility(View.GONE);
            seenProductsSeparator.setVisibility(View.GONE);
        }
        else{
            emptyFeedLayout.setVisibility(View.GONE);
            feedLoading.setVisibility(View.GONE);
            seenProductsLayout.setVisibility(View.VISIBLE);
            seenProductsSeparator.setVisibility(View.VISIBLE);
            seenProductsWrapper.setState(DataWrapper.State.LOADING);
            for(String sku : saveSeenSkus){
                // Initiate SKU API call
                EasyOpenApi api = Access.getInstance().getEasyOpenApi(false);
                api.getSkuDetails(sku, new SkuDetailsCallback());
            }
        }
    }

    private void fillContainer(com.staples.mobile.common.access.easyopen.model.cart.Product cartItem,
                               LinearLayout container, final String containerTitle){
        final String productName = MiscUtils.cleanupHtml(cartItem.getProductName());

        MainActivity activity = (MainActivity) getActivity();
        View row = myInflater.inflate(R.layout.personal_feed_product_item, null);

        TextView title = (TextView) row.findViewById(R.id.title);
        title.setText(productName);

        RatingStars ratingStars = (RatingStars) row.findViewById(R.id.rating);
        ratingStars.setRating(cartItem.getCustomerReviewRating(), cartItem.getCustomerReviewCount());

        // check if the product has discount
        List<com.staples.mobile.common.access.easyopen.model.cart.Pricing> pricings = cartItem.getPricing();
        if (pricings!=null && pricings.size()>0) {
            com.staples.mobile.common.access.easyopen.model.cart.Pricing pricing = pricings.get(0);
            PriceSticker priceSticker = (PriceSticker) row.findViewById(R.id.pricing);
            String unit = pricing.getUnitOfMeasure();

            float rebate = findRebate(pricing);
            int quantity = cartItem.getQuantity();
            float finalPrice = pricing.getTotalOrderItemPrice()/quantity;
            float wasPrice = pricing.getListPrice();

            if (rebate>0.0f) {
                row.findViewById(R.id.rebate_layout).setVisibility(View.VISIBLE);
                TextView rebateText = (TextView) row.findViewById(R.id.rebate_text);
                String text = String.format("$%.2f %s", rebate, activity.getResources().getString(R.string.rebate));
                rebateText.setText(text);
                priceSticker.setPricing(finalPrice, wasPrice, unit, "*");
            } else {
                row.findViewById(R.id.rebate_layout).setVisibility(View.GONE);
                priceSticker.setPricing(finalPrice, wasPrice, unit, null);
            }
        }

        ImageView imageView = (ImageView) row.findViewById(R.id.image);
        String imageUrl= "";
        if(cartItem.getImage() != null && cartItem.getImage().size() > 0){
            imageUrl = cartItem.getImage().get(0).getUrl();
        }
        Picasso.with(activity).load(imageUrl).error(R.drawable.no_photo).into(imageView);

        container.addView(row);

        // Set listener for all the items
        final String sku = cartItem.getSku();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                Tracker.getInstance().trackActionForPersonalFeed(containerTitle);
                activity.selectSkuItem(productName, sku, false);
            }
        });

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                Tracker.getInstance().trackActionForPersonalFeed(containerTitle);
                activity.selectSkuItem(productName, sku, false);
            }
        });
    }

    private float findRebate(Pricing pricing) {
        if (pricing==null) return(0.0f);
        List<Discount> discounts = pricing.getDiscount();
        if (discounts==null) return(0.0f);

        float rebate = 0.0f;
        for(Discount discount : discounts) {
            String name = discount.getName();
            if ("rebate".equals(name)) {
                float amount = discount.getAmount();
                if (amount > rebate) rebate = amount;
            }
        }
        return(rebate);
    }

    private float findRebate(com.staples.mobile.common.access.easyopen.model.cart.Pricing pricing) {
        if (pricing==null) return(0.0f);
        List<Discount> discounts = pricing.getDiscount();
        if (discounts==null) return(0.0f);

        float rebate = 0.0f;
        for(Discount discount : discounts) {
            String name = discount.getName();
            if ("rebate".equals(name)) {
                float amount = discount.getAmount();
                if (amount > rebate) rebate = amount;
            }
        }
        return(rebate);
    }

    private void removeSavedSeenProducts() {
        MainActivity activity = (MainActivity) getActivity();
        SharedPreferences sp = activity.getSharedPreferences(MainActivity.PREFS_FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SEEN_PRODUCT_SKU_LIST, "");
        editor.putString(SEEN_PRODUCT_LIST, "");
        editor.apply();

        //isSeenProductsEmpty = true;
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

package com.staples.mobile.cfa.feed;

/**
 * Author: Yongnan Zhou
 */

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.staples.mobile.cfa.R;
import com.staples.mobile.cfa.BaseFragment;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.cart.CartFragment;
import com.staples.mobile.cfa.cart.CartItem;
import com.staples.mobile.cfa.login.LoginHelper;
import com.staples.mobile.cfa.widget.DataWrapper;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.Access;
import com.staples.mobile.common.access.easyopen.api.EasyOpenApi;
import com.staples.mobile.common.access.easyopen.model.ApiError;
import com.staples.mobile.common.access.easyopen.model.browse.Product;
import com.staples.mobile.common.access.easyopen.model.browse.SkuDetails;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PersonalFeedFragment extends BaseFragment{
    private static final String TAG = "PersonalFeedFragment";

    public static final String DAILY_DEAL_IDENTIFIER = "BI739472";
    public static final String CLEARANCE_IDENTIFIER = "BI642994";

    private static final String RECOMMENDATION = "v1";
    private static final String STORE_ID = "10001";
    private static final String CATALOG_ID = "10051";
    private static final String LOCALE = "en_US";
    private static final String ZIPCODE = "01010";
    //    private static final String CLIENT_ID = "N6CA89Ti14E6PAbGTr5xsCJ2IGaHzGwS";
    private static final String CLIENT_ID = LoginHelper.CLIENT_ID;
    private static final int MAXFETCH = 50;

    private DataWrapper dailyDealWrapper;
    private DataWrapper clearanceWrapper;
    private DataWrapper seenProductsWrapper;

    private LinearLayout dailyDealContainer;
    private LinearLayout clearanceContainer;
    private LinearLayout seenProductsContainer;

    private class SkuDetailsCallback implements Callback<SkuDetails> {
        @Override
        public void success(final SkuDetails sku, Response response) {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }

            LayoutInflater inflater = getActivity().getLayoutInflater();

            Product seenProduct = sku.getProduct().get(0);

            View seenSavedProductRow = inflater.inflate(R.layout.personal_feed_product_item, null);

            TextView title = (TextView) seenSavedProductRow.findViewById(R.id.title);
            title.setText(seenProduct.getProductName());

            RatingStars ratingStars = (RatingStars) seenSavedProductRow.findViewById(R.id.rating);
            ratingStars.setRating(seenProduct.getCustomerReviewRating(),
                    seenProduct.getCustomerReviewCount());

            PriceSticker priceSticker = (PriceSticker) seenSavedProductRow.findViewById(R.id.pricing);
            priceSticker.setPricing(seenProduct.getPricing().get(0).getFinalPrice(),
                    seenProduct.getPricing().get(0).getUnitOfMeasure());

            ImageView imageView = (ImageView) seenSavedProductRow.findViewById(R.id.image);
            Picasso.with(getActivity()).load(seenProduct.getImage().get(0).getUrl()).error(R.drawable.no_photo).into(imageView);

            seenProductsContainer.addView(seenSavedProductRow);

            // Set listener for all the items
            final String skuId = seenProduct.getSku();
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) getActivity()).selectSkuItem(skuId);
                }
            });

            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((MainActivity) getActivity()).selectSkuItem(skuId);
                }
            });

            Log.d(TAG, "Saved seen products: " + seenProduct.getProductName());
        }

        @Override
        public void failure(RetrofitError retrofitError) {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }

            String message = ApiError.getErrorMessage(retrofitError);
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            Log.d(TAG, message);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        LinearLayout personalFeedLayout = (LinearLayout) inflater.inflate(R.layout.personal_feed, container, false);

        dailyDealWrapper = (DataWrapper) personalFeedLayout.findViewById(R.id.daily_deal_wrapper);
        clearanceWrapper = (DataWrapper) personalFeedLayout.findViewById(R.id.clearance_wrapper);
        seenProductsWrapper = (DataWrapper) personalFeedLayout.findViewById(R.id.seen_products_wrapper);

        dailyDealContainer = (LinearLayout) personalFeedLayout.findViewById(R.id.daily_deal_container);
        clearanceContainer = (LinearLayout) personalFeedLayout.findViewById(R.id.clearance_container);
        seenProductsContainer = (LinearLayout) personalFeedLayout.findViewById(R.id.seen_products_container);

        seenProductsWrapper.setState(DataWrapper.State.LOADING);

        final List<CartItem> cartItems = CartFragment.getListItems();

        if(cartItems != null) {
            dailyDealWrapper.setState(DataWrapper.State.LOADING);
            clearanceWrapper.setState(DataWrapper.State.LOADING);

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
                                    //fillContainer(p, dailyDealContainer);
                                    //Log.d(TAG, "Daily Deal Products: " + p.getProductName() + "-" + p.getSku());
                                }
                            }

                            if (cartItems != null) {
                                for (CartItem cartItem : cartItems) {
                                    String cartItemSku = cartItem.getProduct().getSku();
                                    if (dailyDealSkuSet.contains(cartItemSku)) {
                                        fillContainer(cartItem, dailyDealContainer);
                                        Log.d(TAG, "Daily deal products in cart: " + cartItem.getProduct().getProductName());
                                    }
                                }
                            }

                            // display "nothing found" if no daily deal products
                            if (dailyDealContainer.getChildCount() == 0) {
                                dailyDealWrapper.setState(DataWrapper.State.EMPTY);
                            } else {
                                dailyDealWrapper.setState(DataWrapper.State.DONE);
                            }
                        }
                    }); // ProductCollection CallBack

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
                                    //fillContainer(p, clearanceContainer);
                                    //Log.d(TAG, "Clearance Products: " + p.getProductName() + "-" + p.getSku());
                                }

                            }

                            if (cartItems != null) {
                                for (CartItem cartItem : cartItems) {
                                    String cartItemSku = cartItem.getProduct().getSku();
                                    if (clearanceSkuSet.contains(cartItemSku)) {
                                        fillContainer(cartItem, clearanceContainer);
                                        Log.d(TAG, "Clearance products in cart: " + cartItem.getProduct().getProductName());
                                    }
                                }
                            }

                            // display "nothing found" if no clearance products
                            if (clearanceContainer.getChildCount() == 0) {
                                clearanceWrapper.setState(DataWrapper.State.EMPTY);
                            } else {
                                clearanceWrapper.setState(DataWrapper.State.DONE);
                            }
                        }
                    }); // ProductCollection CallBack
        }
        else{
            dailyDealWrapper.setState(DataWrapper.State.EMPTY);
            clearanceWrapper.setState(DataWrapper.State.EMPTY);
        }
        setSeenProductsAdapter();

        return (personalFeedLayout);
    }

    private void setSeenProductsAdapter(){
        // set seen products list
        PersistentSizedArrayList<SeenProductsRowItem> saveSeenProducts =
                PersonalFeedSingleton.getInstance(getActivity()).getSavedSeenProducts(getActivity());

        // display "nothing found" if no saved seen products
        if(saveSeenProducts == null){
            seenProductsWrapper.setState(DataWrapper.State.EMPTY);
        }
        else{
            seenProductsWrapper.setState(DataWrapper.State.LOADING);

            for(SeenProductsRowItem savedSeenProduct : saveSeenProducts){
                String sku = savedSeenProduct.getSku();

                // Initiate SKU API call
                EasyOpenApi api = Access.getInstance().getEasyOpenApi(false);
                api.getSkuDetails(RECOMMENDATION, STORE_ID, sku, CATALOG_ID, LOCALE,
                        ZIPCODE, CLIENT_ID, null, MAXFETCH, new SkuDetailsCallback());
            }

            seenProductsWrapper.setState(DataWrapper.State.DONE);
        }
    }

    private void fillContainer(CartItem cartItem, LinearLayout container){
        String productName = cartItem.getProduct().getProductName();
        String currentPrice = String.valueOf(cartItem.getProduct().getPricing().get(0).getFinalPrice());
        String reviewCount = String.valueOf(cartItem.getProduct().getCustomerReviewCount());
        String rating = String.valueOf(cartItem.getProduct().getCustomerReviewRating());
        String unitOfMeasure = cartItem.getProduct().getPricing().get(0).getUnitOfMeasure();
        String imageUrl = cartItem.getProduct().getImage().get(0).getUrl();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View row = inflater.inflate(R.layout.personal_feed_product_item, null);

        TextView title = (TextView) row.findViewById(R.id.title);
        title.setText(productName);

        RatingStars ratingStars = (RatingStars) row.findViewById(R.id.rating);
        ratingStars.setRating(Float.parseFloat(rating),
                Integer.parseInt(reviewCount));

        PriceSticker priceSticker = (PriceSticker) row.findViewById(R.id.pricing);
        priceSticker.setPricing(Float.parseFloat(currentPrice), unitOfMeasure);

        ImageView imageView = (ImageView) row.findViewById(R.id.image);
        Picasso.with(getActivity()).load(imageUrl).error(R.drawable.no_photo).into(imageView);

        container.addView(row);

        // Set listener for all the items
        final String sku = cartItem.getProduct().getSku();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).selectSkuItem(sku);
            }
        });

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).selectSkuItem(sku);
            }
        });
    }

    private void fillContainer(Product cartItemProduct, LinearLayout container){
        String productName = cartItemProduct.getProductName();
        String currentPrice = String.valueOf(cartItemProduct.getPricing().get(0).getFinalPrice());
        String reviewCount = String.valueOf(cartItemProduct.getCustomerReviewCount());
        String rating = String.valueOf(cartItemProduct.getCustomerReviewRating());
        String unitOfMeasure = cartItemProduct.getPricing().get(0).getUnitOfMeasure();
        String imageUrl = cartItemProduct.getImage().get(0).getUrl();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View row = inflater.inflate(R.layout.personal_feed_product_item, null);

        TextView title = (TextView) row.findViewById(R.id.title);
        title.setText(productName);

        RatingStars ratingStars = (RatingStars) row.findViewById(R.id.rating);
        ratingStars.setRating(Float.parseFloat(rating),
                Integer.parseInt(reviewCount));

        PriceSticker priceSticker = (PriceSticker) row.findViewById(R.id.pricing);
        priceSticker.setPricing(Float.parseFloat(currentPrice), unitOfMeasure);

        ImageView imageView = (ImageView) row.findViewById(R.id.image);
        Picasso.with(getActivity()).load(imageUrl).error(R.drawable.no_photo).into(imageView);

        container.addView(row);

        // Set listener for all the items
        final String sku = cartItemProduct.getSku();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).selectSkuItem(sku);
            }
        });

        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).selectSkuItem(sku);
            }
        });
    }
}
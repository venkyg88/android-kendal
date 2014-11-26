package com.staples.mobile.cfa.feed;

/**
 * Author: Yongnan Zhou
 */

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.staples.mobile.R;
import com.staples.mobile.cfa.BaseFragment;
import com.staples.mobile.cfa.MainActivity;
import com.staples.mobile.cfa.cart.CartFragment;
import com.staples.mobile.cfa.cart.CartItem;
import com.staples.mobile.cfa.widget.PriceSticker;
import com.staples.mobile.cfa.widget.RatingStars;
import com.staples.mobile.common.access.easyopen.model.browse.Product;

import java.util.HashSet;
import java.util.List;

public class PersonalFeedFragment extends BaseFragment
        implements DailyDealProductCollection.ProductCollectionCallBack,
                   ClearanceProductCollection.ProductCollectionCallBack {
    private static final String TAG = "PersonalFeedFragment";

    private static final String DAILY_DEAL_IDENTIFIER = "BI739472";
    private static final String CLEARANCE_IDENTIFIER = "BI642994";

    private LinearLayout clearanceContainer;
    private LinearLayout dailyDealContainer;
    private LinearLayout seenProductsContainer;

    private TextView clearanceProductsNotFound;
    private TextView dailyDealsenProductsNotFound;
    private TextView seenProductsNotFound;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        LinearLayout personalFeedLayout = (LinearLayout) inflater.inflate(R.layout.personal_feed, container, false);

        clearanceContainer = (LinearLayout) personalFeedLayout.findViewById(R.id.clearance_container);
        dailyDealContainer = (LinearLayout) personalFeedLayout.findViewById(R.id.daily_deal_container);
        seenProductsContainer = (LinearLayout) personalFeedLayout.findViewById(R.id.seen_products_container);

        clearanceProductsNotFound = (TextView) personalFeedLayout.findViewById(R.id.clearance_empty);
        dailyDealsenProductsNotFound = (TextView) personalFeedLayout.findViewById(R.id.daily_deal_empty);
        seenProductsNotFound = (TextView) personalFeedLayout.findViewById(R.id.seen_products_empty);

        DailyDealProductCollection dailyDealProductCollection = new DailyDealProductCollection();
        dailyDealProductCollection.getProducts(DAILY_DEAL_IDENTIFIER, 1, 50, this); // identifier, offset, limit, callback

        ClearanceProductCollection clearanceProductCollection = new ClearanceProductCollection();
        clearanceProductCollection.getProducts(CLEARANCE_IDENTIFIER, 1, 50, this); // identifier, offset, limit, callback

        setSeenProductsAdapter();

        return (personalFeedLayout);
    }

    private void setSeenProductsAdapter(){
        // set seen products list
        PersistentSizedArrayList<SeenProductsRowItem> saveSeenProducts =
                PersonalFeedSingleton.getInstance(getActivity()).getSavedSeenProducts(getActivity());

        // display "nothing found" if no seen products
        if(saveSeenProducts == null){
            seenProductsNotFound.setVisibility(View.VISIBLE);
        }
        else{
            seenProductsNotFound.setVisibility(View.GONE);
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        for(SeenProductsRowItem savedSeenProduct : saveSeenProducts){
            View seenSavedProductRow = inflater.inflate(R.layout.personal_feed_product_item, null);

            TextView title = (TextView) seenSavedProductRow.findViewById(R.id.title);
            title.setText(savedSeenProduct.getProduceName());

            RatingStars ratingStars = (RatingStars) seenSavedProductRow.findViewById(R.id.rating);
            ratingStars.setRating(Float.parseFloat(savedSeenProduct.getRating()),
                    Integer.parseInt(savedSeenProduct.getReviewCount()));

            PriceSticker priceSticker = (PriceSticker) seenSavedProductRow.findViewById(R.id.pricing);
            priceSticker.setPricing(Float.parseFloat(savedSeenProduct.getCurrentPrice()),
                    savedSeenProduct.getUnitOfMeasure());

            ImageView imageView = (ImageView) seenSavedProductRow.findViewById(R.id.image);
            Picasso.with(getActivity()).load(savedSeenProduct.getImageUrl()).error(R.drawable.no_photo).into(imageView);

            seenProductsContainer.addView(seenSavedProductRow);

            // Set listener for all the items
            final String sku = savedSeenProduct.getSku();
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

            Log.d(TAG, "Saved seen products: " + savedSeenProduct.getProduceName());
        }
    }

    @Override
    public void onProductCollectionResult(List<Product> dailyDealProducts) {
        HashSet<String> dailyDealSkuSet = new HashSet<String>();
        for(Product p: dailyDealProducts){
            dailyDealSkuSet.add(p.getSku());
            Log.d(TAG, "Daily Deal Products: " + p.getProductName() + "-" + p.getSku());
        }

        List<CartItem> cartItems = CartFragment.getListItems();
        for (CartItem cartItem : cartItems) {
            String cartItemSku = cartItem.getProduct().getSku();
            if(dailyDealSkuSet.contains(cartItemSku)){
                fillContainer(cartItem, dailyDealContainer);
                Log.d(TAG, "Daily deal products in cart: " + cartItem.getProduct().getProductName());
            }
        }

        // display "nothing found" if no daily deal products
        if(dailyDealContainer.getChildCount() == 0){
            dailyDealsenProductsNotFound.setVisibility(View.VISIBLE);
        }
        else{
            dailyDealsenProductsNotFound.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClearanceProductsResult(List<Product> clearanceProducts) {
        HashSet<String> clearanceSkuSet = new HashSet<String>();
        for(Product p: clearanceProducts){
            clearanceSkuSet.add(p.getSku());
            Log.d(TAG, "Clearance Products: " + p.getProductName() + "-" + p.getSku());
        }

        List<CartItem> cartItems = CartFragment.getListItems();
        for (CartItem cartItem : cartItems) {
            String cartItemSku = cartItem.getProduct().getSku();
            if(clearanceSkuSet.contains(cartItemSku)){
                fillContainer(cartItem, clearanceContainer);
                Log.d(TAG, "Clearance products in cart: " + cartItem.getProduct().getProductName());
            }
        }

        // display "nothing found" if no clearance products
        if(clearanceContainer.getChildCount() == 0){
            clearanceProductsNotFound.setVisibility(View.VISIBLE);
        }
        else{
            clearanceProductsNotFound.setVisibility(View.GONE);
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
}
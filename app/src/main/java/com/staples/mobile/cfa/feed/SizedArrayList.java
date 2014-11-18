package com.staples.mobile.cfa.feed;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Author: Yongnan Zhou
 */

public class SizedArrayList<T> extends ArrayList<T> {
    private static final String TAG = "SizedArrayList";
    private int maxSize;

    public SizedArrayList(int size) {
        super();
        this.maxSize = size;
    }

    // save seen product not persistently
    public boolean addSeenProduct(T object, String sku){
        boolean isDone = super.add(object);

        PersonalFeedSingleton feedSingleton = PersonalFeedSingleton.getInstance();
        HashSet<String> savedSkus = feedSingleton.getSavedSkus();

        // Remove elements until it's the right size.
        if (size() > maxSize){
            // remove the earliest saved sku
            savedSkus.remove(sku);
            // remove the earliest saved product
            this.remove(0);
        }

        savedSkus.add(sku);
        return isDone;
    }

    // save seen product persistently
    public boolean addSeenProduct(T object, String sku, Activity activity){
        boolean isDone = super.add(object);
        SeenProductsRowItem seenProduct = (SeenProductsRowItem) object;

        saveSeenProductInPhone(seenProduct, activity);

        PersonalFeedSingleton feedSingleton = PersonalFeedSingleton.getInstance();
        HashSet<String> savedSkus = feedSingleton.getSavedSkus(activity);

        // Remove elements until it's the right size.
        if (size() > maxSize){
            // remove the earliest saved sku
            savedSkus.remove(sku);
            // remove the earliest saved product
            this.remove(0);

            //saveLastSeenProductInPhone(this);
        }

        savedSkus.add(sku);
        return isDone;
    }

    private void saveSeenProductInPhone(SeenProductsRowItem seenProduct, Activity activity){
        SharedPreferences sp = activity.getSharedPreferences("SAVED_SEEN_PRODUCTS", activity.MODE_PRIVATE);

        final String FIELD_SEPARATOR = "/_-_/";
        final String OBJECT_SEPARATOR = "/_&_/";
        String savedSkuString = sp.getString("SEEN_PRODUCT_SKU_LIST", "");
        String savedProductString = sp.getString("SEEN_PRODUCT_LIST", "");

        // first time
        if(savedSkuString.equals("") && savedProductString.equals("")){
            savedSkuString = seenProduct.getSku();

            savedProductString = seenProduct.getSku() + FIELD_SEPARATOR
                    + seenProduct.getProduceName() + FIELD_SEPARATOR
                    + seenProduct.getCurrentPrice() + FIELD_SEPARATOR
                    + seenProduct.getReviewCount() + FIELD_SEPARATOR
                    + seenProduct.getRating() + FIELD_SEPARATOR
                    + seenProduct.getUnitOfMeasure() + FIELD_SEPARATOR
                    + seenProduct.getImageUrl();
        }
        else{
            savedSkuString = savedSkuString + FIELD_SEPARATOR + seenProduct.getSku();

            String productString = seenProduct.getSku() + FIELD_SEPARATOR
                    + seenProduct.getProduceName() + FIELD_SEPARATOR
                    + seenProduct.getCurrentPrice() + FIELD_SEPARATOR
                    + seenProduct.getReviewCount() + FIELD_SEPARATOR
                    + seenProduct.getRating() + FIELD_SEPARATOR
                    + seenProduct.getUnitOfMeasure() + FIELD_SEPARATOR
                    + seenProduct.getImageUrl();
            savedProductString = savedProductString + OBJECT_SEPARATOR + productString;
        }

        // save updated data
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("SEEN_PRODUCT_SKU_LIST", savedSkuString);
        editor.putString("SEEN_PRODUCT_LIST", savedProductString);
        editor.commit();

        Log.d(TAG, "Saved seen products successfully! -> " + savedProductString);
    }
}

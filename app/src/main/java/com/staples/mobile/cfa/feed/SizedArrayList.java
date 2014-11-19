package com.staples.mobile.cfa.feed;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

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

        PersonalFeedSingleton feedSingleton = PersonalFeedSingleton.getInstance(activity);
        HashSet<String> savedSkuSet = feedSingleton.getSavedSkus(activity);

        // Remove elements until it's the right size.
        if (size() > maxSize){
            // remove the earliest saved sku in the set
            savedSkuSet.remove(sku);

            // remove the earliest saved product in the list
            this.remove(0);

            // set updated seen skus
            feedSingleton.setSavedSkus(savedSkuSet);

            // set updated seen products list
            feedSingleton.setSavedSeenProducts((SizedArrayList<SeenProductsRowItem>) this);

            updateSeenProductsInPhone(activity);
        }

        savedSkuSet.add(sku);

        return isDone;
    }

    private void saveSeenProductInPhone(SeenProductsRowItem seenProduct, Activity activity){
        SharedPreferences sp = activity.getSharedPreferences("SAVED_SEEN_PRODUCTS", activity.MODE_PRIVATE);

        final String FIELD_SEPARATOR = "/_-_/";
        final String OBJECT_SEPARATOR = "/_&_/";
        String savedSkusString = sp.getString("SEEN_PRODUCT_SKU_LIST", "");
        String savedProductsString = sp.getString("SEEN_PRODUCT_LIST", "");

        // first item
        if(savedSkusString.equals("") && savedProductsString.equals("")){
            savedSkusString = seenProduct.getSku();

            savedProductsString = seenProduct.getSku() + FIELD_SEPARATOR
                    + seenProduct.getProduceName() + FIELD_SEPARATOR
                    + seenProduct.getCurrentPrice() + FIELD_SEPARATOR
                    + seenProduct.getReviewCount() + FIELD_SEPARATOR
                    + seenProduct.getRating() + FIELD_SEPARATOR
                    + seenProduct.getUnitOfMeasure() + FIELD_SEPARATOR
                    + seenProduct.getImageUrl();
        }
        else{
            savedSkusString = savedSkusString + FIELD_SEPARATOR + seenProduct.getSku();

            String productString = seenProduct.getSku() + FIELD_SEPARATOR
                    + seenProduct.getProduceName() + FIELD_SEPARATOR
                    + seenProduct.getCurrentPrice() + FIELD_SEPARATOR
                    + seenProduct.getReviewCount() + FIELD_SEPARATOR
                    + seenProduct.getRating() + FIELD_SEPARATOR
                    + seenProduct.getUnitOfMeasure() + FIELD_SEPARATOR
                    + seenProduct.getImageUrl();
            savedProductsString = savedProductsString + OBJECT_SEPARATOR + productString;
        }

        // save updated data
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("SEEN_PRODUCT_SKU_LIST", savedSkusString);
        editor.putString("SEEN_PRODUCT_LIST", savedProductsString);
        editor.commit();

        Log.d(TAG, "Saved seen products successfully! -> " + savedProductsString);
    }

    private void updateSeenProductsInPhone(Activity activity){
        SharedPreferences sp = activity.getSharedPreferences("SAVED_SEEN_PRODUCTS", activity.MODE_PRIVATE);

        String savedSkusString = "";
        String savedProductsString = "";

        final String FIELD_SEPARATOR = "/_-_/";
        final String OBJECT_SEPARATOR = "/_&_/";

        PersonalFeedSingleton feedSingleton = PersonalFeedSingleton.getInstance();

        // update saved skus after remove the first one
        HashSet<String> savedSkus = feedSingleton.getSavedSkus();
        for(String savedSku: savedSkus){
            if(savedSkusString.equals("")){
                savedSkusString = savedSku;
            }
            else{
                savedSkusString = savedSkusString + FIELD_SEPARATOR + savedSku;
            }
        }

        // update saved products after remove the first one
        SizedArrayList<SeenProductsRowItem> savedProducts = feedSingleton.getSavedSeenProducts();
        for(SeenProductsRowItem savedProduct : savedProducts){
            String savedSeenProductString = savedProduct.getSku() + FIELD_SEPARATOR
                    + savedProduct.getProduceName() + FIELD_SEPARATOR
                    + savedProduct.getCurrentPrice() + FIELD_SEPARATOR
                    + savedProduct.getReviewCount() + FIELD_SEPARATOR
                    + savedProduct.getRating() + FIELD_SEPARATOR
                    + savedProduct.getUnitOfMeasure() + FIELD_SEPARATOR
                    + savedProduct.getImageUrl();
            if(savedProductsString.equals("")){
                savedProductsString = savedSeenProductString;
            }
            else{
                savedProductsString = savedProductsString + OBJECT_SEPARATOR + savedSeenProductString;
            }
        }

        // save updated data
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("SEEN_PRODUCT_SKU_LIST", savedSkusString);
        editor.putString("SEEN_PRODUCT_LIST", savedProductsString);
        editor.commit();

        Log.d(TAG, "Updated seen products successfully! -> " + savedProductsString);
    }
}

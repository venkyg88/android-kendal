package com.staples.mobile.cfa.feed;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Author: Yongnan Zhou
 */

public class PersistentSizedArrayList<T> extends ArrayList<T> {
    private static final String TAG = "PersistentSizedArrayList";

    public static final String SAVED_SEEN_PRODUCTS = "SAVED_SEEN_PRODUCTS";
    public static final String SEEN_PRODUCT_SKU_LIST = "SEEN_PRODUCT_SKU_LIST";
    public static final String SEEN_PRODUCT_LIST = "SEEN_PRODUCT_LIST";

    public static final String FIELD_SEPARATOR = "/_-_/";

    private int maxListSize;

    public PersistentSizedArrayList(int size) {
        super();
        this.maxListSize = size;
    }

    // add sku only
    public void addSeenProduct(T sku, Activity activity){
        super.add(sku);

        // add the last seen product's sku in the phone
        saveSeenProductInPhone(((String) sku), activity);

        PersonalFeedSingleton feedSingleton = PersonalFeedSingleton.getInstance(activity);
        HashSet<String> savedSkuSet = feedSingleton.getSavedSkus(activity);

        // Remove elements until it's the right size.
        if (size() > maxListSize){
            // remove the earliest saved sku in the set
            String firstSavedSku = (String) this.get(0);
            savedSkuSet.remove(firstSavedSku);

            // remove the earliest saved product in the list
            this.remove(0);

            Log.d(TAG, "Removed the earliest seen product successfully! SKU:" + firstSavedSku);

            // set updated seen skus to singleton
            feedSingleton.setSavedSkus(savedSkuSet);

            // set updated seen products list to singleton
            feedSingleton.setSavedSeenProducts((PersistentSizedArrayList<String>) this);

            // use current set and list to update the data in the phone
            updateSeenProductsInPhone(activity);
        }
        else {
            savedSkuSet.add(((String) sku));

            // set updated seen skus to singleton
            feedSingleton.setSavedSkus(savedSkuSet);

            // set updated seen products list to singleton
            feedSingleton.setSavedSeenProducts((PersistentSizedArrayList<String>) this);
        }
    }

    private void saveSeenProductInPhone(String sku, Activity activity){
        SharedPreferences sp = activity.getSharedPreferences(SAVED_SEEN_PRODUCTS, activity.MODE_PRIVATE);
        String savedSkusString = sp.getString(SEEN_PRODUCT_SKU_LIST, "");
        String savedProductsString = sp.getString(SEEN_PRODUCT_LIST, "");

        // first item
        if(savedSkusString.equals("") && savedProductsString.equals("")){
            savedSkusString = sku;

            savedProductsString = sku + FIELD_SEPARATOR;
        }
        else{
            savedSkusString = savedSkusString + FIELD_SEPARATOR + sku;

            savedProductsString = savedProductsString + FIELD_SEPARATOR + sku;
        }

        // save updated data
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SEEN_PRODUCT_SKU_LIST, savedSkusString);
        editor.putString(SEEN_PRODUCT_LIST, savedProductsString);
        editor.commit();

        Log.d(TAG, "Saved this seen product successfully! -> " + savedProductsString);
    }

    // called for update the list when there are more than 3 items
    public void updateSeenProductsInPhone(Activity activity){
        SharedPreferences sp = activity.getSharedPreferences(SAVED_SEEN_PRODUCTS, activity.MODE_PRIVATE);

        String savedSkusString = "";
        String savedProductsString = "";

        PersonalFeedSingleton feedSingleton = PersonalFeedSingleton.getInstance(activity);

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
        PersistentSizedArrayList<String> savedProducts = feedSingleton.getSavedSeenProducts();
        for(String savedProductSku : savedProducts){
            String savedSeenProductString = savedProductSku;
            if(savedProductsString.equals("")){
                savedProductsString = savedSeenProductString;
            }
            else{
                savedProductsString = savedProductsString + FIELD_SEPARATOR + savedSeenProductString;
            }
        }

        // save updated data
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SEEN_PRODUCT_SKU_LIST, savedSkusString);
        editor.putString(SEEN_PRODUCT_LIST, savedProductsString);
        editor.commit();

        Log.d(TAG, "Updated seen products successfully! -> " + savedProductsString);
    }


    // save seen product persistently
//    public void addSeenProduct(T object, String sku, Activity activity){
//        super.add(object);
//
//        // add the last seen product in the phone
//        SeenProductsRowItem notSavedSeenProduct = (SeenProductsRowItem) object;
//        saveSeenProductInPhone(notSavedSeenProduct, activity);
//
//        PersonalFeedSingleton feedSingleton = PersonalFeedSingleton.getInstance(activity);
//        HashSet<String> savedSkuSet = feedSingleton.getSavedSkus(activity);
//
//        // Remove elements until it's the right size.
//        if (size() > maxListSize){
//            // remove the earliest saved sku in the set
//            SeenProductsRowItem firstSavedProduct = (SeenProductsRowItem) this.get(0);
//            savedSkuSet.remove(firstSavedProduct.getSku());
//
//            // remove the earliest saved product in the list
//            this.remove(0);
//
//            // set updated seen skus to singleton
//            feedSingleton.setSavedSkus(savedSkuSet);
//
//            // set updated seen products list to singleton
//            feedSingleton.setSavedSeenProducts((PersistentSizedArrayList<SeenProductsRowItem>) this);
//
//            // use current set and list to update the data in the phone
//            updateSeenProductsInPhone(activity);
//        }
//        else {
//            savedSkuSet.add(sku);
//
//            // set updated seen skus to singleton
//            feedSingleton.setSavedSkus(savedSkuSet);
//
//            // set updated seen products list to singleton
//            feedSingleton.setSavedSeenProducts((PersistentSizedArrayList<SeenProductsRowItem>) this);
//        }
//    }

    //    private void saveSeenProductInPhone(SeenProductsRowItem notSavedSeenProduct, Activity activity){
//        SharedPreferences sp = activity.getSharedPreferences(SAVED_SEEN_PRODUCTS, activity.MODE_PRIVATE);
//        String savedSkusString = sp.getString(SEEN_PRODUCT_SKU_LIST, "");
//        String savedProductsString = sp.getString(SEEN_PRODUCT_LIST, "");
//
//        // first item
//        if(savedSkusString.equals("") && savedProductsString.equals("")){
//            savedSkusString = notSavedSeenProduct.getSku();
//
//            savedProductsString = notSavedSeenProduct.getSku() + FIELD_SEPARATOR
//                    + notSavedSeenProduct.getProductName() + FIELD_SEPARATOR
//                    + notSavedSeenProduct.getCurrentPrice() + FIELD_SEPARATOR
//                    + notSavedSeenProduct.getReviewCount() + FIELD_SEPARATOR
//                    + notSavedSeenProduct.getRating() + FIELD_SEPARATOR
//                    + notSavedSeenProduct.getUnitOfMeasure() + FIELD_SEPARATOR
//                    + notSavedSeenProduct.getImageUrl();
//        }
//        else{
//            savedSkusString = savedSkusString + FIELD_SEPARATOR + notSavedSeenProduct.getSku();
//
//            String productString = notSavedSeenProduct.getSku() + FIELD_SEPARATOR
//                    + notSavedSeenProduct.getProductName() + FIELD_SEPARATOR
//                    + notSavedSeenProduct.getCurrentPrice() + FIELD_SEPARATOR
//                    + notSavedSeenProduct.getReviewCount() + FIELD_SEPARATOR
//                    + notSavedSeenProduct.getRating() + FIELD_SEPARATOR
//                    + notSavedSeenProduct.getUnitOfMeasure() + FIELD_SEPARATOR
//                    + notSavedSeenProduct.getImageUrl();
//            savedProductsString = savedProductsString + OBJECT_SEPARATOR + productString;
//        }
//
//        // save updated data
//        SharedPreferences.Editor editor = sp.edit();
//        editor.putString(SEEN_PRODUCT_SKU_LIST, savedSkusString);
//        editor.putString(SEEN_PRODUCT_LIST, savedProductsString);
//        editor.commit();
//
//        Log.d(TAG, "Saved this seen product successfully! -> " + savedProductsString);
//    }
}

package com.staples.mobile.cfa.feed;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;

/**
 * Author: Yongnan Zhou
 */

public class PersonalFeedSingleton {
    private static final String TAG = "PersonalFeedSingleton";
    private static final int SEEN_PRODUCTS_AMOUNT = 3;

    private static PersonalFeedSingleton personalFeedSingleton = null;
    private PersistentSizedArrayList<SeenProductsRowItem> savedSeenProducts = null;
    private HashSet<String> savedSkus = null;

    private PersonalFeedSingleton(Activity activity){
        savedSeenProducts = getSavedSeenProducts(activity);
        savedSkus = getSavedSkus(activity);
    }

    public static PersonalFeedSingleton getInstance(Activity activity) {
        if(personalFeedSingleton == null){
            personalFeedSingleton = new PersonalFeedSingleton(activity);
        }
        return personalFeedSingleton;
    }

    public PersistentSizedArrayList<SeenProductsRowItem> getSavedSeenProducts(){
        return savedSeenProducts;
    }

    // get seen products from the phone
    public PersistentSizedArrayList<SeenProductsRowItem> getSavedSeenProducts(Activity activity) {
        SharedPreferences sp =
                activity.getSharedPreferences(PersistentSizedArrayList.SAVED_SEEN_PRODUCTS, activity.MODE_PRIVATE);
        String savedProductsString = sp.getString(PersistentSizedArrayList.SEEN_PRODUCT_LIST, "");

        // initialize SeenProductsRowItem list
        PersistentSizedArrayList<SeenProductsRowItem> savedProductsList
                = new PersistentSizedArrayList<SeenProductsRowItem>(SEEN_PRODUCTS_AMOUNT);;

        if (!savedProductsString.equals("")) {
            SeenProductsRowItem savedSeenProduct;
            String[] savedProductsArray = savedProductsString.split(PersistentSizedArrayList.OBJECT_SEPARATOR);

            for (int i = 0; i < savedProductsArray.length; i++) {
                String savedProductString = savedProductsArray[i];
                String[] productFields = savedProductString.split(PersistentSizedArrayList.FIELD_SEPARATOR);

                // safe check for empty fields
                if(productFields.length > 1) {
                    String sku = productFields[0];
                    String productName = productFields[1];
                    String currentPrice = productFields[2];
                    String reviewCount = productFields[3];
                    String rating = productFields[4];
                    String unitOfMeasure = productFields[5];
                    String imageUrl = productFields[6];

                    savedSeenProduct = new SeenProductsRowItem(sku, productName, currentPrice, reviewCount,
                            rating, unitOfMeasure, imageUrl);

                    savedProductsList.add(savedSeenProduct);

                    // Log.d(TAG, i + 1 + "th Saved Seen Product -> " + savedProductString);
                }
            }
        }
        else{
            Log.d(TAG, "No Saved Seen Products Found!");
        }

       return savedProductsList;
    }

    public HashSet<String> getSavedSkus(){
        return savedSkus;
    }

    // get seen products' sku from the phone
    public HashSet<String> getSavedSkus(Activity activity) {
        SharedPreferences sp =
                activity.getSharedPreferences(PersistentSizedArrayList.SAVED_SEEN_PRODUCTS, activity.MODE_PRIVATE);

        String savedSkusString = sp.getString(PersistentSizedArrayList.SEEN_PRODUCT_SKU_LIST, "");

        HashSet<String> savedProductSkuSet = new HashSet<String>();

        if (!savedSkusString.equals("")) {
            String[] savedSkusArray = savedSkusString.split(PersistentSizedArrayList.FIELD_SEPARATOR);

            // safe check for empty fields
            if(savedSkusArray.length > 0) {
                for (int i = 0; i < savedSkusArray.length; i++) {
                    savedProductSkuSet.add(savedSkusArray[i]);
                    // Log.d(TAG, i + 1 + "th Saved Sku -> " + savedSkusArray[i]);
                }
            }
            else{
                Log.d(TAG, "savedSkusArray[] has nothing. No Saved Sku Yet!");
            }
        }
        else{
            Log.d(TAG, "savedSkusString is empty. No Saved Sku Yet!");
        }

        return savedProductSkuSet;
    }

    public void setSavedSeenProducts(PersistentSizedArrayList<SeenProductsRowItem> updatedSeenProducts){
        savedSeenProducts = updatedSeenProducts;
    }

    public void setSavedSkus(HashSet<String> updatedSkuSet){
        savedSkus = updatedSkuSet;
    }

}

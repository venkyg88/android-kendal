package com.staples.mobile.cfa.feed;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;

/**
 * Author: Yongnan Zhou
 */

public class PersonalFeedSingleton {
    private static final String TAG = "PersonalFeedData";
    private static final int SEEN_PRODUCTS_AMOUNT = 3;

    private static PersonalFeedSingleton personalFeedSingleton = null;
    private SizedArrayList<SeenProductsRowItem> savedSeenProducts = null;
    private HashSet<String> savedSkus = null;

    private PersonalFeedSingleton(int size){
        savedSeenProducts = new SizedArrayList<SeenProductsRowItem>(size);
        savedSkus = new HashSet<String>();
    }

    public static PersonalFeedSingleton getInstance( ) {
        if(personalFeedSingleton == null){
            personalFeedSingleton = new PersonalFeedSingleton(SEEN_PRODUCTS_AMOUNT);
        }
        return personalFeedSingleton;
    }

    // get seen products not persistently
    public SizedArrayList<SeenProductsRowItem> getSavedSeenProducts(){
        return savedSeenProducts;
    }

    // get seen products not persistently
    public SizedArrayList<SeenProductsRowItem> getSavedSeenProducts(Activity activity) {
        // Get saved seen products info from the phone
        SharedPreferences sp = activity.getSharedPreferences("SAVED_SEEN_PRODUCTS", activity.MODE_PRIVATE);
        String savedProductsString = sp.getString("SEEN_PRODUCT_LIST", "");

        final String FIELD_SEPARATOR = "/_-_/";
        final String OBJECT_SEPARATOR = "/_&_/";

        // initialize SeenProductsRowItem list
        SizedArrayList<SeenProductsRowItem> savedProductsList = new SizedArrayList<SeenProductsRowItem>(3);;

        if (!savedProductsString.equals("")) {
            SeenProductsRowItem savedSeenProduct;
            String[] savedProductsArray = savedProductsString.split(OBJECT_SEPARATOR);

            for (int i = 0; i < savedProductsArray.length; i++) {
                String savedProductString = savedProductsArray[i];
                String[] productFields = savedProductString.split(FIELD_SEPARATOR);

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
                    Log.d(TAG, i + "th Saved Seen Product -> " + savedProductString);
                }
                else{
                    Log.d(TAG, "No Saved Seen Products Found!");
                }
            }
        }

       return savedProductsList;
    }

    // get seen products' sku not persistently
    public HashSet<String> getSavedSkus(){
        return savedSkus;
    }

    // get seen products' sku persistently
    public HashSet<String> getSavedSkus(Activity activity) {
        // Get saved seen products info from the phone
        SharedPreferences sp = activity.getSharedPreferences("SAVED_SEEN_PRODUCTS", activity.MODE_PRIVATE);
        String savedSkusString = sp.getString("SEEN_PRODUCT_SKU_LIST", "");

        final String FIELD_SEPARATOR = "/_-_/";

        // initialize SeenProductsRowItem list
        HashSet<String> savedProductSkus = new HashSet<String>();;

        if (!savedSkusString.equals("")) {
            String[] savedSkusArray = savedSkusString.split(FIELD_SEPARATOR);

            // safe check for empty fields
            if(savedSkusArray.length > 0) {
                for (int i = 0; i < savedSkusArray.length; i++) {
                    savedProductSkus.add(savedSkusArray[i]);
                    Log.d(TAG, i + "th Saved Sku -> " + savedSkusArray[i]);
                }
            }
            else{
                Log.d(TAG, "No Saved Sku Yet!");
            }
        }

        return savedProductSkus;
    }
}

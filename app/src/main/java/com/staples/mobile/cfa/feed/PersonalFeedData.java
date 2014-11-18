package com.staples.mobile.cfa.feed;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Author: Yongnan Zhou
 */

public class PersonalFeedData {
    private static final String TAG = "PersonalFeedData";
    private static PersonalFeedData singleton = null;
    private static final int SEEN_PRODUCTS_AMOUNT = 3;
    private SizedArrayList<SeenProductsRowItem> savedSeenProducts = null;
    private HashSet<String> savedSkus = null;

    private PersonalFeedData(int size){
        savedSeenProducts = new SizedArrayList<SeenProductsRowItem>(size);
        savedSkus = new HashSet<String>();
    }

    public static PersonalFeedData getInstance( ) {
        if(singleton == null){
            singleton = new PersonalFeedData(SEEN_PRODUCTS_AMOUNT);
        }
        return singleton;
    }

    public SizedArrayList<SeenProductsRowItem> getSavedSeenProducts(){
        return savedSeenProducts;
    }

    public void getSavedSeenProducts(Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences("SAVED_SEEN_PRODUCTS", activity.MODE_PRIVATE);

        String savedSkuString = sp.getString("SKU_LIST", "");
        String savedProductString = sp.getString("SEEN_PRODUCT_LIST", "");

        final String FIELD_SEPERATOR = "/_/";
        final String OBJECT_SEPERATOR = "/&/";

        if (!savedProductString.equals("")) {
            String[] savedProductsArray = savedProductString.split(OBJECT_SEPERATOR);

            SizedArrayList<SeenProductsRowItem> savedProductsList = new SizedArrayList<SeenProductsRowItem>(3);
            for (int i = 0; i < savedProductsArray.length; i++) {
                Log.d(TAG, "Each Saved Product -> " + savedProductsArray[i]);
                //savedProductsList.add(savedProductsArray[i]);
            }
        }

       //return savedProductsList;
    }

    public HashSet<String> getSavedSku(){
        return savedSkus;
    }
}

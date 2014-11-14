package com.staples.mobile.cfa.feed;

import java.util.HashSet;

/**
 * Author: Yongnan Zhou
 */

public class PersonalFeedData {
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

    public HashSet<String> getSavedSku(){
        return savedSkus;
    }
}

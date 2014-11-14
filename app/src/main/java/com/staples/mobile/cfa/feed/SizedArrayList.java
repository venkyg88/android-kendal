package com.staples.mobile.cfa.feed;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Author: Yongnan Zhou
 */

public class SizedArrayList<T> extends ArrayList<T> {
    private int maxSize;

    public SizedArrayList(int size) {
        super();
        this.maxSize = size;
    }

    public boolean addSeenProduct(T object, String sku){
        boolean isDone = super.add(object);
        PersonalFeedData feedSingleton = PersonalFeedData.getInstance();
        HashSet<String> savedSkus = feedSingleton.getSavedSku();

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
}

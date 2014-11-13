package com.staples.mobile.cfa.feed;

import java.util.Stack;

/**
 * Author: Yongnan Zhou
 */

public class PersonalFeedData extends SizedStack<SeenProductsRowItem> {
    private static PersonalFeedData singleton = null;
    private static final int SEEN_PRODUCTS_AMOUNT = 3;

    private PersonalFeedData(int size){
        super(size);
    }

    public static PersonalFeedData getInstance( ) {
        if(singleton == null){
            singleton = new PersonalFeedData(SEEN_PRODUCTS_AMOUNT);
        }
        return singleton;
    }
}

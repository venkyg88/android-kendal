package com.staples.mobile.cfa.feed;

import java.util.ArrayList;

/**
 * Author: Yongnan Zhou
 */

public class SizedArrayList<T> extends ArrayList<T> {
    private int maxSize;

    public SizedArrayList(int size) {
        super();
        this.maxSize = size;
    }

    public boolean add(T object){
        boolean isDone = super.add(object);

        // Remove elements until it's the right size.
        if (size() > maxSize){
            this.remove(0);
        }

        return isDone;
    }
}

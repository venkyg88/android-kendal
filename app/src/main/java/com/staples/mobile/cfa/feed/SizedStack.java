package com.staples.mobile.cfa.feed;

import java.util.Stack;

/**
 * Author: Yongnan Zhou
 */

public class SizedStack<T> extends Stack<T> {
    private int maxSize;

    public SizedStack(int size) {
        super();
        this.maxSize = size;
    }

    @Override
    public Object push(Object object) {
        // Remove elements until it's the right size.
        while (this.size() >= maxSize) {
            this.remove(0);
        }
        return super.push((T) object);
    }
}

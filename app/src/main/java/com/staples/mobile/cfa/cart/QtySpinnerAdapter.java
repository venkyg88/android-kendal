/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.cart;

import android.content.Context;
import android.widget.ArrayAdapter;

/**
 * Created by sutdi001 on 10/14/14.
 */
public class QtySpinnerAdapter extends ArrayAdapter<String> {

    public QtySpinnerAdapter(Context context) {
        super(context, android.R.layout.simple_spinner_item);
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // add items
        int upperLimit = 5;
        for (int i = 0; i < upperLimit; i++) {
            add(""+i);
        }
        add(upperLimit + "+");
    }

}

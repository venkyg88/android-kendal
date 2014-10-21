/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by sutdi001 on 10/21/14.
 */
public class CartItemQtyEditor extends EditText {

    TextWatcher textChangedListener;


    public CartItemQtyEditor(Context context) {
        super(context);
    }

    public CartItemQtyEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CartItemQtyEditor(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /** allows addition of one text changed listener, removes previous one */
    public void setTextChangedListener(TextWatcher newTextChangedListener) {
        if (textChangedListener != null) {
            removeTextChangedListener(textChangedListener);
        }
        textChangedListener = newTextChangedListener;
        addTextChangedListener(newTextChangedListener);
    }
}

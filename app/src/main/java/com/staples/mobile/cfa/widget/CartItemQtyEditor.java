/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.staples.mobile.R;

/**
 * Created by sutdi001 on 10/21/14.
 */
public class CartItemQtyEditor extends FrameLayout {

    public static final int DEFAULT_MAX_SPINNER_VALUE = 5;

    private Context context;
    private AdapterView.OnItemSelectedListener spinnerSelectionListener;
    private TextWatcher textChangedListener;
    private EditText editText;
    private Spinner spinner;
    private NumericSpinnerAdapter spinnerAdapter;
    private int maxSpinnerValue;

//    public CartItemQtyEditor(Context context) {
//        super(context);
//        initView(context, null);
//    }

    public CartItemQtyEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

//    public CartItemQtyEditor(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        initView(context, attrs);
//    }

    private void initView(Context context, AttributeSet attrs) {

        this.context = context;

        // inflate
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.cart_item_qty_editor, this);
        editText = (EditText)view.findViewById(R.id.cartitem_qty_edittext);
        spinner = (Spinner)view.findViewById(R.id.cartitem_qty_spinner);

        // get attributes from layout if any
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.QtyEditor);
            // get max spinner value attribute from layout if it exists
            maxSpinnerValue = a.getInteger(R.styleable.QtyEditor_maxSpinnerValue, DEFAULT_MAX_SPINNER_VALUE);
            a.recycle();
        }

        // create spinner adapter and bind to spinner
        spinnerAdapter = new NumericSpinnerAdapter(context, maxSpinnerValue);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new NumericSpinnerAdapterItemSelectedListener());
    }


    /** returns qty value from appropriate widget */
    public int getQtyValue(int defaultValue) {
        String value;
        if (isSpinnerVisible()) {
            value = spinnerAdapter.getItem(spinner.getSelectedItemPosition());
        } else {
            value = editText.getText().toString();
        }
        if (value != null && value.length() > 0) {
            try { return Integer.parseInt(value); } catch (NumberFormatException e) {}
        }

        return defaultValue;
    }

    /** sets qty value in appropriate widget */
    public void setQtyValue(int qty) {
        String strQty = ""+qty;
        int spinnerPosition = spinnerAdapter.getPosition(strQty);

        // if specified value exists in the spinner, use the spinner, otherwise the EditText
        if (spinnerPosition >= 0) {
            spinner.setVisibility(View.VISIBLE);
            editText.setVisibility(View.GONE);
            spinner.setSelection(spinnerPosition);
        } else {
            boolean switchingFromSpinner = isSpinnerVisible();
            spinner.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
            editText.setText(strQty);
            if (switchingFromSpinner) {
                showSoftKeyboard();
            }
        }
    }


    /** sets spinner selection listener on the spinner widget */
    public void setSpinnerSelectionListener(AdapterView.OnItemSelectedListener spinnerSelectionListener) {
        this.spinnerSelectionListener = spinnerSelectionListener;
    }

    /** sets text-changed listener on the editText widget
     * (allows addition of only one text changed listener, removes previous one) */
    public void setTextChangedListener(TextWatcher newTextChangedListener) {
        if (textChangedListener != null) {
            editText.removeTextChangedListener(textChangedListener);
        }
        textChangedListener = newTextChangedListener;
        editText.addTextChangedListener(newTextChangedListener);
    }

    /** passes thru to editText widget */
    public void setOnEditorActionListener(TextView.OnEditorActionListener listener) {
        editText.setOnEditorActionListener(listener);
    }

    public void hideSoftKeyboard() {
        if (isEditTextVisible()) {
            InputMethodManager keyboard = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    public void showSoftKeyboard() {
        InputMethodManager keyboard = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(editText, 0);

        editText.requestFocus();
        ((Activity)context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//	    context.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }

    private boolean isEditTextVisible() {
        return View.VISIBLE == editText.getVisibility();
    }

    private boolean isSpinnerVisible() {
        return View.VISIBLE == spinner.getVisibility();
    }

    // --------------------------------------------- //
    // ------------- internal classes -------------- //
    // --------------------------------------------- //

    /** listener class for spinner selection */
    class NumericSpinnerAdapterItemSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // if selection out of range, call setQtyValue to revert to editText widget
            String value = ((TextView)view).getText().toString();
            if (value != null && value.endsWith("+")) {
                setQtyValue(maxSpinnerValue + 1);
            } else {
                // otherwise notify listener of valid item selection
                if (spinnerSelectionListener != null) {
                    spinnerSelectionListener.onItemSelected(parent, view, position, id);
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            if (spinnerSelectionListener != null) {
                spinnerSelectionListener.onNothingSelected(parent);
            }
        }
    }

    /** numeric spinner adapter */
    public class NumericSpinnerAdapter extends ArrayAdapter<String> {
        public NumericSpinnerAdapter(Context context, int maxValue) {
            super(context, android.R.layout.simple_spinner_item);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // add items
            for (int i = 0; i <= maxValue; i++) {
                add(""+i);
            }
            add((maxValue+1) + "+");
        }
    }
}

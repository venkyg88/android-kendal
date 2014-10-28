/*
 * Copyright (c) 2014 Staples, Inc. All rights reserved.
 */

package com.staples.mobile.cfa.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.staples.mobile.R;

/**
 * Quantity editor that presents as a spinner for values 0 thru maxSpinnerValue and as an EditText
 * for higher values. Provides easier UX for the more common lower numbered values.
 *
 * Created by sutdi001 on 10/21/14.
 */
public class QuantityEditor extends FrameLayout {

    public interface OnQtyChangeListener {
        public void onQtyChange(View view);
    }

    private static final String TAG = QuantityEditor.class.getSimpleName();

    public static final int DEFAULT_MAX_SPINNER_VALUE = 5;
    public static final float DEFAULT_TEXT_SIZE = 18;

    private Context context;
    private OnQtyChangeListener qtyChangeListener;
    private EditTextWithImeBackEvent editText;
    private Spinner spinner;
    private NumericSpinnerAdapter spinnerAdapter;
    private int maxSpinnerValue;
    private float textSize = DEFAULT_TEXT_SIZE;

    public QuantityEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {

        this.context = context;

        // inflate
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.quantity_editor, this);
        editText = (EditTextWithImeBackEvent)view.findViewById(R.id.cartitem_qty_edittext);
        spinner = (Spinner)view.findViewById(R.id.cartitem_qty_spinner);

        // this at least helps to select all of the text (e.g. on 2nd click), nothing seems to be foolproof
        editText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ((EditText)view).selectAll();
            }
        });

        // notify qty change listener when soft keyboard action completed
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (qtyChangeListener != null) {
                    qtyChangeListener.onQtyChange(QuantityEditor.this);
                }
                return false;
            }
        });

        // notify qty change listener when soft keyboard dismissed via back button
        editText.setOnImeBackListener(new EditTextWithImeBackEvent.EditTextImeBackListener() {
            @Override
            public void onImeBack(EditTextWithImeBackEvent view, String text) {
                if (qtyChangeListener != null) {
                    qtyChangeListener.onQtyChange(QuantityEditor.this);
                }
            }
        });


        // get attributes from layout if any
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.QtyEditor);
            // get max spinner value attribute from layout if it exists
            maxSpinnerValue = a.getInteger(R.styleable.QtyEditor_maxSpinnerValue, DEFAULT_MAX_SPINNER_VALUE);
            // retrieve text size and apply it to edit text, and to spinner dynamically on item selection
            int textSizePx = a.getDimensionPixelOffset(R.styleable.QtyEditor_android_textSize, 0);
            if (textSizePx != 0) {
                textSize = textSizePx / context.getResources().getDisplayMetrics().scaledDensity;
                editText.setTextSize(textSize);
            }
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
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "Parsing error: " + nfe);
            } catch (Exception e) {
                Log.e(TAG, "Parsing error: " + e);
            }
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
            spinner.setVisibility(View.GONE);
            editText.setVisibility(View.VISIBLE);
            editText.setText(strQty);
        }
    }

    /** sets spinner selection listener on the spinner widget */
    public void setOnQtyChangeListener(OnQtyChangeListener listener) {
        qtyChangeListener = listener;
    }


    public void hideSoftKeyboard() {
        if (isEditTextVisible()) {
            InputMethodManager keyboard = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
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

            // set text size
            ((TextView) parent.getChildAt(0)).setTextSize(textSize);

            // if selection out of range, call setQtyValue to revert to editText widget
            String value = ((TextView)view).getText().toString();
            if (value != null && value.endsWith("+")) {
                setQtyValue(maxSpinnerValue + 1);
            }

            // notify listener of item selection
            if (qtyChangeListener != null) {
                qtyChangeListener.onQtyChange(QuantityEditor.this);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            if (qtyChangeListener != null) {
                qtyChangeListener.onQtyChange(QuantityEditor.this);
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
